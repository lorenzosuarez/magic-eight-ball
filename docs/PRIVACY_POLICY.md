# Privacy Policy for Magic 8 Ball (Wear OS)

**Effective date:** November 10, 2026

This policy explains how the Magic 8 Ball app for Wear OS (package `com.lorenzosuarez.magiceightball`), developed by Lorenzo Suarez, handles information.

## Summary
- No account or sign-in, no ads, no analytics and no crash-reporting tools.
- The app does not save personal information on your watch, and the developer runs no server that receives your data.
- The app connects to two Google services, both described below: Firebase Remote Config, to check its settings, and, when AI answers are switched on, the Gemini API.

## What the app sends, and to whom
**Firebase Remote Config.** When the app starts, and at most about once an hour while you use it, it asks Google's Firebase Remote Config service for its current settings, such as whether AI answers are switched on. This request includes a random installation identifier created by Firebase for this copy of the app, and basic app and device information (for example app version, language, country and operating system version). It does not include your name, account or location. Google handles this under the [Firebase privacy and security terms](https://firebase.google.com/support/privacy).

**Gemini API.** AI answers can be switched on or off by the developer. While they are off, the app shows answers from a list built into the app and sends nothing when you shake. While they are on, a shake sends a request over an encrypted HTTPS connection to Google's Gemini API (`generativelanguage.googleapis.com`). That request contains only:
- a fixed instruction written by the developer, which reflects the personality (Sarcastic or Mystical) and answer language (English or Spanish) you chose in the app;
- the fixed word "shake";
- technical generation settings and the developer's API key.

The app has no text or voice input, and the request does not include your name, account, contacts, location, device identifiers or sensor readings. As with any internet request, Google receives your device's IP address and standard connection information. Google handles these requests under the [Gemini API Additional Terms of Service](https://ai.google.dev/gemini-api/terms) and the [Google Privacy Policy](https://policies.google.com/privacy). Depending on how the API is accessed, Google may keep these requests for a period of time and use them to provide and improve its services.

If the request fails or your watch is offline, the app shows an answer from the built-in list instead.

## Sensors and permissions
- **Motion sensor (accelerometer):** used only on your watch to detect a shake. Sensor readings are not stored or sent anywhere. No permission is required.
- **Internet and network state** (`INTERNET`, `ACCESS_NETWORK_STATE`): used to contact Firebase Remote Config and the Gemini API.
- **Wake lock** (`WAKE_LOCK`): declared by the app but not used by the current version.

The app does not request location, microphone, camera, contacts, body sensors or any other sensitive permission.

## Information stored on your watch
Your language and personality choices and the latest answer are kept in memory only. The app does not write personal information to files or databases. Firebase stores its installation identifier and the latest settings on the watch. Nothing is included in device backups.

## Retention and deletion
The developer does not collect or store your information, so there is nothing for the developer to keep or delete. Information received by Google is handled under Google's policies linked above.

## Children
The app is not directed at children, and the developer does not knowingly collect personal information from children.

## Changes to this policy
If this policy changes, the updated version will be posted here with a new effective date.

## Contact
For questions about this policy, contact the developer through the contact details on the app's Google Play listing.
