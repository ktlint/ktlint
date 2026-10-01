package io.github.ktlint.core.ruleset.standard.rules

import io.github.ktlint.core.rule.engine.core.api.AutocorrectDecision
import io.github.ktlint.core.rule.engine.core.api.ElementType.COMMA
import io.github.ktlint.core.rule.engine.core.api.ElementType.IDENTIFIER
import io.github.ktlint.core.rule.engine.core.api.ElementType.IMPORT_DIRECTIVE
import io.github.ktlint.core.rule.engine.core.api.ElementType.KDOC
import io.github.ktlint.core.rule.engine.core.api.ElementType.PACKAGE_DIRECTIVE
import io.github.ktlint.core.rule.engine.core.api.ElementType.STRING_TEMPLATE
import io.github.ktlint.core.rule.engine.core.api.RuleId
import io.github.ktlint.core.rule.engine.core.api.SinceKtlint
import io.github.ktlint.core.rule.engine.core.api.SinceKtlint.Status.STABLE
import io.github.ktlint.core.rule.engine.core.api.editorconfig.EditorConfig
import io.github.ktlint.core.rule.engine.core.api.editorconfig.EditorConfigProperty
import io.github.ktlint.core.rule.engine.core.api.editorconfig.MAX_LINE_LENGTH_PROPERTY
import io.github.ktlint.core.rule.engine.core.api.editorconfig.MAX_LINE_LENGTH_PROPERTY_OFF
import io.github.ktlint.core.rule.engine.core.api.editorconfig.RULE_EXECUTION_PROPERTY_TYPE
import io.github.ktlint.core.rule.engine.core.api.editorconfig.RuleExecution
import io.github.ktlint.core.rule.engine.core.api.editorconfig.ktLintRuleExecutionPropertyName
import io.github.ktlint.core.rule.engine.core.api.findParentByType
import io.github.ktlint.core.rule.engine.core.api.isPartOf
import io.github.ktlint.core.rule.engine.core.api.isPartOfComment
import io.github.ktlint.core.rule.engine.core.api.isWhiteSpace
import io.github.ktlint.core.rule.engine.core.api.isWhiteSpaceWithNewline
import io.github.ktlint.core.rule.engine.core.api.leavesOnLine
import io.github.ktlint.core.rule.engine.core.api.lineLength
import io.github.ktlint.core.rule.engine.core.api.nextLeaf
import io.github.ktlint.core.rule.engine.core.api.parent
import io.github.ktlint.core.rule.engine.core.api.prevLeaf
import io.github.ktlint.core.ruleset.standard.StandardRule
import org.ec4j.core.model.PropertyType
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.LeafPsiElement

