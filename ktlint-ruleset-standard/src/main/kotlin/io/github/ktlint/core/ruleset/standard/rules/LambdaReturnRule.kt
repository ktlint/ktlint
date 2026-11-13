package io.github.ktlint.core.ruleset.standard.rules

import com.intellij.lang.ASTNode
import io.github.ktlint.core.rule.engine.core.api.ElementType.BLOCK
import io.github.ktlint.core.rule.engine.core.api.ElementType.CALL_EXPRESSION
import io.github.ktlint.core.rule.engine.core.api.ElementType.FUNCTION_LITERAL
import io.github.ktlint.core.rule.engine.core.api.ElementType.IDENTIFIER
import io.github.ktlint.core.rule.engine.core.api.ElementType.LABEL
import io.github.ktlint.core.rule.engine.core.api.ElementType.LABELED_EXPRESSION
import io.github.ktlint.core.rule.engine.core.api.ElementType.LABEL_QUALIFIER
import io.github.ktlint.core.rule.engine.core.api.ElementType.LAMBDA_ARGUMENT
import io.github.ktlint.core.rule.engine.core.api.ElementType.LAMBDA_EXPRESSION
import io.github.ktlint.core.rule.engine.core.api.ElementType.REFERENCE_EXPRESSION
import io.github.ktlint.core.rule.engine.core.api.ElementType.RETURN
import io.github.ktlint.core.rule.engine.core.api.IndentConfig
import io.github.ktlint.core.rule.engine.core.api.RuleId
import io.github.ktlint.core.rule.engine.core.api.RuleV2
import io.github.ktlint.core.rule.engine.core.api.SinceKtlint
import io.github.ktlint.core.rule.engine.core.api.SinceKtlint.Status.EXPERIMENTAL
import io.github.ktlint.core.rule.engine.core.api.editorconfig.CODE_STYLE_PROPERTY
import io.github.ktlint.core.rule.engine.core.api.editorconfig.EditorConfig
import io.github.ktlint.core.rule.engine.core.api.editorconfig.INDENT_SIZE_PROPERTY
import io.github.ktlint.core.rule.engine.core.api.editorconfig.INDENT_STYLE_PROPERTY
import io.github.ktlint.core.rule.engine.core.api.editorconfig.MAX_LINE_LENGTH_PROPERTY
import io.github.ktlint.core.rule.engine.core.api.isWhiteSpace
import io.github.ktlint.core.rule.engine.core.api.nextCodeSibling
import io.github.ktlint.core.rule.engine.core.api.parent
import io.github.ktlint.core.rule.engine.core.api.prevCodeSibling
import io.github.ktlint.core.rule.engine.core.api.remove
import io.github.ktlint.core.ruleset.standard.StandardRule
import org.jetbrains.kotlin.psi.psiUtil.children
import org.jetbrains.kotlin.psi.psiUtil.siblings

/**
 * [Kotlin lang documentation](https://kotlinlang.org/docs/coding-conventions.html#returns-in-a-lambda):
 *
 * Do not use a labeled return for the last statement in a lambda.
 * ```
 * foo {
 *     doSomething()
 *     return@foo "value"
 * }
 * ```
 */
@SinceKtlint("2.0", EXPERIMENTAL)
public class LambdaReturnRule :
    StandardRule(
        id = "lambda-return",
        usesEditorConfigProperties =
            setOf(
                CODE_STYLE_PROPERTY,
                INDENT_SIZE_PROPERTY,
                INDENT_STYLE_PROPERTY,
                MAX_LINE_LENGTH_PROPERTY,
            ),
    ),
    RuleV2.Experimental {
    private var indentConfig = IndentConfig.DEFAULT_INDENT_CONFIG
    private var maxLineLength = MAX_LINE_LENGTH_PROPERTY.defaultValue

    override fun beforeFirstNode(editorConfig: EditorConfig) {
        maxLineLength = editorConfig.maxLineLength()
        indentConfig =
            IndentConfig(
                indentStyle = editorConfig[INDENT_STYLE_PROPERTY],
                tabWidth = editorConfig[INDENT_SIZE_PROPERTY],
            )
    }

    override fun beforeVisitChildNodes(node: ASTNode) {
        node
            .takeIf { it.elementType == FUNCTION_LITERAL }
            ?.findChildByType(BLOCK)
            ?.children()
            ?.lastOrNull { it.elementType == RETURN }
            ?.takeIf { it.nextCodeSibling == null }
            ?.visitReturnStatement()
    }

    private fun ASTNode.visitReturnStatement() {
        require(elementType == RETURN)

        val labelQualifier = findChildByType(LABEL_QUALIFIER)
        labelQualifier
            ?.siblings()
            ?.dropWhile { it.isWhiteSpace }
            ?.takeIf { it.count() > 0 }
            ?.takeIf { refersToInnerMostLambdaArgument(labelQualifier.qualifierIdentifier()) }
            ?.let { siblings ->
                emit(startOffset, "Unnecessary return") {
                    val parent = parent!!
                    siblings
                        // The remaining siblings are moved to before the RETURN sibling. Without collecting the remaining siblings
                        // first into a list, the RETURN sibling itself would also be tried to be moved which results in a failure.
                        .toList()
                        .forEach { sibling -> parent.addChild(sibling, this) }
                    remove()
                }
            }
    }

    private fun ASTNode.refersToInnerMostLambdaArgument(qualifierIdentifier: String?) =
        qualifierIdentifier == listOfNotNull(labeledLambdaArgumentIdentifier(), unlabeledLambdaArgumentIdentifier()).firstOrNull()

    private fun ASTNode.labeledLambdaArgumentIdentifier() =
        // For lambda argument having an explicit label, extract the label (e.g. "someLabel") for code like:
        // .    foo.let someLabel@{ ... }
        parent { it.elementType == LAMBDA_EXPRESSION }
            ?.takeIf { it.treeParent.elementType == LABELED_EXPRESSION }
            ?.prevCodeSibling
            ?.qualifierIdentifier()

    private fun ASTNode.unlabeledLambdaArgumentIdentifier() =
        // For lambda argument not having an explicit label, extract the identifier (e.g. "let") for code like:
        // .    foo.let { ... }
        parent { it.elementType == LAMBDA_EXPRESSION }
            ?.takeIf { it.treeParent.elementType != LABELED_EXPRESSION }
            ?.parent { it.elementType == LAMBDA_ARGUMENT }
            ?.parent { it.elementType == CALL_EXPRESSION }
            ?.findChildByType(REFERENCE_EXPRESSION)
            ?.findChildByType(IDENTIFIER)
            ?.text

    private fun ASTNode?.qualifierIdentifier(): String? =
        this
            ?.takeIf { it.elementType == LABEL_QUALIFIER }
            ?.findChildByType(LABEL)
            ?.findChildByType(IDENTIFIER)
            ?.text
}

public val LAMBDA_RETURN_RULE_ID: RuleId = LambdaReturnRule().ruleId
