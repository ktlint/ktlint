package io.github.ktlint.core.ruleset.standard.rules

import assertk.assertFailure
import assertk.assertThat
import assertk.assertions.hasMessage
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.isNull
import io.github.ktlint.core.rule.engine.core.api.editorconfig.CODE_STYLE_PROPERTY
import io.github.ktlint.core.rule.engine.core.api.editorconfig.CodeStyleValue
import io.github.ktlint.core.rule.engine.core.api.editorconfig.CodeStyleValue.intellij_idea
import io.github.ktlint.core.rule.engine.core.api.editorconfig.CodeStyleValue.ktlint_official
import io.github.ktlint.core.rule.engine.core.api.editorconfig.MAX_LINE_LENGTH_PROPERTY
import io.github.ktlint.core.rule.engine.core.api.editorconfig.ec4j.toPropertyWithValue
import io.github.ktlint.core.ruleset.standard.rules.ClassSignatureRule.Companion.FORCE_MULTILINE_WHEN_PARAMETER_COUNT_GREATER_OR_EQUAL_THAN_PROPERTY
import io.github.ktlint.core.test.KtLintAssertThat.Companion.EOL_CHAR
import io.github.ktlint.core.test.KtLintAssertThat.Companion.MAX_LINE_LENGTH_MARKER
import io.github.ktlint.core.test.KtLintAssertThat.Companion.assertThatRuleBuilder
import io.github.ktlint.core.test.LintViolation
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.EnumSource
import org.junit.jupiter.params.provider.ValueSource

class ClassSignatureRuleTest {
    private val classSignatureWrappingRuleAssertThat =
        assertThatRuleBuilder { ClassSignatureRule() }
            .addAdditionalRuleProvider { MaxLineLengthRule() }
            .assertThat()

    @Nested
    inner class `Given a class with a body` {
        @Test
        fun `Given a single line class signature which is smaller than or equal to the max line length, and the class is followed by a body block, then do not change the signature`() {
            val code =
                """
                // $MAX_LINE_LENGTH_MARKER        $EOL_CHAR
                class Foo(a: Any, b: Any, c: Any) {
                    // body
                }
                """.trimIndent()
            classSignatureWrappingRuleAssertThat(code)
                .setMaxLineLength()
                .withEditorConfigOverride(FORCE_MULTILINE_WHEN_PARAMETER_COUNT_GREATER_OR_EQUAL_THAN_PROPERTY to "unset")
                .hasNoLintViolations()
        }

        @Test
        fun `Given a single line class signature without parameters but exceeding the max line length then do not change the signature`() {
            val code =
                """
                // $MAX_LINE_LENGTH_MARKER   $EOL_CHAR
                class Foooooooooooooooooooooo {
                    // body
                }
                """.trimIndent()
            classSignatureWrappingRuleAssertThat(code)
                .setMaxLineLength()
                .hasNoLintViolationsExceptInAdditionalRules()
        }

        @Test
        fun `Given a single line class signature which is equal to the max line length but missing a space before the opening brace of the body block then reformat to a multiline signature`() {
            val code =
                """
                // $MAX_LINE_LENGTH_MARKER       $EOL_CHAR
                class Foo(a: Any, b: Any, c: Any){
                    // body
                }
                """.trimIndent()
            val formattedCode =
                """
                // $MAX_LINE_LENGTH_MARKER       $EOL_CHAR
                class Foo(
                    a: Any,
                    b: Any,
                    c: Any
                ) {
                    // body
                }
                """.trimIndent()
            classSignatureWrappingRuleAssertThat(code)
                .setMaxLineLength()
                .addAdditionalRuleProvider { FunctionStartOfBodySpacingRule() }
                .withEditorConfigOverride(FORCE_MULTILINE_WHEN_PARAMETER_COUNT_GREATER_OR_EQUAL_THAN_PROPERTY to "unset")
                .hasLintViolation(2, 34, "Expected a single space before class body")
                .isFormattedAs(formattedCode)
        }

        @Test
        fun `Given a single line class signature which is greater then the max line length then reformat to a multiline signature`() {
            val code =
                """
                // $MAX_LINE_LENGTH_MARKER       $EOL_CHAR
                class Foo(a: Any, b: Any, c: Any) {
                    // body
                }
                """.trimIndent()
            val formattedCode =
                """
                // $MAX_LINE_LENGTH_MARKER       $EOL_CHAR
                class Foo(
                    a: Any,
                    b: Any,
                    c: Any
                ) {
                    // body
                }
                """.trimIndent()
            classSignatureWrappingRuleAssertThat(code)
                .setMaxLineLength()
                .hasLintViolations(
                    LintViolation(2, 11, "Newline expected after opening parenthesis"),
                    LintViolation(2, 19, "Parameter should start on a newline"),
                    LintViolation(2, 27, "Parameter should start on a newline"),
                    LintViolation(2, 33, "Newline expected before closing parenthesis"),
                ).isFormattedAs(formattedCode)
        }
    }

    @Test
    fun `Given a single line class signature, without class body, which is greater then the max line length then reformat to a multiline signature`() {
        val code =
            """
            // $MAX_LINE_LENGTH_MARKER      $EOL_CHAR
            class Foo1(a: Any, b: Any, c: Any)
            @Suppress("ktlint:standard:max-line-length")
            class Foo2(a: Any, b: Any, c: Any)
            """.trimIndent()
        val formattedCode =
            """
            // $MAX_LINE_LENGTH_MARKER      $EOL_CHAR
            class Foo1(
                a: Any,
                b: Any,
                c: Any
            )
            @Suppress("ktlint:standard:max-line-length")
            class Foo2(a: Any, b: Any, c: Any)
            """.trimIndent()
        classSignatureWrappingRuleAssertThat(code)
            .setMaxLineLength()
            .withEditorConfigOverride(FORCE_MULTILINE_WHEN_PARAMETER_COUNT_GREATER_OR_EQUAL_THAN_PROPERTY to 4)
            .hasLintViolations(
                LintViolation(2, 12, "Newline expected after opening parenthesis"),
                LintViolation(2, 20, "Parameter should start on a newline"),
                LintViolation(2, 28, "Parameter should start on a newline"),
                LintViolation(2, 34, "Newline expected before closing parenthesis"),
            ).isFormattedAs(formattedCode)
    }

    @Nested
    inner class `Given a single line class signature and max-line-length is not set` {
        @Test
        fun `Given the ktlint code style`() {
            val code =
                """
                // No max line length marker!
                class Foo1(a: Any)
                class Foo2(a: Any, b: Any)
                class Foo3(a: Any, b: Any, c: Any)
                """.trimIndent()
            val formattedCode =
                """
                // No max line length marker!
                class Foo1(
                    a: Any
                )
                class Foo2(
                    a: Any,
                    b: Any
                )
                class Foo3(
                    a: Any,
                    b: Any,
                    c: Any
                )
                """.trimIndent()
            classSignatureWrappingRuleAssertThat(code)
                .withEditorConfigOverride(CODE_STYLE_PROPERTY to ktlint_official)
                .hasLintViolations(
                    LintViolation(2, 12, "Newline expected after opening parenthesis"),
                    LintViolation(2, 18, "Newline expected before closing parenthesis"),
                    LintViolation(3, 12, "Newline expected after opening parenthesis"),
                    LintViolation(3, 20, "Parameter should start on a newline"),
                    LintViolation(3, 26, "Newline expected before closing parenthesis"),
                    LintViolation(4, 12, "Newline expected after opening parenthesis"),
                    LintViolation(4, 20, "Parameter should start on a newline"),
                    LintViolation(4, 28, "Parameter should start on a newline"),
                    LintViolation(4, 34, "Newline expected before closing parenthesis"),
                ).isFormattedAs(formattedCode)
        }

        @ParameterizedTest(name = "Code style: {0}")
        @EnumSource(
            value = CodeStyleValue::class,
            mode = EnumSource.Mode.EXCLUDE,
            names = ["ktlint_official"],
        )
        fun `Given non-ktlint_official code style`(codeStyle: CodeStyleValue) {
            val code =
                """
                // No max line length marker!
                class Foo1(a: Any)
                class Foo2(a: Any, b: Any)
                class Foo3(a: Any, b: Any, c: Any)
                """.trimIndent()
            classSignatureWrappingRuleAssertThat(code)
                .withEditorConfigOverride(CODE_STYLE_PROPERTY to codeStyle)
                .hasNoLintViolations()
        }
    }

    @Test
    fun `Given a single line class signature and first parameter is annotated and class signature has a length greater than the max line length then reformat to multiline signature`() {
        val code =
            """
            // $MAX_LINE_LENGTH_MARKER        $EOL_CHAR
            class Foo(@Foo a: Any, b: Any, c: Any)
            """.trimIndent()
        val formattedCode =
            """
            // $MAX_LINE_LENGTH_MARKER        $EOL_CHAR
            class Foo(
                @Foo a: Any,
                b: Any,
                c: Any
            )
            """.trimIndent()
        classSignatureWrappingRuleAssertThat(code)
            .setMaxLineLength()
            .hasLintViolations(
                LintViolation(2, 11, "Newline expected after opening parenthesis"),
                LintViolation(2, 24, "Parameter should start on a newline"),
                LintViolation(2, 32, "Parameter should start on a newline"),
                LintViolation(2, 38, "Newline expected before closing parenthesis"),
            ).isFormattedAs(formattedCode)
    }

