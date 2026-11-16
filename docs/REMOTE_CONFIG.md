# What can be changed from Firebase

Everything here is a Remote Config parameter of the project. No key for the AI service is among them, and none must be put here: requests go through Firebase AI Logic, which holds it. Versions of the app from before that change still read `gemini_api_key`, `gemini_use_paid_key` and `gemini_paid_api_key`: deleting them turns AI answers off in those versions, so they stay until those versions are no longer in use, and then they go and the keys are revoked. A change reaches a phone that has the app open within moments, and any other the next time it fetches (at most about an hour). Nothing here needs a release. Whatever is missing or wrong falls back to what the app ships with: a slip in the console cannot break the app.

## Switches and numbers

| Parameter | Type | What it does |
|---|---|---|
| `gemini_enabled` | Boolean | AI answers on or off for everyone. |
| `gemini_model` | String | The model asked. Blank or absent: the app's own choice. |
| `gemini_backend` | String | Which of Google's two services Firebase passes a request on to: `agent_platform` (or `vertex`) for the Agent Platform Gemini API, anything else or absent for the Gemini Developer API. It must be the one the project is set up for in the console, under AI Logic. |
| `gemini_limited_use_tokens` | Boolean | Whether each request carries an App Check token made for it alone, which is what replay protection in App Check needs. Off when absent: each such token costs a Play Integrity check, which takes a few seconds of the 4.5 an answer is waited for and counts against Play Integrity's daily allowance for the whole app. Turn it on, see that answers still come, and only then enforce replay protection. |
| `gemini_daily_limit` | Number | AI answers one device may ask for in a day: 20 when absent. |
| `gemini_daily_limit_premium` | Number | The same for whoever has Premium: 40 when absent, and never less than `gemini_daily_limit`, whatever is set here. The sheet that sells Premium promises more answers a day, so this stays above the other. |
| `ai_remark_enabled` | Boolean | Whether a second line may come under an answer to a spoken question. |
| `styles_hidden` | String | Styles not offered at all, same format. |
| `ads_enabled` | Boolean | Ads on or off. Whoever has Premium is shown none either way, and is not offered the one for more answers. |
| `ads_min_answers`, `ads_min_interval_seconds` | Number | How many answers and how much time between two ads. |
| `rewarded_ai_enabled`, `rewarded_ai_answers` | Boolean, Number | Whether an ad can be watched for more AI answers, and how many it gives. |
| `daily_verdict_enabled`, `daily_verdict_hour` | Boolean, Number | The day's verdict notification, and the hour it comes at (0 to 23, device time). |
| `notifications_ask_after_days` | Number | Days of use before the app asks to send notifications. |
| `streak_min_days_shown` | Number | The shortest run of days the app mentions. |
| `ball_sway_enabled` | Boolean | Whether the ball leans with the phone. |
| `account_enabled` | Boolean | Whether Options offers signing in with Google, and with it the profile, answers made for whoever is signed in, and "Create" on the sheet of characters, for characters of their own. Off when absent. It also stays hidden in a build that has no Google sign-in set up, whatever this says. Turn it on only once the three things under "Before the account is turned on" are true. Turning it off again sends nothing more of anyone's profile to the AI service, and deletes nothing. |
| `premium_enabled` | Boolean | Whether making characters of one's own is kept for those who subscribe to Premium: anyone else who touches "Create" is shown what subscribing brings. Off when absent. Turn it on only once everything under "Before Premium is turned on" is true: until Google Play has a price for it, the button on that sheet names none and does not answer. Whoever has Premium keeps it with this off, a subscription goes on renewing, and characters already made go on answering either way. |

## Before the account is turned on

`account_enabled` puts people's first names and signs in what the AI service is asked, and what they wrote of a character they made up, and keeps their profiles and those characters in the project. Three things have to be true first:

1. **The AI service is the paid one.** Its terms forbid sending personal data through the unpaid service, and ask for the paid one for anyone in the European Economic Area, the United Kingdom or Switzerland.
2. **The rules are in the project.** `firestore/firestore.rules` is what keeps each profile and each account's characters to their owner, and an account to five characters: `firebase deploy --only firestore:rules`. Until then the project refuses everything, the app cannot tell whether an account has a profile, and nobody is asked for one. Nor can anything of an account be deleted, the account included: the rules go into the project before accounts are turned on, and again before any release that changes them. After any change to the rules, `firebase emulators:exec --only firestore --project demo-magic-ball "python3 firestore/rules_test.py"` says whether they still hold.
3. **The privacy policy says so.** The one published has to be the one that describes the account, the profile and what is sent of it.

