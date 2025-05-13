plugins {
    id("com.android.library")
    id("kotlin-android")
    id("com.google.devtools.ksp")
}

android {
    compileSdk = 35

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    defaultConfig {
        minSdk = 24
    }

    buildTypes {
        getByName("release") {
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
}