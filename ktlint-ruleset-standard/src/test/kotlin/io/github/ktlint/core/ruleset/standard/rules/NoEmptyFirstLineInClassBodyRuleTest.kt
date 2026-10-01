package io.github.ktlint.core.ruleset.standard.rules

import io.github.ktlint.core.rule.engine.core.api.editorconfig.CODE_STYLE_PROPERTY
import io.github.ktlint.core.rule.engine.core.api.editorconfig.CodeStyleValue
import io.github.ktlint.core.rule.engine.core.api.editorconfig.RuleExecution
import io.github.ktlint.core.rule.engine.core.api.editorconfig.createRuleExecutionEditorConfigProperty
import io.github.ktlint.core.test.KtLintAssertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource

class NoEmptyFirstLineInClassBodyRuleTest {
    private val noEmptyFirstLineInClassBodyRuleAssertThat = KtLintAssertThat.assertThatRule { NoEmptyFirstLineInClassBodyRule() }

    @Test
    fun `Given a class body that does not start with a blank line`() {
        val code =
            """
            class Foo {
                val foo = "foo"
            }
            """.trimIndent()
        noEmptyFirstLineInClassBodyRuleAssertThat(code)
            .withEditorConfigOverride(CODE_STYLE_PROPERTY to CodeStyleValue.KTLINT_OFFICIAL)
            .hasNoLintViolations()
    }

    @Test
    fun `Given a class without body`() {
        val code =
            """
            class Foo

            val foo = "foo"
            """.trimIndent()
        noEmptyFirstLineInClassBodyRuleAssertThat(code)
            .withEditorConfigOverride(CODE_STYLE_PROPERTY to CodeStyleValue.KTLINT_OFFICIAL)
            .hasNoLintViolations()
    }

    @Test
    fun `Given a class body starting with a blank line`() {
        val code =
            """
            class Foo {

                val foo = "foo"
            }
            """.trimIndent()
        val formattedCode =
            """
            class Foo {
                val foo = "foo"
            }
            """.trimIndent()
        noEmptyFirstLineInClassBodyRuleAssertThat(code)
            .withEditorConfigOverride(CODE_STYLE_PROPERTY to CodeStyleValue.KTLINT_OFFICIAL)
            .hasLintViolation(2, 1, "Class body should not start with blank line")
            .isFormattedAs(formattedCode)
    }

    @Test
    fun `Given a nested class body starting with a blank line`() {
        val code =
            """
            class Bar {
                class Foo {

                    val foo = "foo"
                }
            }
            """.trimIndent()
        val formattedCode =
            """
            class Bar {
                class Foo {
                    val foo = "foo"
                }
            }
            """.trimIndent()
        noEmptyFirstLineInClassBodyRuleAssertThat(code)
            .withEditorConfigOverride(CODE_STYLE_PROPERTY to CodeStyleValue.KTLINT_OFFICIAL)
            .hasLintViolation(3, 1, "Class body should not start with blank line")
            .isFormattedAs(formattedCode)
    }

    @ParameterizedTest(name = "Code style: {0}")
    @EnumSource(
        value = CodeStyleValue::class,
        mode = EnumSource.Mode.EXCLUDE,
        names = ["KTLINT_OFFICIAL"],
    )
    fun `Given a non-ktlint-official code style and a class body starting with a blank line then do not return a lint violation`(
        codeStyleValue: CodeStyleValue,
    ) {
        val code =
            """
            class Foo {

                val foo = "foo"
            }
            """.trimIndent()
        noEmptyFirstLineInClassBodyRuleAssertThat(code)
            .withEditorConfigOverride(CODE_STYLE_PROPERTY to codeStyleValue)
            .hasNoLintViolations()
    }

    @ParameterizedTest(name = "Code style: {0}")
    @EnumSource(
        value = CodeStyleValue::class,
        mode = EnumSource.Mode.EXCLUDE,
        names = ["KTLINT_OFFICIAL"],
    )
    fun `Given a non-ktlint-official code style, and the rule has been enabled explicitly, and a class body starting with a blank line then do report a lint violation`(
        codeStyleValue: CodeStyleValue,
    ) {
        val code =
            """
            class Foo {

                val foo = "foo"
            }
            """.trimIndent()
        val formattedCode =
            """
            class Foo {
                val foo = "foo"
            }
            """.trimIndent()
        noEmptyFirstLineInClassBodyRuleAssertThat(code)
            .withEditorConfigOverride(CODE_STYLE_PROPERTY to codeStyleValue)
            .withEditorConfigOverride(
                NO_EMPTY_FIRST_LINE_IN_CLASS_BODY_RULE_ID.createRuleExecutionEditorConfigProperty() to RuleExecution.ENABLED,
            ).hasLintViolation(2, 1, "Class body should not start with blank line")
            .isFormattedAs(formattedCode)
    }
}
