import assert from "node:assert/strict";
import { test } from "node:test";
import { STAFF, giving, takingBack } from "./staff";

test("Premium is given to an account that holds nothing", () => {
  assert.equal(giving(undefined), "given");
});

test("given twice, it is left as it was first given", () => {
  assert.equal(giving({ source: STAFF }), "already given");
});

test("a grant made for a purchase is never written over", () => {
  assert.equal(giving({}), "already bought");
  assert.equal(giving({ source: "anything else" }), "already bought");
});

test("only what was given is taken back", () => {
  assert.equal(takingBack({ source: STAFF }), "taken back");
  assert.equal(takingBack(undefined), "nothing to take");
  assert.equal(takingBack({}), "bought, left alone");
});
