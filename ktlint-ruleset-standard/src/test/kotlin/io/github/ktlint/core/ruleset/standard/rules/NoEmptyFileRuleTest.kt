package io.github.ktlint.core.ruleset.standard.rules

import io.github.ktlint.core.test.KtLintAssertThat.Companion.assertThatRule
import org.junit.jupiter.api.Test

class NoEmptyFileRuleTest {
    private val noEmptyFileRuleAssertThat = assertThatRule { NoEmptyFileRule() }

    @Test
    fun `Given non-empty kotlin file then ignore the rule for this file`() {
        val code =
            """
            package foo
            fun main() {
                println("foo")
            }
            """.trimIndent()
        noEmptyFileRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `Given an empty kotlin file then do a return lint error`() {
        val code = EMPTY_FILE
        noEmptyFileRuleAssertThat(code)
            .asFileWithPath("/some/path/Tmp.kt")
            .hasLintViolationWithoutAutoCorrect(1, 1, "File 'Tmp.kt' should not be empty")
    }

    @Test
    fun `Given an empty kotlin script file then do a return lint error`() {
        val code = EMPTY_FILE
        noEmptyFileRuleAssertThat(code)
            .asFileWithPath("/some/path/Tmp.kts")
            .asKotlinScript()
            .hasLintViolationWithoutAutoCorrect(1, 1, "File 'Tmp.kts' should not be empty")
    }

    @Test
    fun `Given only package statement in kotlin file then do a return lint error`() {
        val code =
            """
            package foo
            """.trimIndent()

        noEmptyFileRuleAssertThat(code)
            .asFileWithPath("/some/path/Tmp.kt")
            .hasLintViolationWithoutAutoCorrect(1, 1, "File 'Tmp.kt' should not be empty")
    }

    @Test
    fun `Given only import statement in kotlin file then do a return lint error`() {
        val code =
            """
            import foo.Bar
            """.trimIndent()
        noEmptyFileRuleAssertThat(code)
            .asFileWithPath("/some/path/Tmp.kt")
            .hasLintViolationWithoutAutoCorrect(1, 1, "File 'Tmp.kt' should not be empty")
    }

    @Test
    fun `Given only package and import statements in kotlin file then do a return lint error`() {
        val code =
            """
            package foo
            import foo.Bar
            """.trimIndent()
        noEmptyFileRuleAssertThat(code)
            .asFileWithPath("/some/path/Tmp.kt")
            .hasLintViolationWithoutAutoCorrect(1, 1, "File 'Tmp.kt' should not be empty")
    }

    @Test
    fun `Given only package, import statements and comments in kotlin file then do a return lint error`() {
        val code =
            """
            package foo
            import foo.Bar

            // some comment

            /*
             * some comment
             */

            /**
             * some comment
             */
            """.trimIndent()
        noEmptyFileRuleAssertThat(code)
            .asFileWithPath("/some/path/Tmp.kt")
            .hasLintViolationWithoutAutoCorrect(1, 1, "File 'Tmp.kt' should not be empty")
    }

    @Test
    fun `Given non-empty kotlin file then ignore this file`() {
        val code =
            """
            package foo
            fun main() {
                println("foo")
            }
            """.trimIndent()
        noEmptyFileRuleAssertThat(code)
            .asFileWithPath("/some/path/Tmp.kt")
            .hasNoLintViolations()
    }

    @Test
    fun `x x`() {
        val code =
            """
            plugins {
                id("ktlint-publication-library")
            }

            dependencies {
                implementation(projects.ktlintLogger)
                implementation(projects.ktlintRuleEngine)
                implementation(projects.ktlintCliRulesetCore)
                api(libs.assertk)
                api(libs.junit5)
                api(libs.jimfs)
            }
            """.trimIndent()
        noEmptyFileRuleAssertThat(code)
            .asFileWithPath("/some/path/Tmp.kts")
            .asKotlinScript()
            .hasNoLintViolations()
    }

    private companion object {
        private const val EMPTY_FILE = ""
    }
}
