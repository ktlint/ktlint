package yourpkgname

import io.github.ktlint.core.cli.ruleset.core.api.RuleSetV2Provider
import io.github.ktlint.core.rule.engine.core.api.RuleSetId
import io.github.ktlint.core.rule.engine.core.api.RuleV2Provider

internal const val CUSTOM_RULE_SET_ID = "custom-rule-set-id"

class CustomRuleSetProvider : RuleSetV2Provider(RuleSetId(CUSTOM_RULE_SET_ID)) {
    override fun getRuleProviders(): Set<RuleV2Provider> =
        setOf(
            RuleV2Provider { NoVarRule() },
        )
}
