package io.github.ktlint.core.rule.engine.internal.rules

import com.intellij.lang.ASTNode
import com.intellij.psi.tree.TokenSet
import io.github.ktlint.core.rule.engine.core.api.ElementType.ANNOTATION
import io.github.ktlint.core.rule.engine.core.api.ElementType.ANNOTATION_ENTRY
import io.github.ktlint.core.rule.engine.core.api.ElementType.BLOCK_COMMENT
import io.github.ktlint.core.rule.engine.core.api.ElementType.CALL_EXPRESSION
import io.github.ktlint.core.rule.engine.core.api.ElementType.CLASS
import io.github.ktlint.core.rule.engine.core.api.ElementType.EOL_COMMENT
import io.github.ktlint.core.rule.engine.core.api.ElementType.FUN
import io.github.ktlint.core.rule.engine.core.api.ElementType.LITERAL_STRING_TEMPLATE_ENTRY
import io.github.ktlint.core.rule.engine.core.api.ElementType.PROPERTY
import io.github.ktlint.core.rule.engine.core.api.ElementType.STRING_TEMPLATE
import io.github.ktlint.core.rule.engine.core.api.ElementType.TYPE_ARGUMENT_LIST
import io.github.ktlint.core.rule.engine.core.api.ElementType.TYPE_PARAMETER
import io.github.ktlint.core.rule.engine.core.api.ElementType.TYPE_PARAMETER_LIST
import io.github.ktlint.core.rule.engine.core.api.ElementType.TYPE_PROJECTION
import io.github.ktlint.core.rule.engine.core.api.ElementType.VALUE_ARGUMENT
import io.github.ktlint.core.rule.engine.core.api.ElementType.VALUE_ARGUMENT_LIST
import io.github.ktlint.core.rule.engine.core.api.ElementType.VALUE_PARAMETER
import io.github.ktlint.core.rule.engine.core.api.ElementType.VALUE_PARAMETER_LIST
import io.github.ktlint.core.rule.engine.core.api.IgnoreKtlintSuppressions
import io.github.ktlint.core.rule.engine.core.api.KtlintKotlinCompiler
import io.github.ktlint.core.rule.engine.core.api.RuleId
import io.github.ktlint.core.rule.engine.core.api.findChildByTypeRecursively
import io.github.ktlint.core.rule.engine.core.api.findParentByType
import io.github.ktlint.core.rule.engine.core.api.isWhiteSpace
import io.github.ktlint.core.rule.engine.core.api.isWhiteSpaceWithNewline
import io.github.ktlint.core.rule.engine.core.api.nextLeaf
import io.github.ktlint.core.rule.engine.core.api.nextSibling
import io.github.ktlint.core.rule.engine.core.api.parent
import io.github.ktlint.core.rule.engine.core.api.prevLeaf
import io.github.ktlint.core.rule.engine.core.api.remove
import io.github.ktlint.core.rule.engine.core.api.replaceWith
import io.github.ktlint.core.rule.engine.internal.KTLINT_SUPPRESSION_ID_ALL_RULES
import io.github.ktlint.core.rule.engine.internal.insertKtlintRuleSuppression
import io.github.ktlint.core.rule.engine.internal.isKtlintSuppressionId
import io.github.ktlint.core.rule.engine.internal.isTopLevel
import io.github.ktlint.core.rule.engine.internal.qualifiedRuleIdString
import io.github.ktlint.core.rule.engine.internal.rules.KtLintDirective.KtlintDirectiveType.KTLINT_DISABLE
import io.github.ktlint.core.rule.engine.internal.rules.KtLintDirective.KtlintDirectiveType.KTLINT_ENABLE
import io.github.ktlint.core.rule.engine.internal.rules.KtLintDirective.SuppressionIdChange.InvalidSuppressionId
import io.github.ktlint.core.rule.engine.internal.rules.KtLintDirective.SuppressionIdChange.ValidSuppressionId
import io.github.ktlint.core.rule.engine.internal.rules.KtLintDirective.SuppressionIdChange.ValidSuppressionId.Companion.KTLINT_SUPPRESSION_ALL
import io.github.ktlint.core.rule.engine.internal.toFullyQualifiedKtlintSuppressionId
import org.jetbrains.kotlin.psi.psiUtil.siblings
import org.jetbrains.kotlin.utils.addToStdlib.applyIf
import kotlin.text.removePrefix

