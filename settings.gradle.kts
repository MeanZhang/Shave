pluginManagement {
    repositories {
        if (System.getenv("METERED_CONNECTION") == "0") {
            maven("https://mirrors.cloud.tencent.com/repository/maven/")
        }
        if (System.getenv("CI") != "true") {
            maven("https://repo.nju.edu.cn/repository/maven-public/")
        }
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        if (System.getenv("METERED_CONNECTION") == "0") {
            maven("https://mirrors.cloud.tencent.com/repository/maven/")
        }
        if (System.getenv("CI") != "true") {
            maven("https://repo.nju.edu.cn/repository/maven-public/")
        }
        google()
        mavenCentral()
    }
}
rootProject.name = "Shave"
include(":app")
