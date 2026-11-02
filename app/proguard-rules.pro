# Project specific ProGuard rules for Campus Plug SA

# Preserve line numbers and source file names for crash reports
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Retain Firestore data models (default constructors and properties mapped via reflection)
-keepclassmembers class com.example.data.model.** {
    <init>();
    <fields>;
    <methods>;
}

# Retain Jetpack Credential Manager and Google ID models
-keep class androidx.credentials.** { *; }
-keep class com.google.android.libraries.identity.googleid.** { *; }

# Retain Firebase model fields
-keepattributes *Annotation*
-keepclassmembers class * {
    @com.google.firebase.firestore.DocumentId <fields>;
    @com.google.firebase.firestore.ServerTimestamp <fields>;
    @com.google.firebase.firestore.PropertyName <fields>;
    @com.google.firebase.firestore.IgnoreExtraProperties <fields>;
}
