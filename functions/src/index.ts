// What the server does for Premium, the one thing the app sells: a subscription, and before
// that a purchase made once, which whoever made it keeps. It asks Google Play whether a purchase
// is real before granting it to an account, follows a subscription for as long as it lasts,
// takes Premium back when Play does, and forgets it with the account. Nothing is granted on the
// app's word: the app reads its own grant, at users/{the account's id}/grants/premium, and can
// write none. See firestore/firestore.rules.
import { initializeApp } from "firebase-admin/app";
import { getAuth } from "firebase-admin/auth";
import { type DocumentSnapshot, FieldValue, Timestamp, type Transaction, getFirestore } from "firebase-admin/firestore";
import { logger, setGlobalOptions } from "firebase-functions";
import { HttpsError, onCall } from "firebase-functions/https";
import { onMessagePublished } from "firebase-functions/pubsub";
import { onSchedule } from "firebase-functions/scheduler";
import { region } from "firebase-functions/v1";
import {
  ONE_TIME,
  type Outcome,
  SUBSCRIPTION,
  lapsedBefore,
  owesAcknowledgement,
  purchaseId,
  settle,
  standing,
  subscribed,
} from "./claim";
import { notified, play, voidedFrom, voidedSince } from "./play";

initializeApp();
const db = getFirestore();

const REGION = "europe-west1";
// No more of these at once than an app of this size can need, whoever is calling.
setGlobalOptions({ region: REGION, maxInstances: 10 });

// Whether a claim is refused unless App Check vouches that it comes from the app itself. Left
// off, a token that comes is still checked and nothing is asked of a request that brings none.
// To be turned on once the version of the app that sends them is the one in people's hands, and
// App Check's own figures show the claims arriving verified: before that it would lock out
// whoever has already paid.
const ENFORCE_APP_CHECK = false;

// The Pub/Sub topic Play Console is told to publish its notifications to.
const NOTIFICATIONS = "play-notifications";

const grantOf = (uid: string) => db.doc(`users/${uid}/grants/premium`);

// What the logs are told of a failure: its kind and its code, never its message, which may
// carry the path of an account's document.
function kindOf(error: unknown) {
  const { name, code, cause } = (error ?? {}) as { name?: unknown; code?: unknown; cause?: { code?: unknown } };
  return { name, code, cause: cause?.code };
}

async function isGone(uid: string): Promise<boolean> {
  try {
    await getAuth().getUser(uid);
    return false;
  } catch (error) {
    if ((error as { code?: unknown }).code === "auth/user-not-found") return true;
    throw error;
  }
}

// What Play confirms of a purchase: its order, whether Play is still to be told it was
// delivered, and, of a subscription, until when it is paid for and whether it is paid for again
// then. Premium bought once has no end.
interface Confirmed {
  order: string | null;
  owed: boolean;
  until?: number;
  renews?: boolean;
}

async function confirm(token: string, subscription: boolean): Promise<"invalid" | "pending" | Confirmed> {
  if (subscription) {
    const answer = await play.subscription(token);
    const state = subscribed(answer, Date.now());
    return typeof state === "string" ? state : { ...state, owed: owesAcknowledgement(answer) };
  }
  const answer = await play.purchase(token);
  const state = standing(answer);
  return state === "purchased" ? { order: answer.orderId ?? null, owed: owesAcknowledgement(answer) } : state;
}

