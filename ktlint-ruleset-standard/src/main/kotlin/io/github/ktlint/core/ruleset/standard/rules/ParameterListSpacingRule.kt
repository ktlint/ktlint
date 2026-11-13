package io.github.ktlint.core.ruleset.standard.rules

import com.intellij.lang.ASTNode
import io.github.ktlint.core.rule.engine.core.api.ElementType.ANNOTATION_ENTRY
import io.github.ktlint.core.rule.engine.core.api.ElementType.COLON
import io.github.ktlint.core.rule.engine.core.api.ElementType.COMMA
import io.github.ktlint.core.rule.engine.core.api.ElementType.MODIFIER_LIST
import io.github.ktlint.core.rule.engine.core.api.ElementType.RPAR
import io.github.ktlint.core.rule.engine.core.api.ElementType.TYPE_REFERENCE
import io.github.ktlint.core.rule.engine.core.api.ElementType.VALUE_PARAMETER
import io.github.ktlint.core.rule.engine.core.api.ElementType.VALUE_PARAMETER_LIST
import io.github.ktlint.core.rule.engine.core.api.ElementType.WHITE_SPACE
import io.github.ktlint.core.rule.engine.core.api.RuleId
import io.github.ktlint.core.rule.engine.core.api.SinceKtlint
import io.github.ktlint.core.rule.engine.core.api.SinceKtlint.Status.EXPERIMENTAL
import io.github.ktlint.core.rule.engine.core.api.SinceKtlint.Status.STABLE
import io.github.ktlint.core.rule.engine.core.api.children
import io.github.ktlint.core.rule.engine.core.api.dropTrailingEolComment
import io.github.ktlint.core.rule.engine.core.api.editorconfig.EditorConfig
import io.github.ktlint.core.rule.engine.core.api.editorconfig.MAX_LINE_LENGTH_PROPERTY
import io.github.ktlint.core.rule.engine.core.api.isPartOfComment
import io.github.ktlint.core.rule.engine.core.api.isWhiteSpace
import io.github.ktlint.core.rule.engine.core.api.isWhiteSpaceWithNewline
import io.github.ktlint.core.rule.engine.core.api.leavesOnLine
import io.github.ktlint.core.rule.engine.core.api.lineLength
import io.github.ktlint.core.rule.engine.core.api.nextCodeSibling
import io.github.ktlint.core.rule.engine.core.api.nextLeaf
import io.github.ktlint.core.rule.engine.core.api.nextSibling
import io.github.ktlint.core.rule.engine.core.api.parent
import io.github.ktlint.core.rule.engine.core.api.prevCodeSibling
import io.github.ktlint.core.rule.engine.core.api.prevLeaf
import io.github.ktlint.core.rule.engine.core.api.remove
import io.github.ktlint.core.rule.engine.core.api.replaceTextWith
import io.github.ktlint.core.rule.engine.core.api.upsertWhitespaceAfterMe
import io.github.ktlint.core.ruleset.standard.StandardRule
import org.jetbrains.kotlin.psi.psiUtil.siblings

/**
 * Ensures consistent spacing inside the parameter list. This rule partly overlaps with other rules like spacing around
 * commas and colons. However, it does have a more complete view on the higher concept of the parameter-list without
 * interfering of the parameter-list-wrapping rule.
 */
