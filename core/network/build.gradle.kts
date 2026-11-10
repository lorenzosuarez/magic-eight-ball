import java.util.Properties

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.serialization)
    id("kotlin-kapt")
    alias(libs.plugins.hilt)
}

// Optional GEMINI_API_KEY from the git-ignored root local.properties (debug builds only).
val localGeminiApiKey: String = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.isFile) file.inputStream().use { load(it) }
}.getProperty("GEMINI_API_KEY", "").trim().trim('"')

android {
    namespace = "com.example.magiceightball.core.network"
    compileSdk = 36

    defaultConfig {
        // Lowest of the apps using this module: :app-phone (26); :app-wear needs 30.
        minSdk = 26
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        buildConfig = true
    }
    buildTypes {
        // The local key is a development convenience only: release builds never embed a key
        // and get it from Firebase Remote Config instead.
        debug {
            buildConfigField("String", "GEMINI_API_KEY", "\"$localGeminiApiKey\"")
        }
        release {
            buildConfigField("String", "GEMINI_API_KEY", "\"\"")
        }
    }
}

dependencies {
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.javax.inject)

    // Network
    implementation(libs.retrofit)
    implementation(libs.retrofit.kotlinx.serialization)
    implementation(libs.okhttp)
    implementation(libs.logging.interceptor)
    implementation(libs.kotlinx.serialization.json)

    // DI
    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)

    // Internal
    implementation(project(":core:common"))
    implementation(project(":core:domain"))
}
