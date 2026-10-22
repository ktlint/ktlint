package io.github.ktlint.core.ruleset.standard.rules

import com.intellij.lang.ASTNode
import com.intellij.psi.impl.source.tree.LeafPsiElement
import com.intellij.psi.tree.TokenSet
import io.github.ktlint.core.rule.engine.core.api.ElementType.COLLECTION_LITERAL_EXPRESSION
import io.github.ktlint.core.rule.engine.core.api.ElementType.COMMA
import io.github.ktlint.core.rule.engine.core.api.ElementType.FUNCTION_LITERAL
import io.github.ktlint.core.rule.engine.core.api.ElementType.GT
import io.github.ktlint.core.rule.engine.core.api.ElementType.INDICES
import io.github.ktlint.core.rule.engine.core.api.ElementType.RBRACKET
import io.github.ktlint.core.rule.engine.core.api.ElementType.RPAR
import io.github.ktlint.core.rule.engine.core.api.ElementType.TYPE_ARGUMENT_LIST
import io.github.ktlint.core.rule.engine.core.api.ElementType.VALUE_ARGUMENT
import io.github.ktlint.core.rule.engine.core.api.ElementType.VALUE_ARGUMENT_LIST
import io.github.ktlint.core.rule.engine.core.api.RuleId
import io.github.ktlint.core.rule.engine.core.api.SinceKtlint
import io.github.ktlint.core.rule.engine.core.api.SinceKtlint.Status.EXPERIMENTAL
import io.github.ktlint.core.rule.engine.core.api.SinceKtlint.Status.STABLE
import io.github.ktlint.core.rule.engine.core.api.children
import io.github.ktlint.core.rule.engine.core.api.editorconfig.EditorConfig
import io.github.ktlint.core.rule.engine.core.api.editorconfig.EditorConfigProperty
import io.github.ktlint.core.rule.engine.core.api.isCode
import io.github.ktlint.core.rule.engine.core.api.isWhiteSpaceWithNewline
import io.github.ktlint.core.rule.engine.core.api.nextSibling
import io.github.ktlint.core.rule.engine.core.api.parent
import io.github.ktlint.core.rule.engine.core.api.prevCodeLeaf
import io.github.ktlint.core.rule.engine.core.api.prevCodeSibling
import io.github.ktlint.core.rule.engine.core.api.prevLeaf
import io.github.ktlint.core.rule.engine.core.api.remove
import io.github.ktlint.core.ruleset.standard.StandardRule
import org.ec4j.core.model.PropertyType
import org.ec4j.core.model.PropertyType.PropertyValueParser

/**
 * Linting trailing comma for call site.
 *
 * @see [Kotlin Style Guide](https://kotlinlang.org/docs/coding-conventions.html#trailing-commas)
 */
