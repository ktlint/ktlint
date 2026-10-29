@file:Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")

package com.pinterest.ktlint.rule.engine.core.api

import com.intellij.lang.ASTNode
import com.pinterest.ktlint.rule.engine.core.api.editorconfig.EditorConfig
import com.pinterest.ktlint.rule.engine.core.api.editorconfig.EditorConfigProperty
import io.github.ktlint.core.rule.engine.api.Code
import io.github.ktlint.core.rule.engine.api.KtLintRuleEngine
import io.github.ktlint.core.rule.engine.core.api.ElementType.EOL_COMMENT
import io.github.ktlint.core.rule.engine.core.api.replaceTextWith
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.ec4j.core.model.PropertyType
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import com.pinterest.ktlint.rule.engine.core.api.editorconfig.CODE_STYLE_PROPERTY as CODE_STYLE_PROPERTY_COM_PINTEREST_KTLINT
import com.pinterest.ktlint.rule.engine.core.api.editorconfig.CodeStyleValue as CodeStyleValueKtlintComPinterestKtlint
import com.pinterest.ktlint.rule.engine.core.api.editorconfig.RULE_EXECUTION_PROPERTY_TYPE as RuleExecutionPropertyTypeKtlint1x
import com.pinterest.ktlint.rule.engine.core.api.editorconfig.RuleExecution as RuleExecutionKtlintComPinterestKtlint
import io.github.ktlint.core.rule.engine.core.api.editorconfig.CODE_STYLE_PROPERTY as CODE_STYLE_PROPERTY_IO_GITHUB_KTLINT
import io.github.ktlint.core.rule.engine.core.api.editorconfig.CodeStyleValue as CodeStyleValueIoGithubKtlint
import io.github.ktlint.core.rule.engine.core.api.editorconfig.EditorConfig as EditorConfigIoGithubKtlint
import io.github.ktlint.core.rule.engine.core.api.editorconfig.RULE_EXECUTION_PROPERTY_TYPE as RuleExecutionPropertyTypeKtlint2x
import io.github.ktlint.core.rule.engine.core.api.editorconfig.RuleExecution as RuleExecutionIoGithubKtlint

@Suppress("DEPRECATION")
class RuleToRuleV2KtTest {
    @Test
    fun `Given a rule without RuleAutocorrectApproveHandler then toRuleV2 throws IllegalArgumentException`() {
        val rule =
            object : Rule(
                ruleId = RuleId("custom:legacy-rule"),
                about = About(),
            ) {}

        assertThatThrownBy { rule.toRuleV2() }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("RuleAutocorrectApproveHandler")
    }

