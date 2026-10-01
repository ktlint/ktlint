package io.github.ktlint.core.ruleset.standard.rules

import io.github.ktlint.core.rule.engine.core.api.AutocorrectDecision
import io.github.ktlint.core.rule.engine.core.api.ElementType.ENUM_ENTRY
import io.github.ktlint.core.rule.engine.core.api.ElementType.IDENTIFIER
import io.github.ktlint.core.rule.engine.core.api.RuleId
import io.github.ktlint.core.rule.engine.core.api.SinceKtlint
import io.github.ktlint.core.rule.engine.core.api.SinceKtlint.Status.EXPERIMENTAL
import io.github.ktlint.core.rule.engine.core.api.SinceKtlint.Status.STABLE
import io.github.ktlint.core.rule.engine.core.api.editorconfig.EditorConfig
import io.github.ktlint.core.rule.engine.core.api.editorconfig.EditorConfigProperty
import io.github.ktlint.core.rule.engine.core.api.editorconfig.SafeEnumValueParser
import io.github.ktlint.core.ruleset.standard.StandardRule
import io.github.ktlint.core.ruleset.standard.rules.internal.regExIgnoringDiacriticsAndStrokesOnLetters
import org.ec4j.core.model.PropertyType
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.CompositeElement
import org.jetbrains.kotlin.psi.KtPsiUtil

/**
 * https://kotlinlang.org/docs/coding-conventions.html#property-names
 */
@SinceKtlint("0.36", EXPERIMENTAL)
@SinceKtlint("0.46", STABLE)
public class EnumEntryNameCaseRule :
    StandardRule(
        id = "enum-entry-name-case",
        usesEditorConfigProperties = setOf(ENUM_ENTRY_NAME_CASING_PROPERTY),
    ) {
    private lateinit var enumEntryCasingRegex: Regex
    private lateinit var enumEntryCasingViolation: String

    override fun beforeFirstNode(editorConfig: EditorConfig) {
        when (editorConfig[ENUM_ENTRY_NAME_CASING_PROPERTY]) {
            EnumEntryNameCasing.UPPER_CASES -> {
                enumEntryCasingRegex = "[A-Z][A-Z_\\d]*".regExIgnoringDiacriticsAndStrokesOnLetters()
                enumEntryCasingViolation = "Enum entry name should be uppercase underscore-separated names like \"ENUM_ENTRY\""
            }

            EnumEntryNameCasing.CAMEL_CASES -> {
                enumEntryCasingRegex = "[A-Z]([A-Za-z\\d]*)".regExIgnoringDiacriticsAndStrokesOnLetters()
                enumEntryCasingViolation = "Enum entry name should be upper camel-case like \"EnumEntry\""
            }

            EnumEntryNameCasing.UPPER_OR_CAMEL_CASES -> {
                enumEntryCasingRegex = "[A-Z]([A-Za-z\\d]*|[A-Z_\\d]*)".regExIgnoringDiacriticsAndStrokesOnLetters()
                enumEntryCasingViolation =
                    "Enum entry name should be uppercase underscore-separated names like \"ENUM_ENTRY\" or upper camel-case like \"EnumEntry\""
            }
        }
    }

    override fun beforeVisitChildNodes(
        node: ASTNode,
        emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> AutocorrectDecision,
    ) {
        if (node !is CompositeElement) {
            return
        }
        if (node.elementType != ENUM_ENTRY) return
        val nameNode = node.findChildByType(IDENTIFIER) ?: return
        val name = KtPsiUtil.unquoteIdentifier(nameNode.text)

        if (!name.matches(enumEntryCasingRegex)) {
            emit(node.startOffset, enumEntryCasingViolation, false)
        }
    }

    public companion object {
        public enum class EnumEntryNameCasing {
            /**
             * Enforce all enum entry names to be uppercase underscore-separated names like "ENUM_ENTRY". Digits, diacritics and strokes are
             * allowed.
             */
            UPPER_CASES,

            /**
             * Enforce all enum entry names to be upper camel-case like "EnumEntry". Digits, diacritics and strokes are allowed.
             */
            CAMEL_CASES,

            /**
             * Enforce all enum entry names to be uppercase underscore-separated names like "ENUM_ENTRY" or upper camel-case like
             * "EnumEntry". Digits, diacritics and strokes are allowed.
             */
            UPPER_OR_CAMEL_CASES,
        }

        public val ENUM_ENTRY_NAME_CASING_PROPERTY_TYPE: PropertyType.LowerCasingPropertyType<EnumEntryNameCasing> =
            PropertyType.LowerCasingPropertyType(
                "ktlint_enum_entry_name_casing",
                "Enforce all enum entry names to be uppercase underscore-separated names like \"ENUM_ENTRY\" and/or upper " +
                    "camel-case like \"EnumEntry\". Digits, diacritics and strokes are always allowed.",
                SafeEnumValueParser(EnumEntryNameCasing::class.java),
                EnumEntryNameCasing.entries.map { it.name.lowercase() }.toSet(),
            )

        public val ENUM_ENTRY_NAME_CASING_PROPERTY: EditorConfigProperty<EnumEntryNameCasing> =
            EditorConfigProperty(
                type = ENUM_ENTRY_NAME_CASING_PROPERTY_TYPE,
                defaultValue = EnumEntryNameCasing.UPPER_OR_CAMEL_CASES,
            )
    }
}

public val ENUM_ENTRY_NAME_CASE_RULE_ID: RuleId = EnumEntryNameCaseRule().ruleId
