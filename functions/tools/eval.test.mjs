// The pilot's tool checked with no model and no network: how it reads an answer, what it
// counts, what it sends, and a whole run stopped and started again.
//
//   node --test functions/tools/eval.test.mjs
import assert from "node:assert/strict";
import { spawnSync } from "node:child_process";
import { existsSync, mkdtempSync, readFileSync, writeFileSync } from "node:fs";
import { tmpdir } from "node:os";
import { join } from "node:path";
import { test } from "node:test";
import { fileURLToPath } from "node:url";
import { compare, firstLine, fits, gemini, geminiRequest, metricsOf, replyOf, reportOf } from "./eval.mjs";

const SCRIPT = fileURLToPath(new URL("./eval.mjs", import.meta.url));
const SHEETS = ["current", "proposed"];

/** Prompts of the shape the domain module writes, small enough to read: 2 characters, 4 questions, 3 times. */
function smallPrompts() {
  const characters = [
    ["PIRATE", "Pirata"],
    ["COACH", "Entrenador"],
  ].map(([id, name]) => ({
    id,
    name,
    sheets: Object.fromEntries(SHEETS.map((sheet) => [sheet, { system: "", examples: [`Sí de ${name} ${sheet}.`, `No de ${name} ${sheet}.`] }])),
  }));
  const questions = ["¿Lloverá?", "¿Me llaman?", "¿Gano?", "¿Qué hora es?"].map((text, index) => ({ id: `q0${index + 1}`, kind: "silly", text }));
  const calls = characters.flatMap((character) =>
    questions.flatMap((question) =>
      [1, 2, 3].flatMap((repetition) =>
        SHEETS.map((sheet) => ({
          character: character.id,
          sheet,
          question: question.id,
          repetition,
          verdict: "YES",
          system: `The ${sheet} sheet of ${character.id}, draw ${repetition}. Never use one of them or its idea: ${character.sheets[sheet].examples.join(" | ")}. Respond in Spanish.`,
        })),
      ),
    ),
  );
  return { language: "es", model: "no-model", maxOutputTokens: 256, maxRetries: 1, repetitions: 3, characters, questions, calls };
}

/** The tool as it is run, with nothing of this shell's environment but where to find node: no key can leak in. */
const cli = (args, env = {}) => spawnSync(process.execPath, [SCRIPT, ...args], { encoding: "utf8", env: { PATH: process.env.PATH, ...env } });

test("an answer is read and held to its length as the app does", () => {
  assert.equal(firstLine("\n Línea 1: «¡Zarpa ya!» \nY mi parte, grumete."), "¡Zarpa ya!");
  assert.equal(firstLine('"No."\r\nLine 2: more'), "No.");
  // What a character does is written between asterisks, and stays.
  assert.equal(firstLine("*se estira* Sí. Ahora, atún."), "*se estira* Sí. Ahora, atún.");
  assert.equal(firstLine(" \n "), "");

  assert.ok(fits("uno dos tres cuatro cinco seis"));
  assert.ok(!fits("uno dos tres cuatro cinco seis siete"));
  assert.ok(fits("x".repeat(72)) && !fits("x".repeat(73)));
});

test("a reply is read as the app reads it: no thoughts, and one stopped for safety is no answer", () => {
  const written = { candidates: [{ content: { parts: [{ text: "Hm.", thought: true }, { text: "Sí, " }, { text: "claro." }] }, finishReason: "STOP" }] };
  assert.deepEqual(replyOf(written), { blocked: false, text: "Sí, claro." });
  assert.deepEqual(replyOf({ candidates: [{ content: { parts: [{ text: "No" }] }, finishReason: "SAFETY" }] }), { blocked: true, text: "" });
  assert.deepEqual(replyOf({ promptFeedback: { blockReason: "OTHER" } }), { blocked: true, text: "" });
  assert.deepEqual(replyOf({ candidates: [{ finishReason: "MAX_TOKENS" }] }), { blocked: false, text: "" });
});

