package io.github.ktlint.core.rule.engine.internal.rulefilter

import assertk.assertThat
import assertk.assertions.containsExactlyInAnyOrder
import assertk.assertions.isEmpty
import io.github.ktlint.core.rule.engine.api.KtLintRuleEngine
import io.github.ktlint.core.rule.engine.core.api.RuleId
import io.github.ktlint.core.rule.engine.core.api.RuleV2
import io.github.ktlint.core.rule.engine.core.api.RuleV2.About
import io.github.ktlint.core.rule.engine.core.api.RuleV2Provider
import org.junit.jupiter.api.Test

class RuleFilterKtTest {
    @Test
    fun `Given an empty list of rule filters then the list of rule providers contains provider for all rule ids initially provided by the ktlint engine`() {
        val actual =
            createKtLintRuleEngine(arrayOf(RULE_SET_A_RULE_A, RULE_SET_A_RULE_B, RULE_SET_B_RULE_A, RULE_SET_B_RULE_B))
                .applyRuleFilters()
                .map { it.ruleId }

        assertThat(actual).containsExactlyInAnyOrder(RULE_SET_A_RULE_A, RULE_SET_A_RULE_B, RULE_SET_B_RULE_A, RULE_SET_B_RULE_B)
    }

    @Test
    fun `Given a single rule filter then the list of rule providers contains only rule ids that match that filter`() {
        val actual =
            createKtLintRuleEngine(arrayOf(RULE_SET_A_RULE_A, RULE_SET_A_RULE_B, RULE_SET_B_RULE_A, RULE_SET_B_RULE_B))
                .applyRuleFilters(RuleIdRuleFilter(RULE_SET_A))
                .map { it.ruleId }

        assertThat(actual).containsExactlyInAnyOrder(RULE_SET_A_RULE_A, RULE_SET_A_RULE_B)
    }

    @Test
    fun `Given multiple rule filters then the list of rule providers contains only rule ids that match all filters`() {
        val actual =
            createKtLintRuleEngine(arrayOf(RULE_SET_A_RULE_A, RULE_SET_A_RULE_B, RULE_SET_B_RULE_B))
                .applyRuleFilters(
                    RuleIdRuleFilter(RULE_SET_A),
                    RuleIdRuleFilter(RULE_B),
                ).map { it.ruleId }

        assertThat(actual).containsExactlyInAnyOrder(RULE_SET_A_RULE_B)
    }

    @Test
    fun `Given multiple rule filters that exclude each other then the list of rule providers is empty`() {
        val actual =
            createKtLintRuleEngine(arrayOf(RULE_SET_A_RULE_A))
                .applyRuleFilters(
                    RuleIdRuleFilter(RULE_A),
                    RuleIdRuleFilter(RULE_SET_B),
                ).map { it.ruleId }

        assertThat(actual).isEmpty()
    }

    private class RuleIdRuleFilter(
        private val string: String,
    ) : RuleFilter {
        override fun filter(ruleProviders: Set<RuleV2Provider>): Set<RuleV2Provider> =
            ruleProviders
                .filter { it.ruleId.value.contains(string) }
                .toSet()
    }

    private fun createKtLintRuleEngine(ruleIds: Array<RuleId>): KtLintRuleEngine =
        ruleIds
            .map {
                RuleV2Provider {
                    RuleV2(
                        ruleId = it,
                        about = About(),
                    )
                }
            }.toSet()
            .let { KtLintRuleEngine(it) }

    private companion object {
        const val RULE_SET_A = "ruleset-a"
        const val RULE_SET_B = "ruleset-b"
        const val RULE_A = "rule-a"
        const val RULE_B = "rule-b"
        val RULE_SET_A_RULE_A = RuleId("$RULE_SET_A:$RULE_A")
        val RULE_SET_A_RULE_B = RuleId("$RULE_SET_A:$RULE_B")
        val RULE_SET_B_RULE_A = RuleId("$RULE_SET_B:$RULE_A")
        val RULE_SET_B_RULE_B = RuleId("$RULE_SET_B:$RULE_B")
    }
}
