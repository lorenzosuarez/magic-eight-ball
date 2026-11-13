# Play Console checklist for the account feature

What has to change in Google Play Console, and around it, when sign-in, the profile and the user's own characters ship. Each answer comes from a Google page read on 13 November 2026; its address is next to it. Play Console itself was not opened to write this, so a question may be worded a little differently on screen.

This is not legal advice. Google's forms are the developer's own declarations: read each answer against what the released build really does before sending it.

## When

- **Together with the feature, not before.** The policy and the delete page describe an app with accounts; published earlier they would be false.
- **"Released" includes closed testing.** The Data safety form covers closed, open and production tracks; only internal testing is exempt. So everything here is due when the first build with accounts leaves internal testing. ([Data safety](https://support.google.com/googleplay/android-developer/answer/10787469), "Which developers need to complete the Data safety form")

On that day, in this order:

1. Replace `RELEASE-DATE` with the date in `docs/PRIVACY_POLICY.md`, `docs/PRIVACY_POLICY.es.md` and `docs/PRIVACY_POLICY.pt.md`. The site does not build until this is done.
2. Check the words the pages quote from the app against the final strings: "Options", "Account", "Delete account", "Sign out" and their Spanish and Portuguese forms (the Portuguese ones, "Conta", "Excluir conta" and "Sair", are a guess).
3. `firebase deploy --only hosting`, then open `/privacy` and `/delete-account` in the three languages.
4. Send the Play Console changes below.
5. Roll the build out.

## 1. Privacy policy

*App content → Privacy policy.* The address does not change: `https://magic-ai-8-ball.web.app/privacy`.

The User Data policy lists what a privacy policy must have: developer information and "a privacy point of contact or a mechanism to submit inquiries", the data collected and who receives it, how it is kept safe, the "data retention and deletion policy", and a title that says it is a privacy policy. ([User Data](https://support.google.com/googleplay/android-developer/answer/10144311), "Privacy Policy")

- [ ] **Weak point: the contact.** The policy and the delete page send people to "the contact details on the app's Google Play listing". A page for people who no longer have the app should print an address. Decide which address, and put it in the three policies and the three delete pages.

## 2. Data safety

*App content → Data safety.* ([Data safety](https://support.google.com/googleplay/android-developer/answer/10787469))

How the page defines the words, since the answers depend on them:

- **Collected** is "transmitting data from your app off a user's device". What stays on the device ("Recents", settings, party names) is not declared.
- **Shared** is transferring it "to a third party". Handing it to a service provider that "processes it on behalf of the developer" is not sharing. Firebase describes Google as processor or service provider for it ([Privacy and Security in Firebase](https://firebase.google.com/support/privacy)), and the paid Gemini service is processed under Google's data processing addendum ([Gemini API terms](https://ai.google.dev/gemini-api/terms), "Paid Services"). AdMob is different: Google's own page says its SDK collects **and shares** ([Mobile Ads SDK disclosure](https://developers.google.com/admob/android/privacy/play-data-disclosure)).
- **Optional** only where the user "can use the app without providing it", for every user in every region. Everything tied to the account is optional, because the account is.
- What an SDK collects counts as the app's. Data kept under a random identifier still counts.

### New with the account

| Data type | What it is here | Collected | Shared | Optional or required | Purposes |
|---|---|---|---|---|---|
| Personal info → **Name** | The name Google gives at sign-in (Firebase Authentication). The first name in the profile (Firestore), sent to Gemini when the user agreed | Yes | No | Optional | Account management, App functionality, Personalization |
| Personal info → **Email address** | From the Google sign-in (Firebase Authentication) | Yes | No | Optional | Account management |
| Personal info → **User IDs** | The Firebase account identifier. Firebase attaches the signed-in account's token to Firestore and AI requests ([Firebase disclosure](https://firebase.google.com/docs/android/play-data-disclosure)) | Yes | No | Optional | Account management, App functionality |
| Personal info → **Other info** | Date of birth (the page's own example for this type). Kept in Firestore; the zodiac sign and "today is the birthday" worked out from it go to Gemini when the user agreed | Yes | No | Optional | App functionality, Personalization |
| App activity → **Other user-generated content** | The name and description of a character the user wrote (Firestore, and Gemini when it answers) | Yes | No | Optional | App functionality, Personalization |
| Device or other IDs → **Device or other IDs** | Add a purpose to the existing row: the Play Integrity token that App Check sends | Yes | (as today) | Required | add: Fraud prevention, security, and compliance |

None of it is "processed ephemerally": it is stored.

- [ ] **Judgement call: the profile photo.** The app keeps only the address of the Google profile picture; it uploads no photo. This list does not declare "Photos". The cautious alternative is to declare Photos as collected, optional, for Account management. Simpler still: do not ask Google for the photo if the app does not show it.

### Already true today: compare with what the form says now

These do not change with the account, but the form has to be right as a whole.

| Data type | What it is here | Collected | Shared | Optional or required | Purposes |
|---|---|---|---|---|---|
| App activity → **App interactions** | Usage counts (Google Analytics for Firebase). Taps and views of ads (AdMob) | Yes | Yes, by AdMob | Required: outside the regions where consent is asked there is no switch | Analytics, Advertising or marketing, Fraud prevention |
| App activity → **Other user-generated content** | The text of a question asked out loud, sent to Gemini. Google logs requests for a limited time, so it is not ephemeral | Yes | No | Optional | App functionality |
| App info and performance → **Crash logs** | Crashlytics | Yes | No | Required | Analytics |
| App info and performance → **Diagnostics** | Crashlytics session records. AdMob diagnostics | Yes | Yes, by AdMob | Required | Analytics, Advertising or marketing, Fraud prevention |
| Device or other IDs → **Device or other IDs** | Firebase installation identifiers (Remote Config, Messaging, Crashlytics, Analytics). Advertising ID and app set ID (AdMob) | Yes | Yes, by AdMob | Required | App functionality, Analytics, Developer communications, Advertising or marketing, Fraud prevention |
| Location → **Approximate location** | Worked out from the IP address by AdMob. The page says location inferred from an IP address "must be disclosed here" | Yes | Yes, by AdMob | Required | Advertising or marketing, Analytics, Fraud prevention |

- [ ] **Judgement call: voice.** The app's own code sends no audio; the device's speech recognition service hears the question, and may send it to its provider. The Data safety page says nothing about data handled by a system service (looked for, not there). This list does not declare "Voice or sound recordings". If the form declares it today, leave it.

### The other questions on the form

- [ ] **Is all collected data encrypted in transit?** Yes. Firebase: "encrypts the data in transit using HTTPS" ([Firebase disclosure](https://firebase.google.com/docs/android/play-data-disclosure)); AdMob states TLS.
- [ ] **Can users ask for their data to be deleted?** Yes.
- [ ] **Does the app let users create an account?** Yes. The page does not name Google sign-in, but it defines an app account as a user identity the app offers and lists single sign-on among the ways in, and it applies even when the app also works without an account. Choose the option for signing in through another provider; the options themselves are not printed on the Help page. ([Account deletion requirements](https://support.google.com/googleplay/android-developer/answer/13327111), "What is an app account?")
- [ ] **Delete account URL:** `https://magic-ai-8-ball.web.app/delete-account`
- [ ] **Can users delete some data without deleting the account?** Optional to answer. Yes is true (characters one by one, and the profile); the same page has a section on it, so give the same address.

What Google asks of the page behind that address, and how the page answers ([Account deletion requirements](https://support.google.com/googleplay/android-developer/answer/13327111), "What are the requirements for the web link and resource?"):

| Asked | The page |
|---|---|
| Loads without error, and the way to delete is easy to find | Its only subject |
| Names the app or the developer "as it appears on your store listing" | Says "Magic AI Ball" and "Lorenzo Suarez": check both against the listing |
| Lets the user ask without going back to the app; an email address is accepted | "Without the app": write to the developer. See the weak point in section 1 |
| Data kept after deletion is disclosed | "What is not deleted" |
| Deletion is real: "Temporary account deactivation, disabling, or 'freezing' the app account does not qualify as account deletion." ([User Data](https://support.google.com/googleplay/android-developer/answer/10144311), "Account Deletion Requirement") | The app must delete the Firebase Authentication user and the Firestore documents, not mark them |

## 3. Target audience

*App content → Target audience and content.* ([Target audience](https://support.google.com/googleplay/android-developer/answer/9867159))

- [ ] **Target age:** "18 and over", and no other group.
- [ ] **Restrict Minor Access:** a box on the same screen, offered when 18 and over is the only group. People Google takes to be under 18, from "the age provided in their Google Account" or its own signals, can then not find, download or buy the app; those who already have it keep using it. Google requires the box only for gambling, dating, random chat with strangers and anonymous messaging ([Age-Restricted Content and Functionality](https://support.google.com/googleplay/android-developer/answer/16302250)), so for this app it is a choice. It fits the reason for 18 and over: the Gemini terms forbid an app "directed towards or is likely to be accessed by individuals under the age of 18" ([Gemini API terms](https://ai.google.dev/gemini-api/terms), "Age Requirements"). The cost is fewer installs.
- [ ] **The listing must not look made for children.** If the store listing has elements that "may appeal to children", such as young characters or youthful animation, "Google Play may reject your app". Look at the screenshots and the icon with that in mind: the drawn characters are the risk.
- The page does not ask an adults-only app for an age screen. The date of birth the app asks is the developer's own check. Where Google describes a neutral age screen, it is one where the user types the date freely and nothing is filled in beforehand.

## 4. Content rating

*App content → Content ratings.* ([Content ratings](https://support.google.com/googleplay/android-developer/answer/9859655))

The questionnaire has to be filled in again for "All app updates where there has been a change to your content or features that would affect the responses to the questionnaire." The Help page does not print the questions, so go through them once more with these facts:

- People cannot talk to each other or send each other anything. Characters are seen only by who made them.
- The app now asks for personal information (name, email address, date of birth) and gives it to no other user.
- Text is written by generative AI and nobody reads it before it is shown; every such answer has a "Report" button.
- What a user writes in a character's description changes what the AI writes back to that same user.
- No location, no purchases.

- [ ] Fill it in again if any answer changes. The rating is a separate thing from the target audience: declaring 18 and over does not make the rating 18.

## 5. Sign-in details

*App content → Sign-in details.* ([Prepare your app for review](https://support.google.com/googleplay/android-developer/answer/9859455))

"If your entire app or parts of your app are restricted based on login credentials, sign in details, memberships, location, or other forms of authentication, you must provide all required details to enable access to your app." Creating a character needs an account, so part of the app is behind a sign-in.

- [ ] Add instructions: sign in with any Google Account from Options → Account; enter any date of birth of an adult; "Create" is in the Characters sheet. If Google asks for an account of its own, make a test Google Account for it and keep its password out of the repository.

## 6. AI-generated content

([AI-Generated Content](https://support.google.com/googleplay/android-developer/answer/13985936))

"Apps that generate content using AI must contain in-app user reporting or flagging features", which "allow users to report or flag offensive content to developers without needing to exit the app."

- [x] Already there: "Report" under an answer written by AI, on the phone and on the watch.
- [ ] Check in the build that "Report" also shows under an answer given by a character the user made.

The User Generated Content policy applies to content "visible to or accessible by at least a subset of the app's users". Private characters are outside it. They would be inside it the day a character can be shared. ([User Generated Content](https://support.google.com/googleplay/android-developer/answer/9876937))

## 7. Ads

No change: the app contains ads.

## 8. Outside Play Console, the same day

- [ ] **Google Cloud console, OAuth consent screen:** the app's name, and the privacy policy address. Google asks developers to "list the privacy policy URL in your OAuth client configuration when your application is made available to the public", and says "Don't request access to information that you don't need." ([Google API Services User Data Policy](https://developers.google.com/terms/api-services-user-data-policy))
- [ ] **Firebase console:** the steps in the feature's plan (Blaze plan, Authentication with Google, Firestore and its rules, App Check with Play Integrity). Firestore was created in `eur3`, Google's Europe multi-region (Belgium and the Netherlands, with Finland as witness), and the policy says the profile, the agreement and the characters are kept in the European Union. A database's location cannot be changed afterwards, and a second database anywhere else would make that sentence false. ([Cloud Firestore locations](https://firebase.google.com/docs/firestore/locations)) App Check may be registered without being enforced: the policy says only that its token tells whether a request comes from the app, which holds either way.

## 9. When a deletion request arrives by email

The delete page promises an answer to the same address and deletion within 30 days.

1. The message must come from the address of the account. Reply to that address and wait for the answer before deleting.
2. Firebase console → Authentication → Users: find the address, copy the User UID.
3. Firestore: delete `users/{UID}`, and make sure its `characters` collection goes with it.
4. Authentication → Users: delete the user.
5. Reply that it is done.

## 10. Age laws: not settled here

For a lawyer, not for this list. Facts from Google's pages to bring to that conversation:

- Google Play has a Play Age Signals API (beta) that tells an app the age range Google holds for a user. Its terms allow that data to be used only to give an age-appropriate experience, never for advertising, marketing, profiling or analytics. ([Play Age Signals](https://developer.android.com/google/play/age-signals/overview))
- Google's page on the United States names Texas SB 2420 as in effect and says similar laws are coming in other states. Where else the API answers today, Brazil included, could not be confirmed on the pages read: check its documentation before relying on it.
- "Google Play doesn't mandate the use of these features", and says it is for each developer to work out how the laws apply to their app. ([Changes to Google Play for upcoming app store bills for users in applicable US states](https://support.google.com/googleplay/android-developer/answer/16569691))
