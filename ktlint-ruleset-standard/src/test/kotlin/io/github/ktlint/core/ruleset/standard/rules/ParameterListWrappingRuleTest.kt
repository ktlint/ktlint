package io.github.ktlint.core.ruleset.standard.rules

import io.github.ktlint.core.rule.engine.core.api.editorconfig.CODE_STYLE_PROPERTY
import io.github.ktlint.core.rule.engine.core.api.editorconfig.CodeStyleValue
import io.github.ktlint.core.rule.engine.core.api.editorconfig.CodeStyleValue.KTLINT_OFFICIAL
import io.github.ktlint.core.test.KtLintAssertThat.Companion.EOL_CHAR
import io.github.ktlint.core.test.KtLintAssertThat.Companion.MAX_LINE_LENGTH_MARKER
import io.github.ktlint.core.test.KtLintAssertThat.Companion.assertThatRuleBuilder
import io.github.ktlint.core.test.LintViolation
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource

class ParameterListWrappingRuleTest {
    private val parameterListWrappingRuleAssertThat =
        assertThatRuleBuilder { ParameterListWrappingRule() }
            .addAdditionalRuleProvider { MaxLineLengthRule() }
            // Keep formatted code readable
            .addAdditionalRuleProvider { IndentationRule() }
            .assertThat()

