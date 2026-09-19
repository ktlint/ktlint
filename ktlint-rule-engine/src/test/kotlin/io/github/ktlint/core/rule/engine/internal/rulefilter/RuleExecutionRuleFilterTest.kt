package io.github.ktlint.core.rule.engine.internal.rulefilter

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import io.github.ktlint.core.rule.engine.core.api.RuleId
import io.github.ktlint.core.rule.engine.core.api.RuleSetId
import io.github.ktlint.core.rule.engine.core.api.RuleV2
import io.github.ktlint.core.rule.engine.core.api.RuleV2Provider
import io.github.ktlint.core.rule.engine.core.api.editorconfig.ALL_RULES_EXECUTION_PROPERTY
import io.github.ktlint.core.rule.engine.core.api.editorconfig.CODE_STYLE_PROPERTY
import io.github.ktlint.core.rule.engine.core.api.editorconfig.EditorConfig
import io.github.ktlint.core.rule.engine.core.api.editorconfig.RULE_EXECUTION_PROPERTY_TYPE
import io.github.ktlint.core.rule.engine.core.api.editorconfig.RuleExecution
import io.github.ktlint.core.rule.engine.core.api.editorconfig.createRuleExecutionEditorConfigProperty
import io.github.ktlint.core.rule.engine.internal.rules.KTLINT_SUPPRESSION_RULE_ID
import io.github.ktlint.core.rule.engine.internal.rules.KtlintSuppressionRule
import org.ec4j.core.model.Property
import org.junit.jupiter.api.Test

class RuleExecutionRuleFilterTest {
    @Test
    fun `Given that standard rule set is enabled explicitly then run all standard rules except those that are disabled explicitly`() {
        val actual =
            runWithRuleExecutionRuleFilter(
                RuleV2Provider { NormalRule(STANDARD_RULE_A) },
                RuleV2Provider { NormalRule(STANDARD_RULE_B) },
                RuleV2Provider { NormalRule(STANDARD_RULE_C) },
                editorConfig =
                    EditorConfig(
                        ktLintRuleExecutionEditorConfigProperty("ktlint_$STANDARD", RuleExecution.enabled),
                        ktLintRuleExecutionEditorConfigProperty(STANDARD_RULE_C, RuleExecution.disabled),
                    ),
            ).toRuleIds()

        assertThat(actual).containsExactly(
            STANDARD_RULE_A,
            STANDARD_RULE_B,
        )
    }

    @Test
    fun `Given that standard rule set is not enabled explicitly then run all standard rules except experimental and explicitly disabled rules`() {
        val actual =
            runWithRuleExecutionRuleFilter(
                RuleV2Provider { NormalRule(STANDARD_RULE_A) },
                RuleV2Provider { ExperimentalRule(STANDARD_RULE_B) },
                RuleV2Provider { NormalRule(STANDARD_RULE_C) },
                RuleV2Provider { NormalRule(STANDARD_RULE_D) },
                editorConfig =
                    EditorConfig(
                        ktLintRuleExecutionEditorConfigProperty(STANDARD_RULE_C, RuleExecution.disabled),
                    ),
            ).toRuleIds()

        assertThat(actual).containsExactly(
            STANDARD_RULE_A,
            STANDARD_RULE_D,
        )
    }

    @Test
    fun `Given that standard rule set is disabled explicitly then only run standard rules that are enabled explicitly`() {
        val actual =
            runWithRuleExecutionRuleFilter(
                RuleV2Provider { NormalRule(STANDARD_RULE_A) },
                RuleV2Provider { NormalRule(STANDARD_RULE_B) },
                RuleV2Provider { NormalRule(STANDARD_RULE_C) },
                editorConfig =
                    EditorConfig(
                        ktLintRuleExecutionEditorConfigProperty("ktlint_$STANDARD", RuleExecution.disabled),
                        ktLintRuleExecutionEditorConfigProperty(STANDARD_RULE_A, RuleExecution.enabled),
                        ktLintRuleExecutionEditorConfigProperty(STANDARD_RULE_B, RuleExecution.enabled),
                    ),
            ).toRuleIds()

        assertThat(actual).containsExactly(
            STANDARD_RULE_A,
            STANDARD_RULE_B,
        )
    }

