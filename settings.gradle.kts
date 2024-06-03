pluginManagement {
    repositories {
        if (System.getenv("CI") != "true") {
            maven("https://repo.nju.edu.cn/repository/maven-public/")
        }
        mavenLocal()
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        if (System.getenv("CI") != "true") {
            maven("https://repo.nju.edu.cn/repository/maven-public/")
        }
        mavenLocal()
        google()
        mavenCentral()
        maven("https://mirrors.cloud.tencent.com/repository/maven/")
    }
}
rootProject.name = "Shave"
include(":app")
