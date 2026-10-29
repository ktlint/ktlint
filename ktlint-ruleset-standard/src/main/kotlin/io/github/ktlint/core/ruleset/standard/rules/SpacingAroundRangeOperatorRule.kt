package io.github.ktlint.core.ruleset.standard.rules

import com.intellij.lang.ASTNode
import io.github.ktlint.core.rule.engine.core.api.ElementType.RANGE
import io.github.ktlint.core.rule.engine.core.api.ElementType.RANGE_UNTIL
import io.github.ktlint.core.rule.engine.core.api.RuleId
import io.github.ktlint.core.rule.engine.core.api.SinceKtlint
import io.github.ktlint.core.rule.engine.core.api.SinceKtlint.Status.STABLE
import io.github.ktlint.core.rule.engine.core.api.isWhiteSpace
import io.github.ktlint.core.rule.engine.core.api.nextLeaf
import io.github.ktlint.core.rule.engine.core.api.prevLeaf
import io.github.ktlint.core.rule.engine.core.api.remove
import io.github.ktlint.core.ruleset.standard.StandardRule
import org.jetbrains.kotlin.lexer.KtSingleValueToken

@SinceKtlint("0.13", STABLE)
public class SpacingAroundRangeOperatorRule : StandardRule("range-spacing") {
    override fun beforeVisitChildNodes(node: ASTNode) {
        if (node.elementType == RANGE || node.elementType == RANGE_UNTIL) {
            val prevLeaf = node.prevLeaf
            val nextLeaf = node.nextLeaf
            when {
                prevLeaf.isWhiteSpace && nextLeaf.isWhiteSpace -> {
                    emit(node.startOffset, "Unexpected spacing around \"${node.elementTypeDescription()}\"") {
                        prevLeaf?.remove()
                        nextLeaf?.remove()
                    }
                }

                prevLeaf != null && prevLeaf.isWhiteSpace -> {
                    emit(prevLeaf.startOffset, "Unexpected spacing before \"${node.elementTypeDescription()}\"") { prevLeaf.remove() }
                }

                nextLeaf != null && nextLeaf.isWhiteSpace -> {
                    emit(nextLeaf.startOffset, "Unexpected spacing after \"${node.elementTypeDescription()}\"") { nextLeaf.remove() }
                }
            }
        }
    }

    private fun ASTNode.elementTypeDescription() = (elementType as? KtSingleValueToken)?.value ?: elementType
}

public val SPACING_AROUND_RANGE_OPERATOR_RULE_ID: RuleId = SpacingAroundRangeOperatorRule().ruleId
