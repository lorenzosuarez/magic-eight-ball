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
