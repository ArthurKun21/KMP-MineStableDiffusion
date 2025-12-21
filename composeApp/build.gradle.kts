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

// ... (保留文件上方的代码)

// ------------------------------------------------------------------------
// 配置 Native 构建任务 (重构后)
//------------------------------------------------------------------------

// 定义原生库目录的 provider，避免在任务执行时直接访问 project
val cppLibsDir = rootProject.layout.projectDirectory.dir(rootProject.extra["cppLibsDir"].toString())
val jvmResourceLibDir = rootProject.layout.projectDirectory.dir(rootProject.extra["jvmResourceLibDir"].toString())
val sourceCodeDir = rootProject.layout.projectDirectory.dir("cpp/diffusion-loader.cpp")

val desktopPlatforms = listOf("windows", "macos", "linux")

desktopPlatforms.forEach { platform ->
    // 创建一个专门的构建任务，而不是通用的 DefaultTask
    tasks.register<Exec>("buildNativeLibFor${platform.capitalize()}") {
        group = "native-build"
        description = "Builds native libraries for $platform"

        // 仅在当前操作系统匹配时执行，否则仅打印跳过信息并执行空命令
        val osName = System.getProperty("os.name").lowercase(Locale.getDefault())
        val isCurrentPlatform = when (platform) {
            "windows" -> osName.contains("windows")
            "macos" -> osName.contains("mac")
            "linux" -> osName.contains("linux")
            else -> false
        }

        // 只有当前平台匹配才真正执行命令
        onlyIf { isCurrentPlatform }

        // 定义输入输出，帮助 Gradle 进行增量构建 (UP-TO-DATE 检查)
        inputs.dir(sourceCodeDir)
        // 假设输出在 build-platform 目录下，具体取决于你的 CMake 配置
        outputs.dir(layout.buildDirectory.dir("native/build-$platform"))

        if (isCurrentPlatform) {
            workingDir = file("$rootDir/cpp/diffusion-loader.cpp")

            val cmakeGenerator = when (platform) {
                "windows" -> "MinGW Makefiles"
                "macos" -> "Xcode"
                else -> "Unix Makefiles"
            }

            val cmakeOptions = mutableListOf<String>()
            if (platform == "windows") {
                cmakeOptions.addAll(listOf(
                    "-DCMAKE_CXX_USE_RESPONSE_FILE_FOR_OBJECTS=1",
                    "-DCMAKE_CXX_USE_RESPONSE_FILE_FOR_LIBRARIES=1",
                    "-DCMAKE_CXX_RESPONSE_FILE_LINK_FLAG=\"@\"",
                    "-DCMAKE_NINJA_FORCE_RESPONSE_FILE=1"
                ))
            } else if (platform == "macos") {
                cmakeOptions.add("-DCMAKE_OSX_ARCHITECTURES=arm64;x86_64")
            }

            // 配置 Exec 任务的命令行
            // 注意：这里简化了逻辑，Exec 只能运行一个命令。
            // 建议将 cmake 配置和构建合并为一个 shell 脚本，或者分为两个依赖任务。
            // 为了简单起见，这里演示将其改为 doFirst/doLast 结构，但避免引用 project

            commandLine("cmake", "--version") // 占位符，实际逻辑在下面

            doLast {
                println("开始构建 $platform 平台的原生库...")

                // 1. CMake Configure
                exec {
                    workingDir = this@register.workingDir
                    commandLine(
                        "cmake", "-S", ".", "-B", "build-$platform", "-G", cmakeGenerator, *cmakeOptions.toTypedArray()
                    )
                }

                // 2. CMake Build
                exec {
                    workingDir = this@register.workingDir
                    commandLine("cmake", "--build", "build-$platform", "--config", "Release")
                }

                // 3. Copy files (使用文件操作而不是 project.copy)
                val srcDir = cppLibsDir.asFile
                val destDir = jvmResourceLibDir.asFile

                if (srcDir.exists()) {
                    srcDir.listFiles { file ->
                        file.name.endsWith(".dll") ||
                                file.name.endsWith(".dll.a") ||
                                file.name.endsWith(".so") ||
                                file.name.endsWith(".dylib")
                    }?.forEach { file ->
                        file.copyTo(destDir.resolve(file.name), overwrite = true)
                    }
                    println("已将库文件从 $srcDir 复制到 $destDir")
                }
            }
        } else {
            // 非当前平台，执行一个空命令避免报错
            commandLine("echo", "Skipping build for $platform on $osName")
        }
    }
}

// 注册一个总入口任务
tasks.register("buildNativeLibsIfNeeded") {
    group = "native-build"

    // 动态计算当前平台对应的任务名称
    val currentPlatformName = when {
        System.getProperty("os.name").lowercase(Locale.getDefault()).contains("windows") -> "Windows"
        System.getProperty("os.name").lowercase(Locale.getDefault()).contains("mac") -> "Macos"
        System.getProperty("os.name").lowercase(Locale.getDefault()).contains("linux") -> "Linux"
        else -> null
    }

    // 关键修复：使用 dependsOn 而不是在 doFirst 中手动 execute
    if (currentPlatformName != null) {
        dependsOn("buildNativeLibFor$currentPlatformName")
    }

    doLast {
        println("原生库检查/构建流程完成。")
        // 如果文件不存在的额外检查逻辑可以放在这里，
        // 但理论上 buildNativeLibFor... 任务执行完后文件就应该存在了。
    }
}

// 建立任务依赖
tasks.matching { it.name.contains("desktopRun") }.configureEach {
    dependsOn("buildNativeLibsIfNeeded")
}

// 针对打包任务（你在日志中执行的是 packageReleaseDeb/Rpm）
tasks.matching { it.name.contains("packageRelease") }.configureEach {
    dependsOn("buildNativeLibsIfNeeded")
}