    @Disabled
    @Test
    fun `Given some class signatures containing at least one comment then do not reformat although the max line length is exceeded`() {
        val code =
            """
            // $MAX_LINE_LENGTH_MARKER                     $EOL_CHAR
            private /* some comment */ class Foo1(a: Any, b: Any)
            private class /* some comment */ Foo2(a: Any, b: Any)
            private class Foo3 /* some comment */ (a: Any, b: Any)
            private class Foo4( /* some comment */ a: Any, b: Any)
            private class Foo5(a /* some comment */ : Any, b: Any)
            private class Foo6(a: /* some comment */ Any, b: Any)
            private class Foo7(a: Any /* some comment */, b: Any)
            private class Foo8(a: Any, b: Any) /* some comment */
            private class Foo9(
                a: Any, // some-comment
                b: Any
            )
            """.trimIndent()
        classSignatureWrappingRuleAssertThat(code)
            .setMaxLineLength()
            .hasNoLintViolations()
    }

    @Test
    fun `Given a class signature with a newline between the last parameter in the parameter list and the closing parenthesis, but which does not fit on a single line then reformat to a proper multiline signature`() {
        val code =
            """
            // $MAX_LINE_LENGTH_MARKER $EOL_CHAR
            class Foo(a: Any, b: Any, c: Any
            )
            """.trimIndent()
        val formattedCode =
            """
            // $MAX_LINE_LENGTH_MARKER $EOL_CHAR
            class Foo(
                a: Any,
                b: Any,
                c: Any
            )
            """.trimIndent()
        classSignatureWrappingRuleAssertThat(code)
            .setMaxLineLength()
            .hasLintViolations(
                LintViolation(2, 11, "Newline expected after opening parenthesis"),
                LintViolation(2, 19, "Parameter should start on a newline"),
                LintViolation(2, 27, "Parameter should start on a newline"),
            ).isFormattedAs(formattedCode)
    }

    @Test
    fun `Given a class without parameters but with parenthesis then reformat to a proper single line signature`() {
        val code =
            """
            class Foo()
            """.trimIndent()
        val formattedCode =
            """
            class Foo
            """.trimIndent()
        classSignatureWrappingRuleAssertThat(code)
            .hasLintViolation(1, 10, "No parenthesis expected")
            .isFormattedAs(formattedCode)
    }

    @Test
    fun `Given a class without parameters but with at least one space between the parenthesis then reformat to a proper single line signature`() {
        val code =
            """
            class Foo( )
            """.trimIndent()
        val formattedCode =
            """
            class Foo
            """.trimIndent()
        classSignatureWrappingRuleAssertThat(code)
            .hasLintViolation(1, 10, "No parenthesis expected")
            .isFormattedAs(formattedCode)
    }

    @Test
    fun `Given a class without parameters but with at least one newline between the parenthesis then reformat to a proper single line signature`() {
        val code =
            """
            class Foo(

            )
            """.trimIndent()
        val formattedCode =
            """
            class Foo
            """.trimIndent()
        classSignatureWrappingRuleAssertThat(code)
            .hasLintViolation(1, 10, "No parenthesis expected")
            .isFormattedAs(formattedCode)
    }

    @Test
    fun `Given a class without parameters but containing an EOL-comment in the parameter list then do not reformat the class signature`() {
        val code =
            """
            class Foo(
                // some comment
            )
            """.trimIndent()
        classSignatureWrappingRuleAssertThat(code)
            .hasNoLintViolations()
    }

    @Test
    fun `Given a multiline class signature which actually fits on a single line then reformat the signature`() {
        val code =
            """
            // $MAX_LINE_LENGTH_MARKER      $EOL_CHAR
            class Foo(
                a: Any,
                b: Any,
                c: Any
            )
            """.trimIndent()
        val formattedCode =
            """
            // $MAX_LINE_LENGTH_MARKER      $EOL_CHAR
            class Foo(a: Any, b: Any, c: Any)
            """.trimIndent()
        classSignatureWrappingRuleAssertThat(code)
            .setMaxLineLength()
            .withEditorConfigOverride(FORCE_MULTILINE_WHEN_PARAMETER_COUNT_GREATER_OR_EQUAL_THAN_PROPERTY to "unset")
            .hasLintViolations(
                LintViolation(3, 5, "No whitespace expected between opening parenthesis and first parameter name"),
                LintViolation(4, 5, "Single whitespace expected before parameter"),
                LintViolation(5, 5, "Single whitespace expected before parameter"),
                LintViolation(5, 11, "No whitespace expected between last parameter and closing parenthesis"),
            ).isFormattedAs(formattedCode)
    }

    @Test
    fun `Given a single line class signature with a body starting on next line as it does not fit on same line then reformat to a multiline class signature`() {
        val code =
            """
            // $MAX_LINE_LENGTH_MARKER       $EOL_CHAR
            class Foo(a: Any, b: Any, c: Any)
            {
                // body
            }
            """.trimIndent()
        val formattedCode =
            """
            // $MAX_LINE_LENGTH_MARKER       $EOL_CHAR
            class Foo(
                a: Any,
                b: Any,
                c: Any
            ) {
                // body
            }
            """.trimIndent()
        classSignatureWrappingRuleAssertThat(code)
            .setMaxLineLength()
            .hasLintViolations(
                LintViolation(2, 11, "Newline expected after opening parenthesis"),
                LintViolation(2, 19, "Parameter should start on a newline"),
                LintViolation(2, 27, "Parameter should start on a newline"),
                LintViolation(2, 33, "Newline expected before closing parenthesis"),
                LintViolation(3, 1, "Expected a single space before class body"),
            ).isFormattedAs(formattedCode)
    }

    @Nested
    inner class `Given a custom setting for the minimum number of parameters which enforces a multiline signature` {
        @Test
        fun `Given a single line class signature which is smaller than or equal to the max line length and not having too many parameters then do not reformat to a multiline signature`() {
            val code =
                """
                // $MAX_LINE_LENGTH_MARKER      $EOL_CHAR
                class Foo(a: Any, b: Any, c: Any)
                """.trimIndent()
            classSignatureWrappingRuleAssertThat(code)
                .setMaxLineLength()
                .withEditorConfigOverride(FORCE_MULTILINE_WHEN_PARAMETER_COUNT_GREATER_OR_EQUAL_THAN_PROPERTY to 4)
                .hasNoLintViolations()
        }

        @Test
        fun `Given a single line class signature which is smaller than or equal to the max line length but having too many parameters then do reformat as multiline signature`() {
            val code =
                """
                // $MAX_LINE_LENGTH_MARKER      $EOL_CHAR
                class Foo(a: Any, b: Any, c: Any)
                """.trimIndent()
            val formattedCode =
                """
                // $MAX_LINE_LENGTH_MARKER      $EOL_CHAR
                class Foo(
                    a: Any,
                    b: Any,
                    c: Any
                )
                """.trimIndent()
            classSignatureWrappingRuleAssertThat(code)
                .setMaxLineLength()
                .withEditorConfigOverride(FORCE_MULTILINE_WHEN_PARAMETER_COUNT_GREATER_OR_EQUAL_THAN_PROPERTY to 3)
                .hasLintViolations(
                    LintViolation(2, 11, "Newline expected after opening parenthesis"),
                    LintViolation(2, 19, "Parameter should start on a newline"),
                    LintViolation(2, 27, "Parameter should start on a newline"),
                    LintViolation(2, 33, "Newline expected before closing parenthesis"),
                ).isFormattedAs(formattedCode)
        }

        @Test
        fun `Given a single line class signature and max line length not set but having too many parameters then do reformat as multiline signature`() {
            val code =
                """
                class Foo(a: Any, b: Any, c: Any)
                """.trimIndent()
            val formattedCode =
                """
                class Foo(
                    a: Any,
                    b: Any,
                    c: Any
                )
                """.trimIndent()
            classSignatureWrappingRuleAssertThat(code)
                .withEditorConfigOverride(FORCE_MULTILINE_WHEN_PARAMETER_COUNT_GREATER_OR_EQUAL_THAN_PROPERTY to 3)
                .hasLintViolations(
                    LintViolation(1, 11, "Newline expected after opening parenthesis"),
                    LintViolation(1, 19, "Parameter should start on a newline"),
                    LintViolation(1, 27, "Parameter should start on a newline"),
                    LintViolation(1, 33, "Newline expected before closing parenthesis"),
                ).isFormattedAs(formattedCode)
        }

        @Test
        fun `Given the ktlint_official code style then avoid wrapping of parameters by overriding property ktlint_class_signature_rule_force_multiline_when_parameter_count_greater_or_equal_than`() {
            val code =
                """
                class Foo(a: Any, b: Any, c: Any)
                """.trimIndent()
            classSignatureWrappingRuleAssertThat(code)
                .withEditorConfigOverride(CODE_STYLE_PROPERTY to ktlint_official)
                .withEditorConfigOverride(FORCE_MULTILINE_WHEN_PARAMETER_COUNT_GREATER_OR_EQUAL_THAN_PROPERTY to "unset")
                .hasNoLintViolations()
        }
    }

