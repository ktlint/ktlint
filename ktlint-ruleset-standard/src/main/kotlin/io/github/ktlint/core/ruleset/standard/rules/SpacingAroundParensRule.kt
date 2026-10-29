package io.github.ktlint.core.ruleset.standard.rules

import com.intellij.lang.ASTNode
import com.intellij.psi.tree.TokenSet
import io.github.ktlint.core.rule.engine.core.api.ElementType.BLOCK_COMMENT
import io.github.ktlint.core.rule.engine.core.api.ElementType.CONSTRUCTOR_CALLEE
import io.github.ktlint.core.rule.engine.core.api.ElementType.EOL_COMMENT
import io.github.ktlint.core.rule.engine.core.api.ElementType.FUNCTION_TYPE
import io.github.ktlint.core.rule.engine.core.api.ElementType.IDENTIFIER
import io.github.ktlint.core.rule.engine.core.api.ElementType.KDOC_START
import io.github.ktlint.core.rule.engine.core.api.ElementType.LPAR
import io.github.ktlint.core.rule.engine.core.api.ElementType.PRIMARY_CONSTRUCTOR
import io.github.ktlint.core.rule.engine.core.api.ElementType.RPAR
import io.github.ktlint.core.rule.engine.core.api.ElementType.SUPER_KEYWORD
import io.github.ktlint.core.rule.engine.core.api.ElementType.SUPER_TYPE_CALL_ENTRY
import io.github.ktlint.core.rule.engine.core.api.ElementType.VALUE_ARGUMENT_LIST
import io.github.ktlint.core.rule.engine.core.api.ElementType.VALUE_PARAMETER_LIST
import io.github.ktlint.core.rule.engine.core.api.RuleId
import io.github.ktlint.core.rule.engine.core.api.SinceKtlint
import io.github.ktlint.core.rule.engine.core.api.SinceKtlint.Status.STABLE
import io.github.ktlint.core.rule.engine.core.api.isWhiteSpace
import io.github.ktlint.core.rule.engine.core.api.isWhiteSpaceWithNewline
import io.github.ktlint.core.rule.engine.core.api.isWhiteSpaceWithoutNewline
import io.github.ktlint.core.rule.engine.core.api.nextLeaf
import io.github.ktlint.core.rule.engine.core.api.nextSibling
import io.github.ktlint.core.rule.engine.core.api.parent
import io.github.ktlint.core.rule.engine.core.api.prevLeaf
import io.github.ktlint.core.rule.engine.core.api.prevSibling
import io.github.ktlint.core.rule.engine.core.api.remove
import io.github.ktlint.core.ruleset.standard.StandardRule
import org.jetbrains.kotlin.psi.psiUtil.siblings

/**
 * Ensures there are no extra spaces around parentheses.
 *
 * See https://kotlinlang.org/docs/reference/coding-conventions.html#horizontal-whitespace
 */
@SinceKtlint("0.24", STABLE)
public class SpacingAroundParensRule : StandardRule("paren-spacing") {
    override fun beforeVisitChildNodes(node: ASTNode) {
        if (node.elementType == LPAR || node.elementType == RPAR) {
            val spacingBefore = node.isUnexpectedSpacingBeforeParenthesis()
            val spacingAfter = node.isUnexpectedSpacingAfterParenthesis()
            when {
                spacingBefore && spacingAfter -> node.fixUnexpectedSpacingAround()
                spacingBefore -> node.fixUnexpectedSpacingBefore()
                spacingAfter -> node.fixUnexpectSpacingAfter()
            }
        }
    }

    private fun ASTNode.isUnexpectedSpacingBeforeParenthesis(): Boolean =
        when {
            prevLeaf.isWhiteSpaceWithNewline && hasNoNewlineAfterLpar() -> {
                true
            }

            !prevLeaf.isWhiteSpaceWithoutNewline -> {
                false
            }

            elementType == LPAR -> {
                parent?.elementType in elementListTokenSet &&
                    (
                        isUnexpectedSpacingBetweenIdentifierAndElementList() ||
                            isUnexpectedSpacingInCallToSuper() ||
                            isUnexpectedSpacingInExplicitConstructor() ||
                            isUnexpectedSpacingInSuperTypeCallEntry()
                    )
            }

            elementType == RPAR -> {
                // Disallow:
                //    val foo = fn("foo" )
                //    val foo = fn( )
                prevLeaf?.prevSibling?.elementType != LPAR
            }

            else -> {
                false
            }
        }

