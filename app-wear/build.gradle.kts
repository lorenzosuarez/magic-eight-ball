import java.io.File
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("kotlin-kapt")
    alias(libs.plugins.hilt)
}

// Firebase is optional: without app-wear/google-services.json the app builds and runs offline-only
// (Remote Config is unavailable, so Gemini stays off unless a local GEMINI_API_KEY is provided).
if (file("google-services.json").isFile) {
    apply(plugin = libs.plugins.google.services.get().pluginId)
}

// Release signing credentials live OUTSIDE the repository.
// Default: ~/.android-keys/magic8ball-keystore.properties
// Override: -Pmagic8ball.keystoreProperties=/path/file.properties or env MAGIC8BALL_KEYSTORE_PROPERTIES.
// Keys: storeFile (absolute, or relative to the properties file), storePassword, keyAlias, keyPassword.
// If the file or the keystore is missing, release builds still succeed but produce UNSIGNED artifacts.
val releaseKeystorePropertiesFile = File(
    providers.gradleProperty("magic8ball.keystoreProperties").orNull
        ?: System.getenv("MAGIC8BALL_KEYSTORE_PROPERTIES")
        ?: "${System.getProperty("user.home")}/.android-keys/magic8ball-keystore.properties"
)
val releaseKeystoreProperties = Properties().apply {
    if (releaseKeystorePropertiesFile.isFile) {
        releaseKeystorePropertiesFile.inputStream().use { load(it) }
    }
}
val releaseStoreFile: File? = releaseKeystoreProperties.getProperty("storeFile")?.let { path ->
    File(path).let { if (it.isAbsolute) it else File(releaseKeystorePropertiesFile.parentFile, path) }
}
val hasReleaseSigning = releaseStoreFile?.isFile == true &&
    listOf("storePassword", "keyAlias", "keyPassword")
        .all { !releaseKeystoreProperties.getProperty(it).isNullOrBlank() }

android {
    namespace = "com.example.magiceightball"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.lorenzosuarez.magiceightball"
        minSdk = 30
        targetSdk = 36
        versionCode = 101
        versionName = "1.0.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        if (hasReleaseSigning) {
            create("release") {
                storeFile = releaseStoreFile
                storePassword = releaseKeystoreProperties.getProperty("storePassword")
                keyAlias = releaseKeystoreProperties.getProperty("keyAlias")
                keyPassword = releaseKeystoreProperties.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            if (hasReleaseSigning) {
                signingConfig = signingConfigs.getByName("release")
            } else {
                logger.warn(
                    "Release signing not configured ($releaseKeystorePropertiesFile missing or incomplete): " +
                        "release artifacts will be UNSIGNED."
                )
            }
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
}

dependencies {
    implementation(platform(libs.compose.bom))
    implementation(libs.ui)
    implementation(libs.ui.graphics)
    implementation(libs.compose.material3)
    implementation(libs.compose.foundation)
    implementation(libs.activity.compose)
    implementation(libs.core.splashscreen)

    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)

    implementation(project(":core:common"))
    implementation(project(":core:domain"))
    implementation(project(":core:data"))
    implementation(project(":core:network"))
    implementation(project(":core:motion"))
    implementation(project(":core:designsystem"))
    implementation(project(":feature:chat"))
}
