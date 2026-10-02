plugins {
    id("ktlint-publication-library")
    alias(libs.plugins.shadow)
}

tasks.jar {
    manifest {
        attributes("Main-Class" to "io.github.ktlint.core.Main")
        attributes("Implementation-Version" to version)
    }
}

tasks.shadowJar {
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
    failOnDuplicateEntries = true
    @Suppress("DEPRECATION") // This flag will be disabled and removed in the next major version of Shadow.
    enableKotlinModuleRemapping = false
    mergeServiceFiles()
    // Exclude all duplicate files except service files, as they should be merged by `mergeServiceFiles`.
    filesNotMatching("META-INF/services/**") {
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    }
    // kotlin-compiler-embeddable ships jline native-image properties that reference
    // reflection-config.json / resource-config.json files that aren't bundled — drop them
    // so GraalVM native-image doesn't fail on missing resources.
    exclude("META-INF/native-image/org.jline/**")
}

dependencies {
    implementation(projects.ktlintLogger)
    implementation(projects.ktlintCliReporterBaseline)
    implementation(projects.ktlintCliReporterCore)
    implementation(projects.ktlintCliReporterPlain)
    implementation(projects.ktlintComPinterestBackwardCompatibility)
    implementation(projects.ktlintRuleEngine)
    implementation(projects.ktlintRulesetStandard)
    implementation(libs.clikt)
    implementation(libs.logback)

    runtimeOnly(projects.ktlintCliReporterCheckstyle)
    runtimeOnly(projects.ktlintCliReporterJson)
    runtimeOnly(projects.ktlintCliReporterFormat)
    runtimeOnly(projects.ktlintCliReporterHtml)
    runtimeOnly(projects.ktlintCliReporterPlainSummary)
    runtimeOnly(projects.ktlintCliReporterSarif)

    testImplementation(projects.ktlintTest)

    testImplementation(libs.junit5.jupiter)
    // Since Gradle 8 the platform launcher needs explicitly be defined as runtime dependency to avoid classpath problems
    // https://docs.gradle.org/8.12/userguide/upgrading_version_8.html#test_framework_implementation_dependencies
    testRuntimeOnly(libs.junit5.platform.launcher)
}

// Directory for files to be distributed as Ktlint CLI
val ktlintCliOutputRoot = layout.buildDirectory.dir("run")

val ktlintCliFiles =
    tasks.register<KtlintCliTask>("ktlintCliFiles") {
        ktlintCliJarFile.set(tasks.shadowJar.get().archiveFile)
        ktlintCliWindowsBatchScriptSource.set(layout.projectDirectory.file("src/main/scripts/ktlint.bat"))
        ktlintCliOutputDirectory.set(ktlintCliOutputRoot)

        if (pluginManager.hasPlugin("signing")) {
            finalizedBy("signKtlintCliFiles")
        }
    }

val signKtlintCliFiles =
    tasks.register<Sign>("signKtlintCliFiles") {
        dependsOn(ktlintCliFiles)

        sign(ktlintCliFiles.flatMap { it.ktlintCliExecutable }.get())
    }

tasks.withType<Test>().configureEach {
    dependsOn(ktlintCliFiles)

    // TODO: Use providers directly after https://github.com/gradle/gradle/issues/12247 is fixed.
    val ktlintCliExecutableFilePath = ktlintCliFiles.flatMap { it.ktlintCliExecutable }.map { it.absolutePath }.get()
    val ktlintVersion = providers.provider { version }.get()
    doFirst {
        systemProperty(
            "ktlint-cli",
            ktlintCliExecutableFilePath,
        )
        systemProperty("ktlint-version", ktlintVersion)
    }
}
