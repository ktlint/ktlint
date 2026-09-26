/*
 * MIT License
 *
 * Copyright (c) 2019 Matheus Candido
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.github.ktlint.core.cli.reporter.html

import assertk.assertThat
import assertk.assertions.isEqualTo
import io.github.ktlint.core.cli.reporter.core.api.KtlintCliError
import io.github.ktlint.core.cli.reporter.core.api.KtlintCliError.Status.FORMAT_IS_AUTOCORRECTED
import io.github.ktlint.core.cli.reporter.core.api.KtlintCliError.Status.LINT_CAN_BE_AUTOCORRECTED
import org.junit.jupiter.api.Test
import java.io.ByteArrayOutputStream
import java.io.PrintStream

class HtmlReporterTest {
    @Test
    fun `Should render empty report when no issues found`() {
        val out = ByteArrayOutputStream()
        val reporter = HtmlReporter(PrintStream(out, true))
        reporter.afterAll()

        val actual =
            """
            <html>
            <head>
            <link href="https://fonts.googleapis.com/css?family=Source+Code+Pro" rel="stylesheet" />
            <meta http-equiv="Content-Type" Content="text/html; Charset=UTF-8">
            <style>
            body {
                font-family: 'Source Code Pro', monospace;
            }
            h3 {
                font-size: 12pt;
            }</style>
            </head>
            <body>
            <p>Congratulations, no issues found!</p>
            </body>
            </html>

            """.trimIndent().replace("\n", System.lineSeparator())

        assertThat(actual).isEqualTo(String(out.toByteArray()))
    }

    @Test
    fun `Should render issues when lint problems found`() {
        val out = ByteArrayOutputStream()
        val reporter = HtmlReporter(PrintStream(out, true))

        reporter.onLintError(
            "/file1.kt",
            KtlintCliError(1, 1, "rule-1", "rule-1 broken", LINT_CAN_BE_AUTOCORRECTED),
        )

        reporter.afterAll()

        val actual =
            """
            <html>
            <head>
            <link href="https://fonts.googleapis.com/css?family=Source+Code+Pro" rel="stylesheet" />
            <meta http-equiv="Content-Type" Content="text/html; Charset=UTF-8">
            <style>
            body {
                font-family: 'Source Code Pro', monospace;
            }
            h3 {
                font-size: 12pt;
            }</style>
            </head>
            <body>
            <h1>Overview</h1>
            <p>Issues found: 1</p>
            <p>Issues corrected: 0</p>
            <h3>/file1.kt</h3>
            <ul>
            <li>(1, 1): rule-1 broken  (rule-1)</li>
            </ul>
            </body>
            </html>

            """.trimIndent().replace("\n", System.lineSeparator())

        assertThat(actual).isEqualTo(String(out.toByteArray()))
    }

    @Test
    fun `Should not render corrected issues when lint one is found`() {
        val out = ByteArrayOutputStream()
        val reporter = HtmlReporter(PrintStream(out, true))

        reporter.onLintError(
            "/file1.kt",
            KtlintCliError(1, 1, "rule-1", "rule-1 broken", LINT_CAN_BE_AUTOCORRECTED),
        )

        reporter.onLintError(
            "/file2.kt",
            KtlintCliError(1, 1, "rule-1", "rule-1 broken", FORMAT_IS_AUTOCORRECTED),
        )

        reporter.afterAll()

        val actual =
            """
            <html>
            <head>
            <link href="https://fonts.googleapis.com/css?family=Source+Code+Pro" rel="stylesheet" />
            <meta http-equiv="Content-Type" Content="text/html; Charset=UTF-8">
            <style>
            body {
                font-family: 'Source Code Pro', monospace;
            }
            h3 {
                font-size: 12pt;
            }</style>
            </head>
            <body>
            <h1>Overview</h1>
            <p>Issues found: 1</p>
            <p>Issues corrected: 1</p>
            <h3>/file1.kt</h3>
            <ul>
            <li>(1, 1): rule-1 broken  (rule-1)</li>
            </ul>
            </body>
            </html>

            """.trimIndent().replace("\n", System.lineSeparator())

        assertThat(actual).isEqualTo(String(out.toByteArray()))
    }

    @Test
    fun `Should render issues and escape special HTML symbols when lint problems found`() {
        val out = ByteArrayOutputStream()
        val reporter = HtmlReporter(PrintStream(out, true))

        @Suppress("ktlint:standard:max-line-length")
        reporter.onLintError(
            "/file1.kt",
            KtlintCliError(1, 1, "rule-1", "Error message contains a generic type like List<Int> (cannot be auto-corrected)", LINT_CAN_BE_AUTOCORRECTED),
        )

        @Suppress("ktlint:standard:max-line-length")
        reporter.onLintError(
            "/file1.kt",
            KtlintCliError(2, 1, "rule-2", "Error message contains special html symbols like a<b>c\"d'e&f (cannot be auto-corrected)", LINT_CAN_BE_AUTOCORRECTED),
        )

        reporter.afterAll()

        val actual =
            """
            <html>
            <head>
            <link href="https://fonts.googleapis.com/css?family=Source+Code+Pro" rel="stylesheet" />
            <meta http-equiv="Content-Type" Content="text/html; Charset=UTF-8">
            <style>
            body {
                font-family: 'Source Code Pro', monospace;
            }
            h3 {
                font-size: 12pt;
            }</style>
            </head>
            <body>
            <h1>Overview</h1>
            <p>Issues found: 2</p>
            <p>Issues corrected: 0</p>
            <h3>/file1.kt</h3>
            <ul>
            <li>(1, 1): Error message contains a generic type like List&lt;Int&gt; (cannot be auto-corrected)  (rule-1)</li>
            <li>(2, 1): Error message contains special html symbols like a&lt;b&gt;c&quot;d&apos;e&amp;f (cannot be auto-corrected)  (rule-2)</li>
            </ul>
            </body>
            </html>

            """.trimIndent().replace("\n", System.lineSeparator())

        assertThat(actual).isEqualTo(String(out.toByteArray()))
    }
}