    private fun ASTNode.isUnexpectedSpacingBetweenIdentifierAndElementList() =
        prevLeaf
            ?.takeIf { it.isWhiteSpace }
            ?.takeIf {
                // Disallow:
                //     fun foo () {}
                // and
                //     @Deprecated ("bar")
                //     fun foo() {}
                it.prevLeaf?.elementType == IDENTIFIER
            }?.let {
                // But do allow:
                //     val foo: @Composable () -> Unit
                parent?.parent?.elementType != FUNCTION_TYPE
            }
            ?: false

    private fun ASTNode.isUnexpectedSpacingInCallToSuper() =
        prevLeaf
            ?.takeIf { it.isWhiteSpace }
            ?.let {
                // Disallow:
                //     class Foo : Bar {
                //         constructor(string: String) : super ()
                //     }
                it.prevLeaf?.elementType == SUPER_KEYWORD
            }
            ?: false

    private fun ASTNode.isUnexpectedSpacingInExplicitConstructor() =
        prevLeaf
            ?.takeIf { it.isWhiteSpace }
            ?.let {
                // Disallow:
                //     class Foo constructor ()
                it.prevLeaf?.parent?.elementType == PRIMARY_CONSTRUCTOR
            }
            ?: false

    private fun ASTNode.isUnexpectedSpacingInSuperTypeCallEntry() =
        prevLeaf
            ?.takeIf { it.isWhiteSpace }
            ?.let {
                // Disallow:
                //     class Foo : Bar ("test")
                //     class Foo : Bar<String> ("test")
                parent?.parent?.elementType == SUPER_TYPE_CALL_ENTRY &&
                    it.prevSibling?.elementType == CONSTRUCTOR_CALLEE
            }
            ?: false

    private fun ASTNode.isUnexpectedSpacingAfterParenthesis(): Boolean =
        when (elementType) {
            LPAR if nextSibling.isWhiteSpaceWithNewline && hasNoOtherNewlineBeforeRpar() -> {
                true
            }

            LPAR -> {
                nextLeaf
                    ?.takeUnless { it.isNextLeafAComment() }
                    ?.let { it.isUnexpectedSpaceAfterLpar() || it.isUnexpectedNewlineAfterLpar() }
                    ?: false
            }

            else -> {
                false
            }
        }

    private fun ASTNode.isUnexpectedSpaceAfterLpar() =
        // Disallow:
        //     val foo = fn( )
        //     val foo = fn( "bar")
        //     val foo = ( (1 + 2) / 3)
        isWhiteSpaceWithoutNewline

    private fun ASTNode.isUnexpectedNewlineAfterLpar() =
        // Disallow:
        //     val foo = fn(
        //         )
        isWhiteSpaceWithNewline && nextLeaf?.elementType == RPAR

    private fun ASTNode.hasNoOtherNewlineBeforeRpar() =
        nextSibling
            .takeIf { it.isWhiteSpaceWithNewline }
            ?.siblings()
            ?.takeWhile { it.elementType != RPAR }
            ?.none { it.isWhiteSpaceWithNewline }
            ?: false

    private fun ASTNode.isNextLeafAComment(): Boolean = nextLeaf?.elementType in commentTypes

    private fun ASTNode.hasNoNewlineAfterLpar() =
        prevSibling
            .takeIf { it.isWhiteSpaceWithNewline }
            ?.takeUnless { it.prevSibling?.elementType == LPAR }
            ?.siblings(false)
            ?.takeWhile { it.elementType != LPAR }
            ?.none { it.textContains('\n') || it.elementType == EOL_COMMENT }
            ?: false

    private fun ASTNode.fixUnexpectedSpacingAround() {
        emit(startOffset, "Unexpected spacing around \"$text\"") {
            prevLeaf!!.remove()
            nextLeaf!!.remove()
        }
    }

    private fun ASTNode.fixUnexpectedSpacingBefore() {
        emit(prevLeaf!!.startOffset, "Unexpected spacing before \"$text\"") { prevLeaf?.remove() }
    }

    private fun ASTNode.fixUnexpectSpacingAfter() {
        emit(startOffset + 1, "Unexpected spacing after \"$text\"") { nextLeaf!!.remove() }
    }

    private companion object {
        val elementListTokenSet = TokenSet.create(VALUE_PARAMETER_LIST, VALUE_ARGUMENT_LIST)
        val commentTypes = TokenSet.create(EOL_COMMENT, BLOCK_COMMENT, KDOC_START)
    }
}

public val SPACING_AROUND_PARENS_RULE_ID: RuleId = SpacingAroundParensRule().ruleId
