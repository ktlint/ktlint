package io.github.ktlint.core.ruleset.standard.rules

import io.github.ktlint.core.rule.engine.core.api.IndentConfig
import io.github.ktlint.core.rule.engine.core.api.editorconfig.INDENT_STYLE_PROPERTY
import io.github.ktlint.core.test.KtLintAssertThat.Companion.assertThatRule
import io.github.ktlint.core.test.LintViolation
import io.github.ktlint.core.test.MULTILINE_STRING_QUOTE
import io.github.ktlint.core.test.TAB
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@Suppress("RemoveCurlyBracesFromTemplate")
class StringTemplateIndentRuleTest {
    private val stringTemplateIndentRuleAssertThat = assertThatRule { StringTemplateIndentRule() }

    @Test
    fun `Do not move a multiline string literal after return statement to a new line as that results in a compilation error`() {
        val code =
            """
            fun someFunction() {
                return $MULTILINE_STRING_QUOTE
                       someText
                       $MULTILINE_STRING_QUOTE.trimIndent()
            }
            """.trimIndent()
        val formattedCode =
            """
            fun someFunction() {
                return $MULTILINE_STRING_QUOTE
                    someText
                    $MULTILINE_STRING_QUOTE.trimIndent()
            }
            """.trimIndent()
        stringTemplateIndentRuleAssertThat(code)
            .hasLintViolations(
                LintViolation(3, 12, "Unexpected indent of raw string literal"),
                LintViolation(3, 12, "Unexpected indent of raw string literal"),
                LintViolation(4, 12, "Unexpected indent of raw string literal"),
            ).isFormattedAs(formattedCode)
    }

    @Test
    fun `Move a multiline string literal (indented with tabs) as body expression`() {
        val code =
            """
            fun someFunction() = $MULTILINE_STRING_QUOTE
            ${TAB}${TAB}${TAB}${TAB}someText
            ${TAB}${TAB}${TAB}${TAB}$MULTILINE_STRING_QUOTE.trimIndent()
            """.trimIndent()
        val formattedCode =
            """
            fun someFunction() =
            ${TAB}$MULTILINE_STRING_QUOTE
            ${TAB}someText
            ${TAB}$MULTILINE_STRING_QUOTE.trimIndent()
            """.trimIndent()
        stringTemplateIndentRuleAssertThat(code)
            .withEditorConfigOverride(INDENT_STYLE_PROPERTY to IndentConfig.IndentStyle.TAB)
            .hasLintViolations(
                LintViolation(1, 22, "Expected newline before multiline string template"),
                LintViolation(2, 5, "Unexpected indent of raw string literal"),
                LintViolation(3, 5, "Unexpected indent of raw string literal"),
            ).isFormattedAs(formattedCode)
    }

    @Nested
    inner class `A multiline raw string literal containing both tabs and spaces in the indentation margin can not be autocorrected` {
        @Test
        fun `Mixed indentation characters on inner lines`() {
            val code =
                """
                val foo = $MULTILINE_STRING_QUOTE
                      line1
                ${TAB} line2
                    $MULTILINE_STRING_QUOTE.trimIndent()
                """.trimIndent()
            stringTemplateIndentRuleAssertThat(code)
                .hasLintViolations(
                    LintViolation(1, 11, "Expected newline before multiline string template"),
                    LintViolation(1, 11, "Indentation of multiline raw string literal should not contain both tab(s) and space(s)", false),
                )
        }

        @Test
        fun `Mixed indentation characters including line with opening quotes`() {
            val code =
                """
                val foo = $MULTILINE_STRING_QUOTE${TAB}line1
                    ${TAB} line2
                    $MULTILINE_STRING_QUOTE.trimIndent()
                """.trimIndent()
            stringTemplateIndentRuleAssertThat(code)
                .hasLintViolations(
                    LintViolation(1, 11, "Expected newline before multiline string template"),
                    LintViolation(1, 11, "Indentation of multiline raw string literal should not contain both tab(s) and space(s)", false),
                )
        }

        @Test
        fun `Mixed indentation characters including line with closing quotes`() {
            val code =
                """
                val foo = $MULTILINE_STRING_QUOTE
                    ${TAB}line1
                    ${TAB} line2$MULTILINE_STRING_QUOTE.trimIndent()
                """.trimIndent()
            stringTemplateIndentRuleAssertThat(code)
                .hasLintViolations(
                    LintViolation(1, 11, "Expected newline before multiline string template"),
                    LintViolation(1, 11, "Indentation of multiline raw string literal should not contain both tab(s) and space(s)", false),
                )
        }
    }

