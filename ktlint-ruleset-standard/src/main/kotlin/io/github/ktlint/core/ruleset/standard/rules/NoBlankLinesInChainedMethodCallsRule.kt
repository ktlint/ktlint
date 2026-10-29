package io.github.ktlint.core.ruleset.standard.rules

import com.intellij.lang.ASTNode
import io.github.ktlint.core.rule.engine.core.api.ElementType.DOT_QUALIFIED_EXPRESSION
import io.github.ktlint.core.rule.engine.core.api.RuleId
import io.github.ktlint.core.rule.engine.core.api.SinceKtlint
import io.github.ktlint.core.rule.engine.core.api.isWhiteSpace
import io.github.ktlint.core.rule.engine.core.api.parent
import io.github.ktlint.core.rule.engine.core.api.replaceTextWith
import io.github.ktlint.core.ruleset.standard.StandardRule

@SinceKtlint("0.46", SinceKtlint.Status.STABLE)
public class NoBlankLinesInChainedMethodCallsRule : StandardRule("no-blank-lines-in-chained-method-calls") {
    override fun beforeVisitChildNodes(node: ASTNode) {
        val isBlankLine = node.isWhiteSpace && node.text.contains("\n\n")
        if (isBlankLine && node.parent?.elementType == DOT_QUALIFIED_EXPRESSION) {
            emit(node.startOffset + 1, "Needless blank line(s)") {
                node.replaceTextWith("\n" + node.text.split("\n\n")[1])
            }
        }
    }
}

public val NO_BLANK_LINES_IN_CHAINED_METHOD_CALLS_RULE_ID: RuleId = NoBlankLinesInChainedMethodCallsRule().ruleId
