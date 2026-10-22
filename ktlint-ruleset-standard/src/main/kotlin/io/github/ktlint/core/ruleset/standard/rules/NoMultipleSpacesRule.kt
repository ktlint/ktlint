package io.github.ktlint.core.ruleset.standard.rules

import com.intellij.lang.ASTNode
import io.github.ktlint.core.rule.engine.core.api.ElementType.KDOC_MARKDOWN_LINK
import io.github.ktlint.core.rule.engine.core.api.ElementType.KDOC_TAG
import io.github.ktlint.core.rule.engine.core.api.RuleId
import io.github.ktlint.core.rule.engine.core.api.SinceKtlint
import io.github.ktlint.core.rule.engine.core.api.SinceKtlint.Status.STABLE
import io.github.ktlint.core.rule.engine.core.api.isWhiteSpace
import io.github.ktlint.core.rule.engine.core.api.parent
import io.github.ktlint.core.rule.engine.core.api.prevSibling
import io.github.ktlint.core.rule.engine.core.api.replaceTextWith
import io.github.ktlint.core.ruleset.standard.StandardRule

@SinceKtlint("0.2", STABLE)
public class NoMultipleSpacesRule : StandardRule("no-multi-spaces") {
    override fun beforeVisitChildNodes(node: ASTNode) {
        node
            .takeIf { node.isWhiteSpace }
            .takeUnless { node.isPossibleAlignmentOfKdocTag() }
            ?.let {
                val beforeIndentation = node.removeIndentation()
                if (beforeIndentation.length > 1) {
                    emit(node.startOffset + 1, "Unnecessary long whitespace") {
                        val remainder = node.text.substring(beforeIndentation.length)
                        node.replaceTextWith(" $remainder")
                    }
                }
            }
    }

    private fun ASTNode.removeIndentation() = this.text.substringBefore("\n")

    // allow multiple spaces in KDoc in case of KDOC_TAG for alignment, e.g.
    // @param foo      stuff
    // @param foobar   stuff2
    private fun ASTNode.isPossibleAlignmentOfKdocTag() = prevSibling?.elementType == KDOC_MARKDOWN_LINK && parent?.elementType == KDOC_TAG
}

public val NO_MULTIPLE_SPACES_RULE_ID: RuleId = NoMultipleSpacesRule().ruleId
