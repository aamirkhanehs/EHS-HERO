# Add project specific ProGuard rules here.

# Keep all data model classes used for Firestore (de)serialization.
# Firestore uses reflection to map documents to these POJOs; without these
# rules, release builds will fail to read/write data correctly after R8 strips
# "unused" fields and no-argument constructors.
-keepclassmembers class com.ehshero.app.data.model.** {
    *;
}
-keep class com.ehshero.app.data.model.** { *; }

# Firebase Firestore
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes EnclosingMethod
-keepattributes InnerClasses

-keepclassmembers class * {
    @com.google.firebase.firestore.PropertyName <fields>;
}
-keepclassmembers class * {
    @com.google.firebase.firestore.PropertyName <methods>;
}

# Kotlin coroutines
-dontwarn kotlinx.coroutines.**
