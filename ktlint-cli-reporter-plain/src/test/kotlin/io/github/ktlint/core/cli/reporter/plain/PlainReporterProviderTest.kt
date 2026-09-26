package io.github.ktlint.core.cli.reporter.plain

import assertk.assertFailure
import assertk.assertThat
import assertk.assertions.hasMessage
import assertk.assertions.isInstanceOf
import assertk.assertions.isNotNull
import org.junit.jupiter.api.Test
import java.io.PrintStream
import java.lang.System.out

class PlainReporterProviderTest {
    @Test
    fun `Given that a valid color name is provided then the plain reporter provider is created without exception`() {
        val plainReporter =
            PlainReporterProvider().get(
                out = PrintStream(out, true),
                opt = mapOf("color_name" to "RED"),
            )

        assertThat(plainReporter).isNotNull()
    }

    @Test
    fun `Given that the color_name attribute name is not provided then throw an IllegalArgumentException`() {
        assertFailure {
            PlainReporterProvider()
                .get(
                    out = PrintStream(out, true),
                    opt = mapOf(),
                )
        }.isInstanceOf<IllegalArgumentException>()
            .hasMessage("Invalid color parameter.")
    }

    @Test
    fun `Given that the color_name attribute name is empty then throw an IllegalArgumentException`() {
        assertFailure {
            PlainReporterProvider()
                .get(
                    out = PrintStream(out, true),
                    opt = mapOf("color_name" to ""),
                )
        }.isInstanceOf<IllegalArgumentException>()
            .hasMessage("Invalid color parameter.")
    }

    @Test
    fun `Given that an invalid color name is provided then the plain reporter provider throws an IllegalArgumentException`() {
        assertFailure {
            PlainReporterProvider()
                .get(
                    out = PrintStream(out, true),
                    opt = mapOf("color_name" to "GARBAGE_INPUT"),
                )
        }.isInstanceOf<IllegalArgumentException>()
            .hasMessage("Invalid color parameter.")
    }
}
