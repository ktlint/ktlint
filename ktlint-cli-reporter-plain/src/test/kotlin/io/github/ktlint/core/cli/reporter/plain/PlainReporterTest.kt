package io.github.ktlint.core.cli.reporter.plain

import assertk.assertThat
import assertk.assertions.isEqualTo
import io.github.ktlint.core.cli.reporter.core.api.KtlintCliError
import io.github.ktlint.core.cli.reporter.core.api.KtlintCliError.Status.FORMAT_IS_AUTOCORRECTED
import io.github.ktlint.core.cli.reporter.core.api.KtlintCliError.Status.KOTLIN_PARSE_EXCEPTION
import io.github.ktlint.core.cli.reporter.core.api.KtlintCliError.Status.LINT_CAN_BE_AUTOCORRECTED
import org.junit.jupiter.api.Test
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.PrintStream

class PlainReporterTest {
    @Test
    fun `Report normal rule violations`() {
        val out = ByteArrayOutputStream()
        val reporter = PlainReporter(PrintStream(out, true))
        reporter.onLintError(
            "file-1.kt",
            @Suppress("ktlint:standard:argument-list-wrapping")
            KtlintCliError(1, 1, "rule-1", "description-error-at-position-1:1", LINT_CAN_BE_AUTOCORRECTED),
        )
        reporter.onLintError(
            "file-1.kt",
            @Suppress("ktlint:standard:argument-list-wrapping")
            KtlintCliError(2, 1, "rule-2", "description-error-at-position-2:1", FORMAT_IS_AUTOCORRECTED),
        )

        reporter.onLintError(
            "file-2.kt",
            @Suppress("ktlint:standard:argument-list-wrapping")
            KtlintCliError(1, 10, "rule-1", "description-error-at-position-1:10", LINT_CAN_BE_AUTOCORRECTED),
        )
        reporter.onLintError(
            "file-2.kt",
            @Suppress("ktlint:standard:argument-list-wrapping")
            KtlintCliError(2, 20, "rule-2", "description-error-at-position-2:20", LINT_CAN_BE_AUTOCORRECTED),
        )

        reporter.onLintError(
            "file-3.kt",
            @Suppress("ktlint:standard:argument-list-wrapping")
            KtlintCliError(1, 1, "rule-1", "description-error-at-position-1:1", FORMAT_IS_AUTOCORRECTED),
        )

        reporter.afterAll()

        assertThat(String(out.toByteArray())).isEqualTo(
            """
            |file-1.kt:1:1: description-error-at-position-1:1 (rule-1)
            |file-2.kt:1:10: description-error-at-position-1:10 (rule-1)
            |file-2.kt:2:20: description-error-at-position-2:20 (rule-2)
            |
            |Summary error count (descending) by rule:
            |  rule-1: 2
            |  rule-2: 1
            |
            """.trimMargin().replace("\n", System.lineSeparator()),
        )
    }

    @Test
    fun `Report other violations`() {
        val out = ByteArrayOutputStream()
        val reporter = PlainReporter(PrintStream(out, true))
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
            |file-1.kt:18:51: Not a valid Kotlin file (18:51 unexpected tokens (use ';' to separate expressions on the same line)) (cannot be auto-corrected) () ()
            |file-2.kt:18:51: Not a valid Kotlin file (18:51 unexpected tokens (use ';' to separate expressions on the same line)) (cannot be auto-corrected) () ()
            |file-3.kt:18:51: Something else ()
            |
            |Summary error count (descending) by rule:
            |  Not a valid Kotlin file: 2
            |  Unknown: 1
            |
            """.trimMargin().replace("\n", System.lineSeparator()),
        )
    }

    @Test
    fun testColoredOutput() {
        val out = ByteArrayOutputStream()
        val outputColor = Color.DARK_GRAY
        val reporter =
            PlainReporter(
                PrintStream(out, true),
                shouldColorOutput = true,
                outputColor = outputColor,
            )
        reporter.onLintError(
            File.separator + "one-fixed-and-one-not.kt",
            @Suppress("ktlint:standard:argument-list-wrapping")
            KtlintCliError(1, 1, "rule-1", "<\"&'>", LINT_CAN_BE_AUTOCORRECTED),
        )
        val outputString = String(out.toByteArray())

        assertThat(outputString).isEqualTo(
            // We don't expect class name, or first line to be colored
            File.separator.color(outputColor) +
                "one-fixed-and-one-not.kt" +
                ":".color(outputColor) +
                "1" +
                ":1:".color(outputColor) +
                " <\"&'> " +
                "(rule-1)".color(outputColor) +
                System.lineSeparator(),
        )
    }

    @Test
    fun testReportGenerationGroupedByFile() {
        val out = ByteArrayOutputStream()
        val reporter = PlainReporter(PrintStream(out, true), groupByFile = true)
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
        reporter.after("/one-fixed-and-one-not.kt")
        reporter.after("/two-not-fixed.kt")
        reporter.after("/all-corrected.kt")
        assertThat(String(out.toByteArray())).isEqualTo(
            """
            /one-fixed-and-one-not.kt
              1:1 <"&'> (rule-1)
            /two-not-fixed.kt
              1:10 I thought I would again (rule-1)
              2:20 A single thin straight line (rule-2)

            """.trimIndent().replace("\n", System.lineSeparator()),
        )
    }
}
