"""Checks firestore.rules against the local emulator, with nothing but Python's own library.

    firebase emulators:exec --only firestore --project demo-magic-ball "python3 firestore/rules_test.py"

Every request is made as the app's would be: signed in as someone, or as nobody. Only what the
server keeps is put there as the server puts it: as the owner of the project, to whom no rule
applies.
"""
import base64
import datetime
import json
import os
import sys
import time
import urllib.error
import urllib.request

HOST = os.environ.get("FIRESTORE_EMULATOR_HOST", "127.0.0.1:8080")
PROJECT = os.environ.get("GCLOUD_PROJECT", "demo-magic-ball")
ROOT = f"projects/{PROJECT}/databases/(default)/documents"
BASE = f"http://{HOST}/v1/{ROOT}"


def token(uid):
    """What the emulator takes for a signed-in user: a token nobody signed."""
    def part(value):
        return base64.urlsafe_b64encode(json.dumps(value).encode()).rstrip(b"=").decode()
    now = int(time.time())
    claims = {
        "iss": f"https://securetoken.google.com/{PROJECT}", "aud": PROJECT, "iat": now, "exp": now + 3600,
        "auth_time": now, "sub": uid, "user_id": uid, "firebase": {"sign_in_provider": "google.com", "identities": {}},
    }
    return part({"alg": "none", "typ": "JWT"}) + "." + part(claims) + "."


SERVER = object()


def call(method, url, uid=None, body=None):
    request = urllib.request.Request(url, method=method, data=json.dumps(body).encode() if body is not None else None)
    request.add_header("Content-Type", "application/json")
    if uid:
        # "owner" is what the emulator takes for the owner of the project.
        request.add_header("Authorization", "Bearer " + ("owner" if uid is SERVER else token(uid)))
    try:
        with urllib.request.urlopen(request) as response:
            return response.status
    except urllib.error.HTTPError as error:
        return error.code


class At(str):
    """A moment written by the app itself, where the server's own is wanted."""


def value(v):
    if isinstance(v, At):
        return {"timestampValue": str(v)}
    if isinstance(v, bool):
        return {"booleanValue": v}
    if isinstance(v, int):
        return {"integerValue": str(v)}
    if isinstance(v, dict):
        return {"mapValue": {"fields": {k: value(x) for k, x in v.items()}}}
    return {"stringValue": v}


def save(uid, as_uid, fields, server_time="consent.at", collection="users"):
    """A whole document written as the app writes it: the time of the agreement left to the server."""
    write = {"update": {"name": f"{ROOT}/{collection}/{uid}", "fields": {k: value(v) for k, v in fields.items()}}}
    if server_time:
        write["updateTransforms"] = [{"fieldPath": server_time, "setToServerValue": "REQUEST_TIME"}]
    return call("POST", f"http://{HOST}/v1/{ROOT}:commit", as_uid, {"writes": [write]})


def correct(uid, as_uid, **fields):
    """Some fields changed and the rest left alone, as the app corrects a profile."""
    write = {
        "update": {"name": f"{ROOT}/users/{uid}", "fields": {k: value(v) for k, v in fields.items()}},
        "updateMask": {"fieldPaths": list(fields)},
        "currentDocument": {"exists": True},
    }
    return call("POST", f"http://{HOST}/v1/{ROOT}:commit", as_uid, {"writes": [write]})


def keep(uid, as_uid, slot, **changed):
    """A character written whole into a slot, as the app writes one."""
    whole = {"name": "Capitán Nemo", "description": "Un capitán gruñón del mar.", "colour": "pirate", "emblem": "cat"}
    whole.update(changed)
    fields = {k: value(v) for k, v in whole.items() if v is not None}
    write = {"update": {"name": f"{ROOT}/users/{uid}/characters/{slot}", "fields": fields}}
    return call("POST", f"http://{HOST}/v1/{ROOT}:commit", as_uid, {"writes": [write]})


def remove(uid, as_uid, slots=("1", "2", "3", "4", "5"), with_profile=True):
    """Everything kept with an account deleted in one change, as the app deletes it."""
    names = [f"{ROOT}/users/{uid}/characters/{slot}" for slot in slots]
    if with_profile:
        names.append(f"{ROOT}/users/{uid}")
    return call("POST", f"http://{HOST}/v1/{ROOT}:commit", as_uid, {"writes": [{"delete": name} for name in names]})