    @Nested
    inner class `Given a rule implementing RuleAutocorrectApproveHandler` {
        @Test
        fun `then toRuleV2 preserves the ruleId`() {
            val ruleV2 = LegacyRule(ruleId = "custom:my-rule").toRuleV2()

            assertThat(ruleV2.ruleId.value).isEqualTo("custom:my-rule")
        }

        @Test
        fun `then toRuleV2 preserves the about metadata`() {
            val ruleV2 =
                LegacyRule(
                    maintainer = "Some Maintainer",
                    repositoryUrl = "https://some-repo.example.com",
                    issueTrackerUrl = "https://some-issues.example.com",
                ).toRuleV2()

            assertThat(ruleV2.about.maintainer).isEqualTo("Some Maintainer")
            assertThat(ruleV2.about.repositoryUrl).isEqualTo("https://some-repo.example.com")
            assertThat(ruleV2.about.issueTrackerUrl).isEqualTo("https://some-issues.example.com")
        }

        @Test
        fun `then toRuleV2 maps usesEditorConfigProperties to io-github equivalents`() {
            val property = sampleEditorConfigProperty("some-property")
            val ruleV2 = LegacyRule(usesEditorConfigProperties = setOf(property)).toRuleV2()

            val mapped = ruleV2.usesEditorConfigProperties.single()
            assertThat(mapped.name).isEqualTo("some-property")
            assertThat(mapped.defaultValue).isEqualTo("default")
            assertThat(mapped.ktlintOfficialCodeStyleDefaultValue).isEqualTo("ktlint-official")
            assertThat(mapped.intellijIdeaCodeStyleDefaultValue).isEqualTo("intellij-idea")
            assertThat(mapped.androidStudioCodeStyleDefaultValue).isEqualTo("android-studio")
            assertThat(mapped.deprecationWarning).isEqualTo("some-deprecation-warning")
            assertThat(mapped.deprecationError).isEqualTo("some-deprecation-error")
        }

        @Test
        fun `then beforeFirstNode delegates to the original rule`() {
            var delegated = false
            val legacyRule =
                object : LegacyRule() {
                    override fun beforeFirstNode(editorConfig: EditorConfig) {
                        delegated = true
                    }
                }
            val ruleV2 = legacyRule.toRuleV2()

            ruleV2.beforeFirstNode(EditorConfigIoGithubKtlint())

            assertThat(delegated).isTrue
        }

        @Test
        fun `then afterLastNode delegates to the original rule`() {
            var delegated = false
            val legacyRule =
                object : LegacyRule() {
                    override fun afterLastNode() {
                        delegated = true
                    }
                }
            val ruleV2 = legacyRule.toRuleV2()

            ruleV2.afterLastNode()

            assertThat(delegated).isTrue
        }

        @Test
        fun `then beforeVisitChildNodes delegates emitting the violation and if applicable the autocorrect`() {
            val code =
                """
                fun bar() {
                    // foo
                    // bar
                    }
                """.trimIndent()
            val formattedCode =
                """
                fun bar() {
                    // foo
                    // BAR
                    }
                """.trimIndent()
            val legacyRule =
                object : LegacyRule() {
                    override fun beforeVisitChildNodes(
                        node: ASTNode,
                        emit: (Int, String, Boolean) -> AutocorrectDecision,
                    ) {
                        if (node.elementType == EOL_COMMENT) {
                            emit(node.startOffset, "Node: ${node.text}", true)
                                .ifAutocorrectAllowed { node.replaceTextWith(node.text.uppercase()) }
                        }
                    }
                }
            val actual =
                KtLintRuleEngine(
                    ruleProviders = setOf(RuleProvider { legacyRule }.toRuleV2Provider()),
                ).format(
                    Code.fromSnippet(code),
                    callback = { lintError ->
                        // Emulate behavior of the API consumer (for example Ktlint IntelliJ Plugin in manual mode where the user indicates
                        // that only the "bar" comment is to be fixed.
                        if (lintError.detail == "Node: // bar") {
                            io.github.ktlint.core.rule.engine.core.api.AutocorrectDecision.ALLOW_AUTOCORRECT
                        } else {
                            io.github.ktlint.core.rule.engine.core.api.AutocorrectDecision.NO_AUTOCORRECT
                        }
                    },
                )
            assertThat(actual).isEqualTo(formattedCode)
        }

        @Test
        fun `then afterVisitChildNodes delegates emitting the violation and if applicable the autocorrect`() {
            val code =
                """
                fun bar() {
                    // foo
                    // bar
                    }
                """.trimIndent()
            val formattedCode =
                """
                fun bar() {
                    // foo
                    // BAR
                    }
                """.trimIndent()
            val legacyRule =
                object : LegacyRule() {
                    override fun afterVisitChildNodes(
                        node: ASTNode,
                        emit: (Int, String, Boolean) -> AutocorrectDecision,
                    ) {
                        if (node.elementType == EOL_COMMENT) {
                            emit(node.startOffset, "Node: ${node.text}", true)
                                .ifAutocorrectAllowed { node.replaceTextWith(node.text.uppercase()) }
                        }
                    }
                }
            val actual =
                KtLintRuleEngine(
                    ruleProviders = setOf(RuleProvider { legacyRule }.toRuleV2Provider()),
                ).format(
                    Code.fromSnippet(code),
                    callback = { lintError ->
                        // Emulate behavior of the API consumer (for example Ktlint IntelliJ Plugin in manual mode where the user indicates
                        // that only the "bar" comment is to be fixed.
                        if (lintError.detail == "Node: // bar") {
                            io.github.ktlint.core.rule.engine.core.api.AutocorrectDecision.ALLOW_AUTOCORRECT
                        } else {
                            io.github.ktlint.core.rule.engine.core.api.AutocorrectDecision.NO_AUTOCORRECT
                        }
                    },
                )
            assertThat(actual).isEqualTo(formattedCode)
        }

        @Nested
        inner class `Given usesEditorConfigProperties containing a com-pinterest CodeStyleValue property` {
            @Test
            fun `then toRuleV2 maps PropertyType to io-github equivalent`() {
                val mapped =
                    LegacyRule(usesEditorConfigProperties = setOf(CODE_STYLE_PROPERTY_COM_PINTEREST_KTLINT))
                        .toRuleV2()
                        .usesEditorConfigProperties
                        .single()

                assertThat(mapped.type).isSameAs(CODE_STYLE_PROPERTY_IO_GITHUB_KTLINT.type)
            }

            @Test
            fun `then toRuleV2 maps default values to io-github CodeStyleValue`() {
                val mapped =
                    LegacyRule(usesEditorConfigProperties = setOf(CODE_STYLE_PROPERTY_COM_PINTEREST_KTLINT))
                        .toRuleV2()
                        .usesEditorConfigProperties
                        .single()

                assertThat(mapped.defaultValue).isEqualTo(CODE_STYLE_PROPERTY_IO_GITHUB_KTLINT.defaultValue)
                assertThat(mapped.ktlintOfficialCodeStyleDefaultValue)
                    .isEqualTo(CODE_STYLE_PROPERTY_IO_GITHUB_KTLINT.ktlintOfficialCodeStyleDefaultValue)
                assertThat(mapped.intellijIdeaCodeStyleDefaultValue)
                    .isEqualTo(CODE_STYLE_PROPERTY_IO_GITHUB_KTLINT.intellijIdeaCodeStyleDefaultValue)
                assertThat(mapped.androidStudioCodeStyleDefaultValue)
                    .isEqualTo(CODE_STYLE_PROPERTY_IO_GITHUB_KTLINT.androidStudioCodeStyleDefaultValue)
            }

            @Test
            fun `then toRuleV2 wraps propertyMapper so that returned com-pinterest CodeStyleValue is converted to io-github`() {
                val codeStylePropertyIoGithubKtlint =
                    // Note: the rule defined is a Ktlint 1.x compatible rule using the Ktlint 1.x  code style property
                    LegacyRule(usesEditorConfigProperties = setOf(CODE_STYLE_PROPERTY_COM_PINTEREST_KTLINT))
                        .toRuleV2()
                        .usesEditorConfigProperties
                        .single()

                val result = codeStylePropertyIoGithubKtlint.type.parse(CodeStyleValueKtlintComPinterestKtlint.intellij_idea.name).parsed

                assertThat(result).isEqualTo(CodeStyleValueIoGithubKtlint.INTELLIJ_IDEA)
            }
        }

        @Nested
        inner class `Given usesEditorConfigProperties containing a com-pinterest RuleExecution property` {
            private val ruleExecutionPropertyComPinterestKtlint =
                EditorConfigProperty(
                    type = RuleExecutionPropertyTypeKtlint1x,
                    name = "ktlint_some_rule",
                    defaultValue = RuleExecutionKtlintComPinterestKtlint.enabled,
                    ktlintOfficialCodeStyleDefaultValue = RuleExecutionKtlintComPinterestKtlint.enabled,
                    intellijIdeaCodeStyleDefaultValue = RuleExecutionKtlintComPinterestKtlint.disabled,
                    androidStudioCodeStyleDefaultValue = RuleExecutionKtlintComPinterestKtlint.disabled,
                )

            @Test
            fun `then toRuleV2 maps PropertyType to io-github equivalent`() {
                val mapped =
                    LegacyRule(usesEditorConfigProperties = setOf(ruleExecutionPropertyComPinterestKtlint))
                        .toRuleV2()
                        .usesEditorConfigProperties
                        .single()

                assertThat(mapped.type).isSameAs(RuleExecutionPropertyTypeKtlint2x)
            }

            @Test
            fun `then toRuleV2 maps default values to io-github RuleExecution`() {
                val mapped =
                    LegacyRule(usesEditorConfigProperties = setOf(ruleExecutionPropertyComPinterestKtlint))
                        .toRuleV2()
                        .usesEditorConfigProperties
                        .single()

                assertThat(mapped.defaultValue).isEqualTo(RuleExecutionIoGithubKtlint.ENABLED)
                assertThat(mapped.ktlintOfficialCodeStyleDefaultValue).isEqualTo(RuleExecutionIoGithubKtlint.ENABLED)
                assertThat(mapped.intellijIdeaCodeStyleDefaultValue).isEqualTo(RuleExecutionIoGithubKtlint.DISABLED)
                assertThat(mapped.androidStudioCodeStyleDefaultValue).isEqualTo(RuleExecutionIoGithubKtlint.DISABLED)
            }
        }

        @Test
        fun `then toRuleV2 wraps propertyMapper returning a non-mapped type without conversion`() {
            val propertyWithStringMapper =
                EditorConfigProperty(
                    name = "some-string-property",
                    type = PropertyType("some-string-property", "", PropertyType.PropertyValueParser.IDENTITY_VALUE_PARSER, emptySet()),
                    defaultValue = "default",
                    propertyMapper = { _, _ -> "remapped-value" },
                )
            val mapped =
                LegacyRule(usesEditorConfigProperties = setOf(propertyWithStringMapper)).toRuleV2().usesEditorConfigProperties.single()

            val result = mapped.propertyMapper?.invoke(null, CodeStyleValueIoGithubKtlint.KTLINT_OFFICIAL)

            assertThat(result).isEqualTo("remapped-value")
        }
    }

