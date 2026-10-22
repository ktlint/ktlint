package io.github.ktlint.core.ruleset.standard.rules

import com.intellij.lang.ASTNode
import com.intellij.psi.tree.IElementType
import io.github.ktlint.core.rule.engine.core.api.ElementType.BINARY_EXPRESSION
import io.github.ktlint.core.rule.engine.core.api.ElementType.CALL_EXPRESSION
import io.github.ktlint.core.rule.engine.core.api.ElementType.CONDITION
import io.github.ktlint.core.rule.engine.core.api.ElementType.ELVIS
import io.github.ktlint.core.rule.engine.core.api.ElementType.EQ
import io.github.ktlint.core.rule.engine.core.api.ElementType.FUN
import io.github.ktlint.core.rule.engine.core.api.ElementType.FUNCTION_LITERAL
import io.github.ktlint.core.rule.engine.core.api.ElementType.LAMBDA_ARGUMENT
import io.github.ktlint.core.rule.engine.core.api.ElementType.LAMBDA_EXPRESSION
import io.github.ktlint.core.rule.engine.core.api.ElementType.LBRACE
import io.github.ktlint.core.rule.engine.core.api.ElementType.LONG_STRING_TEMPLATE_ENTRY
import io.github.ktlint.core.rule.engine.core.api.ElementType.OPERATION_REFERENCE
import io.github.ktlint.core.rule.engine.core.api.ElementType.PROPERTY
import io.github.ktlint.core.rule.engine.core.api.ElementType.RBRACE
import io.github.ktlint.core.rule.engine.core.api.ElementType.VALUE_ARGUMENT
import io.github.ktlint.core.rule.engine.core.api.IndentConfig
import io.github.ktlint.core.rule.engine.core.api.RuleId
import io.github.ktlint.core.rule.engine.core.api.SinceKtlint
import io.github.ktlint.core.rule.engine.core.api.SinceKtlint.Status.EXPERIMENTAL
import io.github.ktlint.core.rule.engine.core.api.SinceKtlint.Status.STABLE
import io.github.ktlint.core.rule.engine.core.api.children
import io.github.ktlint.core.rule.engine.core.api.dropTrailingEolComment
import io.github.ktlint.core.rule.engine.core.api.editorconfig.EditorConfig
import io.github.ktlint.core.rule.engine.core.api.editorconfig.INDENT_SIZE_PROPERTY
import io.github.ktlint.core.rule.engine.core.api.editorconfig.INDENT_STYLE_PROPERTY
import io.github.ktlint.core.rule.engine.core.api.editorconfig.MAX_LINE_LENGTH_PROPERTY
import io.github.ktlint.core.rule.engine.core.api.firstChildLeafOrSelf
import io.github.ktlint.core.rule.engine.core.api.hasNoMaxLineLengthSuppression
import io.github.ktlint.core.rule.engine.core.api.isCode
import io.github.ktlint.core.rule.engine.core.api.isLeaf
import io.github.ktlint.core.rule.engine.core.api.isWhiteSpace
import io.github.ktlint.core.rule.engine.core.api.isWhiteSpaceWithNewline
import io.github.ktlint.core.rule.engine.core.api.lastChildLeafOrSelf
import io.github.ktlint.core.rule.engine.core.api.leavesOnLine
import io.github.ktlint.core.rule.engine.core.api.lineLength
import io.github.ktlint.core.rule.engine.core.api.nextLeaf
import io.github.ktlint.core.rule.engine.core.api.nextSibling
import io.github.ktlint.core.rule.engine.core.api.noNewLineInClosedRange
import io.github.ktlint.core.rule.engine.core.api.parent
import io.github.ktlint.core.rule.engine.core.api.prevLeaf
import io.github.ktlint.core.rule.engine.core.api.prevSibling
import io.github.ktlint.core.rule.engine.core.api.upsertWhitespaceAfterMe
import io.github.ktlint.core.rule.engine.core.api.upsertWhitespaceBeforeMe
import io.github.ktlint.core.ruleset.standard.StandardRule