One thing the app cannot do by itself, and someone has to: **an account that was never given a profile, and could not be deleted, stays in Authentication.** It happens when the app is closed on the screen that asks for a profile and never opened again, or when the account could not be reached to be deleted and the sign-in had gone stale by the next start. Such an account holds what Google gave at sign-in and nothing else. Any user in Authentication with no document under `users/` a day after it was created is one of these, and is to be deleted there.

## Before Premium is turned on

`premium_enabled` keeps making characters of one's own for whoever has Premium: one subscription in Google Play, `premium_subscription`, with one base plan, `monthly`, which renews until it is cancelled. Its price and how often it is charged are set in Play Console and nowhere else: the app shows what Google Play tells it, and sells only that base plan at its own price, never a trial or a discount added beside it. The app never takes its own word for a purchase. It hands the token Google Play gives it to the project's own server, in `functions/`, which asks Google whether it is paid for and up to when, and writes down which account holds it, at `users/{id}/grants/premium`; the app reads that and can write none of it. A purchase belongs to the first account that claims it for as long as that account is there, and is free again once the account is deleted.

Premium ends when the subscription does, and it is the server that ends it. Google Play publishes a notification to the project whenever a subscription changes; the server then asks Google Play how it stands. Active, in its grace period (Google Play still retrying a payment that failed), or cancelled with time left that was paid for: Premium stays, up to the date Google Play gives. Expired, which is also how a revoked or refunded one reads, on hold or paused: the grant is deleted, and if the subscription comes back the app claims it again the next time it starts. Notifications were chosen over asking Google Play on a schedule because a schedule needs every purchase token kept to ask with, and the token is what claims a purchase: it is written nowhere, and a notification brings it. They are also what Google's own documentation says to follow, and they end Premium within moments and not within a day. Should Google Play never be heard from, as when the notifications are not set up, Premium still ends: once a day the server lets go of any subscription three days past the date it was paid up to, and one that had in fact renewed is claimed again by the app.

Premium was first sold as a single payment, the one-time product `premium`. It is sold no more. Whoever bought it keeps it for good: their grant has no end, the app still finds the purchase and the server still checks it, and what Google Play refunds or charges back of those the server takes back within a day.

All of this has to be true first, in this order:

1. **Accounts are on.** Everything under "Before the account is turned on": a purchase is held by an account, and nobody is shown the price without one.
2. **The project pays as it goes.** Cloud Functions need the Blaze plan.
3. **The server may ask Google Play.** In the project's Google Cloud console, enable "Google Play Android Developer API". In Play Console, under Users and permissions, invite the account the functions run as, `PROJECT_NUMBER-compute@developer.gserviceaccount.com`, with "View financial data, orders, and cancellation survey responses" and "Manage orders and subscriptions". It can take a day for Google Play to honour it.
4. **The server and the rules are in the project.** With Node 22 or later: `cd functions && npm ci && npm test`, then `firebase deploy --only functions,firestore:rules`. Five functions, all in `europe-west1`: `claimPremium`, which the app calls; `playNotified`, which Google Play's notifications set off; `endLapsed` and `revokeVoided`, once a day each; `forgetAccount`, when an account is deleted, which also clears whatever that account left behind.
5. **Google Play tells the server.** `playNotified` listens to the Pub/Sub topic `play-notifications`. In the Google Cloud console, under Pub/Sub, check that the topic is there after the deploy and create it under that name if it is not; open its permissions and add `google-play-developer-notifications@system.gserviceaccount.com` with the role "Pub/Sub Publisher". In Play Console, under Monetize with Play, Monetization setup, Real-time developer notifications: enable them, enter the topic as `projects/PROJECT_ID/topics/play-notifications`, choose subscriptions and all voided purchases, and send the test message, which Play Console says whether it could publish.
6. **The subscription is in Google Play.** A payments profile first, without which nothing can be sold. Then, under Monetize with Play, Products, Subscriptions: create a subscription with the product id `premium_subscription`; add a base plan with the id `monthly`, auto-renewing, with its billing period and its price, and activate it. Neither id can be changed or used again afterwards, and the app looks for exactly these. No offer is needed: the app would not sell one. If the one-time product `premium` was ever made there, deactivate it: versions of the app from before the subscription would otherwise go on selling Premium for good. A version of the app that has the billing library has to be in a testing track before Google Play will name a price to it. Subscribe with a licence tester's account, which is charged nothing and renews every few minutes: see `grants/premium` appear under that account with an `until`, see `until` move on at each renewal, cancel it and see the grant go when it runs out.
7. **The policy and the store listing say so.** The privacy policy and the Data safety form have to tell of the subscription: the server keeps, with the account, the order number, the date it is paid up to and a fingerprint of the purchase, never the token itself, and deletes them with the account. The store listing has to say that Premium is a subscription.

