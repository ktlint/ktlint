package io.github.ktlint.core.ruleset.standard.rules

import com.intellij.lang.ASTNode
import io.github.ktlint.core.rule.engine.core.api.ElementType.ANNOTATION
import io.github.ktlint.core.rule.engine.core.api.ElementType.ANNOTATION_ENTRY
import io.github.ktlint.core.rule.engine.core.api.ElementType.CLASS
import io.github.ktlint.core.rule.engine.core.api.ElementType.CLASS_BODY
import io.github.ktlint.core.rule.engine.core.api.ElementType.COLON
import io.github.ktlint.core.rule.engine.core.api.ElementType.COMMA
import io.github.ktlint.core.rule.engine.core.api.ElementType.CONSTRUCTOR_DELEGATION_CALL
import io.github.ktlint.core.rule.engine.core.api.ElementType.CONSTRUCTOR_DELEGATION_REFERENCE
import io.github.ktlint.core.rule.engine.core.api.ElementType.CONSTRUCTOR_KEYWORD
import io.github.ktlint.core.rule.engine.core.api.ElementType.EOL_COMMENT
import io.github.ktlint.core.rule.engine.core.api.ElementType.EXPECT_KEYWORD
import io.github.ktlint.core.rule.engine.core.api.ElementType.MODIFIER_LIST
import io.github.ktlint.core.rule.engine.core.api.ElementType.PRIMARY_CONSTRUCTOR
import io.github.ktlint.core.rule.engine.core.api.ElementType.RPAR
import io.github.ktlint.core.rule.engine.core.api.ElementType.SECONDARY_CONSTRUCTOR
import io.github.ktlint.core.rule.engine.core.api.ElementType.SUPER_TYPE_CALL_ENTRY
import io.github.ktlint.core.rule.engine.core.api.ElementType.SUPER_TYPE_LIST
import io.github.ktlint.core.rule.engine.core.api.ElementType.VALUE_PARAMETER
import io.github.ktlint.core.rule.engine.core.api.ElementType.VALUE_PARAMETER_LIST
import io.github.ktlint.core.rule.engine.core.api.ElementType.WHITE_SPACE
import io.github.ktlint.core.rule.engine.core.api.IndentConfig
import io.github.ktlint.core.rule.engine.core.api.IndentConfig.Companion.DEFAULT_INDENT_CONFIG
import io.github.ktlint.core.rule.engine.core.api.RuleId
import io.github.ktlint.core.rule.engine.core.api.SinceKtlint
import io.github.ktlint.core.rule.engine.core.api.SinceKtlint.Status.EXPERIMENTAL
import io.github.ktlint.core.rule.engine.core.api.SinceKtlint.Status.STABLE
import io.github.ktlint.core.rule.engine.core.api.children
import io.github.ktlint.core.rule.engine.core.api.editorconfig.CODE_STYLE_PROPERTY
import io.github.ktlint.core.rule.engine.core.api.editorconfig.CodeStyleValue.KTLINT_OFFICIAL
import io.github.ktlint.core.rule.engine.core.api.editorconfig.EditorConfig
import io.github.ktlint.core.rule.engine.core.api.editorconfig.EditorConfigProperty
import io.github.ktlint.core.rule.engine.core.api.editorconfig.INDENT_SIZE_PROPERTY
import io.github.ktlint.core.rule.engine.core.api.editorconfig.INDENT_STYLE_PROPERTY
import io.github.ktlint.core.rule.engine.core.api.editorconfig.MAX_LINE_LENGTH_PROPERTY
import io.github.ktlint.core.rule.engine.core.api.editorconfig.MAX_LINE_LENGTH_PROPERTY_OFF
import io.github.ktlint.core.rule.engine.core.api.hasModifier
import io.github.ktlint.core.rule.engine.core.api.hasNoMaxLineLengthSuppression
import io.github.ktlint.core.rule.engine.core.api.indent
import io.github.ktlint.core.rule.engine.core.api.indentWithoutNewlinePrefix
import io.github.ktlint.core.rule.engine.core.api.isCode
import io.github.ktlint.core.rule.engine.core.api.isLeaf
import io.github.ktlint.core.rule.engine.core.api.isPartOfComment
import io.github.ktlint.core.rule.engine.core.api.isWhiteSpace
import io.github.ktlint.core.rule.engine.core.api.isWhiteSpaceWithNewline
import io.github.ktlint.core.rule.engine.core.api.isWhiteSpaceWithoutNewlineOrNull
import io.github.ktlint.core.rule.engine.core.api.nextCodeLeaf
import io.github.ktlint.core.rule.engine.core.api.nextCodeSibling
import io.github.ktlint.core.rule.engine.core.api.nextLeaf
import io.github.ktlint.core.rule.engine.core.api.parent
import io.github.ktlint.core.rule.engine.core.api.prevCodeSibling
import io.github.ktlint.core.rule.engine.core.api.prevLeaf
import io.github.ktlint.core.rule.engine.core.api.prevSibling
import io.github.ktlint.core.rule.engine.core.api.remove
import io.github.ktlint.core.rule.engine.core.api.upsertWhitespaceAfterMe
import io.github.ktlint.core.rule.engine.core.api.upsertWhitespaceBeforeMe
import io.github.ktlint.core.ruleset.standard.StandardRule
import org.ec4j.core.model.PropertyType

