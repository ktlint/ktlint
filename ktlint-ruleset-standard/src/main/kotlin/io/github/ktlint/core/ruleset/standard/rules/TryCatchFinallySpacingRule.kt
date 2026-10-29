package io.github.ktlint.core.ruleset.standard.rules

import com.intellij.lang.ASTNode
import com.intellij.psi.tree.TokenSet
import io.github.ktlint.core.rule.engine.core.api.ElementType.BLOCK
import io.github.ktlint.core.rule.engine.core.api.ElementType.CATCH
import io.github.ktlint.core.rule.engine.core.api.ElementType.FINALLY
import io.github.ktlint.core.rule.engine.core.api.ElementType.LBRACE
import io.github.ktlint.core.rule.engine.core.api.ElementType.RBRACE
import io.github.ktlint.core.rule.engine.core.api.ElementType.TRY
import io.github.ktlint.core.rule.engine.core.api.IndentConfig
import io.github.ktlint.core.rule.engine.core.api.IndentConfig.Companion.DEFAULT_INDENT_CONFIG
import io.github.ktlint.core.rule.engine.core.api.RuleId
import io.github.ktlint.core.rule.engine.core.api.RuleV2
import io.github.ktlint.core.rule.engine.core.api.SinceKtlint
import io.github.ktlint.core.rule.engine.core.api.SinceKtlint.Status.EXPERIMENTAL
import io.github.ktlint.core.rule.engine.core.api.SinceKtlint.Status.STABLE
import io.github.ktlint.core.rule.engine.core.api.editorconfig.EditorConfig
import io.github.ktlint.core.rule.engine.core.api.editorconfig.INDENT_SIZE_PROPERTY
import io.github.ktlint.core.rule.engine.core.api.editorconfig.INDENT_STYLE_PROPERTY
import io.github.ktlint.core.rule.engine.core.api.isPartOfComment
import io.github.ktlint.core.rule.engine.core.api.nextSibling
import io.github.ktlint.core.rule.engine.core.api.parent
import io.github.ktlint.core.rule.engine.core.api.prevLeaf
import io.github.ktlint.core.rule.engine.core.api.prevSibling
import io.github.ktlint.core.rule.engine.core.api.upsertWhitespaceAfterMe
import io.github.ktlint.core.rule.engine.core.api.upsertWhitespaceBeforeMe
import io.github.ktlint.core.ruleset.standard.StandardRule

/**
 * Checks spacing and wrapping of try-catch-finally.
 */
@SinceKtlint("0.49", EXPERIMENTAL)
@SinceKtlint("1.0", STABLE)
public class TryCatchFinallySpacingRule :
    StandardRule(
        id = "try-catch-finally-spacing",
        usesEditorConfigProperties =
            setOf(
                INDENT_SIZE_PROPERTY,
                INDENT_STYLE_PROPERTY,
            ),
    ),
    RuleV2.OfficialCodeStyle {
    private var indentConfig = DEFAULT_INDENT_CONFIG

    override fun beforeFirstNode(editorConfig: EditorConfig) {
        indentConfig =
            IndentConfig(
                indentStyle = editorConfig[INDENT_STYLE_PROPERTY],
                tabWidth = editorConfig[INDENT_SIZE_PROPERTY],
            )
    }

    override fun beforeVisitChildNodes(node: ASTNode) {
        if (node.isPartOfComment && node.parent?.elementType in TRY_CATCH_FINALLY_TOKEN_SET) {
            emit(node.startOffset, "No comment expected at this location")
            return
        }
        when (node.elementType) {
            BLOCK -> {
                node.visitBlock()
            }

            CATCH, FINALLY -> {
                node.visitClause()
            }
        }
    }

    private fun ASTNode.visitBlock() {
        if (parent?.elementType !in TRY_CATCH_FINALLY_TOKEN_SET) {
            return
        }

        findChildByType(LBRACE)!!
            .let { lbrace ->
                val nextSibling = lbrace.nextSibling { !it.isPartOfComment }!!
                if (!nextSibling.text.startsWith("\n")) {
                    emit(lbrace.startOffset + 1, "Expected a newline after '{'") {
                        lbrace.upsertWhitespaceAfterMe(indentConfig.siblingIndentOf(this))
                    }
                }
            }

        findChildByType(RBRACE)!!
            .let { rbrace ->
                val prevSibling = rbrace.prevSibling { !it.isPartOfComment }!!
                if (!prevSibling.text.startsWith("\n")) {
                    emit(rbrace.startOffset, "Expected a newline before '}'") {
                        rbrace.upsertWhitespaceBeforeMe(indentConfig.parentIndentOf(this))
                    }
                }
            }
    }

    private fun ASTNode.visitClause() {
        val prevLeaf = prevLeaf { !it.isPartOfComment }!!
        if (prevLeaf.text != " ") {
            emit(startOffset, "A single space is required before '${elementTypeName()}'") {
                upsertWhitespaceBeforeMe(" ")
            }
        }
    }

    private fun ASTNode.elementTypeName() = elementType.toString().lowercase()

    private companion object {
        val TRY_CATCH_FINALLY_TOKEN_SET = TokenSet.create(TRY, CATCH, FINALLY)
    }
}

public val TRY_CATCH_RULE_ID: RuleId = TryCatchFinallySpacingRule().ruleId
