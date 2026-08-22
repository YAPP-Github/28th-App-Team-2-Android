import java.util.Properties

plugins {
    alias(libs.plugins.todakun.feature)
}

val localProperty =
    Properties().apply {
        rootProject
            .file("local.properties")
            .takeIf { it.exists() }
            ?.inputStream()
            ?.use { load(it) }
    }
val kakaoKeyRelease: String = localProperty.getProperty("KAKAO_NATIVE_APP_KEY", "")
val kakaoKeyDebug: String = localProperty.getProperty("KAKAO_NATIVE_APP_KEY_DEV", "")
val googleClientId: String = localProperty.getProperty("GOOGLE_WEB_CLIENT_ID", "")

android {
    namespace = "com.kikidan.auth"

    defaultConfig {
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")

        buildConfigField("String", "GOOGLE_WEB_CLIENT_ID", "\"$googleClientId\"")
    }
    buildTypes {
        debug {
            buildConfigField("String", "KAKAO_NATIVE_APP_KEY", "\"$kakaoKeyDebug\"")
            manifestPlaceholders["kakaoNativeAppKey"] = kakaoKeyDebug
        }
        release {
            buildConfigField("String", "KAKAO_NATIVE_APP_KEY", "\"$kakaoKeyRelease\"")
            manifestPlaceholders["kakaoNativeAppKey"] = kakaoKeyRelease
        }
    }
    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kakao.user)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.messaging)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)
}
