package io.github.ktlint.core.cli

import assertk.assertAll
import assertk.assertThat
import assertk.assertions.contains
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.io.ByteArrayInputStream
import java.nio.file.Path

class SimpleCLITest {
    @Test
    fun `Given CLI argument --help then return the help output`(
        @TempDir
        tempDir: Path,
    ) {
        CommandLineTestRunner(tempDir)
            .run(
                "no-code-style-error",
                listOf("--help"),
                // The clikt command does not print the exit code as last logline.
                expectExitCodeLoggedWhenKtlintIsFinished = false,
            ) {
                assertAll {
                    assertNormalExitCode()
                    assertThat(normalOutput).containsLineMatching("An anti-bikeshedding Kotlin linter with built-in formatter.")
                    assertThat(normalOutput).containsLineMatching("Usage:")
                    assertThat(normalOutput).containsLineMatching("EXAMPLES")
                }
            }
    }

    @ParameterizedTest(name = "Options: {0}")
    @ValueSource(
        strings = [
            "-v",
            "--version",
        ],
    )
    fun `Given CLI argument --version then return the version information output`(
        version: String,
        @TempDir
        tempDir: Path,
    ) {
        CommandLineTestRunner(tempDir)
            .run(
                "no-code-style-error",
                listOf(version),
                expectExitCodeLoggedWhenKtlintIsFinished = false,
            ) {
                assertAll {
                    assertNormalExitCode()
                    assertThat(normalOutput).contains("ktlint version ${System.getProperty("ktlint-version")}")
                }
            }
    }

    @Test
    fun `Given some code without errors then return from lint with normal exit code and no error output`(
        @TempDir
        tempDir: Path,
    ) {
        CommandLineTestRunner(tempDir)
            .run(
                "no-code-style-error",
            ) {
                assertAll {
                    assertNormalExitCode()
                    assertErrorOutputIsEmpty()
                }
            }
    }

    @Test
    fun `Given some code with an error then return from lint with the error exit code and error output`(
        @TempDir
        tempDir: Path,
    ) {
        CommandLineTestRunner(tempDir)
            .run(
                "too-many-empty-lines",
                listOf("**/*.test"),
            ) {
                assertAll {
                    assertErrorExitCode()
                    assertThat(normalOutput).containsLineMatching("Needless blank line(s)")
                }
            }
    }

    @Test
    fun `Given some code with an error then return from lint with the error exit code and warning to use --format`(
        @TempDir
        tempDir: Path,
    ) {
        CommandLineTestRunner(tempDir)
            .run(
                "too-many-empty-lines",
                listOf("**/*.test"),
            ) {
                assertAll {
                    assertErrorExitCode()
                    assertThat(normalOutput)
                        .containsLineMatching(
                            Regex(".* WARN .* Lint has found errors than can be autocorrected using 'ktlint --format'"),
                        )
                }
            }
    }

    @Test
    fun `Given some code with an error but a glob which does not select the file`(
        @TempDir
        tempDir: Path,
    ) {
        val somePattern = "some-pattern"
        CommandLineTestRunner(tempDir)
            .run(
                "too-many-empty-lines",
                listOf(somePattern),
            ) {
                assertAll {
                    assertNormalExitCode()
                    assertThat(normalOutput).containsLineMatching("No files matched [$somePattern]")
                }
            }
    }

    @Test
    fun `Given some code in a file ending with a default kotlin extension then it will be picked up if no patterns are specified at the command line`(
        @TempDir
        tempDir: Path,
    ) {
        CommandLineTestRunner(tempDir)
            .run(
                "no-code-style-error",
                // No patterns specified at the command line. As of that it has to pick up all files having a default kotlin extension
                emptyList(),
            ) {
                assertAll {
                    assertNormalExitCode()
                    assertThat(normalOutput)
                        .containsLineMatching("Enable default patterns")
                        .containsLineMatching("1 file(s) scanned / 0 error(s)")
                }
            }
    }

    @Test
    fun `Given some code with an error which can be autocorrected then return with the normal exit code`(
        @TempDir
        tempDir: Path,
    ) {
        CommandLineTestRunner(tempDir)
            .run(
                "too-many-empty-lines",
                listOf("-F", "**/*.test"),
            ) {
                assertAll {
                    assertNormalExitCode()
                    // on JDK11+ contains warning about illegal reflective access operation
                    // assertErrorOutputIsEmpty()

                    assertSourceFileWasFormatted("Main.kt.test")
                }
            }
    }