    @Test
    fun `Given an abstract class declaration`() {
        val code =
            """
            // $MAX_LINE_LENGTH_MARKER       $EOL_CHAR                                                                                                       $EOL_CHAR
            abstract class Foo(a: Any, b: Any)
            """.trimIndent()
        classSignatureWrappingRuleAssertThat(code)
            .setMaxLineLength()
            .withEditorConfigOverride(FORCE_MULTILINE_WHEN_PARAMETER_COUNT_GREATER_OR_EQUAL_THAN_PROPERTY to "unset")
            .hasNoLintViolations()
    }

    // The ClassSignatureWrappingRule depends on a lot of different rules to do an initial clean up. The tests below ensure that those rules
    // effectively clean up so that the FunctionSignatureWrappingRule does not need to check for it at all.
    @Nested
    inner class CleanUpByRelatedRules {
        @Test
        fun `Given a nullable type with a space before the quest then remove this space`() {
            @Suppress("ktlint:standard:string-template")
            val code =
                """
                class Foo1<B : Bar$UNEXPECTED_SPACES?>(b: B)
                class Foo2<B : List<Bar$UNEXPECTED_SPACES?>>(b: B)
                class Foo3<B : List<Bar>$UNEXPECTED_SPACES?>(b: B)
                class Foo4(b: B$UNEXPECTED_SPACES?)
                class Foo5(b: List<B$UNEXPECTED_SPACES?>)
                class Foo6(b: List<B>$UNEXPECTED_SPACES?)
                """.trimIndent()
            val formattedCode =
                """
                class Foo1<B : Bar?>(b: B)
                class Foo2<B : List<Bar?>>(b: B)
                class Foo3<B : List<Bar>?>(b: B)
                class Foo4(b: B?)
                class Foo5(b: List<B?>)
                class Foo6(b: List<B>?)
                """.trimIndent()
            classSignatureWrappingRuleAssertThat(code)
                .addAdditionalRuleProvider { NullableTypeSpacingRule() }
                .withEditorConfigOverride(FORCE_MULTILINE_WHEN_PARAMETER_COUNT_GREATER_OR_EQUAL_THAN_PROPERTY to "unset")
                .hasNoLintViolationsExceptInAdditionalRules()
                .isFormattedAs(formattedCode)
        }

        @Test
        fun `Given a class signature contains redundant spaces then ensure that those are removed before running the class signature rule`() {
            val code =
                """
                private${UNEXPECTED_SPACES}class Foo1(a: Any)
                class Foo2$UNEXPECTED_SPACES(a: Any)
                class Foo3(${UNEXPECTED_SPACES}a: Any)
                class Foo4(a$UNEXPECTED_SPACES: Any)
                class Foo5(a:${UNEXPECTED_SPACES}Any)
                class Foo6(a: Any$UNEXPECTED_SPACES, b: Any)
                class Foo7(a: Any,${UNEXPECTED_SPACES}b: Any)
                class Foo8(a: Any, b: Any$UNEXPECTED_SPACES)
                class Foo9(${UNEXPECTED_SPACES}vararg a: Any)
                class Foo10(vararg${UNEXPECTED_SPACES}a: Any)
                class Foo11$UNEXPECTED_SPACES{}
                class Foo12$UNEXPECTED_SPACES: Bar()
                class Foo13 :${UNEXPECTED_SPACES}Bar()
                class Foo14<${UNEXPECTED_SPACES}Any>
                class Foo15<Any${UNEXPECTED_SPACES}>
                class Foo16<A${UNEXPECTED_SPACES}: Any>
                class Foo17<A :${UNEXPECTED_SPACES}Any>
                class Foo18<Any${UNEXPECTED_SPACES}, Any>
                class Foo19<Any,${UNEXPECTED_SPACES}Any>
                class Foo20<Any, Any${UNEXPECTED_SPACES}>
                class Foo21<M : Map<${UNEXPECTED_SPACES}Any, Any>>(map: M)
                class Foo22<M : Map<Any${UNEXPECTED_SPACES}, Any>>(map: M)
                class Foo23<M : Map<Any,${UNEXPECTED_SPACES}Any>>(map: M)
                class Foo24<M : Map<Any, Any${UNEXPECTED_SPACES}>>(map: M)
                """.trimIndent()
            val formattedCode =
                """
                private class Foo1(a: Any)
                class Foo2(a: Any)
                class Foo3(a: Any)
                class Foo4(a: Any)
                class Foo5(a: Any)
                class Foo6(a: Any, b: Any)
                class Foo7(a: Any, b: Any)
                class Foo8(a: Any, b: Any)
                class Foo9(vararg a: Any)
                class Foo10(vararg a: Any)
                class Foo11 {}
                class Foo12 : Bar()
                class Foo13 : Bar()
                class Foo14<Any>
                class Foo15<Any>
                class Foo16<A : Any>
                class Foo17<A : Any>
                class Foo18<Any, Any>
                class Foo19<Any, Any>
                class Foo20<Any, Any>
                class Foo21<M : Map<Any, Any>>(map: M)
                class Foo22<M : Map<Any, Any>>(map: M)
                class Foo23<M : Map<Any, Any>>(map: M)
                class Foo24<M : Map<Any, Any>>(map: M)
                """.trimIndent()
            classSignatureWrappingRuleAssertThat(code)
                .addAdditionalRuleProviders(
                    { NoMultipleSpacesRule() },
                    { SpacingAroundAngleBracketsRule() },
                    { SpacingAroundParensRule() },
                    { SpacingAroundCommaRule() },
                    { SpacingAroundColonRule() },
                ).withEditorConfigOverride(FORCE_MULTILINE_WHEN_PARAMETER_COUNT_GREATER_OR_EQUAL_THAN_PROPERTY to "unset")
                .hasLintViolations(
                    LintViolation(3, 14, "No whitespace expected between opening parenthesis and first parameter name"),
                    LintViolation(7, 21, "Single whitespace expected before parameter"),
                    LintViolation(8, 26, "No whitespace expected between last parameter and closing parenthesis"),
                    LintViolation(9, 14, "No whitespace expected between opening parenthesis and first parameter name"),
                    LintViolation(11, 14, "Expected a single space before class body"),
                    LintViolation(13, 16, "Expected single space before the super type"),
                ).isFormattedAs(formattedCode)
        }

        @Test
        fun `Given a class signature missing required spaces then ensure that those are added before running the class signature rule`() {
            val code =
                """
                class Foo1(a:${NO_SPACE}Any)
                class Foo2(a: Any,${NO_SPACE}b: Any)
                class Foo3$NO_SPACE: Bar()
                class Foo4 :${NO_SPACE}Bar()
                class Foo5<A${NO_SPACE}: Any>
                class Foo6<A :${NO_SPACE}Any>
                class Foo7<Any,${NO_SPACE}Any>
                class Foo8<M : Map<Any,${NO_SPACE}Any>>(map: M)
                """.trimIndent()
            val formattedCode =
                """
                class Foo1(a: Any)
                class Foo2(a: Any, b: Any)
                class Foo3 : Bar()
                class Foo4 : Bar()
                class Foo5<A : Any>
                class Foo6<A : Any>
                class Foo7<Any, Any>
                class Foo8<M : Map<Any, Any>>(map: M)
                """.trimIndent()
            classSignatureWrappingRuleAssertThat(code)
                .addAdditionalRuleProviders(
                    { TypeParameterListSpacingRule() },
                    { FunctionStartOfBodySpacingRule() },
                    { FunctionTypeReferenceSpacingRule() },
                    { SpacingAroundColonRule() },
                    { SpacingAroundCommaRule() },
                    { SpacingAroundOperatorsRule() },
                ).withEditorConfigOverride(FORCE_MULTILINE_WHEN_PARAMETER_COUNT_GREATER_OR_EQUAL_THAN_PROPERTY to "unset")
                .hasLintViolations(
                    LintViolation(2, 19, "Single whitespace expected before parameter"),
                    LintViolation(4, 13, "Expected single space before the super type"),
                ).isFormattedAs(formattedCode)
        }
    }

