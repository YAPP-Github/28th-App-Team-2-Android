import java.util.Properties
import kotlin.apply

plugins {
    id("todakun.feature")
}

val localProperty =
    Properties().apply {
        val f = rootProject.file("local.properties")
        if (f.exists()) f.inputStream().use { load(it) }
    }

android {
    namespace = "com.kikidan.sajucontents"

    buildTypes {
        debug {
            // TODO CI 통과를 위해 공백을 넣음. 추후 CD 설정 시 재설정
            val appLinkHost = localProperty.getProperty("APP_LINK_HOST_DEV") ?: ""
            buildConfigField("String", "APP_LINK_HOST", "\"https://${appLinkHost}\"")
        }
        release {
            // TODO CI 통과를 위해 공백을 넣음. 추후 CD 설정 시 재설정
            val appLinkHost = localProperty.getProperty("APP_LINK_HOST") ?: ""
            buildConfigField("String", "APP_LINK_HOST", "\"https://${appLinkHost}\"")
        }
    }

    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    testImplementation(libs.kotlinx.coroutines.test)
    implementation(libs.kakao.share)
    implementation(libs.kizitonwose.calendar.compose)
    implementation(libs.haze)
}

dependencies {
    // orbit-test는 kotlinx-coroutines-test API를 노출하지만, 버전 충돌 방지를 위해 명시 선언한다.
    testImplementation(libs.kotlinx.coroutines.test)
}
