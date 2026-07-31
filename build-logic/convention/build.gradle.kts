plugins {
    `kotlin-dsl`
}

dependencies {
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.kotlin.gradlePlugin)
    compileOnly(libs.ksp.gradlePlugin)
}

gradlePlugin {
    plugins {
        register("androidApplication") {
            id = "todakun.android.application"
            implementationClass = "AndroidApplicationConventionPlugin"
        }
        register("androidLibrary") {
            id = "todakun.android.library"
            implementationClass = "AndroidLibraryConventionPlugin"
        }
        register("androidLibraryHilt") {
            id = "todakun.android.library.hilt"
            implementationClass = "AndroidLibraryHiltConventionPlugin"
        }
        register("androidLibraryCompose") {
            id = "todakun.android.library.compose"
            implementationClass = "AndroidLibraryComposeConventionPlugin"
        }
        register("kotlinLibrary") {
            id = "todakun.kotlin.library"
            implementationClass = "KotlinLibraryConventionPlugin"
        }
        register("feature") {
            id = "todakun.feature"
            implementationClass = "FeatureConventionPlugin"
        }
        register("androidSigning") {
            id = "todakun.android.signing"
            implementationClass = "AndroidSigningConventionPlugin"
        }
    }
}
