import assert from "node:assert/strict";
import { test } from "node:test";
import { read } from "./model";

test("what the model wrote is read in the pieces it came in, without its thoughts", () => {
  assert.deepEqual(
    read({
      candidates: [
        {
          finishReason: "STOP",
          content: { parts: [{ text: "Let me think.", thought: true }, { text: "Yes, " }, { text: "of course." }] },
        },
      ],
    }),
    { outcome: "written", parts: ["Yes, ", "of course."] },
  );
});

test("a question or an answer stopped for safety is told as turned down", () => {
  assert.deepEqual(read({ promptFeedback: { blockReason: "SAFETY" } }), { outcome: "blocked" });
  assert.deepEqual(read({ candidates: [{ finishReason: "SAFETY", content: { parts: [{ text: "No" }] } }] }), {
    outcome: "blocked",
  });
});

test("an answer cut short is still an answer", () => {
  assert.deepEqual(read({ candidates: [{ finishReason: "MAX_TOKENS", content: { parts: [{ text: "It is cer" }] } }] }), {
    outcome: "written",
    parts: ["It is cer"],
  });
});

test("nothing written is no answer", () => {
  for (const generated of [
    {},
    { candidates: [] },
    { candidates: [{ finishReason: "MAX_TOKENS" }] },
    { candidates: [{ content: { parts: [{ text: "  \n" }] } }] },
    { candidates: [{ content: { parts: [{ text: "Only a thought.", thought: true }] } }] },
  ]) {
    assert.equal(read(generated), undefined);
  }
});
