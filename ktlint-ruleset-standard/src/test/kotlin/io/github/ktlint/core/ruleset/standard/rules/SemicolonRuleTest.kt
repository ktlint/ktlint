package io.github.ktlint.core.ruleset.standard.rules

import io.github.ktlint.core.test.KtLintAssertThat.Companion.assertThatRule
import io.github.ktlint.core.test.LintViolation
import io.github.ktlint.core.test.SPACE
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class SemicolonRuleTest {
    private val semicolonRuleAssertThat = assertThatRule { SemicolonRule() }

    @Test
    fun `Given a package statement followed by a semicolon then remove the semicolon`() {
        val code =
            """
            package a.b.c;
            """.trimIndent()
        val formattedCode =
            """
            package a.b.c
            """.trimIndent()
        semicolonRuleAssertThat(code)
            .hasLintViolation(1, 14, "Unnecessary semicolon")
            .isFormattedAs(formattedCode)
    }

    @Test
    fun `Given multiple statements on a single line then remove the semicolon and wrap statements`() {
        val code =
            """
            val foo = "foo"; val bar = "bar"
            fun name() { a(); return b }
            """.trimIndent()
        val formattedCode =
            """
            val foo = "foo"
            val bar = "bar"
            fun name() {
                a()
                return b
            }
            """.trimIndent()
        semicolonRuleAssertThat(code)
            .addAdditionalRuleProvider { StatementWrappingRule() }
            .hasLintViolations(
                LintViolation(1, 16, "Replace ';' with newline"),
                LintViolation(2, 17, "Replace ';' with newline"),
            ).isFormattedAs(formattedCode)
    }

    @Test
    fun `Given a semi colon at the start of a line and not followed by code or comment on the same line then do remove the semicolons and redundant whitespace`() {
        val code =
            """
            fun foo() {
                ;
                bar()
                ;

                bar()

                ;
            }
            """.trimIndent()
        val formattedCode =
            """
            fun foo() {
                bar()

                bar()
            }
            """.trimIndent()
        semicolonRuleAssertThat(code)
            .hasLintViolations(
                LintViolation(2, 5, "Unnecessary semicolon"),
                LintViolation(4, 5, "Unnecessary semicolon"),
                LintViolation(8, 5, "Unnecessary semicolon"),
            ).isFormattedAs(formattedCode)
    }

    @Test
    fun `Given a semi colon at the start of a line and followed by code or comment on the same line then do report a lint error`() {
        val code =
            """
            ;val fooBar = "fooBar"
            ;// some comment
            """.trimIndent()
        val formattedCode =
            """
            val fooBar = "fooBar"
            // some comment
            """.trimIndent()
        semicolonRuleAssertThat(code)
            .hasLintViolations(
                LintViolation(1, 1, "Unnecessary semicolon"),
                LintViolation(2, 1, "Unnecessary semicolon"),
            ).isFormattedAs(formattedCode)
    }

    @Test
    fun `Given a semi colon at the start of a block then do report a lint error`() {
        val code =
            """
            fun name() { ; a() }
            """.trimIndent()
        val formattedCode =
            """
            fun name() { a() }
            """.trimIndent()
        semicolonRuleAssertThat(code)
            .hasLintViolation(1, 14, "Unnecessary semicolon")
            .isFormattedAs(formattedCode)
    }

    @Test
    fun `Given a semi colon at end of a block then do report a lint error`() {
        val code =
            """
            fun name() { a(); }
            """.trimIndent()
        val formattedCode =
            """
            fun name() { a() }
            """.trimIndent()
        semicolonRuleAssertThat(code)
            .hasLintViolation(1, 17, "Unnecessary semicolon")
            .isFormattedAs(formattedCode)
    }

    @Test
    fun `Given a call to function with an optional lambda as last parameter which is omitted, followed by function call on a lambda receiver requires a semicolon in between`() {
        val code =
            """
            fun fooBar1() {
                // Removing this semicolon on next line results in compile errors!
                foo("some-foo");
                {
                    // Do something
                }.bar()
            }

            fun fooBar2() {
                // Removing this semicolon on next line results in compile errors!
                foo("some-foo"); { /* Do something */ }.bar()
            }

            // This code is here just to demonstrate why the semicolon after the call to foo is important when the
            // optional lambda is omitted
            fun foo(input: String, block: ((String) -> Unit)? = null) {
                if (block != null) {
                    block(input)
                } else {
                    input
                }
            }

            // This code is here just to demonstrate why the semicolon after the call to foo is important when the
            // optional lambda is omitted
            fun <R> (() -> R).bar() {
                // Do something
            }
            """.trimIndent()
        val formattedCode =
            """
            fun fooBar1() {
                // Removing this semicolon on next line results in compile errors!
                foo("some-foo");
                {
                    // Do something
                }.bar()
            }

            fun fooBar2() {
                // Removing this semicolon on next line results in compile errors!
                foo("some-foo");
                { /* Do something */ }.bar()
            }

            // This code is here just to demonstrate why the semicolon after the call to foo is important when the
            // optional lambda is omitted
            fun foo(input: String, block: ((String) -> Unit)? = null) {
                if (block != null) {
                    block(input)
                } else {
                    input
                }
            }

            // This code is here just to demonstrate why the semicolon after the call to foo is important when the
            // optional lambda is omitted
            fun <R> (() -> R).bar() {
                // Do something
            }
            """.trimIndent()
        semicolonRuleAssertThat(code)
            .hasLintViolation(11, 21, "Missing newline after ';'")
            .isFormattedAs(formattedCode)
    }

    @Test
    fun `Given multiple statements on a single line and semicolon not followed by space then do return a lint error`() {
        val code =
            """
            fun name() { a();return b }
            """.trimIndent()
        val formattedCode =
            """
            fun name() {
                a()
                return b
            }
            """.trimIndent()
        semicolonRuleAssertThat(code)
            .addAdditionalRuleProvider { StatementWrappingRule() }
            .hasLintViolation(1, 17, "Replace ';' with newline")
            .isFormattedAs(formattedCode)
    }

    @Test
    fun `Given a function declaration followed by a comma then return a lint error`() {
        val code =
            """
            fun main() {};
            """.trimIndent()
        val formattedCode =
            """
            fun main() {}
            """.trimIndent()
        semicolonRuleAssertThat(code)
            .hasLintViolation(1, 14, "Unnecessary semicolon")
            .isFormattedAs(formattedCode)
    }

    @Test
    fun `Issue 281 - Given a companion object without block and followed by semicolon then do not return a lint error`() {
        val code =
            """
            class A1 {
                companion object;
            }
            class A2 {
                companion object ;
            }
            """.trimIndent()
        semicolonRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `Issue 281 - Given a companion object followed by a block and semicolon then do return a lint error`() {
        val code =
            """
            class A {
                companion object {
                    const val emptyString = ""
                };
            }
            """.trimIndent()
        val formattedCode =
            """
            class A {
                companion object {
                    const val emptyString = ""
                }
            }
            """.trimIndent()
        semicolonRuleAssertThat(code)
            .hasLintViolation(4, 6, "Unnecessary semicolon")
            .isFormattedAs(formattedCode)
    }

    @Test
    fun `Given a KDoc with an identifier followed by a semicolon then do not return a lint error`() {
        val code =
            """
            /**
             * [x];
             */
            fun foo() {
            }
            """.trimIndent()
        semicolonRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `Given a lambda call followed by an annotated block then do not return a lint error`() {
        val code =
            """
            val foo122: () -> String =
                run {
                    bar();
                    // Removing the semicolon on the line above results in a compile error!
                    @Baz
                    { "foo" }
                }
            """.trimIndent()
        semicolonRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `Given a lambda call followed by a block then do not return a lint error`() {
        val code =
            """
            val f: () -> String = run {
                listOf(1).map {
                    it + it
                };
                // Removing the semicolon on the line above results in a compile error!
                { "" }
            }
            """.trimIndent()
        semicolonRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `Given a lambda call followed by a block preceded with a KDoc then do not return a lint error`() {
        val code =
            """
            val f: () -> String = run {
                listOf(1).map {
                    it + it
                };
                /**
                 * kdoc
                 */
                { "" }
            }
            """.trimIndent()
        semicolonRuleAssertThat(code).hasNoLintViolations()
    }

    @Nested
    inner class `Given an enumeration` {
        @Test
        fun `Given an enumeration and the list of values is followed by a declaration then do not return a lint error`() {
            val code =
                """
                enum class E1 {
                    ONE, TWO;
                    fun fn() {}
                }
                enum class E2 {
                    ONE, TWO;
                    val foo = "foo"
                }
                enum class E3 {
                    ONE, TWO;
                    private companion object {
                       val foo = "foo"
                   }
                }
                """.trimIndent()
            semicolonRuleAssertThat(code).hasNoLintViolations()
        }

        @Test
        fun `Issue 1733 - Given an enumeration with primary constructor and the list of values is followed by a code element then do not return a lint error`() {
            val code =
                """
                enum class E1(bar: String) {
                    ONE("one"),
                    TWO("two");
                    fun fn() {}
                }
                enum class E2(bar: String) {
                    ONE("one"),
                    TWO("two");
                    val foo = "foo"
                }
                enum class E3(bar: String) {
                    ONE("one"),
                    TWO("two");
                    private companion object {
                       val foo = "foo"
                   }
                }
                """.trimIndent()
            semicolonRuleAssertThat(code).hasNoLintViolations()
        }

        @Test
        fun `Given an enumeration and the list of values is closed with an EOL comment followed by a semicolon on the next line then do not return a lint error`() {
            val code =
                """
                enum class Test {
                    ONE
                    // comment
                    ;
                }
                enum class Foo(bar: String) {
                    ONE("one")
                    // comment
                    ;
                }
                """.trimIndent()
            val formattedCode =
                """
                enum class Test {
                    ONE
                    // comment
                }
                enum class Foo(bar: String) {
                    ONE("one")
                    // comment
                }
                """.trimIndent()
            semicolonRuleAssertThat(code)
                .hasLintViolations(
                    LintViolation(4, 5, "Unnecessary semicolon"),
                    LintViolation(9, 5, "Unnecessary semicolon"),
                ).isFormattedAs(formattedCode)
        }

        @Test
        fun `Given an enumeration and the list of values is closed with a semicolon not followed by statements then do return a lint error`() {
            val code =
                """
                enum class E1 {
                    A,
                    B;
                }
                enum class E2 {
                    A,
                    B // comment
                    ;
                }
                enum class E3 {
                    ;
                }
                enum class E4 {
                    // comment
                    ;
                }
                """.trimIndent()
            semicolonRuleAssertThat(code)
                .hasLintViolations(
                    LintViolation(3, 6, "Unnecessary semicolon"),
                    LintViolation(8, 5, "Unnecessary semicolon"),
                    LintViolation(11, 5, "Unnecessary semicolon"),
                    LintViolation(15, 5, "Unnecessary semicolon"),
                )
        }

        @Test
        fun `Given an enumeration with a primary constructor and the list of values is closed with a semicolon not followed by statements then do return a lint error`() {
            val code =
                """
                enum class Foo1(bar: String) {
                    A(1),
                    B(2);
                }
                enum class Foo2(bar: String) {
                    A(1),
                    B(2) // comment
                    ;
                }
                enum class Foo3(bar: String) {
                    ;
                }
                enum class Foo4(bar: String) {
                    // comment
                    ;
                }
                """.trimIndent()
            semicolonRuleAssertThat(code)
                .hasLintViolations(
                    LintViolation(3, 9, "Unnecessary semicolon"),
                    LintViolation(8, 5, "Unnecessary semicolon"),
                    LintViolation(11, 5, "Unnecessary semicolon"),
                    LintViolation(15, 5, "Unnecessary semicolon"),
                )
        }
    }

    @Test
    fun `Given a for loop with an empty body then do not report a lint error`() {
        val code =
            """
            fun test(list: List<Int>) {
                for (i in list);
            }
            """.trimIndent()
        semicolonRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `Given a while loop with an empty body then do not report a lint error`() {
        val code =
            """
            fun test() {
                while (true);
            }
            """.trimIndent()
        semicolonRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `Given a do-while loop with an empty body then do report a lint error`() {
        val code =
            """
            fun test() {
                do { bar() } while (true);
            }
            """.trimIndent()
        val formattedCode =
            """
            fun test() {
                do { bar() } while (true)
            }
            """.trimIndent()
        semicolonRuleAssertThat(code)
            .hasLintViolation(2, 30, "Unnecessary semicolon")
            .isFormattedAs(formattedCode)
    }

    @Test
    fun `Given an if-statement with an empty body then do not report a lint error`() {
        val code =
            """
            fun testIf() {
                if (true);
            }
            """.trimIndent()
        semicolonRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `Issue 2771 - Given an enum class without enum entry, and containing some code after the semi then do not report a lint error`() {
        val code =
            """
            enum class Foo {
                ;

                fun foo() {
                    // do something
                }
            }
            """.trimIndent()
        semicolonRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `Issue 2771 - Given an enum class without enum entry, and some comment before and some code after the semi then do not report a lint error`() {
        val code =
            """
            enum class Foo {
                // NO-OP
                ;

                fun foo() {
                    // do something
                }
            }
            """.trimIndent()
        semicolonRuleAssertThat(code).hasNoLintViolations()
    }

    @Nested
    inner class `Issue 1078 - Given multiple expression separated with semi in a single line` {
        @Nested
        inner class `Given multiple variables` {
            @Test
            fun `Given two variables`() {
                val code =
                    """
                    fun foo() {
                        val bar1 = 3; val bar2 = 2
                        val fooBar1: String = ""; val fooBar2: () -> Unit = {  }
                    }
                    """.trimIndent()
                val formattedCode =
                    """
                    fun foo() {
                        val bar1 = 3
                        val bar2 = 2
                        val fooBar1: String = ""
                        val fooBar2: () -> Unit = {  }
                    }
                    """.trimIndent()
                semicolonRuleAssertThat(code)
                    .hasLintViolations(
                        LintViolation(2, 17, "Replace ';' with newline"),
                        LintViolation(3, 29, "Replace ';' with newline"),
                    ).isFormattedAs(formattedCode)
            }

            @Test
            fun `Given two variables run without NoSemicolonsRule`() {
                val code =
                    """
                    fun foo() {
                        val bar1 = 3; val bar2 = 2
                        val fooBar1: String = ""; val fooBar2: () -> Unit = {  }
                    }
                    """.trimIndent()
                val formattedCode =
                    """
                    fun foo() {
                        val bar1 = 3
                        val bar2 = 2
                        val fooBar1: String = ""
                        val fooBar2: () -> Unit = {  }
                    }
                    """.trimIndent()
                semicolonRuleAssertThat(code)
                    .hasLintViolations(
                        LintViolation(2, 17, "Replace ';' with newline"),
                        LintViolation(3, 29, "Replace ';' with newline"),
                    ).isFormattedAs(formattedCode)
            }

            @Test
            fun `Given more than two variables`() {
                val code =
                    """
                    fun foo() {
                        val bar1 = 3; val bar2 = 2; val bar3 = 3; val bar4: () -> Unit = {  }; val bar4: String = "";
                    }
                    """.trimIndent()
                val formattedCode =
                    """
                    fun foo() {
                        val bar1 = 3
                        val bar2 = 2
                        val bar3 = 3
                        val bar4: () -> Unit = {  }
                        val bar4: String = ""
                    }
                    """.trimIndent()
                semicolonRuleAssertThat(code)
                    .hasLintViolations(
                        LintViolation(2, 17, "Replace ';' with newline"),
                        LintViolation(2, 31, "Replace ';' with newline"),
                        LintViolation(2, 45, "Replace ';' with newline"),
                        LintViolation(2, 74, "Replace ';' with newline"),
                        LintViolation(2, 97, "Unnecessary semicolon"),
                    ).isFormattedAs(formattedCode)
            }

            @Test
            fun `Given variables with comments`() {
                val code =
                    """
                    fun foo() {
                        val bar1 = 3; val bar2 = 2; // this is end comment
                        val bar1 = 3; /* block comment */ val bar2 = 2;
                    }
                    """.trimIndent()
                val formattedCode =
                    """
                    fun foo() {
                        val bar1 = 3
                        val bar2 = 2 // this is end comment
                        val bar1 = 3
                        /* block comment */ val bar2 = 2
                    }
                    """.trimIndent()
                semicolonRuleAssertThat(code)
                    .hasLintViolations(
                        LintViolation(2, 17, "Replace ';' with newline"),
                        LintViolation(2, 31, "Unnecessary semicolon"),
                        LintViolation(3, 17, "Replace ';' with newline"),
                        LintViolation(3, 51, "Unnecessary semicolon"),
                    ).isFormattedAs(formattedCode)
            }
        }

        @Nested
        inner class `Given multiple classes, functions and init blocks` {
            @Test
            fun `Given multiple function declaration`() {
                val code =
                    """
                    public fun foo1() {
                        // no-op
                    }; public fun foo2() {
                        // no-op
                    }; fun foo3() = 0

                    public fun foo4() = 1; public fun foo5() {
                        // no-op
                    }
                    """.trimIndent()
                val formattedCode =
                    """
                    public fun foo1() {
                        // no-op
                    }
                    public fun foo2() {
                        // no-op
                    }
                    fun foo3() = 0

                    public fun foo4() = 1
                    public fun foo5() {
                        // no-op
                    }
                    """.trimIndent()
                semicolonRuleAssertThat(code)
                    .hasLintViolations(
                        LintViolation(3, 2, "Replace ';' with newline"),
                        LintViolation(5, 2, "Replace ';' with newline"),
                        LintViolation(7, 22, "Replace ';' with newline"),
                    ).isFormattedAs(formattedCode)
            }

            @Test
            fun `Given multiple function declaration with comments`() {
                val code =
                    """
                    public fun foo1() {
                        // no-op
                    }; /* block comment */ public fun foo2() {
                        // no-op
                    }; fun foo3() = 0 // single line comment
                    """.trimIndent()
                val formattedCode =
                    """
                    public fun foo1() {
                        // no-op
                    }
                    /* block comment */ public fun foo2() {
                        // no-op
                    }
                    fun foo3() = 0 // single line comment
                    """.trimIndent()
                semicolonRuleAssertThat(code)
                    .hasLintViolations(
                        LintViolation(3, 2, "Replace ';' with newline"),
                        LintViolation(5, 2, "Replace ';' with newline"),
                    ).isFormattedAs(formattedCode)
            }

            @Test
            fun `Given multiple function invocations`() {
                val code =
                    """
                    class Bar {
                        public fun foo1() = 0
                        fun foo2() = 0
                        fun foo3(lambda: () -> Unit) = 0

                        init {
                            foo1(); foo3 {  }; foo2()
                        }
                    }
                    """.trimIndent()
                val formattedCode =
                    """
                    class Bar {
                        public fun foo1() = 0
                        fun foo2() = 0
                        fun foo3(lambda: () -> Unit) = 0

                        init {
                            foo1()
                            foo3 {  }
                            foo2()
                        }
                    }
                    """.trimIndent()
                semicolonRuleAssertThat(code)
                    .hasLintViolations(
                        LintViolation(7, 15, "Replace ';' with newline"),
                        LintViolation(7, 26, "Replace ';' with newline"),
                    ).isFormattedAs(formattedCode)
            }

            @Test
            fun `Given a multiline class declaration`() {
                val code =
                    """
                    public class FooBar1 {

                    }; public class FooBar2 {

                    }

                    public class FooBar3; public class FooBar4
                    """.trimIndent()
                val formattedCode =
                    """
                    public class FooBar1 {

                    }
                    public class FooBar2 {

                    }

                    public class FooBar3
                    public class FooBar4
                    """.trimIndent()
                semicolonRuleAssertThat(code)
                    .hasLintViolations(
                        LintViolation(3, 2, "Replace ';' with newline"),
                        LintViolation(7, 21, "Replace ';' with newline"),
                    ).isFormattedAs(formattedCode)
            }

            @Test
            fun `Given a multiline class declaration with comments`() {
                val code =
                    """
                    public class FooBar1 {

                    }; /* block comment */ public class FooBar2 {

                    }; public class FooBar2 {

                    } // single line comment
                    """.trimIndent()
                val formattedCode =
                    """
                    public class FooBar1 {

                    }
                    /* block comment */ public class FooBar2 {

                    }
                    public class FooBar2 {

                    } // single line comment
                    """.trimIndent()
                semicolonRuleAssertThat(code)
                    .hasLintViolations(
                        LintViolation(3, 2, "Replace ';' with newline"),
                        LintViolation(5, 2, "Replace ';' with newline"),
                    ).isFormattedAs(formattedCode)
            }

            @Test
            fun `Given a multiple init block`() {
                val code =
                    """
                    public class Foo {
                        init {

                        };init {

                        }
                    }
                    """.trimIndent()
                val formattedCode =
                    """
                    public class Foo {
                        init {

                        }
                        init {

                        }
                    }
                    """.trimIndent()
                semicolonRuleAssertThat(code)
                    .hasLintViolation(4, 6, "Replace ';' with newline")
                    .isFormattedAs(formattedCode)
            }

            @Test
            fun `Given a multiple init block and variables with nested violations`() {
                val code =
                    """
                    public class Foo {
                        init {
                            val bar1 = 0; val bar2 = 0;
                        };init {
                            val bar3 = 0; val bar4 = 0;
                        }
                    }
                    """.trimIndent()
                val formattedCode =
                    """
                    public class Foo {
                        init {
                            val bar1 = 0
                            val bar2 = 0
                        }
                        init {
                            val bar3 = 0
                            val bar4 = 0
                        }
                    }
                    """.trimIndent()
                semicolonRuleAssertThat(code)
                    .hasLintViolations(
                        LintViolation(3, 21, "Replace ';' with newline"),
                        LintViolation(3, 35, "Unnecessary semicolon"),
                        LintViolation(4, 6, "Replace ';' with newline"),
                        LintViolation(5, 21, "Replace ';' with newline"),
                        LintViolation(5, 35, "Unnecessary semicolon"),
                    ).isFormattedAs(formattedCode)
            }
        }

        @Nested
        inner class `Given flow control statements` {
            @Test
            fun `Given a multiple for statements`() {
                val code =
                    """
                    fun test() {
                        for (i in 0..10) {
                            println(i)
                        }; for (i in 0..100) {
                            println(i)
                        }; for (i in 0..1000) {
                            println(i)
                        }
                    }
                    """.trimIndent()
                val formattedCode =
                    """
                    fun test() {
                        for (i in 0..10) {
                            println(i)
                        }
                        for (i in 0..100) {
                            println(i)
                        }
                        for (i in 0..1000) {
                            println(i)
                        }
                    }
                    """.trimIndent()
                semicolonRuleAssertThat(code)
                    .hasLintViolations(
                        LintViolation(4, 6, "Replace ';' with newline"),
                        LintViolation(6, 6, "Replace ';' with newline"),
                    ).isFormattedAs(formattedCode)
            }

            @Test
            fun `Given a multiline while statements`() {
                val code =
                    """
                    fun test() {
                        while (System.currentTimeMillis() % 2 == 0L) {
                            println(System.currentTimeMillis())
                        }; while (Random(System.currentTimeMillis()).nextBoolean()) {
                            println(System.currentTimeMillis())
                        }
                    }
                    """.trimIndent()
                val formattedCode =
                    """
                    fun test() {
                        while (System.currentTimeMillis() % 2 == 0L) {
                            println(System.currentTimeMillis())
                        }
                        while (Random(System.currentTimeMillis()).nextBoolean()) {
                            println(System.currentTimeMillis())
                        }
                    }
                    """.trimIndent()
                semicolonRuleAssertThat(code)
                    .hasLintViolation(4, 6, "Replace ';' with newline")
                    .isFormattedAs(formattedCode)
            }

            @Test
            fun `Given a multiline do-while statements`() {
                val code =
                    """
                    fun test() {
                        while (System.currentTimeMillis() % 2 == 0L) {
                            println(System.currentTimeMillis())
                        }; do {
                            println(System.currentTimeMillis())
                        } while (System.currentTimeMillis() % 2 == 0L); do {
                            println(System.currentTimeMillis())
                        } while (System.currentTimeMillis() % 2 == 0L)
                    }
                    """.trimIndent()
                val formattedCode =
                    """
                    fun test() {
                        while (System.currentTimeMillis() % 2 == 0L) {
                            println(System.currentTimeMillis())
                        }
                        do {
                            println(System.currentTimeMillis())
                        } while (System.currentTimeMillis() % 2 == 0L)
                        do {
                            println(System.currentTimeMillis())
                        } while (System.currentTimeMillis() % 2 == 0L)
                    }
                    """.trimIndent()
                semicolonRuleAssertThat(code)
                    .hasLintViolations(
                        LintViolation(4, 6, "Replace ';' with newline"),
                        LintViolation(6, 51, "Replace ';' with newline"),
                    ).isFormattedAs(formattedCode)
            }

            @Test
            fun `Given a multiline semi separated control flow with no body`() {
                val code =
                    """
                    fun test() {
                        for (i in 0..10); for (i in 0..100);while (System.currentTimeMillis() % 2 == 0L); while (Random(System.currentTimeMillis()).nextBoolean());
                    }
                    """.trimIndent()
                val formattedCode =
                    """
                    fun test() {
                        for (i in 0..10);
                        for (i in 0..100);
                        while (System.currentTimeMillis() % 2 == 0L);
                        while (Random(System.currentTimeMillis()).nextBoolean());
                    }
                    """.trimIndent()
                semicolonRuleAssertThat(code)
                    .hasLintViolations(
                        LintViolation(2, 22, "Missing newline after ';'"),
                        LintViolation(2, 41, "Missing newline after ';'"),
                        LintViolation(2, 86, "Missing newline after ';'"),
                    ).isFormattedAs(formattedCode)
            }
        }

        @Test
        fun `Given a multiline semi separated import statement then wrap each expression to a new line`() {
            val code =
                """
                import java.util.ArrayList; import java.util.HashMap
                """.trimIndent()
            val formattedCode =
                """
                import java.util.ArrayList
                import java.util.HashMap
                """.trimIndent()
            semicolonRuleAssertThat(code)
                .hasLintViolation(1, 27, "Replace ';' with newline")
                .isFormattedAs(formattedCode)
        }

        @Test
        fun `Given a multiline semi separated with variables, flow controls and method calls`() {
            val code =
                """
                fun test() {
                    val a = 0; val b = 0; fun bar() {
                        // no-op
                    }; for(i in 0..10) {
                        println(i); println(i); a++; println(a)
                    }
                }
                """.trimIndent()
            val formattedCode =
                """
                fun test() {
                    val a = 0
                    val b = 0
                    fun bar() {
                        // no-op
                    }
                    for(i in 0..10) {
                        println(i)
                        println(i)
                        a++
                        println(a)
                    }
                }
                """.trimIndent()
            semicolonRuleAssertThat(code)
                .hasLintViolations(
                    LintViolation(2, 14, "Replace ';' with newline"),
                    LintViolation(2, 25, "Replace ';' with newline"),
                    LintViolation(4, 6, "Replace ';' with newline"),
                    LintViolation(5, 19, "Replace ';' with newline"),
                    LintViolation(5, 31, "Replace ';' with newline"),
                    LintViolation(5, 36, "Replace ';' with newline"),
                ).isFormattedAs(formattedCode)
        }

        @Nested
        inner class `Given enum class` {
            @Test
            fun `Given a enum with semicolon after last value`() {
                val code =
                    """
                    enum class FOO1 { ONE, TWO, THREE; }
                    enum class FOO2 {
                        ONE, TWO, THREE;
                    }
                    enum class FOO3 {
                        ONE,
                        TWO,
                        THREE;
                    }
                    enum class FOO4 {
                        ONE,
                        TWO,
                        THREE,
                        ;
                    }
                    enum class FOO5 {
                        ONE,
                        TWO,
                        THREE,
                        ;
                        fun foo() = ""
                    }
                    """.trimIndent()
                val formattedCode =
                    """
                    enum class FOO1 { ONE, TWO, THREE }
                    enum class FOO2 {
                        ONE, TWO, THREE
                    }
                    enum class FOO3 {
                        ONE,
                        TWO,
                        THREE
                    }
                    enum class FOO4 {
                        ONE,
                        TWO,
                        THREE,
                    }
                    enum class FOO5 {
                        ONE,
                        TWO,
                        THREE,
                        ;
                        fun foo() = ""
                    }
                    """.trimIndent()
                semicolonRuleAssertThat(code)
                    .hasLintViolations(
                        LintViolation(1, 34, "Unnecessary semicolon"),
                        LintViolation(3, 20, "Unnecessary semicolon"),
                        LintViolation(8, 10, "Unnecessary semicolon"),
                        LintViolation(14, 5, "Unnecessary semicolon"),
                    ).isFormattedAs(formattedCode)
            }

            @Test
            fun `Given enum class with methods`() {
                val code =
                    """
                    enum class FOO {
                        A, B, C; fun test() = 0
                    }
                    """.trimIndent()
                val formattedCode =
                    """
                    enum class FOO {
                        A, B, C;
                        fun test() = 0
                    }
                    """.trimIndent()
                semicolonRuleAssertThat(code)
                    .hasLintViolation(2, 13, "Missing newline after ';'")
                    .isFormattedAs(formattedCode)
            }
        }

        @Nested
        inner class `Given companion or object class` {
            @Test
            fun `Given a companion object with semicolon and variable`() {
                val code =
                    """
                    class Foo() {
                        companion object; private var toto: Boolean = false
                    }
                    """.trimIndent()
                val formattedCode =
                    """
                    class Foo() {
                        companion object;
                        private var toto: Boolean = false
                    }
                    """.trimIndent()
                semicolonRuleAssertThat(code)
                    .hasLintViolation(2, 22, "Missing newline after ';'")
                    .isFormattedAs(formattedCode)
            }
        }

        @Test
        fun `Given a single line block containing multiple statements then reformat block after wrapping the statement`() {
            val code =
                """
                val fooBar =
                    fooBar()
                        .map { foo(); bar() }
                """.trimIndent()
            val formattedCode =
                """
                val fooBar =
                    fooBar()
                        .map {
                            foo()
                            bar()
                        }
                """.trimIndent()
            semicolonRuleAssertThat(code)
                .addAdditionalRuleProvider { StatementWrappingRule() }
                .addAdditionalRuleProvider { IndentationRule() }
                .hasLintViolation(3, 21, "Replace ';' with newline")
                .isFormattedAs(formattedCode)
        }

        @Test
        fun `Given a single line lambda expression containing multiple statements then reformat block after wrapping the statement`() {
            val code =
                """
                val fooBar =
                    fooBar()
                        .map { foo, bar -> print(foo); print(bar) }
                """.trimIndent()
            val formattedCode =
                """
                val fooBar =
                    fooBar()
                        .map { foo, bar ->
                            print(foo)
                            print(bar)
                        }
                """.trimIndent()
            semicolonRuleAssertThat(code)
                .addAdditionalRuleProvider { StatementWrappingRule() }
                .addAdditionalRuleProvider { IndentationRule() }
                .hasLintViolation(3, 38, "Replace ';' with newline")
                .isFormattedAs(formattedCode)
        }

        @Test
        fun `Given a single line when entry block containing multiple statements then reformat block after wrapping the statement`() {
            val code =
                """
                val foo =
                    when (value) {
                        0 -> { foo(); true }
                        else -> { bar(); false }
                    }
                """.trimIndent()
            val formattedCode =
                """
                val foo =
                    when (value) {
                        0 -> {
                            foo()
                            true
                        }
                        else -> {
                            bar()
                            false
                        }
                    }
                """.trimIndent()
            semicolonRuleAssertThat(code)
                .addAdditionalRuleProvider { StatementWrappingRule() }
                .addAdditionalRuleProvider { IndentationRule() }
                .hasLintViolations(
                    LintViolation(3, 21, "Replace ';' with newline"),
                    LintViolation(4, 24, "Replace ';' with newline"),
                ).isFormattedAs(formattedCode)
        }
    }

    @Test
    fun `Given multiple statements on a single line and a space before the semicolon then wrap`() {
        val code =
            """
            fun foo() {
                val bar1 = 1; val bar2 = 2
                val bar3 = 3 ;val bar4 = 4
                val bar5 = 5; val bar6 = 6
                val bar7 = 7 ; val bar8 = 8
            }
            """.trimIndent()
        val formattedCode =
            """
            fun foo() {
                val bar1 = 1
                val bar2 = 2
                val bar3 = 3
                val bar4 = 4
                val bar5 = 5
                val bar6 = 6
                val bar7 = 7
                val bar8 = 8
            }
            """.trimIndent()
        semicolonRuleAssertThat(code)
            .hasLintViolations(
                LintViolation(2, 17, "Replace ';' with newline"),
                LintViolation(3, 18, "Replace ';' with newline"),
                LintViolation(4, 17, "Replace ';' with newline"),
                LintViolation(5, 18, "Replace ';' with newline"),
            ).isFormattedAs(formattedCode)
    }

    @Test
    fun `Given multiple statements on a single line separated by two semicolons without statement in between then wrap the statements`() {
        val code =
            """
            fun foo() {
                // Note: ";;" without whitespace in between results in Kotlin compilation error
                val bar1 = 1; ;val bar2 = 2
                val bar3 = 3 ; ;val bar4 = 4
                val bar5 = 5; ; val bar6 = 6
                val bar7 = 7 ; ; val bar8 = 8
            }
            """.trimIndent()
        val formattedCode =
            """
            fun foo() {
                // Note: ";;" without whitespace in between results in Kotlin compilation error
                val bar1 = 1
                val bar2 = 2
                val bar3 = 3
                val bar4 = 4
                val bar5 = 5
                val bar6 = 6
                val bar7 = 7
                val bar8 = 8
            }
            """.trimIndent()
        semicolonRuleAssertThat(code)
            .hasLintViolations(
                LintViolation(3, 17, "Replace ';' with newline"),
                LintViolation(3, 19, "Replace ';' with newline"),
                LintViolation(4, 18, "Replace ';' with newline"),
                LintViolation(4, 20, "Replace ';' with newline"),
                LintViolation(5, 17, "Replace ';' with newline"),
                LintViolation(5, 19, "Replace ';' with newline"),
                LintViolation(6, 18, "Replace ';' with newline"),
                LintViolation(6, 20, "Replace ';' with newline"),
            ).isFormattedAs(formattedCode)
    }

    @Test
    fun `Given single statements on a single line and a space before the semicolon then do not leave a trailing space`() {
        val code =
            """
            fun foo() {
                val bar1 = 1 ;
                val bar2 = 2;
                val bar3 = 3;$SPACE
                val bar4 = 4 ;$SPACE
            }
            """.trimIndent()
        val formattedCode =
            """
            fun foo() {
                val bar1 = 1
                val bar2 = 2
                val bar3 = 3
                val bar4 = 4
            }
            """.trimIndent()
        semicolonRuleAssertThat(code)
            .hasLintViolations(
                LintViolation(2, 18, "Unnecessary semicolon"),
                LintViolation(3, 17, "Unnecessary semicolon"),
                LintViolation(4, 17, "Unnecessary semicolon"),
                LintViolation(5, 18, "Unnecessary semicolon"),
            ).isFormattedAs(formattedCode)
    }
}
