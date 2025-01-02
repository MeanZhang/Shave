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
    val ktlintVersion = libs.versions.ktlint.get()

    kotlin {
        target("**/*.kt")
        targetExclude("${project.layout.buildDirectory}/**/*.kt", "bin/**/*.kt", "timepicker/**/*.kt")
        ktlint(ktlintVersion).setEditorConfigPath("$projectDir/.editorconfig").customRuleSets(
            listOf(
//                "io.nlopez.compose.rules:ktlint:${libs.versions.composeRules.get()}",
            ),
        )
    }
    kotlinGradle {
        target("**.gradle.kts")
        ktlint(ktlintVersion)
    }
}