    @Nested
    inner class `Given a single line class signature with an annotated parameter` {
        @Test
        fun `Given ktlint_official code style`() {
            val code =
                """
                class Foo1(a: Int, bar: String, b: Int)
                class Foo2(a: Int, @Bar bar: String, b: Int)
                """.trimIndent()
            val formattedCode =
                """
                class Foo1(a: Int, bar: String, b: Int)
                class Foo2(
                    a: Int,
                    @Bar bar: String,
                    b: Int
                )
                """.trimIndent()
            classSignatureWrappingRuleAssertThat(code)
                .withEditorConfigOverride(CODE_STYLE_PROPERTY to ktlint_official)
                .withEditorConfigOverride(FORCE_MULTILINE_WHEN_PARAMETER_COUNT_GREATER_OR_EQUAL_THAN_PROPERTY to Int.MAX_VALUE)
                .hasLintViolations(
                    LintViolation(2, 12, "Newline expected after opening parenthesis"),
                    LintViolation(2, 20, "Parameter should start on a newline"),
                    LintViolation(2, 38, "Parameter should start on a newline"),
                    LintViolation(2, 44, "Newline expected before closing parenthesis"),
                ).isFormattedAs(formattedCode)
        }

        @ParameterizedTest(name = "Code style: {0}")
        @EnumSource(
            value = CodeStyleValue::class,
            mode = EnumSource.Mode.EXCLUDE,
            names = ["ktlint_official"],
        )
        fun `Given non-ktlint_official code style`(codeStyle: CodeStyleValue) {
            val code =
                """
                class Foo(a: Int, @Bar bar: String, b: Int)
                """.trimIndent()
            classSignatureWrappingRuleAssertThat(code)
                .withEditorConfigOverride(CODE_STYLE_PROPERTY to codeStyle)
                .withEditorConfigOverride(FORCE_MULTILINE_WHEN_PARAMETER_COUNT_GREATER_OR_EQUAL_THAN_PROPERTY to Int.MAX_VALUE)
                .hasNoLintViolations()
        }
    }

    @Test
    fun `Given a class signature with an annotated parameter and the annotation is on a separate line then reformat it as a multiline signature`() {
        val code =
            """
            // $MAX_LINE_LENGTH_MARKER                $EOL_CHAR
            class Foo(@Bar
                bar: String)
            """.trimIndent()
        val formattedCode =
            """
            // $MAX_LINE_LENGTH_MARKER                $EOL_CHAR
            class Foo(
                @Bar
                bar: String
            )
            """.trimIndent()
        classSignatureWrappingRuleAssertThat(code)
            .setMaxLineLength()
            .hasLintViolations(
                LintViolation(2, 11, "Newline expected after opening parenthesis"),
                LintViolation(3, 16, "Newline expected before closing parenthesis"),
            ).isFormattedAs(formattedCode)
    }

    @Test
    fun `Given a class preceded by an annotation array`() {
        val code =
            """
            // $MAX_LINE_LENGTH_MARKER              $EOL_CHAR
            internal class Foo1(foo1: Foo, foo2: Foo)

            @Bar
            internal class Foo2(foo1: Foo, foo2: Foo)

            @[Bar]
            internal class Foo2(foo1: Foo, foo2: Foo)

            @[Bar1 Bar2 Bar3 Bar4 Bar5 Bar6 Bar7 Bar8 Bar9]
            internal class Foo2(foo1: Foo, foo2: Foo)

            @[Bar1 // some comment
            Bar2]
            internal class Foo2(foo1: Foo, foo2: Foo)
            """.trimIndent()
        classSignatureWrappingRuleAssertThat(code)
            .setMaxLineLength()
            .withEditorConfigOverride(FORCE_MULTILINE_WHEN_PARAMETER_COUNT_GREATER_OR_EQUAL_THAN_PROPERTY to "unset")
            .hasNoLintViolationsExceptInAdditionalRules()
    }

    @Test
    fun `Given a single line class signature extending another class, the primary constructor fitting on a single line but not together with the super type then wrap the super type`() {
        val code =
            """
            // $MAX_LINE_LENGTH_MARKER                     $EOL_CHAR
            class Foo1(a: Any, b: Any, c: Any) : FooBar(a, c)
            @Suppress("ktlint:standard:max-line-length")
            class Foo2(a: Any, b: Any, c: Any) : FooBar(a, c)
            """.trimIndent()
        val formattedCode =
            """
            // $MAX_LINE_LENGTH_MARKER                     $EOL_CHAR
            class Foo1(a: Any, b: Any, c: Any) :
                FooBar(a, c)
            @Suppress("ktlint:standard:max-line-length")
            class Foo2(a: Any, b: Any, c: Any) : FooBar(a, c)
            """.trimIndent()
        classSignatureWrappingRuleAssertThat(code)
            .setMaxLineLength()
            .withEditorConfigOverride(FORCE_MULTILINE_WHEN_PARAMETER_COUNT_GREATER_OR_EQUAL_THAN_PROPERTY to "unset")
            .hasLintViolation(2, 38, "Super type should start on a newline")
            .isFormattedAs(formattedCode)
    }

