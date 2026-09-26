package io.github.ktlint.core.ruleset.standard.rules

import assertk.assertThat
import assertk.assertions.isEqualTo
import io.github.ktlint.core.rule.engine.core.api.editorconfig.CODE_STYLE_PROPERTY
import io.github.ktlint.core.rule.engine.core.api.editorconfig.CodeStyleValue
import io.github.ktlint.core.rule.engine.core.api.editorconfig.EditorConfig
import io.github.ktlint.core.rule.engine.core.api.editorconfig.MAX_LINE_LENGTH_PROPERTY
import io.github.ktlint.core.rule.engine.core.api.editorconfig.RuleExecution.disabled
import io.github.ktlint.core.rule.engine.core.api.editorconfig.RuleExecution.enabled
import io.github.ktlint.core.rule.engine.core.api.editorconfig.createRuleExecutionEditorConfigProperty
import io.github.ktlint.core.rule.engine.core.api.editorconfig.ec4j.toPropertyWithValue
import io.github.ktlint.core.rule.engine.core.api.editorconfig.ktLintRuleExecutionPropertyName
import io.github.ktlint.core.ruleset.standard.rules.MaxLineLengthRule.Companion.IGNORE_BACKTICKED_IDENTIFIER_PROPERTY
import io.github.ktlint.core.test.KtLintAssertThat.Companion.EOL_CHAR
import io.github.ktlint.core.test.KtLintAssertThat.Companion.MAX_LINE_LENGTH_MARKER
import io.github.ktlint.core.test.KtLintAssertThat.Companion.assertThatRule
import io.github.ktlint.core.test.LintViolation
import io.github.ktlint.core.test.MULTILINE_STRING_QUOTE
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class MaxLineLengthRuleTest {
    private val maxLineLengthRuleAssertThat = assertThatRule { MaxLineLengthRule() }

    @Test
    fun `Given some code that exceeds the max line length then return a lint error`() {
        val code =
            """
            // $MAX_LINE_LENGTH_MARKER                   $EOL_CHAR
            val fooooooooooooooo = "fooooooooooooooooooooo"
            val foooooooooooooo = "foooooooooooooooooooo" // some comment
            """.trimIndent()
        maxLineLengthRuleAssertThat(code)
            .setMaxLineLength()
            .hasLintViolationsWithoutAutoCorrect(
                LintViolation(2, 47, "Exceeded max line length (46)"),
                LintViolation(3, 47, "Exceeded max line length (46)"),
            )
    }

    @Nested
    inner class `Given code that exceeds the max line length but for which no lint error should be returned` {
        @Test
        fun `Given a package statement that that exceeds the max line length then do not return a lint error`() {
            val code =
                """
                // $MAX_LINE_LENGTH_MARKER      $EOL_CHAR
                package com.toooooooooooooooo.long
                """.trimIndent()
            maxLineLengthRuleAssertThat(code)
                .setMaxLineLength()
                .hasNoLintViolations()
        }

        @Test
        fun `Given an import statement that that exceeds the max line length then do not return a lint error`() {
            val code =
                """
                // $MAX_LINE_LENGTH_MARKER      $EOL_CHAR
                import com.toooooooooooooooo.long
                """.trimIndent()
            maxLineLengthRuleAssertThat(code)
                .setMaxLineLength()
                .hasNoLintViolations()
        }

        @Test
        fun `Given a multiline string which exceeds the max line length then do not return a lint error`() {
            val code =
                """
                // $MAX_LINE_LENGTH_MARKER      $EOL_CHAR
                val foo =
                    $MULTILINE_STRING_QUOTE
                    foooooooooooooooooooooooooooooooooo
                    $MULTILINE_STRING_QUOTE
                """.trimIndent()
            maxLineLengthRuleAssertThat(code)
                .setMaxLineLength()
                .hasNoLintViolations()
        }

        @Test
        fun `Given a single line string which exceeds the max line length, and which has no other non-whitespace elements on the same line then do not return a lint error`() {
            val code =
                """
                // $MAX_LINE_LENGTH_MARKER          $EOL_CHAR
                fun foo() {
                    logger.info {
                        "fooooooooooooooooooooooooooo"
                    }
                    logger.info {
                        "foo" + "oooooooooooooooooooo"
                    }
                }
                """.trimIndent()
            maxLineLengthRuleAssertThat(code)
                .setMaxLineLength()
                .hasLintViolationWithoutAutoCorrect(7, 38, "Exceeded max line length (37)")
        }

        @Test
        fun `Given a block comment which exceeds the max line length then do not return a lint error`() {
            val code =
                """
                // $MAX_LINE_LENGTH_MARKER      $EOL_CHAR
                /*
                 * This block comment exceeds the max line length
                 */
                """.trimIndent()
            maxLineLengthRuleAssertThat(code)
                .setMaxLineLength()
                .hasNoLintViolations()
        }

        @Test
        fun `Given a KDoc which exceeds the max line length then do not return a lint error`() {
            val code =
                """
                // $MAX_LINE_LENGTH_MARKER      $EOL_CHAR
                /**
                 * This block comment exceeds the max line length
                 */
                """.trimIndent()
            maxLineLengthRuleAssertThat(code)
                .setMaxLineLength()
                .hasNoLintViolations()
        }
    }

    @Nested
    inner class `Given some error suppression` {
        @Test
        fun `Given code that is suppressed then do no return lint errors for lines in this block`() {
            val code =
                """
                // $MAX_LINE_LENGTH_MARKER                     $EOL_CHAR
                fun foo() {
                    println("teeeeeeeeeeeeeeeeeeeeeeeeeeeeeeext")
                    @Suppress("ktlint:standard:max-line-length")
                    println("teeeeeeeeeeeeeeeeeeeeeeeeeeeeeeext")
                    println("teeeeeeeeeeeeeeeeeeeeeeeeeeeeeeext")
                }
                """.trimIndent()
            maxLineLengthRuleAssertThat(code)
                .setMaxLineLength()
                .hasLintViolationsWithoutAutoCorrect(
                    LintViolation(3, 49, "Exceeded max line length (48)"),
                    LintViolation(6, 49, "Exceeded max line length (48)"),
                )
        }
    }

    @Nested
    inner class `Given a string between backticks` {
        @Test
        fun `Given some text wrapped between backticks which causes the line length to be exceeded then do not return a lint error for that line`() {
            val code =
                """
                // $MAX_LINE_LENGTH_MARKER          $EOL_CHAR
                @Test
                fun `Some too long test description between backticks`() {
                    println("teeeeeeeeeeeeeeeeeeeext")
                }
                """.trimIndent()
            maxLineLengthRuleAssertThat(code)
                .setMaxLineLength()
                .withEditorConfigOverride(IGNORE_BACKTICKED_IDENTIFIER_PROPERTY to true)
                // Note that no error was generated on line 2 with the long fun name but on another line
                .hasLintViolationWithoutAutoCorrect(4, 38, "Exceeded max line length (37)")
        }

        @Test
        fun `Given some text wrapped between backticks which is ignored but still the line length is exceeded with remainder of text then do return a lint error`() {
            val code =
                """
                // $MAX_LINE_LENGTH_MARKER          $EOL_CHAR
                @Test
                fun `Some too long test description between backticks`(looooooooongParameterName: String) {
                    println("teeeeeeeeeeeeeeeeeeeext")
                }
                """.trimIndent()
            maxLineLengthRuleAssertThat(code)
                .setMaxLineLength()
                .withEditorConfigOverride(IGNORE_BACKTICKED_IDENTIFIER_PROPERTY to true)
                .hasLintViolationsWithoutAutoCorrect(
                    // Note that no error was generated on line 2 with the long fun name but on another line
                    LintViolation(3, 38, "Exceeded max line length (37)"),
                    LintViolation(4, 38, "Exceeded max line length (37)"),
                )
        }
    }

    @Test
    fun testLintOff() {
        val code = "// some" + " long ".repeat(100) + "comment" // Total length of line is 7 + 600 + 7 = 614 characters
        maxLineLengthRuleAssertThat(code)
            .withEditorConfigOverride(MAX_LINE_LENGTH_PROPERTY to "off")
            .hasNoLintViolations()
    }

    @Test
    fun `Given a block comment at the start of the file`() {
        val code =
            // The MAX_LINE_LENGTH_MARKER comment can not be used in this test as that comment should be the first line in the code. But for
            // test it is required that the block comment is the first comment in the code.
            """
            /*
             * Some comment for which the individual
             * lines do not exceed the max line, but
             * the total length of the comment does.
             */
            """.trimIndent()
        maxLineLengthRuleAssertThat(code)
            .withEditorConfigOverride(MAX_LINE_LENGTH_PROPERTY to 40)
            .hasNoLintViolations()
    }

    @Test
    fun `Given a line containing a string template followed by comma then do not report it`() {
        val code =
            """
            // $MAX_LINE_LENGTH_MARKER                                      $EOL_CHAR
            fun foo() {
                throw SomeException(
                    "A long exception message followed by a comma-----------",
                    e,
                )
                // or
                throw SomeException(
                    "A long exception message followed by a (trailing) comma",
                )
            }
            """.trimIndent()
        maxLineLengthRuleAssertThat(code)
            .setMaxLineLength()
            .hasNoLintViolations()
    }

    @Test
    fun `Given max_line_length property is set and max-line-length rule is enabled then return the max_line_length`() {
        val editorConfig =
            EditorConfig(
                mapOf(
                    MAX_LINE_LENGTH to MAX_LINE_LENGTH_PROPERTY.toPropertyWithValue(SOME_MAX_LINE_LENGTH.toString()),
                    KTLINT_RULE_EXECUTION_PROPERTY_NAME to MAX_LINE_LENGTH_RULE_EXECUTION_PROPERTY.toPropertyWithValue(enabled.name),
                ),
            )

        assertThat(editorConfig.maxLineLength()).isEqualTo(SOME_MAX_LINE_LENGTH)
    }

    @Test
    fun `Given max_line_length property is set and max-line-length rule is disabled then return Int MAX_VALUE`() {
        val editorConfig =
            EditorConfig(
                mapOf(
                    MAX_LINE_LENGTH to MAX_LINE_LENGTH_PROPERTY.toPropertyWithValue(SOME_MAX_LINE_LENGTH.toString()),
                    KTLINT_RULE_EXECUTION_PROPERTY_NAME to MAX_LINE_LENGTH_RULE_EXECUTION_PROPERTY.toPropertyWithValue(disabled.name),
                ),
            )

        assertThat(editorConfig.maxLineLength()).isEqualTo(Int.MAX_VALUE)
    }

    @Test
    fun `Given intellij_idea code style, and max_line_length property equals 'unset' and max-line-length rule is enabled then return Int MAX_VALUE`() {
        val editorConfig =
            EditorConfig(
                mapOf(
                    MAX_LINE_LENGTH to MAX_LINE_LENGTH_PROPERTY.toPropertyWithValue("unset"),
                    CODE_STYLE_PROPERTY.name to CODE_STYLE_PROPERTY.toPropertyWithValue(CodeStyleValue.intellij_idea.name),
                    KTLINT_RULE_EXECUTION_PROPERTY_NAME to MAX_LINE_LENGTH_RULE_EXECUTION_PROPERTY.toPropertyWithValue(enabled.name),
                ),
            )

        assertThat(editorConfig.maxLineLength()).isEqualTo(Int.MAX_VALUE)
    }

    private companion object {
        const val MAX_LINE_LENGTH = "max_line_length"
        const val SOME_MAX_LINE_LENGTH = 123
        val KTLINT_RULE_EXECUTION_PROPERTY_NAME = MAX_LINE_LENGTH_RULE_ID.ktLintRuleExecutionPropertyName()
        val MAX_LINE_LENGTH_RULE_EXECUTION_PROPERTY = MAX_LINE_LENGTH_RULE_ID.createRuleExecutionEditorConfigProperty()
    }
}
