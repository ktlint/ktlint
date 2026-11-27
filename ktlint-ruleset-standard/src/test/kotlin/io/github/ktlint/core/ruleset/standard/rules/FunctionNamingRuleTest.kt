package io.github.ktlint.core.ruleset.standard.rules

import io.github.ktlint.core.ruleset.standard.rules.FunctionNamingRule.Companion.IGNORE_WHEN_ANNOTATED_WITH_PROPERTY
import io.github.ktlint.core.test.KtLintAssertThat
import org.jetbrains.kotlin.lexer.KtTokens
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.junit.jupiter.params.provider.ValueSource

class FunctionNamingRuleTest {
    private val functionNamingRuleAssertThat =
        KtLintAssertThat.assertThatRule { FunctionNamingRule() }

    @Test
    fun `Given a valid function name then do not emit`() {
        val code =
            """
            fun foo1() = "foo"
            """.trimIndent()
        functionNamingRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `Given a factory method then do not emit`() {
        val code =
            """
            interface Foo
            class FooImpl : Foo
            fun Foo(): Foo = FooImpl()
            fun Bar.Foo(): Foo = FooImpl()
            """.trimIndent()
        functionNamingRuleAssertThat(code).hasNoLintViolations()
    }

    @Nested
    inner class `Given a function name between backticks` {
        @Test
        fun `Given a file that does not import a class from the JUnit Jupiter then do emit`() {
            val code =
                """
                fun `Some name`() {}
                """.trimIndent()
            @Suppress("ktlint:standard:max-line-length")
            functionNamingRuleAssertThat(code)
                .hasLintViolationWithoutAutoCorrect(1, 5, "Function name should start with a lowercase letter (except factory methods) and use camel case")
        }

        @ParameterizedTest(name = "Junit import: {0}")
        @ValueSource(
            strings = [
                "org.junit.jupiter.api.Test",
                "org.junit.jupiter.api.*",
                "org.junit.jupiter.*",
                "org.junit.*",
            ],
        )
        fun `Given file which imports a class from the JUnit Jupiter then do not emit`(import: String) {
            val code =
                """
                import $import

                class FunTest {
                    @Test
                    fun `Some descriptive test name`() {}
                }
                """.trimIndent()
            functionNamingRuleAssertThat(code).hasNoLintViolations()
        }
    }

    @Nested
    inner class `Given a function name containing an underscore` {
        @Test
        fun `Given a file that does not import a class from the JUnit Jupiter then do emit`() {
            val code =
                """
                fun do_something() {}
                """.trimIndent()
            @Suppress("ktlint:standard:max-line-length")
            functionNamingRuleAssertThat(code)
                .hasLintViolationWithoutAutoCorrect(1, 5, "Function name should start with a lowercase letter (except factory methods) and use camel case")
        }

        @ParameterizedTest(name = "Junit import: {0}")
        @ValueSource(
            strings = [
                "io.kotest.*",
                "org.junit.jupiter.api.Test",
                "org.junit.jupiter.api.*",
                "org.junit.jupiter.*",
                "org.junit.*",
                "kotlin.test.*",
                "org.testng.*",
            ],
        )
        fun `Given file which imports a class from the JUnit Jupiter then do not emit`(import: String) {
            val code =
                """
                import $import

                class FunTest {
                    @Test
                    fun givenSomeCondition_whenSomeAction_thenExpectation() {}
                }
                """.trimIndent()
            functionNamingRuleAssertThat(code).hasNoLintViolations()
        }
    }

    @ParameterizedTest(name = ": {0}")
    @ValueSource(
        strings = [
            "Foo",
            "Foo_Bar",
        ],
    )
    fun `Given an invalid function name then do emit`(functionName: String) {
        val code =
            """
            fun $functionName() = "foo"
            """.trimIndent()
        @Suppress("ktlint:standard:max-line-length")
        functionNamingRuleAssertThat(code)
            .hasLintViolationWithoutAutoCorrect(1, 5, "Function name should start with a lowercase letter (except factory methods) and use camel case")
    }

    @ParameterizedTest(name = "Suppression annotation: {0}")
    @ValueSource(
        strings = [
            "ktlint:standard:function-naming",
            "FunctionName", // IntelliJ IDEA suppression
        ],
    )
    fun `Given method with a disallowed name which is suppressed`(suppressionName: String) {
        val code =
            """
            @Suppress("$suppressionName")
            fun Foo() {}
            """.trimIndent()
        functionNamingRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `Issue 1757 - Given a function name containing diacritics then do not report a violation on the diacritics`() {
        val code =
            """
            fun ÿèśThîsIsAllowed123() = "foo"
            """.trimIndent()
        functionNamingRuleAssertThat(code).hasNoLintViolations()
    }

    @ParameterizedTest(name = "Junit import: {0}")
    @ValueSource(
        strings = [
            "org.junit.jupiter.api.Test",
            "org.junit.jupiter.api.*",
            "org.junit.jupiter.*",
            "org.junit.*",
            "kotlin.test.*",
            "org.testng.*",
        ],
    )
    fun `Given file which imports a class from the JUnit Jupiter then do not emit`(import: String) {
        val code =
            """
            import $import

            class FunTest {
                @Test
                fun `Ÿèś thîs is allowed 123`() {}
            }
            """.trimIndent()
        functionNamingRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `Issue 2260 - Given an anonymous function in a property assignment`() {
        val code =
            """
            val foo =
                fun(): String {
                    return "foo"
                }
            """.trimIndent()
        functionNamingRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `Issue 2271 - Given an override fun then do not report a violation of the rule as it might not be possible to change the signature of the rule when defined outside scope of project`() {
        val code =
            """
            // Assume that Bar is defined outside the scope of the project. As of that the function signature can not be changed to comply
            // to the function-naming rule. In case that Bar is defined inside the scope of the project, the violation will still be
            // reported in the Bar class/interface itself.
            class Foo : Bar {
                override fun Something()
            }
            """.trimIndent()
        functionNamingRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `Issue 2259 - Given a fun which is to be ignored because it is annotated with a blacklisted annotation`() {
        val code =
            """
            @Bar
            @Foo
            fun SomeFooBar()

            @Composable
            @Foo
            fun SomeComposableFoo()

            @Bar
            @Composable
            fun SomeComposableBar()
            """.trimIndent()
        functionNamingRuleAssertThat(code)
            .withEditorConfigOverride(IGNORE_WHEN_ANNOTATED_WITH_PROPERTY to "Composable, Foo")
            .hasNoLintViolations()
    }

    @Test
    fun `Issue 2259 - Given a fun which is to be ignored because it is annotated with a blacklisted annotation using annotation array`() {
        val code =
            """
            @[Bar Foo]
            fun SomeFooBar()

            @[Composable Foo]
            fun SomeComposableFoo()

            @[Bar Composable]
            fun SomeComposableBar()
            """.trimIndent()
        functionNamingRuleAssertThat(code)
            .withEditorConfigOverride(IGNORE_WHEN_ANNOTATED_WITH_PROPERTY to "Composable, Foo")
            .hasNoLintViolations()
    }

    @Test
    fun `Issue 2350 - Given a factory method with generic type`() {
        val code =
            """
            fun <T> Generics(action: () -> T): Generics<T> = Generics(action())
            """.trimIndent()
        functionNamingRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `Issue 2350 - Given a factory method which overloads a class constructor`() {
        val code =
            """
            class Foo(value: String)

            fun Foo(value: Double) = Foo(value.toString())
            """.trimIndent()
        functionNamingRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `Issue 2383 - Given a class with tests in JUnit3 style`() {
        val code =
            """
            import junit.framework.TestCase

            class FooTest : TestCase() {
                fun testFoo_bar() {
                }
            }
            """.trimIndent()
        functionNamingRuleAssertThat(code).hasNoLintViolations()
    }

    @ParameterizedTest(name = "Keyword: {0}")
    @MethodSource("ktTokens")
    fun `Issue 2352 - Given a keyword then allow it to be wrapped between backticks`(keyword: String) {
        val code =
            """
            fun `$keyword`() = "foo"
            """.trimIndent()
        functionNamingRuleAssertThat(code).hasNoLintViolations()
    }

    private companion object {
        @JvmStatic
        @Suppress("UnstableApiUsage", "UnusedPrivateMember")
        fun ktTokens() =
            KtTokens.KEYWORDS.types
                .plus(KtTokens.SOFT_KEYWORDS.types)
                .filterNot { it == KtTokens.AS_SAFE || it == KtTokens.NOT_IN || it == KtTokens.NOT_IS }
                .map { it.debugName }
    }
}
