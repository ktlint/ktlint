package io.github.ktlint.core.ruleset.standard.rules

import com.intellij.lang.ASTNode
import com.intellij.psi.tree.TokenSet
import io.github.ktlint.core.rule.engine.core.api.ElementType.ANDAND
import io.github.ktlint.core.rule.engine.core.api.ElementType.ARROW
import io.github.ktlint.core.rule.engine.core.api.ElementType.DIV
import io.github.ktlint.core.rule.engine.core.api.ElementType.DIVEQ
import io.github.ktlint.core.rule.engine.core.api.ElementType.ELVIS
import io.github.ktlint.core.rule.engine.core.api.ElementType.EQ
import io.github.ktlint.core.rule.engine.core.api.ElementType.EQEQ
import io.github.ktlint.core.rule.engine.core.api.ElementType.EQEQEQ
import io.github.ktlint.core.rule.engine.core.api.ElementType.EXCLEQ
import io.github.ktlint.core.rule.engine.core.api.ElementType.EXCLEQEQEQ
import io.github.ktlint.core.rule.engine.core.api.ElementType.GT
import io.github.ktlint.core.rule.engine.core.api.ElementType.GTEQ
import io.github.ktlint.core.rule.engine.core.api.ElementType.IDENTIFIER
import io.github.ktlint.core.rule.engine.core.api.ElementType.IMPORT_DIRECTIVE
import io.github.ktlint.core.rule.engine.core.api.ElementType.LT
import io.github.ktlint.core.rule.engine.core.api.ElementType.LTEQ
import io.github.ktlint.core.rule.engine.core.api.ElementType.MINUS
import io.github.ktlint.core.rule.engine.core.api.ElementType.MINUSEQ
import io.github.ktlint.core.rule.engine.core.api.ElementType.MUL
import io.github.ktlint.core.rule.engine.core.api.ElementType.MULTEQ
import io.github.ktlint.core.rule.engine.core.api.ElementType.OPERATION_REFERENCE
import io.github.ktlint.core.rule.engine.core.api.ElementType.OROR
import io.github.ktlint.core.rule.engine.core.api.ElementType.PERC
import io.github.ktlint.core.rule.engine.core.api.ElementType.PERCEQ
import io.github.ktlint.core.rule.engine.core.api.ElementType.PLUS
import io.github.ktlint.core.rule.engine.core.api.ElementType.PLUSEQ
import io.github.ktlint.core.rule.engine.core.api.ElementType.PREFIX_EXPRESSION
import io.github.ktlint.core.rule.engine.core.api.ElementType.VALUE_ARGUMENT
import io.github.ktlint.core.rule.engine.core.api.RuleId
import io.github.ktlint.core.rule.engine.core.api.SinceKtlint
import io.github.ktlint.core.rule.engine.core.api.SinceKtlint.Status.STABLE
import io.github.ktlint.core.rule.engine.core.api.isPartOf
import io.github.ktlint.core.rule.engine.core.api.isWhiteSpace
import io.github.ktlint.core.rule.engine.core.api.nextLeaf
import io.github.ktlint.core.rule.engine.core.api.parent
import io.github.ktlint.core.rule.engine.core.api.prevLeaf
import io.github.ktlint.core.rule.engine.core.api.upsertWhitespaceAfterMe
import io.github.ktlint.core.rule.engine.core.api.upsertWhitespaceBeforeMe
import io.github.ktlint.core.ruleset.standard.StandardRule

@SinceKtlint("0.1", STABLE)
public class SpacingAroundOperatorsRule : StandardRule("op-spacing") {
    override fun beforeVisitChildNodes(node: ASTNode) {
        if (node.elementType in OPERATORS ||
            (node.elementType == IDENTIFIER && node.parent?.elementType == OPERATION_REFERENCE)
        ) {
            node.visitOperator()
        }
    }

    private fun ASTNode.visitOperator() {
        if (shouldCheckSpacing()) {
            val spacingBefore = prevLeaf.isWhiteSpace
            val spacingAfter = nextLeaf.isWhiteSpace
            when {
                !spacingBefore && !spacingAfter -> {
                    emit(startOffset, "Missing spacing around \"$text\"") {
                        upsertWhitespaceBeforeMe(" ")
                        upsertWhitespaceAfterMe(" ")
                    }
                }

                !spacingBefore -> {
                    emit(startOffset, "Missing spacing before \"$text\"") {
                        upsertWhitespaceBeforeMe(" ")
                    }
                }

                !spacingAfter -> {
                    emit(startOffset + textLength, "Missing spacing after \"$text\"") {
                        upsertWhitespaceAfterMe(" ")
                    }
                }
            }
        }
    }

    private fun ASTNode.shouldCheckSpacing(): Boolean =
        !isUnaryOperator() &&
            !isSpreadOperator() &&
            !isImport() &&
            !isTypeParameterOrSpreadOperator()

    private fun ASTNode.isTypeParameterOrSpreadOperator() =
        // Allow:
        //   <T> fun foo(...)
        //   class Foo<T> { ... }
        //   Foo<*>
        parent?.elementType != OPERATION_REFERENCE &&
            (elementType == LT || elementType == GT || elementType == MUL)

    private fun ASTNode.isUnaryOperator() =
        // Allow:
        //   val foo = -1
        PREFIX_EXPRESSION ==
            parent { it.elementType == OPERATION_REFERENCE }
                ?.parent
                ?.elementType

    private fun ASTNode.isSpreadOperator() =
        // Allow:
        // fn(*array)
        elementType == MUL && parent?.elementType == VALUE_ARGUMENT

    private fun ASTNode.isImport() =
        // Allow:
        // import *
        isPartOf(IMPORT_DIRECTIVE)

    private companion object {
        private val OPERATORS =
            TokenSet.create(
                ANDAND,
                ARROW,
                DIV,
                DIVEQ,
                ELVIS,
                EQ,
                EQEQ,
                EQEQEQ,
                EXCLEQ,
                EXCLEQEQEQ,
                GT,
                GTEQ,
                LT,
                LTEQ,
                MINUS,
                MINUSEQ,
                MUL,
                MULTEQ,
                OROR,
                PERC,
                PERCEQ,
                PLUS,
                PLUSEQ,
            )
    }
}

public val SPACING_AROUND_OPERATORS_RULE_ID: RuleId = SpacingAroundOperatorsRule().ruleId
