@file:JvmName("CodeStyleEditorConfigPropertyKt") // Preserve the binary-compatible facade class name after the file rename below
@file:Suppress("DEPRECATION")

package com.pinterest.ktlint.rule.engine.core.api.editorconfig

import org.ec4j.core.model.PropertyType
import org.ec4j.core.model.PropertyType.PropertyValueParser.EnumValueParser

/**
 * Code style to be used while linting and formatting. Note that the [EnumValueParser] requires values to be lowercase.
 */
@Deprecated(message = "Provides backwards compatibility of custom ruleset JARs created for Ktlint 1.x. Don't use for RuleV2")
@Suppress("EnumEntryName")
public enum class CodeStyleValue {
    android_studio,
    intellij_idea,
    ktlint_official,
}

@Deprecated(message = "Provides backwards compatibility of custom ruleset JARs created for Ktlint 1.x. Don't use for RuleV2")
public val CODE_STYLE_PROPERTY_TYPE: PropertyType.LowerCasingPropertyType<CodeStyleValue> =
    PropertyType.LowerCasingPropertyType(
        "ktlint_code_style",
        "The code style ('ktlint_official', 'intellij_idea' or 'android_studio') to be applied. By default the 'ktlint_official' code " +
            "style is used",
        SafeEnumValueParser(CodeStyleValue::class.java),
        CodeStyleValue.entries.map { it.name }.toSet(),
    )

@Deprecated(message = "Provides backwards compatibility of custom ruleset JARs created for Ktlint 1.x. Don't use for RuleV2")
public val CODE_STYLE_PROPERTY: EditorConfigProperty<CodeStyleValue> =
    EditorConfigProperty(
        type = CODE_STYLE_PROPERTY_TYPE,
        defaultValue = CodeStyleValue.ktlint_official,
        androidStudioCodeStyleDefaultValue = CodeStyleValue.android_studio,
        intellijIdeaCodeStyleDefaultValue = CodeStyleValue.intellij_idea,
        ktlintOfficialCodeStyleDefaultValue = CodeStyleValue.ktlint_official,
    )
