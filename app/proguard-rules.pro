# ==============================================================================
# Sulaone Enterprise SuperApp - R8 & ProGuard Optimization Rules
# SMA Islam Sultan Agung 1 Semarang
# ==============================================================================

# 1. General Code Optimization & Shrinking
-optimizationpasses 5
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-verbose

# 2. Keep Data Models & Serialized Objects (Gson)
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod
-keepclassmembers enum * { *; }

-keep class com.sultanagung1.sista.data.model.** { *; }
-keepclassmembers class com.sultanagung1.sista.data.model.** { <fields>; <methods>; }
-keep class * implements java.io.Serializable { *; }

# 3. Retrofit & OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}

# 4. Room Database Entities & DAOs
-keep class androidx.room.** { *; }
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao interface * { *; }

# 5. Jetpack Compose
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**

# 6. Biometrics & Play Services
-keep class androidx.biometric.** { *; }
-keep class com.google.android.gms.** { *; }

# 7. Strip sensitive debug logs in Release build
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int d(...);
}

# Home-screen widget snapshot (Gson → SharedPreferences, read by the widget providers)
-keep class com.sultanagung1.sista.core.widget.** { *; }
