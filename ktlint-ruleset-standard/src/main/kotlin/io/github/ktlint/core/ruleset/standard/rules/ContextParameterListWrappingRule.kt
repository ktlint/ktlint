package io.github.ktlint.core.ruleset.standard.rules

import com.intellij.lang.ASTNode
import io.github.ktlint.core.rule.engine.core.api.ElementType.CONTEXT_PARAMETER_LIST
import io.github.ktlint.core.rule.engine.core.api.ElementType.CONTEXT_RECEIVER
import io.github.ktlint.core.rule.engine.core.api.ElementType.FUN
import io.github.ktlint.core.rule.engine.core.api.ElementType.FUNCTION_TYPE
import io.github.ktlint.core.rule.engine.core.api.ElementType.GT
import io.github.ktlint.core.rule.engine.core.api.ElementType.RPAR
import io.github.ktlint.core.rule.engine.core.api.ElementType.TYPE_ARGUMENT_LIST
import io.github.ktlint.core.rule.engine.core.api.ElementType.TYPE_PROJECTION
import io.github.ktlint.core.rule.engine.core.api.ElementType.TYPE_REFERENCE
import io.github.ktlint.core.rule.engine.core.api.ElementType.VALUE_PARAMETER
import io.github.ktlint.core.rule.engine.core.api.ElementType.VALUE_PARAMETER_LIST
import io.github.ktlint.core.rule.engine.core.api.IndentConfig
import io.github.ktlint.core.rule.engine.core.api.IndentConfig.Companion.DEFAULT_INDENT_CONFIG
import io.github.ktlint.core.rule.engine.core.api.RuleId
import io.github.ktlint.core.rule.engine.core.api.SinceKtlint
import io.github.ktlint.core.rule.engine.core.api.SinceKtlint.Status.STABLE
import io.github.ktlint.core.rule.engine.core.api.children
import io.github.ktlint.core.rule.engine.core.api.editorconfig.EditorConfig
import io.github.ktlint.core.rule.engine.core.api.editorconfig.INDENT_SIZE_PROPERTY
import io.github.ktlint.core.rule.engine.core.api.editorconfig.INDENT_STYLE_PROPERTY
import io.github.ktlint.core.rule.engine.core.api.editorconfig.MAX_LINE_LENGTH_PROPERTY
import io.github.ktlint.core.rule.engine.core.api.firstChildLeafOrSelf
import io.github.ktlint.core.rule.engine.core.api.hasNoMaxLineLengthSuppression
import io.github.ktlint.core.rule.engine.core.api.indent
import io.github.ktlint.core.rule.engine.core.api.indentWithoutNewlinePrefix
import io.github.ktlint.core.rule.engine.core.api.isPartOf
import io.github.ktlint.core.rule.engine.core.api.isPartOfComment
import io.github.ktlint.core.rule.engine.core.api.isWhiteSpaceWithNewline
import io.github.ktlint.core.rule.engine.core.api.isWhiteSpaceWithoutNewline
import io.github.ktlint.core.rule.engine.core.api.lastChildLeafOrSelf
import io.github.ktlint.core.rule.engine.core.api.nextLeaf
import io.github.ktlint.core.rule.engine.core.api.parent
import io.github.ktlint.core.rule.engine.core.api.prevLeaf
import io.github.ktlint.core.rule.engine.core.api.upsertWhitespaceAfterMe
import io.github.ktlint.core.rule.engine.core.api.upsertWhitespaceBeforeMe
import io.github.ktlint.core.ruleset.standard.StandardRule

/**
 * Wrapping of context receiver list to a separate line.
 *
 * IMPORTANT: This rule only affects a context receiver list that does not contain a context receiver. Context receivers are deprecated
 * since Kotlin 2.2.0, and are wrapped by the 'context-receiver-wrapping' rule.
 *
 * Note: In Ktlint 2.x this rule has been renamed from ContextReceiverListWrappingRule to ContextParameterListWrappingRule
 */
