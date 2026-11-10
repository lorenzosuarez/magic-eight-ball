import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("kotlin-kapt")
    alias(libs.plugins.hilt)
    alias(libs.plugins.roborazzi)
}

// Firebase is optional: without app-phone/google-services.json the app builds and runs offline-only
// (Remote Config is unavailable, so Gemini stays off unless a local GEMINI_API_KEY is provided, and
// crashes are not reported).
if (file("google-services.json").isFile) {
    apply(plugin = libs.plugins.google.services.get().pluginId)
    apply(plugin = libs.plugins.firebase.crashlytics.get().pluginId)
}

// AdMob identifiers come from the git-ignored root local.properties (ADMOB_APP_ID and
// ADMOB_INTERSTITIAL_ID). Without them the build uses Google's public test identifiers, which
// only ever serve test ads: a build meant for the store needs the real ones.
val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.isFile) file.inputStream().use(::load)
}
val admobAppId: String = localProperties.getProperty("ADMOB_APP_ID", "ca-app-pub-3940256099942544~3347511713").trim()
val admobInterstitialId: String =
    localProperties.getProperty("ADMOB_INTERSTITIAL_ID", "ca-app-pub-3940256099942544/1033173712").trim()

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

        manifestPlaceholders["admobAppId"] = admobAppId
        buildConfigField("String", "ADMOB_INTERSTITIAL_ID", "\"$admobInterstitialId\"")
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
        buildConfig = true
    }
    bundle {
        // The language is chosen in the app, not by the device: every installation needs both
        // languages, so Google Play must not strip the one the device does not use.
        language {
            enableSplit = false
        }
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

    // Ads, off unless switched on from Remote Config, and the consent they need.
    implementation(libs.play.services.ads)
    implementation(libs.user.messaging.platform)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.config)
    // Crash reports, so a failure in the field can be found and fixed.
    implementation(libs.firebase.crashlytics)

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
    testImplementation(libs.ui.test.junit4)
}
