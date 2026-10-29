package io.github.ktlint.core.ruleset.standard.rules

import com.intellij.lang.ASTNode
import com.intellij.psi.impl.source.tree.LeafElement
import com.intellij.psi.impl.source.tree.LeafPsiElement
import com.intellij.psi.tree.TokenSet
import io.github.ktlint.core.rule.engine.core.api.ElementType.ANDAND
import io.github.ktlint.core.rule.engine.core.api.ElementType.COMMA
import io.github.ktlint.core.rule.engine.core.api.ElementType.DIV
import io.github.ktlint.core.rule.engine.core.api.ElementType.DOT
import io.github.ktlint.core.rule.engine.core.api.ElementType.ELSE_KEYWORD
import io.github.ktlint.core.rule.engine.core.api.ElementType.ELVIS
import io.github.ktlint.core.rule.engine.core.api.ElementType.LBRACE
import io.github.ktlint.core.rule.engine.core.api.ElementType.LPAR
import io.github.ktlint.core.rule.engine.core.api.ElementType.MINUS
import io.github.ktlint.core.rule.engine.core.api.ElementType.MUL
import io.github.ktlint.core.rule.engine.core.api.ElementType.OPERATION_REFERENCE
import io.github.ktlint.core.rule.engine.core.api.ElementType.OROR
import io.github.ktlint.core.rule.engine.core.api.ElementType.PERC
import io.github.ktlint.core.rule.engine.core.api.ElementType.PLUS
import io.github.ktlint.core.rule.engine.core.api.ElementType.PREFIX_EXPRESSION
import io.github.ktlint.core.rule.engine.core.api.ElementType.SAFE_ACCESS
import io.github.ktlint.core.rule.engine.core.api.IndentConfig
import io.github.ktlint.core.rule.engine.core.api.IndentConfig.Companion.DEFAULT_INDENT_CONFIG
import io.github.ktlint.core.rule.engine.core.api.RuleId
import io.github.ktlint.core.rule.engine.core.api.SinceKtlint
import io.github.ktlint.core.rule.engine.core.api.SinceKtlint.Status.STABLE
import io.github.ktlint.core.rule.engine.core.api.editorconfig.EditorConfig
import io.github.ktlint.core.rule.engine.core.api.editorconfig.INDENT_SIZE_PROPERTY
import io.github.ktlint.core.rule.engine.core.api.editorconfig.INDENT_STYLE_PROPERTY
import io.github.ktlint.core.rule.engine.core.api.isPartOfComment
import io.github.ktlint.core.rule.engine.core.api.isWhiteSpaceWithNewline
import io.github.ktlint.core.rule.engine.core.api.isWhiteSpaceWithoutNewline
import io.github.ktlint.core.rule.engine.core.api.nextCodeLeaf
import io.github.ktlint.core.rule.engine.core.api.nextLeaf
import io.github.ktlint.core.rule.engine.core.api.nextSibling
import io.github.ktlint.core.rule.engine.core.api.parent
import io.github.ktlint.core.rule.engine.core.api.prevCodeLeaf
import io.github.ktlint.core.rule.engine.core.api.prevCodeSibling
import io.github.ktlint.core.rule.engine.core.api.prevLeaf
import io.github.ktlint.core.rule.engine.core.api.remove
import io.github.ktlint.core.rule.engine.core.api.upsertWhitespaceAfterMe
import io.github.ktlint.core.rule.engine.core.api.upsertWhitespaceBeforeMe
import io.github.ktlint.core.ruleset.standard.StandardRule
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.psiUtil.leaves

