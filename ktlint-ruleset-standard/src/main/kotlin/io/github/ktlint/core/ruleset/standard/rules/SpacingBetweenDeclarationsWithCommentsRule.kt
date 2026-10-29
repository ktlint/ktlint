package io.github.ktlint.core.ruleset.standard.rules

import com.intellij.lang.ASTNode
import io.github.ktlint.core.rule.engine.core.api.RuleId
import io.github.ktlint.core.rule.engine.core.api.SinceKtlint
import io.github.ktlint.core.rule.engine.core.api.SinceKtlint.Status.EXPERIMENTAL
import io.github.ktlint.core.rule.engine.core.api.SinceKtlint.Status.STABLE
import io.github.ktlint.core.rule.engine.core.api.TokenSets
import io.github.ktlint.core.rule.engine.core.api.isDeclaration
import io.github.ktlint.core.rule.engine.core.api.isWhiteSpace
import io.github.ktlint.core.rule.engine.core.api.parent
import io.github.ktlint.core.rule.engine.core.api.prevCodeSibling
import io.github.ktlint.core.rule.engine.core.api.prevLeaf
import io.github.ktlint.core.rule.engine.core.api.prevSibling
import io.github.ktlint.core.rule.engine.core.api.replaceTextWith
import io.github.ktlint.core.ruleset.standard.StandardRule

/**
 * @see https://youtrack.jetbrains.com/issue/KT-35088
 */
@SinceKtlint("0.37", EXPERIMENTAL)
@SinceKtlint("0.46", STABLE)
public class SpacingBetweenDeclarationsWithCommentsRule : StandardRule("spacing-between-declarations-with-comments") {
    override fun beforeVisitChildNodes(node: ASTNode) {
        node
            .takeIf { it.elementType in TokenSets.COMMENTS }
            ?.takeUnless { it.isTailComment() }
            ?.parent
            ?.takeIf { it.isDeclaration }
            ?.takeIf { it.prevCodeSibling.isDeclaration }
            ?.visitCommentedDeclaration()
    }

    private fun ASTNode.isTailComment() = startOffset > parent!!.startOffset

    private fun ASTNode.visitCommentedDeclaration() {
        prevSibling
            ?.takeUnless { it.isBlankLine() }
            ?.let { prevSibling ->
                emit(startOffset, "Declarations and declarations with comments should have an empty space between.") {
                    val indent =
                        prevLeaf
                            ?.text
                            ?.trim('\n')
                            ?: ""
                    prevSibling.replaceTextWith("\n\n$indent")
                }
            }
    }

    private fun ASTNode.isBlankLine() = isWhiteSpace && text.startsWith("\n\n")
}

public val SPACING_BETWEEN_DECLARATIONS_WITH_COMMENTS_RULE_ID: RuleId = SpacingBetweenDeclarationsWithCommentsRule().ruleId
