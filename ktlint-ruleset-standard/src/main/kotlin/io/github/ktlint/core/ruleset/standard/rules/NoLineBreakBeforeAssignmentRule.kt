package io.github.ktlint.core.ruleset.standard.rules

import com.intellij.lang.ASTNode
import com.intellij.psi.impl.source.tree.LeafPsiElement
import com.intellij.psi.impl.source.tree.PsiWhiteSpaceImpl
import io.github.ktlint.core.rule.engine.core.api.ElementType.EQ
import io.github.ktlint.core.rule.engine.core.api.RuleId
import io.github.ktlint.core.rule.engine.core.api.SinceKtlint
import io.github.ktlint.core.rule.engine.core.api.SinceKtlint.Status.STABLE
import io.github.ktlint.core.rule.engine.core.api.isCode
import io.github.ktlint.core.rule.engine.core.api.isWhiteSpace
import io.github.ktlint.core.rule.engine.core.api.isWhiteSpaceWithNewline
import io.github.ktlint.core.rule.engine.core.api.nextSibling
import io.github.ktlint.core.rule.engine.core.api.parent
import io.github.ktlint.core.rule.engine.core.api.prevSibling
import io.github.ktlint.core.rule.engine.core.api.remove
import io.github.ktlint.core.ruleset.standard.StandardRule
import org.jetbrains.kotlin.psi.psiUtil.siblings

@SinceKtlint("0.13", STABLE)
public class NoLineBreakBeforeAssignmentRule : StandardRule("no-line-break-before-assignment") {
    override fun beforeVisitChildNodes(node: ASTNode) {
        if (node.elementType == EQ) {
            node.visitEquals()
        }
    }

    private fun ASTNode.visitEquals() {
        prevSibling
            .takeIf { it.isWhiteSpaceWithNewline }
            ?.let { unexpectedNewlineBeforeAssignment ->
                emit(unexpectedNewlineBeforeAssignment.startOffset, "Line break before assignment is not allowed") {
                    val parent = parent!!
                    // Insert assignment surrounded by whitespaces at new position
                    siblings(false)
                        .takeWhile { !it.isCode }
                        .last()
                        .let { before ->
                            if (!before.prevSibling.isWhiteSpace) {
                                parent.addChild(PsiWhiteSpaceImpl(" "), before)
                            }
                            parent.addChild(LeafPsiElement(EQ, "="), before)
                            if (!before.isWhiteSpace) {
                                parent.addChild(PsiWhiteSpaceImpl(" "), before)
                            }
                        }
                    // Cleanup old assignment and whitespace after it. The indent before the old assignment is kept unchanged
                    nextSibling
                        .takeIf { it.isWhiteSpace }
                        ?.remove()
                    remove()
                }
            }
    }
}

public val NO_LINE_BREAK_BEFORE_ASSIGNMENT_RULE_ID: RuleId = NoLineBreakBeforeAssignmentRule().ruleId
