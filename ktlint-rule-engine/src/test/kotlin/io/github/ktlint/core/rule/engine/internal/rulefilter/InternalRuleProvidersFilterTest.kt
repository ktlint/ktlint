package io.github.ktlint.core.rule.engine.internal.rulefilter

import assertk.assertThat
import assertk.assertions.containsExactly
import io.github.ktlint.core.rule.engine.api.KtLintRuleEngine
import io.github.ktlint.core.rule.engine.core.api.AutocorrectDecision
import io.github.ktlint.core.rule.engine.core.api.RuleId
import io.github.ktlint.core.rule.engine.core.api.RuleSetId
import io.github.ktlint.core.rule.engine.core.api.RuleV2
import io.github.ktlint.core.rule.engine.core.api.RuleV2Provider
import io.github.ktlint.core.rule.engine.internal.rules.KTLINT_SUPPRESSION_RULE_ID
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.junit.jupiter.api.Test

class InternalRuleProvidersFilterTest {
    @Test
    fun `Given a ktlint rule engine then add the ktlint suppression rule provider`() {
        val ktLintRuleEngine =
            KtLintRuleEngine(
                ruleProviders =
                    setOf(
                        RuleV2Provider {
                            object : R(ruleId = STANDARD_RULE_A) {}
                        },
                    ),
            )
        val actual =
            InternalRuleProvidersFilter(ktLintRuleEngine)
                .filter(ktLintRuleEngine.ruleProviders)
                .map { it.ruleId }

        assertThat(actual).containsExactly(
            STANDARD_RULE_A,
            KTLINT_SUPPRESSION_RULE_ID,
        )
    }

    private companion object {
        const val RULE_A = "rule-a"
        val STANDARD = RuleSetId.STANDARD.value
        val STANDARD_RULE_A = RuleId("$STANDARD:$RULE_A")
    }

    private open class R(
        ruleId: RuleId,
    ) : RuleV2(
            ruleId = ruleId,
            about = About(),
        ) {
        override fun beforeVisitChildNodes(
            node: ASTNode,
            emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> AutocorrectDecision,
        ): Unit =
            throw UnsupportedOperationException(
                "Rule should never be really invoked because that is not the aim of this unit test.",
            )
    }
}
