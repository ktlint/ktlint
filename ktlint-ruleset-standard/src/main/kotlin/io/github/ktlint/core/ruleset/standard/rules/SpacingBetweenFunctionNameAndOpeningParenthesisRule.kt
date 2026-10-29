package io.github.ktlint.core.ruleset.standard.rules

import com.intellij.lang.ASTNode
import io.github.ktlint.core.rule.engine.core.api.ElementType.FUN
import io.github.ktlint.core.rule.engine.core.api.ElementType.IDENTIFIER
import io.github.ktlint.core.rule.engine.core.api.RuleId
import io.github.ktlint.core.rule.engine.core.api.SinceKtlint
import io.github.ktlint.core.rule.engine.core.api.SinceKtlint.Status.EXPERIMENTAL
import io.github.ktlint.core.rule.engine.core.api.SinceKtlint.Status.STABLE
import io.github.ktlint.core.rule.engine.core.api.isWhiteSpace
import io.github.ktlint.core.rule.engine.core.api.nextSibling
import io.github.ktlint.core.rule.engine.core.api.remove
import io.github.ktlint.core.ruleset.standard.StandardRule

@SinceKtlint("0.46", EXPERIMENTAL)
@SinceKtlint("0.49", STABLE)
public class SpacingBetweenFunctionNameAndOpeningParenthesisRule : StandardRule("spacing-between-function-name-and-opening-parenthesis") {
    override fun beforeVisitChildNodes(node: ASTNode) {
        node
            .takeIf { node.elementType == FUN }
            ?.findChildByType(IDENTIFIER)
            ?.nextSibling
            ?.takeIf { it.isWhiteSpace }
            ?.let { whiteSpace ->
                emit(whiteSpace.startOffset, "Unexpected whitespace") { whiteSpace.remove() }
            }
    }
}

public val SPACING_BETWEEN_FUNCTION_NAME_AND_OPENING_PARENTHESIS_RULE_ID: RuleId =
    SpacingBetweenFunctionNameAndOpeningParenthesisRule().ruleId
