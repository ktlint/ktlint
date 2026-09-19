package io.github.ktlint.core.cli.reporter.checkstyle

import assertk.assertThat
import assertk.assertions.isEqualTo
import io.github.ktlint.core.cli.reporter.core.api.KtlintCliError
import io.github.ktlint.core.cli.reporter.core.api.KtlintCliError.Status.FORMAT_IS_AUTOCORRECTED
import io.github.ktlint.core.cli.reporter.core.api.KtlintCliError.Status.LINT_CAN_BE_AUTOCORRECTED
import org.junit.jupiter.api.Test
import java.io.ByteArrayOutputStream
import java.io.PrintStream

class CheckStyleReporterTest {
    @Test
    fun testReportGeneration() {
        val out = ByteArrayOutputStream()
        val reporter = CheckStyleReporter(PrintStream(out, true))
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
            <?xml version="1.0" encoding="utf-8"?>
            <checkstyle version="8.0">
                <file name="/one-fixed-and-one-not.kt">
                    <error line="1" column="1" severity="error" message="&lt;&quot;&amp;&apos;&gt;" source="rule-1" />
                </file>
                <file name="/two-not-fixed.kt">
                    <error line="1" column="10" severity="error" message="I thought I would again" source="rule-1" />
                    <error line="2" column="20" severity="error" message="A single thin straight line" source="rule-2" />
                </file>
            </checkstyle>

            """.trimIndent().replace("\n", System.lineSeparator()),
        )
    }
}
