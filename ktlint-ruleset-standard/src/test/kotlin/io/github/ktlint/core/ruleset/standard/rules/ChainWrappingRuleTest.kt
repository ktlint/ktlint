package io.github.ktlint.core.ruleset.standard.rules

import io.github.ktlint.core.test.KtLintAssertThat.Companion.assertThatRule
import io.github.ktlint.core.test.LintViolation
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class ChainWrappingRuleTest {
    private val chainWrappingRuleAssertThat = assertThatRule { ChainWrappingRule() }

    @Test
    fun `Given some method chain which is incorrectly wrapped`() {
        val code =
            """
            val foo = listOf(1, 2, 3).
                filter { it > 2 }!!.
                takeIf { it.count() > 100 }?.
                sum()
            """.trimIndent()
        val formattedCode =
            """
            val foo = listOf(1, 2, 3)
                .filter { it > 2 }!!
                .takeIf { it.count() > 100 }
                ?.sum()
            """.trimIndent()
        chainWrappingRuleAssertThat(code)
            .hasLintViolations(
                LintViolation(1, 26, "Line must not end with \".\""),
                LintViolation(2, 24, "Line must not end with \".\""),
                LintViolation(3, 32, "Line must not end with \"?.\""),
            ).isFormattedAs(formattedCode)
    }

    @Test
    fun `Given a newline after an elvis operator`() {
        val code =
            """
            val foobar = foo() ?:
                bar
            """.trimIndent()
        val formattedCode =
            """
            val foobar = foo()
                ?: bar
            """.trimIndent()
        chainWrappingRuleAssertThat(code)
            .hasLintViolation(1, 20, "Line must not end with \"?:\"")
            .isFormattedAs(formattedCode)
    }

    @ParameterizedTest(name = "Operator: {0}")
    @ValueSource(strings = ["+", "-", "/", "*", "%"])
    fun `Given a newline after an arithmetic operator`(operator: String) {
        val code =
            """
            val foo1 = 1 $operator
                2
            val foo2 = (1 $operator
                2)
            // Invalid Kotlin code
            // val foo3 = 1
            //    $operator 2
            val foo4 = (1
                $operator 2)
            """.trimIndent()
        chainWrappingRuleAssertThat(code)
            .hasLintViolation(9, 5, "Line must not begin with \"$operator\"")
    }

    @Test
    fun `Given a newline before an logical operator`() {
        val code =
            """
            val foo1 = true
                && false
            val foo2 = ("bar" == "Bar")
                || ("bar" == "bar")
            """.trimIndent()
        val formattedCode =
            """
            val foo1 = true &&
                false
            val foo2 = ("bar" == "Bar") ||
                ("bar" == "bar")
            """.trimIndent()
        chainWrappingRuleAssertThat(code)
            .hasLintViolations(
                LintViolation(2, 5, "Line must not begin with \"&&\""),
                LintViolation(4, 5, "Line must not begin with \"||\""),
            ).isFormattedAs(formattedCode)
    }

    @Nested
    inner class `Given an expression containing a negative number` {
        @Test
        fun `Given a newline after an plus operator`() {
            val code =
                """
                val foo = 1 +
                    -2
                """.trimIndent()
            chainWrappingRuleAssertThat(code).hasNoLintViolations()
        }

        @Test
        fun `Given a negative number in when condition`() {
            val code =
                """
                fun foo() = 1
                val foo = when (foo()) {
                    -1 -> "a"
                    0 -> "b"
                    else -> "c"
                }
                """.trimIndent()
            chainWrappingRuleAssertThat(code).hasNoLintViolations()
        }

        @Test
        fun `Given a negative number in if-condition`() {
            val code =
                """
                fun foo() {
                    if (
                      -3 == foo()
                    ) {}
                    if (
                      // comment
                      -3 == foo()
                    ) {}
                    if (
                      /* comment */
                      -3 == foo()
                    ) {}
                }
                """.trimIndent()
            chainWrappingRuleAssertThat(code).hasNoLintViolations()
        }

        @Test
        fun `Given a negative number in result of if-condition`() {
            val code =
                """
                val foo =
                    if (foo())
                        -1
                    else
                        -2
                """.trimIndent()
            chainWrappingRuleAssertThat(code).hasNoLintViolations()
        }

        @Test
        fun `Given a negative number in try catch`() {
            val code =
                """
                val foo =
                    try {
                      foo()
                      -1
                    } catch(e: Exception) {
                      -2
                    }
                """.trimIndent()
            chainWrappingRuleAssertThat(code).hasNoLintViolations()
        }

        @Test
        fun `Given a negative number in a boolean expression`() {
            val code =
                """
                val foo1 =
                    -2 >
                    (2 + 2)
                val foo2 =
                    (2 + 2) >
                    -2
                """.trimIndent()
            chainWrappingRuleAssertThat(code).hasNoLintViolations()
        }
    }

    @Test
    fun `PR 193 - Given comment in chained command`() {
        val code =
            """
            fun main() {
                var x = false // comment
                    && false
                x = false
                    /* comment */
                    // comment
                    && false
                var y = false. // comment
                    call()
                y = false.
                    // comment
                    call()
                y = false. // comment
                    /* comment */
                    call()
            }
            """.trimIndent()
        val formattedCode =
            """
            fun main() {
                var x = false && // comment
                    false
                x = false &&
                    /* comment */
                    // comment
                    false
                var y = false // comment
                    .call()
                y = false
                    // comment
                    .call()
                y = false // comment
                    /* comment */
                    .call()
            }
            """.trimIndent()
        chainWrappingRuleAssertThat(code)
            .hasLintViolations(
                LintViolation(3, 9, "Line must not begin with \"&&\""),
                LintViolation(7, 9, "Line must not begin with \"&&\""),
                LintViolation(8, 18, "Line must not end with \".\""),
                LintViolation(10, 14, "Line must not end with \".\""),
                LintViolation(13, 14, "Line must not end with \".\""),
            ).isFormattedAs(formattedCode)
    }

    @Test
    fun `Issue 1055 - lint elvis operator and comment`() {
        val code =
            """
            fun test(): Int {
                val foo = foo()
                    ?: // Comment
                    return bar()
                return baz()
            }

            fun foo(): Int? = null
            fun bar(): Int = 1
            fun baz(): Int = 2
            """.trimIndent()
        chainWrappingRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `Issue 1130 - format when conditions`() {
        val code =
            """
            fun test(foo: String?, bar: String?, baz: String?) {
                when {
                    foo != null &&
                        bar != null
                        && baz != null -> {
                    }
                    else -> {
                    }
                }
            }
            """.trimIndent()
        val formattedCode =
            """
            fun test(foo: String?, bar: String?, baz: String?) {
                when {
                    foo != null &&
                        bar != null &&
                        baz != null -> {
                    }
                    else -> {
                    }
                }
            }
            """.trimIndent()
        chainWrappingRuleAssertThat(code)
            .hasLintViolation(5, 13, "Line must not begin with \"&&\"")
            .isFormattedAs(formattedCode)
    }

    @Nested
    inner class `Issue 2297 - Given an && starting on new line should not result in malformed AST which causes NPE in multiline-expression rule` {
        @Test
        fun `No EOL comment on previous line`() {
            val code =
                """
                fun foo(): Boolean {
                    return true
                            &&
                        columns.all { col ->
                            false
                        }
                }
                """.trimIndent()
            val formattedCode =
                """
                fun foo(): Boolean {
                    return true &&
                        columns.all { col ->
                            false
                        }
                }
                """.trimIndent()
            chainWrappingRuleAssertThat(code)
                .addAdditionalRuleProvider { MultilineExpressionWrappingRule() }
                .hasLintViolation(3, 13, "Line must not begin with \"&&\"")
                .isFormattedAs(formattedCode)
        }

        @Test
        fun `EOL comment on previous line`() {
            val code =
                """
                fun foo(): Boolean {
                    return true // some comment
                            &&
                        columns.all { col ->
                            false
                        }
                }
                """.trimIndent()
            val formattedCode =
                """
                fun foo(): Boolean {
                    return true && // some comment
                        columns.all { col ->
                            false
                        }
                }
                """.trimIndent()
            chainWrappingRuleAssertThat(code)
                .addAdditionalRuleProvider { MultilineExpressionWrappingRule() }
                .hasLintViolation(3, 13, "Line must not begin with \"&&\"")
                .isFormattedAs(formattedCode)
        }
    }

    @Test
    fun `Given a spread operator starting on a new line`() {
        val code =
            """
            val foo = foo(
                *bar,
            )
            """.trimIndent()
        chainWrappingRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `Given a DOT QUALIFIED EXPRESSION with an EOL comment between the DOT and the CALL EXPRESSION then reformat`() {
        val code =
            """
            var y =
                false
                    . // comment
                    call()
            """.trimIndent()
        val formattedCode =
            """
            var y =
                false
                    // comment
                    .call()
            """.trimIndent()
        chainWrappingRuleAssertThat(code)
            .addAdditionalRuleProvider { ChainMethodContinuationRule() }
            .hasLintViolation(3, 9, "Line must not end with \".\"")
            .isFormattedAs(formattedCode)
    }
}
