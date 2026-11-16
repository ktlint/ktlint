package io.github.ktlint.core.rule.engine.internal

import io.github.ktlint.core.logger.api.initKtLintKLogger
import io.github.ktlint.core.rule.engine.core.api.RuleId
import io.github.ktlint.core.rule.engine.core.api.RuleSetId
import io.github.ktlint.core.rule.engine.core.api.RuleV2
import io.github.ktlint.core.rule.engine.core.api.RuleV2Provider
import io.github.ktlint.core.rule.engine.internal.rules.InternalRule
import io.github.ktlint.core.rule.engine.internal.rules.KTLINT_SUPPRESSION_RULE_ID
import io.github.oshai.kotlinlogging.KotlinLogging

private val LOGGER = KotlinLogging.logger {}.initKtLintKLogger()

/**
 * Sorts the [RuleV2Provider]s alphabetically on rule id. Standard rules of Ktlint are executed before custom rules.
 */
internal class RuleProviderSorter {
    /**
     * Prevent duplicate debug logging whenever the same set of [RuleV2.ruleId]s (but not the same instance) are sorted. As the sorting of
     * the [RuleV2Provider] has to be executed for each file.
     */
    private val debugLogCache = mutableMapOf<Int, Boolean>()

    @Synchronized
    fun getGroupedAndSortedRuleProviders(ruleProviders: Set<RuleV2Provider>): List<List<RuleV2Provider>> =
        ruleProviders
            .sortedWith(defaultRuleExecutionOrderComparator())
            .also { sortedRuleProviders ->
                if (LOGGER.isDebugEnabled()) {
                    logSortedRuleProviders(sortedRuleProviders)
                }
            }.groupBy { it.ruleId.groupId() }
            .values
            .toList()

    private fun logSortedRuleProviders(sortedRuleProviders: List<RuleV2Provider>) {
        debugLogCache
            .putIfAbsent(createHashCode(sortedRuleProviders), true)
            .also { previousValue ->
                if (previousValue == null) {
                    // Logging was not printed for this combination of rule providers as no entry was found in the cache
                    sortedRuleProviders
                        .joinToString(prefix = "Rules will be executed in order below:") {
                            "\n           - ${it.ruleId.value}"
                        }.also { LOGGER.debug { it } }
                }
            }
    }

    private fun createHashCode(sortedRuleProviders: List<RuleV2Provider>): Int =
        sortedRuleProviders
            .joinToString(
                prefix = "rule-ids=[",
                separator = ",",
                postfix = "]",
            ) { it.ruleId.value }
            .hashCode()

    private fun defaultRuleExecutionOrderComparator() =
        // The sort order below should guarantee a stable order of the rule between multiple invocations of KtLint given
        // the same set of input parameters. There should be no dependency on data ordering outside this class.
        compareBy<RuleV2Provider> { ruleV2Provider ->
            ruleV2Provider.ruleId.groupId()
        }.thenBy { it.ruleId.value }

    private enum class RuleGroup(
        val predicate: (RuleId) -> Boolean,
    ) {
        // Whenever the KtlintSuppressionRule finds a "ktlint-disable" directive, it will traverse up in the AST Node Tree to add a Suppress
        // annotation. Therefore, this rule needs to run on the entire AST Node before processing other rules.
        INTERNAL_SUPPRESSION_RULE({ ruleId -> ruleId == KTLINT_SUPPRESSION_RULE_ID }),

        // Process all other rules (excluding indentation rules) on the same AST Node before proceeding to the next AST Node.
        // The whitespace containing the line indent do appear in the AST before the actual content of the line. All wrapping rules are
        // based on actual content instead of on the whitespace node. If the "indent" rule would be handled normally, then indentation
        // errors would only be fixed in the ktlint run following on the run in which a rule adds or removes a newline.
        // The content of the string templates is handled by the StringTemplateIndentRule. This rule needs the opening and closing nodes of
        // the template to be indented correctly by the IndentationRule.
        STANDARD_EXCEPT_INDENT({ ruleId -> ruleId.ruleSetId == RuleSetId.STANDARD && ruleId !in INDENT_GROUP }),
        CUSTOM({ ruleId -> ruleId.ruleSetId != InternalRule.INTERNAL_RULESET_ID && ruleId.ruleSetId != RuleSetId.STANDARD }),

        // Finally run ident rule. It should run separately on the entire AST after all other rules have been processed.
        STANDARD_INDENT({ ruleId -> ruleId in INDENT_GROUP }),
    }

    fun RuleId.groupId() = RuleGroup.entries.indexOfFirst { it.predicate(this) }

    private companion object {
        val INDENT_GROUP =
            setOf(
                RuleId("standard:indent"),
                RuleId("standard:string-template-indent"),
            )
    }
}