    @Test
    fun `A multiline raw string literal should not contain text on the same line as the opening quotes`() {
        val code =
            """
            val someVar =
                ${MULTILINE_STRING_QUOTE}line1
                line2
                $MULTILINE_STRING_QUOTE.trimIndent()
            """.trimIndent()
        val formattedCode =
            """
            val someVar =
                $MULTILINE_STRING_QUOTE
                line1
                line2
                $MULTILINE_STRING_QUOTE.trimIndent()
            """.trimIndent()
        stringTemplateIndentRuleAssertThat(code)
            .hasLintViolation(2, 13, "Missing newline after the opening quotes of the raw string literal")
            .isFormattedAs(formattedCode)
    }

    @Test
    fun `A multiline raw string literal can contain text on the same line as the closing quotes`() {
        val code =
            """
            val someVar = $MULTILINE_STRING_QUOTE
                          line1
                          line2$MULTILINE_STRING_QUOTE.trimIndent()
            """.trimIndent()
        val formattedCode =
            """
            val someVar =
                $MULTILINE_STRING_QUOTE
                line1
                line2
                $MULTILINE_STRING_QUOTE.trimIndent()
            """.trimIndent()
        stringTemplateIndentRuleAssertThat(code)
            .hasLintViolations(
                LintViolation(1, 15, "Expected newline before multiline string template"),
                LintViolation(2, 15, "Unexpected indent of raw string literal"),
                LintViolation(3, 15, "Unexpected indent of raw string literal"),
                LintViolation(3, 20, "Missing newline before the closing quotes of the raw string literal"),
            ).isFormattedAs(formattedCode)
    }

    @Test
    fun `Move a multiline raw string literal assignment to a variable to a new line`() {
        val code =
            """
            val someVar = $MULTILINE_STRING_QUOTE
                          someText
                          $MULTILINE_STRING_QUOTE.trimIndent()
            """.trimIndent()
        val formattedCode =
            """
            val someVar =
                $MULTILINE_STRING_QUOTE
                someText
                $MULTILINE_STRING_QUOTE.trimIndent()
            """.trimIndent()
        stringTemplateIndentRuleAssertThat(code)
            .hasLintViolations(
                LintViolation(1, 15, "Expected newline before multiline string template"),
                LintViolation(2, 15, "Unexpected indent of raw string literal"),
                LintViolation(3, 15, "Unexpected indent of raw string literal"),
            ).isFormattedAs(formattedCode)
    }

    @Test
    fun `Move a multiline string literal in a function parameter to a new line`() {
        val code =
            """
            fun someFunction() {
                println($MULTILINE_STRING_QUOTE
                        someText
                        $MULTILINE_STRING_QUOTE.trimIndent())
            }
            """.trimIndent()
        val formattedCode =
            """
            fun someFunction() {
                println(
                    $MULTILINE_STRING_QUOTE
                    someText
                    $MULTILINE_STRING_QUOTE.trimIndent()
                )
            }
            """.trimIndent()
        stringTemplateIndentRuleAssertThat(code)
            .addAdditionalRuleProvider { IndentationRule() }
            .hasLintViolations(
                LintViolation(2, 13, "Expected newline before multiline string template"),
                LintViolation(3, 13, "Unexpected indent of raw string literal"),
                LintViolation(4, 13, "Unexpected indent of raw string literal"),
                LintViolation(4, 29, "Expected newline after 'trimIndent' in a multiline string template"),
            ).isFormattedAs(formattedCode)
    }

    @Test
    fun `Format a multiline string literal with text starting at position 1 of the line then do not indent the content`() {
        val code =
            """
            fun foo() {
                println($MULTILINE_STRING_QUOTE
            Some text starting at the beginning of the line

                Some text not starting at the beginning of the line
            $MULTILINE_STRING_QUOTE.trimIndent())
            }
            """.trimIndent()
        val formattedCode =
            """
            fun foo() {
                println(
                    $MULTILINE_STRING_QUOTE
            Some text starting at the beginning of the line

                Some text not starting at the beginning of the line
                    $MULTILINE_STRING_QUOTE.trimIndent()
                )
            }
            """.trimIndent()
        stringTemplateIndentRuleAssertThat(code)
            .addAdditionalRuleProvider { IndentationRule() }
            .hasLintViolationsForAdditionalRule(
                LintViolation(6, 1, "Unexpected indent of multiline string closing quotes"),
            ).hasLintViolations(
                LintViolation(2, 13, "Expected newline before multiline string template"),
                LintViolation(6, 17, "Expected newline after 'trimIndent' in a multiline string template"),
            ).isFormattedAs(formattedCode)
    }

