package io.github.ktlint.core.ruleset.standard.rules

import com.intellij.lang.ASTNode
import com.intellij.psi.impl.source.tree.LeafPsiElement
import com.intellij.psi.impl.source.tree.PsiWhiteSpaceImpl
import com.intellij.psi.tree.IElementType
import io.github.ktlint.core.rule.engine.core.api.ElementType.BLOCK
import io.github.ktlint.core.rule.engine.core.api.ElementType.COLON
import io.github.ktlint.core.rule.engine.core.api.ElementType.EQ
import io.github.ktlint.core.rule.engine.core.api.ElementType.FUN
import io.github.ktlint.core.rule.engine.core.api.ElementType.LBRACE
import io.github.ktlint.core.rule.engine.core.api.ElementType.RBRACE
import io.github.ktlint.core.rule.engine.core.api.ElementType.RETURN
import io.github.ktlint.core.rule.engine.core.api.ElementType.RETURN_KEYWORD
import io.github.ktlint.core.rule.engine.core.api.ElementType.THROW
import io.github.ktlint.core.rule.engine.core.api.ElementType.TYPE_REFERENCE
import io.github.ktlint.core.rule.engine.core.api.IndentConfig
import io.github.ktlint.core.rule.engine.core.api.KtlintKotlinCompiler
import io.github.ktlint.core.rule.engine.core.api.RuleId
import io.github.ktlint.core.rule.engine.core.api.SinceKtlint
import io.github.ktlint.core.rule.engine.core.api.SinceKtlint.Status.EXPERIMENTAL
import io.github.ktlint.core.rule.engine.core.api.SinceKtlint.Status.STABLE
import io.github.ktlint.core.rule.engine.core.api.children
import io.github.ktlint.core.rule.engine.core.api.editorconfig.CODE_STYLE_PROPERTY
import io.github.ktlint.core.rule.engine.core.api.editorconfig.EditorConfig
import io.github.ktlint.core.rule.engine.core.api.editorconfig.INDENT_SIZE_PROPERTY
import io.github.ktlint.core.rule.engine.core.api.editorconfig.INDENT_STYLE_PROPERTY
import io.github.ktlint.core.rule.engine.core.api.firstChildLeafOrSelf
import io.github.ktlint.core.rule.engine.core.api.isWhiteSpace
import io.github.ktlint.core.rule.engine.core.api.lastChildLeafOrSelf
import io.github.ktlint.core.rule.engine.core.api.leavesInClosedRange
import io.github.ktlint.core.rule.engine.core.api.nextSibling
import io.github.ktlint.core.rule.engine.core.api.parent
import io.github.ktlint.core.rule.engine.core.api.prevSibling
import io.github.ktlint.core.rule.engine.core.api.remove
import io.github.ktlint.core.ruleset.standard.StandardRule

/**
 * [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html#functions):
 * Prefer using an expression body for functions with the body consisting of a single expression.
 * ```
 * override fun toString(): String {
 *     return "Hey"
 * }
 * >
 * override fun toString(): String = "Hey"
 * ```
 *
 * [Android Kotlin styleguide](https://developer.android.com/kotlin/style-guide#expression_functions):
 *
 * When a function contains only a single expression it can be represented as an [expression function]
 * ```
 * override fun toString(): String {
 *     return "Hey"
 * }
 * >
 * override fun toString(): String {
 *     return "Hey"
 * }
 * ```
 */
@SinceKtlint("1.0", EXPERIMENTAL)
@SinceKtlint("1.3", STABLE)
public class FunctionExpressionBodyRule :
    StandardRule(
        id = "function-expression-body",
        usesEditorConfigProperties =
            setOf(
                CODE_STYLE_PROPERTY,
                INDENT_SIZE_PROPERTY,
                INDENT_STYLE_PROPERTY,
            ),
    ) {
    private var codeStyle = CODE_STYLE_PROPERTY.defaultValue
    private var indentConfig = IndentConfig.DEFAULT_INDENT_CONFIG

    override fun beforeFirstNode(editorConfig: EditorConfig) {
        codeStyle = editorConfig[CODE_STYLE_PROPERTY]
        indentConfig =
            IndentConfig(
                indentStyle = editorConfig[INDENT_STYLE_PROPERTY],
                tabWidth = editorConfig[INDENT_SIZE_PROPERTY],
            )
        if (indentConfig.disabled) {
            stopTraversalOfAST()
        }
    }

    override fun beforeVisitChildNodes(node: ASTNode) {
        node
            .takeIf { it.elementType == BLOCK && it.parent?.elementType == FUN }
            ?.let { node.visitFunctionBody() }
    }

    private fun ASTNode.visitFunctionBody() {
        require(elementType == BLOCK)
        takeIf { it.containingOnly(RETURN) }
            ?.takeUnless { it.countReturnKeywords() > 1 }
            ?.findChildByType(RETURN)
            ?.findChildByType(RETURN_KEYWORD)
            ?.nextSibling { !it.isWhiteSpace }
            ?.let { codeSibling ->
                emit(startOffset, "Function body should be replaced with body expression") {
                    parent!!.let { parent ->
                        // Insert the code sibling before the block
                        parent.addChild(LeafPsiElement(EQ, "="), this)
                        parent.addChild(PsiWhiteSpaceImpl(" "), this)
                        parent.addChild(codeSibling, this)
                    }
                    remove()
                }
            }
        takeIf { it.containingOnly(THROW) }
            ?.findChildByType(THROW)
            ?.let { throwNode ->
                emit(startOffset, "Function body should be replaced with body expression") {
                    parent!!.let { parent ->
                        // Remove whitespace before block
                        prevSibling
                            .takeIf { it.isWhiteSpace }
                            ?.remove()
                        if (parent.findChildByType(TYPE_REFERENCE) == null) {
                            // Insert Unit as return type as otherwise a compilation error results
                            parent.addChild(LeafPsiElement(COLON, ":"), this)
                            parent.addChild(PsiWhiteSpaceImpl(" "), this)
                            parent.addChild(createUnitTypeReference(), this)
                        }
                        parent.addChild(PsiWhiteSpaceImpl(" "), this)
                        parent.addChild(LeafPsiElement(EQ, "="), this)
                        parent.addChild(PsiWhiteSpaceImpl(" "), this)
                        parent.addChild(throwNode, this)
                    }
                    remove()
                }
            }
    }

    private fun ASTNode.containingOnly(iElementType: IElementType) =
        iElementType ==
            children
                .filterNot { it.elementType == LBRACE || it.elementType == RBRACE || it.isWhiteSpace }
                .singleOrNull()
                ?.elementType

    private fun ASTNode.countReturnKeywords() =
        leavesInClosedRange(firstChildLeafOrSelf, lastChildLeafOrSelf)
            .count { it.elementType == RETURN_KEYWORD }

    private fun createUnitTypeReference() =
        KtlintKotlinCompiler
            .createASTNodeFromText("fun foo(): Unit {}")
            ?.findChildByType(FUN)
            ?.findChildByType(TYPE_REFERENCE)
            ?: error("Can not create function with unit type reference")
}

public val FUNCTION_EXPRESSION_BODY_RULE_ID: RuleId = FunctionExpressionBodyRule().ruleId
