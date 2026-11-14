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
// (there is no project to ask, so Gemini stays off, and crashes are not reported).
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

// The ad a user may choose to watch for more AI answers (ADMOB_REWARDED_ID in local.properties).
// Without it the build uses Google's public test identifier, and a release build then offers no
// such ad at all: see AdMobAdsController.
val admobRewardedId: String =
    localProperties.getProperty("ADMOB_REWARDED_ID", "ca-app-pub-3940256099942544/5224354917").trim()

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
        versionCode = 4
        versionName = "1.2.1"

        manifestPlaceholders["admobAppId"] = admobAppId
        buildConfigField("String", "ADMOB_INTERSTITIAL_ID", "\"$admobInterstitialId\"")
        buildConfigField("String", "ADMOB_REWARDED_ID", "\"$admobRewardedId\"")
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
    // How the app is used, in numbers and only with the user's consent: see FirebaseUsageTracker.
    implementation(libs.firebase.analytics)
    // Messages from the developer to the app's users: see the push package.
    implementation(libs.firebase.messaging)
    // The optional account, signed in to with the user's Google account: see the account package.
    implementation(libs.firebase.auth)
    // The profile of whoever is signed in, in a document only they can read.
    implementation(libs.firebase.firestore)
    // What is sold is bought through Google Play, and a purchase counts once the project's own
    // server has checked it with Google: see the account package's Shop.
    implementation(libs.billing.ktx)
    implementation(libs.firebase.functions)
    implementation(libs.datastore.preferences)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)
    // The day's verdict, made on the device once a day.
    implementation(libs.androidx.work)
    implementation(libs.androidx.preference)

    implementation(project(":core:common"))
    implementation(project(":core:domain"))
    implementation(project(":core:data"))
    implementation(project(":core:motion"))

    debugImplementation(libs.ui.tooling)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.robolectric)
    testImplementation(libs.roborazzi)
    testImplementation(libs.roborazzi.compose)
    testImplementation(libs.ui.test.junit4)
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
    }
}
