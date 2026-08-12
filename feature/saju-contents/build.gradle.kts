plugins {
    id("todakun.feature")
}

android {
    namespace = "com.kikidan.sajucontents"
}

dependencies {
    testImplementation(libs.kotlinx.coroutines.test)
    implementation(libs.kakao.share)
}