@SinceKtlint("0.9", STABLE)
public class MaxLineLengthRule :
    StandardRule(
        id = "max-line-length",
        usesEditorConfigProperties =
            setOf(
                MAX_LINE_LENGTH_PROPERTY,
                IGNORE_BACKTICKED_IDENTIFIER_PROPERTY,
            ),
    ) {
    private var maxLineLength: Int = MAX_LINE_LENGTH_PROPERTY.defaultValue
    private var ignoreBackTickedIdentifier = IGNORE_BACKTICKED_IDENTIFIER_PROPERTY.defaultValue

    override fun beforeFirstNode(editorConfig: EditorConfig) {
        ignoreBackTickedIdentifier = editorConfig[IGNORE_BACKTICKED_IDENTIFIER_PROPERTY]
        maxLineLength = editorConfig.maxLineLength()
        if (maxLineLength == MAX_LINE_LENGTH_PROPERTY_OFF) {
            stopTraversalOfAST()
        }
    }

    override fun beforeVisitChildNodes(
        node: ASTNode,
        emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> AutocorrectDecision,
    ) {
        if (node.isWhiteSpace) {
            return
        }
        node
            .takeIf { it is LeafPsiElement }
            ?.takeIf { it.nextLeaf == null || it.nextLeaf.isWhiteSpaceWithNewline }
            ?.takeIf { it.lineLength() > maxLineLength }
            ?.takeUnless { it.isPartOf(PACKAGE_DIRECTIVE) }
            ?.takeUnless { it.isPartOf(IMPORT_DIRECTIVE) }
            ?.takeUnless { it.isPartOf(KDOC) }
            ?.takeUnless { it.isPartOfRawMultiLineString() }
            ?.takeUnless { it.isLineOnlyContainingSingleTemplateString() }
            ?.takeUnless { it.elementType == COMMA && it.prevLeaf?.isLineOnlyContainingSingleTemplateString() ?: false }
            ?.takeUnless { it.isLineOnlyContainingComment() }
            ?.let {
                // Calculate the offset at the last possible position at which the newline should be inserted on the line
                val offset =
                    node
                        .leavesOnLine
                        .first()
                        .startOffset
                        .plus(maxLineLength + 1)
                emit(
                    offset,
                    "Exceeded max line length ($maxLineLength)",
                    false,
                )
            }
    }

    private fun ASTNode.lineLength() =
        leavesOnLine
            .filterNot {
                ignoreBackTickedIdentifier &&
                    it.elementType == IDENTIFIER &&
                    it.text.matches(BACKTICKED_IDENTIFIER_REGEX)
            }.lineLength

    private fun ASTNode.isPartOfRawMultiLineString() =
        this
            .findParentByType(STRING_TEMPLATE)
            ?.let { it.firstChildNode.text == "\"\"\"" && it.textContains('\n') } == true

    private fun ASTNode.isLineOnlyContainingSingleTemplateString() =
        parent
            ?.takeIf { it.elementType == STRING_TEMPLATE }
            ?.let { stringTemplate ->
                stringTemplate
                    .prevLeaf
                    .let { leafBeforeStringTemplate ->
                        leafBeforeStringTemplate == null || leafBeforeStringTemplate.isWhiteSpaceWithNewline
                    }
            }
            ?: false

    private fun ASTNode.isLineOnlyContainingComment() =
        isPartOfComment &&
            (prevLeaf == null || prevLeaf.isWhiteSpaceWithNewline)

    public companion object {
        public val IGNORE_BACKTICKED_IDENTIFIER_PROPERTY: EditorConfigProperty<Boolean> =
            EditorConfigProperty(
                type =
                    PropertyType.LowerCasingPropertyType(
                        "ktlint_ignore_back_ticked_identifier",
                        "Defines whether the backticked identifier (``) should be ignored",
                        PropertyType.PropertyValueParser.BOOLEAN_VALUE_PARSER,
                        setOf(true.toString(), false.toString()),
                    ),
                defaultValue = false,
            )
        private val BACKTICKED_IDENTIFIER_REGEX = Regex("`.*`")
    }
}

/**
 * Gets the max_line_length property if the `max-line-length` rule is enabled. Otherwise, returns [Int.MAX_VALUE].
 *
 * Normally, rules should not have direct dependencies on other rules. This rule is an exception to that. In case the `max-line-length`
 * property in the `.editorconfig` is set, or inferred via a default value based on the `ktlint_code_style`, but the `max-line-length` rule
 * is disabled, then those other rules might start wrapping lines. Conceptually, the `max-line-length` rule determines whether ktlint should
 * or should not use the `max_line_length` property.
 */
public fun EditorConfig.maxLineLength(): Int =
    if (maxLineLengthRuleEnabled()) {
        this[MAX_LINE_LENGTH_PROPERTY]
    } else {
        Int.MAX_VALUE
    }

private fun EditorConfig.maxLineLengthRuleEnabled(): Boolean =
    RuleExecution.ENABLED ==
        getEditorConfigValueOrNull(
            RULE_EXECUTION_PROPERTY_TYPE,
            MAX_LINE_LENGTH_RULE_ID.ktLintRuleExecutionPropertyName(),
        )

public val MAX_LINE_LENGTH_RULE_ID: RuleId = MaxLineLengthRule().ruleId