Then turn `premium_enabled` on. Once the version that sends App Check tokens is the one in people's hands and App Check shows the claims arriving verified, set `ENFORCE_APP_CHECK` in `functions/src/index.ts` and deploy again.

Turning `premium_enabled` off again stops the sale and takes nothing from anyone: who has Premium keeps the larger allowance of AI answers and sees no ads, and everyone can make characters as before. It cancels no subscription: those go on renewing, the server goes on following them, and "Manage subscription" stays under the account. To stop charging people, deactivate the base plan in Play Console, which stops new purchases, and cancel the subscriptions there.

Three things to know. It is the app that asks for payment before a character is made, not the rules: they cannot see `premium_enabled`, so an app that was tampered with could still make one. Once Premium is on for good, a rule that asks for `grants/premium` before a character is created closes that. A purchase goes to the first account that claims it: on a phone shared by two accounts of the app, a payment that was pending for one and comes through while the other is signed in is granted to the other. And a subscription that comes back from being on hold or paused is Premium again only once the app has been opened, signed in, on a device with that Google Play account.

## `answers_json`: the answers the app keeps

The lines used when the AI is off, away or used up. Same shape as `core/data/src/main/assets/magic_8_ball_responses.json`: a language, then a style, then the list. A list given here takes the place of the app's list for that language and style; the rest stay.

```json
{ "es": { "pirate": ["Sí. La mitad es mía.", "No. A fregar la cubierta."] } }
```

Each line: at most 28 characters and 6 words, so it fits the ball. The Classic style's twenty answers are never replaced. Nor are the lines that speak to whoever is asking, by name, by sign or on their birthday: those come only with the app, in `magic_8_ball_personal.json`, and a list given here takes the place of the ordinary lines alone.

## `characters_json`: who the characters are

What the AI is told about each character, and how an answer is asked for. Any part may be given and nothing else needs to be there.

```json
{
  "rules": {
    "form": "Give that answer as only this character would. Three to five words, six at the very most."
  },
  "characters": {
    "pirate": {
      "who": "A weathered pirate captain who has taken the asker on as the newest deckhand.",
      "wants": "Loyalty to the crew, and a cut of whatever the asker gains.",
      "yes": "An order to set sail at once, and a reminder of the captain's share.",
      "no": "A refusal with a cartoon threat: scrubbing the deck, or the plank.",
      "dodge": "Fog on the spyglass; or the parrot must be consulted first.",
      "habits": "Gruff, hearty, superstitious.",
      "never": "Modern words, or any real violence.",
      "angles": ["the captain wants a cut", "they owe the crew rum", "it suspects a mutiny"],
      "lines": {
        "es": {
          "yes": ["¡Zarpa ya! La mitad es mía.", "Sí. Y me debes ron."],
          "no": ["¡No! A fregar la cubierta.", "Jamás, grumete."],
          "maybe": ["Niebla. Tú sigue remando.", "Pregúntale al loro."],
          "remarks": [{ "q": "¿Pido un aumento?", "a": "Pídelo, y la mitad del aumento es del capitán." }]
        }
      }
    }
  }
}
```

**A character** is named by its style in lower case (`sarcastic`, `mystical`, `grandma`, `pirate`, `philosopher`, `coach`, `lovestruck`, `drama`, `robot`, `detective`, `conspiracy`, `cat`, `zoomer`, `villain`, `poet`, `astro`, `therapist`, `dog`). Classic has no character and takes none.

- `who`, `wants`, `yes`, `no`, `dodge`, `habits`, `never`: a sentence or two each, in English, at most 600 characters. Write what the character does, not its catchwords in quotes: a quoted English word ends up in a Spanish answer.
- `angles`: what the character may bring up when nothing was asked. At least 3, each at most 120 characters.
- `lines`, by language (`en`, `es`, `pt`): lines the character has said, shown to the AI two at a time as its voice. `yes`, `no` and `maybe` need at least 2 lines each, of at most 6 words and 40 characters. `remarks` pair a question with the comeback to it, the comeback of at most 12 words.

**Rules** say how an answer is written, whoever writes it: `craft`, `form`, `examples`, `remark`, `question_craft`, each at most 1200 characters. Their texts as shipped are in `core/domain/.../policy/CharacterBook.kt`. What guards against the question, what a toy does not answer and the limit of six words are not among them and cannot be changed from here.

A value that breaks one of these limits is left out and the app's own is used in its place. A file that is not valid JSON is ignored whole.

## What still needs a release

A new style (its name in three languages, its colour and what the ball wears), the texts of the screens, and anything about how the app looks.
