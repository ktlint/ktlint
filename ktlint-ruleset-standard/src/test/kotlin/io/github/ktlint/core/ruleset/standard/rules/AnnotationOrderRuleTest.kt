package io.github.ktlint.core.ruleset.standard.rules

import io.github.ktlint.core.test.KtLintAssertThat.Companion.assertThatRule
import org.junit.jupiter.api.Test

class AnnotationOrderRuleTest {
    private val annotationOrderRuleAssertThat = assertThatRule { AnnotationOrderRule() }

    @Test
    fun `Given annotations already sorted alphabetically then do not report a violation`() {
        val code =
            """
            @Alpha
            @Beta
            fun foo() {}
            """.trimIndent()
        annotationOrderRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `Given annotations not sorted alphabetically then report a violation and sort them`() {
        val code =
            """
            @Beta
            @Alpha
            fun foo() {}
            """.trimIndent()
        val formattedCode =
            """
            @Alpha
            @Beta
            fun foo() {}
            """.trimIndent()
        annotationOrderRuleAssertThat(code)
            .hasLintViolation(1, 1, "Annotations should be sorted alphabetically")
            .isFormattedAs(formattedCode)
    }

    @Test
    fun `Given three annotations not sorted alphabetically then sort them`() {
        val code =
            """
            @Gamma
            @Alpha
            @Beta
            fun foo() {}
            """.trimIndent()
        val formattedCode =
            """
            @Alpha
            @Beta
            @Gamma
            fun foo() {}
            """.trimIndent()
        annotationOrderRuleAssertThat(code)
            .hasLintViolation(1, 1, "Annotations should be sorted alphabetically")
            .isFormattedAs(formattedCode)
    }

    @Test
    fun `Given annotations with arguments not sorted alphabetically then sort them`() {
        val code =
            """
            @Suppress("FOO")
            @Deprecated("Use bar instead")
            fun foo() {}
            """.trimIndent()
        val formattedCode =
            """
            @Deprecated("Use bar instead")
            @Suppress("FOO")
            fun foo() {}
            """.trimIndent()
        annotationOrderRuleAssertThat(code)
            .hasLintViolation(1, 1, "Annotations should be sorted alphabetically")
            .isFormattedAs(formattedCode)
    }

    @Test
    fun `Given a single annotation then do not report a violation`() {
        val code =
            """
            @Alpha
            fun foo() {}
            """.trimIndent()
        annotationOrderRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `Given annotations on a class not sorted alphabetically then sort them`() {
        val code =
            """
            @Beta
            @Alpha
            class Foo
            """.trimIndent()
        val formattedCode =
            """
            @Alpha
            @Beta
            class Foo
            """.trimIndent()
        annotationOrderRuleAssertThat(code)
            .hasLintViolation(1, 1, "Annotations should be sorted alphabetically")
            .isFormattedAs(formattedCode)
    }

    @Test
    fun `Given annotations on a property not sorted alphabetically then sort them`() {
        val code =
            """
            @Beta
            @Alpha
            val foo = "bar"
            """.trimIndent()
        val formattedCode =
            """
            @Alpha
            @Beta
            val foo = "bar"
            """.trimIndent()
        annotationOrderRuleAssertThat(code)
            .hasLintViolation(1, 1, "Annotations should be sorted alphabetically")
            .isFormattedAs(formattedCode)
    }

    @Test
    fun `Given file-level annotations not sorted alphabetically then sort them`() {
        val code =
            """
            @file:Beta
            @file:Alpha
            package foo
            """.trimIndent()
        val formattedCode =
            """
            @file:Alpha
            @file:Beta
            package foo
            """.trimIndent()
        annotationOrderRuleAssertThat(code)
            .hasLintViolation(1, 1, "Annotations should be sorted alphabetically")
            .isFormattedAs(formattedCode)
    }
}
