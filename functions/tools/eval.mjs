#!/usr/bin/env node
// The pilot of the proposed character sheets. For four characters it asks the AI service the
// same questions with the sheet the app uses and with the one the research proposes, as the app
// would ask them, and reports where the proposed one is no better.
//
//   ./gradlew :core:domain:test --tests '*PilotPromptsTest' --rerun   writes build/eval/prompts.json
//   node functions/tools/eval.mjs                                     asks, and writes build/eval/report.md
//   node functions/tools/eval.mjs --fake                              the same with no model, to check the tool
//   node --test functions/tools/eval.test.mjs                         that check, as a test
//
// Options: --rpm 12 (requests a minute), --judge 30 (how many answers of each character and
// sheet the model is then asked to put a name to; 0 for none), --model (when the app is told
// another from afar), --in and --out (where the prompts are read and the results written).
//
// The key of the AI service is read from GEMINI_API_KEY and from nowhere else. It travels in a
// header of each request and is never printed or written.
//
// A run can be stopped and started again: every answer is written to results.json as it comes,
// and what is already there is not asked for again.
import { createHash } from "node:crypto";
import { existsSync, mkdirSync, readFileSync, renameSync, writeFileSync } from "node:fs";
import { dirname, join, resolve } from "node:path";
import { setTimeout as sleep } from "node:timers/promises";
import { fileURLToPath, pathToFileURL } from "node:url";
import { parseArgs } from "node:util";

const ROOT = resolve(dirname(fileURLToPath(import.meta.url)), "../..");
const [CURRENT, PROPOSED] = ["current", "proposed"];

/** Something that ends the run and is said in one line: there is nothing in it to debug. */
class Stop extends Error {}

const digest = (text) => createHash("sha256").update(text).digest("hex");
const keyOf = (call) => `${call.character}/${call.sheet}/${call.question}/${call.repetition}`;

/** A text with the key struck out of it, should the key ever have found its way in. */
const struck = (text, key) => (key ? text.replaceAll(key, "[key]") : text);

