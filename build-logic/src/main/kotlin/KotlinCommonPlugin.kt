import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
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
import org.jetbrains.kotlin.gradle.dsl.kotlinExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

@Suppress("unused") // See build.gradle.kts in build-logic
abstract class KotlinCommonPlugin : Plugin<Project> {
    override fun apply(target: Project): Unit =
        with(target) {
            pluginManager.apply("org.jetbrains.kotlin.jvm")
            pluginManager.apply("dev.drewhamilton.poko")

            val projectLibs = extensions.getByType<VersionCatalogsExtension>().named("libs")
            val javaCompilationVersion =
                JavaLanguageVersion.of(projectLibs.findVersion("java-compilation").get().requiredVersion)
            val javaTargetVersion = JavaLanguageVersion.of(projectLibs.findVersion("java-target").get().requiredVersion)

            (kotlinExtension as KotlinJvmExtension).apply {
                // All modules, the CLI included, must have an explicit API
                explicitApi()
                jvmToolchain(jdkVersion = javaCompilationVersion.asInt())

                compilerOptions {
                    apiVersion.set(KotlinVersion.KOTLIN_2_3)
                    languageVersion.set(KotlinVersion.KOTLIN_2_3)
                }
            }

            tasks.withType<JavaCompile>().configureEach {
                options.release.set(javaTargetVersion.asInt())
            }
            tasks.withType<KotlinCompile>().configureEach {
                // Convert Java version (e.g. "1.8" or "11") to Kotlin JvmTarget ("8" resp. "11")
                compilerOptions.jvmTarget.set(JvmTarget.fromTarget(JavaVersion.toVersion(javaTargetVersion).toString()))
            }

            val requestedJdkVersion = project.findProperty("testJdkVersion")?.toString()?.toInt()
            // list of Java versions (usually only LTS versions) the developers may want to run via IDE click.
            setOfNotNull(17, 21, requestedJdkVersion).forEach { version ->
                tasks.register<Test>("testOnJdk$version") {
                    javaLauncher.set(
                        target
                            .the<JavaToolchainService>()
                            .launcherFor { languageVersion.set(JavaLanguageVersion.of(version)) },
                    )

                    description = "Runs the test suite on JDK $version"
                    group = LifecycleBasePlugin.VERIFICATION_GROUP

                    // Copy inputs from normal Test task.
                    val testTask = tasks.named<Test>("test").get()
                    classpath = testTask.classpath
                    testClassesDirs = testTask.testClassesDirs
                }
            }

            val skipTests: String = providers.systemProperty("skipTests").getOrElse("false")
            tasks.withType<Test>().configureEach {
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
                        .canCompileOrRun(JavaLanguageVersion.of(24))
                ) {
                    // Suppress warning "sun.misc.Unsafe::objectFieldOffset" on Java24+ (https://github.com/pinterest/ktlint/issues/2973)
                    jvmArgs("--sun-misc-unsafe-memory-access=allow") // Java 24+
                }
            }
        }
}
