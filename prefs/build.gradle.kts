plugins {
    id("com.android.library")
    id("kotlin-android")
    id("com.google.devtools.ksp")

    kotlin("plugin.serialization") version libs.versions.kotlin.version.get()
}

android {
    compileSdk = 34

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    defaultConfig {
        minSdk = 24
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
//            proguardFiles getDefaultProguardFile("proguard-android-optimize.txt")
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"))
        }
        create("ci") {
            initWith(getByName("release"))
        }
    }
    namespace = "io.github.fate_grand_automata.prefs"
}

dependencies {
    implementation(project(":scripts"))
    implementation(libs.androidx.core.ktx)

    implementation(libs.google.gson)

    implementation(libs.dagger.hilt.android)
    ksp(libs.dagger.hilt.compiler)

    api(libs.fredporciuncula.flow.preferences)

    implementation(libs.kotlinx.datetime)
    implementation(libs.kotlinx.serialization)
}