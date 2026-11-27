package io.github.ktlint.core.ruleset.standard.rules

import com.intellij.lang.ASTNode
import io.github.ktlint.core.rule.engine.core.api.ElementType.CONTEXT_PARAMETER_LIST
import io.github.ktlint.core.rule.engine.core.api.ElementType.CONTEXT_RECEIVER
import io.github.ktlint.core.rule.engine.core.api.ElementType.FUNCTION_TYPE
import io.github.ktlint.core.rule.engine.core.api.ElementType.GT
import io.github.ktlint.core.rule.engine.core.api.ElementType.PROPERTY
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
import io.github.ktlint.core.rule.engine.core.api.SinceKtlint.Status.EXPERIMENTAL
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
 * Wrapping of context receiver to a separate line. Arguments of the context receiver are wrapped to separate line
 * whenever the max line length is exceeded.
 *
 * In Kotlin 2.1.21 the context parameters have been introduced as a replacement for context receiver. Like the context receiver, the
 * context parameters are wrapped inside a context receiver list. This rule will be removed once context receivers are no longer supported
 * by the Kotlin compiler. A new rule (ContextReceiverListWrapping) will take care of wrapping the context parameters.
 */
@SinceKtlint("0.48", EXPERIMENTAL)
@SinceKtlint("1.0", STABLE)
public class ContextReceiverWrappingRule :
    StandardRule(
        id = "context-receiver-wrapping",
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
            CONTEXT_PARAMETER_LIST if node.findChildByType(CONTEXT_RECEIVER) != null -> {
                node.visitContextReceiverList()
            }

            TYPE_ARGUMENT_LIST if node.isPartOf(CONTEXT_RECEIVER) -> {
                node.visitContextReceiverTypeArgumentList()
            }
        }
    }

    private fun ASTNode.visitContextReceiverList() {
        // Context receiver must be followed by new line or comment unless it is a type reference of a parameter or property
        takeUnless { it.isFunctionTypeReferenceForParameterOrProperty() }
            ?.lastChildLeafOrSelf
            ?.nextLeaf { !it.isWhiteSpaceWithoutNewline && !it.isPartOfComment }
            ?.takeIf { !it.isWhiteSpaceWithNewline }
            ?.let { nodeAfterContextReceiver ->
                emit(nodeAfterContextReceiver.startOffset, "Expected a newline after the context receiver") {
                    nodeAfterContextReceiver
                        .firstChildLeafOrSelf
                        .upsertWhitespaceBeforeMe(indentConfig.parentIndentOf(this))
                }
            }

        // Check line length assuming that the context receiver is indented correctly. Wrapping rule must however run before indenting.
        if (!textContains('\n') && hasNoMaxLineLengthSuppression() && indentWithoutNewlinePrefix.length + textLength > maxLineLength) {
            children
                .filter { it.elementType == CONTEXT_RECEIVER }
                .forEach {
                    emit(it.startOffset, "Newline expected before context receiver as max line length is violated") {
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

    private fun ASTNode.isFunctionTypeReferenceForParameterOrProperty(): Boolean =
        takeIf { it.elementType == CONTEXT_PARAMETER_LIST }
            ?.parent
            ?.takeIf { it.elementType == FUNCTION_TYPE }
            ?.parent
            ?.takeIf { it.elementType == TYPE_REFERENCE }
            ?.parent
            ?.let { it.isValueParameter() || it.isFunctionTypeReferenceForProperty() }
            ?: false

    private fun ASTNode.isValueParameter() =
        takeIf { it.elementType == VALUE_PARAMETER }
            ?.parent
            ?.let { it.elementType == VALUE_PARAMETER_LIST }
            ?: false

    private fun ASTNode.isFunctionTypeReferenceForProperty() = elementType == PROPERTY

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
                    emit(it.startOffset, "Newline expected before context receiver type projection as max line length is violated") {
                        it.upsertWhitespaceBeforeMe(indentConfig.childIndentOf(this))
                    }
                }
            findChildByType(GT)
                ?.let { gt ->
                    emit(gt.startOffset, "Newline expected before closing angle bracket as max line length is violated") {
                        // Ideally, the closing angle bracket should be de-indented to make it consistent with de-indentation of closing
                        // ")", "}" and "]". This however would be inconsistent with how the type argument lists are formatted by IntelliJ
                        // IDEA default formatter.
                        gt.upsertWhitespaceBeforeMe(indentConfig.childIndentOf(this))
                    }
                }
        }
    }
}

public val CONTEXT_RECEIVER_WRAPPING_RULE_ID: RuleId = ContextReceiverWrappingRule().ruleId
