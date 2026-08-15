plugins {
    alias(libs.plugins.todakun.feature)
}

android {
    namespace = "com.kikidan.home"

    defaultConfig {
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }
}

dependencies {
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.core.ktx)
    implementation(libs.kyant.backdrop)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.haze)
    testImplementation(libs.kotlinx.coroutines.test)
}
