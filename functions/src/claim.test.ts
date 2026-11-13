import assert from "node:assert/strict";
import { test } from "node:test";
import { owesAcknowledgement, purchaseId, settle, standing } from "./claim";
import type { Purchase } from "./play";

/** A purchase of Premium as Play tells of one that went through, with whatever is changed. */
function bought(changed: Purchase = {}): Purchase {
  return {
    productLineItem: [{ productId: "premium" }],
    purchaseStateContext: { purchaseState: "PURCHASED" },
    acknowledgementState: "ACKNOWLEDGEMENT_STATE_PENDING",
    orderId: "GPA.0000-0000-0000-00000",
    ...changed,
  };
}

test("a token Play knows nothing of is invalid", () => {
  assert.equal(standing({}), "invalid");
});

test("a purchase of something else is invalid", () => {
  assert.equal(standing(bought({ productLineItem: [{ productId: "coins" }] })), "invalid");
});

test("a purchase of nothing at all is invalid", () => {
  assert.equal(standing(bought({ productLineItem: [] })), "invalid");
});

test("a cancelled purchase is invalid", () => {
  assert.equal(standing(bought({ purchaseStateContext: { purchaseState: "CANCELLED" } })), "invalid");
});

test("a purchase in no state Play ever sets is invalid", () => {
  assert.equal(standing(bought({ purchaseStateContext: { purchaseState: "PURCHASE_STATE_UNSPECIFIED" } })), "invalid");
  assert.equal(standing(bought({ purchaseStateContext: undefined })), "invalid");
});

test("a purchase not yet paid for is pending", () => {
  assert.equal(standing(bought({ purchaseStateContext: { purchaseState: "PENDING" } })), "pending");
});

test("a purchase that went through is purchased", () => {
  assert.equal(standing(bought()), "purchased");
});

test("and so is Premium bought along with something else", () => {
  assert.equal(standing(bought({ productLineItem: [{ productId: "coins" }, { productId: "premium" }] })), "purchased");
});

test("the first account to claim a purchase is bound to it and granted", () => {
  assert.deepEqual(settle("ana", undefined, false), { outcome: "granted", bind: true });
});

test("claiming again grants again and binds nothing anew", () => {
  assert.deepEqual(settle("ana", "ana", false), { outcome: "granted", bind: false });
});

test("a purchase bound to another account that is still there is taken", () => {
  assert.deepEqual(settle("eve", "ana", false), { outcome: "taken" });
});

test("a purchase bound to an account that is gone changes hands, and the old grant goes", () => {
  assert.deepEqual(settle("eve", "ana", true), { outcome: "granted", bind: true, strip: "ana" });
});

test("Play is told of a purchase it has not been told of", () => {
  assert.equal(owesAcknowledgement(bought()), true);
});

test("and of one whose telling it says nothing of", () => {
  assert.equal(owesAcknowledgement(bought({ acknowledgementState: undefined })), true);
});

test("but not of one it has been told of already", () => {
  assert.equal(owesAcknowledgement(bought({ acknowledgementState: "ACKNOWLEDGEMENT_STATE_ACKNOWLEDGED" })), false);
});

test("a purchase is named by the SHA-256 of its token, in hex, and never by the token", () => {
  assert.equal(purchaseId("abc"), "ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad");
});
