package io.github.ktlint.core.ruleset.standard.rules

import io.github.ktlint.core.test.KtLintAssertThat
import io.github.ktlint.core.test.LintViolation
import org.junit.jupiter.api.Test

class StatementWrappingRuleTest {
    private val statementWrappingRuleAssertThat =
        KtLintAssertThat.assertThatRule { StatementWrappingRule() }

    @Test
    fun `Given a function body with first statement at the same line as lbrace`() {
        val code =
            """
            fun foo() { doSomething()
            }
            """.trimIndent()
        val formattedCode =
            """
            fun foo() {
                doSomething()
            }
            """.trimIndent()
        statementWrappingRuleAssertThat(code)
            .hasLintViolation(1, 13, "Missing newline after '{'")
            .isFormattedAs(formattedCode)
    }

    @Test
    fun `Given a function body with last statement ending on same line as rbrace`() {
        val code =
            """
            fun foo() {
                doSomething() }
            """.trimIndent()
        val formattedCode =
            """
            fun foo() {
                doSomething()
            }
            """.trimIndent()
        statementWrappingRuleAssertThat(code)
            .hasLintViolation(2, 19, "Missing newline before '}'")
            .isFormattedAs(formattedCode)
    }

    @Test
    fun `Given a function body with statement on separate line`() {
        val code =
            """
            fun foo() {
                doSomething()
            }
            """.trimIndent()
        statementWrappingRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `Given a function with a single line body on same line as fun declaration`() {
        val code =
            """
            fun foo() { doSomething() }
            """.trimIndent()
        val formattedCode =
            """
            fun foo() {
                doSomething()
            }
            """.trimIndent()
        statementWrappingRuleAssertThat(code)
            .hasLintViolations(
                LintViolation(1, 13, "Missing newline after '{'"),
                LintViolation(1, 27, "Missing newline before '}'"),
            ).isFormattedAs(formattedCode)
    }

    @Test
    fun `Given a function with a multiline body and statements on same line as lbrace and rbrace`() {
        val code =
            """
            fun foo() { doSomething()
                doSomething() }
            fun foo() { if (true) {
                    doSomething()
                }}
            """.trimIndent()
        val formattedCode =
            """
            fun foo() {
                doSomething()
                doSomething()
            }
            fun foo() {
                if (true) {
                    doSomething()
                }
            }
            """.trimIndent()
        statementWrappingRuleAssertThat(code)
            .hasLintViolations(
                LintViolation(1, 13, "Missing newline after '{'"),
                LintViolation(2, 19, "Missing newline before '}'"),
                LintViolation(3, 13, "Missing newline after '{'"),
                LintViolation(5, 6, "Missing newline before '}'"),
            ).isFormattedAs(formattedCode)
    }

    @Test
    fun `Given a function with a single line body with no more than one statement on same line a fun declaration`() {
        val code =
            """
            fun foo1() {}
            fun foo2() { }
            fun foo3() { /* no-op */ }
            """.trimIndent()
        statementWrappingRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `Given a single line function literal`() {
        val code =
            """
            val foo1 = {}
            val foo2 = { }
            val foo3 = { /* no-op */ }
            val foo4 = { doSomething() }
            val foo5 = { bar -> doSomething(bar) }
            """.trimIndent()
        statementWrappingRuleAssertThat(code)
            .hasNoLintViolations()
    }

    @Test
    fun `Given a multiline line function literal with first statement on same line as lbrace`() {
        val code =
            """
            val foo = {doSomething()
            }
            """.trimIndent()
        val formattedCode =
            """
            val foo = {
                doSomething()
            }
            """.trimIndent()
        statementWrappingRuleAssertThat(code)
            .hasLintViolation(1, 12, "Missing newline after '{'")
            .isFormattedAs(formattedCode)
    }

    @Test
    fun `Given a multiline line function literal with last statement on same line as rbrace`() {
        val code =
            """
            val foo = {
                doSomething()}
            """.trimIndent()
        val formattedCode =
            """
            val foo = {
                doSomething()
            }
            """.trimIndent()
        statementWrappingRuleAssertThat(code)
            .hasLintViolation(2, 18, "Missing newline before '}'")
            .isFormattedAs(formattedCode)
    }

    @Test
    fun `Given a when-body with statements on same line as lbrace and rbrace`() {
        val code =
            """
            fun test(a: Int) {
                when (a) { 1 -> "foo"
                    2 -> "bar" }
            }
            """.trimIndent()
        val formattedCode =
            """
            fun test(a: Int) {
                when (a) {
                    1 -> "foo"
                    2 -> "bar"
                }
            }
            """.trimIndent()
        statementWrappingRuleAssertThat(code)
            .hasLintViolations(
                LintViolation(2, 16, "Missing newline after '{'"),
                LintViolation(3, 20, "Missing newline before '}'"),
            ).isFormattedAs(formattedCode)
    }

    @Test
    fun `Given a for-body with statements on same line as lbrace and rbrace`() {
        val code =
            """
            fun test(a: Int) {
                for (i in 1..10) { doSomething()
                    doSomething() }
            }
            """.trimIndent()
        val formattedCode =
            """
            fun test(a: Int) {
                for (i in 1..10) {
                    doSomething()
                    doSomething()
                }
            }
            """.trimIndent()
        statementWrappingRuleAssertThat(code)
            .hasLintViolations(
                LintViolation(2, 24, "Missing newline after '{'"),
                LintViolation(3, 23, "Missing newline before '}'"),
            ).isFormattedAs(formattedCode)
    }

    @Test
    fun `Given a while-body with statements on same line as lbrace and rbrace`() {
        val code =
            """
            fun test(a: Int) {
                while(a < Int.MAX) { doSomething()
                    doSomething() }
            }
            """.trimIndent()
        val formattedCode =
            """
            fun test(a: Int) {
                while(a < Int.MAX) {
                    doSomething()
                    doSomething()
                }
            }
            """.trimIndent()
        statementWrappingRuleAssertThat(code)
            .hasLintViolations(
                LintViolation(2, 26, "Missing newline after '{'"),
                LintViolation(3, 23, "Missing newline before '}'"),
            ).isFormattedAs(formattedCode)
    }

    @Test
    fun `Given a do-while-body with statements on same line as lbrace and rbrace`() {
        val code =
            """
            fun test(a: Int) {
                do { doSomething()
                    doSomething() } while (a < Int.MAX)
            }
            """.trimIndent()
        val formattedCode =
            """
            fun test(a: Int) {
                do {
                    doSomething()
                    doSomething()
                } while (a < Int.MAX)
            }
            """.trimIndent()
        statementWrappingRuleAssertThat(code)
            .hasLintViolations(
                LintViolation(2, 10, "Missing newline after '{'"),
                LintViolation(3, 23, "Missing newline before '}'"),
            ).isFormattedAs(formattedCode)
    }

    @Test
    fun `Given a try-catch-finally with statements on same line as lbrace`() {
        val code =
            """
            fun test(a: Int) {
                try { doSomething()
                } catch (e: Exception) { doSomething()
                } finally { doSomething()
                }
            }
            """.trimIndent()
        val formattedCode =
            """
            fun test(a: Int) {
                try {
                    doSomething()
                } catch (e: Exception) {
                    doSomething()
                } finally {
                    doSomething()
                }
            }
            """.trimIndent()
        statementWrappingRuleAssertThat(code)
            .hasLintViolations(
                LintViolation(2, 11, "Missing newline after '{'"),
                LintViolation(3, 30, "Missing newline after '{'"),
                LintViolation(4, 17, "Missing newline after '{'"),
            ).isFormattedAs(formattedCode)
    }

    @Test
    fun `Given a try-catch-finally with statements on same line as rbrace`() {
        val code =
            """
            fun test(a: Int) {
                try {
                    doSomething() } catch (e: Exception) {
                    doSomething() } finally {
                    doSomething() }
            }
            """.trimIndent()
        val formattedCode =
            """
            fun test(a: Int) {
                try {
                    doSomething()
                } catch (e: Exception) {
                    doSomething()
                } finally {
                    doSomething()
                }
            }
            """.trimIndent()
        statementWrappingRuleAssertThat(code)
            .hasLintViolations(
                LintViolation(3, 23, "Missing newline before '}'"),
                LintViolation(4, 23, "Missing newline before '}'"),
                LintViolation(5, 23, "Missing newline before '}'"),
            ).isFormattedAs(formattedCode)
    }

    @Test
    fun `Given try-catch-finally body statements align with corresponding rbrace`() {
        val code =
            """
            fun test(a: Int) {
                try {
                    println("1")
                    println("2")  } catch (e: Exception) {
                    println("1")
                    println("2") } finally {
                    println("1")
                    println("2")}
            }
            """.trimIndent()
        val formattedCode =
            """
            fun test(a: Int) {
                try {
                    println("1")
                    println("2")
                } catch (e: Exception) {
                    println("1")
                    println("2")
                } finally {
                    println("1")
                    println("2")
                }
            }
            """.trimIndent()
        statementWrappingRuleAssertThat(code)
            .hasLintViolations(
                LintViolation(4, 23, "Missing newline before '}'"),
                LintViolation(6, 22, "Missing newline before '}'"),
                LintViolation(8, 21, "Missing newline before '}'"),
            ).isFormattedAs(formattedCode)
    }

    @Test
    fun `Given class body with first statement on same line as lbrace`() {
        val code =
            """
            class Foo1 { init {
                    doSomething()
                }
            }
            class Foo2 {
                companion object { init {
                        doSomething()
                    }
                }
            }
            """.trimIndent()
        val formattedCode =
            """
            class Foo1 {
                init {
                    doSomething()
                }
            }
            class Foo2 {
                companion object {
                    init {
                        doSomething()
                    }
                }
            }
            """.trimIndent()
        statementWrappingRuleAssertThat(code)
            .hasLintViolations(
                LintViolation(1, 14, "Missing newline after '{'"),
                LintViolation(6, 24, "Missing newline after '{'"),
            ).isFormattedAs(formattedCode)
    }

    @Test
    fun `Given class body with first statement on same line as rbrace`() {
        val code =
            """
            class Foo1 {
                init {
                    doSomething()
                } }
            class Foo2 {
                companion object {
                    init {
                        doSomething() }
                }
            }
            """.trimIndent()
        val formattedCode =
            """
            class Foo1 {
                init {
                    doSomething()
                }
            }
            class Foo2 {
                companion object {
                    init {
                        doSomething()
                    }
                }
            }
            """.trimIndent()
        statementWrappingRuleAssertThat(code)
            .hasLintViolations(
                LintViolation(4, 7, "Missing newline before '}'"),
                LintViolation(8, 27, "Missing newline before '}'"),
            ).isFormattedAs(formattedCode)
    }

    @Test
    fun `Given lambda with parameters and statement on separate line`() {
        val code =
            """
            val fooBar =
                foo { bar ->
                    doSomething(bar)
                }
            """.trimIndent()
        statementWrappingRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `Given lambda with parameters and statement on same line as arrow`() {
        val code =
            """
            val fooBar =
                foo { bar -> doSomething(bar)
                }
            """.trimIndent()
        val formattedCode =
            """
            val fooBar =
                foo { bar ->
                    doSomething(bar)
                }
            """.trimIndent()
        statementWrappingRuleAssertThat(code)
            .hasLintViolation(2, 18, "Missing newline after '->'")
            .isFormattedAs(formattedCode)
    }

    @Test
    fun `Issue 2177 - Given a single line enumeration then do not wrap the values`() {
        val code =
            """
            enum class FooBar { FOO, BAR }
            """.trimIndent()
        statementWrappingRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `Given a single line enumeration class preceded by a comment`() {
        val code =
            """
            /**
             * Some comment
             */
            enum class Foobar { FOO, BAR }
            """.trimIndent()
        statementWrappingRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `Given a single line enumeration class preceded by one or more annotations`() {
        val code =
            """
            @FooBar
            enum class Foobar { FOO, BAR }
            """.trimIndent()
        statementWrappingRuleAssertThat(code).hasNoLintViolations()
    }
}
