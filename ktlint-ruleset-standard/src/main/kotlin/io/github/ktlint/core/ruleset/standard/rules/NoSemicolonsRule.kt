package io.github.ktlint.core.ruleset.standard.rules

import com.intellij.lang.ASTNode
import io.github.ktlint.core.rule.engine.core.api.ElementType.ANNOTATION_ENTRY
import io.github.ktlint.core.rule.engine.core.api.ElementType.BODY
import io.github.ktlint.core.rule.engine.core.api.ElementType.CLASS
import io.github.ktlint.core.rule.engine.core.api.ElementType.CLASS_BODY
import io.github.ktlint.core.rule.engine.core.api.ElementType.ENUM_ENTRY
import io.github.ktlint.core.rule.engine.core.api.ElementType.ENUM_KEYWORD
import io.github.ktlint.core.rule.engine.core.api.ElementType.FOR
import io.github.ktlint.core.rule.engine.core.api.ElementType.IF
import io.github.ktlint.core.rule.engine.core.api.ElementType.KDOC
import io.github.ktlint.core.rule.engine.core.api.ElementType.OBJECT_KEYWORD
import io.github.ktlint.core.rule.engine.core.api.ElementType.SEMICOLON
import io.github.ktlint.core.rule.engine.core.api.ElementType.THEN
import io.github.ktlint.core.rule.engine.core.api.ElementType.WHILE
import io.github.ktlint.core.rule.engine.core.api.RuleId
import io.github.ktlint.core.rule.engine.core.api.SinceKtlint
import io.github.ktlint.core.rule.engine.core.api.SinceKtlint.Status.STABLE
import io.github.ktlint.core.rule.engine.core.api.findParentByType
import io.github.ktlint.core.rule.engine.core.api.hasModifier
import io.github.ktlint.core.rule.engine.core.api.isCode
import io.github.ktlint.core.rule.engine.core.api.isWhiteSpace
import io.github.ktlint.core.rule.engine.core.api.isWhiteSpaceWithNewline
import io.github.ktlint.core.rule.engine.core.api.lastChildLeafOrSelf
import io.github.ktlint.core.rule.engine.core.api.nextCodeSibling
import io.github.ktlint.core.rule.engine.core.api.nextLeaf
import io.github.ktlint.core.rule.engine.core.api.parent
import io.github.ktlint.core.rule.engine.core.api.prevCodeLeaf
import io.github.ktlint.core.rule.engine.core.api.prevLeaf
import io.github.ktlint.core.rule.engine.core.api.remove
import io.github.ktlint.core.rule.engine.core.api.upsertWhitespaceAfterMe
import io.github.ktlint.core.ruleset.standard.StandardRule
import org.jetbrains.kotlin.lexer.KtTokens

@SinceKtlint("0.1", STABLE)
public class NoSemicolonsRule :
    StandardRule(
        id = "no-semi",
    ) {
    override fun beforeVisitChildNodes(node: ASTNode) {
        if (node.elementType != SEMICOLON) {
            return
        }
        val nextLeaf = node.nextLeaf
        if (nextLeaf.doesNotRequirePreSemi() && node.isNoSemicolonRequiredAfter()) {
            emit(node.startOffset, "Unnecessary semicolon") {
                val prevLeaf = node.prevLeaf
                node.remove()
                if (prevLeaf.isNonNullWhitespace() && nextLeaf.isNonNullWhitespace()) {
                    prevLeaf!!.remove()
                }
            }
        } else if (!nextLeaf.isWhiteSpace) {
            if (node.prevLeaf.isWhiteSpaceWithNewline) {
                return
            }
            // todo: move to a separate rule
            emit(node.startOffset + 1, "Missing spacing after \";\"") {
                node.upsertWhitespaceAfterMe(" ")
            }
        }
    }

    private fun ASTNode?.isNonNullWhitespace(): Boolean = this != null && isWhiteSpace

    private fun ASTNode?.doesNotRequirePreSemi() =
        when {
            this == null -> {
                true
            }

            isWhiteSpace -> {
                nextLeaf {
                    it.isCode &&
                        it.findParentByType(KDOC) == null &&
                        it.findParentByType(ANNOTATION_ENTRY) == null
                }.let { nextLeaf ->
                    nextLeaf == null ||
                        // \s+ and then eof
                        (textContains('\n') && nextLeaf.elementType != KtTokens.LBRACE)
                }
            }

            else -> {
                false
            }
        }

    private fun ASTNode.isNoSemicolonRequiredAfter(): Boolean =
        when {
            prevCodeLeaf?.elementType == OBJECT_KEYWORD -> {
                // https://github.com/ktlint/ktlint/issues/281
                false
            }

            prevCodeLeaf?.parent?.isLoopWithoutBody() ?: false -> {
                // https://github.com/ktlint/ktlint/issues/955
                false
            }

            prevCodeLeaf?.parent?.isIfExpressionWithoutThen() ?: false -> {
                false
            }

            parent?.elementType == ENUM_ENTRY -> {
                // In case of an enum entry the semicolon (e.g. the node) is a direct child node of enum entry
                isLastCodeLeafBeforeClosingOfClassBody()
            }

            isEnumClassWithoutValues() -> {
                false
            }

            else -> {
                true
            }
        }

    private fun ASTNode.isLoopWithoutBody() =
        (elementType == WHILE || elementType == FOR) &&
            findChildByType(BODY)?.firstChildNode == null

    private fun ASTNode.isIfExpressionWithoutThen() = elementType == IF && findChildByType(THEN)?.firstChildNode == null

    private fun ASTNode?.isLastCodeLeafBeforeClosingOfClassBody() = getLastCodeLeafBeforeClosingOfClassBody() == this

    private fun ASTNode?.getLastCodeLeafBeforeClosingOfClassBody() =
        this
            ?.findParentByType(CLASS_BODY)
            ?.lastChildLeafOrSelf
            ?.prevCodeLeaf

    private fun ASTNode?.isEnumClassWithoutValues() =
        this
            ?.takeIf { !it.isLastCodeLeafBeforeClosingOfClassBody() }
            ?.findParentByType(CLASS_BODY)
            ?.takeIf { this == it.firstChildNode.nextCodeSibling }
            ?.findParentByType(CLASS)
            ?.hasModifier(ENUM_KEYWORD)
            ?: false
}

public val NO_SEMICOLONS_RULE_ID: RuleId = NoSemicolonsRule().ruleId