/**
 * Wraps a binary expression whenever the expression does not fit on the line. Wrapping a binary expression should take precedence before
 * argument of function calls inside that binary expression are wrapped.
 */
@SinceKtlint("0.50", EXPERIMENTAL)
@SinceKtlint("1.3", STABLE)
public class BinaryExpressionWrappingRule :
    StandardRule(
        id = "binary-expression-wrapping",
        usesEditorConfigProperties =
            setOf(
                INDENT_SIZE_PROPERTY,
                INDENT_STYLE_PROPERTY,
                MAX_LINE_LENGTH_PROPERTY,
            ),
    ) {
    private var indentConfig = IndentConfig.DEFAULT_INDENT_CONFIG
    private var maxLineLength = MAX_LINE_LENGTH_PROPERTY.defaultValue

    override fun beforeFirstNode(editorConfig: EditorConfig) {
        indentConfig =
            IndentConfig(
                indentStyle = editorConfig[INDENT_STYLE_PROPERTY],
                tabWidth = editorConfig[INDENT_SIZE_PROPERTY],
            )
        maxLineLength = editorConfig.maxLineLength()
    }

    override fun beforeVisitChildNodes(node: ASTNode) {
        when (node.elementType) {
            BINARY_EXPRESSION -> node.visitBinaryExpression()
        }
    }

    private fun ASTNode.visitBinaryExpression() {
        require(elementType == BINARY_EXPRESSION)

        // First check whether the entire expression has to be pushed to the next line after and assignment in a property or function
        takeIf { it.parent!!.elementType.anyOf(PROPERTY, FUN) }
            ?.takeIf { binaryExpression ->
                binaryExpression
                    .prevSibling { it.elementType == EQ }
                    ?.let { noNewLineInClosedRange(it, binaryExpression.firstChildLeafOrSelf) }
                    ?: false
            }?.takeIf { it.isOnLineExceedingMaxLineLength() }
            ?.let { expression ->
                emit(expression.startOffset, "Line is exceeding max line length. Break line between assignment and expression") {
                    expression.upsertWhitespaceBeforeMe(indentConfig.childIndentOf(expression))
                }
            }

        // Prefer to wrap the entire binary expression to a newline instead of wrapping the binary expression at the operation reference.
        // E.g. prefer:
        //     fooBar(
        //         "foooooo" + "bar",
        //     )
        // instead of
        //     fooBar("foooooo" +
        //         "bar")
        takeIf { it.parent?.elementType == VALUE_ARGUMENT }
            ?.takeUnless {
                // Allow
                //     fooBar(
                //         "tooLongToFitOnSingleLine" +
                //             "bar",
                //     )
                prevLeaf.isWhiteSpaceWithNewline
            }?.takeIf { it.causesMaxLineLengthToBeExceeded() }
            ?.let { expression ->
                emit(expression.startOffset, "Line is exceeding max line length. Break line before expression") {
                    expression.upsertWhitespaceBeforeMe(indentConfig.childIndentOf(expression))
                }
            }

        // When left hand side is a call expression which causes the max line length to be exceeded then first wrap that expression
        children
            .firstOrNull { !it.isLeaf || !it.isCode }
            ?.takeIf { it.elementType == CALL_EXPRESSION }
            ?.takeIf { it.causesMaxLineLengthToBeExceeded() }
            ?.visitCallExpression()

        // The remainder (operation reference plus right hand side) might still cause the max line length to be exceeded
        takeIf { lastChildNode.causesMaxLineLengthToBeExceeded() || isPartOfConditionExceedingMaxLineLength() }
            ?.findChildByType(OPERATION_REFERENCE)
            ?.visitOperationReference()
    }

    private fun ASTNode.isPartOfConditionExceedingMaxLineLength() =
        // Checks that when binary expression itself fits on the line, but the closing parenthesis or opening brace does not fit.
        //   // Suppose that X is the last possible character on the
        //   // line                                             X
        //   if (leftHandSideExpression && rightHandSideExpression) {
        parent
            ?.takeIf { it.elementType == CONDITION }
            ?.lastChildLeafOrSelf
            ?.nextLeaf { it.isWhiteSpaceWithNewline }
            ?.prevLeaf
            ?.causesMaxLineLengthToBeExceeded()
            ?: false

    private fun ASTNode.visitCallExpression() {
        takeIf { it.elementType == CALL_EXPRESSION }
            ?.takeIf { it.parent?.elementType == BINARY_EXPRESSION }
            ?.let { callExpression ->
                // Breaking the lambda expression has priority over breaking value arguments
                callExpression
                    .findChildByType(LAMBDA_ARGUMENT)
                    ?.findChildByType(LAMBDA_EXPRESSION)
                    ?.findChildByType(FUNCTION_LITERAL)
                    ?.let { functionLiteral ->
                        functionLiteral
                            .findChildByType(LBRACE)
                            ?.let { lbrace ->
                                emit(lbrace.startOffset + 1, "Newline expected after '{'") {
                                    lbrace.upsertWhitespaceAfterMe(indentConfig.childIndentOf(lbrace.parent!!))
                                }
                            }
                        functionLiteral
                            .findChildByType(RBRACE)
                            ?.let { rbrace ->
                                emit(rbrace.startOffset, "Newline expected before '}'") {
                                    rbrace.upsertWhitespaceBeforeMe(indentConfig.siblingIndentOf(parent!!))
                                }
                            }
                    }
            }
    }

    private fun ASTNode.visitOperationReference() {
        takeIf { it.elementType == OPERATION_REFERENCE }
            ?.takeUnless {
                // Allow:
                //   val foo = "string too long to fit on the line" +
                //       "more text"
                it.nextSibling.isWhiteSpaceWithNewline
            }?.takeIf { it.parent?.elementType == BINARY_EXPRESSION }
            ?.takeIf { binaryExpression ->
                // Ignore binary expression inside raw string literals. Raw string literals are allowed to exceed max-line-length. Wrapping
                // (each) binary expression inside such a literal seems to create more chaos than it resolves.
                binaryExpression.parent { it.elementType == LONG_STRING_TEMPLATE_ENTRY } == null
            }?.let { operationReference ->
                if (operationReference.firstChildNode.elementType == ELVIS) {
                    operationReference
                        .prevLeaf { it.isWhiteSpace }
                        .takeUnless { it.isWhiteSpaceWithNewline }
                        ?.let {
                            // Wrapping after the elvis operator leads to violating the 'chain-wrapping' rule, so it must be wrapped itself
                            emit(operationReference.startOffset, "Line is exceeding max line length. Break line before '?:'") {
                                operationReference.upsertWhitespaceBeforeMe(indentConfig.childIndentOf(operationReference))
                            }
                        }
                } else {
                    operationReference
                        .nextSibling
                        ?.let { nextSibling ->
                            emit(
                                nextSibling.startOffset,
                                "Line is exceeding max line length. Break line after '${operationReference.text}' in binary expression",
                            ) {
                                nextSibling.upsertWhitespaceBeforeMe(indentConfig.childIndentOf(operationReference))
                            }
                        }
                }
            }
    }

    private fun ASTNode.isOnLineExceedingMaxLineLength() =
        hasNoMaxLineLengthSuppression() && maxLineLength < leavesOnLine.dropTrailingEolComment().lineLength

    private fun ASTNode.causesMaxLineLengthToBeExceeded() =
        hasNoMaxLineLengthSuppression() &&
            lastChildLeafOrSelf
                .let { lastChildLeaf ->
                    leavesOnLine
                        .dropTrailingEolComment()
                        .takeWhile { it.prevLeaf != lastChildLeaf }
                        .lineLength
                } > maxLineLength
}

private fun IElementType.anyOf(vararg elementType: IElementType): Boolean = elementType.contains(this)

public val BINARY_EXPRESSION_WRAPPING_RULE_ID: RuleId = BinaryExpressionWrappingRule().ruleId
