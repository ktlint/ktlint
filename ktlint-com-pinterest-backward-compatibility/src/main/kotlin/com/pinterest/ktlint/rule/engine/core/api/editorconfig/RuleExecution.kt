@file:Suppress("DEPRECATION")
@file:JvmName("RuleExecutionEditorConfigPropertyKt") // Preserve the binary-compatible facade class name after the file rename below

package com.pinterest.ktlint.rule.engine.core.api.editorconfig

import com.pinterest.ktlint.rule.engine.core.api.RuleId
import com.pinterest.ktlint.rule.engine.core.api.RuleSetId
import org.ec4j.core.model.PropertyType

@Suppress("EnumEntryName")
@Deprecated(message = "Provides backwards compatibility of custom ruleset JARs created for Ktlint 1.x. Don't use for RuleV2")
public enum class RuleExecution {
    enabled,
    disabled,
}

@Deprecated(message = "Provides backwards compatibility of custom ruleset JARs created for Ktlint 1.x. Don't use for RuleV2")
public val RULE_EXECUTION_PROPERTY_TYPE: PropertyType.LowerCasingPropertyType<RuleExecution> =
    PropertyType.LowerCasingPropertyType(
        "ktlint_rule_execution",
        "When enabled, rule execution is allowed. This property can de defined at different levels like an entire ruleset, a specific " +
            "rule or a specific property of the rule.",
        SafeEnumValueParser(RuleExecution::class.java),
        RuleExecution.entries.map { it.name }.toSet(),
    )

@Deprecated(message = "Provides backwards compatibility of custom ruleset JARs created for Ktlint 1.x. Don't use for RuleV2")
public val ALL_RULES_EXECUTION_PROPERTY: EditorConfigProperty<RuleExecution> =
    EditorConfigProperty(
        name = "ktlint",
        type = RULE_EXECUTION_PROPERTY_TYPE,
        defaultValue = RuleExecution.enabled,
    )

@Deprecated(message = "Provides backwards compatibility of custom ruleset JARs created for Ktlint 1.x. Don't use for RuleV2")
public val EXPERIMENTAL_RULES_EXECUTION_PROPERTY: EditorConfigProperty<RuleExecution> =
    EditorConfigProperty(
        name = "ktlint_experimental",
        type = RULE_EXECUTION_PROPERTY_TYPE,
        defaultValue = RuleExecution.disabled,
    )

@Deprecated(message = "Provides backwards compatibility of custom ruleset JARs created for Ktlint 1.x. Don't use for RuleV2")
public fun RuleSetId.createRuleSetExecutionEditorConfigProperty(
    ruleExecution: RuleExecution = RuleExecution.enabled,
): EditorConfigProperty<RuleExecution> =
    EditorConfigProperty(
        name = ktLintRuleSetExecutionPropertyName(),
        type = RULE_EXECUTION_PROPERTY_TYPE,
        defaultValue = ruleExecution,
    )

@Deprecated(message = "Provides backwards compatibility of custom ruleset JARs created for Ktlint 1.x. Don't use for RuleV2")
public fun RuleId.createRuleExecutionEditorConfigProperty(
    ruleExecution: RuleExecution = RuleExecution.enabled,
): EditorConfigProperty<RuleExecution> =
    EditorConfigProperty(
        name = ktLintRuleExecutionPropertyName(),
        type = RULE_EXECUTION_PROPERTY_TYPE,
        defaultValue = ruleExecution,
    )

@Deprecated(message = "Provides backwards compatibility of custom ruleset JARs created for Ktlint 1.x. Don't use for RuleV2")
public fun RuleId.ktLintRuleExecutionPropertyName(): String = "ktlint_${value.replaceFirst(":", "_")}"

@Deprecated(message = "Provides backwards compatibility of custom ruleset JARs created for Ktlint 1.x. Don't use for RuleV2")
public fun RuleSetId.ktLintRuleSetExecutionPropertyName(): String = "ktlint_$value"
