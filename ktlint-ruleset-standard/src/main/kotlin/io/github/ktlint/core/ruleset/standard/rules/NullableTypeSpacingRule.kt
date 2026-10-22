package io.github.ktlint.core.ruleset.standard.rules

import com.intellij.lang.ASTNode
import com.intellij.psi.impl.source.tree.LeafPsiElement
import io.github.ktlint.core.rule.engine.core.api.ElementType.QUEST
import io.github.ktlint.core.rule.engine.core.api.RuleId
import io.github.ktlint.core.rule.engine.core.api.SinceKtlint
import io.github.ktlint.core.rule.engine.core.api.SinceKtlint.Status.EXPERIMENTAL
import io.github.ktlint.core.rule.engine.core.api.SinceKtlint.Status.STABLE
import io.github.ktlint.core.rule.engine.core.api.isWhiteSpace
import io.github.ktlint.core.rule.engine.core.api.prevLeaf
import io.github.ktlint.core.ruleset.standard.StandardRule

@SinceKtlint("0.46", EXPERIMENTAL)
@SinceKtlint("0.49", STABLE)
public class NullableTypeSpacingRule : StandardRule("nullable-type-spacing") {
    override fun beforeVisitChildNodes(node: ASTNode) {
        node
            .takeIf { node.elementType == QUEST }
            ?.prevLeaf
            ?.takeIf { it.isWhiteSpace }
            ?.let { whiteSpaceBeforeQuest ->
                emit(whiteSpaceBeforeQuest.startOffset, "Unexpected whitespace") {
                    (whiteSpaceBeforeQuest as LeafPsiElement).rawRemove()
                }
            }
    }
}

public val NULLABLE_TYPE_SPACING_RULE_ID: RuleId = NullableTypeSpacingRule().ruleId
