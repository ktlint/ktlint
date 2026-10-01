package io.github.ktlint.core.ruleset.standard.rules

import io.github.ktlint.core.rule.engine.core.api.editorconfig.CODE_STYLE_PROPERTY
import io.github.ktlint.core.rule.engine.core.api.editorconfig.CodeStyleValue.KTLINT_OFFICIAL
import io.github.ktlint.core.test.KtLintAssertThat
import io.github.ktlint.core.test.SPACE
import io.github.ktlint.core.test.TAB
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class NoBlankLineInListRuleTest {
    private val noBlankLineInListRuleAssertThat = KtLintAssertThat.assertThatRule { NoBlankLineInListRule() }

    @Nested
    inner class `Given a super type list` {
        val formattedCode =
            """
            class FooBar:
                Foo,
                Bar {}
            """.trimIndent()

        @Test
        fun `Given one or more blank lines before the first super type`() {
            val code =
                """
                class FooBar:

                    $SPACE
                ${TAB}$TAB
                    Foo,
                    Bar {}
                """.trimIndent()
            noBlankLineInListRuleAssertThat(code)
                .withEditorConfigOverride(CODE_STYLE_PROPERTY to KTLINT_OFFICIAL)
                .hasLintViolation(2, 1, "Unexpected blank line(s) in super type list")
                .isFormattedAs(formattedCode)
        }

        @Test
        fun `Given one or more blank lines between the super type`() {
            val code =
                """
                class FooBar:
                    Foo,

                    $SPACE
                ${TAB}$TAB
                    Bar {}
                """.trimIndent()
            noBlankLineInListRuleAssertThat(code)
                .withEditorConfigOverride(CODE_STYLE_PROPERTY to KTLINT_OFFICIAL)
                .hasLintViolation(3, 1, "Unexpected blank line(s) in super type list")
                .isFormattedAs(formattedCode)
        }

        @Test
        fun `Given one or more blank lines after the last super type`() {
            val code =
                """
                class FooBar:
                    Foo,
                    Bar
                    $SPACE
                ${TAB}$TAB
                    {}
                """.trimIndent()
            noBlankLineInListRuleAssertThat(code)
                .withEditorConfigOverride(CODE_STYLE_PROPERTY to KTLINT_OFFICIAL)
                .hasLintViolation(4, 1, "Unexpected blank line(s) in super type list")
                .isFormattedAs(formattedCode)
        }

        @Test
        fun `Given a class without body and one or more blank lines after the last super type then do not report a violation`() {
            val code =
                """
                class FooBar:
                    Foo,
                    Bar

                    $SPACE
                ${TAB}$TAB
                """.trimIndent()
            noBlankLineInListRuleAssertThat(code)
                .withEditorConfigOverride(CODE_STYLE_PROPERTY to KTLINT_OFFICIAL)
                .hasNoLintViolations()
        }
    }

    @Nested
    inner class `Given a type constraint list` {
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

        @Test
        fun `Given one or more blank lines before the first type constraint`() {
            val code =
                """
                class BiAdapter<C : RecyclerView.ViewHolder, V1 : C, V2 : C, out A1, out A2>(
                    val adapter1: A1,
                    val adapter2: A2
                ) : RecyclerView.Adapter<C>()
                    where
                    $SPACE
                ${TAB}$TAB
                          A1 : RecyclerView.Adapter<V1>, A1 : ComposableAdapter.ViewTypeProvider,
                          A2 : RecyclerView.Adapter<V2>, A2 : ComposableAdapter.ViewTypeProvider {
                }
                """.trimIndent()
            noBlankLineInListRuleAssertThat(code)
                .withEditorConfigOverride(CODE_STYLE_PROPERTY to KTLINT_OFFICIAL)
                .addAdditionalRuleProvider { IndentationRule() }
                .hasLintViolation(6, 1, "Unexpected blank line(s) in type constraint list")
                .isFormattedAs(formattedCode)
        }

        @Test
        fun `Given one or more blank lines between the super type`() {
            val code =
                """
                class BiAdapter<C : RecyclerView.ViewHolder, V1 : C, V2 : C, out A1, out A2>(
                    val adapter1: A1,
                    val adapter2: A2
                ) : RecyclerView.Adapter<C>()
                    where A1 : RecyclerView.Adapter<V1>, A1 : ComposableAdapter.ViewTypeProvider,
                    $SPACE
                ${TAB}$TAB
                          A2 : RecyclerView.Adapter<V2>, A2 : ComposableAdapter.ViewTypeProvider {
                }
                """.trimIndent()
            noBlankLineInListRuleAssertThat(code)
                .withEditorConfigOverride(CODE_STYLE_PROPERTY to KTLINT_OFFICIAL)
                .hasLintViolation(6, 1, "Unexpected blank line(s) in type constraint list")
                .isFormattedAs(formattedCode)
        }

        @Test
        fun `Given one or more blank lines after the last super type`() {
            val code =
                """
                class BiAdapter<C : RecyclerView.ViewHolder, V1 : C, V2 : C, out A1, out A2>(
                    val adapter1: A1,
                    val adapter2: A2
                ) : RecyclerView.Adapter<C>()
                    where A1 : RecyclerView.Adapter<V1>, A1 : ComposableAdapter.ViewTypeProvider,
                          A2 : RecyclerView.Adapter<V2>, A2 : ComposableAdapter.ViewTypeProvider
                    $SPACE
                ${TAB}$TAB
                    {
                }
                """.trimIndent()
            noBlankLineInListRuleAssertThat(code)
                .withEditorConfigOverride(CODE_STYLE_PROPERTY to KTLINT_OFFICIAL)
                .hasLintViolation(7, 1, "Unexpected blank line(s) in type constraint list")
                .isFormattedAs(formattedCode)
        }

        @Test
        fun `Given a class without body and one or more blank lines after the last super type then do not report a violation`() {
            val code =
                """
                class BiAdapter<C : RecyclerView.ViewHolder, V1 : C, V2 : C, out A1, out A2>(
                    val adapter1: A1,
                    val adapter2: A2
                ) : RecyclerView.Adapter<C>()
                    where A1 : RecyclerView.Adapter<V1>, A1 : ComposableAdapter.ViewTypeProvider,
                          A2 : RecyclerView.Adapter<V2>, A2 : ComposableAdapter.ViewTypeProvider
                    $SPACE
                ${TAB}$TAB
                """.trimIndent()
            noBlankLineInListRuleAssertThat(code)
                .withEditorConfigOverride(CODE_STYLE_PROPERTY to KTLINT_OFFICIAL)
                .hasNoLintViolations()
        }
    }

    @Nested
    inner class `Given a type argument list` {
        val formattedCode =
            """
            val foobar: FooBar<
                Foo,
                Bar,
                > = FooBar(Foo(), Bar())
            """.trimIndent()

        @Test
        fun `Given one or more blank lines before the first type argument`() {
            val code =
                """
                val foobar: FooBar<

                    $SPACE
                ${TAB}$TAB
                    Foo,
                    Bar,
                    > = FooBar(Foo(), Bar())
                """.trimIndent()
            noBlankLineInListRuleAssertThat(code)
                .withEditorConfigOverride(CODE_STYLE_PROPERTY to KTLINT_OFFICIAL)
                .hasLintViolation(2, 1, "Unexpected blank line(s) in type argument list")
                .isFormattedAs(formattedCode)
        }

        @Test
        fun `Given one or more blank lines between type arguments`() {
            val code =
                """
                val foobar: FooBar<
                    Foo,

                    $SPACE
                ${TAB}$TAB
                    Bar,
                    > = FooBar(Foo(), Bar())
                """.trimIndent()
            noBlankLineInListRuleAssertThat(code)
                .withEditorConfigOverride(CODE_STYLE_PROPERTY to KTLINT_OFFICIAL)
                .hasLintViolation(3, 1, "Unexpected blank line(s) in type argument list")
                .isFormattedAs(formattedCode)
        }

        @Test
        fun `Given one or more blank lines after the last type argument`() {
            val code =
                """
                val foobar: FooBar<
                    Foo,

                    $SPACE
                ${TAB}$TAB
                    Bar,
                    > = FooBar(Foo(), Bar())
                """.trimIndent()
            noBlankLineInListRuleAssertThat(code)
                .withEditorConfigOverride(CODE_STYLE_PROPERTY to KTLINT_OFFICIAL)
                .hasLintViolation(3, 1, "Unexpected blank line(s) in type argument list")
                .isFormattedAs(formattedCode)
        }
    }

    @Nested
    inner class `Given a type parameter value list` {
        val formattedCode =
            """
            fun <
                Foo,
                Bar,
                > foobar()
            """.trimIndent()

        @Test
        fun `Given one or more blank lines before the first type parameter`() {
            val code =
                """
                fun <

                    $SPACE
                ${TAB}$TAB
                    Foo,
                    Bar,
                    > foobar()
                """.trimIndent()
            noBlankLineInListRuleAssertThat(code)
                .withEditorConfigOverride(CODE_STYLE_PROPERTY to KTLINT_OFFICIAL)
                .hasLintViolation(2, 1, "Unexpected blank line(s) in type parameter list")
                .isFormattedAs(formattedCode)
        }

        @Test
        fun `Given one or more blank lines between type parameters`() {
            val code =
                """
                fun <
                    Foo,

                    $SPACE
                ${TAB}$TAB
                    Bar,
                    > foobar()
                """.trimIndent()
            noBlankLineInListRuleAssertThat(code)
                .withEditorConfigOverride(CODE_STYLE_PROPERTY to KTLINT_OFFICIAL)
                .hasLintViolation(3, 1, "Unexpected blank line(s) in type parameter list")
                .isFormattedAs(formattedCode)
        }

        @Test
        fun `Given one or more blank lines after the last type parameter`() {
            val code =
                """
                fun <
                    Foo,
                    Bar,

                    $SPACE
                ${TAB}$TAB
                    > foobar()
                """.trimIndent()
            noBlankLineInListRuleAssertThat(code)
                .withEditorConfigOverride(CODE_STYLE_PROPERTY to KTLINT_OFFICIAL)
                .hasLintViolation(4, 1, "Unexpected blank line(s) in type parameter list")
                .isFormattedAs(formattedCode)
        }
    }

    @Nested
    inner class `Given a value argument list` {
        val formattedCode =
            """
            val foobar = foobar(
                "foo",
                "bar",
            )
            """.trimIndent()

        @Test
        fun `Given a function call containing one or more blank lines before the first argument`() {
            val code =
                """
                val foobar = foobar(

                    $SPACE
                ${TAB}$TAB
                    "foo",
                    "bar",
                )
                """.trimIndent()
            noBlankLineInListRuleAssertThat(code)
                .withEditorConfigOverride(CODE_STYLE_PROPERTY to KTLINT_OFFICIAL)
                .hasLintViolation(2, 1, "Unexpected blank line(s) in value argument list")
                .isFormattedAs(formattedCode)
        }

        @Test
        fun `Given a function call containing one or more blank lines between the arguments`() {
            val code =
                """
                val foobar = foobar(
                    "foo",

                    $SPACE
                ${TAB}$TAB
                    "bar",
                )
                """.trimIndent()
            noBlankLineInListRuleAssertThat(code)
                .withEditorConfigOverride(CODE_STYLE_PROPERTY to KTLINT_OFFICIAL)
                .hasLintViolation(3, 1, "Unexpected blank line(s) in value argument list")
                .isFormattedAs(formattedCode)
        }

        @Test
        fun `Given a function call containing one or more blank lines after the last argument`() {
            val code =
                """
                val foobar = foobar(
                    "foo",
                    "bar",

                    $SPACE
                ${TAB}$TAB
                )
                """.trimIndent()
            noBlankLineInListRuleAssertThat(code)
                .withEditorConfigOverride(CODE_STYLE_PROPERTY to KTLINT_OFFICIAL)
                .hasLintViolation(4, 1, "Unexpected blank line(s) in value argument list")
                .isFormattedAs(formattedCode)
        }
    }

    @Nested
    inner class `Given a value parameter list` {
        val formattedCode =
            """
            fun foobar(
                foo: String,
                bar: String,
            )
            """.trimIndent()

        @Test
        fun `Given a function signature containing one or more blank lines before the first parameter`() {
            val code =
                """
                fun foobar(

                    $SPACE
                ${TAB}$TAB
                    foo: String,
                    bar: String,
                )
                """.trimIndent()
            noBlankLineInListRuleAssertThat(code)
                .withEditorConfigOverride(CODE_STYLE_PROPERTY to KTLINT_OFFICIAL)
                .hasLintViolation(2, 1, "Unexpected blank line(s) in value parameter list")
                .isFormattedAs(formattedCode)
        }

        @Test
        fun `Given a function signature containing one or more blank lines between the parameters`() {
            val code =
                """
                fun foobar(
                    foo: String,

                    $SPACE
                ${TAB}$TAB
                    bar: String,
                )
                """.trimIndent()
            noBlankLineInListRuleAssertThat(code)
                .withEditorConfigOverride(CODE_STYLE_PROPERTY to KTLINT_OFFICIAL)
                .hasLintViolation(3, 1, "Unexpected blank line(s) in value parameter list")
                .isFormattedAs(formattedCode)
        }

        @Test
        fun `Given a function signature containing one or more blank lines after the last parameter`() {
            val code =
                """
                fun foobar(
                    foo: String,
                    bar: String,

                    $SPACE
                ${TAB}$TAB

                )
                """.trimIndent()
            noBlankLineInListRuleAssertThat(code)
                .withEditorConfigOverride(CODE_STYLE_PROPERTY to KTLINT_OFFICIAL)
                .hasLintViolation(4, 1, "Unexpected blank line(s) in value parameter list")
                .isFormattedAs(formattedCode)
        }
    }
}
