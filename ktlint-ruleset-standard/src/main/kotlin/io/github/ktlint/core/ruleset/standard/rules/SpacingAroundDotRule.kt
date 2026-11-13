package io.github.ktlint.core.ruleset.standard.rules

import com.intellij.lang.ASTNode
import com.intellij.psi.impl.source.tree.LeafPsiElement
import io.github.ktlint.core.rule.engine.core.api.RuleId
import io.github.ktlint.core.rule.engine.core.api.SinceKtlint
import io.github.ktlint.core.rule.engine.core.api.SinceKtlint.Status.STABLE
import io.github.ktlint.core.rule.engine.core.api.isCode
import io.github.ktlint.core.rule.engine.core.api.isWhiteSpace
import io.github.ktlint.core.rule.engine.core.api.isWhiteSpaceWithoutNewline
import io.github.ktlint.core.rule.engine.core.api.nextLeaf
import io.github.ktlint.core.rule.engine.core.api.prevLeaf
import io.github.ktlint.core.rule.engine.core.api.remove
import io.github.ktlint.core.ruleset.standard.StandardRule

@SinceKtlint("0.30", STABLE)
public class SpacingAroundDotRule : StandardRule("dot-spacing") {
    override fun beforeVisitChildNodes(node: ASTNode) {
        if (node is LeafPsiElement && node.isCode && node.textMatches(".")) {
            node
                .prevLeaf
                .takeIf { it.isWhiteSpaceWithoutNewline }
                ?.let { prevLeaf ->
                    emit(prevLeaf.startOffset, "Unexpected spacing before \"${node.text}\"") { prevLeaf.remove() }
                }
            node.nextLeaf
                .takeIf { it.isWhiteSpace }
                ?.let { nextLeaf ->
                    emit(nextLeaf.startOffset, "Unexpected spacing after \"${node.text}\"") { nextLeaf.remove() }
                }
        }
    }
}

public val SPACING_AROUND_DOT_RULE_ID: RuleId = SpacingAroundDotRule().ruleId
