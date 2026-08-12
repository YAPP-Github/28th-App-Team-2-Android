import com.android.build.api.dsl.ApplicationExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import java.io.File
import java.util.Properties

private const val DEBUG_STORE_NAME = ".keystore/todakun-debug.jks"
private const val RELEASE_STORE_NAME = ".keystore/todakun-release.jks"
private const val RELEASE_KEY_ALIAS = "release"

private const val RELEASE_PASSWORD_PROPERTY = "todakun.release.password"

class AndroidSigningConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.withPlugin("com.android.application") {
                extensions.configure<ApplicationExtension>("android") {
                    configureSigningConfigs(
                        rootProject.file(DEBUG_STORE_NAME),
                        rootProject.file(RELEASE_STORE_NAME),
                        rootProject.releasePassword()
                    )
                }
            }
        }
    }
}

private fun ApplicationExtension.configureSigningConfigs(
    debugStoreFile: File,
    releaseStoreFile: File,
    releasePassword: String?,
) {
    signingConfigs {
        getByName("debug") {
            storeFile = debugStoreFile
            storePassword = "android"
            keyAlias = "androiddebugkey"
            keyPassword = "android"
        }

        create("release") {
            storeFile = releaseStoreFile
            storePassword = releasePassword
            keyAlias = RELEASE_KEY_ALIAS
            keyPassword = releasePassword
        }
    }

    buildTypes {
        getByName("debug") {
            applicationIdSuffix = ".debug"
        }

        getByName("release") {
            signingConfig = signingConfigs.findByName("release")
        }
    }
}

private fun Project.releasePassword() =
    file("local.properties")
        .takeIf(File::isFile)
        ?.let { file -> Properties().apply { file.inputStream().use(::load) } }
        ?.getProperty(RELEASE_PASSWORD_PROPERTY)
