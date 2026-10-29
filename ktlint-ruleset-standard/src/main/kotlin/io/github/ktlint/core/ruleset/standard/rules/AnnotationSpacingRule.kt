package io.github.ktlint.core.ruleset.standard.rules

import com.intellij.lang.ASTNode
import com.intellij.psi.impl.source.tree.LeafPsiElement
import com.intellij.psi.impl.source.tree.PsiWhiteSpaceImpl
import io.github.ktlint.core.rule.engine.core.api.ElementType.ANNOTATION_ENTRY
import io.github.ktlint.core.rule.engine.core.api.ElementType.MODIFIER_LIST
import io.github.ktlint.core.rule.engine.core.api.RuleId
import io.github.ktlint.core.rule.engine.core.api.SinceKtlint
import io.github.ktlint.core.rule.engine.core.api.SinceKtlint.Status.EXPERIMENTAL
import io.github.ktlint.core.rule.engine.core.api.SinceKtlint.Status.STABLE
import io.github.ktlint.core.rule.engine.core.api.isCode
import io.github.ktlint.core.rule.engine.core.api.isPartOfComment
import io.github.ktlint.core.rule.engine.core.api.isWhiteSpace
import io.github.ktlint.core.rule.engine.core.api.isWhiteSpaceWithNewline
import io.github.ktlint.core.rule.engine.core.api.nextLeaf
import io.github.ktlint.core.rule.engine.core.api.nextSibling
import io.github.ktlint.core.rule.engine.core.api.parent
import io.github.ktlint.core.rule.engine.core.api.prevSibling
import io.github.ktlint.core.rule.engine.core.api.remove
import io.github.ktlint.core.rule.engine.core.api.replaceTextWith
import io.github.ktlint.core.rule.engine.core.util.safeAs
import io.github.ktlint.core.ruleset.standard.StandardRule
import org.jetbrains.kotlin.psi.psiUtil.children
import org.jetbrains.kotlin.psi.psiUtil.leaves
import org.jetbrains.kotlin.psi.psiUtil.siblings

/**
 * Ensures annotations occur immediately prior to the annotated construct
 *
 * https://kotlinlang.org/docs/reference/coding-conventions.html#annotation-formatting
 */
@SinceKtlint("0.39", EXPERIMENTAL)
@SinceKtlint("0.46", STABLE)
public class AnnotationSpacingRule : StandardRule("annotation-spacing") {
    override fun beforeVisitChildNodes(node: ASTNode) {
        if (node.elementType != MODIFIER_LIST) {
            return
        }

        val annotations =
            node
                .children()
                .filter { it.elementType == ANNOTATION_ENTRY }
                .toList()
                .ifEmpty { return }

        node.fixBlankLinesBetweenAnnotationsAndAnnotatedConstruct(annotations)
        node.fixCommentsBetweenAnnotationsAndAnnotatedConstruct()
    }

    private fun ASTNode.fixBlankLinesBetweenAnnotationsAndAnnotatedConstruct(annotations: List<ASTNode>) {
        // Join the nodes that immediately follow the annotations (whitespace), then add the final whitespace
        // if it's not a child of root. This happens when a new line separates the annotations from the annotated
        // construct. In the following example, there are no whitespace children of root, but root's next sibling is the
        // new line whitespace.
        //
        //      @JvmField
        //      val s: Any
        //
        val whiteSpaces =
            (annotations.asSequence().map { it.nextSibling } + nextSibling)
                .filterNotNull()
                .filter { it.isWhiteSpace }
                .take(annotations.size)
                .toList()
        whiteSpaces
            .filter { it.containsMultipleNewlines() }
            .forEach {
                emit(it.startOffset + 1, "No blank line(s) expected between annotation and annotated construct") {
                    removeIntraLineBreaks(annotations.last())
                    removeExtraLineBreaks()
                }
            }
    }

    private fun ASTNode.fixCommentsBetweenAnnotationsAndAnnotatedConstruct() {
        require(elementType == MODIFIER_LIST)
        // Collect all modifiers from the list itself
        val modifiers = children()
        val noneCodeElementsBetweenModifierListAndAnnotatedConstruct = siblings().takeWhile { !it.isCode }
        (modifiers + noneCodeElementsBetweenModifierListAndAnnotatedConstruct)
            .filter { it.isPartOfComment && !it.isCommentOnSameLineAsPrevLeaf() }
            .toList()
            .forEach { comment ->
                emit(comment.startOffset, "No comment(s) expected between annotation and the annotated construct") {
                    // Remove the comment and whitespace preceding it from the modifier list
                    comment.prevSibling { it.isWhiteSpace }?.remove()
                    comment.remove()

                    // Re-insert the comment and newline prior to the modifier list
                    val beforeAnchor = this
                    parent!!
                        .apply {
                            addChild(comment, beforeAnchor)
                            addChild(PsiWhiteSpaceImpl("\n"), beforeAnchor)
                        }
                }
            }
    }

    private fun ASTNode.containsMultipleNewlines() = text.count { it == '\n' } > 1

    private fun ASTNode.removeExtraLineBreaks() {
        nextSibling { it.isWhiteSpaceWithNewline }
            .safeAs<LeafPsiElement>()
            ?.rawReplaceExtraLineBreaks()
    }

    private fun ASTNode.rawReplaceExtraLineBreaks() {
        // Replace the extra white space with a single break
        val firstIndex = text.indexOf("\n") + 1
        val replacementText =
            text.substring(0, firstIndex) +
                text.substringAfter("\n").replace("\n", "")

        replaceTextWith(replacementText)
    }

    private fun ASTNode.removeIntraLineBreaks(lastAnnotationEntryNode: ASTNode) {
        // Pull the next before raw replace, or it will blow up
        val nextLeaf = nextLeaf
        if (isWhiteSpace) {
            if (text.toCharArray().count { it == '\n' } > 1) {
                rawReplaceExtraLineBreaks()
            }
        }

        if (nextLeaf != null && !lastAnnotationEntryNode.text.endsWith(nextLeaf.text)) {
            nextLeaf.removeIntraLineBreaks(lastAnnotationEntryNode)
        }
    }

    private fun ASTNode.isCommentOnSameLineAsPrevLeaf() =
        isPartOfComment && leaves(forward = false).takeWhile { it.isWhiteSpace }.none { "\n" in it.text }
}

public val ANNOTATION_SPACING_RULE_ID: RuleId = AnnotationSpacingRule().ruleId
