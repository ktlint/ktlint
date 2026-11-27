package io.github.ktlint.core.ruleset.standard.rules

import com.intellij.lang.ASTNode
import com.intellij.psi.impl.source.tree.LeafPsiElement
import com.intellij.psi.impl.source.tree.PsiWhiteSpaceImpl
import io.github.ktlint.core.rule.engine.core.api.ElementType.BINARY_EXPRESSION
import io.github.ktlint.core.rule.engine.core.api.ElementType.BLOCK
import io.github.ktlint.core.rule.engine.core.api.ElementType.DOT_QUALIFIED_EXPRESSION
import io.github.ktlint.core.rule.engine.core.api.ElementType.ELSE
import io.github.ktlint.core.rule.engine.core.api.ElementType.ELSE_KEYWORD
import io.github.ktlint.core.rule.engine.core.api.ElementType.IF
import io.github.ktlint.core.rule.engine.core.api.ElementType.LBRACE
import io.github.ktlint.core.rule.engine.core.api.ElementType.RBRACE
import io.github.ktlint.core.rule.engine.core.api.ElementType.RPAR
import io.github.ktlint.core.rule.engine.core.api.ElementType.THEN
import io.github.ktlint.core.rule.engine.core.api.IndentConfig
import io.github.ktlint.core.rule.engine.core.api.RuleId
import io.github.ktlint.core.rule.engine.core.api.SinceKtlint
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
import io.github.ktlint.core.rule.engine.core.api.prevSibling
import io.github.ktlint.core.rule.engine.core.api.replaceTextWith
import io.github.ktlint.core.rule.engine.core.api.upsertWhitespaceBeforeMe
import io.github.ktlint.core.ruleset.standard.StandardRule
import org.jetbrains.kotlin.psi.KtBlockExpression
import org.jetbrains.kotlin.psi.KtImplementationDetail
import org.jetbrains.kotlin.psi.psiUtil.leaves

/**
 * https://kotlinlang.org/docs/reference/coding-conventions.html#formatting-control-flow-statements
 */
@SinceKtlint("0.25", STABLE)
public class MultiLineIfElseRule :
    StandardRule(
        id = "multiline-if-else",
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
        if (node.elementType == THEN || node.elementType == ELSE) {
            node.visitElseOrThen()
        }
    }

    private fun ASTNode.visitElseOrThen() {
        if (wrapInBlock()) {
            emit(firstChildNode.startOffset, "Missing { ... }") {
                autocorrect()
            }
        }
    }

    private fun ASTNode.wrapInBlock() =
        !isWrappedInBlock() &&
            !elseIfFollowedByBinaryExpression() &&
            !elseIfFollowedByDotQualifiedExpression() &&
            !isSimpleSingleLineIfStatement()

    private fun ASTNode.isWrappedInBlock(): Boolean = firstChildNode?.elementType == BLOCK

    // Allow
    // val foo2 = if (bar1) {
    //     "bar1"
    // } else if (bar2) {
    //     null
    // } else {
    //     null
    // } ?: "something-else"
    private fun ASTNode.elseIfFollowedByBinaryExpression(): Boolean =
        elementType == ELSE &&
            firstChildNode?.elementType == BINARY_EXPRESSION &&
            firstChildNode.firstChildNode?.elementType == IF

    // Allow
    // val foo = if (bar1) {
    //     "bar1"
    // } else if (bar2) {
    //     "bar2"
    // } else {
    //     "bar3"
    // }.plus("foo")
    private fun ASTNode.elseIfFollowedByDotQualifiedExpression(): Boolean =
        elementType == ELSE &&
            firstChildNode?.elementType == DOT_QUALIFIED_EXPRESSION &&
            firstChildNode.firstChildNode?.elementType == IF

    private fun ASTNode.isSimpleSingleLineIfStatement(): Boolean =
        when {
            !prevSibling!!.textContains('\n') && firstChildNode == null -> {
                // if block with an empty 'then' block
                true
            }

            !prevSibling!!.textContains('\n') && firstChildNode.elementType == IF -> {
                // Allow single line for:
                // else if (...)
                true
            }

            !prevSibling!!.textContains('\n') && parent?.textContains('\n') == false -> {
                // Allow single line if statements as long as they are really simple (e.g. do not contain newlines)
                //    if (...) <statement> // no else statement
                //    if (...) <statement> else <statement>
                // but not when
                //    if (...) <statement> else if (...) <statement>
                if (parent?.parent?.elementType == ELSE) {
                    // Except in case nested if-else-if on single line
                    //    if (...) <statement> else if (...) <statement>
                    false
                } else {
                    true
                }
            }

            else -> {
                false
            }
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
            replaceChild(firstChildNode, blockExpression)
            blockExpression.addChild(LeafPsiElement(LBRACE, "{"))
            blockExpression.addChild(PsiWhiteSpaceImpl(indentConfig.childIndentOf(this)))
            prevLeaves
                .dropWhile { it.isWhiteSpace }
                .forEach { blockExpression.addChild(it) }
            blockExpression.addChild(previousChild)
            nextLeaves.forEach { blockExpression.addChild(it) }

            blockExpression.addChild(PsiWhiteSpaceImpl(indent))
            blockExpression.addChild(LeafPsiElement(RBRACE, "}"))
        }

        // Make sure else starts on same line as newly inserted right brace
        if (elementType == THEN) {
            nextSibling { !it.isPartOfComment }
                ?.upsertWhitespaceBeforeMe(" ")
        }
    }
}

public val MULTI_LINE_IF_ELSE_RULE_ID: RuleId = MultiLineIfElseRule().ruleId
