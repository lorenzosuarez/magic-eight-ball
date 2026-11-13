plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("kotlin-kapt")
    alias(libs.plugins.hilt)
}

// Firebase is optional: without app-wear/google-services.json the app builds and runs offline-only
// (there is no project to ask, so Gemini stays off, and crashes are not reported).
if (file("google-services.json").isFile) {
    apply(plugin = libs.plugins.google.services.get().pluginId)
    apply(plugin = libs.plugins.firebase.crashlytics.get().pluginId)
}

// Same upload key as :app-phone; see the script for where the credentials are read from.
apply(from = rootProject.file("gradle/release-signing.gradle"))

android {
    namespace = "com.example.magiceightball"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.lorenzosuarez.magiceightball"
        minSdk = 30
        targetSdk = 36
        versionCode = 103
        versionName = "1.0.1"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(platform(libs.compose.bom))
    implementation(libs.ui)
    implementation(libs.ui.graphics)
    implementation(libs.compose.material3)
    implementation(libs.compose.foundation)
    implementation(libs.activity.compose)
    implementation(libs.core.splashscreen)
    // Tells the app when the watch dims to its always-on screen.
    implementation(libs.wear)

    // The tile: the ball one swipe away from the watch face.
    implementation(libs.wear.tiles)
    implementation(libs.wear.protolayout)
    implementation(libs.concurrent.futures)
    implementation(libs.kotlinx.coroutines.android)

    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)

    // Crash reports, so a failure in the field can be found and fixed.
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.crashlytics)

    implementation(project(":core:common"))
    implementation(project(":core:domain"))
    implementation(project(":core:data"))
    implementation(project(":core:motion"))
    implementation(project(":core:designsystem"))
    implementation(project(":feature:chat"))

    testImplementation(libs.junit)
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
    }
}
