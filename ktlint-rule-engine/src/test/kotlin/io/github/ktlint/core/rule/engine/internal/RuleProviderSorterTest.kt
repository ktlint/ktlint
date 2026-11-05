package io.github.ktlint.core.rule.engine.internal

import io.github.ktlint.core.rule.engine.core.api.RuleId
import io.github.ktlint.core.rule.engine.core.api.RuleSetId
import io.github.ktlint.core.rule.engine.core.api.RuleV2
import io.github.ktlint.core.rule.engine.core.api.RuleV2Provider
import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.junit.jupiter.api.Test

class RuleProviderSorterTest {
    @Test
    fun `Multiple standard rules are run in alphabetical order`() {
        val actual =
            RuleProviderSorter()
                .getGroupedAndSortedRuleProviders(
                    ruleProviders =
                        createRuleProviders(
                            NormalRule(STANDARD_RULE_B),
                            NormalRule(STANDARD_RULE_A),
                        ),
                ).map { ruleProviders -> ruleProviders.map { it.ruleId } }

        assertThat(actual).containsExactly(
            listOf(STANDARD_RULE_A, STANDARD_RULE_B),
        )
    }

    @Test
    fun `Multiple rules in different rule sets are run in alphabetical order but grouped in order standard, and custom`() {
        val actual =
            RuleProviderSorter()
                .getGroupedAndSortedRuleProviders(
                    ruleProviders =
                        createRuleProviders(
                            ExperimentalRule(STANDARD_RULE_B),
                            ExperimentalRule(STANDARD_RULE_A),
                            NormalRule(CUSTOM_RULE_SET_A_RULE_B),
                            NormalRule(CUSTOM_RULE_SET_A_RULE_A),
                            NormalRule(STANDARD_RULE_D),
                            NormalRule(STANDARD_RULE_C),
                            NormalRule(CUSTOM_RULE_SET_B_RULE_B),
                            NormalRule(CUSTOM_RULE_SET_B_RULE_A),
                        ),
                ).map { ruleProviders -> ruleProviders.map { it.ruleId } }

        assertThat(actual).containsExactly(
            listOf(
                STANDARD_RULE_A,
                STANDARD_RULE_B,
                STANDARD_RULE_C,
                STANDARD_RULE_D,
            ),
            // Rules from custom rule sets are all grouped together
            listOf(
                CUSTOM_RULE_SET_A_RULE_A,
                CUSTOM_RULE_SET_A_RULE_B,
                CUSTOM_RULE_SET_B_RULE_A,
                CUSTOM_RULE_SET_B_RULE_B,
            ),
        )
    }

    private fun createRuleProviders(vararg rules: RuleV2): Set<RuleV2Provider> =
        rules
            .map { RuleV2Provider { it } }
            .toSet()

    private companion object {
        const val RULE_A = "rule-a"
        val STANDARD = RuleSetId.STANDARD.value
        const val CUSTOM_RULE_SET_A = "custom-rule-set-a"
        const val CUSTOM_RULE_SET_B = "custom-rule-set-b"
        val STANDARD_RULE_A = RuleId("$STANDARD:$RULE_A")
        val STANDARD_RULE_B = RuleId("$STANDARD:rule-b")
        val STANDARD_RULE_C = RuleId("$STANDARD:rule-c")
        val STANDARD_RULE_D = RuleId("$STANDARD:rule-d")
        val CUSTOM_RULE_SET_A_RULE_A = RuleId("$CUSTOM_RULE_SET_A:rule-a")
        val CUSTOM_RULE_SET_A_RULE_B = RuleId("$CUSTOM_RULE_SET_A:rule-b")
        val CUSTOM_RULE_SET_B_RULE_A = RuleId("$CUSTOM_RULE_SET_B:rule-a")
        val CUSTOM_RULE_SET_B_RULE_B = RuleId("$CUSTOM_RULE_SET_B:rule-b")
    }

    private open class NormalRule(
        ruleId: RuleId,
    ) : R(ruleId)

    private open class ExperimentalRule(
        ruleId: RuleId,
    ) : R(ruleId),
        RuleV2.Experimental

    private open class R(
        ruleId: RuleId,
    ) : RuleV2(
            ruleId = ruleId,
            about = About(),
        ) {
        override fun beforeVisitChildNodes(node: ASTNode): Unit =
            throw UnsupportedOperationException(
                "Rule should never be really invoked because that is not the aim of this unit test.",
            )
    }
}