@SinceKtlint("0.43", EXPERIMENTAL)
@SinceKtlint("0.47", STABLE)
public class TrailingCommaOnCallSiteRule :
    StandardRule(
        id = "trailing-comma-on-call-site",
        usesEditorConfigProperties = setOf(TRAILING_COMMA_ON_CALL_SITE_PROPERTY),
    ) {
    private var allowTrailingCommaOnCallSite = TRAILING_COMMA_ON_CALL_SITE_PROPERTY.defaultValue

    override fun beforeFirstNode(editorConfig: EditorConfig) {
        allowTrailingCommaOnCallSite = editorConfig[TRAILING_COMMA_ON_CALL_SITE_PROPERTY]
    }

    override fun beforeVisitChildNodes(node: ASTNode) {
        // Keep processing of element types in sync with IntelliJ Kotlin formatting settings.
        // https://github.com/JetBrains/intellij-kotlin/blob/master/formatter/src/org/jetbrains/kotlin/idea/formatter/trailingComma/util.kt
        when (node.elementType) {
            COLLECTION_LITERAL_EXPRESSION -> node.visitCollectionLiteralExpression()
            INDICES -> node.visitIndices()
            TYPE_ARGUMENT_LIST -> node.visitTypeList()
            VALUE_ARGUMENT_LIST -> node.visitValueList()
            else -> Unit
        }
    }

    private fun ASTNode.visitCollectionLiteralExpression() {
        val inspectNode = children.last { it.elementType == RBRACKET }
        reportAndCorrectTrailingCommaNodeBefore(
            inspectNode = inspectNode,
            isTrailingCommaAllowed = isTrailingCommaAllowed(),
        )
    }

    private fun ASTNode.isTrailingCommaAllowed() = elementType in TYPES_ON_CALL_SITE && allowTrailingCommaOnCallSite

    private fun ASTNode.visitIndices() {
        val inspectNode =
            children
                .last { it.elementType == RBRACKET }
        reportAndCorrectTrailingCommaNodeBefore(
            inspectNode = inspectNode,
            isTrailingCommaAllowed = isTrailingCommaAllowed(),
        )
    }

    private fun ASTNode.visitValueList() {
        if (parent?.elementType != FUNCTION_LITERAL) {
            children
                .lastOrNull { it.elementType == RPAR }
                ?.let { inspectNode ->
                    reportAndCorrectTrailingCommaNodeBefore(
                        inspectNode = inspectNode,
                        isTrailingCommaAllowed = isTrailingCommaAllowed(),
                    )
                }
        }
    }

    private fun ASTNode.visitTypeList() {
        val inspectNode = children.first { it.elementType == GT }
        reportAndCorrectTrailingCommaNodeBefore(
            inspectNode = inspectNode,
            isTrailingCommaAllowed = isTrailingCommaAllowed(),
        )
    }

    private fun ASTNode.reportAndCorrectTrailingCommaNodeBefore(
        inspectNode: ASTNode,
        isTrailingCommaAllowed: Boolean,
    ) {
        val prevLeaf = inspectNode.prevLeaf
        val trailingCommaNode = prevLeaf?.findPreviousTrailingCommaNodeOrNull()
        val trailingCommaState =
            when {
                isMultiline() -> if (trailingCommaNode != null) TrailingCommaState.EXISTS else TrailingCommaState.MISSING
                else -> if (trailingCommaNode != null) TrailingCommaState.REDUNDANT else TrailingCommaState.NOT_EXISTS
            }
        when (trailingCommaState) {
            TrailingCommaState.EXISTS -> {
                if (!isTrailingCommaAllowed) {
                    emit(trailingCommaNode!!.startOffset, "Unnecessary trailing comma before \"${inspectNode.text}\"") {
                        trailingCommaNode.remove()
                    }
                }
            }

            TrailingCommaState.MISSING -> {
                if (isTrailingCommaAllowed) {
                    val prevNode = inspectNode.prevCodeLeaf!!
                    emit(prevNode.startOffset + prevNode.textLength, "Missing trailing comma before \"${inspectNode.text}\"") {
                        inspectNode
                            .prevCodeSibling
                            ?.nextSibling
                            ?.let { before ->
                                before.parent?.addChild(LeafPsiElement(COMMA, ","), before)
                            }
                    }
                }
            }

            TrailingCommaState.REDUNDANT -> {
                emit(trailingCommaNode!!.startOffset, "Unnecessary trailing comma before \"${inspectNode.text}\"") {
                    trailingCommaNode.remove()
                }
            }

            TrailingCommaState.NOT_EXISTS -> {
                // Nothing to do
            }
        }
    }

    private fun ASTNode.isMultiline(): Boolean =
        if (elementType == VALUE_ARGUMENT_LIST) {
            hasAtLeastOneArgument() && hasValueArgumentFollowedByWhiteSpaceWithNewline()
        } else {
            textContains('\n')
        }

    private fun ASTNode.hasValueArgumentFollowedByWhiteSpaceWithNewline(): Boolean =
        findValueArgumentFollowedByWhiteSpaceWithNewline() != null

    private fun ASTNode.findValueArgumentFollowedByWhiteSpaceWithNewline() =
        this
            .findChildByType(VALUE_ARGUMENT)
            ?.nextSibling { it.isWhiteSpaceWithNewline }

    private fun ASTNode.hasAtLeastOneArgument() = children.any { it.elementType == VALUE_ARGUMENT }

    private fun ASTNode.findPreviousTrailingCommaNodeOrNull(): ASTNode? {
        val codeLeaf =
            if (isCode) {
                this
            } else {
                prevCodeLeaf
            }
        return codeLeaf?.takeIf { it.elementType == COMMA }
    }

    private enum class TrailingCommaState {
        /**
         * The trailing comma is needed and exists
         */
        EXISTS,

        /**
         * The trailing comma is needed and doesn't exist
         */
        MISSING,

        /**
         * The trailing comma isn't needed and doesn't exist
         */
        NOT_EXISTS,

        /**
         * The trailing comma isn't needed, but exists
         */
        REDUNDANT,
    }

    public companion object {
        private val BOOLEAN_VALUES_SET = setOf("true", "false")

        public val TRAILING_COMMA_ON_CALL_SITE_PROPERTY: EditorConfigProperty<Boolean> =
            EditorConfigProperty(
                type =
                    PropertyType.LowerCasingPropertyType(
                        "ij_kotlin_allow_trailing_comma_on_call_site",
                        "Defines whether a trailing comma (or no trailing comma) should be enforced on the calling side," +
                            "e.g. argument-list, when-entries, lambda-arguments, indices, etc." +
                            "When set, IntelliJ IDEA uses this property to allow usage of a trailing comma by discretion " +
                            "of the developer. KtLint however uses this setting to enforce consistent usage of the " +
                            "trailing comma when set.",
                        PropertyValueParser.BOOLEAN_VALUE_PARSER,
                        BOOLEAN_VALUES_SET,
                    ),
                defaultValue = true,
                androidStudioCodeStyleDefaultValue = false,
            )

        private val TYPES_ON_CALL_SITE =
            TokenSet.create(
                COLLECTION_LITERAL_EXPRESSION,
                INDICES,
                TYPE_ARGUMENT_LIST,
                VALUE_ARGUMENT_LIST,
            )
    }
}

public val TRAILING_COMMA_ON_CALL_SITE_RULE_ID: RuleId = TrailingCommaOnCallSiteRule().ruleId
