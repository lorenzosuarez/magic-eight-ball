# Obfuscate the BuildConfig class to protect the API key string
-keepclassmembers class **.BuildConfig {
    public static final java.lang.String GEMINI_API_KEY;
}

# The above keeps the name but R8/ProGuard will still mangle the actual string 
# if we use String obfuscation, but basic AGP/R8 will at least shrink and 
# obfuscate the rest of the application logic.

# Hilt/Dagger specific rules
-keep class dagger.hilt.internal.GeneratedComponentManager { *; }
-keep class * implements dagger.hilt.internal.GeneratedComponent { *; }

# Kotlin Serialization
-keepattributes *Annotation*, EnclosingMethod, Signature, InnerClasses
-keepclassmembers class ** {
    @kotlinx.serialization.Serializable *;
}
-keep class **$$serializer { *; }
-keepclassmembers class ** {
    *** Companion;
}
