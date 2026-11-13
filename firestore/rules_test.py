"""Checks firestore.rules against the local emulator, with nothing but Python's own library.

    firebase emulators:exec --only firestore --project demo-magic-ball "python3 firestore/rules_test.py"

Every request is made as the app's would be: signed in as someone, or as nobody, never as the
owner of the project, to whom no rule applies.
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


def call(method, url, uid=None, body=None):
    request = urllib.request.Request(url, method=method, data=json.dumps(body).encode() if body is not None else None)
    request.add_header("Content-Type", "application/json")
    if uid:
        request.add_header("Authorization", "Bearer " + token(uid))
    try:
        with urllib.request.urlopen(request) as response:
            return response.status
    except urllib.error.HTTPError as error:
        return error.code


def value(v):
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
check("the owner corrects the name", save("ana", "ana", profile(firstName="Ana María")), OK)

check("eighteen today is an adult", save("bea", "bea", profile(birthDate=years_ago(18))), OK)
check("seventeen is refused", save("teo", "teo", profile(birthDate=years_ago(17))), REFUSED)
check("eighteen in two days is refused", save("teo", "teo", profile(birthDate=years_ago(18, days=2))), REFUSED)
check("a profile cannot be corrected into an under-age one", save("ana", "ana", profile(birthDate=years_ago(10))), REFUSED)

check("a field the app does not ask for is refused", save("cai", "cai", profile(premium=True)), REFUSED)
check("no name is refused", save("cai", "cai", profile(firstName=None)), REFUSED)
check("an empty name is refused", save("cai", "cai", profile(firstName="")), REFUSED)
check("a name longer than a name is refused", save("cai", "cai", profile(firstName="A" * 21)), REFUSED)
check("a name that is not text is refused", save("cai", "cai", profile(firstName=7)), REFUSED)
check("a date not written as a date is refused", save("cai", "cai", profile(birthDate="12/03/1990")), REFUSED)
check("no agreement is refused", save("cai", "cai", profile(consent=None), server_time=None), REFUSED)
check("an agreement dated by the app is refused",
      save("cai", "cai", profile(consent={"version": 1, "at": "2020-01-01"}), server_time=None), REFUSED)
check("an agreement to no wording is refused", save("cai", "cai", profile(consent={"version": 0})), REFUSED)
check("something else slipped into the agreement is refused",
      save("cai", "cai", profile(consent={"version": 1, "note": "x"})), REFUSED)

check("nothing else can be kept anywhere", save("ana", "ana", {"a": "b"}, server_time=None, collection="notes"), REFUSED)
check("nor under the profile", call("GET", f"{BASE}/users/ana/characters/one", "ana"), REFUSED)

check("someone else cannot delete it", call("DELETE", f"{BASE}/users/ana", "eve"), REFUSED)
check("the owner deletes it", call("DELETE", f"{BASE}/users/ana", "ana"), OK)

print(f"\n{len(failures)} failed" if failures else "\nall as the rules say")
sys.exit(1 if failures else 0)
