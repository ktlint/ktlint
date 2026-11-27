import org.gradle.api.DefaultTask
import org.gradle.api.model.ObjectFactory
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import javax.inject.Inject

// Implements https://github.com/brianm/really-executable-jars-maven-plugin maven plugin behavior.
// To check details how it works, see https://skife.org/java/unix/2011/06/20/really_executable_jars.html.
abstract class ShadowJarExecutableTask
    @Inject
    constructor(
        objects: ObjectFactory,
    ) : DefaultTask() {
        @InputFile
        val allJarFile = objects.fileProperty()

        @InputFile
        @PathSensitive(PathSensitivity.RELATIVE)
        val windowsBatchScriptSource = objects.fileProperty()

        @Internal
        val outputDirectory = objects.directoryProperty()

        @OutputFile
        val selfExecutable = outputDirectory.map { it.asFile.resolve("ktlint") }

        @OutputFile
        val windowsBatchScript = outputDirectory.map { it.asFile.resolve("ktlint.bat") }

        init {
            description = "Creates self-executable file, that runs generated shadow jar"
            group = "Distribution"
        }

        @TaskAction
        fun action() {
            logger.lifecycle("ktlint-cli: Base jar to build self-executable file: ${allJarFile.get()}")

            selfExecutable.get().apply {
                logger.lifecycle("Creating the self-executable file: $this")

                // writeText effective replaces the entire content if the file already exists. If appendText is used, the file keeps on growing
                // with each build if the clean target is not used.
                writeText(
                    $$"""
                    #!/bin/sh

                    # From this SO answer: https://stackoverflow.com/a/56243046

                    # First we get the major Java version as an integer, e.g. 17, 21, 25
                    JV=$(java -version 2>&1 | sed -E -n 's/.* version "([^.-]*).*".*/\1/p')

                    # Suppress warning "sun.misc.Unsafe::objectFieldOffset" on Java24+ (https://github.com/ktlint/ktlint/issues/2973)
                    X=$( [ "$JV" -ge "24" ] && echo "$X --sun-misc-unsafe-memory-access=allow" || echo "")

                    # Suppress warning "A restricted method in java.lang.System has been called" on Java 24
                    # Error is only printed when running command "ktlint --help"
                    X=$( [ "$JV" -ge "24" ] && echo "$X --enable-native-access=ALL-UNNAMED" || echo "")

                    exec java $X -Xmx512m -jar "$0" "$@"

                    """.trimIndent(),
                )
                // Add the jar
                appendBytes(allJarFile.get().asFile.readBytes())

                setExecutable(true, false)
            }
            logger.lifecycle("Creating the batch file for Windows OS: ${windowsBatchScript.get()}")
            windowsBatchScript.get().writeText(windowsBatchScriptSource.asFile.get().readText())
            logger.lifecycle("Finished creating output files ktlint-cli")
        }
    }
