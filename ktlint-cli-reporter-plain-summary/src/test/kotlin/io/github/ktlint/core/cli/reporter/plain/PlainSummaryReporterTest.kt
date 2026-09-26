package io.github.ktlint.core.cli.reporter.plain

import assertk.assertThat
import assertk.assertions.isEqualTo
import io.github.ktlint.core.cli.reporter.core.api.KtlintCliError
import io.github.ktlint.core.cli.reporter.core.api.KtlintCliError.Status.FORMAT_IS_AUTOCORRECTED
import io.github.ktlint.core.cli.reporter.core.api.KtlintCliError.Status.KOTLIN_PARSE_EXCEPTION
import io.github.ktlint.core.cli.reporter.core.api.KtlintCliError.Status.LINT_CAN_BE_AUTOCORRECTED
import io.github.ktlint.core.cli.reporter.plainsummary.PlainSummaryReporter
import org.junit.jupiter.api.Test
import java.io.ByteArrayOutputStream
import java.io.PrintStream

class PlainSummaryReporterTest {
    @Test
    fun `Run report and print errors which are autocorrect and those that can not be autocorrected and print summaries`() {
        val out = ByteArrayOutputStream()
        PlainSummaryReporter(
            PrintStream(out, true),
        ).apply {
            onLintError(
                "file-1.kt",
                @Suppress("ktlint:standard:argument-list-wrapping")
                KtlintCliError(1, 1, "rule-1", "description-error-at-position-1:1 (cannot be auto-corrected)", LINT_CAN_BE_AUTOCORRECTED),
            )
            onLintError(
                "file-1.kt",
                @Suppress("ktlint:standard:argument-list-wrapping")
                KtlintCliError(2, 1, "rule-2", "description-error-at-position-2:1", FORMAT_IS_AUTOCORRECTED),
            )

            onLintError(
                "file-2.kt",
                @Suppress("ktlint:standard:argument-list-wrapping")
                KtlintCliError(1, 10, "rule-1", "description-error-at-position-1:10 (cannot be auto-corrected)", LINT_CAN_BE_AUTOCORRECTED),
            )
            onLintError(
                "file-2.kt",
                @Suppress("ktlint:standard:argument-list-wrapping")
                KtlintCliError(2, 20, "rule-2", "description-error-at-position-2:20 (cannot be auto-corrected)", LINT_CAN_BE_AUTOCORRECTED),
            )

            onLintError(
                "file-3.kt",
                @Suppress("ktlint:standard:argument-list-wrapping")
                KtlintCliError(1, 1, "rule-1", "description-error-at-position-1:1", FORMAT_IS_AUTOCORRECTED),
            )

            after("file-1.kt")
            after("file-2.kt")
            after("file-3.kt")

            afterAll()
        }

        assertThat(String(out.toByteArray())).isEqualTo(
            """
            Count (descending) of autocorrected errors by rule:
              rule-1: 1
              rule-2: 1

            Count (descending) of errors not autocorrected by rule:
              rule-1: 2
              rule-2: 1

            """.trimIndent().replace("\n", System.lineSeparator()),
        )
    }

    @Test
    fun `Report other violations`() {
        val out = ByteArrayOutputStream()
        val reporter = PlainSummaryReporter(PrintStream(out, true))
        @Suppress("ktlint:standard:max-line-length")
        reporter.onLintError(
            "file-1.kt",
            KtlintCliError(18, 51, "", "Not a valid Kotlin file (18:51 unexpected tokens (use ';' to separate expressions on the same line)) (cannot be auto-corrected) ()", KOTLIN_PARSE_EXCEPTION),
        )
        @Suppress("ktlint:standard:max-line-length")
        reporter.onLintError(
            "file-2.kt",
            KtlintCliError(18, 51, "", "Not a valid Kotlin file (18:51 unexpected tokens (use ';' to separate expressions on the same line)) (cannot be auto-corrected) ()", KOTLIN_PARSE_EXCEPTION),
        )
        @Suppress("ktlint:standard:argument-list-wrapping")
        reporter.onLintError(
            "file-3.kt",
            KtlintCliError(18, 51, "", "Something else", LINT_CAN_BE_AUTOCORRECTED),
        )
        reporter.afterAll()

        assertThat(String(out.toByteArray())).isEqualTo(
            """
            |Count (descending) of errors not autocorrected by rule:
            |  Not a valid Kotlin file: 2
            |  Unknown: 1
            |
            """.trimMargin().replace("\n", System.lineSeparator()),
        )
    }
}
