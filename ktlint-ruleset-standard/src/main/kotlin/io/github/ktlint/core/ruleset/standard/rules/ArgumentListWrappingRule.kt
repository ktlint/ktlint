package io.github.ktlint.core.ruleset.standard.rules

import com.intellij.lang.ASTNode
import com.intellij.psi.impl.source.tree.PsiWhiteSpaceImpl
import io.github.ktlint.core.rule.engine.core.api.ElementType.BINARY_EXPRESSION
import io.github.ktlint.core.rule.engine.core.api.ElementType.COLLECTION_LITERAL_EXPRESSION
import io.github.ktlint.core.rule.engine.core.api.ElementType.DOT_QUALIFIED_EXPRESSION
import io.github.ktlint.core.rule.engine.core.api.ElementType.EQ
import io.github.ktlint.core.rule.engine.core.api.ElementType.FUNCTION_LITERAL
import io.github.ktlint.core.rule.engine.core.api.ElementType.LPAR
import io.github.ktlint.core.rule.engine.core.api.ElementType.OPERATION_REFERENCE
import io.github.ktlint.core.rule.engine.core.api.ElementType.RPAR
import io.github.ktlint.core.rule.engine.core.api.ElementType.TYPE_ARGUMENT_LIST
import io.github.ktlint.core.rule.engine.core.api.ElementType.VALUE_ARGUMENT
import io.github.ktlint.core.rule.engine.core.api.ElementType.VALUE_ARGUMENT_LIST
import io.github.ktlint.core.rule.engine.core.api.IndentConfig
import io.github.ktlint.core.rule.engine.core.api.RuleId
import io.github.ktlint.core.rule.engine.core.api.SinceKtlint
import io.github.ktlint.core.rule.engine.core.api.SinceKtlint.Status.STABLE
import io.github.ktlint.core.rule.engine.core.api.TokenSets
import io.github.ktlint.core.rule.engine.core.api.editorconfig.EditorConfig
import io.github.ktlint.core.rule.engine.core.api.editorconfig.EditorConfigProperty
import io.github.ktlint.core.rule.engine.core.api.editorconfig.INDENT_SIZE_PROPERTY
import io.github.ktlint.core.rule.engine.core.api.editorconfig.INDENT_STYLE_PROPERTY
import io.github.ktlint.core.rule.engine.core.api.editorconfig.MAX_LINE_LENGTH_PROPERTY
import io.github.ktlint.core.rule.engine.core.api.firstChildLeafOrSelf
import io.github.ktlint.core.rule.engine.core.api.hasNoMaxLineLengthSuppression
import io.github.ktlint.core.rule.engine.core.api.indentWithoutNewlinePrefix
import io.github.ktlint.core.rule.engine.core.api.isPartOfComment
import io.github.ktlint.core.rule.engine.core.api.isWhiteSpace
import io.github.ktlint.core.rule.engine.core.api.isWhiteSpaceWithNewline
import io.github.ktlint.core.rule.engine.core.api.leavesOnLine
import io.github.ktlint.core.rule.engine.core.api.lineLength
import io.github.ktlint.core.rule.engine.core.api.nextSibling
import io.github.ktlint.core.rule.engine.core.api.parent
import io.github.ktlint.core.rule.engine.core.api.prevLeaf
import io.github.ktlint.core.rule.engine.core.api.replaceTextWith
import io.github.ktlint.core.ruleset.standard.StandardRule
import org.ec4j.core.model.PropertyType
import org.jetbrains.kotlin.psi.psiUtil.children

/**
 * https://kotlinlang.org/docs/reference/coding-conventions.html#method-call-formatting
 *
 * The rule is more aggressive in inserting newlines after arguments than mentioned in the styleguide:
 * Each argument should be on a separate line if
 * - at least one of the arguments is
 * - maxLineLength exceeded (and separating arguments with \n would actually help)
 * in addition, "(" and ")" must be on separates line if any of the arguments are (otherwise on the same)
 */
