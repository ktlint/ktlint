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
import io.github.ktlint.core.ruleset.standard.StandardRule

/**
 * Annotations above a target are sorted alphabetically.
 */
@SinceKtlint("2.0", EXPERIMENTAL)
public class AnnotationOrderRule :
    StandardRule("annotation-ordering"),
    RuleV2.Experimental {
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
                        { it.annotationName() ?: it.text },
                        { it.text },
                    ),
                )

        if (annotationEntries.map { it.text } == sortedAnnotationEntries.map { it.text }) return

        emit(annotationEntries.first().startOffset, "Annotations should be sorted alphabetically") {
            val anchors = annotationEntries.map { it.treeNext }
            annotationEntries.forEach { node.removeChild(it) }
            sortedAnnotationEntries
                .zip(anchors)
                .forEach { (entry, anchor) -> node.addChild(entry, anchor) }
        }
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
