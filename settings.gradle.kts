pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        // Vosk speech recognition artifacts are hosted here
        maven { url = uri("https://alphacephei.com/maven/") }
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        // Required for Vosk
        maven { url = uri("https://alphacephei.com/maven/") }
    }
}

rootProject.name = "ai-ime-merged"
include(":app")