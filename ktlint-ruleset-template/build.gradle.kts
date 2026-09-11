// This module serves as a sample project for development of a custom ruleset. To avoid any confusion, this build setup is not reusing the
// build logic of other internal ktlint modules (https://github.com/ktlint/ktlint/issues/3048)..

plugins {
    kotlin("jvm") version "2.4.20"
    // Remove the line below when this custom ruleset is not to be published to maven. If you do want to publish your ruleset to Maven, you
    // still might need to configure the Maven Central repository in file `settings.gradle.xml` which is not included in the sample project
    // as it conflicts with the build of the Ktlint itself. Suggested content of that file:
    //     dependencyResolutionManagement {
    //          repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    //          repositories {
    //               mavenCentral()
    //          }
    //     }
    `maven-publish`
}

// Change the group name to your liking
group = "com.github.username.ktlint.ruleset"
version = "1.0-SNAPSHOT"

// repositories {
//    mavenCentral()
// }

// Remove when the Gradle task 'ktlintCheck' is not to be added to the project
val ktlint: Configuration by configurations.creating

// During development in the ktlint repository, substitute Maven coordinates with local projects.
// External users who copy this template should remove this block and ensure mavenCentral() is in their repositories.
configurations.all {
    resolutionStrategy.dependencySubstitution {
        substitute(module("io.github.ktlint.core:ktlint-cli")).using(project(":ktlint-cli"))
        substitute(module("io.github.ktlint.core:ktlint-cli-ruleset-core")).using(project(":ktlint-cli-ruleset-core"))
        substitute(module("io.github.ktlint.core:ktlint-rule-engine-core")).using(project(":ktlint-rule-engine-core"))
        substitute(module("io.github.ktlint.core:ktlint-test")).using(project(":ktlint-test"))
    }
}

// Update the version numbers of dependencies below to the most recent stable versions
dependencies {
    // Remove when the Gradle task 'ktlintCheck' is not to be added to the project
    ktlint("io.github.ktlint.core:ktlint-cli:2.0.0-SNAPSHOT")

    implementation("io.github.ktlint.core:ktlint-cli-ruleset-core:2.0.0-SNAPSHOT")
    implementation("io.github.ktlint.core:ktlint-rule-engine-core:2.0.0-SNAPSHOT")

    testImplementation("org.junit.jupiter:junit-jupiter:6.1.3")
    // Since Gradle 8 the platform launcher needs explicitly be defined as runtime dependency to avoid classpath problems
    // https://docs.gradle.org/8.12/userguide/upgrading_version_8.html#test_framework_implementation_dependencies
    testImplementation("org.junit.platform:junit-platform-launcher:6.1.3")
    testImplementation("org.slf4j:slf4j-simple:2.0.19")
    testImplementation("io.github.ktlint.core:ktlint-test:2.0.0-SNAPSHOT")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    // Keep the Java version of the ktlint-ruleset-template in sync with Java version supported by Gradle + Kotlin. See
    // https://docs.gradle.org/current/userguide/compatibility.html. Contrary to the rest of the ktlint build, this version cannot always be
    // run on latest Java version.
    // Upgrade to Java 25 is only possible after upgrade to Kotlin 2.3.0
    jvmToolchain(21)
}

// Remove when this custom ruleset is not to be published to maven
java {
    withSourcesJar()
    withJavadocJar()
}

// Remove when this custom ruleset is not to be published to maven
publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            pom {
                licenses {
                    license {
                        name = "The Apache Software License, Version 2.0"
                        url = "https://www.apache.org/licenses/LICENSE-2.0.txt"
                        distribution = "repo"
                    }
                }
            }
        }
    }
}

// Remove when the Gradle task 'ktlintCheck' is not to be added to the project
val ktlintCheck by tasks.registering(JavaExec::class) {
    dependsOn(tasks.classes)
    group = LifecycleBasePlugin.VERIFICATION_GROUP
    mainClass = "io.github.ktlint.core.Main"
    // Adding compiled classes of this ruleset to the classpath so that ktlint validates the ruleset using its own ruleset
    classpath(ktlint, sourceSets.main.map { it.output })
    args("--log-level=debug", "src/**/*.kt")
}

// Remove when the Gradle task 'ktlintCheck' is not to be added to the project
tasks.check {
    dependsOn(ktlintCheck)
}
