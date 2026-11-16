import assert from "node:assert/strict";
import { test } from "node:test";
import { lapsedBefore, owesAcknowledgement, purchaseId, settle, standing, subscribed } from "./claim";
import type { Purchase, Subscription } from "./play";

/** A purchase of Premium bought once, as Play tells of one that went through, with whatever is changed. */
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

const NOW = Date.UTC(2026, 10, 16);
const DAY = 24 * 60 * 60 * 1000;
const at = (moment: number) => new Date(moment).toISOString();

/** A subscription to Premium as Play tells of one that is paid for another month, with whatever is changed. */
function running(changed: Subscription = {}): Subscription {
  return {
    subscriptionState: "SUBSCRIPTION_STATE_ACTIVE",
    acknowledgementState: "ACKNOWLEDGEMENT_STATE_PENDING",
    lineItems: [
      { productId: "premium_subscription", expiryTime: at(NOW + 30 * DAY), latestSuccessfulOrderId: "GPA.0000-0000-0000-00000" },
    ],
    ...changed,
  };
}

const ends = (expiry: number, state: string) =>
  running({ subscriptionState: state, lineItems: [{ productId: "premium_subscription", expiryTime: at(expiry) }] });

test("a subscription Play knows nothing of is invalid", () => {
  assert.equal(subscribed({}, NOW), "invalid");
});

test("a subscription to something else is invalid", () => {
  assert.equal(subscribed(running({ lineItems: [{ productId: "premium", expiryTime: at(NOW + DAY) }] }), NOW), "invalid");
  assert.equal(subscribed(running({ lineItems: [] }), NOW), "invalid");
});

test("an active subscription gives Premium until Play says it is paid for, and names its latest order", () => {
  assert.deepEqual(subscribed(running(), NOW), { until: NOW + 30 * DAY, order: "GPA.0000-0000-0000-00000" });
});

test("one with no order yet names none", () => {
  assert.deepEqual(subscribed(ends(NOW + DAY, "SUBSCRIPTION_STATE_ACTIVE"), NOW), { until: NOW + DAY, order: null });
});

test("one whose payment failed keeps Premium while Play is still trying", () => {
  assert.deepEqual(subscribed(ends(NOW + 3 * DAY, "SUBSCRIPTION_STATE_IN_GRACE_PERIOD"), NOW), { until: NOW + 3 * DAY, order: null });
});

test("one Play still calls active past its end keeps Premium, as of now", () => {
  assert.deepEqual(subscribed(ends(NOW - 60_000, "SUBSCRIPTION_STATE_ACTIVE"), NOW), { until: NOW, order: null });
});

test("and so does one whose end Play does not say", () => {
  assert.deepEqual(subscribed(running({ lineItems: [{ productId: "premium_subscription" }] }), NOW), { until: NOW, order: null });
});

test("a cancelled subscription keeps Premium to the end of what was paid for, and no further", () => {
  assert.deepEqual(subscribed(ends(NOW + DAY, "SUBSCRIPTION_STATE_CANCELED"), NOW), { until: NOW + DAY, order: null });
  assert.equal(subscribed(ends(NOW, "SUBSCRIPTION_STATE_CANCELED"), NOW), "invalid");
  assert.equal(subscribed(running({ subscriptionState: "SUBSCRIPTION_STATE_CANCELED", lineItems: [{ productId: "premium_subscription" }] }), NOW), "invalid");
});

test("one on hold, paused, or expired, as a revoked or refunded one is, gives nothing", () => {
  for (const state of ["SUBSCRIPTION_STATE_ON_HOLD", "SUBSCRIPTION_STATE_PAUSED", "SUBSCRIPTION_STATE_EXPIRED"]) {
    assert.equal(subscribed(ends(NOW + DAY, state), NOW), "invalid", state);
  }
});

test("nor does one in no state Play ever sets, or whose first payment was given up on", () => {
  assert.equal(subscribed(running({ subscriptionState: "SUBSCRIPTION_STATE_UNSPECIFIED" }), NOW), "invalid");
  assert.equal(subscribed(running({ subscriptionState: "SUBSCRIPTION_STATE_PENDING_PURCHASE_CANCELED" }), NOW), "invalid");
  assert.equal(subscribed(running({ subscriptionState: undefined }), NOW), "invalid");
});

test("a subscription not yet paid for is pending", () => {
  assert.equal(subscribed(running({ subscriptionState: "SUBSCRIPTION_STATE_PENDING" }), NOW), "pending");
});

test("Play is told of a subscription it has not been told of, and not of one it has", () => {
  assert.equal(owesAcknowledgement(running()), true);
  assert.equal(owesAcknowledgement(running({ acknowledgementState: "ACKNOWLEDGEMENT_STATE_ACKNOWLEDGED" })), false);
});

test("a subscription Play was not heard from about is over three days past its end", () => {
  assert.equal(lapsedBefore(NOW), NOW - 3 * DAY);
});
