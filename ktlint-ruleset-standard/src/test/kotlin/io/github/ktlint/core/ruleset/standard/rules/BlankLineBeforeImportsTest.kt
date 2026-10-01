package io.github.ktlint.core.ruleset.standard.rules

import io.github.ktlint.core.rule.engine.core.api.editorconfig.CODE_STYLE_PROPERTY
import io.github.ktlint.core.rule.engine.core.api.editorconfig.CodeStyleValue
import io.github.ktlint.core.test.KtLintAssertThat.Companion.assertThatRuleBuilder
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.junit.jupiter.params.provider.EnumSource.Mode.EXCLUDE
import org.junit.jupiter.params.provider.EnumSource.Mode.INCLUDE

class BlankLineBeforeImportsTest {
    private val blankLineBeforeImportsRuleAssertThat =
        assertThatRuleBuilder { BlankLineBeforeImports() }
            .addAdditionalRuleProvider { NoConsecutiveBlankLinesRule() }
            .assertThat()

    // Just for one single test evaluates that the rule is working for `ktlint_official` and `android_studio` code styles, but not for
    // `intellij_idea`. It is assumed that all other tests (do not) work similarly.
    @ParameterizedTest(name = "Code style: {0}")
    @EnumSource(CodeStyleValue::class, mode = INCLUDE, names = ["KTLINT_OFFICIAL", "ANDROID_STUDIO"])
    fun `Given a copyright comment and first import statement not separated by a blank line then insert a blank line in between`(
        codeStyleValue: CodeStyleValue,
    ) {
        val code =
            """
            /*
             * Copyright comment
             */
            import foo
            """.trimIndent()
        val formattedCode =
            """
            /*
             * Copyright comment
             */

            import foo
            """.trimIndent()
        blankLineBeforeImportsRuleAssertThat(code)
            .withEditorConfigOverride(CODE_STYLE_PROPERTY to codeStyleValue.name)
            .hasLintViolation(4, 1, "Expected a blank line before the import(s)")
            .isFormattedAs(formattedCode)
    }

    @ParameterizedTest(name = "Code style: {0}")
    @EnumSource(CodeStyleValue::class, mode = EXCLUDE, names = ["KTLINT_OFFICIAL", "ANDROID_STUDIO"])
    fun `Given a copyright comment and first import statement not separated by a blank line then do not insert a blank line in between`(
        codeStyleValue: CodeStyleValue,
    ) {
        val code =
            """
            /*
             * Copyright comment
             */
            import foo
            """.trimIndent()
        blankLineBeforeImportsRuleAssertThat(code)
            .withEditorConfigOverride(CODE_STYLE_PROPERTY to codeStyleValue.name)
            .hasNoLintViolations()
    }

    @Test
    fun `Given an import statement at the first line then do not insert a blank line before it`() {
        val code =
            """
            import foo
            """.trimIndent()
        blankLineBeforeImportsRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `Given a copyright comment and import statement separated by exactly one blank line then do not insert another blank line in between`() {
        val code =
            """
            /*
             * Copyright comment
             */

            import foo
            """.trimIndent()
        blankLineBeforeImportsRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `Given a copyright comment and import statement separated by too many blank lines then do remove the redundant blank lines`() {
        val code =
            """
            /*
             * Copyright comment
             */


            import foo
            """.trimIndent()
        val formattedCode =
            """
            /*
             * Copyright comment
             */

            import foo
            """.trimIndent()
        blankLineBeforeImportsRuleAssertThat(code)
            .hasLintViolationForAdditionalRule(5, 1, "Needless blank line(s)")
            .isFormattedAs(formattedCode)
    }

    @Test
    fun `Given a file annotation and import statement not separated by a blank line then do not insert a blank line in between`() {
        val code =
            """
            @file:bar
            import foo
            """.trimIndent()
        val formattedCode =
            """
            @file:bar

            import foo
            """.trimIndent()
        blankLineBeforeImportsRuleAssertThat(code)
            .hasLintViolation(2, 1, "Expected a blank line before the import(s)")
            .isFormattedAs(formattedCode)
    }

    @Test
    fun `Given a package statement and import statement not separated by a blank line then do not insert a blank line in between`() {
        val code =
            """
            package bar
            import foo
            """.trimIndent()
        val formattedCode =
            """
            package bar

            import foo
            """.trimIndent()
        blankLineBeforeImportsRuleAssertThat(code)
            .hasLintViolation(2, 1, "Expected a blank line before the import(s)")
            .isFormattedAs(formattedCode)
    }

    @Test
    fun `Given a blank line exists between package and import then do not emit`() {
        val code =
            """
            package foo

            import bar.*
            """.trimIndent()
        blankLineBeforeImportsRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `Given a comment above import and no blank line after package then emit and autocorrect`() {
        val code =
            """
            package foo
            // Some comment
            import bar.*
            """.trimIndent()
        val formattedCode =
            """
            package foo

            // Some comment
            import bar.*
            """.trimIndent()
        blankLineBeforeImportsRuleAssertThat(code)
            .hasLintViolation(2, 1, "Expected a blank line before the import(s)")
            .isFormattedAs(formattedCode)
    }

    @Test
    fun `Given too many blank lines between package and import then emit and autocorrect`() {
        val code =
            """
            package foo


            import bar.*
            """.trimIndent()
        val formattedCode =
            """
            package foo

            import bar.*
            """.trimIndent()
        blankLineBeforeImportsRuleAssertThat(code)
            .hasLintViolationForAdditionalRule(3, 1, "Needless blank line(s)")
            .isFormattedAs(formattedCode)
    }

    @Test
    fun `Given a file not containing any import statement`() {
        val code =
            """
            package bar

            val foo = "foo"
            """.trimIndent()
        blankLineBeforeImportsRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `Given no imports then do not emit`() {
        val code =
            """
            package foo
            """.trimIndent()
        blankLineBeforeImportsRuleAssertThat(code).hasNoLintViolations()
    }
}
