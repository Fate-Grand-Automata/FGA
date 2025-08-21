plugins {
    id("java-library")
    id("kotlin")
    id("com.google.devtools.ksp")
}

dependencies {
//    implementation fileTree(dir: "libs", include: ["*.jar"])
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))

    implementation(libs.dagger.hilt.core)
    ksp(libs.dagger.hilt.compiler)

    api(libs.kotlinx.coroutines.core)
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}
