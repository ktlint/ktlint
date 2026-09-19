package io.github.ktlint.core.ruleset.standard.rules

import assertk.assertFailure
import assertk.assertThat
import assertk.assertions.hasMessage
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.isNull
import io.github.ktlint.core.rule.engine.core.api.editorconfig.CODE_STYLE_PROPERTY
import io.github.ktlint.core.rule.engine.core.api.editorconfig.CodeStyleValue
import io.github.ktlint.core.rule.engine.core.api.editorconfig.CodeStyleValue.ktlint_official
import io.github.ktlint.core.rule.engine.core.api.editorconfig.ec4j.toPropertyWithValue
import io.github.ktlint.core.ruleset.standard.rules.FunctionSignatureRule.Companion.FORCE_MULTILINE_WHEN_PARAMETER_COUNT_GREATER_OR_EQUAL_THAN_PROPERTY
import io.github.ktlint.core.ruleset.standard.rules.FunctionSignatureRule.Companion.FUNCTION_BODY_EXPRESSION_WRAPPING_PROPERTY
import io.github.ktlint.core.ruleset.standard.rules.FunctionSignatureRule.FunctionBodyExpressionWrapping
import io.github.ktlint.core.ruleset.standard.rules.FunctionSignatureRule.FunctionBodyExpressionWrapping.always
import io.github.ktlint.core.ruleset.standard.rules.FunctionSignatureRule.FunctionBodyExpressionWrapping.default
import io.github.ktlint.core.test.KtLintAssertThat.Companion.EOL_CHAR
import io.github.ktlint.core.test.KtLintAssertThat.Companion.MAX_LINE_LENGTH_MARKER
import io.github.ktlint.core.test.KtLintAssertThat.Companion.assertThatRuleBuilder
import io.github.ktlint.core.test.LintViolation
import io.github.ktlint.core.test.MULTILINE_STRING_QUOTE
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.EnumSource

class FunctionSignatureRuleTest {
    private val functionSignatureWrappingRuleAssertThat =
        assertThatRuleBuilder { FunctionSignatureRule() }
            .addAdditionalRuleProvider { MaxLineLengthRule() }
            .assertThat()

    @Test
    fun `Given a single line function signature which is smaller than or equal to the max line length, and the function is followed by a body block, then do no change the signature`() {
        val code =
            """
            // $MAX_LINE_LENGTH_MARKER            $EOL_CHAR
            fun f(a: Any, b: Any, c: Any): String {
                // body
            }
            """.trimIndent()
        functionSignatureWrappingRuleAssertThat(code)
            .setMaxLineLength()
            .withEditorConfigOverride(FORCE_MULTILINE_WHEN_PARAMETER_COUNT_GREATER_OR_EQUAL_THAN_PROPERTY to "unset")
            .hasNoLintViolations()
    }

    @Test
    fun `Given a single line function signature which is equal to the max line length but missing a space before the opening brace of the body block, and the function is followed by a body block, then do no change the signature`() {
        // Note: the remainder of the tests primarily uses the single expression body as it is a bit more concise than
        // the block expression.
        val code =
            """
            // $MAX_LINE_LENGTH_MARKER              $EOL_CHAR
            fun foo1(a: Any, b: Any, c: Any): String{
                // body
            }

            @Suppress("ktlint:standard:max-line-length")
            fun foo2(a: Any, b: Any, c: Any): String{
                // body
            }
            """.trimIndent()
        val formattedCode =
            """
            // $MAX_LINE_LENGTH_MARKER              $EOL_CHAR
            fun foo1(
                a: Any,
                b: Any,
                c: Any
            ): String {
                // body
            }

            @Suppress("ktlint:standard:max-line-length")
            fun foo2(a: Any, b: Any, c: Any): String {
                // body
            }
            """.trimIndent()
        functionSignatureWrappingRuleAssertThat(code)
            .setMaxLineLength()
            .addAdditionalRuleProvider { FunctionStartOfBodySpacingRule() }
            .withEditorConfigOverride(FORCE_MULTILINE_WHEN_PARAMETER_COUNT_GREATER_OR_EQUAL_THAN_PROPERTY to "4")
            .hasLintViolations(
                LintViolation(2, 10, "Newline expected after opening parenthesis"),
                LintViolation(2, 18, "Parameter should start on a newline"),
                LintViolation(2, 26, "Parameter should start on a newline"),
                LintViolation(2, 32, "Newline expected before closing parenthesis"),
                LintViolation(2, 41, "Expected a single space before body block"),
                LintViolation(7, 41, "Expected a single space before body block"),
            ).isFormattedAs(formattedCode)
    }

    @Test
    fun `Given a single line function signature which is greater then the max line length then reformat to a multiline signature`() {
        // Note: the remainder of the tests primarily uses the single expression body as it is a bit more concise than
        // the block expression.
        val code =
            """
            // $MAX_LINE_LENGTH_MARKER              $EOL_CHAR
            fun foo1(a: Any, b: Any, c: Any): String {
                // body
            }

            @Suppress("ktlint:standard:max-line-length")
            fun foo2(a: Any, b: Any, c: Any): String {
                // body
            }
            """.trimIndent()
        val formattedCode =
            """
            // $MAX_LINE_LENGTH_MARKER              $EOL_CHAR
            fun foo1(
                a: Any,
                b: Any,
                c: Any
            ): String {
                // body
            }

            @Suppress("ktlint:standard:max-line-length")
            fun foo2(a: Any, b: Any, c: Any): String {
                // body
            }
            """.trimIndent()
        functionSignatureWrappingRuleAssertThat(code)
            .setMaxLineLength()
            .withEditorConfigOverride(FORCE_MULTILINE_WHEN_PARAMETER_COUNT_GREATER_OR_EQUAL_THAN_PROPERTY to "unset")
            .hasLintViolations(
                LintViolation(2, 10, "Newline expected after opening parenthesis"),
                LintViolation(2, 18, "Parameter should start on a newline"),
                LintViolation(2, 26, "Parameter should start on a newline"),
                LintViolation(2, 32, "Newline expected before closing parenthesis"),
            ).isFormattedAs(formattedCode)
    }

    @Test
    fun `Given a single line function signature which is smaller than or equal to the max line length, and the function is followed by a body expression, then do not change the signature`() {
        val code =
            """
            // $MAX_LINE_LENGTH_MARKER                  $EOL_CHAR
            fun f(string: String): String = "some-result"
            """.trimIndent()
        functionSignatureWrappingRuleAssertThat(code)
            .setMaxLineLength()
            .hasNoLintViolations()
    }

    @Test
    fun `Given a single line function signature which is smaller than or equal to the max line length, and the function is followed by a body expression which does not fit on that same line, then do not change the signature but wrap the expression body`() {
        val code =
            """
            // $MAX_LINE_LENGTH_MARKER                 $EOL_CHAR
            fun f(string: String): String = "some-result"
            """.trimIndent()
        val formattedCode =
            """
            // $MAX_LINE_LENGTH_MARKER                 $EOL_CHAR
            fun f(string: String): String =
                "some-result"
            """.trimIndent()
        functionSignatureWrappingRuleAssertThat(code)
            .setMaxLineLength()
            .hasLintViolation(2, 33, "Newline expected before expression body")
            .isFormattedAs(formattedCode)
    }

