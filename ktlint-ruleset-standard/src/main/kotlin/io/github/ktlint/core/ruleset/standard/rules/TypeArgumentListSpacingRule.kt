package io.github.ktlint.core.ruleset.standard.rules

import com.intellij.lang.ASTNode
import io.github.ktlint.core.rule.engine.core.api.ElementType.CALL_EXPRESSION
import io.github.ktlint.core.rule.engine.core.api.ElementType.GT
import io.github.ktlint.core.rule.engine.core.api.ElementType.LAMBDA_ARGUMENT
import io.github.ktlint.core.rule.engine.core.api.ElementType.LT
import io.github.ktlint.core.rule.engine.core.api.ElementType.SUPER_EXPRESSION
import io.github.ktlint.core.rule.engine.core.api.ElementType.SUPER_TYPE_LIST
import io.github.ktlint.core.rule.engine.core.api.ElementType.TYPE_ARGUMENT_LIST
import io.github.ktlint.core.rule.engine.core.api.ElementType.TYPE_REFERENCE
import io.github.ktlint.core.rule.engine.core.api.ElementType.WHITE_SPACE
import io.github.ktlint.core.rule.engine.core.api.IndentConfig
import io.github.ktlint.core.rule.engine.core.api.RuleId
import io.github.ktlint.core.rule.engine.core.api.SinceKtlint
import io.github.ktlint.core.rule.engine.core.api.SinceKtlint.Status.EXPERIMENTAL
import io.github.ktlint.core.rule.engine.core.api.SinceKtlint.Status.STABLE
import io.github.ktlint.core.rule.engine.core.api.editorconfig.EditorConfig
import io.github.ktlint.core.rule.engine.core.api.editorconfig.INDENT_SIZE_PROPERTY
import io.github.ktlint.core.rule.engine.core.api.editorconfig.INDENT_STYLE_PROPERTY
import io.github.ktlint.core.rule.engine.core.api.findParentByType
import io.github.ktlint.core.rule.engine.core.api.isPartOf
import io.github.ktlint.core.rule.engine.core.api.isWhiteSpace
import io.github.ktlint.core.rule.engine.core.api.isWhiteSpaceWithoutNewline
import io.github.ktlint.core.rule.engine.core.api.nextLeaf
import io.github.ktlint.core.rule.engine.core.api.nextSibling
import io.github.ktlint.core.rule.engine.core.api.prevLeaf
import io.github.ktlint.core.rule.engine.core.api.prevSibling
import io.github.ktlint.core.rule.engine.core.api.remove
import io.github.ktlint.core.rule.engine.core.api.upsertWhitespaceAfterMe
import io.github.ktlint.core.rule.engine.core.api.upsertWhitespaceBeforeMe
import io.github.ktlint.core.ruleset.standard.StandardRule

/**
 * Lints and formats the spacing before and after the angle brackets of a type argument list.
 */
@SinceKtlint("0.45", EXPERIMENTAL)
@SinceKtlint("1.0", STABLE)
public class TypeArgumentListSpacingRule :
    StandardRule(
        id = "type-argument-list-spacing",
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
        when (node.elementType) {
            TYPE_ARGUMENT_LIST -> {
                node.visitFunctionDeclaration()
                node.visitInsideTypeArgumentList()
            }

            SUPER_TYPE_LIST, SUPER_EXPRESSION -> {
                node.visitInsideTypeArgumentList()
            }
        }
    }

    private fun ASTNode.visitFunctionDeclaration() {
        // No whitespace expected before type argument list of function call
        //    val list = listOf <String>()
        prevLeaf
            ?.takeIf { it.elementType == WHITE_SPACE }
            ?.noWhitespaceExpected()

        // No whitespace expected after type argument list of function call
        //    val list = listOf<String> ()
        takeUnless {
            // unless it is part of a type reference:
            //    fun foo(): List<Foo> { ... }
            //    var bar: List<Bar> = emptyList()
            it.isPartOf(TYPE_REFERENCE)
        }?.takeUnless {
            // unless it is part of a call expression followed by lambda:
            //    bar<Foo> { ... }
            it.isPartOfCallExpressionFollowedByLambda()
        }?.lastChildNode
            ?.nextLeaf
            ?.takeIf { it.elementType == WHITE_SPACE }
            ?.noWhitespaceExpected()
    }

    private fun ASTNode.visitInsideTypeArgumentList() {
        val multiline = textContains('\n')
        val expectedIndent =
            if (multiline) {
                indentConfig.childIndentOf(this)
            } else {
                indentConfig.siblingIndentOf(this)
            }

        findChildByType(LT)
            ?.nextSibling
            ?.let { nextSibling ->
                if (multiline) {
                    if (nextSibling.text != expectedIndent) {
                        if (nextSibling.isWhiteSpaceWithoutNewline) {
                            emit(nextSibling.startOffset, "Expected newline") {
                                nextSibling.upsertWhitespaceAfterMe(expectedIndent)
                            }
                        } else {
                            // Let Indentation rule fix the indentation
                        }
                    }
                } else {
                    if (nextSibling.isWhiteSpace) {
                        // Disallow
                        //    val list = listOf< String>()
                        nextSibling.noWhitespaceExpected()
                    }
                }
            }

        findChildByType(GT)
            ?.prevSibling
            ?.let { prevSibling ->
                if (multiline) {
                    if (prevSibling.text != expectedIndent) {
                        if (prevSibling.isWhiteSpaceWithoutNewline) {
                            emit(prevSibling.startOffset, "Expected newline") {
                                prevSibling.upsertWhitespaceBeforeMe(expectedIndent)
                            }
                        } else {
                            // Let Indentation rule fix the indentation
                        }
                    }
                } else {
                    if (prevSibling.isWhiteSpace) {
                        // Disallow
                        //    val list = listOf<String >()
                        prevSibling.noWhitespaceExpected()
                    }
                }
            }
    }

    private fun ASTNode.noWhitespaceExpected() {
        if (text != "") {
            emit(startOffset, "No whitespace expected at this position") { remove() }
        }
    }
}

private fun ASTNode.isPartOfCallExpressionFollowedByLambda(): Boolean =
    findParentByType(CALL_EXPRESSION)
        ?.takeIf { it.elementType == CALL_EXPRESSION }
        ?.findChildByType(LAMBDA_ARGUMENT)
        .let { it != null }

public val TYPE_ARGUMENT_LIST_SPACING_RULE_ID: RuleId = TypeArgumentListSpacingRule().ruleId
