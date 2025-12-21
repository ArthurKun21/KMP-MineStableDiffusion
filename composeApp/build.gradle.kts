import org.gradle.kotlin.dsl.support.serviceOf
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig
import java.util.Locale
import java.lang.System.getenv

plugins {
    id("android-application-convention")
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeHotReload)
}

kotlin {
    androidTarget {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_21)
        }
    }
    
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }
    
    jvm("desktop")
    
    sourceSets {
        val desktopMain by getting
        
        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)
        }
        commonMain.dependencies {
            // ---- Resource,KMP目前无法跨模块获取Res ------
            implementation(compose.materialIconsExtended)
            implementation(compose.components.resources)
            // ---- DI ------
            implementation(libs.koin.core)
            implementation(libs.koin.compose.viewmodel)
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            // ---- App Runtime ------
            implementation(libs.runtime.shapes)
            implementation(libs.runtime.navigation)
            implementation(libs.runtime.savedstate)
            implementation(libs.runtime.viewmodel)
            implementation(libs.runtime.lifecycle)
            // ---- IO ------
            implementation(libs.filekit.core)
            implementation(libs.filekit.dialogs)
            implementation(libs.filekit.dialogs.compose)
            implementation(libs.filekit.coil)
            implementation(libs.coil.compose)
            // ---- Project Module ------
            implementation(projects.shared)
            implementation(projects.uiTheme)
            implementation(projects.dataNetwork)
            implementation(libs.quickjs.kt)
        }
        commonTest.dependencies {
            implementation(projects.dataNetwork)
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutinesTest)
            implementation(libs.quickjs.kt)
            implementation("org.drewcarlson:ktsoup-core:0.6.0")
            implementation("org.drewcarlson:ktsoup-fs:0.6.0")
            implementation("org.drewcarlson:ktsoup-ktor:0.6.0")
        }
        desktopMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutinesSwing)
        }

        val jvmMain by creating {
            dependencies {
                //implementation(fileTree(mapOf("dir" to "path/path", "include" to listOf("*.jar"))))
            }
        }
        desktopMain.dependsOn(jvmMain)
    }
}

