package io.github.ktlint.core.ruleset.standard.rules

import io.github.ktlint.core.test.KtLintAssertThat.Companion.assertThatRule
import org.jetbrains.kotlin.lexer.KtTokens
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.junit.jupiter.params.provider.ValueSource

class ClassNamingRuleTest {
    private val classNamingRuleAssertThat = assertThatRule { ClassNamingRule() }

    @Nested
    inner class `Given a class declaration` {
        @Test
        fun `Given a valid class name then do not emit`() {
            val code =
                """
                class Foo1
                """.trimIndent()
            classNamingRuleAssertThat(code).hasNoLintViolations()
        }

        @ParameterizedTest(name = ": {0}")
        @ValueSource(
            strings = [
                "foo",
                "Foo_Bar",
            ],
        )
        fun `Given an invalid class name then do emit`(className: String) {
            val code =
                """
                class $className
                """.trimIndent()
            @Suppress("ktlint:standard:max-line-length")
            classNamingRuleAssertThat(code)
                .hasLintViolationWithoutAutoCorrect(1, 7, "Class or object name should start with an uppercase letter and use camel case")
        }

        @Nested
        inner class `Given a class with name between backticks` {
            @Test
            fun `Given a file that does not import a class from the JUnit Jupiter Api then do emit`() {
                val code =
                    """
                    class `foo`
                    """.trimIndent()
                @Suppress("ktlint:standard:max-line-length")
                classNamingRuleAssertThat(code)
                    .hasLintViolationWithoutAutoCorrect(1, 7, "Class or object name should start with an uppercase letter and use camel case")
            }

            @Test
            fun `Given file which imports a class from the JUnit Jupiter Api class then do not emit`() {
                val code =
                    """
                    import org.junit.jupiter.api.Nested
                    import org.junit.jupiter.api.Test

                    class FunTest {
                        @Nested
                        inner class `Some descriptive class name` {
                            @Test
                            fun `Some descriptive test name`() {}
                        }
                    }
                    """.trimIndent()
                classNamingRuleAssertThat(code).hasNoLintViolations()
            }
        }

        @ParameterizedTest(name = "Suppression annotation: {0}")
        @ValueSource(
            strings = [
                "ktlint:standard:class-naming",
                "ClassName", // IntelliJ IDEA suppression
            ],
        )
        fun `Given class with a disallowed name which is suppressed`(suppressionName: String) {
            val code =
                """
                @Suppress("$suppressionName")
                class Foo_Bar
                """.trimIndent()
            classNamingRuleAssertThat(code).hasNoLintViolations()
        }
    }

    @Nested
    inner class `Given an object declaration` {
        @Test
        fun `Given a valid object name then do not emit`() {
            val code =
                """
                object Foo
                """.trimIndent()
            classNamingRuleAssertThat(code).hasNoLintViolations()
        }

        @ParameterizedTest(name = ": {0}")
        @ValueSource(
            strings = [
                "foo",
                "Foo_Bar",
            ],
        )
        fun `Given an invalid object name then do emit`(objectName: String) {
            val code =
                """
                object $objectName
                """.trimIndent()
            classNamingRuleAssertThat(code)
                .hasLintViolationWithoutAutoCorrect(1, 8, "Class or object name should start with an uppercase letter and use camel case")
        }

        @ParameterizedTest(name = "Suppression annotation: {0}")
        @ValueSource(
            strings = [
                "ktlint:standard:class-naming",
                "ClassName", // IntelliJ IDEA suppression
            ],
        )
        fun `Given object with a disallowed name which is suppressed`(suppressionName: String) {
            val code =
                """
                @Suppress("$suppressionName")
                object Foo_Bar
                """.trimIndent()
            classNamingRuleAssertThat(code).hasNoLintViolations()
        }
    }

    @Test
    fun `Issue 1757 - Given a class name containing diacritics is allowed`() {
        val code =
            """
            class ŸèśThîsIsAllowed123
            """.trimIndent()
        classNamingRuleAssertThat(code).hasNoLintViolations()
    }

    @ParameterizedTest(name = "Keyword: {0}")
    @MethodSource("ktTokens")
    fun `Issue 2352 - Given a keyword then allow it to be wrapped between backticks`(keyword: String) {
        val code =
            """
            class `$keyword`
            """.trimIndent()
        classNamingRuleAssertThat(code).hasNoLintViolations()
    }

    private companion object {
        @Suppress("UnstableApiUsage", "UnusedPrivateMember")
        @JvmStatic
        fun ktTokens() =
            KtTokens.KEYWORDS.types
                .plus(KtTokens.SOFT_KEYWORDS.types)
                .filterNot { it == KtTokens.AS_SAFE || it == KtTokens.NOT_IN || it == KtTokens.NOT_IS }
                .map { it.debugName }
    }
}