    @ParameterizedTest(name = "Option: {0}")
    @ValueSource(
        strings = [
            "--patterns-from-stdin",
            "--patterns-from-stdin=",
        ],
    )
    fun `Given some code with an error and a pattern which is read in from stdin which does not select the file then return the no files matched warning`(
        patternsFromStdin: String,
        @TempDir
        tempDir: Path,
    ) {
        val pathToFile1 = "path/to/file/1.kt"
        val pathToFile2 = "path/to/file/2.kts"
        val somePatternProvidedViaStdin = "$pathToFile1\u0000$pathToFile2\u0000"
        CommandLineTestRunner(tempDir)
            .run(
                "too-many-empty-lines",
                listOf(patternsFromStdin),
                stdin = ByteArrayInputStream(somePatternProvidedViaStdin.toByteArray()),
            ) {
                assertAll {
                    assertNormalExitCode()
                    assertThat(normalOutput).containsLineMatching("No files matched [$pathToFile1, $pathToFile2]")
                }
            }
    }

    @Test
    fun `Issue 1793 - Given some code with an error and no patterns read in from stdin then return nothing `(
        @TempDir
        tempDir: Path,
    ) {
        CommandLineTestRunner(tempDir)
            .run(
                "too-many-empty-lines",
                listOf("--patterns-from-stdin"),
                stdin = ByteArrayInputStream(ByteArray(0)),
            ) {
                assertNormalExitCode()
                assertThat(normalOutput)
                    .doesNotContainLineMatching("Enable default patterns")
                    .containsLineMatching("No files matched []")
            }
    }

    @Test
    fun `Issue 1608 - --relative and --reporter=sarif should play well together`(
        @TempDir
        tempDir: Path,
    ) {
        CommandLineTestRunner(tempDir)
            .run(
                "too-many-empty-lines",
                listOf("**/*.test", "--relative", "--reporter=sarif"),
            ) {
                assertAll {
                    assertErrorExitCode()
                    assertThat(errorOutput).doesNotContainLineMatching(
                        "Exception in thread \"main\" java.lang.IllegalArgumentException: this and base files have different roots:",
                    )
                }
            }
    }

    @Test
    fun `Issue 2576 - single reporter`(
        @TempDir
        tempDir: Path,
    ) {
        CommandLineTestRunner(tempDir)
            .run(
                "too-many-empty-lines",
                listOf("**/*.test", "--reporter=plain"),
            ) {
                assertAll {
                    assertErrorExitCode()
                    assertThat(normalOutput)
                        .containsLineMatching("Main.kt.test:2:1: First line in a method block should not be empty")
                }
            }
    }

    @Test
    fun `Issue 2576 - multiple reporters`(
        @TempDir
        tempDir: Path,
    ) {
        CommandLineTestRunner(tempDir)
            .run(
                "too-many-empty-lines",
                listOf("**/*.test", "--reporter=plain", "--reporter=json,output=ktlint-violations.json"),
            ) {
                assertAll {
                    assertErrorExitCode()
                    assertThat(normalOutput)
                        .containsLineMatching("Initializing \"plain\" reporter")
                        .containsLineMatching(Regex(".*Initializing \"json\" reporter with .*, output=ktlint-violations.json"))
                        .containsLineMatching("Main.kt.test:2:1: First line in a method block should not be empty")
                        .containsLineMatching(Regex(".*ReporterAggregator -- \"json\" report written to .*ktlint-violations.json"))
                }
            }
    }

    @Test
    fun `Given a custom reporter which does not exist`(
        @TempDir
        tempDir: Path,
    ) {
        CommandLineTestRunner(tempDir)
            .run(
                "too-many-empty-lines",
                listOf("**/*.test", "--reporter=custom,artifact=custom-reporter.jar"),
            ) {
                assertAll {
                    assertErrorExitCode()
                    assertThat(normalOutput)
                        .containsLineMatching("File 'custom-reporter.jar' does not exist")
                }
            }
    }

    @Test
    fun `Generate git pre commit hook`(
        @TempDir
        tempDir: Path,
    ) {
        CommandLineTestRunner(tempDir)
            .run(
                "too-many-empty-lines",
                listOf("installGitPreCommitHook"),
            ) {
                assertAll {
                    // The command will throw an error because the testProjectName directory does not contain a
                    // '.git' directory. This is sufficient to know that the ktlint command was recognized.
                    assertErrorExitCode()
                    assertThat(errorOutput).containsLineMatching(
                        "git directory not found. Are you sure you are inside project directory?",
                    )
                }
            }
    }

    @Test
    fun `Generate git pre push hook`(
        @TempDir
        tempDir: Path,
    ) {
        CommandLineTestRunner(tempDir)
            .run(
                "too-many-empty-lines",
                listOf("installGitPrePushHook"),
            ) {
                assertAll {
                    // The command will throw an error because the testProjectName directory does not contain a
                    // '.git' directory. This is sufficient to know that the ktlint command was recognized.
                    assertErrorExitCode()
                    assertThat(errorOutput).containsLineMatching(
                        "git directory not found. Are you sure you are inside project directory?",
                    )
                }
            }
    }

