package io.github.ktlint.core.ruleset.standard.rules

import io.github.ktlint.core.rule.engine.core.api.editorconfig.CODE_STYLE_PROPERTY
import io.github.ktlint.core.rule.engine.core.api.editorconfig.CodeStyleValue.ANDROID_STUDIO
import io.github.ktlint.core.rule.engine.core.api.editorconfig.CodeStyleValue.INTELLIJ_IDEA
import io.github.ktlint.core.rule.engine.core.api.editorconfig.CodeStyleValue.KTLINT_OFFICIAL
import io.github.ktlint.core.test.KtLintAssertThat.Companion.assertThatRule
import io.github.ktlint.core.test.KtlintDocumentationTest
import io.github.ktlint.core.test.LintViolation
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.ValueSource

class BackingPropertyNamingRuleTest {
    private val backingPropertyNamingRuleAssertThat = assertThatRule { BackingPropertyNamingRule() }

    @Nested
    inner class `Given a backing property defined as normal class member` {
        @Nested
        inner class `Given a correlated property` {
            @ParameterizedTest(name = "Correlated property name: {0}")
            @ValueSource(
                strings = [
                    "foo",
                    "føø",
                ],
            )
            fun `Given the correlated property is defined as class member then do not emit`(propertyName: String) {
                val code =
                    """
                    class Foo {
                        private var _$propertyName = "some-value"

                        val $propertyName: String
                            get() = _$propertyName
                    }
                    """.trimIndent()
                backingPropertyNamingRuleAssertThat(code).hasNoLintViolations()
            }

            @Test
            fun `Given that the correlated property is implicitly public then do not emit`() {
                val code =
                    """
                    class Foo {
                        private val _elementList = mutableListOf<Element>()

                        val elementList: List<Element>
                            get() = _elementList
                    }
                    """.trimIndent()
                backingPropertyNamingRuleAssertThat(code).hasNoLintViolations()
            }

            @Test
            fun `Given that the correlated property is explicitly public then do not emit`() {
                val code =
                    """
                    class Foo {
                        private val _elementList = mutableListOf<Element>()

                        public val elementList: List<Element>
                            get() = _elementList
                    }
                    """.trimIndent()
                backingPropertyNamingRuleAssertThat(code).hasNoLintViolations()
            }

            @ParameterizedTest(name = "Modifier: {0}")
            @ValueSource(
                strings = [
                    "private",
                    "protected",
                    "internal",
                ],
            )
            fun `Given ktlint_official code style, and the correlated property is non-public then emit`(modifier: String) {
                val code =
                    """
                    class Foo {
                        private val _elementList = mutableListOf<Element>()

                        $modifier val elementList: List<Element>
                            get() = _elementList
                    }
                    """.trimIndent()
                @Suppress("ktlint:standard:max-line-length")
                backingPropertyNamingRuleAssertThat(code)
                    .withEditorConfigOverride(CODE_STYLE_PROPERTY to KTLINT_OFFICIAL)
                    .hasLintViolationWithoutAutoCorrect(2, 17, "Backing property is only allowed when the matching property or function is public")
            }

            @ParameterizedTest(name = "Modifier: {0}")
            @ValueSource(
                strings = [
                    "private",
                    "protected",
                    "internal",
                ],
            )
            fun `Given intellij_idea code style, and the correlated property is non-public then emit`(modifier: String) {
                val code =
                    """
                    class Foo {
                        private val _elementList = mutableListOf<Element>()

                        $modifier val elementList: List<Element>
                            get() = _elementList
                    }
                    """.trimIndent()
                @Suppress("ktlint:standard:max-line-length")
                backingPropertyNamingRuleAssertThat(code)
                    .withEditorConfigOverride(CODE_STYLE_PROPERTY to INTELLIJ_IDEA)
                    .hasLintViolationWithoutAutoCorrect(2, 17, "Backing property is only allowed when the matching property or function is public")
            }

            @ParameterizedTest(name = "Modifier: {0}")
            @ValueSource(
                strings = [
                    "private",
                    "protected",
                    "internal",
                ],
            )
            fun `Given android_studio code style, and the correlated property is non-public then do not emit`(modifier: String) {
                val code =
                    """
                    class Foo {
                        private val _elementList = mutableListOf<Element>()

                        $modifier val elementList: List<Element>
                            get() = _elementList
                    }
                    """.trimIndent()
                backingPropertyNamingRuleAssertThat(code)
                    .withEditorConfigOverride(CODE_STYLE_PROPERTY to ANDROID_STUDIO)
                    .hasNoLintViolations()
            }
        }

        @Nested
        inner class `Given a co=related function` {
            @ParameterizedTest(name = "Correlated property name: {0}")
            @CsvSource(
                value = [
                    "foo,getFoo",
                    "føø,getFøø",
                ],
            )
            fun `Given a valid backing property then do not emit`(
                propertyName: String,
                functionName: String,
            ) {
                val code =
                    """
                    class Foo {
                        private var _$propertyName = "some-value"

                        fun $functionName(): String = _$propertyName
                    }
                    """.trimIndent()
                backingPropertyNamingRuleAssertThat(code).hasNoLintViolations()
            }

            @Test
            fun `Given that the correlated function is implicitly public then do not emit`() {
                val code =
                    """
                    class Foo {
                        private val _elementList = mutableListOf<Element>()

                        fun getElementList(): List<Element> = _elementList
                    }
                    """.trimIndent()
                backingPropertyNamingRuleAssertThat(code).hasNoLintViolations()
            }

            @Test
            fun `Given that the correlated function is explicitly public then do not emit`() {
                val code =
                    """
                    class Foo {
                        private val _elementList = mutableListOf<Element>()

                        public fun getElementList(): List<Element> = _elementList
                    }
                    """.trimIndent()
                backingPropertyNamingRuleAssertThat(code).hasNoLintViolations()
            }

            @ParameterizedTest(name = "Modifier: {0}")
            @ValueSource(
                strings = [
                    "private",
                    "protected",
                    "internal",
                ],
            )
            fun `Given ktlint_official code style, and the correlated function is non-public then emit`(modifier: String) {
                val code =
                    """
                    class Foo {
                        private val _elementList = mutableListOf<Element>()

                        $modifier fun getElementList(): List<Element> = _elementList
                    }
                    """.trimIndent()
                @Suppress("ktlint:standard:max-line-length")
                backingPropertyNamingRuleAssertThat(code)
                    .withEditorConfigOverride(CODE_STYLE_PROPERTY to KTLINT_OFFICIAL)
                    .hasLintViolationWithoutAutoCorrect(2, 17, "Backing property is only allowed when the matching property or function is public")
            }

            @ParameterizedTest(name = "Modifier: {0}")
            @ValueSource(
                strings = [
                    "private",
                    "protected",
                    "internal",
                ],
            )
            fun `Given intellij_idea code style, and the correlated function is non-public then emit`(modifier: String) {
                val code =
                    """
                    class Foo {
                        private val _elementList = mutableListOf<Element>()

                        $modifier fun getElementList(): List<Element> = _elementList
                    }
                    """.trimIndent()
                @Suppress("ktlint:standard:max-line-length")
                backingPropertyNamingRuleAssertThat(code)
                    .withEditorConfigOverride(CODE_STYLE_PROPERTY to INTELLIJ_IDEA)
                    .hasLintViolationWithoutAutoCorrect(2, 17, "Backing property is only allowed when the matching property or function is public")
            }

            @ParameterizedTest(name = "Modifier: {0}")
            @ValueSource(
                strings = [
                    "private",
                    "protected",
                    "internal",
                ],
            )
            fun `Given android_studio code style, and the correlated function is non-public then do not emit`(modifier: String) {
                val code =
                    """
                    class Foo {
                        private val _elementList = mutableListOf<Element>()

                        $modifier fun getElementList(): List<Element> = _elementList
                    }
                    """.trimIndent()
                backingPropertyNamingRuleAssertThat(code)
                    .withEditorConfigOverride(CODE_STYLE_PROPERTY to ANDROID_STUDIO)
                    .hasNoLintViolations()
            }

            @Test
            fun `Given that the correlated function has at least 1 parameter then emit`() {
                val code =
                    """
                    class Foo {
                        private val _elementList = mutableListOf<Element>()

                        fun getElementList(bar: String): List<Element> = _elementList + bar
                    }
                    """.trimIndent()
                @Suppress("ktlint:standard:max-line-length")
                backingPropertyNamingRuleAssertThat(code)
                    .hasLintViolationWithoutAutoCorrect(2, 17, "Backing property is only allowed when a matching property or function exists")
            }
        }
    }

