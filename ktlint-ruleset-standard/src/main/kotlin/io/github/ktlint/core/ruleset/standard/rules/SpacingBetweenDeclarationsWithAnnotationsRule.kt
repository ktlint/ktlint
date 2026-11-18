package io.github.ktlint.core.ruleset.standard.rules

import com.intellij.lang.ASTNode
import io.github.ktlint.core.rule.engine.core.api.ElementType.ANNOTATION_ENTRY
import io.github.ktlint.core.rule.engine.core.api.ElementType.MODIFIER_LIST
import io.github.ktlint.core.rule.engine.core.api.ElementType.PROPERTY_ACCESSOR
import io.github.ktlint.core.rule.engine.core.api.RuleId
import io.github.ktlint.core.rule.engine.core.api.SinceKtlint
import io.github.ktlint.core.rule.engine.core.api.SinceKtlint.Status.EXPERIMENTAL
import io.github.ktlint.core.rule.engine.core.api.SinceKtlint.Status.STABLE
import io.github.ktlint.core.rule.engine.core.api.children
import io.github.ktlint.core.rule.engine.core.api.indent
import io.github.ktlint.core.rule.engine.core.api.isCode
import io.github.ktlint.core.rule.engine.core.api.isDeclaration
import io.github.ktlint.core.rule.engine.core.api.isWhiteSpace
import io.github.ktlint.core.rule.engine.core.api.nextLeaf
import io.github.ktlint.core.rule.engine.core.api.prevCodeLeaf
import io.github.ktlint.core.rule.engine.core.api.prevCodeSibling
import io.github.ktlint.core.rule.engine.core.api.upsertWhitespaceBeforeMe
import io.github.ktlint.core.ruleset.standard.StandardRule
import org.jetbrains.kotlin.psi.psiUtil.leaves

/**
 * @see https://youtrack.jetbrains.com/issue/KT-35106
 */
@SinceKtlint("0.37", EXPERIMENTAL)
@SinceKtlint("0.46", STABLE)
public class SpacingBetweenDeclarationsWithAnnotationsRule : StandardRule("spacing-between-declarations-with-annotations") {
    override fun beforeVisitChildNodes(node: ASTNode) {
        if ((node.isDeclarationOrPropertyAccessor()) && node.isAnnotated()) {
            node.visitDeclaration()
        }
    }

    private fun ASTNode.isDeclarationOrPropertyAccessor() = isDeclaration || elementType == PROPERTY_ACCESSOR

    private fun ASTNode.visitDeclaration() {
        prevCodeSibling
            ?.takeIf { it.isDeclarationOrPropertyAccessor() }
            ?.takeIf { prevDeclaration -> hasNoBlankLineBetweenDeclarations(this, prevDeclaration) }
            ?.let {
                val prevLeaf = prevCodeLeaf?.nextLeaf { it.isWhiteSpace }!!
                emit(prevLeaf.startOffset + 1, "Declarations and declarations with annotations should have an empty space between.") {
                    prevLeaf.upsertWhitespaceBeforeMe("\n".plus(indent))
                }
            }
    }

    private fun ASTNode.isAnnotated(): Boolean =
        findChildByType(MODIFIER_LIST)
            ?.children
            ?.any { it.elementType == ANNOTATION_ENTRY }
            ?: false

    private fun hasNoBlankLineBetweenDeclarations(
        node: ASTNode,
        prevDeclaration: ASTNode,
    ) = node
        .leaves(false)
        .takeWhile { !it.isCode }
        .takeWhile { it != prevDeclaration }
        .none { it.isBlankLine() }

    private fun ASTNode.isBlankLine() = isWhiteSpace && text.count { it == '\n' } > 1
}

public val SPACING_BETWEEN_DECLARATIONS_WITH_ANNOTATIONS_RULE_ID: RuleId = SpacingBetweenDeclarationsWithAnnotationsRule().ruleId
