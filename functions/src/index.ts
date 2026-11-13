// What the server does for Premium, the one thing the app sells. It asks Google Play whether a
// purchase is real before granting it to an account, takes it back when Play does, and forgets
// it with the account. Nothing is granted on the app's word: the app reads its own grant, at
// users/{the account's id}/grants/premium, and can write none. See firestore/firestore.rules.
import { initializeApp } from "firebase-admin/app";
import { getAuth } from "firebase-admin/auth";
import { FieldValue, Timestamp, getFirestore } from "firebase-admin/firestore";
import { logger, setGlobalOptions } from "firebase-functions";
import { HttpsError, onCall } from "firebase-functions/https";
import { onSchedule } from "firebase-functions/scheduler";
import { region } from "firebase-functions/v1";
import { type Outcome, owesAcknowledgement, purchaseId, settle, standing } from "./claim";
import { play, voidedFrom, voidedSince } from "./play";

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

async function claim(uid: string, token: string): Promise<Outcome> {
  const purchase = await play.purchase(token);
  const state = standing(purchase);
  if (state !== "purchased") return state;

  const id = purchaseId(token);
  const binding = db.doc(`purchases/${id}`);
  // Which account a purchase belongs to and that account's grant change together or not at all:
  // two accounts claiming the same purchase at once cannot both come away with it.
  const outcome = await db.runTransaction(async (transaction) => {
    const [bound, granted] = await transaction.getAll(binding, grantOf(uid));
    const owner: string | undefined = bound.get("uid");
    const settlement = settle(uid, owner, owner !== undefined && owner !== uid && (await isGone(owner)));
    if (settlement.outcome === "taken") return settlement.outcome;
    if (settlement.bind) transaction.set(binding, { uid, at: FieldValue.serverTimestamp() });
    if (settlement.strip) transaction.delete(grantOf(settlement.strip));
    // A grant already there is left as it was made: claiming again does not make it newer.
    if (!granted.exists) {
      transaction.set(grantOf(uid), {
        since: FieldValue.serverTimestamp(),
        purchase: id,
        order: purchase.orderId ?? null,
      });
    }
    return settlement.outcome;
  });

  if (outcome === "granted" && owesAcknowledgement(purchase)) await tellPlay(token);
  return outcome;
}

// Play is told that what was bought has been delivered. Premium is granted by now, and stays so
// if Play cannot be told: it is told twice here before giving up, and the app claims the
// purchase again every time it starts for as long as Play has not heard.
async function tellPlay(token: string): Promise<void> {
  for (let attempt = 1; attempt <= 2; attempt++) {
    try {
      return await play.acknowledge(token);
    } catch (error) {
      logger.warn("claimPremium: granted, but Play could not be told so", { attempt, ...kindOf(error) });
    }
  }
}

export const claimPremium = onCall({ enforceAppCheck: ENFORCE_APP_CHECK }, async (request) => {
  const uid = request.auth?.uid;
  if (!uid) throw new HttpsError("unauthenticated", "Sign in to claim a purchase.");
  const token: unknown = request.data?.token;
  if (typeof token !== "string" || token.length === 0 || token.length > 1000) {
    throw new HttpsError("invalid-argument", "token is not a purchase token.");
  }
  try {
    const outcome = await claim(uid, token);
    logger.info("claimPremium", { outcome });
    return { outcome };
  } catch (error) {
    logger.error("claimPremium: Play or Firestore could not be reached", kindOf(error));
    throw new HttpsError("unavailable", "The purchase could not be checked. Try again later.");
  }
});

export const revokeVoided = onSchedule("every 24 hours", async () => {
  const cursor = db.doc("state/voidedPurchases");
  const now = Date.now();
  const lastAsked = ((await cursor.get()).get("at") as Timestamp | undefined)?.toMillis();
  for await (const token of voidedSince(play, voidedFrom(lastAsked, now))) {
    const binding = db.doc(`purchases/${purchaseId(token)}`);
    await db.runTransaction(async (transaction) => {
      const uid: string | undefined = (await transaction.get(binding)).get("uid");
      if (uid === undefined) return;
      // An account's grant goes only if it was made for this purchase, and not for another
      // that is still good.
      const grant = await transaction.get(grantOf(uid));
      if (grant.get("purchase") === binding.id) transaction.delete(grant.ref);
      transaction.delete(binding);
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
