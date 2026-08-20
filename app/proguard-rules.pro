# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# ─────────────────────────────────────────────
# 공통: 리플렉션/제네릭 기반 라이브러리들이 필요로 하는 속성
# (Kakao SDK 내부 Retrofit+Gson, kotlinx.serialization 모두 공통으로 사용)
# ─────────────────────────────────────────────
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-keepattributes AnnotationDefault

# ─────────────────────────────────────────────
# Kakao SDK
# 카카오 SDK는 내부적으로 Retrofit + Gson을 번들링해서 씀.
# 모델 클래스가 리플렉션으로 필드에 접근하므로 난독화/축소에서 제외.
# ─────────────────────────────────────────────
-keep class com.kakao.sdk.** { *; }
-keep class * extends com.google.gson.TypeAdapter
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

-dontwarn org.slf4j.**
-dontwarn org.bouncycastle.jsse.**
-dontwarn org.conscrypt.**
-dontwarn org.openjsse.**
-dontwarn javax.annotation.**

# ─────────────────────────────────────────────
# OkHttp / Okio (Ktor의 OkHttp 엔진이 사용)
# 데스크톱 전용 TLS 프로바이더 감지 코드가 참조하지만 안드로이드엔 없는 클래스들.
# ─────────────────────────────────────────────
-dontwarn okhttp3.internal.platform.**
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement

# ─────────────────────────────────────────────
# Ktor
# 클라이언트 엔진(OkHttp)을 리플렉션 기반 ServiceLoader로 찾기 때문에 keep 필요.
# ─────────────────────────────────────────────
-keep class io.ktor.** { *; }
-keep interface io.ktor.** { *; }
-dontwarn io.ktor.**

# ─────────────────────────────────────────────
# kotlinx.serialization
# 공식 권장 규칙(https://github.com/Kotlin/kotlinx.serialization#android) —
# @Serializable 클래스의 Companion/serializer()가 리플렉션처럼 조회되는 경로를 보존.
# ─────────────────────────────────────────────
-if @kotlinx.serialization.Serializable class **
-keepclassmembers class <1> {
    static <1>$Companion Companion;
}

-if @kotlinx.serialization.Serializable class ** {
    static **$* *;
}
-keepclassmembers class <2>$* {
    kotlinx.serialization.KSerializer serializer(...);
}

-if @kotlinx.serialization.Serializable class ** {
    public static ** INSTANCE;
}
-keepclassmembers class <1> {
    public static <1> INSTANCE;
    kotlinx.serialization.KSerializer serializer(...);
}

-keepclasseswithmembers class **$$serializer {
    *** serializer(...);
}

-dontnote kotlinx.serialization.AnnotationsKt