    @Test
    fun `Given that the experimental rules are not disabled explicitly then only run rules that are enabled explicitly`() {
        val actual =
            runWithRuleExecutionRuleFilter(
                RuleV2Provider { ExperimentalRule(STANDARD_RULE_B) },
                RuleV2Provider { ExperimentalRule(STANDARD_RULE_C) },
                RuleV2Provider { ExperimentalRule(CUSTOM_RULE_B) },
                RuleV2Provider { ExperimentalRule(CUSTOM_RULE_C) },
                editorConfig =
                    EditorConfig(
                        ktLintRuleExecutionEditorConfigProperty(STANDARD_RULE_B, RuleExecution.enabled),
                        ktLintRuleExecutionEditorConfigProperty(CUSTOM_RULE_B, RuleExecution.enabled),
                    ),
            ).toRuleIds()

        assertThat(actual).containsExactly(
            STANDARD_RULE_B,
            CUSTOM_RULE_B,
        )
    }

    @Test
    fun `Given that a experimental rules are disabled explicitly then only run rules that are enabled explicitly`() {
        val actual =
            runWithRuleExecutionRuleFilter(
                RuleV2Provider { ExperimentalRule(STANDARD_RULE_B) },
                RuleV2Provider { ExperimentalRule(STANDARD_RULE_C) },
                RuleV2Provider { ExperimentalRule(CUSTOM_RULE_B) },
                RuleV2Provider { ExperimentalRule(CUSTOM_RULE_C) },
                editorConfig =
                    EditorConfig(
                        ktLintRuleExecutionEditorConfigProperty("ktlint_experimental", RuleExecution.disabled),
                        ktLintRuleExecutionEditorConfigProperty(STANDARD_RULE_B, RuleExecution.enabled),
                        ktLintRuleExecutionEditorConfigProperty("ktlint_$CUSTOM", RuleExecution.disabled),
                        ktLintRuleExecutionEditorConfigProperty(CUSTOM_RULE_B, RuleExecution.enabled),
                    ),
            ).toRuleIds()

        assertThat(actual).containsExactly(
            STANDARD_RULE_B,
            CUSTOM_RULE_B,
        )
    }

    @Test
    fun `Given that the experimental rules are enabled then only run rules that are not disabled explicitly`() {
        val actual =
            runWithRuleExecutionRuleFilter(
                RuleV2Provider { ExperimentalRule(STANDARD_RULE_B) },
                RuleV2Provider { ExperimentalRule(STANDARD_RULE_C) },
                RuleV2Provider { ExperimentalRule(CUSTOM_RULE_B) },
                RuleV2Provider { ExperimentalRule(CUSTOM_RULE_C) },
                editorConfig =
                    EditorConfig(
                        ktLintRuleExecutionEditorConfigProperty("ktlint_experimental", RuleExecution.enabled),
                        ktLintRuleExecutionEditorConfigProperty(STANDARD_RULE_C, RuleExecution.disabled),
                        ktLintRuleExecutionEditorConfigProperty("ktlint_$CUSTOM", RuleExecution.enabled),
                        ktLintRuleExecutionEditorConfigProperty(CUSTOM_RULE_C, RuleExecution.disabled),
                    ),
            ).toRuleIds()

        assertThat(actual).containsExactly(
            STANDARD_RULE_B,
            CUSTOM_RULE_B,
        )
    }

    @Test
    fun `When some standard rules which are all disabled explicitly then return empty`() {
        val actual =
            runWithRuleExecutionRuleFilter(
                RuleV2Provider { NormalRule(STANDARD_RULE_A) },
                RuleV2Provider { NormalRule(STANDARD_RULE_B) },
                editorConfig =
                    EditorConfig(
                        ktLintRuleExecutionEditorConfigProperty(STANDARD_RULE_A, RuleExecution.disabled),
                        ktLintRuleExecutionEditorConfigProperty(STANDARD_RULE_B, RuleExecution.disabled),
                    ),
            ).toRuleIds()

        assertThat(actual).isEmpty()
    }

    @Test
    fun `Given that the ktlint-suppression is disabled in the editorconfig properties then ignore that property`() {
        val actual =
            runWithRuleExecutionRuleFilter(
                RuleV2Provider { NormalRule(STANDARD_RULE_A) },
                RuleV2Provider { KtlintSuppressionRule(listOf(STANDARD_RULE_A)) },
                editorConfig =
                    EditorConfig(
                        ktLintRuleExecutionEditorConfigProperty(STANDARD_RULE_A, RuleExecution.disabled),
                        ktLintRuleExecutionEditorConfigProperty(KTLINT_SUPPRESSION_RULE_ID, RuleExecution.disabled),
                    ),
            ).toRuleIds()

        assertThat(actual).containsExactly(KTLINT_SUPPRESSION_RULE_ID)
    }

