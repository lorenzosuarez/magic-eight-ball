import assert from "node:assert/strict";
import { test } from "node:test";
import { DEFAULT_LIMITS, DEFAULT_MODEL, allowance, asked, dayFor, limitsFrom, modelFrom, spend } from "./ask";

const NOON = Date.UTC(2026, 10, 13, 12);

test("a request is an instruction, a question and a size, and nothing else is one", () => {
  assert.deepEqual(asked({ instruction: "Be brief.", question: "shake", maxOutputTokens: 60 }), {
    instruction: "Be brief.",
    question: "shake",
    maxOutputTokens: 60,
  });
  for (const data of [
    undefined,
    null,
    "shake",
    {},
    { instruction: "", question: "shake", maxOutputTokens: 60 },
    { instruction: "Be brief.", question: "", maxOutputTokens: 60 },
    { instruction: "Be brief.", question: 7, maxOutputTokens: 60 },
    { instruction: "Be brief.", question: "shake" },
    { instruction: "Be brief.", question: "shake", maxOutputTokens: 0 },
    { instruction: "Be brief.", question: "shake", maxOutputTokens: 1.5 },
    { instruction: "x".repeat(8_001), question: "shake", maxOutputTokens: 60 },
    { instruction: "Be brief.", question: "x".repeat(301), maxOutputTokens: 60 },
  ]) {
    assert.equal(asked(data), undefined);
  }
});

test("no more is written than an answer needs, whatever is asked for", () => {
  assert.equal(asked({ instruction: "Be brief.", question: "shake", maxOutputTokens: 100_000 })?.maxOutputTokens, 512);
});

test("the limits and the model are the project's, and the defaults where it says nothing usable", () => {
  assert.deepEqual(limitsFrom(undefined), DEFAULT_LIMITS);
  assert.deepEqual(
    limitsFrom({
      gemini_daily_limit: { defaultValue: { value: "15" } },
      gemini_daily_limit_premium: { defaultValue: { value: "60" } },
    }),
    { free: 15, premium: 60 },
  );
  assert.deepEqual(
    limitsFrom({
      gemini_daily_limit: { defaultValue: { value: "0" } },
      gemini_daily_limit_premium: { defaultValue: { value: "many" } },
    }),
    DEFAULT_LIMITS,
  );
  // A parameter left to the app's own default has no value here.
  assert.deepEqual(limitsFrom({ gemini_daily_limit: { defaultValue: {} } }), DEFAULT_LIMITS);

  assert.equal(modelFrom(undefined), DEFAULT_MODEL);
  assert.equal(modelFrom({ gemini_model: { defaultValue: { value: "  " } } }), DEFAULT_MODEL);
  assert.equal(modelFrom({ gemini_model: { defaultValue: { value: "gemini-x" } } }), "gemini-x");
});

test("paying never leaves an account with less", () => {
  assert.equal(allowance({ free: 20, premium: 40 }, false), 20);
  assert.equal(allowance({ free: 20, premium: 40 }, true), 40);
  assert.equal(allowance({ free: 20, premium: 5 }, true), 20);
});

test("the day is the phone's where that can be today, and the server's otherwise", () => {
  assert.equal(dayFor("2026-11-13", NOON), "2026-11-13");
  // Ahead of the server, or behind it: both are today somewhere.
  assert.equal(dayFor("2026-11-14", NOON), "2026-11-14");
  assert.equal(dayFor("2026-11-12", NOON), "2026-11-12");
  for (const claimed of ["2026-11-15", "2026-11-11", "2031-01-01", "", "today", undefined, 20261113]) {
    assert.equal(dayFor(claimed, NOON), "2026-11-13");
  }
});

test("an account has its answers for the day and no more", () => {
  let count = undefined as ReturnType<typeof spend>["next"] | undefined;
  const allowed = [];
  for (let asking = 0; asking < 4; asking++) {
    const spent = spend(count, "2026-11-13", 3);
    allowed.push(spent.allowed);
    count = spent.next;
  }
  assert.deepEqual(allowed, [true, true, true, false]);
  assert.deepEqual(count, { day: "2026-11-13", used: 3 });
});

test("a new day starts a new count, and going back a day does not", () => {
  const spentUp = { day: "2026-11-13", used: 3 };
  assert.deepEqual(spend(spentUp, "2026-11-14", 3), { allowed: true, next: { day: "2026-11-14", used: 1 } });
  // The phone says it is yesterday again: the count is still today's, and today is over.
  assert.deepEqual(spend(spentUp, "2026-11-12", 3), { allowed: false, next: spentUp });
});

test("a count that is no count is taken for none", () => {
  for (const kept of [{}, { day: "2026-11-13" }, { day: "2026-11-13", used: -4 }, { day: "2026-11-13", used: 1.5 }]) {
    assert.deepEqual(spend(kept, "2026-11-13", 3), { allowed: true, next: { day: "2026-11-13", used: 1 } });
  }
});

test("a limit that was lowered during the day stops an account that is already past it", () => {
  assert.deepEqual(spend({ day: "2026-11-13", used: 30 }, "2026-11-13", 20), {
    allowed: false,
    next: { day: "2026-11-13", used: 30 },
  });
});
