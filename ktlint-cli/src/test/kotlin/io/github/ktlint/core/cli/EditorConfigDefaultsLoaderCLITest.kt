package io.github.ktlint.core.cli

import assertk.assertAll
import assertk.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.nio.file.Path

class EditorConfigDefaultsLoaderCLITest {
    @Test
    fun `When no default editorconfig is specified only the normal editorconfig file(s) on the file paths are used`(
        @TempDir
        tempDir: Path,
    ) {
        CommandLineTestRunner(tempDir)
            .run(
                testProjectName = "editorconfig-path",
                arguments = listOf("**/*.test"),
            ) {
                assertAll {
                    assertErrorExitCode()
                    assertThat(normalOutput)
                        .containsLineMatching(Regex(".*Foo.*Exceeded max line length \\(30\\).*"))
                        .containsLineMatching(Regex(".*Wildcard2.*Wildcard import.*"))
                        // The Bar files are not matched by any glob
                        .doesNotContainLineMatching(Regex(".*Bar.*Exceeded max line length"))
                        // The filename rule is disabled for the examples-directory only
                        .doesNotContainLineMatching(Regex(".*Wildcard1.*Wildcard import.*"))
                }
            }
    }

    @ParameterizedTest(name = "Alternative editorconfig file: {0}")
    @ValueSource(
        strings = [
            ".editorconfig-bar",
            "editorconfig-alternative",
            // Ensure that relative urls are allowed as well
            "../project/editorconfig-alternative",
        ],
    )
    fun `Given a default editorconfig path then use defaults when editorconfig files on the filepath do not resolve the property`(
        editorconfigPath: String,
        @TempDir
        tempDir: Path,
    ) {
        CommandLineTestRunner(tempDir)
            .run(
                testProjectName = "editorconfig-path",
                arguments =
                    listOf(
                        "**/*.test",
                        "--editorconfig=$tempDir/editorconfig-path/project/$editorconfigPath",
                    ),
            ) {
                assertAll {
                    assertErrorExitCode()
                    assertThat(normalOutput)
                        .containsLineMatching(Regex(".*FooTest.*Exceeded max line length \\(30\\).*"))
                        .containsLineMatching(Regex(".*Foo.*Exceeded max line length \\(30\\).*"))
                        // Only the Bar-files fall back on the default editorconfig!
                        .containsLineMatching(Regex(".*BarTest.*Exceeded max line length \\(20\\).*"))
                        .containsLineMatching(Regex(".*Bar.*Exceeded max line length \\(20\\).*"))
                }
            }
    }

    @Test
    fun `Given that the default editorconfig sets the default max line length for Test files only then use defaults when editorconfig files on the filepath do not resolve the property`(
        @TempDir
        tempDir: Path,
    ) {
        CommandLineTestRunner(tempDir)
            .run(
                testProjectName = "editorconfig-path",
                arguments =
                    listOf(
                        "**/*.test",
                        "--editorconfig=$tempDir/editorconfig-path/project/.editorconfig-default-max-line-length-on-tests-only",
                    ),
            ) {
                assertAll {
                    assertErrorExitCode()
                    assertThat(normalOutput)
                        .containsLineMatching(Regex(".*FooTest.*Exceeded max line length \\(30\\).*"))
                        .containsLineMatching(Regex(".*Foo.*Exceeded max line length \\(30\\).*"))
                        // Only the BarTest-file falls back on the default editorconfig!
                        .containsLineMatching(Regex(".*BarTest.*Exceeded max line length \\(25\\).*"))
                }
            }
    }

    @Test
    fun `Given that the default editorconfig disables no-wildcard-import rule for all example files`(
        @TempDir
        tempDir: Path,
    ) {
        CommandLineTestRunner(tempDir)
            .run(
                testProjectName = "editorconfig-path",
                arguments =
                    listOf(
                        "**/*.test",
                        "--editorconfig=$tempDir/editorconfig-path/project/.editorconfig-disable-no-wildcard-imports-rule",
                    ),
            ) {
                assertAll {
                    assertErrorExitCode()
                    assertThat(normalOutput)
                        .doesNotContainLineMatching(Regex(".*Wildcard1.*Wildcard import.*"))
                        .doesNotContainLineMatching(Regex(".*Wildcard2.*Wildcard import.*"))
                }
            }
    }

    @Test
    fun `Issue 1627 - Given a default editorconfig containing a boolean setting then do not throw a class cast exception`(
        @TempDir
        tempDir: Path,
    ) {
        CommandLineTestRunner(tempDir)
            .run(
                testProjectName = "editorconfig-path",
                arguments =
                    listOf(
                        "**/*.test",
                        "--editorconfig=$tempDir/editorconfig-path/project/editorconfig-boolean-setting",
                    ),
            ) {
                assertAll {
                    assertErrorExitCode()
                    assertThat(errorOutput).doesNotContainLineMatching(
                        Regex(".*java.lang.ClassCastException: java.lang.String cannot be cast to java.lang.Boolean.*"),
                    )
                }
            }
    }
}
