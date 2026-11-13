package io.github.ktlint.core.ruleset.standard.rules

import com.intellij.lang.ASTNode
import com.intellij.psi.impl.source.tree.LeafPsiElement
import com.intellij.psi.impl.source.tree.PsiWhiteSpaceImpl
import io.github.ktlint.core.rule.engine.core.api.ElementType.BLOCK
import io.github.ktlint.core.rule.engine.core.api.ElementType.ELSE
import io.github.ktlint.core.rule.engine.core.api.ElementType.ELSE_KEYWORD
import io.github.ktlint.core.rule.engine.core.api.ElementType.IF
import io.github.ktlint.core.rule.engine.core.api.ElementType.LBRACE
import io.github.ktlint.core.rule.engine.core.api.ElementType.RBRACE
import io.github.ktlint.core.rule.engine.core.api.ElementType.RPAR
import io.github.ktlint.core.rule.engine.core.api.ElementType.THEN
import io.github.ktlint.core.rule.engine.core.api.IndentConfig
import io.github.ktlint.core.rule.engine.core.api.RuleId
import io.github.ktlint.core.rule.engine.core.api.RuleV2
import io.github.ktlint.core.rule.engine.core.api.SinceKtlint
import io.github.ktlint.core.rule.engine.core.api.SinceKtlint.Status.EXPERIMENTAL
import io.github.ktlint.core.rule.engine.core.api.SinceKtlint.Status.STABLE
import io.github.ktlint.core.rule.engine.core.api.editorconfig.EditorConfig
import io.github.ktlint.core.rule.engine.core.api.editorconfig.INDENT_SIZE_PROPERTY
import io.github.ktlint.core.rule.engine.core.api.editorconfig.INDENT_STYLE_PROPERTY
import io.github.ktlint.core.rule.engine.core.api.indent
import io.github.ktlint.core.rule.engine.core.api.isPartOfComment
import io.github.ktlint.core.rule.engine.core.api.isWhiteSpace
import io.github.ktlint.core.rule.engine.core.api.isWhiteSpaceWithoutNewline
import io.github.ktlint.core.rule.engine.core.api.nextSibling
import io.github.ktlint.core.rule.engine.core.api.parent
import io.github.ktlint.core.rule.engine.core.api.replaceTextWith
import io.github.ktlint.core.rule.engine.core.api.upsertWhitespaceBeforeMe
import io.github.ktlint.core.ruleset.standard.StandardRule
import org.jetbrains.kotlin.psi.KtBlockExpression
import org.jetbrains.kotlin.psi.KtImplementationDetail
import org.jetbrains.kotlin.psi.psiUtil.leaves

/**
 * All branches of the if-statement should be wrapped between braces if at least one branch is wrapped between braces. Consistent bracing
 * makes statements easier to read.
 */
@SinceKtlint("0.49", EXPERIMENTAL)
@SinceKtlint("1.0", STABLE)
public class IfElseBracingRule :
    StandardRule(
        id = "if-else-bracing",
        usesEditorConfigProperties =
            setOf(
                INDENT_SIZE_PROPERTY,
                INDENT_STYLE_PROPERTY,
            ),
    ),
    RuleV2.OfficialCodeStyle {
    private var indentConfig = IndentConfig.DEFAULT_INDENT_CONFIG

    override fun beforeFirstNode(editorConfig: EditorConfig) {
        indentConfig =
            IndentConfig(
                indentStyle = editorConfig[INDENT_STYLE_PROPERTY],
                tabWidth = editorConfig[INDENT_SIZE_PROPERTY],
            )
    }

    override fun beforeVisitChildNodes(node: ASTNode) {
        if (node.elementType == IF) {
            node.visitIfStatement()
        }
    }

    private fun ASTNode.visitIfStatement() {
        val thenNode = requireNotNull(findChildByType(THEN)) { "Can not find THEN branch in IF" }
        val elseNode = findChildByType(ELSE) ?: return
        val parentIfBracing =
            parent
                ?.takeIf { it.elementType == ELSE }
                ?.parent
                ?.hasBracing()
                ?: false
        val thenBracing = thenNode.hasBracing()
        val elseBracing = elseNode.hasBracing()
        if (parentIfBracing || thenBracing || elseBracing) {
            if (!thenBracing) {
                thenNode.visitBranchWithoutBraces()
            }
            if (!elseBracing) {
                if (elseNode.firstChildNode?.elementType != IF) {
                    elseNode.visitBranchWithoutBraces()
                } else {
                    // Postpone changing the else-if until that node is being processed
                }
            }
        }
    }

    private fun ASTNode?.hasBracing(): Boolean =
        when {
            this == null -> {
                false
            }

            elementType == BLOCK -> {
                true
            }

            elementType == IF -> {
                findChildByType(THEN).hasBracing() || findChildByType(ELSE).hasBracing()
            }

            else -> {
                firstChildNode.hasBracing()
            }
        }

    private fun ASTNode.visitBranchWithoutBraces(): Boolean {
        emit(
            firstChildNode?.startOffset ?: startOffset,
            "All branches of the if statement should be wrapped between braces if at least one branch is wrapped between braces",
        ) {
            autocorrect()
        }
        return true
    }

    private fun ASTNode.autocorrect() {
        val prevLeaves =
            leaves(forward = false)
                .takeWhile { it.elementType !in listOf(RPAR, ELSE_KEYWORD) }
                .toList()
                .reversed()
        val nextLeaves =
            leaves(forward = true)
                .takeWhile { it.isWhiteSpaceWithoutNewline || it.isPartOfComment }
                .toList()
                .dropLastWhile { it.isWhiteSpaceWithoutNewline }

        prevLeaves
            .firstOrNull()
            .takeIf { it.isWhiteSpace }
            ?.replaceTextWith(" ")
        @OptIn(KtImplementationDetail::class)
        KtBlockExpression(null).let { blockExpression ->
            val previousChild = firstChildNode
            if (previousChild == null) {
                addChild(blockExpression, null)
            } else {
                replaceChild(firstChildNode, blockExpression)
            }
            blockExpression.addChild(LeafPsiElement(LBRACE, "{"))
            if (previousChild != null) {
                blockExpression.addChild(PsiWhiteSpaceImpl(indentConfig.childIndentOf(this)))
            }
            prevLeaves
                .dropWhile { it.isWhiteSpace }
                .takeIf { it.isNotEmpty() }
                ?.forEach { blockExpression.addChild(it) }
            if (previousChild != null) {
                blockExpression.addChild(previousChild)
            }
            nextLeaves.forEach { blockExpression.addChild(it) }
            if (previousChild != null) {
                blockExpression.addChild(PsiWhiteSpaceImpl(indent))
            }
            blockExpression.addChild(LeafPsiElement(RBRACE, "}"))
        }

        // Make sure else starts on same line as newly inserted right brace
        if (elementType == THEN) {
            nextSibling { !it.isPartOfComment }
                ?.upsertWhitespaceBeforeMe(" ")
        }
    }
}

public val IF_ELSE_BRACING_RULE_ID: RuleId = IfElseBracingRule().ruleId