/**
 * Disallow usage of the old "ktlint-disable" and "ktlint-enable" directives.
 *
 * A ktlint-disable directive is replaced with an annotation on the closest parent declaration or expression, or as annotation on the file
 * level in case the directive is associated with a top level element. Ktlint-disable directives placed in block comments are only
 * autocorrected when placed as top level element or in case a matching Ktlint-enable directive is found in the same parent as the disable
 * directive.
 *
 * If the target element is annotated with a [Suppress] (or, if missing, is annotated with a [SuppressWarnings] annotation) then the
 * ktlint-directive will be matched against this annotation. If this annotation already contains a suppression for *all* ktlint rules, or
 * for the specific rule id, then it is not added to annotation as it would be redundant. In case a suppression identifier is added to an
 * existing annotation then all identifiers in the annotation are alphabetically sorted.
 *
 * If the target element is not annotated with [Suppress] or [SuppressWarnings] then a [Suppress] annotation is added.
 *
 * Ktlint-enable directives are removed as annotations have a scope in which the suppression will be active.
 */
public class KtlintSuppressionRule(
    private val allowedRuleIds: List<RuleId>,
) : InternalRule("ktlint-suppression"),
    // The SuppressionLocatorBuilder no longer support the old ktlint suppression directives using comments. This rule may not be disabled
    // in any way as it would fail to process suppressions.
    IgnoreKtlintSuppressions {
    private val allowedRuleIdAsStrings = allowedRuleIds.map { it.value }

    private val ruleIdValidator: (String) -> Boolean = { ruleId -> allowedRuleIdAsStrings.contains(ruleId) }

    override fun beforeVisitChildNodes(node: ASTNode) {
        node
            .takeIf { isKtlintRuleSuppressionInAnnotation(it) }
            ?.let { visitKtlintSuppressionInAnnotation(node) }

        node
            .ktlintDirectiveOrNull(ruleIdValidator)
            ?.visitKtlintDirective()
    }

    private fun isKtlintRuleSuppressionInAnnotation(node: ASTNode) =
        node
            .takeIf { it.elementType == STRING_TEMPLATE }
            ?.takeIf { it.text.isKtlintSuppressionId() }
            ?.let { literalStringTemplate ->
                literalStringTemplate
                    .findParentByType(VALUE_ARGUMENT)
                    ?.isPartOfAnnotation()
            }
            ?: false

    private fun ASTNode.isPartOfAnnotation() = parent { it.elementType == ANNOTATION || it.elementType == ANNOTATION_ENTRY } != null

    private fun visitKtlintSuppressionInAnnotation(node: ASTNode) {
        node
            .findChildByTypeRecursively(LITERAL_STRING_TEMPLATE_ENTRY)
            ?.let { literalStringTemplateEntry ->
                val prefixedSuppression =
                    literalStringTemplateEntry
                        .text
                        .toFullyQualifiedKtlintSuppressionId()
                        .removeSurrounding("\"")
                val offset = literalStringTemplateEntry.startOffset
                if (prefixedSuppression.isUnknownKtlintSuppression()) {
                    emit(offset, "Ktlint rule with id '$prefixedSuppression' is unknown or not loaded")
                } else if (prefixedSuppression != literalStringTemplateEntry.text) {
                    emit(offset, "Identifier to suppress ktlint rule must be fully qualified with the rule set id") {
                        createLiteralStringTemplateEntry(prefixedSuppression)
                            ?.let { literalStringTemplateEntry.replaceWith(it) }
                    }
                }
            }
    }

    private fun ASTNode.removePrecedingWhitespace() {
        prevLeaf
            .takeIf { it.isWhiteSpace }
            ?.remove()
    }

    private fun createLiteralStringTemplateEntry(prefixedSuppression: String) =
        KtlintKotlinCompiler
            .createASTNodeFromText("listOf(\"$prefixedSuppression\")")
            ?.findChildByType(CALL_EXPRESSION)
            ?.findChildByType(VALUE_ARGUMENT_LIST)
            ?.findChildByType(VALUE_ARGUMENT)
            ?.findChildByType(STRING_TEMPLATE)
            ?.findChildByType(LITERAL_STRING_TEMPLATE_ENTRY)

    private fun KtLintDirective.visitKtlintDirective() {
        when (ktlintDirectiveType) {
            KTLINT_DISABLE -> {
                if (node.elementType == EOL_COMMENT && node.prevLeaf.isWhiteSpaceWithNewline) {
                    removeDanglingEolCommentWithKtlintDisableDirective()
                } else {
                    visitKtlintDisableDirective()
                }
            }

            KTLINT_ENABLE -> {
                removeKtlintEnableDirective()
            }
        }
    }

    private fun KtLintDirective.removeDanglingEolCommentWithKtlintDisableDirective() {
        emit(offset, "Directive 'ktlint-disable' in EOL comment is ignored as it is not preceded by a code element") {
            node.removePrecedingWhitespace()
            node.remove()
        }
    }

    private fun KtLintDirective.visitKtlintDisableDirective() {
        if (node.elementType == BLOCK_COMMENT && hasNoMatchingKtlintEnableDirective(ruleIdValidator)) {
            emit(
                offset,
                "Directive 'ktlint-disable' is deprecated. The matching 'ktlint-enable' directive is not found in same scope. Replace " +
                    "with @Suppress annotation",
            )
            return
        }
        var autocorrect = false
        emit(offset, "Directive 'ktlint-disable' is deprecated. Replace with @Suppress annotation") {
            autocorrect = true
        }
        suppressionIdChanges
            .filterIsInstance<InvalidSuppressionId>()
            .forEach { ktlintDirectiveChange ->
                emit(
                    offset +
                        ktlintDirectiveType.id.length +
                        ktlintDirectiveChange.offsetOriginalRuleId,
                    "Ktlint rule with id '${ktlintDirectiveChange.originalRuleId}' is unknown or not loaded",
                )
            }
        if (autocorrect) {
            val suppressionIds =
                suppressionIdChanges
                    .filterIsInstance<ValidSuppressionId>()
                    .map { it.suppressionId }
                    .toSet()
            node
                .applyIf(node.elementType == BLOCK_COMMENT && shouldBePromotedToParentDeclaration(ruleIdValidator)) {
                    parent?.parent
                }?.insertKtlintRuleSuppression(suppressionIds, forceFileAnnotation = node.shouldBeConvertedToFileAnnotation())
            if (node.elementType == EOL_COMMENT) {
                node.removePrecedingWhitespace()
            } else {
                if (node.nextLeaf.isWhiteSpaceWithNewline) {
                    node
                        .nextLeaf
                        ?.remove()
                } else {
                    node.removePrecedingWhitespace()
                }
            }
            node.remove()
        }
    }

    private fun KtLintDirective.removeKtlintEnableDirective() {
        emit(offset, "Directive 'ktlint-enable' is obsolete after migrating to suppress annotations") {
            node.removePrecedingWhitespace()
            node.remove()
        }
    }

    private fun String.isUnknownKtlintSuppression(): Boolean =
        qualifiedRuleIdString()
            .let { ruleId ->
                allowedRuleIds.none { it.value == ruleId }
            }
}

