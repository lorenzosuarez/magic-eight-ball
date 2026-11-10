plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("kotlin-kapt")
    alias(libs.plugins.hilt)
    alias(libs.plugins.roborazzi)
}

// Firebase is optional: without app-phone/google-services.json the app builds and runs offline-only
// (Remote Config is unavailable, so Gemini stays off unless a local GEMINI_API_KEY is provided).
if (file("google-services.json").isFile) {
    apply(plugin = libs.plugins.google.services.get().pluginId)
}

// Same upload key as :app-wear; see the script for where the credentials are read from.
apply(from = rootProject.file("gradle/release-signing.gradle"))

android {
    namespace = "com.lorenzosuarez.magiceightball.phone"
    compileSdk = 36

    defaultConfig {
        // Google Play pairs the phone and the watch app through the shared applicationId.
        // Version codes must never collide: phone uses 1, 2, 3...; :app-wear uses 101, 102...
        applicationId = "com.lorenzosuarez.magiceightball"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
    testOptions {
        // Robolectric renders the real screen for the screenshot test, so it needs the resources.
        unitTests.isIncludeAndroidResources = true
    }
}

dependencies {
    implementation(platform(libs.compose.bom))
    implementation(libs.ui)
    implementation(libs.ui.graphics)
    implementation(libs.ui.tooling.preview)
    implementation(libs.material3)
    implementation(libs.activity.compose)
    implementation(libs.lifecycle.runtime.compose)
    implementation(libs.lifecycle.viewmodel.compose)

    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)

    implementation(project(":core:common"))
    implementation(project(":core:domain"))
    implementation(project(":core:data"))
    implementation(project(":core:network"))
    implementation(project(":core:motion"))

    debugImplementation(libs.ui.tooling)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.robolectric)
    testImplementation(libs.roborazzi)
    testImplementation(libs.roborazzi.compose)
}
