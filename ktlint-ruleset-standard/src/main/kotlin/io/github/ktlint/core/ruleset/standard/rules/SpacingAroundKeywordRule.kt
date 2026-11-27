package io.github.ktlint.core.ruleset.standard.rules

import com.intellij.lang.ASTNode
import com.intellij.psi.tree.TokenSet.create
import io.github.ktlint.core.rule.engine.core.api.ElementType.BLOCK
import io.github.ktlint.core.rule.engine.core.api.ElementType.CATCH_KEYWORD
import io.github.ktlint.core.rule.engine.core.api.ElementType.DO_KEYWORD
import io.github.ktlint.core.rule.engine.core.api.ElementType.ELSE_KEYWORD
import io.github.ktlint.core.rule.engine.core.api.ElementType.FINALLY_KEYWORD
import io.github.ktlint.core.rule.engine.core.api.ElementType.FOR_KEYWORD
import io.github.ktlint.core.rule.engine.core.api.ElementType.GET_KEYWORD
import io.github.ktlint.core.rule.engine.core.api.ElementType.IF_KEYWORD
import io.github.ktlint.core.rule.engine.core.api.ElementType.KDOC_NAME
import io.github.ktlint.core.rule.engine.core.api.ElementType.PROPERTY_ACCESSOR
import io.github.ktlint.core.rule.engine.core.api.ElementType.RBRACE
import io.github.ktlint.core.rule.engine.core.api.ElementType.SET_KEYWORD
import io.github.ktlint.core.rule.engine.core.api.ElementType.TRY_KEYWORD
import io.github.ktlint.core.rule.engine.core.api.ElementType.VALUE_PARAMETER_LIST
import io.github.ktlint.core.rule.engine.core.api.ElementType.WHEN_ENTRY
import io.github.ktlint.core.rule.engine.core.api.ElementType.WHEN_KEYWORD
import io.github.ktlint.core.rule.engine.core.api.ElementType.WHILE_KEYWORD
import io.github.ktlint.core.rule.engine.core.api.RuleId
import io.github.ktlint.core.rule.engine.core.api.SinceKtlint
import io.github.ktlint.core.rule.engine.core.api.SinceKtlint.Status.STABLE
import io.github.ktlint.core.rule.engine.core.api.isWhiteSpace
import io.github.ktlint.core.rule.engine.core.api.isWhiteSpaceWithNewline
import io.github.ktlint.core.rule.engine.core.api.nextLeaf
import io.github.ktlint.core.rule.engine.core.api.parent
import io.github.ktlint.core.rule.engine.core.api.prevLeaf
import io.github.ktlint.core.rule.engine.core.api.remove
import io.github.ktlint.core.rule.engine.core.api.replaceTextWith
import io.github.ktlint.core.rule.engine.core.api.upsertWhitespaceAfterMe
import io.github.ktlint.core.ruleset.standard.StandardRule

@SinceKtlint("0.1", STABLE)
public class SpacingAroundKeywordRule : StandardRule("keyword-spacing") {
    private val noLFBeforeSet = create(ELSE_KEYWORD, CATCH_KEYWORD, FINALLY_KEYWORD)
    private val tokenSet =
        create(
            CATCH_KEYWORD,
            DO_KEYWORD,
            ELSE_KEYWORD,
            FINALLY_KEYWORD,
            FOR_KEYWORD,
            IF_KEYWORD,
            TRY_KEYWORD,
            WHEN_KEYWORD,
            WHILE_KEYWORD,
        )

    private val keywordsWithoutSpaces = create(GET_KEYWORD, SET_KEYWORD)

    override fun beforeVisitChildNodes(node: ASTNode) {
        if (node.elementType in tokenSet && node.parent?.elementType != KDOC_NAME && !node.nextLeaf.isWhiteSpace) {
            emit(node.startOffset + node.text.length, "Missing spacing after \"${node.text}\"") { node.upsertWhitespaceAfterMe(" ") }
        }
        node
            .takeIf { keywordsWithoutSpaces.contains(it.elementType) }
            .takeIf { node.isPropertyAccessorWithValueParameterList() }
            ?.nextLeaf
            ?.takeIf { it.isWhiteSpace }
            ?.also { nextLeaf ->
                emit(node.startOffset, "Unexpected spacing after \"${node.text}\"") { nextLeaf.remove() }
            }
        if (noLFBeforeSet.contains(node.elementType)) {
            val prevLeaf = node.prevLeaf
            val isElseKeyword = node.elementType == ELSE_KEYWORD
            if (prevLeaf.isWhiteSpaceWithNewline &&
                (!isElseKeyword || node.parent?.elementType != WHEN_ENTRY)
            ) {
                val rBrace = prevLeaf?.prevLeaf?.takeIf { it.elementType == RBRACE }
                rBrace
                    ?.parent
                    ?.takeIf { it.elementType == BLOCK }
                    ?.takeIf { !isElseKeyword || it.parent?.parent == node.parent }
                    ?.run {
                        emit(node.startOffset, "Unexpected newline before \"${node.text}\"") { prevLeaf.replaceTextWith(" ") }
                    }
            }
        }
    }

    private fun ASTNode.isPropertyAccessorWithValueParameterList() =
        null !=
            parent
                ?.takeIf { it.elementType == PROPERTY_ACCESSOR }
                ?.findChildByType(VALUE_PARAMETER_LIST)
}

public val SPACING_AROUND_KEYWORD_RULE_ID: RuleId = SpacingAroundKeywordRule().ruleId
