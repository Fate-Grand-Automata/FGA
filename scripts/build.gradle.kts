import org.gradle.api.attributes.java.TargetJvmVersion
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("java-library")
    id("kotlin")
    id("com.google.devtools.ksp")
    alias(libs.plugins.kotlin.serialization)
}

dependencies {
//    implementation fileTree(dir: "libs", include: ["*.jar"])
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    api(project(":libautomata"))

    implementation(libs.kotlin.stdlib)
    // api: the @Serializable models are part of this module's public surface
    api(libs.kotlinx.serialization.core)

    implementation(libs.dagger.hilt.core)
    ksp(libs.dagger.hilt.compiler)

    testImplementation(platform(libs.junit.bom)) {
        because("kotlin-test comes with conflicting junit versions")
    }
    testImplementation(libs.kotlin.test)
    testImplementation(libs.willowtreeapps.assertk)
    testImplementation(libs.mockk)
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

tasks.test {
    useJUnitPlatform()
}

// Unit tests run on the Gradle toolchain JVM (21) and never reach Android, so they may target a
// newer bytecode level than the library itself, which stays at 11 for `app`. JUnit >= 6 ships
// Java 17 bytecode, and Gradle's variant resolution rejects it against a Java 11 test compilation.
tasks.compileTestKotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

// Kotlin refuses to compile when the paired Java task disagrees, even with no Java test sources.
tasks.compileTestJava {
    sourceCompatibility = JavaVersion.VERSION_17.toString()
    targetCompatibility = JavaVersion.VERSION_17.toString()
    options.release.set(17)
}

listOf(configurations.testCompileClasspath, configurations.testRuntimeClasspath).forEach { config ->
    config.configure {
        attributes {
            attribute(TargetJvmVersion.TARGET_JVM_VERSION_ATTRIBUTE, 17)
        }
    }
}
