import java.util.Properties


plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
    alias(libs.plugins.todakun.android.signing)
    alias(libs.plugins.google.services)
}

val localProperty =
    Properties().apply {
        val f = rootProject.file("local.properties")
        if (f.exists()) f.inputStream().use { load(it) }
    }

android {
    namespace = "com.kikidan.todakun"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.kikidan.todakun"
        minSdk = 26
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            // TODO CI 통과를 위해 공백을 넣음. 추후 CD 설정 시 재설정
            val kakaoKey = localProperty.getProperty("KAKAO_NATIVE_APP_KEY_DEV") ?: ""
            val appLinkHost = localProperty.getProperty("APP_LINK_HOST_DEV") ?: ""
            manifestPlaceholders["KAKAO_APP_KEY"] = kakaoKey
            manifestPlaceholders["APP_LINK_HOST"] = appLinkHost
        }
        release {
            // TODO CI 통과를 위해 공백을 넣음. 추후 CD 설정 시 재설정
            val kakaoKey = localProperty.getProperty("KAKAO_NATIVE_APP_KEY") ?: ""
            val appLinkHost = localProperty.getProperty("APP_LINK_HOST") ?: ""
            manifestPlaceholders["KAKAO_APP_KEY"] = kakaoKey
            manifestPlaceholders["APP_LINK_HOST"] = appLinkHost

            isMinifyEnabled = true
            isShrinkResources = true

            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.hilt.android)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.messaging)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)

    implementation(projects.core.domain)
    implementation(projects.core.data)
    implementation(projects.core.dataRemote)
    implementation(projects.core.dataLocal)
    implementation(projects.core.navigation)
    implementation(projects.core.designsystem)
    implementation(projects.feature.mypage)
    implementation(projects.feature.onboarding)
    implementation(projects.feature.auth)
    implementation(projects.feature.sajuContents)
    implementation(projects.feature.luckAction)
    implementation(projects.feature.home)
    implementation(projects.feature.chat)
    implementation(projects.feature.notification)
    implementation(projects.feature.sajuContents)

    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.navigation3.ui)
    implementation(libs.androidx.lifecycle.viewmodel.navigation3)
    implementation(libs.androidx.material3.adaptive.navigation3)
    implementation(libs.orbit.core)
    implementation(libs.orbit.viewmodel)
    implementation(libs.orbit.compose)
}
