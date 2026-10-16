pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("androidx\\..*")
                includeGroupByRegex("com\\.android(\\..*|)")
                includeGroupByRegex("com\\.google\\.android\\..*")
                includeGroupByRegex("com\\.google\\.testing\\.platform")
            }
            mavenContent {
                releasesOnly()
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google {
            content {
                includeGroupByRegex("androidx\\..*")
                includeGroupByRegex("com\\.android(\\..*|)")
                includeGroupByRegex("com\\.google\\.android\\..*")
                includeGroupByRegex("com\\.google\\.testing\\.platform")
            }
            mavenContent {
                releasesOnly()
            }
        }
        // fallback for the rest of the dependencies
        mavenCentral()
        // tesseract4android is published nowhere else
        maven("https://jitpack.io") {
            content {
                includeGroup("cz.adaptech.tesseract4android")
            }
        }
    }
}

include(":prefs")
include(":scripts")
include(":libautomata")
include(":app")

rootProject.name="Fate Grand Automata"
