package io.github.ktlint.core.cli.api

import assertk.assertAll
import assertk.assertThat
import io.github.ktlint.core.cli.CommandLineTestRunner
import io.github.ktlint.core.cli.containsLineMatching
import io.github.ktlint.core.cli.doesNotContainLineMatching
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path

class BaselineCLITest {
    @Test
    fun `Given files containing lint errors then find those errors when the baseline is ignored`(
        @TempDir
        tempDir: Path,
    ) {
        val projectName = "baseline"
        CommandLineTestRunner(tempDir)
            .run(
                projectName,
                listOf(
                    "TestBaselineFile.kt.test",
                    "some/path/to/TestBaselineFile.kt.test",
                ),
            ) {
                assertAll {
                    assertErrorExitCode()
                    assertThat(normalOutput)
                        .containsLineMatching(Regex(".*/$projectName/TestBaselineFile.kt.test:1:24: Unnecessary block.*"))
                        .containsLineMatching(Regex(".*/$projectName/some/path/to/TestBaselineFile.kt.test:1:24: Unnecessary block.*"))
                }
            }
    }

    @Nested
    inner class `Given files containing lint errors which are all registered in the baseline file and as of that are all ignored` {
        @Test
        fun `Given a baseline file in the root of the working directory`(
            @TempDir
            tempDir: Path,
        ) {
            val projectName = "baseline"
            val baselinePath = "test-baseline.xml"

            CommandLineTestRunner(tempDir)
                .run(
                    projectName,
                    listOf(
                        "--baseline=$baselinePath",
                        "--format",
                        "TestBaselineFile.kt.test",
                        "some/path/to/TestBaselineFile.kt.test",
                    ),
                ) {
                    assertAll {
                        assertNormalExitCode()
                        assertThat(normalOutput)
                            .doesNotContainLineMatching(Regex(".*/$projectName/TestBaselineFile.kt.test:1:24: Unnecessary block.*"))
                            .doesNotContainLineMatching(
                                Regex(".*/$projectName/some/path/to/TestBaselineFile.kt.test:1:24: Unnecessary block.*"),
                            ).doesNotContainLineMatching(
                                Regex(
                                    ".*Format was not able to resolve all violations which \\(theoretically\\) can be autocorrected in file.*",
                                ),
                            )
                    }
                }
        }

        @Test
        fun `Given a baseline file in a subdirectory of the working directory`(
            @TempDir
            tempDir: Path,
        ) {
            val projectName = "baseline"
            val baselinePath = "config/test-baseline.xml"

            CommandLineTestRunner(tempDir)
                .run(
                    projectName,
                    listOf(
                        "--baseline=$baselinePath",
                        "TestBaselineFile.kt.test",
                        "some/path/to/TestBaselineFile.kt.test",
                    ),
                ) {
                    assertAll {
                        assertNormalExitCode()
                        assertThat(normalOutput)
                            .doesNotContainLineMatching(Regex(".*/$projectName/TestBaselineFile.kt.test:1:24: Unnecessary block.*"))
                            .doesNotContainLineMatching(
                                Regex(".*/$projectName/some/path/to/TestBaselineFile.kt.test:1:24: Unnecessary block.*"),
                            )
                    }
                }
        }

        @Test
        fun `Given a baseline file with an absolute path, not necessarily in the working directory or one of its subdirectories`(
            @TempDir
            tempDir: Path,
        ) {
            val baselinePath = "$tempDir/baseline/config/test-baseline.xml"

            CommandLineTestRunner(tempDir)
                .run(
                    "baseline",
                    listOf(
                        "--baseline=$baselinePath",
                        "TestBaselineFile.kt.test",
                        "some/path/to/TestBaselineFile.kt.test",
                    ),
                ) {
                    assertAll {
                        assertNormalExitCode()
                        assertThat(normalOutput)
                            .doesNotContainLineMatching(Regex("^TestBaselineFile.kt.test:1:24: Unnecessary block.*"))
                            .doesNotContainLineMatching(Regex("^some/path/to/TestBaselineFile.kt.test:1:24: Unnecessary block.*"))
                    }
                }
        }
    }

    @Test
    fun `Given files containing lint errors which are not registered in the baseline file and as of that are all reported`(
        @TempDir
        tempDir: Path,
    ) {
        val projectName = "baseline"
        val baselinePath = "test-baseline.xml"

        CommandLineTestRunner(tempDir)
            .run(
                projectName,
                listOf(
                    "--baseline=$baselinePath",
                    "TestBaselineExtraErrorFile.kt.test",
                    "some/path/to/TestBaselineExtraErrorFile.kt.test",
                ),
            ) {
                assertAll {
                    assertErrorExitCode()
                    assertThat(normalOutput)
                        .containsLineMatching(
                            Regex(
                                ".*/$projectName/TestBaselineExtraErrorFile.kt.test:1:1: Replace the block comment with an " +
                                    "EOL comment.*",
                            ),
                        ).containsLineMatching(
                            Regex(
                                ".*/$projectName/some/path/to/TestBaselineExtraErrorFile.kt.test:1:1: Replace the block comment with " +
                                    "an EOL comment.*",
                            ),
                        )
                }
            }
    }
}
