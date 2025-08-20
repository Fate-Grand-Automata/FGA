plugins {
    id("java-library")
    id("kotlin")
    alias(libs.plugins.ksp)
}

dependencies {
//    implementation fileTree(dir: "libs", include: ["*.jar"])
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))

    implementation(libs.dagger.hilt.core)
    ksp(libs.dagger.hilt.compiler)

    api(libs.kotlinx.coroutines.core)
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}