@SinceKtlint("0.46", EXPERIMENTAL)
@SinceKtlint("1.0", STABLE)
public class ParameterListSpacingRule :
    StandardRule(
        id = "parameter-list-spacing",
        usesEditorConfigProperties =
            setOf(MAX_LINE_LENGTH_PROPERTY),
    ) {
    private var maxLineLength = MAX_LINE_LENGTH_PROPERTY.defaultValue

    override fun beforeFirstNode(editorConfig: EditorConfig) {
        maxLineLength = editorConfig.maxLineLength()
    }

    override fun beforeVisitChildNodes(node: ASTNode) {
        if (node.elementType == VALUE_PARAMETER_LIST) {
            node.visitValueParameterList()
        }
    }

    private fun ASTNode.visitValueParameterList() {
        require(elementType == VALUE_PARAMETER_LIST)
        val iterator =
            children
                // Store elements in list before changing them as otherwise only one element is being changed
                .toList()
                .iterator()
        while (iterator.hasNext()) {
            val el = iterator.next()
            when (el.elementType) {
                WHITE_SPACE -> el.visitWhiteSpaceInValueParameterList()
                COMMA -> el.visitCommaInValueParameterList()
                VALUE_PARAMETER -> el.visitValueParameter()
            }
        }
    }

    private fun ASTNode.visitWhiteSpaceInValueParameterList() {
        if (parent!!.findChildByType(VALUE_PARAMETER) == null && parent!!.containsNoComments()) {
            // Remove whitespace from parameter list not containing a value parameter or comment
            //     fun foo( )
            removeUnexpectedWhiteSpace()
        } else if ((isBeforeFirstOrAfterLastValueParameter()) && isNotIndent()) {
            if (parent!!.containsNoComments()) {
                removeUnexpectedWhiteSpace()
            } else {
                // Avoid conflict with comment spacing rule
            }
        } else if (nextCodeSibling?.elementType == COMMA) {
            // No whitespace between parameter name and comma allowed
            removeUnexpectedWhiteSpace()
        } else if (isWhiteSpace && isNotIndent() && isNotSingleSpace()) {
            require(prevCodeSibling?.elementType == COMMA)
            replaceWithSingleSpace()
        }
    }

    private fun ASTNode.containsNoComments() = children.none { it.isPartOfComment }

    private fun ASTNode.isBeforeFirstOrAfterLastValueParameter(): Boolean = isBeforeFirstValueParameter() || isAfterLastValueParameter()

    private fun ASTNode.isBeforeFirstValueParameter(): Boolean = siblings(forward = false).none { it.elementType == VALUE_PARAMETER }

    private fun ASTNode.isAfterLastValueParameter(): Boolean = siblings().none { it.elementType == VALUE_PARAMETER }

    private fun ASTNode.visitCommaInValueParameterList() {
        // Comma, except when it is the trailing comma, must be followed by whitespace
        nextLeaf
            ?.takeUnless { it.isWhiteSpace || it.elementType == RPAR }
            ?.let { addMissingWhiteSpaceAfterMe() }
    }

    private fun ASTNode.visitValueParameter() {
        visitModifierList()
        removeWhiteSpaceBetweenParameterIdentifierAndColon()
        fixWhiteSpaceAfterColonInParameter()
    }

    private fun ASTNode.visitModifierList() {
        val modifierList = findChildByType(MODIFIER_LIST) ?: return
        modifierList.removeWhiteSpaceBetweenModifiersInList()
        modifierList.removeWhiteSpaceBetweenModifierListAndParameterIdentifier()
    }

    private fun ASTNode.removeWhiteSpaceBetweenModifiersInList() {
        require(elementType == MODIFIER_LIST)
        children
            .filter { it.isWhiteSpace }
            // Store elements in the list before changing them as otherwise only the first whitespace is being changed
            .toList()
            .forEach { it.visitWhiteSpaceAfterModifier() }
    }

    private fun ASTNode.visitWhiteSpaceAfterModifier() {
        if (isIndent() && getPrecedingModifier()?.elementType == ANNOTATION_ENTRY) {
            // Ignore indent when the modifier is an annotation which is placed on a separate line
            return
        }
        if (isNotSingleSpace()) {
            replaceWithSingleSpace()
        }
    }

    private fun ASTNode.removeWhiteSpaceBetweenModifierListAndParameterIdentifier() {
        require(elementType == MODIFIER_LIST)
        nextSibling
            ?.takeIf { it.isWhiteSpace }
            ?.visitWhiteSpaceAfterModifier()
    }

    private fun ASTNode.removeWhiteSpaceBetweenParameterIdentifierAndColon() {
        findChildByType(COLON)
            ?.prevLeaf
            ?.takeIf { it.isWhiteSpace }
            ?.removeUnexpectedWhiteSpace()
    }

    private fun ASTNode.fixWhiteSpaceAfterColonInParameter() {
        val colonNode = findChildByType(COLON) ?: return
        colonNode
            .nextLeaf
            ?.takeIf { it.isWhiteSpace }
            .let { whiteSpaceAfterColon ->
                if (whiteSpaceAfterColon == null) {
                    colonNode.addMissingWhiteSpaceAfterMe()
                } else {
                    if (isTypeReferenceWithModifierList() && whiteSpaceAfterColon.isIndent()) {
                        // Allow the type to be wrapped to the next line when it has a modifier:
                        //   data class Foo(
                        //       val bar:
                        //           @FooBar("foobar")
                        //           Bar,
                        //   )
                    } else if (whiteSpaceAfterColon.hasTypeReferenceWhichDoesNotFitOnSameLineAsColon()) {
                        // Allow the type to be wrapped to the next line when the type does not fit on same line as colon:
                        //   class Foo(
                        //       val someReallyLongFieldNameUsedInMyClass:
                        //           SomeReallyLongDependencyClass
                        //   )
                    } else if (whiteSpaceAfterColon.isNotSingleSpace()) {
                        whiteSpaceAfterColon.replaceWithSingleSpace()
                    }
                }
            }
    }

    private fun ASTNode.addMissingWhiteSpaceAfterMe() {
        require(elementType == COLON || elementType == COMMA)
        emit(startOffset, "Whitespace after '$text' is missing") { upsertWhitespaceAfterMe(" ") }
    }

    private fun ASTNode.isNotIndent(): Boolean = !isIndent()

    private fun ASTNode.isIndent(): Boolean = isWhiteSpaceWithNewline

    private fun ASTNode.isNotSingleSpace(): Boolean {
        require(isWhiteSpace)
        return text != " "
    }

    private fun ASTNode.removeUnexpectedWhiteSpace() {
        emit(startOffset, "Unexpected whitespace") { remove() }
    }

    private fun ASTNode.replaceWithSingleSpace() {
        emit(startOffset, "Expected a single space") { replaceTextWith(" ") }
    }

    private fun ASTNode.getPrecedingModifier(): ASTNode? =
        prevCodeSibling
            ?.let { prevCodeSibling ->
                if (prevCodeSibling.elementType == MODIFIER_LIST) {
                    prevCodeSibling.lastChildNode
                } else {
                    require(prevCodeSibling.parent?.elementType == MODIFIER_LIST)
                    prevCodeSibling
                }
            }

    private fun ASTNode?.isTypeReferenceWithModifierList() =
        null !=
            this
                ?.findChildByType(TYPE_REFERENCE)
                ?.findChildByType(MODIFIER_LIST)

    private fun ASTNode.hasTypeReferenceWhichDoesNotFitOnSameLineAsColon() =
        takeIf { it.isWhiteSpaceWithNewline }
            ?.nextCodeSibling
            ?.takeIf { it.elementType == TYPE_REFERENCE }
            ?.let { typeReference ->
                val length =
                    // length of the previous line
                    leavesOnLine
                        .dropTrailingEolComment()
                        .lineLength +
                        // single space before type reference
                        1 -
                        // length of current indent before typeReference
                        text.substringAfterLast("\n").length +
                        // length of line containing typeReference
                        typeReference
                            .leavesOnLine
                            .dropTrailingEolComment()
                            .lineLength
                length > maxLineLength
            }
            ?: false
}

public val PARAMETER_LIST_SPACING_RULE_ID: RuleId = ParameterListSpacingRule().ruleId
