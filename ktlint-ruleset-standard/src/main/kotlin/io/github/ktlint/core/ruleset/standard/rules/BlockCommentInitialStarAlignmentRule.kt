package io.github.ktlint.core.ruleset.standard.rules

import com.intellij.lang.ASTNode
import io.github.ktlint.core.rule.engine.core.api.ElementType.BLOCK_COMMENT
import io.github.ktlint.core.rule.engine.core.api.RuleId
import io.github.ktlint.core.rule.engine.core.api.SinceKtlint
import io.github.ktlint.core.rule.engine.core.api.SinceKtlint.Status.STABLE
import io.github.ktlint.core.rule.engine.core.api.indentWithoutNewlinePrefix
import io.github.ktlint.core.rule.engine.core.api.replaceTextWith
import io.github.ktlint.core.ruleset.standard.StandardRule

/**
 * When present, align the initial star in a block comment.
 */
@SinceKtlint("0.45", STABLE)
public class BlockCommentInitialStarAlignmentRule : StandardRule("block-comment-initial-star-alignment") {
    override fun beforeVisitChildNodes(node: ASTNode) {
        if (node.elementType == BLOCK_COMMENT) {
            val expectedIndentForLineWithInitialStar = node.indentWithoutNewlinePrefix + " *"
            val lines = node.text.split("\n")
            var offset = node.startOffset
            val modifiedLines = mutableListOf<String>()
            var autocorrect = false
            lines.forEach { line ->
                val modifiedLine =
                    CONTINUATION_COMMENT_REGEX
                        .find(line)
                        ?.let { matchResult ->
                            val (prefix, content) = matchResult.destructured
                            if (prefix != expectedIndentForLineWithInitialStar) {
                                emit(offset + prefix.length, "Initial star should align with start of block comment") { autocorrect = true }
                                expectedIndentForLineWithInitialStar + content
                            } else {
                                line
                            }
                        }
                        ?: line
                modifiedLines.add(modifiedLine)
                offset += line.length + 1
            }
            if (autocorrect) {
                val newText = modifiedLines.joinToString(separator = "\n")
                if (node.text != newText) {
                    node.replaceTextWith(newText)
                }
            }
        }
    }

    private companion object {
        val CONTINUATION_COMMENT_REGEX = Regex("^([\t ]+\\*)(.*)$")
    }
}

public val BLOCK_COMMENT_INITIAL_STAR_ALIGNMENT_RULE_ID: RuleId = BlockCommentInitialStarAlignmentRule().ruleId
