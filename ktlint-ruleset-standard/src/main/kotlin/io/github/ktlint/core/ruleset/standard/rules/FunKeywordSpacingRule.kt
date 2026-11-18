package io.github.ktlint.core.ruleset.standard.rules

import com.intellij.lang.ASTNode
import com.intellij.psi.impl.source.tree.PsiWhiteSpaceImpl
import io.github.ktlint.core.rule.engine.core.api.ElementType.FUN_KEYWORD
import io.github.ktlint.core.rule.engine.core.api.ElementType.IDENTIFIER
import io.github.ktlint.core.rule.engine.core.api.ElementType.WHITE_SPACE
import io.github.ktlint.core.rule.engine.core.api.RuleId
import io.github.ktlint.core.rule.engine.core.api.SinceKtlint
import io.github.ktlint.core.rule.engine.core.api.SinceKtlint.Status.EXPERIMENTAL
import io.github.ktlint.core.rule.engine.core.api.SinceKtlint.Status.STABLE
import io.github.ktlint.core.rule.engine.core.api.nextLeaf
import io.github.ktlint.core.rule.engine.core.api.parent
import io.github.ktlint.core.rule.engine.core.api.replaceTextWith
import io.github.ktlint.core.ruleset.standard.StandardRule

/**
 * Lints and formats the spacing after the fun keyword
 */
@SinceKtlint("0.45", EXPERIMENTAL)
@SinceKtlint("0.49", STABLE)
public class FunKeywordSpacingRule : StandardRule("fun-keyword-spacing") {
    override fun beforeVisitChildNodes(node: ASTNode) {
        node
            .takeIf { it.elementType == FUN_KEYWORD }
            ?.nextLeaf
            ?.let { leafAfterFunKeyword ->
                when (leafAfterFunKeyword.elementType) {
                    WHITE_SPACE if leafAfterFunKeyword.text != " " -> {
                        emit(
                            leafAfterFunKeyword.startOffset,
                            "Single space expected after the fun keyword",
                        ) { leafAfterFunKeyword.replaceTextWith(" ") }
                    }

                    IDENTIFIER -> {
                        // Identifier can only be adjacent to fun keyword in case the identifier is wrapped between backticks:
                        //     fun`foo`() {}
                        emit(leafAfterFunKeyword.startOffset, "Space expected between the fun keyword and backtick") {
                            leafAfterFunKeyword.parent?.addChild(PsiWhiteSpaceImpl(" "), leafAfterFunKeyword)
                        }
                    }

                    else -> {
                        Unit
                    }
                }
            }
    }
}

public val FUN_KEYWORD_SPACING_RULE: RuleId = FunKeywordSpacingRule().ruleId
