package io.github.ktlint.core.ruleset.standard.rules

import com.intellij.lang.ASTNode
import io.github.ktlint.core.rule.engine.core.api.ElementType.FUN
import io.github.ktlint.core.rule.engine.core.api.ElementType.NULLABLE_TYPE
import io.github.ktlint.core.rule.engine.core.api.ElementType.TYPE_REFERENCE
import io.github.ktlint.core.rule.engine.core.api.ElementType.VALUE_PARAMETER_LIST
import io.github.ktlint.core.rule.engine.core.api.RuleId
import io.github.ktlint.core.rule.engine.core.api.SinceKtlint
import io.github.ktlint.core.rule.engine.core.api.SinceKtlint.Status.EXPERIMENTAL
import io.github.ktlint.core.rule.engine.core.api.SinceKtlint.Status.STABLE
import io.github.ktlint.core.rule.engine.core.api.isWhiteSpace
import io.github.ktlint.core.rule.engine.core.api.nextSibling
import io.github.ktlint.core.rule.engine.core.api.remove
import io.github.ktlint.core.ruleset.standard.StandardRule

@SinceKtlint("0.45", EXPERIMENTAL)
@SinceKtlint("1.0", STABLE)
public class FunctionTypeReferenceSpacingRule : StandardRule("function-type-reference-spacing") {
    override fun beforeVisitChildNodes(node: ASTNode) {
        if (node.elementType == FUN) {
            node
                .findFunctionReceiverTypeReference()
                ?.let { typeReference ->
                    typeReference
                        .firstChildNode
                        .takeIf { it.elementType == NULLABLE_TYPE }
                        ?.firstChildNode
                        ?.visitNodesUntilStartOfValueParameterList()

                    if (typeReference.elementType != NULLABLE_TYPE) {
                        typeReference.visitNodesUntilStartOfValueParameterList()
                    }
                }
        }
    }

    private fun ASTNode.findFunctionReceiverTypeReference(): ASTNode? {
        require(elementType == FUN)
        var currentNode: ASTNode? = firstChildNode
        while (currentNode != null && currentNode.elementType != VALUE_PARAMETER_LIST) {
            if (currentNode.elementType == TYPE_REFERENCE) {
                return currentNode
            }
            currentNode = currentNode.nextSibling
        }
        return null
    }

    private fun ASTNode.visitNodesUntilStartOfValueParameterList() {
        var currentNode: ASTNode? = this
        while (currentNode != null && currentNode.elementType != VALUE_PARAMETER_LIST) {
            val nextNode = currentNode.nextSibling
            currentNode.removeIfNonEmptyWhiteSpace()
            currentNode = nextNode
        }
    }

    private fun ASTNode.removeIfNonEmptyWhiteSpace() {
        if (isWhiteSpace && text.isNotEmpty()) {
            emit(startOffset, "Unexpected whitespace") { remove() }
        }
    }
}

public val FUNCTION_TYPE_REFERENCE_SPACING_RULE_ID: RuleId = FunctionTypeReferenceSpacingRule().ruleId