private class KtLintDirective(
    val node: ASTNode,
    val ruleIdValidator: (String) -> Boolean,
    val ktlintDirectiveType: KtlintDirectiveType,
    val ktlintDirectives: String,
    val suppressionIdChanges: Set<SuppressionIdChange>,
) {
    val offset = node.startOffset + node.text.indexOf(ktlintDirectiveType.id)

    fun hasNoMatchingKtlintEnableDirective(ruleIdValidator: (String) -> Boolean): Boolean {
        require(ktlintDirectiveType == KTLINT_DISABLE && node.elementType == BLOCK_COMMENT)

        return if (node.shouldBeConvertedToFileAnnotation()) {
            false
        } else {
            findMatchingKtlintEnableDirective(ruleIdValidator) == null
        }
    }

    private fun findMatchingKtlintEnableDirective(ruleIdValidator: (String) -> Boolean) =
        node
            .applyIf(node.isSuppressibleDeclaration()) { node.parent }
            ?.siblings()
            ?.firstOrNull { node ->
                node
                    .ktlintDirectiveOrNull(ruleIdValidator)
                    ?.takeIf { it.ktlintDirectiveType == KTLINT_ENABLE }
                    ?.ktlintDirectives == ktlintDirectives
            }

    fun shouldBePromotedToParentDeclaration(ruleIdValidator: (String) -> Boolean): Boolean {
        require(ktlintDirectiveType == KTLINT_DISABLE && node.elementType == BLOCK_COMMENT)
        return when {
            node.shouldBeConvertedToFileAnnotation() -> false
            node.isSuppressibleDeclaration() -> !hasMatchingKtlintEnabledDirective(ruleIdValidator)
            else -> node.surroundsMultipleListElements()
        }
    }

    private fun hasMatchingKtlintEnabledDirective(ruleIdValidator: (String) -> Boolean): Boolean =
        findMatchingKtlintEnableDirective(ruleIdValidator)
            .let { matchingKtlintEnabledDirective ->
                // In case the node is part of a suppressible declaration and the next sibling matches the enable directive then the
                // block directive should be match with this declaration only and not be moved to the parent.
                matchingKtlintEnabledDirective ==
                    node
                        .parent
                        ?.nextSibling { !it.isWhiteSpace }
            }

    private fun ASTNode.surroundsMultipleListElements(): Boolean {
        require(ktlintDirectiveType == KTLINT_DISABLE && elementType == BLOCK_COMMENT)
        return parent?.elementType in listTypeTokenSet &&
            findMatchingKtlintEnableDirective(ruleIdValidator)
                ?.siblings(false)
                ?.takeWhile { it != this }
                ?.count { it.elementType in listElementTypeTokenSet }
                ?.let { it > 1 }
                ?: false
    }

    private val listTypeTokenSet =
        TokenSet.create(
            TYPE_ARGUMENT_LIST,
            TYPE_PARAMETER_LIST,
            VALUE_ARGUMENT_LIST,
            VALUE_PARAMETER_LIST,
        )

    private val listElementTypeTokenSet =
        TokenSet.create(
            TYPE_PROJECTION,
            TYPE_PARAMETER,
            VALUE_ARGUMENT,
            VALUE_PARAMETER,
        )

    enum class KtlintDirectiveType(
        val id: String,
    ) {
        KTLINT_DISABLE("ktlint-disable"),
        KTLINT_ENABLE("ktlint-enable"),
    }

    sealed class SuppressionIdChange {
        class ValidSuppressionId(
            val suppressionId: String,
        ) : SuppressionIdChange() {
            companion object {
                val KTLINT_SUPPRESSION_ALL = ValidSuppressionId(KTLINT_SUPPRESSION_ID_ALL_RULES)
            }
        }

        class InvalidSuppressionId(
            val originalRuleId: String,
            val offsetOriginalRuleId: Int,
        ) : SuppressionIdChange()
    }
}

