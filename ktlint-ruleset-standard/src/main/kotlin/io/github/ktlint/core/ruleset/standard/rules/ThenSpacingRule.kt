package io.github.ktlint.core.ruleset.standard.rules

import com.intellij.lang.ASTNode
import io.github.ktlint.core.rule.engine.core.api.ElementType.THEN
import io.github.ktlint.core.rule.engine.core.api.RuleId
import io.github.ktlint.core.rule.engine.core.api.SinceKtlint
import io.github.ktlint.core.rule.engine.core.api.SinceKtlint.Status.STABLE
import io.github.ktlint.core.rule.engine.core.api.endOffset
import io.github.ktlint.core.rule.engine.core.api.isWhiteSpace
import io.github.ktlint.core.rule.engine.core.api.lastChildLeafOrSelf
import io.github.ktlint.core.rule.engine.core.api.nextSibling
import io.github.ktlint.core.rule.engine.core.api.prevLeaf
import io.github.ktlint.core.rule.engine.core.api.prevSibling
import io.github.ktlint.core.rule.engine.core.api.upsertWhitespaceAfterMe
import io.github.ktlint.core.ruleset.standard.StandardRule

/**
 * Checks spacing around then block in an if-statement
 */
@SinceKtlint("1.8", STABLE)
public class ThenSpacingRule : StandardRule(id = "then-spacing") {
    override fun beforeVisitChildNodes(node: ASTNode) {
        if (node.elementType == THEN) {
            node.visitThen()
        }
    }

    private fun ASTNode.visitThen() {
        takeUnless { it.prevSibling.isWhiteSpace }
            ?.let {
                emit(startOffset, "Expected a whitespace before 'then' block") { prevLeaf?.upsertWhitespaceAfterMe(" ") }
            }
        takeUnless { it.nextSibling == null || it.nextSibling.isWhiteSpace }
            ?.let {
                lastChildLeafOrSelf
                    .let { lastLeafInThen ->
                        emit(lastLeafInThen.endOffset, "Expected a whitespace after 'then' block") {
                            lastLeafInThen.upsertWhitespaceAfterMe(" ")
                        }
                    }
            }
    }
}

public val THEN_SPACING_RULE_ID: RuleId = ThenSpacingRule().ruleId