    @Nested
    inner class `Given a private backing property defined in a non-private companion object` {
        @Nested
        inner class `Given a correlated property` {
            @ParameterizedTest(name = "Correlated property name: {0}")
            @ValueSource(
                strings = [
                    "foo",
                    "føø",
                ],
            )
            fun `Given the correlated property is defined as class member then do not emit`(propertyName: String) {
                val code =
                    """
                    class Foo {
                        val $propertyName: String
                            get() = _$propertyName

                        companion object {
                            private var _$propertyName = "some-value"
                        }
                    }
                    """.trimIndent()
                backingPropertyNamingRuleAssertThat(code).hasNoLintViolations()
            }

            @Test
            fun `Given that the correlated property is implicitly public then do not emit`() {
                val code =
                    """
                    class Foo {
                        val elementList: List<Element>
                            get() = _elementList

                        companion object {
                            private val _elementList = mutableListOf<Element>()
                        }
                    }
                    """.trimIndent()
                backingPropertyNamingRuleAssertThat(code).hasNoLintViolations()
            }

            @Test
            fun `Given that the correlated property is explicitly public then do not emit`() {
                val code =
                    """
                    class Foo {
                        public val elementList: List<Element>
                            get() = _elementList

                        companion object {
                            private val _elementList = mutableListOf<Element>()
                        }
                    }
                    """.trimIndent()
                backingPropertyNamingRuleAssertThat(code).hasNoLintViolations()
            }

            @ParameterizedTest(name = "Modifier: {0}")
            @ValueSource(
                strings = [
                    "private",
                    "protected",
                    "internal",
                ],
            )
            fun `Given ktlint_official code style, and the correlated property is non-public then emit`(modifier: String) {
                val code =
                    """
                    class Foo {
                        $modifier val elementList: List<Element>
                            get() = _elementList

                        companion object {
                            private val _elementList = mutableListOf<Element>()
                        }
                    }
                    """.trimIndent()
                @Suppress("ktlint:standard:max-line-length")
                backingPropertyNamingRuleAssertThat(code)
                    .withEditorConfigOverride(CODE_STYLE_PROPERTY to KTLINT_OFFICIAL)
                    .hasLintViolationWithoutAutoCorrect(6, 21, "Backing property is only allowed when the matching property or function is public")
            }

            @ParameterizedTest(name = "Modifier: {0}")
            @ValueSource(
                strings = [
                    "private",
                    "protected",
                    "internal",
                ],
            )
            fun `Given intellij_idea code style, and the correlated property is non-public then emit`(modifier: String) {
                val code =
                    """
                    class Foo {
                        $modifier val elementList: List<Element>
                            get() = _elementList

                        companion object {
                            private val _elementList = mutableListOf<Element>()
                        }
                    }
                    """.trimIndent()
                @Suppress("ktlint:standard:max-line-length")
                backingPropertyNamingRuleAssertThat(code)
                    .withEditorConfigOverride(CODE_STYLE_PROPERTY to INTELLIJ_IDEA)
                    .hasLintViolationWithoutAutoCorrect(6, 21, "Backing property is only allowed when the matching property or function is public")
            }

            @ParameterizedTest(name = "Modifier: {0}")
            @ValueSource(
                strings = [
                    "private",
                    "protected",
                    "internal",
                ],
            )
            fun `Given android_studio code style, and the correlated property is non-public then do not emit`(modifier: String) {
                val code =
                    """
                    class Foo {
                        $modifier val elementList: List<Element>
                            get() = _elementList

                        companion object {
                            private val _elementList = mutableListOf<Element>()
                        }
                    }
                    """.trimIndent()
                backingPropertyNamingRuleAssertThat(code)
                    .withEditorConfigOverride(CODE_STYLE_PROPERTY to ANDROID_STUDIO)
                    .hasNoLintViolations()
            }
        }

        @Nested
        inner class `Given a co=related function` {
            @ParameterizedTest(name = "Correlated property name: {0}")
            @CsvSource(
                value = [
                    "foo,getFoo",
                    "føø,getFøø",
                ],
            )
            fun `Given a valid backing property then do not emit`(
                propertyName: String,
                functionName: String,
            ) {
                val code =
                    """
                    class Foo {
                        fun $functionName(): String = _$propertyName

                        companion object {
                            private var _$propertyName = "some-value"
                        }
                    }
                    """.trimIndent()
                backingPropertyNamingRuleAssertThat(code).hasNoLintViolations()
            }

            @Test
            fun `Given that the correlated function is implicitly public then do not emit`() {
                val code =
                    """
                    class Foo {
                        fun getElementList(): List<Element> = _elementList

                        companion object {
                            private val _elementList = mutableListOf<Element>()
                        }
                    }
                    """.trimIndent()
                backingPropertyNamingRuleAssertThat(code).hasNoLintViolations()
            }

            @Test
            fun `Given that the correlated function is explicitly public then do not emit`() {
                val code =
                    """
                    class Foo {
                        public fun getElementList(): List<Element> = _elementList

                        companion object {
                            private val _elementList = mutableListOf<Element>()
                        }
                    }
                    """.trimIndent()
                backingPropertyNamingRuleAssertThat(code).hasNoLintViolations()
            }

            @ParameterizedTest(name = "Modifier: {0}")
            @ValueSource(
                strings = [
                    "private",
                    "protected",
                    "internal",
                ],
            )
            fun `Given ktlint_official code style, and the correlated function is non-public then emit`(modifier: String) {
                val code =
                    """
                    class Foo {
                        $modifier fun getElementList(): List<Element> = _elementList

                        companion object {
                            private val _elementList = mutableListOf<Element>()
                        }
                    }
                    """.trimIndent()
                @Suppress("ktlint:standard:max-line-length")
                backingPropertyNamingRuleAssertThat(code)
                    .withEditorConfigOverride(CODE_STYLE_PROPERTY to KTLINT_OFFICIAL)
                    .hasLintViolationWithoutAutoCorrect(5, 21, "Backing property is only allowed when the matching property or function is public")
            }

            @ParameterizedTest(name = "Modifier: {0}")
            @ValueSource(
                strings = [
                    "private",
                    "protected",
                    "internal",
                ],
            )
            fun `Given intellij_idea code style, and the correlated function is non-public then emit`(modifier: String) {
                val code =
                    """
                    class Foo {
                        $modifier fun getElementList(): List<Element> = _elementList

                        companion object {
                            private val _elementList = mutableListOf<Element>()
                        }
                    }
                    """.trimIndent()
                @Suppress("ktlint:standard:max-line-length")
                backingPropertyNamingRuleAssertThat(code)
                    .withEditorConfigOverride(CODE_STYLE_PROPERTY to INTELLIJ_IDEA)
                    .hasLintViolationWithoutAutoCorrect(5, 21, "Backing property is only allowed when the matching property or function is public")
            }

            @ParameterizedTest(name = "Modifier: {0}")
            @ValueSource(
                strings = [
                    "private",
                    "protected",
                    "internal",
                ],
            )
            fun `Given android_studio code style, and the correlated function is non-public then do not emit`(modifier: String) {
                val code =
                    """
                    class Foo {
                        $modifier fun getElementList(): List<Element> = _elementList

                        companion object {
                            private val _elementList = mutableListOf<Element>()
                        }
                    }
                    """.trimIndent()
                backingPropertyNamingRuleAssertThat(code)
                    .withEditorConfigOverride(CODE_STYLE_PROPERTY to ANDROID_STUDIO)
                    .hasNoLintViolations()
            }

            @Test
            fun `Given that the correlated function has at least 1 parameter then emit`() {
                val code =
                    """
                    class Foo {
                        fun getElementList(bar: String): List<Element> = _elementList + bar

                        companion object {
                            private val _elementList = mutableListOf<Element>()
                        }
                    }
                    """.trimIndent()
                @Suppress("ktlint:standard:max-line-length")
                backingPropertyNamingRuleAssertThat(code)
                    .hasLintViolationWithoutAutoCorrect(5, 21, "Backing property is only allowed when a matching property or function exists")
            }
        }
    }

