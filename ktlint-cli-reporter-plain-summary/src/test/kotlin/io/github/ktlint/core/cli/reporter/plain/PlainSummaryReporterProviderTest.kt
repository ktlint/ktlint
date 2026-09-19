package io.github.ktlint.core.cli.reporter.plain

import assertk.assertThat
import assertk.assertions.isNotNull
import io.github.ktlint.core.cli.reporter.plainsummary.PlainSummaryReporterProvider
import org.junit.jupiter.api.Test
import java.io.PrintStream
import java.lang.System.out

class PlainSummaryReporterProviderTest {
    @Test
    fun `Get a plain summary reporter then create it without exception`() {
        val plainSummaryReporter =
            PlainSummaryReporterProvider().get(
                out = PrintStream(out, true),
                opt = emptyMap(),
            )

        assertThat(plainSummaryReporter).isNotNull()
    }
}