    @Nested
    inner class `Generate 'editorconfig' file` {
        @Test
        fun `Given that the code-style is specified then exit with error`(
            @TempDir
            tempDir: Path,
        ) {
            CommandLineTestRunner(tempDir)
                .run(
                    "too-many-empty-lines",
                    listOf("generateEditorConfig", "--code-style ktlint_official"),
                    expectExitCodeLoggedWhenKtlintIsFinished = false,
                ) {
                    assertAll {
                        assertNormalExitCode()
                        assertThat(normalOutput).containsLineMatching("ktlint_code_style = ktlint_official")
                    }
                }
        }

        @Test
        fun `Given that the code-style is not specified then the command should fail`(
            @TempDir
            tempDir: Path,
        ) {
            CommandLineTestRunner(tempDir)
                .run(
                    "too-many-empty-lines",
                    listOf("generateEditorConfig"),
                    expectExitCodeLoggedWhenKtlintIsFinished = false,
                ) {
                    assertAll {
                        assertErrorExitCode()
                        assertThat(errorOutput).containsLineMatching("Error: missing option --code-style")
                    }
                }
        }
    }

    @Test
    fun `Issue 1742 - Disable the filename rule when --stdin is used`(
        @TempDir
        tempDir: Path,
    ) {
        CommandLineTestRunner(tempDir)
            .run(
                testProjectName = "too-many-empty-lines",
                arguments = listOf("--stdin"),
                stdin = ByteArrayInputStream("fun foo() = 42".toByteArray()),
            ) {
                assertThat(normalOutput).containsLineMatching(Regex(".*ktlint_standard_filename: disabled.*"))
            }
    }

    @Test
    fun `Issue 1123 - Enable filename rule when --stdin and --stdin-path is used`(
        @TempDir
        tempDir: Path,
    ) {
        CommandLineTestRunner(tempDir)
            .run(
                testProjectName = "too-many-empty-lines",
                arguments = listOf("--stdin", "--stdin-path", "/foo/Foo.kt"),
                stdin = ByteArrayInputStream("fun foo() = 42".toByteArray()),
            ) {
                assertThat(normalOutput).doesNotContainLineMatching(Regex(".*ktlint_standard_filename: disabled.*"))
            }
    }

    @Test
    fun `Issue 3296 - Merge editorconfig properties from subdirectory with root directory when relative stdin-path is used`(
        @TempDir
        tempDir: Path,
    ) {
        CommandLineTestRunner(tempDir)
            .run(
                testProjectName = "nested-editorconfig",
                arguments = listOf("--stdin", "--stdin-path", "foo/Foo.kt"),
                stdin = ByteArrayInputStream("fun foo() = 42".toByteArray()),
            ) {
                assertAll {
                    // Check properties overridden in "foo/.editorconfig"
                    assertThat(normalOutput).containsLineMatching(Regex(".*indent_size: 6.*"))
                    assertThat(normalOutput).containsLineMatching(Regex(".*ktlint_code_style: intellij_idea.*"))
                    // Check properties not overridden in "foo/.editorconfig" but defined in root ".editorconfig"
                    assertThat(normalOutput).containsLineMatching(Regex(".*end_of_line: crlf.*"))
                }
            }
    }

    @Test
    fun `Issue 1832 - Given stdin input containing Kotlin code resulting in a KtLintParseException when linted as Kotlin code then process the input as Kotlin Script`(
        @TempDir
        tempDir: Path,
    ) {
        CommandLineTestRunner(tempDir)
            .run(
                testProjectName = "too-many-empty-lines",
                arguments = listOf("--stdin"),
                stdin =
                    ByteArrayInputStream(
                        """
                        pluginManagement {
                            repositories {
                                mavenCentral()
                                gradlePluginPortal()
                            }


                            includeBuild("build-logic")
                        }
                        """.trimIndent().toByteArray(),
                    ),
            ) {
                assertThat(normalOutput).containsLineMatching("Can not parse input from <stdin> as Kotlin, due to error below:")
                assertThat(errorOutput).containsLineMatching("Needless blank line")
            }
    }

    @Test
    fun `Issue 1832 - Given stdin input containing valid Kotlin Script but results in a KtLintParseException when formatted as Kotlin code then process the input as Kotlin Script`(
        @TempDir
        tempDir: Path,
    ) {
        CommandLineTestRunner(tempDir)
            .run(
                testProjectName = "too-many-empty-lines",
                arguments = listOf("--stdin", "--format"),
                stdin =
                    ByteArrayInputStream(
                        """
                        pluginManagement {
                            repositories {
                                mavenCentral()
                                gradlePluginPortal()
                            }


                            includeBuild("build-logic")
                        }
                        """.trimIndent().toByteArray(),
                    ),
            ) {
                assertThat(normalOutput)
                    .containsLineMatching("Can not parse input from <stdin> as Kotlin, due to error below:")
                    .containsLineMatching("Now, trying to read the input as Kotlin Script.")
                    .doesNotContainLineMatching("Can not parse input from <stdin> as Kotlin script, due to error below:")
            }
    }

