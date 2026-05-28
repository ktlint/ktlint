import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

plugins {
    `java-gradle-plugin`
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.sam.with.receiver)
}

repositories {
    mavenCentral()
}

samWithReceiver {
    annotation("org.gradle.api.HasImplicitReceiver")
}

kotlin {
    jvmToolchain(
        libs
            .versions
            .java
            .compilation
            .get()
            .toInt(),
    )

    compilerOptions {
        // Match language version enforced by current Gradle version https://docs.gradle.org/current/userguide/compatibility.html#kotlin
        apiVersion.set(KotlinVersion.KOTLIN_2_3)
    }
}

tasks.withType<KotlinCompile>().configureEach {
    compilerOptions {
        freeCompilerArgs.add("-Xsuppress-version-warnings") // ignores deprecated kotlin language version
    }
}

// Override java target for build-logic, to the latest version supported by gradle-kotlin-dsl (https://plugins.gradle.org/plugin/org.gradle.kotlin.kotlin-dsl)
// plugin. Note that this plugin usually lags behind with the latest Java versions.
val buildLogicJavaTarget =
    JvmTarget
        .entries
        .filterNot { it == JvmTarget.JVM_1_8 }
        .maxBy { it.target.toInt() }
        .also {
            logger.lifecycle("Set build logic Java Target: $it")
        }
tasks.withType<KotlinJvmCompile>().configureEach {
    compilerOptions.jvmTarget.set(buildLogicJavaTarget)
}
tasks.withType<JavaCompile>().configureEach {
    options.release.set(buildLogicJavaTarget.target.toInt())
}

gradlePlugin {
    plugins {
        register("ktlint-dokka") {
            id = "ktlint-dokka"
            implementationClass = "DokkaPlugin"
        }
        register("ktlint-kotlin-common") {
            id = "ktlint-kotlin-common"
            implementationClass = "KotlinCommonPlugin"
        }
        register("ktlint-publication") {
            id = "ktlint-publication"
            implementationClass = "PublicationPlugin"
        }
        register("ktlint-publication-library") {
            id = "ktlint-publication-library"
            implementationClass = "PublicationLibraryPlugin"
        }
    }
}

dependencies {
    val kotlinPlugin =
        if (hasProperty("kotlinDev")) {
            // Pass '-PkotlinDev' to command line to enable kotlin-in-development version
            logger.warn("Enabling kotlin dev version!")
            libs.kotlin.plugin.dev
        } else {
            libs.kotlin.plugin.asProvider()
        }
    implementation(kotlinPlugin)
    implementation(gradleKotlinDsl())
    implementation(libs.dokka)
    implementation(libs.poko)
    implementation(libs.gradle.maven.publish.plugin)
}
