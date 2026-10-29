package io.github.ktlint.core.rule.engine.internal

import com.intellij.lang.ASTNode
import io.github.ktlint.core.rule.engine.core.api.ElementType.ANNOTATION_ENTRY
import io.github.ktlint.core.rule.engine.core.api.ElementType.CONSTRUCTOR_CALLEE
import io.github.ktlint.core.rule.engine.core.api.ElementType.RBRACE
import io.github.ktlint.core.rule.engine.core.api.ElementType.TYPE_REFERENCE
import io.github.ktlint.core.rule.engine.core.api.ElementType.VALUE_ARGUMENT
import io.github.ktlint.core.rule.engine.core.api.IgnoreKtlintSuppressions
import io.github.ktlint.core.rule.engine.core.api.RuleV2
import io.github.ktlint.core.rule.engine.core.api.TokenSets
import io.github.ktlint.core.rule.engine.core.api.editorconfig.EditorConfig
import io.github.ktlint.core.rule.engine.core.api.isKtAnnotated
import io.github.ktlint.core.rule.engine.core.api.nextSibling
import io.github.ktlint.core.rule.engine.core.api.parent
import io.github.ktlint.core.rule.engine.core.api.recursiveChildren
import io.github.ktlint.core.rule.engine.internal.SuppressionLocator.CommentSuppressionHint.Type.BLOCK_END
import io.github.ktlint.core.rule.engine.internal.SuppressionLocator.CommentSuppressionHint.Type.BLOCK_START

