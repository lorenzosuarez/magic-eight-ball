# Privacy Policy for Magic AI Ball (Wear OS and Android phones and tablets)

**Effective date:** November 12, 2026

This policy explains how the Magic AI Ball app (package `com.lorenzosuarez.magiceightball`), developed by Lorenzo Suarez, handles information. It covers both versions of the app: the one for Wear OS watches and the one for Android phones and tablets. Where they differ, this policy says so.

## Summary
- No account or sign-in. On phones and tablets, and only with your consent where the law requires it, the app counts how it is used (never what you ask or what it answers), as described under "Usage measurement". The watch app measures nothing. If the app crashes, a technical crash report is sent to the developer, as described under "Crash reports". The watch app shows no ads. The phone and tablet app may show an occasional ad, as described under "Ads"; you are asked for consent first where the law requires it.
- The only things the app saves on your device are your choices in the app (language, answer style, whether to use AI answers and, on phones and tablets, whether to use voice questions and spoken answers, and the names you type for party mode) and, on phones and tablets, a list of your latest questions and answers ("Recents"). The developer runs no server that receives your data.
- An answer leaves the app for another app or person only when you choose "Share" under it, as described under "Sharing an answer".
- You can switch AI answers off in the app at any time.
- The app connects to Google services, all described below: Firebase Remote Config, to check its settings; the Gemini API, when AI answers are switched on; Firebase Crashlytics, when the app crashes; and, on phones and tablets, Firebase Cloud Messaging for notifications, Google Analytics for Firebase when your consent allows it and Google AdMob while ads are switched on.
- **Asking out loud:** on both versions you can hold and say your question. The app uses the microphone only while it is listening to a question you started, your speech is turned into text by your device's speech recognition service, and that text is sent to the Gemini API only when AI answers are switched on. The app never stores your voice. On phones and tablets the text of your question is saved, with its answer, only in the "Recents" list on your device, which you can clear at any time; the watch saves nothing. The app works without the microphone.
- **On the watch** holding to ask is offered only while AI answers are in use, because only the AI service can answer a question; otherwise the watch answers to a shake and never listens.

## What the app sends, and to whom
**Firebase Remote Config.** When the app starts, and at most about once an hour while you use it, it asks Google's Firebase Remote Config service for its current settings, such as whether AI answers are switched on. This request includes a random installation identifier created by Firebase for this copy of the app, and basic app and device information (for example app version, language, country and operating system version). It does not include your name, account or location. Google handles this under the [Firebase privacy and security terms](https://firebase.google.com/support/privacy).

**Gemini API.** AI answers are used only when both you and the developer have them switched on: you choose with "AI answers" in the app's options (on by default), and the developer can switch them on or off for everyone. When the developer has them switched off, that page shows them as not available. While AI answers are off, by your choice or the developer's, the app shows answers from a list built into the app and sends nothing to the Gemini API. The same is true while the "Classic" answer style is chosen: its answers are always the classic twenty built into the app, whatever else is switched on. While they are on, asking the ball sends a request over an encrypted HTTPS connection to Google's Gemini API (`generativelanguage.googleapis.com`). That request contains only:
- an instruction written by the developer, which reflects the answer style and answer language (English, Spanish or Portuguese) you chose in the app, together with what the app draws at random for each answer so that no two are alike: whether it is to be a yes, a no or undecided, and a theme and a turn of phrase taken from lists built into the app. Nothing in it comes from you;
- when you shake, or tap on a phone or tablet: the fixed word "shake";
- when you asked out loud and your question was recognised: the text of that question (at most 200 characters) instead of the fixed word, so that the answer responds to it;
- technical generation settings and the developer's API key.

On both versions the request never includes audio, and it does not include your name, the names typed for party mode, your account, contacts, location, device identifiers or sensor readings. Please do not say anything in a question that you would not want to send to Google. As with any internet request, Google receives your device's IP address and standard connection information. Google handles these requests under the [Gemini API Additional Terms of Service](https://ai.google.dev/gemini-api/terms) and the [Google Privacy Policy](https://policies.google.com/privacy). Depending on how the API is accessed, Google may keep these requests for a period of time and use them to provide and improve its services.

