@Suppress("DSL_SCOPE_VIOLATION") // TODO: Remove once KTIJ-19369 is fixed
plugins {
    alias(libs.plugins.com.android.application)
    alias(libs.plugins.org.jetbrains.kotlin.android)
}

android {
    namespace="com.mean.shave"
    compileSdk=33

    defaultConfig {
        applicationId="com.mean.shave"
        minSdk=23
        targetSdk=33
        versionCode=4
        versionName="1.2.0"

        testInstrumentationRunner="androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary=true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            versionNameSuffix="-debug"
        }
    }
    compileOptions {
        sourceCompatibility=JavaVersion.VERSION_17
        targetCompatibility=JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose=true
    }
    composeOptions {
        kotlinCompilerExtensionVersion="1.4.3"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(libs.core.ktx)
    implementation(libs.lifecycle.runtime.ktx)
    implementation(libs.activity.compose)
    implementation(platform(libs.compose.bom))
    implementation(libs.ui)
    implementation(libs.ui.graphics)
    implementation(libs.ui.tooling.preview)
    implementation(libs.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.ui.test.junit4)
    debugImplementation(libs.ui.tooling)
    debugImplementation(libs.ui.test.manifest)

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