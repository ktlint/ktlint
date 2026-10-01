package io.github.ktlint.core.ruleset.standard.rules

import io.github.ktlint.core.rule.engine.core.api.editorconfig.CODE_STYLE_PROPERTY
import io.github.ktlint.core.rule.engine.core.api.editorconfig.CodeStyleValue
import io.github.ktlint.core.test.KtLintAssertThat.Companion.assertThatRuleBuilder
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.junit.jupiter.params.provider.EnumSource.Mode.EXCLUDE
import org.junit.jupiter.params.provider.EnumSource.Mode.INCLUDE

class BlankLineBeforePackageTest {
    private val blankLineBeforePackageRuleAssertThat =
        assertThatRuleBuilder { BlankLineBeforePackage() }
            .addAdditionalRuleProvider { NoConsecutiveBlankLinesRule() }
            .assertThat()

    // Just for one single test evaluates that the rule is working for `ktlint_official` and `android_studio` code styles, but not for
    // `intellij_idea`. It is assumed that all other tests (do not) work similarly.
    @ParameterizedTest(name = "Code style: {0}")
    @EnumSource(CodeStyleValue::class, mode = INCLUDE, names = ["KTLINT_OFFICIAL", "ANDROID_STUDIO"])
    fun `Given a copyright comment and package statement not separated by a blank line then insert a blank line in between`(
        codeStyleValue: CodeStyleValue,
    ) {
        val code =
            """
            /*
             * Copyright comment
             */
            package foo
            """.trimIndent()
        val formattedCode =
            """
            /*
             * Copyright comment
             */

            package foo
            """.trimIndent()
        blankLineBeforePackageRuleAssertThat(code)
            .withEditorConfigOverride(CODE_STYLE_PROPERTY to codeStyleValue.name)
            .hasLintViolation(4, 1, "Expected a blank line before the package statement")
            .isFormattedAs(formattedCode)
    }

    @ParameterizedTest(name = "Code style: {0}")
    @EnumSource(CodeStyleValue::class, mode = EXCLUDE, names = ["KTLINT_OFFICIAL", "ANDROID_STUDIO"])
    fun `Given a copyright comment and package statement not separated by a blank line then do not insert a blank line in between`(
        codeStyleValue: CodeStyleValue,
    ) {
        val code =
            """
            /*
             * Copyright comment
             */
            package foo
            """.trimIndent()
        blankLineBeforePackageRuleAssertThat(code)
            .withEditorConfigOverride(CODE_STYLE_PROPERTY to codeStyleValue.name)
            .hasNoLintViolations()
    }

    @Test
    fun `Given a package statement at the first line then do not insert a blank line before it`() {
        val code =
            """
            package Foo
            """.trimIndent()
        blankLineBeforePackageRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `Given a copyright comment and package statement separated by exactly one blank line then do not insert another blank line in between`() {
        val code =
            """
            /*
             * Copyright comment
             */

            package foo
            """.trimIndent()
        blankLineBeforePackageRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `Given a copyright comment and package statement separated by too many blank line then do remove the redundant blank lines`() {
        val code =
            """
            /*
             * Copyright comment
             */


            package foo
            """.trimIndent()
        val formattedCode =
            """
            /*
             * Copyright comment
             */

            package foo
            """.trimIndent()
        blankLineBeforePackageRuleAssertThat(code)
            .hasNoLintViolationsExceptInAdditionalRules()
            .isFormattedAs(formattedCode)
    }

    @Test
    fun `Given a file annotation and package statement not separated by a blank line then do not insert a blank line in between`() {
        val code =
            """
            @file:bar
            package foo
            """.trimIndent()
        val formattedCode =
            """
            @file:bar

            package foo
            """.trimIndent()
        blankLineBeforePackageRuleAssertThat(code)
            .hasLintViolation(2, 1, "Expected a blank line before the package statement")
            .isFormattedAs(formattedCode)
    }

    @Test
    fun `Given a file containing a file annotation and an import separated by a blank line, but without package statement then do not add an additional blank line before the empty package directive`() {
        val code =
            """
            @file:bar

            import foo
            """.trimIndent()
        blankLineBeforePackageRuleAssertThat(code)
            .addAdditionalRuleProvider { IndentationRule() }
            .hasNoLintViolations()
    }
}
