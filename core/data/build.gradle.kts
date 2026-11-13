plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    id("kotlin-kapt")
    alias(libs.plugins.hilt)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.example.magiceightball.core.data"
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
    testOptions {
        // android.util.Log is a no-op in local unit tests.
        unitTests.isReturnDefaultValues = true
    }
}

dependencies {
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.javax.inject)
    implementation(libs.kotlinx.serialization.json)

    // Remote Config: the switch for AI answers and the model they are asked of
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.config)
    // AI answers, asked through Firebase so that no key for them travels with the app, and the
    // proof that a request comes from this app: Google Play's in a release, a debug token otherwise.
    implementation(libs.firebase.ai)
    releaseImplementation(libs.firebase.appcheck.playintegrity)
    debugImplementation(libs.firebase.appcheck.debug)
    // Whether the app is on screen: the proof is made ahead only then.
    implementation(libs.lifecycle.process)

    // User settings
    implementation(libs.datastore.preferences)

    // DI
    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)

    // Internal
    implementation(project(":core:common"))
    implementation(project(":core:domain"))

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockk)
}
