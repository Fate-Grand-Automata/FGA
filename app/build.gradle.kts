import javax.inject.Inject

plugins {
    alias(libs.plugins.android.application)
    id("kotlin-parcelize")
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlin.serialization)
}

/*
 * OpenCV is pinned to 4.11.0 because every later release bundles a KleidiCV that runs
 * SVE instructions, and BlueStacks Air on macOS advertises SVE2 in HWCAP2 without
 * implementing SVE, so those builds die with SIGILL. Do not bump `opencv_version`
 * until that is fixed upstream.
 *
 * The 4.11.0 AAR ships a 4 KB aligned libc++_shared.so, which Play's 16 KB page size
 * requirement rejects, so the 16 KB aligned copy from a current OpenCV release is
 * packaged in its place. libc++_shared.so only ever gains symbols, so the newer one
 * still satisfies the older libopencv_java4.so.
 */
val alignedLibCxx = configurations.create("alignedLibCxx") {
    isTransitive = false
}

abstract class ExtractAlignedLibCxx : DefaultTask() {
    @get:InputFiles
    abstract val aar: ConfigurableFileCollection

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    @get:Inject
    abstract val archives: ArchiveOperations

    @get:Inject
    abstract val files: FileSystemOperations

    @TaskAction
    fun extract() {
        files.sync {
            from(archives.zipTree(aar.singleFile)) {
                include("jni/*/libc++_shared.so")
                // jniLibs source dirs are laid out as <abi>/<lib>, the AAR as jni/<abi>/<lib>.
                eachFile { path = path.removePrefix("jni/") }
            }
            includeEmptyDirs = false
            into(outputDir)
        }

        /*
         * On an empty extraction the packaging step falls back to OpenCV's own 4 KB aligned
         * copy and the build still succeeds, so Play would be the first thing to complain.
         */
        if (!outputDir.get().file("arm64-v8a/libc++_shared.so").asFile.exists()) {
            throw GradleException(
                "No arm64-v8a/libc++_shared.so in ${aar.singleFile.name} — its layout changed.",
            )
        }
    }
}

val extractAlignedLibCxx = tasks.register<ExtractAlignedLibCxx>("extractAlignedLibCxx") {
    description = "Extracts the 16 KB aligned libc++_shared.so packaged in place of OpenCV's."
    aar.from(alignedLibCxx)
    outputDir.set(layout.buildDirectory.dir("alignedLibCxx"))
}

androidComponents {
    onVariants { variant ->
        variant.sources.jniLibs?.addGeneratedSourceDirectory(
            extractAlignedLibCxx,
            ExtractAlignedLibCxx::outputDir,
        )
    }
}

android {
    compileSdk = 37

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
    // run tests in CI builds instad of debug
    testBuildType = "ci"

    packaging {
        jniLibs {
            // Project-local jniLibs are merged ahead of any dependency's, so ours wins.
            pickFirsts += "**/libc++_shared.so"
        }
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
    implementation(libs.androidx.constraintlayout)

    implementation(libs.opencv)
    alignedLibCxx("${libs.opencv.aligned.libcxx.get()}@aar")
    implementation(libs.tesseract4android)

    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.lifecycle.viewmodel.savedstate)
    implementation(libs.lifecycle.viewmodel.compose)


    implementation(libs.kotlinx.serialization.json)

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
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.navigation3.ui)
    implementation(libs.androidx.lifecycle.viewmodel.navigation3)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.hilt.lifecycle.viewmodel.compose)



    implementation(libs.accompanist.permissions)

    implementation(libs.google.android.play.update.ktx)
    implementation(libs.coil)
    implementation(libs.coil.gif)

    implementation(libs.reorderable)

    testImplementation(platform(libs.junit.bom)) {
        because("kotlin-test comes with conflicting junit versions")
    }
    testImplementation(libs.kotlin.test.junit5)
    testImplementation(libs.willowtreeapps.assertk)
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()

    val supportAssets = layout.projectDirectory.dir("src/main/assets/Support")

    /*
     * Assets are not on the unit test classpath, and relying on the working directory breaks
     * as soon as the test is run from the IDE instead of Gradle.
     */
    systemProperty("fga.supportAssets", supportAssets.asFile.path)

    /*
     * Triggers the task when the support assets change, so that the system property above is always
     * up to date.
     */
    inputs.dir(supportAssets).withPathSensitivity(PathSensitivity.RELATIVE)
}
