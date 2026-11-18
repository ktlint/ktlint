package io.github.ktlint.core.ruleset.standard.rules

import com.intellij.lang.ASTNode
import io.github.ktlint.core.rule.engine.core.api.ElementType.CALLABLE_REFERENCE_EXPRESSION
import io.github.ktlint.core.rule.engine.core.api.ElementType.CLASS_LITERAL_EXPRESSION
import io.github.ktlint.core.rule.engine.core.api.ElementType.COLONCOLON
import io.github.ktlint.core.rule.engine.core.api.RuleId
import io.github.ktlint.core.rule.engine.core.api.SinceKtlint
import io.github.ktlint.core.rule.engine.core.api.SinceKtlint.Status.STABLE
import io.github.ktlint.core.rule.engine.core.api.isPartOf
import io.github.ktlint.core.rule.engine.core.api.isWhiteSpace
import io.github.ktlint.core.rule.engine.core.api.isWhiteSpaceWithoutNewline
import io.github.ktlint.core.rule.engine.core.api.nextLeaf
import io.github.ktlint.core.rule.engine.core.api.prevLeaf
import io.github.ktlint.core.rule.engine.core.api.prevSibling
import io.github.ktlint.core.rule.engine.core.api.remove
import io.github.ktlint.core.rule.engine.core.api.replaceTextWith
import io.github.ktlint.core.ruleset.standard.StandardRule

@SinceKtlint("0.37", STABLE)
public class SpacingAroundDoubleColonRule : StandardRule("double-colon-spacing") {
    override fun beforeVisitChildNodes(node: ASTNode) {
        if (node.elementType == COLONCOLON) {
            val prevLeaf = node.prevLeaf
            val nextLeaf = node.nextLeaf

            var removeSingleWhiteSpace = false
            val spacingBefore =
                when {
                    node.isPartOf(CLASS_LITERAL_EXPRESSION) && prevLeaf.isWhiteSpace -> {
                        true
                    }

                    // Clazz::class
                    node.isPartOf(CALLABLE_REFERENCE_EXPRESSION) && prevLeaf.isWhiteSpace -> {
                        // String::length, ::isOdd
                        if (node.prevSibling == null) { // compose(length, ::isOdd), val predicate = ::isOdd
                            removeSingleWhiteSpace = true
                            prevLeaf
                                .takeIf { it.isWhiteSpaceWithoutNewline }
                                ?.let { it.textLength > 1 }
                                ?: false
                        } else { // String::length, List<String>::isEmpty
                            prevLeaf.isWhiteSpaceWithoutNewline
                        }
                    }

                    else -> {
                        false
                    }
                }
            val spacingAfter = nextLeaf.isWhiteSpace
            when {
                spacingBefore && spacingAfter -> {
                    emit(node.startOffset, "Unexpected spacing around \"${node.text}\"") {
                        prevLeaf!!.removeSelf(removeSingleWhiteSpace)
                        nextLeaf!!.remove()
                    }
                }

                spacingBefore -> {
                    emit(prevLeaf!!.startOffset, "Unexpected spacing before \"${node.text}\"") {
                        prevLeaf.removeSelf(removeSingleWhiteSpace)
                    }
                }

                spacingAfter -> {
                    emit(nextLeaf!!.startOffset, "Unexpected spacing after \"${node.text}\"") { nextLeaf.remove() }
                }
            }
        }
    }

    private fun ASTNode.removeSelf(removeSingleWhiteSpace: Boolean) {
        if (removeSingleWhiteSpace) {
            replaceTextWith(text.substring(0, textLength - 1))
        } else {
            this.remove()
        }
    }
}

public val SPACING_AROUND_DOUBLE_COLON_RULE_ID: RuleId = SpacingAroundDoubleColonRule().ruleId