    @Nested
    inner class `Given a class declaration` {
        @ParameterizedTest(name = "{0}")
        @ValueSource(
            strings = [
                """
                class Foo {
                    // body
                }
                """,
                """
                class Foo(
                    val bar1: Bar,
                    val bar2: Bar,
                ) {
                    // body
                }
                """,
                """
                class Foo : FooBar("bar") {
                    // body
                }
                """,
                """
                class Foo : FooBar("bar1", "bar2") {
                    // body
                }
                """,
                """
                class Foo :
                    FooBar(
                        "bar1",
                        "bar2",
                    ) {
                    // body
                }
                """,
                """
                class Foo(
                    val bar1: Bar,
                    val bar2: Bar,
                ) : FooBar(bar1) {
                    // body
                }
                """,
                """
                class Foo(
                    val bar1: Bar,
                    val bar2: Bar,
                ) : FooBar(bar1, bar2) {
                    // body
                }
                """,
                """
                class Foo :
                    FooBar(
                        "bar1",
                        "bar2",
                    ),
                    BarFoo1,
                    BarFoo2 {
                    // body
                }
                """,
                """
                class Foo(
                    val bar1: Bar,
                    val bar2: Bar,
                ) : FooBar(bar1, bar2),
                    BarFoo1,
                    BarFoo2 {
                    // body
                }
                """,
            ],
        )
        fun `Given some correctly formatted class`(code: String) {
            classSignatureWrappingRuleAssertThat(code.trimIndent())
                .withEditorConfigOverride(CODE_STYLE_PROPERTY to ktlint_official)
                .addAdditionalRuleProvider { IndentationRule() }
                .hasNoLintViolations()
            classSignatureWrappingRuleAssertThat(code.trimIndent())
                .withEditorConfigOverride(CODE_STYLE_PROPERTY to intellij_idea)
                .withEditorConfigOverride(FORCE_MULTILINE_WHEN_PARAMETER_COUNT_GREATER_OR_EQUAL_THAN_PROPERTY to 1)
                .addAdditionalRuleProvider { IndentationRule() }
                .hasNoLintViolations()
        }

        @Test
        fun `Given a class with an empty primary constructor only`() {
            val code =
                """
                class Foo() {
                    // body
                }
                """.trimIndent()
            val formattedCode =
                """
                class Foo {
                    // body
                }
                """.trimIndent()
            classSignatureWrappingRuleAssertThat(code)
                .withEditorConfigOverride(CODE_STYLE_PROPERTY to ktlint_official)
                .addAdditionalRuleProvider { IndentationRule() }
                .isFormattedAs(formattedCode)

            // non-ktlint_official code style
            classSignatureWrappingRuleAssertThat(code)
                .withEditorConfigOverride(CODE_STYLE_PROPERTY to intellij_idea)
                .withEditorConfigOverride(FORCE_MULTILINE_WHEN_PARAMETER_COUNT_GREATER_OR_EQUAL_THAN_PROPERTY to 1)
                .addAdditionalRuleProvider { IndentationRule() }
                .isFormattedAs(formattedCode)
            classSignatureWrappingRuleAssertThat(code)
                .withEditorConfigOverride(CODE_STYLE_PROPERTY to intellij_idea)
                .addAdditionalRuleProvider { IndentationRule() }
                .isFormattedAs(formattedCode)
        }

        @Test
        fun `Given a class with primary constructor only`() {
            val code =
                """
                class Foo(val bar1: Bar) {
                    // body
                }
                """.trimIndent()
            val formattedCode =
                """
                class Foo(
                    val bar1: Bar
                ) {
                    // body
                }
                """.trimIndent()
            classSignatureWrappingRuleAssertThat(code)
                .withEditorConfigOverride(CODE_STYLE_PROPERTY to ktlint_official)
                .addAdditionalRuleProvider { IndentationRule() }
                .isFormattedAs(formattedCode)

            // non-ktlint_official code style
            classSignatureWrappingRuleAssertThat(code)
                .withEditorConfigOverride(CODE_STYLE_PROPERTY to intellij_idea)
                .withEditorConfigOverride(FORCE_MULTILINE_WHEN_PARAMETER_COUNT_GREATER_OR_EQUAL_THAN_PROPERTY to 1)
                .addAdditionalRuleProvider { IndentationRule() }
                .isFormattedAs(formattedCode)
            classSignatureWrappingRuleAssertThat(code)
                .withEditorConfigOverride(CODE_STYLE_PROPERTY to intellij_idea)
                .addAdditionalRuleProvider { IndentationRule() }
                .hasNoLintViolations()
        }

        @Test
        fun `Given a class without primary constructor and multiline super type call entry starting on same line as class keyword`() {
            val code =
                """
                class Foo : FooBar(
                    "bar1",
                    "bar2",
                ) {
                    // body
                }
                """.trimIndent()
            val formattedCode =
                """
                class Foo :
                    FooBar(
                        "bar1",
                        "bar2",
                    ) {
                    // body
                }
                """.trimIndent()
            classSignatureWrappingRuleAssertThat(code)
                .withEditorConfigOverride(CODE_STYLE_PROPERTY to ktlint_official)
                .addAdditionalRuleProvider { IndentationRule() }
                .isFormattedAs(formattedCode)

            // non-ktlint_official code style
            classSignatureWrappingRuleAssertThat(code)
                .withEditorConfigOverride(CODE_STYLE_PROPERTY to intellij_idea)
                .addAdditionalRuleProvider { IndentationRule() }
                .isFormattedAs(formattedCode)
        }

        @Test
        fun `Given a class with primary constructor and single line super type call entry on same line as class keyword`() {
            val code =
                """
                class Foo(val bar1: Bar) : FooBar(bar1) {
                    // body
                }
                """.trimIndent()
            val formattedCode =
                """
                class Foo(
                    val bar1: Bar
                ) : FooBar(bar1) {
                    // body
                }
                """.trimIndent()
            classSignatureWrappingRuleAssertThat(code)
                .withEditorConfigOverride(CODE_STYLE_PROPERTY to ktlint_official)
                .addAdditionalRuleProvider { IndentationRule() }
                .isFormattedAs(formattedCode)

            // non-ktlint_official code style
            classSignatureWrappingRuleAssertThat(code)
                .withEditorConfigOverride(CODE_STYLE_PROPERTY to intellij_idea)
                .withEditorConfigOverride(FORCE_MULTILINE_WHEN_PARAMETER_COUNT_GREATER_OR_EQUAL_THAN_PROPERTY to 1)
                .addAdditionalRuleProvider { IndentationRule() }
                .isFormattedAs(formattedCode)
            classSignatureWrappingRuleAssertThat(code)
                .withEditorConfigOverride(CODE_STYLE_PROPERTY to intellij_idea)
                .addAdditionalRuleProvider { IndentationRule() }
                .hasNoLintViolations()
        }

        @Test
        fun `Given a class with a multiline primary constructor and multiline super type call entry starting on same line as closing parenthesis of primary constructor`() {
            val code =
                """
                class Foo(
                    val bar1: Bar,
                    val bar2: Bar
                ) : FooBar(
                    bar1,
                    bar2
                ) {
                    // body
                }
                """.trimIndent()
            val formattedCodeKtlintOfficialCodeStyle =
                """
                class Foo(
                    val bar1: Bar,
                    val bar2: Bar
                ) : FooBar(
                        bar1,
                        bar2
                    ) {
                    // body
                }
                """.trimIndent()
            classSignatureWrappingRuleAssertThat(code)
                .withEditorConfigOverride(CODE_STYLE_PROPERTY to ktlint_official)
                .addAdditionalRuleProvider { IndentationRule() }
                .isFormattedAs(formattedCodeKtlintOfficialCodeStyle)

            // non-ktlint_official code style
            classSignatureWrappingRuleAssertThat(code)
                .withEditorConfigOverride(CODE_STYLE_PROPERTY to intellij_idea)
                .withEditorConfigOverride(FORCE_MULTILINE_WHEN_PARAMETER_COUNT_GREATER_OR_EQUAL_THAN_PROPERTY to 1)
                .addAdditionalRuleProvider { IndentationRule() }
                .hasNoLintViolations()
            classSignatureWrappingRuleAssertThat(code)
                .withEditorConfigOverride(CODE_STYLE_PROPERTY to intellij_idea)
                .addAdditionalRuleProvider { IndentationRule() }
                .hasNoLintViolations()
        }

        @Test
        fun `Given a class with a multiline primary constructor and multiline super type call entry starting on same line as closing parenthesis of primary constructor and followed by other super type entries`() {
            val code =
                """
                class Foo(
                    val bar1: Bar,
                    val bar2: Bar
                ) : FooBar(
                    bar1,
                    bar2
                ),
                    BarFoo1,
                    BarFoo2 {
                    // body
                }
                """.trimIndent()
            val formattedCodeKtlintOfficialCodeStyle =
                """
                class Foo(
                    val bar1: Bar,
                    val bar2: Bar
                ) : FooBar(
                        bar1,
                        bar2
                    ),
                    BarFoo1,
                    BarFoo2 {
                    // body
                }
                """.trimIndent()
            classSignatureWrappingRuleAssertThat(code)
                .withEditorConfigOverride(CODE_STYLE_PROPERTY to ktlint_official)
                .addAdditionalRuleProvider { IndentationRule() }
                .isFormattedAs(formattedCodeKtlintOfficialCodeStyle)

            // non-ktlint_official code style
            classSignatureWrappingRuleAssertThat(code)
                .withEditorConfigOverride(CODE_STYLE_PROPERTY to intellij_idea)
                .withEditorConfigOverride(FORCE_MULTILINE_WHEN_PARAMETER_COUNT_GREATER_OR_EQUAL_THAN_PROPERTY to 1)
                .addAdditionalRuleProvider { IndentationRule() }
                .hasNoLintViolations()
            classSignatureWrappingRuleAssertThat(code)
                .withEditorConfigOverride(CODE_STYLE_PROPERTY to intellij_idea)
                .addAdditionalRuleProvider { IndentationRule() }
                .hasNoLintViolations()
        }

        @Test
        fun `Given a class without primary constructor, a super type call entry with a single parameter, and multiple super type entries on a single line`() {
            val code =
                """
                class Foo : FooBar("bar"), BarFoo1, BarFoo2 {
                    // body
                }
                """.trimIndent()
            val formattedCode =
                """
                class Foo :
                    FooBar("bar"),
                    BarFoo1,
                    BarFoo2 {
                    // body
                }
                """.trimIndent()
            classSignatureWrappingRuleAssertThat(code)
                .withEditorConfigOverride(CODE_STYLE_PROPERTY to ktlint_official)
                .addAdditionalRuleProvider { IndentationRule() }
                .isFormattedAs(formattedCode)

            // non-ktlint_official code style
            classSignatureWrappingRuleAssertThat(code)
                .withEditorConfigOverride(CODE_STYLE_PROPERTY to intellij_idea)
                .withEditorConfigOverride(FORCE_MULTILINE_WHEN_PARAMETER_COUNT_GREATER_OR_EQUAL_THAN_PROPERTY to 1)
                .addAdditionalRuleProvider { IndentationRule() }
                .isFormattedAs(formattedCode)
            classSignatureWrappingRuleAssertThat(code)
                .withEditorConfigOverride(CODE_STYLE_PROPERTY to intellij_idea)
                .addAdditionalRuleProvider { IndentationRule() }
                .isFormattedAs(formattedCode)
        }

        @Test
        fun `Given a class without primary constructor, a super type call entry with a multiple parameters, and multiple super type entries on a single line`() {
            val code =
                """
                class Foo : FooBar("bar1", "bar2"), BarFoo1, BarFoo2 {
                    // body
                }
                """.trimIndent()
            val formattedCode =
                """
                class Foo :
                    FooBar("bar1", "bar2"),
                    BarFoo1,
                    BarFoo2 {
                    // body
                }
                """.trimIndent()
            classSignatureWrappingRuleAssertThat(code)
                .withEditorConfigOverride(CODE_STYLE_PROPERTY to ktlint_official)
                .addAdditionalRuleProvider { IndentationRule() }
                .isFormattedAs(formattedCode)

            // non-ktlint_official code style
            classSignatureWrappingRuleAssertThat(code)
                .withEditorConfigOverride(CODE_STYLE_PROPERTY to intellij_idea)
                .withEditorConfigOverride(FORCE_MULTILINE_WHEN_PARAMETER_COUNT_GREATER_OR_EQUAL_THAN_PROPERTY to 1)
                .addAdditionalRuleProvider { IndentationRule() }
                .isFormattedAs(formattedCode)
            classSignatureWrappingRuleAssertThat(code)
                .withEditorConfigOverride(CODE_STYLE_PROPERTY to intellij_idea)
                .addAdditionalRuleProvider { IndentationRule() }
                .isFormattedAs(formattedCode)
        }

        @Test
        fun `Given a class without primary constructor, a multiline super type call entry starting on the same line as the class keyword and at least one other super type entry`() {
            val code =
                """
                class Foo : FooBar(
                    "bar1",
                    "bar2"
                ), BarFoo1, BarFoo2 {
                    // body
                }
                """.trimIndent()
            val formattedCode =
                """
                class Foo :
                    FooBar(
                        "bar1",
                        "bar2"
                    ),
                    BarFoo1,
                    BarFoo2 {
                    // body
                }
                """.trimIndent()
            classSignatureWrappingRuleAssertThat(code)
                .withEditorConfigOverride(CODE_STYLE_PROPERTY to ktlint_official)
                .addAdditionalRuleProvider { IndentationRule() }
                .isFormattedAs(formattedCode)

            // non-ktlint_official code style
            classSignatureWrappingRuleAssertThat(code)
                .withEditorConfigOverride(CODE_STYLE_PROPERTY to intellij_idea)
                .withEditorConfigOverride(FORCE_MULTILINE_WHEN_PARAMETER_COUNT_GREATER_OR_EQUAL_THAN_PROPERTY to 1)
                .addAdditionalRuleProvider { IndentationRule() }
                .isFormattedAs(formattedCode)
            classSignatureWrappingRuleAssertThat(code)
                .withEditorConfigOverride(CODE_STYLE_PROPERTY to intellij_idea)
                .addAdditionalRuleProvider { IndentationRule() }
                .isFormattedAs(formattedCode)
        }

        @Test
        fun `Given a class without primary constructor, a multiline super type call entry starting on a separate line but indented as non-ktlint_official and at least one other super type entry`() {
            val code =
                """
                class Foo : FooBar(
                    "bar1",
                    "bar2"
                ),
                    BarFoo1,
                    BarFoo2 {
                    // body
                }
                """.trimIndent()
            val formattedCode =
                """
                class Foo :
                    FooBar(
                        "bar1",
                        "bar2"
                    ),
                    BarFoo1,
                    BarFoo2 {
                    // body
                }
                """.trimIndent()
            classSignatureWrappingRuleAssertThat(code)
                .withEditorConfigOverride(CODE_STYLE_PROPERTY to ktlint_official)
                .addAdditionalRuleProvider { IndentationRule() }
                .isFormattedAs(formattedCode)

            // non-ktlint_official code style
            classSignatureWrappingRuleAssertThat(code)
                .withEditorConfigOverride(CODE_STYLE_PROPERTY to intellij_idea)
                .withEditorConfigOverride(FORCE_MULTILINE_WHEN_PARAMETER_COUNT_GREATER_OR_EQUAL_THAN_PROPERTY to 1)
                .addAdditionalRuleProvider { IndentationRule() }
                .isFormattedAs(formattedCode)
            classSignatureWrappingRuleAssertThat(code)
                .withEditorConfigOverride(CODE_STYLE_PROPERTY to intellij_idea)
                .addAdditionalRuleProvider { IndentationRule() }
                .isFormattedAs(formattedCode)
        }

        @Test
        fun `Given a class with an explicit multiline constructor, a multiline super type call entry starting on a separate line but indented as non-ktlint_official and at least one other super type entry`() {
            val code =
                """
                class Foo
                constructor(
                    val bar1: Bar,
                    val bar2: Bar,
                ) : FooBar(bar1, bar2),
                    BarFoo1,
                    BarFoo2 {
                    // body
                }
                """.trimIndent()
            val formattedCode =
                """
                class Foo
                    constructor(
                        val bar1: Bar,
                        val bar2: Bar,
                    ) : FooBar(bar1, bar2),
                        BarFoo1,
                        BarFoo2 {
                        // body
                    }
                """.trimIndent()

            classSignatureWrappingRuleAssertThat(code)
                .withEditorConfigOverride(CODE_STYLE_PROPERTY to ktlint_official)
                .addAdditionalRuleProvider { IndentationRule() }
                .isFormattedAs(formattedCode)

            // non-ktlint_official code style
            classSignatureWrappingRuleAssertThat(code)
                .withEditorConfigOverride(CODE_STYLE_PROPERTY to intellij_idea)
                .withEditorConfigOverride(FORCE_MULTILINE_WHEN_PARAMETER_COUNT_GREATER_OR_EQUAL_THAN_PROPERTY to 1)
                .addAdditionalRuleProvider { IndentationRule() }
                .hasNoLintViolations()
            classSignatureWrappingRuleAssertThat(code)
                .withEditorConfigOverride(CODE_STYLE_PROPERTY to intellij_idea)
                .addAdditionalRuleProvider { IndentationRule() }
                .hasNoLintViolations()
        }

        @Test
        fun `Given a class with an explicit multiline constructor, a multiline super type call entry starting on a separate line below the colon and at least one other super type entry`() {
            val code =
                """
                class Foo
                constructor(
                    val bar1: Bar,
                    val bar2: Bar,
                ) :
                    FooBar(bar1, bar2),
                    BarFoo1,
                    BarFoo2 {
                    // body
                }
                """.trimIndent()
            val formattedCodeKtlintOfficial =
                """
                class Foo
                    constructor(
                        val bar1: Bar,
                        val bar2: Bar,
                    ) : FooBar(bar1, bar2),
                        BarFoo1,
                        BarFoo2 {
                        // body
                    }
                """.trimIndent()
            val formattedCodeNonKtlintOfficial =
                """
                class Foo
                constructor(
                    val bar1: Bar,
                    val bar2: Bar,
                ) : FooBar(bar1, bar2),
                    BarFoo1,
                    BarFoo2 {
                    // body
                }
                """.trimIndent()
            classSignatureWrappingRuleAssertThat(code)
                .withEditorConfigOverride(CODE_STYLE_PROPERTY to ktlint_official)
                .addAdditionalRuleProvider { IndentationRule() }
                .isFormattedAs(formattedCodeKtlintOfficial)

            // non-ktlint_official code style
            classSignatureWrappingRuleAssertThat(code)
                .withEditorConfigOverride(CODE_STYLE_PROPERTY to intellij_idea)
                .withEditorConfigOverride(FORCE_MULTILINE_WHEN_PARAMETER_COUNT_GREATER_OR_EQUAL_THAN_PROPERTY to 1)
                .addAdditionalRuleProvider { IndentationRule() }
                .isFormattedAs(formattedCodeNonKtlintOfficial)
            classSignatureWrappingRuleAssertThat(code)
                .withEditorConfigOverride(CODE_STYLE_PROPERTY to intellij_idea)
                .addAdditionalRuleProvider { IndentationRule() }
                .isFormattedAs(formattedCodeNonKtlintOfficial)
        }
    }

