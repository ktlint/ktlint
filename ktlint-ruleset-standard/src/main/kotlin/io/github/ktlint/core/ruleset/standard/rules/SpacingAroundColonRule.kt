package io.github.ktlint.core.ruleset.standard.rules

import com.intellij.lang.ASTNode
import io.github.ktlint.core.rule.engine.core.api.ElementType.ANNOTATION
import io.github.ktlint.core.rule.engine.core.api.ElementType.ANNOTATION_ENTRY
import io.github.ktlint.core.rule.engine.core.api.ElementType.BLOCK
import io.github.ktlint.core.rule.engine.core.api.ElementType.CLASS
import io.github.ktlint.core.rule.engine.core.api.ElementType.COLON
import io.github.ktlint.core.rule.engine.core.api.ElementType.EQ
import io.github.ktlint.core.rule.engine.core.api.ElementType.FUN
import io.github.ktlint.core.rule.engine.core.api.ElementType.OBJECT_DECLARATION
import io.github.ktlint.core.rule.engine.core.api.ElementType.PROPERTY
import io.github.ktlint.core.rule.engine.core.api.ElementType.SECONDARY_CONSTRUCTOR
import io.github.ktlint.core.rule.engine.core.api.ElementType.TYPE_CONSTRAINT
import io.github.ktlint.core.rule.engine.core.api.ElementType.TYPE_PARAMETER_LIST
import io.github.ktlint.core.rule.engine.core.api.RuleId
import io.github.ktlint.core.rule.engine.core.api.SinceKtlint
import io.github.ktlint.core.rule.engine.core.api.SinceKtlint.Status.STABLE
import io.github.ktlint.core.rule.engine.core.api.isCode
import io.github.ktlint.core.rule.engine.core.api.isPartOfComment
import io.github.ktlint.core.rule.engine.core.api.isWhiteSpace
import io.github.ktlint.core.rule.engine.core.api.isWhiteSpaceWithNewline
import io.github.ktlint.core.rule.engine.core.api.isWhiteSpaceWithoutNewline
import io.github.ktlint.core.rule.engine.core.api.nextLeaf
import io.github.ktlint.core.rule.engine.core.api.nextSibling
import io.github.ktlint.core.rule.engine.core.api.parent
import io.github.ktlint.core.rule.engine.core.api.prevLeaf
import io.github.ktlint.core.rule.engine.core.api.prevSibling
import io.github.ktlint.core.rule.engine.core.api.remove
import io.github.ktlint.core.rule.engine.core.api.replaceTextWith
import io.github.ktlint.core.rule.engine.core.api.upsertWhitespaceAfterMe
import io.github.ktlint.core.rule.engine.core.api.upsertWhitespaceBeforeMe
import io.github.ktlint.core.ruleset.standard.StandardRule
import org.jetbrains.kotlin.psi.psiUtil.siblings

@SinceKtlint("0.1", STABLE)
public class SpacingAroundColonRule : StandardRule("colon-spacing") {
    override fun beforeVisitChildNodes(node: ASTNode) {
        if (node.elementType == COLON) {
            node.removeUnexpectedNewlineBefore()
            node.removeUnexpectedSpacingAround()
            node.addMissingSpacingAround()
        }
    }

    private fun ASTNode.removeUnexpectedNewlineBefore() {
        prevLeaf
            ?.takeIf { it.isWhiteSpaceWithNewline }
            ?.let { prevLeaf ->
                emit(prevLeaf.startOffset, "Unexpected newline before \":\"") {
                    val parentType = parent?.elementType
                    when {
                        parentType == PROPERTY || parentType == FUN -> {
                            removeUnexpectedNewlineBeforeColonInPropertyOrFunctionDeclaration()
                        }

                        prevLeaf.prevLeaf?.isPartOfComment == true -> {
                            removeUnexpectedNewlineBetweenCommentAndColon()
                        }

                        else -> {
                            val text = prevLeaf.text
                            if (spacingBefore) {
                                prevLeaf.replaceTextWith(" ")
                            } else {
                                prevLeaf.remove()
                            }
                            upsertWhitespaceAfterMe(text)
                        }
                    }
                }
            }
    }

