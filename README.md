# Magic 8 Ball Wear OS 🎱

**Author:** Lorenzo Suarez

An AI-powered decision-making companion for Wear OS, blending the classic Magic 8 Ball nostalgia with modern Large Language Model (Gemini) intelligence.

## Features ✨

-   **Hybrid Intelligence**: Instantly provides answers using a local deterministic fallback or the powerful Gemini API for creative responses.
-   **Immersive UI**: "Console-style" aesthetic on the language selection screen and a fluid, physics-based inverted triangle animation for revealing predictions.
-   **Multi-language Support**: Seamless toggle between English and Spanish.
-   **Wear OS Optimized**: Built with Wear Compose Material 3, supporting rotary input, swipe gestures, and ambient mode.
-   **Adaptive Typography**: Intelligent text scaling ensures predictions of any length fit perfectly within the iconic blue triangle.

## Setup 🛠️

This project uses the Gemini API. To build and run the app, you must provide your own API key.

1.  **Get an API Key**: Visit [Google AI Studio](https://aistudio.google.com/) to generate a key.
2.  **Configure Local Properties**:
    Create a file named `local.properties` in the root directory (if it doesn't exist) and add:
    ```properties
    GEMINI_API_KEY=your_api_key_here
    ```

    > **Note:** The `local.properties` file is excluded from version control to keep your key secure.

3.  **Build**: Open the project in Android Studio and sync Gradle.

## Tech Stack 📚

-   **Kotlin** & **Compose for Wear OS**
-   **Material 3** Design System
-   **Hilt** for Dependency Injection
-   **Retrofit** & **OkHttp** for Networking
-   **Gemini API** (Generative AI)
-   **Coroutines** & **Flow** for async operations

## License

Copyright © 2024 Lorenzo Suarez. All rights reserved.