// What the app holds an answer to and how it reads one, from GeminiRemoteDataSource.kt and
// FirebaseAiGeminiClient.kt. That code cannot be run from here: this is kept the same by hand.
const RETRY = "Line 1 MUST use 6 words or less.";
const MAX_WORDS = 6;
const MAX_LENGTH = 72;
const LINE_LABEL = /^(line|línea|linea)\s*[12]\s*[:.)-]\s*/i;
const WRAPPING = /^["'«»“”–•]+|["'«»“”–•]+$/g;

/** How many times a request that failed on the way is sent before the run gives up. */
const TRIES = 4;

/** A line without what a model sometimes wraps it in. Not asterisks: a character may act between them. */
export const tidied = (line) => line.trim().replace(LINE_LABEL, "").trim().replace(WRAPPING, "").trim();

/** The answer is the first line that says anything; what follows is the remark. */
export const firstLine = (written) =>
  written
    .split(/\r\n|\n|\r/)
    .map(tidied)
    .find((line) => line !== "") ?? "";

/** Six words at most, and no more letters than six words take. */
export const fits = (line) =>
  line.split(/[ \t\n\v\f\r]+/).filter((word) => word.trim() !== "").length <= MAX_WORDS && line.length <= MAX_LENGTH;

/** A reply in the app's terms: thoughts are no part of it, and one stopped for safety is no answer. */
export function replyOf(data) {
  const candidate = data.candidates?.[0];
  if (data.promptFeedback?.blockReason || candidate?.finishReason === "SAFETY") return { blocked: true, text: "" };
  const parts = candidate?.content?.parts ?? [];
  const written = parts.filter((part) => typeof part.text === "string" && !part.thought);
  return { blocked: false, text: written.map((part) => part.text).join("") };
}

/** The request the app's would be, were it sent without Firebase in between. */
export function geminiRequest(model, key, { system, user, maxOutputTokens }) {
  return {
    url: `https://generativelanguage.googleapis.com/v1beta/models/${model}:generateContent`,
    init: {
      method: "POST",
      // In a header and not in the address: an address is what errors and logs repeat.
      headers: { "content-type": "application/json", "x-goog-api-key": key },
      body: JSON.stringify({
        ...(system ? { systemInstruction: { parts: [{ text: system }] } } : {}),
        contents: [{ role: "user", parts: [{ text: user }] }],
        // Nothing about sampling: the app leaves it to the model, and so does this.
        generationConfig: { maxOutputTokens },
      }),
    },
  };
}

/** Asks the service, no faster than it allows. What cannot be mended by asking again ends the run. */
export function gemini({ model, key, maxOutputTokens, rpm }) {
  let next = 0;
  return async (asked) => {
    const { url, init } = geminiRequest(model, key, { ...asked, maxOutputTokens });
    for (let attempt = 1; ; attempt++) {
      const wait = next - Date.now();
      next = Math.max(next, Date.now()) + 60_000 / rpm;
      if (wait > 0) await sleep(wait);
      const started = performance.now();
      let failure;
      try {
        const response = await fetch(url, { ...init, signal: AbortSignal.timeout(60_000) });
        if (response.ok) return { data: await response.json(), ms: Math.round(performance.now() - started) };
        failure = `HTTP ${response.status}: ${(await response.text()).replace(/\s+/g, " ").slice(0, 300)}`;
        // A wrong key, a wrong model, a request it cannot read: the next one would fare the same.
        if (response.status < 500 && response.status !== 429) attempt = TRIES;
      } catch (error) {
        failure = String(error.cause?.code ?? error.message);
      }
      // What the service sends back may repeat what it was sent: the key is not passed on with it.
      if (attempt >= TRIES) throw new Stop(`The AI service did not answer (${struck(failure, key)}). Run again to go on from here.`);
      await sleep(attempt * 5_000);
    }
  };
}

/**
 * In place of the service when there is none to ask: a reply that depends on nothing but what was
 * asked, and takes every shape a real one can. It says nothing about any sheet.
 */
export function fakeModel({ system = "", user }) {
  const hash = createHash("sha256").update(`${system}\n${user}`).digest();
  const ms = 300 + (hash.readUInt16BE(2) % 900);
  const said = (text) => ({
    ms,
    data: { candidates: [{ content: { parts: [{ text: "Thinking.", thought: true }, { text }] }, finishReason: "STOP" }] },
  });
  // With no instruction it is being asked who said a line: it guesses among the names it is given.
  if (!system) return said(String(1 + (hash[0] % (user.match(/^\d+\. /gm)?.length ?? 1))));
  const shown = system.match(/or its idea: (.+?) \| /)?.[1];
  const roll = hash[0] % 16;
  if (roll === 0) return { ms, data: { promptFeedback: { blockReason: "SAFETY" } } };
  if (roll === 1) return { ms, data: { candidates: [{ content: { parts: [] }, finishReason: "MAX_TOKENS" }] } };
  if (roll <= 3) return said("Esta respuesta tiene bastante más de seis palabras.\nY sigue.");
  if (roll === 4 && shown) return said(shown);
  return said(`Línea 1: «Respuesta ${hash[1] % 12}.»\nUn comentario cualquiera.`);
}

/** One answer, asked for as the app asks: once, and once more when its first line is too long. */
async function answerTo(call, question, maxRetries, ask) {
  const attempts = [];
  for (let attempt = 1; ; attempt++) {
    const system = attempt === 1 ? call.system : `${call.system} ${RETRY}`;
    const { data, ms } = await ask({ system, user: question });
    const reply = replyOf(data);
    const text = firstLine(reply.text);
    attempts.push({ said: reply.text, ms, usage: data.usageMetadata });
    const outcome = reply.blocked ? "blocked" : text === "" ? "empty" : fits(text) ? "ok" : attempt > maxRetries ? "too long" : null;
    if (outcome) return { verdict: call.verdict, outcome, text, attempts };
  }
}

/**
 * The answers the model is asked to put a name to: the same questions and repetitions for both
 * sheets, picked by a digest so that a run started again picks the same ones.
 */
function toJudge(prompts, perCharacter) {
  const cellOf = (call) => `${call.character}/${call.question}/${call.repetition}`;
  const picked = new Set(
    prompts.characters.flatMap(({ id }) =>
      [...new Set(prompts.calls.filter((call) => call.character === id).map(cellOf))]
        .sort((a, b) => digest(a).localeCompare(digest(b)))
        .slice(0, perCharacter),
    ),
  );
  return prompts.calls.filter((call) => picked.has(cellOf(call)));
}

const judgeQuestion = (text, names) =>
  "Una bola 8 mágica contesta a preguntas de sí o no con la voz de un personaje." +
  ` Uno de estos ${names.length} dijo:\n\n«${text}»\n\n¿Cuál fue?\n` +
  names.map((name, index) => `${index + 1}. ${name}`).join("\n") +
  "\n\nResponde solo con el número.";

export async function run({ fake, rpm, judge, model, input, output, key, log }) {
  if (!existsSync(input)) {
    throw new Stop(`No ${input}: write it with ./gradlew :core:domain:test --tests '*PilotPromptsTest' --rerun`);
  }
  const read = readFileSync(input, "utf8");
  const prompts = JSON.parse(read);
  model ??= prompts.model;
  // What the stand-in says is kept apart: it is never taken for an answer of the service.
  const suffix = fake ? ".fake" : "";
  const paths = { results: join(output, `results${suffix}.json`), report: join(output, `report${suffix}.md`) };
  let results = { prompts: digest(read), model, fake, answers: {}, judged: {} };
  if (existsSync(paths.results)) {
    const held = JSON.parse(readFileSync(paths.results, "utf8"));
    if (held.prompts !== results.prompts || held.model !== model) {
      throw new Stop(`${paths.results} holds answers to other prompts or from another model: move it away and run again.`);
    }
    results = held;
  }
  mkdirSync(output, { recursive: true });
  // Written whole and then put in place: a run stopped in the middle of writing leaves the last one.
  const save = () => {
    writeFileSync(`${paths.results}.tmp`, JSON.stringify(results, null, 1));
    renameSync(`${paths.results}.tmp`, paths.results);
  };
  const ask = fake ? fakeModel : gemini({ model, key, maxOutputTokens: prompts.maxOutputTokens, rpm });
  const questions = new Map(prompts.questions.map((question) => [question.id, question.text]));
  const names = new Map(prompts.characters.map((character) => [character.id, character.name]));
  const count = { asked: 0, judged: 0 };

  try {
    for (const call of prompts.calls) {
      const id = keyOf(call);
      if (results.answers[id]) continue;
      const answer = (results.answers[id] = await answerTo(call, questions.get(call.question), prompts.maxRetries, ask));
      save();
      log(`${++count.asked} ${id} ${answer.outcome}: ${answer.text}`);
    }
    for (const call of toJudge(prompts, judge)) {
      const id = keyOf(call);
      if (results.judged[id] || results.answers[id].outcome !== "ok") continue;
      // Turned by the digest: whoever said it is not always at the same place in the list.
      const turn = parseInt(digest(id).slice(0, 8), 16) % names.size;
      const options = [...names.keys()].slice(turn).concat([...names.keys()].slice(0, turn));
      const { data, ms } = await ask({ user: judgeQuestion(results.answers[id].text, options.map((option) => names.get(option))) });
      const said = replyOf(data).text.trim();
      const guess = options[Number(said.match(/\d/)?.[0]) - 1] ?? null;
      results.judged[id] = { options, said, guess, ms };
      save();
      log(`${++count.judged} ${id} taken for ${guess}`);
    }
  } finally {
    // Whatever there is, said to be incomplete when it is: a run that stopped still shows where it got to.
    writeFileSync(paths.report, reportOf(prompts, results));
  }
  const held = [Object.keys(results.answers).length - count.asked, Object.keys(results.judged).length - count.judged];
  return `Asked for ${count.asked} answers (${held[0]} already held) and ${count.judged} names (${held[1]} already held). Report: ${paths.report}`;
}

const plain = (text) => text.toLowerCase().replace(/\s+/g, " ").trim();
const rank = (sorted, share) => (sorted.length ? sorted[Math.max(0, Math.ceil(share * sorted.length) - 1)] : null);
const sorted = (numbers) => [...numbers].sort((a, b) => a - b);

/** What is counted of some calls: each comes with what was answered and guessed, when anything was. */
export function metricsOf(rows) {
  const answers = rows.filter((row) => row.answer);
  const ok = answers.filter((row) => row.answer.outcome === "ok");
  const judged = rows.filter((row) => row.judged);
  const waits = sorted(answers.map((row) => row.answer.attempts.reduce((sum, attempt) => sum + attempt.ms, 0)));
  const having = (outcome) => answers.filter((row) => row.answer.outcome === outcome).length;
  return {
    asked: answers.length,
    ok: ok.length,
    distinct: new Set(ok.map((row) => `${row.call.character}/${plain(row.answer.text)}`)).size,
    copied: ok.filter((row) => row.lines.has(plain(row.answer.text))).length,
    retried: answers.filter((row) => row.answer.attempts.length > 1).length,
    tooLong: having("too long"),
    empty: having("empty"),
    blocked: having("blocked"),
    median: rank(waits, 0.5),
    p90: rank(waits, 0.9),
    judged: judged.length,
    guessed: judged.filter((row) => row.judged.guess === row.call.character).length,
    unread: judged.filter((row) => row.judged.guess === null).length,
    promptLength: rank(sorted(rows.map((row) => row.call.system.length)), 0.5),
  };
}

// What the two sheets are compared on. A count out of a total is a share; `better` is the way
// the proposed sheet has to move for it to be an improvement.
const MEASURES = [
  { name: "respuestas distintas", of: (m) => [m.distinct, m.ok], better: 1 },
  { name: "copias literales de un ejemplo", of: (m) => [m.copied, m.ok], better: -1 },
  { name: "reintentos por longitud", of: (m) => [m.retried, m.asked], better: -1 },
  { name: "longitud aún rota tras el reintento", of: (m) => [m.tooLong, m.asked], better: -1 },
  { name: "vacías o bloqueadas", of: (m) => [m.empty + m.blocked, m.asked], better: -1 },
  { name: "atribución a ciegas", of: (m) => [m.guessed, m.judged], better: 1 },
  { name: "latencia mediana", of: (m) => m.median, better: -1, unit: " ms", slack: 0.1 },
  { name: "latencia p90", of: (m) => m.p90, better: -1, unit: " ms", slack: 0.1 },
  { name: "largo del prompt", of: (m) => m.promptLength, better: -1, unit: " caracteres", slack: 0 },
];

const share = ([count, of]) => (of ? `${Math.round((100 * count) / of)} %` : "–");

/**
 * Whether the proposed sheet is better on one measure, and both figures as they are read. Two
 * shares closer than twice their standard error are not told apart: with ninety answers that is
 * as much as fifteen points, and calling the smaller gaps a gain would be reading noise.
 */
export function compare(measure, current, proposed) {
  const [a, b] = [measure.of(current), measure.of(proposed)];
  const shares = Array.isArray(a);
  const figures = shares ? `${share(b)} frente a ${share(a)}` : `${b}${measure.unit} frente a ${a}${measure.unit}`;
  if (shares ? !a[1] || !b[1] : a === null || b === null) return { verdict: "sin datos", figures };
  const [p, q] = shares ? [a[0] / a[1], b[0] / b[1]] : [a, b];
  const noise = shares ? 2 * Math.sqrt((p * (1 - p)) / a[1] + (q * (1 - q)) / b[1]) : measure.slack * p;
  if (Math.abs(q - p) <= noise) return { verdict: "sin diferencia clara", figures };
  return { verdict: Math.sign(q - p) === measure.better ? "mejor" : "peor", figures };
}

export function reportOf(prompts, results) {
  const lines = new Map(
    prompts.characters.flatMap((character) =>
      [CURRENT, PROPOSED].map((sheet) => [`${character.id}/${sheet}`, new Set(character.sheets[sheet].examples.map(plain))]),
    ),
  );
  const rows = prompts.calls.map((call) => ({
    call,
    answer: results.answers[keyOf(call)],
    judged: results.judged[keyOf(call)],
    lines: lines.get(`${call.character}/${call.sheet}`),
  }));
  const scopes = [
    { name: "Los cuatro juntos", rows },
    ...prompts.characters.map((character) => ({ name: character.name, rows: rows.filter((row) => row.call.character === character.id) })),
  ].map((scope) => ({
    name: scope.name,
    [CURRENT]: metricsOf(scope.rows.filter((row) => row.call.sheet === CURRENT)),
    [PROPOSED]: metricsOf(scope.rows.filter((row) => row.call.sheet === PROPOSED)),
  }));
  const compared = scopes.map((scope) => ({
    name: scope.name,
    measures: MEASURES.map((measure) => ({ name: measure.name, ...compare(measure, scope[CURRENT], scope[PROPOSED]) })),
  }));
  const all = compared.flatMap((scope) => scope.measures);
  const tally = (verdict) => all.filter((measure) => measure.verdict === verdict).length;
  const answered = Object.keys(results.answers).length;
  const called = { [CURRENT]: "actual", [PROPOSED]: "propuesta" };
  const table = (head, body) => [`| ${head.join(" | ")} |`, `|${head.map(() => "---").join("|")}|`, ...body.map((cells) => `| ${cells.join(" | ")} |`)];
  const bySheet = (cells) => scopes.flatMap((scope) => [CURRENT, PROPOSED].map((sheet) => [scope.name, called[sheet], ...cells(scope[sheet])]));

  return [
    "# Piloto de fichas de personaje: la actual frente a la propuesta",
    "",
    ...(results.fake
      ? ["> **Informe de prueba.** Las respuestas son de un sustituto del modelo (`--fake`): comprueba la herramienta y no dice nada de las fichas.", ""]
      : []),
    `Modelo \`${results.model}\`. ${answered} de ${prompts.calls.length} respuestas pedidas` +
      (answered < prompts.calls.length ? ": **incompleto**, vuelve a ejecutarlo para seguir donde quedó." : ".") +
      ` ${Object.keys(results.judged).length} respuestas atribuidas a ciegas.`,
    "",
    "## Dónde la ficha propuesta no es mejor",
    "",
    `De ${all.length} comparaciones, la propuesta es mejor en ${tally("mejor")}, peor en ${tally("peor")},` +
      ` no se distingue de la actual en ${tally("sin diferencia clara")} y no hay datos en ${tally("sin datos")}.` +
      " Cada cifra es la de la propuesta frente a la de la actual.",
    "",
    ...compared.map((scope) => {
      const not = scope.measures.filter((measure) => measure.verdict !== "mejor");
      const said = not.map((measure) => `${measure.name}, ${measure.figures} (${measure.verdict})`).join("; ");
      return `- **${scope.name}**: ${not.length ? said : "mejor en todo lo medido"}.`;
    }),
    "",
    "## Medidas",
    "",
    ...table(
      ["Personaje", "Ficha", "Pedidas", "Con respuesta", "Distintas", "Copias de ejemplo", "Reintentos", "Rota tras reintento", "Vacías", "Bloqueadas", "Latencia mediana", "Latencia p90", "Prompt"],
      bySheet((m) => [
        m.asked,
        m.ok,
        share([m.distinct, m.ok]),
        share([m.copied, m.ok]),
        share([m.retried, m.asked]),
        m.tooLong,
        m.empty,
        m.blocked,
        m.median === null ? "–" : `${m.median} ms`,
        m.p90 === null ? "–" : `${m.p90} ms`,
        m.promptLength,
      ]),
    ),
    "",
    "## Atribución a ciegas",
    "",
    `Al mismo modelo, sin ficha, se le da una respuesta y los nombres de los ${prompts.characters.length} personajes, y dice cuál la dijo.` +
      ` Al azar acertaría el ${Math.round(100 / prompts.characters.length)} %.`,
    "",
    ...table(
      ["Personaje", "Ficha", "Juzgadas", "Acertadas", "Sin nombre legible"],
      bySheet((m) => [m.judged, share([m.guessed, m.judged]), m.unread]),
    ),
    "",
    "## Cómo se ha contado",
    "",
    "- **Con respuesta**: la primera línea cumple lo que la app exige (6 palabras y 72 caracteres como mucho), a la primera o tras el único reintento que hace la app.",
    "- **Distintas** y **copias de ejemplo**: sobre las que tienen respuesta, sin mirar mayúsculas ni espacios. Copia es la que coincide entera con una línea de ejemplo de esa ficha.",
    "- **Reintentos**: la primera línea rompió la regla de longitud y se pidió otra vez, como hace la app. **Rota tras reintento**: tampoco cumplió entonces, y la app no la mostraría.",
    "- **Vacías** y **bloqueadas**: el modelo no escribió nada, o se paró por seguridad. La app no reintenta ninguna de las dos.",
    "- **Latencia**: lo que tarda una respuesta contando su reintento, sin la espera entre peticiones. **Prompt**: mediana de caracteres de la instrucción.",
    "- **Mejor**, **peor** o **sin diferencia clara**: dos porcentajes no se distinguen si difieren menos del doble de su error estándar; las latencias, si difieren menos de un 10 %; el largo del prompt se compara tal cual.",
    "",
  ].join("\n");
}

async function main() {
  const key = process.env.GEMINI_API_KEY ?? "";
  // The key is in no message this writes; were it ever in one that reaches here, it is struck out.
  const hushed = (text) => struck(text, key);
  try {
    const { values } = parseArgs({
      options: {
        fake: { type: "boolean", default: false },
        rpm: { type: "string", default: "12" },
        judge: { type: "string", default: "30" },
        model: { type: "string" },
        in: { type: "string", default: join(ROOT, "build/eval/prompts.json") },
        out: { type: "string", default: join(ROOT, "build/eval") },
      },
    });
    if (!values.fake && !key) throw new Stop("GEMINI_API_KEY is not set: put the key of the AI service in it, or pass --fake to run with no model.");
    const [rpm, judge] = [Number(values.rpm), Number(values.judge)];
    if (!(rpm > 0) || !(judge >= 0)) throw new Stop("--rpm takes a number above 0, and --judge one from 0 up.");
    const done = await run({
      fake: values.fake,
      rpm,
      judge,
      model: values.model,
      input: resolve(values.in),
      output: resolve(values.out),
      key,
      // The stand-in answers faster than its lines can be read.
      log: values.fake ? () => {} : (line) => console.log(hushed(line)),
    });
    console.log(done);
  } catch (error) {
    console.error(hushed(error instanceof Stop || error.code?.startsWith("ERR_PARSE_ARGS") ? error.message : String(error.stack)));
    process.exitCode = 1;
  }
}

if (process.argv[1] && import.meta.url === pathToFileURL(process.argv[1]).href) await main();
