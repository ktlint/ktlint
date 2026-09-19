package io.github.ktlint.core.cli.reporter.json

import assertk.assertThat
import assertk.assertions.isEqualTo
import io.github.ktlint.core.cli.reporter.core.api.KtlintCliError
import io.github.ktlint.core.cli.reporter.core.api.KtlintCliError.Status.FORMAT_IS_AUTOCORRECTED
import io.github.ktlint.core.cli.reporter.core.api.KtlintCliError.Status.LINT_CAN_BE_AUTOCORRECTED
import org.junit.jupiter.api.Test
import java.io.ByteArrayOutputStream
import java.io.PrintStream

class JsonReporterTest {
    @Test
    fun testReportGeneration() {
        val out = ByteArrayOutputStream()
        val reporter = JsonReporter(PrintStream(out, true))
        reporter.onLintError(
            "/one-fixed-and-one-not.kt",
            @Suppress("ktlint:standard:argument-list-wrapping")
            KtlintCliError(1, 1, "rule-1", "<\"&'>", LINT_CAN_BE_AUTOCORRECTED),
        )
        reporter.onLintError(
            "/one-fixed-and-one-not.kt",
            @Suppress("ktlint:standard:argument-list-wrapping")
            KtlintCliError(2, 1, "rule-2", "And if you see my friend", FORMAT_IS_AUTOCORRECTED),
        )

        reporter.onLintError(
            "/two-not-fixed.kt",
            @Suppress("ktlint:standard:argument-list-wrapping")
            KtlintCliError(1, 10, "rule-1", "I thought I would again", LINT_CAN_BE_AUTOCORRECTED),
        )
        reporter.onLintError(
            "/two-not-fixed.kt",
            @Suppress("ktlint:standard:argument-list-wrapping")
            KtlintCliError(2, 20, "rule-2", "A single thin straight line", LINT_CAN_BE_AUTOCORRECTED),
        )

        reporter.onLintError(
            "/all-corrected.kt",
            @Suppress("ktlint:standard:argument-list-wrapping")
            KtlintCliError(1, 1, "rule-1", "I thought we had more time", FORMAT_IS_AUTOCORRECTED),
        )
        reporter.afterAll()
        assertThat(String(out.toByteArray())).isEqualTo(
            """
            [
                {
                    "file": "/one-fixed-and-one-not.kt",
                    "errors": [
                        {
                            "line": 1,
                            "column": 1,
                            "message": "<\"&'>",
                            "rule": "rule-1"
                        }
                    ]
                },
                {
                    "file": "/two-not-fixed.kt",
                    "errors": [
                        {
                            "line": 1,
                            "column": 10,
                            "message": "I thought I would again",
                            "rule": "rule-1"
                        },
                        {
                            "line": 2,
                            "column": 20,
                            "message": "A single thin straight line",
                            "rule": "rule-2"
                        }
                    ]
                }
            ]

            """.trimIndent().replace("\n", System.lineSeparator()),
        )
    }

    @Test
    fun testProperEscaping() {
        val out = ByteArrayOutputStream()
        val reporter = JsonReporter(PrintStream(out, true))
        reporter.onLintError(
            "src\\main\\all\\corrected.kt",
            KtlintCliError(4, 7, "rule-7", "\\n\n\r\t\"", LINT_CAN_BE_AUTOCORRECTED),
        )
        reporter.afterAll()
        assertThat(String(out.toByteArray())).isEqualTo(
            """
            [
                {
                    "file": "src\\main\\all\\corrected.kt",
                    "errors": [
                        {
                            "line": 4,
                            "column": 7,
                            "message": "\\n\n\r\t\"",
                            "rule": "rule-7"
                        }
                    ]
                }
            ]

            """.trimIndent().replace("\n", System.lineSeparator()),
        )
    }
}