    @Test
    fun `Issue 2379 - Given stdin input resulting in a KtLintParseException when parsed as Kotlin code and Kotlin Script`(
        @TempDir
        tempDir: Path,
    ) {
        CommandLineTestRunner(tempDir)
            .run(
                testProjectName = "too-many-empty-lines",
                arguments = listOf("--stdin", "--format"),
                stdin =
                    ByteArrayInputStream(
                        """
                        fun foo() =
                        """.trimIndent().toByteArray(),
                    ),
            ) {
                assertThat(normalOutput)
                    .containsLineMatching("Can not parse input from <stdin> as Kotlin, due to error below:")
                    .containsLineMatching("Can not parse input from <stdin> as Kotlin script, due to error below:")
                    .containsLineMatching("Not a valid Kotlin file")
            }
    }

    @Test
    fun `Issue 3096 - Given some code containing unicode characters, the code is provided via stdin then write the formatted code to stdout in UTF-8 to preserve the unicode characters`(
        @TempDir
        tempDir: Path,
    ) {
        val code =
            """
            // Preserve § symbol when formatting the file with ktlint
            val sectionSign = { Char(167) }
            """.trimIndent()
        CommandLineTestRunner(tempDir)
            .run(
                testProjectName = "too-many-empty-lines",
                arguments = listOf("--stdin", "--format"),
                stdin = ByteArrayInputStream(code.toByteArray()),
            ) {
                assertThat(normalOutput.joinToString(separator = "\n")).contains(code)
            }
    }

    @Test
    fun `Issue 3278 - Given some code containing a string with a percent character, the code is provided via stdin then write the formatted code without interpreting the percent character as format instruction`(
        @TempDir
        tempDir: Path,
    ) {
        val code =
            """
            class Foo { val foo = "%"; }
            """.trimIndent()
        val formattedCode =
            """
            class Foo {
                val foo = "%"
            }
            """.trimIndent()
        CommandLineTestRunner(tempDir)
            .run(
                testProjectName = "too-many-empty-lines",
                arguments = listOf("--stdin", "--format"),
                stdin = ByteArrayInputStream(code.toByteArray()),
            ) {
                assertThat(normalOutput.joinToString(separator = "\n")).contains(formattedCode)
            }
    }

    @Nested
    inner class `Autocorrect violations` {
        @Test
        fun `Issue 3150 - Report violations that cannot be auto-corrected`(
            @TempDir
            tempDir: Path,
        ) {
            CommandLineTestRunner(tempDir)
                .run(
                    "ignore-autocorrect-failures",
                    arguments = listOf("--format"),
                ) {
                    assertAll {
                        assertErrorExitCode()
                        assertThat(normalOutput)
                            .containsLineMatching(Regex(".*\\(cannot be auto-corrected\\) \\(standard:filename\\).*"))
                    }
                }
        }

        @Test
        fun `Issue 3150 - Ignore violations that cannot be auto-corrected`(
            @TempDir
            tempDir: Path,
        ) {
            CommandLineTestRunner(tempDir)
                .run(
                    "ignore-autocorrect-failures",
                    arguments = listOf("--format", "--ignore-autocorrect-failures"),
                ) {
                    assertAll {
                        assertNormalExitCode()
                        assertThat(normalOutput)
                            .doesNotContainLineMatching(Regex(".*\\(cannot be auto-corrected\\) \\(standard:filename\\).*"))
                    }
                }
        }
    }

    @Nested
    inner class `Do not print kotlin-logging startup message to stdout` {
        @Test
        fun `Given a file containing a violation which can be autocorrected`(
            @TempDir
            tempDir: Path,
        ) {
            CommandLineTestRunner(tempDir)
                .run(
                    "too-many-empty-lines",
                    listOf("**/*.test", "--reporter=plain"),
                ) {
                    assertThat(normalOutput).doesNotContainLineMatching("kotlin-logging: initializing...")
                }
        }

        @Test
        fun `Given a command with option --stdin and which reformats the file`(
            @TempDir
            tempDir: Path,
        ) {
            CommandLineTestRunner(tempDir)
                .run(
                    testProjectName = "too-many-empty-lines",
                    arguments = listOf("--stdin"),
                    stdin = ByteArrayInputStream("   fun foo() = 42".toByteArray()),
                ) {
                    assertThat(normalOutput).doesNotContainLineMatching("kotlin-logging: initializing...")
                }
        }
    }
}
