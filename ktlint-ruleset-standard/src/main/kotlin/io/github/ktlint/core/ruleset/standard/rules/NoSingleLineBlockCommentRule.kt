package io.github.ktlint.core.ruleset.standard.rules

import com.intellij.lang.ASTNode
import com.intellij.psi.impl.source.tree.LeafPsiElement
import com.intellij.psi.impl.source.tree.PsiCommentImpl
import io.github.ktlint.core.rule.engine.core.api.ElementType.BLOCK_COMMENT
import io.github.ktlint.core.rule.engine.core.api.ElementType.EOL_COMMENT
import io.github.ktlint.core.rule.engine.core.api.RuleId
import io.github.ktlint.core.rule.engine.core.api.RuleV2
import io.github.ktlint.core.rule.engine.core.api.SinceKtlint
import io.github.ktlint.core.rule.engine.core.api.SinceKtlint.Status.EXPERIMENTAL
import io.github.ktlint.core.rule.engine.core.api.SinceKtlint.Status.STABLE
import io.github.ktlint.core.rule.engine.core.api.editorconfig.INDENT_SIZE_PROPERTY
import io.github.ktlint.core.rule.engine.core.api.editorconfig.INDENT_STYLE_PROPERTY
import io.github.ktlint.core.rule.engine.core.api.firstChildLeafOrSelf
import io.github.ktlint.core.rule.engine.core.api.isWhiteSpaceWithNewline
import io.github.ktlint.core.rule.engine.core.api.isWhiteSpaceWithoutNewline
import io.github.ktlint.core.rule.engine.core.api.lastChildLeafOrSelf
import io.github.ktlint.core.rule.engine.core.api.nextLeaf
import io.github.ktlint.core.rule.engine.core.api.prevLeaf
import io.github.ktlint.core.rule.engine.core.api.upsertWhitespaceBeforeMe
import io.github.ktlint.core.ruleset.standard.StandardRule
import org.jetbrains.kotlin.psi.psiUtil.leaves

/**
 * A block comment following another element on the same line is replaced with an EOL comment, if possible.
 */
@SinceKtlint("0.49", EXPERIMENTAL)
@SinceKtlint("1.0", STABLE)
public class NoSingleLineBlockCommentRule :
    StandardRule(
        id = "no-single-line-block-comment",
        usesEditorConfigProperties =
            setOf(
                INDENT_SIZE_PROPERTY,
                INDENT_STYLE_PROPERTY,
            ),
    ),
    RuleV2.OfficialCodeStyle {
    override fun beforeVisitChildNodes(node: ASTNode) {
        if (node.elementType == BLOCK_COMMENT) {
            val afterBlockComment =
                node
                    .leaves()
                    .takeWhile { it.isWhiteSpaceWithoutNewline }
                    .firstOrNull()
                    ?: node.lastChildLeafOrSelf

            if (!node.textContains('\n') &&
                afterBlockComment.nextLeaf.isWhitespaceWithNewlineOrNull()
            ) {
                emit(node.startOffset, "Replace the block comment with an EOL comment") {
                    val beforeBlockComment =
                        node
                            .leaves(false)
                            .takeWhile { it.isWhiteSpaceWithoutNewline }
                            .firstOrNull()
                            ?: node.firstChildLeafOrSelf
                    beforeBlockComment
                        .prevLeaf
                        .takeIf { !it.isWhitespaceWithNewlineOrNull() }
                        ?.let {
                            node.upsertWhitespaceBeforeMe(" ")
                        }
                    node.replaceWithEndOfLineComment()
                }
            }
        }
    }

    private fun ASTNode.replaceWithEndOfLineComment() {
        val content = text.removeSurrounding("/*", "*/").trim()
        val eolComment = PsiCommentImpl(EOL_COMMENT, "// $content")
        (this as LeafPsiElement).rawInsertBeforeMe(eolComment)
        rawRemove()
    }

    private fun ASTNode?.isWhitespaceWithNewlineOrNull() = this == null || this.isWhiteSpaceWithNewline
}

public val NO_SINGLE_LINE_BLOCK_COMMENT_RULE_ID: RuleId = NoSingleLineBlockCommentRule().ruleId
