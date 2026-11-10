# Privacy Policy for Magic 8 Ball (Wear OS and Android phones and tablets)

**Effective date:** November 10, 2026

This policy explains how the Magic 8 Ball app (package `com.lorenzosuarez.magiceightball`), developed by Lorenzo Suarez, handles information. It covers both versions of the app: the one for Wear OS watches and the one for Android phones and tablets. Where they differ, this policy says so.

## Summary
- No account or sign-in, no ads, no analytics and no crash-reporting tools.
- The only things the app saves on your device are your choices in the app (language, personality, whether to use AI answers and, on phones and tablets, whether to use voice questions) and, on phones and tablets, a list of your latest questions and answers ("Recents"). The developer runs no server that receives your data.
- You can switch AI answers off in the app at any time.
- The app connects to two Google services, both described below: Firebase Remote Config, to check its settings, and, when AI answers are switched on, the Gemini API.
- **Asking out loud:** on both versions you can hold and say your question. The app uses the microphone only while it is listening to a question you started, your speech is turned into text by your device's speech recognition service, and that text is sent to the Gemini API only when AI answers are switched on. The app never stores your voice. On phones and tablets the text of your question is saved, with its answer, only in the "Recents" list on your device, which you can clear at any time; the watch saves nothing. The app works without the microphone.
- **On the watch** holding to ask is offered only while AI answers are in use, because only the AI service can answer a question; otherwise the watch answers to a shake and never listens.

## What the app sends, and to whom
**Firebase Remote Config.** When the app starts, and at most about once an hour while you use it, it asks Google's Firebase Remote Config service for its current settings, such as whether AI answers are switched on. This request includes a random installation identifier created by Firebase for this copy of the app, and basic app and device information (for example app version, language, country and operating system version). It does not include your name, account or location. Google handles this under the [Firebase privacy and security terms](https://firebase.google.com/support/privacy).

**Gemini API.** AI answers are used only when both you and the developer have them switched on: you choose on the "AI answers" page of the app (on by default), and the developer can switch them on or off for everyone. When the developer has them switched off, that page shows them as not available. While AI answers are off, by your choice or the developer's, the app shows answers from a list built into the app and sends nothing to the Gemini API. While they are on, asking the ball sends a request over an encrypted HTTPS connection to Google's Gemini API (`generativelanguage.googleapis.com`). That request contains only:
- a fixed instruction written by the developer, which reflects the answer style (Sarcastic, Mystical or Neutral) and answer language (English or Spanish) you chose in the app;
- when you shake, or tap on a phone or tablet: the fixed word "shake";
- when you asked out loud and your question was recognised: the text of that question (at most 200 characters) instead of the fixed word, so that the answer responds to it;
- technical generation settings and the developer's API key.

On both versions the request never includes audio, and it does not include your name, account, contacts, location, device identifiers or sensor readings. Please do not say anything in a question that you would not want to send to Google. As with any internet request, Google receives your device's IP address and standard connection information. Google handles these requests under the [Gemini API Additional Terms of Service](https://ai.google.dev/gemini-api/terms) and the [Google Privacy Policy](https://policies.google.com/privacy). Depending on how the API is accessed, Google may keep these requests for a period of time and use them to provide and improve its services.

If the request fails or your device is offline, the app shows an answer from the built-in list instead.

## Voice questions
On a phone or tablet you hold the ball and say your question; on the watch you hold the screen. This is optional. On a phone or tablet you can switch "Voice questions" off in the settings; on either device you can decline the microphone permission, and then the app answers to a tap or a shake only. The watch offers it only while AI answers are in use.

- **When the microphone is used.** Only while you are holding to ask (or, on a phone or tablet with an accessibility service, between choosing "Ask by voice" and the end of your question), and only while the app is on screen. The app does not listen at any other time and never in the background.
- **Who turns your speech into text.** The app does not process or record audio itself. It hands the listening to the speech recognition service of your device, through Android's standard speech recognition feature. Where your device offers on-device recognition for the app's language (Android 12 and later), the app uses it, and the audio is processed on the device. Otherwise the app uses your device's default speech recognition provider, which is typically Google and which may send the audio to its own servers to recognise it. That provider handles the audio under its own privacy policy (for Google, the [Google Privacy Policy](https://policies.google.com/privacy)); you can see or change the provider in your device's settings.
- **What happens to the text.** The recognised question is shown on screen. If AI answers are switched on, it is sent to the Gemini API as described above; if they are off or not available, it is not sent anywhere by the app (and the watch does not listen at all).
- **What is kept.** Audio is never saved. On phones and tablets the recognised question is saved, together with its answer, in the "Recents" list described below, on your device only. The watch keeps the question in memory only until the answer has been shown.

## Recents: your latest questions and answers (phones and tablets only)
The phone and tablet app keeps a list of the latest answers the ball gave you, so you can read them again. The watch app keeps no such list.

- **What is saved.** For each answer: the text of your question if you asked out loud (nothing for a tap or a shake), the answer, the date and time, and whether the answer was written by AI or came from the app's built-in list.
- **Where.** In a private file in the app's own storage on your device. It is not sent to the developer, to Google or to anyone else, it is not synchronised between devices, and it is not included in device backups or device-to-device transfers.
- **How much.** At most the latest 50 answers; older ones are deleted automatically as new ones arrive.
- **Your control.** You can delete any single entry (swipe it away or use its delete button; "Undo" brings it back for a few seconds), or all of them with "Clear all", from the list itself. Clearing the app's data or uninstalling the app also deletes the list.
- **Sharing.** The app has no way to send your questions or answers anywhere. "Share the app" in the settings hands only a short text with the app's Google Play link to the app you pick.

## Sensors and permissions
- **Motion sensor (accelerometer):** used only on your device to detect a shake. Sensor readings are not stored or sent anywhere. No permission is required.
- **Internet and network state** (`INTERNET`, `ACCESS_NETWORK_STATE`): used to contact Firebase Remote Config and the Gemini API.
- **Microphone** (`RECORD_AUDIO`): requested the first time you hold to ask out loud, and used only as described under "Voice questions". You can refuse it or take it back in your device's settings at any time; the app keeps working by tap and shake.

Neither version requests location, camera, contacts, body sensors or any other sensitive permission.

## Information stored on your device
The app saves your choices (language, personality, whether to use AI answers and, on phones and tablets, whether to use voice questions) in a small settings file on your device, so they are still there the next time you open the app. They stay on the device, are not sent anywhere on their own, and are removed when you clear the app's data or uninstall it. On phones and tablets the app also saves the "Recents" list described above (at most your latest 50 questions and answers), which is removed the same way or from the list itself. On the watch, the latest answer is kept in memory only. The app stores nothing else of its own. Firebase stores its installation identifier and the latest settings on the device. Nothing is included in device backups.

## Retention and deletion
The developer does not collect or store your information, so there is nothing for the developer to keep or delete. What the app saves on your device stays there until you delete it, as described above. Information received by Google, or by your device's speech recognition provider, is handled under their policies as described above.

## Children
The app is not directed at children, and the developer does not knowingly collect personal information from children.

## Changes to this policy
If this policy changes, the updated version will be posted here with a new effective date.

## Contact
For questions about this policy, contact the developer through the contact details on the app's Google Play listing.
