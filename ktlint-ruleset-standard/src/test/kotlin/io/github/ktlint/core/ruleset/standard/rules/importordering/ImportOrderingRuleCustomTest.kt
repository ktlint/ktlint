package io.github.ktlint.core.ruleset.standard.rules.importordering

import io.github.ktlint.core.ruleset.standard.rules.ImportOrderingRule
import io.github.ktlint.core.ruleset.standard.rules.ImportOrderingRule.Companion.IJ_KOTLIN_IMPORTS_LAYOUT_PROPERTY
import io.github.ktlint.core.test.KtLintAssertThat.Companion.assertThatRule
import org.junit.jupiter.api.Test

class ImportOrderingRuleCustomTest {
    private val importOrderingRuleAssertThat = assertThatRule { ImportOrderingRule() }

    @Test
    fun `Given some imports with an empty line between imports and aliases as is required then do not return lint errors`() {
        val code =
            """
            import android.content.Context as Ctx
            import androidx.fragment.app.Fragment as F

            import android.app.Activity
            import android.view.View
            import android.view.ViewGroup
            import java.util.List
            import kotlin.concurrent.Thread
            """.trimIndent()
        importOrderingRuleAssertThat(code)
            .withEditorConfigOverride(IJ_KOTLIN_IMPORTS_LAYOUT_PROPERTY to "^,|,*")
            .hasNoLintViolations()
    }

    @Test
    fun `Given some import without empty line between imports and aliases while this is required then do return a lint error`() {
        val code =
            """
            import android.app.Activity
            import android.view.View
            import android.view.ViewGroup
            import java.util.List
            import kotlin.concurrent.Thread
            import android.content.Context as Ctx
            import androidx.fragment.app.Fragment as F
            """.trimIndent()
        val formattedCode =
            """
            import android.app.Activity
            import android.view.View
            import android.view.ViewGroup
            import java.util.List
            import kotlin.concurrent.Thread

            import android.content.Context as Ctx
            import androidx.fragment.app.Fragment as F
            """.trimIndent()
        importOrderingRuleAssertThat(code)
            .withEditorConfigOverride(IJ_KOTLIN_IMPORTS_LAYOUT_PROPERTY to "*,|,^")
            .hasLintViolation(1, 1, "Imports must be ordered according to the pattern specified in .editorconfig")
            .isFormattedAs(formattedCode)
    }

    @Test
    fun `Given some imports incorrectly ordered and with unexpected empty lines then do return lint errors`() {
        val code =
            """
            import android.app.Activity
            import android.content.Context as Ctx
            import androidx.fragment.app.Fragment as F


            import android.view.View
            import android.view.ViewGroup

            import java.util.List
            import kotlin.concurrent.Thread
            """.trimIndent()
        val formattedCode =
            """
            import android.content.Context as Ctx
            import androidx.fragment.app.Fragment as F

            import android.app.Activity
            import android.view.View
            import android.view.ViewGroup
            import java.util.List
            import kotlin.concurrent.Thread
            """.trimIndent()
        importOrderingRuleAssertThat(code)
            .withEditorConfigOverride(IJ_KOTLIN_IMPORTS_LAYOUT_PROPERTY to "^,|,*")
            .hasLintViolation(1, 1, "Imports must be ordered according to the pattern specified in .editorconfig")
            .isFormattedAs(formattedCode)
    }

    @Test
    fun `Given some imports ordered as required then do no return lint errors`() {
        val code =
            """
            import android.app.Activity
            import android.view.View

            import org.junit.Assert

            import net.stuff.A

            import org.foo.Bar

            import java.util.List

            import kotlin.concurrent.Thread
            import kotlin.io.Closeable
            """.trimIndent()
        importOrderingRuleAssertThat(code)
            .withEditorConfigOverride(
                IJ_KOTLIN_IMPORTS_LAYOUT_PROPERTY to "android.**,|,org.junit.**,|,net.**,|,org.**,|,java.**,|,com.**,|,javax.**,|,*",
            ).hasNoLintViolations()
    }

    @Test
    fun `Given some imports not ordered as required then do return lint errors`() {
        val code =
            """
            import android.app.Activity
            import android.view.View

            import net.stuff.A
            import org.junit.Assert
            import kotlin.io.Closeable
            import kotlin.concurrent.Thread




            import java.util.List
            import org.foo.Bar
            """.trimIndent()
        val formattedCode =
            """
            import android.app.Activity
            import android.view.View

            import org.junit.Assert

            import net.stuff.A

            import org.foo.Bar

            import java.util.List

            import kotlin.concurrent.Thread
            import kotlin.io.Closeable
            """.trimIndent()
        importOrderingRuleAssertThat(code)
            .withEditorConfigOverride(
                IJ_KOTLIN_IMPORTS_LAYOUT_PROPERTY to "android.**,|,org.junit.**,|,net.**,|,org.**,|,java.**,|,com.**,|,javax.**,|,*",
            ).hasLintViolation(1, 1, "Imports must be ordered according to the pattern specified in .editorconfig")
            .isFormattedAs(formattedCode)
    }

