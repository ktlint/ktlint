package io.github.ktlint.core.ruleset.standard.rules

import com.intellij.lang.ASTNode
import io.github.ktlint.core.rule.engine.core.api.ElementType.BLOCK_COMMENT
import io.github.ktlint.core.rule.engine.core.api.ElementType.LBRACE
import io.github.ktlint.core.rule.engine.core.api.ElementType.RBRACE
import io.github.ktlint.core.rule.engine.core.api.RuleId
import io.github.ktlint.core.rule.engine.core.api.SinceKtlint
import io.github.ktlint.core.rule.engine.core.api.SinceKtlint.Status.EXPERIMENTAL
import io.github.ktlint.core.rule.engine.core.api.SinceKtlint.Status.STABLE
import io.github.ktlint.core.rule.engine.core.api.editorconfig.INDENT_SIZE_PROPERTY
import io.github.ktlint.core.rule.engine.core.api.editorconfig.INDENT_STYLE_PROPERTY
import io.github.ktlint.core.rule.engine.core.api.firstChildLeafOrSelf
import io.github.ktlint.core.rule.engine.core.api.hasNewLineInClosedRange
import io.github.ktlint.core.rule.engine.core.api.indent
import io.github.ktlint.core.rule.engine.core.api.isWhiteSpaceWithNewline
import io.github.ktlint.core.rule.engine.core.api.isWhiteSpaceWithoutNewline
import io.github.ktlint.core.rule.engine.core.api.lastChildLeafOrSelf
import io.github.ktlint.core.rule.engine.core.api.nextLeaf
import io.github.ktlint.core.rule.engine.core.api.prevLeaf
import io.github.ktlint.core.rule.engine.core.api.upsertWhitespaceBeforeMe
import io.github.ktlint.core.ruleset.standard.StandardRule
import org.jetbrains.kotlin.psi.psiUtil.leaves

/**
 * Checks external wrapping of block comments. Wrapping inside the comment is not altered.
 */
@SinceKtlint("0.45", EXPERIMENTAL)
@SinceKtlint("0.49", STABLE)
public class CommentWrappingRule :
    StandardRule(
        id = "comment-wrapping",
        usesEditorConfigProperties =
            setOf(
                INDENT_SIZE_PROPERTY,
                INDENT_STYLE_PROPERTY,
            ),
    ) {
    override fun beforeVisitChildNodes(node: ASTNode) {
        if (node.elementType == BLOCK_COMMENT) {
            val beforeBlockComment =
                node
                    .leaves(false)
                    .takeWhile { it.isWhiteSpaceWithoutNewline }
                    .firstOrNull()
                    ?: node.firstChildLeafOrSelf
            val afterBlockComment =
                node
                    .leaves()
                    .takeWhile { it.isWhiteSpaceWithoutNewline }
                    .firstOrNull()
                    ?: node.lastChildLeafOrSelf

            if (!beforeBlockComment.prevLeaf.isWhitespaceWithNewlineOrNull() &&
                !afterBlockComment.nextLeaf.isWhitespaceWithNewlineOrNull()
            ) {
                if (hasNewLineInClosedRange(beforeBlockComment, afterBlockComment)) {
                    // Do not try to fix constructs like below:
                    //    val foo = "foo" /*
                    //    some comment
                    //    */ val bar = "bar"
                    emit(
                        node.startOffset,
                        "A block comment starting on same line as another element and ending on another line before another element is " +
                            "disallowed",
                    )
                } else if (beforeBlockComment.prevLeaf?.elementType == LBRACE &&
                    afterBlockComment.nextLeaf?.elementType == RBRACE
                ) {
                    // Allow single line blocks containing a block comment
                    //   val foo = { /* no-op */ }
                    return
                } else {
                    // Do not try to fix constructs like below:
                    //    val foo /* some comment */ = "foo"
                    emit(node.startOffset, "A block comment in between other elements on the same line is disallowed")
                }
                return
            }

            beforeBlockComment
                .prevLeaf
                .takeIf { !it.isWhitespaceWithNewlineOrNull() }
                ?.let {
                    if (node.textContains('\n')) {
                        // It can not be autocorrected as it might depend on the situation and code style what is preferred.
                        emit(node.startOffset, "A block comment after any other element on the same line must be separated by a new line")
                    }
                }

            afterBlockComment
                .nextLeaf
                .takeIf { !it.isWhitespaceWithNewlineOrNull() }
                ?.let { nextLeaf ->
                    emit(nextLeaf.startOffset, "A block comment may not be followed by any other element on that same line") {
                        nextLeaf.upsertWhitespaceBeforeMe(node.indent)
                    }
                }
        }
    }

    private fun ASTNode?.isWhitespaceWithNewlineOrNull() = this == null || this.isWhiteSpaceWithNewline
}

public val COMMENT_WRAPPING_RULE_ID: RuleId = CommentWrappingRule().ruleId
