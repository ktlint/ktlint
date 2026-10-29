package io.github.ktlint.core.ruleset.standard.rules

import com.intellij.lang.ASTNode
import io.github.ktlint.core.rule.engine.core.api.ElementType.CLASS_BODY
import io.github.ktlint.core.rule.engine.core.api.ElementType.FUN
import io.github.ktlint.core.rule.engine.core.api.ElementType.LBRACE
import io.github.ktlint.core.rule.engine.core.api.RuleId
import io.github.ktlint.core.rule.engine.core.api.SinceKtlint
import io.github.ktlint.core.rule.engine.core.api.SinceKtlint.Status.EXPERIMENTAL
import io.github.ktlint.core.rule.engine.core.api.SinceKtlint.Status.STABLE
import io.github.ktlint.core.rule.engine.core.api.isPartOf
import io.github.ktlint.core.rule.engine.core.api.isWhiteSpaceWithNewline
import io.github.ktlint.core.rule.engine.core.api.parent
import io.github.ktlint.core.rule.engine.core.api.prevLeaf
import io.github.ktlint.core.rule.engine.core.api.replaceTextWith
import io.github.ktlint.core.ruleset.standard.StandardRule

@SinceKtlint("0.34", EXPERIMENTAL)
@SinceKtlint("0.46", STABLE)
public class NoEmptyFirstLineInMethodBlockRule : StandardRule("no-empty-first-line-in-method-block") {
    override fun beforeVisitChildNodes(node: ASTNode) {
        if (node.isWhiteSpaceAfterOpeningBraceOfFunBody()) {
            val split = node.text.split("\n")
            if (split.size > 2) {
                emit(node.startOffset + 1, "First line in a method block should not be empty") {
                    node.replaceTextWith("${split.first()}\n${split.last()}")
                }
            }
        }
    }

    private fun ASTNode.isWhiteSpaceAfterOpeningBraceOfFunBody() =
        isWhiteSpaceWithNewline &&
            prevLeaf?.elementType == LBRACE &&
            isPartOf(FUN) &&
            // Allow: fun fn() = object : Builder {\n\n fun stuff() = Unit }
            parent?.elementType != CLASS_BODY
}

public val NO_EMPTY_FIRST_LINE_IN_METHOD_BLOCK_RULE_ID: RuleId = NoEmptyFirstLineInMethodBlockRule().ruleId
