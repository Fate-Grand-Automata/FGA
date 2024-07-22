plugins {
    id("com.gradle.enterprise") version("3.17.6")
}
gradleEnterprise {
    buildScan {
        termsOfServiceUrl = "https://gradle.com/terms-of-service"
        termsOfServiceAgree = "yes"
    }
}
include(":prefs")
include(":scripts")
include(":libautomata")
include(":app")

rootProject.name="Fate Grand Automata"

