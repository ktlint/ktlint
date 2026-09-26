package io.github.ktlint.core.cli.reporter.baseline

import assertk.assertThat
import assertk.assertions.isEqualTo
import io.github.ktlint.core.cli.reporter.core.api.KtlintCliError
import io.github.ktlint.core.cli.reporter.core.api.KtlintCliError.Status.FORMAT_IS_AUTOCORRECTED
import io.github.ktlint.core.cli.reporter.core.api.KtlintCliError.Status.LINT_CAN_BE_AUTOCORRECTED
import org.junit.jupiter.api.Test
import java.io.ByteArrayOutputStream
import java.io.PrintStream

class BaselineReporterTest {
    @Test
    fun testReportGeneration() {
        val out = ByteArrayOutputStream()
        val reporter = BaselineReporter(PrintStream(out, true))
        reporter.onLintError(
            "one-fixed-and-one-not.kt",
            KtlintCliError(1, 1, "rule-1", "<\"&'>", LINT_CAN_BE_AUTOCORRECTED),
        )
        reporter.onLintError(
            "one-fixed-and-one-not.kt",
            KtlintCliError(2, 1, "rule-2", "And if you see my friend", FORMAT_IS_AUTOCORRECTED),
        )

        reporter.onLintError(
            "two-not-fixed.kt",
            KtlintCliError(1, 10, "rule-1", "I thought I would again", LINT_CAN_BE_AUTOCORRECTED),
        )
        reporter.onLintError(
            "two-not-fixed.kt",
            KtlintCliError(2, 20, "rule-2", "A single thin straight line", LINT_CAN_BE_AUTOCORRECTED),
        )

        reporter.onLintError(
            "all-corrected.kt",
            KtlintCliError(1, 1, "rule-1", "I thought we had more time", FORMAT_IS_AUTOCORRECTED),
        )
        reporter.afterAll()
        assertThat(String(out.toByteArray())).isEqualTo(
            """
            <?xml version="1.0" encoding="utf-8"?>
            <baseline version="1.0">
                <file name="one-fixed-and-one-not.kt">
                    <error line="1" column="1" source="rule-1" />
                </file>
                <file name="two-not-fixed.kt">
                    <error line="1" column="10" source="rule-1" />
                    <error line="2" column="20" source="rule-2" />
                </file>
            </baseline>

            """.trimIndent().replace("\n", System.lineSeparator()),
        )
    }
}
