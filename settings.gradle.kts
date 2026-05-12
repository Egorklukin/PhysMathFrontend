pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
    plugins {
        id("com.android.application") version "9.2.0" apply false
        id("org.jetbrains.kotlin.android") version "2.2.10" apply false
        id("com.google.devtools.ksp") version "2.3.2" apply false
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "PhysMath"
include(":app")