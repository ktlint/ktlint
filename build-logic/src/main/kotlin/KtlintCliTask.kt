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
abstract class KtlintCliTask
    @Inject
    constructor(
        objects: ObjectFactory,
    ) : DefaultTask() {
        @InputFile
        val ktlintCliJarFile = objects.fileProperty()

        @InputFile
        @PathSensitive(PathSensitivity.RELATIVE)
        val ktlintCliWindowsBatchScriptSource = objects.fileProperty()

        @Internal
        val ktlintCliOutputDirectory = objects.directoryProperty()

        @OutputFile
        val ktlintCliExecutable = ktlintCliOutputDirectory.map { it.asFile.resolve("ktlint") }

        @OutputFile
        val ktlintCliWindowsBatchScript = ktlintCliOutputDirectory.map { it.asFile.resolve("ktlint.bat") }

        init {
            description = "Creates Ktlint CLI files"
            group = "Distribution"
        }

        @TaskAction
        fun action() {
            logger.lifecycle("Start creating Ktlint CLI files")

            ktlintCliExecutable.get().apply {
                logger.lifecycle("ktlint-cli: build self-executable jar file: $this")

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
                appendBytes(ktlintCliJarFile.get().asFile.readBytes())

                setExecutable(true, false)
            }
            logger.lifecycle("Creating the batch file for Windows OS: ${ktlintCliWindowsBatchScript.get()}")
            ktlintCliWindowsBatchScript.get().writeText(ktlintCliWindowsBatchScriptSource.asFile.get().readText())
            logger.lifecycle("Finished creating Ktlint CLI files")
        }
    }
