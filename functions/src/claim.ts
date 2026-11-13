import { createHash } from "node:crypto";
import type { Purchase } from "./play";

// The app as Google Play knows it, and the one thing it sells there: bought once and kept.
export const PACKAGE = "com.lorenzosuarez.magiceightball";
export const PRODUCT = "premium";

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

/** What Play's answer comes to, before anything kept here is looked at. */
export function standing(purchase: Purchase): "invalid" | "pending" | "purchased" {
  if (!purchase.productLineItem?.some((item) => item.productId === PRODUCT)) return "invalid";
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

/**
 * What is done with a purchase Play has confirmed: whether it is bound to the account that
 * claims it, and whose grant goes when it changes hands.
 */
export type Settlement = { outcome: "taken" } | { outcome: "granted"; bind: boolean; strip?: string };

/**
 * A purchase belongs to the first account that claims it, and to that one for as long as the
 * account is there: Premium is bought once and cannot be handed round. Once the account is
 * deleted the purchase is free again, so that whoever paid is not left without it for having
 * started over.
 */
export function settle(caller: string, owner: string | undefined, ownerGone: boolean): Settlement {
  if (owner === undefined) return { outcome: "granted", bind: true };
  if (owner === caller) return { outcome: "granted", bind: false };
  if (!ownerGone) return { outcome: "taken" };
  return { outcome: "granted", bind: true, strip: owner };
}

/**
 * Play gives back the money for a purchase that nobody has told it was delivered, three days
 * after it was made. So Play is told unless it says it already has been: one time too many
 * costs nothing, and one too few costs the user what they bought.
 */
export function owesAcknowledgement(purchase: Purchase): boolean {
  return purchase.acknowledgementState !== "ACKNOWLEDGEMENT_STATE_ACKNOWLEDGED";
}
