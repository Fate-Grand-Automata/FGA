plugins {
    id("com.android.library")
    id("com.google.devtools.ksp")
    alias(libs.plugins.kotlin.serialization)
}

android {
    compileSdk = 37

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
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
    // run tests in CI builds instad of debug
    testBuildType = "ci"

    namespace = "io.github.fate_grand_automata.prefs"
}

dependencies {
    implementation(project(":scripts"))
    implementation(libs.androidx.core.ktx)

    implementation(libs.kotlinx.serialization.json)

    implementation(libs.dagger.hilt.android)
    ksp(libs.dagger.hilt.compiler)

    api(libs.fredporciuncula.flow.preferences)

    testImplementation(platform(libs.junit.bom)) {
        because("kotlin-test comes with conflicting junit versions")
    }
    testImplementation(libs.kotlin.test.junit5)
    testImplementation(libs.willowtreeapps.assertk)
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}