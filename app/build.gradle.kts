plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

android {
    signingConfigs {
        create("release") {
            storeFile = file("../shave.jks")
            storePassword = env.fetch("KEYSTORE_PASSWORD")
            keyPassword = env.fetch("KEY_PASSWORD")
            keyAlias = env.fetch("KEY_ALIAS")
        }
    }
    namespace = "com.mean.shave"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.mean.shave"
        minSdk = 23
        targetSdk = 37
        versionCode = 8
        versionName = "1.3.3"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            // 完整 R8 优化（代码 + 资源）。
            // AGP 9.3+ 新 DSL：不设置 packageScope 即为全量优化，
            // 且默认自带 Android 平台保留规则（等同 proguard-android-optimize.txt）。
            optimization {
                enable = true
            }
            signingConfig = signingConfigs.getByName("release")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material3.adaptive.navigation.suite)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)

    // DataStore
    implementation(libs.datastore)
    // 图标扩展
    implementation(libs.material.icons.extended)
    // Material
    implementation(libs.material.components)
    // XLog
    implementation(libs.xlog)
    // InvalidFragmentVersionForActivityResult
    implementation(libs.fragment.ktx)
}