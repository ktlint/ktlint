plugins {
    id("ktlint-publication-library")
}

dependencies {
    implementation(projects.ktlintLogger)
    implementation(libs.kotlin.analysisApiImplementation)
    implementation(libs.kotlin.analysisApiIntellijImplementationComponents)

    api(libs.ec4j)
    api(libs.kotlin.analysisApiIntellijApiSurfaceComponents)
    api(libs.kotlin.analysisApiSurface)

    testImplementation(projects.ktlintTest)
    testImplementation(projects.ktlintRuleEngine)
    testRuntimeOnly(libs.slf4j)

    testImplementation(libs.junit5.jupiter)
    // Since Gradle 8 the platform launcher needs explicitly be defined as runtime dependency to avoid classpath problems
    // https://docs.gradle.org/8.12/userguide/upgrading_version_8.html#test_framework_implementation_dependencies
    testRuntimeOnly(libs.junit5.platform.launcher)
}
