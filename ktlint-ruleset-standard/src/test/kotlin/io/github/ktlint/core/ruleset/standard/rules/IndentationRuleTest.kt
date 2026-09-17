package io.github.ktlint.core.ruleset.standard.rules

import io.github.ktlint.core.rule.engine.core.api.editorconfig.CODE_STYLE_PROPERTY
import io.github.ktlint.core.rule.engine.core.api.editorconfig.CodeStyleValue
import io.github.ktlint.core.rule.engine.core.api.editorconfig.CodeStyleValue.intellij_idea
import io.github.ktlint.core.rule.engine.core.api.editorconfig.CodeStyleValue.ktlint_official
import io.github.ktlint.core.rule.engine.core.api.editorconfig.INDENT_SIZE_PROPERTY
import io.github.ktlint.core.rule.engine.core.api.editorconfig.INDENT_STYLE_PROPERTY
import io.github.ktlint.core.ruleset.standard.rules.IndentationRule.Companion.INDENT_EXPLICIT_CONSTRUCTOR_PROPERTY
import io.github.ktlint.core.test.KtLintAssertThat.Companion.EOL_CHAR
import io.github.ktlint.core.test.KtLintAssertThat.Companion.MAX_LINE_LENGTH_MARKER
import io.github.ktlint.core.test.KtLintAssertThat.Companion.assertThatRule
import io.github.ktlint.core.test.LintViolation
import io.github.ktlint.core.test.MULTILINE_STRING_QUOTE
import io.github.ktlint.core.test.SPACE
import io.github.ktlint.core.test.TAB
import io.github.ktlint.core.test.replaceStringTemplatePlaceholder
import org.ec4j.core.model.PropertyType
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.junit.jupiter.params.provider.ValueSource

@Suppress("RemoveCurlyBracesFromTemplate")
internal class IndentationRuleTest {
    @BeforeEach
    internal fun setUp() {
        // The system property below can be set to "on" to enable extensive trace logging. Do not commit/push such
        // change to any branch as it pollutes the build output too much!
        System.setProperty("KTLINT_UNIT_TEST_TRACE", "off")
    }

    private val indentationRuleAssertThat = assertThatRule { IndentationRule() }

    @Nested
    inner class `Given a basic construct` {
        @Nested
        inner class `Given a variable declaration` {
            @Test
            fun `Given a variable declaration with primitive value on same line`() {
                val code =
                    """
                    val foo = 42
                    """.trimIndent()
                indentationRuleAssertThat(code).hasNoLintViolations()
            }

            @Test
            fun `Given a variable declaration with primitive value on next line`() {
                val code =
                    """
                    val foo =
                        42
                    """.trimIndent()
                indentationRuleAssertThat(code).hasNoLintViolations()
            }

            @Test
            fun `Given a variable declaration with string value on same line`() {
                val code =
                    """
                    val foo = "foo"
                    """.trimIndent()
                indentationRuleAssertThat(code).hasNoLintViolations()
            }

            @Test
            fun `Given a variable declaration with string value on next line`() {
                val code =
                    """
                    val foo =
                        "foo"
                    """.trimIndent()
                indentationRuleAssertThat(code).hasNoLintViolations()
            }

            @Test
            fun `Given a variable declaration with raw string literal value on same line`() {
                val code =
                    """
                    val foo = ${MULTILINE_STRING_QUOTE}foo$MULTILINE_STRING_QUOTE.trimIndent()
                    """.trimIndent()
                indentationRuleAssertThat(code).hasNoLintViolations()
            }

            @Test
            fun `Given a variable declaration with raw string literal value on next line`() {
                val code =
                    """
                    val foo =
                        ${MULTILINE_STRING_QUOTE}foo$MULTILINE_STRING_QUOTE.trimIndent()
                    """.trimIndent()
                indentationRuleAssertThat(code).hasNoLintViolations()
            }

            @Test
            fun `Given a variable declaration with raw string literal value starting on same line but ending on separate line`() {
                val code =
                    """
                    val foo = $MULTILINE_STRING_QUOTE
                        foo
                    $MULTILINE_STRING_QUOTE.trimIndent()
                    """.trimIndent()
                indentationRuleAssertThat(code).hasNoLintViolations()
            }

            @Test
            fun `Given a variable declaration with value from function call with parameter on separate lines`() {
                val code =
                    """
                    val foo =
                        bar(
                            3,
                            3
                        )
                    """.trimIndent()
                indentationRuleAssertThat(code).hasNoLintViolations()
            }

            @Test
            fun `Given a variable declaration with value from chained method call`() {
                val code =
                    """
                    val foo =
                        "fooBar"
                            .substring(
                                3,
                                3
                            )
                    """.trimIndent()
                indentationRuleAssertThat(code).hasNoLintViolations()
            }

            @Test
            fun `Given a variable declaration with lambda where value is on same line as arrow`() {
                val code =
                    """
                    val foo = { bar: String -> "bar" }
                    """.trimIndent()
                indentationRuleAssertThat(code).hasNoLintViolations()
            }

            @Test
            fun `Given a variable declaration with lambda where value is not on same line as arrow`() {
                val code =
                    """
                    val foo = { bar: String ->
                        "bar"
                    }
                    """.trimIndent()
                indentationRuleAssertThat(code).hasNoLintViolations()
            }
        }

        @Nested
        inner class `Given a function declarations` {
            @Test
            fun `Given a function not returning a value`() {
                val code =
                    """
                    fun foo() {
                        // do something
                    }
                    """.trimIndent()
                indentationRuleAssertThat(code).hasNoLintViolations()
            }

            @Test
            fun `Given a function with body returning a value`() {
                val code =
                    """
                    fun foo(): Int {
                        return 42
                    }
                    """.trimIndent()
                indentationRuleAssertThat(code).hasNoLintViolations()
            }

            @Test
            fun `Given a function with body expression on same line`() {
                val code =
                    """
                    fun foo() = 42
                    """.trimIndent()
                indentationRuleAssertThat(code).hasNoLintViolations()
            }

            @Test
            fun `Given a function with body expression on next line`() {
                val code =
                    """
                    fun foo() =
                        42
                    """.trimIndent()
                indentationRuleAssertThat(code).hasNoLintViolations()
            }
        }

        @Test
        fun `Given a class`() {
            val code =
                """
                class Foo {
                    val foo = 42
                }
                """.trimIndent()
            indentationRuleAssertThat(code).hasNoLintViolations()
        }

        @Nested
        inner class `Given a function call` {
            @Test
            fun `Given a function call without parameters`() {
                val code =
                    """
                    val foo = foo()
                    """.trimIndent()
                indentationRuleAssertThat(code).hasNoLintViolations()
            }

            @Test
            fun `Given a function call with parameters on same line`() {
                val code =
                    """
                    val foo1 = foo(1)
                    val foo2 = foo(1, 2)
                    """.trimIndent()
                indentationRuleAssertThat(code).hasNoLintViolations()
            }

            @Test
            fun `Given a function call with parameters on separate lines`() {
                val code =
                    """
                    val foo = foo(
                        1,
                        2
                    )
                    """.trimIndent()
                indentationRuleAssertThat(code).hasNoLintViolations()
            }

            @Test
            fun `Given a function call with lambda parameter on separate line`() {
                val code =
                    """
                    val foo = fooBar(
                        { bar: String ->
                            42
                        }
                    )
                    """.trimIndent()
                indentationRuleAssertThat(code).hasNoLintViolations()
            }

            @Test
            fun `333Given a function call with lambda parameter on separate line`() {
                val code =
                    """
                    val foo = fooBar(
                        {
                            42
                        }
                    )
                    """.trimIndent()
                indentationRuleAssertThat(code).hasNoLintViolations()
            }

            @Test
            fun `111Given a function call with lambda parameter on separate line`() {
                val code =
                    """
                    val foo = fooBar({ bar: String ->
                        bar(
                            3,
                            4
                        )
                    }, {
                        "42"
                    }, { bar: String ->
                        bar(
                            3,
                            4
                        )
                    })
                    """.trimIndent()
                indentationRuleAssertThat(code).hasNoLintViolations()
            }

            @Test
            fun `22222Given a function call with lambda parameter on separate line`() {
                val code =
                    """
                    val foo = fooBar(
                        { bar: String ->
                            bar(
                                3,
                                4
                            )
                        }
                    )
                    """.trimIndent()
                indentationRuleAssertThat(code).hasNoLintViolations()
            }
        }
    }

    @Nested
    inner class `Given a multiline value declaration` {
        val code =
            """
            class Foo {
            val foo1 =
            "foo"
            val foo2 =
            { bar: Int -> 2 * bar }
            }
            """.trimIndent()

        @Test
        fun `Given a multiline value declaration`() {
            val formattedCode =
                """
                class Foo {
                    val foo1 =
                        "foo"
                    val foo2 =
                        { bar: Int -> 2 * bar }
                }
                """.trimIndent()
            indentationRuleAssertThat(code)
                .hasLintViolations(
                    LintViolation(2, 1, "Unexpected indentation (0) (should be 4)"),
                    LintViolation(3, 1, "Unexpected indentation (0) (should be 8)"),
                    LintViolation(4, 1, "Unexpected indentation (0) (should be 4)"),
                    LintViolation(5, 1, "Unexpected indentation (0) (should be 8)"),
                ).isFormattedAs(formattedCode)
        }

        @Test
        fun `Given a multiline value declaration (tab indentation)`() {
            val formattedCode =
                """
                class Foo {
                ${TAB}val foo1 =
                ${TAB}${TAB}"foo"
                ${TAB}val foo2 =
                ${TAB}${TAB}{ bar: Int -> 2 * bar }
                }
                """.trimIndent()
            indentationRuleAssertThat(code)
                .withEditorConfigOverride(INDENT_STYLE_TAB)
                .hasLintViolations(
                    LintViolation(2, 1, "Unexpected indentation (0) (should be 1)"),
                    LintViolation(3, 1, "Unexpected indentation (0) (should be 2)"),
                    LintViolation(4, 1, "Unexpected indentation (0) (should be 1)"),
                    LintViolation(5, 1, "Unexpected indentation (0) (should be 2)"),
                ).isFormattedAs(formattedCode)
        }
    }

    @Nested
    inner class `Given an annotated function declaration` {
        val code =
            """
            class Foo {
            @Deprecated("Foo")
            fun foo() = "foo"
            }

            @Deprecated("Foo")
            fun foo() = "foo"
            """.trimIndent()

        @Test
        fun `Given an annotated function declaration`() {
            val formattedCode =
                """
                class Foo {
                    @Deprecated("Foo")
                    fun foo() = "foo"
                }

                @Deprecated("Foo")
                fun foo() = "foo"
                """.trimIndent()
            indentationRuleAssertThat(code)
                .hasLintViolations(
                    LintViolation(2, 1, "Unexpected indentation (0) (should be 4)"),
                    LintViolation(3, 1, "Unexpected indentation (0) (should be 4)"),
                ).isFormattedAs(formattedCode)
        }

        @Test
        fun `Given an annotated function declaration (tab indentation)`() {
            val formattedCode =
                """
                class Foo {
                ${TAB}@Deprecated("Foo")
                ${TAB}fun foo() = "foo"
                }

                @Deprecated("Foo")
                fun foo() = "foo"
                """.trimIndent()
            indentationRuleAssertThat(code)
                .withEditorConfigOverride(INDENT_STYLE_TAB)
                .hasLintViolations(
                    LintViolation(2, 1, "Unexpected indentation (0) (should be 1)"),
                    LintViolation(3, 1, "Unexpected indentation (0) (should be 1)"),
                ).isFormattedAs(formattedCode)
        }
    }

    @Nested
    inner class `Given a function declaration with parameters wrapped on separate lines` {
        val code =
            """
            class Foo {
            fun foo(
            foo1: Int,
            foo2: Int
            ) = foo1 + foo2
            }
            """.trimIndent()

        @Test
        fun `Given a function declaration with parameters wrapped to separate lines`() {
            val formattedCode =
                """
                class Foo {
                    fun foo(
                        foo1: Int,
                        foo2: Int
                    ) = foo1 + foo2
                }
                """.trimIndent()
            indentationRuleAssertThat(code)
                .hasLintViolations(
                    LintViolation(2, 1, "Unexpected indentation (0) (should be 4)"),
                    LintViolation(3, 1, "Unexpected indentation (0) (should be 8)"),
                    LintViolation(4, 1, "Unexpected indentation (0) (should be 8)"),
                    LintViolation(5, 1, "Unexpected indentation (0) (should be 4)"),
                ).isFormattedAs(formattedCode)
        }

        @Test
        fun `Given a function declaration with parameters wrapped to separate lines (tab indentation)`() {
            val formattedCode =
                """
                class Foo {
                ${TAB}fun foo(
                ${TAB}${TAB}foo1: Int,
                ${TAB}${TAB}foo2: Int
                ${TAB}) = foo1 + foo2
                }
                """.trimIndent()
            indentationRuleAssertThat(code)
                .withEditorConfigOverride(INDENT_STYLE_TAB)
                .hasLintViolations(
                    LintViolation(2, 1, "Unexpected indentation (0) (should be 1)"),
                    LintViolation(3, 1, "Unexpected indentation (0) (should be 2)"),
                    LintViolation(4, 1, "Unexpected indentation (0) (should be 2)"),
                    LintViolation(5, 1, "Unexpected indentation (0) (should be 1)"),
                ).isFormattedAs(formattedCode)
        }
    }

    @Nested
    inner class `Given a when statement` {
        val code =
            """
            fun foo(bar: Any) =
            when (bar) {
            is Number -> 0
            else -> 1
            }
            """.trimIndent()

        @Test
        fun `Given a when-statement`() {
            val formattedCode =
                """
                fun foo(bar: Any) =
                    when (bar) {
                        is Number -> 0
                        else -> 1
                    }
                """.trimIndent()
            indentationRuleAssertThat(code)
                .hasLintViolations(
                    LintViolation(2, 1, "Unexpected indentation (0) (should be 4)"),
                    LintViolation(3, 1, "Unexpected indentation (0) (should be 8)"),
                    LintViolation(4, 1, "Unexpected indentation (0) (should be 8)"),
                    LintViolation(5, 1, "Unexpected indentation (0) (should be 4)"),
                ).isFormattedAs(formattedCode)
        }

        @Test
        fun `Given a when-statement (tab indentation)`() {
            val formattedCode =
                """
                fun foo(bar: Any) =
                ${TAB}when (bar) {
                ${TAB}${TAB}is Number -> 0
                ${TAB}${TAB}else -> 1
                ${TAB}}
                """.trimIndent()
            indentationRuleAssertThat(code)
                .withEditorConfigOverride(INDENT_STYLE_TAB)
                .hasLintViolations(
                    LintViolation(2, 1, "Unexpected indentation (0) (should be 1)"),
                    LintViolation(3, 1, "Unexpected indentation (0) (should be 2)"),
                    LintViolation(4, 1, "Unexpected indentation (0) (should be 2)"),
                    LintViolation(5, 1, "Unexpected indentation (0) (should be 1)"),
                ).isFormattedAs(formattedCode)
        }
    }

    @Nested
    inner class `Given a if statement` {
        val code =
            """
            fun foo(i1: Int, i2: Int) =
            if (i1 > 0 &&
            i2 < 0
            ) {
            1
            } else {
            2
            }
            """.trimIndent()

        @Test
        fun `Given an if-statement`() {
            val formattedCode =
                """
                fun foo(i1: Int, i2: Int) =
                    if (i1 > 0 &&
                        i2 < 0
                    ) {
                        1
                    } else {
                        2
                    }
                """.trimIndent()
            indentationRuleAssertThat(code)
                .hasLintViolations(
                    LintViolation(2, 1, "Unexpected indentation (0) (should be 4)"),
                    LintViolation(3, 1, "Unexpected indentation (0) (should be 8)"),
                    LintViolation(4, 1, "Unexpected indentation (0) (should be 4)"),
                    LintViolation(5, 1, "Unexpected indentation (0) (should be 8)"),
                    LintViolation(6, 1, "Unexpected indentation (0) (should be 4)"),
                    LintViolation(7, 1, "Unexpected indentation (0) (should be 8)"),
                    LintViolation(8, 1, "Unexpected indentation (0) (should be 4)"),
                ).isFormattedAs(formattedCode)
        }

        @Test
        fun `Given an if-statement (tab indentation)`() {
            val formattedCode =
                """
                fun foo(i1: Int, i2: Int) =
                ${TAB}if (i1 > 0 &&
                ${TAB}${TAB}i2 < 0
                ${TAB}) {
                ${TAB}${TAB}1
                ${TAB}} else {
                ${TAB}${TAB}2
                ${TAB}}
                """.trimIndent()
            indentationRuleAssertThat(code)
                .withEditorConfigOverride(INDENT_STYLE_TAB)
                .hasLintViolations(
                    LintViolation(2, 1, "Unexpected indentation (0) (should be 1)"),
                    LintViolation(3, 1, "Unexpected indentation (0) (should be 2)"),
                    LintViolation(4, 1, "Unexpected indentation (0) (should be 1)"),
                    LintViolation(5, 1, "Unexpected indentation (0) (should be 2)"),
                    LintViolation(6, 1, "Unexpected indentation (0) (should be 1)"),
                    LintViolation(7, 1, "Unexpected indentation (0) (should be 2)"),
                    LintViolation(8, 1, "Unexpected indentation (0) (should be 1)"),
                ).isFormattedAs(formattedCode)
        }
    }

    @Nested
    inner class `Given an EOL comment start at position 0 of the line` {
        val code =
            """
            fun foo() =
            // To be implemented
            "foo"
            """.trimIndent()

        @Test
        fun `Given an EOL comment starting at position 0 is not fixed`() {
            val formattedCode =
                """
                fun foo() =
                // To be implemented
                    "foo"
                """.trimIndent()
            indentationRuleAssertThat(code)
                .hasLintViolation(3, 1, "Unexpected indentation (0) (should be 4)")
                .isFormattedAs(formattedCode)
        }

        @Test
        fun `Given an EOL comment starting at position 0 is not fixed (tab indentation)`() {
            val formattedCode =
                """
                fun foo() =
                // To be implemented
                ${TAB}"foo"
                """.trimIndent()
            indentationRuleAssertThat(code)
                .withEditorConfigOverride(INDENT_STYLE_TAB)
                .hasLintViolation(3, 1, "Unexpected indentation (0) (should be 1)")
                .isFormattedAs(formattedCode)
        }
    }

    @Nested
    inner class `Given a function call with parameters wrapped on separate lines` {
        val code =
            """
            fun bar() =
            foo(
            1,
            2
            )
            """.trimIndent()

        @Test
        fun `Given a function call with parameters wrapped to separate lines`() {
            val formattedCode =
                """
                fun bar() =
                    foo(
                        1,
                        2
                    )
                """.trimIndent()
            indentationRuleAssertThat(code)
                .hasLintViolations(
                    LintViolation(2, 1, "Unexpected indentation (0) (should be 4)"),
                    LintViolation(3, 1, "Unexpected indentation (0) (should be 8)"),
                    LintViolation(4, 1, "Unexpected indentation (0) (should be 8)"),
                    LintViolation(5, 1, "Unexpected indentation (0) (should be 4)"),
                ).isFormattedAs(formattedCode)
        }

        @Test
        fun `Given a function call with parameters wrapped to separate lines (tab indentation)`() {
            val formattedCode =
                """
                fun bar() =
                ${TAB}foo(
                ${TAB}${TAB}1,
                ${TAB}${TAB}2
                ${TAB})
                """.trimIndent()
            indentationRuleAssertThat(code)
                .withEditorConfigOverride(INDENT_STYLE_TAB)
                .hasLintViolations(
                    LintViolation(2, 1, "Unexpected indentation (0) (should be 1)"),
                    LintViolation(3, 1, "Unexpected indentation (0) (should be 2)"),
                    LintViolation(4, 1, "Unexpected indentation (0) (should be 2)"),
                    LintViolation(5, 1, "Unexpected indentation (0) (should be 1)"),
                ).isFormattedAs(formattedCode)
        }
    }

    @Nested
    inner class `Given a try-catch-finally statement` {
        val code =
            """
            fun foo() =
            try {
            "foo1"
            } catch (e: Exception) {
            "foo2"
            } finally {
            Unit // do something
            }
            """.trimIndent()

        @Test
        fun `Given an try-catch-finally-statement`() {
            val formattedCode =
                """
                fun foo() =
                    try {
                        "foo1"
                    } catch (e: Exception) {
                        "foo2"
                    } finally {
                        Unit // do something
                    }
                """.trimIndent()
            indentationRuleAssertThat(code)
                .hasLintViolations(
                    LintViolation(2, 1, "Unexpected indentation (0) (should be 4)"),
                    LintViolation(3, 1, "Unexpected indentation (0) (should be 8)"),
                    LintViolation(4, 1, "Unexpected indentation (0) (should be 4)"),
                    LintViolation(5, 1, "Unexpected indentation (0) (should be 8)"),
                    LintViolation(6, 1, "Unexpected indentation (0) (should be 4)"),
                    LintViolation(7, 1, "Unexpected indentation (0) (should be 8)"),
                    LintViolation(8, 1, "Unexpected indentation (0) (should be 4)"),
                ).isFormattedAs(formattedCode)
        }

        @Test
        fun `Given an try-catch-finally-statement (tab indentation)`() {
            val formattedCode =
                """
                fun foo() =
                ${TAB}try {
                ${TAB}${TAB}"foo1"
                ${TAB}} catch (e: Exception) {
                ${TAB}${TAB}"foo2"
                ${TAB}} finally {
                ${TAB}${TAB}Unit // do something
                ${TAB}}
                """.trimIndent()
            indentationRuleAssertThat(code)
                .withEditorConfigOverride(INDENT_STYLE_TAB)
                .hasLintViolations(
                    LintViolation(2, 1, "Unexpected indentation (0) (should be 1)"),
                    LintViolation(3, 1, "Unexpected indentation (0) (should be 2)"),
                    LintViolation(4, 1, "Unexpected indentation (0) (should be 1)"),
                    LintViolation(5, 1, "Unexpected indentation (0) (should be 2)"),
                    LintViolation(6, 1, "Unexpected indentation (0) (should be 1)"),
                    LintViolation(7, 1, "Unexpected indentation (0) (should be 2)"),
                    LintViolation(8, 1, "Unexpected indentation (0) (should be 1)"),
                ).isFormattedAs(formattedCode)
        }
    }

