import { GoogleAuth } from "google-auth-library";
import { ONE_TIME, PACKAGE, SUBSCRIPTION } from "./claim";

/**
 * What Play says of a purchase of Premium bought once, as far as granting it needs. The fields
 * are Play's own, under the names it gives them in ProductPurchaseV2. With none of them, Play
 * knows of no such purchase.
 */
export interface Purchase {
  productLineItem?: { productId?: string }[];
  purchaseStateContext?: { purchaseState?: string };
  acknowledgementState?: string;
  orderId?: string;
}

/**
 * What Play says of a subscription, as far as granting Premium needs it, under the names Play
 * gives the fields in SubscriptionPurchaseV2. With none of them, Play knows of no such
 * subscription.
 */
export interface Subscription {
  subscriptionState?: string;
  acknowledgementState?: string;
  lineItems?: { productId?: string; expiryTime?: string; latestSuccessfulOrderId?: string }[];
}

/** One page of the purchases Play has taken back: refunded, cancelled or charged back. */
export interface VoidedPage {
  voidedPurchases?: { purchaseToken?: string }[];
  tokenPagination?: { nextPageToken?: string | null };
}

/** What is asked of Play. A fake stands in for it where what is built on it is tested. */
export interface Play {
  purchase(token: string): Promise<Purchase>;
  subscription(token: string): Promise<Subscription>;
  /** Tells Play that what was paid for has been delivered: a subscription, or Premium bought once. */
  acknowledge(token: string, subscription: boolean): Promise<void>;
  /** Since a moment in milliseconds, or as far back as Play goes; from a page on, or the first. */
  voided(since: number | undefined, page: string | undefined): Promise<VoidedPage>;
}

const PURCHASES = `https://androidpublisher.googleapis.com/androidpublisher/v3/applications/${PACKAGE}/purchases`;

// Play is asked as whoever the function runs as: that account, and no key kept anywhere, is what
// Play Console is told to trust.
const auth = new GoogleAuth({ scopes: "https://www.googleapis.com/auth/androidpublisher" });

async function ask(path: string, method: "GET" | "POST" = "GET"): Promise<Response> {
  return fetch(`${PURCHASES}/${path}`, {
    method,
    headers: { Authorization: `Bearer ${await auth.getAccessToken()}`, "Content-Type": "application/json" },
    body: method === "POST" ? "{}" : undefined,
    // Play out of reach is told to the app soon enough for it to try again, not after a minute.
    signal: AbortSignal.timeout(10_000),
  });
}

// A refusal is told by its status alone. The address that was asked holds the purchase token,
// and what Play answers may say whose purchase it is: neither is for the logs.
function refusal(response: Response): Error {
  return Object.assign(new Error(`Play answered ${response.status}`), { code: `play/${response.status}` });
}

// What Play has of a token, asked for under one of its two kinds of purchase.
async function held<T extends object>(kind: "productsv2" | "subscriptionsv2", token: string): Promise<T | {}> {
  // A token goes into the address as one step of its path, whatever it holds. A dot or two
  // alone would be read as a step back along it instead, and are no purchase of anyone's.
  if (token === "." || token === "..") return {};
  const response = await ask(`${kind}/tokens/${encodeURIComponent(token)}`);
  // What Play answers of a token that is none of its own, none of this app's, of the other kind
  // of purchase, or gone for good. Only these: any other refusal is ours to put right, and is
  // never taken for a bad purchase.
  if ([400, 404, 410].includes(response.status)) return {};
  if (!response.ok) throw refusal(response);
  return (await response.json()) as T;
}

export const play: Play = {
  purchase: (token) => held<Purchase>("productsv2", token),

  subscription: (token) => held<Subscription>("subscriptionsv2", token),

  async acknowledge(token, subscription) {
    const product = subscription ? `subscriptions/${SUBSCRIPTION}` : `products/${ONE_TIME}`;
    const response = await ask(`${product}/tokens/${encodeURIComponent(token)}:acknowledge`, "POST");
    if (!response.ok) throw refusal(response);
  },

  async voided(since, page) {
    // Type 0 is what was bought once, and nothing else. Subscriptions are not asked for here:
    // Play tells of each one as it changes, a refund among the rest. See notified().
    const query = new URLSearchParams({ type: "0" });
    if (since !== undefined) query.set("startTime", String(since));
    if (page !== undefined) query.set("token", page);
    const response = await ask(`voidedpurchases?${query}`);
    if (!response.ok) throw refusal(response);
    return (await response.json()) as VoidedPage;
  },
};

/** The token of every purchase Play has taken back since a moment, one page after another. */
export async function* voidedSince(play: Play, since: number | undefined): AsyncGenerator<string> {
  let page: string | undefined;
  do {
    const answer = await play.voided(since, page);
    for (const voided of answer.voidedPurchases ?? []) {
      if (voided.purchaseToken) yield voided.purchaseToken;
    }
    page = answer.tokenPagination?.nextPageToken ?? undefined;
  } while (page);
}

const HOUR = 60 * 60 * 1000;
const DAY = 24 * HOUR;

/**
 * The moment from which to ask Play what it has taken back, given when it was last asked: an hour
 * before that, since Play goes by when it came to know of each one, which may be a little after
 * it happened. What is read twice is taken back twice, and the second time changes nothing.
 * Play keeps thirty days of it and refuses to be asked for more, so when the last time is out of
 * its reach, or there was none, no moment is given and Play starts as far back as it goes.
 */
export function voidedFrom(lastAsked: number | undefined, now: number): number | undefined {
  const from = (lastAsked ?? 0) - HOUR;
  return now - from < 29 * DAY ? from : undefined;
}

// What Play calls a subscription where it tells of a purchase it took back.
const VOIDED_SUBSCRIPTION = 1;

/**
 * The token of the subscription one of Play's notifications is about, or nothing when it is
 * about anything else: a purchase bought once, another app, the test message Play Console
 * sends, or no notification at all. What a notification says has happened is not read: it only
 * says that something has, and Play is then asked how the subscription stands.
 */
export function notified(message: unknown): string | undefined {
  const { packageName, subscriptionNotification, voidedPurchaseNotification } = (message ?? {}) as {
    packageName?: unknown;
    subscriptionNotification?: { purchaseToken?: unknown };
    voidedPurchaseNotification?: { purchaseToken?: unknown; productType?: unknown };
  };
  if (packageName !== PACKAGE) return undefined;
  const voided = voidedPurchaseNotification?.productType === VOIDED_SUBSCRIPTION ? voidedPurchaseNotification : undefined;
  const token = (subscriptionNotification ?? voided)?.purchaseToken;
  return typeof token === "string" && token.length > 0 ? token : undefined;
}