@SinceKtlint("0.13", STABLE)
public class ChainWrappingRule :
    StandardRule(
        id = "chain-wrapping",
        usesEditorConfigProperties =
            setOf(
                INDENT_SIZE_PROPERTY,
                INDENT_STYLE_PROPERTY,
            ),
    ) {
    private var indentConfig = DEFAULT_INDENT_CONFIG

    override fun beforeFirstNode(editorConfig: EditorConfig) {
        indentConfig =
            IndentConfig(
                indentStyle = editorConfig[INDENT_STYLE_PROPERTY],
                tabWidth = editorConfig[INDENT_SIZE_PROPERTY],
            )
    }

    override fun beforeVisitChildNodes(node: ASTNode) {
        if (!node.isPartOfComment) {
            when (node.elementType) {
                in NEXT_LINE_TOKENS -> node.fixChainOperatorWhichMayNotBeUsedAtEndOfLine()
                in SAME_LINE_TOKENS, in PREFIX_TOKENS -> node.fixChainOperatorWhichMayNotUsedAtStartOfLine()
            }
        }
    }

    private fun ASTNode.fixChainOperatorWhichMayNotBeUsedAtEndOfLine() {
        val nextLeaf = nextCodeLeaf?.prevLeaf
        if (nextLeaf.isWhiteSpaceWithNewline && !isElvisOperatorAndComment()) {
            emit(startOffset, "Line must not end with \"$text\"") {
                // rewriting
                // <prevLeaf><node="."><nextLeaf="\n"> to
                // <prevLeaf><delete space if any><nextLeaf="\n"><node="."><space if needed>
                // (or)
                // <prevLeaf><node="."><spaceBeforeComment><comment><nextLeaf="\n"> to
                // <prevLeaf><delete space if any><spaceBeforeComment><comment><nextLeaf="\n"><node="."><space if needed>
                if (elementType == ELVIS) {
                    upsertWhitespaceBeforeMe(indentConfig.childIndentOf(this))
                    upsertWhitespaceAfterMe(" ")
                } else {
                    remove()
                    (nextLeaf as LeafElement).rawInsertAfterMe(this as LeafElement)
                }
            }
        }
    }

    private fun ASTNode.fixChainOperatorWhichMayNotUsedAtStartOfLine(): Boolean =
        if (isPartOfSpread() || isInPrefixPosition()) {
            true
        } else {
            val prevLeaf = prevLeaf
            if (prevLeaf != null && prevLeaf.isWhiteSpaceWithNewline) {
                emit(startOffset, "Line must not begin with \"$text\"") {
                    // rewriting
                    // <insertionPoint><prevLeaf="\n"><node="&&"><nextLeaf=" "> to
                    // <insertionPoint><prevLeaf=" "><node="&&"><nextLeaf="\n"><delete node="&&"><delete nextLeaf=" ">
                    // (or)
                    // <insertionPoint><spaceBeforeComment><comment><prevLeaf="\n"><node="&&"><nextLeaf=" "> to
                    // <insertionPoint><space if needed><node="&&"><spaceBeforeComment><comment><prevLeaf="\n"><delete node="&&"><delete nextLeaf=" ">
                    val nextLeaf = nextLeaf
                    val whiteSpaceToBeDeleted =
                        when {
                            nextLeaf.isWhiteSpaceWithNewline -> {
                                // Node is preceded and followed by whitespace. Prefer to remove the whitespace before the node as this will
                                // change the indent of the next line
                                prevLeaf
                            }

                            nextLeaf.isWhiteSpaceWithoutNewline -> {
                                nextLeaf
                            }

                            else -> {
                                null
                            }
                        }

                    val parent = parent
                    if (parent?.elementType == OPERATION_REFERENCE) {
                        val insertBeforeSibling =
                            parent
                                .prevCodeSibling
                                ?.nextSibling
                        parent.remove()
                        insertBeforeSibling?.parent?.addChild(parent, insertBeforeSibling)
                        parent.upsertWhitespaceBeforeMe(" ")
                    } else {
                        val insertionPoint = prevLeaf.prevCodeLeaf as LeafPsiElement
                        (this as LeafPsiElement).remove()
                        insertionPoint.rawInsertAfterMe(this)
                        (insertionPoint as ASTNode).upsertWhitespaceAfterMe(" ")
                    }
                    whiteSpaceToBeDeleted?.remove()
                }
            }
            false
        }

    private fun ASTNode.isPartOfSpread() =
        // Allow:
        //    fn(
        //        *typedArray<...>()
        //    )
        elementType == MUL &&
            prevCodeLeaf
                ?.let { leaf ->
                    val type = leaf.elementType
                    type == LPAR ||
                        type == COMMA ||
                        type == LBRACE ||
                        type == ELSE_KEYWORD ||
                        KtTokens.OPERATIONS.contains(type)
                } == true

    private fun ASTNode.isInPrefixPosition() =
        // Allow:
        //    fn(
        //        -42
        //    )
        elementType in PREFIX_TOKENS && parent?.parent?.elementType == PREFIX_EXPRESSION

    private fun ASTNode.isElvisOperatorAndComment(): Boolean =
        elementType == ELVIS &&
            leaves().takeWhile { it.isWhiteSpaceWithoutNewline || it.isPartOfComment }.any()

    private companion object {
        val NEXT_LINE_TOKENS = TokenSet.create(DOT, SAFE_ACCESS, ELVIS)
        val SAME_LINE_TOKENS = TokenSet.create(MUL, DIV, PERC, ANDAND, OROR)
        val PREFIX_TOKENS = TokenSet.create(PLUS, MINUS)
    }
}

public val CHAIN_WRAPPING_RULE_ID: RuleId = ChainWrappingRule().ruleId
