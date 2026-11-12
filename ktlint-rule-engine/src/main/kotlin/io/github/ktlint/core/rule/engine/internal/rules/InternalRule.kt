package io.github.ktlint.core.rule.engine.internal.rules

import io.github.ktlint.core.rule.engine.core.api.RuleId
import io.github.ktlint.core.rule.engine.core.api.RuleSetId
import io.github.ktlint.core.rule.engine.core.api.RuleV2
import io.github.ktlint.core.rule.engine.core.api.editorconfig.EditorConfigProperty

internal val INTERNAL_RULE_ABOUT =
    RuleV2.About(
        maintainer = "KtLint",
        repositoryUrl = "https://github.com/ktlint/ktlint",
        issueTrackerUrl = "https://github.com/ktlint/ktlint/issues",
    )

/**
 * Internal rules can only be declared and instantiated in the 'ktlint-rule-engine'.
 */
public open class InternalRule internal constructor(
    id: String,
    override val usesEditorConfigProperties: Set<EditorConfigProperty<*>> = emptySet(),
) : RuleV2(
        ruleId = RuleId("${INTERNAL_RULESET_ID.value}:$id"),
        usesEditorConfigProperties = usesEditorConfigProperties,
        about = INTERNAL_RULE_ABOUT,
    ) {
    internal companion object {
        val INTERNAL_RULESET_ID = RuleSetId("internal")
    }
}
