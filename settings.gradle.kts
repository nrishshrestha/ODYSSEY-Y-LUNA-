pluginManagement {
    repositories {
        google() // Keeps your Map and Android tools working
        mavenCentral()
        maven { url = uri("https://storage.zego.im/maven") }
        maven { url = uri("https://jitpack.io") }
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google() // Maps need this
        mavenCentral() // Maps and standard libraries need this
        maven { url = uri("https://storage.zego.im/maven") } // Zego needs this
        maven { url = uri("https://jitpack.io") } // UIKits need this
    }
}
rootProject.name = "ODYSSEY"
include(":app")
