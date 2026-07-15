// Top-level build file where you can add configuration options common to all sub-projects/modules.
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.jlleitschuh.gradle.ktlint.KtlintExtension
import org.jlleitschuh.gradle.ktlint.reporter.ReporterType

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.jetbrains.kotlin.jvm) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.ktlint)
}

subprojects {
    apply(plugin = "org.jlleitschuh.gradle.ktlint")
}

allprojects {
    val versionCatalog = rootProject.extensions.getByType<VersionCatalogsExtension>().named("libs")
    val ktlintVersion = versionCatalog.findVersion("ktlint").get()

    plugins.withId("org.jlleitschuh.gradle.ktlint") {
        configure<KtlintExtension> {
            version.set(ktlintVersion.requiredVersion)
            reporters {
                reporter(ReporterType.PLAIN)
                reporter(ReporterType.SARIF)
            }
        }
        dependencies {
            add("ktlintRuleset", versionCatalog.findLibrary("nlopez-compose-rules-ktlint").get().get())
        }
    }
}