    @Test
    fun `Format multiline string with unexpected tabs in indentation margin`() {
        val code =
            """
            val str = $MULTILINE_STRING_QUOTE
            ${TAB}line1
            ${TAB}${TAB}line2
            $MULTILINE_STRING_QUOTE.trimIndent()
            """.trimIndent()
        val formattedCode =
            """
            val str =
                $MULTILINE_STRING_QUOTE
                line1
                ${TAB}line2
                $MULTILINE_STRING_QUOTE.trimIndent()
            """.trimIndent()
        stringTemplateIndentRuleAssertThat(code)
            .hasLintViolations(
                LintViolation(1, 11, "Expected newline before multiline string template"),
                LintViolation(2, 1, "Unexpected 'tab' character(s) in margin of multiline string"),
                LintViolation(3, 1, "Unexpected 'tab' character(s) in margin of multiline string"),
                LintViolation(4, 1, "Unexpected indent of raw string literal"),
            ).isFormattedAs(formattedCode)
    }

    @Test
    fun `Format multiline string without tabs in the indentation margin`() {
        val code =
            """
            val str = $MULTILINE_STRING_QUOTE
                  ${TAB}Tab at the beginning of this line but after the indentation margin
                  Tab${TAB}in the middle of this string
                  Tab at the end of this line.${TAB}
            $MULTILINE_STRING_QUOTE.trimIndent()
            """.trimIndent()
        val formattedCode =
            """
            val str =
                $MULTILINE_STRING_QUOTE
                ${TAB}Tab at the beginning of this line but after the indentation margin
                Tab${TAB}in the middle of this string
                Tab at the end of this line.${TAB}
                $MULTILINE_STRING_QUOTE.trimIndent()
            """.trimIndent()
        stringTemplateIndentRuleAssertThat(code)
            .hasLintViolations(
                LintViolation(1, 11, "Expected newline before multiline string template"),
                LintViolation(2, 7, "Unexpected indent of raw string literal"),
                LintViolation(3, 7, "Unexpected indent of raw string literal"),
                LintViolation(4, 7, "Unexpected indent of raw string literal"),
                LintViolation(5, 1, "Unexpected indent of raw string literal"),
            ).isFormattedAs(formattedCode)
    }

    @Test
    fun `lint property delegate is indented properly 1`() {
        val code =
            """
            val string: String
                by lazy { $MULTILINE_STRING_QUOTE
                          someText
                          $MULTILINE_STRING_QUOTE.trimIndent()
                }
            """.trimIndent()
        val formattedCode =
            """
            val string: String
                by lazy {
                    $MULTILINE_STRING_QUOTE
                    someText
                    $MULTILINE_STRING_QUOTE.trimIndent()
                }
            """.trimIndent()
        stringTemplateIndentRuleAssertThat(code)
            .hasLintViolations(
                LintViolation(2, 15, "Expected newline before multiline string template"),
                LintViolation(3, 15, "Unexpected indent of raw string literal"),
                LintViolation(4, 15, "Unexpected indent of raw string literal"),
            ).isFormattedAs(formattedCode)
    }

    @Test
    fun `lint property delegate is indented properly 4`() {
        val code =
            """
            fun lazyString() =
                lazy { $MULTILINE_STRING_QUOTE
                       someText
                       $MULTILINE_STRING_QUOTE.trimIndent()
                }
            """.trimIndent()
        val formattedCode =
            """
            fun lazyString() =
                lazy {
                    $MULTILINE_STRING_QUOTE
                    someText
                    $MULTILINE_STRING_QUOTE.trimIndent()
                }
            """.trimIndent()
        stringTemplateIndentRuleAssertThat(code)
            .hasLintViolations(
                LintViolation(2, 12, "Expected newline before multiline string template"),
                LintViolation(3, 12, "Unexpected indent of raw string literal"),
                LintViolation(4, 12, "Unexpected indent of raw string literal"),
            ).isFormattedAs(formattedCode)
    }

