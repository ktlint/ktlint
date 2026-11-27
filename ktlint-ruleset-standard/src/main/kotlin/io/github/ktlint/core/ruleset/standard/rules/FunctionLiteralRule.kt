package io.github.ktlint.core.ruleset.standard.rules

import com.intellij.lang.ASTNode
import io.github.ktlint.core.logger.api.initKtLintKLogger
import io.github.ktlint.core.rule.engine.core.api.ElementType.ARROW
import io.github.ktlint.core.rule.engine.core.api.ElementType.BLOCK
import io.github.ktlint.core.rule.engine.core.api.ElementType.ELSE
import io.github.ktlint.core.rule.engine.core.api.ElementType.FUNCTION_LITERAL
import io.github.ktlint.core.rule.engine.core.api.ElementType.LAMBDA_ARGUMENT
import io.github.ktlint.core.rule.engine.core.api.ElementType.LAMBDA_EXPRESSION
import io.github.ktlint.core.rule.engine.core.api.ElementType.LBRACE
import io.github.ktlint.core.rule.engine.core.api.ElementType.RBRACE
import io.github.ktlint.core.rule.engine.core.api.ElementType.THEN
import io.github.ktlint.core.rule.engine.core.api.ElementType.VALUE_PARAMETER
import io.github.ktlint.core.rule.engine.core.api.ElementType.VALUE_PARAMETER_LIST
import io.github.ktlint.core.rule.engine.core.api.ElementType.WHEN_ENTRY
import io.github.ktlint.core.rule.engine.core.api.IndentConfig
import io.github.ktlint.core.rule.engine.core.api.RuleId
import io.github.ktlint.core.rule.engine.core.api.SinceKtlint
import io.github.ktlint.core.rule.engine.core.api.SinceKtlint.Status.EXPERIMENTAL
import io.github.ktlint.core.rule.engine.core.api.SinceKtlint.Status.STABLE
import io.github.ktlint.core.rule.engine.core.api.children
import io.github.ktlint.core.rule.engine.core.api.dropTrailingEolComment
import io.github.ktlint.core.rule.engine.core.api.editorconfig.CODE_STYLE_PROPERTY
import io.github.ktlint.core.rule.engine.core.api.editorconfig.EditorConfig
import io.github.ktlint.core.rule.engine.core.api.editorconfig.INDENT_SIZE_PROPERTY
import io.github.ktlint.core.rule.engine.core.api.editorconfig.INDENT_STYLE_PROPERTY
import io.github.ktlint.core.rule.engine.core.api.editorconfig.MAX_LINE_LENGTH_PROPERTY
import io.github.ktlint.core.rule.engine.core.api.findParentByType
import io.github.ktlint.core.rule.engine.core.api.firstChildLeafOrSelf
import io.github.ktlint.core.rule.engine.core.api.hasNoMaxLineLengthSuppression
import io.github.ktlint.core.rule.engine.core.api.isPartOfComment
import io.github.ktlint.core.rule.engine.core.api.isWhiteSpace
import io.github.ktlint.core.rule.engine.core.api.isWhiteSpaceWithNewline
import io.github.ktlint.core.rule.engine.core.api.isWhiteSpaceWithoutNewline
import io.github.ktlint.core.rule.engine.core.api.isWhiteSpaceWithoutNewlineOrNull
import io.github.ktlint.core.rule.engine.core.api.lastChildLeafOrSelf
import io.github.ktlint.core.rule.engine.core.api.leavesForwardsIncludingSelf
import io.github.ktlint.core.rule.engine.core.api.leavesOnLine
import io.github.ktlint.core.rule.engine.core.api.lineLength
import io.github.ktlint.core.rule.engine.core.api.nextCodeSibling
import io.github.ktlint.core.rule.engine.core.api.nextLeaf
import io.github.ktlint.core.rule.engine.core.api.nextSibling
import io.github.ktlint.core.rule.engine.core.api.parent
import io.github.ktlint.core.rule.engine.core.api.prevCodeSibling
import io.github.ktlint.core.rule.engine.core.api.prevLeaf
import io.github.ktlint.core.rule.engine.core.api.prevSibling
import io.github.ktlint.core.rule.engine.core.api.upsertWhitespaceAfterMe
import io.github.ktlint.core.rule.engine.core.api.upsertWhitespaceBeforeMe
import io.github.ktlint.core.ruleset.standard.StandardRule
import io.github.oshai.kotlinlogging.KotlinLogging
import org.jetbrains.kotlin.psi.psiUtil.siblings

