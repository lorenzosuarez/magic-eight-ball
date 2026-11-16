import { createHash } from "node:crypto";
import type { Purchase, Subscription } from "./play";

// The app as Google Play knows it, and what it sells there: Premium as a subscription, paid for
// again each period until it is cancelled. The id is made in Play Console and cannot be changed
// there afterwards: see docs/REMOTE_CONFIG.md.
export const PACKAGE = "com.lorenzosuarez.magiceightball";
export const SUBSCRIPTION = "premium_subscription";
// What Premium was first sold as: bought once and kept. It is sold no more, and whoever bought
// it keeps it, so a purchase of it is still checked and granted as it always was.
export const ONE_TIME = "premium";

/** What the app is told of a purchase it asks to have granted. */
export type Outcome = "granted" | "pending" | "taken" | "invalid";

/**
 * The name a purchase goes by wherever it is kept. Whoever holds a purchase token can claim the
 * purchase, so the token itself is written nowhere: this says as much of it as is needed, that
 * it is the same one.
 */
export function purchaseId(token: string): string {
  return createHash("sha256").update(token).digest("hex");
}

/** What Play's answer comes to for Premium bought once, before anything kept here is looked at. */
export function standing(purchase: Purchase): "invalid" | "pending" | "purchased" {
  if (!purchase.productLineItem?.some((item) => item.productId === ONE_TIME)) return "invalid";
  switch (purchase.purchaseStateContext?.purchaseState) {
    case "PURCHASED":
      return "purchased";
    case "PENDING":
      return "pending";
    // Cancelled, or a state Play says it never sets.
    default:
      return "invalid";
  }
}

/** A subscription that gives Premium: until when Play says it is paid for, and its latest order. */
export interface Subscribed {
  until: number;
  order: string | null;
}

/**
 * What Play's answer comes to for a subscription at a given moment, as Play's own account of a
 * subscription's life says to read it. Premium is theirs while it is active, while Play is still
 * trying a payment that failed (the grace period), and, once cancelled, up to the end of what
 * was paid for. It is not while payment is on hold, while it is paused, or once it has expired,
 * which is also how Play shows one it revoked or refunded.
 */
export function subscribed(subscription: Subscription, now: number): "invalid" | "pending" | Subscribed {
  const item = subscription.lineItems?.find((line) => line.productId === SUBSCRIPTION);
  if (item === undefined) return "invalid";
  const expiry = Date.parse(item.expiryTime ?? "");
  const order = item.latestSuccessfulOrderId ?? null;
  switch (subscription.subscriptionState) {
    // For a day after a renewal fails Play still calls the subscription active, with its end
    // already behind it. Premium stays, and what is written down as its end is then this
    // moment: Play tells of whatever happens next, and that is when it is read again.
    case "SUBSCRIPTION_STATE_ACTIVE":
    case "SUBSCRIPTION_STATE_IN_GRACE_PERIOD":
      return { until: expiry > now ? expiry : now, order };
    case "SUBSCRIPTION_STATE_CANCELED":
      return expiry > now ? { until: expiry, order } : "invalid";
    case "SUBSCRIPTION_STATE_PENDING":
      return "pending";
    // On hold, paused, expired, a payment given up on before it was made, or no state at all.
    default:
      return "invalid";
  }
}

const DAY = 24 * 60 * 60 * 1000;

/**
 * The moment before which a subscription's written end means it is over, when Play has said
 * nothing since. Play tells of every renewal and every ending as it happens, and that is what
 * keeps Premium in step; this only stops Premium from outliving a subscription Play was never
 * heard from about again. Three days past the end: Play calls a subscription active for a day
 * after a renewal fails, and what it has to tell may come late.
 */
export function lapsedBefore(now: number): number {
  return now - 3 * DAY;
}

/**
 * What is done with a purchase Play has confirmed: whether it is bound to the account that
 * claims it, and whose grant goes when it changes hands.
 */
export type Settlement = { outcome: "taken" } | { outcome: "granted"; bind: boolean; strip?: string };

/**
 * A purchase belongs to the first account that claims it, and to that one for as long as the
 * account is there: Premium is paid for by one account and cannot be handed round. Once the
 * account is deleted the purchase is free again, so that whoever paid is not left without it
 * for having started over.
 */
export function settle(caller: string, owner: string | undefined, ownerGone: boolean): Settlement {
  if (owner === undefined) return { outcome: "granted", bind: true };
  if (owner === caller) return { outcome: "granted", bind: false };
  if (!ownerGone) return { outcome: "taken" };
  return { outcome: "granted", bind: true, strip: owner };
}

/**
 * Play gives back the money for a purchase that nobody has told it was delivered, three days
 * after it was made, a subscription's first payment included. So Play is told unless it says it
 * already has been: one time too many costs nothing, and one too few costs the user what they
 * paid for.
 */
export function owesAcknowledgement(purchase: { acknowledgementState?: string }): boolean {
  return purchase.acknowledgementState !== "ACKNOWLEDGEMENT_STATE_ACKNOWLEDGED";
}
