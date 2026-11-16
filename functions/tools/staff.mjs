#!/usr/bin/env node
// Premium for the team's own accounts: given, taken back, and listed.
//
//   npm --prefix functions run staff -- list
//   npm --prefix functions run staff -- give someone@example.com
//   npm --prefix functions run staff -- take someone@example.com
//
// It runs with the Google account of whoever runs it (gcloud auth application-default login),
// which has to be allowed to write to the project's database; no key is kept anywhere. The
// project is named here and not taken from the environment: this writes to no other.
import { applicationDefault, initializeApp } from "firebase-admin/app";
import { getAuth } from "firebase-admin/auth";
import { FieldValue, getFirestore } from "firebase-admin/firestore";
import { STAFF, giving, takingBack } from "../lib/staff.js";

const PROJECT = "magic-ai-8-ball";
const [action, email] = process.argv.slice(2);

// A person's own credentials name no project to count their calls against: this one's, then.
process.env.GOOGLE_CLOUD_QUOTA_PROJECT ??= PROJECT;
initializeApp({ credential: applicationDefault(), projectId: PROJECT });
const db = getFirestore();
const grantOf = (uid) => db.doc(`users/${uid}/grants/premium`);

async function list() {
  const { users } = await getAuth().listUsers(1000);
  for (const user of users) {
    const held = (await grantOf(user.uid).get()).data();
    const premium = held === undefined ? "-" : held.source === STAFF ? "given" : held.until ? "subscribed" : "bought";
    console.log([user.email ?? "(no email)", premium, `last in ${user.metadata.lastSignInTime ?? "never"}`].join("\t"));
  }
}

async function change(decide, write) {
  if (!email) throw new Error("Whose? Give the email the account signs in with.");
  const { uid } = await getAuth().getUserByEmail(email);
  // Read and written together: a purchase claimed at the same moment is not written over.
  const outcome = await db.runTransaction(async (transaction) => {
    const grant = await transaction.get(grantOf(uid));
    const decided = decide(grant.data());
    write(transaction, grant.ref, decided);
    return decided;
  });
  console.log(`${email}: ${outcome}`);
}

const actions = {
  list,
  give: () =>
    change(giving, (transaction, ref, decided) => {
      if (decided === "given") transaction.set(ref, { since: FieldValue.serverTimestamp(), source: STAFF });
    }),
  take: () =>
    change(takingBack, (transaction, ref, decided) => {
      if (decided === "taken back") transaction.delete(ref);
    }),
};

const run = actions[action];
if (!run) {
  console.error("usage: staff list | give <email> | take <email>");
  process.exit(2);
}
run().catch((error) => {
  // Its kind and no more: a message may carry the path of an account's document.
  console.error(`failed: ${error.code ?? error.name}`);
  process.exit(1);
});
