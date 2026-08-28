plugins {
    id("com.android.application")
    id("kotlin-parcelize")
    id("dagger.hilt.android.plugin")
    id("com.google.devtools.ksp")
    alias(libs.plugins.compose.compiler)
}

android {
    compileSdk = 37
    ndkVersion = "21.3.6528147"

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    androidResources {
        generateLocaleConfig = true
    }

    defaultConfig {
        applicationId = "io.github.fate_grand_automata"
        minSdk = 24
        targetSdk = 36
        versionCode = System.getenv("FGA_VERSION_CODE")?.toInt() ?: 1
        versionName = System.getenv("FGA_VERSION_NAME") ?: System.getenv("FGA_VERSION_CODE") ?: "0.1.0"
    }

    signingConfigs {
        getByName("debug") {
            storeFile = file("fgadebug.keystore")
            storePassword = "123abc"
            keyAlias = "fgadebug"
            keyPassword = "123abc"
        }
        create("release") {
            storeFile = file("fgautomata.keystore")
            storePassword = System.getenv("KEYSTORE_PASS")
            keyAlias = "fgautomata"
            keyPassword = System.getenv("KEYSTORE_PASS")
        }
    }

    buildTypes {
        getByName("debug") {
            isDebuggable = true
            applicationIdSuffix = ".test"
            signingConfig = signingConfigs.getByName("debug")
        }
        getByName("release") {
            isMinifyEnabled = true
            // TODO test app extensively before enabling
            // isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.getByName("release")
        }
        create("ci") {
            initWith(getByName("release"))
            applicationIdSuffix = ".debug"
            signingConfig = signingConfigs.getByName("debug")

            ndk {
                //noinspection ChromeOsAbiSupport
                abiFilters.add("armeabi-v7a")
                abiFilters.add("arm64-v8a")
            }
        }
    }
    lint {
        abortOnError = false
        disable += "MissingTranslation"
    }
    namespace = "io.github.fate_grand_automata"
}

kotlin {
    compilerOptions {
        optIn.add("androidx.compose.material3.ExperimentalMaterial3Api")
        optIn.add("androidx.compose.foundation.ExperimentalFoundationApi")
        optIn.add("androidx.compose.animation.ExperimentalAnimationApi")
        optIn.add("androidx.compose.ui.ExperimentalComposeUiApi")
        optIn.add("androidx.compose.foundation.layout.ExperimentalLayoutApi")
        optIn.add("androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi")
    }
}

dependencies {
    implementation(project(":libautomata"))
    implementation(project(":scripts"))
    implementation(project(":prefs"))


    implementation(libs.kotlin.stdlib)

    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.core.ktx)

    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.documentfile)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.constraintlayout)

    implementation(libs.opencv)
    implementation(libs.tesseract4android)

    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.lifecycle.viewmodel.savedstate)
    implementation(libs.lifecycle.viewmodel.compose)


    implementation(libs.google.gson)

    implementation(libs.dagger.hilt.android)
    ksp(libs.dagger.hilt.compiler)

    implementation(libs.timber)

    val composeBom = platform(libs.compose.bom)
    implementation(composeBom)
    androidTestImplementation(composeBom)
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.tooling)
    implementation(libs.compose.foundation)
    implementation(libs.compose.material3)
    implementation(libs.compose.material3.window.size)
    implementation(libs.compose.material.icons.core)
    implementation(libs.compose.material.icons.extended)

    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.hilt.lifecycle.viewmodel.compose)



    implementation(libs.accompanist.permissions)

    implementation(libs.google.android.play.update.ktx)
    implementation(libs.coil)
    implementation(libs.coil.gif)

}

/*
Keeps SupportNameResources in sync with the default support images. A key that no longer
matches an asset degrades silently to the English name — indistinguishable from the
intended fallback for user-added supports — so it has to fail the build instead.

Hooked into `preBuild` rather than `check` because CI only ever runs `assembleCi`.
*/
val verifySupportNames = tasks.register("verifySupportNames") {
    group = "verification"
    description = "Checks SupportNameResources against the default support images."

    val servantDir = layout.projectDirectory.dir("src/main/assets/Support/servant")
    val ceDir = layout.projectDirectory.dir("src/main/assets/Support/ce")
    val source = layout.projectDirectory
        .file("src/main/java/io/github/fate_grand_automata/util/SupportNameResources.kt")
    val stamp = layout.buildDirectory.file("verifySupportNames.stamp")

    inputs.dir(servantDir).withPathSensitivity(PathSensitivity.RELATIVE)
    inputs.dir(ceDir).withPathSensitivity(PathSensitivity.RELATIVE)
    inputs.file(source).withPathSensitivity(PathSensitivity.RELATIVE)
    outputs.file(stamp)

    doLast {
        val text = source.asFile.readText()

        // Matched on the resource prefix rather than the map block, so reformatting the
        // source or renaming the maps can't quietly turn the check into a no-op.
        fun mappedNames(resPrefix: String) =
            Regex(""""([^"]+)"\s+to\s+R\.string\.$resPrefix""")
                .findAll(text)
                .map { it.groupValues[1] }
                .toSet()

        val problems = mutableListOf<String>()

        fun compare(kind: String, assetNames: Set<String>, mapped: Set<String>) {
            if (assetNames.isEmpty()) {
                problems += "found no $kind assets at all — has the layout changed?"
            }
            (assetNames - mapped).sorted().forEach {
                problems += "$kind \"$it\" has no entry in SupportNameResources, " +
                        "so it would display untranslated"
            }
            (mapped - assetNames).sorted().forEach {
                problems += "$kind \"$it\" is mapped in SupportNameResources " +
                        "but no such asset exists"
            }
        }

        compare(
            "servant",
            servantDir.asFile.listFiles().orEmpty()
                .filter { it.isDirectory }
                .map { it.name }
                .toSet(),
            mappedNames("servant_name_")
        )
        compare(
            "CE",
            ceDir.asFile.listFiles().orEmpty()
                .filter { it.isFile && it.extension == "png" }
                .map { it.nameWithoutExtension }
                .toSet(),
            mappedNames("ce_name_")
        )

        if (problems.isNotEmpty()) {
            throw GradleException(
                problems.joinToString(
                    separator = "\n  - ",
                    prefix = "SupportNameResources is out of sync with assets/Support:\n  - "
                )
            )
        }

        stamp.get().asFile.apply {
            parentFile.mkdirs()
            writeText("ok")
        }
    }
}

tasks.named("preBuild") { dependsOn(verifySupportNames) }
