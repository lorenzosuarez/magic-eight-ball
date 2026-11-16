import assert from "node:assert/strict";
import { test } from "node:test";
import { type Play, type VoidedPage, notified, voidedFrom, voidedSince } from "./play";

/**
 * A Play that answers with these pages and remembers what it was asked. A page is asked for by
 * its place among them, which is what each one gives as the token of the next.
 */
function fake(pages: VoidedPage[]) {
  const asked: Array<[number | undefined, string | undefined]> = [];
  const play: Play = {
    purchase: async () => ({}),
    subscription: async () => ({}),
    acknowledge: async () => {},
    voided: async (since, page) => {
      asked.push([since, page]);
      return pages[Number(page ?? 0)];
    },
  };
  return { play, asked };
}

async function tokens(play: Play, since?: number): Promise<string[]> {
  const all: string[] = [];
  for await (const token of voidedSince(play, since)) all.push(token);
  return all;
}

const HOUR = 60 * 60 * 1000;
const DAY = 24 * HOUR;
const NOW = Date.UTC(2026, 10, 13);

test("a single page is read and no other is asked for", async () => {
  const { play, asked } = fake([{ voidedPurchases: [{ purchaseToken: "a" }, { purchaseToken: "b" }] }]);
  assert.deepEqual(await tokens(play, 5), ["a", "b"]);
  assert.deepEqual(asked, [[5, undefined]]);
});

test("every page is read, each asked for by the token the one before gave", async () => {
  const { play, asked } = fake([
    { voidedPurchases: [{ purchaseToken: "a" }], tokenPagination: { nextPageToken: "1" } },
    { voidedPurchases: [{ purchaseToken: "b" }], tokenPagination: { nextPageToken: "2" } },
    { voidedPurchases: [{ purchaseToken: "c" }], tokenPagination: { nextPageToken: null } },
  ]);
  assert.deepEqual(await tokens(play, 5), ["a", "b", "c"]);
  assert.deepEqual(asked, [[5, undefined], [5, "1"], [5, "2"]]);
});

test("a page with nothing on it does not end the reading while another follows", async () => {
  const { play } = fake([
    { tokenPagination: { nextPageToken: "1" } },
    { voidedPurchases: [], tokenPagination: { nextPageToken: "2" } },
    { voidedPurchases: [{ purchaseToken: "a" }] },
  ]);
  assert.deepEqual(await tokens(play), ["a"]);
});

test("nothing taken back is nothing read", async () => {
  const { play, asked } = fake([{}]);
  assert.deepEqual(await tokens(play), []);
  assert.deepEqual(asked, [[undefined, undefined]]);
});

test("one that comes without its token is passed over", async () => {
  const { play } = fake([{ voidedPurchases: [{}, { purchaseToken: "a" }] }]);
  assert.deepEqual(await tokens(play), ["a"]);
});

test("Play out of reach half way through is not taken for the end", async () => {
  const { play } = fake([{ voidedPurchases: [{ purchaseToken: "a" }], tokenPagination: { nextPageToken: "1" } }]);
  play.voided = ((voided) => async (since, page) => {
    if (page !== undefined) throw new Error("Play answered 503");
    return voided(since, page);
  })(play.voided);
  await assert.rejects(tokens(play), /503/);
});

test("asked for the first time, Play starts as far back as it goes", () => {
  assert.equal(voidedFrom(undefined, NOW), undefined);
});

test("asked the day before, it starts an hour before that", () => {
  assert.equal(voidedFrom(NOW - DAY, NOW), NOW - DAY - HOUR);
});

test("last asked further back than it keeps, it starts as far back as it goes", () => {
  assert.equal(voidedFrom(NOW - 29 * DAY, NOW), undefined);
  assert.equal(voidedFrom(NOW - 90 * DAY, NOW), undefined);
});

test("last asked just within what it keeps, it starts from there", () => {
  assert.equal(voidedFrom(NOW - 28 * DAY, NOW), NOW - 28 * DAY - HOUR);
});

const PACKAGE = "com.lorenzosuarez.magiceightball";

test("a notification about a subscription gives its token, whatever it says has happened", () => {
  for (const notificationType of [1, 2, 3, 4, 5, 6, 7, 10, 12, 13, 99]) {
    const message = { version: "1.0", packageName: PACKAGE, subscriptionNotification: { version: "1.0", notificationType, purchaseToken: "a" } };
    assert.equal(notified(message), "a");
  }
});

test("so does one about a subscription Play took back", () => {
  const voidedPurchaseNotification = { purchaseToken: "a", orderId: "GPA.1", productType: 1, refundType: 1 };
  assert.equal(notified({ packageName: PACKAGE, voidedPurchaseNotification }), "a");
});

test("one about a purchase bought once gives none: those are read from Play's list", () => {
  const voidedPurchaseNotification = { purchaseToken: "a", orderId: "GPA.1", productType: 2, refundType: 1 };
  assert.equal(notified({ packageName: PACKAGE, voidedPurchaseNotification }), undefined);
  assert.equal(notified({ packageName: PACKAGE, oneTimeProductNotification: { purchaseToken: "a", sku: "premium" } }), undefined);
});

test("nor does the test message Play Console sends, or one about another app", () => {
  assert.equal(notified({ packageName: PACKAGE, testNotification: { version: "1.0" } }), undefined);
  assert.equal(notified({ packageName: "com.example", subscriptionNotification: { purchaseToken: "a" } }), undefined);
});

test("nor does anything that is no notification at all", () => {
  for (const message of [undefined, null, "a", 5, [], {}, { packageName: PACKAGE }]) assert.equal(notified(message), undefined);
  assert.equal(notified({ packageName: PACKAGE, subscriptionNotification: { purchaseToken: "" } }), undefined);
  assert.equal(notified({ packageName: PACKAGE, subscriptionNotification: { purchaseToken: 5 } }), undefined);
});
