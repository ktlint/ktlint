package io.github.ktlint.core.ruleset.standard.rules

import io.github.ktlint.core.rule.engine.core.api.ElementType.BLOCK
import io.github.ktlint.core.rule.engine.core.api.ElementType.BODY
import io.github.ktlint.core.rule.engine.core.api.ElementType.DO_KEYWORD
import io.github.ktlint.core.rule.engine.core.api.ElementType.LBRACE
import io.github.ktlint.core.rule.engine.core.api.ElementType.RBRACE
import io.github.ktlint.core.rule.engine.core.api.ElementType.RPAR
import io.github.ktlint.core.rule.engine.core.api.IndentConfig
import io.github.ktlint.core.rule.engine.core.api.RuleId
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
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.LeafPsiElement
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.PsiWhiteSpaceImpl
import org.jetbrains.kotlin.psi.KtBlockExpression
import org.jetbrains.kotlin.psi.KtImplementationDetail
import org.jetbrains.kotlin.psi.psiUtil.leaves

/**
 * https://developer.android.com/kotlin/style-guide#braces
 */
@SinceKtlint("1.0", EXPERIMENTAL)
@SinceKtlint("1.3", STABLE)
public class MultilineLoopRule :
    StandardRule(
        id = "multiline-loop",
        usesEditorConfigProperties =
            setOf(
                INDENT_SIZE_PROPERTY,
                INDENT_STYLE_PROPERTY,
            ),
    ) {
    private var indentConfig = IndentConfig.DEFAULT_INDENT_CONFIG

    override fun beforeFirstNode(editorConfig: EditorConfig) {
        indentConfig =
            IndentConfig(
                indentStyle = editorConfig[INDENT_STYLE_PROPERTY],
                tabWidth = editorConfig[INDENT_SIZE_PROPERTY],
            )
    }

    override fun beforeVisitChildNodes(node: ASTNode) {
        node
            .takeIf { it.elementType == BODY }
            ?.takeUnless {
                // Ignore loop with empty body
                it.firstChildNode == null
            }?.takeUnless { it.firstChildNode.elementType == BLOCK }
            ?.parent
            ?.takeIf {
                // Allow single line loop statements as long as they are really simple (e.g. do not contain newlines)
                //    for (...) <statement>
                //    while (...) <statement>
                //    do <statement> while (...)
                it.textContains('\n')
            } ?: return
        emit(node.firstChildNode.startOffset, "Missing { ... }") {
            autocorrect(node)
        }
    }

    private fun autocorrect(node: ASTNode) {
        val prevLeaves =
            node
                .leaves(forward = false)
                .takeWhile { it.elementType !in listOf(RPAR, DO_KEYWORD) }
                .toList()
                .reversed()
        val nextLeaves =
            node
                .leaves(forward = true)
                .takeWhile { it.isWhiteSpaceWithoutNewline || it.isPartOfComment }
                .toList()
                .dropLastWhile { it.isWhiteSpaceWithoutNewline }

        prevLeaves
            .firstOrNull()
            .takeIf { it.isWhiteSpace }
            ?.replaceTextWith(" ")
        @OptIn(KtImplementationDetail::class)
        KtBlockExpression(null).let { blockExpression ->
            val previousChild = node.firstChildNode
            node.replaceChild(node.firstChildNode, blockExpression)
            blockExpression.addChild(LeafPsiElement(LBRACE, "{"))
            blockExpression.addChild(PsiWhiteSpaceImpl(indentConfig.childIndentOf(node)))
            prevLeaves
                .dropWhile { it.isWhiteSpace }
                .forEach { blockExpression.addChild(it) }
            blockExpression.addChild(previousChild)
            nextLeaves.forEach { blockExpression.addChild(it) }
            blockExpression.addChild(PsiWhiteSpaceImpl(node.indent))
            blockExpression.addChild(LeafPsiElement(RBRACE, "}"))
        }

        node
            .nextSibling { !it.isPartOfComment }
            ?.upsertWhitespaceBeforeMe(" ")
    }
}

public val MULTILINE_LOOP_RULE_ID: RuleId = MultilineLoopRule().ruleId
