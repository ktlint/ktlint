package io.github.ktlint.core.ruleset.standard.rules

import com.intellij.lang.ASTNode
import com.intellij.psi.tree.TokenSet
import io.github.ktlint.core.rule.engine.core.api.ElementType.ABSTRACT_KEYWORD
import io.github.ktlint.core.rule.engine.core.api.ElementType.ACTUAL_KEYWORD
import io.github.ktlint.core.rule.engine.core.api.ElementType.ANNOTATION_ENTRY
import io.github.ktlint.core.rule.engine.core.api.ElementType.ANNOTATION_KEYWORD
import io.github.ktlint.core.rule.engine.core.api.ElementType.COMPANION_KEYWORD
import io.github.ktlint.core.rule.engine.core.api.ElementType.CONST_KEYWORD
import io.github.ktlint.core.rule.engine.core.api.ElementType.CONTEXT_PARAMETER_LIST
import io.github.ktlint.core.rule.engine.core.api.ElementType.DATA_KEYWORD
import io.github.ktlint.core.rule.engine.core.api.ElementType.ENUM_KEYWORD
import io.github.ktlint.core.rule.engine.core.api.ElementType.EXPECT_KEYWORD
import io.github.ktlint.core.rule.engine.core.api.ElementType.EXTERNAL_KEYWORD
import io.github.ktlint.core.rule.engine.core.api.ElementType.FINAL_KEYWORD
import io.github.ktlint.core.rule.engine.core.api.ElementType.INFIX_KEYWORD
import io.github.ktlint.core.rule.engine.core.api.ElementType.INLINE_KEYWORD
import io.github.ktlint.core.rule.engine.core.api.ElementType.INNER_KEYWORD
import io.github.ktlint.core.rule.engine.core.api.ElementType.INTERNAL_KEYWORD
import io.github.ktlint.core.rule.engine.core.api.ElementType.LATEINIT_KEYWORD
import io.github.ktlint.core.rule.engine.core.api.ElementType.MODIFIER_LIST
import io.github.ktlint.core.rule.engine.core.api.ElementType.OPEN_KEYWORD
import io.github.ktlint.core.rule.engine.core.api.ElementType.OPERATOR_KEYWORD
import io.github.ktlint.core.rule.engine.core.api.ElementType.OVERRIDE_KEYWORD
import io.github.ktlint.core.rule.engine.core.api.ElementType.PRIVATE_KEYWORD
import io.github.ktlint.core.rule.engine.core.api.ElementType.PROTECTED_KEYWORD
import io.github.ktlint.core.rule.engine.core.api.ElementType.PUBLIC_KEYWORD
import io.github.ktlint.core.rule.engine.core.api.ElementType.SEALED_KEYWORD
import io.github.ktlint.core.rule.engine.core.api.ElementType.SUSPEND_KEYWORD
import io.github.ktlint.core.rule.engine.core.api.ElementType.TAILREC_KEYWORD
import io.github.ktlint.core.rule.engine.core.api.ElementType.VARARG_KEYWORD
import io.github.ktlint.core.rule.engine.core.api.RuleId
import io.github.ktlint.core.rule.engine.core.api.SinceKtlint
import io.github.ktlint.core.rule.engine.core.api.SinceKtlint.Status.STABLE
import io.github.ktlint.core.ruleset.standard.StandardRule

@SinceKtlint("0.7", STABLE)
public class ModifierOrderRule : StandardRule("modifier-order") {
    override fun beforeVisitChildNodes(node: ASTNode) {
        if (node.elementType == MODIFIER_LIST) {
            val modifierArr = node.getChildren(tokenSet)
            val sorted = modifierArr.copyOf().apply { sortWith(compareBy { ORDERED_MODIFIERS.indexOf(it.elementType) }) }
            if (!modifierArr.contentEquals(sorted)) {
                // Since annotations can be fairly lengthy and/or span multiple lines we are
                // squashing them into a single placeholder text to guarantee a single line output
                var autocorrect = false
                squashAnnotations(sorted)
                    .joinToString(" ")
                    .let { squashedAnnotations ->
                        emit(
                            node.startOffset,
                            "Incorrect modifier order (should be \"$squashedAnnotations\")",
                        ) { autocorrect = true }
                    }
                if (autocorrect) {
                    modifierArr.forEachIndexed { i, n ->
                        node.replaceChild(n, sorted[i].clone() as ASTNode)
                    }
                }
            }
        }
    }

    private fun squashAnnotations(sorted: Array<ASTNode>): List<String> {
        val nonAnnotationModifiers = sorted.filter { it.elementType != ANNOTATION_ENTRY }
        return if (nonAnnotationModifiers.size != sorted.size) {
            listOf("@Annotation...") + nonAnnotationModifiers.map { it.text }
        } else {
            nonAnnotationModifiers.map { it.text }
        }
    }

    private companion object {
        // subset of ElementType.MODIFIER_KEYWORDS_ARRAY (+ annotations entries)
        private val ORDERED_MODIFIERS =
            arrayOf(
                ANNOTATION_ENTRY,
                CONTEXT_PARAMETER_LIST,
                PUBLIC_KEYWORD,
                PROTECTED_KEYWORD,
                PRIVATE_KEYWORD,
                INTERNAL_KEYWORD,
                EXPECT_KEYWORD,
                ACTUAL_KEYWORD,
                FINAL_KEYWORD,
                OPEN_KEYWORD,
                ABSTRACT_KEYWORD,
                SEALED_KEYWORD,
                CONST_KEYWORD,
                EXTERNAL_KEYWORD,
                OVERRIDE_KEYWORD,
                LATEINIT_KEYWORD,
                TAILREC_KEYWORD,
                VARARG_KEYWORD,
                SUSPEND_KEYWORD,
                INNER_KEYWORD,
                ENUM_KEYWORD,
                ANNOTATION_KEYWORD,
                COMPANION_KEYWORD,
                INLINE_KEYWORD,
                INFIX_KEYWORD,
                OPERATOR_KEYWORD,
                DATA_KEYWORD,
                // NOINLINE_KEYWORD, CROSSINLINE_KEYWORD, OUT_KEYWORD, IN_KEYWORD, REIFIED_KEYWORD
            )
        private val tokenSet = TokenSet.create(*ORDERED_MODIFIERS)
    }
}

public val MODIFIER_ORDER_RULE_ID: RuleId = ModifierOrderRule().ruleId
