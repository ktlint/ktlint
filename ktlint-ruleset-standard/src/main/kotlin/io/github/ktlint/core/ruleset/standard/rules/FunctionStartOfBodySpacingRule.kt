package io.github.ktlint.core.ruleset.standard.rules

import com.intellij.lang.ASTNode
import io.github.ktlint.core.rule.engine.core.api.ElementType.BLOCK
import io.github.ktlint.core.rule.engine.core.api.ElementType.EQ
import io.github.ktlint.core.rule.engine.core.api.ElementType.FUN
import io.github.ktlint.core.rule.engine.core.api.ElementType.WHITE_SPACE
import io.github.ktlint.core.rule.engine.core.api.RuleId
import io.github.ktlint.core.rule.engine.core.api.SinceKtlint
import io.github.ktlint.core.rule.engine.core.api.SinceKtlint.Status.EXPERIMENTAL
import io.github.ktlint.core.rule.engine.core.api.SinceKtlint.Status.STABLE
import io.github.ktlint.core.rule.engine.core.api.isWhiteSpaceWithNewline
import io.github.ktlint.core.rule.engine.core.api.nextLeaf
import io.github.ktlint.core.rule.engine.core.api.prevLeaf
import io.github.ktlint.core.rule.engine.core.api.upsertWhitespaceAfterMe
import io.github.ktlint.core.rule.engine.core.api.upsertWhitespaceBeforeMe
import io.github.ktlint.core.ruleset.standard.StandardRule

/**
 * Lints and formats the spacing after the fun keyword
 */
@SinceKtlint("0.46", EXPERIMENTAL)
@SinceKtlint("0.49", STABLE)
public class FunctionStartOfBodySpacingRule : StandardRule("function-start-of-body-spacing") {
    override fun beforeVisitChildNodes(node: ASTNode) {
        if (node.elementType == FUN) {
            node
                .findChildByType(EQ)
                ?.let { node.visitFunctionFollowedByBodyExpression() }

            node
                .findChildByType(BLOCK)
                ?.let { node.visitFunctionFollowedByBodyBlock() }
        }
    }

    private fun ASTNode.visitFunctionFollowedByBodyExpression() {
        fixWhiteSpaceBeforeAssignmentOfBodyExpression()
        fixWhiteSpaceBetweenAssignmentAndBodyExpression()
    }

    private fun ASTNode.fixWhiteSpaceBeforeAssignmentOfBodyExpression() {
        findChildByType(EQ)
            ?.let { assignmentExpression ->
                assignmentExpression
                    .prevLeaf
                    ?.takeIf { it.elementType == WHITE_SPACE }
                    .let { whiteSpaceBeforeAssignment ->
                        if (whiteSpaceBeforeAssignment == null) {
                            emit(assignmentExpression.startOffset, "Expected a single white space before assignment of expression body") {
                                assignmentExpression.upsertWhitespaceBeforeMe(" ")
                            }
                        } else if (whiteSpaceBeforeAssignment.text != " ") {
                            emit(whiteSpaceBeforeAssignment.startOffset, "Unexpected whitespace") {
                                assignmentExpression.upsertWhitespaceBeforeMe(" ")
                            }
                        }
                    }
            }
    }

    private fun ASTNode.fixWhiteSpaceBetweenAssignmentAndBodyExpression() {
        findChildByType(EQ)
            ?.let { assignmentExpression ->
                assignmentExpression
                    .nextLeaf
                    ?.takeIf { it.elementType == WHITE_SPACE }
                    .let { whiteSpaceAfterAssignment ->
                        if (whiteSpaceAfterAssignment?.text != " " && !whiteSpaceAfterAssignment.isWhiteSpaceWithNewline) {
                            emit(
                                assignmentExpression.startOffset,
                                "Expected a single white space between assignment and expression body on same line",
                            ) {
                                assignmentExpression.upsertWhitespaceAfterMe(" ")
                            }
                        }
                    }
            }
    }

    private fun ASTNode.visitFunctionFollowedByBodyBlock() {
        findChildByType(BLOCK)
            ?.let { block ->
                block
                    .prevLeaf
                    ?.takeIf { it.elementType == WHITE_SPACE }
                    .let { whiteSpaceBeforeExpressionBlock ->
                        if (whiteSpaceBeforeExpressionBlock?.text != " ") {
                            emit(block.startOffset, "Expected a single white space before start of function body") {
                                block
                                    .firstChildNode
                                    .prevLeaf
                                    ?.upsertWhitespaceAfterMe(" ")
                            }
                        }
                    }
            }
    }
}

public val FUNCTION_START_OF_BODY_SPACING_RULE_ID: RuleId = FunctionStartOfBodySpacingRule().ruleId
