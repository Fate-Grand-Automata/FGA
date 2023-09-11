// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    val kotlinVersion by extra("1.8.22")
    val hiltVersion by extra("2.46.1")
    val gsonVersion by extra("2.10.1")

    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }

    dependencies {
        classpath("com.android.tools.build:gradle:8.0.2")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
        classpath("com.google.dagger:hilt-android-gradle-plugin:$hiltVersion")


        // NOTE: Do not place your application dependencies here; they belong
        // in the individual module build.gradle files
    }
}
plugins {
    id("com.github.ben-manes.versions") version "0.47.0"
}


allprojects {
    repositories {
        google()
        maven { url = uri("https://repo1.maven.org/maven2") }
        maven { url = uri("https://jitpack.io") }
        maven { url = uri("https://oss.sonatype.org/content/repositories/snapshots/") }
    }
}

subprojects {
    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
        kotlinOptions {
            freeCompilerArgs += "-opt-in=kotlin.time.ExperimentalTime"

            // Set JVM target to 11
            jvmTarget = "11"
        }
    }
}

tasks.register<Delete>("clean") {
    delete(rootProject.buildDir)
}