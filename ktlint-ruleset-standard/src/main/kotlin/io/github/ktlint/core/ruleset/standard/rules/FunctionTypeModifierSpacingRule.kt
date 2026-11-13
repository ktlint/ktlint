package io.github.ktlint.core.ruleset.standard.rules

import com.intellij.lang.ASTNode
import io.github.ktlint.core.rule.engine.core.api.ElementType.FUNCTION_TYPE
import io.github.ktlint.core.rule.engine.core.api.ElementType.MODIFIER_LIST
import io.github.ktlint.core.rule.engine.core.api.RuleId
import io.github.ktlint.core.rule.engine.core.api.SinceKtlint
import io.github.ktlint.core.rule.engine.core.api.SinceKtlint.Status.EXPERIMENTAL
import io.github.ktlint.core.rule.engine.core.api.SinceKtlint.Status.STABLE
import io.github.ktlint.core.rule.engine.core.api.isWhiteSpace
import io.github.ktlint.core.rule.engine.core.api.nextCodeSibling
import io.github.ktlint.core.rule.engine.core.api.prevSibling
import io.github.ktlint.core.rule.engine.core.api.upsertWhitespaceBeforeMe
import io.github.ktlint.core.ruleset.standard.StandardRule

/**
 * Lints and formats a single space between the modifier list and the function type
 */
@SinceKtlint("1.0", EXPERIMENTAL)
@SinceKtlint("1.3", STABLE)
public class FunctionTypeModifierSpacingRule : StandardRule("function-type-modifier-spacing") {
    override fun beforeVisitChildNodes(node: ASTNode) {
        node
            .takeIf { it.elementType == MODIFIER_LIST }
            ?.nextCodeSibling
            ?.takeIf { it.elementType == FUNCTION_TYPE }
            ?.takeUnless { it.isPrecededBySingleSpace() }
            ?.let { functionTypeNode ->
                emit(functionTypeNode.startOffset, "Expected a single space between the modifier list and the function type") {
                    functionTypeNode.upsertWhitespaceBeforeMe(" ")
                }
            }
    }

    private fun ASTNode.isPrecededBySingleSpace(): Boolean =
        prevSibling
            ?.let { it.isWhiteSpace && it.text == " " }
            ?: false
}

public val FUNCTION_TYPE_MODIFIER_SPACING_RULE: RuleId = FunctionTypeModifierSpacingRule().ruleId