def grant(uid, as_uid, name="premium"):
    """What the server writes for an account once Google Play has confirmed its purchase."""
    fields = {"purchase": PURCHASE, "order": "GPA.0000-0000-0000-00000"}
    return save(f"{uid}/grants/{name}", as_uid, fields, server_time="since")


def bind(purchase, uid, as_uid):
    """A purchase bound to an account, as the server binds one: by the hash of its token."""
    return save(purchase, as_uid, {"uid": uid}, server_time="at", collection="purchases")


def profile(**changed):
    whole = {"firstName": "Lorena", "birthDate": "1990-03-12", "consent": {"version": 1}}
    whole.update(changed)
    return {k: v for k, v in whole.items() if v is not None}


def years_ago(years, days=0):
    day = datetime.datetime.now(datetime.timezone.utc).date() + datetime.timedelta(days=days)
    try:
        return day.replace(year=day.year - years).isoformat()
    except ValueError:  # 29 February
        return day.replace(year=day.year - years, day=28).isoformat()


OK, REFUSED = 200, 403
PURCHASE = "0" * 64
failures = []


def check(what, got, wanted):
    print(("ok   " if got == wanted else "FAIL ") + what + ("" if got == wanted else f": {got}, not {wanted}"))
    if got != wanted:
        failures.append(what)


check("the owner saves a profile", save("ana", "ana", profile()), OK)
check("the owner reads it", call("GET", f"{BASE}/users/ana", "ana"), OK)
check("someone else cannot read it", call("GET", f"{BASE}/users/ana", "eve"), REFUSED)
check("nobody signed in cannot read it", call("GET", f"{BASE}/users/ana"), REFUSED)
check("someone else cannot write it", save("ana", "eve", profile()), REFUSED)
check("nobody signed in cannot write it", save("ana", None, profile()), REFUSED)
check("nobody can list the profiles", call("GET", f"{BASE}/users", "ana"), REFUSED)
check("the owner corrects the name, and the agreement stays as it was made", correct("ana", "ana", firstName="Ana María"), OK)
check("a correction cannot redate the agreement", correct("ana", "ana", consent={"version": 1, "at": At("2020-01-01T00:00:00Z")}), REFUSED)
check("a correction cannot slip a field in", correct("ana", "ana", premium=True), REFUSED)
check("someone else cannot correct it", correct("ana", "eve", firstName="Eve"), REFUSED)
check("agreeing again, dated by the server, is allowed", save("ana", "ana", profile(firstName="Ana María")), OK)
check("twenty letters with accents are a name", save("ñu", "ñu", profile(firstName="Ñ" * 20)), OK)
check("someone born on a 29 February is kept", save("bis", "bis", profile(birthDate="2000-02-29")), OK)

check("eighteen today is an adult", save("bea", "bea", profile(birthDate=years_ago(18))), OK)
check("seventeen is refused", save("teo", "teo", profile(birthDate=years_ago(17))), REFUSED)
check("eighteen in two days is refused", save("teo", "teo", profile(birthDate=years_ago(18, days=2))), REFUSED)
check("a profile cannot be corrected into an under-age one", save("ana", "ana", profile(birthDate=years_ago(10))), REFUSED)

check("a field the app does not ask for is refused", save("cai", "cai", profile(premium=True)), REFUSED)
check("no name is refused", save("cai", "cai", profile(firstName=None)), REFUSED)
check("an empty name is refused", save("cai", "cai", profile(firstName="")), REFUSED)
check("a name longer than a name is refused", save("cai", "cai", profile(firstName="A" * 41)), REFUSED)
check("twenty letters from beyond the common ones are a name", save("got", "got", profile(firstName="𐌰" * 20)), OK)
check("a name that is not text is refused", save("cai", "cai", profile(firstName=7)), REFUSED)
check("a date not written as a date is refused", save("cai", "cai", profile(birthDate="12/03/1990")), REFUSED)
check("a month that is none is refused", save("cai", "cai", profile(birthDate="1990-13-45")), REFUSED)
check("the year nought is refused", save("cai", "cai", profile(birthDate="0000-00-00")), REFUSED)
check("no agreement is refused", save("cai", "cai", profile(consent=None), server_time=None), REFUSED)
check("an agreement dated in words is refused",
      save("cai", "cai", profile(consent={"version": 1, "at": "2020-01-01"}), server_time=None), REFUSED)