/**
 * Formats the class signature according to https://kotlinlang.org/docs/coding-conventions.html#class-headers
 *
 * In code style `ktlint_official` class headers containing 2 or more parameters are formatted as multiline signature. As the Kotlin Coding
 * conventions do not specify what is meant with a "few parameters", no default is set for other code styles.
 */
@SinceKtlint("1.0", EXPERIMENTAL)
@SinceKtlint("1.3", STABLE)
public class ClassSignatureRule :
    StandardRule(
        id = "class-signature",
        usesEditorConfigProperties =
            setOf(
                INDENT_SIZE_PROPERTY,
                INDENT_STYLE_PROPERTY,
                MAX_LINE_LENGTH_PROPERTY,
                FORCE_MULTILINE_WHEN_PARAMETER_COUNT_GREATER_OR_EQUAL_THAN_PROPERTY,
            ),
    ) {
    private var codeStyle = CODE_STYLE_PROPERTY.defaultValue
    private var indentConfig = DEFAULT_INDENT_CONFIG
    private var maxLineLength = MAX_LINE_LENGTH_PROPERTY.defaultValue
    private var classSignatureWrappingMinimumParameters = FORCE_MULTILINE_WHEN_PARAMETER_COUNT_GREATER_OR_EQUAL_THAN_PROPERTY.defaultValue

    override fun beforeFirstNode(editorConfig: EditorConfig) {
        codeStyle = editorConfig[CODE_STYLE_PROPERTY]
        classSignatureWrappingMinimumParameters = editorConfig[FORCE_MULTILINE_WHEN_PARAMETER_COUNT_GREATER_OR_EQUAL_THAN_PROPERTY]
        indentConfig =
            IndentConfig(
                indentStyle = editorConfig[INDENT_STYLE_PROPERTY],
                tabWidth = editorConfig[INDENT_SIZE_PROPERTY],
            )
        maxLineLength = editorConfig.maxLineLength()
    }

    override fun beforeVisitChildNodes(node: ASTNode) {
        if (node.elementType == CLASS) {
            node.visitClass()
        }
    }

    private fun ASTNode.classSignatureNodes(excludeSuperTypes: Boolean): List<ASTNode> {
        // Find the nodes that are to be placed on the same line if no max line length is set
        //     internal class Foo(bar: String) { // Class without super type
        // or
        //     public class Foo(bar: String) : Bar { // Class with exactly one super type
        // or
        //     private class Foo(bar: String) : // Class with multiple super types which have to be wrapped to next lines
        val firstCodeChild = getFirstChildInSignature()
        val lastNodeInPrimaryClassSignatureLine =
            takeIf { excludeSuperTypes }
                // When the class extends multiple super types or if the super type list contains a newline, all super types are wrapped
                // on separate line
                ?.findChildByType(COLON)
                ?: findChildByType(CLASS_BODY)?.firstChildNode
        return collectLeavesRecursively()
            .childrenBetween(
                startASTNodePredicate = { it == firstCodeChild },
                endASTNodePredicate = { it == lastNodeInPrimaryClassSignatureLine },
            )
    }

    private fun ASTNode.superTypes() =
        findChildByType(SUPER_TYPE_LIST)
            ?.children
            ?.filter { it.isCode && it.elementType != COMMA }

    private fun ASTNode.hasMultilineSuperTypeList() = findChildByType(SUPER_TYPE_LIST)?.textContains('\n') == true

    private fun ASTNode.getFirstChildInSignature(): ASTNode? {
        val modifierList = findChildByType(MODIFIER_LIST)
        return if (modifierList == null) {
            nextCodeLeaf
        } else {
            val iterator = modifierList.children.iterator()
            var currentNode: ASTNode
            while (iterator.hasNext()) {
                currentNode = iterator.next()
                if (currentNode.isNotAnnotationOrWhitespaceOrComment()) {
                    return currentNode
                }
            }
            modifierList.nextCodeSibling
        }
    }

    private fun ASTNode.isNotAnnotationOrWhitespaceOrComment() =
        elementType != ANNOTATION &&
            elementType != ANNOTATION_ENTRY &&
            elementType != WHITE_SPACE &&
            elementType != EOL_COMMENT

    private fun ASTNode.visitClass() {
        require(elementType == CLASS)

        val wrapPrimaryConstructorParameters =
            hasTooManyParameters() ||
                containsMultilineParameter() ||
                (codeStyle == KTLINT_OFFICIAL && containsAnnotatedParameter()) ||
                (isMaxLineLengthSet() && hasNoMaxLineLengthSuppression() && classSignatureExcludingSuperTypesExceedsMaxLineLength()) ||
                (!isMaxLineLengthSet() && classSignatureExcludingSuperTypesIsMultiline()) ||
                containsEolComment()
        fixWhiteSpacesInValueParameterList(multiline = wrapPrimaryConstructorParameters, dryRun = false)
        fixWhitespacesInSuperTypeList(wrapPrimaryConstructor = wrapPrimaryConstructorParameters)
        fixClassBody()
    }

    private fun ASTNode.containsEolComment() =
        getPrimaryConstructorParameterListOrNull()
            ?.children
            ?.any { it.elementType == EOL_COMMENT }
            ?: false

    private fun ASTNode.classSignatureExcludingSuperTypesExceedsMaxLineLength(): Boolean {
        val actualClassSignatureLength = getClassSignatureLength(excludeSuperTypes = true)
        // Calculate the length of the class signature in case it, excluding the super types, would be rewritten as single
        // line (and without a maximum line length). The white space correction will be calculated via a dry run of the
        // actual fix.
        val length =
            actualClassSignatureLength +
                // Calculate the white space correction in case the signature would be rewritten to a single line
                fixWhiteSpacesInValueParameterList(multiline = false, dryRun = true)
        return hasNoMaxLineLengthSuppression() && length > maxLineLength
    }

    private fun ASTNode.classSignatureExcludingSuperTypesIsMultiline() =
        classSignatureNodes(excludeSuperTypes = true)
            .any { it.isWhiteSpaceWithNewline }

    private fun ASTNode.getClassSignatureLength(excludeSuperTypes: Boolean) =
        indentWithoutNewlinePrefix.length + getClassSignatureNodesLength(excludeSuperTypes)

    private fun ASTNode.getClassSignatureNodesLength(excludeSuperTypes: Boolean) =
        classSignatureNodes(excludeSuperTypes)
            .joinTextToString()
            .length

    private fun ASTNode.containsMultilineParameter(): Boolean =
        getPrimaryConstructorParameterListOrNull()
            ?.children
            .orEmpty()
            .filter { it.elementType == VALUE_PARAMETER }
            .any { it.textContains('\n') }

    private fun ASTNode.containsAnnotatedParameter(): Boolean =
        getPrimaryConstructorParameterListOrNull()
            ?.children
            .orEmpty()
            .filter { it.elementType == VALUE_PARAMETER }
            .any { it.isAnnotated() }

    private fun ASTNode.isAnnotated() =
        findChildByType(MODIFIER_LIST)
            ?.children
            .orEmpty()
            .any { it.elementType == ANNOTATION_ENTRY }

    private fun ASTNode.fixWhiteSpacesInValueParameterList(
        multiline: Boolean,
        dryRun: Boolean,
    ): Int {
        var whiteSpaceCorrection = 0

        val primaryConstructorParameterList = getPrimaryConstructorParameterListOrNull()
        val hasNoValueParameters =
            primaryConstructorParameterList
                ?.children
                .orEmpty()
                .none { it.elementType == VALUE_PARAMETER }

        whiteSpaceCorrection +=
            if (hasNoValueParameters) {
                fixWhiteSpacesInEmptyValueParameterList(dryRun)
            } else {
                fixWhiteSpacesBeforeFirstParameterInValueParameterList(multiline, dryRun) +
                    fixWhiteSpacesBetweenParametersInValueParameterList(multiline, dryRun) +
                    fixWhiteSpaceBeforeClosingParenthesis(multiline, dryRun)
            }

        return whiteSpaceCorrection
    }

    private fun ASTNode.fixWhiteSpacesInEmptyValueParameterList(dryRun: Boolean): Int {
        var whiteSpaceCorrection = 0

        takeUnless {
            // https://kotlinlang.org/docs/multiplatform-expect-actual.html#rules-for-expected-and-actual-declarations
            it.hasModifier(EXPECT_KEYWORD)
        }?.getPrimaryConstructorParameterListOrNull()
            ?.takeUnless { it.containsComment() }
            ?.takeUnless {
                // Allow:
                //     class Foo constructor() { ... }
                it.prevCodeSibling?.elementType == CONSTRUCTOR_KEYWORD
            }?.takeUnless {
                // Allow
                //     class Foo() {
                //         constructor(foo: String): this() {
                //             println(foo)
                //         }
                //     }
                findChildByType(CLASS_BODY)
                    ?.findChildByType(SECONDARY_CONSTRUCTOR)
                    ?.findChildByType(CONSTRUCTOR_DELEGATION_CALL)
                    ?.firstChildNode
                    ?.elementType == CONSTRUCTOR_DELEGATION_REFERENCE
            }?.let { parameterList ->
                if (!dryRun) {
                    emit(parameterList.startOffset, "No parenthesis expected") { parameterList.remove() }
                } else {
                    whiteSpaceCorrection -= parameterList.textLength
                }
            }

        return whiteSpaceCorrection
    }

    private fun ASTNode.containsComment() = children.any { it.isPartOfComment }

    private fun ASTNode.fixWhiteSpacesBeforeFirstParameterInValueParameterList(
        multiline: Boolean,
        dryRun: Boolean,
    ): Int {
        var whiteSpaceCorrection = 0

        val valueParameterList = getPrimaryConstructorParameterListOrNull()
        val firstParameterInList =
            valueParameterList
                ?.children
                ?.first { it.elementType == VALUE_PARAMETER }
                ?: return 0

        val firstParameter = firstParameterInList.firstChildNode
        firstParameter
            ?.prevLeaf
            ?.takeIf { it.isWhiteSpace }
            .let { whiteSpaceBeforeIdentifier ->
                if (multiline) {
                    if (whiteSpaceBeforeIdentifier.isWhiteSpaceWithoutNewlineOrNull) {
                        // Let indent rule determine the exact indent
                        val expectedParameterIndent = indentConfig.childIndentOf(this)
                        if (!dryRun) {
                            emit(firstParameterInList.startOffset, "Newline expected after opening parenthesis") {
                                valueParameterList.firstChildNode.upsertWhitespaceAfterMe(expectedParameterIndent)
                            }
                        } else {
                            whiteSpaceCorrection += expectedParameterIndent.length - (whiteSpaceBeforeIdentifier?.textLength ?: 0)
                        }
                    }
                } else if (whiteSpaceBeforeIdentifier != null) {
                    if (!dryRun) {
                        emit(firstParameter!!.startOffset, "No whitespace expected between opening parenthesis and first parameter name") {
                            whiteSpaceBeforeIdentifier.remove()
                        }
                    } else {
                        whiteSpaceCorrection -= whiteSpaceBeforeIdentifier.textLength
                    }
                }
            }

        return whiteSpaceCorrection
    }

    private fun ASTNode.fixWhiteSpacesBetweenParametersInValueParameterList(
        multiline: Boolean,
        dryRun: Boolean,
    ): Int {
        var whiteSpaceCorrection = 0

        val valueParameterList = getPrimaryConstructorParameterListOrNull()
        val firstParameterInList =
            valueParameterList
                ?.children
                ?.first { it.elementType == VALUE_PARAMETER }
                ?: return 0

        valueParameterList
            .children
            .filter { it.elementType == VALUE_PARAMETER }
            .filter { it != firstParameterInList }
            .forEach { whiteSpaceCorrection += it.fixWhiteSpaceValueParameter(this, multiline, dryRun) }

        return whiteSpaceCorrection
    }

    private fun ASTNode.fixWhiteSpaceValueParameter(
        node: ASTNode,
        multiline: Boolean,
        dryRun: Boolean,
    ): Int {
        var whiteSpaceCorrection = 0
        val firstChildNodeInValueParameter = firstChildNode
        firstChildNodeInValueParameter
            ?.prevLeaf
            ?.takeIf { it.isWhiteSpace }
            .let { whiteSpaceBeforeIdentifier ->
                if (multiline) {
                    if (whiteSpaceBeforeIdentifier.isWhiteSpaceWithoutNewlineOrNull) {
                        // Let IndentationRule determine the exact indent
                        val expectedParameterIndent = indentConfig.childIndentOf(node)
                        if (!dryRun) {
                            emit(startOffset, "Parameter should start on a newline") {
                                firstChildNodeInValueParameter.upsertWhitespaceBeforeMe(expectedParameterIndent)
                            }
                        } else {
                            whiteSpaceCorrection += expectedParameterIndent.length - (whiteSpaceBeforeIdentifier?.textLength ?: 0)
                        }
                    }
                } else {
                    if (whiteSpaceBeforeIdentifier == null || whiteSpaceBeforeIdentifier.text != " ") {
                        if (!dryRun) {
                            emit(firstChildNodeInValueParameter!!.startOffset, "Single whitespace expected before parameter") {
                                firstChildNodeInValueParameter.upsertWhitespaceBeforeMe(" ")
                            }
                        } else {
                            whiteSpaceCorrection += 1 - (whiteSpaceBeforeIdentifier?.textLength ?: 0)
                        }
                    }
                }
            }
        return whiteSpaceCorrection
    }

    private fun ASTNode.fixWhiteSpaceBeforeClosingParenthesis(
        multiline: Boolean,
        dryRun: Boolean,
    ): Int {
        var whiteSpaceCorrection = 0

        val closingParenthesisPrimaryConstructor =
            getPrimaryConstructorParameterListOrNull()
                ?.findChildByType(RPAR)
        closingParenthesisPrimaryConstructor
            ?.prevSibling
            ?.takeIf { it.isWhiteSpace }
            .let { whiteSpaceBeforeClosingParenthesis ->
                if (multiline) {
                    if (whiteSpaceBeforeClosingParenthesis.isWhiteSpaceWithoutNewlineOrNull) {
                        // Let IndentationRule determine the exact indent
                        val expectedParameterIndent = indent
                        if (!dryRun) {
                            emit(closingParenthesisPrimaryConstructor!!.startOffset, "Newline expected before closing parenthesis") {
                                closingParenthesisPrimaryConstructor.upsertWhitespaceBeforeMe(expectedParameterIndent)
                            }
                        } else {
                            whiteSpaceCorrection += expectedParameterIndent.length - (whiteSpaceBeforeClosingParenthesis?.textLength ?: 0)
                        }
                    }
                } else if (whiteSpaceBeforeClosingParenthesis != null && whiteSpaceBeforeClosingParenthesis.nextLeaf?.elementType == RPAR) {
                    if (!dryRun) {
                        emit(
                            whiteSpaceBeforeClosingParenthesis.startOffset,
                            "No whitespace expected between last parameter and closing parenthesis",
                        ) { whiteSpaceBeforeClosingParenthesis.remove() }
                    } else {
                        whiteSpaceCorrection -= whiteSpaceBeforeClosingParenthesis.textLength
                    }
                }
            }
        return whiteSpaceCorrection
    }

    private fun ASTNode.fixWhitespacesInSuperTypeList(wrapPrimaryConstructor: Boolean) {
        val superTypes = superTypes() ?: return

        fixWhitespacesInSuperTypeCallEntry(superTypes)

        if (superTypes.count() == 1) {
            val superType = superTypes.first()
            if (wrapPrimaryConstructor) {
                // Format
                //     class ClassWithPrimaryConstructorWhichWillBeWrapped(...) :
                //         SomeSuperTypeEntry
                // to
                //     class ClassWithPrimaryConstructorWhichWillBeWrapped(
                //         ...
                //     ) : SomeSuperTypeEntry
                superType.fixWhiteSpacesInSuperTypeListWithSingleEntryAndWrappedPrimaryConstructor()
            } else {
                fixWhiteSpacesInSuperTypeListWithSingleEntryAndUnwrappedPrimaryConstructor(superType)
            }
        } else {
            fixWhitespacesInSuperTypeListWithMultipleEntries(superTypes)
        }
    }

    private fun fixWhitespacesInSuperTypeCallEntry(superTypes: Sequence<ASTNode>) {
        superTypes
            .firstOrNull { it.elementType == SUPER_TYPE_CALL_ENTRY }
            ?.let { superTypeCallEntry ->
                if (superTypeCallEntry != superTypes.first()) {
                    emit(superTypeCallEntry.startOffset, "Super type call must be first super type") {
                        val superTypeList = superTypeCallEntry.parent!!
                        val originalFirstSuperType = superTypes.first()
                        val commaBeforeSuperTypeCall = requireNotNull(superTypeCallEntry.prevSibling { it.elementType == COMMA })

                        // Remove the whitespace before the super type call and do not insert a new whitespace as it will be fixed later
                        superTypeCallEntry
                            .prevSibling
                            ?.takeIf { it.isWhiteSpace }
                            ?.remove()

                        superTypeList.addChild(superTypeCallEntry, originalFirstSuperType)
                        superTypeList.addChild(commaBeforeSuperTypeCall, originalFirstSuperType)
                    }
                }

                // Disallow:
                //    class Foo : Bar<String> ("foobar")
                superTypeCallEntry
                    .findChildByType(WHITE_SPACE)
                    ?.let { whitespace ->
                        emit(whitespace.startOffset, "No whitespace expected") { whitespace.remove() }
                    }
            }
    }

    private fun ASTNode.fixWhiteSpacesInSuperTypeListWithSingleEntryAndWrappedPrimaryConstructor() {
        prevLeaf
            .takeIf { it.isWhiteSpaceWithNewline }
            ?.takeUnless { it.prevSibling?.elementType == EOL_COMMENT }
            ?.let { whiteSpaceBeforeSuperType ->
                val expectedWhitespace = " "
                if (whiteSpaceBeforeSuperType.text != expectedWhitespace) {
                    emit(startOffset, "Expected single space before the super type") {
                        upsertWhitespaceBeforeMe(expectedWhitespace)
                    }
                }
            }
    }

    private fun ASTNode.fixWhiteSpacesInSuperTypeListWithSingleEntryAndUnwrappedPrimaryConstructor(superType: ASTNode) {
        val superTypeFirstChildNode = superType.firstChildNode
        val whiteSpaceBeforeIdentifierOfSuperTypeFirstChildNode = superTypeFirstChildNode?.prevLeaf?.takeIf { it.isWhiteSpace }

        whiteSpaceBeforeIdentifierOfSuperTypeFirstChildNode
            .let { whiteSpaceBeforeIdentifier ->
                if (hasMultilineSuperTypeList() ||
                    classSignaturesIncludingFirstSuperTypeExceedsMaxLineLength()
                ) {
                    if (whiteSpaceBeforeIdentifier.isWhiteSpaceWithoutNewlineOrNull) {
                        emit(superTypeFirstChildNode.startOffset, "Super type should start on a newline") {
                            // Let IndentationRule determine the exact indent
                            superTypeFirstChildNode.upsertWhitespaceBeforeMe(indentConfig.childIndentOf(this))
                        }
                    }
                } else if (superTypeFirstChildNode.prevLeaf?.prevSibling?.elementType != EOL_COMMENT) {
                    val expectedWhitespace = " "
                    if (whiteSpaceBeforeIdentifier == null ||
                        whiteSpaceBeforeIdentifier.text != expectedWhitespace
                    ) {
                        emit(superTypeFirstChildNode.startOffset, "Expected single space before the super type") {
                            superTypeFirstChildNode.upsertWhitespaceBeforeMe(expectedWhitespace)
                        }
                    }
                }
            }
    }

    private fun ASTNode.fixWhitespacesInSuperTypeListWithMultipleEntries(superTypes: Sequence<ASTNode>) {
        require(superTypes.count() > 1)
        superTypes
            .forEachIndexed { index, superType ->
                val firstChildNodeInSuperType = superType.firstChildNode
                firstChildNodeInSuperType
                    ?.prevLeaf
                    ?.takeIf { it.isWhiteSpace }
                    .let { whiteSpaceBeforeIdentifier ->
                        if (index == 0 && hasMultilinePrimaryConstructor()) {
                            val expectedWhitespace = " "
                            if (whiteSpaceBeforeIdentifier?.prevLeaf?.elementType != EOL_COMMENT &&
                                (whiteSpaceBeforeIdentifier == null || whiteSpaceBeforeIdentifier.text != expectedWhitespace)
                            ) {
                                emit(firstChildNodeInSuperType.startOffset, "Expected single space before the first super type") {
                                    firstChildNodeInSuperType.upsertWhitespaceBeforeMe(expectedWhitespace)
                                }
                            }
                        } else {
                            if (whiteSpaceBeforeIdentifier.isWhiteSpaceWithoutNewlineOrNull) {
                                emit(firstChildNodeInSuperType.startOffset, "Super type should start on a newline") {
                                    // Let IndentationRule determine the exact indent
                                    firstChildNodeInSuperType.upsertWhitespaceBeforeMe(indentConfig.childIndentOf(this))
                                }
                            }
                        }
                    }
            }
    }

    private fun ASTNode.classSignaturesIncludingFirstSuperTypeExceedsMaxLineLength(): Boolean {
        val actualClassSignatureLength = getClassSignatureLength(excludeSuperTypes = false)
        // Calculate the length of the class signature in case it, including the super types, would be rewritten as single
        // line (and without a maximum line length). The white space correction will be calculated via a dry run of the
        // actual fix.
        val length =
            actualClassSignatureLength +
                // Calculate the white space correction in case the signature would be rewritten to a single line
                fixWhiteSpacesInValueParameterList(multiline = false, dryRun = true)
        return hasNoMaxLineLengthSuppression() && length > maxLineLength
    }

    private fun ASTNode.hasMultilinePrimaryConstructor() =
        findChildByType(PRIMARY_CONSTRUCTOR)
            ?.findChildByType(VALUE_PARAMETER_LIST)
            ?.findChildByType(RPAR)
            ?.prevLeaf { !it.isPartOfComment }
            .isWhiteSpaceWithNewline

    private fun ASTNode.fixClassBody() {
        findChildByType(CLASS_BODY)
            ?.let { classBody ->
                if (classBody.prevLeaf?.text != " ") {
                    emit(classBody.startOffset, "Expected a single space before class body") {
                        classBody
                            .prevLeaf
                            ?.upsertWhitespaceAfterMe(" ")
                    }
                }
            }
    }

    private fun isMaxLineLengthSet() = maxLineLength != MAX_LINE_LENGTH_PROPERTY_OFF

    private fun List<ASTNode>.collectLeavesRecursively(): List<ASTNode> = flatMap { it.collectLeavesRecursively() }

    private fun ASTNode.collectLeavesRecursively(): List<ASTNode> =
        if (isLeaf) {
            listOf(this)
        } else {
            children
                .flatMap { it.collectLeavesRecursively() }
                .toList()
        }

    private fun List<ASTNode>.childrenBetween(
        startASTNodePredicate: (ASTNode) -> Boolean,
        endASTNodePredicate: (ASTNode) -> Boolean,
    ): List<ASTNode> {
        val iterator = iterator()
        var currentNode: ASTNode
        val childrenBetween: MutableList<ASTNode> = mutableListOf()

        while (iterator.hasNext()) {
            currentNode = iterator.next()
            if (startASTNodePredicate(currentNode)) {
                childrenBetween.add(currentNode)
                break
            }
        }

        while (iterator.hasNext()) {
            currentNode = iterator.next()
            childrenBetween.add(currentNode)
            if (endASTNodePredicate(currentNode)) {
                break
            }
        }

        return childrenBetween
    }

    private fun List<ASTNode>.joinTextToString(): String = collectLeavesRecursively().joinToString(separator = "") { it.text }

    private fun ASTNode.hasTooManyParameters(): Boolean = countParameters() >= classSignatureWrappingMinimumParameters

    private fun ASTNode.countParameters() =
        getPrimaryConstructorParameterListOrNull()
            ?.children
            .orEmpty()
            .count { it.elementType == VALUE_PARAMETER }

    private fun ASTNode.getPrimaryConstructorParameterListOrNull() =
        findChildByType(PRIMARY_CONSTRUCTOR)?.findChildByType(VALUE_PARAMETER_LIST)

    public companion object {
        private const val FORCE_MULTILINE_WHEN_PARAMETER_COUNT_GREATER_OR_EQUAL_THAN_PROPERTY_UNSET = Int.MAX_VALUE
        public val FORCE_MULTILINE_WHEN_PARAMETER_COUNT_GREATER_OR_EQUAL_THAN_PROPERTY: EditorConfigProperty<Int> =
            EditorConfigProperty(
                type =
                    PropertyType.LowerCasingPropertyType(
                        "ktlint_class_signature_rule_force_multiline_when_parameter_count_greater_or_equal_than",
                        "Force wrapping of the parameters of the class signature in case it contains at least the specified " +
                            "number of parameters, even in case the entire class signature would fit on a single line. " +
                            "Use value 'unset' to disable this setting.",
                        PropertyType.PropertyValueParser.POSITIVE_INT_VALUE_PARSER,
                        setOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "unset"),
                    ),
                defaultValue = FORCE_MULTILINE_WHEN_PARAMETER_COUNT_GREATER_OR_EQUAL_THAN_PROPERTY_UNSET,
                ktlintOfficialCodeStyleDefaultValue = 1,
                propertyMapper = { property, _ ->
                    if (property?.isUnset == true) {
                        FORCE_MULTILINE_WHEN_PARAMETER_COUNT_GREATER_OR_EQUAL_THAN_PROPERTY_UNSET
                    } else {
                        property?.getValueAs<Int>()
                    }
                },
                propertyWriter = { property ->
                    if (property == FORCE_MULTILINE_WHEN_PARAMETER_COUNT_GREATER_OR_EQUAL_THAN_PROPERTY_UNSET) {
                        "unset"
                    } else {
                        property.toString()
                    }
                },
            )
    }
}

public val CLASS_SIGNATURE_RULE_ID: RuleId = ClassSignatureRule().ruleId
