pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }

    includeBuild("build-logic")
}

dependencyResolutionManagement {
    repositoriesMode = RepositoriesMode.FAIL_ON_PROJECT_REPOS
    repositories {
        mavenCentral()
    }
}

plugins {
    id("com.gradle.develocity") version "4.4.2"
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

develocity {
    buildScan {
        termsOfUseUrl = "https://gradle.com/terms-of-service"
        termsOfUseAgree = "yes"
        // TODO: workaround for https://github.com/gradle/gradle/issues/22879.
        val isCI = providers.environmentVariable("CI").isPresent
        publishing.onlyIf { isCI }
    }
}

rootProject.name = "ktlint-root"

enableFeaturePreview("STABLE_CONFIGURATION_CACHE")
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

include(
    ":ktlint-api-consumer",
    ":ktlint-bom",
    ":ktlint-cli",
    ":ktlint-cli-reporter-baseline",
    ":ktlint-cli-reporter-checkstyle",
    ":ktlint-cli-reporter-core",
    ":ktlint-cli-reporter-format",
    ":ktlint-cli-reporter-json",
    ":ktlint-cli-reporter-sarif",
    ":ktlint-cli-reporter-html",
    ":ktlint-cli-reporter-plain",
    ":ktlint-cli-reporter-plain-summary",
    ":ktlint-cli-ruleset-core",
    ":ktlint-logger",
    ":ktlint-rule-engine",
    ":ktlint-rule-engine-core",
    ":ktlint-ruleset-standard",
    ":ktlint-ruleset-template",
    ":ktlint-test",
)