    @Test
    fun `Given a multiline class signature extending another class but fitting on a single line then format as single line signature`() {
        val code =
            """
            // $MAX_LINE_LENGTH_MARKER                     $EOL_CHAR
            class Foo(
                a: Any,
                b: Any,
                c: Any
            ) : FooBar(a, c)
            """.trimIndent()
        val formattedCode =
            """
            // $MAX_LINE_LENGTH_MARKER                     $EOL_CHAR
            class Foo(a: Any, b: Any, c: Any) : FooBar(a, c)
            """.trimIndent()
        classSignatureWrappingRuleAssertThat(code)
            .setMaxLineLength()
            .withEditorConfigOverride(FORCE_MULTILINE_WHEN_PARAMETER_COUNT_GREATER_OR_EQUAL_THAN_PROPERTY to "unset")
            .hasLintViolations(
                LintViolation(3, 5, "No whitespace expected between opening parenthesis and first parameter name"),
                LintViolation(4, 5, "Single whitespace expected before parameter"),
                LintViolation(5, 5, "Single whitespace expected before parameter"),
                LintViolation(5, 11, "No whitespace expected between last parameter and closing parenthesis"),
            ).isFormattedAs(formattedCode)
    }

    @Nested
    inner class `Given a single line class signature extending another class and some interfaces and fitting on a single line` {
        @Test
        fun `Given that the super type call is the first super type`() {
            val code =
                """
                // $MAX_LINE_LENGTH_MARKER                                 $EOL_CHAR
                class Foo(a: Any, b: Any, c: Any) : Bar1(a, c), Bar2, Bar3 {
                    // body
                }
                """.trimIndent()
            val formattedCode =
                """
                // $MAX_LINE_LENGTH_MARKER                                 $EOL_CHAR
                class Foo(a: Any, b: Any, c: Any) :
                    Bar1(a, c),
                    Bar2,
                    Bar3 {
                    // body
                }
                """.trimIndent()
            classSignatureWrappingRuleAssertThat(code)
                .setMaxLineLength()
                .withEditorConfigOverride(FORCE_MULTILINE_WHEN_PARAMETER_COUNT_GREATER_OR_EQUAL_THAN_PROPERTY to "unset")
                .hasLintViolations(
                    LintViolation(2, 37, "Super type should start on a newline"),
                    LintViolation(2, 49, "Super type should start on a newline"),
                    LintViolation(2, 55, "Super type should start on a newline"),
                ).isFormattedAs(formattedCode)
        }

        @Test
        fun `Given that the super type call is not the first or last super type`() {
            val code =
                """
                // $MAX_LINE_LENGTH_MARKER                                 $EOL_CHAR
                class Foo(a: Any, b: Any, c: Any) : Bar1, Bar2(a, c), Bar3 {
                    // body
                }
                """.trimIndent()
            val formattedCode =
                """
                // $MAX_LINE_LENGTH_MARKER                                 $EOL_CHAR
                class Foo(a: Any, b: Any, c: Any) :
                    Bar2(a, c),
                    Bar1,
                    Bar3 {
                    // body
                }
                """.trimIndent()
            classSignatureWrappingRuleAssertThat(code)
                .setMaxLineLength()
                .withEditorConfigOverride(FORCE_MULTILINE_WHEN_PARAMETER_COUNT_GREATER_OR_EQUAL_THAN_PROPERTY to "unset")
                .hasLintViolations(
                    LintViolation(2, 37, "Super type should start on a newline"),
                    LintViolation(2, 43, "Super type call must be first super type"),
                    LintViolation(2, 43, "Super type should start on a newline"),
                    LintViolation(2, 55, "Super type should start on a newline"),
                ).isFormattedAs(formattedCode)
        }

        @Test
        fun `Given that the super type call is the last super type`() {
            val code =
                """
                // $MAX_LINE_LENGTH_MARKER                                 $EOL_CHAR
                class Foo(a: Any, b: Any, c: Any) : Bar1, Bar2, Bar3(a, c) {
                    // body
                }
                """.trimIndent()
            val formattedCode =
                """
                // $MAX_LINE_LENGTH_MARKER                                 $EOL_CHAR
                class Foo(a: Any, b: Any, c: Any) :
                    Bar3(a, c),
                    Bar1,
                    Bar2 {
                    // body
                }
                """.trimIndent()
            classSignatureWrappingRuleAssertThat(code)
                .setMaxLineLength()
                .withEditorConfigOverride(FORCE_MULTILINE_WHEN_PARAMETER_COUNT_GREATER_OR_EQUAL_THAN_PROPERTY to "unset")
                .hasLintViolations(
                    LintViolation(2, 37, "Super type should start on a newline"),
                    LintViolation(2, 43, "Super type should start on a newline"),
                    LintViolation(2, 49, "Super type call must be first super type"),
                    LintViolation(2, 49, "Super type should start on a newline"),
                ).isFormattedAs(formattedCode)
        }
    }

