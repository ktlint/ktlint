package io.github.ktlint.core.ruleset.standard.rules

import com.intellij.lang.ASTNode
import com.intellij.psi.tree.TokenSet
import io.github.ktlint.core.rule.engine.core.api.ElementType.ANDAND
import io.github.ktlint.core.rule.engine.core.api.ElementType.BINARY_EXPRESSION
import io.github.ktlint.core.rule.engine.core.api.ElementType.OPERATION_REFERENCE
import io.github.ktlint.core.rule.engine.core.api.ElementType.OROR
import io.github.ktlint.core.rule.engine.core.api.RuleId
import io.github.ktlint.core.rule.engine.core.api.SinceKtlint
import io.github.ktlint.core.rule.engine.core.api.SinceKtlint.Status.EXPERIMENTAL
import io.github.ktlint.core.rule.engine.core.api.SinceKtlint.Status.STABLE
import io.github.ktlint.core.rule.engine.core.api.parent
import io.github.ktlint.core.ruleset.standard.StandardRule

/**
 * Conditions should not use a both '&&' and '||' operators between operators at the same level. By using parenthesis the expression is to
 * be clarified.
 */
@SinceKtlint("1.1", EXPERIMENTAL)
@SinceKtlint("1.8", STABLE)
public class MixedConditionOperatorsRule : StandardRule("mixed-condition-operators") {
    override fun beforeVisitChildNodes(node: ASTNode) {
        node
            .takeIf { it.isLogicalBinaryExpression() }
            ?.takeIf { it.isPartOfExpressionUsingDifferentLogicalOperators() }
            ?.visitLogicalExpression()
    }

    private fun ASTNode.isLogicalBinaryExpression() =
        takeIf { elementType == BINARY_EXPRESSION }
            ?.findChildByType(OPERATION_REFERENCE)
            ?.let { it.firstChildNode.elementType in logicalOperators }
            ?: false

    private fun ASTNode.visitLogicalExpression() {
        parent { it.elementType == BINARY_EXPRESSION && it.parent?.elementType != BINARY_EXPRESSION }
            ?.let { rootBinaryExpression ->
                emit(
                    rootBinaryExpression.startOffset,
                    "A condition with mixed usage of '&&' and '||' is hard to read. Use parenthesis to clarify the (sub)condition.",
                )
            }
    }

    private fun ASTNode.anyParentBinaryExpression(predicate: (ASTNode) -> Boolean): Boolean {
        var current: ASTNode? = this
        while (current?.elementType == BINARY_EXPRESSION) {
            if (predicate(current)) {
                return true
            }
            current = current.parent
        }
        return false
    }

    private fun ASTNode.isPartOfExpressionUsingDifferentLogicalOperators(): Boolean {
        val firstLogicalOperator = findOperationElementTypeOrNull() ?: return false

        return anyParentBinaryExpression { parent ->
            parent
                .findOperationElementTypeOrNull()
                ?.let { it != firstLogicalOperator }
                ?: false
        }
    }

    private fun ASTNode.findOperationElementTypeOrNull() =
        findChildByType(OPERATION_REFERENCE)
            ?.firstChildNode
            ?.elementType
            ?.takeIf { it in (logicalOperators) }

    private companion object {
        val logicalOperators = TokenSet.create(OROR, ANDAND)
    }
}

public val MIXED_CONDITION_OPERATORS_RULE_ID: RuleId = MixedConditionOperatorsRule().ruleId
