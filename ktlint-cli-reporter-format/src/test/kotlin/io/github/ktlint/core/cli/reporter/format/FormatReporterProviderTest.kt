package io.github.ktlint.core.cli.reporter.format

import assertk.assertFailure
import assertk.assertThat
import assertk.assertions.hasMessage
import assertk.assertions.isInstanceOf
import assertk.assertions.isNotNull
import org.junit.jupiter.api.Test
import java.io.PrintStream
import java.lang.System.out

class FormatReporterProviderTest {
    @Test
    fun `Given that the format configuration option is not specified then then throw an IllegalArgumentException`() {
        assertFailure {
            FormatReporterProvider()
                .get(
                    out = PrintStream(out, true),
                    opt = emptyMap(),
                )
        }.isInstanceOf<IllegalArgumentException>()
            .hasMessage("Format is not specified in config options")
    }

    @Test
    fun `Given that the format configuration and a valid color name is provided then the format reporter provider is created without exception`() {
        val formatReporter =
            FormatReporterProvider().get(
                out = PrintStream(out, true),
                opt =
                    mapOf(
                        "format" to "true",
                        "color_name" to "RED",
                    ),
            )

        assertThat(formatReporter).isNotNull()
    }

    @Test
    fun `Given that the format configuration is specified with an invalid value and a valid color name is provided then throw an IllegalArgumentException`() {
        assertFailure {
            FormatReporterProvider()
                .get(
                    out = PrintStream(out, true),
                    opt =
                        mapOf(
                            "format" to "invalid",
                            "color_name" to "RED",
                        ),
                )
        }.isInstanceOf<IllegalArgumentException>()
            .hasMessage("The string doesn't represent a boolean value: invalid")
    }

    @Test
    fun `Given that the format configuration is provided but the color_name attribute name is not provided then throw an IllegalArgumentException`() {
        assertFailure {
            FormatReporterProvider()
                .get(
                    out = PrintStream(out, true),
                    opt = mapOf("format" to "true"),
                )
        }.isInstanceOf<IllegalArgumentException>()
            .hasMessage("Invalid color parameter.")
    }

    @Test
    fun `Given that the format configuration is provided and the color_name attribute name is empty then throw an IllegalArgumentException`() {
        assertFailure {
            FormatReporterProvider()
                .get(
                    out = PrintStream(out, true),
                    opt =
                        mapOf(
                            "format" to "true",
                            "color_name" to "",
                        ),
                )
        }.isInstanceOf<IllegalArgumentException>()
            .hasMessage("Invalid color parameter.")
    }

    @Test
    fun `Given that the format configuration is provided an invalid color name is provided then the format reporter provider throws an IllegalArgumentException`() {
        assertFailure {
            FormatReporterProvider()
                .get(
                    out = PrintStream(out, true),
                    opt =
                        mapOf(
                            "format" to "true",
                            "color_name" to "GARBAGE_INPUT",
                        ),
                )
        }.isInstanceOf<IllegalArgumentException>()
            .hasMessage("Invalid color parameter.")
    }
}
