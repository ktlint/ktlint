package io.github.ktlint.core.ruleset.standard.rules

import io.github.ktlint.core.rule.engine.core.api.editorconfig.RuleExecution.DISABLED
import io.github.ktlint.core.rule.engine.core.api.editorconfig.RuleExecution.ENABLED
import io.github.ktlint.core.rule.engine.core.api.editorconfig.createRuleExecutionEditorConfigProperty
import io.github.ktlint.core.test.KtLintAssertThat.Companion.EOL_CHAR
import io.github.ktlint.core.test.KtLintAssertThat.Companion.MAX_LINE_LENGTH_MARKER
import io.github.ktlint.core.test.KtLintAssertThat.Companion.assertThatRuleBuilder
import io.github.ktlint.core.test.LintViolation
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class FunctionLiteralRuleTest {
    private val functionLiteralRuleAssertThat =
        assertThatRuleBuilder { FunctionLiteralRule() }
            .addAdditionalRuleProvider { MaxLineLengthRule() }
            .assertThat()

    @Test
    fun `Given a single line lambda without parameters`() {
        val code =
            """
            val foobar = { foo + bar }
            """.trimIndent()
        functionLiteralRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `Given a multiline lambda without parameters`() {
        val code =
            """
            val foobar =
                {
                    foo + bar
                }
            """.trimIndent()
        functionLiteralRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `Given a lambda with a single parameter fitting on the first line`() {
        val code =
            """
            val foobar =
                { foo: Foo ->
                    foo.repeat(2)
                }
            """.trimIndent()
        functionLiteralRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `Given a lambda with a single parameter not on same line as opening brace`() {
        val code =
            """
            val foobar =
                {
                    foo: Foo ->
                    foo.repeat(2)
                }
            """.trimIndent()
        val formattedCode =
            """
            val foobar =
                { foo: Foo ->
                    foo.repeat(2)
                }
            """.trimIndent()
        functionLiteralRuleAssertThat(code)
            .hasLintViolation(3, 9, "No newline expected before parameter")
            .isFormattedAs(formattedCode)
    }

    @Test
    fun `Given a lambda with a single parameter and arrow on separate line`() {
        val code =
            """
            val foobar =
                { foo: Foo
                    ->
                    foo.repeat(2)
                }
            """.trimIndent()
        val formattedCode =
            """
            val foobar =
                { foo: Foo ->
                    foo.repeat(2)
                }
            """.trimIndent()
        functionLiteralRuleAssertThat(code)
            .hasLintViolation(2, 15, "No newline expected after parameter")
            .isFormattedAs(formattedCode)
    }

    @Test
    fun `Given a lambda with a single parameter not fitting on the first line`() {
        val code =
            """
            // $MAX_LINE_LENGTH_MARKER  $EOL_CHAR
            val foobar =
                { fooooooooooooooo: Foo ->
                    foo.repeat(2)
                }
            """.trimIndent()
        functionLiteralRuleAssertThat(code)
            .setMaxLineLength()
            .hasNoLintViolationsExceptInAdditionalRules()
    }

    @Test
    fun `Given a call expression followed by a lambda with a single parameter not fitting on the same line as the opening brace`() {
        val code =
            """
            // $MAX_LINE_LENGTH_MARKER  $EOL_CHAR
            val foobar =
                barrrrrrrrrr { foooooooooooo: Foo ->
                    foo.repeat(2)
                }
            """.trimIndent()
        val formattedCode =
            """
            // $MAX_LINE_LENGTH_MARKER  $EOL_CHAR
            val foobar =
                barrrrrrrrrr {
                    foooooooooooo: Foo
                    ->
                    foo.repeat(2)
                }
            """.trimIndent()
        functionLiteralRuleAssertThat(code)
            .setMaxLineLength()
            .hasLintViolations(
                LintViolation(3, 20, "Newline expected before parameter"),
                LintViolation(3, 39, "Newline expected before arrow"),
            ).isFormattedAs(formattedCode)
    }

    @Test
    fun `Given a lambda with multiple parameters fitting on the first line`() {
        val code =
            """
            val foobar =
                { foo: Foo, bar: Bar ->
                    foo + bar
                }
            """.trimIndent()
        functionLiteralRuleAssertThat(code).hasNoLintViolations()
    }

    @Nested
    inner class `Given a lambda with multiple parameters but not fitting on the first line` {
        private val code =
            """
            // $MAX_LINE_LENGTH_MARKER  $EOL_CHAR
            val foobar =
                { fooooo: Foo, bar: Bar ->
                    foo + bar
                }
            """.trimIndent()

        @Test
        fun `Given that max-line-length rule is enabled`() {
            val formattedCode =
                """
                // $MAX_LINE_LENGTH_MARKER  $EOL_CHAR
                val foobar =
                    {
                        fooooo: Foo,
                        bar: Bar
                        ->
                        foo + bar
                    }
                """.trimIndent()
            functionLiteralRuleAssertThat(code)
                .setMaxLineLength()
                .withEditorConfigOverride(MAX_LINE_LENGTH_RULE_ID.createRuleExecutionEditorConfigProperty() to ENABLED)
                .hasLintViolations(
                    LintViolation(3, 7, "Newline expected before parameter"),
                    LintViolation(3, 20, "Newline expected before parameter"),
                    LintViolation(3, 29, "Newline expected before arrow"),
                ).isFormattedAs(formattedCode)
        }

        @Test
        fun `Given that max-line-length rule is disabled`() {
            functionLiteralRuleAssertThat(code)
                .setMaxLineLength()
                .withEditorConfigOverride(MAX_LINE_LENGTH_RULE_ID.createRuleExecutionEditorConfigProperty() to DISABLED)
                .hasNoLintViolations()
        }
    }

    @Test
    fun `Given a lambda with multiple parameters of which some are not fitting on line`() {
        val code =
            """
            // $MAX_LINE_LENGTH_MARKER  $EOL_CHAR
            val foobar =
                { fooooooooooo: Foo, bar: Bar ->
                    foo.repeat(2)
                }
            """.trimIndent()
        val formattedCode =
            """
            // $MAX_LINE_LENGTH_MARKER  $EOL_CHAR
            val foobar =
                {
                    fooooooooooo: Foo,
                    bar: Bar
                    ->
                    foo.repeat(2)
                }
            """.trimIndent()
        functionLiteralRuleAssertThat(code)
            .setMaxLineLength()
            .hasLintViolations(
                LintViolation(3, 7, "Newline expected before parameter"),
                LintViolation(3, 26, "Newline expected before parameter"),
                LintViolation(3, 35, "Newline expected before arrow"),
            ).isFormattedAs(formattedCode)
    }

    @Test
    fun `Given a lambda with a multiline parameter list starting on same line as opening brace`() {
        val code =
            """
            // $MAX_LINE_LENGTH_MARKER  $EOL_CHAR
            val foobar =
                { foo: Foo,
                    bar: Bar ->
                    foo + bar
                }
            """.trimIndent()
        val formattedCode =
            """
            // $MAX_LINE_LENGTH_MARKER  $EOL_CHAR
            val foobar =
                {
                    foo: Foo,
                    bar: Bar
                    ->
                    foo + bar
                }
            """.trimIndent()
        functionLiteralRuleAssertThat(code)
            .setMaxLineLength()
            .hasLintViolations(
                LintViolation(3, 7, "Newline expected before parameter"),
                LintViolation(4, 18, "Newline expected before arrow"),
            ).isFormattedAs(formattedCode)
    }

    @Test
    fun `Given a lambda with a multiline parameter list starting on the next line below the opening brace`() {
        val code =
            """
            // $MAX_LINE_LENGTH_MARKER  $EOL_CHAR
            val foobar =
                {
                    foo: Foo,
                    bar: Bar ->
                    foo + bar
                }
            """.trimIndent()
        val formattedCode =
            """
            // $MAX_LINE_LENGTH_MARKER  $EOL_CHAR
            val foobar =
                {
                    foo: Foo,
                    bar: Bar
                    ->
                    foo + bar
                }
            """.trimIndent()
        functionLiteralRuleAssertThat(code)
            .setMaxLineLength()
            .hasLintViolation(5, 18, "Newline expected before arrow")
            .isFormattedAs(formattedCode)
    }

    @Test
    fun `Given a single line parameter list starting on the next line below the opening brace`() {
        val code =
            """
            // $MAX_LINE_LENGTH_MARKER  $EOL_CHAR
            val foobar =
                {
                  foo: Foo, bar: Bar ->
                    foo + bar
                }
            """.trimIndent()
        val formattedCode =
            """
            // $MAX_LINE_LENGTH_MARKER  $EOL_CHAR
            val foobar =
                { foo: Foo, bar: Bar ->
                    foo + bar
                }
            """.trimIndent()
        functionLiteralRuleAssertThat(code)
            .setMaxLineLength()
            .hasLintViolation(4, 7, "No newline expected before parameter")
            .isFormattedAs(formattedCode)
    }

    @Test
    fun `Given a single line parameter list starting on the next line below the opening brace and arrow on separate line which can be merged to a single line after opening brace`() {
        val code =
            """
            // $MAX_LINE_LENGTH_MARKER          $EOL_CHAR
            val foobar =
                {
                  foo: Foo, bar: Bar, baz: Baz
                  ->
                    foo + bar
                }
            """.trimIndent()
        val formattedCode =
            """
            // $MAX_LINE_LENGTH_MARKER          $EOL_CHAR
            val foobar =
                { foo: Foo, bar: Bar, baz: Baz ->
                    foo + bar
                }
            """.trimIndent()
        functionLiteralRuleAssertThat(code)
            .setMaxLineLength()
            .hasLintViolations(
                LintViolation(4, 7, "No newline expected before parameter"),
                LintViolation(4, 35, "No newline expected after parameter"),
            ).isFormattedAs(formattedCode)
    }

    @Test
    fun `Given a single line parameter list starting on the next line below the opening brace and arrow on separate line which can not be merged to a single line after opening brace`() {
        val code =
            """
            // $MAX_LINE_LENGTH_MARKER         $EOL_CHAR
            val foobar =
                {
                    foo: Foo, bar: Bar, baz: Baz
                    ->
                    foo + bar
                }
            """.trimIndent()
        val formattedCode =
            """
            // $MAX_LINE_LENGTH_MARKER         $EOL_CHAR
            val foobar =
                {
                    foo: Foo,
                    bar: Bar,
                    baz: Baz
                    ->
                    foo + bar
                }
            """.trimIndent()
        functionLiteralRuleAssertThat(code)
            .setMaxLineLength()
            .hasLintViolations(
                LintViolation(4, 19, "Newline expected before parameter"),
                LintViolation(4, 29, "Newline expected before parameter"),
            ).isFormattedAs(formattedCode)
    }

    @Test
    fun `Issue 2450 - Given a parameter list followed by EOL comment which causes the max line length to be exceed then only report the violation via the max-line-length rule`() {
        val code =
            """
            // $MAX_LINE_LENGTH_MARKER  $EOL_CHAR
            val foobar =
                { foo: Foo // Some comment
                    ->
                    foo // Some other comment
                }
            """.trimIndent()
        functionLiteralRuleAssertThat(code)
            .setMaxLineLength()
            .hasLintViolationsForAdditionalRule(
                LintViolation(3, 30, "Exceeded max line length (29)", false),
                LintViolation(5, 30, "Exceeded max line length (29)", false),
            ).hasNoLintViolationsExceptInAdditionalRules()
    }

    @Test
    fun `Given a single line function literal not exceeding the max line length and having a parameter list`() {
        val code =
            """
            // $MAX_LINE_LENGTH_MARKER                     $EOL_CHAR
            val foobar = { foo: Foo, bar: Bar -> foo + bar }
            """.trimIndent()
        functionLiteralRuleAssertThat(code)
            .setMaxLineLength()
            .hasNoLintViolations()
    }

    @Test
    fun `Given a single line function literal exceeding the max line length and having a parameter list`() {
        val code =
            """
            // $MAX_LINE_LENGTH_MARKER                    $EOL_CHAR
            val foobar = { foo: Foo, bar: Bar -> foo + bar }
            """.trimIndent()
        val formattedCode =
            """
            // $MAX_LINE_LENGTH_MARKER                    $EOL_CHAR
            val foobar = { foo: Foo, bar: Bar ->
                foo + bar
            }
            """.trimIndent()
        functionLiteralRuleAssertThat(code)
            .addAdditionalRuleProvider { MultilineExpressionWrappingRule() }
            .setMaxLineLength()
            .hasLintViolations(
                LintViolation(2, 36, "Newline expected after arrow"),
                LintViolation(2, 48, "Newline expected before closing brace"),
            ).isFormattedAs(formattedCode)
    }

    @Test
    fun `Given a multiline code block starting on same line a arrow`() {
        val code =
            """
            // $MAX_LINE_LENGTH_MARKER               $EOL_CHAR
            val foobar = { foo: Foo, bar: Bar -> foo +
                bar
            }
            """.trimIndent()
        val formattedCode =
            """
            // $MAX_LINE_LENGTH_MARKER               $EOL_CHAR
            val foobar = { foo: Foo, bar: Bar ->
                foo +
                bar
            }
            """.trimIndent()
        functionLiteralRuleAssertThat(code)
            .addAdditionalRuleProvider { MultilineExpressionWrappingRule() }
            .setMaxLineLength()
            .hasLintViolation(2, 36, "Newline expected after arrow")
            .isFormattedAs(formattedCode)
    }

    @Test
    fun `Given a single line function literal without parameters that exceeds the maximum line length`() {
        val code =
            """
            // $MAX_LINE_LENGTH_MARKER    $EOL_CHAR
            val foobar = { it.foo().foobar().foobar2() }
            val foo = bar.filter { it > 2 }!!.takeIf { it.count() > 100 }.map { it * it }
                ?.sum()!!
            """.trimIndent()
        val formattedCode =
            """
            // $MAX_LINE_LENGTH_MARKER    $EOL_CHAR
            val foobar = {
                it.foo().foobar().foobar2()
            }
            val foo =
                bar
                    .filter { it > 2 }!!
                    .takeIf {
                        it.count() > 100
                    }.map { it * it }
                    ?.sum()!!
            """.trimIndent()
        functionLiteralRuleAssertThat(code)
            .addAdditionalRuleProvider { MultilineExpressionWrappingRule() }
            .addAdditionalRuleProvider { ChainMethodContinuationRule() }
            .addAdditionalRuleProvider { IndentationRule() }
            .addAdditionalRuleProvider { ArgumentListWrappingRule() }
            .setMaxLineLength()
            .hasLintViolations(
                LintViolation(2, 14, "Newline expected after opening brace"),
                LintViolation(2, 44, "Newline expected before closing brace"),
                LintViolation(3, 42, "Newline expected after opening brace"),
                LintViolation(3, 61, "Newline expected before closing brace"),
                LintViolation(3, 67, "Newline expected after opening brace"),
                LintViolation(3, 77, "Newline expected before closing brace"),
            ).isFormattedAs(formattedCode)
    }

    @Test
    fun `Issue 2331 - Given a function literal with redundant arrow`() {
        val code =
            """
            fun foo(block: () -> Unit): Unit = foo { -> block() }
            """.trimIndent()
        val formattedCode =
            """
            fun foo(block: () -> Unit): Unit = foo { block() }
            """.trimIndent()
        functionLiteralRuleAssertThat(code)
            .hasLintViolation(1, 42, "Arrow is redundant when parameter list is empty")
            .isFormattedAs(formattedCode)
    }

    @Test
    fun `Issue 2465 - Given a function literal without parameters and with an empty block then do not remove the arrow`() {
        val code =
            """
            fun foo(block: () -> Unit): Unit = foo { -> }
            """.trimIndent()
        functionLiteralRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `Issue 2450 - Given function literal with an EOL-comment as body then do not throw an exception`() {
        val code =
            """
            fun foo() {
                shouldFail<IllegalArgumentException>(sinceKotlin = "255.255.255") {
                    // no-op
                }
            }
            """.trimIndent()
        functionLiteralRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `Issue 2758 - Given function literal with an arrow without parameters arrow literal as leaf of when then do not remove the arrow`() {
        val code =
            """
            val foo =
                when {
                    false -> { -> "bar" }
                    else -> { -> "baz" }
                }
            """.trimIndent()
        functionLiteralRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `Issue 2758 - Given function literal with an arrow without parameters arrow literal not as leaf of when then do remove the arrow`() {
        val code =
            """
            val foo =
                when {
                    false -> { { -> "bar" } }
                    else -> { { -> "baz" } }
                }
            """.trimIndent()
        val formattedCode =
            """
            val foo =
                when {
                    false -> { { "bar" } }
                    else -> { { "baz" } }
                }
            """.trimIndent()
        functionLiteralRuleAssertThat(code)
            .hasLintViolations(
                LintViolation(3, 22, "Arrow is redundant when parameter list is empty"),
                LintViolation(4, 21, "Arrow is redundant when parameter list is empty"),
            ).isFormattedAs(formattedCode)
    }

    @Test
    fun `Issue 2758 - Given function literal with an arrow without parameters arrow literal as leaf of if then do not remove the arrow`() {
        val code =
            """
            val foo = if (cond) { -> "bar" } else { -> "baz" }
            """.trimIndent()
        functionLiteralRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `Issue 2758 - Given function literal with an arrow without parameters arrow literal not as leaf of if then do remove the arrow`() {
        val code =
            """
            val foo = if (cond) {
                { -> "bar" }
            } else {
                { -> "baz" }
            }
            """.trimIndent()
        val formattedCode =
            """
            val foo = if (cond) {
                { "bar" }
            } else {
                { "baz" }
            }
            """.trimIndent()
        functionLiteralRuleAssertThat(code)
            .hasLintViolations(
                LintViolation(2, 7, "Arrow is redundant when parameter list is empty"),
                LintViolation(4, 7, "Arrow is redundant when parameter list is empty"),
            ).isFormattedAs(formattedCode)
    }

    @Test
    fun `Issue 2850 - Given function literal with a comment before the parameter list which contains a redundant parameter then do remove the redundant parameter but keep the comment`() {
        val code =
            """
            val foo1 =
                {
                    // some comment
                    foo: String -> "foo = " + foo
                }
            val foo2 =
                { // some comment
                    foo: String -> "foo = " + foo
                }
            val foo3 =
                {
                    /* some comment */
                    foo: String -> "foo = " + foo
                }
            val foo4 =
                { /* some comment */
                    foo: String -> "foo = " + foo
                }
            """.trimIndent()
        functionLiteralRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `Issue 2451 - Given a function literal that does not fit the line then wrap after the opening brace`() {
        val code =
            """
            // $MAX_LINE_LENGTH_MARKER                                      $EOL_CHAR
            val foo1 = requireNotNull(bar.filter { it == "bar" }) { "some message" }
            val foo2 = requireNotNull(bar?.filter { it == "bar" }) { "some message" }
            """.trimIndent()
        val formattedCode =
            """
            // $MAX_LINE_LENGTH_MARKER                                      $EOL_CHAR
            val foo1 =
                requireNotNull(bar.filter { it == "bar" }) { "some message" }
            val foo2 =
                requireNotNull(bar?.filter { it == "bar" }) {
                    "some message"
                }
            """.trimIndent()
        functionLiteralRuleAssertThat(code)
            .setMaxLineLength()
            .addAdditionalRuleProvider { IndentationRule() }
            .addAdditionalRuleProvider { PropertyWrappingRule() }
            .hasLintViolations(
                LintViolation(line = 2, col = 55, detail = "Newline expected after opening brace"),
                LintViolation(line = 2, col = 72, detail = "Newline expected before closing brace"),
                LintViolation(line = 3, col = 56, detail = "Newline expected after opening brace"),
                LintViolation(line = 3, col = 73, detail = "Newline expected before closing brace"),
            ).isFormattedAs(formattedCode)
    }
}
