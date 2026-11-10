plugins {
    alias(libs.plugins.kotlin.jvm)
}

dependencies {
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.javax.inject)
    implementation(project(":core:common"))

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
}
