import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig

plugins {
    id("android-library-convention")
    id("kotlin-library-convention")
    id("org.jetbrains.compose")
    alias(libs.plugins.composeCompiler)
}
android {
    namespace = "com.onion.ui.theme"
}

kotlin {



    sourceSets {
        named("commonMain") {
            dependencies {
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.ui)
                implementation(compose.components.resources)
                implementation(compose.components.uiToolingPreview)
                implementation(libs.compose.adaptive)
                implementation(libs.compottie)
                implementation(libs.compottie.dot)
            }
        }
        named("androidMain") {
            dependencies {
                //implementation(libs.ktor.client.android)
            }
        }
        named("desktopMain") {
            dependencies {
                //implementation(libs.ktor.client.java)
            }
        }
    }
}

