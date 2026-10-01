package io.github.ktlint.core.ruleset.standard.rules

import io.github.ktlint.core.rule.engine.core.api.editorconfig.CODE_STYLE_PROPERTY
import io.github.ktlint.core.rule.engine.core.api.editorconfig.CodeStyleValue
import io.github.ktlint.core.test.KtLintAssertThat.Companion.assertThatRuleBuilder
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.junit.jupiter.params.provider.EnumSource.Mode.EXCLUDE
import org.junit.jupiter.params.provider.EnumSource.Mode.INCLUDE

class BlankLineBeforeFileAnnotationTest {
    private val blankLineBeforeFileAnnotationRuleAssertThat =
        assertThatRuleBuilder { BlankLineBeforeFileAnnotation() }
            .addAdditionalRuleProvider { NoConsecutiveBlankLinesRule() }
            .assertThat()

    // Just for one single test evaluates that the rule is working for `ktlint_official` and `android_studio` code styles, but not for
    // `intellij_idea`. It is assumed that all other tests (do not) work similarly.
    @ParameterizedTest(name = "Code style: {0}")
    @EnumSource(CodeStyleValue::class, mode = INCLUDE, names = ["KTLINT_OFFICIAL", "ANDROID_STUDIO"])
    fun `Given a copyright comment and file annotation not separated by a blank line then insert a blank line in between`(
        codeStyleValue: CodeStyleValue,
    ) {
        val code =
            """
            /*
             * Copyright comment
             */
            @file:Foo
            """.trimIndent()
        val formattedCode =
            """
            /*
             * Copyright comment
             */

            @file:Foo
            """.trimIndent()
        blankLineBeforeFileAnnotationRuleAssertThat(code)
            .withEditorConfigOverride(CODE_STYLE_PROPERTY to codeStyleValue.name)
            .hasLintViolation(4, 1, "Expected a blank line before the file annotation(s)")
            .isFormattedAs(formattedCode)
    }

    @ParameterizedTest(name = "Code style: {0}")
    @EnumSource(CodeStyleValue::class, mode = EXCLUDE, names = ["KTLINT_OFFICIAL", "ANDROID_STUDIO"])
    fun `Given a copyright comment and file annotation not separated by a blank line then do not insert a blank line in between`(
        codeStyleValue: CodeStyleValue,
    ) {
        val code =
            """
            /*
             * Copyright comment
             */
            @file:Foo
            """.trimIndent()
        blankLineBeforeFileAnnotationRuleAssertThat(code)
            .withEditorConfigOverride(CODE_STYLE_PROPERTY to codeStyleValue.name)
            .hasNoLintViolations()
    }

    @Test
    fun `Given a file annotation at the first line then do not insert a blank line before it`() {
        val code =
            """
            @file:Foo
            """.trimIndent()
        blankLineBeforeFileAnnotationRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `Given a copyright comment and file annotation separated by exactly one blank line then do not insert another blank line in between`() {
        val code =
            """
            /*
             * Copyright comment
             */

            @file:Foo
            """.trimIndent()
        blankLineBeforeFileAnnotationRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `Given a copyright comment and file annotation separated by too many blank lines then remove redundant blank lines`() {
        val code =
            """
            /*
             * Copyright comment
             */


            @file:Foo
            """.trimIndent()
        val formattedCode =
            """
            /*
             * Copyright comment
             */

            @file:Foo
            """.trimIndent()
        blankLineBeforeFileAnnotationRuleAssertThat(code)
            .hasNoLintViolationsExceptInAdditionalRules()
            .isFormattedAs(formattedCode)
    }
}
