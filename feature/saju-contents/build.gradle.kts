plugins {
    id("todakun.feature")
}

android {
    namespace = "com.kikidan.sajucontents"
}

dependencies {
    // orbit-test는 kotlinx-coroutines-test API를 노출하지만, 버전 충돌 방지를 위해 명시 선언한다.
    testImplementation(libs.kotlinx.coroutines.test)
}
