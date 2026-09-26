package io.github.ktlint.core.rule.engine.internal

import assertk.assertThat
import assertk.assertions.isEqualTo
import io.github.ktlint.core.rule.engine.api.Code
import io.github.ktlint.core.rule.engine.api.KtLintRuleEngine
import io.github.ktlint.core.rule.engine.core.api.RuleId
import io.github.ktlint.core.rule.engine.core.api.RuleV2
import io.github.ktlint.core.rule.engine.core.api.RuleV2Provider
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class KtlintSuppressionKtTest {
    @Nested
    inner class `Given a file suppression to be inserted` {
        @Test
        fun `Given a suppression to be inserted on a package statement`() {
            val code =
                """
                package foo.foo_bar
                """.trimIndent()
            val formattedCode =
                """
                @file:Suppress("ktlint:standard:package-name")

                package foo.foo_bar
                """.trimIndent()
            val actual =
                code
                    .atOffset(1, 1)
                    .insertKtlintRuleSuppression(setOf("ktlint:standard:package-name"))

            assertThat(actual).isEqualTo(formattedCode)
        }

        @Test
        fun `Given a suppression to be inserted on an import statement`() {
            val code =
                """
                import foo.*
                """.trimIndent()
            val formattedCode =
                """
                @file:Suppress("ktlint:standard:no-wildcard-imports")

                import foo.*
                """.trimIndent()
            val actual =
                code
                    .atOffset(1, 1)
                    .insertKtlintRuleSuppression(setOf("ktlint:standard:no-wildcard-imports"))

            assertThat(actual).isEqualTo(formattedCode)
        }

        @Test
        fun `Given code with a file suppression, but no suppression ids, and an import statement then insert a file suppression`() {
            val code =
                """
                @file:Suppress

                import foo.*
                """.trimIndent()
            val formattedCode =
                """
                @file:Suppress("ktlint:standard:no-wildcard-imports")

                import foo.*
                """.trimIndent()
            val actual =
                code
                    .atOffset(3, 1)
                    .insertKtlintRuleSuppression(setOf("ktlint:standard:no-wildcard-imports"))

            assertThat(actual).isEqualTo(formattedCode)
        }

        @Test
        fun `Given code with a file suppression already containing the suppression id then not add that same suppression id again`() {
            val code =
                """
                @file:Suppress("ktlint:standard:no-wildcard-imports")

                import foo.*
                """.trimIndent()
            val actual =
                code
                    .atOffset(3, 1)
                    .insertKtlintRuleSuppression(setOf("ktlint:standard:no-wildcard-imports"))

            assertThat(actual).isEqualTo(code)
        }

        @Test
        fun `Given code with a file suppression not containing any suppression id lexicograhically bigger than the new id then add it as last element`() {
            val code =
                """
                @file:Suppress("aaa")

                import foo.*
                """.trimIndent()
            val formattedCode =
                """
                @file:Suppress("aaa", "ktlint:standard:no-wildcard-imports")

                import foo.*
                """.trimIndent()
            val actual =
                code
                    .atOffset(3, 1)
                    .insertKtlintRuleSuppression(setOf("ktlint:standard:no-wildcard-imports"))

            assertThat(actual).isEqualTo(formattedCode)
        }

        @Test
        fun `Given code with a file suppression not containing any suppression id lexicograhically smaller than the new id then add it as first element`() {
            val code =
                """
                @file:Suppress("zzz")

                import foo.*
                """.trimIndent()
            val formattedCode =
                """
                @file:Suppress("ktlint:standard:no-wildcard-imports", "zzz")

                import foo.*
                """.trimIndent()
            val actual =
                code
                    .atOffset(3, 1)
                    .insertKtlintRuleSuppression(setOf("ktlint:standard:no-wildcard-imports"))

            assertThat(actual).isEqualTo(formattedCode)
        }

        @Test
        fun `Given code with a file suppression having unsorted ids, and the new suppression id is lexicograhically in between other elements`() {
            val code =
                """
                @file:Suppress("zzz", "aaa")

                import foo.*
                """.trimIndent()
            val formattedCode =
                """
                @file:Suppress("aaa", "ktlint:standard:no-wildcard-imports", "zzz")

                import foo.*
                """.trimIndent()
            val actual =
                code
                    .atOffset(3, 1)
                    .insertKtlintRuleSuppression(setOf("ktlint:standard:no-wildcard-imports"))

            assertThat(actual).isEqualTo(formattedCode)
        }

        @Test
        fun `Given code with a copyright comment before the package statement, then insert the suppression below the copyright comment`() {
            val code =
                """
                /* Some copyright notice before package statement */
                package foobar

                import foo.*
                """.trimIndent()
            val formattedCode =
                """
                /* Some copyright notice before package statement */
                @file:Suppress("ktlint:standard:no-wildcard-imports")

                package foobar

                import foo.*
                """.trimIndent()
            val actual =
                code
                    .atOffset(4, 1)
                    .insertKtlintRuleSuppression(setOf("ktlint:standard:no-wildcard-imports"))

            assertThat(actual).isEqualTo(formattedCode)
        }

        @Nested
        inner class `Given that all rules for entire file have to be disabled` {
            @Test
            fun `Given that no file annotation is defined`() {
                val code =
                    """
                    import foo.*
                    """.trimIndent()
                val formattedCode =
                    """
                    @file:Suppress("ktlint")

                    import foo.*
                    """.trimIndent()
                val actual =
                    code
                        .atOffset(1, 1)
                        .insertKtlintRuleSuppression(setOf("ktlint"), true)

                assertThat(actual).isEqualTo(formattedCode)
            }

            @Test
            fun `Given that a file annotation is defined then remove ktlint suppression ids only as they become redundant`() {
                val code =
                    """
                    @file:Suppress("ktlint:standard:no-wildcard-imports", "unused")

                    import foo.*
                    """.trimIndent()
                val formattedCode =
                    """
                    @file:Suppress("ktlint", "unused")

                    import foo.*
                    """.trimIndent()
                val actual =
                    code
                        .atOffset(1, 1)
                        .insertKtlintRuleSuppression(setOf("ktlint"), true)

                assertThat(actual).isEqualTo(formattedCode)
            }
        }
    }

    @Test
    fun `Given a top level declaration at which a suppression is to be added`() {
        val code =
            """
            val foo = "Foo"
            """.trimIndent()
        val formattedCode =
            """
            @Suppress("ktlint:standard:some-rule-id")
            val foo = "Foo"
            """.trimIndent()
        val actual =
            code
                .atOffset(1, 1)
                .insertKtlintRuleSuppression(setOf("ktlint:standard:some-rule-id"))

        assertThat(actual).isEqualTo(formattedCode)
    }

    @Nested
    inner class `Given an element annotated with @SuppressWarnings` {
        @Test
        fun `Given a ktlint suppression then add it to the existing SuppressWarnings and sort all suppressions alphabetically`() {
            val code =
                """
                @SuppressWarnings("zzz", "aaa")
                val foo = "foo"
                """.trimIndent()
            val formattedCode =
                """
                @SuppressWarnings("aaa", "ktlint:standard:foo", "zzz")
                val foo = "foo"
                """.trimIndent()
            val actual =
                code
                    .atOffset(2, 1)
                    .insertKtlintRuleSuppression(setOf("ktlint:standard:foo"))

            assertThat(actual).isEqualTo(formattedCode)
        }

        @Test
        fun `Given the target element is already annotated with both @Suppress and @SuppressWarnings then add the ktlint suppression to the @Suppress`() {
            val code =
                """
                @Suppress("aaa", "zzz")
                @SuppressWarnings("bbb", "yyy")
                val foo = "foo"
                """.trimIndent()
            val formattedCode =
                """
                @Suppress("aaa", "ktlint:standard:foo", "zzz")
                @SuppressWarnings("bbb", "yyy")
                val foo = "foo"
                """.trimIndent()
            val actual =
                code
                    .atOffset(3, 1)
                    .insertKtlintRuleSuppression(setOf("ktlint:standard:foo"))

            assertThat(actual).isEqualTo(formattedCode)
        }
    }

    @Test
    fun `Given an init block comment to which an suppression is being added`() {
        val code =
            """
            class Foo() {
                var foo: String
                var bar: String

                init {
                    foo = "foo"
                }
            }
            """.trimIndent()
        val formattedCode =
            """
            @Suppress("ktlint:standard:foo")
            class Foo() {
                var foo: String
                var bar: String

                init {
                    foo = "foo"
                }
            }
            """.trimIndent()
        val actual =
            code
                .atOffset(6, 1)
                .insertKtlintRuleSuppression(setOf("ktlint:standard:foo"))

        assertThat(actual).isEqualTo(formattedCode)
    }

    @Test
    fun `Given a setter on which a suppression is added`() {
        val code =
            """
            class Foo {
                var foo: Int = 1
                    set(value) {
                        field = value
                    }
            }
            """.trimIndent()
        val formattedCode =
            """
            class Foo {
                var foo: Int = 1
                    @Suppress("ktlint:standard:foo")
                    set(value) {
                        field = value
                    }
            }
            """.trimIndent()
        val actual =
            code
                .atOffset(4, 1)
                .insertKtlintRuleSuppression(setOf("ktlint:standard:foo"))

        assertThat(actual).isEqualTo(formattedCode)
    }

    @Test
    fun `Given a primary constructor on which a suppression is added`() {
        val code =
            """
            class Foo constructor(bar: Bar) {
                // foo
            }
            """.trimIndent()
        val formattedCode =
            """
            @Suppress("ktlint:standard:foo")
            class Foo constructor(bar: Bar) {
                // foo
            }
            """.trimIndent()
        val actual =
            code
                .atOffset(2, 8)
                .insertKtlintRuleSuppression(setOf("ktlint:standard:foo"))

        assertThat(actual).isEqualTo(formattedCode)
    }

    @Nested
    inner class `Given a type argument list` {
        @Test
        fun `Given a type argument on which a suppression is added`() {
            val code =
                """
                fun FooBar<
                    in FOO,
                    in BAR
                    >.foo(foo: FOO, bar: BAR) {}
                """.trimIndent()
            val formattedCode =
                """
                fun FooBar<
                    @Suppress("ktlint:standard:foo")
                    in FOO,
                    in BAR
                    >.foo(foo: FOO, bar: BAR) {}
                """.trimIndent()
            val actual =
                code
                    .atOffset(2, 5)
                    .insertKtlintRuleSuppression(setOf("ktlint:standard:foo"))

            assertThat(actual).isEqualTo(formattedCode)
        }

        @Test
        fun `Given a type argument on which a suppression is added on the comma or non-code leaf in the type argument list`() {
            val code =
                """
                fun FooBar<
                    in FOO,
                    in BAR
                    >.foo(foo: FOO, bar: BAR) {}
                """.trimIndent()
            val formattedCode =
                """
                fun FooBar<
                    in FOO,
                    @Suppress("ktlint:standard:foo")
                    in BAR
                    >.foo(foo: FOO, bar: BAR) {}
                """.trimIndent()
            val actual =
                code
                    .atOffset(2, 11)
                    .also { require(it.charAtOffset() == ',') }
                    .insertKtlintRuleSuppression(setOf("ktlint:standard:foo"))

            assertThat(actual).isEqualTo(formattedCode)
        }
    }

    @Nested
    inner class `Given a type parameter list` {
        @Test
        fun `Given a type parameter on which a suppression is added`() {
            val code =
                """
                fun <
                    FOO,
                    BAR,
                    > foobar(foo: FOO, bar: BAR) = "foo"
                """.trimIndent()
            val formattedCode =
                """
                fun <
                    @Suppress("ktlint:standard:foo")
                    FOO,
                    BAR,
                    > foobar(foo: FOO, bar: BAR) = "foo"
                """.trimIndent()
            val actual =
                code
                    .atOffset(2, 5)
                    .insertKtlintRuleSuppression(setOf("ktlint:standard:foo"))

            assertThat(actual).isEqualTo(formattedCode)
        }

        @Test
        fun `Given a type parameter on which a suppression is added on the comma or non-code leaf in the type parameter list`() {
            val code =
                """
                fun <
                    FOO,
                    BAR,
                    > foobar(foo: FOO, bar: BAR) = "foo"
                """.trimIndent()
            val formattedCode =
                """
                fun <
                    FOO,
                    @Suppress("ktlint:standard:foo")
                    BAR,
                    > foobar(foo: FOO, bar: BAR) = "foo"
                """.trimIndent()
            val actual =
                code
                    .atOffset(2, 8)
                    .also { require(it.charAtOffset() == ',') }
                    .insertKtlintRuleSuppression(setOf("ktlint:standard:foo"))

            assertThat(actual).isEqualTo(formattedCode)
        }
    }

    @Nested
    inner class `Given a value argument list` {
        @Test
        fun `Given a value argument on which a suppression is added`() {
            val code =
                """
                val foobar = foobar(
                    foo = "foo",
                    bar = "bar",
                )
                """.trimIndent()
            val formattedCode =
                """
                val foobar = foobar(
                    @Suppress("ktlint:standard:foo")
                    foo = "foo",
                    bar = "bar",
                )
                """.trimIndent()
            val actual =
                code
                    .atOffset(2, 5)
                    .insertKtlintRuleSuppression(setOf("ktlint:standard:foo"))

            assertThat(actual).isEqualTo(formattedCode)
        }

        @Test
        fun `Given a value argument on which a suppression is added on the comma or non-code leaf in the value argument list`() {
            val code =
                """
                val foobar = foobar(
                    foo = "foo",
                    bar = "bar",
                )
                """.trimIndent()
            val formattedCode =
                """
                val foobar = foobar(
                    foo = "foo",
                    @Suppress("ktlint:standard:foo")
                    bar = "bar",
                )
                """.trimIndent()
            val actual =
                code
                    .atOffset(2, 16)
                    .also { require(it.charAtOffset() == ',') }
                    .insertKtlintRuleSuppression(setOf("ktlint:standard:foo"))

            assertThat(actual).isEqualTo(formattedCode)
        }
    }

    @Nested
    inner class `Given a value parameter list` {
        @Test
        fun `Given a class parameter on which a suppression is added`() {
            val code =
                """
                class Foobar(
                    val foo: Foo,
                )
                """.trimIndent()
            val formattedCode =
                """
                class Foobar(
                    @Suppress("ktlint:standard:foo")
                    val foo: Foo,
                )
                """.trimIndent()
            val actual =
                code
                    .atOffset(2, 9)
                    .insertKtlintRuleSuppression(setOf("ktlint:standard:foo"))

            assertThat(actual).isEqualTo(formattedCode)
        }

        @Test
        fun `Given a value parameter on which a suppression is added on the comma or non-code leaf in the value parameter list`() {
            val code =
                """
                class Foobar(
                    val foo: Foo,
                    val bar: Bar
                )
                """.trimIndent()
            val formattedCode =
                """
                class Foobar(
                    val foo: Foo,
                    @Suppress("ktlint:standard:foo")
                    val bar: Bar
                )
                """.trimIndent()
            val actual =
                code
                    .atOffset(2, 17)
                    .also { require(it.charAtOffset() == ',') }
                    .insertKtlintRuleSuppression(setOf("ktlint:standard:foo"))

            assertThat(actual).isEqualTo(formattedCode)
        }
    }

    @Test
    fun `Given a declaration with a @Suppress annotation using a named argument and a suppression`() {
        val code =
            """
            @Suppress(names = ["unused"])
            val foo = "foo"
            """.trimIndent()
        val formattedCode =
            """
            @Suppress("ktlint:standard:foo", "unused")
            val foo = "foo"
            """.trimIndent()
        val actual =
            code
                .atOffset(2, 5)
                .insertKtlintRuleSuppression(setOf("ktlint:standard:foo"))

        assertThat(actual).isEqualTo(formattedCode)
    }

    @Test
    fun `Given a suppression which is added on a property delegate`() {
        val code =
            """
            val foo by lazy(LazyThreadSafetyMode.PUBLICATION) {
                // do something
            }
            """.trimIndent()
        val formattedCode =
            """
            val foo by @Suppress("ktlint:standard:foo")
            lazy(LazyThreadSafetyMode.PUBLICATION) {
                // do something
            }
            """.trimIndent()
        val actual =
            code
                .atOffset(1, 12)
                .insertKtlintRuleSuppression(setOf("ktlint:standard:foo"))

        assertThat(actual).isEqualTo(formattedCode)
    }

    @Test
    fun `Given a nested expression on which a suppression is added`() {
        val code =
            """
            val foo =
                setOf("a")
                    .map {
                        bar(it)
                    }
            """.trimIndent()
        val formattedCode =
            """
            val foo =
                setOf("a")
                    .map {
                        @Suppress("ktlint:standard:foo")
                        bar(it)
                    }
            """.trimIndent()
        val actual =
            code
                .atOffset(4, 13)
                .insertKtlintRuleSuppression(setOf("ktlint:standard:foo"))

        assertThat(actual).isEqualTo(formattedCode)
    }

    private fun String.atOffset(
        line: Int,
        col: Int,
    ): CodeWithOffset {
        require(line >= 1)
        require(col >= 1)

        val lines = split("\n")
        require(line <= lines.size)

        val startOffsetOfLineContainingLintError =
            lines
                .take((line - 1).coerceAtLeast(0))
                .sumOf { text ->
                    // Fix length for newlines which were removed while splitting the original code
                    text.length + 1
                }

        val codeLine = lines[line - 1]
        require(col <= codeLine.length)

        return CodeWithOffset(this, startOffsetOfLineContainingLintError + (col - 1))
    }

    private data class CodeWithOffset(
        val code: String,
        val offset: Int,
    ) {
        fun insertKtlintRuleSuppression(
            suppressionIds: Set<String>,
            forceFileAnnotation: Boolean = false,
        ): String =
            ktLintRuleEngine
                .transformToAst(Code.fromSnippet(code))
                .also {
                    it
                        .findLeafElementAt(offset)
                        ?.insertKtlintRuleSuppression(suppressionIds, forceFileAnnotation)
                }.text

        fun charAtOffset(): Char = code[offset]

        private companion object {
            class SomeRule : RuleV2(ruleId = SOME_RULE_ID, about = About())

            val ktLintRuleEngine =
                KtLintRuleEngine(
                    ruleProviders = setOf(RuleV2Provider { SomeRule() }),
                )
        }
    }

    private companion object {
        val SOME_RULE_ID = RuleId("standard:some-rule-id")
    }
}