check("an agreement dated by the app, at a moment of its choosing, is refused",
      save("cai", "cai", profile(consent={"version": 1, "at": At("2020-01-01T00:00:00Z")}), server_time=None), REFUSED)
check("an agreement to no wording is refused", save("cai", "cai", profile(consent={"version": 0})), REFUSED)
check("something else slipped into the agreement is refused",
      save("cai", "cai", profile(consent={"version": 1, "note": "x"})), REFUSED)

check("nothing else can be kept anywhere", save("ana", "ana", {"a": "b"}, server_time=None, collection="notes"), REFUSED)
check("nor under the profile", call("GET", f"{BASE}/users/ana/notes/one", "ana"), REFUSED)

check("the owner keeps a character in the first slot", keep("ana", "ana", "1"), OK)
check("and in the fifth", keep("ana", "ana", "5"), OK)
check("there is no sixth slot", keep("ana", "ana", "6"), REFUSED)
check("nor a slot by any other name", keep("ana", "ana", "nemo"), REFUSED)
check("the owner changes a character", keep("ana", "ana", "1", name="Nemo"), OK)
check("the owner reads a character", call("GET", f"{BASE}/users/ana/characters/1", "ana"), OK)
check("and lists them", call("GET", f"{BASE}/users/ana/characters", "ana"), OK)
check("someone else cannot read a character", call("GET", f"{BASE}/users/ana/characters/1", "eve"), REFUSED)
check("nor list them", call("GET", f"{BASE}/users/ana/characters", "eve"), REFUSED)
check("nor write one", keep("ana", "eve", "2"), REFUSED)
check("nobody signed in cannot write one", keep("ana", None, "2"), REFUSED)
check("an account with no profile keeps no character", keep("zoe", "zoe", "1"), REFUSED)
check("a character with no name is refused", keep("ana", "ana", "2", name=None), REFUSED)
check("an empty name is refused", keep("ana", "ana", "2", name=""), REFUSED)
check("a name longer than a name is refused", keep("ana", "ana", "2", name="N" * 33), REFUSED)
check("sixteen letters with accents are a name", keep("ana", "ana", "2", name="Ñ" * 16), OK)
check("nothing said of how it answers is refused", keep("ana", "ana", "3", description=""), REFUSED)
check("more than can be said is refused", keep("ana", "ana", "3", description="a" * 561), REFUSED)
check("as much as can be said is kept, in letters from beyond the common ones too", keep("ana", "ana", "3", description="𐌰" * 280), OK)
check("a colour that is no name is refused", keep("ana", "ana", "4", colour="#FF0000"), REFUSED)
check("an accessory that is not text is refused", keep("ana", "ana", "4", emblem=7), REFUSED)
check("a field the app does not ask for is refused", keep("ana", "ana", "4", premium=True), REFUSED)
check("no slot is named with a nought before it", keep("ana", "ana", "01"), REFUSED)
check("nor with a space after it", keep("ana", "ana", "1 "), REFUSED)
check("nor nought", keep("ana", "ana", "0"), REFUSED)
check("nor with a wide digit", keep("ana", "ana", "１"), REFUSED)
check("something said of it that is not text is refused", keep("ana", "ana", "4", description=7), REFUSED)
check("sixteen letters from beyond the common ones are a name", keep("ana", "ana", "4", name="𐌰" * 16), OK)
check("and seventeen are not", keep("ana", "ana", "4", name="𐌰" * 17), REFUSED)
check("someone else cannot delete a character", call("DELETE", f"{BASE}/users/ana/characters/1", "eve"), REFUSED)
check("the owner deletes a character", call("DELETE", f"{BASE}/users/ana/characters/1", "ana"), OK)
check("a profile is not deleted from over its characters", call("DELETE", f"{BASE}/users/ana", "ana"), REFUSED)
check("nor with only some of them", remove("ana", "ana", slots=("2", "3")), REFUSED)
check("someone else cannot take an account's things away", remove("ana", "eve"), REFUSED)
check("the owner deletes the profile and every character at once", remove("ana", "ana"), OK)
check("and nothing of it is left", call("GET", f"{BASE}/users/ana/characters/2", "ana"), 404)
check("with the profile gone, no character can be kept", keep("ana", "ana", "1"), REFUSED)
check("an account with no character deletes its profile alone", save("uma", "uma", profile()), OK)
check("as it could before there were any", call("DELETE", f"{BASE}/users/uma", "uma"), OK)

