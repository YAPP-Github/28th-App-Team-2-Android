plugins {
    alias(libs.plugins.todakun.feature)
}

android {
    namespace = "com.kikidan.notification"

    defaultConfig {
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    testOptions {
        unitTests.isReturnDefaultValues = true
    }
}

dependencies {
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.core.ktx)
    implementation(libs.kotlinx.coroutines.core)

    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.messaging)
    implementation(libs.androidx.lifecycle.process)

    testImplementation(libs.kotlinx.coroutines.test)
}
