package io.github.ktlint.core.ruleset.standard.rules

import io.github.ktlint.core.rule.engine.core.api.editorconfig.CODE_STYLE_PROPERTY
import io.github.ktlint.core.rule.engine.core.api.editorconfig.CodeStyleValue
import io.github.ktlint.core.rule.engine.core.api.editorconfig.CodeStyleValue.KTLINT_OFFICIAL
import io.github.ktlint.core.ruleset.standard.rules.AnnotationRule.Companion.ANNOTATIONS_WITH_PARAMETERS_NOT_TO_BE_WRAPPED_PROPERTY
import io.github.ktlint.core.test.KtLintAssertThat.Companion.assertThatRule
import io.github.ktlint.core.test.LintViolation
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class AnnotationRuleTest {
    private val annotationRuleAssertThat = assertThatRule { AnnotationRule() }

    @Test
    fun `Given a single annotation on same line before the annotated construct`() {
        val code =
            """
            @FunctionalInterface class FooBar {
                @JvmField var foo: String
                @Test fun bar() {}
            }
            """.trimIndent()
        annotationRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `Given a single annotation on line above the annotated construct`() {
        val code =
            """
            @FunctionalInterface
            class FooBar {
                @JvmField
                var foo: String
                @Test
                fun bar() {}
            }
            """.trimIndent()
        annotationRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `Given an annotation with a parameter followed by a EOL comment`() {
        val code =
            """
            @Suppress("Something") // some comment
            class FooBar {
                @Suppress("Something") // some comment
                var foo: String
                @Suppress("Something") // some comment
                fun bar() {}
            }
            """.trimIndent()
        annotationRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `Given an annotation with a parameter on same line as annotation construct (possibly separated by a block comment or KDoc)`() {
        val code =
            """
            @Suppress("Something") class FooBar1 {
                @Suppress("Something") var foo: String
                @Suppress("Something") fun bar() {}
            }
            @Suppress("Something") /* some comment */ class FooBar2 {
                @Suppress("Something") /* some comment */ var foo: String
                @Suppress("Something") /* some comment */ fun bar() {}
            }
            @Suppress("Something") /** some comment */ class FooBar3 {
                @Suppress("Something") /** some comment */ var foo: String
                @Suppress("Something") /** some comment */ fun bar() {}
            }
            """.trimIndent()
        val formattedCode =
            """
            @Suppress("Something")
            class FooBar1 {
                @Suppress("Something")
                var foo: String
                @Suppress("Something")
                fun bar() {}
            }
            @Suppress("Something") /* some comment */
            class FooBar2 {
                @Suppress("Something") /* some comment */
                var foo: String
                @Suppress("Something") /* some comment */
                fun bar() {}
            }
            @Suppress("Something") /** some comment */
            class FooBar3 {
                @Suppress("Something") /** some comment */
                var foo: String
                @Suppress("Something") /** some comment */
                fun bar() {}
            }
            """.trimIndent()
        annotationRuleAssertThat(code)
            .hasLintViolations(
                LintViolation(1, 23, "Expected newline after last annotation"),
                LintViolation(2, 27, "Expected newline after last annotation"),
                LintViolation(3, 27, "Expected newline after last annotation"),
                LintViolation(5, 42, "Expected newline after last annotation"),
                LintViolation(6, 46, "Expected newline after last annotation"),
                LintViolation(7, 46, "Expected newline after last annotation"),
                LintViolation(9, 43, "Expected newline after last annotation"),
                LintViolation(10, 47, "Expected newline after last annotation"),
                LintViolation(11, 47, "Expected newline after last annotation"),
            ).isFormattedAs(formattedCode)
    }

    @Test
    fun `Given multiple annotations on same line as annotated construct`() {
        val code =
            """
            @Foo @Bar class FooBar {
                @Foo @Bar var foo: String
                @Foo @Bar fun bar() {}
            }
            """.trimIndent()
        val formattedCode =
            """
            @Foo @Bar
            class FooBar {
                @Foo @Bar
                var foo: String
                @Foo @Bar
                fun bar() {}
            }
            """.trimIndent()
        annotationRuleAssertThat(code)
            .hasLintViolations(
                LintViolation(1, 10, "Expected newline after last annotation"),
                LintViolation(2, 14, "Expected newline after last annotation"),
                LintViolation(3, 14, "Expected newline after last annotation"),
            ).isFormattedAs(formattedCode)
    }

    @Test
    fun `Given an array of annotations on the same line as the annotated construct then do not report a violation`() {
        val code =
            """
            @[Foo Bar] class FooBar2 {
                @[Foo Bar] var foo: String
                @[Foo Bar] fun bar() {}
            }
            """.trimIndent()
        annotationRuleAssertThat(code)
            .hasNoLintViolations()
    }

    @Test
    fun `Given multiple annotations on same line as annotated construct (without indentation)`() {
        val code =
            """
            @JvmField @Volatile var foo: String
            """.trimIndent()
        val formattedCode =
            """
            @JvmField @Volatile
            var foo: String
            """.trimIndent()
        annotationRuleAssertThat(code)
            .hasLintViolation(1, 20, "Expected newline after last annotation")
            .isFormattedAs(formattedCode)
    }

    @Test
    fun `Given multiple annotations on same line as annotated construct (without indentation but preceded by one or more blank line)`() {
        val code =
            """
            @JvmField @Volatile var foo: String
            """.trimIndent()
        val formattedCode =
            """
            @JvmField @Volatile
            var foo: String
            """.trimIndent()
        annotationRuleAssertThat(code)
            .hasLintViolation(1, 20, "Expected newline after last annotation")
            .isFormattedAs(formattedCode)
    }

    @Test
    fun `Given an annotation with a parameter not followed by a space but on same line as annotated construct`() {
        val code =
            """
            @Suppress("Something")class FooBar {
                @Suppress("Something")var foo: String
                @Suppress("Something")fun bar() {}
            }
            """.trimIndent()
        val formattedCode =
            """
            @Suppress("Something")
            class FooBar {
                @Suppress("Something")
                var foo: String
                @Suppress("Something")
                fun bar() {}
            }
            """.trimIndent()
        annotationRuleAssertThat(code)
            .hasLintViolations(
                LintViolation(1, 22, "Expected newline after last annotation"),
                LintViolation(2, 26, "Expected newline after last annotation"),
                LintViolation(3, 26, "Expected newline after last annotation"),
            ).isFormattedAs(formattedCode)
    }

    @Test
    fun `Given a multiline annotation and annotated construct on same line as closing parenthesis of the annotation`() {
        val code =
            """
            class FooBar {
                @Foo(
                    groups = [
                        "a",
                        "b"
                    ]
                ) val bar: Any
            }
            """.trimIndent()
        val formattedCode =
            """
            class FooBar {
                @Foo(
                    groups = [
                        "a",
                        "b"
                    ]
                )
                val bar: Any
            }
            """.trimIndent()
        annotationRuleAssertThat(code)
            .hasLintViolation(7, 6, "Expected newline after last annotation")
            .isFormattedAs(formattedCode)
    }

    @Test
    fun `Given a annotation with parameter followed by an annotation without parameters on the same line as the annotated construct`() {
        val code =
            """
            class FooBar {
                @Foo("foo")
                @Bar val bar: Any
                @Baz("baz") @Bar val baz: Any
            }
            """.trimIndent()
        val formattedCode =
            """
            class FooBar {
                @Foo("foo")
                @Bar
                val bar: Any
                @Baz("baz") @Bar
                val baz: Any
            }
            """.trimIndent()
        annotationRuleAssertThat(code)
            .withEditorConfigOverride(ANNOTATIONS_WITH_PARAMETERS_NOT_TO_BE_WRAPPED_PROPERTY to "Baz")
            .hasLintViolations(
                LintViolation(3, 9, "Expected newline after last annotation"),
                LintViolation(4, 21, "Expected newline after last annotation"),
            ).isFormattedAs(formattedCode)
    }

    @Test
    fun `Issue 497 - Given a data class`() {
        val code =
            """
            data class Foo(val bar: String)
            """.trimIndent()
        annotationRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `Issue 509 - Given a overridden function class`() {
        val code =
            """
            override fun foo()
            """.trimIndent()
        annotationRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `Issue 552 - Given multiple blank lines before annotation`() {
        val code =
            """


            @JvmField
            var foo: String
            """.trimIndent()
        annotationRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `Issue 642 - Given annotations on method parameters on same line as parameter`() {
        val code =
            """
            fun foo1(
                @Path("fooId") fooId: String,
                @Path("bar") bar: String,
                @Body body: Foo
            ): Completable

            fun foo2(@Query("include") include: String? = null, @QueryMap fields: Map<String, String> = emptyMap()): Single

            fun foo3(@Path("fooId") fooId: String): Completable
            """.trimIndent()
        annotationRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `Issue 642 - Given annotations on constructor parameters on same line as parameter`() {
        val code =
            """
            class Foo(@Path("fooId") val fooId: String)
            class Bar(
                @NotNull("fooId") val fooId: String,
                @NotNull("bar") bar: String
            )
            """.trimIndent()
        annotationRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `Issue 642 - Given annotations on arguments on same line as argument`() {
        val code =
            """
            val foo =
                foo(
                    @ExpressionStringAnn("foo") "test",
                    @ExpressionIntAnn("bar") 42
                )
            """.trimIndent()
        annotationRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `Given a function signature having annotated parameters, then allow the annotation to be on the same line as the annotated construct`() {
        val code =
            """
            fun foo(
                a: Int,
                @Bar1 b: Int,
                @Bar1 @Bar2 c: Int,
                @Bar3("bar3") @Bar1 d: Int
            ) {}
            """.trimIndent()
        annotationRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `Issue 740 - Given multiple annotations and ending with a EOL comment before annotated construct`() {
        val code =
            """
            annotation class A
            annotation class B

            @A // comment
            @B
            fun foo1() {}

            @A
            @B // comment
            fun foo2() {}
            """.trimIndent()
        annotationRuleAssertThat(code).hasNoLintViolations()
    }

    @Nested
    inner class `Given an annotation at file level` {
        @Test
        fun `Issue 714 - Given a file annotation without parameter on same line as package`() {
            val code =
                """
                @file:JvmName package foo.bar
                """.trimIndent()
            val formattedCode =
                """
                @file:JvmName

                package foo.bar
                """.trimIndent()
            annotationRuleAssertThat(code)
                .hasLintViolation(1, 15, "File annotations should be separated from file contents with a blank line")
                .isFormattedAs(formattedCode)
        }

        @Test
        fun `Given a file annotation with parameter followed by a blank line`() {
            val code =
                """
                @file:JvmName("FooClass")

                package foo.bar
                """.trimIndent()
            annotationRuleAssertThat(code).hasNoLintViolations()
        }

        @Test
        fun `Issue 624 - Given a file annotation with parameter on same line as package`() {
            val code =
                """
                @file:JvmName("FooClass") package foo.bar
                """.trimIndent()
            val formattedCode =
                """
                @file:JvmName("FooClass")

                package foo.bar
                """.trimIndent()
            annotationRuleAssertThat(code)
                .hasLintViolations(
                    LintViolation(1, 26, "Expected newline after last annotation"),
                    LintViolation(1, 27, "File annotations should be separated from file contents with a blank line"),
                ).isFormattedAs(formattedCode)
        }

        @Test
        fun `Issue 714 - Given a file annotation on the line above the package statement but without blank line in between`() {
            val code =
                """
                @file:JvmName
                package foo.bar
                """.trimIndent()
            val formattedCode =
                """
                @file:JvmName

                package foo.bar
                """.trimIndent()
            annotationRuleAssertThat(code)
                .hasLintViolation(2, 1, "File annotations should be separated from file contents with a blank line")
                .isFormattedAs(formattedCode)
        }

        @Test
        fun `Issue 714 - Given a file annotation followed by an EOL comment, on the line above the package statement but without blank line in between`() {
            val code =
                """
                @file:JvmName  // comment
                package foo.bar
                """.trimIndent()
            val formattedCode =
                """
                @file:JvmName  // comment

                package foo.bar
                """.trimIndent()
            annotationRuleAssertThat(code)
                .hasLintViolation(2, 1, "File annotations should be separated from file contents with a blank line")
                .isFormattedAs(formattedCode)
        }

        @Test
        fun `Issue 714 - Given a file annotation followed by an block comment, on the same line as the package statement`() {
            val code =
                """
                @file:JvmName /* comment */ package foo.bar
                """.trimIndent()
            val formattedCode =
                """
                @file:JvmName /* comment */

                package foo.bar
                """.trimIndent()
            annotationRuleAssertThat(code)
                .hasLintViolation(1, 29, "File annotations should be separated from file contents with a blank line")
                .isFormattedAs(formattedCode)
        }

        @Test
        fun `lint file annotations should be separated with a blank line in script 1`() {
            val code =
                """
                @file:Suppress("UnstableApiUsage")
                pluginManagement {
                }
                """.trimIndent()
            val formattedCode =
                """
                @file:Suppress("UnstableApiUsage")

                pluginManagement {
                }
                """.trimIndent()
            annotationRuleAssertThat(code)
                .asKotlinScript()
                .hasLintViolation(2, 1, "File annotations should be separated from file contents with a blank line")
                .isFormattedAs(formattedCode)
        }
    }

    @Test
    fun `Given a receiver with annotation having a parameter should not be separate line`() {
        val code =
            """
            annotation class Ann(val arg: Int = 0)

            fun @receiver:Ann(1) String.test() {}

            """.trimIndent()
        annotationRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `Issue 1539 - Given an annotation with parameter followed by an EOL comment and followed by another annotation`() {
        val code =
            """
            @Suppress("Something") // some comment
            @Bar
            class Foo
            """.trimIndent()
        annotationRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `Issue 1539 - Given an annotation with parameter followed by an EOL comment on separate line before annotated construct`() {
        val code =
            """
            @Suppress("Something")
            // some comment between last annotation and annotated construct
            class Foo
            """.trimIndent()
        annotationRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `Given multiple annotations on the same line without parameter but above the annotated construct`() {
        val code =
            """
            @Foo1 @Foo2
            fun foo() {}
            """.trimIndent()
        annotationRuleAssertThat(code)
            .hasNoLintViolations()
    }

    @Test
    fun `Given a line containing multiple annotations without parameters and another line with another annotation`() {
        val code =
            """
            @Foo1
            @Foo2 @Foo3
            fun foo() {}
            """.trimIndent()
        val formattedCode =
            """
            @Foo1
            @Foo2
            @Foo3
            fun foo() {}
            """.trimIndent()
        annotationRuleAssertThat(code)
            .asKotlinScript()
            .hasLintViolation(2, 7, "All annotations should either be on a single line or all annotations should be on a separate line")
            .isFormattedAs(formattedCode)
    }

    @Test
    fun `Given an annotated expression on same line as annotated construct and the annotation contains a parameter`() {
        val code =
            """
            fun foo() = @Foo1 @Foo2 @Foo3("bar3") @Foo4 baz()
            fun bar() = @Bar1 @Bar2 @Bar3("bar3") @Bar4 baz()
            """.trimIndent()
        val formattedCode =
            """
            fun foo() =
                @Foo1 @Foo2
                @Foo3("bar3")
                @Foo4
                baz()
            fun bar() =
                @Bar1 @Bar2 @Bar3("bar3") @Bar4
                baz()
            """.trimIndent()
        annotationRuleAssertThat(code)
            .withEditorConfigOverride(ANNOTATIONS_WITH_PARAMETERS_NOT_TO_BE_WRAPPED_PROPERTY to "Bar3")
            .hasLintViolations(
                LintViolation(1, 12, "Expected newline before annotation"),
                LintViolation(1, 24, "Expected newline before annotation"),
                LintViolation(1, 38, "Expected newline before annotation"),
                LintViolation(1, 44, "Expected newline after last annotation"),
                LintViolation(2, 12, "Expected newline before annotation"),
                LintViolation(2, 44, "Expected newline after last annotation"),
            ).isFormattedAs(formattedCode)
    }

    @Test
    fun `Given an annotated expression on same line as annotated construct and no annotation contains a parameter`() {
        val code =
            """
            fun foo() = @Bar1 @Bar2 bar()
            """.trimIndent()
        val formattedCode =
            """
            fun foo() =
                @Bar1 @Bar2
                bar()
            """.trimIndent()
        annotationRuleAssertThat(code)
            .hasLintViolations(
                LintViolation(1, 12, "Expected newline before annotation"),
                LintViolation(1, 24, "Expected newline after last annotation"),
            ).isFormattedAs(formattedCode)
    }

    @Test
    fun `Given an annotation with multiple annotation entries then do not force wrapping of the entries`() {
        val code =
            """
            @[JvmStatic Provides]
            fun foo() = 42
            """.trimIndent()
        annotationRuleAssertThat(code).hasNoLintViolations()
    }

    @Nested
    inner class `Array syntax annotations, Issue #1765` {
        @Test
        fun `annotation preceded by array syntax annotation`() {
            val code =
                """
                class Main {
                    @[Foo1 Foo2] @Foo3
                    fun foo() {}
                }
                """.trimIndent()
            val formattedCode =
                """
                class Main {
                    @[Foo1 Foo2]
                    @Foo3
                    fun foo() {}
                }
                """.trimIndent()
            annotationRuleAssertThat(code)
                .hasLintViolation(2, 5, "@[...] style annotations should be on a separate line from other annotations.")
                .isFormattedAs(formattedCode)
        }

        @Test
        fun `annotation followed by array syntax annotation`() {
            val code =
                """
                @Foo3 @[Foo1 Foo2]
                fun foo() {}
                """.trimIndent()
            val formattedCode =
                """
                @Foo3
                @[Foo1 Foo2]
                fun foo() {}
                """.trimIndent()
            annotationRuleAssertThat(code)
                .hasLintViolation(1, 7, "@[...] style annotations should be on a separate line from other annotations.")
                .isFormattedAs(formattedCode)
        }
    }

    @Test
    fun `Given an annotation and other modifiers before the annotated construct`() {
        val code =
            """
            @Bar("bar") public class Baz1
            @Foo("foo") public class Baz2

            @Bar("bar") public fun baz1() {}
            @Foo("foo") public fun baz2() {}
            """.trimIndent()
        val formattedCode =
            """
            @Bar("bar")
            public class Baz1
            @Foo("foo") public class Baz2

            @Bar("bar")
            public fun baz1() {}
            @Foo("foo") public fun baz2() {}
            """.trimIndent()
        annotationRuleAssertThat(code)
            .withEditorConfigOverride(ANNOTATIONS_WITH_PARAMETERS_NOT_TO_BE_WRAPPED_PROPERTY to "Foo")
            .hasLintViolations(
                LintViolation(1, 12, "Expected newline after last annotation"),
                LintViolation(4, 12, "Expected newline after last annotation"),
            ).isFormattedAs(formattedCode)
    }

    @Test
    fun `Given a single annotation on same line as a type parameter then do not report a violation`() {
        val code =
            """
            @Target(AnnotationTarget.TYPE)
            annotation class Foo
            val foo1: List<@Foo String> = emptyList()
            val foo2 = emptyList<@Foo String>()
            val foo3: Map<@Foo String, @Foo String> = emptyMap()
            val foo4 = emptyMap<@Foo String, @Foo String>()
            """.trimIndent()
        annotationRuleAssertThat(code)
            .hasNoLintViolations()
    }

    @Test
    fun `Given an annotation with parameter on followed by another modifier on the next line then do not report a violation`() {
        val code =
            """
            @Target(AnnotationTarget.TYPE)
            annotation class Foo
            """.trimIndent()
        annotationRuleAssertThat(code)
            .hasNoLintViolations()
    }

    @Nested
    inner class `Issue 1725 - Given multiple annotations on same line as a type parameter` {
        @Test
        fun `Given a list with multiple annotations on its type`() {
            val code =
                """
                val fooBar: List<@Foo @Bar String> = emptyList()
                """.trimIndent()
            val formattedCode =
                """
                val fooBar: List<
                    @Foo @Bar
                    String
                    > = emptyList()
                """.trimIndent()
            annotationRuleAssertThat(code)
                .addAdditionalRuleProvider { TrailingCommaOnDeclarationSiteRule() }
                .addAdditionalRuleProvider { WrappingRule() }
                .hasLintViolations(
                    LintViolation(1, 17, "Expected newline"),
                    LintViolation(1, 17, "Expected newline before annotation"),
                    LintViolation(1, 27, "Expected newline after last annotation"),
                    LintViolation(1, 34, "Expected newline"),
                ).isFormattedAs(formattedCode)
        }

        @Test
        fun `Given a custom type with multiple annotations on it type parameter(s)`() {
            val code =
                """
                val fooBar1: FooBar<String, @Foo String, @Foo @Bar String, @Bar("bar") @Foo String> = FooBar()
                val fooBar2: FooBar<String, @Foo String, @Foo @Bar String, @Baz("baz") @Foo String> = FooBar()
                """.trimIndent()
            val formattedCode =
                """
                val fooBar1: FooBar<
                    String,
                    @Foo String,
                    @Foo @Bar
                    String,
                    @Bar("bar")
                    @Foo
                    String
                > = FooBar()
                val fooBar2: FooBar<
                    String,
                    @Foo String,
                    @Foo @Bar
                    String,
                    @Baz("baz") @Foo
                    String
                > = FooBar()
                """.trimIndent()
            annotationRuleAssertThat(code)
                .addAdditionalRuleProvider { IndentationRule() }
                .addAdditionalRuleProvider { WrappingRule() }
                .withEditorConfigOverride(ANNOTATIONS_WITH_PARAMETERS_NOT_TO_BE_WRAPPED_PROPERTY to "Baz")
                .hasLintViolations(
                    LintViolation(1, 20, "Expected newline"),
                    LintViolation(1, 28, "Expected newline"),
                    LintViolation(1, 41, "Expected newline"),
                    LintViolation(1, 41, "Expected newline before annotation"),
                    LintViolation(1, 51, "Expected newline after last annotation"),
                    LintViolation(1, 59, "Expected newline"),
                    LintViolation(1, 59, "Expected newline before annotation"),
                    LintViolation(1, 71, "Expected newline before annotation"),
                    LintViolation(1, 76, "Expected newline after last annotation"),
                    LintViolation(1, 83, "Expected newline"),
                    LintViolation(2, 20, "Expected newline"),
                    LintViolation(2, 28, "Expected newline"),
                    LintViolation(2, 41, "Expected newline"),
                    LintViolation(2, 41, "Expected newline before annotation"),
                    LintViolation(2, 51, "Expected newline after last annotation"),
                    LintViolation(2, 59, "Expected newline"),
                    LintViolation(2, 59, "Expected newline before annotation"),
                    LintViolation(2, 76, "Expected newline after last annotation"),
                    LintViolation(2, 83, "Expected newline"),
                ).isFormattedAs(formattedCode)
        }
    }

    @Nested
    inner class `Given an array of annotations on same line as a type parameter` {
        @Test
        fun `Given a list with an array of annotations on its type`() {
            val code =
                """
                val fooBar: List<@[Foo Bar] String> = emptyList()
                """.trimIndent()
            annotationRuleAssertThat(code)
                .hasNoLintViolations()
        }
    }

    @Test
    fun `Given property allow trailing comma on declaration site is not set then remove trailing commas`() {
        val code =
            """
            enum class Foo {
                A,
                @Bar1 B,
                @Bar1 @Bar2 C,
                @Bar3("bar3") D,
                @Bar4("bar4") E,
                @Bar3("bar3") @Bar1 F,
                @Bar4("bar4") @Bar1 G
            }
            """.trimIndent()
        val formattedCode =
            """
            enum class Foo {
                A,
                @Bar1 B,
                @Bar1 @Bar2
                C,
                @Bar3("bar3")
                D,
                @Bar4("bar4") E,
                @Bar3("bar3")
                @Bar1
                F,
                @Bar4("bar4") @Bar1
                G
            }
            """.trimIndent()
        annotationRuleAssertThat(code)
            .withEditorConfigOverride(ANNOTATIONS_WITH_PARAMETERS_NOT_TO_BE_WRAPPED_PROPERTY to "Bar4")
            .hasLintViolations(
                LintViolation(4, 16, "Expected newline after last annotation"),
                LintViolation(5, 18, "Expected newline after last annotation"),
                LintViolation(7, 18, "Expected newline before annotation"),
                LintViolation(7, 24, "Expected newline after last annotation"),
                LintViolation(8, 24, "Expected newline after last annotation"),
            ).isFormattedAs(formattedCode)
    }

    @Nested
    inner class `Given a class with a primary constructor` {
        @Test
        fun `Issue 628 - Given a non-ktlint_official code style and an annotation followed by other modifier before the primary constructor (non ktlint_official code style)`() {
            val code =
                """
                class Foo @Inject internal constructor()
                """.trimIndent()
            annotationRuleAssertThat(code)
                .withEditorConfigOverride(CODE_STYLE_PROPERTY to CodeStyleValue.INTELLIJ_IDEA)
                .hasNoLintViolations()
        }

        @Nested
        inner class `Given ktlint_official code style` {
            @Test
            fun `Issue 628 - Given an annotation followed by other modifier before the primary constructor (ktlint_official code style)`() {
                val code =
                    """
                    class Foo @Inject internal constructor()
                    """.trimIndent()
                val formattedCode =
                    """
                    class Foo
                        @Inject
                        internal constructor()
                    """.trimIndent()
                annotationRuleAssertThat(code)
                    .withEditorConfigOverride(CODE_STYLE_PROPERTY to KTLINT_OFFICIAL)
                    .hasLintViolations(
                        LintViolation(1, 10, "Expected newline before annotation"),
                        LintViolation(1, 18, "Expected newline after last annotation"),
                    ).isFormattedAs(formattedCode)
            }

            @Test
            fun `Given an annotation with parameter`() {
                val code =
                    """
                    data class Foo @Bar1 @Bar2("bar") @Bar3 @Bar4 constructor(private val foobar: Int) {
                        fun foo(): String = "foo"
                    }
                    data class Baz @Baz1 @Baz2("bar") @Baz3 @Baz4 constructor(private val foobar: Int) {
                        fun foo(): String = "foo"
                    }
                    """.trimIndent()
                val formattedCode =
                    """
                    data class Foo
                        @Bar1
                        @Bar2("bar")
                        @Bar3
                        @Bar4
                        constructor(private val foobar: Int) {
                            fun foo(): String = "foo"
                        }
                    data class Baz
                        @Baz1 @Baz2("bar") @Baz3 @Baz4
                        constructor(private val foobar: Int) {
                            fun foo(): String = "foo"
                        }
                    """.trimIndent()
                annotationRuleAssertThat(code)
                    .addAdditionalRuleProvider { IndentationRule() }
                    .withEditorConfigOverride(CODE_STYLE_PROPERTY to KTLINT_OFFICIAL)
                    .withEditorConfigOverride(ANNOTATIONS_WITH_PARAMETERS_NOT_TO_BE_WRAPPED_PROPERTY to "Baz2")
                    .hasLintViolations(
                        LintViolation(1, 15, "Expected newline before annotation"),
                        LintViolation(1, 21, "Expected newline before annotation"),
                        LintViolation(1, 34, "Expected newline before annotation"),
                        LintViolation(1, 46, "Expected newline after last annotation"),
                        LintViolation(4, 15, "Expected newline before annotation"),
                        LintViolation(4, 46, "Expected newline after last annotation"),
                    ).isFormattedAs(formattedCode)
            }

            @Test
            fun `Given annotations without parameters`() {
                val code =
                    """
                    data class Foo @Bar1 @Bar2 constructor(private val foobar: Int) {
                        fun foo(): String = "foo"
                    }
                    """.trimIndent()
                val formattedCode =
                    """
                    data class Foo
                        @Bar1 @Bar2
                        constructor(private val foobar: Int) {
                        fun foo(): String = "foo"
                    }
                    """.trimIndent()
                annotationRuleAssertThat(code)
                    .withEditorConfigOverride(CODE_STYLE_PROPERTY to KTLINT_OFFICIAL)
                    .hasLintViolations(
                        LintViolation(1, 15, "Expected newline before annotation"),
                        LintViolation(1, 27, "Expected newline after last annotation"),
                    ).isFormattedAs(formattedCode)
            }

            @Test
            fun `Given single annotation without parameter`() {
                val code =
                    """
                    data class Foo @Bar1 constructor(private val foobar: Int) {
                        fun foo(): String = "foo"
                    }
                    """.trimIndent()
                val formattedCode =
                    """
                    data class Foo
                        @Bar1
                        constructor(private val foobar: Int) {
                        fun foo(): String = "foo"
                    }
                    """.trimIndent()
                annotationRuleAssertThat(code)
                    .withEditorConfigOverride(CODE_STYLE_PROPERTY to KTLINT_OFFICIAL)
                    .hasLintViolations(
                        LintViolation(1, 15, "Expected newline before annotation"),
                        LintViolation(1, 21, "Expected newline after last annotation"),
                    ).isFormattedAs(formattedCode)
            }
        }
    }

    @Test
    fun `Given an annotation with parameter preceded by a blank line then do not remove the blank line`() {
        val code =
            """
            val foo = "foo"

            @Bar("bar")
            fun bar() = "bar"
            """.trimIndent()
        annotationRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `Given a function literal containing an annotated expression`() {
        val code =
            """
            val foo1 = {
                @Bar("bar")
                foobar { "foobar" }
            }
            val foo2 = { @Bar("bar") foobar { "foobar" } }
            val foo3 = { @Baz("baz") foobar { "foobar" } }
            """.trimIndent()
        val formattedCode =
            """
            val foo1 = {
                @Bar("bar")
                foobar { "foobar" }
            }
            val foo2 = {
                @Bar("bar")
                foobar { "foobar" }
            }
            val foo3 = { @Baz("baz") foobar { "foobar" } }
            """.trimIndent()
        annotationRuleAssertThat(code)
            .addAdditionalRuleProvider { IndentationRule() }
            .withEditorConfigOverride(ANNOTATIONS_WITH_PARAMETERS_NOT_TO_BE_WRAPPED_PROPERTY to "Baz")
            .hasLintViolations(
                LintViolation(5, 13, "Expected newline before annotation"),
                LintViolation(5, 25, "Expected newline after last annotation"),
                LintViolation(5, 45, "Expected newline"),
            ).isFormattedAs(formattedCode)
    }

    @Test
    fun `Given a try-finally containing an annotated expression`() {
        val code =
            """
            val foo = try {
                @Bar("bar")
                bar()
            } finally {
                // something
            }
            """.trimIndent()
        annotationRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `Given a class with a super type list starting with an annotation having parameter`() {
        val code =
            """
            class Foo(
                bar: Bar,
            ) : @Suppress("DEPRECATION")
                FooBar()
            """.trimIndent()
        annotationRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `Issue 2399 - Given a type projection with an annotated type reference`() {
        val code =
            """
            val foo: List<
                @Bar("bar")
                Any,
            >? = null
            """.trimIndent()
        annotationRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `Issue 2852 - Handle whitelisted annotations the same as annotations without parameters`() {
        val code =
            """
            val list: List<
                @Annotation1(name = "foo") Foo,
                @Annotation2(name = "bar") Bar,
                @Annotation3(name = "baz") Baz,
            >
            """.trimIndent()
        val formattedCode =
            """
            val list: List<
                @Annotation1(name = "foo") Foo,
                @Annotation2(name = "bar") Bar,
                @Annotation3(name = "baz")
                Baz,
            >
            """.trimIndent()
        annotationRuleAssertThat(code)
            .withEditorConfigOverride(ANNOTATIONS_WITH_PARAMETERS_NOT_TO_BE_WRAPPED_PROPERTY to "Annotation1, Annotation2")
            .hasLintViolation(4, 31, "Expected newline after last annotation")
            .isFormattedAs(formattedCode)
    }

    @Test
    fun `Issue 2852 - Handle all annotations the same as annotations without parameters`() {
        val code =
            """
            val list: List<
                @Annotation1(name = "foo") Foo,
                @Annotation2(name = "bar") Bar,
                @Annotation3(name = "baz") Baz,
            >
            """.trimIndent()
        annotationRuleAssertThat(code)
            .withEditorConfigOverride(ANNOTATIONS_WITH_PARAMETERS_NOT_TO_BE_WRAPPED_PROPERTY to "*")
            .hasNoLintViolations()
    }

    @Test
    fun `Issue 2955 - Given an annotated expression followed by an operation reference`() {
        val code =
            """
            fun foo() =
                @Suppress("DEPRECATION")
                false != true
            """.trimIndent()
        annotationRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `Issue 3213 - Given an annotated before a lambda expression then do no wrap the annotation to a new line as it results in a compilation error`() {
        val code =
            """
            val foo1 = bar @FooBar() { baz }
            val foo2 = @FooBar() { baz }
            """.trimIndent()
        annotationRuleAssertThat(code).hasNoLintViolations()
    }
}
