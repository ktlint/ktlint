package io.github.ktlint.core.ruleset.standard.rules

import com.intellij.lang.ASTNode
import io.github.ktlint.core.rule.engine.core.api.ElementType.ELSE_KEYWORD
import io.github.ktlint.core.rule.engine.core.api.ElementType.IF_KEYWORD
import io.github.ktlint.core.rule.engine.core.api.ElementType.LBRACE
import io.github.ktlint.core.rule.engine.core.api.RuleId
import io.github.ktlint.core.rule.engine.core.api.SinceKtlint
import io.github.ktlint.core.rule.engine.core.api.SinceKtlint.Status.STABLE
import io.github.ktlint.core.rule.engine.core.api.isWhiteSpaceWithNewline
import io.github.ktlint.core.rule.engine.core.api.nextLeaf
import io.github.ktlint.core.rule.engine.core.api.prevLeaf
import io.github.ktlint.core.rule.engine.core.api.replaceTextWith
import io.github.ktlint.core.ruleset.standard.StandardRule

@SinceKtlint("0.15", STABLE)
public class NoLineBreakAfterElseRule : StandardRule("no-line-break-after-else") {
    override fun beforeVisitChildNodes(node: ASTNode) {
        if (node.isWhiteSpaceWithNewline &&
            node.prevLeaf?.elementType == ELSE_KEYWORD &&
            node.isFollowedByIfOrBlock()
        ) {
            emit(node.startOffset + 1, "Unexpected line break after \"else\"") { node.replaceTextWith(" ") }
        }
    }

    private fun ASTNode.isFollowedByIfOrBlock() = nextLeaf?.elementType.let { it == IF_KEYWORD || it == LBRACE }
}

public val NO_LINE_BREAK_AFTER_ELSE_RULE_ID: RuleId = NoLineBreakAfterElseRule().ruleId