    @Nested
    inner class `Given a chained method` {
        val code: String =
            """
            fun foo1(bar: String) =
            bar.uppercase(Locale.getDefault())
            .trim()
            .length.also {
            println("done")
            }
            """.trimIndent()

        @Test
        fun `Given some chained method calls`() {
            val formattedCode =
                """
                fun foo1(bar: String) =
                    bar.uppercase(Locale.getDefault())
                        .trim()
                        .length.also {
                            println("done")
                        }
                """.trimIndent()
            indentationRuleAssertThat(code)
                .hasLintViolations(
                    LintViolation(2, 1, "Unexpected indentation (0) (should be 4)"),
                    LintViolation(3, 1, "Unexpected indentation (0) (should be 8)"),
                    LintViolation(4, 1, "Unexpected indentation (0) (should be 8)"),
                    LintViolation(5, 1, "Unexpected indentation (0) (should be 12)"),
                    LintViolation(6, 1, "Unexpected indentation (0) (should be 8)"),
                ).isFormattedAs(formattedCode)
        }

        @Test
        fun `Given some chained method calls (tab indentation)`() {
            val formattedCode =
                """
                fun foo1(bar: String) =
                ${TAB}bar.uppercase(Locale.getDefault())
                ${TAB}${TAB}.trim()
                ${TAB}${TAB}.length.also {
                ${TAB}${TAB}${TAB}println("done")
                ${TAB}${TAB}}
                """.trimIndent()
            indentationRuleAssertThat(code)
                .withEditorConfigOverride(INDENT_STYLE_TAB)
                .hasLintViolations(
                    LintViolation(2, 1, "Unexpected indentation (0) (should be 1)"),
                    LintViolation(3, 1, "Unexpected indentation (0) (should be 2)"),
                    LintViolation(4, 1, "Unexpected indentation (0) (should be 2)"),
                    LintViolation(5, 1, "Unexpected indentation (0) (should be 3)"),
                    LintViolation(6, 1, "Unexpected indentation (0) (should be 2)"),
                ).isFormattedAs(formattedCode)
        }
    }

    @Nested
    inner class `Given a function with an annotated parameter` {
        val code =
            """
            fun foo(
            @Named("foo1") bar1: Int,
            bar2: String
            ) = "foobar"
            """.trimIndent()

        @Test
        fun `Given an annotated function parameter`() {
            val formattedCode =
                """
                fun foo(
                    @Named("foo1") bar1: Int,
                    bar2: String
                ) = "foobar"
                """.trimIndent()
            indentationRuleAssertThat(code)
                .hasLintViolations(
                    LintViolation(2, 1, "Unexpected indentation (0) (should be 4)"),
                    LintViolation(3, 1, "Unexpected indentation (0) (should be 4)"),
                ).isFormattedAs(formattedCode)
        }

        @Test
        fun `Given an annotated function parameter (tab indentation)`() {
            val formattedCode =
                """
                fun foo(
                ${TAB}@Named("foo1") bar1: Int,
                ${TAB}bar2: String
                ) = "foobar"
                """.trimIndent()
            indentationRuleAssertThat(code)
                .withEditorConfigOverride(INDENT_STYLE_TAB)
                .hasLintViolations(
                    LintViolation(2, 1, "Unexpected indentation (0) (should be 1)"),
                    LintViolation(3, 1, "Unexpected indentation (0) (should be 1)"),
                ).isFormattedAs(formattedCode)
        }
    }

    @Nested
    inner class `Given a claas with an annotated property` {
        val code =
            """
            class Foo {
            @Deprecated("Foo") val foo1 = "foo"

            @Deprecated("Foo")
            val foo2 = "foo"
            }

            @Deprecated("Foo")
            val foo3 = "foo"
            """.trimIndent()

        @Test
        fun `Given an annotated value declaration`() {
            val formattedCode =
                """
                class Foo {
                    @Deprecated("Foo") val foo1 = "foo"

                    @Deprecated("Foo")
                    val foo2 = "foo"
                }

                @Deprecated("Foo")
                val foo3 = "foo"
                """.trimIndent()
            indentationRuleAssertThat(code)
                .hasLintViolations(
                    LintViolation(2, 1, "Unexpected indentation (0) (should be 4)"),
                    LintViolation(4, 1, "Unexpected indentation (0) (should be 4)"),
                    LintViolation(5, 1, "Unexpected indentation (0) (should be 4)"),
                ).isFormattedAs(formattedCode)
        }

        @Test
        fun `Given an annotated value declaration (tab indentation)`() {
            val formattedCode =
                """
                class Foo {
                ${TAB}@Deprecated("Foo") val foo1 = "foo"

                ${TAB}@Deprecated("Foo")
                ${TAB}val foo2 = "foo"
                }

                @Deprecated("Foo")
                val foo3 = "foo"
                """.trimIndent()
            indentationRuleAssertThat(code)
                .withEditorConfigOverride(INDENT_STYLE_TAB)
                .hasLintViolations(
                    LintViolation(2, 1, "Unexpected indentation (0) (should be 1)"),
                    LintViolation(4, 1, "Unexpected indentation (0) (should be 1)"),
                    LintViolation(5, 1, "Unexpected indentation (0) (should be 1)"),
                ).isFormattedAs(formattedCode)
        }
    }

    @Nested
    inner class `Given a function declaration with a nexted elvis operator` {
        val code =
            """
            fun foo1(bar: String?) =
            bar?.map { it } ?: 0
            ?: -1
            fun foo2(bar: String?) =
            bar?.map { it }
            ?: 0
            ?: -1
            """.trimIndent()

        @Test
        fun `Given some function declaration having nested elvis operators`() {
            val formattedCode =
                """
                fun foo1(bar: String?) =
                    bar?.map { it } ?: 0
                        ?: -1
                fun foo2(bar: String?) =
                    bar?.map { it }
                        ?: 0
                        ?: -1
                """.trimIndent()
            indentationRuleAssertThat(code)
                .hasLintViolations(
                    LintViolation(2, 1, "Unexpected indentation (0) (should be 4)"),
                    LintViolation(3, 1, "Unexpected indentation (0) (should be 8)"),
                    LintViolation(5, 1, "Unexpected indentation (0) (should be 4)"),
                    LintViolation(6, 1, "Unexpected indentation (0) (should be 8)"),
                    LintViolation(7, 1, "Unexpected indentation (0) (should be 8)"),
                ).isFormattedAs(formattedCode)
        }

        @Test
        fun `Given some function declaration having nested elvis operators (tab indentation)`() {
            val formattedCode =
                """
                fun foo1(bar: String?) =
                ${TAB}bar?.map { it } ?: 0
                ${TAB}${TAB}?: -1
                fun foo2(bar: String?) =
                ${TAB}bar?.map { it }
                ${TAB}${TAB}?: 0
                ${TAB}${TAB}?: -1
                """.trimIndent()
            indentationRuleAssertThat(code)
                .withEditorConfigOverride(INDENT_STYLE_TAB)
                .hasLintViolations(
                    LintViolation(2, 1, "Unexpected indentation (0) (should be 1)"),
                    LintViolation(3, 1, "Unexpected indentation (0) (should be 2)"),
                    LintViolation(5, 1, "Unexpected indentation (0) (should be 1)"),
                    LintViolation(6, 1, "Unexpected indentation (0) (should be 2)"),
                    LintViolation(7, 1, "Unexpected indentation (0) (should be 2)"),
                ).isFormattedAs(formattedCode)
        }
    }

    @Nested
    inner class `Given a class declaration having a supertype which uses generics` {
        val code =
            """
            open class Foo<K, V>
            class Bar
            class FooBar :
            Foo<
            String,
            Int
            >,
            Bar() {
            }
            """.trimIndent()

        @Test
        fun `Given ktlint-official code style and a class declaration implementing a super type with generics`() {
            val formattedCode =
                """
                open class Foo<K, V>
                class Bar
                class FooBar :
                    Foo<
                        String,
                        Int
                    >,
                    Bar() {
                }
                """.trimIndent()
            indentationRuleAssertThat(code)
                .withEditorConfigOverride(CODE_STYLE_PROPERTY to ktlint_official)
                .hasLintViolations(
                    LintViolation(4, 1, "Unexpected indentation (0) (should be 4)"),
                    LintViolation(5, 1, "Unexpected indentation (0) (should be 8)"),
                    LintViolation(6, 1, "Unexpected indentation (0) (should be 8)"),
                    LintViolation(7, 1, "Unexpected indentation (0) (should be 4)"),
                    LintViolation(8, 1, "Unexpected indentation (0) (should be 4)"),
                ).isFormattedAs(formattedCode)
        }

        @Test
        fun `Given ktlint-official code style and a class declaration implementing a super type with generics (tab indentation)`() {
            val formattedCode =
                """
                open class Foo<K, V>
                class Bar
                class FooBar :
                ${TAB}Foo<
                ${TAB}${TAB}String,
                ${TAB}${TAB}Int
                ${TAB}>,
                ${TAB}Bar() {
                }
                """.trimIndent()
            indentationRuleAssertThat(code)
                .withEditorConfigOverride(INDENT_STYLE_TAB)
                .withEditorConfigOverride(CODE_STYLE_PROPERTY to ktlint_official)
                .hasLintViolations(
                    LintViolation(4, 1, "Unexpected indentation (0) (should be 1)"),
                    LintViolation(5, 1, "Unexpected indentation (0) (should be 2)"),
                    LintViolation(6, 1, "Unexpected indentation (0) (should be 2)"),
                    LintViolation(7, 1, "Unexpected indentation (0) (should be 1)"),
                    LintViolation(8, 1, "Unexpected indentation (0) (should be 1)"),
                ).isFormattedAs(formattedCode)
        }

        @ParameterizedTest(name = "CodeStyle: {0}")
        @EnumSource(
            value = CodeStyleValue::class,
            mode = EnumSource.Mode.EXCLUDE,
            names = ["ktlint_official"],
        )
        fun `Given non-ktlint-official code style and a class declaration implementing a super type with generics`(
            codeStyleValue: CodeStyleValue,
        ) {
            val formattedCode =
                """
                open class Foo<K, V>
                class Bar
                class FooBar :
                    Foo<
                        String,
                        Int
                        >,
                    Bar() {
                }
                """.trimIndent()
            indentationRuleAssertThat(code)
                .withEditorConfigOverride(CODE_STYLE_PROPERTY to codeStyleValue)
                .hasLintViolations(
                    LintViolation(4, 1, "Unexpected indentation (0) (should be 4)"),
                    LintViolation(5, 1, "Unexpected indentation (0) (should be 8)"),
                    LintViolation(6, 1, "Unexpected indentation (0) (should be 8)"),
                    LintViolation(7, 1, "Unexpected indentation (0) (should be 8)"),
                    LintViolation(8, 1, "Unexpected indentation (0) (should be 4)"),
                ).isFormattedAs(formattedCode)
        }

        @ParameterizedTest(name = "CodeStyle: {0}")
        @EnumSource(
            value = CodeStyleValue::class,
            mode = EnumSource.Mode.EXCLUDE,
            names = ["ktlint_official"],
        )
        fun `Given non-ktlint-official code style and a class declaration implementing a super type with generics (tab indentation)`(
            codeStyleValue: CodeStyleValue,
        ) {
            val formattedCode =
                """
                open class Foo<K, V>
                class Bar
                class FooBar :
                ${TAB}Foo<
                ${TAB}${TAB}String,
                ${TAB}${TAB}Int
                ${TAB}${TAB}>,
                ${TAB}Bar() {
                }
                """.trimIndent()
            indentationRuleAssertThat(code)
                .withEditorConfigOverride(INDENT_STYLE_TAB)
                .withEditorConfigOverride(CODE_STYLE_PROPERTY to codeStyleValue)
                .hasLintViolations(
                    LintViolation(4, 1, "Unexpected indentation (0) (should be 1)"),
                    LintViolation(5, 1, "Unexpected indentation (0) (should be 2)"),
                    LintViolation(6, 1, "Unexpected indentation (0) (should be 2)"),
                    LintViolation(7, 1, "Unexpected indentation (0) (should be 2)"),
                    LintViolation(8, 1, "Unexpected indentation (0) (should be 1)"),
                ).isFormattedAs(formattedCode)
        }
    }

    @Nested
    inner class `Given a class declaration having multiple supertypes` {
        val code =
            """
            abstract class Foo :
            Comparable<Any>,
            Appendable {
            fun foo() = "foo"
            }
            """.trimIndent()

        @Test
        fun `Given a class declaration implementing multiple super types`() {
            val formattedCode =
                """
                abstract class Foo :
                    Comparable<Any>,
                    Appendable {
                    fun foo() = "foo"
                }
                """.trimIndent()
            indentationRuleAssertThat(code)
                .hasLintViolations(
                    LintViolation(2, 1, "Unexpected indentation (0) (should be 4)"),
                    LintViolation(3, 1, "Unexpected indentation (0) (should be 4)"),
                    LintViolation(4, 1, "Unexpected indentation (0) (should be 4)"),
                ).isFormattedAs(formattedCode)
        }

        @Test
        fun `Given a class declaration implementing multiple super types (tab indentation)`() {
            val formattedCode =
                """
                abstract class Foo :
                ${TAB}Comparable<Any>,
                ${TAB}Appendable {
                ${TAB}fun foo() = "foo"
                }
                """.trimIndent()
            indentationRuleAssertThat(code)
                .withEditorConfigOverride(INDENT_STYLE_TAB)
                .hasLintViolations(
                    LintViolation(2, 1, "Unexpected indentation (0) (should be 1)"),
                    LintViolation(3, 1, "Unexpected indentation (0) (should be 1)"),
                    LintViolation(4, 1, "Unexpected indentation (0) (should be 1)"),
                ).isFormattedAs(formattedCode)
        }
    }

    @Nested
    inner class `Given a class which is annotated` {
        val code =
            """
            @Deprecated("Foo")
            class Foo
            """.trimIndent()

        @Test
        fun `Given an annotated class declaration`() {
            indentationRuleAssertThat(code).hasNoLintViolations()
        }

        @Test
        fun `Given an annotated class declaration (tab indentation)`() {
            indentationRuleAssertThat(code)
                .withEditorConfigOverride(INDENT_STYLE_TAB)
                .hasNoLintViolations()
        }
    }

    @Nested
    inner class `Given an enumeration class` {
        val code =
            """
            enum class FooBar {
            FOO,
            BAR
            }
            """.trimIndent()

        @Test
        fun `Given an enumeration class`() {
            val formattedCode =
                """
                enum class FooBar {
                    FOO,
                    BAR
                }
                """.trimIndent()
            indentationRuleAssertThat(code)
                .hasLintViolations(
                    LintViolation(2, 1, "Unexpected indentation (0) (should be 4)"),
                    LintViolation(3, 1, "Unexpected indentation (0) (should be 4)"),
                ).isFormattedAs(formattedCode)
        }

        @Test
        fun `Given an enumeration class (tab indentation)`() {
            val formattedCode =
                """
                enum class FooBar {
                ${TAB}FOO,
                ${TAB}BAR
                }
                """.trimIndent()
            indentationRuleAssertThat(code)
                .withEditorConfigOverride(INDENT_STYLE_TAB)
                .hasLintViolations(
                    LintViolation(2, 1, "Unexpected indentation (0) (should be 1)"),
                    LintViolation(3, 1, "Unexpected indentation (0) (should be 1)"),
                ).isFormattedAs(formattedCode)
        }
    }

    @Test
    fun `Given that the indent size is set to 2 spaces and the code is properly indented the do not return lint errors`() {
        val code =
            """
            fun main() {
               val v = ""
                println(v)
            }
            fun main() {
              val v = ""
              println(v)
            }
            class A {
              var x: String
                get() = ""
                set(v: String) { x = v }
            }
            """.trimIndent()
        val formattedCode =
            """
            fun main() {
              val v = ""
              println(v)
            }
            fun main() {
              val v = ""
              println(v)
            }
            class A {
              var x: String
                get() = ""
                set(v: String) { x = v }
            }
            """.trimIndent()
        indentationRuleAssertThat(code)
            .withEditorConfigOverride(INDENT_SIZE_PROPERTY to 2)
            .hasLintViolations(
                LintViolation(2, 1, "Unexpected indentation (3) (should be 2)"),
                LintViolation(3, 1, "Unexpected indentation (4) (should be 2)"),
            ).isFormattedAs(formattedCode)
    }

    @Test
    fun `Given that the indent style is set to tabs and the code is properly indented then do not return lint errors`() {
        val code =
            """
            fun main() {
            val v = ""
            ${TAB}${TAB}println(v)
            }
            fun main() {
            ${TAB}val v = ""
            ${TAB}println(v)
            }
            class A {
            ${TAB}var x: String
            ${TAB}${TAB}get() = ""
            ${TAB}${TAB}set(v: String) { x = v }
            }
            """.trimIndent()
        val formattedCode =
            """
            fun main() {
            ${TAB}val v = ""
            ${TAB}println(v)
            }
            fun main() {
            ${TAB}val v = ""
            ${TAB}println(v)
            }
            class A {
            ${TAB}var x: String
            ${TAB}${TAB}get() = ""
            ${TAB}${TAB}set(v: String) { x = v }
            }
            """.trimIndent()
        indentationRuleAssertThat(code)
            .withEditorConfigOverride(INDENT_STYLE_TAB)
            .hasLintViolations(
                LintViolation(2, 1, "Unexpected indentation (0) (should be 1)"),
                LintViolation(3, 1, "Unexpected indentation (2) (should be 1)"),
            ).isFormattedAs(formattedCode)
    }

    @Test
    fun `Given that the indent size property is not set`() {
        val code =
            """
            fun main() {
               val v = ""
                println(v)
            }
            """.trimIndent()
        indentationRuleAssertThat(code)
            .withEditorConfigOverride(INDENT_SIZE_PROPERTY to "unset")
            .hasNoLintViolations()
    }

    // https://kotlinlang.org/docs/reference/coding-conventions.html#method-call-formatting
    @Test
    fun `Given some function call`() {
        val code =
            """
            fun main() {
                foobar(
                      a,
                      b,
                      c
                      )
            }
            """.trimIndent()
        val formattedCode =
            """
            fun main() {
                foobar(
                    a,
                    b,
                    c
                )
            }
            """.trimIndent()
        indentationRuleAssertThat(code)
            .hasLintViolations(
                LintViolation(3, 1, "Unexpected indentation (10) (should be 8)"),
                LintViolation(4, 1, "Unexpected indentation (10) (should be 8)"),
                LintViolation(5, 1, "Unexpected indentation (10) (should be 8)"),
                LintViolation(6, 1, "Unexpected indentation (10) (should be 4)"),
            ).isFormattedAs(formattedCode)
    }

    @Test
    fun `Given some KDoc incorrectly indented`() {
        val code =
            """
            data class Foo(
              /**
               * Some bar
               */
              var bar: String
            )
            """.trimIndent()
        val formattedCode =
            """
            data class Foo(
                /**
                 * Some bar
                 */
                var bar: String
            )
            """.trimIndent()
        indentationRuleAssertThat(code)
            .hasLintViolations(
                LintViolation(2, 1, "Unexpected indentation (2) (should be 4)"),
                LintViolation(3, 1, "Unexpected indentation (3) (should be 5)"),
                LintViolation(4, 1, "Unexpected indentation (3) (should be 5)"),
                LintViolation(5, 1, "Unexpected indentation (2) (should be 4)"),
            ).isFormattedAs(formattedCode)
    }

    @Nested
    inner class `Given an EOL comment` {
        @Test
        fun `Given some correctly indented EOL comment`() {
            val code =
                """
                fun foo() =
                    // Some comment
                    "foo"
                """.trimIndent()
            indentationRuleAssertThat(code).hasNoLintViolations()
        }

        @Test
        fun `Given some incorrectly indented EOL comment`() {
            val code =
                """
                fun foo() =
                        // Some comment
                    "foo"
                """.trimIndent()
            val formattedCode =
                """
                fun foo() =
                    // Some comment
                    "foo"
                """.trimIndent()
            indentationRuleAssertThat(code)
                .hasLintViolation(2, 1, "Unexpected indentation (8) (should be 4)")
                .isFormattedAs(formattedCode)
        }

        @Test
        fun `Given EOL comment starting at position 0`() {
            val code =
                """
                fun foo() =
                // Some comment
                    "foo"
                """.trimIndent()
            indentationRuleAssertThat(code).hasNoLintViolations()
        }
    }

