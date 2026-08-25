import com.android.build.gradle.internal.api.BaseVariantOutputImpl
import java.io.FileInputStream
import java.util.Properties

// AGP
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    // 命名空间
    namespace = "com.ltx"
    // 编译时使用的Android SDK版本
    compileSdk = 37
    defaultConfig {
        // 应用ID: 包名
        applicationId = "com.ltx"
        // 最低支持SDK版本
        minSdk = 26
        // 目标设备的SDK版本
        targetSdk = 37
        // 版本号
        versionCode = 20
        // 版本名称
        versionName = "2.6.1"
        // 单元测试
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    // ==================== 自签名配置开始 ====================
    signingConfigs {
        create("release") {
            // 1. 优先从环境变量读取（给 GitHub Actions 使用）
            val envKeystorePath = System.getenv("KEYSTORE_PATH")
            val envKeystorePassword = System.getenv("KEYSTORE_PASSWORD")
            val envKeyAlias = System.getenv("KEY_ALIAS")
            val envKeyPassword = System.getenv("KEY_PASSWORD")

            // 2. 尝试从 local.properties 读取（给本地 Android Studio 编译使用）
            val localProperties = Properties()
            val localPropertiesFile = rootProject.file("local.properties")
            if (localPropertiesFile.exists()) {
                localProperties.load(FileInputStream(localPropertiesFile))
            }

            val keystorePath = envKeystorePath ?: localProperties.getProperty("KEYSTORE_PATH")
            val keystorePassword = envKeystorePassword ?: localProperties.getProperty("KEYSTORE_PASSWORD")
            val keyAlias = envKeyAlias ?: localProperties.getProperty("KEY_ALIAS")
            val keyPassword = envKeyPassword ?: localProperties.getProperty("KEY_PASSWORD")

            // 3. 如果配置了签名文件，则进行配置
            if (!keystorePath.isNullOrEmpty()) {
                storeFile = file(keystorePath)
                storePassword = keystorePassword
                this.keyAlias = keyAlias
                this.keyPassword = keyPassword
                // 开启 V1 和 V2, V3签名，确保高版本 Android 系统正常安装
                enableV1Signing = true
                enableV2Signing = true
                enableV3Signing = true
            }
        }
    }
    // ==================== 自签名配置结束 ====================

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"
            )
            // 绑定上面配置好的 release 自签名
            signingConfig = signingConfigs.getByName("release")
        }
    }

    // 自定义APK输出名称
    applicationVariants.all {
        outputs.all {
            (this as BaseVariantOutputImpl).outputFileName =
                "AutoSlide-v${defaultConfig.versionName}.apk"
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        // 启用视图绑定
        viewBinding = true
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.shizuku.api)
    implementation(libs.shizuku.provider)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