private val LOGGER = KotlinLogging.logger {}.initKtLintKLogger()

/**
 * [Kotlin lang documentation](https://kotlinlang.org/docs/coding-conventions.html#lambdas):
 *
 * When declaring parameter names in a multiline lambda, put the names on the first line, followed by the arrow and the newline:
 *
 * ```
 * appendCommaSeparated(properties) { prop ->
 *     val propertyValue = prop.get(obj)  // ...
 * }
 * ```
 *
 * If the parameter list is too long to fit on a line, put the arrow on a separate line:
 *
 * ```
 * foo {
 *    context: Context,
 *    environment: Env
 *    ->
 *    context.configureEnv(environment)
 * }
 * ```
 */
private const val SPACE_AND_ARROW_LENGTH = " ->".length

@SinceKtlint("1.0", EXPERIMENTAL)
@SinceKtlint("1.3", STABLE)
public class FunctionLiteralRule :
    StandardRule(
        id = "function-literal",
        usesEditorConfigProperties =
            setOf(
                CODE_STYLE_PROPERTY,
                INDENT_SIZE_PROPERTY,
                INDENT_STYLE_PROPERTY,
                MAX_LINE_LENGTH_PROPERTY,
            ),
    ) {
    private var codeStyle = CODE_STYLE_PROPERTY.defaultValue
    private var indentConfig = IndentConfig.DEFAULT_INDENT_CONFIG
    private var maxLineLength = MAX_LINE_LENGTH_PROPERTY.defaultValue

    override fun beforeFirstNode(editorConfig: EditorConfig) {
        codeStyle = editorConfig[CODE_STYLE_PROPERTY]
        maxLineLength = editorConfig.maxLineLength()
        indentConfig =
            IndentConfig(
                indentStyle = editorConfig[INDENT_STYLE_PROPERTY],
                tabWidth = editorConfig[INDENT_SIZE_PROPERTY],
            )
        if (indentConfig.disabled) {
            stopTraversalOfAST()
        }
    }

    override fun beforeVisitChildNodes(node: ASTNode) {
        if (node.elementType == FUNCTION_LITERAL) {
            node.visitFunctionLiteral()
        }
    }

    private fun ASTNode.visitFunctionLiteral() {
        require(elementType == FUNCTION_LITERAL)
        if (findChildByType(VALUE_PARAMETER_LIST)?.hasEmptyParameterList() == true) {
            if (hasRedundantArrow()) {
                removeAllBetweenLBraceAndArrow()
            }
        } else {
            findChildByType(VALUE_PARAMETER_LIST)
                ?.visitValueParameterList()
        }
        findChildByType(BLOCK)
            ?.visitBlock()
    }

    private fun ASTNode.removeAllBetweenLBraceAndArrow() {
        findChildByType(ARROW)
            ?.let { arrow ->
                emit(arrow.startOffset, "Arrow is redundant when parameter list is empty") {
                    removeRange(findChildByType(LBRACE)!!.nextSibling!!, arrow.nextSibling)
                }
            }
    }

    private fun ASTNode.visitValueParameterList() {
        val valueParameters = children.filter { it.elementType == VALUE_PARAMETER }
        if (valueParameters.count() > 1 || wrapFirstParameterToNewline()) {
            if (textContains('\n') || doesNotFitOnSameLineAsStartOfFunctionLiteral()) {
                rewriteToMultilineParameterList()
            } else {
                rewriteToSingleLineParameterList()
            }
        } else {
            if (textContains('\n')) {
                // Allow:
                //    val foo = {
                //            bar:
                //                @Baz("baz")
                //                Bar
                //        ->
                //        bar()
                //    }
            } else {
                // Disallow:
                //    val foo = {
                //            bar ->
                //        bar()
                //    }
                //    val foo = { bar
                //        ->
                //        bar()
                //    }
                rewriteToSingleLineParameterList()
            }
        }
    }

    private fun ASTNode.doesNotFitOnSameLineAsStartOfFunctionLiteral(): Boolean {
        require(elementType == VALUE_PARAMETER_LIST && parent?.elementType == FUNCTION_LITERAL)
        val lineLength =
            lineLengthIncludingLbrace()
                .plus(1) // space before parameter list
                .plus(lengthOfParameterListWhenOnSingleLine())
                .plus(SPACE_AND_ARROW_LENGTH) // space after parameter list followed by ->
        return hasNoMaxLineLengthSuppression() && lineLength > maxLineLength
    }

    private fun ASTNode.lineLengthIncludingLbrace(): Int {
        require(elementType == VALUE_PARAMETER_LIST && parent?.elementType == FUNCTION_LITERAL)
        val lbrace = parent?.findChildByType(LBRACE)!!
        return lbrace
            .leavesOnLine
            .dropTrailingEolComment()
            .takeWhile { it.prevLeaf != lbrace }
            .lineLength
    }

    private fun ASTNode.lengthOfParameterListWhenOnSingleLine(): Int {
        require(elementType == VALUE_PARAMETER_LIST)
        val stopAtLeaf = lastChildLeafOrSelf.nextLeaf
        return firstChildLeafOrSelf
            .leavesForwardsIncludingSelf
            .takeWhile { it != stopAtLeaf }
            .joinToString(separator = "") {
                if (it.isWhiteSpace) {
                    // Eliminate newlines and redundant spaces
                    " "
                } else {
                    it.text
                }
            }.length
    }

    private fun ASTNode.exceedsMaxLineLength(): Boolean {
        require(elementType == BLOCK)
        val stopAtLeaf = nextSibling { it.elementType == RBRACE }
        return hasNoMaxLineLengthSuppression() &&
            maxLineLength <
            leavesOnLine
                .dropTrailingEolComment()
                .takeWhile { it.prevLeaf != stopAtLeaf }
                .lineLength
    }

    private fun ASTNode.wrapFirstParameterToNewline() =
        if (isFunctionLiteralLambdaWithNonEmptyValueParameterList() && hasNoMaxLineLengthSuppression()) {
            // Disallow when max line is exceeded:
            //    val foo = someCallExpression { someLongParameterName ->
            //        bar()
            //    }
            val stopAtLeaf =
                children
                    .first { it.elementType == VALUE_PARAMETER }
                    .lastChildLeafOrSelf
                    .nextLeaf { !it.isWhiteSpaceWithoutNewline && !it.isPartOfComment }
            leavesOnLine
                .dropTrailingEolComment()
                .takeWhile { it.prevLeaf != stopAtLeaf }
                .lineLength
                .let { it > maxLineLength }
        } else {
            false
        }

    private fun ASTNode.isFunctionLiteralLambdaWithNonEmptyValueParameterList() =
        takeIf { it.elementType == VALUE_PARAMETER_LIST }
            ?.takeIf { it.findChildByType(VALUE_PARAMETER) != null }
            ?.takeIf { it.parent?.elementType == FUNCTION_LITERAL }
            ?.parent
            ?.takeIf { it.parent?.elementType == LAMBDA_EXPRESSION }
            ?.parent
            ?.takeIf { it.parent?.elementType == LAMBDA_ARGUMENT }
            .let { it != null }

    private fun ASTNode.rewriteToMultilineParameterList() {
        require(elementType == VALUE_PARAMETER_LIST)
        children
            .filter { it.elementType == VALUE_PARAMETER }
            .forEach { it.wrapValueParameter() }
        parent
            ?.findChildByType(ARROW)
            ?.wrapArrow()
        parent
            ?.findChildByType(RBRACE)
            ?.wrapBeforeRbrace()
    }

    private fun ASTNode.wrapValueParameter() {
        require(elementType == VALUE_PARAMETER)
        if (prevLeaf.isWhiteSpaceWithoutNewline) {
            emit(startOffset, "Newline expected before parameter") {
                upsertWhitespaceBeforeMe(
                    indentConfig.childIndentOf(findParentByType(FUNCTION_LITERAL)!!),
                )
            }
        }
    }

    private fun ASTNode.wrapArrow() {
        wrapBeforeArrow()
        wrapAfterArrow()
    }

    private fun ASTNode.wrapBeforeArrow() {
        require(elementType == ARROW)
        prevLeaf
            .takeIf { it.isWhiteSpaceWithoutNewline }
            ?.let {
                emit(startOffset, "Newline expected before arrow") {
                    upsertWhitespaceBeforeMe(indentConfig.childIndentOf(parent!!))
                }
            }
    }

    private fun ASTNode.wrapAfterArrow() {
        require(elementType == ARROW)
        nextLeaf
            .takeIf { it.isWhiteSpaceWithoutNewlineOrNull }
            ?.let {
                emit(startOffset + textLength - 1, "Newline expected after arrow") {
                    upsertWhitespaceAfterMe(indentConfig.siblingIndentOf(this))
                }
            }
    }

    private fun ASTNode.wrapBeforeRbrace() {
        require(elementType == RBRACE)
        prevLeaf
            .takeIf { it.isWhiteSpaceWithoutNewlineOrNull }
            ?.let {
                emit(startOffset, "Newline expected before closing brace") {
                    upsertWhitespaceBeforeMe(indentConfig.parentIndentOf(this))
                }
            }
    }

    private fun ASTNode.rewriteToSingleLineParameterList() {
        require(elementType == VALUE_PARAMETER_LIST)
        takeUnless { it.isPrecededByComment() }
            ?.prevSibling { it.isWhiteSpaceWithNewline }
            ?.let { whitespaceBeforeParameterList ->
                emit(startOffset, "No newline expected before parameter") {
                    whitespaceBeforeParameterList.upsertWhitespaceBeforeMe(" ")
                }
            }
        nextSibling { it.isWhiteSpace }
            ?.takeIf { it.isWhiteSpaceWithNewline }
            ?.let { whitespaceAfterParameterList ->
                emit(startOffset + textLength, "No newline expected after parameter") {
                    whitespaceAfterParameterList.upsertWhitespaceAfterMe(" ")
                }
            }
    }

    private fun ASTNode.isPrecededByComment() = siblings(forward = false).any { it.isPartOfComment }

    private fun ASTNode.hasRedundantArrow(): Boolean {
        require(elementType == FUNCTION_LITERAL)
        return isNotLambdaArgument() &&
            isLambdaExpressionWrappedInBlock()
    }

    private fun ASTNode.hasEmptyParameterList(): Boolean {
        require(elementType == VALUE_PARAMETER_LIST)
        return findChildByType(VALUE_PARAMETER) == null
    }

    private fun ASTNode.isNotLambdaArgument(): Boolean =
        // For a lambda argument it can not be determined without resolving the return type of the reference expression whether the arrow is
        // needed or not (see #3415). Removing the arrow could result in a compilation error.
        parent
            ?.takeIf { it.elementType == LAMBDA_EXPRESSION }
            ?.let { parent?.elementType != LAMBDA_ARGUMENT }
            ?: true

    private fun ASTNode.isLambdaExpressionWrappedInBlock(): Boolean {
        require(elementType == FUNCTION_LITERAL)
        return findChildByType(ARROW)
            ?.findParentByType(LAMBDA_EXPRESSION)
            ?.parent
            ?.takeIf { it.elementType == BLOCK }
            ?.parent
            ?.let {
                // Allow:
                //     val foo = when {
                //         1 == 2 -> { -> "hi" }
                //         else -> { -> "ho" }
                //     }
                // or
                //     val foo = if (cond) { -> "hi" } else { -> "ho" } parent ->
                it.elementType == WHEN_ENTRY || it.elementType == THEN || it.elementType == ELSE
            }
            ?: false
    }

    private fun ASTNode.visitBlock() {
        require(elementType == BLOCK)
        if (textContains('\n') || exceedsMaxLineLength()) {
            prevCodeSibling
                ?.let { prevCodeSibling ->
                    when (prevCodeSibling.elementType) {
                        ARROW -> prevCodeSibling.wrapAfterArrow()
                        LBRACE -> prevCodeSibling.wrapAfterLbrace()
                        else -> LOGGER.debug { "Unexpected type of element ${prevCodeSibling.elementType}" }
                    }
                }

            nextCodeSibling
                ?.takeIf { it.elementType == RBRACE }
                ?.wrapBeforeRbrace()
        }
    }

    private fun ASTNode.wrapAfterLbrace() {
        require(elementType == LBRACE)
        nextLeaf
            .takeIf { it.isWhiteSpace }
            .let { whitespaceAfterLbrace ->
                if (whitespaceAfterLbrace.isWhiteSpaceWithoutNewlineOrNull) {
                    emit(startOffset, "Newline expected after opening brace") {
                        upsertWhitespaceAfterMe(indentConfig.childIndentOf(this))
                    }
                }
            }
    }
}

public val FUNCTION_LITERAL_RULE_ID: RuleId = FunctionLiteralRule().ruleId
