package io.github.ktlint.core.ruleset.standard.rules

import com.intellij.lang.ASTNode
import com.intellij.psi.impl.source.tree.LeafPsiElement
import com.intellij.psi.impl.source.tree.PsiCommentImpl
import io.github.ktlint.core.rule.engine.core.api.ElementType.EOL_COMMENT
import io.github.ktlint.core.rule.engine.core.api.RuleId
import io.github.ktlint.core.rule.engine.core.api.SinceKtlint
import io.github.ktlint.core.rule.engine.core.api.isWhiteSpace
import io.github.ktlint.core.rule.engine.core.api.prevLeaf
import io.github.ktlint.core.rule.engine.core.api.remove
import io.github.ktlint.core.rule.engine.core.api.upsertWhitespaceBeforeMe
import io.github.ktlint.core.ruleset.standard.StandardRule

@SinceKtlint("0.23", SinceKtlint.Status.STABLE)
public class CommentSpacingRule : StandardRule("comment-spacing") {
    override fun beforeVisitChildNodes(node: ASTNode) {
        if (node.elementType == EOL_COMMENT) {
            val prevLeaf = node.prevLeaf
            if (!prevLeaf.isWhiteSpace && prevLeaf is LeafPsiElement) {
                emit(node.startOffset, "Missing space before //") {
                    node.upsertWhitespaceBeforeMe(" ")
                }
            }
            val text = node.text
            if (text.isMissingSpaceAfterDoubleSlash()) {
                emit(node.startOffset, "Missing space after //") {
                    // Simply replacing the text of node can result in a nullpointer exception in a rule that run after this rule as the
                    // reference to the prev leaf, and the parent node are lost
                    val newEolComment = PsiCommentImpl(EOL_COMMENT, "// ${text.removePrefix("//")}")
                    (node as LeafPsiElement).rawInsertBeforeMe(newEolComment)
                    node.remove()
                }
            }
        }
    }

    private fun String.isMissingSpaceAfterDoubleSlash() =
        length != 2 &&
            !startsWith("// ") &&
            !startsWith("//noinspection") &&
            !startsWith("//region") &&
            !startsWith("//endregion") &&
            !startsWith("//language=")
}

public val COMMENT_SPACING_RULE_ID: RuleId = CommentSpacingRule().ruleId
