package io.github.ktlint.core.ruleset.standard.rules

import io.github.ktlint.core.test.KtLintAssertThat.Companion.assertThatRule
import io.github.ktlint.core.test.LintViolation
import io.github.ktlint.core.test.MULTILINE_STRING_QUOTE
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class NoConsecutiveBlankLinesRuleTest {
    private val noConsecutiveBlankLinesRuleAssertThat = assertThatRule { NoConsecutiveBlankLinesRule() }

    @Test
    fun `Given needless blank lines between declarations then do return lint errors`() {
        val code =
            """
            package com.test


            import com.test.util


            val a = "a"


            fun b() {
            }


            fun c()
            """.trimIndent()
        val formattedCode =
            """
            package com.test

            import com.test.util

            val a = "a"

            fun b() {
            }

            fun c()
            """.trimIndent()
        noConsecutiveBlankLinesRuleAssertThat(code)
            .hasLintViolations(
                LintViolation(3, 1, "Needless blank line(s)"),
                LintViolation(6, 1, "Needless blank line(s)"),
                LintViolation(9, 1, "Needless blank line(s)"),
                LintViolation(13, 1, "Needless blank line(s)"),
            ).isFormattedAs(formattedCode)
    }

    @Nested
    inner class `Given some blank lines at the end of the file` {
        @Test
        fun `Given two blank lines at end of file then do return lint errors`() {
            val code =
                """
                fun main() {
                }


                """.trimIndent()
            val formattedCode =
                """
                fun main() {
                }

                """.trimIndent()
            noConsecutiveBlankLinesRuleAssertThat(code)
                .hasLintViolation(4, 1, "Needless blank line(s)")
                .isFormattedAs(formattedCode)
        }

        @Test
        fun `Given three or more blank lines at end of file then do return lint errors`() {
            val code =
                """
                fun main() {
                }



                """.trimIndent()
            val formattedCode =
                """
                fun main() {
                }

                """.trimIndent()
            noConsecutiveBlankLinesRuleAssertThat(code)
                .hasLintViolation(4, 1, "Needless blank line(s)")
                .isFormattedAs(formattedCode)
        }
    }

    @Test
    fun `Given a string with multiple blank lines then do no return lint errors`() {
        val code =
            """
            fun main() {
                println(
                    $MULTILINE_STRING_QUOTE


                    $MULTILINE_STRING_QUOTE
                )
            }
            """.trimIndent()
        noConsecutiveBlankLinesRuleAssertThat(code).hasNoLintViolations()
    }

    @Nested
    inner class `Given some blank line between the class name and the primary constructor` {
        @Test
        fun `Given one or more blank line between the class name and primary constructor then do return a lint error`() {
            val code =
                """
                class A

                constructor(a: Int)

                class B


                private constructor(b: Int)
                """.trimIndent()
            val formattedCode =
                """
                class A
                constructor(a: Int)

                class B
                private constructor(b: Int)
                """.trimIndent()
            noConsecutiveBlankLinesRuleAssertThat(code)
                .hasLintViolations(
                    LintViolation(2, 1, "Needless blank line(s)"),
                    LintViolation(6, 1, "Needless blank line(s)"),
                ).isFormattedAs(formattedCode)
        }

        @Test
        fun `Given a class followed bij an EOL comment and a blank line before the constructor then do no return a lint error`() {
            val code =
                """
                class A // comment

                constructor(a: Int)
                """.trimIndent()
            val formattedCode =
                """
                class A // comment
                constructor(a: Int)
                """.trimIndent()
            noConsecutiveBlankLinesRuleAssertThat(code)
                .hasLintViolation(2, 1, "Needless blank line(s)")
                .isFormattedAs(formattedCode)
        }
    }

    @Test
    fun `should not raise NPE on linting Kotlin script file`() {
        val code =
            """
            import java.net.URI

            plugins {
                `java-library`
            }
            """.trimIndent()
        noConsecutiveBlankLinesRuleAssertThat(code)
            .asKotlinScript()
            .hasNoLintViolations()
    }

    @Test
    fun `Given single blank line in dot qualified expression should not return lint errors`() {
        val code =
            """
            fun foo(inputText: String) {
                inputText

                    .lowercase(Locale.getDefault())
            }
            """.trimIndent()

        noConsecutiveBlankLinesRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `Given multiple blank line in dot qualified expression should return lint error`() {
        val code =
            """
            fun foo(inputText: String) {
                inputText


                    .lowercase(Locale.getDefault())
            }
            """.trimIndent()

        noConsecutiveBlankLinesRuleAssertThat(code)
            .hasLintViolations(LintViolation(4, 1, "Needless blank line(s)"))
            .isFormattedAs(
                """
                fun foo(inputText: String) {
                    inputText

                        .lowercase(Locale.getDefault())
                }
                """.trimIndent(),
            )
    }

    @Test
    fun `Issue 1987 - Class without body but followed by multiple blank lines until end of file should not throw exception`() {
        val code = "class Foo\n\n\n"
        val formattedCode = "class Foo\n"
        noConsecutiveBlankLinesRuleAssertThat(code)
            .hasLintViolations(LintViolation(3, 1, "Needless blank line(s)"))
            .isFormattedAs(formattedCode)
    }

    @Test
    fun `Issue 3396 - Given consecutive blank lines at end of file, should not result in exception thrown in 'indent' rule`() {
        val code = "class Foo\n\n\n"
        val formattedCode = "class Foo\n"
        noConsecutiveBlankLinesRuleAssertThat(code)
            .addAdditionalRuleProvider { IndentationRule() }
            .hasLintViolations(LintViolation(3, 1, "Needless blank line(s)"))
            .isFormattedAs(formattedCode)
    }
}