    @Test
    fun `Given an imports layout with multiple consecutive empty lines then fold to one single empty line`() {
        val code =
            """
            import java.util.List

            import kotlin.concurrent.Thread
            import kotlin.io.Closeable
            import android.app.Activity
            import android.view.View
            """.trimIndent()
        importOrderingRuleAssertThat(code)
            .withEditorConfigOverride(IJ_KOTLIN_IMPORTS_LAYOUT_PROPERTY to "java.**,|,|,|,kotlin.**,*")
            .hasNoLintViolations()
    }

    @Test
    fun `Given some imports with aliases correctly ordered`() {
        val code =
            """
            import kotlin.io.Closeable as C
            import android.view.View as V
            import android.app.Activity

            import kotlin.concurrent.Thread
            import java.util.List as L
            """.trimIndent()
        importOrderingRuleAssertThat(code)
            .withEditorConfigOverride(IJ_KOTLIN_IMPORTS_LAYOUT_PROPERTY to "^kotlin.**,^android.**,android.**,|,*,^")
            .hasNoLintViolations()
    }

    @Test
    fun `Given some imports with aliases incorrectly ordered`() {
        val code =
            """
            import android.app.Activity
            import android.view.View as V
            import java.util.List as L
            import kotlin.concurrent.Thread
            import kotlin.io.Closeable as C
            """.trimIndent()
        val formattedCode =
            """
            import kotlin.io.Closeable as C
            import android.view.View as V
            import android.app.Activity

            import java.util.List as L
            import kotlin.concurrent.Thread
            """.trimIndent()
        importOrderingRuleAssertThat(code)
            .withEditorConfigOverride(IJ_KOTLIN_IMPORTS_LAYOUT_PROPERTY to "^kotlin.**,^android.**,android.**,|,^,*")
            .hasLintViolation(1, 1, "Imports must be ordered according to the pattern specified in .editorconfig")
            .isFormattedAs(formattedCode)
    }

    @Test
    fun `Given some import layout which matches a complete import and imports in correct order`() {
        val code =
            """
            import kotlin.io.Closeable
            import kotlin.concurrent.Thread
            import android.app.Activity
            import android.view.View
            import java.util.List
            """.trimIndent()
        importOrderingRuleAssertThat(code)
            .withEditorConfigOverride(IJ_KOTLIN_IMPORTS_LAYOUT_PROPERTY to "kotlin.io.Closeable.*,kotlin.**,*")
            .hasNoLintViolations()
    }

    @Test
    fun `Given some import layout which matches a complete import and imports in incorrect order`() {
        val code =
            """
            import android.app.Activity
            import android.view.View
            import java.util.List

            import kotlin.concurrent.Thread
            import kotlin.io.Closeable
            """.trimIndent()
        val formattedCode =
            """
            import kotlin.io.Closeable
            import kotlin.concurrent.Thread
            import android.app.Activity
            import android.view.View
            import java.util.List
            """.trimIndent()
        importOrderingRuleAssertThat(code)
            .withEditorConfigOverride(IJ_KOTLIN_IMPORTS_LAYOUT_PROPERTY to "kotlin.io.Closeable.*,kotlin.**,*")
            .isFormattedAs(formattedCode)
    }

    @Test
    @Suppress("DANGEROUS_CHARACTERS")
    fun `Issue 1845 - Given a list of pattern entries including a '|' pattern but no import exists that match any pattern before the first '|'`() {
        val code =
            """
            import java.nio.file.Files
            import java.nio.file.Paths
            import java.util.Objects

            object TestData {
                fun loadString(dataset: String): String {
                    val path = Paths.get(Objects.requireNonNull(TestData::class.java.getResource(dataset)).toURI())
                    return String(Files.readAllBytes(path))
                }
            }
            """.trimIndent()
        importOrderingRuleAssertThat(code)
            .withEditorConfigOverride(IJ_KOTLIN_IMPORTS_LAYOUT_PROPERTY to "*, |, javax.**, java.**, |, kotlinx.**, kotlin.**")
            .hasNoLintViolations()
    }
}
