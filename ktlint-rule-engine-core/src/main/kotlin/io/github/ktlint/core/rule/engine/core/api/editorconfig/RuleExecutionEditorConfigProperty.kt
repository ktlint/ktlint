package io.github.ktlint.core.rule.engine.core.api.editorconfig

import io.github.ktlint.core.rule.engine.core.api.RuleId
import io.github.ktlint.core.rule.engine.core.api.RuleSetId
import org.ec4j.core.model.PropertyType

public enum class RuleExecution {
    ENABLED,
    DISABLED,
}

public val RULE_EXECUTION_PROPERTY_TYPE: PropertyType.LowerCasingPropertyType<RuleExecution> =
    PropertyType.LowerCasingPropertyType(
        "ktlint_rule_execution",
        "When enabled, rule execution is allowed. This property can de defined at different levels like an entire ruleset, a specific " +
            "rule or a specific property of the rule.",
        SafeEnumValueParser(RuleExecution::class.java),
        RuleExecution.entries.map { it.name }.toSet(),
    )

/**
 * When disabled, no ktlint rules are executed. This property can be used to disable all rulesets (including internal rules) for a given
 * glob in the '.editorconfig'.
 */
public val ALL_RULES_EXECUTION_PROPERTY: EditorConfigProperty<RuleExecution> =
    EditorConfigProperty(
        // Explicitly name the rule as multiple properties exists for this property type
        name = "ktlint",
        type = RULE_EXECUTION_PROPERTY_TYPE,
        defaultValue = RuleExecution.ENABLED,
    )

/**
 * When enabled, a rule that implements interface "Rule.Experimental" is executed unless that rule itself is explicitly disabled.
 */
public val EXPERIMENTAL_RULES_EXECUTION_PROPERTY: EditorConfigProperty<RuleExecution> =
    EditorConfigProperty(
        // Explicitly name the rule as multiple properties exists for this property type
        name = "ktlint_experimental",
        type = RULE_EXECUTION_PROPERTY_TYPE,
        defaultValue = RuleExecution.DISABLED,
    )

/**
 * Generates the rule execution '.editorconfig' property for the given [RuleSetId].
 */
public fun RuleSetId.createRuleSetExecutionEditorConfigProperty(
    ruleExecution: RuleExecution = RuleExecution.ENABLED,
): EditorConfigProperty<RuleExecution> =
    EditorConfigProperty(
        // Explicitly name the rule as multiple properties exists for this property type
        name = ktLintRuleSetExecutionPropertyName(),
        type = RULE_EXECUTION_PROPERTY_TYPE,
        defaultValue = ruleExecution,
    )

/**
 * Generates the rule execution '.editorconfig' property for the given [RuleId].
 */
public fun RuleId.createRuleExecutionEditorConfigProperty(
    ruleExecution: RuleExecution = RuleExecution.ENABLED,
): EditorConfigProperty<RuleExecution> =
    EditorConfigProperty(
        name = ktLintRuleExecutionPropertyName(),
        type = RULE_EXECUTION_PROPERTY_TYPE,
        defaultValue = ruleExecution,
    )

/**
 * Constructs the name of the '.editorconfig' property that determines whether the given [RuleId] is to be executed.
 */
public fun RuleId.ktLintRuleExecutionPropertyName(): String = "ktlint_${value.replaceFirst(":", "_")}"

/**
 * Constructs the name of the '.editorconfig' property that determines whether the rule set with the given [RuleSetId] is to be executed.
 */
public fun RuleSetId.ktLintRuleSetExecutionPropertyName(): String = "ktlint_$value"