    @Nested
    inner class `Given a block comment` {
        @Test
        fun `Given some correctly indented block comment`() {
            val code =
                """
                fun foo() =
                    /*
                     * Some comment
                     */
                    "foo"
                """.trimIndent()
            indentationRuleAssertThat(code).hasNoLintViolations()
        }

        @Test
        fun `Given some incorrectly indented block comment (single line)`() {
            val code =
                """
                fun foo() =
                        /* Some comment */
                    "foo"
                """.trimIndent()
            val formattedCode =
                """
                fun foo() =
                    /* Some comment */
                    "foo"
                """.trimIndent()
            indentationRuleAssertThat(code)
                .hasLintViolation(2, 1, "Unexpected indentation (8) (should be 4)")
                .isFormattedAs(formattedCode)
        }

        @Disabled("To be fixed")
        @Test
        fun `Given some incorrectly indented block comment`() {
            val code =
                """
                fun foo() =
                        /*
                         * Some comment
                         */
                    "foo"
                """.trimIndent()
            val formattedCode =
                """
                fun foo() =
                    /*
                     * Some comment
                     */
                    "foo"
                """.trimIndent()
            indentationRuleAssertThat(code)
                .hasLintViolations(
                    LintViolation(2, 1, "Unexpected indentation (8) (should be 4)"),
                    LintViolation(3, 1, "Unexpected indentation (9) (should be 5)"),
                    LintViolation(4, 1, "Unexpected indentation (9) (should be 5)"),
                ).isFormattedAs(formattedCode)
        }

        @Test
        fun `Given block comment starting at position 0`() {
            val code =
                """
                fun foo() =
                /*
                 * Some comment
                 */
                    "foo"
                """.trimIndent()
            indentationRuleAssertThat(code).hasNoLintViolations()
        }
    }

    @Nested
    inner class `Given a KDoc` {
        @Test
        fun `Given a correctly indented KDoc with tab indentation`() {
            val code =
                """
                class Foo {
                ${TAB}/**
                ${TAB} * Some comment
                ${TAB} */
                ${TAB}fun foo() = "foo"
                }
                """.trimIndent()

            indentationRuleAssertThat(code)
                .withEditorConfigOverride(INDENT_STYLE_TAB)
                .hasNoLintViolations()
        }

        @Test
        fun `Given some correctly indented KDoc`() {
            val code =
                """
                fun foo() =
                    /**
                     * Some comment
                     */
                    "foo"
                """.trimIndent()
            indentationRuleAssertThat(code).hasNoLintViolations()
        }

        @Test
        fun `Given some incorrectly indented KDoc (single line)`() {
            val code =
                """
                fun foo() =
                        /** Some comment */
                    "foo"
                """.trimIndent()
            val formattedCode =
                """
                fun foo() =
                    /** Some comment */
                    "foo"
                """.trimIndent()
            indentationRuleAssertThat(code)
                .hasLintViolation(2, 1, "Unexpected indentation (8) (should be 4)")
                .isFormattedAs(formattedCode)
        }

        @Test
        fun `Given some incorrectly indented KDoc`() {
            val code =
                """
                fun foo() =
                        /**
                         * Some comment
                         */
                    "foo"
                """.trimIndent()
            val formattedCode =
                """
                fun foo() =
                    /**
                     * Some comment
                     */
                    "foo"
                """.trimIndent()
            indentationRuleAssertThat(code)
                .hasLintViolations(
                    LintViolation(2, 1, "Unexpected indentation (8) (should be 4)"),
                    LintViolation(3, 1, "Unexpected indentation (9) (should be 5)"),
                    LintViolation(4, 1, "Unexpected indentation (9) (should be 5)"),
                ).isFormattedAs(formattedCode)
        }

        @Disabled("To be fixed")
        @Test
        fun `Given KDoc starting at position 0`() {
            val code =
                """
                fun foo() =
                /**
                 * Some comment
                 */
                    "foo"
                """.trimIndent()
            indentationRuleAssertThat(code).hasNoLintViolations()
        }
    }

    @Nested
    inner class `Given a nested conditional` {
        @Test
        fun `Given a non-ktlint_official code style and a simple nested conditional`() {
            val code =
                """
                val foo =
                false ||
                (
                true ||
                false
                )
                """.trimIndent()
            val formattedCode =
                """
                val foo =
                    false ||
                        (
                            true ||
                                false
                            )
                """.trimIndent()
            indentationRuleAssertThat(code)
                .withEditorConfigOverride(CODE_STYLE_PROPERTY to intellij_idea)
                .hasLintViolations(
                    LintViolation(2, 1, "Unexpected indentation (0) (should be 4)"),
                    LintViolation(3, 1, "Unexpected indentation (0) (should be 8)"),
                    LintViolation(4, 1, "Unexpected indentation (0) (should be 12)"),
                    LintViolation(5, 1, "Unexpected indentation (0) (should be 16)"),
                    LintViolation(6, 1, "Unexpected indentation (0) (should be 12)"),
                ).isFormattedAs(formattedCode)
        }

        @Test
        fun `Given a non-ktlint_official code style and a method chain combined with nested conditionals`() {
            val code =
                """
                val foo =
                listOf<Any>()
                .toString()
                .isEmpty() &&
                false ||
                (
                true ||
                false ||
                listOf<Any>()
                .toString()
                .isEmpty()
                ) ||
                false
                """.trimIndent()
            val formattedCode =
                """
                val foo =
                    listOf<Any>()
                        .toString()
                        .isEmpty() &&
                        false ||
                        (
                            true ||
                                false ||
                                listOf<Any>()
                                    .toString()
                                    .isEmpty()
                            ) ||
                        false
                """.trimIndent()
            indentationRuleAssertThat(code)
                .withEditorConfigOverride(CODE_STYLE_PROPERTY to intellij_idea)
                .hasLintViolations(
                    LintViolation(2, 1, "Unexpected indentation (0) (should be 4)"),
                    LintViolation(3, 1, "Unexpected indentation (0) (should be 8)"),
                    LintViolation(4, 1, "Unexpected indentation (0) (should be 8)"),
                    LintViolation(5, 1, "Unexpected indentation (0) (should be 8)"),
                    LintViolation(6, 1, "Unexpected indentation (0) (should be 8)"),
                    LintViolation(7, 1, "Unexpected indentation (0) (should be 12)"),
                    LintViolation(8, 1, "Unexpected indentation (0) (should be 16)"),
                    LintViolation(9, 1, "Unexpected indentation (0) (should be 16)"),
                    LintViolation(10, 1, "Unexpected indentation (0) (should be 20)"),
                    LintViolation(11, 1, "Unexpected indentation (0) (should be 20)"),
                    LintViolation(12, 1, "Unexpected indentation (0) (should be 12)"),
                    LintViolation(13, 1, "Unexpected indentation (0) (should be 8)"),
                ).isFormattedAs(formattedCode)
        }

        @Test
        fun `Given a non-ktlint_official code style and a while statement with method chain combined with nested conditionals`() {
            val code =
                """
                fun foo() {
                while (
                listOf<Any>()
                .toString()
                .isEmpty() &&
                false ||
                (
                true ||
                false ||
                listOf<Any>()
                .toString()
                .isEmpty()
                ) ||
                false
                ) {
                println("hello")
                }
                }
                """.trimIndent()
            val formattedCode =
                """
                fun foo() {
                    while (
                        listOf<Any>()
                            .toString()
                            .isEmpty() &&
                        false ||
                        (
                            true ||
                                false ||
                                listOf<Any>()
                                    .toString()
                                    .isEmpty()
                            ) ||
                        false
                    ) {
                        println("hello")
                    }
                }
                """.trimIndent()
            indentationRuleAssertThat(code)
                .withEditorConfigOverride(CODE_STYLE_PROPERTY to intellij_idea)
                .hasLintViolations(
                    LintViolation(2, 1, "Unexpected indentation (0) (should be 4)"),
                    LintViolation(3, 1, "Unexpected indentation (0) (should be 8)"),
                    LintViolation(4, 1, "Unexpected indentation (0) (should be 12)"),
                    LintViolation(5, 1, "Unexpected indentation (0) (should be 12)"),
                    LintViolation(6, 1, "Unexpected indentation (0) (should be 8)"),
                    LintViolation(7, 1, "Unexpected indentation (0) (should be 8)"),
                    LintViolation(8, 1, "Unexpected indentation (0) (should be 12)"),
                    LintViolation(9, 1, "Unexpected indentation (0) (should be 16)"),
                    LintViolation(10, 1, "Unexpected indentation (0) (should be 16)"),
                    LintViolation(11, 1, "Unexpected indentation (0) (should be 20)"),
                    LintViolation(12, 1, "Unexpected indentation (0) (should be 20)"),
                    LintViolation(13, 1, "Unexpected indentation (0) (should be 12)"),
                    LintViolation(14, 1, "Unexpected indentation (0) (should be 8)"),
                    LintViolation(15, 1, "Unexpected indentation (0) (should be 4)"),
                    LintViolation(16, 1, "Unexpected indentation (0) (should be 8)"),
                    LintViolation(17, 1, "Unexpected indentation (0) (should be 4)"),
                ).isFormattedAs(formattedCode)
        }

        @Test
        fun `Given a non-ktlint_official code style and an if-statement with method chain combined with nested conditionals`() {
            val code =
                """
                fun foo() {
                if (
                listOf<Any>()
                .toString()
                .isEmpty() &&
                false ||
                (
                true ||
                false ||
                listOf<Any>()
                .toString()
                .isEmpty()
                ) ||
                false
                ) {
                println("hello")
                }
                }
                """.trimIndent()
            val formattedCode =
                """
                fun foo() {
                    if (
                        listOf<Any>()
                            .toString()
                            .isEmpty() &&
                        false ||
                        (
                            true ||
                                false ||
                                listOf<Any>()
                                    .toString()
                                    .isEmpty()
                            ) ||
                        false
                    ) {
                        println("hello")
                    }
                }
                """.trimIndent()
            indentationRuleAssertThat(code)
                .withEditorConfigOverride(CODE_STYLE_PROPERTY to intellij_idea)
                .hasLintViolations(
                    LintViolation(2, 1, "Unexpected indentation (0) (should be 4)"),
                    LintViolation(3, 1, "Unexpected indentation (0) (should be 8)"),
                    LintViolation(4, 1, "Unexpected indentation (0) (should be 12)"),
                    LintViolation(5, 1, "Unexpected indentation (0) (should be 12)"),
                    LintViolation(6, 1, "Unexpected indentation (0) (should be 8)"),
                    LintViolation(7, 1, "Unexpected indentation (0) (should be 8)"),
                    LintViolation(8, 1, "Unexpected indentation (0) (should be 12)"),
                    LintViolation(9, 1, "Unexpected indentation (0) (should be 16)"),
                    LintViolation(10, 1, "Unexpected indentation (0) (should be 16)"),
                    LintViolation(11, 1, "Unexpected indentation (0) (should be 20)"),
                    LintViolation(12, 1, "Unexpected indentation (0) (should be 20)"),
                    LintViolation(13, 1, "Unexpected indentation (0) (should be 12)"),
                    LintViolation(14, 1, "Unexpected indentation (0) (should be 8)"),
                    LintViolation(15, 1, "Unexpected indentation (0) (should be 4)"),
                    LintViolation(16, 1, "Unexpected indentation (0) (should be 8)"),
                    LintViolation(17, 1, "Unexpected indentation (0) (should be 4)"),
                ).isFormattedAs(formattedCode)
        }
    }

    @Test
    fun `Given some property accessors`() {
        val code =
            """
            class Foo {
            val b: Boolean
            get() = true

            var value: String = ""
            get() = ""
            set(v: String) { field = v }

            var valueMultiLine: String = ""
            get() {
            return ""
            }
                // comment
            set(v: String) {
            field = v
            }

            val isEmpty: Boolean
            get() = this.size == 0

            var counter = 0 // comment
            set(value) { // comment
            if (value >= 0) field = value
            }

            var setterVisibility: String = "abc"
            private set

            var setterWithAnnotation: Any? = null
            @Inject set

            var multilineInitialValue: String =
            "tooooooooooooo loooooooooooooooong"
            private set
            }
            """.trimIndent()
        val formattedCode =
            """
            class Foo {
                val b: Boolean
                    get() = true

                var value: String = ""
                    get() = ""
                    set(v: String) { field = v }

                var valueMultiLine: String = ""
                    get() {
                        return ""
                    }
                    // comment
                    set(v: String) {
                        field = v
                    }

                val isEmpty: Boolean
                    get() = this.size == 0

                var counter = 0 // comment
                    set(value) { // comment
                        if (value >= 0) field = value
                    }

                var setterVisibility: String = "abc"
                    private set

                var setterWithAnnotation: Any? = null
                    @Inject set

                var multilineInitialValue: String =
                    "tooooooooooooo loooooooooooooooong"
                    private set
            }
            """.trimIndent()
        indentationRuleAssertThat(code)
            .hasLintViolations(
                LintViolation(2, 1, "Unexpected indentation (0) (should be 4)"),
                LintViolation(3, 1, "Unexpected indentation (0) (should be 8)"),
                LintViolation(5, 1, "Unexpected indentation (0) (should be 4)"),
                LintViolation(6, 1, "Unexpected indentation (0) (should be 8)"),
                LintViolation(7, 1, "Unexpected indentation (0) (should be 8)"),
                LintViolation(9, 1, "Unexpected indentation (0) (should be 4)"),
                LintViolation(10, 1, "Unexpected indentation (0) (should be 8)"),
                LintViolation(11, 1, "Unexpected indentation (0) (should be 12)"),
                LintViolation(12, 1, "Unexpected indentation (0) (should be 8)"),
                LintViolation(13, 1, "Unexpected indentation (4) (should be 8)"),
                LintViolation(14, 1, "Unexpected indentation (0) (should be 8)"),
                LintViolation(15, 1, "Unexpected indentation (0) (should be 12)"),
                LintViolation(16, 1, "Unexpected indentation (0) (should be 8)"),
                LintViolation(18, 1, "Unexpected indentation (0) (should be 4)"),
                LintViolation(19, 1, "Unexpected indentation (0) (should be 8)"),
                LintViolation(21, 1, "Unexpected indentation (0) (should be 4)"),
                LintViolation(22, 1, "Unexpected indentation (0) (should be 8)"),
                LintViolation(23, 1, "Unexpected indentation (0) (should be 12)"),
                LintViolation(24, 1, "Unexpected indentation (0) (should be 8)"),
                LintViolation(26, 1, "Unexpected indentation (0) (should be 4)"),
                LintViolation(27, 1, "Unexpected indentation (0) (should be 8)"),
                LintViolation(29, 1, "Unexpected indentation (0) (should be 4)"),
                LintViolation(30, 1, "Unexpected indentation (0) (should be 8)"),
                LintViolation(32, 1, "Unexpected indentation (0) (should be 4)"),
                LintViolation(33, 1, "Unexpected indentation (0) (should be 8)"),
                LintViolation(34, 1, "Unexpected indentation (0) (should be 8)"),
            ).isFormattedAs(formattedCode)
    }

    @Test
    fun `Given a multiline string template containing an incorrectly indented expression `() {
        // Interpret "$." in code samples below as "$". It is used whenever the code which has to be inspected should
        // actually contain a string template. Using "$" instead of "$." would result in a String in which the string
        // templates would have been evaluated before the code would actually be processed by the rule.
        val code =
            """
            fun foo1() =
                "Sum of uneven numbers = $.{
                    listOf(1,2,3)
                        .filter { it % 2 == 0 }
                        .sum()
                }"
            fun foo2() = "Sum of uneven numbers = $.{
            listOf(1,2,3)
                .filter { it % 2 == 0 }
                .sum()
            }"
            """.trimIndent().replaceStringTemplatePlaceholder()
        val formattedCode =
            """
            fun foo1() =
                "Sum of uneven numbers = $.{
                    listOf(1,2,3)
                        .filter { it % 2 == 0 }
                        .sum()
                }"
            fun foo2() = "Sum of uneven numbers = $.{
                listOf(1,2,3)
                    .filter { it % 2 == 0 }
                    .sum()
            }"
            """.trimIndent().replaceStringTemplatePlaceholder()
        indentationRuleAssertThat(code)
            .hasLintViolations(
                LintViolation(8, 1, "Unexpected indentation (0) (should be 4)"),
                LintViolation(9, 1, "Unexpected indentation (4) (should be 8)"),
                LintViolation(10, 1, "Unexpected indentation (4) (should be 8)"),
            ).isFormattedAs(formattedCode)
    }

    @Nested
    inner class `Given that the first line in a file contains a non-code element` {
        @Test
        fun `Given a file with indented code on the first line then report unexpected indentation on first line`() {
            val code =
                """
                |${SPACE}${SPACE}// comment
                """.trimMargin() // Do not use trimIndent as that removes the spaces before the comment
            val formattedCode =
                """
                // comment
                """.trimIndent()
            indentationRuleAssertThat(code)
                .hasLintViolation(1, 1, "Unexpected indentation")
                .isFormattedAs(formattedCode)
        }

        @ParameterizedTest(name = "As Kotlin script: {0}")
        @ValueSource(booleans = [true, false])
        fun `Given a file with blanks only on the first line then do not report unexpected indentation for that first line`(
            kotlinScript: Boolean,
        ) {
            val code =
                """
                |${SPACE}${SPACE}
                |${SPACE}${SPACE}// comment
                """.trimMargin()
            val formattedCode =
                """
                |${SPACE}${SPACE}
                |// comment
                """.trimMargin()
            indentationRuleAssertThat(code)
                .asKotlinScript(kotlinScript)
                // Note that no LintError is created for the first line as it does not contain any code
                .hasLintViolation(2, 1, "Unexpected indentation (2) (should be 0)")
                .isFormattedAs(formattedCode)
        }
    }

    @Nested
    inner class `Given a binary expression` {
        @Test
        fun `Given a multiline binary expression of simple additions`() {
            val code =
                """
                val foo1 =
                    1 + 2 +
                        3 +
                4
                val foo2 =
                    "a" + "b" +
                        "c" +
                "d"
                """.trimIndent()
            val formattedCode =
                """
                val foo1 =
                    1 + 2 +
                        3 +
                        4
                val foo2 =
                    "a" + "b" +
                        "c" +
                        "d"
                """.trimIndent()
            indentationRuleAssertThat(code)
                .hasLintViolations(
                    LintViolation(4, 1, "Unexpected indentation (0) (should be 8)"),
                    LintViolation(8, 1, "Unexpected indentation (0) (should be 8)"),
                ).isFormattedAs(formattedCode)
        }

        @Test
        fun `Given a binary expression of strings created from chained methods and function calls`() {
            val code =
                """
                val foo =
                listOf("foo", "bar").joinToString {
                it.toUpperCaseAsciiOnly()
                } + bar(
                "foo"
                ) + bar(
                "foo"
                )
                """.trimIndent()
            val formattedCode =
                """
                val foo =
                    listOf("foo", "bar").joinToString {
                        it.toUpperCaseAsciiOnly()
                    } + bar(
                        "foo"
                    ) + bar(
                        "foo"
                    )
                """.trimIndent()
            indentationRuleAssertThat(code)
                .hasLintViolations(
                    LintViolation(2, 1, "Unexpected indentation (0) (should be 4)"),
                    LintViolation(3, 1, "Unexpected indentation (0) (should be 8)"),
                    LintViolation(4, 1, "Unexpected indentation (0) (should be 4)"),
                    LintViolation(5, 1, "Unexpected indentation (0) (should be 8)"),
                    LintViolation(6, 1, "Unexpected indentation (0) (should be 4)"),
                    LintViolation(7, 1, "Unexpected indentation (0) (should be 8)"),
                    LintViolation(8, 1, "Unexpected indentation (0) (should be 4)"),
                ).isFormattedAs(formattedCode)
        }

        @Test
        fun `Given a binary expression in a map entry`() {
            val code =
                """
                val foo1 =
                "bar" to
                "a" +
                "b" +
                "c"
                val foo2 =
                "bar" to
                "a"
                .plus("b")
                .plus("c")
                """.trimIndent()
            val formattedCode =
                """
                val foo1 =
                    "bar" to
                        "a" +
                        "b" +
                        "c"
                val foo2 =
                    "bar" to
                        "a"
                            .plus("b")
                            .plus("c")
                """.trimIndent()
            indentationRuleAssertThat(code)
                .hasLintViolations(
                    LintViolation(2, 1, "Unexpected indentation (0) (should be 4)"),
                    LintViolation(3, 1, "Unexpected indentation (0) (should be 8)"),
                    LintViolation(4, 1, "Unexpected indentation (0) (should be 8)"),
                    LintViolation(5, 1, "Unexpected indentation (0) (should be 8)"),
                    LintViolation(7, 1, "Unexpected indentation (0) (should be 4)"),
                    LintViolation(8, 1, "Unexpected indentation (0) (should be 8)"),
                    LintViolation(9, 1, "Unexpected indentation (0) (should be 12)"),
                    LintViolation(10, 1, "Unexpected indentation (0) (should be 12)"),
                ).isFormattedAs(formattedCode)
        }

        @Test
        fun `Given a binary expression in annotation parameter`() {
            val code =
                """
                annotation class Bar(val description: String, val names: Array<String>)

                @Bar(
                description = "foo",
                names = arrayOf(
                "foo" +
                "bar"
                )
                )
                private val foo: String = "foo"
                """.trimIndent()
            val formattedCode =
                """
                annotation class Bar(val description: String, val names: Array<String>)

                @Bar(
                    description = "foo",
                    names = arrayOf(
                        "foo" +
                            "bar"
                    )
                )
                private val foo: String = "foo"
                """.trimIndent()
            indentationRuleAssertThat(code)
                .hasLintViolations(
                    LintViolation(4, 1, "Unexpected indentation (0) (should be 4)"),
                    LintViolation(5, 1, "Unexpected indentation (0) (should be 4)"),
                    LintViolation(6, 1, "Unexpected indentation (0) (should be 8)"),
                    LintViolation(7, 1, "Unexpected indentation (0) (should be 12)"),
                    LintViolation(8, 1, "Unexpected indentation (0) (should be 4)"),
                ).isFormattedAs(formattedCode)
        }
    }

