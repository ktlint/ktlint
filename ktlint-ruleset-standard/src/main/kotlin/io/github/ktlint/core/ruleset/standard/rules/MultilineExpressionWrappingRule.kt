package io.github.ktlint.core.ruleset.standard.rules

import io.github.ktlint.core.rule.engine.core.api.AutocorrectDecision
import io.github.ktlint.core.rule.engine.core.api.ElementType.ARRAY_ACCESS_EXPRESSION
import io.github.ktlint.core.rule.engine.core.api.ElementType.ARROW
import io.github.ktlint.core.rule.engine.core.api.ElementType.BINARY_EXPRESSION
import io.github.ktlint.core.rule.engine.core.api.ElementType.BINARY_WITH_TYPE
import io.github.ktlint.core.rule.engine.core.api.ElementType.BLOCK
import io.github.ktlint.core.rule.engine.core.api.ElementType.CALL_EXPRESSION
import io.github.ktlint.core.rule.engine.core.api.ElementType.COMMA
import io.github.ktlint.core.rule.engine.core.api.ElementType.DOT_QUALIFIED_EXPRESSION
import io.github.ktlint.core.rule.engine.core.api.ElementType.ELVIS
import io.github.ktlint.core.rule.engine.core.api.ElementType.EQ
import io.github.ktlint.core.rule.engine.core.api.ElementType.FUN
import io.github.ktlint.core.rule.engine.core.api.ElementType.FUNCTION_LITERAL
import io.github.ktlint.core.rule.engine.core.api.ElementType.IF
import io.github.ktlint.core.rule.engine.core.api.ElementType.IS_EXPRESSION
import io.github.ktlint.core.rule.engine.core.api.ElementType.LAMBDA_EXPRESSION
import io.github.ktlint.core.rule.engine.core.api.ElementType.MUL
import io.github.ktlint.core.rule.engine.core.api.ElementType.OBJECT_LITERAL
import io.github.ktlint.core.rule.engine.core.api.ElementType.OPERATION_REFERENCE
import io.github.ktlint.core.rule.engine.core.api.ElementType.POSTFIX_EXPRESSION
import io.github.ktlint.core.rule.engine.core.api.ElementType.PREFIX_EXPRESSION
import io.github.ktlint.core.rule.engine.core.api.ElementType.REFERENCE_EXPRESSION
import io.github.ktlint.core.rule.engine.core.api.ElementType.REGULAR_STRING_PART
import io.github.ktlint.core.rule.engine.core.api.ElementType.RPAR
import io.github.ktlint.core.rule.engine.core.api.ElementType.SAFE_ACCESS_EXPRESSION
import io.github.ktlint.core.rule.engine.core.api.ElementType.TRY
import io.github.ktlint.core.rule.engine.core.api.ElementType.VALUE_ARGUMENT
import io.github.ktlint.core.rule.engine.core.api.ElementType.VALUE_ARGUMENT_LIST
import io.github.ktlint.core.rule.engine.core.api.ElementType.VALUE_PARAMETER_LIST
import io.github.ktlint.core.rule.engine.core.api.ElementType.WHEN
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
import io.github.ktlint.core.rule.engine.core.api.firstChildLeafOrSelf
import io.github.ktlint.core.rule.engine.core.api.ifAutocorrectAllowed
import io.github.ktlint.core.rule.engine.core.api.isPartOfComment
import io.github.ktlint.core.rule.engine.core.api.isWhiteSpaceWithNewline
import io.github.ktlint.core.rule.engine.core.api.isWhiteSpaceWithoutNewline
import io.github.ktlint.core.rule.engine.core.api.lastChildLeafOrSelf
import io.github.ktlint.core.rule.engine.core.api.leavesForwardsIncludingSelf
import io.github.ktlint.core.rule.engine.core.api.nextLeaf
import io.github.ktlint.core.rule.engine.core.api.parent
import io.github.ktlint.core.rule.engine.core.api.prevCodeLeaf
import io.github.ktlint.core.rule.engine.core.api.prevCodeSibling
import io.github.ktlint.core.rule.engine.core.api.prevLeaf
import io.github.ktlint.core.rule.engine.core.api.upsertWhitespaceBeforeMe
import io.github.ktlint.core.ruleset.standard.StandardRule
import io.github.ktlint.core.ruleset.standard.rules.FunctionSignatureRule.Companion.FUNCTION_BODY_EXPRESSION_WRAPPING_PROPERTY
import io.github.ktlint.core.ruleset.standard.rules.FunctionSignatureRule.FunctionBodyExpressionWrapping.DEFAULT
import org.jetbrains.kotlin.com.intellij.lang.ASTNode

