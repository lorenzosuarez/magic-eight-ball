// Premium given by whoever runs the project to an account of the team's, not bought. It is a
// grant like any other, at users/{the account's id}/grants/premium, so the app treats whoever
// holds it as it treats whoever paid; it has no end and names no purchase, so nothing said of
// a purchase ever takes it back. It is written by tools/staff.mjs, with the rights of whoever
// runs that, and by nothing the app can reach: see firestore/firestore.rules.

/** What marks a grant as given and not bought. */
export const STAFF = "staff";

/** A grant as it is kept, or none. */
export type Held = { source?: unknown } | undefined;

/**
 * What giving Premium does to what an account already holds: it is written where there is
 * nothing, and what is there is left as it is. A grant made for a purchase stays that
 * purchase's, so that it still ends when the purchase does.
 */
export function giving(held: Held): "given" | "already given" | "already bought" {
  if (held === undefined) return "given";
  return held.source === STAFF ? "already given" : "already bought";
}

/** What taking it back does: only what was given is taken, never what was bought. */
export function takingBack(held: Held): "taken back" | "nothing to take" | "bought, left alone" {
  if (held === undefined) return "nothing to take";
  return held.source === STAFF ? "taken back" : "bought, left alone";
}