android {
    namespace = "org.onion.diffusion"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "org.onion.diffusion"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = libs.versions.app.version.get()
        ndk {
            abiFilters.clear()
            abiFilters += "arm64-v8a"
        }
    }

    signingConfigs {
        create("release") {
            storeFile = file("release.jks")
            storePassword = getenv("RELEASE_KEY_STORE_PASSWORD")
            keyAlias = "nova"
            keyPassword = getenv("RELEASE_KEY_STORE_PASSWORD")
            enableV1Signing = true
            enableV2Signing = true
            enableV3Signing = true
        }
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        if (getenv("RELEASE_KEY_EXISTS") == "true") {
            getByName("release") {
                isShrinkResources = true
                isMinifyEnabled = true
                proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }
    android.applicationVariants.all {
        outputs.all {
            if (this is com.android.build.gradle.internal.api.ApkVariantOutputImpl) {
                this.outputFileName = "MineStableDiffusion-$versionName.apk"
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    externalNativeBuild {
        cmake {
            path = file("${rootProject.extra["dirCppMakeFile"]}")
            version = "3.22.1"
        }
    }
    lint {
        disable += "NullSafeMutableLiveData"
    }
}

dependencies {
    debugImplementation(compose.uiTooling)
}

compose.desktop {
    application {
        mainClass = "org.onion.diffusion.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "org.onion.diffusion"
            packageVersion = "1.0.0"

            targetFormats(
                TargetFormat.Dmg,
                TargetFormat.Msi,
                TargetFormat.Exe,
                TargetFormat.Deb,
                TargetFormat.Rpm
            )

            packageName = "MineStableDiffusion"
            packageVersion = libs.versions.app.version.get()
            vendor = "Onion"
            licenseFile.set(rootProject.rootDir.resolve("LICENSE"))


            linux {
                iconFile.set(rootProject.file("docs/AppIcon.png"))
            }
            windows {
                iconFile.set(rootProject.file("docs/AppIcon.ico"))
                dirChooser = true
                perUserInstall = true
                shortcut = true
                menu = true
            }
            macOS {
                iconFile.set(rootProject.file("docs/AppIcon.icns"))
                bundleID = "org.onion.diffusion"
                appCategory = "public.app-category.productivity"
                jvmArgs += listOf(
                    "-Dapple.awt.application.name=MineStableDiffusion",
                    "-Dsun.java2d.metal=true",
                    "--add-opens=java.desktop/sun.lwawt=ALL-UNNAMED",
                    "--add-opens=java.desktop/sun.lwawt.macosx=ALL-UNNAMED",
                )
            }
        }

        jvmArgs += listOf(
            //"-XX:+UseZGC",
            "-XX:SoftMaxHeapSize=2048m",
            "--add-opens=java.desktop/java.awt.peer=ALL-UNNAMED",
            "--add-opens=java.desktop/sun.awt=ALL-UNNAMED",
            "-Xverify:none" // 错误 java.lang.VerifyError: Expecting a stackmap frame 表明 JVM 在加载类时，发现 Compose 编译器生成的字节码与 Java 21 严格的验证机制不兼容。 你需要禁用 JVM 的字节码验证，或者降级 Java 版本。最快的修复方法是在 build.gradle.kts 的 jvmArgs 中添加 -Xverify:none。
        )

        buildTypes.release.proguard {
            isEnabled = false
            version.set("7.7.0")
            configurationFiles.from("proguard-rules.pro")
        }
    }
}

// ------------------------------------------------------------------------
// 配置Run脚本
// ------------------------------------------------------------------------
afterEvaluate {
    val run = tasks.named("run")
    // 运行 桌面程序 Debug
    val desktopRunDebug by tasks.registering {
        dependsOn(run)
    }
}

// 获取当前的操作系统
val osName = System.getProperty("os.name").lowercase(Locale.getDefault())

// 提取路径为本地 File 对象，避免在 Task Action 中引用 project
val cppSourceDir = layout.projectDirectory.dir("cpp/diffusion-loader.cpp").asFile
// 从 Extra 中安全获取路径 (在配置阶段完成)
val libsExportDir = file(rootProject.extra["cppLibsDir"].toString())
val jvmResDir = file(rootProject.extra["jvmResourceLibDir"].toString())

val platforms = listOf("windows", "macos", "linux")

platforms.forEach { platform ->
    val taskName = "buildNativeLibFor${platform.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }}"

    tasks.register(taskName) {
        group = "native-build"

        // 只有平台匹配才执行
        val isTargetPlatform = when (platform) {
            "windows" -> osName.contains("windows")
            "macos" -> osName.contains("mac")
            "linux" -> osName.contains("linux")
            else -> false
        }

        onlyIf { isTargetPlatform }

        // 声明输入输出，以便 Gradle 跳过未变动的构建 (UP-TO-DATE)
        inputs.dir(cppSourceDir)
        outputs.dir(cppSourceDir.resolve("build-$platform"))
        outputs.dir(jvmResDir)

        // 使用 serviceOf 获取执行器，这是解决配置缓存的关键
        val execOps = serviceOf<ExecOperations>()

        doLast {
            val cmakeGenerator = when {
                osName.contains("windows") -> "MinGW Makefiles"
                osName.contains("mac") -> "Unix Makefiles" // macOS 建议用 Unix Makefiles 或 Xcode
                else -> "Unix Makefiles"
            }

            println("Checking CMake version...")
            exec { commandLine("cmake", "--version") }

            // 1. CMake Configure
            println("Configuring for $platform...")
            exec {
                workingDir = cppSourceDir
                val args = mutableListOf("cmake", "-S", ".", "-B", "build-$platform", "-G", cmakeGenerator)
                if (platform == "windows") {
                    args.addAll(listOf("-DCMAKE_CXX_USE_RESPONSE_FILE_FOR_OBJECTS=1", "-DCMAKE_NINJA_FORCE_RESPONSE_FILE=1"))
                }
                commandLine(args)
            }

            // 2. CMake Build
            println("Building for $platform...")
            exec {
                workingDir = cppSourceDir
                commandLine("cmake", "--build", "build-$platform", "--config", "Release")
            }

            // 3. 复制结果 (使用标准的 File API)
            if (libsExportDir.exists()) {
                println("Copying libraries from $libsExportDir to $jvmResDir")
                jvmResDir.mkdirs()
                libsExportDir.listFiles()?.filter {
                    val name = it.name.lowercase()
                    name.endsWith(".dll") || name.endsWith(".so") || name.endsWith(".dylib") || name.endsWith(".dll.a")
                }?.forEach {
                    it.copyTo(jvmResDir.resolve(it.name), overwrite = true)
                }
            }
        }
    }
}

// 注册总入口
tasks.register("buildNativeLibsIfNeeded") {
    val currentPlatformTask = when {
        osName.contains("windows") -> "buildNativeLibForWindows"
        osName.contains("mac") -> "buildNativeLibForMacos"
        osName.contains("linux") -> "buildNativeLibForLinux"
        else -> null
    }
    if (currentPlatformTask != null) {
        dependsOn(currentPlatformTask)
    }
}

// ------------------------------------------------------------------------
// 建立任务依赖
// ------------------------------------------------------------------------

tasks.withType<org.jetbrains.compose.desktop.application.tasks.AbstractJPackageTask>().configureEach {
    dependsOn("buildNativeLibsIfNeeded")
}

// 确保在运行和打包前执行原生构建
tasks.matching {
    it.name.contains("desktopRun") || it.name.contains("packageRelease")
}.configureEach {
    if (this.name != "buildNativeLibsIfNeeded") {
        dependsOn("buildNativeLibsIfNeeded")
    }
}