/**
 * This rule wraps each multiline expression to a newline.
 */
@SinceKtlint("0.49", EXPERIMENTAL)
@SinceKtlint("1.0", STABLE)
public class MultilineExpressionWrappingRule :
    StandardRule(
        id = "multiline-expression-wrapping",
        usesEditorConfigProperties =
            setOf(
                INDENT_SIZE_PROPERTY,
                INDENT_STYLE_PROPERTY,
                FUNCTION_BODY_EXPRESSION_WRAPPING_PROPERTY,
            ),
    ),
    RuleV2.OfficialCodeStyle {
    private var indentConfig = DEFAULT_INDENT_CONFIG
    private lateinit var functionBodyExpressionWrapping: FunctionSignatureRule.FunctionBodyExpressionWrapping

    override fun beforeFirstNode(editorConfig: EditorConfig) {
        indentConfig =
            IndentConfig(
                indentStyle = editorConfig[INDENT_STYLE_PROPERTY],
                tabWidth = editorConfig[INDENT_SIZE_PROPERTY],
            )
        functionBodyExpressionWrapping = editorConfig[FUNCTION_BODY_EXPRESSION_WRAPPING_PROPERTY]
    }

    override fun beforeVisitChildNodes(
        node: ASTNode,
        emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> AutocorrectDecision,
    ) {
        if (node.elementType in CHAINABLE_EXPRESSION &&
            !node.isPartOfSpreadOperatorExpression() &&
            (node.parent?.elementType !in CHAINABLE_EXPRESSION || node.isRightHandSideOfBinaryExpression())
        ) {
            visitExpression(node, emit)
        }
        if (node.elementType == BINARY_EXPRESSION &&
            node.parent?.elementType != BINARY_EXPRESSION
        ) {
            visitExpression(node, emit)
        }
    }

    private fun ASTNode.isPartOfSpreadOperatorExpression() =
        prevCodeLeaf?.elementType == MUL &&
            parent?.elementType == VALUE_ARGUMENT

    private fun visitExpression(
        node: ASTNode,
        emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> AutocorrectDecision,
    ) {
        if (node.containsWhitespaceWithNewline() && node.needToWrapMultilineExpression()) {
            node
                .prevLeaf { !it.isPartOfComment }
                .takeUnless { it.isWhiteSpaceWithNewline }
                ?.let {
                    emit(node.startOffset, "A multiline expression should start on a new line", true)
                        .ifAutocorrectAllowed {
                            node.upsertWhitespaceBeforeMe(indentConfig.siblingIndentOf(node))
                            val leafOnSameLineAfterMultilineExpression =
                                node
                                    .lastChildLeafOrSelf
                                    .nextLeaf { !it.isWhiteSpaceWithoutNewline && !it.isPartOfComment }
                                    ?.takeIf { !it.isWhiteSpaceWithNewline }
                            when {
                                leafOnSameLineAfterMultilineExpression == null -> {
                                    Unit
                                }

                                leafOnSameLineAfterMultilineExpression.parent?.elementType == OPERATION_REFERENCE -> {
                                    // When binary expressions are wrapped, each binary expression for itself is checked whether it is a
                                    // multiline expression. So there is no need to check whether wrapping after the operation reference is
                                    // needed
                                    Unit
                                }

                                leafOnSameLineAfterMultilineExpression.elementType == COMMA &&
                                    (
                                        leafOnSameLineAfterMultilineExpression.parent?.elementType == VALUE_ARGUMENT_LIST ||
                                            leafOnSameLineAfterMultilineExpression.parent?.elementType == VALUE_PARAMETER_LIST
                                    ) -> {
                                    // Keep comma on same line as multiline expression:
                                    //   foo(
                                    //      fooBar
                                    //          .filter { it.bar },
                                    //   )
                                    leafOnSameLineAfterMultilineExpression
                                        .nextLeaf
                                        ?.upsertWhitespaceBeforeMe(indentConfig.siblingIndentOf(node))
                                }

                                else -> {
                                    leafOnSameLineAfterMultilineExpression.upsertWhitespaceBeforeMe(indentConfig.siblingIndentOf(node))
                                }
                            }
                        }
                }
        }
    }

    private fun ASTNode.containsWhitespaceWithNewline(): Boolean {
        val lastLeaf = lastChildLeafOrSelf
        return firstChildLeafOrSelf
            .leavesForwardsIncludingSelf
            .takeWhile { it != lastLeaf }
            .any { it.isWhiteSpaceWithNewline || it.isRegularStringPartWithNewline() }
    }

    private fun ASTNode.isRegularStringPartWithNewline() =
        elementType == REGULAR_STRING_PART &&
            text.startsWith("\n")

    private fun ASTNode.needToWrapMultilineExpression() =
        isValueInAnAssignment() ||
            isLambdaExpression() ||
            isValueArgument() ||
            isAfterArrow()

    private fun ASTNode.isValueInAnAssignment() =
        null !=
            prevCodeSibling
                ?.takeIf { it.elementType == EQ || it.elementType == OPERATION_REFERENCE }
                ?.takeUnless { functionBodyExpressionWrapping == DEFAULT && it.parent?.elementType == FUN }
                ?.takeUnless { it.isElvisOperator() }
                ?.takeUnless {
                    it
                        .closingParenthesisOfFunctionOrNull()
                        ?.prevLeaf
                        .isWhiteSpaceWithNewline
                }

    private fun ASTNode?.isElvisOperator() =
        this != null &&
            elementType == OPERATION_REFERENCE &&
            firstChildNode.elementType == ELVIS

    private fun ASTNode.closingParenthesisOfFunctionOrNull() =
        takeIf { parent?.elementType == FUN }
            ?.prevCodeLeaf
            ?.takeIf { it.elementType == RPAR }

    private fun ASTNode.isLambdaExpression() =
        null !=
            parent
                ?.takeIf {
                    // Function literals in lambda expression have an implicit block (no LBRACE and RBRACE). So only wrap when the node is
                    // the first node in the block
                    it.elementType == BLOCK && it.firstChildNode == this
                }?.parent
                ?.takeIf { it.elementType == FUNCTION_LITERAL }
                ?.parent
                ?.takeIf { it.elementType == LAMBDA_EXPRESSION }

    private fun ASTNode.isValueArgument() = parent?.elementType == VALUE_ARGUMENT

    private fun ASTNode.isAfterArrow() = prevCodeLeaf?.elementType == ARROW

    private fun ASTNode.isRightHandSideOfBinaryExpression() =
        null !=
            takeIf { it.parent?.elementType == BINARY_EXPRESSION }
                .takeIf { it?.prevCodeSibling?.elementType == OPERATION_REFERENCE }

    private companion object {
        // Based  on https://kotlinlang.org/spec/expressions.html#expressions
        val CHAINABLE_EXPRESSION =
            setOf(
                ARRAY_ACCESS_EXPRESSION,
                BINARY_WITH_TYPE,
                CALL_EXPRESSION,
                DOT_QUALIFIED_EXPRESSION,
                IF,
                IS_EXPRESSION,
                OBJECT_LITERAL,
                PREFIX_EXPRESSION,
                POSTFIX_EXPRESSION,
                REFERENCE_EXPRESSION,
                SAFE_ACCESS_EXPRESSION,
                TRY,
                WHEN,
            )
    }
}

public val MULTILINE_EXPRESSION_WRAPPING_RULE_ID: RuleId = MultilineExpressionWrappingRule().ruleId
