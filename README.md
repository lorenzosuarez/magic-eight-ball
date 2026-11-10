# Magic 8 Ball Wear OS 🎱

**Author:** Lorenzo Suarez

An advanced, AI-powered decision-making companion built exclusively for Wear OS. This application modernizes the classic Magic 8 Ball experience by fusing nostalgic charm with the cutting-edge intelligence of Google's Gemini Large Language Model, all wrapped in a premium, console-inspired interface.

## Overview 🚀

Magic 8 Ball Wear OS goes beyond simple random responses. It features a **Hybrid Intelligence** system that intelligently switches between a local deterministic engine for instant offline answers and a cloud-based Gemini AI for creative, context-aware predictions. The UI is meticulously crafted with physics-based animations, adaptive typography, and a "Matrix-green" console aesthetic that feels right at home on modern smartwatches.

## User Interface & Flow 📸

Experience the complete journey from startup to prediction.

| Splash | Home | Language | AI personality | Reveal |
|:---:|:---:|:---:|:---:|:---:|
| <img src="docs/screenshots/1.png" width="200" /> | <img src="docs/screenshots/2.png" width="200" /> | <img src="docs/screenshots/3.png" width="200" /> | <img src="docs/screenshots/6.png" width="200" /> | <img src="docs/screenshots/5.png" width="200" /> |

### Live Demo
![Screen Recording 2026-01-04 at 12 36 27 AM](https://github.com/user-attachments/assets/c7515835-c4ac-4325-a158-1a197ac621e1)

*Real-time physics and API interaction*

*(Please rename your screenshots to match: `splash_screen.png`, `language_selection.png`, `thinking_state.png`, `prediction_result.png`, and `demo.gif`)*

## Key Features ✨

*   **🧠 Hybrid AI Engine**:
    *   **Local Fallback**: Instant, zero-latency responses using a local database when offline.
    *   **Cloud Intelligence**: Taps into the Gemini API for unique, witty, and varied predictions when connected.
    *   **Your Call**: An "AI answers" page lets the user switch AI answers off (or back on) at any time. Gemini is asked only when the user's choice is on, the developer's remote switch is on and a key is available; when AI answers are not available the page says so instead of offering a toggle that does nothing.
    *   **Smart Retry Policies**: Robust network handling with exponential backoff and strict safety filters.

*   **🎨 Premium Wear OS Experience**:
    *   **Physics-Based Animation**: The signature blue triangle floats and settles with realistic spring dynamics.
    *   **Console Aesthetic**: A retro-futuristic language selection screen with monospace typography and green accent highlights.
    *   **Haptic Feedback**: Subtle vibrations enhance the tactile feel of shaking the device or receiving an answer.

*   **📐 Adaptive Typography**:
    *   **Auto-Sizing Text**: `TriangleFittedText` uses a binary search over font sizes to fit predictions of any length perfectly within the inverted triangle geometry without truncation.
    *   **Dynamic Gravity**: Text naturally "floats" to the visual center but adapts its position based on phrase length and triangle width.

*   **🌍 Multi-Language Support**:
    *   Seamlessly toggle between **English** and **Spanish**.
    *   Every text in the app follows the language chosen in the app; on first launch it is Spanish on a Spanish-language watch and English otherwise.
    *   **Dynamic System Prompts**: The AI's personae and language instructions are injected dynamically based on user preference.

*   **💾 Remembers Your Choices**:
    *   Language, personality and the AI answers choice are saved on the watch with Jetpack DataStore and restored before the first frame. Nothing else is stored.

## Tech Stack 🛠️

Built with modern Android development standards and best practices:

*   **Language**: [Kotlin](https://kotlinlang.org/) (v2.0+)
*   **UI Framework**: [Compose for Wear OS](https://developer.android.com/training/wearables/compose) (Material 3)
*   **Architecture**: Clean Architecture + Multi-Module (Modularization by Layer & Feature)
*   **Dependency Injection**: [Hilt](https://dagger.dev/hilt/)
*   **Networking**: [Retrofit](https://square.github.io/retrofit/) + [OkHttp](https://square.github.io/okhttp/)
*   **AI Integration**: [Google Gemini API](https://ai.google.dev/)
*   **Concurrency**: Kotlin Coroutines & Flow
*   **Persistence**: [Jetpack DataStore](https://developer.android.com/topic/libraries/architecture/datastore) (Preferences)
*   **Serialization**: Kotlinx Serialization

## Architecture 🏗️

The project follows a strict **Clean Architecture** pattern, modularized to separate concerns and ensure scalability:

```text
:app-wear           # Application entry point & DI orchestration
├── :feature:chat   # Presentation layer (UI, ViewModel)
├── :core:domain    # Business logic (UseCases, Repository Interfaces, Policies)
├── :core:data      # Data implementation (Repositories, DataSources, API)
├── :core:network   # Network infrastructure (Retrofit, OkHttp, API Keys)
├── :core:motion    # Sensor logic & Shake detection strategy
├── :core:common    # Shared utilities & configurations
└── :core:designsystem # Theming, Color, Type
```

## Setup & Configuration ⚙️

The app builds and runs with no setup at all: without Firebase it answers from the bundled offline list.

1.  **Clone the Repository**:
    ```bash
    git clone https://github.com/lorenzosuarez/magic-eight-ball.git
    ```

2.  **Enable Gemini from the Firebase console (optional)**:
    *   Create a Firebase project and add an Android app with the package `com.lorenzosuarez.magiceightball`.
    *   Download `google-services.json` into `app-wear/` (git-ignored).
    *   In **Remote Config**, add and publish two parameters:

        | Parameter | Type | Meaning |
        |:---|:---|:---|
        | `gemini_enabled` | Boolean | Turns Gemini answers on or off for everyone. Off when missing. Each user can still switch AI answers off in the app. |
        | `gemini_api_key` | String | The Gemini API key from [Google AI Studio](https://aistudio.google.com/). |
        | `gemini_model` | String | Optional Gemini model ID. Defaults to `gemini-3.5-flash-lite`; change it here when Google retires a model. |

    The app checks Remote Config at launch and at most once an hour, so a change can take up to an hour to reach a watch. Values in Remote Config can be read by anyone with the app, so restrict the key to the Generative Language API and set a quota.

3.  **Local key for debug builds (optional)**:
    `GEMINI_API_KEY=...` in the git-ignored `local.properties` is compiled into **debug** builds only and is used when Remote Config provides no key. Release builds never embed a key.

4.  **Build & Run**:
    Open the project in Android Studio (Koala or later recommended) and deploy to a Wear OS emulator or physical device.

## License 📄

Copyright © 2024 **Lorenzo Suarez**. All rights reserved.