If the request fails or your device is offline, the app shows an answer from the built-in list instead. The same happens once a device has asked for its daily allowance of AI answers; to keep that count, the app saves today's date and a number on your device, and nothing else.

Each request also names the app that is making it (its package name and the fingerprint of its signing certificate), so that Google can refuse requests that do not come from this app.

## Crash reports
If the app stops working unexpectedly, it sends a crash report to Google's Firebase Crashlytics service the next time it is opened, so that the developer can find and fix the fault. Both versions of the app do this.

- **What a report contains.** The technical trace of the failure (where in the app's code it happened), the app version, the device model, the operating system version, the device's language and orientation, free memory and storage at the time, the date and time, and a random installation identifier created by Crashlytics for this copy of the app.
- **What it never contains.** Your questions, the ball's answers, your "Recents" list, audio, your name, account or location. The app adds nothing of its own to a report.
- **Who receives it.** Google, on the developer's behalf, under the [Firebase privacy and security terms](https://firebase.google.com/support/privacy). Reports are kept for 90 days and then deleted. They are used only to fix the app, never for ads.
- **When the app has not crashed.** Each time the app is opened it sends Crashlytics a short session record (app version, device model, operating system version and the same random installation identifier), which is what lets the developer see how many sessions end without a crash. It contains nothing about what you do in the app.

## Usage measurement (phones and tablets only)
To learn which parts of the app are used and whether a change made it better, the phone and tablet app counts a few things you do in it, with Google Analytics for Firebase. The watch app does not.

- **What is counted.** That the ball answered, and for that answer: whether you tapped, shook, spoke or used "Who of you…?", the answer style in use, whether the answer was written by AI or came from the built-in list, and whether it had a second line. That you shared an answer or the app. That you chose an answer style or turned one of the switches in the options, and which. That party mode was set up, and for how many players.
- **What is never counted.** Your questions, the ball's answers, the names typed for party mode, your "Recents" list, audio, or anything else you said or wrote. The app has no way to put them in what it counts.
- **What goes with it.** Google Analytics adds what it collects for every app: a random identifier created for this copy of the app, the app version, the device model and operating system, the language and country, and when and for how long the app was used. The app has switched off its collection of your advertising ID and of the device's own ID.
- **Your consent.** Where the law requires it (for example in the European Economic Area, the United Kingdom and Switzerland), nothing is counted unless you agreed to it in the consent message, to storing information on your device, to measuring and to improving the service. Until that message has been answered, nothing is counted either. You can change your choice at any time under "Ad privacy choices" in the options, and the counting follows it.
- **Not for advertising.** The app tells Google Analytics not to use these counts for advertising or for personalising ads.
- **Who receives it.** Google, on the developer's behalf, under the [Firebase privacy and security terms](https://firebase.google.com/support/privacy). The counts are kept for at most 14 months.
- **Shared links.** The Google Play link the app puts in what you share says that it was shared from the app, so the developer can see how many installs come from sharing. It says nothing about you.

## Notifications (phones and tablets only)
The developer can send the app's users a notification now and then, about something new in the app or as a reminder to come back, through Google's Firebase Cloud Messaging. The watch app receives none.

- **Your permission.** On Android 13 and later the app shows no notification unless you allow it. It asks once, and only after you have used the app on several days. You can change your answer at any time in your device's settings, under the app's notifications.
- **How a message reaches you.** Messages are sent to topics, not to people: every copy of the app follows one topic common to all and one for the language it is set to, so a message can be written in your language. To deliver them, Firebase Cloud Messaging gives this copy of the app a registration token, tied to a random installation identifier created by Firebase. Neither says who you are, and the app sends nothing of your questions, answers or choices with them, apart from the language.
- **Who receives it.** Google, on the developer's behalf, under the [Firebase privacy and security terms](https://firebase.google.com/support/privacy).

## Ads (phones and tablets only)
The phone and tablet app can show a full-screen ad from time to time, only after you have closed an answer and never while you are asking or reading one. The developer can switch ads on and off for everyone; while they are off, nothing described in this section happens. The watch app never shows ads.

- **An ad you choose to watch.** Once you have used the day's AI answers, the options may offer more of them in exchange for watching an ad. It is shown only if you ask for it, and it is requested and handled like the other ads described here.
- **Who shows them.** Ads are provided by Google AdMob. To request and show an ad, Google's advertising software in the app collects and uses information such as your device's advertising ID, IP address, approximate location derived from it, device and app information, and how you interact with ads. Google handles it under [its policy on how it uses information from apps that use its services](https://policies.google.com/technologies/partner-sites) and the [Google Privacy Policy](https://policies.google.com/privacy).
- **Your consent.** Where the law requires it (for example in the European Economic Area, the United Kingdom and Switzerland), the app asks for your choice before any ad is requested, through Google's consent message. You can change that choice at any time under "Ad privacy choices" in the app's settings. You can also reset or delete your advertising ID, or turn off ad personalisation, in your device's settings.
- **What ads never get.** Your questions, the ball's answers and your "Recents" list are never used for ads or shared with the ad service by the app.
- **Permission.** The advertising software adds the `com.google.android.gms.permission.AD_ID` permission to the phone and tablet app.

## Voice questions
On a phone or tablet you hold the ball and say your question; on the watch you hold the screen. This is optional. On a phone or tablet you can switch "Voice questions" off in the settings; on either device you can decline the microphone permission, and then the app answers to a tap or a shake only. The watch offers it only while AI answers are in use.

- **When the microphone is used.** Only while you are holding to ask (or, on a phone or tablet with an accessibility service, between choosing "Ask by voice" and the end of your question), and only while the app is on screen. The app does not listen at any other time and never in the background.
- **Who turns your speech into text.** The app does not process or record audio itself. It hands the listening to the speech recognition service of your device, through Android's standard speech recognition feature. Where your device offers on-device recognition for the app's language (Android 12 and later), the app uses it, and the audio is processed on the device. Otherwise the app uses your device's default speech recognition provider, which is typically Google and which may send the audio to its own servers to recognise it. That provider handles the audio under its own privacy policy (for Google, the [Google Privacy Policy](https://policies.google.com/privacy)); you can see or change the provider in your device's settings.
- **What happens to the text.** The recognised question is shown on screen, written as a question: a recogniser hears words and not the tone they were said in, so the app adds the question marks itself. If AI answers are switched on, it is sent to the Gemini API as described above; if they are off or not available, it is not sent anywhere by the app (and the watch does not listen at all).
- **What is kept.** Audio is never saved. On phones and tablets the recognised question is saved, together with its answer, in the "Recents" list described below, on your device only. The watch keeps the question in memory only until the answer has been shown.

## Spoken answers (phones and tablets only)
The phone and tablet app can read each answer out loud. This is off until you switch "Spoken answers" on in the options, and you can also have a single answer read with the speaker button shown while it is on screen.

- **Who speaks.** The app does not produce the voice itself. It hands the text of the answer, and nothing else, to the text-to-speech service of your device, through Android's standard feature. That service usually speaks on the device; depending on the voice installed, it may send the text to its provider's servers to produce the audio. The provider (typically Google) handles it under its own privacy policy, and you can see or change it in your device's settings.
- **What is never handed over.** Your question, your "Recents" list and the names typed for party mode. An answer that is one of those names, as described under "Party mode", is read out like any other answer.
- **What is kept.** Nothing: the app saves no audio.

## Party mode (phones and tablets only)
In the options you can type the names of the people playing, from two to eight. The app then shows whose turn it is and offers "Who of you…?", which answers with one of those names picked at random on the device.

- **Where the names are.** In the app's settings file on your device, and in "Recents" when a name was an answer. They are not sent to the developer, to the Gemini API or to anyone else, and "Who of you…?" contacts no service at all.
- **Your control.** "Turn off" in the party mode dialog deletes the names. Clearing the app's data or uninstalling the app does too.
- Please type first names or nicknames only, and only of people who are playing with you.

## Sharing an answer (phones and tablets only)
Under an answer, "Share" makes a picture of it and hands it to the app you pick from your device's share menu. Nothing is shared unless you choose it, each time.

- **What is in the picture.** The app's name, your question if you asked one out loud, the answer with the line the character added under it, and a line saying it was written by AI when it was.
- **What goes with it.** A short text with the app's Google Play link.
- **Where it goes.** Only to the app you pick, which handles it under its own terms. The developer receives nothing, and the app does not learn which app you picked.
- **What is kept.** The picture is saved as one file in the app's temporary storage so that the other app can read it, and is replaced by the next one you share. It is removed when you clear the app's cache or data or uninstall the app, and your device may remove it on its own.

## Home screen widget and watch tile
The phone and tablet app offers a home screen widget, and the watch app a tile, which show an answer when touched. Their answers always come from the list built into the app: they send nothing to the Gemini API, use no microphone, and are not saved in "Recents".

## Recents: your latest questions and answers (phones and tablets only)
The phone and tablet app keeps a list of the latest answers the ball gave you, so you can read them again. The watch app keeps no such list.

- **What is saved.** For each answer: the text of your question if you asked out loud (nothing for a tap or a shake; for "Who of you…?" in party mode, those words and the name that was picked), the answer, the date and time, and whether the answer was written by AI or came from the app's built-in list.
- **Where.** In a private file in the app's own storage on your device. It is not sent to the developer, to Google or to anyone else, it is not synchronised between devices, and it is not included in device backups or device-to-device transfers.
- **How much.** At most the latest 50 answers; older ones are deleted automatically as new ones arrive.
- **Your control.** You can delete any single entry (with its delete button; "Undo" brings it back for a few seconds), or all of them with "Clear all", from the list itself. Clearing the app's data or uninstalling the app also deletes the list.
- **Sharing.** The list itself is never sent anywhere. An answer on screen can be shared by you as described under "Sharing an answer". "Share the app" in the options hands only a short text with the app's Google Play link to the app you pick.

## Sensors and permissions
- **Motion sensor (accelerometer):** used only on your device to detect a shake and, on phones and tablets, to move the ball slightly as you move the device. Sensor readings are not stored or sent anywhere. No permission is required.
- **Internet and network state** (`INTERNET`, `ACCESS_NETWORK_STATE`): used to contact Firebase Remote Config, the Gemini API, Firebase Crashlytics after a crash and, on phones and tablets when your consent allows it, Google Analytics for Firebase.
- **Other apps on your device:** the app asks Android which speech recognition and text-to-speech services are installed, only to be able to listen to a question and to read answers out loud. It reads nothing else about your apps.
- **Notifications** (`POST_NOTIFICATIONS`): asked for once, as described under "Notifications". The app works the same without it.
- **Microphone** (`RECORD_AUDIO`): requested the first time you hold to ask out loud, and used only as described under "Voice questions". You can refuse it or take it back in your device's settings at any time; the app keeps working by tap and shake.

Neither version requests location, camera, contacts, body sensors or any other sensitive permission.

## Information stored on your device
The app saves your choices (language, answer style, whether to use AI answers and, on phones and tablets, whether to use voice questions and spoken answers, and the names typed for party mode) in a small settings file on your device, so they are still there the next time you open the app. They stay on the device, are not sent anywhere on their own, and are removed when you clear the app's data or uninstall it. On phones and tablets the app also saves the "Recents" list described above (at most your latest 50 questions and answers), which is removed the same way or from the list itself. On phones and tablets the app also keeps how many days in a row you have asked the ball, as two numbers and a date, which never leave the device. On phones and tablets the picture of the last answer you shared waits in temporary storage, as described under "Sharing an answer". On the watch, the latest answer is kept in memory only. The app stores nothing else of its own. Firebase stores its installation identifiers and the latest settings on the device, and a crash report waits there until it has been sent. Nothing is included in device backups.

## Retention and deletion
Apart from the crash reports described above, which Google keeps for the developer for 90 days, and the usage counts described under "Usage measurement", which it keeps for at most 14 months, the developer does not collect or store your information. What the app saves on your device stays there until you delete it, as described above. Information received by Google, by your device's speech recognition or text-to-speech provider, or by an app you share an answer with, is handled under their policies as described above.

## Children
The app is not directed at children, and the developer does not knowingly collect personal information from children.

## Changes to this policy
If this policy changes, the updated version will be posted here with a new effective date.

## Contact
For questions about this policy, contact the developer through the contact details on the app's Google Play listing.
