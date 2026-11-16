package io.github.ktlint.core.ruleset.standard.rules

import io.github.ktlint.core.ruleset.standard.rules.TrailingCommaOnDeclarationSiteRule.Companion.TRAILING_COMMA_ON_DECLARATION_SITE_PROPERTY
import io.github.ktlint.core.test.KtLintAssertThat.Companion.assertThatRule
import io.github.ktlint.core.test.KtLintAssertThat.Companion.assertThatRuleBuilder
import io.github.ktlint.core.test.LintViolation
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class TrailingCommaOnDeclarationSiteRuleTest {
    private val trailingCommaOnDeclarationSiteRuleAssertThat = assertThatRule { TrailingCommaOnDeclarationSiteRule() }

    @Test
    fun `Given property allow trailing comma on declaration site is not set then remove trailing commas`() {
        val code =
            """
            data class Foo1(val bar: Int,)

            class Foo2<A, B,> {}

            fun foo3(bar: Int): String = when(bar) {
                1, 2, -> "a"
                else -> "b"
            }

            fun foo4() {
                fun bar(): Pair<Int, Int> = Pair(1, 2)

                val (x, y,) = bar()
                val [a, b,] = bar()
            }

            val foo5: (Int, Int,) -> Int = 42

            val foo6: (Int, Int,) -> Int = { foo, bar, -> foo * bar }
            """.trimIndent()
        val formattedCode =
            """
            data class Foo1(val bar: Int)

            class Foo2<A, B> {}

            fun foo3(bar: Int): String = when(bar) {
                1, 2 -> "a"
                else -> "b"
            }

            fun foo4() {
                fun bar(): Pair<Int, Int> = Pair(1, 2)

                val (x, y) = bar()
                val [a, b] = bar()
            }

            val foo5: (Int, Int) -> Int = 42

            val foo6: (Int, Int) -> Int = { foo, bar -> foo * bar }
            """.trimIndent()
        trailingCommaOnDeclarationSiteRuleAssertThat(code)
            .hasLintViolations(
                LintViolation(1, 29, "Unnecessary trailing comma before \")\""),
                LintViolation(3, 16, "Unnecessary trailing comma before \">\""),
                LintViolation(6, 9, "Unnecessary trailing comma before \"->\""),
                LintViolation(13, 14, "Unnecessary trailing comma before \")\""),
                LintViolation(14, 14, "Unnecessary trailing comma before \"]\""),
                LintViolation(17, 20, "Unnecessary trailing comma before \")\""),
                LintViolation(19, 20, "Unnecessary trailing comma before \")\""),
                LintViolation(19, 42, "Unnecessary trailing comma before \"->\""),
            ).isFormattedAs(formattedCode)
    }

    @Test
    fun `Given that the trailing comma is not allowed on declaration site then remove it from an parameter list when present`() {
        val code =
            """
            data class Foo1(val bar: Int,)
            data class Foo2(
                val bar: Int, // The comma before the comment should be removed without removing the comment itself
            )
            data class Foo3(
                val bar: Int, /* The comma before the comment should be removed without removing the comment itself */
            )
            """.trimIndent()
        val formattedCode =
            """
            data class Foo1(val bar: Int)
            data class Foo2(
                val bar: Int // The comma before the comment should be removed without removing the comment itself
            )
            data class Foo3(
                val bar: Int /* The comma before the comment should be removed without removing the comment itself */
            )
            """.trimIndent()
        trailingCommaOnDeclarationSiteRuleAssertThat(code)
            .withEditorConfigOverride(TRAILING_COMMA_ON_DECLARATION_SITE_PROPERTY to false)
            .hasLintViolations(
                LintViolation(1, 29, "Unnecessary trailing comma before \")\""),
                LintViolation(3, 17, "Unnecessary trailing comma before \")\""),
                LintViolation(6, 17, "Unnecessary trailing comma before \")\""),
            ).isFormattedAs(formattedCode)
    }

    @Test
    fun `Given that the trailing comma is required on declaration site then add it to the parameter list when missing`() {
        val code =
            """
            data class Foo1(val bar: Int)
            data class Foo2(
                val bar: Int // The comma should be inserted before the comment
            )
            data class Foo3(
                val bar: Int /* The comma should be inserted before the comment */
            )
            """.trimIndent()
        val formattedCode =
            """
            data class Foo1(val bar: Int)
            data class Foo2(
                val bar: Int, // The comma should be inserted before the comment
            )
            data class Foo3(
                val bar: Int, /* The comma should be inserted before the comment */
            )
            """.trimIndent()
        trailingCommaOnDeclarationSiteRuleAssertThat(code)
            .withEditorConfigOverride(TRAILING_COMMA_ON_DECLARATION_SITE_PROPERTY to true)
            .hasLintViolations(
                LintViolation(3, 17, "Missing trailing comma before \")\""),
                LintViolation(6, 17, "Missing trailing comma before \")\""),
            ).isFormattedAs(formattedCode)
    }

    @Test
    fun `Given that the trailing comma is not allowed on declaration site then remove it from an type parameter list when present`() {
        val code =
            """
            class Foo1<A, B,> {}
            class Foo2<
                A,
                B, // The comma before the comment should be removed without removing the comment itself
            > {}
            class Foo3<
                A,
                B, /* The comma before the comment should be removed without removing the comment itself */
            > {}
            """.trimIndent()
        val formattedCode =
            """
            class Foo1<A, B> {}
            class Foo2<
                A,
                B // The comma before the comment should be removed without removing the comment itself
            > {}
            class Foo3<
                A,
                B /* The comma before the comment should be removed without removing the comment itself */
            > {}
            """.trimIndent()
        trailingCommaOnDeclarationSiteRuleAssertThat(code)
            .withEditorConfigOverride(TRAILING_COMMA_ON_DECLARATION_SITE_PROPERTY to false)
            .hasLintViolations(
                LintViolation(1, 16, "Unnecessary trailing comma before \">\""),
                LintViolation(4, 6, "Unnecessary trailing comma before \">\""),
                LintViolation(8, 6, "Unnecessary trailing comma before \">\""),
            ).isFormattedAs(formattedCode)
    }

    @Test
    fun `Given that the trailing comma is required on declaration site then add it to the type parameter list when missing`() {
        val code =
            """
            class Foo1<A, B> {}
            class Foo2<
                A,
                B // The comma should be inserted before the comment
            > {}
            class Foo3<
                A,
                B /* The comma should be inserted before the comment */
            > {}
            """.trimIndent()
        val formattedCode =
            """
            class Foo1<A, B> {}
            class Foo2<
                A,
                B, // The comma should be inserted before the comment
            > {}
            class Foo3<
                A,
                B, /* The comma should be inserted before the comment */
            > {}
            """.trimIndent()
        trailingCommaOnDeclarationSiteRuleAssertThat(code)
            .withEditorConfigOverride(TRAILING_COMMA_ON_DECLARATION_SITE_PROPERTY to true)
            .hasLintViolations(
                LintViolation(4, 6, "Missing trailing comma before \">\""),
                LintViolation(8, 6, "Missing trailing comma before \">\""),
            ).isFormattedAs(formattedCode)
    }

    @Test
    fun `Given that the trailing comma is not allowed on declaration site then remove it from when-condition when present`() {
        val code =
            """
            fun foo(bar: Int): String = when(bar) {
                1, 2, -> "a"
                3, 4, // The comma before the comment should be removed without removing the comment itself
                -> "a"
                5,
                6, /* The comma before the comment should be removed without removing the comment itself */
                -> "a"
                else -> "b"
            }
            """.trimIndent()
        val formattedCode =
            """
            fun foo(bar: Int): String = when(bar) {
                1, 2 -> "a"
                3, 4 // The comma before the comment should be removed without removing the comment itself
                -> "a"
                5,
                6 /* The comma before the comment should be removed without removing the comment itself */
                -> "a"
                else -> "b"
            }
            """.trimIndent()
        trailingCommaOnDeclarationSiteRuleAssertThat(code)
            .withEditorConfigOverride(TRAILING_COMMA_ON_DECLARATION_SITE_PROPERTY to false)
            .hasLintViolations(
                LintViolation(2, 9, "Unnecessary trailing comma before \"->\""),
                LintViolation(3, 9, "Unnecessary trailing comma before \"->\""),
                LintViolation(6, 6, "Unnecessary trailing comma before \"->\""),
            ).isFormattedAs(formattedCode)
    }

    @Test
    fun `Given that the trailing comma is required on declaration site then add it to the when-condition when missing`() {
        val code =
            """
            fun foo(bar: Int): String = when(bar) {
                1, 2 -> "a"
                3, 4 // The comma should be inserted before the comment
                -> "a"
                5,
                6 /* The comma should be inserted before the comment */
                -> "a"
                else -> "b"
            }
            """.trimIndent()
        val formattedCode =
            """
            fun foo(bar: Int): String = when(bar) {
                1, 2 -> "a"
                3, 4, // The comma should be inserted before the comment
                -> "a"
                5,
                6, /* The comma should be inserted before the comment */
                -> "a"
                else -> "b"
            }
            """.trimIndent()
        trailingCommaOnDeclarationSiteRuleAssertThat(code)
            .withEditorConfigOverride(TRAILING_COMMA_ON_DECLARATION_SITE_PROPERTY to true)
            .hasLintViolations(
                LintViolation(3, 9, "Missing trailing comma before \"->\""),
                LintViolation(6, 6, "Missing trailing comma before \"->\""),
            ).isFormattedAs(formattedCode)
    }

    @Test
    fun `Issue 1519 - Given a when entry which is not a simple value but a dot qualified expression and that the trailing comma is required on declaration site then add it when missing`() {
        val code =
            """
            fun foo(bar: Any): String = when(bar) {
                bar.foobar1(), bar.foobar2() -> "a"
                bar.foobar3(), bar.foobar4() // The comma should be inserted before the comment
                -> "a"
                bar.foobar5(),
                bar.foobar6() /* The comma should be inserted before the comment */
                -> "a"
                else -> "b"
            }
            """.trimIndent()
        val formattedCode =
            """
            fun foo(bar: Any): String = when(bar) {
                bar.foobar1(), bar.foobar2() -> "a"
                bar.foobar3(), bar.foobar4(), // The comma should be inserted before the comment
                -> "a"
                bar.foobar5(),
                bar.foobar6(), /* The comma should be inserted before the comment */
                -> "a"
                else -> "b"
            }
            """.trimIndent()
        trailingCommaOnDeclarationSiteRuleAssertThat(code)
            .withEditorConfigOverride(TRAILING_COMMA_ON_DECLARATION_SITE_PROPERTY to true)
            .hasLintViolations(
                LintViolation(3, 33, "Missing trailing comma before \"->\""),
                LintViolation(6, 18, "Missing trailing comma before \"->\""),
            ).isFormattedAs(formattedCode)
    }

    @Test
    fun `Given that the trailing comma is required on declaration site and that the trailing comma exists but is not followed by a newline then add the newline before the arrow`() {
        val code =
            """
            fun foo(bar: Any): String = when(bar) {
                1,
                2,-> {
                    "a"
                }
                3,
                4,-> {
                    "b"
                }
                5,
                6 /* some comment */,-> {
                    "c"
                }
                else -> {
                    "d"
                }
            }
            """.trimIndent()
        val formattedCode =
            """
            fun foo(bar: Any): String = when(bar) {
                1,
                2,
                -> {
                    "a"
                }
                3,
                4,
                -> {
                    "b"
                }
                5,
                6 /* some comment */,
                -> {
                    "c"
                }
                else -> {
                    "d"
                }
            }
            """.trimIndent()
        trailingCommaOnDeclarationSiteRuleAssertThat(code)
            .withEditorConfigOverride(TRAILING_COMMA_ON_DECLARATION_SITE_PROPERTY to true)
            .hasLintViolations(
                LintViolation(3, 6, "Expected a newline between the trailing comma and  \"->\""),
                LintViolation(7, 6, "Expected a newline between the trailing comma and  \"->\""),
                LintViolation(11, 25, "Expected a newline between the trailing comma and  \"->\""),
            ).isFormattedAs(formattedCode)
    }

    @Test
    fun `Given that the trailing comma is not allowed on declaration site then remove it from the destructuring declaration when present`() {
        val code =
            """
            fun foo() {
                fun bar(): Pair<Int, Int> = Pair(1, 2)

                val (x, y,) = bar()
                val (
                    x,
                    y, // The comma before the comment should be removed without removing the comment itself
                ) = bar()
                val (
                    x,
                    y, /* The comma before the comment should be removed without removing the comment itself */
                ) = bar()
            }
            """.trimIndent()
        val formattedCode =
            """
            fun foo() {
                fun bar(): Pair<Int, Int> = Pair(1, 2)

                val (x, y) = bar()
                val (
                    x,
                    y // The comma before the comment should be removed without removing the comment itself
                ) = bar()
                val (
                    x,
                    y /* The comma before the comment should be removed without removing the comment itself */
                ) = bar()
            }
            """.trimIndent()
        trailingCommaOnDeclarationSiteRuleAssertThat(code)
            .withEditorConfigOverride(TRAILING_COMMA_ON_DECLARATION_SITE_PROPERTY to false)
            .hasLintViolations(
                LintViolation(4, 14, "Unnecessary trailing comma before \")\""),
                LintViolation(7, 10, "Unnecessary trailing comma before \")\""),
                LintViolation(11, 10, "Unnecessary trailing comma before \")\""),
            ).isFormattedAs(formattedCode)
    }

    @Test
    fun `Given that the trailing comma is required on declaration site then add it to the destructuring declaration when missing`() {
        val code =
            """
            fun foo() {
                fun bar(): Pair<Int, Int> = Pair(1, 2)

                val (x, y) = bar()
                val (
                    x,
                    y // The comma should be inserted before the comment
                ) = bar()
                val (
                    x,
                    y /* The comma should be inserted before the comment */
                ) = bar()
            }
            """.trimIndent()
        val formattedCode =
            """
            fun foo() {
                fun bar(): Pair<Int, Int> = Pair(1, 2)

                val (x, y) = bar()
                val (
                    x,
                    y, // The comma should be inserted before the comment
                ) = bar()
                val (
                    x,
                    y, /* The comma should be inserted before the comment */
                ) = bar()
            }
            """.trimIndent()
        trailingCommaOnDeclarationSiteRuleAssertThat(code)
            .withEditorConfigOverride(TRAILING_COMMA_ON_DECLARATION_SITE_PROPERTY to true)
            .hasLintViolations(
                LintViolation(7, 10, "Missing trailing comma before \")\""),
                LintViolation(11, 10, "Missing trailing comma before \")\""),
            ).isFormattedAs(formattedCode)
    }

    @Test
    fun `Given that the trailing comma is not allowed on declaration site then remove it from the function literal when present`() {
        val code =
            """
            val fooBar1: (Int, Int) -> Int = { foo, bar, -> foo * bar }
            val fooBar2: (Int, Int) -> Int = {
                foo,
                bar, // The comma before the comment should be removed without removing the comment itself
                ->
                foo * bar
            }
            val fooBar3: (Int, Int) -> Int = {
                foo,
                bar, /* The comma before the comment should be removed without removing the comment itself */
                ->
                foo * bar
            }
            """.trimIndent()
        val formattedCode =
            """
            val fooBar1: (Int, Int) -> Int = { foo, bar -> foo * bar }
            val fooBar2: (Int, Int) -> Int = {
                foo,
                bar // The comma before the comment should be removed without removing the comment itself
                ->
                foo * bar
            }
            val fooBar3: (Int, Int) -> Int = {
                foo,
                bar /* The comma before the comment should be removed without removing the comment itself */
                ->
                foo * bar
            }
            """.trimIndent()
        trailingCommaOnDeclarationSiteRuleAssertThat(code)
            .withEditorConfigOverride(TRAILING_COMMA_ON_DECLARATION_SITE_PROPERTY to false)
            .hasLintViolations(
                LintViolation(1, 44, "Unnecessary trailing comma before \"->\""),
                LintViolation(4, 8, "Unnecessary trailing comma before \"->\""),
                LintViolation(10, 8, "Unnecessary trailing comma before \"->\""),
            ).isFormattedAs(formattedCode)
    }

    @Test
    fun `Given that the trailing comma is required on declaration site then add it to function literal when missing`() {
        val code =
            """
            val fooBar1: (Int, Int) -> Int = { foo, bar -> foo * bar }
            val fooBar2: (Int, Int) -> Int = {
                foo,
                bar // The comma should be inserted before the comment
                ->
                foo * bar
            }
            val fooBar3: (Int, Int) -> Int = {
                foo,
                bar /* The comma should be inserted before the comment */
                ->
                foo * bar
            }
            """.trimIndent()
        val formattedCode =
            """
            val fooBar1: (Int, Int) -> Int = { foo, bar -> foo * bar }
            val fooBar2: (Int, Int) -> Int = {
                foo,
                bar, // The comma should be inserted before the comment
                ->
                foo * bar
            }
            val fooBar3: (Int, Int) -> Int = {
                foo,
                bar, /* The comma should be inserted before the comment */
                ->
                foo * bar
            }
            """.trimIndent()
        trailingCommaOnDeclarationSiteRuleAssertThat(code)
            .withEditorConfigOverride(TRAILING_COMMA_ON_DECLARATION_SITE_PROPERTY to true)
            .hasLintViolations(
                LintViolation(4, 8, "Missing trailing comma before \"->\""),
                LintViolation(10, 8, "Missing trailing comma before \"->\""),
            ).isFormattedAs(formattedCode)
    }

    @Test
    fun `Given that a trailing comma is required on declaration site then add it to function declaration`() {
        val code =
            """
            fun test(
                x: Int,
                y: Int,
                block: (Int, Int) -> Int
            ): (
                Int, Int
            ) -> Int = { foo, bar ->
                block(foo, bar)
            }
            """.trimIndent()
        val formattedCode =
            """
            fun test(
                x: Int,
                y: Int,
                block: (Int, Int) -> Int,
            ): (
                Int, Int,
            ) -> Int = { foo, bar ->
                block(foo, bar)
            }
            """.trimIndent()
        trailingCommaOnDeclarationSiteRuleAssertThat(code)
            .withEditorConfigOverride(TRAILING_COMMA_ON_DECLARATION_SITE_PROPERTY to true)
            .hasLintViolations(
                LintViolation(4, 29, "Missing trailing comma before \")\""),
                LintViolation(6, 13, "Missing trailing comma before \")\""),
            ).isFormattedAs(formattedCode)
    }

    @Test
    fun `Given that a trailing comma is required on declaration site then still it should not be added to the setter`() {
        val code =
            """
            class Test {
                var foo = Bar()
                    set(value) {
                    }
            }
            """.trimIndent()
        trailingCommaOnDeclarationSiteRuleAssertThat(code)
            .withEditorConfigOverride(TRAILING_COMMA_ON_DECLARATION_SITE_PROPERTY to true)
            .hasNoLintViolations()
    }

    @Test
    fun `Issue 1312 - Given that a trailing comma is required on declaration site and multiple elements then force lambda arrow to next line`() {
        val code =
            """
            fun foo(bar: Int): String = when(bar) {
                1, 2 -> "a"
                3,
                4 -> "b"
                5,
                6-> "c"
                else -> "d"
            }
            """.trimIndent()
        val formattedCode =
            """
            fun foo(bar: Int): String = when(bar) {
                1, 2 -> "a"
                3,
                4,
                -> "b"
                5,
                6,
                -> "c"
                else -> "d"
            }
            """.trimIndent()
        trailingCommaOnDeclarationSiteRuleAssertThat(code)
            .withEditorConfigOverride(TRAILING_COMMA_ON_DECLARATION_SITE_PROPERTY to true)
            .hasLintViolations(
                LintViolation(4, 6, "Missing trailing comma and newline before \"->\""),
                LintViolation(6, 6, "Missing trailing comma and newline before \"->\""),
            ).isFormattedAs(formattedCode)
    }

    @Test
    fun `Given that a trailing comma is required on declaration site and unused imports do not affect each other`() {
        val code =
            """
            package io.github.ktlint.core
            import io.github.ktlint.core.enum.Enum
            import io.github.ktlint.core.enum.EnumThree
            import io.github.ktlint.core.enum.EnumTwo
            data class TrailingCommaTest(
                val foo: String,
                val bar: Enum,
                val bar2: EnumTwo,
                val bar3: EnumThree
            )
            """.trimIndent()
        val formattedCode =
            """
            package io.github.ktlint.core
            import io.github.ktlint.core.enum.Enum
            import io.github.ktlint.core.enum.EnumThree
            import io.github.ktlint.core.enum.EnumTwo
            data class TrailingCommaTest(
                val foo: String,
                val bar: Enum,
                val bar2: EnumTwo,
                val bar3: EnumThree,
            )
            """.trimIndent()
        trailingCommaOnDeclarationSiteRuleAssertThat(code)
            // When running format mode, the rules are first executed in parallel to find linting errors. In this
            // process, no unused import are found because the trailing comma is not yet added to variable "bar3". Then
            // in the next stage the rules are run consecutively. Now the trailing comma rule is adding a trailing comma
            // after the type of variable "bar3". When the no-unused-import rule runs after the trailing-comma rule, it
            // was incorrectly seen as part of the type of variable "bar3" and a reference "EnumThree," (with the
            // trailing comma was added) which in turn resulted in not recognizing that the import of EnumThree actually
            // was used.
            .addAdditionalRuleProvider { NoUnusedImportsRule() }
            .withEditorConfigOverride(TRAILING_COMMA_ON_DECLARATION_SITE_PROPERTY to true)
            .hasLintViolation(9, 24, "Missing trailing comma before \")\"")
            .isFormattedAs(formattedCode)
    }

    @Test
    fun `Given that a trailing comma is is not allowed then remove comma after last enum member`() {
        val code =
            """
            enum class Shape {
                SQUARE,
                TRIANGLE,
            }
            """.trimIndent()
        val formattedCode =
            """
            enum class Shape {
                SQUARE,
                TRIANGLE
            }
            """.trimIndent()
        trailingCommaOnDeclarationSiteRuleAssertThat(code)
            .withEditorConfigOverride(TRAILING_COMMA_ON_DECLARATION_SITE_PROPERTY to false)
            .hasLintViolation(3, 13, "Unnecessary trailing comma before \"}\"")
            .isFormattedAs(formattedCode)
    }

    @Test
    fun `Given that a trailing comma is not allowed then it is removed for enums terminated with semicolon`() {
        val code =
            """
            enum class Shape {
                SQUARE,
                TRIANGLE,
                ;
                fun print() = name()
            }
            """.trimIndent()
        val formattedCode =
            """
            enum class Shape {
                SQUARE,
                TRIANGLE
                ;
                fun print() = name()
            }
            """.trimIndent()
        trailingCommaOnDeclarationSiteRuleAssertThat(code)
            .withEditorConfigOverride(TRAILING_COMMA_ON_DECLARATION_SITE_PROPERTY to false)
            .hasLintViolation(3, 13, "Unnecessary trailing comma before \";\"")
            .isFormattedAs(formattedCode)
    }

    @Test
    fun `Given that a trailing comma is not allowed then remove it for enums where last two entries are on same line and followed by trailing comma`() {
        val code =
            """
            enum class Shape {
                SQUARE, TRIANGLE,
            }
            """.trimIndent()
        val formattedCode =
            """
            enum class Shape {
                SQUARE, TRIANGLE
            }
            """.trimIndent()
        trailingCommaOnDeclarationSiteRuleAssertThat(code)
            .withEditorConfigOverride(TRAILING_COMMA_ON_DECLARATION_SITE_PROPERTY to false)
            .hasLintViolation(2, 21, "Unnecessary trailing comma before \"}\"")
            .isFormattedAs(formattedCode)
    }

    @Nested
    inner class `Given that a required trailing comma is missing` {
        @Test
        fun `Given that last two enumeration entries are on same line, do not add a trailing comma`() {
            val code =
                """
                enum class Shape {
                    SQUARE, TRIANGLE
                }
                """.trimIndent()
            trailingCommaOnDeclarationSiteRuleAssertThat(code)
                .withEditorConfigOverride(TRAILING_COMMA_ON_DECLARATION_SITE_PROPERTY to true)
                .hasNoLintViolations()
        }

        @Test
        fun `Issue 1605 - Given that an empty parameter list with a comment, dot add a trailing comma`() {
            val code =
                """
                class Test(
                    // comment
                )
                """.trimIndent()
            trailingCommaOnDeclarationSiteRuleAssertThat(code)
                .withEditorConfigOverride(TRAILING_COMMA_ON_DECLARATION_SITE_PROPERTY to true)
                .hasNoLintViolations()
        }

        @Test
        fun `Given an enum is terminated by a semicolon and EOL comment without a trailing comma, then it is added `() {
            val code =
                """
                enum class Shape {
                    SQUARE,
                    TRIANGLE; // EOL Comment should be kept
                }
                """.trimIndent()
            val formattedCode =
                """
                enum class Shape {
                    SQUARE,
                    TRIANGLE, // EOL Comment should be kept
                    ;
                }
                """.trimIndent()
            trailingCommaOnDeclarationSiteRuleAssertThat(code)
                .withEditorConfigOverride(TRAILING_COMMA_ON_DECLARATION_SITE_PROPERTY to true)
                .hasLintViolation(3, 13, "Missing trailing comma before \";\"")
                .isFormattedAs(formattedCode)
        }

        @Test
        fun `Given an enum is terminated by a semicolon and block comment, then it is added `() {
            val code =
                """
                enum class Shape {
                    SQUARE,
                    TRIANGLE; /* block comment should be kept */
                }
                """.trimIndent()
            val formattedCode =
                """
                enum class Shape {
                    SQUARE,
                    TRIANGLE, /* block comment should be kept */
                    ;
                }
                """.trimIndent()
            trailingCommaOnDeclarationSiteRuleAssertThat(code)
                .withEditorConfigOverride(TRAILING_COMMA_ON_DECLARATION_SITE_PROPERTY to true)
                .hasLintViolation(3, 13, "Missing trailing comma before \";\"")
                .isFormattedAs(formattedCode)
        }

        @Test
        fun `Given an enum terminated by semicolon without a trailing comma then it is added`() {
            val code =
                """
                enum class Shape {
                    SQUARE,
                    TRIANGLE;
                }
                """.trimIndent()
            val formattedCode =
                """
                enum class Shape {
                    SQUARE,
                    TRIANGLE,
                    ;
                }
                """.trimIndent()
            trailingCommaOnDeclarationSiteRuleAssertThat(code)
                .withEditorConfigOverride(TRAILING_COMMA_ON_DECLARATION_SITE_PROPERTY to true)
                .hasLintViolation(3, 13, "Missing trailing comma before \";\"")
                .isFormattedAs(formattedCode)
        }

        @Test
        fun `Given an enum without trailing-comma with other declarations following the enum entries then it is added`() {
            val code =
                """
                enum class Shape {
                    SQUARE,
                    TRIANGLE;
                    fun print() = name()
                }
                """.trimIndent()
            val formattedCode =
                """
                enum class Shape {
                    SQUARE,
                    TRIANGLE,
                    ;
                    fun print() = name()
                }
                """.trimIndent()
            trailingCommaOnDeclarationSiteRuleAssertThat(code)
                .withEditorConfigOverride(TRAILING_COMMA_ON_DECLARATION_SITE_PROPERTY to true)
                .hasLintViolation(3, 13, "Missing trailing comma before \";\"")
                .isFormattedAs(formattedCode)
        }

        @Test
        fun `Given that a trailing comma is required then it is added for complicated enums`() {
            val code =
                """
                interface Printable {
                    fun print(): String
                }
                enum class Shape : Printable {
                    Square {
                        override fun print() = "■"
                    },
                    Triangle {
                        override fun print() = "▲"
                    }
                }
                """.trimIndent()
            val formattedCode =
                """
                interface Printable {
                    fun print(): String
                }
                enum class Shape : Printable {
                    Square {
                        override fun print() = "■"
                    },
                    Triangle {
                        override fun print() = "▲"
                    },
                }
                """.trimIndent()
            trailingCommaOnDeclarationSiteRuleAssertThat(code)
                .withEditorConfigOverride(TRAILING_COMMA_ON_DECLARATION_SITE_PROPERTY to true)
                .hasLintViolation(10, 6, "Missing trailing comma before \"}\"")
                .isFormattedAs(formattedCode)
        }

        @Test
        fun `Given that a trailing comma is required then add trailing comma after last enum member`() {
            val code =
                """
                enum class Shape {
                    SQUARE,
                    TRIANGLE
                }
                """.trimIndent()
            val formattedCode =
                """
                enum class Shape {
                    SQUARE,
                    TRIANGLE,
                }
                """.trimIndent()
            trailingCommaOnDeclarationSiteRuleAssertThat(code)
                .withEditorConfigOverride(TRAILING_COMMA_ON_DECLARATION_SITE_PROPERTY to true)
                .hasLintViolation(3, 13, "Missing trailing comma before \"}\"")
                .isFormattedAs(formattedCode)
        }
    }

    @Test
    fun `Given that a trailing comma is required then add trailing comma after last enum member`() {
        val code =
            """
            val foobar = if (true) {
                "foo"
            } else "bar"

            enum class SomeEnum {
                A, B
            }
            """.trimIndent()
        val formattedCode =
            """
            val foobar = if (true) {
                "foo"
            } else {
                "bar"
            }

            enum class SomeEnum {
                A, B
            }
            """.trimIndent()

        // Ensure that AST before Enumeration class is changed by another rule before the running the
        // TrailingCommaOnDeclarationSiteRule
        val multiLineIfElseRuleAssertThat =
            assertThatRuleBuilder { MultiLineIfElseRule() }
                .addAdditionalRuleProvider { TrailingCommaOnDeclarationSiteRule() }
                .addAdditionalRuleProvider { WrappingRule() }
                .assertThat()

        multiLineIfElseRuleAssertThat(code)
            .withEditorConfigOverride(TRAILING_COMMA_ON_DECLARATION_SITE_PROPERTY to true)
            .isFormattedAs(formattedCode)
    }

    @Test
    fun `Issue 1609 - Given that a trailing comma is required then add trailing comma after last enum member and indent the semi-colon`() {
        val code =
            """
            enum class SomeEnum1(id: String) {
                FOO("foo"),
                BAR("bar");

                fun doSomething(id: String) {}
            }
            """.trimIndent()
        val formattedCode =
            """
            enum class SomeEnum1(id: String) {
                FOO("foo"),
                BAR("bar"),
                ;

                fun doSomething(id: String) {}
            }
            """.trimIndent()
        trailingCommaOnDeclarationSiteRuleAssertThat(code)
            .withEditorConfigOverride(TRAILING_COMMA_ON_DECLARATION_SITE_PROPERTY to true)
            .hasLintViolation(3, 15, "Missing trailing comma before \";\"")
            .isFormattedAs(formattedCode)
    }

    @Test
    fun `Issue 1676 - Given a trailing comma followed by a kdoc then do no add another trailing comma`() {
        val code =
            """
            fun foo(
                val bar: Bar, /** Comment */
            )
            fun foo(
                val bar: Bar,
                /** Comment */
            )
            fun foo(
                val bar: Bar,
                /**
                 * Comment
                 */
            )
            """.trimIndent()
        trailingCommaOnDeclarationSiteRuleAssertThat(code)
            .withEditorConfigOverride(TRAILING_COMMA_ON_DECLARATION_SITE_PROPERTY to true)
            .hasNoLintViolations()
    }

    @Test
    fun `Issue 1786 - Given an enumeration containing a companion object, and that a trailing comma is inserted then do not report a unnecessary semicolon`() {
        val code =
            """
            enum class TestEnum() {
                A,
                B;

                companion object {
                    const val A_NUMBER = 1L
                }
            }
            """.trimIndent()
        val formattedCode =
            """
            enum class TestEnum() {
                A,
                B,
                ;

                companion object {
                    const val A_NUMBER = 1L
                }
            }
            """.trimIndent()
        val semicolonRuleAssertThat =
            assertThatRuleBuilder { SemicolonRule() }
                // Keep formatted code readable
                .addAdditionalRuleProvider { IndentationRule() }
                .addAdditionalRuleProvider { TrailingCommaOnDeclarationSiteRule() }
                .withEditorConfigOverride(TRAILING_COMMA_ON_DECLARATION_SITE_PROPERTY to true)
                .assertThat()

        semicolonRuleAssertThat(code)
            .hasNoLintViolationsForRuleId(NO_SEMICOLONS_RULE_ID)
            .isFormattedAs(formattedCode)
    }

    @Test
    fun `Issue 1711 - Given an empty enum class then do not throw an exception`() {
        val code =
            """
            enum class Dummy(val type: String) {
                // empty
            }
            """.trimIndent()
        trailingCommaOnDeclarationSiteRuleAssertThat(code)
            .withEditorConfigOverride(TRAILING_COMMA_ON_DECLARATION_SITE_PROPERTY to true)
            .hasNoLintViolations()
    }

    @Test
    fun `Given a function literal with a trailing comma in the parameter list and arrow on the next line then do not report a violation`() {
        val code =
            """
            val foo = {
                string: String,
                int: Int ->
                // do something
            }
            """.trimIndent()
        val formattedCode =
            """
            val foo = {
                string: String,
                int: Int,
                ->
                // do something
            }
            """.trimIndent()
        trailingCommaOnDeclarationSiteRuleAssertThat(code)
            .withEditorConfigOverride(TRAILING_COMMA_ON_DECLARATION_SITE_PROPERTY to true)
            .hasLintViolation(3, 13, "Missing trailing comma and newline before \"->\"")
            .isFormattedAs(formattedCode)
    }

    @Test
    fun `Issue 2817 - Given when condition with guard clause`() {
        val code =
            """
            val x1 =
                when (true) {
                    true if foo("a") -> true
                    else -> false
                }
            val x2 =
                when (true) {
                    true if foo(
                        "a",
                    )
                    -> true

                    else -> false
                }
            """.trimIndent()
        trailingCommaOnDeclarationSiteRuleAssertThat(code)
            .withEditorConfigOverride(TRAILING_COMMA_ON_DECLARATION_SITE_PROPERTY to true)
            .hasNoLintViolations()
    }

    @Test
    fun `Issue 3364 - Given that the trailing comma is not allowed on declaration site then remove it from the positional destructuring declaration when present`() {
        val code =
            """
            fun foo() {
                fun bar(): Pair<Int, Int> = Pair(1, 2)

                val [x, y,] = bar()
                val [
                    x,
                    y, // The comma before the comment should be removed without removing the comment itself
                ] = bar()
                val [
                    x,
                    y, /* The comma before the comment should be removed without removing the comment itself */
                ] = bar()
            }
            """.trimIndent()
        val formattedCode =
            """
            fun foo() {
                fun bar(): Pair<Int, Int> = Pair(1, 2)

                val [x, y] = bar()
                val [
                    x,
                    y // The comma before the comment should be removed without removing the comment itself
                ] = bar()
                val [
                    x,
                    y /* The comma before the comment should be removed without removing the comment itself */
                ] = bar()
            }
            """.trimIndent()
        trailingCommaOnDeclarationSiteRuleAssertThat(code)
            .withEditorConfigOverride(TRAILING_COMMA_ON_DECLARATION_SITE_PROPERTY to false)
            .hasLintViolations(
                LintViolation(4, 14, "Unnecessary trailing comma before \"]\""),
                LintViolation(7, 10, "Unnecessary trailing comma before \"]\""),
                LintViolation(11, 10, "Unnecessary trailing comma before \"]\""),
            ).isFormattedAs(formattedCode)
    }

    @Test
    fun `Issue 3364 - Given that the trailing comma is required on declaration site then add it to the positional destructuring declaration when missing`() {
        val code =
            """
            fun foo() {
                fun bar(): Pair<Int, Int> = Pair(1, 2)

                val [x, y] = bar()
                val [
                    x,
                    y // The comma should be inserted before the comment
                ] = bar()
                val [
                    x,
                    y /* The comma should be inserted before the comment */
                ] = bar()
            }
            """.trimIndent()
        val formattedCode =
            """
            fun foo() {
                fun bar(): Pair<Int, Int> = Pair(1, 2)

                val [x, y] = bar()
                val [
                    x,
                    y, // The comma should be inserted before the comment
                ] = bar()
                val [
                    x,
                    y, /* The comma should be inserted before the comment */
                ] = bar()
            }
            """.trimIndent()
        trailingCommaOnDeclarationSiteRuleAssertThat(code)
            .withEditorConfigOverride(TRAILING_COMMA_ON_DECLARATION_SITE_PROPERTY to true)
            .hasLintViolations(
                LintViolation(7, 10, "Missing trailing comma before \"]\""),
                LintViolation(11, 10, "Missing trailing comma before \"]\""),
            ).isFormattedAs(formattedCode)
    }
}