test("a request carries the key in a header only, and a refusal ends the run without repeating it", async (t) => {
  const key = "not-a-key-only-a-marker";
  const { url, init } = geminiRequest("some-model", key, { system: "Who you are.", user: "¿Lloverá?", maxOutputTokens: 256 });
  assert.ok(url.endsWith("/models/some-model:generateContent") && !url.includes(key) && !init.body.includes(key));
  assert.equal(init.headers["x-goog-api-key"], key);
  // What the app sends and no more: the instruction, the question and the one limit it sets.
  assert.deepEqual(JSON.parse(init.body), {
    systemInstruction: { parts: [{ text: "Who you are." }] },
    contents: [{ role: "user", parts: [{ text: "¿Lloverá?" }] }],
    generationConfig: { maxOutputTokens: 256 },
  });
  // Asked who said a line, the model is given no sheet at all.
  assert.ok(!("systemInstruction" in JSON.parse(geminiRequest("some-model", key, { user: "¿Quién?", maxOutputTokens: 256 }).init.body)));

  const sent = [];
  t.mock.method(globalThis, "fetch", async (address, request) => {
    sent.push({ address, request });
    return sent.length === 1
      ? new Response(JSON.stringify({ candidates: [{ content: { parts: [{ text: "Sí." }] } }] }))
      : new Response(JSON.stringify({ error: { message: `No such model for ${key}` } }), { status: 404 });
  });
  const ask = gemini({ model: "some-model", key, maxOutputTokens: 256, rpm: 60_000 });
  assert.equal(replyOf((await ask({ system: "Who you are.", user: "¿Lloverá?" })).data).text, "Sí.");
  // The refusal quotes the key back, and what the run stops with does not.
  await assert.rejects(ask({ user: "¿Lloverá?" }), (stopped) => stopped.message.includes("HTTP 404") && stopped.message.includes("No such model for [key]") && !stopped.message.includes(key));
  // Not sent a second time: the same request would be refused the same way.
  assert.equal(sent.length, 2);
  assert.ok(sent.every(({ address, request }) => !String(address).includes(key) && request.headers["x-goog-api-key"] === key));
});

test("what is counted of a sheet's answers", () => {
  const row = (text, outcome, waits, guess) => ({
    call: { character: "PIRATE", system: "x".repeat(waits[0]) },
    lines: new Set(["¡sí! tú pagas el ron."]),
    answer: { outcome, text, attempts: waits.map((ms) => ({ ms })) },
    judged: guess === undefined ? undefined : { guess },
  });
  const counted = metricsOf([
    row("¡Zarpa ya!", "ok", [100], "PIRATE"),
    // The same answer but for its capitals and spaces.
    row("¡zarpa  ya!", "ok", [200], "COACH"),
    // One of the lines it was shown, word for word, and only on the second try.
    row("¡Sí! Tú pagas el ron.", "ok", [300, 400], null),
    row("", "empty", [500]),
    row("", "blocked", [600]),
    row("una dos tres cuatro cinco seis siete", "too long", [700, 800]),
    // Not asked yet: a run that stopped early counts what it has.
    { call: { character: "PIRATE", system: "x".repeat(900) }, lines: new Set() },
  ]);

  assert.deepEqual(counted, {
    asked: 6,
    ok: 3,
    distinct: 2,
    copied: 1,
    retried: 2,
    tooLong: 1,
    empty: 1,
    blocked: 1,
    median: 500,
    p90: 1500,
    judged: 3,
    guessed: 1,
    unread: 1,
    promptLength: 500,
  });
});

test("a gap smaller than its noise is not called a gain, and one that is worse is called worse", () => {
  const more = { name: "share", of: (counted) => counted, better: 1 };
  const less = { ...more, better: -1 };
  assert.equal(compare(more, [45, 90], [50, 90]).verdict, "sin diferencia clara");
  assert.deepEqual(compare(more, [45, 90], [72, 90]), { verdict: "mejor", figures: "80 % frente a 50 %" });
  assert.equal(compare(less, [45, 90], [72, 90]).verdict, "peor");
  assert.equal(compare(more, [0, 0], [72, 90]).verdict, "sin datos");

  const wait = { name: "wait", of: (counted) => counted, better: -1, unit: " ms", slack: 0.1 };
  assert.equal(compare(wait, 1000, 1050).verdict, "sin diferencia clara");
  assert.deepEqual(compare(wait, 1000, 1200), { verdict: "peor", figures: "1200 ms frente a 1000 ms" });
  assert.equal(compare(wait, null, 1200).verdict, "sin datos");
});

test("the report says in so many words where the proposed sheet is worse", () => {
  const prompts = smallPrompts();
  // The sheet in use never says the same thing twice; the proposed one says one thing only.
  const answers = Object.fromEntries(
    prompts.calls.map((call, index) => [
      `${call.character}/${call.sheet}/${call.question}/${call.repetition}`,
      { outcome: "ok", text: call.sheet === "current" ? `Respuesta ${index}.` : "Lo mismo.", attempts: [{ ms: 100 }] },
    ]),
  );
  const report = reportOf(prompts, { model: "no-model", fake: false, answers, judged: {} });

  assert.match(report, /^- \*\*Pirata\*\*: respuestas distintas, 8 % frente a 100 % \(peor\);/m);
  assert.match(report, /^- \*\*Los cuatro juntos\*\*: respuestas distintas, 8 % frente a 100 % \(peor\);.*atribución a ciegas, – frente a – \(sin datos\)/m);
  assert.match(report, /^\| Entrenador \| propuesta \| 12 \| 12 \| 8 % \|/m);
  assert.ok(!report.includes("Informe de prueba") && report.includes("48 de 48 respuestas pedidas."));
});

