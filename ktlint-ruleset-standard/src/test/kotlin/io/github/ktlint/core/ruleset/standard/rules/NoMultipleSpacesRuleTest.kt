package io.github.ktlint.core.ruleset.standard.rules

import io.github.ktlint.core.test.KtLintAssertThat.Companion.assertThatRule
import io.github.ktlint.core.test.SPACE
import io.github.ktlint.core.test.TAB
import org.junit.jupiter.api.Test

class NoMultipleSpacesRuleTest {
    private val noMultipleSpacesRuleAssertThat = assertThatRule { NoMultipleSpacesRule() }

    @Test
    fun `Given a whitespace element not being an indent containing multiple spaces then replace it with a single space`() {
        val code =
            """
            fun main() {
                x(1,$SPACE${SPACE}3)
            }
            """.trimIndent()
        val formattedCode =
            """
            fun main() {
                x(1,${SPACE}3)
            }
            """.trimIndent()
        noMultipleSpacesRuleAssertThat(code)
            .hasLintViolation(2, 10, "Unnecessary long whitespace")
            .isFormattedAs(formattedCode)
    }

    @Test
    fun `Given a whitespace element containing multiple tabs then replace it with a single space`() {
        val code =
            """
            fun main() {
                x(1,$TAB${TAB}3)
                val fooBar = "Foo$TAB${TAB}Bar"
            }
            """.trimIndent()
        val formattedCode =
            """
            fun main() {
                x(1,${SPACE}3)
                val fooBar = "Foo$TAB${TAB}Bar"
            }
            """.trimIndent()
        noMultipleSpacesRuleAssertThat(code)
            .hasLintViolation(2, 10, "Unnecessary long whitespace")
            .isFormattedAs(formattedCode)
    }

    @Test
    fun `Given KDoc with multiple consecutive spaces in kdoc then do not return lint errors`() {
        val code =
            """
            /**
             * Gets Blabla from user.
             *
             * @param blabls12      1234
             * @param blabla123     6789
             * @param blabla        5678
             * @param longparam345  4567890
             * @param userId        567890
             * @return the user profile
             *
             */
            """.trimIndent()
        noMultipleSpacesRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `test multiple spaces in the beginning of the file`() {
        val code = "  package my.company.android"
        // The formatted code still has an unwanted space at the beginning. However, from the perspective of this rule,
        // it is correct.
        val formattedCode = " package my.company.android"
        noMultipleSpacesRuleAssertThat(code)
            .hasLintViolation(1, 2, "Unnecessary long whitespace")
            .isFormattedAs(formattedCode)
    }

    @Test
    fun `Given a line having one or more trailing spaces then ignore the trailing spaces as they are handled by the no-trailing-spaces rule`() {
        val code =
            """
            class Foo1 {$SPACE
                fun foo1() = 42
            }
            class Foo2 {$SPACE$SPACE
                fun foo2() = 42
            }
            """.trimIndent()
        noMultipleSpacesRuleAssertThat(code).hasNoLintViolations()
    }
}