    @Test
    fun `Given a single line function signature and max-line-length is not set then do not rewrite a multiline signature`() {
        val code =
            """
            // No max line length marker!
            fun f(string: String): String = string.uppercase(Locale.getDefault())
            """.trimIndent()
        functionSignatureWrappingRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `Given a single line function signature with a length greater than the max line length then reformat to multiline signature`() {
        val code =
            """
            // $MAX_LINE_LENGTH_MARKER        $EOL_CHAR
            fun f(a: Any, b: Any, c: Any): String = "some-result"
            """.trimIndent()
        val formattedCode =
            """
            // $MAX_LINE_LENGTH_MARKER        $EOL_CHAR
            fun f(
                a: Any,
                b: Any,
                c: Any
            ): String = "some-result"
            """.trimIndent()
        functionSignatureWrappingRuleAssertThat(code)
            .setMaxLineLength()
            .hasLintViolations(
                LintViolation(2, 7, "Newline expected after opening parenthesis"),
                LintViolation(2, 15, "Parameter should start on a newline"),
                LintViolation(2, 23, "Parameter should start on a newline"),
                LintViolation(2, 29, "Newline expected before closing parenthesis"),
            ).isFormattedAs(formattedCode)
    }

    @Test
    fun `Given a single line function signature and first parameter is annotated and function signature has a length greater than the max line length then reformat to multiline signature`() {
        val code =
            """
            // $MAX_LINE_LENGTH_MARKER        $EOL_CHAR
            fun f(@Foo a: Any, b: Any, c: Any): String = "some-result"
            """.trimIndent()
        val formattedCode =
            """
            // $MAX_LINE_LENGTH_MARKER        $EOL_CHAR
            fun f(
                @Foo a: Any,
                b: Any,
                c: Any
            ): String = "some-result"
            """.trimIndent()
        functionSignatureWrappingRuleAssertThat(code)
            .setMaxLineLength()
            .hasLintViolations(
                LintViolation(2, 7, "Newline expected after opening parenthesis"),
                LintViolation(2, 20, "Parameter should start on a newline"),
                LintViolation(2, 28, "Parameter should start on a newline"),
                LintViolation(2, 34, "Newline expected before closing parenthesis"),
            ).isFormattedAs(formattedCode)
    }

    @Test
    fun `Given some function signatures containing at least one comment then do not reformat although the max line length is exceeded`() {
        val code =
            """
            // $MAX_LINE_LENGTH_MARKER                     $EOL_CHAR
            private /* some comment */ fun f1(a: Any, b: Any): String = "some-result"
            private fun /* some comment */ f2(a: Any, b: Any): String = "some-result"
            private fun f3 /* some comment */ (a: Any, b: Any): String = "some-result"
            private fun f5(a /* some comment */: Any, b: Any): String = "some-result"
            private fun f6(a: /* some comment */ Any, b: Any): String = "some-result"
            private fun f7(a: Any /* some comment */, b: Any): String = "some-result"
            private fun f8(a: Any, b: Any) /* some comment */: String = "some-result"
            private fun f9(a: Any, b: Any): /* some comment */ String = "some-result"
            private fun f10(a: Any, b: Any): String /* some comment */ = "some-result"
            """.trimIndent()
        @Suppress("ktlint:standard:max-line-length")
        functionSignatureWrappingRuleAssertThat(code)
            .setMaxLineLength()
            .addAdditionalRuleProvider { ValueParameterCommentRule() }
            .hasLintViolationsForAdditionalRule(
                LintViolation(2, 49, "Exceeded max line length (48)", canBeAutoCorrected = false),
                LintViolation(3, 49, "Exceeded max line length (48)", canBeAutoCorrected = false),
                LintViolation(4, 49, "Exceeded max line length (48)", canBeAutoCorrected = false),
                LintViolation(5, 18, "A comment inside or on same line after a 'value_parameter' is not allowed. It may be placed on a separate line above.", false),
                LintViolation(5, 49, "Exceeded max line length (48)", canBeAutoCorrected = false),
                LintViolation(6, 19, "A comment inside or on same line after a 'value_parameter' is not allowed. It may be placed on a separate line above.", false),
                LintViolation(6, 49, "Exceeded max line length (48)", canBeAutoCorrected = false),
                LintViolation(7, 23, "A comment inside or on same line after a 'value_parameter' is not allowed. It may be placed on a separate line above.", false),
                LintViolation(7, 49, "Exceeded max line length (48)", canBeAutoCorrected = false),
                LintViolation(8, 49, "Exceeded max line length (48)", canBeAutoCorrected = false),
                LintViolation(9, 49, "Exceeded max line length (48)", canBeAutoCorrected = false),
                LintViolation(10, 49, "Exceeded max line length (48)", canBeAutoCorrected = false),
            ).hasNoLintViolationsExceptInAdditionalRules()
    }

    @Test
    fun `Issue 2445 - Given value-parameter-comment rule is disabled or not loaded`() {
        val code =
            """
            private fun f5(a /* some comment */: Any, b: Any): String = "some-result"
            private fun f6(a: /* some comment */ Any, b: Any): String = "some-result"
            private fun f7(a: Any /* some comment */, b: Any): String = "some-result"
            """.trimIndent()
        @Suppress("ktlint:standard:max-line-length")
        functionSignatureWrappingRuleAssertThat(code)
            .addAdditionalRuleProvider { ValueParameterCommentRule() }
            .hasLintViolationsForAdditionalRule(
                LintViolation(1, 18, "A comment inside or on same line after a 'value_parameter' is not allowed. It may be placed on a separate line above.", false),
                LintViolation(2, 19, "A comment inside or on same line after a 'value_parameter' is not allowed. It may be placed on a separate line above.", false),
                LintViolation(3, 23, "A comment inside or on same line after a 'value_parameter' is not allowed. It may be placed on a separate line above.", false),
            ).hasNoLintViolationsExceptInAdditionalRules()
        // When ValueParameterCommentRule is not loaded or disabled:
        functionSignatureWrappingRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `Given a function signature with a newline between the last parameter in the parameter list and the closing parenthesis, but which does not fit on a single line then reformat to a proper multiline signature`() {
        val code =
            """
            // $MAX_LINE_LENGTH_MARKER $EOL_CHAR
            fun f(a: Any, b: Any, c: Any
            ) = "some-result"
            """.trimIndent()
        val formattedCode =
            """
            // $MAX_LINE_LENGTH_MARKER $EOL_CHAR
            fun f(
                a: Any,
                b: Any,
                c: Any
            ) = "some-result"
            """.trimIndent()
        functionSignatureWrappingRuleAssertThat(code)
            .setMaxLineLength()
            .hasLintViolations(
                LintViolation(2, 7, "Newline expected after opening parenthesis"),
                LintViolation(2, 15, "Parameter should start on a newline"),
                LintViolation(2, 23, "Parameter should start on a newline"),
            ).isFormattedAs(formattedCode)
    }

    @Test
    fun `Given a single line function signature with an expression body that is missing the required space just before the expression, but which after adding this spaces no longer fits on a single line, then push the expression body to the next line`() {
        val code =
            """
            // $MAX_LINE_LENGTH_MARKER                 $EOL_CHAR
            fun f(a: Any, b: Any, c: Any) ="some-result"
            """.trimIndent()
        val formattedCode =
            """
            // $MAX_LINE_LENGTH_MARKER                 $EOL_CHAR
            fun f(a: Any, b: Any, c: Any) =
                "some-result"
            """.trimIndent()
        functionSignatureWrappingRuleAssertThat(code)
            .setMaxLineLength()
            .withEditorConfigOverride(FORCE_MULTILINE_WHEN_PARAMETER_COUNT_GREATER_OR_EQUAL_THAN_PROPERTY to "unset")
            .hasLintViolation(2, 32, "Newline expected before expression body")
            .isFormattedAs(formattedCode)
    }

    @Test
    fun `Given a single line function signature with an expression body on the next line which actually fits on the same line as the function signature`() {
        val code =
            """
            // $MAX_LINE_LENGTH_MARKER                  $EOL_CHAR
            fun f(a: Any, b: Any, c: Any) =
                "some-result"
            """.trimIndent()
        val formattedCode =
            """
            // $MAX_LINE_LENGTH_MARKER                  $EOL_CHAR
            fun f(a: Any, b: Any, c: Any) = "some-result"
            """.trimIndent()
        functionSignatureWrappingRuleAssertThat(code)
            .setMaxLineLength()
            .withEditorConfigOverride(FORCE_MULTILINE_WHEN_PARAMETER_COUNT_GREATER_OR_EQUAL_THAN_PROPERTY to "unset")
            .hasLintViolation(2, 32, "First line of body expression fits on same line as function signature")
            .isFormattedAs(formattedCode)
    }

    @Test
    fun `Given a single line function signature returning a lambda which not entire fits on the same line, then push the lambda to the next line`() {
        val code =
            """
            // $MAX_LINE_LENGTH_MARKER                             $EOL_CHAR                                                                                                    $EOL_CHAR
            fun foo(bar: String): (String) -> Boolean = { it == bar }
            """.trimIndent()
        val formattedCode =
            """
            // $MAX_LINE_LENGTH_MARKER                             $EOL_CHAR                                                                                                    $EOL_CHAR
            fun foo(bar: String): (String) -> Boolean =
                { it == bar }
            """.trimIndent()
        functionSignatureWrappingRuleAssertThat(code)
            .setMaxLineLength()
            .hasLintViolation(2, 45, "Newline expected before expression body")
            .isFormattedAs(formattedCode)
    }

    @Test
    fun `Given a single line function signature returning a lambda on the next line which actually fits on the same line as the function signature`() {
        val code =
            """
            // $MAX_LINE_LENGTH_MARKER                              $EOL_CHAR                                                                                                    $EOL_CHAR
            fun foo(bar: String): (String) -> Boolean =
                { it == bar }
            """.trimIndent()
        val formattedCode =
            """
            // $MAX_LINE_LENGTH_MARKER                              $EOL_CHAR                                                                                                    $EOL_CHAR
            fun foo(bar: String): (String) -> Boolean = { it == bar }
            """.trimIndent()
        functionSignatureWrappingRuleAssertThat(code)
            .setMaxLineLength()
            .hasLintViolation(2, 44, "First line of body expression fits on same line as function signature")
            .isFormattedAs(formattedCode)
    }

    @Test
    fun `Given a function without parameters but with at least one space between the parenthesis then reformat to a proper single line signature`() {
        val code =
            """
            fun f( ) = "some-result"
            """.trimIndent()
        val formattedCode =
            """
            fun f() = "some-result"
            """.trimIndent()
        functionSignatureWrappingRuleAssertThat(code)
            .hasLintViolation(1, 7, "No whitespace expected in empty parameter list")
            .isFormattedAs(formattedCode)
    }

    @Test
    fun `Given a function without parameters but with at least one newline between the parenthesis then reformat to a proper single line signature`() {
        val code =
            """
            fun f(

            ) = "some-result"
            """.trimIndent()
        val formattedCode =
            """
            fun f() = "some-result"
            """.trimIndent()
        functionSignatureWrappingRuleAssertThat(code)
            .hasLintViolation(1, 7, "No whitespace expected in empty parameter list")
            .isFormattedAs(formattedCode)
    }

    @Test
    fun `Given a multiline function signature which actually fits on a single line but for which the expression body does not fit at that same line then reformat the signature and wrap the body expression to the next line`() {
        val code =
            """
            // $MAX_LINE_LENGTH_MARKER                    $EOL_CHAR
            // Entire signature is just one character too long to fit on a single line
            // .foo1(a: Any, b: Any, c: Any) = "some-result"
            fun foo1(
                a: Any,
                b: Any,
                c: Any
            ) = "some-result"

            // But thanks to max-line-length suppression, the following signature can be written on a single line
            @Suppress("ktlint:standard:max-line-length")
            fun foo2(
                a: Any,
                b: Any,
                c: Any
            ) = "some-result"
            """.trimIndent()
        val formattedCode =
            """
            // $MAX_LINE_LENGTH_MARKER                    $EOL_CHAR
            // Entire signature is just one character too long to fit on a single line
            // .foo1(a: Any, b: Any, c: Any) = "some-result"
            fun foo1(a: Any, b: Any, c: Any) =
                "some-result"

            // But thanks to max-line-length suppression, the following signature can be written on a single line
            @Suppress("ktlint:standard:max-line-length")
            fun foo2(a: Any, b: Any, c: Any) = "some-result"
            """.trimIndent()
        functionSignatureWrappingRuleAssertThat(code)
            .setMaxLineLength()
            .withEditorConfigOverride(FORCE_MULTILINE_WHEN_PARAMETER_COUNT_GREATER_OR_EQUAL_THAN_PROPERTY to "unset")
            .hasLintViolations(
                LintViolation(5, 5, "No whitespace expected between opening parenthesis and first parameter name"),
                LintViolation(6, 5, "Single whitespace expected before parameter"),
                LintViolation(7, 5, "Single whitespace expected before parameter"),
                LintViolation(7, 11, "No whitespace expected between last parameter and closing parenthesis"),
                LintViolation(8, 5, "Newline expected before expression body"),
                LintViolation(13, 5, "No whitespace expected between opening parenthesis and first parameter name"),
                LintViolation(14, 5, "Single whitespace expected before parameter"),
                LintViolation(15, 5, "Single whitespace expected before parameter"),
                LintViolation(15, 11, "No whitespace expected between last parameter and closing parenthesis"),
            ).isFormattedAs(formattedCode)
    }

    @Test
    fun `Given a class with a multiline function signature which actually fits on a single line but for which the expression body does not fit at that same line then reformat the signature and wrap the body expression to the next line`() {
        val code =
            """
            // $MAX_LINE_LENGTH_MARKER                   $EOL_CHAR
            // Entire signature is just one character too long to fit on a single line
            //  fun(a: Any, b: Any, c: Any) = "some-result"
            class Foo {
                fun f(
                    a: Any,
                    b: Any,
                    c: Any
                ) = "some-result"
            }
            """.trimIndent()
        val formattedCode =
            """
            // $MAX_LINE_LENGTH_MARKER                   $EOL_CHAR
            // Entire signature is just one character too long to fit on a single line
            //  fun(a: Any, b: Any, c: Any) = "some-result"
            class Foo {
                fun f(a: Any, b: Any, c: Any) =
                    "some-result"
            }
            """.trimIndent()
        functionSignatureWrappingRuleAssertThat(code)
            .setMaxLineLength()
            .withEditorConfigOverride(FORCE_MULTILINE_WHEN_PARAMETER_COUNT_GREATER_OR_EQUAL_THAN_PROPERTY to "unset")
            .hasLintViolations(
                LintViolation(6, 9, "No whitespace expected between opening parenthesis and first parameter name"),
                LintViolation(7, 9, "Single whitespace expected before parameter"),
                LintViolation(8, 9, "Single whitespace expected before parameter"),
                LintViolation(8, 15, "No whitespace expected between last parameter and closing parenthesis"),
                LintViolation(9, 9, "Newline expected before expression body"),
            ).isFormattedAs(formattedCode)
    }

    @Nested
    inner class `Given a single line function signature having too many parameters then do reformat as multiline signature` {
        @Test
        fun `Given a single line function signature which is smaller than or equal to the max line length but having too many parameters then do reformat as multiline signature`() {
            val code =
                """
                // $MAX_LINE_LENGTH_MARKER            $EOL_CHAR
                fun f(a: Any, b: Any, c: Any): String {
                    // body
                }
                """.trimIndent()
            val formattedCode =
                """
                // $MAX_LINE_LENGTH_MARKER            $EOL_CHAR
                fun f(
                    a: Any,
                    b: Any,
                    c: Any
                ): String {
                    // body
                }
                """.trimIndent()
            functionSignatureWrappingRuleAssertThat(code)
                .setMaxLineLength()
                .withEditorConfigOverride(FORCE_MULTILINE_WHEN_PARAMETER_COUNT_GREATER_OR_EQUAL_THAN_PROPERTY to 2)
                .hasLintViolations(
                    LintViolation(2, 7, "Newline expected after opening parenthesis"),
                    LintViolation(2, 15, "Parameter should start on a newline"),
                    LintViolation(2, 23, "Parameter should start on a newline"),
                    LintViolation(2, 29, "Newline expected before closing parenthesis"),
                ).isFormattedAs(formattedCode)
        }

        @Test
        fun `Given a single line function signature and max line length not set but having too many parameters then do reformat as multiline signature`() {
            val code =
                """
                fun f(a: Any, b: Any, c: Any): String {
                    // body
                }
                """.trimIndent()
            val formattedCode =
                """
                fun f(
                    a: Any,
                    b: Any,
                    c: Any
                ): String {
                    // body
                }
                """.trimIndent()
            functionSignatureWrappingRuleAssertThat(code)
                .withEditorConfigOverride(FORCE_MULTILINE_WHEN_PARAMETER_COUNT_GREATER_OR_EQUAL_THAN_PROPERTY to 2)
                .hasLintViolations(
                    LintViolation(1, 7, "Newline expected after opening parenthesis"),
                    LintViolation(1, 15, "Parameter should start on a newline"),
                    LintViolation(1, 23, "Parameter should start on a newline"),
                    LintViolation(1, 29, "Newline expected before closing parenthesis"),
                ).isFormattedAs(formattedCode)
        }
    }

    @Test
    fun `Given a function declaration without default implementation in an interface`() {
        val code =
            """
            // $MAX_LINE_LENGTH_MARKER        $EOL_CHAR                                                                                                       $EOL_CHAR
            interface Foo {
                fun foo(a: Any, b: Any): String
            }
            """.trimIndent()
        functionSignatureWrappingRuleAssertThat(code)
            .setMaxLineLength()
            .withEditorConfigOverride(FORCE_MULTILINE_WHEN_PARAMETER_COUNT_GREATER_OR_EQUAL_THAN_PROPERTY to "unset")
            .hasNoLintViolations()
    }

    @Test
    fun `Given an abstract function declaration`() {
        val code =
            """
            // $MAX_LINE_LENGTH_MARKER                 $EOL_CHAR                                                                                                       $EOL_CHAR
            abstract class Foo {
                abstract fun foo(a: Any, b: Any): String
            }
            """.trimIndent()
        functionSignatureWrappingRuleAssertThat(code)
            .setMaxLineLength()
            .withEditorConfigOverride(FORCE_MULTILINE_WHEN_PARAMETER_COUNT_GREATER_OR_EQUAL_THAN_PROPERTY to "unset")
            .hasNoLintViolations()
    }

    // The FunctionSignatureWrappingRule depends on a lot of different rules to do an initial clean up. The tests below
    // ensure that those rules effectively clean up so that the FunctionSignatureWrappingRule does not need to check for
    // it at all.
    @Nested
    inner class CleanUpByRelatedRules {
        @Test
        fun `Given a nullable type with a space before the quest then remove this space`() {
            @Suppress("ktlint:standard:string-template")
            val code =
                """
                fun String$UNEXPECTED_SPACES?.f1() = "some-result"
                fun List<String$UNEXPECTED_SPACES?>.f2() = "some-result"
                fun f3(string: String$UNEXPECTED_SPACES?) = "some-result"
                fun f4(string: List<String$UNEXPECTED_SPACES?>) = "some-result"
                fun f5(): String$UNEXPECTED_SPACES? = "some-result"
                fun f6(): List<String$UNEXPECTED_SPACES?> = listOf("some-result", null)
                fun f7(): List<String>$UNEXPECTED_SPACES? = null
                """.trimIndent()
            val formattedCode =
                """
                fun String?.f1() = "some-result"
                fun List<String?>.f2() = "some-result"
                fun f3(string: String?) = "some-result"
                fun f4(string: List<String?>) = "some-result"
                fun f5(): String? = "some-result"
                fun f6(): List<String?> = listOf("some-result", null)
                fun f7(): List<String>? = null
                """.trimIndent()
            functionSignatureWrappingRuleAssertThat(code)
                .addAdditionalRuleProvider { NullableTypeSpacingRule() }
                .hasNoLintViolationsExceptInAdditionalRules()
                .isFormattedAs(formattedCode)
        }

        @Test
        fun `Given a function signature contains redundant spaces then ensure that those are removed before running the function signature rule`() {
            val code =
                """
                private${UNEXPECTED_SPACES}fun f1() = "some-result"
                fun f2$UNEXPECTED_SPACES() = "some-result"
                fun f3(${UNEXPECTED_SPACES}a: Any) = "some-result"
                fun f4(a$UNEXPECTED_SPACES: Any) = "some-result"
                fun f5(a:${UNEXPECTED_SPACES}Any) = "some-result"
                fun f6(a: Any$UNEXPECTED_SPACES, b: Any) = "some-result"
                fun f7(a: Any,${UNEXPECTED_SPACES}b: Any) = "some-result"
                fun f8(a: Any, b: Any$UNEXPECTED_SPACES) = "some-result"
                fun f9(${UNEXPECTED_SPACES}vararg a: Any) = "some-result"
                fun f10(vararg${UNEXPECTED_SPACES}a: Any) = "some-result"
                fun f11()$UNEXPECTED_SPACES= "some-result"
                fun f12()$UNEXPECTED_SPACES: String = "some-result"
                fun f13():${UNEXPECTED_SPACES}String = "some-result"
                fun f14(): String$UNEXPECTED_SPACES= "some-result"
                fun f15() =$UNEXPECTED_SPACES"some-result"
                fun <${UNEXPECTED_SPACES}T> f16(t: T) = "some-result"
                fun <T>${UNEXPECTED_SPACES}f17(t: T) = "some-result"
                fun <T$UNEXPECTED_SPACES, U> f18(t: T, u: U) = "some-result"
                fun <T,${UNEXPECTED_SPACES}U> f19(t: T, u: U) = "some-result"
                fun <T, U$UNEXPECTED_SPACES> f20(t: T) = "some-result"
                fun Map<${UNEXPECTED_SPACES}Any, Any>.f21(a: Any, b: Any) = "some-result"
                fun Map<Any$UNEXPECTED_SPACES, Any>.f22(a: Any, b: Any) = "some-result"
                fun Map<Any,${UNEXPECTED_SPACES}Any>.f23(a: Any, b: Any) = "some-result"
                fun Map<Any, Any$UNEXPECTED_SPACES>.f24(a: Any, b: Any) = "some-result"
                fun Map<Any, Any>$UNEXPECTED_SPACES.f25(a: Any, b: Any) = "some-result"
                fun f26(block: (${UNEXPECTED_SPACES}T) -> String) = "some-result"
                fun f27(block: (T$UNEXPECTED_SPACES) -> String) = "some-result"
                fun f28(block: (T)$UNEXPECTED_SPACES-> String) = "some-result"
                fun f29(block: (T) ->${UNEXPECTED_SPACES}String) = "some-result"
                """.trimIndent()
            val formattedCode =
                """
                private fun f1() = "some-result"
                fun f2() = "some-result"
                fun f3(a: Any) = "some-result"
                fun f4(a: Any) = "some-result"
                fun f5(a: Any) = "some-result"
                fun f6(a: Any, b: Any) = "some-result"
                fun f7(a: Any, b: Any) = "some-result"
                fun f8(a: Any, b: Any) = "some-result"
                fun f9(vararg a: Any) = "some-result"
                fun f10(vararg a: Any) = "some-result"
                fun f11() = "some-result"
                fun f12(): String = "some-result"
                fun f13(): String = "some-result"
                fun f14(): String = "some-result"
                fun f15() = "some-result"
                fun <T> f16(t: T) = "some-result"
                fun <T> f17(t: T) = "some-result"
                fun <T, U> f18(t: T, u: U) = "some-result"
                fun <T, U> f19(t: T, u: U) = "some-result"
                fun <T, U> f20(t: T) = "some-result"
                fun Map<Any, Any>.f21(a: Any, b: Any) = "some-result"
                fun Map<Any, Any>.f22(a: Any, b: Any) = "some-result"
                fun Map<Any, Any>.f23(a: Any, b: Any) = "some-result"
                fun Map<Any, Any>.f24(a: Any, b: Any) = "some-result"
                fun Map<Any, Any>.f25(a: Any, b: Any) = "some-result"
                fun f26(block: (T) -> String) = "some-result"
                fun f27(block: (T) -> String) = "some-result"
                fun f28(block: (T) -> String) = "some-result"
                fun f29(block: (T) -> String) = "some-result"
                """.trimIndent()
            functionSignatureWrappingRuleAssertThat(code)
                .addAdditionalRuleProviders(
                    { NoMultipleSpacesRule() },
                    { SpacingAroundAngleBracketsRule() },
                    { SpacingAroundParensRule() },
                    { SpacingAroundDotRule() },
                    { SpacingAroundCommaRule() },
                    { SpacingAroundColonRule() },
                ).withEditorConfigOverride(FORCE_MULTILINE_WHEN_PARAMETER_COUNT_GREATER_OR_EQUAL_THAN_PROPERTY to "unset")
                .hasLintViolations(
                    LintViolation(3, 10, "No whitespace expected between opening parenthesis and first parameter name"),
                    LintViolation(7, 17, "Single whitespace expected before parameter"),
                    LintViolation(8, 22, "No whitespace expected between last parameter and closing parenthesis"),
                    LintViolation(9, 10, "No whitespace expected between opening parenthesis and first parameter name"),
                ).isFormattedAs(formattedCode)
        }

        @Test
        fun `Given a function signature missing required spaces then ensure that those are added before running the function signature rule`() {
            val code =
                """
                fun f1(a:${NO_SPACE}Any) = "some-result"
                fun f2(a: Any,${NO_SPACE}b: Any) = "some-result"
                fun f3()$NO_SPACE= "some-result"
                fun f4():${NO_SPACE}String = "some-result"
                fun f5(): String$NO_SPACE= "some-result"
                fun f6() =$NO_SPACE"some-result"
                fun <T>${NO_SPACE}f7(t: T) = "some-result"
                fun <T,${NO_SPACE}U> f8(t: T, u: U) = "some-result"
                fun Map<Any,${NO_SPACE}Any>.f9(a: Any, b: Any) = "some-result"
                fun f10(block: (T)$NO_SPACE-> String) = "some-result"
                fun f11(block: (T) ->${NO_SPACE}String) = "some-result"
                """.trimIndent()
            val formattedCode =
                """
                fun f1(a: Any) = "some-result"
                fun f2(a: Any, b: Any) = "some-result"
                fun f3() = "some-result"
                fun f4(): String = "some-result"
                fun f5(): String = "some-result"
                fun f6() = "some-result"
                fun <T> f7(t: T) = "some-result"
                fun <T, U> f8(t: T, u: U) = "some-result"
                fun Map<Any, Any>.f9(a: Any, b: Any) = "some-result"
                fun f10(block: (T) -> String) = "some-result"
                fun f11(block: (T) -> String) = "some-result"
                """.trimIndent()
            functionSignatureWrappingRuleAssertThat(code)
                .addAdditionalRuleProviders(
                    { TypeParameterListSpacingRule() },
                    { FunctionStartOfBodySpacingRule() },
                    { FunctionTypeReferenceSpacingRule() },
                    { SpacingAroundColonRule() },
                    { SpacingAroundCommaRule() },
                    { SpacingAroundOperatorsRule() },
                ).withEditorConfigOverride(FORCE_MULTILINE_WHEN_PARAMETER_COUNT_GREATER_OR_EQUAL_THAN_PROPERTY to "unset")
                .hasLintViolation(2, 15, "Single whitespace expected before parameter")
                .isFormattedAs(formattedCode)
        }
    }

    @Nested
    @DisplayName("Given a single line function signature followed by a body expression starting on that same line")
    inner class BodyExpressionOnSameLine {
        @ParameterizedTest(name = "bodyExpressionWrapping: {0}")
        @EnumSource(
            value = FunctionBodyExpressionWrapping::class,
            names = ["default", "multiline"],
        )
        fun `Given that the function signature and a single line body expression body fit on the same line then do not reformat function signature or body expression`(
            bodyExpressionWrapping: FunctionBodyExpressionWrapping,
        ) {
            val code =
                """
                // $MAX_LINE_LENGTH_MARKER                  $EOL_CHAR
                fun f(a: Any, b: Any): String = "some-result"
                """.trimIndent()
            functionSignatureWrappingRuleAssertThat(code)
                .setMaxLineLength()
                .withEditorConfigOverride(FORCE_MULTILINE_WHEN_PARAMETER_COUNT_GREATER_OR_EQUAL_THAN_PROPERTY to "unset")
                .withEditorConfigOverride(FUNCTION_BODY_EXPRESSION_WRAPPING_PROPERTY to bodyExpressionWrapping)
                .hasNoLintViolations()
        }

        @Test
        fun `Given that the function signature and a single line body expression body fit on the same line then do not reformat function signature but move the body expression to a separate line`() {
            val code =
                """
                // $MAX_LINE_LENGTH_MARKER                  $EOL_CHAR
                fun f(a: Any, b: Any): String = "some-result"
                """.trimIndent()
            val formattedCode =
                """
                // $MAX_LINE_LENGTH_MARKER                  $EOL_CHAR
                fun f(a: Any, b: Any): String =
                    "some-result"
                """.trimIndent()
            functionSignatureWrappingRuleAssertThat(code)
                .setMaxLineLength()
                .withEditorConfigOverride(FORCE_MULTILINE_WHEN_PARAMETER_COUNT_GREATER_OR_EQUAL_THAN_PROPERTY to "unset")
                .withEditorConfigOverride(FUNCTION_BODY_EXPRESSION_WRAPPING_PROPERTY to always)
                .addAdditionalRuleProvider { IndentationRule() }
                .hasLintViolation(2, 33, "Newline expected before expression body")
                .isFormattedAs(formattedCode)
        }

        @ParameterizedTest(name = "code style: {0}")
        @EnumSource(value = CodeStyleValue::class)
        fun `Issue 2827 - Given that no max_line_length set, and the function signature is a single line body expression body that does fit on the same line then wrap the body expression to a separate line`(
            codeStyleValue: CodeStyleValue,
        ) {
            val code =
                """
                fun f1(a: Any, b: Any): String = "some-result"
                fun f2(a: Any, b: Any) = "some-result"
                """.trimIndent()
            val formattedCode =
                """
                fun f1(a: Any, b: Any): String =
                    "some-result"
                fun f2(a: Any, b: Any) =
                    "some-result"
                """.trimIndent()
            functionSignatureWrappingRuleAssertThat(code)
                .withEditorConfigOverride(CODE_STYLE_PROPERTY to codeStyleValue)
                .withEditorConfigOverride(FORCE_MULTILINE_WHEN_PARAMETER_COUNT_GREATER_OR_EQUAL_THAN_PROPERTY to "unset")
                .withEditorConfigOverride(FUNCTION_BODY_EXPRESSION_WRAPPING_PROPERTY to always)
                .addAdditionalRuleProvider { IndentationRule() }
                .hasLintViolations(
                    LintViolation(1, 34, "Newline expected before expression body"),
                    LintViolation(2, 26, "Newline expected before expression body"),
                ).isFormattedAs(formattedCode)
        }

        @ParameterizedTest(name = "bodyExpressionWrapping: {0}")
        @EnumSource(
            value = FunctionBodyExpressionWrapping::class,
            names = ["multiline", "always"],
        )
        fun `Given that the function signature and first line of a multiline body expression body fit on the same line then do not reformat the function signature, move the body expression to a separate line`(
            bodyExpressionWrapping: FunctionBodyExpressionWrapping,
        ) {
            val code =
                """
                // $MAX_LINE_LENGTH_MARKER                  $EOL_CHAR
                fun f(a: Any, b: Any): String = "some-result"
                    .uppercase()
                """.trimIndent()
            val formattedCode =
                """
                // $MAX_LINE_LENGTH_MARKER                  $EOL_CHAR
                fun f(a: Any, b: Any): String =
                    "some-result"
                        .uppercase()
                """.trimIndent()
            functionSignatureWrappingRuleAssertThat(code)
                .setMaxLineLength()
                .withEditorConfigOverride(FORCE_MULTILINE_WHEN_PARAMETER_COUNT_GREATER_OR_EQUAL_THAN_PROPERTY to "unset")
                .withEditorConfigOverride(FUNCTION_BODY_EXPRESSION_WRAPPING_PROPERTY to bodyExpressionWrapping)
                .addAdditionalRuleProvider { IndentationRule() }
                .hasLintViolation(2, 33, "Newline expected before expression body")
                .isFormattedAs(formattedCode)
        }
    }

    @Nested
    @DisplayName("Given a multiline function signature followed by a body expression starting on a separate line")
    inner class BodyExpressionOnSeparateLine {
        @ParameterizedTest(name = "bodyExpressionWrapping: {0}")
        @EnumSource(
            value = FunctionBodyExpressionWrapping::class,
            names = ["default", "multiline"],
        )
        fun `Given that the function signature and a single line body expression body fit on the same line then do reformat as single line signature`(
            bodyExpressionWrapping: FunctionBodyExpressionWrapping,
        ) {
            val code =
                """
                // $MAX_LINE_LENGTH_MARKER                  $EOL_CHAR
                fun f(
                    a: Any,
                    b: Any
                ): String =
                    "some-result"
                """.trimIndent()
            val formattedCode =
                """
                // $MAX_LINE_LENGTH_MARKER                  $EOL_CHAR
                fun f(a: Any, b: Any): String = "some-result"
                """.trimIndent()
            functionSignatureWrappingRuleAssertThat(code)
                .setMaxLineLength()
                .withEditorConfigOverride(FORCE_MULTILINE_WHEN_PARAMETER_COUNT_GREATER_OR_EQUAL_THAN_PROPERTY to "unset")
                .withEditorConfigOverride(FUNCTION_BODY_EXPRESSION_WRAPPING_PROPERTY to bodyExpressionWrapping)
                .addAdditionalRuleProvider { IndentationRule() }
                .hasLintViolations(
                    LintViolation(3, 5, "No whitespace expected between opening parenthesis and first parameter name"),
                    LintViolation(4, 5, "Single whitespace expected before parameter"),
                    LintViolation(4, 11, "No whitespace expected between last parameter and closing parenthesis"),
                    LintViolation(5, 12, "First line of body expression fits on same line as function signature"),
                ).isFormattedAs(formattedCode)
        }

        @ParameterizedTest(name = "bodyExpressionWrapping: {0}")
        @EnumSource(
            value = FunctionBodyExpressionWrapping::class,
            names = ["default", "multiline", "always"],
        )
        fun `Given that the function signature and first line of a multi line body expression body do not fit on the same line then do reformat`(
            bodyExpressionWrapping: FunctionBodyExpressionWrapping,
        ) {
            val code =
                """
                // $MAX_LINE_LENGTH_MARKER                 $EOL_CHAR
                fun f(
                    a: Any,
                    b: Any
                ): String = "some-result"
                """.trimIndent()
            val formattedCode =
                """
                // $MAX_LINE_LENGTH_MARKER                 $EOL_CHAR
                fun f(a: Any, b: Any): String =
                    "some-result"
                """.trimIndent()
            functionSignatureWrappingRuleAssertThat(code)
                .setMaxLineLength()
                .withEditorConfigOverride(FORCE_MULTILINE_WHEN_PARAMETER_COUNT_GREATER_OR_EQUAL_THAN_PROPERTY to "unset")
                .withEditorConfigOverride(FUNCTION_BODY_EXPRESSION_WRAPPING_PROPERTY to bodyExpressionWrapping)
                .addAdditionalRuleProvider { IndentationRule() }
                .hasLintViolations(
                    LintViolation(3, 5, "No whitespace expected between opening parenthesis and first parameter name"),
                    LintViolation(4, 5, "Single whitespace expected before parameter"),
                    LintViolation(4, 11, "No whitespace expected between last parameter and closing parenthesis"),
                    LintViolation(5, 13, "Newline expected before expression body"),
                ).isFormattedAs(formattedCode)
        }

        @ParameterizedTest(name = "bodyExpressionWrapping: {0}")
        @EnumSource(
            value = FunctionBodyExpressionWrapping::class,
            names = ["multiline", "always"],
        )
        fun `Given that the function signature and the first line of a multi line body expression body fit on the same line then reformat to single line signature but keep body expression on separate line`(
            bodyExpressionWrapping: FunctionBodyExpressionWrapping,
        ) {
            val code =
                """
                // $MAX_LINE_LENGTH_MARKER                  $EOL_CHAR
                fun f(
                    a: Any,
                    b: Any
                ): String =
                    "some-result"
                        .trim()
                """.trimIndent()
            val formattedCode =
                """
                // $MAX_LINE_LENGTH_MARKER                  $EOL_CHAR
                fun f(a: Any, b: Any): String =
                    "some-result"
                        .trim()
                """.trimIndent()
            functionSignatureWrappingRuleAssertThat(code)
                .setMaxLineLength()
                .withEditorConfigOverride(FORCE_MULTILINE_WHEN_PARAMETER_COUNT_GREATER_OR_EQUAL_THAN_PROPERTY to "unset")
                .withEditorConfigOverride(FUNCTION_BODY_EXPRESSION_WRAPPING_PROPERTY to bodyExpressionWrapping)
                .addAdditionalRuleProvider { IndentationRule() }
                .hasLintViolations(
                    LintViolation(3, 5, "No whitespace expected between opening parenthesis and first parameter name"),
                    LintViolation(4, 5, "Single whitespace expected before parameter"),
                    LintViolation(4, 11, "No whitespace expected between last parameter and closing parenthesis"),
                ).isFormattedAs(formattedCode)
        }

        @ParameterizedTest(name = "bodyExpressionWrapping: {0}")
        @EnumSource(
            value = FunctionBodyExpressionWrapping::class,
            names = ["default"],
        )
        fun `Given that the function signature and first line of a multiline body expression body fit on the same line then do reformat as single line signature`(
            bodyExpressionWrapping: FunctionBodyExpressionWrapping,
        ) {
            val code =
                """
                // $MAX_LINE_LENGTH_MARKER                  $EOL_CHAR
                fun f(
                    a: Any,
                    b: Any
                ): String =
                    "some-result"
                        .uppercase()
                """.trimIndent()
            val formattedCode =
                """
                // $MAX_LINE_LENGTH_MARKER                  $EOL_CHAR
                fun f(a: Any, b: Any): String = "some-result"
                    .uppercase()
                """.trimIndent()
            functionSignatureWrappingRuleAssertThat(code)
                .setMaxLineLength()
                .withEditorConfigOverride(FORCE_MULTILINE_WHEN_PARAMETER_COUNT_GREATER_OR_EQUAL_THAN_PROPERTY to "unset")
                .withEditorConfigOverride(FUNCTION_BODY_EXPRESSION_WRAPPING_PROPERTY to bodyExpressionWrapping)
                .addAdditionalRuleProvider { IndentationRule() }
                .hasLintViolations(
                    LintViolation(3, 5, "No whitespace expected between opening parenthesis and first parameter name"),
                    LintViolation(4, 5, "Single whitespace expected before parameter"),
                    LintViolation(4, 11, "No whitespace expected between last parameter and closing parenthesis"),
                    LintViolation(5, 12, "First line of body expression fits on same line as function signature"),
                ).isFormattedAs(formattedCode)
        }

        @ParameterizedTest(name = "bodyExpressionWrapping: {0}")
        @EnumSource(
            value = FunctionBodyExpressionWrapping::class,
            names = ["always"],
        )
        fun `Given that the function signature and first line of a multiline body expression body fit on the same line then do reformat as single line signature, keep the body expression on a separate line`(
            bodyExpressionWrapping: FunctionBodyExpressionWrapping,
        ) {
            val code =
                """
                // $MAX_LINE_LENGTH_MARKER                  $EOL_CHAR
                fun f(
                    a: Any,
                    b: Any
                ): String =
                    "some-result"
                        .uppercase()
                """.trimIndent()
            val formattedCode =
                """
                // $MAX_LINE_LENGTH_MARKER                  $EOL_CHAR
                fun f(a: Any, b: Any): String =
                    "some-result"
                        .uppercase()
                """.trimIndent()
            functionSignatureWrappingRuleAssertThat(code)
                .setMaxLineLength()
                .withEditorConfigOverride(FORCE_MULTILINE_WHEN_PARAMETER_COUNT_GREATER_OR_EQUAL_THAN_PROPERTY to "unset")
                .withEditorConfigOverride(FUNCTION_BODY_EXPRESSION_WRAPPING_PROPERTY to bodyExpressionWrapping)
                .addAdditionalRuleProvider { IndentationRule() }
                .hasLintViolations(
                    LintViolation(3, 5, "No whitespace expected between opening parenthesis and first parameter name"),
                    LintViolation(4, 5, "Single whitespace expected before parameter"),
                    LintViolation(4, 11, "No whitespace expected between last parameter and closing parenthesis"),
                ).isFormattedAs(formattedCode)
        }
    }

    @ParameterizedTest(name = "bodyExpressionWrapping: {0}")
    @EnumSource(
        value = FunctionBodyExpressionWrapping::class,
        names = ["default", "multiline"],
    )
    fun `Given a multiline function signature without explicit return type and start of body expression on next line then keep first line of body expression body on the same line as the last line of the function signature`(
        bodyExpressionWrapping: FunctionBodyExpressionWrapping,
    ) {
        val code =
            """
            // $MAX_LINE_LENGTH_MARKER       $EOL_CHAR
            fun functionSignatureTooLongForSingleLine(
                a: Any,
                b: Any
            ) =
                "some-result"
                    .uppercase()
            """.trimIndent()
        val formattedCode =
            """
            // $MAX_LINE_LENGTH_MARKER       $EOL_CHAR
            fun functionSignatureTooLongForSingleLine(
                a: Any,
                b: Any
            ) = "some-result"
                .uppercase()
            """.trimIndent()
        functionSignatureWrappingRuleAssertThat(code)
            .setMaxLineLength()
            .withEditorConfigOverride(FUNCTION_BODY_EXPRESSION_WRAPPING_PROPERTY to bodyExpressionWrapping)
            .addAdditionalRuleProvider { IndentationRule() }
            .hasLintViolation(5, 4, "First line of body expression fits on same line as function signature")
            .isFormattedAs(formattedCode)
    }

    @ParameterizedTest(name = "bodyExpressionWrapping: {0}")
    @EnumSource(
        value = FunctionBodyExpressionWrapping::class,
        names = ["default", "multiline"],
    )
    fun `Given a multiline function signature without explicit return type and start of body expression on same line as last line of function signature then do not reformat`(
        bodyExpressionWrapping: FunctionBodyExpressionWrapping,
    ) {
        val code =
            """
            // $MAX_LINE_LENGTH_MARKER       $EOL_CHAR
            fun functionSignatureTooLongForSingleLine(
                a: Any,
                b: Any
            ) = "some-result"
                .uppercase()
            """.trimIndent()
        functionSignatureWrappingRuleAssertThat(code)
            .setMaxLineLength()
            .withEditorConfigOverride(FUNCTION_BODY_EXPRESSION_WRAPPING_PROPERTY to bodyExpressionWrapping)
            .addAdditionalRuleProvider { IndentationRule() }
            .hasNoLintViolationsExceptInAdditionalRules()
    }

    @Test
    fun `Issue 2872 - Given that expression bodies have to be wrapped always, a multiline function signature without explicit return type and start of body expression on same line as last line of function signature then do reformat`() {
        val code =
            """
            // $MAX_LINE_LENGTH_MARKER       $EOL_CHAR
            fun functionSignatureTooLongForSingleLine(
                a: Any,
                b: Any
            ) = "some-result"
                .uppercase()
            """.trimIndent()
        val formattedCode =
            """
            // $MAX_LINE_LENGTH_MARKER       $EOL_CHAR
            fun functionSignatureTooLongForSingleLine(
                a: Any,
                b: Any
            ) =
                "some-result"
                    .uppercase()
            """.trimIndent()
        functionSignatureWrappingRuleAssertThat(code)
            .setMaxLineLength()
            .withEditorConfigOverride(FUNCTION_BODY_EXPRESSION_WRAPPING_PROPERTY to always)
            .addAdditionalRuleProvider { IndentationRule() }
            .hasLintViolation(5, 5, "Newline expected before expression body")
            .isFormattedAs(formattedCode)
    }

    @Nested
    inner class `Given a single line function signature with an annotated parameter` {
        @Test
        fun `Given ktlint_official code style`() {
            val code =
                """
                fun foo(a: Int, @Bar bar: String, b: Int) = "some-result"
                """.trimIndent()
            val formattedCode =
                """
                fun foo(
                    a: Int,
                    @Bar bar: String,
                    b: Int
                ) = "some-result"
                """.trimIndent()
            functionSignatureWrappingRuleAssertThat(code)
                .withEditorConfigOverride(CODE_STYLE_PROPERTY to ktlint_official)
                .hasLintViolations(
                    LintViolation(1, 9, "Newline expected after opening parenthesis"),
                    LintViolation(1, 17, "Parameter should start on a newline"),
                    LintViolation(1, 35, "Parameter should start on a newline"),
                    LintViolation(1, 41, "Newline expected before closing parenthesis"),
                ).isFormattedAs(formattedCode)
        }

        @ParameterizedTest(name = "Code style: {0}")
        @EnumSource(
            value = CodeStyleValue::class,
            mode = EnumSource.Mode.EXCLUDE,
            names = ["ktlint_official"],
        )
        fun `Given non-ktlint_official code style`(codeStyle: CodeStyleValue) {
            val code =
                """
                fun foo(a: Int, @Bar bar: String, b: Int) = "some-result"
                """.trimIndent()
            functionSignatureWrappingRuleAssertThat(code)
                .withEditorConfigOverride(CODE_STYLE_PROPERTY to codeStyle)
                .hasNoLintViolations()
        }
    }

    @Test
    fun `Given a function signature with an annotated parameter and the annotation is on a separate line then reformat it as a multiline signature`() {
        val code =
            """
            // $MAX_LINE_LENGTH_MARKER                $EOL_CHAR
            fun foo(@Bar
                bar: String) = "some-result"
            """.trimIndent()
        val formattedCode =
            """
            // $MAX_LINE_LENGTH_MARKER                $EOL_CHAR
            fun foo(
                @Bar
                bar: String
            ) = "some-result"
            """.trimIndent()
        functionSignatureWrappingRuleAssertThat(code)
            .setMaxLineLength()
            .hasLintViolations(
                LintViolation(2, 9, "Newline expected after opening parenthesis"),
                LintViolation(3, 16, "Newline expected before closing parenthesis"),
            ).isFormattedAs(formattedCode)
    }

    @Test
    fun `Issue 1527 - Given a function signature with an expression body which does not fit on the same line as the signature then do not reformat`() {
        val code =
            """
            // $MAX_LINE_LENGTH_MARKER        $EOL_CHAR
            fun foo(bar: String) =
                "some-result"
            """.trimIndent()
        functionSignatureWrappingRuleAssertThat(code)
            .setMaxLineLength()
            .hasNoLintViolations()
    }

    @Test
    fun `Issue 1690 - Given a function preceded by an annotation array`() {
        val code =
            """
            // $MAX_LINE_LENGTH_MARKER                   $EOL_CHAR
            internal fun foo1(foo1: Foo, foo2: Foo): Foo =
                "foooooooooooooooooooooooooooooooooooooo"

            @Bar
            internal fun foo2(foo1: Foo, foo2: Foo): Foo =
                "foooooooooooooooooooooooooooooooooooooo"

            @[Bar]
            internal fun foo2(foo1: Foo, foo2: Foo): Foo =
                "foooooooooooooooooooooooooooooooooooooo"

            @[Bar1 Bar2 Bar3 Bar4 Bar5 Bar6 Bar7 Bar8 Bar9]
            internal fun foo2(foo1: Foo, foo2: Foo): Foo =
                "foooooooooooooooooooooooooooooooooooooo"

            @[Bar1 // some comment
            Bar2]
            internal fun foo2(foo1: Foo, foo2: Foo): Foo =
                "foooooooooooooooooooooooooooooooooooooo"
            """.trimIndent()
        functionSignatureWrappingRuleAssertThat(code)
            .setMaxLineLength()
            .withEditorConfigOverride(FORCE_MULTILINE_WHEN_PARAMETER_COUNT_GREATER_OR_EQUAL_THAN_PROPERTY to "unset")
            .hasNoLintViolationsExceptInAdditionalRules()
    }

    @Nested
    inner class `Issue 1773 - Given a unction signature without parameters exceeding the max line length` {
        @Test
        fun `Given a function name between backticks and expression body`() {
            val code =
                """
                // $MAX_LINE_LENGTH_MARKER                $EOL_CHAR
                fun `a very long function name as found in a test case`() =
                    "some-result"
                """.trimIndent()
            functionSignatureWrappingRuleAssertThat(code)
                .setMaxLineLength()
                .hasNoLintViolationsExceptInAdditionalRules()
        }

        @Test
        fun `Given a function name not between backticks and expression body`() {
            val code =
                """
                // $MAX_LINE_LENGTH_MARKER                $EOL_CHAR
                fun aVeryLongFunctionNameAsFoundInATestCase() =
                    "some-result"
                """.trimIndent()
            functionSignatureWrappingRuleAssertThat(code)
                .setMaxLineLength()
                .hasNoLintViolationsExceptInAdditionalRules()
        }

        @Test
        fun `Given a function name not between backticks and a return type and body block`() {
            val code =
                """
                // $MAX_LINE_LENGTH_MARKER                        $EOL_CHAR
                fun aVeryLongFunctionNameAsFoundInATestCase(): String {
                    return "some-result"
                }
                """.trimIndent()
            functionSignatureWrappingRuleAssertThat(code)
                .setMaxLineLength()
                .hasNoLintViolationsExceptInAdditionalRules()
        }
    }

    @Nested
    inner class `Property ktlint_function_signature_rule_force_multiline_when_parameter_count_greater_or_equal_than` {
        val propertyMapper = FORCE_MULTILINE_WHEN_PARAMETER_COUNT_GREATER_OR_EQUAL_THAN_PROPERTY.propertyMapper!!

        @ParameterizedTest(name = "Code style: {0}")
        @EnumSource(CodeStyleValue::class)
        fun `Given a null property then the property mapper returns null`(codeStyleValue: CodeStyleValue) {
            val actual = propertyMapper(null, codeStyleValue)

            assertThat(actual).isNull()
        }

        @ParameterizedTest(name = "Code style: {0}")
        @EnumSource(CodeStyleValue::class)
        fun `Given a property which is unset then the property mapper returns max integer which is set as the default value`(
            codeStyleValue: CodeStyleValue,
        ) {
            val property = FORCE_MULTILINE_WHEN_PARAMETER_COUNT_GREATER_OR_EQUAL_THAN_PROPERTY.toPropertyWithValue("unset")

            val actual = propertyMapper(property, codeStyleValue)

            assertThat(actual).isEqualTo(Int.MAX_VALUE)
        }

        @ParameterizedTest(name = "Code style: {0}")
        @EnumSource(CodeStyleValue::class)
        fun `Given a valid string value then the property mapper returns the integer value`(codeStyleValue: CodeStyleValue) {
            val someValue = 123
            val property = FORCE_MULTILINE_WHEN_PARAMETER_COUNT_GREATER_OR_EQUAL_THAN_PROPERTY.toPropertyWithValue(someValue.toString())

            val actual = propertyMapper(property, codeStyleValue)

            assertThat(actual).isEqualTo(someValue)
        }

        @ParameterizedTest(name = "Code style: {0}")
        @EnumSource(CodeStyleValue::class)
        fun `Given a negative value then the property mapper throws and exception`(codeStyleValue: CodeStyleValue) {
            val someNegativeValue = "-1"
            val property = FORCE_MULTILINE_WHEN_PARAMETER_COUNT_GREATER_OR_EQUAL_THAN_PROPERTY.toPropertyWithValue(someNegativeValue)

            assertFailure { propertyMapper(property, codeStyleValue) }
                .isInstanceOf<RuntimeException>()
                .hasMessage(
                    "Property 'ktlint_function_signature_rule_force_multiline_when_parameter_count_greater_or_equal_than' expects a " +
                        "positive integer; found '$someNegativeValue'",
                )
        }

        @ParameterizedTest(name = "Code style: {0}")
        @EnumSource(CodeStyleValue::class)
        fun `Given a value bigger than max integer then the property mapper throws and exception`(codeStyleValue: CodeStyleValue) {
            val someValueBiggerThanMaxInt = (1L + Int.MAX_VALUE).toString()
            val property =
                FORCE_MULTILINE_WHEN_PARAMETER_COUNT_GREATER_OR_EQUAL_THAN_PROPERTY.toPropertyWithValue(someValueBiggerThanMaxInt)

            assertFailure { propertyMapper(property, codeStyleValue) }
                .isInstanceOf<RuntimeException>()
                .hasMessage(
                    "Property 'ktlint_function_signature_rule_force_multiline_when_parameter_count_greater_or_equal_than' expects an " +
                        "integer. The parsed '$someValueBiggerThanMaxInt' is not an integer.",
                )
        }

        @ParameterizedTest(name = "Code style: {0}")
        @EnumSource(CodeStyleValue::class)
        fun `Given a invalid string value then the property mapper returns the integer value`(codeStyleValue: CodeStyleValue) {
            val property =
                FORCE_MULTILINE_WHEN_PARAMETER_COUNT_GREATER_OR_EQUAL_THAN_PROPERTY.toPropertyWithValue("some-invalid-value")

            assertFailure { propertyMapper(property, codeStyleValue) }
                .isInstanceOf<RuntimeException>()
                .hasMessage(
                    "Property 'ktlint_function_signature_rule_force_multiline_when_parameter_count_greater_or_equal_than' expects an " +
                        "integer. The parsed 'some-invalid-value' is not an integer.",
                )
        }

        @ParameterizedTest(name = "Input value: {0}, output value: {1}")
        @CsvSource(
            value = [
                "1, 1",
                "${Int.MAX_VALUE}, unset",
            ],
        )
        fun `Given a property with an integer value than write that property`(
            inputValue: Int,
            expectedOutputValue: String,
        ) {
            val actual = FORCE_MULTILINE_WHEN_PARAMETER_COUNT_GREATER_OR_EQUAL_THAN_PROPERTY.propertyWriter(inputValue)

            assertThat(actual).isEqualTo(expectedOutputValue)
        }
    }

    @Test
    fun `Given the ktlint_official code style then avoid wrapping of parameters by overriding property ktlint_function_signature_rule_force_multiline_when_parameter_count_greater_or_equal_than`() {
        val code =
            """
            fun f(a: Any, b: Any, c: Any): String {
                // body
            }
            """.trimIndent()
        functionSignatureWrappingRuleAssertThat(code)
            .withEditorConfigOverride(CODE_STYLE_PROPERTY to ktlint_official)
            .withEditorConfigOverride(FORCE_MULTILINE_WHEN_PARAMETER_COUNT_GREATER_OR_EQUAL_THAN_PROPERTY to "unset")
            .hasNoLintViolations()
    }

    @Test
    fun `Issue 2043 - Given a function signature with an expression body that is an annotated expression then do not reformat to single line function signature`() {
        val code =
            """
            // $MAX_LINE_LENGTH_MARKER                                                    $EOL_CHAR
            fun foo1(bar: String): String =
                @Bar
                bar
            fun foo2(bar: String): String =
                @Bar bar
            """.trimIndent()
        functionSignatureWrappingRuleAssertThat(code)
            .setMaxLineLength()
            .withEditorConfigOverride(FUNCTION_BODY_EXPRESSION_WRAPPING_PROPERTY to default)
            .hasNoLintViolations()
    }

    @Test
    fun `Issue 2592 - Given a function signature with an expression body that is multiline raw string literal then do not join the first leaf with the function signature`() {
        val code =
            """
            // $MAX_LINE_LENGTH_MARKER        $EOL_CHAR
            fun foo1(): String =
                $MULTILINE_STRING_QUOTE
                some text
                $MULTILINE_STRING_QUOTE.trimIndent()
            fun foo2() =
                $MULTILINE_STRING_QUOTE
                some text
                $MULTILINE_STRING_QUOTE.trimIndent()
            """.trimIndent()
        functionSignatureWrappingRuleAssertThat(code)
            .setMaxLineLength()
            .withEditorConfigOverride(FUNCTION_BODY_EXPRESSION_WRAPPING_PROPERTY to default)
            .hasNoLintViolations()
    }

    @Test
    fun `Issue 2592 - Given a multiline function signature with an expression body that is multiline raw string literal then do join the first leaf with the function signature`() {
        val code =
            """
            // $MAX_LINE_LENGTH_MARKER        $EOL_CHAR
            fun foo(
                foo: Foo,
                bar: Bar,
            ) =
                $MULTILINE_STRING_QUOTE
                some text
                $MULTILINE_STRING_QUOTE.trimIndent()
            """.trimIndent()
        val formattedCode =
            """
            // $MAX_LINE_LENGTH_MARKER        $EOL_CHAR
            fun foo(
                foo: Foo,
                bar: Bar,
            ) = $MULTILINE_STRING_QUOTE
                some text
                $MULTILINE_STRING_QUOTE.trimIndent()
            """.trimIndent()
        functionSignatureWrappingRuleAssertThat(code)
            .setMaxLineLength()
            .withEditorConfigOverride(FUNCTION_BODY_EXPRESSION_WRAPPING_PROPERTY to default)
            .hasLintViolation(5, 4, "First line of body expression fits on same line as function signature")
            .isFormattedAs(formattedCode)
    }

    @Nested
    inner class `Issue 2800 - Given a function signature with a context receiver` {
        @Test
        fun `Issue 2800 - Given context receiver on a separate line then ignore the context receiver before rewriting the signature`() {
            val code =
                """
                // $MAX_LINE_LENGTH_MARKER           $EOL_CHAR
                context(Foo)
                fun barrrrrrrrrrrrrr(string: String) {
                   println(string)
                }
                """.trimIndent()
            functionSignatureWrappingRuleAssertThat(code)
                .setMaxLineLength()
                .hasNoLintViolations()
        }

        @Test
        fun `Issue 2800 - Given context receiver on a separate line, followed by a comment on separate line then ignore the context receiver and comment before rewriting the signature`() {
            val code =
                """
                // $MAX_LINE_LENGTH_MARKER           $EOL_CHAR
                context(Foo)
                // some comment
                fun barrrrrrrrrrrrrr(string: String) {
                   println(string)
                }
                """.trimIndent()
            functionSignatureWrappingRuleAssertThat(code)
                .setMaxLineLength()
                .hasNoLintViolations()
        }

        @Test
        fun `Issue 2800 - Given context receiver on a separate line, followed by an EOL comment then ignore the context receiver and comment before rewriting the signature`() {
            val code =
                """
                // $MAX_LINE_LENGTH_MARKER           $EOL_CHAR
                context(Foo) // some comment
                fun barrrrrrrrrrrrrr(string: String) {
                   println(string)
                }
                """.trimIndent()
            functionSignatureWrappingRuleAssertThat(code)
                .setMaxLineLength()
                .hasNoLintViolations()
        }

        @Test
        fun `Issue 2800 - Given context receiver on same line then take the context receiver into account when rewriting the signature`() {
            // Normally the
            val code =
                """
                // $MAX_LINE_LENGTH_MARKER          $EOL_CHAR
                context(Foo) fun bar(string: String) {
                   println(string)
                }
                """.trimIndent()
            val formattedCode =
                """
                // $MAX_LINE_LENGTH_MARKER          $EOL_CHAR
                context(Foo) fun bar(
                    string: String
                ) {
                   println(string)
                }
                """.trimIndent()
            functionSignatureWrappingRuleAssertThat(code)
                .setMaxLineLength()
                .isFormattedAs(formattedCode)
                .hasLintViolations(
                    LintViolation(2, 22, "Newline expected after opening parenthesis"),
                    LintViolation(2, 36, "Newline expected before closing parenthesis"),
                ).isFormattedAs(formattedCode)
        }
    }

    @Nested
    inner class `Issue 3018 - Given a function signature with a context parameter` {
        @Test
        fun `Issue 2800 - Given context parameter on a separate line then ignore the context parameter before rewriting the signature`() {
            val code =
                """
                // $MAX_LINE_LENGTH_MARKER           $EOL_CHAR
                context(_: Foo)
                fun barrrrrrrrrrrrrr(string: String) {
                   println(string)
                }
                """.trimIndent()
            functionSignatureWrappingRuleAssertThat(code)
                .setMaxLineLength()
                .hasNoLintViolations()
        }

        @Test
        fun `Issue 2800 - Given context parameter on a separate line, followed by a comment on separate line then ignore the context parameter and comment before rewriting the signature`() {
            val code =
                """
                // $MAX_LINE_LENGTH_MARKER           $EOL_CHAR
                context(_: Foo)
                // some comment
                fun barrrrrrrrrrrrrr(string: String) {
                   println(string)
                }
                """.trimIndent()
            functionSignatureWrappingRuleAssertThat(code)
                .setMaxLineLength()
                .hasNoLintViolations()
        }

        @Test
        fun `Issue 2800 - Given context parameter on a separate line, followed by an EOL comment then ignore the context parameter and comment before rewriting the signature`() {
            val code =
                """
                // $MAX_LINE_LENGTH_MARKER           $EOL_CHAR
                context(_: Foo) // some comment
                fun barrrrrrrrrrrrrr(string: String) {
                   println(string)
                }
                """.trimIndent()
            functionSignatureWrappingRuleAssertThat(code)
                .setMaxLineLength()
                .hasNoLintViolations()
        }

        @Test
        fun `Issue 2800 - Given context parameter on same line then take the context parameter into account when rewriting the signature`() {
            // Normally the
            val code =
                """
                // $MAX_LINE_LENGTH_MARKER          $EOL_CHAR
                context(_: Foo) fun bar(string: String) {
                   println(string)
                }
                """.trimIndent()
            val formattedCode =
                """
                // $MAX_LINE_LENGTH_MARKER          $EOL_CHAR
                context(_: Foo) fun bar(
                    string: String
                ) {
                   println(string)
                }
                """.trimIndent()
            functionSignatureWrappingRuleAssertThat(code)
                .setMaxLineLength()
                .isFormattedAs(formattedCode)
                .hasLintViolations(
                    LintViolation(2, 25, "Newline expected after opening parenthesis"),
                    LintViolation(2, 39, "Newline expected before closing parenthesis"),
                ).isFormattedAs(formattedCode)
        }
    }

    private companion object {
        const val UNEXPECTED_SPACES = "  "
        const val NO_SPACE = ""
    }
}
