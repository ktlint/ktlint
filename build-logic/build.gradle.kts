import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
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
        freeCompilerArgs.add("-Xcontext-parameters")
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
    implementation(libs.dokka)
    implementation(libs.poko)
    implementation(libs.gradle.maven.publish.plugin)
}
