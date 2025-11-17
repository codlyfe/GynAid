# GynAid Healthcare App Proguard Rules - Performance Optimization

# React Native optimization
-keep,allowobfuscation class com.facebook.hermes.unicode.** { *; }
-keep class com.facebook.jni.** { *; }
-dontwarn com.facebook.jni.**

# Preserve all healthcare app functionality
-keep class com.gynaid.** { *; }
-keep class * extends java.lang.annotation.Annotation { *; }
-dontwarn com.gynaid.**

# Expo optimization
-keep class expo.modules.** { *; }
-dontwarn expo.modules.**

# Hermes bytecode optimization
-keep class hermes.** { *; }
-dontwarn hermes.**

# Healthcare specific libraries
-keep class org.bouncycastle.** { *; }
-dontwarn org.bouncycastle.**

# Network and API libraries
-keep class okhttp3.** { *; }
-keep class okio.** { *; }
-dontwarn okhttp3.**
-dontwarn okio.**

# JSON and data parsing
-keep class com.google.gson.** { *; }
-dontwarn com.google.gson.**

# React Native JavaScript engine
-keepclassmembers class * {
  @android.webkit.JavascriptInterface <methods>;
}

# Keep native methods
-keepclasseswithmembernames class * {
    native <methods>;
}

# Keep enums
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Remove logging in release
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}

# Optimize for speed
-optimizations !code/simplification/arithmetic,!code/simplification/cast,!field/*,!class/merging/*
-optimizationpasses 5
-allowaccessmodification