    @Test
    fun `Given a single line class signature having a super type by delegate which does not fit on a single line then wrap the super type`() {
        val code =
            """
            // $MAX_LINE_LENGTH_MARKER      $EOL_CHAR
            class Foo(bar: Bar) : Bar by bar {
                // body
            }
            """.trimIndent()
        val formattedCode =
            """
            // $MAX_LINE_LENGTH_MARKER      $EOL_CHAR
            class Foo(bar: Bar) :
                Bar by bar {
                // body
            }
            """.trimIndent()
        classSignatureWrappingRuleAssertThat(code)
            .setMaxLineLength()
            .withEditorConfigOverride(FORCE_MULTILINE_WHEN_PARAMETER_COUNT_GREATER_OR_EQUAL_THAN_PROPERTY to "unset")
            .hasLintViolation(2, 23, "Super type should start on a newline")
            .isFormattedAs(formattedCode)
    }

    @Test
    fun `Given a class having a multiline super type list`() {
        val code =
            """
            // $MAX_LINE_LENGTH_MARKER             $EOL_CHAR
            class Foo(a: Any, b: Any, c: Any) : Bar(
                a,
                c
            ) {
                // body
            }
            """.trimIndent()
        val formattedCode =
            """
            // $MAX_LINE_LENGTH_MARKER             $EOL_CHAR
            class Foo(a: Any, b: Any, c: Any) :
                Bar(
                    a,
                    c
                ) {
                // body
            }
            """.trimIndent()
        classSignatureWrappingRuleAssertThat(code)
            .setMaxLineLength()
            .addAdditionalRuleProvider { IndentationRule() }
            .withEditorConfigOverride(FORCE_MULTILINE_WHEN_PARAMETER_COUNT_GREATER_OR_EQUAL_THAN_PROPERTY to "unset")
            .hasLintViolation(2, 37, "Super type should start on a newline")
            .isFormattedAs(formattedCode)
    }

    @Test
    fun `Given that the primary constructor parameters have to be wrapped and that the class has a multiline super type call entry then keep start of body on same line as end of super type call entry`() {
        val code =
            """
            // $MAX_LINE_LENGTH_MARKER                          $EOL_CHAR
            class Foo(a: Any, b: Any, c: Any) : Bar(
                a,
                c
            ) {
                // body
            }
            """.trimIndent()
        val formattedCode =
            """
            // $MAX_LINE_LENGTH_MARKER                          $EOL_CHAR
            class Foo(
                a: Any,
                b: Any,
                c: Any
            ) : Bar(
                    a,
                    c
                ) {
                // body
            }
            """.trimIndent()
        classSignatureWrappingRuleAssertThat(code)
            .setMaxLineLength()
            .addAdditionalRuleProvider { IndentationRule() }
            .withEditorConfigOverride(FORCE_MULTILINE_WHEN_PARAMETER_COUNT_GREATER_OR_EQUAL_THAN_PROPERTY to 3)
            .hasLintViolations(
                LintViolation(2, 11, "Newline expected after opening parenthesis"),
                LintViolation(2, 19, "Parameter should start on a newline"),
                LintViolation(2, 27, "Parameter should start on a newline"),
                LintViolation(2, 33, "Newline expected before closing parenthesis"),
            ).isFormattedAs(formattedCode)
    }

    @Test
    fun `Given a super type call entry with arguments which after wrapping to the next line does not fit on that line then keep start of body on same line as end of super type call entry`() {
        val code =
            """
            // $MAX_LINE_LENGTH_MARKER                            $EOL_CHAR
            // Note that the super type call does not fit the line after it has been wrapped
            // ...Foo(a: Any, b: Any, c: Any) :
            //  FooBar(a, "longggggggggggggggggggggggggggggggggggg")
            class Foo(a: Any, b: Any, c: Any) : FooBar(a, "longggggggggggggggggggggggggggggggggggg") {
                // body
            }
            """.trimIndent()
        val formattedCode =
            """
            // $MAX_LINE_LENGTH_MARKER                            $EOL_CHAR
            // Note that the super type call does not fit the line after it has been wrapped
            // ...Foo(a: Any, b: Any, c: Any) :
            //  FooBar(a, "longggggggggggggggggggggggggggggggggggg")
            class Foo(a: Any, b: Any, c: Any) :
                FooBar(
                    a,
                    "longggggggggggggggggggggggggggggggggggg"
                ) {
                // body
            }
            """.trimIndent()
        classSignatureWrappingRuleAssertThat(code)
            .setMaxLineLength()
            .addAdditionalRuleProvider { ValueArgumentCommentRule() }
            .addAdditionalRuleProvider { ArgumentListWrappingRule() }
            .addAdditionalRuleProvider { WrappingRule() }
            .withEditorConfigOverride(FORCE_MULTILINE_WHEN_PARAMETER_COUNT_GREATER_OR_EQUAL_THAN_PROPERTY to 4)
            .hasLintViolation(5, 37, "Super type should start on a newline")
            .isFormattedAs(formattedCode)
    }

    @Nested
    inner class `Property ktlint_class_signature_rule_force_multiline_when_parameter_count_greater_or_equal_than` {
        val propertyMapper = FORCE_MULTILINE_WHEN_PARAMETER_COUNT_GREATER_OR_EQUAL_THAN_PROPERTY.propertyMapper!!

        @ParameterizedTest(name = "Code style: {0}")
        @EnumSource(CodeStyleValue::class)
        fun `Given a null property then the property mapper returns null`(codeStyleValue: CodeStyleValue) {
            val actual = propertyMapper(null, codeStyleValue)

            assertThat(actual).isNull()
        }

        @ParameterizedTest(name = "Code style: {0}")
        @EnumSource(CodeStyleValue::class)
        fun `Given a property which is unset then the property mapper returns max integer which is set as the default value`(
            codeStyleValue: CodeStyleValue,
        ) {
            val property = FORCE_MULTILINE_WHEN_PARAMETER_COUNT_GREATER_OR_EQUAL_THAN_PROPERTY.toPropertyWithValue("unset")

            val actual = propertyMapper(property, codeStyleValue)

            assertThat(actual).isEqualTo(Int.MAX_VALUE)
        }

        @ParameterizedTest(name = "Code style: {0}")
        @EnumSource(CodeStyleValue::class)
        fun `Given a valid string value then the property mapper returns the integer value`(codeStyleValue: CodeStyleValue) {
            val someValue = 123
            val property = FORCE_MULTILINE_WHEN_PARAMETER_COUNT_GREATER_OR_EQUAL_THAN_PROPERTY.toPropertyWithValue(someValue.toString())

            val actual = propertyMapper(property, codeStyleValue)

            assertThat(actual).isEqualTo(someValue)
        }

        @ParameterizedTest(name = "Code style: {0}")
        @EnumSource(CodeStyleValue::class)
        fun `Given a negative value then the property mapper throws and exception`(codeStyleValue: CodeStyleValue) {
            val someNegativeValue = "-1"
            val property = FORCE_MULTILINE_WHEN_PARAMETER_COUNT_GREATER_OR_EQUAL_THAN_PROPERTY.toPropertyWithValue(someNegativeValue)

            assertFailure { propertyMapper(property, codeStyleValue) }
                .isInstanceOf<RuntimeException>()
                .hasMessage(
                    "Property 'ktlint_class_signature_rule_force_multiline_when_parameter_count_greater_or_equal_than' expects a " +
                        "positive integer; found '$someNegativeValue'",
                )
        }

        @ParameterizedTest(name = "Code style: {0}")
        @EnumSource(CodeStyleValue::class)
        fun `Given a value bigger than max integer then the property mapper throws and exception`(codeStyleValue: CodeStyleValue) {
            val someValueBiggerThanMaxInt = (1L + Int.MAX_VALUE).toString()
            val property =
                FORCE_MULTILINE_WHEN_PARAMETER_COUNT_GREATER_OR_EQUAL_THAN_PROPERTY.toPropertyWithValue(someValueBiggerThanMaxInt)

            assertFailure { propertyMapper(property, codeStyleValue) }
                .isInstanceOf<RuntimeException>()
                .hasMessage(
                    "Property 'ktlint_class_signature_rule_force_multiline_when_parameter_count_greater_or_equal_than' expects an " +
                        "integer. The parsed '$someValueBiggerThanMaxInt' is not an integer.",
                )
        }

        @ParameterizedTest(name = "Code style: {0}")
        @EnumSource(CodeStyleValue::class)
        fun `Given a invalid string value then the property mapper returns the integer value`(codeStyleValue: CodeStyleValue) {
            val property =
                FORCE_MULTILINE_WHEN_PARAMETER_COUNT_GREATER_OR_EQUAL_THAN_PROPERTY.toPropertyWithValue("some-invalid-value")

            assertFailure { propertyMapper(property, codeStyleValue) }
                .isInstanceOf<RuntimeException>()
                .hasMessage(
                    "Property 'ktlint_class_signature_rule_force_multiline_when_parameter_count_greater_or_equal_than' expects an " +
                        "integer. The parsed 'some-invalid-value' is not an integer.",
                )
        }

        @ParameterizedTest(name = "Input value: {0}, output value: {1}")
        @CsvSource(
            value = [
                "1, 1",
                "${Int.MAX_VALUE}, unset",
            ],
        )
        fun `Given a property with an integer value than write that property`(
            inputValue: Int,
            expectedOutputValue: String,
        ) {
            val actual = FORCE_MULTILINE_WHEN_PARAMETER_COUNT_GREATER_OR_EQUAL_THAN_PROPERTY.propertyWriter(inputValue)

            assertThat(actual).isEqualTo(expectedOutputValue)
        }
    }