check("someone else cannot delete a profile", save("ivo", "ivo", profile()), OK)
check("not theirs", call("DELETE", f"{BASE}/users/ivo", "eve"), REFUSED)
check("the owner deletes it", call("DELETE", f"{BASE}/users/ivo", "ivo"), OK)

check("the server grants Premium to an account", grant("pia", SERVER), OK)
check("the owner reads the grant", call("GET", f"{BASE}/users/pia/grants/premium", "pia"), OK)
check("and lists what the account was granted", call("GET", f"{BASE}/users/pia/grants", "pia"), OK)
check("someone else cannot read it", call("GET", f"{BASE}/users/pia/grants/premium", "eve"), REFUSED)
check("nor list it", call("GET", f"{BASE}/users/pia/grants", "eve"), REFUSED)
check("nobody signed in cannot read it", call("GET", f"{BASE}/users/pia/grants/premium"), REFUSED)
check("nobody grants themselves Premium", grant("eve", "eve"), REFUSED)
check("nor anything by another name", grant("eve", "eve", name="gold"), REFUSED)
check("the owner cannot change the grant", grant("pia", "pia"), REFUSED)
check("nor delete it", call("DELETE", f"{BASE}/users/pia/grants/premium", "pia"), REFUSED)
check("someone else cannot write it", grant("pia", "eve"), REFUSED)
check("nobody signed in cannot write it", grant("pia", None), REFUSED)
check("an account with a grant saves its profile", save("pia", "pia", profile()), OK)
check("and keeps a character", keep("pia", "pia", "1"), OK)
check("the grant does not stand in the way of deleting them", remove("pia", "pia"), OK)
check("and is still there, for the server to take away", call("GET", f"{BASE}/users/pia/grants/premium", "pia"), OK)

check("the server binds a purchase to an account", bind(PURCHASE, "pia", SERVER), OK)
check("which that account cannot read", call("GET", f"{BASE}/purchases/{PURCHASE}", "pia"), REFUSED)
check("nor anyone list", call("GET", f"{BASE}/purchases", "pia"), REFUSED)
check("nor nobody signed in read", call("GET", f"{BASE}/purchases/{PURCHASE}"), REFUSED)
check("nobody binds a purchase to themselves", bind("1" * 64, "eve", "eve"), REFUSED)
check("nor takes over one that is bound", bind(PURCHASE, "eve", "eve"), REFUSED)
check("nor unbinds their own", call("DELETE", f"{BASE}/purchases/{PURCHASE}", "pia"), REFUSED)
check("the server keeps its place in the list of purchases taken back",
      save("voidedPurchases", SERVER, {}, server_time="at", collection="state"), OK)
check("which nobody else reads", call("GET", f"{BASE}/state/voidedPurchases", "pia"), REFUSED)
check("nor lists", call("GET", f"{BASE}/state", "pia"), REFUSED)
check("nor moves", save("voidedPurchases", "pia", {}, server_time="at", collection="state"), REFUSED)
check("nor deletes", call("DELETE", f"{BASE}/state/voidedPurchases", "pia"), REFUSED)

COUNT = {"day": "2026-11-13", "used": 20}
check("the server counts an account's answers for the day",
      save("pia/usage/ai", SERVER, COUNT, server_time="at"), OK)
check("which the account itself cannot read", call("GET", f"{BASE}/users/pia/usage/ai", "pia"), REFUSED)
check("nor list", call("GET", f"{BASE}/users/pia/usage", "pia"), REFUSED)
check("nor set back", save("pia/usage/ai", "pia", {"day": "2026-11-13", "used": 0}, server_time="at"), REFUSED)
check("nor delete", call("DELETE", f"{BASE}/users/pia/usage/ai", "pia"), REFUSED)
check("nor start for itself", save("eve/usage/ai", "eve", COUNT, server_time="at"), REFUSED)

print(f"\n{len(failures)} failed" if failures else "\nall as the rules say")
sys.exit(1 if failures else 0)
