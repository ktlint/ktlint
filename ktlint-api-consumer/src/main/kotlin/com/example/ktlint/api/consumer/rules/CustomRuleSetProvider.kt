package com.example.ktlint.api.consumer.rules

import io.github.ktlint.core.rule.engine.core.api.RuleV2Provider

internal const val CUSTOM_RULE_SET_ID = "custom-rule-set-id"

internal val KTLINT_API_CONSUMER_RULE_PROVIDERS =
    setOf(
        // Can provide custom rules
        RuleV2Provider { NoVarRule() },
        // If rulesets are include at compile time, they can be added to the custom rule provider.
        // RuleV2Provider { IndentationRule() },
    )
