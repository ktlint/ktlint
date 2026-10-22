package io.github.ktlint.core.ruleset.standard.rules

import com.intellij.lang.ASTNode
import io.github.ktlint.core.rule.engine.core.api.ElementType.CLASS_BODY
import io.github.ktlint.core.rule.engine.core.api.ElementType.LBRACE
import io.github.ktlint.core.rule.engine.core.api.ElementType.OBJECT_LITERAL
import io.github.ktlint.core.rule.engine.core.api.ElementType.RBRACE
import io.github.ktlint.core.rule.engine.core.api.RuleId
import io.github.ktlint.core.rule.engine.core.api.SinceKtlint
import io.github.ktlint.core.rule.engine.core.api.SinceKtlint.Status.STABLE
import io.github.ktlint.core.rule.engine.core.api.children
import io.github.ktlint.core.rule.engine.core.api.isPartOf
import io.github.ktlint.core.rule.engine.core.api.isWhiteSpace
import io.github.ktlint.core.rule.engine.core.api.nextLeaf
import io.github.ktlint.core.rule.engine.core.api.parent
import io.github.ktlint.core.rule.engine.core.api.prevSibling
import io.github.ktlint.core.rule.engine.core.api.remove
import io.github.ktlint.core.ruleset.standard.StandardRule

@SinceKtlint("0.9", STABLE)
public class NoEmptyClassBodyRule : StandardRule("no-empty-class-body") {
    override fun beforeVisitChildNodes(node: ASTNode) {
        if (node.isRemovableEmptyClassBody()) {
            emit(node.startOffset, "Unnecessary block (\"{}\")") {
                // remove space between declaration and block
                node.prevSibling.takeIf { it.isWhiteSpace }?.remove()
                // remove block
                node.remove()
            }
        }
    }

    private fun ASTNode.isRemovableEmptyClassBody() =
        elementType == CLASS_BODY &&
            isEmptyBlockBody() &&
            !isPartOf(OBJECT_LITERAL) &&
            isNotCompanion()

    private fun ASTNode.isEmptyBlockBody(): Boolean =
        firstChildNode != null &&
            firstChildNode.elementType == LBRACE &&
            firstChildNode.nextLeaf { !it.isWhiteSpace }?.elementType == RBRACE

    private fun ASTNode.isNotCompanion(): Boolean =
        parent
            ?.firstChildNode
            ?.children
            .orEmpty()
            .none { it.text == "companion" }
}

public val NO_EMPTY_CLASS_BODY_RULE_ID: RuleId = NoEmptyClassBodyRule().ruleId
