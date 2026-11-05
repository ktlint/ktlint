package io.github.ktlint.core.rule.engine.internal

import io.github.ktlint.core.logger.api.initKtLintKLogger
import io.github.ktlint.core.rule.engine.core.api.RuleV2
import io.github.ktlint.core.rule.engine.core.api.RuleV2Provider
import io.github.ktlint.core.rule.engine.core.api.editorconfig.EditorConfig
import io.github.oshai.kotlinlogging.KotlinLogging

private val LOGGER = KotlinLogging.logger {}.initKtLintKLogger()

/**
 * The VisitorProvider is created for each file being scanned. As the [RuleProviderSorter] logs the order in which the
 * rules are executed, a singleton instance of the class is used to prevent that the logs are flooded with duplicate
 * log lines.
 */
private val RULE_PROVIDER_SORTER = RuleProviderSorter()

internal class VisitorProvider(
    /**
     * The set of [RuleV2Provider]s to be executed. This set should not contain any [RuleV2Provider]s which are disabled via the
     * [EditorConfig].
     */
    ruleProviders: Set<RuleV2Provider>,
) {
    private val ruleProvidersGroupedAndSorted: List<List<RuleV2Provider>> =
        RULE_PROVIDER_SORTER.getGroupedAndSortedRuleProviders(ruleProviders)

    internal val rules: List<List<RuleV2>>
        get() {
            if (ruleProvidersGroupedAndSorted.isEmpty()) {
                LOGGER.debug { "Skipping file as no enabled rules are found to be executed" }
                return emptyList()
            }
            return ruleProvidersGroupedAndSorted
                .map { group -> group.map { it.createNewRuleInstance() } }
        }
}
