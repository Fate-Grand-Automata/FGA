// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {

    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }

    dependencies {
        // https://developer.android.com/build/migrate-to-catalogs#migrate-dependencies
        classpath(libs.android.tools.build.gradle)
        classpath(libs.kotlin.gradle.plugin)
        classpath(libs.hilt.android.gradle.plugin)


        // NOTE: Do not place your application dependencies here; they belong
        // in the individual module build.gradle files
    }
}
plugins {
    alias(libs.plugins.ben.manes.versions)
    alias(libs.plugins.ksp) apply false
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
    delete(rootProject.layout.buildDirectory)
}