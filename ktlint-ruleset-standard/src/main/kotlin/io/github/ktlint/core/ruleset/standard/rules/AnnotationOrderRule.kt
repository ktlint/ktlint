package io.github.ktlint.core.ruleset.standard.rules

import com.intellij.lang.ASTNode
import io.github.ktlint.core.rule.engine.core.api.ElementType.ANNOTATED_EXPRESSION
import io.github.ktlint.core.rule.engine.core.api.ElementType.ANNOTATION_ENTRY
import io.github.ktlint.core.rule.engine.core.api.ElementType.CONSTRUCTOR_CALLEE
import io.github.ktlint.core.rule.engine.core.api.ElementType.FILE_ANNOTATION_LIST
import io.github.ktlint.core.rule.engine.core.api.ElementType.IDENTIFIER
import io.github.ktlint.core.rule.engine.core.api.ElementType.MODIFIER_LIST
import io.github.ktlint.core.rule.engine.core.api.ElementType.REFERENCE_EXPRESSION
import io.github.ktlint.core.rule.engine.core.api.ElementType.TYPE_REFERENCE
import io.github.ktlint.core.rule.engine.core.api.ElementType.USER_TYPE
import io.github.ktlint.core.rule.engine.core.api.RuleId
import io.github.ktlint.core.rule.engine.core.api.RuleV2
import io.github.ktlint.core.rule.engine.core.api.SinceKtlint
import io.github.ktlint.core.rule.engine.core.api.SinceKtlint.Status.EXPERIMENTAL
import io.github.ktlint.core.rule.engine.core.api.children
import io.github.ktlint.core.rule.engine.core.api.editorconfig.EditorConfig
import io.github.ktlint.core.rule.engine.core.api.editorconfig.EditorConfigProperty
import io.github.ktlint.core.ruleset.standard.StandardRule
import org.ec4j.core.model.PropertyType

/**
 * Annotations above a target are sorted alphabetically, with optional pinned ordering configured via [KTLINT_ANNOTATION_ORDERING_PROPERTY].
 * Annotations listed in that property come first in the specified order; remaining annotations are sorted alphabetically after them.
 */
@SinceKtlint("2.0", EXPERIMENTAL)
public class AnnotationOrderRule :
    StandardRule(
        id = "annotation-ordering",
        usesEditorConfigProperties = setOf(KTLINT_ANNOTATION_ORDERING_PROPERTY),
    ),
    RuleV2.Experimental {
    private var customAnnotationOrder: List<String> = emptyList()

    override fun beforeFirstNode(editorConfig: EditorConfig) {
        customAnnotationOrder = editorConfig[KTLINT_ANNOTATION_ORDERING_PROPERTY]
    }

    override fun beforeVisitChildNodes(node: ASTNode) {
        if (node.elementType !in ANNOTATION_CONTAINER) return

        val annotationEntries =
            node
                .children
                .filter { it.elementType == ANNOTATION_ENTRY }
                .toList()

        if (annotationEntries.size < 2) return

        val sortedAnnotationEntries =
            annotationEntries
                .sortedWith(
                    compareBy(
                        { pinnedIndex(it.annotationName()) },
                        { it.annotationName() ?: it.text },
                        { it.text },
                    ),
                )

        if (annotationEntries.map { it.text } == sortedAnnotationEntries.map { it.text }) return

        emit(annotationEntries.first().startOffset, "Annotations should be sorted alphabetically after annotation with preset order") {
            val anchors = annotationEntries.map { it.treeNext }
            annotationEntries.forEach { node.removeChild(it) }
            sortedAnnotationEntries
                .zip(anchors)
                .forEach { (entry, anchor) -> node.addChild(entry, anchor) }
        }
    }

    private fun pinnedIndex(name: String?): Int {
        if (customAnnotationOrder.isEmpty() || name == null) return Int.MAX_VALUE
        val index = customAnnotationOrder.indexOf(name)
        return if (index >= 0) index else Int.MAX_VALUE
    }

    private fun ASTNode.annotationName(): String? =
        findChildByType(CONSTRUCTOR_CALLEE)
            ?.findChildByType(TYPE_REFERENCE)
            ?.findChildByType(USER_TYPE)
            ?.findChildByType(REFERENCE_EXPRESSION)
            ?.findChildByType(IDENTIFIER)
            ?.text

    private companion object {
        val ANNOTATION_CONTAINER = listOf(ANNOTATED_EXPRESSION, FILE_ANNOTATION_LIST, MODIFIER_LIST)
    }
}

public val ANNOTATION_ORDER_RULE_ID: RuleId = AnnotationOrderRule().ruleId

private val ANNOTATION_ORDERING_PROPERTY_PARSER: (String, String?) -> PropertyType.PropertyValue<List<String>> =
    { _, value ->
        when {
            value.isNullOrBlank() || value.trim() == "unset" -> {
                PropertyType.PropertyValue.valid(value, listOf("unset"))
            }

            else -> {
                PropertyType.PropertyValue.valid(
                    value,
                    value.split(",").map { it.trim() }.filter { it.isNotEmpty() },
                )
            }
        }
    }

public val KTLINT_ANNOTATION_ORDERING_PROPERTY: EditorConfigProperty<List<String>> =
    EditorConfigProperty(
        type =
            PropertyType(
                "ktlint_annotation_ordering",
                "Comma-separated list of annotation names that defines a fixed ordering. Listed annotations are placed first " +
                    "in the specified order; remaining annotations are sorted alphabetically. List the annotation names without the `@` " +
                    "prefix. Set to `unset` to disable custom ordering.",
                ANNOTATION_ORDERING_PROPERTY_PARSER,
            ),
        defaultValue = listOf("unset"),
        propertyMapper = { property, _ ->
            when {
                property == null || property.isUnset -> emptyList()
                else -> null
            }
        },
        propertyWriter = { it.joinToString(",") },
    )
