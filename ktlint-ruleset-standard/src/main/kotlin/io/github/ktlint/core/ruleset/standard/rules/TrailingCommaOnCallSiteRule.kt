package io.github.ktlint.core.ruleset.standard.rules

import com.intellij.lang.ASTNode
import com.intellij.psi.impl.source.tree.LeafPsiElement
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
            RBRACKET if node.parent!!.elementType == COLLECTION_LITERAL_EXPRESSION -> node.visitEndOfCollectionLiteralExpression()
            RBRACKET if node.parent!!.elementType == INDICES -> node.visitEndOfIndices()
            GT if node.parent!!.elementType == TYPE_ARGUMENT_LIST -> node.visitEndOfTypeList()
            RPAR if node.parent?.elementType == VALUE_ARGUMENT_LIST -> node.visitEndOfValueArgumentList()
            else -> Unit
        }
    }

    private fun ASTNode.visitEndOfCollectionLiteralExpression() {
        require(elementType == RBRACKET && parent!!.elementType == COLLECTION_LITERAL_EXPRESSION)
        this.reportAndCorrectTrailingCommaNodeBefore(
            isTrailingCommaAllowed = allowTrailingCommaOnCallSite,
        )
    }

    private fun ASTNode.visitEndOfIndices() {
        require(elementType == RBRACKET && parent!!.elementType == INDICES)
        this.reportAndCorrectTrailingCommaNodeBefore(
            isTrailingCommaAllowed = allowTrailingCommaOnCallSite,
        )
    }

    private fun ASTNode.visitEndOfValueArgumentList() {
        require(elementType == RPAR && parent!!.elementType == VALUE_ARGUMENT_LIST)
        if (parent!!.parent?.elementType != FUNCTION_LITERAL) {
            this.reportAndCorrectTrailingCommaNodeBefore(
                isTrailingCommaAllowed = allowTrailingCommaOnCallSite,
            )
        }
    }

    private fun ASTNode.visitEndOfTypeList() {
        require(elementType == GT && parent!!.elementType == TYPE_ARGUMENT_LIST)
        this.reportAndCorrectTrailingCommaNodeBefore(
            isTrailingCommaAllowed = allowTrailingCommaOnCallSite,
        )
    }

    private fun ASTNode.reportAndCorrectTrailingCommaNodeBefore(isTrailingCommaAllowed: Boolean) {
        val trailingCommaNode = prevLeaf?.findPreviousTrailingCommaNodeOrNull()
        val trailingCommaState =
            when {
                parent!!.isMultiline() -> if (trailingCommaNode != null) TrailingCommaState.EXISTS else TrailingCommaState.MISSING
                else -> if (trailingCommaNode != null) TrailingCommaState.REDUNDANT else TrailingCommaState.NOT_EXISTS
            }
        when (trailingCommaState) {
            TrailingCommaState.EXISTS -> {
                if (!isTrailingCommaAllowed) {
                    emit(trailingCommaNode!!.startOffset, "Unnecessary trailing comma before \"${this.text}\"") {
                        trailingCommaNode.remove()
                    }
                }
            }

            TrailingCommaState.MISSING -> {
                if (isTrailingCommaAllowed) {
                    val prevNode = prevCodeLeaf!!
                    emit(prevNode.startOffset + prevNode.textLength, "Missing trailing comma before \"${this.text}\"") {
                        prevCodeSibling
                            ?.nextSibling
                            ?.let { before ->
                                before.parent?.addChild(LeafPsiElement(COMMA, ","), before)
                            }
                    }
                }
            }

            TrailingCommaState.REDUNDANT -> {
                emit(trailingCommaNode!!.startOffset, "Unnecessary trailing comma before \"${this.text}\"") {
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
    }
}

public val TRAILING_COMMA_ON_CALL_SITE_RULE_ID: RuleId = TrailingCommaOnCallSiteRule().ruleId
