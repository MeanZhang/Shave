pluginManagement {
    repositories {
        mavenLocal()
        google()
        mavenCentral()
        gradlePluginPortal()
        maven("https://mirrors.cloud.tencent.com/repository/maven/")
        maven("https://repo.nju.edu.cn/repository/maven-public/")
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenLocal()
        google()
        mavenCentral()
        maven("https://mirrors.cloud.tencent.com/repository/maven/")
        maven("https://repo.nju.edu.cn/repository/maven-public/")
    }
}
rootProject.name = "Shave"
include(":app")