async function claim(uid: string, token: string, subscription: boolean): Promise<Outcome> {
  const confirmed = await confirm(token, subscription);
  if (typeof confirmed === "string") return confirmed;

  const id = purchaseId(token);
  const binding = db.doc(`purchases/${id}`);
  // Which account a purchase belongs to and that account's grant change together or not at all:
  // two accounts claiming the same purchase at once cannot both come away with it.
  const outcome = await db.runTransaction(async (transaction) => {
    const [bound, granted] = await transaction.getAll(binding, grantOf(uid));
    const owner: string | undefined = bound.get("uid");
    const settlement = settle(uid, owner, owner !== undefined && owner !== uid && (await isGone(owner)));
    if (settlement.outcome === "taken") return settlement.outcome;
    // An end already written that is later than this one came from a newer answer of Play's,
    // as when a renewal is told of while this claim is on its way: it is not put back.
    const written = settlement.bind ? undefined : (bound.get("until") as Timestamp | undefined)?.toMillis();
    const lasting =
      confirmed.until === undefined ? {} : { until: Timestamp.fromMillis(Math.max(confirmed.until, written ?? 0)) };
    // Whether it is paid for again at its end is written with the end, on the grant alone: it
    // is what the app tells whoever holds it. An end already written for this purchase that is
    // later than this answer's keeps what was said with it.
    const stale =
      written !== undefined && confirmed.until !== undefined && written > confirmed.until && granted.get("purchase") === id;
    const renews: boolean | undefined = stale ? granted.get("renews") : confirmed.renews;
    const terms = renews === undefined ? lasting : { ...lasting, renews };
    if (settlement.bind) transaction.set(binding, { uid, at: FieldValue.serverTimestamp(), ...lasting });
    // Claimed again by the account that holds it: only how long it lasts is news.
    else if (confirmed.until !== undefined) transaction.update(binding, lasting);
    if (settlement.strip) transaction.delete(grantOf(settlement.strip));
    // A grant for Premium bought once has no end, and is left as it was made: claiming again
    // does not make it newer, and a subscription does not put an end on it. Any other grant
    // follows the purchase being claimed, and keeps its date if it was made for this one.
    const forGood = granted.exists && granted.get("until") === undefined;
    if (!forGood) {
      transaction.set(grantOf(uid), {
        since: granted.get("purchase") === id ? granted.get("since") : FieldValue.serverTimestamp(),
        purchase: id,
        order: confirmed.order,
        ...terms,
      });
    }
    return settlement.outcome;
  });

  if (outcome === "granted" && confirmed.owed) await tellPlay(token, subscription);
  return outcome;
}

// Play is told that what was paid for has been delivered. Premium is granted by now, and stays
// so if Play cannot be told: it is told twice here before giving up, and the app claims the
// purchase again every time it starts for as long as Play has not heard.
async function tellPlay(token: string, subscription: boolean): Promise<void> {
  for (let attempt = 1; attempt <= 2; attempt++) {
    try {
      return await play.acknowledge(token, subscription);
    } catch (error) {
      logger.warn("claimPremium: granted, but Play could not be told so", { attempt, ...kindOf(error) });
    }
  }
}

// A purchase that gives Premium no longer is let go of: the grant made for it, and the note of
// which account held it. An account's grant goes only if it was made for this purchase, and not
// for another that is still good.
async function release(transaction: Transaction, bound: DocumentSnapshot): Promise<void> {
  const uid: string | undefined = bound.get("uid");
  if (uid !== undefined) {
    const grant = await transaction.get(grantOf(uid));
    if (grant.get("purchase") === bound.id) transaction.delete(grant.ref);
  }
  transaction.delete(bound.ref);
}

// How a subscription stands with Play now is made true of the account that holds it: paid for
// further, its grant lasts further; over, on hold or paused, the grant goes. One that comes
// back afterwards is claimed again by the app the next time it starts.
async function follow(token: string): Promise<void> {
  const binding = db.doc(`purchases/${purchaseId(token)}`);
  // Play tells of every subscription, the ones no account has claimed yet among them: nothing
  // is kept of those, and Play is not asked about them.
  if (!(await binding.get()).exists) return;
  const state = subscribed(await play.subscription(token), Date.now());
  await db.runTransaction(async (transaction) => {
    const bound = await transaction.get(binding);
    // Gone meanwhile, or Premium bought once, which nothing said of a subscription can end.
    if (bound.get("until") === undefined) return;
    if (typeof state === "string") return release(transaction, bound);
    const grant = await transaction.get(grantOf(bound.get("uid")));
    const lasting = { until: Timestamp.fromMillis(state.until) };
    transaction.update(binding, lasting);
    if (grant.get("purchase") === bound.id) {
      transaction.update(grant.ref, { ...lasting, order: state.order, renews: state.renews });
    }
  });
}

