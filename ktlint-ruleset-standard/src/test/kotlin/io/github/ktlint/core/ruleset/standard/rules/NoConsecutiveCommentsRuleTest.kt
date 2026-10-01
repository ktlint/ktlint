package io.github.ktlint.core.ruleset.standard.rules

import io.github.ktlint.core.rule.engine.core.api.editorconfig.CODE_STYLE_PROPERTY
import io.github.ktlint.core.rule.engine.core.api.editorconfig.CodeStyleValue
import io.github.ktlint.core.rule.engine.core.api.editorconfig.CodeStyleValue.KTLINT_OFFICIAL
import io.github.ktlint.core.rule.engine.core.api.editorconfig.RuleExecution
import io.github.ktlint.core.rule.engine.core.api.editorconfig.createRuleExecutionEditorConfigProperty
import io.github.ktlint.core.test.KtLintAssertThat.Companion.assertThatRule
import io.github.ktlint.core.test.LintViolation
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource

class NoConsecutiveCommentsRuleTest {
    private val noConsecutiveBlankLinesRuleAssertThat = assertThatRule { NoConsecutiveCommentsRule() }

    @ParameterizedTest(name = "Code style: {0}")
    @EnumSource(
        value = CodeStyleValue::class,
        mode = EnumSource.Mode.EXCLUDE,
        names = ["KTLINT_OFFICIAL"],
    )
    fun `Given a code style other than ktlint_official and some consecutive block comments then do no report a violation`(
        codeStyle: CodeStyleValue,
    ) {
        val code =
            """
            // EOL comment
            /* Block comment 1 */
            /** KDoc 1 */
            /* Block comment 2 */
            """.trimIndent()
        noConsecutiveBlankLinesRuleAssertThat(code)
            .withEditorConfigOverride(CODE_STYLE_PROPERTY to codeStyle)
            .hasNoLintViolations()
    }

    @ParameterizedTest(name = "Code style: {0}")
    @EnumSource(
        value = CodeStyleValue::class,
        mode = EnumSource.Mode.EXCLUDE,
        names = ["KTLINT_OFFICIAL"],
    )
    fun `Given a code style other than ktlint_official, and the rule has been enabled explicitly, and some consecutive block comments then do report a violation`(
        codeStyle: CodeStyleValue,
    ) {
        val code =
            """
            // EOL comment
            /* Block comment 1 */
            /** KDoc 1 */
            /* Block comment 2 */
            """.trimIndent()
        @Suppress("ktlint:standard:max-line-length")
        noConsecutiveBlankLinesRuleAssertThat(code)
            .withEditorConfigOverride(CODE_STYLE_PROPERTY to codeStyle)
            .withEditorConfigOverride(NO_CONSECUTIVE_COMMENTS_RULE_ID.createRuleExecutionEditorConfigProperty() to RuleExecution.ENABLED)
            .hasLintViolationsWithoutAutoCorrect(
                LintViolation(2, 1, "a block comment may not be preceded by an EOL comment unless separated by a blank line"),
                LintViolation(3, 1, "a KDoc may not be preceded by a block comment unless separated by a blank line"),
                LintViolation(4, 1, "a block comment may not be preceded by a KDoc. Reversed order is allowed though when separated by a newline."),
            )
    }

    @Test
    fun `Given some consecutive block comments then report a violation`() {
        val code =
            """
            /* Block comment 1 */
            /* Block comment 2 */

            /* Block comment 3 */
            /* Block comment 4 */ /* Block comment 5 */
            /* Block comment 6 *//* Block comment 7 */
            """.trimIndent()
        noConsecutiveBlankLinesRuleAssertThat(code)
            .withEditorConfigOverride(CODE_STYLE_PROPERTY to KTLINT_OFFICIAL)
            .hasLintViolationsWithoutAutoCorrect(
                LintViolation(2, 1, "a block comment may not be preceded by a block comment"),
                LintViolation(4, 1, "a block comment may not be preceded by a block comment"),
                LintViolation(5, 1, "a block comment may not be preceded by a block comment"),
                LintViolation(5, 23, "a block comment may not be preceded by a block comment"),
                LintViolation(6, 1, "a block comment may not be preceded by a block comment"),
                LintViolation(6, 22, "a block comment may not be preceded by a block comment"),
            )
    }

    @Test
    fun `Given some consecutive KDocs then report a violation`() {
        val code =
            """
            /** KDoc 1 */
            /** KDoc 2 */

            /** KDoc 3 */
            /** KDoc 4 */ /** KDoc 5 */
            /** KDoc 6 *//** KDoc 7 */
            """.trimIndent()
        noConsecutiveBlankLinesRuleAssertThat(code)
            .withEditorConfigOverride(CODE_STYLE_PROPERTY to KTLINT_OFFICIAL)
            .hasLintViolationsWithoutAutoCorrect(
                LintViolation(2, 1, "a KDoc may not be preceded by a KDoc"),
                LintViolation(4, 1, "a KDoc may not be preceded by a KDoc"),
                LintViolation(5, 1, "a KDoc may not be preceded by a KDoc"),
                LintViolation(5, 15, "a KDoc may not be preceded by a KDoc"),
                LintViolation(6, 1, "a KDoc may not be preceded by a KDoc"),
                LintViolation(6, 14, "a KDoc may not be preceded by a KDoc"),
            )
    }

