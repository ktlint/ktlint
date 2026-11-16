package io.github.ktlint.core.ruleset.standard.rules

import io.github.ktlint.core.ruleset.standard.rules.ChainMethodContinuationRule.Companion.FORCE_MULTILINE_WHEN_CHAIN_OPERATOR_COUNT_GREATER_OR_EQUAL_THAN_PROPERTY
import io.github.ktlint.core.test.KtLintAssertThat.Companion.EOL_CHAR
import io.github.ktlint.core.test.KtLintAssertThat.Companion.MAX_LINE_LENGTH_MARKER
import io.github.ktlint.core.test.KtLintAssertThat.Companion.assertThatRuleBuilder
import io.github.ktlint.core.test.LintViolation
import io.github.ktlint.core.test.MULTILINE_STRING_QUOTE
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class ChainMethodContinuationRuleTest {
    private val chainMethodContinuationRuleAssertThat =
        assertThatRuleBuilder { ChainMethodContinuationRule() }
            .addAdditionalRuleProvider { MaxLineLengthRule() }
            .assertThat()

    @Test
    fun `Given that no maximum line length is set, and a single line method chain does not exceed the maximum number of chain operators then do not wrap`() {
        val code =
            """
            val foo = listOf(1, 2, 3).filter { it > 2 }!!.takeIf { it.count() > 100 }?.sum()!!
            """.trimIndent()
        chainMethodContinuationRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `Given that no maximum line length is set, and a single line method chain does exceed the maximum number of chain operators then wrap`() {
        val code =
            """
            val foo = listOf(1, 2, 3).filter { it > 2 }!!.takeIf { it.count() > 100 }?.sum()!!
            """.trimIndent()
        val formattedCode =
            """
            val foo = listOf(1, 2, 3)
                .filter { it > 2 }!!
                .takeIf { it.count() > 100 }
                ?.sum()!!
            """.trimIndent()
        chainMethodContinuationRuleAssertThat(code)
            .withEditorConfigOverride(FORCE_MULTILINE_WHEN_CHAIN_OPERATOR_COUNT_GREATER_OR_EQUAL_THAN_PROPERTY to 3)
            .hasLintViolations(
                LintViolation(1, 26, "Expected newline before '.'"),
                LintViolation(1, 46, "Expected newline before '.'"),
                LintViolation(1, 74, "Expected newline before '?.'"),
            ).isFormattedAs(formattedCode)
    }

    @Test
    fun `Given that maximum line length is set but not exceeded, and a single line method chain does not exceed the maximum line length then do not wrap`() {
        val code =
            """
            // $MAX_LINE_LENGTH_MARKER                                                                       $EOL_CHAR
            val foo = listOf(1, 2, 3).filter { it > 2 }!!.takeIf { it.count() > 100 }.map { it * it }?.sum()!!
            """.trimIndent()
        chainMethodContinuationRuleAssertThat(code)
            .setMaxLineLength()
            .withEditorConfigOverride(FORCE_MULTILINE_WHEN_CHAIN_OPERATOR_COUNT_GREATER_OR_EQUAL_THAN_PROPERTY to 5)
            .hasNoLintViolations()
    }

    @Test
    fun `Given that maximum line length is set, and a single line method chain does exceed the maximum line length then wrap`() {
        val code =
            """
            // $MAX_LINE_LENGTH_MARKER                            $EOL_CHAR
            val foo1 = "foo".filter { it.isUpperCase() }.lowercase()
            @Suppress("ktlint:standard:max-line-length")
            val foo2 = "foo".filter { it.isUpperCase() }.lowercase()
            """.trimIndent()
        val formattedCode =
            """
            // $MAX_LINE_LENGTH_MARKER                            $EOL_CHAR
            val foo1 = "foo"
                .filter { it.isUpperCase() }
                .lowercase()
            @Suppress("ktlint:standard:max-line-length")
            val foo2 = "foo".filter { it.isUpperCase() }.lowercase()
            """.trimIndent()
        chainMethodContinuationRuleAssertThat(code)
            .setMaxLineLength()
            .hasLintViolations(
                LintViolation(2, 17, "Expected newline before '.'"),
                // During formatting this violation will not occur due to wrapping of previous chain operators
                LintViolation(2, 45, "Expected newline before '.'"),
            ).isFormattedAs(formattedCode)
    }

    @Test
    fun `Given method chain which is correctly wrapped as multiline`() {
        val code =
            """
            val foo = listOf(1, 2, 3)
                .filter { it > 2 }!!
                .takeIf { it.count() > 100 }
                .map {
                    it * it
                }?.sum()!!
            """.trimIndent()
        chainMethodContinuationRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `Given a method chain which wrapped after the dot operator instead of before the dot operator`() {
        val code =
            """
            val foo = listOf(1, 2, 3).
                filter { it > 2 }.
                filter {
                    it > 2
                }.
                sum()
            """.trimIndent()
        val formattedCode =
            """
            val foo = listOf(1, 2, 3)
                .filter { it > 2 }
                .filter {
                    it > 2
                }.sum()
            """.trimIndent()
        chainMethodContinuationRuleAssertThat(code)
            .hasLintViolations(
                LintViolation(1, 26, "Expected newline before '.'"),
                LintViolation(1, 26, "Unexpected newline after '.'"),
                LintViolation(2, 22, "Expected newline before '.'"),
                LintViolation(2, 22, "Unexpected newline after '.'"),
                LintViolation(5, 6, "Unexpected newline after '.'"),
            ).isFormattedAs(formattedCode)
    }

    @Test
    fun `Given a method chain on the same line as the previous method chain`() {
        val code =
            """
            val foo = listOf(1, 2, 3, 4).
                filter { it > 2 }.filter { it > 3 }.
                filter {
                    it > 4
                }.
                sum().dec()
            """.trimIndent()
        val formattedCode =
            """
            val foo = listOf(1, 2, 3, 4)
                .filter { it > 2 }
                .filter { it > 3 }
                .filter {
                    it > 4
                }.sum()
                .dec()
            """.trimIndent()
        chainMethodContinuationRuleAssertThat(code)
            .hasLintViolations(
                LintViolation(1, 29, "Expected newline before '.'"),
                LintViolation(1, 29, "Unexpected newline after '.'"),
                LintViolation(2, 22, "Expected newline before '.'"),
                LintViolation(2, 40, "Expected newline before '.'"),
                LintViolation(2, 40, "Unexpected newline after '.'"),
                LintViolation(5, 6, "Unexpected newline after '.'"),
                LintViolation(6, 10, "Expected newline before '.'"),
            ).isFormattedAs(formattedCode)
    }

    @Test
    fun `Given next method chain which is incorrectly wrapped with !! operator`() {
        val code =
            """
            val foo = listOf(1, 2, 3)!!.
                filter { it > 2 }!!.
                filter {
                    it > 2
                }!!.
                sum()
            """.trimIndent()
        val formattedCode =
            """
            val foo = listOf(1, 2, 3)!!
                .filter { it > 2 }!!
                .filter {
                    it > 2
                }!!
                .sum()
            """.trimIndent()
        chainMethodContinuationRuleAssertThat(code)
            .hasLintViolations(
                LintViolation(1, 28, "Expected newline before '.'"),
                LintViolation(1, 28, "Unexpected newline after '.'"),
                LintViolation(2, 24, "Expected newline before '.'"),
                LintViolation(2, 24, "Unexpected newline after '.'"),
                LintViolation(5, 8, "Expected newline before '.'"),
                LintViolation(5, 8, "Unexpected newline after '.'"),
            ).isFormattedAs(formattedCode)
    }

    @Test
    fun `Given next method chain which is incorrectly wrapped with safe dot operator`() {
        val code =
            """
            val foo = listOf(1, 2, 3)?.
                filter { it > 2 }?.
                filter {
                    it > 2
                }?.
                sum()
            """.trimIndent()
        val formattedCode =
            """
            val foo = listOf(1, 2, 3)
                ?.filter { it > 2 }
                ?.filter {
                    it > 2
                }?.sum()
            """.trimIndent()
        chainMethodContinuationRuleAssertThat(code)
            .hasLintViolations(
                LintViolation(1, 26, "Expected newline before '?.'"),
                LintViolation(1, 27, "Unexpected newline after '?.'"),
                LintViolation(2, 22, "Expected newline before '?.'"),
                LintViolation(2, 23, "Unexpected newline after '?.'"),
                LintViolation(5, 7, "Unexpected newline after '?.'"),
            ).isFormattedAs(formattedCode)
    }

    @Nested
    inner class `Given a method chain which is incorrectly wrapped with preceding closing element of preceding chain` {
        @Test
        fun `Given that first method in chain has a closing parenthesis on a separate line`() {
            val code =
                """
                val foo = listOf(
                    1,
                    2,
                    3
                )
                .sum()
                """.trimIndent()
            val formattedCode =
                """
                val foo = listOf(
                    1,
                    2,
                    3
                ).sum()
                """.trimIndent()
            chainMethodContinuationRuleAssertThat(code)
                .addAdditionalRuleProvider { IndentationRule() }
                .hasLintViolation(6, 1, "Unexpected newline before '.'")
                .isFormattedAs(formattedCode)
        }

        @Test
        fun `Given that first method in chain has a closing brace on a separate line`() {
            val code =
                """
                val foo1 = listOf(1, 2, 3)
                    .filter {
                        it > 2
                    }
                    .sum()
                """.trimIndent()
            val formattedCode =
                """
                val foo1 = listOf(1, 2, 3)
                    .filter {
                        it > 2
                    }.sum()
                """.trimIndent()
            chainMethodContinuationRuleAssertThat(code)
                .addAdditionalRuleProvider { IndentationRule() }
                .hasLintViolation(5, 5, "Unexpected newline before '.'")
                .isFormattedAs(formattedCode)
        }

        @Test
        fun `Given that first method in chain has a closing bracket on a separate line`() {
            val code =
                """
                val foo =
                    matrix[
                        row,
                        column,
                    ]
                    .foo()
                """.trimIndent()
            val formattedCode =
                """
                val foo =
                    matrix[
                        row,
                        column,
                    ].foo()
                """.trimIndent()
            chainMethodContinuationRuleAssertThat(code)
                .hasLintViolation(6, 5, "Unexpected newline before '.'")
                .isFormattedAs(formattedCode)
        }

        @Test
        fun `Given that first method in chain has a closing quotes on a separate line`() {
            val code =
                """
                val foo =
                    $MULTILINE_STRING_QUOTE
                    Some text
                    $MULTILINE_STRING_QUOTE
                    .trimIndent()
                """.trimIndent()
            val formattedCode =
                """
                val foo =
                    $MULTILINE_STRING_QUOTE
                    Some text
                    $MULTILINE_STRING_QUOTE.trimIndent()
                """.trimIndent()
            chainMethodContinuationRuleAssertThat(code)
                .hasLintViolation(5, 5, "Unexpected newline before '.'")
                .isFormattedAs(formattedCode)
        }
    }

    @Test
    fun `Given a multiline method chain which is correctly wrapped`() {
        val code =
            """
            val foo1 = listOf(1, 2, 3)
                .filter { it > 2 }
                .sum()
            val foo2 = listOf(1, 2, 3)
                .filter { it > 2 }!!
                .sum()
            val foo3 = listOf(1, 2, 3)
            .filter { it > 2 }!!
                ?.sum()
            """.trimIndent()
        chainMethodContinuationRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `Given a multiline method chain which is not correctly wrapped`() {
        val code =
            """
            val foo1 = listOf(1, 2, 3).filter { it > 2 }
                .sum()
            val foo2 = listOf(1, 2, 3).filter { it > 2 }!!
                .sum()
            val foo3 = listOf(1, 2, 3).filter { it > 2 }!!
                ?.sum()
            """.trimIndent()
        val formattedCode =
            """
            val foo1 = listOf(1, 2, 3)
                .filter { it > 2 }
                .sum()
            val foo2 = listOf(1, 2, 3)
                .filter { it > 2 }!!
                .sum()
            val foo3 = listOf(1, 2, 3)
                .filter { it > 2 }!!
                ?.sum()
            """.trimIndent()
        chainMethodContinuationRuleAssertThat(code)
            .hasLintViolations(
                LintViolation(1, 27, "Expected newline before '.'"),
                LintViolation(3, 27, "Expected newline before '.'"),
                LintViolation(5, 27, "Expected newline before '.'"),
            ).isFormattedAs(formattedCode)
    }

    @Test
    fun `Given next chain which is incorrectly wrapped chain operator in object declaration`() {
        val code =
            """
            val foo = object : Runnable {
                override fun run() {
                    /* no-op */
                }
            }.
                run()
            """.trimIndent()
        val formattedCode =
            """
            val foo = object : Runnable {
                override fun run() {
                    /* no-op */
                }
            }.run()
            """.trimIndent()
        chainMethodContinuationRuleAssertThat(code)
            .hasLintViolation(5, 2, "Unexpected newline after '.'")
            .isFormattedAs(formattedCode)
    }

    @Test
    fun `Given nested chains which are correctly wrapped`() {
        val code =
            """
            val foo = listOf(1, 2, 3)
                .filter {
                    listOf(1, 2, 3).map { it * it }.size > 1
                }!!
                .map {
                    it * it
                }
            """.trimIndent()
        chainMethodContinuationRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `Given nested chains which are incorrectly wrapped`() {
        val code =
            """
            val foo = listOf(1, 2, 3).
                filter {
                    listOf(1, 2, 3).
                        map {
                            it * it
                        }.size > 1
                }!!.
                map {
                    it * it
                }
            """.trimIndent()
        val formattedCode =
            """
            val foo = listOf(1, 2, 3)
                .filter {
                    listOf(1, 2, 3)
                        .map {
                            it * it
                        }.size > 1
                }!!
                .map {
                    it * it
                }
            """.trimIndent()
        chainMethodContinuationRuleAssertThat(code)
            .hasLintViolations(
                LintViolation(1, 26, "Expected newline before '.'"),
                LintViolation(1, 26, "Unexpected newline after '.'"),
                LintViolation(3, 24, "Expected newline before '.'"),
                LintViolation(3, 24, "Unexpected newline after '.'"),
                LintViolation(7, 8, "Expected newline before '.'"),
                LintViolation(7, 8, "Unexpected newline after '.'"),
            ).isFormattedAs(formattedCode)
    }

    @Test
    fun `Given a multiline method chain then wrap each method chain to a separate line`() {
        val code =
            """
            val foo1 = listOf(1, 2, 3)
                .bar().foo()?.fooBar()
            val foo2 = listOf(1, 2, 3).bar()
                .foo()?.fooBar()
            val foo3 = listOf(1, 2, 3).bar().foo()
                ?.fooBar()
            """.trimIndent()
        val formattedCode =
            """
            val foo1 = listOf(1, 2, 3)
                .bar()
                .foo()
                ?.fooBar()
            val foo2 = listOf(1, 2, 3)
                .bar()
                .foo()
                ?.fooBar()
            val foo3 = listOf(1, 2, 3)
                .bar()
                .foo()
                ?.fooBar()
            """.trimIndent()
        chainMethodContinuationRuleAssertThat(code)
            .hasLintViolations(
                LintViolation(2, 11, "Expected newline before '.'"),
                LintViolation(2, 17, "Expected newline before '?.'"),
                LintViolation(3, 27, "Expected newline before '.'"),
                LintViolation(4, 11, "Expected newline before '?.'"),
                LintViolation(5, 27, "Expected newline before '.'"),
                LintViolation(5, 33, "Expected newline before '.'"),
            ).isFormattedAs(formattedCode)
    }

    @Test
    fun `Given a simple multiline method chain then do not wrap`() {
        val code =
            """
            val foo = listOf(1, 2, 3).map {
                it.foo()
            }
            """.trimIndent()
        chainMethodContinuationRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `Given a raw string literal which exceeds the maximum line length and contains a dot qualified expression`() {
        val code =
            $$"""
            // $$MAX_LINE_LENGTH_MARKER  $$EOL_CHAR
            val foo =
                $$MULTILINE_STRING_QUOTE
                Some text and a ${foo.bar}
                $$MULTILINE_STRING_QUOTE
            """.trimIndent()
        chainMethodContinuationRuleAssertThat(code)
            .setMaxLineLength()
            .hasNoLintViolations()
    }

    @Test
    fun `Given a multiline raw string literal followed by multiple chains on the same line as the closing quotes`() {
        val code =
            """
            val foo =
                $MULTILINE_STRING_QUOTE
                Some text
                $MULTILINE_STRING_QUOTE.trimIndent().uppercase()
            """.trimIndent()
        chainMethodContinuationRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `Given a Java class literal expression which exceeds the maximum line length then do not break on the dot between 'class' and 'java'`() {
        val code =
            """
            // $MAX_LINE_LENGTH_MARKER                        $EOL_CHAR
            val foo1 = Foo::class.java.canonicalName
            val foo2 = Foo::class.java.canonicalName.uppercase()
            """.trimIndent()
        val formattedCode =
            """
            // $MAX_LINE_LENGTH_MARKER                        $EOL_CHAR
            val foo1 = Foo::class.java.canonicalName
            val foo2 = Foo::class.java.canonicalName
                .uppercase()
            """.trimIndent()
        chainMethodContinuationRuleAssertThat(code)
            .setMaxLineLength()
            .hasLintViolation(3, 41, "Expected newline before '.'")
            .isFormattedAs(formattedCode)
    }

    @Nested
    inner class `Given a chained expression as function argument` {
        @Test
        fun `Issue 2452 - Given that the argument-list-wrapping rule is not loaded or not enabled`() {
            val code =
                """
                val foo1 = requireNotNull(bar.filter { it == 'b' }.filter { it == 'a' }
                        .filter { it == 'r' })
                val foo2 = requireNotNull(bar.filter { it == 'b' }.filter { it == 'a' }.filter { it == 'r' })
                """.trimIndent()
            val formattedCode =
                """
                val foo1 = requireNotNull(bar
                    .filter { it == 'b' }
                    .filter { it == 'a' }
                    .filter { it == 'r' })
                val foo2 = requireNotNull(bar.filter { it == 'b' }.filter { it == 'a' }.filter { it == 'r' })
                """.trimIndent()
            chainMethodContinuationRuleAssertThat(code)
                .addAdditionalRuleProvider { IndentationRule() }
                .isFormattedAs(formattedCode)
        }

        @Test
        fun `Wrapping the argument has precedence above wrapping on the chain operator`() {
            val code =
                """
                // $MAX_LINE_LENGTH_MARKER                         $EOL_CHAR
                val foo1 = requireNotNull(bar.filter { it == "bar" }) { "some message" }
                val foo2 = requireNotNull(bar?.filter { it == "bar" }) { "some message" }
                """.trimIndent()
            val formattedCode =
                """
                // $MAX_LINE_LENGTH_MARKER                         $EOL_CHAR
                val foo1 = requireNotNull(
                    bar.filter { it == "bar" }
                ) { "some message" }
                val foo2 = requireNotNull(
                    bar?.filter { it == "bar" }
                ) { "some message" }
                """.trimIndent()
            chainMethodContinuationRuleAssertThat(code)
                .setMaxLineLength()
                .addAdditionalRuleProvider { ArgumentListWrappingRule() }
                .hasNoLintViolationsExceptInAdditionalRules()
                .isFormattedAs(formattedCode)
        }

        @Test
        fun `Given the argument-list-wrapping and function-literal rules are enabled`() {
            val code =
                """
                // $MAX_LINE_LENGTH_MARKER   $EOL_CHAR
                val foo1 = requireNotNull(bar.filter { it == "bar" }) { "some message" }
                val foo2 = requireNotNull(bar?.filter { it == "bar" }) { "some message" }
                """.trimIndent()
            val formattedCode =
                """
                // $MAX_LINE_LENGTH_MARKER   $EOL_CHAR
                val foo1 = requireNotNull(
                    bar.filter { it == "bar" }
                ) { "some message" }
                val foo2 = requireNotNull(
                    bar?.filter {
                        it == "bar"
                    }
                ) { "some message" }
                """.trimIndent()
            chainMethodContinuationRuleAssertThat(code)
                .setMaxLineLength()
                .addAdditionalRuleProvider { ArgumentListWrappingRule() }
                .addAdditionalRuleProvider { FunctionLiteralRule() }
                .addAdditionalRuleProvider { MaxLineLengthRule() }
                .addAdditionalRuleProvider { IndentationRule() }
                .hasLintViolations(
                    // Lint violation below will not be triggered during format as argument-list-wrapping rule prevents this error from occurring
                    LintViolation(2, 30, "Expected newline before '.'"),
                    LintViolation(3, 30, "Expected newline before '?.'"),
                ).isFormattedAs(formattedCode)
        }

        @Test
        fun `Given the argument-list-wrapping, function-literal, multiline-expression and indent rules are enabled`() {
            val code =
                """
                // $MAX_LINE_LENGTH_MARKER   $EOL_CHAR
                val foo1 = requireNotNull(bar.filter { it == "bar" }) { "some message" }
                val foo2 = requireNotNull(bar?.filter { it == "bar" }) { "some message" }
                """.trimIndent()
            val formattedCode =
                """
                // $MAX_LINE_LENGTH_MARKER   $EOL_CHAR
                val foo1 =
                    requireNotNull(
                        bar.filter {
                            it == "bar"
                        }
                    ) { "some message" }
                val foo2 =
                    requireNotNull(
                        bar?.filter {
                            it == "bar"
                        }
                    ) { "some message" }
                """.trimIndent()
            chainMethodContinuationRuleAssertThat(code)
                .setMaxLineLength()
                .withRerunFormatAfterAutocorrect(
                    "Once the ChainMethodContinuationRule rule has ran, the function body expression becomes a multiline expression. As " +
                        "the root call expression 'requireNot(...)' has already been processed, the MultilineExpressionWrappingRule will " +
                        "not be triggered until the next format round.",
                ).addAdditionalRuleProvider { ArgumentListWrappingRule() }
                .addAdditionalRuleProvider { FunctionLiteralRule() }
                .addAdditionalRuleProvider { MultilineExpressionWrappingRule() }
                .addAdditionalRuleProvider { IndentationRule() }
                .addAdditionalRuleProvider { MaxLineLengthRule() }
                .isFormattedAs(formattedCode)
                .hasLintViolations(
                    // Lint violation below will not be triggered during format as argument-list-wrapping rule prevents this error from occurring
                    LintViolation(2, 30, "Expected newline before '.'"),
                    LintViolation(3, 30, "Expected newline before '?.'"),
                ).isFormattedAs(formattedCode)
        }
    }

    @Test
    fun `Given a single line chained expression, not exceeding max line length, with too many chain operators`() {
        val code =
            """
            val foo = listOf(1, 2, 3).foo1().foo2().foo3().foo4().foo5().foo6()
            """.trimIndent()
        val formattedCode =
            """
            val foo = listOf(1, 2, 3)
                .foo1()
                .foo2()
                .foo3()
                .foo4()
                .foo5()
                .foo6()
            """.trimIndent()
        chainMethodContinuationRuleAssertThat(code)
            .hasLintViolations(
                LintViolation(1, 26, "Expected newline before '.'"),
                LintViolation(1, 33, "Expected newline before '.'"),
                LintViolation(1, 40, "Expected newline before '.'"),
                LintViolation(1, 47, "Expected newline before '.'"),
                LintViolation(1, 54, "Expected newline before '.'"),
                LintViolation(1, 61, "Expected newline before '.'"),
            ).isFormattedAs(formattedCode)
    }

    @Test
    fun `Given a chained method including an array access expression`() {
        val code =
            """
            val foo =
                arrayOf(1, 2, 3).bar[0].foo()
                    ?.foobar()
            """.trimIndent()
        val formattedCode =
            """
            val foo =
                arrayOf(1, 2, 3)
                    .bar[0]
                    .foo()
                    ?.foobar()
            """.trimIndent()
        chainMethodContinuationRuleAssertThat(code)
            .hasLintViolations(
                LintViolation(2, 21, "Expected newline before '.'"),
                LintViolation(2, 28, "Expected newline before '.'"),
            ).isFormattedAs(formattedCode)
    }

    @Nested
    inner class `Given chain with comments` {
        @Test
        fun `Comments between a DOT chain operator and the next chained method are disallowed`() {
            val code =
                """
                val foo1 = listOf(1, 2, 3)
                    ./** some comment */size
                val foo2 = listOf(1, 2, 3)
                    ./** some comment */single()
                val foo3 = listOf(1, 2, 3)
                    ./** some comment */filter { it > 2 }
                val foo4 = listOf(1, 2, 3)
                    ./* some comment */size
                val foo5 = listOf(1, 2, 3)
                    ./* some comment */single()
                val foo6 = listOf(1, 2, 3)
                    ./* some comment */filter { it > 2 }
                val foo7 = listOf(1, 2, 3)
                    .// some comment
                    size
                val foo8 = listOf(1, 2, 3)
                    .// some comment
                    single()
                val foo9 = listOf(1, 2, 3)
                    .// some comment
                    filter { it > 2 }
                """.trimIndent()
            chainMethodContinuationRuleAssertThat(code)
                .hasLintViolations(
                    LintViolation(2, 6, "No comment expected at this location in method chain", false),
                    LintViolation(4, 6, "No comment expected at this location in method chain", false),
                    LintViolation(6, 6, "No comment expected at this location in method chain", false),
                    LintViolation(8, 6, "No comment expected at this location in method chain", false),
                    LintViolation(10, 6, "No comment expected at this location in method chain", false),
                    LintViolation(12, 6, "No comment expected at this location in method chain", false),
                    LintViolation(14, 6, "No comment expected at this location in method chain", false),
                    LintViolation(17, 6, "No comment expected at this location in method chain", false),
                    LintViolation(20, 6, "No comment expected at this location in method chain", false),
                ).hasNoLintViolationsForRuleId(CHAIN_WRAPPING_RULE_ID)
        }

        @Test
        fun `Comments between a SAFE ACCESS chain operator and the next chained method are disallowed`() {
            val code =
                """
                val foo1 = listOf(1, 2, 3)
                    ?./** some comment */size
                val foo2 = listOf(1, 2, 3)
                    ?./** some comment */single()
                val foo3 = listOf(1, 2, 3)
                    ?./** some comment */filter { it > 2 }
                val foo4 = listOf(1, 2, 3)
                    ?./* some comment */size
                val foo5 = listOf(1, 2, 3)
                    ?./* some comment */single()
                val foo6 = listOf(1, 2, 3)
                    ?./* some comment */filter { it > 2 }
                val foo7 = listOf(1, 2, 3)
                    ?.// some comment
                    size
                val foo8 = listOf(1, 2, 3)
                    ?.// some comment
                    single()
                val foo9 = listOf(1, 2, 3)
                    ?.// some comment
                    filter { it > 2 }
                """.trimIndent()
            chainMethodContinuationRuleAssertThat(code)
                .hasLintViolations(
                    LintViolation(2, 7, "No comment expected at this location in method chain", false),
                    LintViolation(4, 7, "No comment expected at this location in method chain", false),
                    LintViolation(6, 7, "No comment expected at this location in method chain", false),
                    LintViolation(8, 7, "No comment expected at this location in method chain", false),
                    LintViolation(10, 7, "No comment expected at this location in method chain", false),
                    LintViolation(12, 7, "No comment expected at this location in method chain", false),
                    LintViolation(14, 7, "No comment expected at this location in method chain", false),
                    LintViolation(17, 7, "No comment expected at this location in method chain", false),
                    LintViolation(20, 7, "No comment expected at this location in method chain", false),
                ).hasNoLintViolationsForRuleId(CHAIN_WRAPPING_RULE_ID)
        }

        @Test
        fun `Given a single line method chain with comments before the chain operator`() {
            val code =
                """
                val foo = listOf(1, 2, 3) /* 1 */ .filter { it > 2 }!! /* 2 */ .takeIf { it.count() > 100 } /* 3 */?.sum()!!
                """.trimIndent()
            val formattedCode =
                """
                val foo = listOf(1, 2, 3) /* 1 */
                    .filter { it > 2 }!! /* 2 */
                    .takeIf { it.count() > 100 } /* 3 */
                    ?.sum()!!
                """.trimIndent()
            chainMethodContinuationRuleAssertThat(code)
                .hasLintViolations(
                    LintViolation(1, 35, "Expected newline before '.'"),
                    LintViolation(1, 64, "Expected newline before '.'"),
                    LintViolation(1, 100, "Expected newline before '?.'"),
                ).isFormattedAs(formattedCode)
        }

        @Test
        fun `Given a multiline method chain with comments at end of previous line and chain operator on the next line`() {
            val code =
                """
                val foo1 = listOf(1, 2, 3) /* 1 */
                    .filter { it > 2 }!! /* 2 */
                    .takeIf { it.count() > 100 } /* 3 */
                    ?.sum()!!
                val foo2 = listOf(1, 2, 3) // 1
                    .filter { it > 2 }!! // 2
                    .takeIf { it.count() > 100 } // 3
                    ?.sum()!!
                """.trimIndent()
            chainMethodContinuationRuleAssertThat(code).hasNoLintViolations()
        }

        @Test
        fun `Given method chain which is incorrectly wrapped with dot operator with block comments`() {
            val code =
                """
                val foo = listOf(1, 2, 3)/* 1 */.filter {
                        it > 2
                    }/* 2 */.sum()
                """.trimIndent()
            val formattedCode =
                """
                val foo = listOf(1, 2, 3)/* 1 */
                    .filter {
                        it > 2
                    }/* 2 */
                    .sum()
                """.trimIndent()
            chainMethodContinuationRuleAssertThat(code)
                .hasLintViolations(
                    LintViolation(1, 33, "Expected newline before '.'"),
                    LintViolation(3, 13, "Expected newline before '.'"),
                ).isFormattedAs(formattedCode)
        }

        @Test
        fun `Given method chain which is incorrectly wrapped in next line with previous RBrace with block comments`() {
            val code =
                """
                val foo = listOf(1, 2, 3).filter {
                    it > 2
                } // 1
                  /* 2 */.sum()
                """.trimIndent()
            val formattedCode =
                """
                val foo = listOf(1, 2, 3)
                    .filter {
                        it > 2
                    } // 1
                    /* 2 */
                    .sum()
                """.trimIndent()
            chainMethodContinuationRuleAssertThat(code)
                .addAdditionalRuleProvider { IndentationRule() }
                .hasLintViolations(
                    LintViolation(1, 26, "Expected newline before '.'"),
                    LintViolation(4, 10, "Expected newline before '.'"),
                ).isFormattedAs(formattedCode)
        }

        @Test
        fun `Given method chain which is incorrectly wrapped in next line with previous RBrace with single line comments`() {
            val code =
                """
                val foo = listOf(1, 2, 3).filter {
                    it > 2
                }// 1
                    .sum()
                """.trimIndent()
            val formattedCode =
                """
                val foo = listOf(1, 2, 3)
                    .filter {
                        it > 2
                    }// 1
                    .sum()
                """.trimIndent()
            chainMethodContinuationRuleAssertThat(code)
                .addAdditionalRuleProvider { IndentationRule() }
                .hasLintViolation(1, 26, "Expected newline before '.'")
                .isFormattedAs(formattedCode)
        }
    }

    @ParameterizedTest(name = "Code: {0}")
    @ValueSource(
        strings = [
            """
            fun String.foo() = "foo"
            """,
            """
            class FooBar : Foo.Bar
            """,
            """
            val Foo.bar: Bar
            """,
            """
            fun foo(bar: FooBar.() -> Unit) {}
            """,
            """
            /**
             * Some comment with [Foo.Bar] reference
             */
            """,
        ],
    )
    fun `Given a chain operator not in a dot qualified or safe access expression`(code: String) {
        chainMethodContinuationRuleAssertThat(code.trimIndent())
            .hasNoLintViolations()
    }

    @ParameterizedTest(name = "Code: {0}")
    @ValueSource(
        strings = [
            """
            // $MAX_LINE_LENGTH_MARKER    $EOL_CHAR
            package Fooooooooooooooooooo.Bar
            """,
            """
            // $MAX_LINE_LENGTH_MARKER   $EOL_CHAR
            import Fooooooooooooooooooo.Bar
            """,
        ],
    )
    fun `Given that max line length is set and a chain operator not in a dot qualified or safe access expression`(code: String) {
        chainMethodContinuationRuleAssertThat(code.trimIndent())
            .setMaxLineLength()
            .hasNoLintViolations()
    }

    @Test
    fun `Issue 2304 - Given a dot qualified expression in which the call expression returns a function which is called with value argument`() {
        val code =
            """
            fun foo(baz: Baz) = bar.get()(baz)
            """.trimIndent()
        chainMethodContinuationRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `Issue 2304 - Given a dot qualified expression in which the call expression returns a function which is called with value argument and has a trailing lambda`() {
        val code =
            """
            fun foo(baz: Baz) =
                bar.get()(baz) {
                    // do something
                }
            """.trimIndent()
        chainMethodContinuationRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `Issue 2450 - Given a chained method and a chain is followed by EOL comment that causes max line length to be exceeded then only report violation via max-line-length rule`() {
        val code =
            """
            // $MAX_LINE_LENGTH_MARKER                                $EOL_CHAR
            val foo1 = "foo".filter { it.isUpperCase() }.lowercase() // Some comment
            val foo2 =
                "foo"
                    .filter { it.isUpperCase() } // Some longgggggggg comment
                    .lowercase()
            """.trimIndent()
        chainMethodContinuationRuleAssertThat(code)
            .setMaxLineLength()
            .addAdditionalRuleProvider { MaxLineLengthRule() }
            .hasLintViolationsForAdditionalRule(
                LintViolation(2, 60, "Exceeded max line length (59)", false),
                LintViolation(5, 60, "Exceeded max line length (59)", false),
            ).hasNoLintViolationsExceptInAdditionalRules()
    }

    @Test
    fun `Issue 2450 - Given a chained method and a chain contains an EOL comment that causes max line length to be exceeded then only report violation via max-line-length rule`() {
        val code =
            """
            // $MAX_LINE_LENGTH_MARKER                  $EOL_CHAR
            val foo2 =
                "foo"
                    .filter {
                         it
                             .uppercase()  // Some comment
                             .isUpperCase()
                    }.lowercase()
            """.trimIndent()
        chainMethodContinuationRuleAssertThat(code)
            .setMaxLineLength()
            .addAdditionalRuleProvider { MaxLineLengthRule() }
            .hasLintViolationsForAdditionalRule(
                LintViolation(6, 46, "Exceeded max line length (45)", false),
            ).hasNoLintViolationsExceptInAdditionalRules()
    }

    @Test
    fun `Issue 2455 - Given a chained method including some simple reference expressions then do not wrap simple reference expressions`() {
        val code =
            """
            // $MAX_LINE_LENGTH_MARKER      $EOL_CHAR
            fun buildBar(): Foo.Bar = Foo.Bar.builder().baz().baz.build()
            """.trimIndent()
        val formattedCode =
            """
            // $MAX_LINE_LENGTH_MARKER      $EOL_CHAR
            fun buildBar(): Foo.Bar = Foo.Bar
                .builder()
                .baz()
                .baz
                .build()
            """.trimIndent()
        chainMethodContinuationRuleAssertThat(code)
            .setMaxLineLength()
            .addAdditionalRuleProvider { MaxLineLengthRule() }
            .hasLintViolations(
                LintViolation(2, 34, "Expected newline before '.'"),
                LintViolation(2, 44, "Expected newline before '.'"),
                LintViolation(2, 50, "Expected newline before '.'"),
                LintViolation(2, 54, "Expected newline before '.'"),
            ).isFormattedAs(formattedCode)
    }

    @Test
    fun `Issue 2602 - Given a chained method starting with some nested reference expressions then do not wrap the reference expressions`() {
        val code =
            """
            // $MAX_LINE_LENGTH_MARKER                        $EOL_CHAR
            fun buildBar1(): Foo.Bar = Foo.Bar.builder().build()
            fun buildBar2(): Foo.Bar = Foo.bar.Bar.builder().build()
            fun buildBar3(): Foo.Bar = Foo.bar.bar.Bar.builder().build()
            """.trimIndent()
        val formattedCode =
            """
            // $MAX_LINE_LENGTH_MARKER                        $EOL_CHAR
            fun buildBar1(): Foo.Bar = Foo.Bar
                .builder()
                .build()
            fun buildBar2(): Foo.Bar = Foo.bar.Bar
                .builder()
                .build()
            fun buildBar3(): Foo.Bar = Foo.bar.bar.Bar
                .builder()
                .build()
            """.trimIndent()
        chainMethodContinuationRuleAssertThat(code)
            .setMaxLineLength()
            .addAdditionalRuleProvider { MaxLineLengthRule() }
            .hasLintViolations(
                LintViolation(2, 35, "Expected newline before '.'"),
                LintViolation(2, 45, "Expected newline before '.'"),
                LintViolation(3, 39, "Expected newline before '.'"),
                LintViolation(3, 49, "Expected newline before '.'"),
                LintViolation(4, 43, "Expected newline before '.'"),
                LintViolation(4, 53, "Expected newline before '.'"),
            ).isFormattedAs(formattedCode)
    }

    @Test
    fun `Given a chained method having some nested reference expressions after chained method call then do not wrap the reference expressions`() {
        val code =
            """
            // $MAX_LINE_LENGTH_MARKER                        $EOL_CHAR
            fun buildBar1(): Foo.Bar = Foo.baz().Bar.builder().build()
            fun buildBar2(): Foo.Bar = Foo.baz().bar.Bar.builder().build()
            fun buildBar3(): Foo.Bar = Foo.baz().bar.bar.Bar.builder().build()
            """.trimIndent()
        val formattedCode =
            """
            // $MAX_LINE_LENGTH_MARKER                        $EOL_CHAR
            fun buildBar1(): Foo.Bar = Foo
                .baz()
                .Bar
                .builder()
                .build()
            fun buildBar2(): Foo.Bar = Foo
                .baz()
                .bar.Bar
                .builder()
                .build()
            fun buildBar3(): Foo.Bar = Foo
                .baz()
                .bar.bar.Bar
                .builder()
                .build()
            """.trimIndent()
        chainMethodContinuationRuleAssertThat(code)
            .setMaxLineLength()
            .addAdditionalRuleProvider { MaxLineLengthRule() }
            .hasLintViolations(
                LintViolation(2, 31, "Expected newline before '.'"),
                LintViolation(2, 37, "Expected newline before '.'"),
                LintViolation(2, 41, "Expected newline before '.'"),
                LintViolation(2, 51, "Expected newline before '.'"),
                LintViolation(3, 31, "Expected newline before '.'"),
                LintViolation(3, 37, "Expected newline before '.'"),
                LintViolation(3, 45, "Expected newline before '.'"),
                LintViolation(3, 55, "Expected newline before '.'"),
                LintViolation(4, 31, "Expected newline before '.'"),
                LintViolation(4, 37, "Expected newline before '.'"),
                LintViolation(4, 49, "Expected newline before '.'"),
                LintViolation(4, 59, "Expected newline before '.'"),
            ).isFormattedAs(formattedCode)
    }

    @Test
    fun `Issue 2712 - Given some code having more chain operators than the default, but with ktlint_chain_method_rule_force_multiline_when_chain_operator_count_greater_or_equal_than unset`() {
        val code =
            """
            val foo = listOf(1, 2, 3).plus(4).plus(5).plus(6).plus(7).plus(8)
            """.trimIndent()
        require(code.count { it == '.' } > FORCE_MULTILINE_WHEN_CHAIN_OPERATOR_COUNT_GREATER_OR_EQUAL_THAN_PROPERTY.defaultValue)
        chainMethodContinuationRuleAssertThat(code)
            .withEditorConfigOverride(FORCE_MULTILINE_WHEN_CHAIN_OPERATOR_COUNT_GREATER_OR_EQUAL_THAN_PROPERTY to "unset")
            .hasNoLintViolations()
    }
}
