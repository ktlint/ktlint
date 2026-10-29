package io.github.ktlint.core.ruleset.standard.rules

import com.intellij.lang.ASTNode
import io.github.ktlint.core.rule.engine.core.api.ElementType.COLON
import io.github.ktlint.core.rule.engine.core.api.ElementType.FUN
import io.github.ktlint.core.rule.engine.core.api.ElementType.LBRACE
import io.github.ktlint.core.rule.engine.core.api.ElementType.TYPE_REFERENCE
import io.github.ktlint.core.rule.engine.core.api.RuleId
import io.github.ktlint.core.rule.engine.core.api.SinceKtlint
import io.github.ktlint.core.rule.engine.core.api.SinceKtlint.Status.STABLE
import io.github.ktlint.core.rule.engine.core.api.isWhiteSpace
import io.github.ktlint.core.rule.engine.core.api.nextCodeSibling
import io.github.ktlint.core.rule.engine.core.api.nextLeaf
import io.github.ktlint.core.rule.engine.core.api.parent
import io.github.ktlint.core.rule.engine.core.api.prevLeaf
import io.github.ktlint.core.rule.engine.core.api.remove
import io.github.ktlint.core.ruleset.standard.StandardRule

@SinceKtlint("0.7", STABLE)
public class NoUnitReturnRule : StandardRule("no-unit-return") {
    override fun beforeVisitChildNodes(node: ASTNode) {
        if (node.isUnitReturnTypeInFunWithBlockBody()) {
            emit(node.startOffset, "Unnecessary \"Unit\" return type") {
                node
                    .parent
                    ?.findChildByType(COLON)
                    ?.let { colonNode ->
                        // Remove space after colon when not followed by Unit node
                        node
                            .nextLeaf
                            .takeIf { it.isWhiteSpace }
                            ?.takeIf { it.nextLeaf != node }
                            ?.remove()
                        colonNode.remove()
                    }
                node
                    .prevLeaf
                    .takeIf { it.isWhiteSpace }
                    ?.takeIf { it.prevLeaf?.elementType != COLON }
                    ?.remove()
                node.remove()
            }
        }
    }

    private fun ASTNode.isUnitReturnTypeInFunWithBlockBody() =
        elementType == TYPE_REFERENCE &&
            text == "Unit" &&
            parent?.elementType == FUN &&
            nextCodeSibling?.firstChildNode?.elementType == LBRACE
}

public val NO_UNIT_RETURN_RULE_ID: RuleId = NoUnitReturnRule().ruleId