test("with no key it says so in one line and asks nothing", () => {
  const folder = mkdtempSync(join(tmpdir(), "eval-"));
  writeFileSync(join(folder, "prompts.json"), JSON.stringify(smallPrompts()));
  const ran = cli(["--in", join(folder, "prompts.json"), "--out", folder]);

  assert.equal(ran.status, 1);
  assert.match(ran.stderr, /^GEMINI_API_KEY is not set[^\n]*\n$/);
  assert.equal(ran.stdout, "");
  assert.ok(!existsSync(join(folder, "results.json")) && !existsSync(join(folder, "report.md")));
});

test("a whole run with no model, started again from where it stopped", () => {
  const folder = mkdtempSync(join(tmpdir(), "eval-"));
  const input = join(folder, "prompts.json");
  const prompts = smallPrompts();
  writeFileSync(input, JSON.stringify(prompts));
  const marker = "not-a-key-only-a-marker";
  const run = () => cli(["--fake", "--judge", "5", "--in", input, "--out", folder], { GEMINI_API_KEY: marker });
  const read = (name) => readFileSync(join(folder, name), "utf8");

  const first = run();
  assert.equal(first.status, 0, first.stderr);
  assert.match(first.stdout, /^Asked for 48 answers \(0 already held\) and (\d+) names \(0 already held\)\. Report: /);
  // Kept apart from what the service itself would say, so that neither is taken for the other.
  assert.ok(!existsSync(join(folder, "results.json")) && !existsSync(join(folder, "report.md")));

  const results = JSON.parse(read("results.fake.json"));
  const answers = Object.values(results.answers);
  assert.equal(answers.length, prompts.calls.length);
  for (const answer of answers) {
    // Each was asked as the app asks: again only when the first line was too long, and once.
    assert.ok(answer.attempts.length <= 2);
    assert.equal(answer.attempts.length === 2 || answer.outcome === "too long", !fits(firstLine(answer.attempts[0].said)) && firstLine(answer.attempts[0].said) !== "");
    assert.equal(answer.outcome === "ok", answer.text !== "" && fits(answer.text));
  }
  // The stand-in takes every shape a reply can: all of them came through.
  assert.deepEqual(new Set(answers.map((answer) => answer.outcome)), new Set(["ok", "too long", "empty", "blocked"]));
  const judged = Object.entries(results.judged);
  // Five questions-and-repetitions a character, with both sheets, but for the answers that are none.
  assert.ok(judged.length > 0 && judged.length <= 5 * 2 * 2);
  assert.ok(judged.every(([id, name]) => results.answers[id].outcome === "ok" && name.options.includes(name.guess)));

  const report = read("report.fake.md");
  assert.ok(report.includes("Informe de prueba") && report.includes("48 de 48 respuestas pedidas."));
  assert.ok(report.includes("## Dónde la ficha propuesta no es mejor") && report.includes("- **Los cuatro juntos**:"));
  assert.match(report, /^\| Pirata \| actual \| 12 \|/m);
  for (const written of [first.stdout, first.stderr, read("results.fake.json"), report]) assert.ok(!written.includes(marker));

  // Stopped part of the way: some answers and the names put to them never came.
  const lost = Object.keys(results.answers).filter((_, index) => index % 7 === 0);
  const partial = structuredClone(results);
  for (const id of lost) {
    delete partial.answers[id];
    delete partial.judged[id];
  }
  writeFileSync(join(folder, "results.fake.json"), JSON.stringify(partial));
  const again = run();
  assert.equal(again.status, 0, again.stderr);
  assert.match(again.stdout, new RegExp(`^Asked for ${lost.length} answers \\(${48 - lost.length} already held\\)`));
  assert.deepEqual(JSON.parse(read("results.fake.json")), results);
  assert.equal(read("report.fake.md"), report);

  // Other prompts are not answered by what was asked of these.
  writeFileSync(input, JSON.stringify({ ...prompts, repetitions: 4 }));
  const stale = run();
  assert.equal(stale.status, 1);
  assert.match(stale.stderr, /holds answers to other prompts or from another model[^\n]*\n$/);
});
