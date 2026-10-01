package io.github.ktlint.core.ruleset.standard.rules

import io.github.ktlint.core.rule.engine.core.api.editorconfig.CODE_STYLE_PROPERTY
import io.github.ktlint.core.rule.engine.core.api.editorconfig.CodeStyleValue
import io.github.ktlint.core.test.KtLintAssertThat.Companion.assertThatRule
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class NoSingleLineBlockCommentRuleTest {
    private val noSingleLineBlockCommentRuleAssertThat = assertThatRule { NoSingleLineBlockCommentRule() }

    @Test
    fun `Given a single line block comment then replace it with an EOL comment`() {
        val code =
            """
            fun bar() {
                /* Some comment */
            }
            """.trimIndent()
        val formattedCode =
            """
            fun bar() {
                // Some comment
            }
            """.trimIndent()
        noSingleLineBlockCommentRuleAssertThat(code)
            .withEditorConfigOverride(CODE_STYLE_PROPERTY to CodeStyleValue.KTLINT_OFFICIAL)
            .hasLintViolation(2, 5, "Replace the block comment with an EOL comment")
            .isFormattedAs(formattedCode)
    }

    @Test
    fun `Given a multi line block comment that start starts and end on a separate line then do not reformat`() {
        val code =
            """
            /*
             * Some comment
             */
            """.trimIndent()
        noSingleLineBlockCommentRuleAssertThat(code).hasNoLintViolations()
    }

    @Nested
    inner class `Given some code code followed by a block comment on the same line` {
        @Test
        fun `Given a comment followed by a property and separated with space`() {
            val code =
                """
                val foo = "foo" /* Some comment */
                """.trimIndent()
            val formattedCode =
                """
                val foo = "foo" // Some comment
                """.trimIndent()
            noSingleLineBlockCommentRuleAssertThat(code)
                .hasLintViolation(1, 17, "Replace the block comment with an EOL comment")
                .isFormattedAs(formattedCode)
        }

        @Test
        fun `Given a comment followed by a property but not separated with space`() {
            val code =
                """
                val foo = "foo"/* Some comment */
                """.trimIndent()
            val formattedCode =
                """
                val foo = "foo" // Some comment
                """.trimIndent()
            noSingleLineBlockCommentRuleAssertThat(code)
                .hasLintViolation(1, 16, "Replace the block comment with an EOL comment")
                .isFormattedAs(formattedCode)
        }

        @Test
        fun `Given a comment followed by a function and separated with space`() {
            val code =
                """
                fun foo() = "foo" /* Some comment */
                """.trimIndent()
            val formattedCode =
                """
                fun foo() = "foo" // Some comment
                """.trimIndent()
            noSingleLineBlockCommentRuleAssertThat(code)
                .hasLintViolation(1, 19, "Replace the block comment with an EOL comment")
                .isFormattedAs(formattedCode)
        }

        @Test
        fun `Given a comment followed by a function but not separated with space`() {
            val code =
                """
                fun foo() = "foo"/* Some comment */
                """.trimIndent()
            val formattedCode =
                """
                fun foo() = "foo" // Some comment
                """.trimIndent()
            noSingleLineBlockCommentRuleAssertThat(code)
                .hasLintViolation(1, 18, "Replace the block comment with an EOL comment")
                .isFormattedAs(formattedCode)
        }
    }

    @Test
    fun `Given a block comment that does not contain a newline and which is after some code om the same line is changed to an EOL comment`() {
        val code =
            """
            val foo = "foo" /* Some comment */
            """.trimIndent()
        val formattedCode =
            """
            val foo = "foo" // Some comment
            """.trimIndent()
        noSingleLineBlockCommentRuleAssertThat(code)
            .hasLintViolation(1, 17, "Replace the block comment with an EOL comment")
            .isFormattedAs(formattedCode)
    }

    @Test
    fun `Given a single line block containing a block comment then do not reformat`() {
        val code =
            """
            val foo = { /* no-op */ }
            """.trimIndent()
        noSingleLineBlockCommentRuleAssertThat(code)
            .withEditorConfigOverride(CODE_STYLE_PROPERTY to CodeStyleValue.KTLINT_OFFICIAL)
            .hasNoLintViolations()
    }
}