    @Test
    fun `Given that ktlint is disabled entirely then the internal rule for migrating the ktlint-disable directives is disabled`() {
        val actual =
            runWithRuleExecutionRuleFilter(
                RuleV2Provider { NormalRule(STANDARD_RULE_A) },
                RuleV2Provider { KtlintSuppressionRule(listOf(STANDARD_RULE_A)) },
                editorConfig =
                    EditorConfig(
                        ktLintDisableAllRuleExecutionEditorConfigProperty(),
                    ),
            ).toRuleIds()

        assertThat(actual).isEmpty()
    }

    @Test
    fun `Given a rule that only should be run when enabled explicitly, and the rule execution property is not set, then do not execute the rule`() {
        val actual =
            runWithRuleExecutionRuleFilter(
                RuleV2Provider { OnlyWhenEnabledInEditorconfigRule(STANDARD_RULE_A) },
                editorConfig = EditorConfig(),
            ).toRuleIds()

        assertThat(actual).isEmpty()
    }

    @Test
    fun `Given a rule that only should be run when enabled explicitly, and the rule execution property is enabled, then do execute the rule`() {
        val actual =
            runWithRuleExecutionRuleFilter(
                RuleV2Provider { OnlyWhenEnabledInEditorconfigRule(STANDARD_RULE_A) },
                editorConfig =
                    EditorConfig(
                        ktLintRuleExecutionEditorConfigProperty(STANDARD_RULE_A, RuleExecution.enabled),
                    ),
            ).toRuleIds()

        assertThat(actual).containsExactly(STANDARD_RULE_A)
    }

    /**
     * Create a [RuleExecutionRuleFilter] for a given set of [RuleV2Provider]s and an [EditorConfig].
     */
    private fun runWithRuleExecutionRuleFilter(
        vararg ruleProviders: RuleV2Provider,
        editorConfig: EditorConfig,
    ): Set<RuleV2Provider> =
        RuleExecutionRuleFilter(
            editorConfig = editorConfig.addPropertiesWithDefaultValueIfMissing(CODE_STYLE_PROPERTY),
        ).filter(
            ruleProviders.toSet(),
        )

    private fun Set<RuleV2Provider>.toRuleIds() = map { it.ruleId }

    private fun ktLintDisableAllRuleExecutionEditorConfigProperty() =
        Property
            .builder()
            .type(RULE_EXECUTION_PROPERTY_TYPE)
            .name(ALL_RULES_EXECUTION_PROPERTY.name)
            .value(RuleExecution.disabled.name)
            .build()

    private fun ktLintRuleExecutionEditorConfigProperty(
        ktlintRuleExecutionPropertyName: String,
        ruleExecution: RuleExecution,
    ) = Property
        .builder()
        .type(RULE_EXECUTION_PROPERTY_TYPE)
        .name(ktlintRuleExecutionPropertyName)
        .value(ruleExecution.name)
        .build()

    private fun ktLintRuleExecutionEditorConfigProperty(
        ruleId: RuleId,
        ruleExecution: RuleExecution,
    ) = ktLintRuleExecutionEditorConfigProperty(ruleId.createRuleExecutionEditorConfigProperty().name, ruleExecution)

    private companion object {
        val STANDARD = RuleSetId.STANDARD.value
        const val CUSTOM = "custom"
        val STANDARD_RULE_A = RuleId("$STANDARD:rule-a")
        val STANDARD_RULE_B = RuleId("$STANDARD:rule-b")
        val STANDARD_RULE_C = RuleId("$STANDARD:rule-c")
        val STANDARD_RULE_D = RuleId("$STANDARD:rule-d")
        val CUSTOM_RULE_B = RuleId("$CUSTOM:rule-b")
        val CUSTOM_RULE_C = RuleId("$CUSTOM:rule-c")
    }

    private open class NormalRule(
        ruleId: RuleId,
    ) : RuleV2(
            ruleId = ruleId,
            about = About(),
        )

    private open class ExperimentalRule(
        ruleId: RuleId,
    ) : RuleV2(
            ruleId = ruleId,
            about = About(),
        ),
        RuleV2.Experimental

    private open class OnlyWhenEnabledInEditorconfigRule(
        ruleId: RuleId,
    ) : RuleV2(
            ruleId = ruleId,
            about = About(),
        ),
        RuleV2.OnlyWhenEnabledInEditorconfig
}