internal class SuppressionLocator(
    editorConfig: EditorConfig,
) {
    private val formatterTags = FormatterTags.from(editorConfig)

    // Hashcode of the raw code for which the suppressionHints are collected. The raw code can be represented by different AST's. For the
    // SuppressionLocator that is not relevant as the offsets refer to positions in the raw code and not in the AST.
    private var hashcodeASTNodeText: Int? = null
    private var suppressionHints: List<SuppressionHint> = emptyList()

    /**
     * Check if element at [offset] in [rootNode] has to be suppressed for given [rule].
     */
    fun suppress(
        rootNode: ASTNode,
        offset: Int,
        rule: RuleV2,
    ): Boolean {
        // (Re)build the list of suppressions for given [rootNode] of AST tree in case content of AST has changed. When linting, the
        // rootNode is never changed. During format, the rootNode is changed whenever a LintError is corrected.
        rootNode
            .text
            .hashCode()
            .takeIf { it != hashcodeASTNodeText }
            ?.let { hashCode ->
                hashcodeASTNodeText = hashCode
                suppressionHints = findSuppressionHints(rootNode)
            }

        return if (rule is IgnoreKtlintSuppressions || suppressionHints.isEmpty()) {
            false
        } else {
            suppressionHints
                .filter { offset in it.range }
                .any { hint -> hint.disabledRuleIds.isEmpty() || hint.disabledRuleIds.contains(rule.ruleId.value) }
        }
    }

    private fun findSuppressionHints(rootNode: ASTNode): List<SuppressionHint> {
        val suppressionHints = ArrayList<SuppressionHint>()
        val commentSuppressionsHints = mutableListOf<CommentSuppressionHint>()
        rootNode.recursiveChildren.forEach { node ->
            when (node.elementType) {
                in TokenSets.COMMENTS -> {
                    node
                        .createSuppressionHintFromComment()
                        ?.let(commentSuppressionsHints::add)
                }

                ANNOTATION_ENTRY -> {
                    node
                        .takeIf { it.isSuppressAnnotation() }
                        ?.createSuppressionHintFromAnnotations()
                        ?.let(suppressionHints::add)
                }
            }
        }
        return suppressionHints + commentSuppressionsHints.toSuppressionHints()
    }

    private fun ASTNode.createSuppressionHintFromComment(): CommentSuppressionHint? =
        text
            .removePrefix("//")
            .removePrefix("/*")
            .removeSuffix("*/")
            .trim()
            .split(" ")
            .takeIf { it.isNotEmpty() }
            ?.let { parts ->
                when (parts[0]) {
                    formatterTags.formatterTagOff -> {
                        CommentSuppressionHint(
                            this,
                            HashSet(parts.tail()),
                            BLOCK_START,
                        )
                    }

                    formatterTags.formatterTagOn -> {
                        CommentSuppressionHint(
                            this,
                            HashSet(parts.tail()),
                            BLOCK_END,
                        )
                    }

                    else -> {
                        null
                    }
                }
            }

    private fun MutableList<CommentSuppressionHint>.toSuppressionHints(): MutableList<SuppressionHint> {
        val suppressionHints = mutableListOf<SuppressionHint>()
        val blockCommentSuppressionHints = mutableListOf<CommentSuppressionHint>()
        forEach { commentSuppressionHint ->
            when (commentSuppressionHint.type) {
                BLOCK_START -> {
                    blockCommentSuppressionHints.add(commentSuppressionHint)
                }

                BLOCK_END -> {
                    // match open hint
                    blockCommentSuppressionHints
                        .lastOrNull { it.disabledRuleIds == commentSuppressionHint.disabledRuleIds }
                        ?.let { openHint ->
                            blockCommentSuppressionHints.remove(openHint)
                            suppressionHints.add(
                                SuppressionHint(
                                    IntRange(openHint.node.startOffset, commentSuppressionHint.node.startOffset - 1),
                                    commentSuppressionHint.disabledRuleIds,
                                ),
                            )
                        }
                }
            }
        }
        suppressionHints.addAll(
            // Remaining hints were not properly closed
            blockCommentSuppressionHints.map {
                val rbraceOfContainingBlock = it.node.rbraceOfContainingBlock()
                if (rbraceOfContainingBlock == null) {
                    // Apply suppression on next sibling only, when the outer element does not end with a RBRACE
                    it.node
                        .nextSibling
                        .let { nextSibling ->
                            SuppressionHint(
                                IntRange(
                                    it.node.startOffset,
                                    nextSibling?.textRange?.endOffset ?: it.node.textRange.endOffset,
                                ),
                                it.disabledRuleIds,
                            )
                        }
                } else {
                    // Exclude closing curly brace of the containing block
                    SuppressionHint(
                        IntRange(it.node.startOffset, rbraceOfContainingBlock.startOffset - 1),
                        it.disabledRuleIds,
                    )
                }
            },
        )
        return suppressionHints
    }

    private fun ASTNode.rbraceOfContainingBlock(): ASTNode? =
        parent
            ?.lastChildNode
            ?.takeIf { it.elementType == RBRACE }

    private fun <T> List<T>.tail() = this.subList(1, this.size)

    private fun ASTNode.isSuppressAnnotation(): Boolean =
        findChildByType(CONSTRUCTOR_CALLEE)
            ?.findChildByType(TYPE_REFERENCE)
            ?.text in SUPPRESS_ANNOTATIONS

    private fun ASTNode.createSuppressionHintFromAnnotations(): SuppressionHint? {
        val suppressedRuleIds =
            recursiveChildren
                .filter { it.elementType == VALUE_ARGUMENT }
                .flatMapTo(mutableListOf()) {
                    it.text.removeSurrounding("\"").findRuleSuppressionIds()
                }.ifEmpty { return null }
        return suppressionRange()?.let { intRange ->
            SuppressionHint(
                intRange,
                if (suppressedRuleIds.contains(ALL_KTLINT_RULES_SUPPRESSION_ID)) {
                    emptySet()
                } else {
                    suppressedRuleIds.toSet()
                },
            )
        }
    }

    private fun ASTNode.suppressionRange(): IntRange? =
        parent { it.isKtAnnotated }
            ?.textRange
            ?.let { IntRange(it.startOffset, it.endOffset) }

    private fun String.findRuleSuppressionIds(): List<String> =
        when {
            this == "ktlint" -> {
                // Disable all rules
                listOf(ALL_KTLINT_RULES_SUPPRESSION_ID)
            }

            startsWith("ktlint:") -> {
                // Disable specific rule. For backwards compatibility prefix rules without rule set id with the "standard" rule set
                // id. Note that the KtlintSuppressionRule will emit a lint violation on the id. So this fix is only applicable for
                // code bases in which the rule and suppression id's have not yet been fixed.
                listOf(removePrefix("ktlint:"))
            }

            else -> {
                // Disable specific rule if the annotation value is mapped to a specific rule
                SUPPRESS_ANNOTATION_RULE_MAP[this].orEmpty()
            }
        }

    /**
     * @param range zero-based range of lines where lint errors should be suppressed
     * @param disabledRuleIds empty set means "all"
     */
    private data class SuppressionHint(
        val range: IntRange,
        val disabledRuleIds: Set<String> = emptySet(),
    )

    private data class CommentSuppressionHint(
        val node: ASTNode,
        val disabledRuleIds: Set<String> = emptySet(),
        val type: Type,
    ) {
        enum class Type {
            BLOCK_START,
            BLOCK_END,
        }
    }

    private companion object {
        /**
         * Mapping of non-ktlint annotations to ktlint-annotation so that ktlint rules will be suppressed automatically
         * when specific non-ktlint annotations are found. The prevents that developers have to specify multiple annotations
         * for the same violation.
         */
        val SUPPRESS_ANNOTATION_RULE_MAP =
            mapOf(
                // It would have been nice if the official rule id's as defined in the Rules themselves could have been used here. But that
                // would introduce a circular dependency between the ktlint-rule-engine and the ktlint-ruleset-standard modules.
                "EnumEntryName" to listOf("standard:enum-entry-name-case"),
                "RemoveCurlyBracesFromTemplate" to listOf("standard:string-template"),
                "ClassName" to listOf("standard:class-naming"),
                "FunctionName" to listOf("standard:function-naming"),
                "LocalVariableName" to listOf("standard:backing-property-naming"),
                "PackageName" to listOf("standard:package-name"),
                "PropertyName" to listOf("standard:property-naming", "standard:backing-property-naming"),
                "ConstPropertyName" to listOf("standard:property-naming"),
                "ObjectPropertyName" to listOf("standard:property-naming", "standard:backing-property-naming"),
                "PrivatePropertyName" to listOf("standard:property-naming"),
                "UnusedImport" to listOf("standard:no-unused-imports"),
            )
        val SUPPRESS_ANNOTATIONS = setOf("Suppress", "SuppressWarnings")
        const val ALL_KTLINT_RULES_SUPPRESSION_ID = "ktlint:suppress-all-rules"
    }
}