    private open class LegacyRule(
        ruleId: String = "custom:legacy-rule",
        maintainer: String = "Test Maintainer",
        repositoryUrl: String = "https://repo.example.com",
        issueTrackerUrl: String = "https://issues.example.com",
        usesEditorConfigProperties: Set<EditorConfigProperty<*>> = emptySet(),
    ) : Rule(
            ruleId = RuleId(ruleId),
            about =
                About(
                    maintainer = maintainer,
                    repositoryUrl = repositoryUrl,
                    issueTrackerUrl = issueTrackerUrl,
                ),
            usesEditorConfigProperties = usesEditorConfigProperties,
        ),
        RuleAutocorrectApproveHandler

    @Suppress("SameParameterValue")
    private fun sampleEditorConfigProperty(name: String): EditorConfigProperty<String> =
        EditorConfigProperty(
            name = name,
            type = PropertyType(name, "", PropertyType.PropertyValueParser.IDENTITY_VALUE_PARSER, emptySet()),
            defaultValue = "default",
            ktlintOfficialCodeStyleDefaultValue = "ktlint-official",
            intellijIdeaCodeStyleDefaultValue = "intellij-idea",
            androidStudioCodeStyleDefaultValue = "android-studio",
            deprecationWarning = "some-deprecation-warning",
            deprecationError = "some-deprecation-error",
        )
}
