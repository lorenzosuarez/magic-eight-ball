plugins {
    alias(libs.plugins.kotlin.jvm)
    id("kotlin-kapt")
}

dependencies {
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.javax.inject)
    
    // Using simple Dagger/Hilt annotations directly if possible, or usually hilt-core for pure JVM modules.
    // Since this is key for "Expert" refactor, we should use 'hilt-core' for JVM module or just javax.inject + manual module if we don't want Android dependence.
    // BUT, 'hilt-android' brings the component definitions. 
    // Ideally core:common is pure Kotlin. 'hilt-core' is the way.
    // Let's check libs.versions.toml for hilt-core. It's not there.
    // For simplicity in this environment, we'll keep javax.inject logic and move the Module to a new :core:di (Android Library) OR just make core:common an Android Lib?
    // No, core:common should be pure.
    // "Principal Engineer" decision: Keep core:common pure. The DI Module that binds it should be in a DI module or the App.
    // The "Audit" said "Move CommonModule... to core:common".
    // I will convert core:common to use javax.inject (done). 
    // The BINDING must happen where Hilt components exist.
    // Use :core:common just for the interface/impl.
    // I will Create :core:di (Android Library) to hold the bindings, OR put it in :core:common IF I change it to Android Lib (not ideal).
    // BETTER: Put it in :core:common and use `dagger` (pure) dependencies? 
    // Let's stick to :app-wear for the module for now to avoid altering :core:common structure too much, BUT I will refactor the ShakeConfig properly.
    
    // WAIT, I really should parameterize ShakeStrategy.
}