    @Test
    fun `Given a when-statement then the value might be placed at a separate line`() {
        val code =
            """
            val foo = when (1) {
                1 ->
            "1"
                else ->
            "2"
            }
            """.trimIndent()
        val formattedCode =
            """
            val foo = when (1) {
                1 ->
                    "1"
                else ->
                    "2"
            }
            """.trimIndent()
        indentationRuleAssertThat(code)
            .hasLintViolations(
                LintViolation(3, 1, "Unexpected indentation (0) (should be 8)"),
                LintViolation(5, 1, "Unexpected indentation (0) (should be 8)"),
            ).isFormattedAs(formattedCode)
    }

    @Test
    fun `Given a when-statement then the value might be placed in a block which opened on same line as arrow`() {
        val code =
            """
            val foo = when (1) {
                1 -> {
            "1"
            }
                else -> {
            "2"
            }
            }
            """.trimIndent()
        val formattedCode =
            """
            val foo = when (1) {
                1 -> {
                    "1"
                }
                else -> {
                    "2"
                }
            }
            """.trimIndent()
        indentationRuleAssertThat(code)
            .hasLintViolations(
                LintViolation(3, 1, "Unexpected indentation (0) (should be 8)"),
                LintViolation(4, 1, "Unexpected indentation (0) (should be 4)"),
                LintViolation(6, 1, "Unexpected indentation (0) (should be 8)"),
                LintViolation(7, 1, "Unexpected indentation (0) (should be 4)"),
            ).isFormattedAs(formattedCode)
    }

