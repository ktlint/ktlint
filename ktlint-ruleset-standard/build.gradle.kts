plugins {
    id("ktlint-publication-library")
}

dependencies {
    implementation(projects.ktlintLogger)
    implementation(libs.kotlin.analysisApiImplementation)

    api(projects.ktlintCliRulesetCore)
    api(projects.ktlintRuleEngineCore)

    testImplementation(projects.ktlintTest)
    testImplementation(libs.kotlin.reflect)
    testRuntimeOnly(libs.logback)

    testImplementation(libs.junit5.jupiter)
    // Since Gradle 8 the platform launcher needs explicitly be defined as runtime dependency to avoid classpath problems
    // https://docs.gradle.org/8.12/userguide/upgrading_version_8.html#test_framework_implementation_dependencies
    testRuntimeOnly(libs.junit5.platform.launcher)
}