    @Test
    fun `Given a single line primary constructor which needs to be wrapped followed by a super type on the next line then merge supertype with line containing the closing parenthesis of the constructor`() {
        val code =
            """
            class Foo(bar: Bar) :
                FooBar(bar) {
                    fun doSomething() {}
                }
            """.trimIndent()
        val formattedCode =
            """
            class Foo(
                bar: Bar
            ) : FooBar(bar) {
                fun doSomething() {}
            }
            """.trimIndent()
        classSignatureWrappingRuleAssertThat(code)
            .addAdditionalRuleProvider { IndentationRule() }
            .withEditorConfigOverride(FORCE_MULTILINE_WHEN_PARAMETER_COUNT_GREATER_OR_EQUAL_THAN_PROPERTY to 1)
            .hasLintViolations(
                LintViolation(1, 11, "Newline expected after opening parenthesis"),
                LintViolation(1, 19, "Newline expected before closing parenthesis"),
                LintViolation(2, 5, "Expected single space before the super type"),
            ).isFormattedAs(formattedCode)
    }

    @Test
    fun `Given a class with an explicit constructor not having arguments`() {
        val code =
            """
            class Foo constructor() {
                fun bar()
            }
            """.trimIndent()
        classSignatureWrappingRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `Given a class with an annotated super type call entry`() {
        val code =
            """
            class Foo(
                bar: Bar,
            ) : // Some comment
                @Unused
                FooBar()
            """.trimIndent()
        classSignatureWrappingRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `Given a class with an annotated super type entry`() {
        val code =
            """
            class Foo(
                bar: Bar,
            ) : // Some comment
                @Unused
                FooBar(),
                FooBar2
            """.trimIndent()
        classSignatureWrappingRuleAssertThat(code)
            .hasNoLintViolations()
    }

    @Test
    fun `Issue 2425 - Given an expected annotation class then the empty constructor may not be removed`() {
        val code =
            """
            @OptIn(ExperimentalMultiplatform::class)
            expect annotation class Parcelize()
            """.trimIndent()
        classSignatureWrappingRuleAssertThat(code)
            .hasNoLintViolations()
    }

    @Nested
    inner class `Issue 2630 - White space in super type call entry` {
        @Test
        fun `Issue 2630 - Given a super type call entry without type argument list`() {
            val code =
                """
                class Foo1 : Bar("foobar")
                class Foo2 : Bar ("foobar")
                class Foo3 : Bar
                    ("foobar")
                """.trimIndent()
            val formattedCode =
                """
                class Foo1 : Bar("foobar")
                class Foo2 : Bar("foobar")
                class Foo3 : Bar("foobar")
                """.trimIndent()
            classSignatureWrappingRuleAssertThat(code)
                .hasLintViolations(
                    LintViolation(2, 17, "No whitespace expected"),
                    LintViolation(3, 14, "Super type should start on a newline"),
                    LintViolation(3, 17, "No whitespace expected"),
                ).isFormattedAs(formattedCode)
        }

        @Test
        fun `Issue 2630 - Given a super type call entry with type argument list`() {
            val code =
                """
                class Foo1 : Bar<String>("foobar")
                class Foo2 : Bar<String> ("foobar")
                class Foo3 : Bar<String>
                    ("foobar")
                """.trimIndent()
            val formattedCode =
                """
                class Foo1 : Bar<String>("foobar")
                class Foo2 : Bar<String>("foobar")
                class Foo3 : Bar<String>("foobar")
                """.trimIndent()
            classSignatureWrappingRuleAssertThat(code)
                .hasLintViolations(
                    LintViolation(2, 25, "No whitespace expected"),
                    LintViolation(3, 14, "Super type should start on a newline"),
                    LintViolation(3, 25, "No whitespace expected"),
                ).isFormattedAs(formattedCode)
        }
    }

    @Nested
    inner class `Issue 2689 - Given a multiline class containing an EOL comment` {
        @Test
        fun `Issue 2689 - Given ktlint_official code style`() {
            val code =
                """
                data class Foo(
                    // Foo
                    val foo: String,
                    val bar: String,
                )
                """.trimIndent()
            classSignatureWrappingRuleAssertThat(code)
                // Unset the property that forces the class always to be written as multiline signature
                .withEditorConfigOverride(FORCE_MULTILINE_WHEN_PARAMETER_COUNT_GREATER_OR_EQUAL_THAN_PROPERTY to "unset")
                .hasNoLintViolations()
        }

        @Test
        fun `Issue 2689 - Given android_studio code style`() {
            val code =
                """
                data class Foo(
                    // Foo
                    val foo: String,
                    val bar: String,
                )
                """.trimIndent()
            classSignatureWrappingRuleAssertThat(code)
                .withEditorConfigOverride(CODE_STYLE_PROPERTY to CodeStyleValue.android_studio)
                .hasNoLintViolations()
        }

        @Test
        fun `Issue 2689 - Given intellij_idea code style`() {
            val code =
                """
                data class Foo(
                    // Foo
                    val foo: String,
                    val bar: String,
                )
                """.trimIndent()
            classSignatureWrappingRuleAssertThat(code)
                .withEditorConfigOverride(CODE_STYLE_PROPERTY to intellij_idea)
                // Set max_line_length as other the class signature would not be rewritten to single line
                .withEditorConfigOverride(MAX_LINE_LENGTH_PROPERTY to 999)
                .hasNoLintViolations()
        }
    }

    @Test
    fun `Issue 2690 - Given an empty primary constructor and secondary constructor with delegation reference`() {
        val code =
            """
            class Foo() {
                constructor(foo: String) : this() {
                    // N/A
                }
            }
            """.trimIndent()
        classSignatureWrappingRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `Issue 2755 - Given a class preceded by an annotation, and EOL comment, and another class modifier then ignore the EOL comment in determination of class signature length`() {
        val code =
            """
            // $MAX_LINE_LENGTH_MARKER                           $EOL_CHAR
            @Suppress("UnnecessaryAbstractClass") // some comment
            abstract class Bar(val baz1: String, val baz2: String)
            """.trimIndent()
        classSignatureWrappingRuleAssertThat(code)
            .setMaxLineLength()
            .withEditorConfigOverride(FORCE_MULTILINE_WHEN_PARAMETER_COUNT_GREATER_OR_EQUAL_THAN_PROPERTY to "unset")
            .hasNoLintViolations()
    }

    @Test
    fun `Issue 3293 - Given an EOL comment between class name and super class then do not join comment and super class as this leads to a compilation error`() {
        val code =
            """
            class Foo : // comment
                Bar {
                override fun fooBar() = "foobar"
            }

            interface Bar {
                fun fooBar(): String
            }
            """.trimIndent()
        classSignatureWrappingRuleAssertThat(code).hasNoLintViolations()
    }

    private companion object {
        const val UNEXPECTED_SPACES = "  "
        const val NO_SPACE = ""
    }
}
