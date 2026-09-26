package io.github.ktlint.core.rule.engine.internal

import assertk.all
import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.containsAtLeast
import assertk.assertions.doesNotContain
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.absolutePathString
import kotlin.io.path.writeText

class EditorConfigFinderTest {
    private lateinit var editorConfigFinder: EditorConfigFinder

    @BeforeEach
    fun setUp() {
        // Create a new EditorConfigFinder in each test to avoid that tests influence each other
        editorConfigFinder =
            EditorConfigFinder(
                EditorConfigLoaderEc4j(emptySet()),
            )
    }

    @Nested
    inner class `Given a kotlin file in a subdirectory ` {
        @Test
        fun `Given a root-editorconfig file in the same directory then get the path of that editorconfig file`(
            @TempDir
            tempDir: Path,
        ) {
            val someSubDir = "some-project/src/main/kotlin"
            tempDir.createFile("$someSubDir/.editorconfig", "root=true")
            val kotlinFilePath = tempDir.createFile("$someSubDir/test.kt", "val foo = 42")

            val actual = editorConfigFinder.findEditorConfigs(kotlinFilePath)

            assertThat(actual).contains(
                tempDir.plus("$someSubDir/.editorconfig"),
            )
        }

        @Test
        fun `Given a root-editorconfig file in a parent directory then get the path of that parent editorconfig file`(
            @TempDir
            tempDir: Path,
        ) {
            val someProjectDirectory = "some-project"
            tempDir.createFile("$someProjectDirectory/.editorconfig", "root=true")
            val kotlinFilePath = tempDir.createFile("$someProjectDirectory/src/main/kotlin/test.kt", "val foo = 42")

            val actual = editorConfigFinder.findEditorConfigs(kotlinFilePath)

            assertThat(actual).contains(
                tempDir.plus("$someProjectDirectory/.editorconfig"),
            )
        }

        @Test
        fun `Given a non-root-editorconfig file in that same directory and a root-editorconfig file in a parent directory then get the paths of both editorconfig files`(
            @TempDir
            tempDir: Path,
        ) {
            val someProjectDirectory = "some-project"
            tempDir.createFile("$someProjectDirectory/.editorconfig", "root=true")
            tempDir.createFile("$someProjectDirectory/src/main/.editorconfig", "root=false")
            val kotlinFilePath = tempDir.createFile("$someProjectDirectory/src/main/kotlin/test.kt", "val foo = 42")

            val actual = editorConfigFinder.findEditorConfigs(kotlinFilePath)

            assertThat(actual).containsAtLeast(
                tempDir.plus("$someProjectDirectory/.editorconfig"),
                tempDir.plus("$someProjectDirectory/src/main/.editorconfig"),
            )
        }

        @Test
        fun `Given a root-editorconfig file in the parent directory and another root-editorconfig file in a great-parent directory then get the paths of editorconfig files excluding root-editorconfig once the first one is found`(
            @TempDir
            tempDir: Path,
        ) {
            val someProjectDirectory = "some-project"
            tempDir.createFile("$someProjectDirectory/src/main/.editorconfig", "root=false")
            tempDir.createFile("$someProjectDirectory/src/.editorconfig", "root=true")
            tempDir.createFile("$someProjectDirectory/.editorconfig", "root=true")
            val kotlinFilePath = tempDir.createFile("$someProjectDirectory/src/main/kotlin/test.kt", "val foo = 42")

            val actual = editorConfigFinder.findEditorConfigs(kotlinFilePath)

            assertThat(actual).all {
                containsAtLeast(
                    tempDir.plus("$someProjectDirectory/src/main/.editorconfig"),
                    tempDir.plus("$someProjectDirectory/src/.editorconfig"),
                )
                doesNotContain(
                    tempDir.plus("$someProjectDirectory/.editorconfig"),
                )
            }
        }
    }

    @Nested
    inner class `Given a directory` {
        @Test
        fun `Given a root-editorconfig file and a subdirectory containing a editorconfig file then get the paths of both editorconfig files`(
            @TempDir
            tempDir: Path,
        ) {
            val someDirectory = "some-project"
            tempDir.createFile("$someDirectory/.editorconfig", "root=true")
            tempDir.createFile("$someDirectory/src/main/kotlin/.editorconfig", "some-property=some-value")

            val actual = editorConfigFinder.findEditorConfigs(tempDir.plus(someDirectory))

            assertThat(actual).containsAtLeast(
                tempDir.plus("$someDirectory/.editorconfig"),
                tempDir.plus("$someDirectory/src/main/kotlin/.editorconfig"),
            )
        }

        @Test
        fun `Given an editorconfig file and a sibling subdirectory contain a editorconfig file in a parent directory then get the path of all editorconfig file except of the sibling subdirectory`(
            @TempDir
            tempDir: Path,
        ) {
            val someProjectDirectory = "some-project"
            tempDir.createFile("$someProjectDirectory/.editorconfig", "root=true")
            tempDir.createFile("$someProjectDirectory/src/main/kotlin/.editorconfig", "some-property=some-value")
            tempDir.createFile("$someProjectDirectory/src/test/kotlin/.editorconfig", "some-property=some-value")

            val actual = editorConfigFinder.findEditorConfigs(tempDir.plus("$someProjectDirectory/src/main/kotlin"))

            assertThat(actual).all {
                containsAtLeast(
                    tempDir.plus("$someProjectDirectory/.editorconfig"),
                    tempDir.plus("$someProjectDirectory/src/main/kotlin/.editorconfig"),
                )
                doesNotContain(
                    tempDir.plus("$someProjectDirectory/src/test/kotlin/.editorconfig"),
                )
            }
        }

        @Test
        fun `Given an editorconfig file and multiple subdirectories containing a editorconfig file then get the path of all editorconfig files`(
            @TempDir
            tempDir: Path,
        ) {
            val someProjectDirectory = "some-project"
            tempDir.createFile("$someProjectDirectory/.editorconfig", "root=true")
            tempDir.createFile("$someProjectDirectory/src/main/kotlin/.editorconfig", "some-property=some-value")
            tempDir.createFile("$someProjectDirectory/src/test/kotlin/.editorconfig", "some-property=some-value")

            val actual = editorConfigFinder.findEditorConfigs(tempDir.plus(someProjectDirectory))

            assertThat(actual).containsAtLeast(
                tempDir.plus("$someProjectDirectory/.editorconfig"),
                tempDir.plus("$someProjectDirectory/src/main/kotlin/.editorconfig"),
                tempDir.plus("$someProjectDirectory/src/test/kotlin/.editorconfig"),
            )
        }
    }

    private fun Path.createFile(
        fileName: String,
        content: String,
    ): Path {
        val dirPath = fileName.substringBeforeLast("/", "")
        Files.createDirectories(this.plus(dirPath))
        return Files
            .createFile(this.plus(fileName))
            .also { it.writeText(content) }
    }

    private fun Path.plus(subPath: String): Path =
        this
            .absolutePathString()
            .plus(this.fileSystem.separator)
            .plus(subPath)
            .let { Paths.get(it) }
}
