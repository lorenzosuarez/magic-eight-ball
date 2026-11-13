import { GoogleAuth } from "google-auth-library";
import { PACKAGE, PRODUCT } from "./claim";

/**
 * What Play says of a purchase, as far as granting Premium needs it. The fields are Play's own,
 * under the names it gives them in ProductPurchaseV2. With none of them, Play knows of no such
 * purchase.
 */
export interface Purchase {
  productLineItem?: { productId?: string }[];
  purchaseStateContext?: { purchaseState?: string };
  acknowledgementState?: string;
  orderId?: string;
}

/** One page of the purchases Play has taken back: refunded, cancelled or charged back. */
export interface VoidedPage {
  voidedPurchases?: { purchaseToken?: string }[];
  tokenPagination?: { nextPageToken?: string | null };
}

/** What is asked of Play. A fake stands in for it where what is built on it is tested. */
export interface Play {
  purchase(token: string): Promise<Purchase>;
  acknowledge(token: string): Promise<void>;
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

export const play: Play = {
  async purchase(token) {
    // A token goes into the address as one step of its path, whatever it holds. A dot or two
    // alone would be read as a step back along it instead, and are no purchase of anyone's.
    if (token === "." || token === "..") return {};
    const response = await ask(`productsv2/tokens/${encodeURIComponent(token)}`);
    // What Play answers of a token that is none of its own, none of this app's, or gone for good.
    // Only these: any other refusal is ours to put right, and is never taken for a bad purchase.
    if ([400, 404, 410].includes(response.status)) return {};
    if (!response.ok) throw refusal(response);
    return (await response.json()) as Purchase;
  },

  async acknowledge(token) {
    const response = await ask(`products/${PRODUCT}/tokens/${encodeURIComponent(token)}:acknowledge`, "POST");
    if (!response.ok) throw refusal(response);
  },

  async voided(since, page) {
    // Type 0 is what was bought once. Subscriptions are of another type, and none is sold.
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
