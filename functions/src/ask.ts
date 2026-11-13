// What asking the ball through the server comes to before anything is read or written: what may
// be asked, which day an answer belongs to, and whether that day has one left for the account.

/** What the app is told of an answer it asked for. A request that fails is told as an error. */
export type Answer =
  | { outcome: "written"; parts: string[] }
  // The model would not answer this, for the safety of who asks or of others.
  | { outcome: "blocked" }
  // The account has had its answers for the day.
  | { outcome: "spent" };

/** One request, as far as it is the app's to say. The model and the day's allowance are not. */
export interface Asked {
  instruction: string;
  question: string;
  maxOutputTokens: number;
}

// Longer than anything the app sends: what it is told of a character, an asker and an answer
// comes to a few thousand characters, and a question to two hundred.
const MAX_INSTRUCTION = 8_000;
const MAX_QUESTION = 300;
// An answer is a line or two. Whatever is asked for, no more than this is paid for.
const MAX_OUTPUT_TOKENS = 512;

/** The request, if it is one: anything else is refused before it costs anything. */
export function asked(data: unknown): Asked | undefined {
  const { instruction, question, maxOutputTokens } = (data ?? {}) as Record<string, unknown>;
  if (typeof instruction !== "string" || instruction.length === 0 || instruction.length > MAX_INSTRUCTION) return undefined;
  if (typeof question !== "string" || question.length === 0 || question.length > MAX_QUESTION) return undefined;
  if (typeof maxOutputTokens !== "number" || !Number.isInteger(maxOutputTokens) || maxOutputTokens < 1) return undefined;
  return { instruction, question, maxOutputTokens: Math.min(maxOutputTokens, MAX_OUTPUT_TOKENS) };
}

/** How many answers an account may have in a day, with Premium and without. */
export interface Limits {
  free: number;
  premium: number;
}

// The same the app falls back on: see GeminiSettings.
export const DEFAULT_LIMITS: Limits = { free: 20, premium: 40 };
export const DEFAULT_MODEL = "gemini-3.5-flash-lite";

/** A parameter of the project's Remote Config, as far as its value for everyone goes. */
export type Parameters = Record<string, { defaultValue?: { value?: string } } | undefined>;

function positive(parameters: Parameters | undefined, key: string): number | undefined {
  const value = Number(parameters?.[key]?.defaultValue?.value);
  return Number.isInteger(value) && value > 0 ? value : undefined;
}

/**
 * The limits as the project's Remote Config has them, under the names the app reads them by, so
 * that the two are set in one place. What is missing or is no count of answers is the default.
 */
export function limitsFrom(parameters: Parameters | undefined): Limits {
  return {
    free: positive(parameters, "gemini_daily_limit") ?? DEFAULT_LIMITS.free,
    premium: positive(parameters, "gemini_daily_limit_premium") ?? DEFAULT_LIMITS.premium,
  };
}

/** The model asked: the one the app is told to ask, or the one it would choose itself. */
export function modelFrom(parameters: Parameters | undefined): string {
  const named = parameters?.["gemini_model"]?.defaultValue?.value?.trim();
  return named ? named : DEFAULT_MODEL;
}

/** The day's allowance of an account: never less for having paid, whatever was set. */
export function allowance(limits: Limits, premium: boolean): number {
  return premium ? Math.max(limits.premium, limits.free) : limits.free;
}

const DAY = 24 * 60 * 60 * 1000;

function dateOf(millis: number): string {
  return new Date(millis).toISOString().slice(0, 10);
}

/**
 * The day an answer is counted in. The app says which day it is where the phone is, so that the
 * count starts again at its midnight and not at the server's; it is believed only if that day
 * is today somewhere in the world, and otherwise the server's own day stands.
 */
export function dayFor(claimed: unknown, now: number): string {
  const possible = [dateOf(now - DAY), dateOf(now), dateOf(now + DAY)];
  return typeof claimed === "string" && possible.includes(claimed) ? claimed : dateOf(now);
}

/** What is kept of an account's answers: the day last counted, and how many it has had in it. */
export interface Count {
  day?: string;
  used?: number;
}

/**
 * One more answer on a day, if the day has one left. A count never goes back to an earlier day:
 * a phone that says it is yesterday again, after having had today's, starts no new count.
 */
export function spend(count: Count | undefined, day: string, limit: number): { allowed: boolean; next: Count } {
  const kept = count?.day !== undefined && count.day >= day ? count.day : day;
  const used = count?.day === kept && Number.isInteger(count.used) && count.used! > 0 ? count.used! : 0;
  return used < limit ? { allowed: true, next: { day: kept, used: used + 1 } } : { allowed: false, next: { day: kept, used } };
}
