package io.github.ktlint.core.ruleset.standard.rules

import io.github.ktlint.core.rule.engine.core.api.editorconfig.CODE_STYLE_PROPERTY
import io.github.ktlint.core.rule.engine.core.api.editorconfig.CodeStyleValue
import io.github.ktlint.core.ruleset.standard.rules.FunctionSignatureRule.Companion.FUNCTION_BODY_EXPRESSION_WRAPPING_PROPERTY
import io.github.ktlint.core.ruleset.standard.rules.FunctionSignatureRule.FunctionBodyExpressionWrapping.DEFAULT
import io.github.ktlint.core.test.KtLintAssertThat
import io.github.ktlint.core.test.LintViolation
import io.github.ktlint.core.test.MULTILINE_STRING_QUOTE
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class MultilineExpressionWrappingRuleTest {
    private val multilineExpressionWrappingRuleAssertThat = KtLintAssertThat.assertThatRule { MultilineExpressionWrappingRule() }

    @Nested
    inner class `Given a function call using a named argument` {
        @Test
        fun `Given value argument for a named parameter in a function with a multiline dot qualified expression on the same line as the assignment`() {
            val code =
                """
                val foo = foo(
                    parameterName = "The quick brown fox "
                        .plus("jumps ")
                        .plus("over the lazy dog"),
                )
                """.trimIndent()
            val formattedCode =
                """
                val foo =
                    foo(
                        parameterName =
                            "The quick brown fox "
                                .plus("jumps ")
                                .plus("over the lazy dog"),
                    )
                """.trimIndent()
            multilineExpressionWrappingRuleAssertThat(code)
                .addAdditionalRuleProvider { IndentationRule() }
                .withEditorConfigOverride(CODE_STYLE_PROPERTY to CodeStyleValue.KTLINT_OFFICIAL)
                .hasLintViolations(
                    LintViolation(1, 11, "A multiline expression should start on a new line"),
                    LintViolation(2, 21, "A multiline expression should start on a new line"),
                ).isFormattedAs(formattedCode)
        }

        @Test
        fun `Given value argument in a function with a multiline safe access expression on the same line as the assignment`() {
            val code =
                """
                val foo = foo(
                    parameterName = theQuickBrownFoxOrNull
                        ?.plus("jumps ")
                        ?.plus("over the lazy dog"),
                )
                """.trimIndent()
            val formattedCode =
                """
                val foo =
                    foo(
                        parameterName =
                            theQuickBrownFoxOrNull
                                ?.plus("jumps ")
                                ?.plus("over the lazy dog"),
                    )
                """.trimIndent()
            multilineExpressionWrappingRuleAssertThat(code)
                .addAdditionalRuleProvider { IndentationRule() }
                .withEditorConfigOverride(CODE_STYLE_PROPERTY to CodeStyleValue.KTLINT_OFFICIAL)
                .hasLintViolations(
                    LintViolation(1, 11, "A multiline expression should start on a new line"),
                    LintViolation(2, 21, "A multiline expression should start on a new line"),
                ).isFormattedAs(formattedCode)
        }

        @Test
        fun `Given value argument in a function with a multiline combination of a safe access expression and a call expression on the same line as the assignment`() {
            val code =
                """
                val foo = foo(
                    parameterName = theQuickBrownFoxOrNull()
                        ?.plus("jumps ")
                        ?.plus("over the lazy dog"),
                )
                """.trimIndent()
            val formattedCode =
                """
                val foo =
                    foo(
                        parameterName =
                            theQuickBrownFoxOrNull()
                                ?.plus("jumps ")
                                ?.plus("over the lazy dog"),
                    )
                """.trimIndent()
            multilineExpressionWrappingRuleAssertThat(code)
                .addAdditionalRuleProvider { IndentationRule() }
                .withEditorConfigOverride(CODE_STYLE_PROPERTY to CodeStyleValue.KTLINT_OFFICIAL)
                .hasLintViolations(
                    LintViolation(1, 11, "A multiline expression should start on a new line"),
                    LintViolation(2, 21, "A multiline expression should start on a new line"),
                ).isFormattedAs(formattedCode)
        }

        @Test
        fun `Given value argument in a function with a multiline combination of a dot qualified and a safe access expression on the same line as the assignment`() {
            val code =
                """
                val foo = foo(
                    parameterName = "The quick brown fox "
                        .takeIf { it.jumps }
                        ?.plus("jumps over the lazy dog"),
                )
                """.trimIndent()
            val formattedCode =
                """
                val foo =
                    foo(
                        parameterName =
                            "The quick brown fox "
                                .takeIf { it.jumps }
                                ?.plus("jumps over the lazy dog"),
                    )
                """.trimIndent()
            multilineExpressionWrappingRuleAssertThat(code)
                .addAdditionalRuleProvider { IndentationRule() }
                .withEditorConfigOverride(CODE_STYLE_PROPERTY to CodeStyleValue.KTLINT_OFFICIAL)
                .hasLintViolations(
                    LintViolation(1, 11, "A multiline expression should start on a new line"),
                    LintViolation(2, 21, "A multiline expression should start on a new line"),
                ).isFormattedAs(formattedCode)
        }

        @Test
        fun `Given value argument in a function with a multiline call expression on the same line as the assignment`() {
            val code =
                """
                val foo = foo(
                    parameterName = bar(
                        "bar"
                    )
                )
                """.trimIndent()
            val formattedCode =
                """
                val foo =
                    foo(
                        parameterName =
                            bar(
                                "bar"
                            )
                    )
                """.trimIndent()
            multilineExpressionWrappingRuleAssertThat(code)
                .addAdditionalRuleProvider { IndentationRule() }
                .withEditorConfigOverride(CODE_STYLE_PROPERTY to CodeStyleValue.KTLINT_OFFICIAL)
                .hasLintViolations(
                    LintViolation(1, 11, "A multiline expression should start on a new line"),
                    LintViolation(2, 21, "A multiline expression should start on a new line"),
                ).isFormattedAs(formattedCode)
        }
    }

    @Nested
    inner class `Given a function call using an unnamed argument` {
        @Test
        fun `Given value argument in a function with a multiline binary expression on the same line as the assignment`() {
            val code =
                """
                val foo = foo("The quick brown fox " +
                    "jumps " +
                    "over the lazy dog",
                )
                """.trimIndent()
            val formattedCode =
                """
                val foo =
                    foo(
                        "The quick brown fox " +
                            "jumps " +
                            "over the lazy dog",
                    )
                """.trimIndent()
            multilineExpressionWrappingRuleAssertThat(code)
                .addAdditionalRuleProvider { IndentationRule() }
                .withEditorConfigOverride(CODE_STYLE_PROPERTY to CodeStyleValue.KTLINT_OFFICIAL)
                .hasLintViolations(
                    LintViolation(1, 11, "A multiline expression should start on a new line"),
                    LintViolation(1, 15, "A multiline expression should start on a new line"),
                ).isFormattedAs(formattedCode)
        }

        @Test
        fun `Given value argument in a function with a multiline safe access expression on the same line as the assignment`() {
            val code =
                """
                val foo = foo(theQuickBrownFoxOrNull
                    ?.plus("jumps ")
                    ?.plus("over the lazy dog"),
                )
                """.trimIndent()
            val formattedCode =
                """
                val foo =
                    foo(
                        theQuickBrownFoxOrNull
                            ?.plus("jumps ")
                            ?.plus("over the lazy dog"),
                    )
                """.trimIndent()
            multilineExpressionWrappingRuleAssertThat(code)
                .addAdditionalRuleProvider { IndentationRule() }
                .withEditorConfigOverride(CODE_STYLE_PROPERTY to CodeStyleValue.KTLINT_OFFICIAL)
                .hasLintViolations(
                    LintViolation(1, 11, "A multiline expression should start on a new line"),
                    LintViolation(1, 15, "A multiline expression should start on a new line"),
                ).isFormattedAs(formattedCode)
        }

        @Test
        fun `Given value argument in a function with a multiline combination of a safe access expression and a call expression on the same line as the assignment`() {
            val code =
                """
                val foo = foo(theQuickBrownFoxOrNull()
                    ?.plus("jumps ")
                    ?.plus("over the lazy dog"),
                )
                """.trimIndent()
            val formattedCode =
                """
                val foo =
                    foo(
                        theQuickBrownFoxOrNull()
                            ?.plus("jumps ")
                            ?.plus("over the lazy dog"),
                    )
                """.trimIndent()
            multilineExpressionWrappingRuleAssertThat(code)
                .addAdditionalRuleProvider { IndentationRule() }
                .withEditorConfigOverride(CODE_STYLE_PROPERTY to CodeStyleValue.KTLINT_OFFICIAL)
                .hasLintViolations(
                    LintViolation(1, 11, "A multiline expression should start on a new line"),
                    LintViolation(1, 15, "A multiline expression should start on a new line"),
                ).isFormattedAs(formattedCode)
        }

        @Test
        fun `Given value argument in a function with a multiline combination of a dot qualified and a safe access expression on the same line as the assignment`() {
            val code =
                """
                val foo = foo("The quick brown fox "
                    .takeIf { it.jumps }
                    ?.plus("jumps over the lazy dog"),
                )
                """.trimIndent()
            val formattedCode =
                """
                val foo =
                    foo(
                        "The quick brown fox "
                            .takeIf { it.jumps }
                            ?.plus("jumps over the lazy dog"),
                    )
                """.trimIndent()
            multilineExpressionWrappingRuleAssertThat(code)
                .addAdditionalRuleProvider { IndentationRule() }
                .withEditorConfigOverride(CODE_STYLE_PROPERTY to CodeStyleValue.KTLINT_OFFICIAL)
                .hasLintViolations(
                    LintViolation(1, 11, "A multiline expression should start on a new line"),
                    LintViolation(1, 15, "A multiline expression should start on a new line"),
                ).isFormattedAs(formattedCode)
        }

        @Test
        fun `Given value argument in a function with a multiline call expression on the same line as the assignment`() {
            val code =
                """
                val foo = foo(bar(
                    "bar"
                ))
                """.trimIndent()
            val formattedCode =
                """
                val foo =
                    foo(
                        bar(
                            "bar"
                        )
                    )
                """.trimIndent()
            multilineExpressionWrappingRuleAssertThat(code)
                .addAdditionalRuleProvider { IndentationRule() }
                .addAdditionalRuleProvider { ParameterWrappingRule() }
                .withEditorConfigOverride(CODE_STYLE_PROPERTY to CodeStyleValue.KTLINT_OFFICIAL)
                .hasLintViolations(
                    LintViolation(1, 11, "A multiline expression should start on a new line"),
                    LintViolation(1, 15, "A multiline expression should start on a new line"),
                ).isFormattedAs(formattedCode)
        }
    }

    @Test
    fun `Given a declaration with parameter having a default value which is a multiline expression then keep trailing comma after the parameter`() {
        val code =
            """
            fun foo(
                val string: String = barFoo
                    .count { it == "bar" },
                val int: Int
            )
            """.trimIndent()
        val formattedCode =
            """
            fun foo(
                val string: String =
                    barFoo
                        .count { it == "bar" },
                val int: Int
            )
            """.trimIndent()
        multilineExpressionWrappingRuleAssertThat(code)
            .addAdditionalRuleProvider { IndentationRule() }
            .hasLintViolation(2, 26, "A multiline expression should start on a new line")
            .isFormattedAs(formattedCode)
    }

    @Test
    fun `Given a return statement with a multiline expression then do not reformat as it would result in a compilation error`() {
        val code =
            """
            fun foo() {
                return bar(
                    "bar"
                )
            }
            """.trimIndent()
        multilineExpressionWrappingRuleAssertThat(code)
            .withEditorConfigOverride(CODE_STYLE_PROPERTY to CodeStyleValue.KTLINT_OFFICIAL)
            .hasNoLintViolations()
    }

    @Test
    fun `Given a function with a multiline body expression`() {
        val code =
            """
            fun foo() = bar(
                "bar"
            )
            """.trimIndent()
        val formattedCode =
            """
            fun foo() =
                bar(
                    "bar"
                )
            """.trimIndent()
        multilineExpressionWrappingRuleAssertThat(code)
            .addAdditionalRuleProvider { IndentationRule() }
            .withEditorConfigOverride(CODE_STYLE_PROPERTY to CodeStyleValue.KTLINT_OFFICIAL)
            .hasLintViolation(1, 13, "A multiline expression should start on a new line")
            .isFormattedAs(formattedCode)
    }

    @Test
    fun `Given a function with a multiline body expression, and the function signature body expression wrapping is set to 'default' (eg keep first line of expression on same line)`() {
        val code =
            """
            fun foo() = bar(
                "bar"
            )
            """.trimIndent()
        multilineExpressionWrappingRuleAssertThat(code)
            .addAdditionalRuleProvider { IndentationRule() }
            .addAdditionalRuleProvider { FunctionSignatureRule() }
            .withEditorConfigOverride(CODE_STYLE_PROPERTY to CodeStyleValue.KTLINT_OFFICIAL)
            .withEditorConfigOverride(FUNCTION_BODY_EXPRESSION_WRAPPING_PROPERTY to DEFAULT)
            .hasNoLintViolations()
    }

    @Test
    fun `Given a function with a multiline signature without a return type but with a multiline expression body starting on same line as closing parenthesis of function`() {
        val code =
            """
            fun foo(
                foobar: String
            ) = bar(
                foobar
            )
            """.trimIndent()
        multilineExpressionWrappingRuleAssertThat(code)
            .addAdditionalRuleProvider { IndentationRule() }
            .withEditorConfigOverride(CODE_STYLE_PROPERTY to CodeStyleValue.KTLINT_OFFICIAL)
            .hasNoLintViolations()
    }

    @Test
    fun `Given a function with a multiline lambda expression containing a binary expression`() {
        val code =
            """
            val string: String
                by lazy { "The quick brown fox " +
                              "jumps " +
                              "over the lazy dog"
                }
            """.trimIndent()
        val formattedCode =
            """
            val string: String
                by lazy {
                    "The quick brown fox " +
                        "jumps " +
                        "over the lazy dog"
                }
            """.trimIndent()
        multilineExpressionWrappingRuleAssertThat(code)
            .addAdditionalRuleProvider { IndentationRule() }
            .withEditorConfigOverride(CODE_STYLE_PROPERTY to CodeStyleValue.KTLINT_OFFICIAL)
            .hasLintViolation(2, 15, "A multiline expression should start on a new line")
            .isFormattedAs(formattedCode)
    }

    @Test
    fun `Given a function with a lambda expression containing a multiline string template`() {
        val code =
            """
            val string: String
                by lazy { ${MULTILINE_STRING_QUOTE}The quick brown fox
                    jumps
                    over the lazy dog$MULTILINE_STRING_QUOTE.trimIndent()
                }
            """.trimIndent()
        val formattedCode =
            """
            val string: String
                by lazy {
                    ${MULTILINE_STRING_QUOTE}The quick brown fox
                    jumps
                    over the lazy dog$MULTILINE_STRING_QUOTE.trimIndent()
                }
            """.trimIndent()
        multilineExpressionWrappingRuleAssertThat(code)
            .addAdditionalRuleProvider { IndentationRule() }
            .withEditorConfigOverride(CODE_STYLE_PROPERTY to CodeStyleValue.KTLINT_OFFICIAL)
            .hasLintViolation(2, 15, "A multiline expression should start on a new line")
            .isFormattedAs(formattedCode)
    }

    @Test
    fun `Given a function with a multiline lambda expression`() {
        val code =
            """
            val string =
                listOf("The quick brown fox", "jumps", "over the lazy dog")
                    .map { it
                        .lowercase()
                        .substringAfter("o")
                    }
            """.trimIndent()
        val formattedCode =
            """
            val string =
                listOf("The quick brown fox", "jumps", "over the lazy dog")
                    .map {
                        it
                            .lowercase()
                            .substringAfter("o")
                    }
            """.trimIndent()
        multilineExpressionWrappingRuleAssertThat(code)
            .addAdditionalRuleProvider { IndentationRule() }
            .withEditorConfigOverride(CODE_STYLE_PROPERTY to CodeStyleValue.KTLINT_OFFICIAL)
            .hasLintViolation(3, 16, "A multiline expression should start on a new line")
            .isFormattedAs(formattedCode)
    }

    @Test
    fun `Given a multiline string template in a function parameter to a new line`() {
        val code =
            """
            fun someFunction() {
                println($MULTILINE_STRING_QUOTE
                        The quick brown fox
                        jumps over the lazy dog
                        $MULTILINE_STRING_QUOTE.trimIndent())
            }
            """.trimIndent()
        val formattedCode =
            """
            fun someFunction() {
                println(
                    $MULTILINE_STRING_QUOTE
                        The quick brown fox
                        jumps over the lazy dog
                    $MULTILINE_STRING_QUOTE.trimIndent()
                )
            }
            """.trimIndent()
        multilineExpressionWrappingRuleAssertThat(code)
            .addAdditionalRuleProvider { IndentationRule() }
            .withEditorConfigOverride(CODE_STYLE_PROPERTY to CodeStyleValue.KTLINT_OFFICIAL)
            .hasLintViolation(2, 13, "A multiline expression should start on a new line")
            .isFormattedAs(formattedCode)
    }

    @Test
    fun `Given a multiline string template after an arrow`() {
        val code =
            """
            fun foo(bar: String) =
                when (bar) {
                    "bar bar bar bar bar bar bar bar bar" -> $MULTILINE_STRING_QUOTE
                        The quick brown fox
                        jumps over the lazy dog
                        $MULTILINE_STRING_QUOTE.trimIndent()
                    else -> ""
                }
            """.trimIndent()
        val formattedCode =
            """
            fun foo(bar: String) =
                when (bar) {
                    "bar bar bar bar bar bar bar bar bar" ->
                        $MULTILINE_STRING_QUOTE
                        The quick brown fox
                        jumps over the lazy dog
                        $MULTILINE_STRING_QUOTE.trimIndent()
                    else -> ""
                }
            """.trimIndent()
        multilineExpressionWrappingRuleAssertThat(code)
            .addAdditionalRuleProvider { IndentationRule() }
            .withEditorConfigOverride(CODE_STYLE_PROPERTY to CodeStyleValue.KTLINT_OFFICIAL)
            .hasLintViolation(3, 50, "A multiline expression should start on a new line")
            .isFormattedAs(formattedCode)
    }

    @Test
    fun `Move a multiline when statement as part of an assignment`() {
        val code =
            """
            fun foo(bar: String) = when (bar) {
                "bar" -> true
                else -> false
            }
            """.trimIndent()
        val formattedCode =
            """
            fun foo(bar: String) =
                when (bar) {
                    "bar" -> true
                    else -> false
                }
            """.trimIndent()
        multilineExpressionWrappingRuleAssertThat(code)
            .addAdditionalRuleProvider { IndentationRule() }
            .withEditorConfigOverride(CODE_STYLE_PROPERTY to CodeStyleValue.KTLINT_OFFICIAL)
            .hasLintViolation(1, 24, "A multiline expression should start on a new line")
            .isFormattedAs(formattedCode)
    }

    @Test
    fun `Move a multiline if statement as part of an assignment`() {
        val code =
            """
            fun foo(bar: Boolean) = if (bar) {
                "bar"
            } else {
                "foo"
            }
            """.trimIndent()
        val formattedCode =
            """
            fun foo(bar: Boolean) =
                if (bar) {
                    "bar"
                } else {
                    "foo"
                }
            """.trimIndent()
        multilineExpressionWrappingRuleAssertThat(code)
            .addAdditionalRuleProvider { IndentationRule() }
            .withEditorConfigOverride(CODE_STYLE_PROPERTY to CodeStyleValue.KTLINT_OFFICIAL)
            .hasLintViolation(1, 25, "A multiline expression should start on a new line")
            .isFormattedAs(formattedCode)
    }

    @Test
    fun `Move a multiline try catch as part of an assignment`() {
        val code =
            """
            fun foo() = try {
                // do something that might cause an exception
            } catch(e: Exception) {
                // handle exception
            }
            """.trimIndent()
        val formattedCode =
            """
            fun foo() =
                try {
                    // do something that might cause an exception
                } catch(e: Exception) {
                    // handle exception
                }
            """.trimIndent()
        multilineExpressionWrappingRuleAssertThat(code)
            .addAdditionalRuleProvider { IndentationRule() }
            .withEditorConfigOverride(CODE_STYLE_PROPERTY to CodeStyleValue.KTLINT_OFFICIAL)
            .hasLintViolation(1, 13, "A multiline expression should start on a new line")
            .isFormattedAs(formattedCode)
    }

    @Test
    fun `Move a multiline is-expression as part of an assignment`() {
        val code =
            """
            fun foo(any: Any) = any is
                Foo
            """.trimIndent()
        val formattedCode =
            """
            fun foo(any: Any) =
                any is
                    Foo
            """.trimIndent()
        multilineExpressionWrappingRuleAssertThat(code)
            .addAdditionalRuleProvider { IndentationRule() }
            .withEditorConfigOverride(CODE_STYLE_PROPERTY to CodeStyleValue.KTLINT_OFFICIAL)
            .hasLintViolation(1, 21, "A multiline expression should start on a new line")
            .isFormattedAs(formattedCode)
    }

    @Test
    fun `Move a multiline binary with type as part of an assignment`() {
        val code =
            """
            fun foo(any: Any) = any as
                Foo
            """.trimIndent()
        val formattedCode =
            """
            fun foo(any: Any) =
                any as
                    Foo
            """.trimIndent()
        multilineExpressionWrappingRuleAssertThat(code)
            .addAdditionalRuleProvider { IndentationRule() }
            .withEditorConfigOverride(CODE_STYLE_PROPERTY to CodeStyleValue.KTLINT_OFFICIAL)
            .hasLintViolation(1, 21, "A multiline expression should start on a new line")
            .isFormattedAs(formattedCode)
    }

    @Test
    fun `Move a multiline prefix expression as part of an assignment`() {
        val code =
            """
            fun foo(any: Int) = ++
                42
            """.trimIndent()
        val formattedCode =
            """
            fun foo(any: Int) =
                ++
                    42
            """.trimIndent()
        multilineExpressionWrappingRuleAssertThat(code)
            .addAdditionalRuleProvider { IndentationRule() }
            .withEditorConfigOverride(CODE_STYLE_PROPERTY to CodeStyleValue.KTLINT_OFFICIAL)
            .hasLintViolation(1, 21, "A multiline expression should start on a new line")
            .isFormattedAs(formattedCode)
    }

    @Test
    fun `Move a multiline array access expression as part of an assignment`() {
        val code =
            """
            fun foo(any: Array<String>) = any[
                42
            ]
            """.trimIndent()
        val formattedCode =
            """
            fun foo(any: Array<String>) =
                any[
                    42
                ]
            """.trimIndent()
        multilineExpressionWrappingRuleAssertThat(code)
            .addAdditionalRuleProvider { IndentationRule() }
            .withEditorConfigOverride(CODE_STYLE_PROPERTY to CodeStyleValue.KTLINT_OFFICIAL)
            .hasLintViolation(1, 31, "A multiline expression should start on a new line")
            .isFormattedAs(formattedCode)
    }

    @Test
    fun `Move a multiline object literal as part of an assignment`() {
        val code =
            """
            fun foo() = object :
                Foo() {}
            """.trimIndent()
        val formattedCode =
            """
            fun foo() =
                object :
                    Foo() {}
            """.trimIndent()
        multilineExpressionWrappingRuleAssertThat(code)
            .addAdditionalRuleProvider { IndentationRule() }
            .withEditorConfigOverride(CODE_STYLE_PROPERTY to CodeStyleValue.KTLINT_OFFICIAL)
            .hasLintViolation(1, 13, "A multiline expression should start on a new line")
            .isFormattedAs(formattedCode)
    }

    @Test
    fun `Given a lambda expression with a multiline expression starting on a new line then do not report a violation`() {
        val code =
            """
            val foo =
                listOf("foo")
                    .let { bar ->
                        if (fooBar > 42) {
                            "foo"
                        } else {
                            "bar"
                        }
                    }
            """.trimIndent()
        multilineExpressionWrappingRuleAssertThat(code)
            .addAdditionalRuleProvider { IndentationRule() }
            .withEditorConfigOverride(CODE_STYLE_PROPERTY to CodeStyleValue.KTLINT_OFFICIAL)
            .hasNoLintViolations()
    }

    @Test
    fun `Given a multiline expression with an EOL comment on the last line`() {
        val code =
            """
            val foo = bar
                .length() // some-comment

            val foobar = "foobar"
            """.trimIndent()
        val formattedCode =
            """
            val foo =
                bar
                    .length() // some-comment

            val foobar = "foobar"
            """.trimIndent()
        multilineExpressionWrappingRuleAssertThat(code)
            .addAdditionalRuleProvider { IndentationRule() }
            .withEditorConfigOverride(CODE_STYLE_PROPERTY to CodeStyleValue.KTLINT_OFFICIAL)
            .hasLintViolation(1, 11, "A multiline expression should start on a new line")
            .isFormattedAs(formattedCode)
    }

    @Test
    fun `Given an assignment to variable`() {
        val code =
            """
            fun foo() {
                var givenCode: String

                givenCode = $MULTILINE_STRING_QUOTE
                    some text
                    $MULTILINE_STRING_QUOTE.trimIndent()
            }
            """.trimIndent()
        val formattedCode =
            """
            fun foo() {
                var givenCode: String

                givenCode =
                    $MULTILINE_STRING_QUOTE
                    some text
                    $MULTILINE_STRING_QUOTE.trimIndent()
            }
            """.trimIndent()
        multilineExpressionWrappingRuleAssertThat(code)
            .addAdditionalRuleProvider { IndentationRule() }
            .withEditorConfigOverride(CODE_STYLE_PROPERTY to CodeStyleValue.KTLINT_OFFICIAL)
            .hasLintViolation(4, 17, "A multiline expression should start on a new line")
            .isFormattedAs(formattedCode)
    }

    @Test
    fun `Given a comparison in which the right hand side is a multiline expression`() {
        val code =
            """
            fun foo(bar: String): Boolean {
                return bar != $MULTILINE_STRING_QUOTE
                    some text
                $MULTILINE_STRING_QUOTE.trimIndent()
            }
            """.trimIndent()
        val formattedCode =
            """
            fun foo(bar: String): Boolean {
                return bar !=
                    $MULTILINE_STRING_QUOTE
                    some text
                    $MULTILINE_STRING_QUOTE.trimIndent()
            }
            """.trimIndent()
        multilineExpressionWrappingRuleAssertThat(code)
            .addAdditionalRuleProvider { IndentationRule() }
            .withEditorConfigOverride(CODE_STYLE_PROPERTY to CodeStyleValue.KTLINT_OFFICIAL)
            .hasLintViolation(2, 19, "A multiline expression should start on a new line")
            .isFormattedAs(formattedCode)
    }

    @Test
    fun `Given an elvis operator followed by a multiline expression then do not reformat`() {
        val code =
            """
            fun fooBar(foobar: String?, bar: String) =
                foo
                    ?.lowercase()
                    ?: bar
                        .uppercase()
                        .trimIndent()
            """.trimIndent()
        multilineExpressionWrappingRuleAssertThat(code)
            .addAdditionalRuleProvider { IndentationRule() }
            .hasNoLintViolations()
    }

    @Test
    fun `Issue 2183 - Given a multiline postfix expression then reformat`() {
        val code =
            """
            val foobar = foo!!
                .bar()
            """.trimIndent()
        val formattedCode =
            """
            val foobar =
                foo!!
                    .bar()
            """.trimIndent()
        multilineExpressionWrappingRuleAssertThat(code)
            .addAdditionalRuleProvider { IndentationRule() }
            .hasLintViolation(1, 14, "A multiline expression should start on a new line")
            .isFormattedAs(formattedCode)
    }

    @Test
    fun `Issue 2188 - Given a multiline prefix expression then reformat but do not wrap after prefix operator`() {
        val code =
            """
            val bar = bar(
                *foo(
                    "a",
                    "b"
                )
            )
            """.trimIndent()
        val formattedCode =
            """
            val bar =
                bar(
                    *foo(
                        "a",
                        "b"
                    )
                )
            """.trimIndent()
        multilineExpressionWrappingRuleAssertThat(code)
            .addAdditionalRuleProvider { IndentationRule() }
            .hasLintViolation(1, 11, "A multiline expression should start on a new line")
            .isFormattedAs(formattedCode)
    }

    @Test
    fun `Issue 2286 - `() {
        val code =
            """
            val foo = foo() + bar1 {
                "bar1"
            } +
            bar2 {
                "bar2"
            }
            """.trimIndent()
        val formattedCode =
            """
            val foo =
                foo() +
                    bar1 {
                        "bar1"
                    } +
                    bar2 {
                        "bar2"
                    }
            """.trimIndent()
        multilineExpressionWrappingRuleAssertThat(code)
            .addAdditionalRuleProvider { IndentationRule() }
            .hasLintViolations(
                LintViolation(1, 11, "A multiline expression should start on a new line"),
                LintViolation(1, 19, "A multiline expression should start on a new line"),
            ).isFormattedAs(formattedCode)
    }

    @Disabled
    @Test
    fun `Issue 2286 - xx `() {
        val code =
            """
            val foo = foo() + bar1 {
                "bar1"
            } + "bar3" +
            bar2 {
                "bar2"
            }
            """.trimIndent()
        val formattedCode =
            """
            val foo =
                foo() +
                    bar1 {
                        "bar1"
                    } +
                    bar2 {
                        "bar2"
                    }
            """.trimIndent()
        multilineExpressionWrappingRuleAssertThat(code)
            .addAdditionalRuleProvider { IndentationRule() }
            .hasLintViolations(
                LintViolation(1, 11, "A multiline expression should start on a new line"),
                LintViolation(1, 19, "A multiline expression should start on a new line"),
            ).isFormattedAs(formattedCode)
    }
}
