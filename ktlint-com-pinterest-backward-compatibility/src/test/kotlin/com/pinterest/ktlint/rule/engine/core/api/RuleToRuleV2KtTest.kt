@file:Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")

package com.pinterest.ktlint.rule.engine.core.api

import com.pinterest.ktlint.rule.engine.core.api.AutocorrectDecision.ALLOW_AUTOCORRECT
import com.pinterest.ktlint.rule.engine.core.api.AutocorrectDecision.NO_AUTOCORRECT
import com.pinterest.ktlint.rule.engine.core.api.editorconfig.EditorConfig
import com.pinterest.ktlint.rule.engine.core.api.editorconfig.EditorConfigProperty
import io.github.ktlint.core.rule.engine.core.api.KtlintKotlinCompiler
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.ec4j.core.model.PropertyType
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import com.pinterest.ktlint.rule.engine.core.api.editorconfig.CODE_STYLE_PROPERTY as CODE_STYLE_PROPERTY_COM_PINTEREST_KTLINT
import com.pinterest.ktlint.rule.engine.core.api.editorconfig.CodeStyleValue as CodeStyleValueKtlintComPinterestKtlint
import com.pinterest.ktlint.rule.engine.core.api.editorconfig.RULE_EXECUTION_PROPERTY_TYPE as RuleExecutionPropertyTypeKtlint1x
import com.pinterest.ktlint.rule.engine.core.api.editorconfig.RuleExecution as RuleExecutionKtlintComPinterestKtlint
import io.github.ktlint.core.rule.engine.core.api.AutocorrectDecision as AutocorrectDecisionIoGithubKtlint
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
            val ruleV2 = legacyRule(ruleId = "custom:my-rule").toRuleV2()

            assertThat(ruleV2.ruleId.value).isEqualTo("custom:my-rule")
        }

        @Test
        fun `then toRuleV2 preserves the about metadata`() {
            val ruleV2 =
                legacyRule(
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
            val ruleV2 = legacyRule(usesEditorConfigProperties = setOf(property)).toRuleV2()

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
            val ruleV2 = legacyRule(onBeforeFirstNode = { delegated = true }).toRuleV2()

            ruleV2.beforeFirstNode(EditorConfigIoGithubKtlint())

            assertThat(delegated).isTrue
        }

        @Test
        fun `then afterLastNode delegates to the original rule`() {
            var delegated = false
            val ruleV2 = legacyRule(onAfterLastNode = { delegated = true }).toRuleV2()

            ruleV2.afterLastNode()

            assertThat(delegated).isTrue
        }

        @Test
        fun `then beforeVisitChildNodes delegates and converts ALLOW_AUTOCORRECT`() {
            var capturedDecision: AutocorrectDecision? = null
            val ruleV2 =
                legacyRule(
                    onBeforeVisitChildNodes = { _, emit -> capturedDecision = emit(0, "error", true) },
                ).toRuleV2()

            ruleV2.beforeVisitChildNodes(fakeNode()) { _, _, _ -> AutocorrectDecisionIoGithubKtlint.ALLOW_AUTOCORRECT }

            assertThat(capturedDecision).isEqualTo(ALLOW_AUTOCORRECT)
        }

        @Test
        fun `then beforeVisitChildNodes delegates and converts NO_AUTOCORRECT`() {
            var capturedDecision: AutocorrectDecision? = null
            val ruleV2 =
                legacyRule(
                    onBeforeVisitChildNodes = { _, emit -> capturedDecision = emit(0, "error", true) },
                ).toRuleV2()

            ruleV2.beforeVisitChildNodes(fakeNode()) { _, _, _ -> AutocorrectDecisionIoGithubKtlint.NO_AUTOCORRECT }

            assertThat(capturedDecision).isEqualTo(NO_AUTOCORRECT)
        }

        @Nested
        inner class `Given usesEditorConfigProperties containing a com-pinterest CodeStyleValue property` {
            @Test
            fun `then toRuleV2 maps PropertyType to io-github equivalent`() {
                val mapped =
                    legacyRule(usesEditorConfigProperties = setOf(CODE_STYLE_PROPERTY_COM_PINTEREST_KTLINT))
                        .toRuleV2()
                        .usesEditorConfigProperties
                        .single()

                assertThat(mapped.type).isSameAs(CODE_STYLE_PROPERTY_IO_GITHUB_KTLINT.type)
            }

            @Test
            fun `then toRuleV2 maps default values to io-github CodeStyleValue`() {
                val mapped =
                    legacyRule(usesEditorConfigProperties = setOf(CODE_STYLE_PROPERTY_COM_PINTEREST_KTLINT))
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
                    legacyRule(usesEditorConfigProperties = setOf(CODE_STYLE_PROPERTY_COM_PINTEREST_KTLINT))
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
                    legacyRule(usesEditorConfigProperties = setOf(ruleExecutionPropertyComPinterestKtlint))
                        .toRuleV2()
                        .usesEditorConfigProperties
                        .single()

                assertThat(mapped.type).isSameAs(RuleExecutionPropertyTypeKtlint2x)
            }

            @Test
            fun `then toRuleV2 maps default values to io-github RuleExecution`() {
                val mapped =
                    legacyRule(usesEditorConfigProperties = setOf(ruleExecutionPropertyComPinterestKtlint))
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
                legacyRule(usesEditorConfigProperties = setOf(propertyWithStringMapper)).toRuleV2().usesEditorConfigProperties.single()

            val result = mapped.propertyMapper?.invoke(null, CodeStyleValueIoGithubKtlint.KTLINT_OFFICIAL)

            assertThat(result).isEqualTo("remapped-value")
        }

        @Test
        fun `then afterVisitChildNodes delegates to the original rule`() {
            var delegated = false
            val ruleV2 =
                legacyRule(
                    onAfterVisitChildNodes = { _, _ -> delegated = true },
                ).toRuleV2()

            ruleV2.afterVisitChildNodes(fakeNode()) { _, _, _ -> AutocorrectDecisionIoGithubKtlint.NO_AUTOCORRECT }

            assertThat(delegated).isTrue
        }
    }

    private fun legacyRule(
        ruleId: String = "custom:legacy-rule",
        maintainer: String = "Test Maintainer",
        repositoryUrl: String = "https://repo.example.com",
        issueTrackerUrl: String = "https://issues.example.com",
        usesEditorConfigProperties: Set<EditorConfigProperty<*>> = emptySet(),
        onBeforeFirstNode: (EditorConfig) -> Unit = {},
        onAfterLastNode: () -> Unit = {},
        onBeforeVisitChildNodes: (ASTNode, (Int, String, Boolean) -> AutocorrectDecision) -> Unit = { _, _ -> },
        onAfterVisitChildNodes: (ASTNode, (Int, String, Boolean) -> AutocorrectDecision) -> Unit = { _, _ -> },
    ): Rule =
        object :
            Rule(
                ruleId = RuleId(ruleId),
                about =
                    About(
                        maintainer = maintainer,
                        repositoryUrl = repositoryUrl,
                        issueTrackerUrl = issueTrackerUrl,
                    ),
                usesEditorConfigProperties = usesEditorConfigProperties,
            ),
            RuleAutocorrectApproveHandler {
            override fun beforeFirstNode(editorConfig: EditorConfig) = onBeforeFirstNode(editorConfig)

            override fun afterLastNode() = onAfterLastNode()

            override fun beforeVisitChildNodes(
                node: ASTNode,
                emit: (Int, String, Boolean) -> AutocorrectDecision,
            ) = onBeforeVisitChildNodes(node, emit)

            override fun afterVisitChildNodes(
                node: ASTNode,
                emit: (Int, String, Boolean) -> AutocorrectDecision,
            ) = onAfterVisitChildNodes(node, emit)
        }

    private fun fakeNode(): ASTNode = KtlintKotlinCompiler.createPsiFileFromText("Fake.kt", "val x = 1").node

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