    @Nested
    inner class `Given a backing property defined in a private companion object` {
        @Nested
        inner class `Given a correlated property` {
            @ParameterizedTest(name = "Correlated property name: {0}")
            @ValueSource(
                strings = [
                    "foo",
                    "føø",
                ],
            )
            fun `Given the correlated property is defined as class member then do not emit`(propertyName: String) {
                val code =
                    """
                    class Foo {
                        val $propertyName: String
                            get() = _$propertyName

                        private companion object {
                            var _$propertyName = "some-value"
                        }
                    }
                    """.trimIndent()
                backingPropertyNamingRuleAssertThat(code).hasNoLintViolations()
            }

            @Test
            fun `Given that the correlated property is implicitly public then do not emit`() {
                val code =
                    """
                    class Foo {
                        val elementList: List<Element>
                            get() = _elementList

                        private companion object {
                            val _elementList = mutableListOf<Element>()
                        }
                    }
                    """.trimIndent()
                backingPropertyNamingRuleAssertThat(code).hasNoLintViolations()
            }

            @Test
            fun `Given that the correlated property is explicitly public then do not emit`() {
                val code =
                    """
                    class Foo {
                        public val elementList: List<Element>
                            get() = _elementList

                        private companion object {
                            val _elementList = mutableListOf<Element>()
                        }
                    }
                    """.trimIndent()
                backingPropertyNamingRuleAssertThat(code).hasNoLintViolations()
            }

            @ParameterizedTest(name = "Modifier: {0}")
            @ValueSource(
                strings = [
                    "private",
                    "protected",
                    "internal",
                ],
            )
            fun `Given ktlint_official code style, and the correlated property is non-public then emit`(modifier: String) {
                val code =
                    """
                    class Foo {
                        $modifier val elementList: List<Element>
                            get() = _elementList

                        private companion object {
                            val _elementList = mutableListOf<Element>()
                        }
                    }
                    """.trimIndent()
                @Suppress("ktlint:standard:max-line-length")
                backingPropertyNamingRuleAssertThat(code)
                    .withEditorConfigOverride(CODE_STYLE_PROPERTY to KTLINT_OFFICIAL)
                    .hasLintViolationWithoutAutoCorrect(6, 13, "Backing property is only allowed when the matching property or function is public")
            }

            @ParameterizedTest(name = "Modifier: {0}")
            @ValueSource(
                strings = [
                    "private",
                    "protected",
                    "internal",
                ],
            )
            fun `Given intellij_idea code style, and the correlated property is non-public then emit`(modifier: String) {
                val code =
                    """
                    class Foo {
                        $modifier val elementList: List<Element>
                            get() = _elementList

                        private companion object {
                            val _elementList = mutableListOf<Element>()
                        }
                    }
                    """.trimIndent()
                @Suppress("ktlint:standard:max-line-length")
                backingPropertyNamingRuleAssertThat(code)
                    .withEditorConfigOverride(CODE_STYLE_PROPERTY to INTELLIJ_IDEA)
                    .hasLintViolationWithoutAutoCorrect(6, 13, "Backing property is only allowed when the matching property or function is public")
            }

            @ParameterizedTest(name = "Modifier: {0}")
            @ValueSource(
                strings = [
                    "private",
                    "protected",
                    "internal",
                ],
            )
            fun `Given android_studio code style, and the correlated property is non-public then do not emit`(modifier: String) {
                val code =
                    """
                    class Foo {
                        $modifier val elementList: List<Element>
                            get() = _elementList

                        private companion object {
                            val _elementList = mutableListOf<Element>()
                        }
                    }
                    """.trimIndent()
                backingPropertyNamingRuleAssertThat(code)
                    .withEditorConfigOverride(CODE_STYLE_PROPERTY to ANDROID_STUDIO)
                    .hasNoLintViolations()
            }
        }

        @Nested
        inner class `Given a co=related function` {
            @ParameterizedTest(name = "Correlated property name: {0}")
            @CsvSource(
                value = [
                    "foo,getFoo",
                    "føø,getFøø",
                ],
            )
            fun `Given a valid backing property then do not emit`(
                propertyName: String,
                functionName: String,
            ) {
                val code =
                    """
                    class Foo {
                        fun $functionName(): String = _$propertyName

                        private companion object {
                            var _$propertyName = "some-value"
                        }
                    }
                    """.trimIndent()
                backingPropertyNamingRuleAssertThat(code).hasNoLintViolations()
            }

            @Test
            fun `Given that the correlated function is implicitly public then do not emit`() {
                val code =
                    """
                    class Foo {
                        fun getElementList(): List<Element> = _elementList

                        private companion object {
                            val _elementList = mutableListOf<Element>()
                        }
                    }
                    """.trimIndent()
                backingPropertyNamingRuleAssertThat(code).hasNoLintViolations()
            }

            @Test
            fun `Given that the correlated function is explicitly public then do not emit`() {
                val code =
                    """
                    class Foo {
                        public fun getElementList(): List<Element> = _elementList

                        private companion object {
                            val _elementList = mutableListOf<Element>()
                        }
                    }
                    """.trimIndent()
                backingPropertyNamingRuleAssertThat(code).hasNoLintViolations()
            }

            @ParameterizedTest(name = "Modifier: {0}")
            @ValueSource(
                strings = [
                    "private",
                    "protected",
                    "internal",
                ],
            )
            fun `Given ktlint_official code style, and the correlated function is non-public then emit`(modifier: String) {
                val code =
                    """
                    class Foo {
                        $modifier fun getElementList(): List<Element> = _elementList

                        private companion object {
                            val _elementList = mutableListOf<Element>()
                        }
                    }
                    """.trimIndent()
                @Suppress("ktlint:standard:max-line-length")
                backingPropertyNamingRuleAssertThat(code)
                    .withEditorConfigOverride(CODE_STYLE_PROPERTY to KTLINT_OFFICIAL)
                    .hasLintViolationWithoutAutoCorrect(5, 13, "Backing property is only allowed when the matching property or function is public")
            }

            @ParameterizedTest(name = "Modifier: {0}")
            @ValueSource(
                strings = [
                    "private",
                    "protected",
                    "internal",
                ],
            )
            fun `Given intellij_idea code style, and the correlated function is non-public then emit`(modifier: String) {
                val code =
                    """
                    class Foo {
                        $modifier fun getElementList(): List<Element> = _elementList

                        private companion object {
                            val _elementList = mutableListOf<Element>()
                        }
                    }
                    """.trimIndent()
                @Suppress("ktlint:standard:max-line-length")
                backingPropertyNamingRuleAssertThat(code)
                    .withEditorConfigOverride(CODE_STYLE_PROPERTY to INTELLIJ_IDEA)
                    .hasLintViolationWithoutAutoCorrect(5, 13, "Backing property is only allowed when the matching property or function is public")
            }

            @ParameterizedTest(name = "Modifier: {0}")
            @ValueSource(
                strings = [
                    "private",
                    "protected",
                    "internal",
                ],
            )
            fun `Given android_studio code style, and the correlated function is non-public then do not emit`(modifier: String) {
                val code =
                    """
                    class Foo {
                        $modifier fun getElementList(): List<Element> = _elementList

                        private companion object {
                            val _elementList = mutableListOf<Element>()
                        }
                    }
                    """.trimIndent()
                backingPropertyNamingRuleAssertThat(code)
                    .withEditorConfigOverride(CODE_STYLE_PROPERTY to ANDROID_STUDIO)
                    .hasNoLintViolations()
            }

            @Test
            fun `Given that the correlated function has at least 1 parameter then emit`() {
                val code =
                    """
                    class Foo {
                        fun getElementList(bar: String): List<Element> = _elementList + bar

                        private companion object {
                            val _elementList = mutableListOf<Element>()
                        }
                    }
                    """.trimIndent()
                @Suppress("ktlint:standard:max-line-length")
                backingPropertyNamingRuleAssertThat(code)
                    .hasLintViolationWithoutAutoCorrect(5, 13, "Backing property is only allowed when a matching property or function exists")
            }
        }
    }