    @Test
    fun `Given a when-statement in a list of additions`() {
        val code =
            """
            val foo1 = 0 + 1 + when {
                else -> 2 + 3
            } + 4
            val foo2 = when {
                true -> 0 + 1 + when {
                    else -> 2 + 3
                } + 4
                else -> -1
            }
            """.trimIndent()
        indentationRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `Issue 916 - Given a multiline statement, not wrapped inside a block, as value in when-statement starting on the same line as the arrow`() {
        val code =
            """
            val foo = when (1) {
                1 -> if (true) {
                    2
                } else {
                    3
                }
                2 -> 1.let {
                    it + 1
                }.let {
                    it + 1
                }
                else -> 0
            }
            """.trimIndent()
        indentationRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `Given if-else-statement for which branches are not wrapped in block`() {
        val code =
            """
            fun foo() {
                if (true)
                    1
                else
                    2
            }
            """.trimIndent()
        indentationRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `Given a property assignment with if-else-expression for which branches are not wrapped in block`() {
        val code =
            """
            val foo = if (true)
                1
            else
                2
            """.trimIndent()
        indentationRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `Given non-ktlint_official code style and if-else-statement for which branches are wrapped between parenthesis`() {
        val code =
            """
            val foo = if (true) (
                1 + 2
                ) else ( // IDEA quirk
                3 + 4
                ) // IDEA quirk
            """.trimIndent()
        indentationRuleAssertThat(code)
            .withEditorConfigOverride(CODE_STYLE_PROPERTY to intellij_idea)
            .hasNoLintViolations()
    }

    @Test
    fun `Given a while statement for which the body is not wrapped in a block`() {
        val code =
            """
            fun foo() {
                while (true)
                    println()
            }
            """.trimIndent()
        indentationRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `Given some dot qualified expressions`() {
        val code =
            """
            val foo1 =
            nullableList
            .find { !it.empty() }
            ?.map { x + 2 }
            ?.filter { true }
            val foo2 =
            listOf(listOf(1, 2, 3))
            .map {
            it
            .map { it + 1 }
            .filter { it > 3 }
            }
            .reduce { acc, curr -> acc + curr }
            .toString()
            val foo3 = 1
            """.trimIndent()
        val formattedCode =
            """
            val foo1 =
                nullableList
                    .find { !it.empty() }
                    ?.map { x + 2 }
                    ?.filter { true }
            val foo2 =
                listOf(listOf(1, 2, 3))
                    .map {
                        it
                            .map { it + 1 }
                            .filter { it > 3 }
                    }
                    .reduce { acc, curr -> acc + curr }
                    .toString()
            val foo3 = 1
            """.trimIndent()
        indentationRuleAssertThat(code)
            .hasLintViolations(
                LintViolation(2, 1, "Unexpected indentation (0) (should be 4)"),
                LintViolation(3, 1, "Unexpected indentation (0) (should be 8)"),
                LintViolation(4, 1, "Unexpected indentation (0) (should be 8)"),
                LintViolation(5, 1, "Unexpected indentation (0) (should be 8)"),
                LintViolation(7, 1, "Unexpected indentation (0) (should be 4)"),
                LintViolation(8, 1, "Unexpected indentation (0) (should be 8)"),
                LintViolation(9, 1, "Unexpected indentation (0) (should be 12)"),
                LintViolation(10, 1, "Unexpected indentation (0) (should be 16)"),
                LintViolation(11, 1, "Unexpected indentation (0) (should be 16)"),
                LintViolation(12, 1, "Unexpected indentation (0) (should be 8)"),
                LintViolation(13, 1, "Unexpected indentation (0) (should be 8)"),
                LintViolation(14, 1, "Unexpected indentation (0) (should be 8)"),
            ).isFormattedAs(formattedCode)
    }

    @Test
    fun `Given an elvis operator with an incorrect indented dot qualified expression`() {
        val code =
            """
            fun bar1(): Int? = null
            fun bar2(): List<Int> = emptyList()
            fun foo(): Int? = bar1()
                ?: bar2().firstOrNull()
                .also {
                    println("bar2")
                }
            """.trimIndent()
        val formattedCode =
            """
            fun bar1(): Int? = null
            fun bar2(): List<Int> = emptyList()
            fun foo(): Int? = bar1()
                ?: bar2().firstOrNull()
                    .also {
                        println("bar2")
                    }
            """.trimIndent()
        indentationRuleAssertThat(code)
            .hasLintViolations(
                LintViolation(5, 1, "Unexpected indentation (4) (should be 8)"),
                LintViolation(6, 1, "Unexpected indentation (8) (should be 12)"),
                LintViolation(7, 1, "Unexpected indentation (4) (should be 8)"),
            ).isFormattedAs(formattedCode)
    }

    @Test
    fun `Given an elvis operator after a dot qualified expression`() {
        val code =
            """
            fun bar1(): Int? = null
            fun bar2(): List<Int> = emptyList()
            fun foo(): Int? =
                bar1().also {
                    println("bar1")
                }
                ?: bar2().also {
                    println("bar2")
                }
            """.trimIndent()
        val formattedCode =
            """
            fun bar1(): Int? = null
            fun bar2(): List<Int> = emptyList()
            fun foo(): Int? =
                bar1().also {
                    println("bar1")
                }
                    ?: bar2().also {
                        println("bar2")
                    }
            """.trimIndent()
        indentationRuleAssertThat(code)
            .hasLintViolations(
                LintViolation(7, 1, "Unexpected indentation (4) (should be 8)"),
                LintViolation(8, 1, "Unexpected indentation (8) (should be 12)"),
                LintViolation(9, 1, "Unexpected indentation (4) (should be 8)"),
            ).isFormattedAs(formattedCode)
    }

    @Test
    fun `Given a multiline string containing a string-template as parameter value but then wrap the value to a start and end on separate lines`() {
        // Interpret "$." in code samples below as "$". It is used here as otherwise the indentation in the code sample
        // is disapproved when running ktlint on the unit tests during the build process (not that the indent rule can
        // not be disabled for a block).
        val code =
            """
            fun foo() {
                println("$.{
                true
                }")
            }
            """.trimIndent().replaceStringTemplatePlaceholder()
        val formattedCode =
            """
            fun foo() {
                println(
                    "$.{
                        true
                    }"
                )
            }
            """.trimIndent().replaceStringTemplatePlaceholder()
        indentationRuleAssertThat(code)
            .addAdditionalRuleProvider { WrappingRule() }
            .hasLintViolation(3, 1, "Unexpected indentation (4) (should be 8)")
            .isFormattedAs(formattedCode)
    }

    @Test
    fun `Given a multiline raw string literal then wrap and indent conditionally`() {
        val code =
            """
            fun foo() {
                println(
                $MULTILINE_STRING_QUOTE
                $MULTILINE_STRING_QUOTE // Indent of this line will not be fixed
                )
                println(
                $MULTILINE_STRING_QUOTE
                $MULTILINE_STRING_QUOTE.trimIndent()
                )
                println(
                $MULTILINE_STRING_QUOTE
                $MULTILINE_STRING_QUOTE.trimMargin()
                )
            }
            """.trimIndent()
        val formattedCode =
            """
            fun foo() {
                println(
                    $MULTILINE_STRING_QUOTE
                $MULTILINE_STRING_QUOTE // Indent of this line will not be fixed
                )
                println(
                    $MULTILINE_STRING_QUOTE
                    $MULTILINE_STRING_QUOTE.trimIndent()
                )
                println(
                    $MULTILINE_STRING_QUOTE
                    $MULTILINE_STRING_QUOTE.trimMargin()
                )
            }
            """.trimIndent()
        indentationRuleAssertThat(code)
            .hasLintViolations(
                LintViolation(3, 1, "Unexpected indentation (4) (should be 8)"),
                LintViolation(7, 1, "Unexpected indentation (4) (should be 8)"),
                LintViolation(8, 1, "Unexpected indent of multiline string closing quotes"),
                LintViolation(11, 1, "Unexpected indentation (4) (should be 8)"),
                LintViolation(12, 1, "Unexpected indent of multiline string closing quotes"),
            ).isFormattedAs(formattedCode)
    }

    @Test
    fun `Given some multiline raw string literal contain multiline string templates`() {
        val code =
            """
            fun foo1() {
                foo2(
                $MULTILINE_STRING_QUOTE$.{
            true
                }
                text
            _$.{
            true
                }$MULTILINE_STRING_QUOTE.trimIndent(),
                ${MULTILINE_STRING_QUOTE}text$MULTILINE_STRING_QUOTE
                )
            }
            """.trimIndent().replaceStringTemplatePlaceholder()
        val formattedCode =
            """
            fun foo1() {
                foo2(
                    $MULTILINE_STRING_QUOTE$.{
                        true
                    }
                text
            _$.{
                        true
                    }$MULTILINE_STRING_QUOTE.trimIndent(),
                    ${MULTILINE_STRING_QUOTE}text$MULTILINE_STRING_QUOTE
                )
            }
            """.trimIndent().replaceStringTemplatePlaceholder()
        indentationRuleAssertThat(code)
            .hasLintViolations(
                LintViolation(3, 1, "Unexpected indentation (4) (should be 8)"),
                LintViolation(4, 1, "Unexpected indentation (0) (should be 12)"),
                LintViolation(5, 1, "Unexpected indentation (4) (should be 8)"),
                LintViolation(8, 1, "Unexpected indentation (0) (should be 12)"),
                LintViolation(9, 1, "Unexpected indentation (4) (should be 8)"),
                LintViolation(10, 1, "Unexpected indentation (4) (should be 8)"),
            ).isFormattedAs(formattedCode)
    }

    @Test
    fun `Given a type alias of a lambda`() {
        val code =
            """
            typealias F = (
            v: String
            ) -> Unit
            """.trimIndent()
        val formattedCode =
            """
            typealias F = (
                v: String
            ) -> Unit
            """.trimIndent()
        indentationRuleAssertThat(code)
            .hasLintViolation(2, 1, "Unexpected indentation (0) (should be 4)")
            .isFormattedAs(formattedCode)
    }

    @Test
    fun `Given some call to a function with a lambda as parameter`() {
        val code =
            """
            fun main() {
            f({ v ->
            d(
            1
            )
            })
            f({ v ->
            x
            .f()
            })
            }
            """.trimIndent()
        val formattedCode =
            """
            fun main() {
                f({ v ->
                    d(
                        1
                    )
                })
                f({ v ->
                    x
                        .f()
                })
            }
            """.trimIndent()
        indentationRuleAssertThat(code)
            .hasLintViolations(
                LintViolation(2, 1, "Unexpected indentation (0) (should be 4)"),
                LintViolation(3, 1, "Unexpected indentation (0) (should be 8)"),
                LintViolation(4, 1, "Unexpected indentation (0) (should be 12)"),
                LintViolation(5, 1, "Unexpected indentation (0) (should be 8)"),
                LintViolation(6, 1, "Unexpected indentation (0) (should be 4)"),
                LintViolation(7, 1, "Unexpected indentation (0) (should be 4)"),
                LintViolation(8, 1, "Unexpected indentation (0) (should be 8)"),
                LintViolation(9, 1, "Unexpected indentation (0) (should be 12)"),
                LintViolation(10, 1, "Unexpected indentation (0) (should be 4)"),
            ).isFormattedAs(formattedCode)
    }

    @Test
    fun `Given a when statement with multiple values in the clause then those can be separated to distinct lines `() {
        val code =
            """
            val foo =
                when {
                    1, 2 -> "a"
                    3,
                    4 -> "b"
                    5,
                    6 ->
                        "c"
                }
            """.trimIndent()
        indentationRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `Given a when-statement with an EOL comment before or in the value list`() {
        val code =
            """
            fun foo() {
                when {
                    // comment
                    true -> {
                    }
                }
                when {
                    1, // first element
                    2 // second element
                    -> true
                }
            }
            """.trimIndent()
        indentationRuleAssertThat(code).hasNoLintViolations()
    }

    @Nested
    inner class `Given an incorrectly indented lambda block` {
        @Test
        fun `Issue 2816 - Given ktlint_official code style`() {
            val code =
                """
                fun main() {
                    foo.func {
                        param1, param2 ->
                            doSomething()
                            doSomething2()
                        }
                }
                """.trimIndent()
            val formattedCode =
                """
                fun main() {
                    foo.func {
                        param1, param2 ->
                        doSomething()
                        doSomething2()
                    }
                }
                """.trimIndent()
            indentationRuleAssertThat(code)
                .withEditorConfigOverride(CODE_STYLE_PROPERTY to ktlint_official)
                .hasLintViolations(
                    LintViolation(4, 1, "Unexpected indentation (12) (should be 8)"),
                    LintViolation(5, 1, "Unexpected indentation (12) (should be 8)"),
                    LintViolation(6, 1, "Unexpected indentation (8) (should be 4)"),
                ).isFormattedAs(formattedCode)
        }

        @ParameterizedTest(name = "Code style: {0}")
        @EnumSource(
            value = CodeStyleValue::class,
            mode = EnumSource.Mode.EXCLUDE,
            names = ["ktlint_official"],
        )
        fun `Given code style other than 'ktlint_official'`(codeStyleValue: CodeStyleValue) {
            val code =
                """
                fun main() {
                    foo.func {
                        param1, param2 ->
                            doSomething()
                            doSomething2()
                        }
                }
                """.trimIndent()
            val formattedCode =
                """
                fun main() {
                    foo.func {
                            param1, param2 ->
                        doSomething()
                        doSomething2()
                    }
                }
                """.trimIndent()
            indentationRuleAssertThat(code)
                .withEditorConfigOverride(CODE_STYLE_PROPERTY to codeStyleValue)
                .hasLintViolations(
                    LintViolation(3, 1, "Unexpected indentation (8) (should be 12)"),
                    LintViolation(4, 1, "Unexpected indentation (12) (should be 8)"),
                    LintViolation(5, 1, "Unexpected indentation (12) (should be 8)"),
                    LintViolation(6, 1, "Unexpected indentation (8) (should be 4)"),
                ).isFormattedAs(formattedCode)
        }
    }

    @Test
    fun `Given some EOL comment after assignment`() {
        val code =
            """
            val foo = // comment
                "foo"
            """.trimIndent()
        indentationRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `Given some parameter lists`() {
        val code =
            """
            class C (val a: Int, val b: Int, val e: (
            r: Int
            ) -> Unit, val c: Int, val d: Int) {
            fun f(a: Int, b: Int, e: (
            r: Int
            ) -> Unit, c: Int, d: Int) {}
            }
            """.trimIndent()
        val formattedCode =
            """
            class C (val a: Int, val b: Int, val e: (
                r: Int
            ) -> Unit, val c: Int, val d: Int) {
                fun f(a: Int, b: Int, e: (
                    r: Int
                ) -> Unit, c: Int, d: Int) {}
            }
            """.trimIndent()
        indentationRuleAssertThat(code)
            .hasLintViolations(
                LintViolation(2, 1, "Unexpected indentation (0) (should be 4)"),
                LintViolation(4, 1, "Unexpected indentation (0) (should be 4)"),
                LintViolation(5, 1, "Unexpected indentation (0) (should be 8)"),
                LintViolation(6, 1, "Unexpected indentation (0) (should be 4)"),
            ).isFormattedAs(formattedCode)
    }

    @Test // "https://github.com/shyiko/ktlint/issues/180"
    fun `Given a class declaration using the WHERE keyword`() {
        val code =
            """
            class BiAdapter<C : RecyclerView.ViewHolder, V1 : C, V2 : C, out A1, out A2>(
                val adapter1: A1,
                val adapter2: A2
            ) : RecyclerView.Adapter<C>()
                where A1 : RecyclerView.Adapter<V1>, A1 : ComposableAdapter.ViewTypeProvider,
                    A2 : RecyclerView.Adapter<V2>, A2 : ComposableAdapter.ViewTypeProvider {
            }
            """.trimIndent()
        val formattedCode =
            """
            class BiAdapter<C : RecyclerView.ViewHolder, V1 : C, V2 : C, out A1, out A2>(
                val adapter1: A1,
                val adapter2: A2
            ) : RecyclerView.Adapter<C>()
                where A1 : RecyclerView.Adapter<V1>, A1 : ComposableAdapter.ViewTypeProvider,
                      A2 : RecyclerView.Adapter<V2>, A2 : ComposableAdapter.ViewTypeProvider {
            }
            """.trimIndent()
        indentationRuleAssertThat(code)
            .hasLintViolation(6, 1, "Unexpected indentation (8) (should be 10)")
            .isFormattedAs(formattedCode)
    }

    @Nested
    inner class `Issue 2175 - Given a function using the WHERE keyword` {
        @Test
        fun `Issue 2175 - Given a function without return type but with WHERE`() {
            val code =
                """
                fun <TFeature, TValidated> applyToAllCloseFeaturesWithUiFlow(
                    thisFeature: TFeature,
                    allFeaturesOfThisKind: List<TFeature>,
                    optionsToApply: TValidated,
                // .. more parameters
                ) where TFeature : Clusterable,
                    TFeature : SupportsExternalObjectCoordinates<out Options<out Options.Validated>, out Options.Validated, *>,
                    TValidated : Options.Validated {
                    // do something
                }
                """.trimIndent()
            val formattedCode =
                """
                fun <TFeature, TValidated> applyToAllCloseFeaturesWithUiFlow(
                    thisFeature: TFeature,
                    allFeaturesOfThisKind: List<TFeature>,
                    optionsToApply: TValidated,
                // .. more parameters
                ) where TFeature : Clusterable,
                        TFeature : SupportsExternalObjectCoordinates<out Options<out Options.Validated>, out Options.Validated, *>,
                        TValidated : Options.Validated {
                    // do something
                }
                """.trimIndent()
            indentationRuleAssertThat(code)
                .hasLintViolations(
                    LintViolation(7, 1, "Unexpected indentation (4) (should be 8)"),
                    LintViolation(8, 1, "Unexpected indentation (4) (should be 8)"),
                ).isFormattedAs(formattedCode)
        }

        @Test
        fun `Issue 2175 - Given a function with return type and WHERE on separate lines`() {
            val code =
                """
                fun <T> copyWhenGreater(list: List<T>, threshold: T): List<String>
                    where T : CharSequence,
                          T : Comparable<T> {
                    return list.filter { it > threshold }.map { it.toString() }
                }
                """.trimIndent()
            indentationRuleAssertThat(code).hasNoLintViolations()
        }

        @Test
        fun `Issue 2175 - Given a function with return type and WHERE on same line`() {
            val code =
                """
                fun <T> copyWhenGreater(
                    list: List<T>, threshold: T
                ): List<String> where T : CharSequence,
                                      T : Comparable<T> {
                    return list.filter { it > threshold }.map { it.toString() }
                }
                """.trimIndent()
            indentationRuleAssertThat(code).hasNoLintViolations()
        }
    }

    @Nested
    inner class `Issue 3380 - Given a class declaration using the WHERE keyword on the same line` {
        @Test
        fun `Issue 3380 - Given a class declaration with WHERE on same line`() {
            val code =
                """
                class Container<T> where T : Comparable<T>,
                                         T : java.io.Serializable {
                    fun body() {}
                }
                """.trimIndent()
            indentationRuleAssertThat(code).hasNoLintViolations()
        }

        @Test
        fun `Issue 3380 - Given a misaligned class declaration with WHERE on same line`() {
            val code =
                """
                class Container<T> where T : Comparable<T>,
                          T : java.io.Serializable {
                    fun body() {}
                }
                """.trimIndent()
            val formattedCode =
                """
                class Container<T> where T : Comparable<T>,
                                         T : java.io.Serializable {
                    fun body() {}
                }
                """.trimIndent()
            indentationRuleAssertThat(code)
                .hasLintViolation(2, 1, "Unexpected indentation (10) (should be 25)")
                .isFormattedAs(formattedCode)
        }
    }

    @Nested
    inner class `Issue 3362 - Given the WHERE keyword and tab indentation` {
        @Test
        fun `Given a well formed WHERE with multiple type constraints`() {
            val code =
                """
                private fun <T> example(value: T)
                ${TAB}where T : First,
                ${TAB}      T : Second {
                ${TAB}println(value)
                }
                """.trimIndent()
            indentationRuleAssertThat(code)
                .withEditorConfigOverride(INDENT_STYLE_TAB)
                .hasNoLintViolations()
        }

        @Test
        fun `Given a malformed WHERE with multiple type constraints, and too few spaces before second type constraint`() {
            val code =
                """
                private fun <T> example(value: T)
                ${TAB}where T : First,
                ${TAB}     T : Second {
                ${TAB}println(value)
                }
                """.trimIndent()
            val formattedCode =
                """
                private fun <T> example(value: T)
                ${TAB}where T : First,
                ${TAB}      T : Second {
                ${TAB}println(value)
                }
                """.trimIndent()
            indentationRuleAssertThat(code)
                .withEditorConfigOverride(INDENT_STYLE_TAB)
                .hasLintViolation(3, 1, "Unexpected indentation (6) (should be 7)")
                .isFormattedAs(formattedCode)
        }

        @Test
        fun `Given a malformed WHERE with multiple type constraints, and too much spaces before second type constraint`() {
            val code =
                """
                private fun <T> example(value: T)
                ${TAB}where T : First,
                ${TAB}        T : Second {
                ${TAB}println(value)
                }
                """.trimIndent()
            val formattedCode =
                """
                private fun <T> example(value: T)
                ${TAB}where T : First,
                ${TAB}      T : Second {
                ${TAB}println(value)
                }
                """.trimIndent()
            indentationRuleAssertThat(code)
                .withEditorConfigOverride(INDENT_STYLE_TAB)
                .hasLintViolations(
                    LintViolation(3, 1, "Unexpected indentation (9) (should be 7)"),
                    LintViolation(3, 8, "Unexpected space character(s)"),
                ).isFormattedAs(formattedCode)
        }

        @Test
        fun `Given a comma at the start of a continuation line`() {
            val code =
                // The comma at start of the continuation line should have been placed at the end of the previous line. But as the indent
                // is correct, no violation should be reported about unexpected spaces in the indent.
                """
                private fun <T> example(value: T)
                ${TAB}where T : First
                ${TAB}      , T : Second {
                ${TAB}println(value)
                }
                """.trimIndent()
            indentationRuleAssertThat(code)
                .withEditorConfigOverride(INDENT_STYLE_TAB)
                .hasNoLintViolations()
        }

        @Test
        fun `Given comments before a type constraint`() {
            val code =
                """
                private fun <T> example(value: T)
                ${TAB}where T : First,
                ${TAB}      // Line comment before the second constraint
                ${TAB}      /* Block comment before the second constraint */
                ${TAB}      T : Second {
                ${TAB}println(value)
                }
                """.trimIndent()

            indentationRuleAssertThat(code)
                .withEditorConfigOverride(INDENT_STYLE_TAB)
                .hasNoLintViolations()
        }

        @Test
        fun `Given a function with WHERE on a separate line`() {
            val code =
                """
                private interface First

                private interface Second

                private fun <T> example(value: T)
                    where T : First,
                          T : Second {
                    println(value)
                }
                """.trimIndent()
            val formattedCode =
                """
                private interface First

                private interface Second

                private fun <T> example(value: T)
                ${TAB}where T : First,
                ${TAB}      T : Second {
                ${TAB}println(value)
                }
                """.trimIndent()

            indentationRuleAssertThat(code)
                .withEditorConfigOverride(INDENT_STYLE_TAB)
                .isFormattedAs(formattedCode)

            indentationRuleAssertThat(formattedCode)
                .withEditorConfigOverride(INDENT_STYLE_TAB)
                .hasNoLintViolations()
        }
    }

    @Test // "https://github.com/ktlint/ktlint/issues/433"
    fun `Given a parameter list in which parameters are prefixed with a comment block`() {
        val code =
            """
            fun main() {
                foo(
                    /*param1=*/param1,
                    /*param2=*/param2
                )

                foo(
                    /*param1=*/ param1,
                    /*param2=*/ param2
                )
            }
            """.trimIndent()
        indentationRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `Given code with unexpected SPACE characters in the indentation`() {
        val code =
            """
            fun main() {
                return 0
              }
            """.trimIndent()
        val formattedCode =
            """
            fun main() {
            ${TAB}return 0
            }
            """.trimIndent()
        indentationRuleAssertThat(code)
            .withEditorConfigOverride(INDENT_STYLE_TAB)
            .hasLintViolations(
                LintViolation(2, 1, "Unexpected space character(s)"),
                LintViolation(2, 1, "Unexpected indentation (4) (should be 1)"),
                LintViolation(3, 1, "Unexpected space character(s)"),
                LintViolation(3, 1, "Unexpected indentation (2) (should be 0)"),
            ).isFormattedAs(formattedCode)
    }

    @Test
    fun `Given code with unexpected TAB characters in the indentation`() {
        val code =
            """
            fun main() {
            ${TAB}${TAB}return 0
            ${TAB}}
            """.trimIndent()
        val formattedCode =
            """
            fun main() {
                return 0
            }
            """.trimIndent()
        indentationRuleAssertThat(code)
            .hasLintViolations(
                LintViolation(2, 1, "Unexpected tab character(s)"),
                LintViolation(2, 1, "Unexpected indentation (2) (should be 4)"),
                LintViolation(3, 1, "Unexpected tab character(s)"),
                LintViolation(3, 1, "Unexpected indentation (1) (should be 0)"),
            ).isFormattedAs(formattedCode)
    }

    @Test
    fun `Given code indented with TABS at the correct level while it should be indented with SPACES`() {
        val code =
            """
            class Foo {
            ${TAB}fun doBar() {
            ${TAB}${TAB}println("test")
            ${TAB}}
            }
            """.trimIndent()
        val formattedCode =
            """
            class Foo {
                fun doBar() {
                    println("test")
                }
            }
            """.trimIndent()
        indentationRuleAssertThat(code)
            .hasLintViolations(
                LintViolation(2, 1, "Unexpected tab character(s)"),
                LintViolation(2, 1, "Unexpected indentation (1) (should be 4)"),
                LintViolation(3, 1, "Unexpected tab character(s)"),
                LintViolation(3, 1, "Unexpected indentation (2) (should be 8)"),
                LintViolation(4, 1, "Unexpected tab character(s)"),
                LintViolation(4, 1, "Unexpected indentation (1) (should be 4)"),
            ).isFormattedAs(formattedCode)
    }

    @Test
    fun `Given code which is correctly indented with TABS while a custom indent size of 2 SPACES should be used`() {
        val code =
            """
            class Foo {
            ${TAB}fun main() {
            ${TAB}${TAB}return 0
            ${TAB}}
            }
            """.trimIndent()
        val formattedCode =
            """
            class Foo {
              fun main() {
                return 0
              }
            }
            """.trimIndent()
        indentationRuleAssertThat(code)
            .withEditorConfigOverride(INDENT_SIZE_PROPERTY to 2)
            .hasLintViolations(
                LintViolation(2, 1, "Unexpected tab character(s)"),
                LintViolation(2, 1, "Unexpected indentation (1) (should be 2)"),
                LintViolation(3, 1, "Unexpected tab character(s)"),
                LintViolation(3, 1, "Unexpected indentation (2) (should be 4)"),
                LintViolation(4, 1, "Unexpected tab character(s)"),
                LintViolation(4, 1, "Unexpected indentation (1) (should be 2)"),
            ).isFormattedAs(formattedCode)
    }

    @Test
    fun `Given a function declaration with a new line after the equality sign then do no return any lint errors`() {
        val code =
            """
            private fun getImplementationVersion() =
                javaClass.`package`.implementationVersion
                    ?: javaClass.getResourceAsStream("/META-INF/MANIFEST.MF")
                        ?.let { stream ->
                            Manifest(stream).mainAttributes.getValue("Implementation-Version")
                        }
            """.trimIndent()
        indentationRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `Given a lambda with incorrect indentation after lambda arrow`() {
        val code =
            """
            fun bar() {
                Pair("val1", "val2")
                    .let { (first, second) ->
                            first + second
                    }
            }
            """.trimIndent()
        val formattedCode =
            """
            fun bar() {
                Pair("val1", "val2")
                    .let { (first, second) ->
                        first + second
                    }
            }
            """.trimIndent()
        indentationRuleAssertThat(code)
            .hasLintViolation(4, 1, "Unexpected indentation (16) (should be 12)")
            .isFormattedAs(formattedCode)
    }

    @Test
    fun `Given a function declaration with the return type incorrectly indented at new line`() {
        val code =
            """
            abstract fun doPerformSomeOperation(param: ALongParameter):
            SomeLongInterface<ALongParameter.InnerClass, SomeOtherClass>
            """.trimIndent()
        val formattedCode =
            """
            abstract fun doPerformSomeOperation(param: ALongParameter):
                SomeLongInterface<ALongParameter.InnerClass, SomeOtherClass>
            """.trimIndent()
        indentationRuleAssertThat(code)
            .hasLintViolation(2, 1, "Unexpected indentation (0) (should be 4)")
            .isFormattedAs(formattedCode)
    }

    @Nested
    inner class `Given a variable declaration with type incorrectly indented on a new line` {
        @Test
        fun `Given a code style other than ktlint_official`() {
            val code =
                """
                val s:
                        String = ""

                fun process(
                    fileName:
                        String
                ): List<Output>
                """.trimIndent()
            val formattedCode =
                """
                val s:
                    String = ""

                fun process(
                    fileName:
                    String
                ): List<Output>
                """.trimIndent()
            indentationRuleAssertThat(code)
                .withEditorConfigOverride(CODE_STYLE_PROPERTY to intellij_idea)
                .hasLintViolations(
                    LintViolation(2, 1, "Unexpected indentation (8) (should be 4)"),
                    LintViolation(6, 1, "Unexpected indentation (8) (should be 4)"),
                ).isFormattedAs(formattedCode)
        }

        @Test
        fun `Given the code style ktlint_official`() {
            val code =
                """
                val s:
                        String = ""

                fun process(
                    fileName:
                        String
                ): List<Output>
                """.trimIndent()
            val formattedCode =
                """
                val s:
                    String = ""

                fun process(
                    fileName:
                        String
                ): List<Output>
                """.trimIndent()
            indentationRuleAssertThat(code)
                .withEditorConfigOverride(CODE_STYLE_PROPERTY to ktlint_official)
                .hasLintViolation(2, 1, "Unexpected indentation (8) (should be 4)")
                .isFormattedAs(formattedCode)
        }
    }

    @Test
    fun `Given some code with an EOL comment in a multiline parameter then do not return lint errors`() {
        val code =
            """
            fun foo(param: Foo, other: String) {
                foo(
                    param = param
                        .copy(foo = ""), // A comment
                    other = ""
                )
            }
            """.trimIndent()
        indentationRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `Given a safe-called lambda then do not return lint errors`() {
        val code =
            """
            val foo = bar
                ?.filter { number ->
                    number == 0
                }?.map { evenNumber ->
                    evenNumber * evenNumber
                }
            """.trimIndent()
        indentationRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `Given a statement (not wrapped in a block) after an if then do no return lint errors`() {
        val code =
            """
            fun test() {
                if (true)
                    (1).toString()
                else
                    2.toString()
            }
            """.trimIndent()
        indentationRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `Issue 796 - Given an if-condition with multiline call expression which is indented properly then do no return lint errors`() {
        val code =
            """
            private val gpsRegion =
                if (permissionHandler.isPermissionGranted(
                        context, Manifest.permission.ACCESS_FINE_LOCATION
                    )
                ) {
                    // stuff
                }
            """.trimIndent()
        indentationRuleAssertThat(code).hasNoLintViolations()
    }

    @Nested
    inner class `Given a raw string literals` {
        @Test
        fun `Given a raw string literal with an indented string template`() {
            val code =
                """
                val foo =
                    $MULTILINE_STRING_QUOTE
                        ${true}
                    $MULTILINE_STRING_QUOTE.trimIndent()
                """.trimIndent()
            indentationRuleAssertThat(code).hasNoLintViolations()
        }

        @Test
        fun `Given a raw string literal as single line`() {
            val code =
                """
                val foo = $MULTILINE_STRING_QUOTE${true}$MULTILINE_STRING_QUOTE.trimIndent()
                """.trimIndent()
            indentationRuleAssertThat(code).hasNoLintViolations()
        }

        @Test
        fun `Given a empty raw string literal with closing and ending quotes on different lines`() {
            val code =
                """
                val foo1 =
                    $MULTILINE_STRING_QUOTE
                    $MULTILINE_STRING_QUOTE.trimIndent()
                val foo2 =
                    $MULTILINE_STRING_QUOTE

                    $MULTILINE_STRING_QUOTE.trimIndent()
                val foo3 =
                $MULTILINE_STRING_QUOTE

                $MULTILINE_STRING_QUOTE.trimIndent()
                """.trimIndent()
            indentationRuleAssertThat(code).hasNoLintViolations()
        }

        @Test
        fun `Given a raw string literal assignment to variable with opening quotes on same line as declaration`() {
            val code =
                """
                fun foo() {
                    val bar = $MULTILINE_STRING_QUOTE
                              line1
                                  line2
                              $MULTILINE_STRING_QUOTE.trimIndent()
                }
                """.trimIndent()
            val formattedCode =
                """
                fun foo() {
                    val bar = $MULTILINE_STRING_QUOTE
                              line1
                                  line2
                    $MULTILINE_STRING_QUOTE.trimIndent()
                }
                """.trimIndent()
            indentationRuleAssertThat(code)
                .hasLintViolation(5, 1, "Unexpected indent of multiline string closing quotes")
                .isFormattedAs(formattedCode)
        }

        @Test
        fun `Given a raw string literal containing quotation marks for which the closing quotes are not correctly indented`() {
            val code =
                """
                fun foo() {
                    println(
                        $MULTILINE_STRING_QUOTE
                        text ""

                             text
                             ""
                    $MULTILINE_STRING_QUOTE.trimIndent()
                    )
                }
                """.trimIndent()
            val formattedCode =
                """
                fun foo() {
                    println(
                        $MULTILINE_STRING_QUOTE
                        text ""

                             text
                             ""
                        $MULTILINE_STRING_QUOTE.trimIndent()
                    )
                }
                """.trimIndent()
            indentationRuleAssertThat(code)
                .hasLintViolation(8, 1, "Unexpected indent of multiline string closing quotes")
                .isFormattedAs(formattedCode)
        }

        @Test
        fun `Given a multiline raw string literal as function call parameter but not starting and ending on a separate line`() {
            val code =
                """
                fun foo() {
                println($MULTILINE_STRING_QUOTE
                    text

                        text
                $MULTILINE_STRING_QUOTE.trimIndent().toByteArray())
                }
                """.trimIndent()
            val formattedCode =
                """
                fun foo() {
                    println(
                        $MULTILINE_STRING_QUOTE
                        text

                            text
                        $MULTILINE_STRING_QUOTE.trimIndent().toByteArray()
                    )
                }
                """.trimIndent()
            indentationRuleAssertThat(code)
                .addAdditionalRuleProvider { WrappingRule() }
                .addAdditionalRuleProvider { StringTemplateIndentRule() }
                .hasLintViolations(
                    LintViolation(2, 1, "Unexpected indentation (0) (should be 4)"),
                    LintViolation(6, 1, "Unexpected indent of multiline string closing quotes"),
                ).isFormattedAs(formattedCode)
        }

        @Disabled("to be fixed")
        @Test
        fun `Given a multiline raw string literal in which the opening quotes are followed with some text on the same line`() {
            val code =
                """
                val foo1 = ${MULTILINE_STRING_QUOTE}Text
                    More text
                   $MULTILINE_STRING_QUOTE
                val foo2 = ${MULTILINE_STRING_QUOTE}Text
                $MULTILINE_STRING_QUOTE
                """.trimIndent()
            val formattedCode =
                """
                val foo1 = ${MULTILINE_STRING_QUOTE}
                Text
                    More text
                    $MULTILINE_STRING_QUOTE
                val foo2 = ${MULTILINE_STRING_QUOTE}
                    Text
                    $MULTILINE_STRING_QUOTE
                """.trimIndent()
            indentationRuleAssertThat(code)
                .hasLintViolations(
                    LintViolation(3, 3, "Unexpected indent of multiline string closing quotes"),
                    LintViolation(5, 1, "Unexpected indent of multiline string closing quotes"),
                ).isFormattedAs(formattedCode)
        }

        @Test
        fun `Given some class variables having a raw string literal been assigned as value`() {
            val code =
                """
                class Foo {
                    val foo1 = $MULTILINE_STRING_QUOTE
                        {
                        }
                    $MULTILINE_STRING_QUOTE.trimIndent()
                    val foo2 =
                    $MULTILINE_STRING_QUOTE
                        {
                        }
                    $MULTILINE_STRING_QUOTE.trimIndent()
                    val foo3 = // comment
                    $MULTILINE_STRING_QUOTE
                    {
                    }
                    $MULTILINE_STRING_QUOTE.trimIndent()
                }
                """.trimIndent()
            val formattedCode =
                """
                class Foo {
                    val foo1 = $MULTILINE_STRING_QUOTE
                        {
                        }
                    $MULTILINE_STRING_QUOTE.trimIndent()
                    val foo2 =
                        $MULTILINE_STRING_QUOTE
                        {
                        }
                        $MULTILINE_STRING_QUOTE.trimIndent()
                    val foo3 = // comment
                        $MULTILINE_STRING_QUOTE
                    {
                    }
                        $MULTILINE_STRING_QUOTE.trimIndent()
                }
                """.trimIndent()
            indentationRuleAssertThat(code)
                .hasLintViolations(
                    LintViolation(7, 1, "Unexpected indentation (4) (should be 8)"),
                    LintViolation(10, 1, "Unexpected indent of multiline string closing quotes"),
                    LintViolation(12, 1, "Unexpected indentation (4) (should be 8)"),
                    LintViolation(15, 1, "Unexpected indent of multiline string closing quotes"),
                ).isFormattedAs(formattedCode)
        }

        @Test
        fun `format raw string literal containing a template string as the first non blank element on the line`() {
            // Escape '${true}' as '${"$"}{true}' to prevent evaluation before actually processing the multiline string
            val code =
                """
                fun foo() {
                    println(
                        $MULTILINE_STRING_QUOTE
                        ${"$"}{true}

                            ${"$"}{true}
                    $MULTILINE_STRING_QUOTE.trimIndent()
                    )
                    println(
                $MULTILINE_STRING_QUOTE
                ${"$"}{true}

                    ${"$"}{true}
                $MULTILINE_STRING_QUOTE.trimIndent()
                    )
                }
                """.trimIndent()
            val formattedCode =
                """
                fun foo() {
                    println(
                        $MULTILINE_STRING_QUOTE
                        ${"$"}{true}

                            ${"$"}{true}
                        $MULTILINE_STRING_QUOTE.trimIndent()
                    )
                    println(
                $MULTILINE_STRING_QUOTE
                ${"$"}{true}

                    ${"$"}{true}
                $MULTILINE_STRING_QUOTE.trimIndent()
                    )
                }
                """.trimIndent()
            indentationRuleAssertThat(code)
                .hasLintViolation(7, 1, "Unexpected indent of multiline string closing quotes")
                .isFormattedAs(formattedCode)
        }

        @Test
        fun `Issue 575 - Given a raw string literal which is properly indented but does contain tabs after the margin then do not return lint errors`() {
            val code =
                """
                val str =
                    $MULTILINE_STRING_QUOTE
                    ${TAB}Tab at the beginning of this line but after the indentation margin
                    Tab${TAB}in the middle of this string
                    Tab at the end of this line.$TAB
                    $MULTILINE_STRING_QUOTE.trimIndent()
                """.trimIndent()
            indentationRuleAssertThat(code).hasNoLintViolations()
        }

        @Test
        fun `Given a raw string literal with mixed indentation characters, can not be autocorrected`() {
            val code =
                """
                val foo = $MULTILINE_STRING_QUOTE
                      line1
                ${TAB}line2
                    $MULTILINE_STRING_QUOTE.trimIndent()
                """.trimIndent()
            indentationRuleAssertThat(code)
                .hasLintViolationWithoutAutoCorrect(1, 11, "Indentation of multiline string should not contain both tab(s) and space(s)")
        }

        @Test
        fun `Give a raw string literal at start of line`() {
            val code =
                """
                fun foo() =
                $MULTILINE_STRING_QUOTE
                some text
                $MULTILINE_STRING_QUOTE
                """.trimIndent()
            indentationRuleAssertThat(code).hasNoLintViolations()
        }
    }

    @Test
    fun `Given an if-condition with line break and multiline call expression which is indented properly then do not return lint errors`() {
        val code =
            """
            // https://github.com/ktlint/ktlint/issues/871
            fun function(param1: Int, param2: Int, param3: Int?): Boolean {
                return if (
                    listOf(
                        param1,
                        param2,
                        param3
                    ).none { it != null }
                ) {
                    true
                } else {
                    false
                }
            }

            // https://github.com/ktlint/ktlint/issues/900
            enum class Letter(val value: String) {
                A("a"),
                B("b");
            }
            fun broken(key: String): Letter {
                for (letter in Letter.values()) {
                    if (
                        letter.value
                            .equals(
                                key,
                                ignoreCase = true
                            )
                    ) {
                        return letter
                    }
                }
                return Letter.B
            }
            """.trimIndent()
        indentationRuleAssertThat(code).hasNoLintViolations()
    }

    @Nested
    inner class `Given a property delegate` {
        @Test
        fun `Property delegate is indented properly 1`() {
            val code =
                """
                val i: Int
                    by lazy { 1 }

                val j = 0
                """.trimIndent()
            indentationRuleAssertThat(code).hasNoLintViolations()
        }

        @Test
        fun `Property delegate is indented properly 2`() {
            val code =
                """
                val i: Int
                    by lazy {
                        "".let {
                            println(it)
                        }
                        1
                    }

                val j = 0
                """.trimIndent()
            indentationRuleAssertThat(code).hasNoLintViolations()
        }

        @Test
        fun `Property delegate is indented properly 3`() {
            val code =
                """
                val i: Int by lazy {
                    "".let {
                        println(it)
                    }
                    1
                }

                val j = 0
                """.trimIndent()
            indentationRuleAssertThat(code).hasNoLintViolations()
        }

        @Test
        fun `Property delegate is indented properly 4`() {
            val code =
                """
                fun lazyList() = lazy { mutableListOf<String>() }

                class Test {
                    val list: List<String>
                        by lazyList()

                    val aVar = 0
                }
                """.trimIndent()
            indentationRuleAssertThat(code).hasNoLintViolations()
        }

        @Test
        fun `Property delegate is indented properly 5`() {
            val code =
                """
                fun lazyList(a: Int, b: Int) = lazy { mutableListOf<String>() }

                class Test {
                    val list: List<String>
                        by lazyList(
                            1,
                            2
                        )

                    val aVar = 0
                }
                """.trimIndent()
            indentationRuleAssertThat(code).hasNoLintViolations()
        }

        @Test
        fun `Issue 1340 - Given a dot-qualified-expression as delegated property value`() {
            val code =
                """
                class Foo {
                    private val foo
                        by option("--myOption")
                                .int()
                                    .default(1)
                }
                """.trimIndent()
            val formattedCode =
                """
                class Foo {
                    private val foo
                        by option("--myOption")
                            .int()
                            .default(1)
                }
                """.trimIndent()
            indentationRuleAssertThat(code)
                .hasLintViolations(
                    LintViolation(4, 1, "Unexpected indentation (16) (should be 12)"),
                    LintViolation(5, 1, "Unexpected indentation (20) (should be 12)"),
                ).isFormattedAs(formattedCode)
        }

        @Test
        fun `Issue 1510 - Given a dot-qualified-expression as delegated property value starting on same line as previous code sibling`() {
            val code =
                """
                val locale: Locale by option
                    .default()
                """.trimIndent()
            indentationRuleAssertThat(code).hasNoLintViolations()
        }

        @Test
        fun `Issue 1340 - Given a dot-qualified-expression wrapped in a block as delegated property value`() {
            val code =
                """
                class MyCliktCommand : CliktCommand() {
                    private val myOption
                        by {
                            option("--myOption")
                                .int()
                                .default(1)
                        }
                }
                """.trimIndent()
            indentationRuleAssertThat(code).hasNoLintViolations()
        }
    }

    @Test
    fun `Issue 1210 - lint delegated properties with a lambda argument`() {
        val code =
            """
            import kotlin.properties.Delegates

            class Test {
                private var test
                    by Delegates.vetoable("") { _, old, new ->
                        true
                    }
            }
            """.trimIndent()
        indentationRuleAssertThat(code).hasNoLintViolations()
    }

    @Nested
    inner class `Given some delegation construction` {
        @Test
        fun `Delegation 1`() {
            val code =
                """
                interface Foo

                class Bar(a: Int, b: Int, c: Int) : Foo

                class Test1 : Foo by Bar(
                    a = 1,
                    b = 2,
                    c = 3
                )
                """.trimIndent()
            indentationRuleAssertThat(code).hasNoLintViolations()
        }

        @Test
        fun `Delegation 2`() {
            val code =
                """
                class Test2 : Foo
                by Bar(
                    a = 1,
                    b = 2,
                    c = 3
                )
                """.trimIndent()
            indentationRuleAssertThat(code).hasNoLintViolations()
        }

        @Test
        fun `Delegation 3`() {
            val code =
                """
                interface Foo

                class Bar(a: Int, b: Int, c: Int) : Foo

                class Test3 :
                    Foo by Bar(
                        a = 1,
                        b = 2,
                        c = 3
                    )
                """.trimIndent()
            indentationRuleAssertThat(code).hasNoLintViolations()
        }

        @Test
        fun `Delegation 4`() {
            val code =
                """
                interface Foo

                class Bar(a: Int, b: Int, c: Int) : Foo

                class Test4 :
                    Foo
                    by Bar(
                        a = 1,
                        b = 2,
                        c = 3
                    )
                """.trimIndent()
            indentationRuleAssertThat(code).hasNoLintViolations()
        }

        @Test
        fun `Delegation 5`() {
            val code =
                """
                interface Foo

                class Bar(a: Int, b: Int, c: Int) : Foo

                class Test5 {
                    companion object : Foo by Bar(
                        a = 1,
                        b = 2,
                        c = 3
                    )
                }
                """.trimIndent()
            indentationRuleAssertThat(code).hasNoLintViolations()
        }

        @Test
        fun `Delegation 6`() {
            val code =
                """
                data class Shortcut(val id: String, val url: String)

                object Someclass : List<Shortcut> by listOf(
                    Shortcut(
                        id = "1",
                        url = "url"
                    ),
                    Shortcut(
                        id = "2",
                        url = "asd"
                    ),
                    Shortcut(
                        id = "3",
                        url = "TV"
                    )
                )
                """.trimIndent()
            indentationRuleAssertThat(code).hasNoLintViolations()
        }
    }

    @Test
    fun `Given a named argument`() {
        val code =
            """
            data class D(val a: Int, val b: Int, val c: Int)

            fun test() {
                val d = D(
                    a = 1,
                    b =
                    2,
                    c = 3
                )
            }
            """.trimIndent()
        indentationRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `Given a default parameter`() {
        val code =
            """
            data class D(
                val a: Int = 1,
                val b: Int =
                    2,
                val c: Int = 3
            )
            """.trimIndent()
        indentationRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `Issue 959 - Given conditions with multi-line call expressions indented properly`() {
        val code =
            """
            fun test() {
                val result = true &&
                    minOf(
                        1, 2
                    ) == 2
            }
            """.trimIndent()
        indentationRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `Issue 1003 - Given non-ktlint_official code style and multiple interfaces`() {
        val code =
            """
            abstract class Parent(a: Int, b: Int)

            interface Parent2

            class Child(
                a: Int,
                b: Int
            ) : Parent(
                a,
                b
            ),
                Parent2
            """.trimIndent()
        indentationRuleAssertThat(code)
            .withEditorConfigOverride(CODE_STYLE_PROPERTY to intellij_idea)
            .hasNoLintViolations()
    }

    @Test
    fun `Issue 918 - Given newline after type reference in functions`() {
        val code =
            """
            override fun actionProcessor():
                ObservableTransformer<in SomeVeryVeryLongNameOverHereAction, out SomeVeryVeryLongNameOverHereResult> =
                ObservableTransformer { actions ->
                    // ...
                }

            fun generateGooooooooooooooooogle():
                Gooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooogle {
                return Gooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooogle()
            }
            """.trimIndent()
        indentationRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `Issue 764 - Given value argument list with lambda`() {
        val code =
            """
            fun test(i: Int, f: (Int) -> Unit) {
                f(i)
            }

            fun main() {
                test(1, f = {
                    println(it)
                })
            }
            """.trimIndent()
        indentationRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `Given value argument list with two lambdas`() {
        val code =
            """
            fun test(f: () -> Unit, g: () -> Unit) {
                f()
                g()
            }

            fun main() {
                test({
                    println(1)
                }, {
                    println(2)
                })
            }
            """.trimIndent()
        indentationRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `Given a value argument list with anonymous function`() {
        val code =
            """
            fun test(i: Int, f: (Int) -> Unit) {
                f(i)
            }

            fun main() {
                test(1, fun(it: Int) {
                    println(it)
                })
            }
            """.trimIndent()
        indentationRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `Given a value argument list with lambda in super type entry`() {
        val code =
            """
            class A : B({
                1
            }) {
                val a = 1
            }

            open class B(f: () -> Int)
            """.trimIndent()
        indentationRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `Issue 1202 - lint lambda argument and call chain`() {
        val code =
            """
            class Foo {
                fun bar() {
                    val foo = bar.associateBy({ item -> item.toString() }, ::someFunction).toMap()
                }
            }
            """.trimIndent()
        indentationRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `Issue 1165 - lint multiline expression with elvis operator in assignment`() {
        val code =
            """
            fun test() {
                val a: String = ""

                val someTest: Int?

                someTest =
                    a
                        .toIntOrNull()
                        ?: 1
            }
            """.trimIndent()
        indentationRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `Given some kdoc and SPACE indent style`() {
        val code =
            """
            /**
             * some function1
             */
            fun someFunction1() {
                return Unit
            }

            class SomeClass {
                /**
                 * some function2
                 */
                fun someFunction2() {
                    return Unit
                }
            }
            """.trimIndent()
        indentationRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `Given some kdoc and TAB indent style`() {
        val code =
            """
            /**
             * some function1
             */
            fun someFunction1() {
            ${TAB}return Unit
            }

            class SomeClass {
            ${TAB}/**
            ${TAB} * some function2
            ${TAB} */
            ${TAB}fun someFunction2() {
            ${TAB}${TAB}return Unit
            ${TAB}}
            }
            """.trimIndent()
        indentationRuleAssertThat(code)
            .withEditorConfigOverride(INDENT_STYLE_TAB)
            .hasNoLintViolations()
    }

    @Test
    fun `Issue 1222 - Given a class with a secondary constructor`() {
        val code =
            """
            class Issue1222 {
                constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) :
                        super(context, attrs, defStyleAttr, defStyleRes) {
                    init(attrs, defStyleAttr, defStyleRes)
                }
            }
            """.trimIndent()
        val formattedCode =
            """
            class Issue1222 {
                constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) :
                    super(context, attrs, defStyleAttr, defStyleRes) {
                    init(attrs, defStyleAttr, defStyleRes)
                }
            }
            """.trimIndent()
        indentationRuleAssertThat(code)
            .hasLintViolation(3, 1, "Unexpected indentation (12) (should be 8)")
            .isFormattedAs(formattedCode)
    }

    @Test
    fun `Issue 1222 - Given a class constructor, parameter of super invocations are indented`() {
        val code =
            """
            class Issue1222 {
                constructor(string1: String, string2: String2) :
                    super(
                    string1, string2
                    ) {
                    // do something
                }
            }
            """.trimIndent()
        val formattedCode =
            """
            class Issue1222 {
                constructor(string1: String, string2: String2) :
                    super(
                        string1, string2
                    ) {
                    // do something
                }
            }
            """.trimIndent()
        indentationRuleAssertThat(code)
            .hasLintViolation(4, 1, "Unexpected indentation (8) (should be 12)")
            .isFormattedAs(formattedCode)
    }

    @Test
    fun `Given a function literal with comment before the parameter list`() {
        val code =
            """
            val foo1: (String) -> String = { // Some comment which should not be moved to the next line when formatting
                    s: String
                ->
                // does something with string
            }

            val foo2: (String) -> String = {
                    // Some comment which has to be indented with the parameter list
                    s: String
                ->
                // does something with string
            }

            val foo3 = { // Some comment which should not be moved to the next line when formatting
                    s: String,
                ->
                // does something with string
            }

            val foo4 = {
                    // Some comment which has to be indented with the parameter list
                    s: String,
                ->
                // does something with string
            }
            """.trimIndent()
        val formattedCode =
            """
            val foo1: (String) -> String = { // Some comment which should not be moved to the next line when formatting
                s: String
                ->
                // does something with string
            }

            val foo2: (String) -> String = {
                // Some comment which has to be indented with the parameter list
                s: String
                ->
                // does something with string
            }

            val foo3 = { // Some comment which should not be moved to the next line when formatting
                s: String,
                ->
                // does something with string
            }

            val foo4 = {
                // Some comment which has to be indented with the parameter list
                s: String,
                ->
                // does something with string
            }
            """.trimIndent()
        indentationRuleAssertThat(code)
            .hasLintViolations(
                LintViolation(2, 1, "Unexpected indentation (8) (should be 4)"),
                LintViolation(8, 1, "Unexpected indentation (8) (should be 4)"),
                LintViolation(9, 1, "Unexpected indentation (8) (should be 4)"),
                LintViolation(15, 1, "Unexpected indentation (8) (should be 4)"),
                LintViolation(21, 1, "Unexpected indentation (8) (should be 4)"),
                LintViolation(22, 1, "Unexpected indentation (8) (should be 4)"),
            ).isFormattedAs(formattedCode)
    }

    @Test
    fun `Issue 1247 - Given a function literal with single value parameter`() {
        val code =
            """
            val foo1: (String) -> String = {
                    s: String
                ->
                // does something with string
            }

            val foo2 = {
                    // Trailing comma on last element is allowed and does not have effect
                    s: String,
                ->
                // does something with string
            }
            """.trimIndent()
        val formattedCode =
            """
            val foo1: (String) -> String = {
                s: String
                ->
                // does something with string
            }

            val foo2 = {
                // Trailing comma on last element is allowed and does not have effect
                s: String,
                ->
                // does something with string
            }
            """.trimIndent()
        indentationRuleAssertThat(code)
            .hasLintViolations(
                LintViolation(2, 1, "Unexpected indentation (8) (should be 4)"),
                LintViolation(8, 1, "Unexpected indentation (8) (should be 4)"),
                LintViolation(9, 1, "Unexpected indentation (8) (should be 4)"),
            ).isFormattedAs(formattedCode)
    }

    @Test
    fun `Issue 1247 - Formats function literal with multiple value parameters`() {
        val code =
            """
            val foo1: (String, String) -> String = {
                    s1: String,
                    s2: String
                ->
                // does something with strings
            }

            val foo2 = {
                    s1: String,
                    // Trailing comma on last element is allowed and does not have effect
                    s2: String,
                ->
                // does something with strings
            }
            """.trimIndent()
        val formattedCode =
            """
            val foo1: (String, String) -> String = {
                s1: String,
                s2: String
                ->
                // does something with strings
            }

            val foo2 = {
                s1: String,
                // Trailing comma on last element is allowed and does not have effect
                s2: String,
                ->
                // does something with strings
            }
            """.trimIndent()
        indentationRuleAssertThat(code)
            .hasLintViolations(
                LintViolation(2, 1, "Unexpected indentation (8) (should be 4)"),
                LintViolation(3, 1, "Unexpected indentation (8) (should be 4)"),
                LintViolation(9, 1, "Unexpected indentation (8) (should be 4)"),
                LintViolation(10, 1, "Unexpected indentation (8) (should be 4)"),
                LintViolation(11, 1, "Unexpected indentation (8) (should be 4)"),
            ).isFormattedAs(formattedCode)
    }

    @Test // user_type
    fun `Issue 1210 - Given a supertype delegate`() {
        val code =
            """
            object ApplicationComponentFactory : ApplicationComponent.Factory
            by DaggerApplicationComponent.factory()
            """.trimIndent()
        indentationRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `Issue 1210 - Given some statements after supertype delegated entry 2`() {
        val code =
            """
            interface Foo

            class Bar(a: Int, b: Int, c: Int) : Foo

            class Test4 :
                Foo
                by Bar(
                    a = 1,
                    b = 2,
                    c = 3
                )

            // The next line ensures that the fix regarding the expectedIndex due to alignment of "by" keyword in
            // class above, is still in place. Without this fix, the expectedIndex would hold a negative value,
            // resulting in the formatting to crash on the next line.
            val bar = 1
            """.trimIndent()
        indentationRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `Issue 1330 - Given a function with a lambda parameter having a default value is allowed on a single line`() {
        val code =
            """
            fun func(lambdaArg: Unit.() -> Unit = {}, secondArg: Int) {
                println()
            }
            fun func(lambdaArg: Unit.(a: String) -> Unit = { it -> it.toUpperCaseAsciiOnly() }, secondArg: Int) {
                println()
            }
            """.trimIndent()
        indentationRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `Given a function with multiple lambda parameters can be formatted differently`() {
        val code =
            """
            // https://github.com/ktlint/ktlint/issues/764#issuecomment-646822853
            val foo1 = println({
                bar()
            }, {
                bar()
            })
            // Other formats which should be allowed as well
            val foo2 = println(
                {
                    bar()
                },
                { bar() }
            )
            val foo3 = println(
                // Some comment
                {
                    bar()
                },
                // Some comment
                { bar() }
            )
            val foo4 = println(
                /* Some comment */
                {
                    bar()
                },
                /* Some comment */
                { bar() }
            )
            val foo5 = println(
                { bar() },
                { bar() }
            )
            val foo6 = println(
                // Some comment
                { bar() },
                // Some comment
                { bar() }
            )
            val foo7 = println(
                /* Some comment */
                { bar() },
                /* Some comment */
                { bar() }
            )
            val foo8 = println(
                { bar() }, { bar() }
            )
            val foo9 = println({ bar() }, { bar()})
            """.trimIndent()
        indentationRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `Binary expression`() {
        val code =
            """
            val x = "" +
                "" +
                f2(
                    "" // IDEA quirk (ignored)
                )
            """.trimIndent()
        indentationRuleAssertThat(code).hasNoLintViolations()
    }

    @Disabled("To be fixed as code as lambda with parameters on multiple line")
    @Test
    fun `Given a function with lambda parameters on multiple lines then align the parameters`() {
        val code =
            """
            val fieldExample =
                LongNameClass { paramA,
                        paramB,
                        paramC ->
                    ClassB(paramA, paramB, paramC)
                }
            """.trimIndent()
        val formattedCode =
            """
            val fieldExample =
                LongNameClass { paramA,
                                paramB,
                                paramC ->
                    ClassB(paramA, paramB, paramC)
                }
            """.trimIndent()
        indentationRuleAssertThat(code)
            .hasLintViolations(
                LintViolation(3, 1, "Unexpected indentation (12) (should be 20)"),
                LintViolation(4, 1, "Unexpected indentation (12) (should be 20)"),
            ).isFormattedAs(formattedCode)
    }

    @Nested
    inner class `Issue1350 - Given a for-loop` {
        @Test
        fun `Issue 1350 - Given a for-loop containing a newline before the declaration then do not indent it to keep it in sync with IntelliJ default formatting`() {
            val code =
                """
                fun foo() {
                    for (
                    item in listOf(
                        "a",
                        "b"
                    )) {
                        println(item)
                    }
                }
                """.trimIndent()
            indentationRuleAssertThat(code).hasNoLintViolations()
        }

        @Test
        fun `Issue 1350 - Given a for-loop containing a newline before the 'in' operator then do not indent it to keep it in sync with IntelliJ default formatting`() {
            val code =
                """
                fun foo() {
                    for (item
                    in listOf(
                        "a",
                        "b"
                    )) {
                        println(item)
                    }
                }
                """.trimIndent()
            indentationRuleAssertThat(code).hasNoLintViolations()
        }

        @Test
        fun `Issue 1350 - Given a for-loop containing a newline before the expression then do not indent it to keep it in sync with IntelliJ default formatting`() {
            val code =
                """
                fun foo() {
                    for (item in
                    listOf(
                        "a",
                        "b"
                    )) {
                        println(item)
                    }
                }
                """.trimIndent()
            indentationRuleAssertThat(code).hasNoLintViolations()
        }

        @Test
        fun `Issue 1350 - Given a for-loop with a multiline expression then indent that expression normally`() {
            val code =
                """
                fun foo() {
                    for (item in listOf(
                        "a",
                        "b"
                    )) {
                        println(item)
                    }
                }
                """.trimIndent()
            indentationRuleAssertThat(code).hasNoLintViolations()
        }
    }

    @Nested
    inner class `Issue 1335 - Given a property with an initializer on a separate line` {
        @Test
        fun `Issue 1335 - Given the initializer is followed by a getter with a multiline body expression`() {
            val code =
                """
                private val foo: String =
                    "foo"
                    get() =
                        listOf("a", value, "c")
                            .filterNotNull()
                            .joinToString()
                """.trimIndent()
            indentationRuleAssertThat(code).hasNoLintViolations()
        }

        @Test
        fun `Issue 1335 - Given the initializer is followed by a getter with a block body containing a multiline dot qualified expression`() {
            val code =
                """
                private val foo1: String =
                    "foo"
                    get() {
                        return listOf("a", value, "c")
                            .filterNotNull()
                            .joinToString()
                    }
                """.trimIndent()
            indentationRuleAssertThat(code).hasNoLintViolations()
        }

        @Test
        fun `Issue 1335 - Given a the initializer is followed by a setter with a block body a multiline dot qualified expression`() {
            val code =
                """
                private var foo: String =
                    "foo"
                    set(value) {
                        listOf("a", value, "c")
                            .filterNotNull()
                            .joinToString()
                    }
                """.trimIndent()
            indentationRuleAssertThat(code).hasNoLintViolations()
        }
    }

    @Nested
    inner class `Issue 631 - Given some suppression directive in the middle of a file` {
        @Test
        fun `Issue 631 - Given some code for which indentation is disabled with ktlint-suppression then do not fix indentation of that block only`() {
            val code =
                """
                val fooWithIndentationFixing1: String =
                    "foo" +
                        "bar"
                @Suppress("ktlint:standard:indent")
                val fooWithIndentationFixingSuppressed: String =
                    "foo" +
                    "bar"
                val fooWithIndentationFixing2: String =
                    "foo" +
                        "bar"
                """.trimIndent()
            indentationRuleAssertThat(code).hasNoLintViolations()
        }

        @Test
        fun `Issue 631 - Given some code for which indentation is disabled with @Suppress on an element then do not fix indentation of that element only`() {
            val code =
                """
                val fooWithIndentationFixing1: String =
                    "foo" +
                        "bar"
                @Suppress("ktlint:standard:indent")
                val fooWithIndentationFixingSuppressed: String =
                    "foo" +
                    "bar"
                val fooWithIndentationFixing2: String =
                    "foo" +
                        "bar"
                """.trimIndent()
            indentationRuleAssertThat(code).hasNoLintViolations()
        }
    }

    @Test
    fun `Given a class prefixed with an annotation and another access modifier`() {
        val code =
            """
            @Foo("foo")
            public class Bar {
                fun bar() = "bar"
            }
            """.trimIndent()
        indentationRuleAssertThat(code).hasNoLintViolations()
    }

    @Nested
    inner class `Given a safe access expression` {
        @Test
        fun `Given a safe access expression as function parameter`() {
            val code =
                """
                val editorConfigDefaults = EditorConfigDefaults.load(
                    editorConfigPath
                        ?.expandTildeToFullPath()
                        ?.let { path -> Paths.get(path) },
                )
                """.trimIndent()
            indentationRuleAssertThat(code).hasNoLintViolations()
        }

        @Test
        fun `Given a nested safe access expression`() {
            val code =
                """
                val foo = bar
                    ?.filter { number ->
                        number == 0
                    }?.map { evenNumber ->
                        evenNumber * evenNumber
                    }?.map { evenNumber ->
                        evenNumber * evenNumber
                    }?.map { evenNumber ->
                        evenNumber * evenNumber
                    }
                """.trimIndent()
            indentationRuleAssertThat(code).hasNoLintViolations()
        }
    }

    @Test
    fun `Given an array`() {
        val code =
            """
            val foo = [
                "bar1",
                "bar2"
            ]
            """.trimIndent()
        indentationRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `Given a nested dot qualified expression`() {
        val code =
            """
            fun foo() =
                FooBar
                    .fooBar(
                        "foobar"
                    ).filter {
                        it == "bar"
                    }.associate { ruleSetProviderV1 ->
                        "bar"
                    }.also {
                        bar()
                    }
            """.trimIndent()
        indentationRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `Given some Kdoc or comment before annotation on secondary constructor`() {
        val code =
            """
            class Foo {
                /**
                 * Some comment
                 */
                @Bar
                public constructor(string: String) : this(string)

                /*
                 * Some comment
                 */
                @Bar
                public constructor(string: String) : this(string)

                // Some comment
                @Bar
                public constructor(string: String) : this(string)
            }
            """.trimIndent()
        indentationRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `Given a multiline binary with type`() {
        val code =
            """
            fun foo() {
                node.prevLeaf { it is PsiWhiteSpace && it.textContains('\n') } as
                    PsiWhiteSpace?
            }
            """.trimIndent()
        indentationRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `Given a multiline destructuring declaration`() {
        val code =
            """
            fun foobar() {
                val (foo, bar) =
                    Pair(
                        foo(),
                        bar()
                    )
            }
            """.trimIndent()
        indentationRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `Given a function with annotation of first parameter on same line as left parenthesis of function`() {
        val code =
            """
            class A {
                fun f(@Annotation
                    a: Any,
                    @Annotation(
                        [
                            "v1",
                            "v2"
                        ]
                    )
                    b: Any,
                    c: Any =
                        false,
                    @Annotation d: Any,
                    @SingleLineAnnotation([1, 2])
                    e: Any) {
                }
            }
            """.trimIndent()
        indentationRuleAssertThat(code).hasNoLintViolations()
    }

    @Nested
    inner class `Given a type parameter list` {
        @Test
        fun `Given type parameter list`() {
            val code =
                """
                public class Foo
                <Bar : String> {}
                """.trimIndent()
            indentationRuleAssertThat(code).hasNoLintViolations()
        }

        @Test
        fun `Given ktlint-official code style and a nested type parameter list`() {
            val code =
                """
                public class Foo<
                    Bar1 : String,
                    Bar2 : Map<
                        Int,
                        List<String>
                        >
                    > {}
                """.trimIndent()
            val formattedCode =
                """
                public class Foo<
                    Bar1 : String,
                    Bar2 : Map<
                        Int,
                        List<String>
                    >
                > {}
                """.trimIndent()
            indentationRuleAssertThat(code)
                .withEditorConfigOverride(CODE_STYLE_PROPERTY to ktlint_official)
                .isFormattedAs(formattedCode)
        }

        @ParameterizedTest(name = "CodeStyle: {0}")
        @EnumSource(
            value = CodeStyleValue::class,
            mode = EnumSource.Mode.EXCLUDE,
            names = ["ktlint_official"],
        )
        fun `Given non-ktlint-official code style and a nested type parameter list`(codeStyleValue: CodeStyleValue) {
            val code =
                """
                public class Foo<
                    Bar1 : String,
                    Bar2 : Map<
                        Int,
                        List<String>
                    >
                > {}
                """.trimIndent()
            val formattedCode =
                """
                public class Foo<
                    Bar1 : String,
                    Bar2 : Map<
                        Int,
                        List<String>
                        >
                    > {}
                """.trimIndent()
            indentationRuleAssertThat(code)
                .withEditorConfigOverride(CODE_STYLE_PROPERTY to codeStyleValue)
                .isFormattedAs(formattedCode)
        }
    }

    @Test
    fun `Given a multiline nullable function type between parenthesis`() {
        val code =
            """
            var foo: (
                () -> Unit
            )? = null
            """.trimIndent()
        indentationRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `Given a destructuring declaration`() {
        val code =
            """
            fun foo() {
                fun bar(): Pair<Int, Int> = Pair(1, 2)

                val (x, y) = bar()
                val (
                    x,
                    y
                ) = bar()
                val (
                    x,
                    y
                ) = bar()
                val (
                    x,
                    y
                ) =
                    bar()
            }
            """.trimIndent()
        indentationRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `Issue 1127 - Given a raw string literal followed by trimIndent in parameter list`() {
        val code =
            """
            interface UserRepository : JpaRepository<User, UUID> {
                @Query(
                    ${MULTILINE_STRING_QUOTE}
                    select u from User u
                    inner join Organization o on u.organization = o
                    where o = :organization
                ${MULTILINE_STRING_QUOTE}.trimIndent()
                )
                fun findByOrganization(organization: Organization, pageable: Pageable): Page<User>
            }
            """.trimIndent()
        val formattedCode =
            """
            interface UserRepository : JpaRepository<User, UUID> {
                @Query(
                    ${MULTILINE_STRING_QUOTE}
                    select u from User u
                    inner join Organization o on u.organization = o
                    where o = :organization
                    ${MULTILINE_STRING_QUOTE}.trimIndent()
                )
                fun findByOrganization(organization: Organization, pageable: Pageable): Page<User>
            }
            """.trimIndent()
        indentationRuleAssertThat(code)
            .isFormattedAs(formattedCode)
    }

    @Test
    fun `Given a for loop without braces around body`() {
        val code =
            """
            fun bar() {
                for (foo in fooList)
                    foo()
            }
            """.trimIndent()
        indentationRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `Given some comment preceded by a blank line`() {
        val code =
            """
            class Foo {

            //  Some comment
            }
            """.trimIndent()
        indentationRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `Given a property accessor preceded with an annotation or comment`() {
        val code =
            """
            var foo1: String
                @Internal
                get() = "foo"
                @Internal
                set(value) = "foo"
            var foo1: String
                // Some comment
                get() = "foo"
                /*
                 * Some comment
                 */
                set(value) = "foo"
            """.trimIndent()
        indentationRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `Given an object declaration with a super type list`() {
        val code =
            """
            class FooBar :
                Foo,
                Bar {
                // Do something
            }
            class Foo {
                companion object FooBar :
                    Foo,
                    Bar {
                    // Do something
                }
            }
            """.trimIndent()
        indentationRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `Issue 2257 - Given a class containing an object declaration having a super type with parameters`() {
        val code =
            """
            class Foo(
                foo: String,
            ) {
                object Bar : Baz(
                    baz = "baz",
                    bar = "bar",
                )
            }
            """.trimIndent()
        indentationRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `Given an if statement with multiple EOL comments between end of then and else`() {
        val code =
            """
            fun foo() {
                if (true) {
                    // Do something
                } // Some comment 1
                // Some comment 2
                else {
                    // Do something
                }
            }
            """.trimIndent()
        indentationRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `Given a type reference preceded by an annotation or comment`() {
        val code =
            """
            class FooBar :
                @Suppress("DEPRECATION")
                Foo,
                // Some comment
                Bar {
            }
            """.trimIndent()
        indentationRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `Given a nested type reference preceded by an annotation or comment`() {
        val code =
            """
            class KtLintMultiRule :
                @Suppress("DEPRECATION")
                Foo.bar {
            }
            """.trimIndent()
        indentationRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `Given a when statement with a multiline condition`() {
        val code =
            """
            fun foo() {
                return when (
                    bar()
                        .filter { it > 0 }
                        .min()
                ) {
                    1 -> "A"
                    2 -> "B"
                    else -> "C"
                }
            }
            """.trimIndent()
        indentationRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `Given an annotated expression`() {
        val code =
            """
            public fun Window.asCoroutineDispatcher(): CoroutineDispatcher =
                @Suppress("UnsafeCastFromDynamic")
                asDynamic().coroutineDispatcher ?: WindowDispatcher(this).also {
                    asDynamic().coroutineDispatcher = it
                }
            """.trimIndent()
        indentationRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `Issue 1644 - Given multiple nested brackets and some of them have EOL comments`() {
        val code =
            """
            fun fooBar() {
                fun foo() {
                    // some code
                } // some comment

                fun bar() {
                    // some code
                }
            }
            """.trimIndent()
        indentationRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `Issue 1563 - Given some code starting with a comment should not introduce a blank line before the comment`() {
        val code =
            """
            // Some comment or code element
            """.trimIndent()
        indentationRuleAssertThat(code).hasNoLintViolations()
    }

    // Should be resolved in IntelliJ IDEA default formatter:
    // https://youtrack.jetbrains.com/issue/KTIJ-14859/Too-little-indentation-inside-the-brackets-in-multiple-annotations-with-the-same-target
    @Test
    fun `Issue 1639 - Given an annotation entry within an annotation`() {
        val code =
            """
            package mypackage

            private annotation class A
            private annotation class B

            private class C {
                @field: [
                A
                B
                ]
                val a: Int = 42
            }
            """.trimIndent()
        indentationRuleAssertThat(code).hasNoLintViolations()
    }

    @Nested
    inner class `Issue 1639 - Context receivers` {
        @Test
        fun `Given a context receiver with multiple parameters`() {
            val code =
                """
                context(
                Foo,
                Bar
                )
                fun fooBar()
                """.trimIndent()
            val formattedCode =
                """
                context(
                    Foo,
                    Bar
                )
                fun fooBar()
                """.trimIndent()
            indentationRuleAssertThat(code)
                .hasLintViolations(
                    LintViolation(2, 1, "Unexpected indentation (0) (should be 4)"),
                    LintViolation(3, 1, "Unexpected indentation (0) (should be 4)"),
                ).isFormattedAs(formattedCode)
        }

        @Test
        fun `Given a context receiver with a parameter containing generic types with multiple parameters`() {
            val code =
                """
                context(
                FooBar<
                Foo,
                Bar
                >
                )
                fun fooBar()
                """.trimIndent()
            val formattedCode =
                """
                context(
                    FooBar<
                        Foo,
                        Bar
                    >
                )
                fun fooBar()
                """.trimIndent()
            indentationRuleAssertThat(code)
                .hasLintViolations(
                    LintViolation(2, 1, "Unexpected indentation (0) (should be 4)"),
                    LintViolation(3, 1, "Unexpected indentation (0) (should be 8)"),
                    LintViolation(4, 1, "Unexpected indentation (0) (should be 8)"),
                    LintViolation(5, 1, "Unexpected indentation (0) (should be 4)"),
                ).isFormattedAs(formattedCode)
        }

        @Test
        fun `Issue 1639 - Given a context receiver on a function having at least one modifier`() {
            // Formatting below conflicts with IntelliJ IDEA default formatting and need to be fixed in IntelliJ:
            // https://youtrack.jetbrains.com/issue/KTIJ-21072/Provide-proper-indentation-on-declarations-with-context-receivers
            val code =
                """
                context(Comparator<T>)
                public fun <T> T.compareTo(other: T) = compare(this, other)
                """.trimIndent()
            indentationRuleAssertThat(code).hasNoLintViolations()
        }
    }

    @Nested
    inner class `Given the ktlint_official code style is enabled` {
        @Test
        fun `Issue 1681 - Given a lambda having multiple arguments on different lines`() {
            val code =
                """
                val bar =
                    BarBarBarBar { paramA,
                                   paramB,
                                   paramC ->
                        Bar(paramA, paramB, paramC)
                    }
                """.trimIndent()
            val formattedCode =
                """
                val bar =
                    BarBarBarBar {
                        paramA,
                        paramB,
                        paramC ->
                        Bar(paramA, paramB, paramC)
                    }
                """.trimIndent()
            indentationRuleAssertThat(code)
                .addAdditionalRuleProvider { ParameterListWrappingRule() }
                .withEditorConfigOverride(CODE_STYLE_PROPERTY to ktlint_official)
                .hasLintViolationForAdditionalRule(2, 20, "Parameter should start on a newline")
                .isFormattedAs(formattedCode)
        }

        @Test
        fun `Issue 1540 - Given a DOT_QUALIFIED_EXPRESSION wrapped inside an ARRAY_ACCESS_EXPRESSION`() {
            val code =
                """
                val fooBar1 = foo
                    .bar {
                        "foobar"
                    }
                val fooBar2 = foo
                    .bar[0] {
                        "foobar"
                    }
                """.trimIndent()
            indentationRuleAssertThat(code)
                .withEditorConfigOverride(CODE_STYLE_PROPERTY to ktlint_official)
                .hasNoLintViolations()
        }

        @Test
        fun `Given a type which does not fit on the same line as the variable name`() {
            val code =
                """
                // $MAX_LINE_LENGTH_MARKER           $EOL_CHAR
                class Bar(
                    val foooooooooooooooooTooLong: Foo,
                    val foooooooooooooNotTooLong: Foo,
                )
                """.trimIndent()
            val formattedCode =
                """
                // $MAX_LINE_LENGTH_MARKER           $EOL_CHAR
                class Bar(
                    val foooooooooooooooooTooLong:
                        Foo,
                    val foooooooooooooNotTooLong: Foo,
                )
                """.trimIndent()
            indentationRuleAssertThat(code)
                .addAdditionalRuleProvider { ParameterWrappingRule() }
                .addAdditionalRuleProvider { MaxLineLengthRule() }
                .withEditorConfigOverride(CODE_STYLE_PROPERTY to ktlint_official)
                .setMaxLineLength()
                .isFormattedAs(formattedCode)
        }
    }

    @ParameterizedTest(name = "Description: {0}")
    @ValueSource(strings = ["ktlint_official", "android_studio"])
    fun `Issue 1217 & 3062 - Given a function parameter with a multiline expression starting on a new line`(
        codeStyleValue: CodeStyleValue,
    ) {
        val code =
            """
            val foo = foo(
                parameterName =
                "The quick brown fox "
                    .plus("jumps ")
                    .plus("over the lazy dog"),
            )
            """.trimIndent()
        val formattedCode =
            """
            val foo = foo(
                parameterName =
                    "The quick brown fox "
                        .plus("jumps ")
                        .plus("over the lazy dog"),
            )
            """.trimIndent()
        indentationRuleAssertThat(code)
            .withEditorConfigOverride(CODE_STYLE_PROPERTY to codeStyleValue)
            .hasLintViolations(
                LintViolation(3, 1, "Unexpected indentation (4) (should be 8)"),
                LintViolation(4, 1, "Unexpected indentation (8) (should be 12)"),
                LintViolation(5, 1, "Unexpected indentation (8) (should be 12)"),
            ).isFormattedAs(formattedCode)
    }

    @Nested
    inner class `Given a try-catch-finally` {
        @Test
        fun `Given a try catch finally which if properly formatted`() {
            val code =
                """
                fun foo() = try {
                    1
                } catch(_: Throwable) {
                    2
                } finally {
                    3
                }
                """.trimIndent()
            indentationRuleAssertThat(code).hasNoLintViolations()
        }

        @Test
        fun `Issue 1788 - Given a try catch finally where catch and finally starts on a new line`() {
            val code =
                """
                fun foo() = try {
                    1
                }
                catch(_: Throwable) {
                    2
                }
                finally {
                    3
                }
                """.trimIndent()
            indentationRuleAssertThat(code).hasNoLintViolations()
        }
    }

    @Test
    fun `Given a type alias with the value on a separate line`() {
        val code =
            """
            typealias FooBar =
                HashMap<Foo, Bar>
            """.trimIndent()
        indentationRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `Issue 1830 - Given a dot qualified expression followed by an safe access expression on the same line`() {
        val code =
            """
            private fun test(): Boolean? =
                runCatching { true }
                    .getOrNull()?.let { result ->
                        !result
                    }
            """.trimIndent()
        indentationRuleAssertThat(code).hasNoLintViolations()
    }

    @Nested
    inner class `Issue 1916, issue 2115 - Given a class declaration with an annotation before the constructor keyword` {
        @Test
        fun `Issue 1916, issue 2115 - Given indent explicit constructor is disabled`() {
            val code =
                """
                class Foo
                @Bar1 @Bar2
                constructor(
                    foo1: Foo1,
                    foo2: Foo2,
                ) {
                    fun foo() = "foo"
                }
                """.trimIndent()
            indentationRuleAssertThat(code)
                .withEditorConfigOverride(INDENT_EXPLICIT_CONSTRUCTOR_PROPERTY to false)
                .hasNoLintViolations()
        }

        @Test
        fun `Issue 1916, issue 2115 - Given indent explicit constructor is enabled`() {
            val code =
                """
                class Foo
                @Bar1 @Bar2
                constructor(
                    foo1: Foo1,
                    foo2: Foo2,
                ) {
                    fun foo() = "foo"
                }
                """.trimIndent()
            val formattedCode =
                """
                class Foo
                    @Bar1 @Bar2
                    constructor(
                        foo1: Foo1,
                        foo2: Foo2,
                    ) {
                        fun foo() = "foo"
                    }
                """.trimIndent()
            indentationRuleAssertThat(code)
                .withEditorConfigOverride(INDENT_EXPLICIT_CONSTRUCTOR_PROPERTY to true)
                .hasLintViolations(
                    LintViolation(2, 1, "Unexpected indentation (0) (should be 4)"),
                    LintViolation(3, 1, "Unexpected indentation (0) (should be 4)"),
                    LintViolation(4, 1, "Unexpected indentation (4) (should be 8)"),
                    LintViolation(5, 1, "Unexpected indentation (4) (should be 8)"),
                    LintViolation(6, 1, "Unexpected indentation (0) (should be 4)"),
                    LintViolation(7, 1, "Unexpected indentation (4) (should be 8)"),
                    LintViolation(8, 1, "Unexpected indentation (0) (should be 4)"),
                ).isFormattedAs(formattedCode)
        }
    }

    @Nested
    inner class `Issue 1916, issue 2115 - Given a class declaration with an annotation before the constructor and having a single super type` {
        @Test
        fun `Issue 1916, issue 2115 - Given indent explicit constructor is disabled`() {
            val code =
                """
                class Foo
                @Bar1 @Bar2
                constructor(
                    foo1: Foo1,
                    foo2: Foo2,
                ) : Foobar(
                        "foobar1",
                        "foobar2",
                    ) {
                    fun foo() = "foo"
                }
                """.trimIndent()
            indentationRuleAssertThat(code)
                .withEditorConfigOverride(INDENT_EXPLICIT_CONSTRUCTOR_PROPERTY to false)
                .hasNoLintViolations()
        }

        @Test
        fun `Issue 1916, issue 2115 - Given indent explicit constructor is enabled`() {
            val code =
                """
                class Foo
                @Bar1 @Bar2
                constructor(
                    foo1: Foo1,
                    foo2: Foo2,
                ) : Foobar(
                        "foobar1",
                        "foobar2",
                    ) {
                    fun foo() = "foo"
                }
                """.trimIndent()
            val formattedCode =
                """
                class Foo
                    @Bar1 @Bar2
                    constructor(
                        foo1: Foo1,
                        foo2: Foo2,
                    ) : Foobar(
                            "foobar1",
                            "foobar2",
                        ) {
                        fun foo() = "foo"
                    }
                """.trimIndent()
            indentationRuleAssertThat(code)
                .withEditorConfigOverride(INDENT_EXPLICIT_CONSTRUCTOR_PROPERTY to true)
                .isFormattedAs(formattedCode)
                .hasLintViolations(
                    LintViolation(2, 1, "Unexpected indentation (0) (should be 4)"),
                    LintViolation(3, 1, "Unexpected indentation (0) (should be 4)"),
                    LintViolation(4, 1, "Unexpected indentation (4) (should be 8)"),
                    LintViolation(5, 1, "Unexpected indentation (4) (should be 8)"),
                    LintViolation(6, 1, "Unexpected indentation (0) (should be 4)"),
                    LintViolation(7, 1, "Unexpected indentation (8) (should be 12)"),
                    LintViolation(8, 1, "Unexpected indentation (8) (should be 12)"),
                    LintViolation(9, 1, "Unexpected indentation (4) (should be 8)"),
                    LintViolation(10, 1, "Unexpected indentation (4) (should be 8)"),
                    LintViolation(11, 1, "Unexpected indentation (0) (should be 4)"),
                ).isFormattedAs(formattedCode)
        }
    }

    @Nested
    inner class `Issue 1916, issue 2115 - Given a class declaration with an annotation before the constructor and having multiple super types` {
        @Test
        fun `Issue 1916, issue 2115 - Given indent explicit constructor is disabled`() {
            val code =
                """
                class Foo
                @Bar1 @Bar2
                constructor(
                    foo1: Foo1,
                    foo2: Foo2,
                ) : Foobar1(
                        "foobar1",
                        "foobar2",
                    ),
                    FooBar2,
                    FooBar3 {
                    fun foo() = "foo"
                }
                """.trimIndent()
            indentationRuleAssertThat(code)
                .withEditorConfigOverride(INDENT_EXPLICIT_CONSTRUCTOR_PROPERTY to false)
                .hasNoLintViolations()
        }

        @Test
        fun `Issue 1916, issue 2115 - Given indent explicit constructor is enabled`() {
            val code =
                """
                class Foo
                @Bar1 @Bar2
                constructor(
                    foo1: Foo1,
                    foo2: Foo2,
                ) : Foobar1(
                        "foobar1",
                        "foobar2",
                    ),
                    FooBar2,
                    FooBar3 {
                    fun foo() = "foo"
                }
                """.trimIndent()
            val formattedCode =
                """
                class Foo
                    @Bar1 @Bar2
                    constructor(
                        foo1: Foo1,
                        foo2: Foo2,
                    ) : Foobar1(
                            "foobar1",
                            "foobar2",
                        ),
                        FooBar2,
                        FooBar3 {
                        fun foo() = "foo"
                    }
                """.trimIndent()
            indentationRuleAssertThat(code)
                .withEditorConfigOverride(INDENT_EXPLICIT_CONSTRUCTOR_PROPERTY to true)
                .isFormattedAs(formattedCode)
                .hasLintViolations(
                    LintViolation(2, 1, "Unexpected indentation (0) (should be 4)"),
                    LintViolation(3, 1, "Unexpected indentation (0) (should be 4)"),
                    LintViolation(4, 1, "Unexpected indentation (4) (should be 8)"),
                    LintViolation(5, 1, "Unexpected indentation (4) (should be 8)"),
                    LintViolation(6, 1, "Unexpected indentation (0) (should be 4)"),
                    LintViolation(7, 1, "Unexpected indentation (8) (should be 12)"),
                    LintViolation(8, 1, "Unexpected indentation (8) (should be 12)"),
                    LintViolation(9, 1, "Unexpected indentation (4) (should be 8)"),
                    LintViolation(10, 1, "Unexpected indentation (4) (should be 8)"),
                    LintViolation(11, 1, "Unexpected indentation (4) (should be 8)"),
                    LintViolation(12, 1, "Unexpected indentation (4) (should be 8)"),
                    LintViolation(13, 1, "Unexpected indentation (0) (should be 4)"),
                ).isFormattedAs(formattedCode)
        }

        @Test
        fun `Issue 2115 - Given a class without an explicit constructor and with a multiline super type call entry then do not indent the class body`() {
            val code =
                """
                class Foo :
                    FooBar(
                        "bar1",
                        "bar2",
                    ) {
                    // body
                }
                """.trimIndent()
            indentationRuleAssertThat(code)
                .withEditorConfigOverride(CODE_STYLE_PROPERTY to ktlint_official)
                .hasNoLintViolations()
        }

        @Test
        fun `Issue 2115 - Given a class without an explicit constructor and with a long super type list then do not indent the class body`() {
            val code =
                """
                class Foo1(
                    val bar1: Bar,
                    val bar2: Bar,
                ) : BarFoo1,
                    BarFoo2 {
                    // body
                }
                """.trimIndent()
            indentationRuleAssertThat(code)
                .withEditorConfigOverride(CODE_STYLE_PROPERTY to ktlint_official)
                .hasNoLintViolations()
        }

        @Test
        fun `Given a class preceded by a multiline comment and with an explicit constructor on same line as class keyword`() {
            val code =
                """
                /**
                 *  Some kdoc
                 */
                class Foo internal constructor(
                    private val bar: Bar
                ): FooBar {
                    fun bar()
                }
                """.trimIndent()
            indentationRuleAssertThat(code)
                .hasNoLintViolations()
        }
    }

    @Test
    fun `Given an elvis operator followed by a multiline expression`() {
        val code =
            """
            fun fooBar(foo: String?, bar: String) =
                foo
                    ?.lowercase()
                    ?: bar
                        .uppercase()
                        .trimIndent()
            """.trimIndent()
        indentationRuleAssertThat(code).hasNoLintViolations()
    }

    @Nested
    inner class `Given a function with raw string literal as result` {
        @Test
        fun `As body expression on next line`() {
            val code =
                """
                private fun foo( bar: String) =
                    $MULTILINE_STRING_QUOTE
                    bar
                    $MULTILINE_STRING_QUOTE.trimIndent()
                """.trimIndent()
            indentationRuleAssertThat(code).hasNoLintViolations()
        }

        @Nested
        inner class `Given non-ktlint_official code style` {
            private val nonKtlintOfficialCodeStyle = CodeStyleValue.android_studio

            @Test
            fun `As body expression on same line as equals and preceded by space`() {
                val code =
                    """
                    private fun foo(
                        bar: String,
                    ) = $MULTILINE_STRING_QUOTE
                        bar
                    $MULTILINE_STRING_QUOTE.trimIndent()
                    """.trimIndent()
                indentationRuleAssertThat(code)
                    .withEditorConfigOverride(CODE_STYLE_PROPERTY to nonKtlintOfficialCodeStyle)
                    .hasNoLintViolations()
            }

            @Test
            fun `As body expression on same line as equals but not preceded by space`() {
                val code =
                    """
                    private fun foo(
                        bar: String,
                    ) =$MULTILINE_STRING_QUOTE
                        bar
                    $MULTILINE_STRING_QUOTE.trimIndent()
                    """.trimIndent()
                indentationRuleAssertThat(code)
                    .withEditorConfigOverride(CODE_STYLE_PROPERTY to nonKtlintOfficialCodeStyle)
                    .hasNoLintViolations()
            }

            @Test
            fun `As block body`() {
                val code =
                    """
                    private fun foo( bar: String): String {
                        return $MULTILINE_STRING_QUOTE
                            bar
                        $MULTILINE_STRING_QUOTE.trimIndent()
                    }
                    """.trimIndent()
                indentationRuleAssertThat(code)
                    .withEditorConfigOverride(CODE_STYLE_PROPERTY to nonKtlintOfficialCodeStyle)
                    .hasNoLintViolations()
            }

            @Test
            fun `As body expression of function wrapped in class`() {
                val code =
                    """
                    class Bar {
                        private fun foo(
                            bar: String,
                        ) = $MULTILINE_STRING_QUOTE
                            bar
                        $MULTILINE_STRING_QUOTE.trimIndent()
                    }
                    """.trimIndent()
                indentationRuleAssertThat(code)
                    .withEditorConfigOverride(CODE_STYLE_PROPERTY to nonKtlintOfficialCodeStyle)
                    .hasNoLintViolations()
            }
        }

        @Nested
        inner class `Given ktlint_official code style` {
            @Test
            fun `As body expression on same line as equals and preceded by space`() {
                val code =
                    """
                    private fun foo(
                        bar: String,
                    ) = $MULTILINE_STRING_QUOTE
                        bar
                        $MULTILINE_STRING_QUOTE.trimIndent()
                    """.trimIndent()
                indentationRuleAssertThat(code)
                    .withEditorConfigOverride(CODE_STYLE_PROPERTY to ktlint_official)
                    .hasNoLintViolations()
            }

            @Test
            fun `As body expression on same line as equals but not preceded by space`() {
                val code =
                    """
                    private fun foo(
                        bar: String,
                    ) =$MULTILINE_STRING_QUOTE
                        bar
                        $MULTILINE_STRING_QUOTE.trimIndent()
                    """.trimIndent()
                indentationRuleAssertThat(code)
                    .withEditorConfigOverride(CODE_STYLE_PROPERTY to ktlint_official)
                    .hasNoLintViolations()
            }

            @Test
            fun `As block body`() {
                val code =
                    """
                    private fun foo( bar: String): String {
                        return $MULTILINE_STRING_QUOTE
                            bar
                            $MULTILINE_STRING_QUOTE.trimIndent()
                    }
                    """.trimIndent()
                indentationRuleAssertThat(code)
                    .withEditorConfigOverride(CODE_STYLE_PROPERTY to ktlint_official)
                    .hasNoLintViolations()
            }

            @Test
            fun `As body expression of function wrapped in class`() {
                val code =
                    """
                    class Bar {
                        private fun foo(
                            bar: String,
                        ) = $MULTILINE_STRING_QUOTE
                            bar
                            $MULTILINE_STRING_QUOTE.trimIndent()
                    }
                    """.trimIndent()
                indentationRuleAssertThat(code)
                    .withEditorConfigOverride(CODE_STYLE_PROPERTY to ktlint_official)
                    .hasNoLintViolations()
            }
        }
    }

    @Test
    fun `Issue 1993 - An or operator at start of line followed by a dot qualified expressions should not throw an exception`() {
        val code =
            """
            val foo =
                if (false
                    || foobar.bar()
                ) {
                    // Do something
                }
            """.trimIndent()
        val formattedCode =
            """
            val foo =
                if (false ||
                    foobar.bar()
                ) {
                    // Do something
                }
            """.trimIndent()
        indentationRuleAssertThat(code)
            .addAdditionalRuleProvider { ChainWrappingRule() }
            .hasLintViolationForAdditionalRule(3, 9, "Line must not begin with \"||\"")
            .isFormattedAs(formattedCode)
    }

    @Test
    fun `Issue 920 - Given ktlint_official codestyle and a PARENTHESIZED expression`() {
        val code =
            """
            val foobar =
                if (true) {
                    (
                        foo()
                    )
                } else {
                    bar()
                }
            """.trimIndent()
        indentationRuleAssertThat(code)
            .withEditorConfigOverride(CODE_STYLE_PROPERTY to ktlint_official)
            .hasNoLintViolations()
    }

    @Test
    fun `Issue 2094 - Given a malformed IS_EXPRESSION`() {
        val code =
            """
            fun foo(any: Any) =
                any is
                Foo
            """.trimIndent()
        val formattedCode =
            """
            fun foo(any: Any) =
                any is
                    Foo
            """.trimIndent()
        indentationRuleAssertThat(code)
            .hasLintViolation(3, 1, "Unexpected indentation (4) (should be 8)")
            .isFormattedAs(formattedCode)
    }

    @Test
    fun `Issue 2094 - Given a malformed PREFIX_EXPRESSION`() {
        val code =
            """
            fun foo(value: Int) =
                ++
                value
            """.trimIndent()
        val formattedCode =
            """
            fun foo(value: Int) =
                ++
                    value
            """.trimIndent()
        indentationRuleAssertThat(code)
            .hasLintViolation(3, 1, "Unexpected indentation (4) (should be 8)")
            .isFormattedAs(formattedCode)
    }

    @Test
    fun `Issue 2094 - Given a malformed POSTFIX_EXPRESSION`() {
        val code =
            """
            fun foo(value: Int) =
                --
                value
            """.trimIndent()
        val formattedCode =
            """
            fun foo(value: Int) =
                --
                    value
            """.trimIndent()
        indentationRuleAssertThat(code)
            .hasLintViolation(3, 1, "Unexpected indentation (4) (should be 8)")
            .isFormattedAs(formattedCode)
    }

    @Test
    fun `Given indent explicit constructor is enabled, a class with an explicit constructor and a super type entry`() {
        val code =
            """
            class Foo
            constructor(
                private val bar: Bar,
            ) : FooBar {
                fun baz()
            }
            """.trimIndent()
        val formattedCode =
            """
            class Foo
                constructor(
                    private val bar: Bar,
                ) : FooBar {
                    fun baz()
                }
            """.trimIndent()
        indentationRuleAssertThat(code)
            .withEditorConfigOverride(INDENT_EXPLICIT_CONSTRUCTOR_PROPERTY to true)
            .isFormattedAs(formattedCode)
            .hasLintViolations(
                LintViolation(2, 1, "Unexpected indentation (0) (should be 4)"),
                LintViolation(3, 1, "Unexpected indentation (4) (should be 8)"),
                LintViolation(4, 1, "Unexpected indentation (0) (should be 4)"),
                LintViolation(5, 1, "Unexpected indentation (4) (should be 8)"),
                LintViolation(6, 1, "Unexpected indentation (0) (should be 4)"),
            ).isFormattedAs(formattedCode)
    }

    @Test
    fun `Given explicit constructor indent is enabled, a class with an explicit constructor and multiple super types entry`() {
        val code =
            """
            class Foo
            constructor(
                private val bar: Bar
            ) : FooBar1,
                FooBar2 {
                fun baz()
            }
            """.trimIndent()
        val formattedCode =
            """
            class Foo
                constructor(
                    private val bar: Bar
                ) : FooBar1,
                    FooBar2 {
                    fun baz()
                }
            """.trimIndent()
        indentationRuleAssertThat(code)
            .withEditorConfigOverride(INDENT_EXPLICIT_CONSTRUCTOR_PROPERTY to true)
            .isFormattedAs(formattedCode)
            .hasLintViolations(
                LintViolation(2, 1, "Unexpected indentation (0) (should be 4)"),
                LintViolation(3, 1, "Unexpected indentation (4) (should be 8)"),
                LintViolation(4, 1, "Unexpected indentation (0) (should be 4)"),
                LintViolation(5, 1, "Unexpected indentation (4) (should be 8)"),
                LintViolation(6, 1, "Unexpected indentation (4) (should be 8)"),
                LintViolation(7, 1, "Unexpected indentation (0) (should be 4)"),
            ).isFormattedAs(formattedCode)
    }

    @Test
    fun `Given a class with a comment before the super type list`() {
        val code =
            """
            class Foo(
                bar: Bar,
            ) : // Some comment
                @Unused
                FooBar()
            """.trimIndent()
        indentationRuleAssertThat(code).hasNoLintViolations()
    }

    @Nested
    inner class `Issue 2808 - Indent arrow in when-entry when starting on newline, and the arrow is followed by a statement` {
        val code =
            // Default IDEA formatting in version '2024.1.6'
            """
            val foo =
                when (bar()) {
                    is Bar1
                    -> "bar1"

                    is Bar2
                    -> "bar2"
                        .also { println(it) }

                    else
                    ->
                        "bar3"
                            .also { println(it) }
                }
            """.trimIndent()

        @ParameterizedTest(name = "CodeStyle: {0}")
        @EnumSource(CodeStyleValue::class)
        fun `Issue 2808 - Given property 'ij_kotlin_indent_before_arrow_on_new_line' is disabled`(codeStyleValue: CodeStyleValue) {
            indentationRuleAssertThat(code)
                .withEditorConfigOverride(CODE_STYLE_PROPERTY to codeStyleValue)
                .withEditorConfigOverride(IndentationRule.INDENT_WHEN_ARROW_ON_NEW_LINE to false)
                .hasNoLintViolations()
        }

        @ParameterizedTest(name = "CodeStyle: {0}")
        @EnumSource(CodeStyleValue::class)
        fun `Issue 2808 - Given property 'ij_kotlin_indent_before_arrow_on_new_line' is enabled`(codeStyleValue: CodeStyleValue) {
            val formattedCode =
                """
                val foo =
                    when (bar()) {
                        is Bar1
                            -> "bar1"

                        is Bar2
                            -> "bar2"
                                .also { println(it) }

                        else
                            ->
                            "bar3"
                                .also { println(it) }
                    }
                """.trimIndent()

            indentationRuleAssertThat(code)
                .withEditorConfigOverride(CODE_STYLE_PROPERTY to codeStyleValue)
                .withEditorConfigOverride(IndentationRule.INDENT_WHEN_ARROW_ON_NEW_LINE to true)
                .hasLintViolations(
                    LintViolation(4, 1, "Unexpected indentation (8) (should be 12)"),
                    LintViolation(7, 1, "Unexpected indentation (8) (should be 12)"),
                    LintViolation(8, 1, "Unexpected indentation (12) (should be 16)"),
                    LintViolation(11, 1, "Unexpected indentation (8) (should be 12)"),
                ).isFormattedAs(formattedCode)
        }

        @ParameterizedTest(name = "CodeStyle: {0}")
        @EnumSource(CodeStyleValue::class)
        fun `Issue 2808 - Given non-ktlint_official code style and property 'ij_kotlin_indent_before_arrow_on_new_line' not set`(
            codeStyleValue: CodeStyleValue,
        ) {
            indentationRuleAssertThat(code)
                .withEditorConfigOverride(CODE_STYLE_PROPERTY to codeStyleValue)
                .hasNoLintViolations()
        }
    }

    @Nested
    inner class `Issue 2808 - Indent arrow in when-entry when starting on newline, and the arrow is followed by a block body` {
        val code =
            // Default IDEA formatting in version '2024.1.6'
            """
            val foo =
                when (bar()) {
                    is Bar1
                    -> {
                        "bar1"
                    }

                    is Bar2
                    -> {
                        "bar2"
                            .also { println(it) }
                    }

                    else
                    -> {
                        "bar3"
                            .also { println(it) }
                    }
                }
            """.trimIndent()

        @ParameterizedTest(name = "CodeStyle: {0}")
        @EnumSource(CodeStyleValue::class)
        fun `Issue 2808 - Given property 'ij_kotlin_indent_before_arrow_on_new_line' is disabled`(codeStyleValue: CodeStyleValue) {
            indentationRuleAssertThat(code)
                .withEditorConfigOverride(CODE_STYLE_PROPERTY to codeStyleValue)
                .withEditorConfigOverride(IndentationRule.INDENT_WHEN_ARROW_ON_NEW_LINE to false)
                .hasNoLintViolations()
        }

        @ParameterizedTest(name = "CodeStyle: {0}")
        @EnumSource(
            value = CodeStyleValue::class,
            mode = EnumSource.Mode.EXCLUDE,
            names = ["ktlint_official"],
        )
        fun `Issue 2808 - Given non-ktlint_official code style, and property 'ij_kotlin_indent_before_arrow_on_new_line' is enabled`(
            codeStyleValue: CodeStyleValue,
        ) {
            val formattedCode =
                // Formatting below is used by IDEA version '2024.2' or higher
                """
                val foo =
                    when (bar()) {
                        is Bar1
                            -> {
                            "bar1"
                        }

                        is Bar2
                            -> {
                            "bar2"
                                .also { println(it) }
                        }

                        else
                            -> {
                            "bar3"
                                .also { println(it) }
                        }
                    }
                """.trimIndent()

            indentationRuleAssertThat(code)
                .withEditorConfigOverride(CODE_STYLE_PROPERTY to codeStyleValue)
                .withEditorConfigOverride(IndentationRule.INDENT_WHEN_ARROW_ON_NEW_LINE to true)
                .hasLintViolations(
                    LintViolation(4, 1, "Unexpected indentation (8) (should be 12)"),
                    LintViolation(9, 1, "Unexpected indentation (8) (should be 12)"),
                    LintViolation(15, 1, "Unexpected indentation (8) (should be 12)"),
                ).isFormattedAs(formattedCode)
        }

        @Test
        fun `Issue 2808 - Given ktlint_official code style, and property 'ij_kotlin_indent_before_arrow_on_new_line' is enabled`() {
            val formattedCode =
                // Formatting below is used by IDEA version '2024.2' or higher
                """
                val foo =
                    when (bar()) {
                        is Bar1
                            -> {
                                "bar1"
                            }

                        is Bar2
                            -> {
                                "bar2"
                                    .also { println(it) }
                            }

                        else
                            -> {
                                "bar3"
                                    .also { println(it) }
                            }
                    }
                """.trimIndent()

            indentationRuleAssertThat(code)
                .withEditorConfigOverride(CODE_STYLE_PROPERTY to ktlint_official)
                .withEditorConfigOverride(IndentationRule.INDENT_WHEN_ARROW_ON_NEW_LINE to true)
                .hasLintViolations(
                    LintViolation(4, 1, "Unexpected indentation (8) (should be 12)"),
                    LintViolation(5, 1, "Unexpected indentation (12) (should be 16)"),
                    LintViolation(6, 1, "Unexpected indentation (8) (should be 12)"),
                    LintViolation(9, 1, "Unexpected indentation (8) (should be 12)"),
                    LintViolation(10, 1, "Unexpected indentation (12) (should be 16)"),
                    LintViolation(11, 1, "Unexpected indentation (16) (should be 20)"),
                    LintViolation(12, 1, "Unexpected indentation (8) (should be 12)"),
                    LintViolation(15, 1, "Unexpected indentation (8) (should be 12)"),
                    LintViolation(16, 1, "Unexpected indentation (12) (should be 16)"),
                    LintViolation(17, 1, "Unexpected indentation (16) (should be 20)"),
                    LintViolation(18, 1, "Unexpected indentation (8) (should be 12)"),
                ).isFormattedAs(formattedCode)
        }

        @ParameterizedTest(name = "CodeStyle: {0}")
        @EnumSource(
            value = CodeStyleValue::class,
            mode = EnumSource.Mode.EXCLUDE,
            names = ["ktlint_official"],
        )
        fun `Issue 2808 - Given property 'ij_kotlin_indent_before_arrow_on_new_line' not set`(codeStyleValue: CodeStyleValue) {
            indentationRuleAssertThat(code)
                .withEditorConfigOverride(CODE_STYLE_PROPERTY to codeStyleValue)
                .hasNoLintViolations()
        }
    }

    @ParameterizedTest(name = "Suppression: {0}")
    @ValueSource(
        strings = [
            "ktlint",
            "ktlint:standard:indent",
        ],
    )
    fun `Issue 3109 - Suppressing all ktlint violations may not result in failing to empty the stack`(suppression: String) {
        val code =
            """
            fun foo() =
                @Suppress("${suppression}")
                listOf("foo")
            """.trimIndent()

        indentationRuleAssertThat(code).hasNoLintViolations()
    }

    private companion object {
        val INDENT_STYLE_TAB =
            INDENT_STYLE_PROPERTY to PropertyType.IndentStyleValue.tab
    }
}