@SinceKtlint("1.7", STABLE)
public class ContextParameterListWrappingRule :
    StandardRule(
        id = "context-parameter-list-wrapping",
        usesEditorConfigProperties =
            setOf(
                INDENT_SIZE_PROPERTY,
                INDENT_STYLE_PROPERTY,
                MAX_LINE_LENGTH_PROPERTY,
            ),
    ) {
    private var indentConfig = DEFAULT_INDENT_CONFIG
    private var maxLineLength = MAX_LINE_LENGTH_PROPERTY.defaultValue

    override fun beforeFirstNode(editorConfig: EditorConfig) {
        indentConfig =
            IndentConfig(
                indentStyle = editorConfig[INDENT_STYLE_PROPERTY],
                tabWidth = editorConfig[INDENT_SIZE_PROPERTY],
            )
        maxLineLength = editorConfig.maxLineLength()
    }

    override fun beforeVisitChildNodes(node: ASTNode) {
        when (node.elementType) {
            CONTEXT_PARAMETER_LIST if node.isContextParameter() -> {
                node.visitContextReceiverList()
            }

            TYPE_ARGUMENT_LIST if node.isContextParameter() -> {
                node.visitContextReceiverTypeArgumentList()
            }
        }
    }

    private fun ASTNode.isContextParameter(): Boolean = isPartOf(CONTEXT_PARAMETER_LIST) && findChildByType(CONTEXT_RECEIVER) == null

    private fun ASTNode.visitContextReceiverList() {
        // Context receiver must be followed by new line or comment unless it is a type reference of a parameter
        takeUnless { it.isTypeReferenceParameterInFunction() }
            ?.lastChildLeafOrSelf
            ?.nextLeaf { !it.isWhiteSpaceWithoutNewline && !it.isPartOfComment }
            ?.takeIf { !it.isWhiteSpaceWithNewline }
            ?.let { nodeAfterContextReceiver ->
                emit(nodeAfterContextReceiver.startOffset, "Expected a newline after the context parameter") {
                    nodeAfterContextReceiver
                        .firstChildLeafOrSelf
                        .upsertWhitespaceBeforeMe(indentConfig.parentIndentOf(this))
                }
            }

        // Check line length assuming that the context receiver is indented correctly. Wrapping rule must however run before indenting.
        if (!textContains('\n') &&
            hasNoMaxLineLengthSuppression() &&
            indentWithoutNewlinePrefix.length + textLength > maxLineLength
        ) {
            children
                .filter { it.elementType == VALUE_PARAMETER }
                .forEach {
                    emit(it.startOffset, "Newline expected before context parameter as max line length is violated") {
                        it
                            .prevLeaf
                            ?.upsertWhitespaceAfterMe(indentConfig.childIndentOf(this))
                    }
                }
            findChildByType(RPAR)
                ?.let { rpar ->
                    emit(rpar.startOffset, "Newline expected before closing parenthesis as max line length is violated") {
                        rpar.upsertWhitespaceBeforeMe(indent)
                    }
                }
        }
    }

    private fun ASTNode.isTypeReferenceParameterInFunction() =
        takeIf { it.elementType == CONTEXT_PARAMETER_LIST }
            ?.parent
            ?.takeIf { it.elementType == FUNCTION_TYPE }
            ?.parent
            ?.takeIf { it.elementType == TYPE_REFERENCE }
            ?.parent
            ?.takeIf { it.elementType == VALUE_PARAMETER }
            ?.parent
            ?.takeIf { it.elementType == VALUE_PARAMETER_LIST }
            ?.parent
            ?.let { it.elementType == FUN }
            ?: false

    private fun ASTNode.visitContextReceiverTypeArgumentList() {
        val contextReceiverText = parent?.text.orEmpty()
        // Check line length assuming that the context receiver is indented correctly. Wrapping rule must however run
        // before indenting.
        if (!contextReceiverText.contains('\n') &&
            hasNoMaxLineLengthSuppression() &&
            indentWithoutNewlinePrefix.length + contextReceiverText.length > maxLineLength
        ) {
            children
                .filter { it.elementType == TYPE_PROJECTION }
                .forEach {
                    emit(it.startOffset, "Newline expected before context parameter type projection as max line length is violated") {
                        it.upsertWhitespaceBeforeMe(indentConfig.childIndentOf(this))
                    }
                }
            findChildByType(GT)
                ?.let { gt ->
                    emit(gt.startOffset, "Newline expected before closing angle bracket as max line length is violated") {
                        // Ideally, the closing angle bracket should be de-indented to make it consistent with
                        // de-indentation of closing ")", "}" and "]". This however would be inconsistent with how the
                        // type argument lists are formatted by IntelliJ IDEA default formatter.
                        gt.upsertWhitespaceBeforeMe(indentConfig.childIndentOf(this))
                    }
                }
        }
    }
}

public val CONTEXT_PARAMETER_LIST_WRAPPING_RULE_ID: RuleId = ContextParameterListWrappingRule().ruleId
