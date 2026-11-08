package io.github.ktlint.core.ruleset.standard.rules

import io.github.ktlint.core.test.KtLintAssertThat.Companion.EOL_CHAR
import io.github.ktlint.core.test.KtLintAssertThat.Companion.MAX_LINE_LENGTH_MARKER
import io.github.ktlint.core.test.KtLintAssertThat.Companion.assertThatRuleBuilder
import io.github.ktlint.core.test.LintViolation
import io.github.ktlint.core.test.MULTILINE_STRING_QUOTE
import org.junit.jupiter.api.Test

internal class PropertyWrappingRuleTest {
    private val propertyWrappingRuleAssertThat =
        assertThatRuleBuilder { PropertyWrappingRule() }
            .addAdditionalRuleProvider { MaxLineLengthRule() }
            .assertThat()

    @Test
    fun `Given that the variable name and the following colon do not fit on the same line as val or var keyword then wrap after the colon`() {
        val code =
            """
            // $MAX_LINE_LENGTH_MARKER     $EOL_CHAR
            val aVariableWithALoooooongName1: String

            @Suppress("ktlint:standard:max-line-length")
            val aVariableWithALoooooongName2: String
            """.trimIndent()
        val formattedCode =
            """
            // $MAX_LINE_LENGTH_MARKER     $EOL_CHAR
            val aVariableWithALoooooongName1:
                String

            @Suppress("ktlint:standard:max-line-length")
            val aVariableWithALoooooongName2: String
            """.trimIndent()
        propertyWrappingRuleAssertThat(code)
            .setMaxLineLength()
            .hasLintViolation(2, 34, "Missing newline after \":\"")
            .isFormattedAs(formattedCode)
    }

    @Test
    fun `Given that the type does not fit on the same line as the variable name`() {
        val code =
            """
            // $MAX_LINE_LENGTH_MARKER                   $EOL_CHAR
            val aVariableWithALongerName: TypeWithALongName
            val aVariableWithALongName2: TypeWithALongName
            """.trimIndent()
        val formattedCode =
            """
            // $MAX_LINE_LENGTH_MARKER                   $EOL_CHAR
            val aVariableWithALongerName:
                TypeWithALongName
            val aVariableWithALongName2: TypeWithALongName
            """.trimIndent()
        propertyWrappingRuleAssertThat(code)
            .setMaxLineLength()
            .hasLintViolation(2, 30, "Missing newline before \"TypeWithALongName\"")
            .isFormattedAs(formattedCode)
    }

    @Test
    fun `Given that equals sign before the value does not fit on the same line as the type and variable name then wrap after the equals sign`() {
        val code =
            """
            // $MAX_LINE_LENGTH_MARKER                     $EOL_CHAR
            val aVariableWithALongName: TypeWithALongerName = TypeWithALongName(123)
            val aVariableWithALongName: TypeWithALongName2 = TypeWithALongName(123)
            """.trimIndent()
        val formattedCode =
            """
            // $MAX_LINE_LENGTH_MARKER                     $EOL_CHAR
            val aVariableWithALongName: TypeWithALongerName =
                TypeWithALongName(123)
            val aVariableWithALongName: TypeWithALongName2 =
                TypeWithALongName(123)
            """.trimIndent()
        propertyWrappingRuleAssertThat(code)
            .setMaxLineLength()
            .hasLintViolations(
                LintViolation(2, 50, "Missing newline after \"=\""),
                LintViolation(3, 49, "Missing newline before \"TypeWithALongName(123)\""),
            ).isFormattedAs(formattedCode)
    }

    @Test
    fun `Given that the value does not fit on the same line as the type and variable name then wrap after the equals sign`() {
        val code =
            """
            // $MAX_LINE_LENGTH_MARKER                                            $EOL_CHAR
            val aVariableWithALongName: TypeWithALongerName = TypeWithALongName(123)
            val aVariableWithALongName: TypeWithALongName2 = TypeWithALongName(123)
            """.trimIndent()
        val formattedCode =
            """
            // $MAX_LINE_LENGTH_MARKER                                            $EOL_CHAR
            val aVariableWithALongName: TypeWithALongerName =
                TypeWithALongName(123)
            val aVariableWithALongName: TypeWithALongName2 = TypeWithALongName(123)
            """.trimIndent()
        propertyWrappingRuleAssertThat(code)
            .setMaxLineLength()
            .hasLintViolation(2, 50, "Missing newline before \"TypeWithALongName(123)\"")
            .isFormattedAs(formattedCode)
    }

    @Test
    fun `Should not wrap the argument list when wrapping the DOT_QUALIFIED_EXPRESSION before the argument list prevents exceeding the max line length`() {
        val code =
            """
            // $MAX_LINE_LENGTH_MARKER  $EOL_CHAR
            val foo = Foo.foooooooooooo(1)
            """.trimIndent()
        val formattedCode =
            """
            // $MAX_LINE_LENGTH_MARKER  $EOL_CHAR
            val foo =
                Foo.foooooooooooo(1)
            """.trimIndent()
        propertyWrappingRuleAssertThat(code)
            .setMaxLineLength()
            .addAdditionalRuleProvider { ArgumentListWrappingRule() }
            .hasLintViolation(2, 10, "Missing newline before \"Foo.foooooooooooo(1)\"")
            .isFormattedAs(formattedCode)
    }

    @Test
    fun `Issue 3458 - Given a property with a multiline raw string value for which the first line fits on the same line then do not wrap`() {
        val code =
            // Before fixing issue 3456 the entire raw string literal was considered to be on the same line as the property declaration as
            // the newline after the opening quote was ignored, and the total length of the property declaration exceeded the max line
            // length.
            """
            // $MAX_LINE_LENGTH_MARKER                             $EOL_CHAR
            val code = $MULTILINE_STRING_QUOTE
                Fooooo
                Barrrr
                $MULTILINE_STRING_QUOTE.trimIndent()
            """.trimIndent()
        propertyWrappingRuleAssertThat(code)
            .setMaxLineLength()
            .hasNoLintViolations()
    }

    @Test
    fun `Issue 3458 - Givexn a property with a multiline raw string value for which the first line fits then do not wrap`() {
        val code =
            """
            // $MAX_LINE_LENGTH_MARKER       $EOL_CHAR
            val code = "foooooooooooooooooo" +
                "barrrrrrrrrrrrrrrrrrrrrrrrrr\nooooooo"
                    .trimEnd()
            """.trimIndent()
        propertyWrappingRuleAssertThat(code)
            .setMaxLineLength()
            .hasNoLintViolations()
    }
}
