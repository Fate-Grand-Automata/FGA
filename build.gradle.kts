import org.jetbrains.kotlin.gradle.dsl.JvmTarget

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
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.spotless) apply false
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
    apply(plugin = "com.diffplug.spotless")
    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
        compilerOptions {
            freeCompilerArgs.add("-opt-in=kotlin.time.ExperimentalTime")
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    configure<com.diffplug.gradle.spotless.SpotlessExtension> {
        kotlin {
            target("**/*.kt", "**/*.kts")
            targetExclude("**/build/**/*.kt")
            ktlint(libs.ktlint.core.get().version).editorConfigOverride(
                mapOf(
                    "ktlint_standard_annotation" to "disabled",
                    "ktlint_standard_package-name" to "disabled"
                ),
            )
            trimTrailingWhitespace()
            endWithNewline()
        }
        format("xml") {
            target("**/*.xml")
            trimTrailingWhitespace()
            endWithNewline()
        }
    }
}

tasks.register<Delete>("clean") {
    delete(rootProject.layout.buildDirectory)
}