export const claimPremium = onCall({ enforceAppCheck: ENFORCE_APP_CHECK }, async (request) => {
  const uid = request.auth?.uid;
  if (!uid) throw new HttpsError("unauthenticated", "Sign in to claim a purchase.");
  const token: unknown = request.data?.token;
  if (typeof token !== "string" || token.length === 0 || token.length > 1000) {
    throw new HttpsError("invalid-argument", "token is not a purchase token.");
  }
  // What the app says the token is a purchase of, which only chooses how Play is asked: Play
  // says what it really is. Versions of the app from before the subscription name nothing, and
  // can only mean Premium bought once.
  const product: unknown = request.data?.product ?? ONE_TIME;
  if (product !== SUBSCRIPTION && product !== ONE_TIME) {
    throw new HttpsError("invalid-argument", "product is not one the app sells.");
  }
  try {
    const outcome = await claim(uid, token, product === SUBSCRIPTION);
    logger.info("claimPremium", { outcome });
    return { outcome };
  } catch (error) {
    logger.error("claimPremium: Play or Firestore could not be reached", kindOf(error));
    throw new HttpsError("unavailable", "The purchase could not be checked. Try again later.");
  }
});

// Play publishes here whenever a subscription changes: renewed, cancelled, expired, on hold,
// paused, recovered, revoked or refunded. This, and no list of tokens kept to ask about, is how
// Premium ends when a subscription does: the notification brings the token, which is written
// nowhere. Tried again if it fails, since Play tells of each change once; following a
// subscription twice changes nothing more.
export const playNotified = onMessagePublished({ topic: NOTIFICATIONS, retry: true }, async (event) => {
  let message: unknown;
  try {
    message = event.data.message.json;
  } catch {
    // Not one of Play's: trying it again would not make it one.
    return;
  }
  const token = notified(message);
  if (token !== undefined) await follow(token);
});

// What keeps Premium from outliving a subscription Play was never heard from about again, as
// when its notifications are not set up or one was lost: a subscription whose end is well past
// is let go of. If it was in fact renewed, the app claims it again the next time it starts.
export const endLapsed = onSchedule("every 24 hours", async () => {
  const before = Timestamp.fromMillis(lapsedBefore(Date.now()));
  const lapsed = await db.collection("purchases").where("until", "<", before).get();
  for (const found of lapsed.docs) {
    await db.runTransaction(async (transaction) => {
      const bound = await transaction.get(found.ref);
      // As it is now, not as it was found: one renewed in the meantime stays.
      const until: Timestamp | undefined = bound.get("until");
      if (until !== undefined && until.toMillis() < before.toMillis()) await release(transaction, bound);
    });
  }
});

export const revokeVoided = onSchedule("every 24 hours", async () => {
  const cursor = db.doc("state/voidedPurchases");
  const now = Date.now();
  const lastAsked = ((await cursor.get()).get("at") as Timestamp | undefined)?.toMillis();
  for await (const token of voidedSince(play, voidedFrom(lastAsked, now))) {
    const binding = db.doc(`purchases/${purchaseId(token)}`);
    await db.runTransaction(async (transaction) => {
      const bound = await transaction.get(binding);
      if (bound.exists) await release(transaction, bound);
    });
  }
  // Written only once everything was read and dealt with: a run that fails is made up for by
  // the next, which starts from where the last good one did.
  await cursor.set({ at: Timestamp.fromMillis(now) });
});

// The profile, the characters, the grant and whatever else was left under the account, and the
// purchases bound to it, which are thereby free to be claimed by the account their buyer makes
// next. Of the first generation: the second offers this trigger only as a preview. Tried again
// if it fails, since nothing else would ever come back for what an account left behind; doing it
// twice deletes nothing more.
export const forgetAccount = region(REGION)
  .runWith({ failurePolicy: true })
  .auth.user()
  .onDelete(async (user) => {
    await db.recursiveDelete(db.doc(`users/${user.uid}`));
    const bound = await db.collection("purchases").where("uid", "==", user.uid).get();
    // Only as it was found: a purchase claimed by another account in the meantime is theirs by
    // now, and is not unbound from under them.
    await Promise.all(bound.docs.map((binding) => binding.ref.delete({ lastUpdateTime: binding.updateTime })));
  });
