package io.github.ktlint.core.ruleset.standard.rules

import com.intellij.lang.ASTNode
import io.github.ktlint.core.rule.engine.core.api.ElementType.POSTFIX_EXPRESSION
import io.github.ktlint.core.rule.engine.core.api.ElementType.PREFIX_EXPRESSION
import io.github.ktlint.core.rule.engine.core.api.RuleId
import io.github.ktlint.core.rule.engine.core.api.SinceKtlint
import io.github.ktlint.core.rule.engine.core.api.SinceKtlint.Status.EXPERIMENTAL
import io.github.ktlint.core.rule.engine.core.api.SinceKtlint.Status.STABLE
import io.github.ktlint.core.rule.engine.core.api.children
import io.github.ktlint.core.rule.engine.core.api.isPartOfComment
import io.github.ktlint.core.rule.engine.core.api.isWhiteSpace
import io.github.ktlint.core.rule.engine.core.api.remove
import io.github.ktlint.core.ruleset.standard.StandardRule

/**
 * Ensures there are no spaces around unary operators
 *
 * @see [Kotlin Style Guide](https://kotlinlang.org/docs/reference/coding-conventions.html#horizontal-whitespace)
 */
@SinceKtlint("0.38", EXPERIMENTAL)
@SinceKtlint("0.46", STABLE)
public class SpacingAroundUnaryOperatorRule : StandardRule("unary-op-spacing") {
    override fun beforeVisitChildNodes(node: ASTNode) {
        if (node.elementType == PREFIX_EXPRESSION || node.elementType == POSTFIX_EXPRESSION) {
            val children = node.children.toList()

            // ignore: var a = + /* comment */ 1
            if (children.any { it.isPartOfComment }) {
                return
            }

            children
                .firstOrNull { it.isWhiteSpace }
                ?.let { whiteSpace ->
                    emit(whiteSpace.startOffset, "Unexpected spacing in ${node.text.replace("\n", "\\n")}") { whiteSpace.remove() }
                }
        }
    }
}

public val SPACING_AROUND_UNARY_OPERATOR_RULE_ID: RuleId = SpacingAroundUnaryOperatorRule().ruleId
