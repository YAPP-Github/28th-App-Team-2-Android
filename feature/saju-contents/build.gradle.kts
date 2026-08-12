import java.util.Properties
import kotlin.apply

plugins {
    id("todakun.feature")
}

val props =
    Properties().apply {
        val f = rootProject.file("local.properties")
        if (f.exists()) f.inputStream().use { load(it) }
    }

android {
    namespace = "com.kikidan.sajucontents"

    buildTypes {
        debug {
            val appLinkHost = props.getProperty("APP_LINK_HOST_DEV") ?: error("APP_LINK_HOST_DEV가 null 입니다")
            buildConfigField("String", "APP_LINK_HOST", "\"https://${appLinkHost}\"")
        }
        release {
            val appLinkHost = props.getProperty("APP_LINK_HOST") ?: error("APP_LINK_HOST가 null 입니다")
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
}
