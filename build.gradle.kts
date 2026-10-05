// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
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