    @Test
    fun `lint parameter with default multiline string raw string literal`() {
        val code =
            """
            data class SomeDataClass(
                val string: String = $MULTILINE_STRING_QUOTE
                                     someText
                                     $MULTILINE_STRING_QUOTE.trimIndent(),
                val int: Int
            )
            """.trimIndent()
        val formattedCode =
            """
            data class SomeDataClass(
                val string: String =
                    $MULTILINE_STRING_QUOTE
                    someText
                    $MULTILINE_STRING_QUOTE.trimIndent(),
                val int: Int
            )
            """.trimIndent()
        stringTemplateIndentRuleAssertThat(code)
            .hasLintViolations(
                LintViolation(2, 26, "Expected newline before multiline string template"),
                LintViolation(3, 26, "Unexpected indent of raw string literal"),
                LintViolation(4, 26, "Unexpected indent of raw string literal"),
            ).isFormattedAs(formattedCode)
    }

    @Test
    fun `lint parameter with multiline string raw string literal after arrow`() {
        val code =
            """
            val result =
                when {
                    someBooleanFunction() -> $MULTILINE_STRING_QUOTE
                                             someText
                                             $MULTILINE_STRING_QUOTE.trimIndent()
                    else -> $MULTILINE_STRING_QUOTE
                            someOtherText
                            $MULTILINE_STRING_QUOTE.trimIndent()
                }
            """.trimIndent()
        val formattedCode =
            """
            val result =
                when {
                    someBooleanFunction() ->
                        $MULTILINE_STRING_QUOTE
                        someText
                        $MULTILINE_STRING_QUOTE.trimIndent()
                    else ->
                        $MULTILINE_STRING_QUOTE
                        someOtherText
                        $MULTILINE_STRING_QUOTE.trimIndent()
                }
            """.trimIndent()
        stringTemplateIndentRuleAssertThat(code)
            .hasLintViolations(
                LintViolation(3, 34, "Expected newline before multiline string template"),
                LintViolation(4, 34, "Unexpected indent of raw string literal"),
                LintViolation(5, 34, "Unexpected indent of raw string literal"),
                LintViolation(6, 17, "Expected newline before multiline string template"),
                LintViolation(7, 17, "Unexpected indent of raw string literal"),
                LintViolation(8, 17, "Unexpected indent of raw string literal"),
            ).isFormattedAs(formattedCode)
    }

    @Test
    fun `Given a multiline raw string literal and trimIndent is followed by another dot qualified expression`() {
        val code =
            """
            val foo = $MULTILINE_STRING_QUOTE
                someText
                $MULTILINE_STRING_QUOTE.trimIndent().lowercase()
            """.trimIndent()
        val formattedCode =
            """
            val foo =
                $MULTILINE_STRING_QUOTE
                someText
                $MULTILINE_STRING_QUOTE.trimIndent().lowercase()
            """.trimIndent()
        stringTemplateIndentRuleAssertThat(code)
            .hasLintViolation(1, 11, "Expected newline before multiline string template")
            .isFormattedAs(formattedCode)
    }

    @Test
    fun `Issue 2530 - Given a raw string literal containing string templates at position 1 of the line`() {
        val code =
            $$"""
            fun foo() {
                val strings = listOf("a")
                println(
                    $$MULTILINE_STRING_QUOTE
            ${strings.joinToString { "a" }}
            $strings
                    $$MULTILINE_STRING_QUOTE
                )
            }
            val foo =
                $$MULTILINE_STRING_QUOTE
            ${strings.joinToString { "a" }}
            $strings
                $$MULTILINE_STRING_QUOTE.trimIndent()
            """.trimIndent()
        stringTemplateIndentRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `Given a string template on same line as closing parenthesis of multiline function signature`() {
        val code =
            $$"""
            fun foo(
                bar: String
            ) = $$MULTILINE_STRING_QUOTE
                Bar: $bar
                $$MULTILINE_STRING_QUOTE.trimIndent()
            """.trimIndent()
        stringTemplateIndentRuleAssertThat(code)
            .addAdditionalRuleProvider { MultilineExpressionWrappingRule() }
            .hasNoLintViolations()
    }

    @Test
    fun `Issue 2577 - Given a multiline string template as left hand side in a binary expression then do not wrap the operator to the next line`() {
        val code =
            """
            val foo =
                $MULTILINE_STRING_QUOTE
                Some text
                $MULTILINE_STRING_QUOTE.trimIndent() + "more texttttttttttt"
            """.trimIndent()
        stringTemplateIndentRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `Issue 2885 - Given a multiline string template prefix with a multi-dollar interpolation prefix`() {
        val code =
            """
            val foo =
                $$$MULTILINE_STRING_QUOTE
                Some text
                $MULTILINE_STRING_QUOTE.trimIndent()
            """.trimIndent()
        stringTemplateIndentRuleAssertThat(code).hasNoLintViolations()
    }
}
