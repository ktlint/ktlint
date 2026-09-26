package io.github.ktlint.core.cli.reporter.sarif

import assertk.assertThat
import assertk.assertions.isEqualTo
import io.github.ktlint.core.cli.reporter.core.api.KtlintCliError
import io.github.ktlint.core.cli.reporter.core.api.KtlintCliError.Status.FORMAT_IS_AUTOCORRECTED
import io.github.ktlint.core.cli.reporter.core.api.KtlintCliError.Status.LINT_CAN_BE_AUTOCORRECTED
import org.junit.jupiter.api.Test
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.PrintStream

class SarifReporterTest {
    @Test
    fun testReportGeneration() {
        val workingDirectory = System.getProperty("user.home").sanitize()
        val out = ByteArrayOutputStream()
        val reporter = SarifReporter(PrintStream(out))
        reporter.beforeAll()
        reporter.onLintError(
            "$workingDirectory/one-fixed-and-one-not.kt",
            KtlintCliError(1, 1, "rule-1", "<\"&'>", LINT_CAN_BE_AUTOCORRECTED),
        )
        reporter.onLintError(
            "$workingDirectory/one-fixed-and-one-not.kt",
            KtlintCliError(2, 1, "rule-2", "And if you see my friend", FORMAT_IS_AUTOCORRECTED),
        )
        reporter.onLintError(
            "$workingDirectory/two-not-fixed.kt",
            KtlintCliError(1, 10, "rule-1", "I thought I would again", LINT_CAN_BE_AUTOCORRECTED),
        )
        reporter.onLintError(
            "$workingDirectory/two-not-fixed.kt",
            KtlintCliError(2, 20, "rule-2", "A single thin straight line", LINT_CAN_BE_AUTOCORRECTED),
        )
        reporter.onLintError(
            "$workingDirectory/all-corrected.kt",
            KtlintCliError(1, 1, "rule-1", "I thought we had more time", FORMAT_IS_AUTOCORRECTED),
        )
        reporter.afterAll()

        val actual = String(out.toByteArray()).replace("\\s".toRegex(), "")
        val expected =
            File(javaClass.getResource("/relative-path.sarif").path)
                .readText()
                .replace("{WORKINDG_DIR}", workingDirectory)
                .replace("\\s".toRegex(), "")
        assertThat(actual).isEqualTo(expected)
    }
}
