import com.diffplug.spotless.LineEnding

// Top-level build file where you can add configuration options common to all sub-projects/modules.
@Suppress("DSL_SCOPE_VIOLATION") // TODO: Remove once KTIJ-19369 is fixed
plugins {
    alias(libs.plugins.com.android.application) apply false
    alias(libs.plugins.org.jetbrains.kotlin.android) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.spotless)
    alias(libs.plugins.dotenv)
}

spotless {
    // TODO 等待修复
    lineEndings = LineEnding.PLATFORM_NATIVE
    val ktlintVersion = "1.1.1"

    kotlin {
        target("**/*.kt")
        targetExclude("$buildDir/**/*.kt", "bin/**/*.kt")
        ktlint(ktlintVersion)
            .setEditorConfigPath("$projectDir/.editorconfig")
    }
    kotlinGradle {
        target("**.gradle.kts")
        ktlint(ktlintVersion)
    }
}
