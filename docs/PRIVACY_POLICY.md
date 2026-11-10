# Privacy Policy for Magic 8 Ball (Wear OS and Android phones and tablets)

**Effective date:** November 10, 2026

This policy explains how the Magic 8 Ball app (package `com.lorenzosuarez.magiceightball`), developed by Lorenzo Suarez, handles information. It covers both versions of the app: the one for Wear OS watches and the one for Android phones and tablets. Where they differ, this policy says so.

## Summary
- No account or sign-in, no ads, no analytics and no crash-reporting tools.
- The only things the app saves on your device are your choices in the app (language, personality, whether to use AI answers and, on phones and tablets, whether to use voice questions). The developer runs no server that receives your data.
- You can switch AI answers off in the app at any time.
- The app connects to two Google services, both described below: Firebase Remote Config, to check its settings, and, when AI answers are switched on, the Gemini API.
- **Phones and tablets only:** you can ask your question out loud. The app uses the microphone only while you hold the screen to ask, your speech is turned into text by your device's speech recognition service, and that text is sent to the Gemini API only when AI answers are switched on. The app does not store your voice or your question. You can switch voice questions off, and the app works without the microphone.
- **The watch app** has no microphone feature and never sends anything you say or write.

## What the app sends, and to whom
**Firebase Remote Config.** When the app starts, and at most about once an hour while you use it, it asks Google's Firebase Remote Config service for its current settings, such as whether AI answers are switched on. This request includes a random installation identifier created by Firebase for this copy of the app, and basic app and device information (for example app version, language, country and operating system version). It does not include your name, account or location. Google handles this under the [Firebase privacy and security terms](https://firebase.google.com/support/privacy).

**Gemini API.** AI answers are used only when both you and the developer have them switched on: you choose on the "AI answers" page of the app (on by default), and the developer can switch them on or off for everyone. When the developer has them switched off, that page shows them as not available. While AI answers are off, by your choice or the developer's, the app shows answers from a list built into the app and sends nothing to the Gemini API. While they are on, asking the ball sends a request over an encrypted HTTPS connection to Google's Gemini API (`generativelanguage.googleapis.com`). That request contains only:
- a fixed instruction written by the developer, which reflects the personality (Sarcastic or Mystical) and answer language (English or Spanish) you chose in the app;
- on the watch, and on a phone or tablet when you tap or shake: the fixed word "shake";
- on a phone or tablet when you asked out loud and your question was recognised: the text of that question (at most 200 characters) instead of the fixed word, so that the answer responds to it;
- technical generation settings and the developer's API key.

The watch app has no text or voice input. On both versions, the request never includes audio, and it does not include your name, account, contacts, location, device identifiers or sensor readings. Please do not say anything in a question that you would not want to send to Google. As with any internet request, Google receives your device's IP address and standard connection information. Google handles these requests under the [Gemini API Additional Terms of Service](https://ai.google.dev/gemini-api/terms) and the [Google Privacy Policy](https://policies.google.com/privacy). Depending on how the API is accessed, Google may keep these requests for a period of time and use them to provide and improve its services.

If the request fails or your device is offline, the app shows an answer from the built-in list instead.

## Voice questions (phones and tablets only)
The phone and tablet app lets you hold the screen and say your question. This is optional: you can switch "Voice questions" off in the app's settings, or decline the microphone permission, and then the app answers to a tap or a shake only.

- **When the microphone is used.** Only while you are holding the screen to ask (or, with an accessibility service, between choosing "Ask by voice" and the end of your question), and only while the app is on screen. The app does not listen at any other time and never in the background.
- **Who turns your speech into text.** The app does not process or record audio itself. It hands the listening to the speech recognition service of your device, through Android's standard speech recognition feature. Where your device offers on-device recognition for the app's language (Android 12 and later), the app uses it, and the audio is processed on the device. Otherwise the app uses your device's default speech recognition provider, which is typically Google and which may send the audio to its own servers to recognise it. That provider handles the audio under its own privacy policy (for Google, the [Google Privacy Policy](https://policies.google.com/privacy)); you can see or change the provider in your device's settings.
- **What happens to the text.** The recognised question is shown on screen under the ball. If AI answers are switched on, it is sent to the Gemini API as described above; if they are off or not available, it is not sent anywhere by the app.
- **What is kept.** Nothing. The app does not save audio or the recognised question to storage and has no history: the question stays in memory only until you ask again or the app is closed.

## Sensors and permissions
- **Motion sensor (accelerometer):** used only on your device to detect a shake. Sensor readings are not stored or sent anywhere. No permission is required.
- **Internet and network state** (`INTERNET`, `ACCESS_NETWORK_STATE`): used to contact Firebase Remote Config and the Gemini API.
- **Microphone** (`RECORD_AUDIO`), phones and tablets only: requested the first time you hold the screen to ask out loud, and used only as described under "Voice questions". You can refuse it or take it back in your device's settings at any time; the app keeps working by tap and shake.
- **Wake lock** (`WAKE_LOCK`), watch only: declared by the app but not used by the current version.

The watch app does not request the microphone. Neither version requests location, camera, contacts, body sensors or any other sensitive permission.

## Information stored on your device
The app saves your choices (language, personality, whether to use AI answers and, on phones and tablets, whether to use voice questions) in a small settings file on your device, so they are still there the next time you open the app. They stay on the device, are not sent anywhere on their own, and are removed when you clear the app's data or uninstall it. The latest answer, and on phones and tablets the latest recognised question, are kept in memory only. The app stores nothing else of its own. Firebase stores its installation identifier and the latest settings on the device. Nothing is included in device backups.

## Retention and deletion
The developer does not collect or store your information, so there is nothing for the developer to keep or delete. Information received by Google, or by your device's speech recognition provider, is handled under their policies as described above.

## Children
The app is not directed at children, and the developer does not knowingly collect personal information from children.

## Changes to this policy
If this policy changes, the updated version will be posted here with a new effective date.

## Contact
For questions about this policy, contact the developer through the contact details on the app's Google Play listing.