private fun ASTNode.ktlintDirectiveOrNull(ruleIdValidator: (String) -> Boolean): KtLintDirective? {
    val ktlintDirectiveString = ktlintDirectiveString()
    return ktlintDirectiveString
        ?.toKtlintDirectiveTypeOrNull()
        ?.let { ktlintDirectiveType ->
            Pair(ktlintDirectiveType, ktlintDirectiveString.removePrefix(ktlintDirectiveType.id))
            val ruleIds = ktlintDirectiveString.removePrefix(ktlintDirectiveType.id)
            val suppressionIdChanges = ruleIds.toSuppressionIdChanges(ruleIdValidator)
            return KtLintDirective(this, ruleIdValidator, ktlintDirectiveType, ruleIds, suppressionIdChanges)
        }
}

private fun ASTNode.ktlintDirectiveString(): String? =
    when (elementType) {
        EOL_COMMENT -> {
            text
                .removePrefix("//")
                .trim()
        }

        BLOCK_COMMENT -> {
            text
                .removePrefix("/*")
                .removeSuffix("*/")
                .trim()
        }

        else -> {
            null
        }
    }

private fun String.toKtlintDirectiveTypeOrNull() =
    when {
        startsWith(KTLINT_DISABLE.id) -> KTLINT_DISABLE
        startsWith(KTLINT_ENABLE.id) -> KTLINT_ENABLE
        else -> null
    }

// Transform the string: "ktlint-disable foo standard:bar"
// to a (sorted) list containing elements:
//    ktlint:standard:bar
//    ktlint:standard:foo
private fun String.toSuppressionIdChanges(ruleIdValidator: (String) -> Boolean) =
    trim()
        .split(" ")
        .asSequence()
        .map { it.trim() }
        .filter { it.isNotBlank() }
        .map { it.qualifiedRuleIdString() }
        .map { originalRuleId ->
            if (ruleIdValidator(originalRuleId)) {
                ValidSuppressionId(originalRuleId)
            } else {
                InvalidSuppressionId(
                    originalRuleId,
                    this.indexOf(originalRuleId),
                )
            }
        }.toSet()
        .ifEmpty { setOf(KTLINT_SUPPRESSION_ALL) }

private fun ASTNode.shouldBeConvertedToFileAnnotation() =
    isTopLevel() ||
        (elementType == BLOCK_COMMENT && isSuppressibleDeclaration() && parent!!.isTopLevel())

private fun ASTNode.isSuppressibleDeclaration() =
    when (parent?.elementType) {
        CLASS, FUN, PROPERTY -> true
        else -> false
    }

public val KTLINT_SUPPRESSION_RULE_ID: RuleId = KtlintSuppressionRule(emptyList()).ruleId
