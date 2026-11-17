import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.ben.manes.versions)
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.hilt.android) apply false
}

subprojects {
    // Compile every module with JDK 21, whichever JVM happens to launch Gradle.
    // Bytecode stays at 11 - see jvmTarget below and each module's compileOptions.
    pluginManager.withPlugin("java-base") {
        extensions.configure<JavaPluginExtension> {
            toolchain {
                languageVersion.set(JavaLanguageVersion.of(21))
            }
        }
    }

    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }
}

tasks.register<Delete>("clean") {
    description = "Cleans the build directory of the root project."
    delete(rootProject.layout.buildDirectory)
}

/**
 * Google and androidx publish alphas to the same channel as stable releases, so
 * `dependencyUpdates` reports them as available updates unless they are filtered out here.
 *
 * A version is stable if it is digits and separators only, or has release, final, or GA in its
 * version.
 */
fun isStable(version: String): Boolean {
    val stableSuffix = listOf("RELEASE", "FINAL", "GA").any { version.uppercase().contains(it) }
    return stableSuffix || Regex("^[0-9,.v-]+(-r)?$").matches(version)
}

tasks.withType<DependencyUpdatesTask>().configureEach {
    rejectVersionIf { !isStable(candidate.version) }
}
