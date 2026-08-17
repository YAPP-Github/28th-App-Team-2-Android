plugins {
    alias(libs.plugins.todakun.feature)
}

android {
    namespace = "com.kikidan.mypage"
}

dependencies {
    implementation(libs.play.app.update.ktx)
    implementation(libs.kotlinx.coroutines.play.services)
}
