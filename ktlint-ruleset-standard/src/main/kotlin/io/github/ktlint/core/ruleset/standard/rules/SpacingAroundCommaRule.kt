package io.github.ktlint.core.ruleset.standard.rules

import com.intellij.lang.ASTNode
import com.intellij.psi.impl.source.tree.LeafPsiElement
import com.intellij.psi.tree.TokenSet
import io.github.ktlint.core.rule.engine.core.api.ElementType.GT
import io.github.ktlint.core.rule.engine.core.api.ElementType.RBRACKET
import io.github.ktlint.core.rule.engine.core.api.ElementType.RPAR
import io.github.ktlint.core.rule.engine.core.api.RuleId
import io.github.ktlint.core.rule.engine.core.api.SinceKtlint
import io.github.ktlint.core.rule.engine.core.api.SinceKtlint.Status.STABLE
import io.github.ktlint.core.rule.engine.core.api.isPartOfComment
import io.github.ktlint.core.rule.engine.core.api.isPartOfString
import io.github.ktlint.core.rule.engine.core.api.isWhiteSpace
import io.github.ktlint.core.rule.engine.core.api.isWhiteSpaceWithNewline
import io.github.ktlint.core.rule.engine.core.api.nextLeaf
import io.github.ktlint.core.rule.engine.core.api.nextSibling
import io.github.ktlint.core.rule.engine.core.api.parent
import io.github.ktlint.core.rule.engine.core.api.prevCodeLeaf
import io.github.ktlint.core.rule.engine.core.api.prevLeaf
import io.github.ktlint.core.rule.engine.core.api.remove
import io.github.ktlint.core.rule.engine.core.api.upsertWhitespaceAfterMe
import io.github.ktlint.core.ruleset.standard.StandardRule

@SinceKtlint("0.1", STABLE)
public class SpacingAroundCommaRule : StandardRule("comma-spacing") {
    private val rTokenSet = TokenSet.create(RPAR, RBRACKET, GT)

    override fun beforeVisitChildNodes(node: ASTNode) {
        if (node is LeafPsiElement && node.textMatches(",") && !node.isPartOfString) {
            node
                .prevLeaf
                .takeIf { it.isWhiteSpace }
                ?.let { prevLeaf ->
                    emit(prevLeaf.startOffset, "Unexpected spacing before \"${node.text}\"") {
                        val isPrecededByComment =
                            prevLeaf
                                .prevLeaf { !it.isWhiteSpace }
                                ?.isPartOfComment
                                ?: false
                        if (isPrecededByComment && prevLeaf.isWhiteSpaceWithNewline) {
                            // If comma is on new line and preceded by a comment, it should be moved before this comment
                            // https://github.com/ktlint/ktlint/issues/367
                            val previousStatement = node.prevCodeLeaf!!
                            previousStatement.parent?.addChild(node.clone(), previousStatement.nextSibling)
                            node.nextLeaf.takeIf { it.isWhiteSpace }?.remove()
                            node.remove()
                        } else {
                            prevLeaf.remove()
                        }
                    }
                }
            node
                .nextLeaf
                .takeUnless { it.isWhiteSpace }
                ?.takeUnless { it.elementType in rTokenSet }
                ?.let {
                    emit(node.startOffset + 1, "Missing spacing after \"${node.text}\"") { node.upsertWhitespaceAfterMe(" ") }
                }
        }
    }
}

public val SPACING_AROUND_COMMA_RULE_ID: RuleId = SpacingAroundCommaRule().ruleId