    private fun ASTNode.removeUnexpectedNewlineBeforeColonInPropertyOrFunctionDeclaration() {
        siblings(forward = true)
            .firstOrNull { it.elementType == EQ }
            ?.nextSibling
            ?.let { nextSibling ->
                previousNonCodeSiblings().forEach {
                    parent?.addChild(it, nextSibling)
                }
                if (nextSibling.isWhiteSpace) {
                    nextSibling.remove()
                }
            }
        val blockElement = siblings(forward = true).firstOrNull { it.elementType == BLOCK }
        if (blockElement != null) {
            val before =
                blockElement
                    .firstChildNode
                    .nextSibling
            previousNonCodeSiblings()
                .let {
                    if (it.first().isWhiteSpace) {
                        it.first().remove()
                        it.drop(1)
                    }
                    if (it.last().isWhiteSpaceWithNewline) {
                        it.last().remove()
                        it.dropLast(1)
                    } else {
                        it
                    }
                }.forEach {
                    blockElement.addChild(it, before)
                }
        }
    }

    private fun ASTNode.previousNonCodeSiblings(): List<ASTNode> =
        siblings(forward = false)
            .takeWhile { !it.isCode }
            .toList()
            .reversed()

    private fun ASTNode.removeUnexpectedNewlineBetweenCommentAndColon() {
        require(elementType == COLON)
        val beforeAnchor = nextLeaf
        previousNonCodeSiblings()
            .forEach { parent?.addChild(it, beforeAnchor) }
        beforeAnchor.takeIf { it.isWhiteSpace }?.remove()
    }

    private fun ASTNode.removeUnexpectedSpacingAround() {
        if (prevSibling.isWhiteSpaceWithoutNewline && noSpacingBefore) {
            emit(startOffset, "Unexpected spacing before \":\"") {
                prevSibling
                    ?.remove()
            }
        }
        if (nextSibling.isWhiteSpaceWithoutNewline && spacingAfter) {
            emit(startOffset, "Unexpected spacing after \":\"") {
                nextSibling
                    ?.remove()
            }
        }
    }

    private fun ASTNode.addMissingSpacingAround() {
        val missingSpacingBefore = !prevSibling.isWhiteSpace && spacingBefore
        val missingSpacingAfter = !nextSibling.isWhiteSpace && noSpacingAfter
        when {
            missingSpacingBefore && missingSpacingAfter -> {
                emit(startOffset, "Missing spacing around \":\"") {
                    upsertWhitespaceBeforeMe(" ")
                    upsertWhitespaceAfterMe(" ")
                }
            }

            missingSpacingBefore -> {
                emit(startOffset, "Missing spacing before \":\"") {
                    upsertWhitespaceBeforeMe(" ")
                }
            }

            missingSpacingAfter -> {
                emit(startOffset + 1, "Missing spacing after \":\"") {
                    upsertWhitespaceAfterMe(" ")
                }
            }
        }
    }

    private inline val ASTNode.spacingBefore: Boolean
        get() =
            when (parent?.elementType) {

                CLASS, OBJECT_DECLARATION -> {
                    true
                }

                SECONDARY_CONSTRUCTOR -> {
                    // constructor : this/super
                    true
                }

                TYPE_CONSTRAINT -> {
                    // where T : S
                    true
                }

                else -> {
                    parent?.parent?.elementType == TYPE_PARAMETER_LIST
                }
            }

    private inline val ASTNode.noSpacingBefore: Boolean
        get() = !spacingBefore

    private inline val ASTNode.spacingAfter: Boolean
        get() =
            when (parent?.elementType) {
                ANNOTATION, ANNOTATION_ENTRY -> true
                else -> false
            }

    private inline val ASTNode.noSpacingAfter: Boolean
        get() = !spacingAfter
}

public val SPACING_AROUND_COLON_RULE_ID: RuleId = SpacingAroundColonRule().ruleId