    @ParameterizedTest(name = "Suppression annotation: {0}")
    @ValueSource(
        strings = [
            "ktlint:standard:backing-property-naming",
            "PropertyName", // IntelliJ IDEA suppression
            "ObjectPropertyName", // IntelliJ IDEA suppression
        ],
    )
    fun `Given a property with a disallowed name which is suppressed`(suppressionName: String) {
        val code =
            """
            @Suppress("$suppressionName")
            val _foo = Foo()
            """.trimIndent()
        backingPropertyNamingRuleAssertThat(code).hasNoLintViolations()
    }

    @KtlintDocumentationTest
    fun `Ktlint allowed examples`() {
        val code =
            """
            class Bar {
                // Backing property
                private val _elementList = mutableListOf<Element>()
                val elementList: List<Element>
                    get() = _elementList

                // Backing property defined in companion object
                val elementList2: List<Element>
                    get() = _elementList2

                companion object {
                    private val _elementList2 = mutableListOf<Element>()
                }
            }
            """.trimIndent()
        backingPropertyNamingRuleAssertThat(code).hasNoLintViolations()
    }

    @KtlintDocumentationTest
    fun `Ktlint disallowed examples`() {
        val code =
            """
            class Bar1 {
                // Incomplete backing property as public property 'elementList' or function `getElementList` is missing
                private val _elementList = mutableListOf<Element>()
            }
            class Bar2 {
                // Invalid backing property as '_elementList' is not a private property
                val _elementList = mutableListOf<Element>()
                val elementList: List<Element>
                    get() = _elementList2
            }
            class Bar3 {
                // Invalid backing property as 'elementList' is not a public property
                // Note: code below is allowed in `android_studio` code style!
                private val _elementList = mutableListOf<Element>()
                internal val elementList: List<Element>
                    get() = _elementList2
            }
            """.trimIndent()
        @Suppress("ktlint:standard:max-line-length")
        backingPropertyNamingRuleAssertThat(code)
            .hasLintViolations(
                LintViolation(3, 17, "Backing property is only allowed when a matching property or function exists", canBeAutoCorrected = false),
                LintViolation(7, 9, "Backing property not allowed when 'private' modifier is missing", canBeAutoCorrected = false),
                LintViolation(14, 17, "Backing property is only allowed when the matching property or function is public", canBeAutoCorrected = false),
            )
    }

