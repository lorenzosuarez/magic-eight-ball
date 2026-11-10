plugins {
    alias(libs.plugins.kotlin.jvm)
    id("kotlin-kapt")
}

// Pure Kotlin: no Android and no Hilt here. What this module declares is bound in the apps,
// where the Hilt components live.
dependencies {
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.javax.inject)
}
