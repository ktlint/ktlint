package io.github.ktlint.core.ruleset.standard.rules

import io.github.ktlint.core.test.KtLintAssertThat.Companion.EOL_CHAR
import io.github.ktlint.core.test.KtLintAssertThat.Companion.MAX_LINE_LENGTH_MARKER
import io.github.ktlint.core.test.KtLintAssertThat.Companion.assertThatRule
import io.github.ktlint.core.test.KtLintAssertThat.Companion.assertThatRuleBuilder
import io.github.ktlint.core.test.KtlintDocumentationTest
import io.github.ktlint.core.test.LintViolation
import org.junit.jupiter.api.Test

class CallExpressionWrappingRuleTest {
    private val callExpressionWrappingRuleAssertThat =
        assertThatRuleBuilder { CallExpressionWrappingRule() }
            .addAdditionalRuleProvider { MaxLineLengthRule() }
            .assertThat()

    @Test
    fun `Given some call expressions that do fit on the line then do not wrap`() {
        val code =
            """
            // $MAX_LINE_LENGTH_MARKER        $EOL_CHAR
            val foo1 = bar() { "some message" }
            val foo2 = bar() {
                "some message"
            }
            val foo3 = bar() { message ->
                "some message"
            }
            """.trimIndent()
        callExpressionWrappingRuleAssertThat(code)
            .setMaxLineLength()
            .hasNoLintViolations()
    }

    @Test
    fun `Given a single line call expression, and lambda argument with parameter list that fits on the line then do not wrap`() {
        val code =
            """
            // $MAX_LINE_LENGTH_MARKER                  $EOL_CHAR
            val foo = bar() { message -> "some message" }
            """.trimIndent()
        callExpressionWrappingRuleAssertThat(code)
            .setMaxLineLength()
            .hasNoLintViolations()
    }

    @Test
    fun `Given a single line call expression for which the lambda expression does not fits on the line then wrap after opening brace of lambda expression`() {
        val code =
            """
            // $MAX_LINE_LENGTH_MARKER      $EOL_CHAR
            val foo = bar() { "some message" }
            """.trimIndent()
        val formattedCode =
            """
            // $MAX_LINE_LENGTH_MARKER      $EOL_CHAR
            val foo = bar() {
                "some message"
            }
            """.trimIndent()
        callExpressionWrappingRuleAssertThat(code)
            .setMaxLineLength()
            .hasLintViolations(
                LintViolation(2, 17, "Expected new line after '{'"),
                LintViolation(2, 34, "Expected new line before '}'"),
            ).isFormattedAs(formattedCode)
    }

    @Test
    fun `Given a single line call expression, and lambda argument with parameter list, and the function literal of the lambda expression does not fits on the line then wrap after the arrow`() {
        val code =
            """
            // $MAX_LINE_LENGTH_MARKER                 $EOL_CHAR
            val foo = bar() { message -> "some message" }
            """.trimIndent()
        val formattedCode =
            """
            // $MAX_LINE_LENGTH_MARKER                 $EOL_CHAR
            val foo = bar() { message ->
                "some message"
            }
            """.trimIndent()
        callExpressionWrappingRuleAssertThat(code)
            .setMaxLineLength()
            .hasLintViolations(
                LintViolation(2, 28, "Expected new line after '->'"),
                LintViolation(2, 45, "Expected new line before '}'"),
            ).isFormattedAs(formattedCode)
    }

    @Test
    fun `Given a single line call expression, and lambda argument with parameter list, and the arrow of the lambda expression does not fits on the line then wrap after opening brace of lambda expression`() {
        val code =
            """
            // $MAX_LINE_LENGTH_MARKER   $EOL_CHAR
            val foo = barrrr() { message -> "some message" }
            """.trimIndent()
        val formattedCode =
            """
            // $MAX_LINE_LENGTH_MARKER   $EOL_CHAR
            val foo = barrrr() {
                message -> "some message"
            }
            """.trimIndent()
        callExpressionWrappingRuleAssertThat(code)
            .setMaxLineLength()
            .hasLintViolations(
                LintViolation(2, 20, "Expected new line after '{'"),
                LintViolation(2, 48, "Expected new line before '}'"),
            ).isFormattedAs(formattedCode)
    }

