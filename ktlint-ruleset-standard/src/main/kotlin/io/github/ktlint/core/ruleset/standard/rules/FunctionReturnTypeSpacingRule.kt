package io.github.ktlint.core.ruleset.standard.rules

import com.intellij.lang.ASTNode
import io.github.ktlint.core.rule.engine.core.api.ElementType.COLON
import io.github.ktlint.core.rule.engine.core.api.ElementType.FUN
import io.github.ktlint.core.rule.engine.core.api.RuleId
import io.github.ktlint.core.rule.engine.core.api.SinceKtlint
import io.github.ktlint.core.rule.engine.core.api.SinceKtlint.Status.EXPERIMENTAL
import io.github.ktlint.core.rule.engine.core.api.SinceKtlint.Status.STABLE
import io.github.ktlint.core.rule.engine.core.api.editorconfig.EditorConfig
import io.github.ktlint.core.rule.engine.core.api.editorconfig.MAX_LINE_LENGTH_PROPERTY
import io.github.ktlint.core.rule.engine.core.api.isWhiteSpace
import io.github.ktlint.core.rule.engine.core.api.isWhiteSpaceWithNewline
import io.github.ktlint.core.rule.engine.core.api.nextLeaf
import io.github.ktlint.core.rule.engine.core.api.prevLeaf
import io.github.ktlint.core.rule.engine.core.api.remove
import io.github.ktlint.core.rule.engine.core.api.upsertWhitespaceAfterMe
import io.github.ktlint.core.ruleset.standard.StandardRule
import org.jetbrains.kotlin.psi.psiUtil.leaves

@SinceKtlint("0.46", EXPERIMENTAL)
@SinceKtlint("0.49", STABLE)
public class FunctionReturnTypeSpacingRule :
    StandardRule(
        id = "function-return-type-spacing",
        usesEditorConfigProperties = setOf(MAX_LINE_LENGTH_PROPERTY),
    ) {
    private var maxLineLength = MAX_LINE_LENGTH_PROPERTY.defaultValue

    override fun beforeFirstNode(editorConfig: EditorConfig) {
        maxLineLength = editorConfig.maxLineLength()
    }

    override fun beforeVisitChildNodes(node: ASTNode) {
        node.firstChildNode
        node
            .takeIf { node.elementType == FUN }
            ?.let { node.findChildByType(COLON) }
            ?.let { colonNode ->
                colonNode.removeWhiteSpaceBetweenClosingParenthesisAndColon()
                colonNode.fixWhiteSpaceBetweenColonAndReturnType()
            }
    }

    private fun ASTNode.removeWhiteSpaceBetweenClosingParenthesisAndColon() {
        require(elementType == COLON)
        prevLeaf
            ?.takeIf { it.isWhiteSpace }
            ?.let { whitespaceBeforeColonNode ->
                emit(whitespaceBeforeColonNode.startOffset, "Unexpected whitespace") { whitespaceBeforeColonNode.remove() }
            }
    }

    private fun ASTNode.fixWhiteSpaceBetweenColonAndReturnType() {
        require(elementType == COLON)
        nextLeaf
            ?.takeIf { it.isWhiteSpace }
            .let { whiteSpaceAfterColon ->
                if (whiteSpaceAfterColon?.text != " ") {
                    // In case the whitespace contains a newline than replacing it with a single space results in merging the lines to a
                    // single line. This rule allows this only when the merged lines entirely fit on a single line. Suppose that code below
                    // does not fit on a single line:
                    //    fun foo():
                    //        String = "some-looooooooooooooooong-string"
                    // This rule does *not* attempt to reformat the code as follows:
                    //    fun foo(): String =
                    //        "some-looooooooooooooooong-string"
                    // See FunctionSignatureRule for such reformatting.
                    val newLineLength =
                        lengthUntilNewline(false) + // Length of line before but excluding the colon
                            textLength + // Length of the colon itself
                            1 + // Length of the fixed whitespace
                            whiteSpaceAfterColon.lengthUntilNewline(true) // Length of the line after but excluding the whitespace
                    if (newLineLength <= maxLineLength) {
                        emit(startOffset, "Single space expected between colon and return type") {
                            upsertWhitespaceAfterMe(" ")
                        }
                    }
                }
            }
    }

    private fun ASTNode?.lengthUntilNewline(forward: Boolean) =
        if (this == null) {
            0
        } else {
            leaves(forward = forward)
                .takeWhile { !it.isWhiteSpaceWithNewline }
                .sumOf { it.textLength }
        }
}

public val FUNCTION_RETURN_TYPE_SPACING_RULE_ID: RuleId = FunctionReturnTypeSpacingRule().ruleId
