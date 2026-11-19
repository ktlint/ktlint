package io.github.ktlint.core.ruleset.standard.rules

import io.github.ktlint.core.test.KtLintAssertThat.Companion.assertThatRule
import org.junit.jupiter.api.Test

class SpacingBetweenDeclarationsWithAnnotationsRuleTest {
    private val spacingBetweenDeclarationsWithAnnotationsRuleAssertThat =
        assertThatRule { SpacingBetweenDeclarationsWithAnnotationsRule() }

    @Test
    fun `Given an annotation at top of file should do nothing`() {
        val code =
            """
            @Foo
            fun a()
            """.trimIndent()
        spacingBetweenDeclarationsWithAnnotationsRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `Given multiple annotations should do nothing`() {
        val code =
            """
            @Foo
            @Bar
            fun a()
            """.trimIndent()
        spacingBetweenDeclarationsWithAnnotationsRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `Given a comment on line above annotation should do nothing`() {
        val code =
            """
            // some comment
            @Foo
            fun a()
            """.trimIndent()
        spacingBetweenDeclarationsWithAnnotationsRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `Given no blank line before declaration with annotation`() {
        val code =
            """
            fun a()
            @Foo
            fun b()
            """.trimIndent()
        val formattedCode =
            """
            fun a()

            @Foo
            fun b()
            """.trimIndent()
        spacingBetweenDeclarationsWithAnnotationsRuleAssertThat(code)
            .hasLintViolation(2, 1, "Declarations and declarations with annotations should have an empty space between.")
            .isFormattedAs(formattedCode)
    }

    @Test
    fun `Given no blank line before declaration with multiple annotations`() {
        val code =
            """
            fun a()
            @Foo
            @Bar
            fun b()
            """.trimIndent()
        val formattedCode =
            """
            fun a()

            @Foo
            @Bar
            fun b()
            """.trimIndent()
        spacingBetweenDeclarationsWithAnnotationsRuleAssertThat(code)
            .hasLintViolation(2, 1, "Declarations and declarations with annotations should have an empty space between.")
            .isFormattedAs(formattedCode)
    }

    @Test
    fun `Issue 971 - Given an annotated primary class constructor without blank line above the annotation`() {
        val code =
            """
            annotation class E

            private class A
            @E
            constructor(a: Int)
            """.trimIndent()
        spacingBetweenDeclarationsWithAnnotationsRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `Issue 971 - Given an annotated function parameter without blank line above`() {
        val code =
            """
            annotation class E

            fun foo(
                a: String,
                @E
                b: String
            ) = 1
            """.trimIndent()
        spacingBetweenDeclarationsWithAnnotationsRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `Issue 971 - missing space before member function`() {
        val code =
            """
            annotation class E

            class C {
                fun foo() = 1
                @E
                fun bar() = 2
            }
            """.trimIndent()
        val formattedCode =
            """
            annotation class E

            class C {
                fun foo() = 1

                @E
                fun bar() = 2
            }
            """.trimIndent()
        spacingBetweenDeclarationsWithAnnotationsRuleAssertThat(code)
            .hasLintViolation(5, 1, "Declarations and declarations with annotations should have an empty space between.")
            .isFormattedAs(formattedCode)
    }

    @Test
    fun `Given an annotated function preceded by multiple blank lines`() {
        val code =
            """
            annotation class E
            class C {
                fun foo() = 1


                @E
                fun bar() = 2
            }
            """.trimIndent()
        spacingBetweenDeclarationsWithAnnotationsRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `Issue 1126 - Given a comment on line above annotated class member should do nothing`() {
        val code =
            """
            annotation class E
            class C {
                fun foo() = 1

                // Hello
                @E
                fun bar() = 2
            }
            """.trimIndent()
        spacingBetweenDeclarationsWithAnnotationsRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `Issue 1126 - Given a comment on line above annotated variable in function should do nothing`() {
        val code =
            """
            fun foo() {
                val a = 1

                // hello
                @Foo
                val b = 2
            }
            """.trimIndent()
        spacingBetweenDeclarationsWithAnnotationsRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `Issue 1281 - No blank line is required between comment and an annotated declaration when previous declaration ends with a comment`() {
        val code =
            """
            class KotlinPluginTest {
                // tag::setUp[]
                @BeforeEach
                fun setUp() {
                }
                // end::setUp[]

                // tag::testQuery[]
                @Test
                    fun testFindById() {
                }
                // end::testQuery[]
            }
            """.trimIndent()
        spacingBetweenDeclarationsWithAnnotationsRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `Issue 1802 - Given a declaration followed by multiple kdocs and an annotated declaration`() {
        val code =
            """
            public var foo = "foo"

            /**
             * Kdoc1
             */
            /** Kdoc2 */
            @Bar
            public var bar = "bar"
            """.trimIndent()
        spacingBetweenDeclarationsWithAnnotationsRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `Issue 2416 - Given a declaration followed by an EOL comment and other annotated declaration`() {
        val code =
            """
            fun foobar() {
                val bar = "bar"
                // Some comment
                @Suppress("unused")
                val foo  = "foo"
            }
            """.trimIndent()
        val formattedCode =
            """
            fun foobar() {
                val bar = "bar"

                // Some comment
                @Suppress("unused")
                val foo  = "foo"
            }
            """.trimIndent()
        spacingBetweenDeclarationsWithAnnotationsRuleAssertThat(code)
            .hasLintViolation(3, 1, "Declarations and declarations with annotations should have an empty space between.")
            .isFormattedAs(formattedCode)
    }

    @Test
    fun `Issue 2416 - Given a declaration followed by a block comment and other annotated declaration`() {
        val code =
            """
            fun foobar() {
                val bar = "bar"
                /*
                 * Some comment
                 */
                @Suppress("unused")
                val foo  = "foo"
            }
            """.trimIndent()
        val formattedCode =
            """
            fun foobar() {
                val bar = "bar"

                /*
                 * Some comment
                 */
                @Suppress("unused")
                val foo  = "foo"
            }
            """.trimIndent()
        spacingBetweenDeclarationsWithAnnotationsRuleAssertThat(code)
            .hasLintViolation(3, 1, "Declarations and declarations with annotations should have an empty space between.")
            .isFormattedAs(formattedCode)
    }

    @Test
    fun `Issue 2416 - Given a declaration followed by a KDoc and other annotated declaration`() {
        val code =
            """
            fun foobar() {
                val bar = "bar"
                /**
                 * Some comment
                 */
                @Suppress("unused")
                val foo  = "foo"
            }
            """.trimIndent()
        val formattedCode =
            """
            fun foobar() {
                val bar = "bar"

                /**
                 * Some comment
                 */
                @Suppress("unused")
                val foo  = "foo"
            }
            """.trimIndent()
        spacingBetweenDeclarationsWithAnnotationsRuleAssertThat(code)
            .hasLintViolation(3, 1, "Declarations and declarations with annotations should have an empty space between.")
            .isFormattedAs(formattedCode)
    }

    @Test
    fun `Issue 2901 - Given a variable with property accessors not separated by a blank line, and the second accessor is annotated then add a blank in between`() {
        val code =
            """
            public var foo: Boolean
                get() = false
                @Foo
                set(value) {
                    foo = value
                }
            """.trimIndent()
        val formattedCode =
            """
            public var foo: Boolean
                get() = false

                @Foo
                set(value) {
                    foo = value
                }
            """.trimIndent()
        spacingBetweenDeclarationsWithAnnotationsRuleAssertThat(code)
            .hasLintViolation(3, 1, "Declarations and declarations with annotations should have an empty space between.")
            .isFormattedAs(formattedCode)
    }

    @Test
    fun `Given a declaration followed by EOL comment on same line, followed by an annotated declaration without blank line in between`() {
        val code =
            """
            class Foo {
                val bar = "bar" // Some comment
                @Baz
                val baz = "Baz"
            }
            """.trimIndent()
        val formattedCode =
            """
            class Foo {
                val bar = "bar" // Some comment

                @Baz
                val baz = "Baz"
            }
            """.trimIndent()
        spacingBetweenDeclarationsWithAnnotationsRuleAssertThat(code)
            .hasLintViolation(3, 1, "Declarations and declarations with annotations should have an empty space between.")
            .isFormattedAs(formattedCode)
    }
}
