package io.github.ktlint.core.ruleset.standard.rules

import io.github.ktlint.core.test.KtLintAssertThat.Companion.assertThatRule
import io.github.ktlint.core.test.LintViolation
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class SpacingAroundColonRuleTest {
    private val spacingAroundColonRuleAssertThat = assertThatRule { SpacingAroundColonRule() }

    @Test
    fun `Given some child class declarations`() {
        val code =
            """
            class A:B
            class A2 : B2
            """.trimIndent()
        val formattedCode =
            """
            class A : B
            class A2 : B2
            """.trimIndent()
        spacingAroundColonRuleAssertThat(code)
            .hasLintViolation(1, 8, "Missing spacing around \":\"")
            .isFormattedAs(formattedCode)
    }

    @Test
    fun `Given some interfaces`() {
        val code =
            """
            interface D
            interface C: D
            interface C2 : D
            """.trimIndent()
        val formattedCode =
            """
            interface D
            interface C : D
            interface C2 : D
            """.trimIndent()
        spacingAroundColonRuleAssertThat(code)
            .hasLintViolation(2, 12, "Missing spacing before \":\"")
            .isFormattedAs(formattedCode)
    }

    @Test
    fun `Given some variable declarations inside a function`() {
        val code =
            """
            fun main() {
                var x:Boolean
                var y: Boolean
            }
            """.trimIndent()
        val formattedCode =
            """
            fun main() {
                var x: Boolean
                var y: Boolean
            }
            """.trimIndent()
        spacingAroundColonRuleAssertThat(code)
            .hasLintViolation(2, 11, "Missing spacing after \":\"")
            .isFormattedAs(formattedCode)
    }

    @Test
    fun `Given some object creation as parameter value in function without a space before the colon`() {
        val code =
            """
            interface Foo
            fun main() {
                bar(object: Foo {})
            }
            """.trimIndent()
        val formattedCode =
            """
            interface Foo
            fun main() {
                bar(object : Foo {})
            }
            """.trimIndent()
        spacingAroundColonRuleAssertThat(code)
            .hasLintViolation(3, 15, "Missing spacing before \":\"")
            .isFormattedAs(formattedCode)
    }

    @Test
    fun `Given some annotation use-site targets`() {
        // https://kotlinlang.org/docs/annotations.html#annotation-use-site-targets
        val code =
            """
            @file:JvmName("Foo")
            @file : JvmName("Foo")
            class Example(@field:Ann val foo: String, @get:Ann val bar: String)
            class Example(@field : Ann val foo: String, @get : Ann val bar: String)
            class Example {
                @set:[Inject VisibleForTesting]
                public var collaborator: Collaborator
                @set : [Inject VisibleForTesting]
                public var collaborator: Collaborator
            }
            fun @receiver:Fancy String.myExtension() { }
            fun @receiver : Fancy String.myExtension() { }
            """.trimIndent()
        val formattedCode =
            """
            @file:JvmName("Foo")
            @file:JvmName("Foo")
            class Example(@field:Ann val foo: String, @get:Ann val bar: String)
            class Example(@field:Ann val foo: String, @get:Ann val bar: String)
            class Example {
                @set:[Inject VisibleForTesting]
                public var collaborator: Collaborator
                @set:[Inject VisibleForTesting]
                public var collaborator: Collaborator
            }
            fun @receiver:Fancy String.myExtension() { }
            fun @receiver:Fancy String.myExtension() { }
            """.trimIndent()
        spacingAroundColonRuleAssertThat(code)
            .hasLintViolations(
                LintViolation(2, 7, "Unexpected spacing before \":\""),
                LintViolation(2, 7, "Unexpected spacing after \":\""),
                LintViolation(4, 22, "Unexpected spacing before \":\""),
                LintViolation(4, 22, "Unexpected spacing after \":\""),
                LintViolation(4, 50, "Unexpected spacing before \":\""),
                LintViolation(4, 50, "Unexpected spacing after \":\""),
                LintViolation(8, 10, "Unexpected spacing before \":\""),
                LintViolation(8, 10, "Unexpected spacing after \":\""),
                LintViolation(12, 15, "Unexpected spacing before \":\""),
                LintViolation(12, 15, "Unexpected spacing after \":\""),
            ).isFormattedAs(formattedCode)
    }

    @Test
    fun `Given a double colon operator then do not add spaces`() {
        val code =
            """
            fun main() {
                val x = Foo::class
            }
            """.trimIndent()
        spacingAroundColonRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun testLint4() {
        val code =
            """
            class A {
                @Deprecated("") @Throws(IOException::class, SecurityException::class)
                protected abstract fun <T> f(
                    @Nullable thing: String, things: List<T>): Runnable where T : Runnable, T: Closeable
            }
            """.trimIndent()
        val formattedCode =
            """
            class A {
                @Deprecated("") @Throws(IOException::class, SecurityException::class)
                protected abstract fun <T> f(
                    @Nullable thing: String, things: List<T>): Runnable where T : Runnable, T : Closeable
            }
            """.trimIndent()
        spacingAroundColonRuleAssertThat(code)
            .hasLintViolation(4, 82, "Missing spacing before \":\"")
            .isFormattedAs(formattedCode)
    }

    @Test
    fun `Given some classes missing spaces around the colon before the super type`() {
        val code =
            """
            open class C1(string: String)
            class C2(string: String):C1(string)
            class C3 constructor(string: String):C1(string)
            class C4:C1 {
                constructor():this("")
                constructor(param: String):super(param)

                val c1 = object:C1("foo") { }
                val c2 = C4()
            }
            """.trimIndent()
        val formattedCode =
            """
            open class C1(string: String)
            class C2(string: String) : C1(string)
            class C3 constructor(string: String) : C1(string)
            class C4 : C1 {
                constructor() : this("")
                constructor(param: String) : super(param)

                val c1 = object : C1("foo") { }
                val c2 = C4()
            }
            """.trimIndent()
        spacingAroundColonRuleAssertThat(code)
            .hasLintViolations(
                LintViolation(2, 25, "Missing spacing around \":\""),
                LintViolation(3, 37, "Missing spacing around \":\""),
                LintViolation(4, 9, "Missing spacing around \":\""),
                LintViolation(5, 18, "Missing spacing around \":\""),
                LintViolation(6, 31, "Missing spacing around \":\""),
                LintViolation(8, 20, "Missing spacing around \":\""),
            ).isFormattedAs(formattedCode)
    }

    @Test
    fun `Given a type constraint missing a space before the colon`() {
        val code =
            """
            fun <T> max(a: T, b: T) where T: Comparable<T>
            """.trimIndent()
        val formattedCode =
            """
            fun <T> max(a: T, b: T) where T : Comparable<T>
            """.trimIndent()
        spacingAroundColonRuleAssertThat(code)
            .hasLintViolation(1, 32, "Missing spacing before \":\"")
            .isFormattedAs(formattedCode)
    }

    @Test
    fun `Given a function declaration with an unexpected space before the colon before the return type`() {
        val code =
            """
            fun foo() : String = "foo"
            """.trimIndent()
        val formattedCode =
            """
            fun foo(): String = "foo"
            """.trimIndent()
        spacingAroundColonRuleAssertThat(code)
            .hasLintViolation(1, 11, "Unexpected spacing before \":\"")
            .isFormattedAs(formattedCode)
    }

    @Test
    fun `Given a function parameter with an unexpected space before the colon`() {
        val code =
            """
            fun foo(bar : String) {}
            """.trimIndent()
        val formattedCode =
            """
            fun foo(bar: String) {}
            """.trimIndent()
        spacingAroundColonRuleAssertThat(code)
            .hasLintViolation(1, 13, "Unexpected spacing before \":\"")
            .isFormattedAs(formattedCode)
    }

    @Test
    fun `Given a generic function type parameter without space before the colon`() {
        val code =
            """
            fun <T: Any> trueIdentity(value: T): T = value
            """.trimIndent()
        val formattedCode =
            """
            fun <T : Any> trueIdentity(value: T): T = value
            """.trimIndent()
        spacingAroundColonRuleAssertThat(code)
            .hasLintViolation(1, 7, "Missing spacing before \":\"")
            .isFormattedAs(formattedCode)
    }

    @Test
    fun testFormatEOF() {
        val code =
            """
            class X
                : Y,
                Z
            class A // comment
                : B
            class A /*

            */
                : B
            val xmlFormatter
                : String = ""
            """.trimIndent()
        val formattedCode =
            """
            class X :
                Y,
                Z
            class A : // comment
                B
            class A : /*

            */
                B
            val xmlFormatter: String =
                ""
            """.trimIndent()
        spacingAroundColonRuleAssertThat(code)
            .hasLintViolations(
                LintViolation(1, 8, "Unexpected newline before \":\""),
                LintViolation(4, 19, "Unexpected newline before \":\""),
                LintViolation(8, 3, "Unexpected newline before \":\""),
                LintViolation(10, 17, "Unexpected newline before \":\""),
            ).isFormattedAs(formattedCode)
    }

    @Nested
    inner class `Issue 1057 - Given some declaration with an unexpected newline before the colon` {
        @Test
        fun `Property with colon on next line`() {
            val code =
                """
                val v1
                    : Int = 1

                val v2// comment
                    : Int = 1

                val v3 // comment
                    : Int = 1

                val v4
                    // comment
                    : Int = 1
                """.trimIndent()
            val formattedCode =
                """
                val v1: Int =
                    1

                val v2: Int =// comment
                    1

                val v3: Int = // comment
                    1

                val v4: Int =
                    // comment
                    1
                """.trimIndent()
            spacingAroundColonRuleAssertThat(code)
                .hasLintViolations(
                    LintViolation(1, 7, "Unexpected newline before \":\""),
                    LintViolation(4, 17, "Unexpected newline before \":\""),
                    LintViolation(7, 18, "Unexpected newline before \":\""),
                    LintViolation(11, 15, "Unexpected newline before \":\""),
                ).isFormattedAs(formattedCode)
        }

        @Test
        fun `Function with colon on next line followed by body expression`() {
            val code =
                """
                fun foo1()
                    : Int = 1

                fun foo2()// comment
                    : Int = 1

                fun foo3() // comment
                    : Int = 1

                fun foo4()
                    // comment
                    : Int = 1
                """.trimIndent()
            val formattedCode =
                """
                fun foo1(): Int =
                    1

                fun foo2(): Int =// comment
                    1

                fun foo3(): Int = // comment
                    1

                fun foo4(): Int =
                    // comment
                    1
                """.trimIndent()
            spacingAroundColonRuleAssertThat(code)
                .hasLintViolations(
                    LintViolation(1, 11, "Unexpected newline before \":\""),
                    LintViolation(4, 21, "Unexpected newline before \":\""),
                    LintViolation(7, 22, "Unexpected newline before \":\""),
                    LintViolation(11, 15, "Unexpected newline before \":\""),
                ).isFormattedAs(formattedCode)
        }

        @Test
        fun `Function with colon on next line followed by body block`() {
            val code =
                """
                fun foo1()
                    : Int {
                    1
                }

                fun foo2()// comment
                    : Int {
                    1
                }

                fun foo3() // comment
                    : Int {
                    1
                }

                fun foo4()
                    // comment
                    : Int {
                    1
                }
                """.trimIndent()
            val formattedCode =
                """
                fun foo1(): Int {
                    1
                }

                fun foo2(): Int {// comment
                    1
                }

                fun foo3(): Int { // comment
                    1
                }

                fun foo4(): Int {
                    // comment
                    1
                }
                """.trimIndent()
            spacingAroundColonRuleAssertThat(code)
                .hasLintViolations(
                    LintViolation(1, 11, "Unexpected newline before \":\""),
                    LintViolation(6, 21, "Unexpected newline before \":\""),
                    LintViolation(11, 22, "Unexpected newline before \":\""),
                    LintViolation(17, 15, "Unexpected newline before \":\""),
                ).isFormattedAs(formattedCode)
        }

        @Test
        fun `Object declaration with colon on next line`() {
            val code =
                """
                val foo1 = object
                    : Foo() {}

                val foo2 = object// comment
                    : Foo() {}

                val foo3 = object // comment
                    : Foo() {}

                val foo4 = object
                    // comment
                    : Foo() {}
                """.trimIndent()
            val formattedCode =
                """
                val foo1 = object :
                    Foo() {}

                val foo2 = object : // comment
                    Foo() {}

                val foo3 = object : // comment
                    Foo() {}

                val foo4 = object :
                    // comment
                    Foo() {}
                """.trimIndent()
            spacingAroundColonRuleAssertThat(code)
                .hasLintViolations(
                    LintViolation(1, 18, "Unexpected newline before \":\""),
                    LintViolation(4, 28, "Unexpected newline before \":\""),
                    LintViolation(7, 29, "Unexpected newline before \":\""),
                    LintViolation(11, 15, "Unexpected newline before \":\""),
                ).isFormattedAs(formattedCode)
        }

        @Test
        fun `Class with a comment and colon on next line`() {
            val code =
                """
                class Foo
                // comment
                :Bar
                """.trimIndent()
            val formattedCode =
                """
                class Foo : Bar
                // comment

                """.trimIndent()
            spacingAroundColonRuleAssertThat(code)
                .hasLintViolations(
                    LintViolation(2, 11, "Unexpected newline before \":\""),
                    LintViolation(3, 2, "Missing spacing after \":\""),
                ).isFormattedAs(formattedCode)
        }
    }
}
