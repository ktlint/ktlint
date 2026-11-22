package io.github.ktlint.core.ruleset.standard.rules

import io.github.ktlint.core.test.KtLintAssertThat.Companion.assertThatRule
import io.github.ktlint.core.test.LintViolation
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class FunctionExpressionBodyRuleTest {
    private val functionExpressionBodyRule = assertThatRule { FunctionExpressionBodyRule() }

    @Test
    fun `Given a function body without any statement`() {
        val code =
            """
            fun foo() {
            }
            """.trimIndent()
        functionExpressionBodyRule(code).hasNoLintViolations()
    }

    @Test
    fun `Given a function body with a comment but without any statement`() {
        val code =
            """
            fun foo() {
                // some comment
            }
            """.trimIndent()
        functionExpressionBodyRule(code).hasNoLintViolations()
    }

    @Test
    fun `Given a single line function body with a return statement`() {
        val code =
            """
            fun foo() { return "foo" }
            """.trimIndent()
        val formattedCode =
            """
            fun foo() = "foo"
            """.trimIndent()
        functionExpressionBodyRule(code)
            .hasLintViolation(1, 11, "Function body should be replaced with body expression")
            .isFormattedAs(formattedCode)
    }

    @Test
    fun `Given a function body with a return statement`() {
        val code =
            """
            fun foo() {
                return "foo"
            }
            """.trimIndent()
        val formattedCode =
            """
            fun foo() = "foo"
            """.trimIndent()
        functionExpressionBodyRule(code)
            .addAdditionalRuleProvider { FunctionSignatureRule() }
            .withRerunFormatAfterAutocorrect(
                "The FunctionSignatureRule triggers on the EQ sign, which is already processed when the " +
                    "FunctionExpressionBodyRule is triggered on the BLOCK element.",
            ).hasLintViolation(1, 11, "Function body should be replaced with body expression")
            .isFormattedAs(formattedCode)
    }

    @Test
    fun `Given a function with a return type and a body with a return statement`() {
        val code =
            """
            fun foo(): String {
                return "foo"
            }
            """.trimIndent()
        val formattedCode =
            """
            fun foo(): String = "foo"
            """.trimIndent()
        functionExpressionBodyRule(code)
            .addAdditionalRuleProvider { FunctionSignatureRule() }
            .withRerunFormatAfterAutocorrect(
                "The FunctionSignatureRule triggers on the EQ sign, which is already processed when the " +
                    "FunctionExpressionBodyRule is triggered on the BLOCK element.",
            ).hasLintViolation(1, 19, "Function body should be replaced with body expression")
            .isFormattedAs(formattedCode)
    }

    @Test
    fun `Given a function body with a multiline expression as return statement`() {
        val code =
            """
            fun foo(bar: Boolean) {
                return if (bar) {
                    "bar"
                } else {
                    "foo"
                }
            }
            """.trimIndent()
        val formattedCode =
            """
            fun foo(bar: Boolean) =
                if (bar) {
                    "bar"
                } else {
                    "foo"
                }
            """.trimIndent()
        functionExpressionBodyRule(code)
            .hasLintViolation(1, 23, "Function body should be replaced with body expression")
            .isFormattedAs(formattedCode)
    }

    @ParameterizedTest(name = "Body: {0}")
    @ValueSource(
        strings = [
            """
            fun foo() {
                // some comment
                return "foo"
            }
            """,
            """
            fun foo() {
                return "foo" // some comment
            }
            """,
            """
            fun foo() {
                return "foo"
                // some comment
            }
            """,
            """
            fun foo() {
                val bar = bar()
                return "foo"
            }
            """,
        ],
    )
    fun `Given a function body with a return statement and a comment or other code leaf`(code: String) {
        functionExpressionBodyRule(code.trimIndent()).hasNoLintViolations()
    }

    @Test
    fun `Given a function body with throws expression as only statement then convert to body expression and add Unit return type`() {
        val code =
            """
            fun foo() {
                throw IllegalArgumentException("some message")
            }
            """.trimIndent()
        val formattedCode =
            """
            fun foo(): Unit = throw IllegalArgumentException("some message")
            """.trimIndent()
        functionExpressionBodyRule(code)
            .isFormattedAs(formattedCode)
            .hasLintViolation(1, 11, "Function body should be replaced with body expression")
    }

    @Test
    fun `Given a function with return type and body with throws expression as only statement then convert to body expression and keep original return type`() {
        val code =
            """
            fun foo(): Foo {
                throw IllegalArgumentException("some message")
            }
            """.trimIndent()
        val formattedCode =
            """
            fun foo(): Foo = throw IllegalArgumentException("some message")
            """.trimIndent()
        functionExpressionBodyRule(code)
            .isFormattedAs(formattedCode)
            .hasLintViolation(1, 16, "Function body should be replaced with body expression")
    }

    @Test
    fun `Given a function with a single expression but having multiple return expression inside then do not convert as it results in a compilation error`() {
        val code =
            """
            fun foo(): Any {
                return if (true) {
                    Foo()
                } else {
                    return Bar()
                }
            }
            """.trimIndent()
        functionExpressionBodyRule(code).hasNoLintViolations()
    }

    @Test
    fun `Issue 2394 - Given multiple function bodies with a single return statement`() {
        val code =
            """
            fun foo1(): String {
                return "foo1"
            }
            fun foo2(): String {
                return "foo2"
            }
            """.trimIndent()
        val formattedCode =
            """
            fun foo1(): String = "foo1"
            fun foo2(): String = "foo2"
            """.trimIndent()
        functionExpressionBodyRule(code)
            .addAdditionalRuleProvider { FunctionSignatureRule() }
            .withRerunFormatAfterAutocorrect(
                "The FunctionSignatureRule triggers on the EQ sign, which is already processed when the " +
                    "FunctionExpressionBodyRule is triggered on the BLOCK element.",
            ).hasLintViolations(
                LintViolation(1, 20, "Function body should be replaced with body expression"),
                LintViolation(4, 20, "Function body should be replaced with body expression"),
            ).isFormattedAs(formattedCode)
    }
}
