plugins {
    id("java-library")
    id("kotlin")
    id("kotlin-kapt")
}

dependencies {
//    implementation fileTree(dir: "libs", include: ["*.jar"])
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    api(project(":libautomata"))

    implementation(libs.kotlin.stdlib)

    implementation(libs.dagger.hilt.core)
    kapt(libs.dagger.hilt.compiler)

    testImplementation(platform("org.junit:junit-bom:5.9.3")) {
        because("kotlin-test comes with conflicting junit versions")
    }
    testImplementation("org.jetbrains.kotlin:kotlin-test")
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
