import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.testing.Test
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.jvm.toolchain.JavaToolchainService
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.the
import org.gradle.kotlin.dsl.withType
import org.gradle.language.base.plugins.LifecycleBasePlugin
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion
import org.jetbrains.kotlin.gradle.dsl.abi.ExperimentalAbiValidation
import org.jetbrains.kotlin.gradle.dsl.kotlinExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

abstract class KotlinCommonPlugin : Plugin<Project> {
    override fun apply(target: Project): Unit =
        with(target) {
            pluginManager.apply("org.jetbrains.kotlin.jvm")
            pluginManager.apply("dev.drewhamilton.poko")

            group = localGradleProperty("POM_GROUP_ID").get()

            configureJavaCompilationAndTargetVersion()
            registerTestTask()
            configureTestTask()
        }

    context(project: Project)
    private fun configureJavaCompilationAndTargetVersion() {
        val projectLibs = project.extensions.getByType<VersionCatalogsExtension>().named("libs")
        val javaCompilationVersion = JavaLanguageVersion.of(projectLibs.findVersion("java-compilation").get().requiredVersion)
        val javaTargetVersion = JavaLanguageVersion.of(projectLibs.findVersion("java-target").get().requiredVersion)

        (project.kotlinExtension as KotlinJvmExtension).apply {
            // All modules, the CLI included, must have an explicit API
            explicitApi()
            if (project.path !in internalNonPublishableProjects) {
                @OptIn(ExperimentalAbiValidation::class)
                abiValidation()
            }
            jvmToolchain(jdkVersion = javaCompilationVersion.asInt())

            compilerOptions {
                apiVersion.set(KotlinVersion.KOTLIN_2_2)
                languageVersion.set(KotlinVersion.KOTLIN_2_2)
            }
        }

        project.tasks.withType<JavaCompile>().configureEach {
            options.release.set(javaTargetVersion.asInt())
        }
        project.tasks.withType<KotlinCompile>().configureEach {
            // Convert Java version (e.g. "1.8" or "11") to Kotlin JvmTarget ("8" resp. "11")
            compilerOptions.jvmTarget.set(JvmTarget.fromTarget(JavaVersion.toVersion(javaTargetVersion).toString()))
        }
    }

    context(project: Project)
    private fun registerTestTask() {
        val requestedJdkVersion =
            project.project
                .findProperty("testJdkVersion")
                ?.toString()
                ?.toInt()
        // list of Java versions (usually only LTS versions) the developers may want to run via IDE click.
        setOfNotNull(LTS_JAVA_VERSION_17, LTS_JAVA_VERSION_21, requestedJdkVersion).forEach { version ->
            project.tasks.register<Test>("testOnJdk$version") {
                javaLauncher.set(
                    project
                        .the<JavaToolchainService>()
                        .launcherFor { languageVersion.set(JavaLanguageVersion.of(version)) },
                )

                description = "Runs the test suite on JDK $version"
                group = LifecycleBasePlugin.VERIFICATION_GROUP

                // Copy inputs from normal Test task.
                val testTask = project.tasks.named<Test>("test").get()
                classpath = testTask.classpath
                testClassesDirs = testTask.testClassesDirs
            }
        }
    }

    context(project: Project)
    private fun configureTestTask() {
        val skipTests: String = project.providers.systemProperty("skipTests").getOrElse("false")
        project.tasks.withType<Test>().configureEach {
            if (skipTests == "false") {
                useJUnitPlatform()
            } else {
                logger.warn("Skipping tests for task '$name' as system property 'skipTests=$skipTests'")
            }

            maxParallelForks =
                if (System.getenv("CI") != null) {
                    Runtime.getRuntime().availableProcessors()
                } else {
                    // https://docs.gradle.org/8.0/userguide/performance.html#execute_tests_in_parallel
                    (Runtime.getRuntime().availableProcessors() / 2).takeIf { it > 0 } ?: 1
                }

            if (javaLauncher
                    .get()
                    .metadata
                    .languageVersion
                    .canCompileOrRun(JavaLanguageVersion.of(JAVA_VERSION_UNSAFE_MEMORY_ACCESS_WARNING))
            ) {
                // Suppress warning "sun.misc.Unsafe::objectFieldOffset" on Java24+ (https://github.com/ktlint/ktlint/issues/2973)
                jvmArgs("--sun-misc-unsafe-memory-access=allow") // Java 24+
            }
        }
    }

    // TODO: remove this once https://github.com/gradle/gradle/issues/23572 is fixed
    fun Project.localGradleProperty(name: String): Provider<String> =
        provider {
            if (hasProperty(name)) property(name)?.toString() else null
        }

    private companion object {
        private const val LTS_JAVA_VERSION_17 = 17
        private const val LTS_JAVA_VERSION_21 = 21
        private const val JAVA_VERSION_UNSAFE_MEMORY_ACCESS_WARNING = 24
        val internalNonPublishableProjects =
            setOf(
                ":ktlint-api-consumer",
                ":ktlint-bom",
                ":ktlint-ruleset-template",
            )
    }
}
