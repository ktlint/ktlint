package io.github.ktlint.core.ruleset.standard.rules

import com.intellij.lang.ASTNode
import com.intellij.psi.tree.IElementType
import io.github.ktlint.core.rule.engine.core.api.ElementType.CLASS_BODY
import io.github.ktlint.core.rule.engine.core.api.ElementType.SUPER_TYPE_LIST
import io.github.ktlint.core.rule.engine.core.api.ElementType.TYPE_ARGUMENT_LIST
import io.github.ktlint.core.rule.engine.core.api.ElementType.TYPE_CONSTRAINT_LIST
import io.github.ktlint.core.rule.engine.core.api.ElementType.TYPE_PARAMETER_LIST
import io.github.ktlint.core.rule.engine.core.api.ElementType.VALUE_ARGUMENT_LIST
import io.github.ktlint.core.rule.engine.core.api.ElementType.VALUE_PARAMETER_LIST
import io.github.ktlint.core.rule.engine.core.api.RuleId
import io.github.ktlint.core.rule.engine.core.api.RuleV2
import io.github.ktlint.core.rule.engine.core.api.SinceKtlint
import io.github.ktlint.core.rule.engine.core.api.SinceKtlint.Status.EXPERIMENTAL
import io.github.ktlint.core.rule.engine.core.api.SinceKtlint.Status.STABLE
import io.github.ktlint.core.rule.engine.core.api.isWhiteSpace
import io.github.ktlint.core.rule.engine.core.api.nextSibling
import io.github.ktlint.core.rule.engine.core.api.parent
import io.github.ktlint.core.rule.engine.core.api.prevSibling
import io.github.ktlint.core.rule.engine.core.api.replaceTextWith
import io.github.ktlint.core.ruleset.standard.StandardRule

@SinceKtlint("0.49", EXPERIMENTAL)
@SinceKtlint("1.0", STABLE)
public class NoBlankLineInListRule :
    StandardRule("no-blank-line-in-list"),
    RuleV2.OfficialCodeStyle {
    override fun beforeVisitChildNodes(node: ASTNode) {
        if (!node.isWhiteSpace) {
            return
        }

        node
            .parent
            ?.elementType
            ?.takeIf { it in LIST_TYPES }
            ?.let { treeParentElementType ->
                visitWhiteSpace(node, treeParentElementType)
            }

        // Note: depending on the implementation of the list type in the Kotlin language, the whitespace before the first and after the last
        // element in the list might be or not be part of the list.
        // In a VALUE_ARGUMENT_LIST the whitespaces before the first and after the last element are included in the VALUE_ARGUMENT_LIST. In
        // the SUPER_TYPE_LIST of a CLASS the whitespaces before the first super type is a child of the CLASS. The whitespace after the last
        // SUPER_TYPE is part of the class only when the class has a body.
        node
            .nextSibling
            ?.elementType
            ?.takeIf { it in LIST_TYPES }
            ?.let { treeParentElementType ->
                visitWhiteSpace(
                    node = node,
                    partOfElementType = treeParentElementType,
                    replaceWithSingeSpace = treeParentElementType == TYPE_CONSTRAINT_LIST,
                )
            }

        // Note: depending on the implementation of the list type in the Kotlin language, the whitespace before the first and after the last
        // element in the list might be or not be part of the list.
        // In a VALUE_ARGUMENT_LIST the whitespaces before the first and after the last element are included in the VALUE_ARGUMENT_LIST. In
        // the SUPER_TYPE_LIST of a CLASS the whitespaces before the first super type is a child of the CLASS. The whitespace after the last
        // SUPER_TYPE is part of the class only when the class has a body.
        node
            .prevSibling
            ?.elementType
            ?.takeIf { it in LIST_TYPES }
            ?.let { treeParentElementType ->
                visitWhiteSpace(
                    node = node,
                    partOfElementType = treeParentElementType,
                    replaceWithSingeSpace = node.nextSibling?.elementType == CLASS_BODY,
                )
            }
    }

    private fun visitWhiteSpace(
        node: ASTNode,
        partOfElementType: IElementType,
        replaceWithSingeSpace: Boolean = false,
    ) {
        node
            .text
            .split("\n")
            .let { lines ->
                if (lines.size > 2) {
                    emit(node.startOffset + 1, "Unexpected blank line(s) in ${partOfElementType.elementTypeDescription()}") {
                        if (replaceWithSingeSpace) {
                            node.replaceTextWith(" ")
                        } else {
                            node.replaceTextWith("${lines.first()}\n${lines.last()}")
                        }
                    }
                }
            }
    }

    private fun IElementType.elementTypeDescription() =
        toString()
            .lowercase()
            .replace('_', ' ')

    private companion object {
        // The MODIFIER_LIST is handled by separate rule ModifierListSpacingRule
        val LIST_TYPES =
            listOf(
                SUPER_TYPE_LIST,
                TYPE_ARGUMENT_LIST,
                TYPE_CONSTRAINT_LIST,
                TYPE_PARAMETER_LIST,
                VALUE_ARGUMENT_LIST,
                VALUE_PARAMETER_LIST,
            )
    }
}

public val NO_BLANK_LINE_IN_LIST_RULE_ID: RuleId = NoBlankLineInListRule().ruleId