    @Test
    fun `Given a property name suppressed via 'PropertyName' then also suppress the ktlint violation`() {
        val code =
            """
            class Foo {
                @Suppress("PropertyName")
                var FOO = "foo"
            }
            """.trimIndent()
        backingPropertyNamingRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `Issue 2748 - Given an override property with name starting with '_' then do not report a violation`() {
        val code =
            """
            // The property "__foo" in example below can be defined in an external dependency, which can not be changed. Even in case it is
            // a (internal) dependency that can be changed, the violation should only be reported at the base property, but on the overrides
            val fooBar =
                object : FooBar {
                    override val __foo = "foo"
                }
            """.trimIndent()
        backingPropertyNamingRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `Issue 2779 - Given a property name suppressed via 'LocalVariableName' then also suppress the ktlint violation`() {
        val code =
            """
            @Suppress("LocalVariableName")
            fun test() {
                val _test = "test"
                println(_test)
            }
            """.trimIndent()
        backingPropertyNamingRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `Issue 3166 - Allow local properties with name '_'`() {
        val code =
            """
            // https://github.com/Kotlin/KEEP/blob/main/proposals/KEEP-0412-underscores-for-local-variables.md
            fun underscore() {
                val _ = functionWhichReturnsValueWhichIsNotRelevantOnThisCallSite()
            }
            """.trimIndent()
        backingPropertyNamingRuleAssertThat(code).hasNoLintViolations()
    }
}