    @Test
    fun `Given a single line call expression with argument in the reference expression which does not fits on the line then wrap after opening parenthesis of the reference expression`() {
        val code =
            """
            // $MAX_LINE_LENGTH_MARKER       $EOL_CHAR
            val foo1 = bar("foobarrrrrrrrrrrr") { "some message" }
            @Suppress("ktlint:standard:max-line-length")
            val foo2 = bar("foobarrrrrrrrrrrr") { "some message" }
            """.trimIndent()
        val formattedCode =
            """
            // $MAX_LINE_LENGTH_MARKER       $EOL_CHAR
            val foo1 = bar(
                "foobarrrrrrrrrrrr"
            ) { "some message" }
            @Suppress("ktlint:standard:max-line-length")
            val foo2 = bar("foobarrrrrrrrrrrr") { "some message" }
            """.trimIndent()
        callExpressionWrappingRuleAssertThat(code)
            .setMaxLineLength()
            .hasLintViolations(
                LintViolation(2, 15, "Expected new line after '('"),
                LintViolation(2, 35, "Expected new line before ')'"),
                LintViolation(2, 37, "Expected new line after '{'"),
                LintViolation(2, 54, "Expected new line before '}'"),
            ).isFormattedAs(formattedCode)
    }

    @Test
    fun `Given a single line call expression with an argument value list which does not fits on the line, and a lambda argument that after wrapping also does not fit the line then wrap both the value argument list and the lambda argument`() {
        val code =
            """
            // $MAX_LINE_LENGTH_MARKER      $EOL_CHAR
            val foo = bar("foobarrrrrrrrrrrr") { "some longggggggggg message" }
            """.trimIndent()
        val formattedCode =
            """
            // $MAX_LINE_LENGTH_MARKER      $EOL_CHAR
            val foo = bar(
                "foobarrrrrrrrrrrr"
            ) {
                "some longggggggggg message"
            }
            """.trimIndent()
        callExpressionWrappingRuleAssertThat(code)
            .setMaxLineLength()
            .hasLintViolations(
                LintViolation(2, 14, "Expected new line after '('"),
                LintViolation(2, 34, "Expected new line before ')'"),
                LintViolation(2, 36, "Expected new line after '{'"),
                LintViolation(2, 67, "Expected new line before '}'"),
            ).isFormattedAs(formattedCode)
    }

    @KtlintDocumentationTest
    fun `Given some examples for documentation`() {
        val code =
            """
            // $MAX_LINE_LENGTH_MARKER       $EOL_CHAR
            val foo1 = bar() { "some message" }
            val foo2 = bar("foobarrrrrrrrrrrr") { "some message" }
            val foo3 = bar("foobarrrrrrrrrrrr") { "some longgggggggggg message" }
            """.trimIndent()
        val formattedCode =
            """
            // $MAX_LINE_LENGTH_MARKER       $EOL_CHAR
            val foo1 = bar() {
                "some message"
            }
            val foo2 = bar(
                "foobarrrrrrrrrrrr"
            ) { "some message" }
            val foo3 = bar(
                "foobarrrrrrrrrrrr"
            ) {
                "some longgggggggggg message"
            }
            """.trimIndent()
        callExpressionWrappingRuleAssertThat(code)
            .setMaxLineLength()
            .isFormattedAs(formattedCode)
    }

    @Test
    fun `Given a call expression with a function literal having an empty multiline body block then do not add an addition line break before the closing brace`() {
        val code =
            """
            val foo =
                bar {
                }
            """.trimIndent()
        callExpressionWrappingRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `Given a call expression exceeding max-line-length, but the max-line-length rule is not enabled, then do not wrap`() {
        val code =
            """
            // $MAX_LINE_LENGTH_MARKER      $EOL_CHAR
            fun foo() = bar("barrrrrrrrrrrrr")
            """.trimIndent()
        val callExpressionWrappingRuleAssertThat = assertThatRule { CallExpressionWrappingRule() }
        callExpressionWrappingRuleAssertThat(code)
            .setMaxLineLength()
            .hasNoLintViolations()
    }

    @Test
    fun `Given a call expression inside a string template, ending after the max-line-length then ignore the expression as strings may exceed the max-line-length`() {
        val code =
            $$"""
            // $$MAX_LINE_LENGTH_MARKER                    $$EOL_CHAR
            fun foo() =
                "fooooooooooooooooooooooo ${arrayOf("bar").contentToString()}"
            """.trimIndent()
        callExpressionWrappingRuleAssertThat(code)
            .setMaxLineLength()
            .hasNoLintViolations()
    }
}