@SinceKtlint("0.1", STABLE)
public class ArgumentListWrappingRule :
    StandardRule(
        id = "argument-list-wrapping",
        usesEditorConfigProperties =
            setOf(
                IGNORE_WHEN_PARAMETER_COUNT_GREATER_OR_EQUAL_THAN_PROPERTY,
                INDENT_SIZE_PROPERTY,
                INDENT_STYLE_PROPERTY,
                MAX_LINE_LENGTH_PROPERTY,
            ),
    ) {
    private var editorConfigIndent = IndentConfig.DEFAULT_INDENT_CONFIG

    private var maxLineLength = MAX_LINE_LENGTH_PROPERTY.defaultValue
    private var ignoreWhenParameterCountGreaterOrEqualThanProperty = UNSET_IGNORE_WHEN_PARAMETER_COUNT_GREATER_OR_EQUAL_THAN_PROPERTY

    override fun beforeFirstNode(editorConfig: EditorConfig) {
        editorConfigIndent =
            IndentConfig(
                indentStyle = editorConfig[INDENT_STYLE_PROPERTY],
                tabWidth = editorConfig[INDENT_SIZE_PROPERTY],
            )
        maxLineLength = editorConfig.maxLineLength()
        ignoreWhenParameterCountGreaterOrEqualThanProperty = editorConfig[IGNORE_WHEN_PARAMETER_COUNT_GREATER_OR_EQUAL_THAN_PROPERTY]
    }

    override fun beforeVisitChildNodes(node: ASTNode) {
        if (editorConfigIndent.disabled) {
            return
        }

        if (node.elementType == VALUE_ARGUMENT_LIST) {
            if (needToWrapArgumentList(node)) {
                node
                    .children()
                    .forEach { child -> wrapArgumentInList(child) }
            }
        }
    }

    private fun needToWrapArgumentList(node: ASTNode) =
        if ( // skip when there are no arguments
            node.firstChildNode?.nextSibling?.elementType != RPAR &&
            // skip lambda arguments
            node.parent?.elementType != FUNCTION_LITERAL &&
            // skip if number of arguments is big
            node.children().count { it.elementType == VALUE_ARGUMENT } <= ignoreWhenParameterCountGreaterOrEqualThanProperty
        ) {
            // each argument should be on a separate line if
            // - at least one of the arguments is
            // - maxLineLength exceeded (and separating arguments with \n would actually help)
            // in addition, "(" and ")" must be on separates line if any of the arguments are (otherwise on the same)
            node.textContainsIgnoringLambda('\n') || node.exceedsMaxLineLength()
        } else {
            false
        }

    private fun ASTNode.exceedsMaxLineLength(): Boolean {
        if (textContains('\n')) return false
        require(this.elementType == VALUE_ARGUMENT_LIST)
        val stopAtLeaf = findChildByType(RPAR)?.firstChildLeafOrSelf
        return hasNoMaxLineLengthSuppression() &&
            maxLineLength < leavesOnLine.takeWhile { it.prevLeaf != stopAtLeaf }.lineLength
    }

    private fun intendedIndent(child: ASTNode): String =
        when {
            // IDEA quirk:
            // generic<
            //     T,
            //     R>(
            //     1,
            //     2
            // )
            // instead of
            // generic<
            //     T,
            //     R>(
            //         1,
            //         2
            //     )
            child.parent!!.hasTypeArgumentListInFront() -> -1

            // IDEA quirk:
            // foo
            //     .bar = Baz(
            //     1,
            //     2
            // )
            // instead of
            // foo
            //     .bar = Baz(
            //         1,
            //         2
            //     )
            child.parent!!.isPartOfDotQualifiedAssignmentExpression() -> -1

            else -> 0
        }.let {
            if (child.parent!!.isOnSameLineAsControlFlowKeyword()) {
                it + 1
            } else {
                it
            }
        }.let {
            if (child.elementType == VALUE_ARGUMENT) {
                it + 1
            } else {
                it
            }
        }.let { indentLevelFix ->
            val indentLevel =
                editorConfigIndent
                    .indentLevelFrom(child.parent!!.indentWithoutNewlinePrefix)
                    .plus(indentLevelFix)
            "\n" + editorConfigIndent.indent.repeat(maxOf(indentLevel, 0))
        }

    private fun wrapArgumentInList(child: ASTNode) {
        when (child.elementType) {
            LPAR -> {
                child
                    .prevLeaf
                    ?.let { prevLeaf ->
                        if (prevLeaf.isWhiteSpaceWithNewline) {
                            emit(child.startOffset, errorMessage(child)) {
                                prevLeaf.psi.delete()
                            }
                        }
                    }
            }

            VALUE_ARGUMENT,
            RPAR,
            -> {
                // aiming for
                // ... LPAR
                // <line indent + indentSize> VALUE_PARAMETER...
                // <line indent> RPAR
                val intendedIndent = intendedIndent(child)
                val prevLeaf = child.prevWhiteSpaceWithNewLine() ?: child.prevLeaf
                when {
                    prevLeaf.isWhiteSpaceWithNewline -> {
                        // The current child is already wrapped to a new line. Checking and fixing the
                        // correct size of the indent is the responsibility of the IndentationRule.
                        return
                    }

                    prevLeaf.isWhiteSpace -> {
                        // The current child needs to be wrapped to a newline.
                        emit(child.startOffset, errorMessage(child)) {
                            // The indentation is purely based on the previous leaf only. Note that in
                            // autoCorrect mode the indent rule, if enabled, runs after this rule and
                            // determines the final indentation. But if the indent rule is disabled then the
                            // indent of this rule is kept.
                            prevLeaf?.replaceTextWith(intendedIndent)
                        }
                    }

                    else -> {
                        // Insert a new whitespace element in order to wrap the current child to a new line.
                        emit(child.startOffset, errorMessage(child)) {
                            child.parent?.addChild(PsiWhiteSpaceImpl(intendedIndent), child)
                        }
                    }
                }
                // Indentation of child nodes need to be fixed by the IndentationRule.
            }
        }
    }

    private fun errorMessage(node: ASTNode) =
        when (node.elementType) {
            LPAR -> """Unnecessary newline before "(""""
            VALUE_ARGUMENT -> "Argument should be on a separate line (unless all arguments can fit a single line)"
            RPAR -> """Missing newline before ")""""
            else -> throw UnsupportedOperationException()
        }

    private fun ASTNode.textContainsIgnoringLambda(char: Char): Boolean =
        children().any { child ->
            child.isWhitespaceContaining(char) ||
                child.isCollectionLiteralContaining(char) ||
                child.isValueArgumentContaining(char)
        }

    private fun ASTNode.isWhitespaceContaining(char: Char) = isWhiteSpace && textContains(char)

    private fun ASTNode.isCollectionLiteralContaining(char: Char) = elementType == COLLECTION_LITERAL_EXPRESSION && textContains(char)

    private fun ASTNode.isValueArgumentContaining(char: Char) =
        elementType == VALUE_ARGUMENT && children().any { it.textContainsIgnoringLambda(char) }

    private fun ASTNode.hasTypeArgumentListInFront(): Boolean =
        parent
            ?.findChildByType(TYPE_ARGUMENT_LIST)
            ?.children()
            ?.any { it.isWhiteSpaceWithNewline } == true

    private fun ASTNode.isPartOfDotQualifiedAssignmentExpression(): Boolean =
        parent
            ?.parent
            ?.takeIf { it.elementType == BINARY_EXPRESSION }
            ?.let { binaryExpression ->
                binaryExpression.firstChildNode.elementType == DOT_QUALIFIED_EXPRESSION &&
                    binaryExpression.findChildByType(OPERATION_REFERENCE)?.firstChildNode?.elementType == EQ
            }
            ?: false

    private fun ASTNode.prevWhiteSpaceWithNewLine(): ASTNode? {
        var prev = prevLeaf
        while (prev != null && (prev.isWhiteSpace || prev.isPartOfComment)) {
            if (prev.isWhiteSpaceWithNewline) {
                return prev
            }
            prev = prev.prevLeaf
        }
        return null
    }

    private fun ASTNode.isOnSameLineAsControlFlowKeyword(): Boolean {
        val prevLeaf = prevLeaf { it.isWhiteSpaceWithNewline || it.elementType in TokenSets.CONTROL_FLOW_KEYWORDS }
        return prevLeaf != null && !prevLeaf.isWhiteSpaceWithNewline
    }

    public companion object {
        private const val UNSET_IGNORE_WHEN_PARAMETER_COUNT_GREATER_OR_EQUAL_THAN_PROPERTY = Int.MAX_VALUE
        public val IGNORE_WHEN_PARAMETER_COUNT_GREATER_OR_EQUAL_THAN_PROPERTY: EditorConfigProperty<Int> =
            EditorConfigProperty(
                type =
                    PropertyType.LowerCasingPropertyType(
                        "ktlint_argument_list_wrapping_ignore_when_parameter_count_greater_or_equal_than",
                        "Do not wrap parameters on separate lines when at least the specified number of parameters are " +
                            "specified. Use 'unset' to always wrap each parameter.",
                        PropertyType.PropertyValueParser.POSITIVE_INT_VALUE_PARSER,
                        setOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "unset"),
                    ),
                // Historically, all code styles have used 8 as the magic value.
                defaultValue = 8,
                ktlintOfficialCodeStyleDefaultValue = UNSET_IGNORE_WHEN_PARAMETER_COUNT_GREATER_OR_EQUAL_THAN_PROPERTY,
                propertyMapper = { property, _ ->
                    if (property?.isUnset == true) {
                        UNSET_IGNORE_WHEN_PARAMETER_COUNT_GREATER_OR_EQUAL_THAN_PROPERTY
                    } else {
                        property?.getValueAs<Int>()
                    }
                },
                propertyWriter = { property ->
                    if (property == UNSET_IGNORE_WHEN_PARAMETER_COUNT_GREATER_OR_EQUAL_THAN_PROPERTY) {
                        "unset"
                    } else {
                        property.toString()
                    }
                },
            )
    }
}

public val ARGUMENT_LIST_WRAPPING_RULE_ID: RuleId = ArgumentListWrappingRule().ruleId
