plugins {
    id("java-library")
    id("kotlin")
    id("com.google.devtools.ksp")

    kotlin("plugin.serialization") version libs.versions.kotlin.version.get()
}

dependencies {
//    implementation fileTree(dir: "libs", include: ["*.jar"])
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    api(project(":libautomata"))

    implementation(libs.kotlin.stdlib)

    implementation(libs.dagger.hilt.core)
    ksp(libs.dagger.hilt.compiler)

    implementation(libs.kotlinx.datetime)
    implementation(libs.kotlinx.serialization)

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
