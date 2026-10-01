import java.net.URI

plugins {
    alias(libs.plugins.detekt)
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.shadow) apply false
}

detekt {
    // Each module's build.gradle.kts is intentionally left untouched; detekt is wired up once here, at the root,
    // and scans the whole checkout in a single pass, mirroring how `ktlintCheck`/`ktlintFormat` above already work.
    buildUponDefaultConfig = true
    config.setFrom(rootDir.resolve("config/detekt.yml"))
    // No `detekt-formatting` dependency is added on purpose: that submodule wraps ktlint itself, so adding it here
    // would just duplicate what `ktlintCheck` already enforces on this codebase.
    source.setFrom(
        fileTree(rootDir) {
            include("**/src/main/kotlin/**/*.kt", "**/src/test/kotlin/**/*.kt", "**/*.kts")
            exclude("**/build/**")
        },
    )
    // Grandfathers all violations that exist at the time detekt was introduced, so `detekt` only fails on issues in
    // newly added or modified code. Regenerate with `./gradlew detektBaseline` after intentionally cleaning up
    // pre-existing findings.
    baseline = rootDir.resolve("config/detekt-baseline.xml")
}

val ktlint: Configuration by configurations.creating

dependencies {
    ktlint(projects.ktlintCli)
}

tasks.register<JavaExec>("ktlintCheck") {
    group = LifecycleBasePlugin.VERIFICATION_GROUP
    description = "Check Kotlin code style"
    classpath = ktlint
    mainClass = "io.github.ktlint.core.Main"
    args(
        "**/src/**/*.kt",
        "**.kts",
        "!**/build/**",
        // Do not run with option "--log-level=debug" or "--log-level=trace" as the lint violations will be difficult
        // to spot between the amount of output lines.
    )
}

tasks.register<JavaExec>("ktlintFormat") {
    group = LifecycleBasePlugin.VERIFICATION_GROUP
    description = "Check Kotlin code style and format"
    classpath = ktlint
    mainClass = "io.github.ktlint.core.Main"
    // Suppress "sun.misc.Unsafe::objectFieldOffset" on Java24 (warning) (https://github.com/ktlint/ktlint/issues/2973)
    // jvmArgs("--sun-misc-unsafe-memory-access=allow") // Java 24+
    args(
        "-F",
        "**/src/**/*.kt",
        "**.kts",
        "!**/build/**",
        // Do not run with option "--log-level=debug" or "--log-level=trace" as the lint violations will be difficult
        // to spot between the amount of output lines.
    )
}

tasks.wrapper {
    distributionSha256Sum =
        URI
            .create("$distributionUrl.sha256")
            .toURL()
            .openStream()
            .use { it.reader().readText().trim() }
}