    @Test
    fun `Given a class with parameters on multiple lines then put each parameter and closing parenthesis on a separate line`() {
        val code =
            """
            class ClassA(paramA: String, paramB: String,
                         paramC: String)
            """.trimIndent()
        val formattedCode =
            """
            class ClassA(
                paramA: String,
                paramB: String,
                paramC: String
            )
            """.trimIndent()
        parameterListWrappingRuleAssertThat(code)
            .hasLintViolations(
                LintViolation(1, 14, "Parameter should start on a newline"),
                LintViolation(1, 30, "Parameter should start on a newline"),
                LintViolation(2, 28, """Missing newline before ")""""),
            ).isFormattedAs(formattedCode)
    }

    @Test
    fun `Given a single line class header which exceeds the maximum line length`() {
        val code =
            """
            // $MAX_LINE_LENGTH_MARKER                                $EOL_CHAR
            class ClassA(paramA: String, paramB: String, paramC: String)
            """.trimIndent()
        val formattedCode =
            """
            // $MAX_LINE_LENGTH_MARKER                                $EOL_CHAR
            class ClassA(
                paramA: String,
                paramB: String,
                paramC: String
            )
            """.trimIndent()
        parameterListWrappingRuleAssertThat(code)
            .setMaxLineLength()
            .hasLintViolations(
                LintViolation(2, 14, "Parameter should start on a newline"),
                LintViolation(2, 30, "Parameter should start on a newline"),
                LintViolation(2, 46, "Parameter should start on a newline"),
                LintViolation(2, 60, """Missing newline before ")""""),
            ).isFormattedAs(formattedCode)
    }

    @Test
    fun `Given a class header with a very long name and without parameters which exceeds the maximum line length then do not change the class header`() {
        val code =
            """
            // $MAX_LINE_LENGTH_MARKER    $EOL_CHAR
            class ClassAWithALoooooongName()
            """.trimIndent()
        parameterListWrappingRuleAssertThat(code)
            .setMaxLineLength()
            .hasNoLintViolationsExceptInAdditionalRules()
    }

    @Test
    fun `Given a single line class header with parameters which is formatted correctly then do not change the class header`() {
        val code =
            """
            class ClassA(paramA: String, paramB: String, paramC: String)
            """.trimIndent()
        parameterListWrappingRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `Given a multiline class header with parameters which is formatted correctly then do not change the class header`() {
        val code =
            """
            class ClassA(
                paramA: String,
                paramB: String,
                paramC: String
            )
            """.trimIndent()
        parameterListWrappingRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `Given a multiline function with parameters then start each parameter and closing parenthesis on a separate line`() {
        val code =
            """
            fun f(a: Any,
                  b: Any,
                  c: Any) {
            }
            """.trimIndent()
        val formattedCode =
            """
            fun f(
                a: Any,
                b: Any,
                c: Any
            ) {
            }
            """.trimIndent()
        parameterListWrappingRuleAssertThat(code)
            .hasLintViolations(
                LintViolation(1, 7, "Parameter should start on a newline"),
                LintViolation(3, 13, """Missing newline before ")""""),
            ).isFormattedAs(formattedCode)
    }

    @Test
    fun `Given a multiline function with parameters where some parameters are on the same line then start each parameter and closing parenthesis on a separate line`() {
        val code =
            """
            fun f(
                a: Any,
                b: Any, c: Any
            )
            """.trimIndent()
        val formattedCode =
            """
            fun f(
                a: Any,
                b: Any,
                c: Any
            )
            """.trimIndent()
        parameterListWrappingRuleAssertThat(code)
            .hasLintViolation(3, 13, "Parameter should start on a newline")
            .isFormattedAs(formattedCode)
    }

    @Test
    fun `Given a single line function which exceeds the maximum line length then start each parameter and the closing parenthesis on a separate line`() {
        val code =
            """
            // $MAX_LINE_LENGTH_MARKER      $EOL_CHAR
            fun foo1(a: Any, b: Any, c: Any) {
            }

            @Suppress("ktlint:standard:max-line-length")
            fun foo2(a: Any, b: Any, c: Any) {
            }
            """.trimIndent()
        val formattedCode =
            """
            // $MAX_LINE_LENGTH_MARKER      $EOL_CHAR
            fun foo1(
                a: Any,
                b: Any,
                c: Any
            ) {
            }

            @Suppress("ktlint:standard:max-line-length")
            fun foo2(a: Any, b: Any, c: Any) {
            }
            """.trimIndent()
        parameterListWrappingRuleAssertThat(code)
            .setMaxLineLength()
            .hasLintViolations(
                LintViolation(2, 10, "Parameter should start on a newline"),
                LintViolation(2, 18, "Parameter should start on a newline"),
                LintViolation(2, 26, "Parameter should start on a newline"),
                LintViolation(2, 32, """Missing newline before ")""""),
            ).isFormattedAs(formattedCode)
    }

    @Nested
    inner class `Given a function literal having a multiline parameter list and the first parameter starts on same line as LBRACE` {
        private val code =
            """
            val fieldExample =
                LongNameClass { paramA,
                                paramB,
                                paramC ->
                    ClassB(paramA, paramB, paramC)
                }
            """.trimIndent()

        @Test
        fun `Given ktlint_official code style then reformat`() {
            val formattedCode =
                """
                val fieldExample =
                    LongNameClass {
                        paramA,
                        paramB,
                        paramC ->
                        ClassB(paramA, paramB, paramC)
                    }
                """.trimIndent()
            parameterListWrappingRuleAssertThat(code)
                .withEditorConfigOverride(CODE_STYLE_PROPERTY to KTLINT_OFFICIAL)
                // Indent violations will not be reported until after the wrapping of the first parameter is completed and as of that will
                // not be found during linting
                .isFormattedAs(formattedCode)
        }

        @ParameterizedTest(name = "Code style = {0}")
        @EnumSource(
            value = CodeStyleValue::class,
            mode = EnumSource.Mode.EXCLUDE,
            names = ["KTLINT_OFFICIAL"],
        )
        fun `Given another code style than ktlint_official then do not reformat`(codeStyleValue: CodeStyleValue) {
            parameterListWrappingRuleAssertThat(code)
                .withEditorConfigOverride(CODE_STYLE_PROPERTY to codeStyleValue)
                .hasNoLintViolations()
        }
    }

    @ParameterizedTest(name = "Code style = {0}")
    @EnumSource(value = CodeStyleValue::class)
    fun `Given a multiline reference expression with trailing lambda having a multiline parameter list and the first parameter starts on same line as LBRACE`(
        codeStyleValue: CodeStyleValue,
    ) {
        val code =
            """
            val foo =
                bar(
                    Any(),
                    Any()
                ) { a,
                    b
                    ->
                    foobar()
                }
            """.trimIndent()
        parameterListWrappingRuleAssertThat(code)
            .withEditorConfigOverride(CODE_STYLE_PROPERTY to codeStyleValue)
            .hasNoLintViolations()
    }

    @Test
    fun `Given a function with annotated parameters then start each parameter on a separate line but preserve spacing between annotation and parameter name`() {
        val code =
            """
            // $MAX_LINE_LENGTH_MARKER    $EOL_CHAR
            class A {
                fun f(@Annotation
                      a: Any,
                      b: Any,
                      @Annotation
                      c: Any, @Annotation d: Any) {
                }
            }
            """.trimIndent()
        val formattedCode =
            """
            // $MAX_LINE_LENGTH_MARKER    $EOL_CHAR
            class A {
                fun f(
                    @Annotation
                    a: Any,
                    b: Any,
                    @Annotation
                    c: Any,
                    @Annotation d: Any
                ) {
                }
            }
            """.trimIndent()
        parameterListWrappingRuleAssertThat(code)
            .setMaxLineLength()
            .hasLintViolations(
                LintViolation(3, 11, "Parameter should start on a newline"),
                LintViolation(7, 19, "Parameter should start on a newline"),
                LintViolation(7, 37, """Missing newline before ")""""),
            ).isFormattedAs(formattedCode)
    }

    @Test
    fun `Given a multiline function signature with each parameter, except first, and closing parenthesis on separate lines then reformat properly`() {
        val code =
            """
            class A {
                fun f(a: Any,
                      b: Any,
                      c: Any
                   ) {
                }
            }
            """.trimIndent()
        val formattedCode =
            """
            class A {
                fun f(
                    a: Any,
                    b: Any,
                    c: Any
                ) {
                }
            }
            """.trimIndent()
        parameterListWrappingRuleAssertThat(code)
            .hasLintViolation(2, 11, "Parameter should start on a newline")
            .isFormattedAs(formattedCode)
    }

    @Test
    fun `Given a multiline function with nested declarations then format each parameter and closing parenthesis on a separate line`() {
        val code =
            """
            fun visit(
                node: ASTNode,
                    autoCorrect: Boolean,
                emit: (offset: Int, errorMessage: String,
                canBeAutoCorrected: Boolean) -> Unit) {}
            """.trimIndent()
        val formattedCode =
            """
            fun visit(
                node: ASTNode,
                autoCorrect: Boolean,
                emit: (
                    offset: Int,
                    errorMessage: String,
                    canBeAutoCorrected: Boolean
                ) -> Unit
            ) {}
            """.trimIndent()
        parameterListWrappingRuleAssertThat(code)
            .hasLintViolations(
                LintViolation(4, 12, "Parameter should start on a newline"),
                LintViolation(4, 25, "Parameter should start on a newline"),
                LintViolation(5, 32, """Missing newline before ")""""),
                LintViolation(5, 41, """Missing newline before ")""""),
            ).isFormattedAs(formattedCode)
    }

    @Test
    fun `Given a single line function with nested declarations which exceeds the maximum line length then format each parameter and closing parenthesis on a separate line`() {
        val code =
            """
            // $MAX_LINE_LENGTH_MARKER                                                      $EOL_CHAR
            fun visit(node: ASTNode, autoCorrect: Boolean, emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit) {}
            """.trimIndent()
        val formattedCode =
            """
            // $MAX_LINE_LENGTH_MARKER                                                      $EOL_CHAR
            fun visit(
                node: ASTNode,
                autoCorrect: Boolean,
                emit: (
                    offset: Int,
                    errorMessage: String,
                    canBeAutoCorrected: Boolean
                ) -> Unit
            ) {}
            """.trimIndent()
        parameterListWrappingRuleAssertThat(code)
            .setMaxLineLength()
            .hasLintViolations(
                LintViolation(2, 11, "Parameter should start on a newline"),
                LintViolation(2, 26, "Parameter should start on a newline"),
                LintViolation(2, 48, "Parameter should start on a newline"),
                LintViolation(2, 55, "Parameter should start on a newline"),
                LintViolation(2, 68, "Parameter should start on a newline"),
                LintViolation(2, 90, "Parameter should start on a newline"),
                LintViolation(2, 117, "Missing newline before \")\""),
                LintViolation(2, 126, "Missing newline before \")\""),
            ).isFormattedAs(formattedCode)
    }

    @Test
    fun `Given a function with nested declarations which is formatted correct then do not change the function signature`() {
        val code =
            """
            fun visit(
                node: ASTNode,
                autoCorrect: Boolean,
                emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit
            ) {}
            """.trimIndent()
        parameterListWrappingRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun testCommentsAreIgnored() {
        val code =
            """
            data class A(
                /*
                 * comment
                 */
                //
                var v: String
            )
            """.trimIndent()
        parameterListWrappingRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `Given a class declaration with a dangling opening parenthesis`() {
        val code =
            """
            class ClassA
            (
                paramA: String,
                paramB: String,
                paramC: String
            )
            """.trimIndent()
        val formattedCode =
            """
            class ClassA(
                paramA: String,
                paramB: String,
                paramC: String
            )
            """.trimIndent()
        parameterListWrappingRuleAssertThat(code)
            .hasLintViolation(2, 1, """Unnecessary newline before "("""")
            .isFormattedAs(formattedCode)
    }

    @Test
    fun `Given a function declaration with a dangling opening parenthesis`() {
        val code =
            """
            fun doSomething
            (
                paramA: String,
                paramB: String,
                paramC: String
            )
            """.trimIndent()
        val formattedCode =
            """
            fun doSomething(
                paramA: String,
                paramB: String,
                paramC: String
            )
            """.trimIndent()
        parameterListWrappingRuleAssertThat(code)
            .hasLintViolation(2, 1, """Unnecessary newline before "("""")
            .isFormattedAs(formattedCode)
    }

    @Test
    fun `Issue 680 - multiline type parameter list in function signature - indented correctly`() {
        val code =
            """
            object TestCase {
                inline fun <
                    T1,
                    T2,
                    T3> create(
                    t1: T1,
                    t2: T2,
                    t3: T3
                ) {
                    // do things
                }
            }
            """.trimIndent()
        parameterListWrappingRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `Given a class with a multiline type parameter which is indented correctly then do not reformat`() {
        val code =
            """
            // https://github.com/ktlint/ktlint/issues/921
            class ComposableLambda<
                P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16,
                P17, P18, R>(
                val key: Int,
                private val tracked: Boolean,
                private val sourceInformation: String?
            )
            // https://github.com/ktlint/ktlint/issues/938
            class GenericTypeWithALongLongALong1
            class GenericTypeWithALongLongALong2
            class GenericTypeWithALongLongALong3
            class ViewModelWithALongLongLongLongLongLongLongLongName3<
                A : GenericTypeWithALongLongALong1,
                B : GenericTypeWithALongLongALong2,
                C : GenericTypeWithALongLongALong3
            > constructor(
                parameterWithLongLongLongLongLongLongLongLongNameA: A,
                parameterWithLongLongLongLongLongLongLongLongNameB: B,
                parameterWithLongLongLongLongLongLongLongLongNameC: C
            )
            """.trimIndent()
        parameterListWrappingRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `multiline type argument list in function signature`() {
        val code =
            """
            class Foo<A, B, C>

            fun Foo<String, Boolean,
                Int>.bar(
                i: Int
            ) = apply {
            }
            """.trimIndent()
        parameterListWrappingRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `Given a multiline type argument list in a class property`() {
        val code =
            """
            data class FooBar(
                public val fooBar: List<
                    Foo,
                    Bar
                >,
            )
            """.trimIndent()
        parameterListWrappingRuleAssertThat(code).hasNoLintViolations()
    }

    @Nested
    inner class `Issue 1255 - Given a variable declaration for nullable function type` {
        @Test
        fun `Given a nullable function type for which the function type fits on a single line after wrapping the nullable type`() {
            val code =
                """
                // $MAX_LINE_LENGTH_MARKER                  $EOL_CHAR
                var foo1: ((bar1: Bar, bar2: Bar, bar3: Bar) -> Unit)? = null
                var foo2: (
                    (bar1: Bar, bar2: Bar, bar3: Bar) -> Unit
                )? = null

                @Suppress("ktlint:standard:max-line-length")
                var foo3: ((bar1: Bar, bar2: Bar, bar3: Bar) -> Unit)? = null
                """.trimIndent()
            val formattedCode =
                """
                // $MAX_LINE_LENGTH_MARKER                  $EOL_CHAR
                var foo1: (
                    (bar1: Bar, bar2: Bar, bar3: Bar) -> Unit
                )? = null
                var foo2: (
                    (bar1: Bar, bar2: Bar, bar3: Bar) -> Unit
                )? = null

                @Suppress("ktlint:standard:max-line-length")
                var foo3: ((bar1: Bar, bar2: Bar, bar3: Bar) -> Unit)? = null
                """.trimIndent()
            parameterListWrappingRuleAssertThat(code)
                .setMaxLineLength()
                .hasLintViolations(
                    // Some violations below are only reported during linting but not on formatting. After wrapping the
                    // nullable type, there is no need to wrap the parameters.
                    LintViolation(2, 12, "Expected new line before function type as it does not fit on a single line"),
                    LintViolation(2, 13, "Parameter should start on a newline"),
                    LintViolation(2, 24, "Parameter should start on a newline"),
                    LintViolation(2, 35, "Parameter should start on a newline"),
                    LintViolation(2, 44, "Missing newline before \")\""),
                    LintViolation(2, 53, "Expected new line after function type as it does not fit on a single line"),
                ).isFormattedAs(formattedCode)
        }

        @Test
        fun `Given a nullable function type for which the function type fits does not fit on a single line after wrapping the nullable type`() {
            val code =
                """
                // $MAX_LINE_LENGTH_MARKER                $EOL_CHAR
                var foo1: ((bar1: Bar, bar2: Bar, bar3: Bar) -> Unit)? = null
                var foo2: (
                    (bar1: Bar, bar2: Bar, bar3: Bar) -> Unit
                )? = null
                """.trimIndent()
            val formattedCode =
                """
                // $MAX_LINE_LENGTH_MARKER                $EOL_CHAR
                var foo1: (
                    (
                        bar1: Bar,
                        bar2: Bar,
                        bar3: Bar
                    ) -> Unit
                )? = null
                var foo2: (
                    (
                        bar1: Bar,
                        bar2: Bar,
                        bar3: Bar
                    ) -> Unit
                )? = null
                """.trimIndent()
            parameterListWrappingRuleAssertThat(code)
                .setMaxLineLength()
                .hasLintViolations(
                    LintViolation(2, 12, "Expected new line before function type as it does not fit on a single line"),
                    LintViolation(2, 13, "Parameter should start on a newline"),
                    LintViolation(2, 24, "Parameter should start on a newline"),
                    LintViolation(2, 35, "Parameter should start on a newline"),
                    LintViolation(2, 44, "Missing newline before \")\""),
                    LintViolation(2, 53, "Expected new line after function type as it does not fit on a single line"),
                    LintViolation(4, 6, "Parameter should start on a newline"),
                    LintViolation(4, 17, "Parameter should start on a newline"),
                    LintViolation(4, 28, "Parameter should start on a newline"),
                    LintViolation(4, 37, "Missing newline before \")\""),
                ).isFormattedAs(formattedCode)
        }

        @Test
        fun `Given a function containing a nullable function type for which the function type fits does not fit on a single line after wrapping the nullable type`() {
            val code =
                """
                // $MAX_LINE_LENGTH_MARKER                      $EOL_CHAR
                fun foo() {
                    var changesListener: ((bar1: Bar, bar2: Bar, bar3: Bar) -> Unit)? = null
                }
                """.trimIndent()
            val formattedCode =
                """
                // $MAX_LINE_LENGTH_MARKER                      $EOL_CHAR
                fun foo() {
                    var changesListener: (
                        (bar1: Bar, bar2: Bar, bar3: Bar) -> Unit
                    )? = null
                }
                """.trimIndent()
            parameterListWrappingRuleAssertThat(code)
                .setMaxLineLength()
                .hasLintViolations(
                    // Some violations below are only reported during linting but not on formatting. After wrapping the
                    // nullable type, there is no need to wrap the parameters.
                    LintViolation(3, 27, "Expected new line before function type as it does not fit on a single line"),
                    LintViolation(3, 28, "Parameter should start on a newline"),
                    LintViolation(3, 39, "Parameter should start on a newline"),
                    LintViolation(3, 50, "Parameter should start on a newline"),
                    LintViolation(3, 59, "Missing newline before \")\""),
                    LintViolation(3, 68, "Expected new line after function type as it does not fit on a single line"),
                ).isFormattedAs(formattedCode)
        }
    }

    @Nested
    inner class `Given a single line function signature with an annotated parameter` {
        @Test
        fun `Given ktlint_official code style`() {
            val code =
                """
                fun foo(a: Int, @Bar bar: String, b: Int) = "some-result"
                """.trimIndent()
            val formattedCode =
                """
                fun foo(
                    a: Int,
                    @Bar bar: String,
                    b: Int
                ) = "some-result"
                """.trimIndent()
            parameterListWrappingRuleAssertThat(code)
                .withEditorConfigOverride(CODE_STYLE_PROPERTY to KTLINT_OFFICIAL)
                .hasLintViolations(
                    LintViolation(1, 9, "Parameter should start on a newline"),
                    LintViolation(1, 17, "Parameter should start on a newline"),
                    LintViolation(1, 35, "Parameter should start on a newline"),
                    LintViolation(1, 41, "Missing newline before \")\""),
                ).isFormattedAs(formattedCode)
        }

        @ParameterizedTest(name = "Code style: {0}")
        @EnumSource(
            value = CodeStyleValue::class,
            mode = EnumSource.Mode.EXCLUDE,
            names = ["KTLINT_OFFICIAL"],
        )
        fun `Given non-ktlint_official code style`(codeStyle: CodeStyleValue) {
            val code =
                """
                fun foo(a: Int, @Bar bar: String, b: Int) = "some-result"
                """.trimIndent()
            parameterListWrappingRuleAssertThat(code)
                .withEditorConfigOverride(CODE_STYLE_PROPERTY to codeStyle)
                .hasNoLintViolations()
        }
    }

    @Test
    fun `Issue 2450 - Given a variable declaration for a function type followed by an EOL comment that causes the max line length to be exceeded, then only report a violation via max-line-length rule`() {
        val code =
            """
            // $MAX_LINE_LENGTH_MARKER                                 $EOL_CHAR
            val fooooo: ((bar1: Bar, bar2: Bar, bar3: Bar) -> Unit) = {} // some comment
            var foo: ((bar1: Bar, bar2: Bar, bar3: Bar) -> Unit)? = null // some comment
            """.trimIndent()
        parameterListWrappingRuleAssertThat(code)
            .setMaxLineLength()
            .addAdditionalRuleProvider { MaxLineLengthRule() }
            .hasLintViolationsForAdditionalRule(
                LintViolation(2, 61, "Exceeded max line length (60)", false),
                LintViolation(3, 61, "Exceeded max line length (60)", false),
            ).hasNoLintViolationsExceptInAdditionalRules()
    }

    @Test
    fun `Issue 2535 - Given a class with a KDoc on the primary constructor instead of on the class name`() {
        val code =
            // Code sample below contains a code smell. The KDoc is at the wrong position.
            """
            class ClassA
                /**
                 * some comment
                 */(paramA: String)
            """.trimIndent()
        val formattedCode =
            // Code sample below contains a code smell. The KDoc is at the wrong position. The formatted code is not intended to show
            // that this code is well formatted according to good coding practices. It merely is used to validate that the newline is
            // inserted at the correct position in the AST so that no exception will be thrown in the ParameterListWrappingRule.
            """
            class ClassA
            /**
             * some comment
             */
            (paramA: String)
            """.trimIndent()
        parameterListWrappingRuleAssertThat(code)
            .addAdditionalRuleProvider { KdocWrappingRule() }
            .hasLintViolations(
                LintViolation(4, 8, "Parameter list should not be preceded by a comment", false),
            ).isFormattedAs(formattedCode)
    }
}