    @Nested
    inner class `Given a KDoc and block comment` {
        @Test
        fun `Given a KDoc followed by a block comment then report a violation`() {
            val code =
                """
                /** KDoc */
                /* Block comment */
                """.trimIndent()
            @Suppress("ktlint:standard:max-line-length")
            noConsecutiveBlankLinesRuleAssertThat(code)
                .withEditorConfigOverride(CODE_STYLE_PROPERTY to KTLINT_OFFICIAL)
                .hasLintViolationWithoutAutoCorrect(2, 1, "a block comment may not be preceded by a KDoc. Reversed order is allowed though when separated by a newline.")
        }

        @Test
        fun `Given a block comment followed by a KDoc then report a violation`() {
            val code =
                """
                /* Block comment */
                /** KDoc */
                """.trimIndent()
            noConsecutiveBlankLinesRuleAssertThat(code)
                .withEditorConfigOverride(CODE_STYLE_PROPERTY to KTLINT_OFFICIAL)
                .hasLintViolationWithoutAutoCorrect(2, 1, "a KDoc may not be preceded by a block comment unless separated by a blank line")
        }

        @Test
        fun `Given a block comment followed by a blank line and a KDoc then do not report a violation`() {
            val code =
                """
                /* Block comment */

                /** KDoc */
                """.trimIndent()
            noConsecutiveBlankLinesRuleAssertThat(code)
                .withEditorConfigOverride(CODE_STYLE_PROPERTY to KTLINT_OFFICIAL)
                .hasNoLintViolations()
        }
    }

    @Nested
    inner class `Given a KDoc and EOL comment` {
        @Test
        fun `Given a KDoc followed by an EOL comment then report a violation`() {
            val code =
                """
                /** KDoc */
                // EOL comment
                """.trimIndent()
            @Suppress("ktlint:standard:max-line-length")
            noConsecutiveBlankLinesRuleAssertThat(code)
                .withEditorConfigOverride(CODE_STYLE_PROPERTY to KTLINT_OFFICIAL)
                .hasLintViolationWithoutAutoCorrect(2, 1, "an EOL comment may not be preceded by a KDoc. Reversed order is allowed though when separated by a newline.")
        }

        @Test
        fun `Given an EOL comment followed by a KDoc then report a violation`() {
            val code =
                """
                // EOL comment
                /** KDoc */
                """.trimIndent()
            noConsecutiveBlankLinesRuleAssertThat(code)
                .withEditorConfigOverride(CODE_STYLE_PROPERTY to KTLINT_OFFICIAL)
                .hasLintViolationWithoutAutoCorrect(2, 1, "a KDoc may not be preceded by an EOL comment unless separated by a blank line")
        }

        @Test
        fun `Given an EOL comment followed by a blank line and a KDoc then report a violation`() {
            val code =
                """
                // EOL comment

                /** KDoc */
                """.trimIndent()
            noConsecutiveBlankLinesRuleAssertThat(code)
                .withEditorConfigOverride(CODE_STYLE_PROPERTY to KTLINT_OFFICIAL)
                .hasNoLintViolations()
        }
    }

    @Nested
    inner class `Given a block and EOL comment` {
        @Test
        fun `Given an EOL comment followed by a block comment then report a violation`() {
            val code =
                """
                // EOL comment
                /* Block comment */
                """.trimIndent()
            @Suppress("ktlint:standard:max-line-length")
            noConsecutiveBlankLinesRuleAssertThat(code)
                .withEditorConfigOverride(CODE_STYLE_PROPERTY to KTLINT_OFFICIAL)
                .hasLintViolationWithoutAutoCorrect(2, 1, "a block comment may not be preceded by an EOL comment unless separated by a blank line")
        }

        @Test
        fun `Given an EOL comment followed by a blank line and a block comment then report a violation`() {
            val code =
                """
                // EOL comment

                /* Block comment */
                """.trimIndent()
            noConsecutiveBlankLinesRuleAssertThat(code)
                .withEditorConfigOverride(CODE_STYLE_PROPERTY to KTLINT_OFFICIAL)
                .hasNoLintViolations()
        }

        @Test
        fun `Given a block comment followed by an EOL comment then report a violation`() {
            val code =
                """
                /* Block comment */
                // EOL comment
                """.trimIndent()
            @Suppress("ktlint:standard:max-line-length")
            noConsecutiveBlankLinesRuleAssertThat(code)
                .withEditorConfigOverride(CODE_STYLE_PROPERTY to KTLINT_OFFICIAL)
                .hasLintViolationWithoutAutoCorrect(2, 1, "an EOL comment may not be preceded by a block comment unless separated by a blank line")
        }

        @Test
        fun `Given a block comment followed by a blank line and an EOL comment then report a violation`() {
            val code =
                """
                /* Block comment */

                // EOL comment
                """.trimIndent()
            noConsecutiveBlankLinesRuleAssertThat(code)
                .withEditorConfigOverride(CODE_STYLE_PROPERTY to KTLINT_OFFICIAL)
                .hasNoLintViolations()
        }
    }

    @Test
    fun `Given an EOL comment followed by another EOL comment then do not report a violation`() {
        val code =
            """
            // val foo = "foo"
            // val bar = "bar"
            """.trimIndent()
        noConsecutiveBlankLinesRuleAssertThat(code)
            .withEditorConfigOverride(CODE_STYLE_PROPERTY to KTLINT_OFFICIAL)
            .hasNoLintViolations()
    }
}
