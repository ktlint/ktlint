package io.github.ktlint.core.rule.engine.internal.rulefilter

import io.github.ktlint.core.logger.api.initKtLintKLogger
import io.github.ktlint.core.rule.engine.api.KtLintRuleEngine
import io.github.ktlint.core.rule.engine.core.api.RuleV2
import io.github.ktlint.core.rule.engine.core.api.RuleV2Provider
import io.github.ktlint.core.rule.engine.core.api.editorconfig.ALL_RULES_EXECUTION_PROPERTY
import io.github.ktlint.core.rule.engine.core.api.editorconfig.CODE_STYLE_PROPERTY
import io.github.ktlint.core.rule.engine.core.api.editorconfig.CodeStyleValue
import io.github.ktlint.core.rule.engine.core.api.editorconfig.EXPERIMENTAL_RULES_EXECUTION_PROPERTY
import io.github.ktlint.core.rule.engine.core.api.editorconfig.EditorConfig
import io.github.ktlint.core.rule.engine.core.api.editorconfig.EditorConfigProperty
import io.github.ktlint.core.rule.engine.core.api.editorconfig.RULE_EXECUTION_PROPERTY_TYPE
import io.github.ktlint.core.rule.engine.core.api.editorconfig.RuleExecution
import io.github.ktlint.core.rule.engine.core.api.editorconfig.ktLintRuleExecutionPropertyName
import io.github.ktlint.core.rule.engine.core.api.editorconfig.ktLintRuleSetExecutionPropertyName
import io.github.ktlint.core.rule.engine.internal.rules.KTLINT_SUPPRESSION_RULE_ID
import io.github.oshai.kotlinlogging.KotlinLogging

private val LOGGER = KotlinLogging.logger {}.initKtLintKLogger()

/**
 * Filters the [RuleV2Provider]s defined in the [KtLintRuleEngine] for [RuleV2]s which are enabled for the given [EditorConfig].
 */
internal class RuleExecutionRuleFilter(
    private val editorConfig: EditorConfig,
) : RuleFilter {
    override fun filter(ruleProviders: Set<RuleV2Provider>): Set<RuleV2Provider> =
        if (disableKtlintEntirely()) {
            emptySet()
        } else {
            val ruleExecutionFilter =
                RuleExecutionFilter(
                    editorConfig.ruleExecutionProperties(ruleProviders),
                    editorConfig[CODE_STYLE_PROPERTY],
                )
            ruleProviders
                .filter { ruleExecutionFilter.isEnabled(it) }
                .toSet()
        }

    private fun disableKtlintEntirely() =
        editorConfig.getEditorConfigValueOrNull(RULE_EXECUTION_PROPERTY_TYPE, ALL_RULES_EXECUTION_PROPERTY.name) == RuleExecution.DISABLED

    private fun EditorConfig.ruleExecutionProperties(ruleProviders: Set<RuleV2Provider>): Map<String, RuleExecution> {
        val ruleExecutionPropertyNames =
            ruleExecutionPropertyNames(ruleProviders)
                .plus(ruleSetExecutionPropertyNames(ruleProviders))
                .plus(EXPERIMENTAL_RULES_EXECUTION_PROPERTY.name)
        return map { it.name to it.sourceValue }
            .toMap()
            .filterKeys { it in ruleExecutionPropertyNames }
            .mapValues { RULE_EXECUTION_PROPERTY_TYPE.parse(it.value).parsed }
    }

    private fun ruleExecutionPropertyNames(ruleProviders: Set<RuleV2Provider>) =
        ruleProviders
            .map { it.ruleId.ktLintRuleExecutionPropertyName() }
            .distinct()

    private fun ruleSetExecutionPropertyNames(ruleProviders: Set<RuleV2Provider>) =
        ruleProviders
            .map { it.ruleId.ruleSetId.ktLintRuleSetExecutionPropertyName() }
            .distinct()
}

/**
 * Creates a filter of [RuleExecution] properties. This filter is basically an extract of the [EditorConfig] which allows to retrieve rule
 * execution properties for which no proper [EditorConfigProperty] is defined as rules have no need to be able to extract such a property
 * from [EditorConfig].
 */
private class RuleExecutionFilter(
    val ruleExecutionProperties: Map<String, RuleExecution>,
    val codeStyleValue: CodeStyleValue,
) {
    fun isEnabled(ruleProvider: RuleV2Provider) = isRuleEnabled(ruleProvider.createNewRuleInstance())

    private fun isRuleEnabled(rule: RuleV2) =
        /*
         * If set for the rule, the rule execution property takes precedence above other checks. This allows for execution of a specific
         * experimental or ktlint_official code style rule without enabling them all. Also, this allows to disable a specific rule in case
         * the experimental and/or ktlint_official code style rules are enabled.
         */
        ruleExecution(rule.ruleId.ktLintRuleExecutionPropertyName())
            ?.let {
                if (rule.ruleId == KTLINT_SUPPRESSION_RULE_ID && it == RuleExecution.DISABLED) {
                    LOGGER.warn { "Rule '${rule.ruleId.value}' can not be disabled via the '.editorconfig'" }
                    true
                } else {
                    it == RuleExecution.ENABLED
                }
            }
            ?: isRuleConditionallyEnabled(rule)

    private fun isRuleConditionallyEnabled(rule: RuleV2) =
        when {
            rule is RuleV2.Experimental && rule is RuleV2.OfficialCodeStyle -> {
                isExperimentalEnabled(rule) && isOfficialCodeStyleEnabled(rule)
            }

            rule is RuleV2.Experimental -> {
                isExperimentalEnabled(rule)
            }

            rule is RuleV2.OfficialCodeStyle -> {
                isOfficialCodeStyleEnabled(rule)
            }

            rule is RuleV2.OnlyWhenEnabledInEditorconfig -> {
                ruleExecution(rule.ruleId.ktLintRuleExecutionPropertyName()) == RuleExecution.DISABLED
            }

            else -> {
                isRuleSetEnabled(rule)
            }
        }

    private fun isExperimentalEnabled(rule: RuleV2) =
        ruleExecution(EXPERIMENTAL_RULES_EXECUTION_PROPERTY.name) == RuleExecution.ENABLED &&
            ruleExecution(rule.ruleId.ruleSetId.ktLintRuleSetExecutionPropertyName()) != RuleExecution.DISABLED &&
            ruleExecution(rule.ruleId.ktLintRuleExecutionPropertyName()) != RuleExecution.DISABLED

    private fun isOfficialCodeStyleEnabled(rule: RuleV2) =
        codeStyleValue == CodeStyleValue.KTLINT_OFFICIAL &&
            ruleExecution(rule.ruleId.ruleSetId.ktLintRuleSetExecutionPropertyName()) != RuleExecution.DISABLED &&
            ruleExecution(rule.ruleId.ktLintRuleExecutionPropertyName()) != RuleExecution.DISABLED

    private fun isRuleSetEnabled(rule: RuleV2) =
        ruleExecution(rule.ruleId.ruleSetId.ktLintRuleSetExecutionPropertyName())
            .let { ruleSetExecution ->
                if (ruleSetExecution?.name == EXPERIMENTAL_RULES_EXECUTION_PROPERTY.name) {
                    // Rules in the experimental rule set are only run when enabled explicitly.
                    ruleSetExecution == RuleExecution.ENABLED
                } else {
                    // Rules in other rule sets are enabled by default.
                    ruleSetExecution != RuleExecution.DISABLED
                }
            }

    private fun ruleExecution(ruleExecutionPropertyName: String) = ruleExecutionProperties[ruleExecutionPropertyName]
}
