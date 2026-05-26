// This module serves as a sample project for development of a custom ruleset. To avoid any confusion, this build setup is not reusing the
// build logic of other internal ktlint modules (https://github.com/pinterest/ktlint/issues/3048)..

plugins {
    kotlin("jvm") version "2.3.20"
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

// Update the version numbers of dependencies below to the most recent stable versions
dependencies {
    // Remove when the Gradle task 'ktlintCheck' is not to be added to the project
    ktlint("com.pinterest.ktlint:ktlint-cli:1.8.0")

    implementation("com.pinterest.ktlint:ktlint-cli-ruleset-core:1.8.0")
    implementation("com.pinterest.ktlint:ktlint-rule-engine-core:1.8.0")

    testImplementation("org.junit.jupiter:junit-jupiter:6.0.3")
    // Since Gradle 8 the platform launcher needs explicitly be defined as runtime dependency to avoid classpath problems
    // https://docs.gradle.org/8.12/userguide/upgrading_version_8.html#test_framework_implementation_dependencies
    testImplementation("org.junit.platform:junit-platform-launcher:6.0.3")
    testImplementation("org.slf4j:slf4j-simple:2.0.17")
    testImplementation("com.pinterest.ktlint:ktlint-test:1.8.0")
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
    mainClass = "com.pinterest.ktlint.Main"
    // Adding compiled classes of this ruleset to the classpath so that ktlint validates the ruleset using its own ruleset
    classpath(ktlint, sourceSets.main.map { it.output })
    args("--log-level=debug", "src/**/*.kt")
}

// Remove when the Gradle task 'ktlintCheck' is not to be added to the project
tasks.check {
    dependsOn(ktlintCheck)
}
