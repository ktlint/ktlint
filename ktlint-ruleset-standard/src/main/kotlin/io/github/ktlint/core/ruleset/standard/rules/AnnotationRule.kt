package io.github.ktlint.core.ruleset.standard.rules

import com.intellij.lang.ASTNode
import io.github.ktlint.core.rule.engine.core.api.ElementType.ANNOTATED_EXPRESSION
import io.github.ktlint.core.rule.engine.core.api.ElementType.ANNOTATION
import io.github.ktlint.core.rule.engine.core.api.ElementType.ANNOTATION_ENTRY
import io.github.ktlint.core.rule.engine.core.api.ElementType.ANNOTATION_TARGET
import io.github.ktlint.core.rule.engine.core.api.ElementType.CLASS
import io.github.ktlint.core.rule.engine.core.api.ElementType.COLON
import io.github.ktlint.core.rule.engine.core.api.ElementType.CONSTRUCTOR_CALLEE
import io.github.ktlint.core.rule.engine.core.api.ElementType.CONSTRUCTOR_KEYWORD
import io.github.ktlint.core.rule.engine.core.api.ElementType.FILE_ANNOTATION_LIST
import io.github.ktlint.core.rule.engine.core.api.ElementType.GT
import io.github.ktlint.core.rule.engine.core.api.ElementType.IDENTIFIER
import io.github.ktlint.core.rule.engine.core.api.ElementType.LAMBDA_EXPRESSION
import io.github.ktlint.core.rule.engine.core.api.ElementType.MODIFIER_LIST
import io.github.ktlint.core.rule.engine.core.api.ElementType.OPERATION_REFERENCE
import io.github.ktlint.core.rule.engine.core.api.ElementType.REFERENCE_EXPRESSION
import io.github.ktlint.core.rule.engine.core.api.ElementType.RPAR
import io.github.ktlint.core.rule.engine.core.api.ElementType.TYPE_ARGUMENT_LIST
import io.github.ktlint.core.rule.engine.core.api.ElementType.TYPE_PROJECTION
import io.github.ktlint.core.rule.engine.core.api.ElementType.TYPE_REFERENCE
import io.github.ktlint.core.rule.engine.core.api.ElementType.USER_TYPE
import io.github.ktlint.core.rule.engine.core.api.ElementType.VALUE_ARGUMENT
import io.github.ktlint.core.rule.engine.core.api.ElementType.VALUE_ARGUMENT_LIST
import io.github.ktlint.core.rule.engine.core.api.ElementType.VALUE_PARAMETER
import io.github.ktlint.core.rule.engine.core.api.IndentConfig
import io.github.ktlint.core.rule.engine.core.api.RuleId
import io.github.ktlint.core.rule.engine.core.api.SinceKtlint
import io.github.ktlint.core.rule.engine.core.api.SinceKtlint.Status.STABLE
import io.github.ktlint.core.rule.engine.core.api.children
import io.github.ktlint.core.rule.engine.core.api.editorconfig.CODE_STYLE_PROPERTY
import io.github.ktlint.core.rule.engine.core.api.editorconfig.CodeStyleValue
import io.github.ktlint.core.rule.engine.core.api.editorconfig.CommaSeparatedListValueParser
import io.github.ktlint.core.rule.engine.core.api.editorconfig.EditorConfig
import io.github.ktlint.core.rule.engine.core.api.editorconfig.EditorConfigProperty
import io.github.ktlint.core.rule.engine.core.api.editorconfig.INDENT_SIZE_PROPERTY
import io.github.ktlint.core.rule.engine.core.api.editorconfig.INDENT_STYLE_PROPERTY
import io.github.ktlint.core.rule.engine.core.api.firstChildLeafOrSelf
import io.github.ktlint.core.rule.engine.core.api.indent
import io.github.ktlint.core.rule.engine.core.api.isWhiteSpace
import io.github.ktlint.core.rule.engine.core.api.isWhiteSpaceWithNewline
import io.github.ktlint.core.rule.engine.core.api.isWhiteSpaceWithoutNewline
import io.github.ktlint.core.rule.engine.core.api.lastChildLeafOrSelf
import io.github.ktlint.core.rule.engine.core.api.nextCodeLeaf
import io.github.ktlint.core.rule.engine.core.api.nextCodeSibling
import io.github.ktlint.core.rule.engine.core.api.parent
import io.github.ktlint.core.rule.engine.core.api.prevCodeLeaf
import io.github.ktlint.core.rule.engine.core.api.prevCodeSibling
import io.github.ktlint.core.rule.engine.core.api.prevLeaf
import io.github.ktlint.core.rule.engine.core.api.prevSibling
import io.github.ktlint.core.rule.engine.core.api.upsertWhitespaceAfterMe
import io.github.ktlint.core.rule.engine.core.api.upsertWhitespaceBeforeMe
import io.github.ktlint.core.ruleset.standard.StandardRule
import org.ec4j.core.model.PropertyType
import org.jetbrains.kotlin.descriptors.annotations.AnnotationUseSiteTarget
import org.jetbrains.kotlin.psi.psiUtil.siblings
import org.jetbrains.kotlin.utils.addToStdlib.ifTrue

/**
 * Ensures that annotation are wrapped to separate lines.
 *
 * https://kotlinlang.org/docs/reference/coding-conventions.html#annotation-formatting
 *
 * 1) Place annotations on separate lines before the declaration to which they are attached, and with the same indentation:
 *
 *    ```
 *    @Target(AnnotationTarget.PROPERTY)
 *    annotation class JsonExclude
 *    ```
 *
 * 2) Annotations without arguments may be placed on the same line:
 *
 *    ```
 *    @JsonExclude @JvmField
 *    var x: String
 *    ```
 *
 * 3) A single annotation without arguments may be placed on the same line as the corresponding declaration:
 *
 *    ```
 *    @Test fun foo() { /*...*/ }
 *    ```
 *
 * 4) File annotations are placed after the file comment (if any), before the package statement, and are separated from package with a blank
 *    line (to emphasize the fact that they target the file and not the package).
 *
 * @see [AnnotationSpacingRule] for white space rules.
 */
@SinceKtlint("0.30", STABLE)
public class AnnotationRule :
    StandardRule(
        id = "annotation",
        usesEditorConfigProperties =
            setOf(
                INDENT_SIZE_PROPERTY,
                INDENT_STYLE_PROPERTY,
                ANNOTATIONS_WITH_PARAMETERS_NOT_TO_BE_WRAPPED_PROPERTY,
            ),
    ) {
    private var codeStyle = CODE_STYLE_PROPERTY.defaultValue
    private var indentConfig = IndentConfig.DEFAULT_INDENT_CONFIG
    private var annotationsWithParametersNoToBeWrapped =
        ANNOTATIONS_WITH_PARAMETERS_NOT_TO_BE_WRAPPED_PROPERTY.defaultValue

    private val handleAllAnnotationsWithParametersSameAsAnnotationsWithoutParameters by lazy {
        annotationsWithParametersNoToBeWrapped.contains("*")
    }

    override fun beforeFirstNode(editorConfig: EditorConfig) {
        codeStyle = editorConfig[CODE_STYLE_PROPERTY]
        indentConfig =
            IndentConfig(
                indentStyle = editorConfig[INDENT_STYLE_PROPERTY],
                tabWidth = editorConfig[INDENT_SIZE_PROPERTY],
            )
        annotationsWithParametersNoToBeWrapped =
            editorConfig[ANNOTATIONS_WITH_PARAMETERS_NOT_TO_BE_WRAPPED_PROPERTY]
    }

    override fun beforeVisitChildNodes(node: ASTNode) {
        when (node.elementType) {
            FILE_ANNOTATION_LIST -> {
                node.visitAnnotationList()
                node.visitFileAnnotationList()
            }

            ANNOTATED_EXPRESSION, MODIFIER_LIST -> {
                node.visitAnnotationList()
            }

            ANNOTATION -> {
                // Annotation array
                //     @[...]
                node.visitAnnotation()
            }

            ANNOTATION_ENTRY -> {
                node.visitAnnotationEntry()
            }

            TYPE_ARGUMENT_LIST -> {
                node.visitTypeArgumentList()
            }
        }
    }

    private fun ASTNode.visitAnnotationList() {
        require(elementType in ANNOTATION_CONTAINER)

        if (shouldWrapAnnotations()) {
            wrapAnnotationEntries()
            if (elementType == ANNOTATED_EXPRESSION) {
                visitAnnotatedExpression()
            }
        }
    }

    private fun ASTNode.wrapAnnotationEntries() {
        require(elementType in ANNOTATION_CONTAINER)
        val expectedIndent =
            when {
                elementType == ANNOTATED_EXPRESSION -> indentConfig.siblingIndentOf(this)
                hasAnnotationBeforeConstructor() -> indentConfig.siblingIndentOf(parent!!)
                else -> indentConfig.parentIndentOf(this)
            }
        wrapBeforeAnnotationEntries(expectedIndent)
        wrapAfterLastAnnotationEntry(expectedIndent)
    }

    private fun ASTNode.wrapBeforeAnnotationEntries(expectedIndent: String) {
        require(elementType in ANNOTATION_CONTAINER)
        children
            .filter { it.elementType == ANNOTATION_ENTRY }
            .filter {
                it.isAnnotationEntryWithValueArgumentListThatShouldBeWrapped() ||
                    !it.isPrecededByOtherAnnotationEntryWithoutParametersOnTheSameLine()
            }.forEachIndexed { index, annotationEntry ->
                annotationEntry
                    .prevLeaf
                    .takeUnless {
                        // Allow in ktlint_official code style:
                        //     class Foo(
                        //         bar: Bar,
                        //     ) : @Suppress("DEPRECATION")
                        //         FooBar()
                        index == 0 &&
                            codeStyle == CodeStyleValue.KTLINT_OFFICIAL &&
                            it.annotationOnSameLineAsClosingParenthesisOfClassParameterList()
                    }?.takeUnless { it.isWhiteSpaceWithNewline }
                    ?.let { prevLeaf ->
                        emit(prevLeaf.startOffset, "Expected newline before annotation") {
                            // Let the indentation rule determine the exact indentation and only report and fix when the line needs
                            // to be wrapped
                            prevLeaf.upsertWhitespaceBeforeMe(
                                prevLeaf
                                    .text
                                    .substringBeforeLast('\n', "")
                                    .plus(expectedIndent),
                            )
                        }
                    }
            }
    }

    private fun ASTNode.wrapAfterLastAnnotationEntry(expectedIndent: String) {
        require(elementType in ANNOTATION_CONTAINER)
        children
            .lastOrNull { it.elementType == ANNOTATION_ENTRY }
            ?.lastChildLeafOrSelf
            ?.nextCodeLeaf
            ?.prevLeaf
            ?.takeUnless { it.isWhiteSpaceWithNewline }
            ?.let { prevLeaf ->
                // Let the indentation rule determine the exact indentation and only report and fix when the line needs to be wrapped
                emit(prevLeaf.startOffset, "Expected newline after last annotation") {
                    prevLeaf.upsertWhitespaceAfterMe(expectedIndent)
                }
            }
    }

    private fun ASTNode.visitAnnotatedExpression() {
        require(elementType == ANNOTATED_EXPRESSION)
        takeUnless { it.nextCodeSibling?.elementType == OPERATION_REFERENCE }
            ?.lastChildLeafOrSelf
            ?.nextCodeLeaf
            ?.prevLeaf
            ?.takeUnless { it.isWhiteSpaceWithNewline }
            ?.let { leaf ->
                // Let the indentation rule determine the exact indentation and only report and fix when the line needs to be wrapped
                emit(leaf.startOffset, "Expected newline") { leaf.upsertWhitespaceBeforeMe(indent) }
            }
    }

    private fun ASTNode.shouldWrapAnnotations() =
        if (isAnnotatedExpressionBeforeLambdaExpression()) {
            false
        } else {
            hasAnnotationWithParameter() ||
                hasMultipleAnnotationsOnSameLine() ||
                hasAnnotationBeforeConstructor()
        }

    private fun ASTNode.isAnnotatedExpressionBeforeLambdaExpression() =
        null !=
            takeIf { it.elementType == ANNOTATED_EXPRESSION }
                ?.findChildByType(ANNOTATION_ENTRY)
                ?.takeIf { it.nextCodeSibling?.elementType == LAMBDA_EXPRESSION }

    private fun ASTNode.hasAnnotationWithParameter(): Boolean {
        require(elementType in ANNOTATION_CONTAINER)
        return children
            .any {
                it.isAnnotationEntryWithValueArgumentListThatShouldBeWrapped() &&
                    it.parent?.parent?.elementType != VALUE_PARAMETER &&
                    it.parent?.parent?.elementType != VALUE_ARGUMENT &&
                    it.isNotReceiverTargetAnnotation()
            }
    }

    private fun ASTNode.hasMultipleAnnotationsOnSameLine(): Boolean {
        require(elementType in ANNOTATION_CONTAINER)
        return children
            .any {
                it.parent?.elementType != ANNOTATION &&
                    it.parent?.parent?.elementType != VALUE_PARAMETER &&
                    it.parent?.parent?.elementType != VALUE_ARGUMENT &&
                    it.isPrecededByOtherAnnotationEntryOnTheSameLine() &&
                    it.isLastAnnotationEntry()
                // Code below is disallowed
                //   @Foo1 @Foo2 fun foo() {}
                // But following is allowed:
                //   @[Foo1 Foo2] fun foo() {}
                //   fun foo(@Bar1 @Bar2 bar) {}
            }
    }

    private fun ASTNode.hasAnnotationBeforeConstructor() =
        codeStyle == CodeStyleValue.KTLINT_OFFICIAL &&
            hasAnnotationEntry() &&
            nextCodeSibling?.elementType == CONSTRUCTOR_KEYWORD

    private fun ASTNode.hasAnnotationEntry() = children.any { it.elementType == ANNOTATION_ENTRY }

    private fun ASTNode.visitTypeArgumentList() {
        children
            .filter { it.elementType == TYPE_PROJECTION }
            .mapNotNull { it.findChildByType(TYPE_REFERENCE) }
            .filter { it.elementType == TYPE_REFERENCE }
            .mapNotNull { it.findChildByType(MODIFIER_LIST) }
            .filter { it.elementType == MODIFIER_LIST }
            .any { it.shouldWrapAnnotations() }
            .ifTrue { wrapTypeArgumentList() }
    }

    private fun ASTNode.wrapTypeArgumentList() {
        children
            .filter { it.elementType == TYPE_PROJECTION }
            .forEach { typeProjection ->
                val prevLeaf = typeProjection.prevLeaf.takeIf { it.isWhiteSpace }
                if (prevLeaf == null || prevLeaf.isWhiteSpaceWithoutNewline) {
                    emit(typeProjection.startOffset - 1, "Expected newline") {
                        typeProjection.upsertWhitespaceBeforeMe(indentConfig.childIndentOf(this))
                    }
                }
            }

        findChildByType(GT)
            ?.let { gt ->
                val prevLeaf = gt.prevLeaf.takeIf { it.isWhiteSpace }
                if (prevLeaf == null || prevLeaf.isWhiteSpaceWithoutNewline) {
                    emit(gt.startOffset, "Expected newline") {
                        gt.upsertWhitespaceBeforeMe(indentConfig.childIndentOf(this))
                    }
                }
            }
    }

    private fun ASTNode.visitAnnotationEntry() {
        require(elementType == ANNOTATION_ENTRY)
        if (isPrecededByOtherAnnotationEntryOnTheSameLine() && isPrecededByAnnotationOnAnotherLine()) {
            // Code below is disallowed
            //   @Foo1
            //   @Foo2 @Foo3
            //   fun foo() {}
            emit(startOffset, "All annotations should either be on a single line or all annotations should be on a separate line") {
                firstChildLeafOrSelf
                    .upsertWhitespaceBeforeMe(parent!!.getNewlineWithIndent())
            }
        }
    }

    private fun ASTNode.isPrecededByAnnotationOnAnotherLine(): Boolean {
        val firstAnnotation = parent?.findChildByType(ANNOTATION_ENTRY)
        return siblings(forward = false)
            .takeWhile { it != firstAnnotation }
            .any { it.isWhiteSpaceWithNewline }
    }

    private fun ASTNode.isNotReceiverTargetAnnotation() = getAnnotationUseSiteTarget() != AnnotationUseSiteTarget.RECEIVER

    private fun ASTNode.getAnnotationUseSiteTarget(): AnnotationUseSiteTarget? =
        takeIf { it.elementType == ANNOTATION_ENTRY }
            ?.findChildByType(ANNOTATION_TARGET)
            ?.let { USE_SITE_TARGETS[it.text] }

    private fun ASTNode.isAnnotationEntryWithValueArgumentListThatShouldBeWrapped() =
        when {
            handleAllAnnotationsWithParametersSameAsAnnotationsWithoutParameters -> {
                // Do never distinct between annotation with and without parameters
                false
            }

            annotationsWithParametersNoToBeWrapped.isEmpty() -> {
                // All annotations with parameters should be wrapped as the whitelist is empty
                true
            }

            elementType == ANNOTATION_ENTRY && findChildByType(VALUE_ARGUMENT_LIST) != null -> {
                // Only wrap annotation with parameters when it is not on the whitelist
                getAnnotationIdentifier() !in annotationsWithParametersNoToBeWrapped
            }

            else -> {
                // Annotation without parameter
                false
            }
        }

    private fun ASTNode.getAnnotationIdentifier() =
        findChildByType(CONSTRUCTOR_CALLEE)
            ?.findChildByType(TYPE_REFERENCE)
            ?.findChildByType(USER_TYPE)
            ?.findChildByType(REFERENCE_EXPRESSION)
            ?.findChildByType(IDENTIFIER)
            ?.text

    private fun ASTNode.isLastAnnotationEntry() =
        this ==
            parent
                ?.children
                ?.lastOrNull { it.elementType == ANNOTATION_ENTRY }

    private fun ASTNode.isPrecededByOtherAnnotationEntryWithoutParametersOnTheSameLine() =
        siblings(forward = false)
            .takeWhile { !it.isWhiteSpaceWithNewline && !it.isAnnotationEntryWithValueArgumentListThatShouldBeWrapped() }
            .any { it.elementType == ANNOTATION_ENTRY && !it.isAnnotationEntryWithValueArgumentListThatShouldBeWrapped() }

    private fun ASTNode.isPrecededByOtherAnnotationEntryOnTheSameLine() =
        siblings(forward = false)
            .takeWhile { !it.isWhiteSpaceWithNewline }
            .any { it.elementType == ANNOTATION_ENTRY }

    private fun ASTNode.isPrecededByOtherAnnotationEntry() =
        siblings(forward = false)
            .any { it.elementType == ANNOTATION_ENTRY }

    private fun ASTNode.isOnSameLineAsPreviousAnnotationEntry() =
        siblings(forward = false)
            .takeWhile { it.elementType != ANNOTATION_ENTRY }
            .none { it.isWhiteSpaceWithNewline }

    private fun ASTNode.isFollowedByOtherAnnotationEntry() = siblings(forward = true).any { it.elementType == ANNOTATION_ENTRY }

    private fun ASTNode?.annotationOnSameLineAsClosingParenthesisOfClassParameterList() =
        // Allow:
        //     class Foo(
        //         bar: Bar,
        //     ) : @Suppress("DEPRECATION")
        //         FooBar()
        this
            ?.takeIf { it.parent?.elementType == CLASS }
            ?.prevCodeSibling
            ?.takeIf { it.elementType == COLON }
            ?.prevCodeLeaf
            ?.takeIf { it.elementType == RPAR }
            ?.prevLeaf
            ?.isWhiteSpaceWithNewline
            ?: false

    private fun ASTNode.isOnSameLineAsNextAnnotationEntry() =
        siblings(forward = true)
            .takeWhile { it.elementType != ANNOTATION_ENTRY }
            .none { it.isWhiteSpaceWithNewline }

    private fun ASTNode.visitFileAnnotationList() {
        lastChildLeafOrSelf
            .nextCodeLeaf
            ?.let { codeLeaf ->
                val whitespaceBefore = codeLeaf.prevLeaf { it.isWhiteSpace }

                if (whitespaceBefore == null || whitespaceBefore.text != "\n\n") {
                    emit(codeLeaf.startOffset, "File annotations should be separated from file contents with a blank line") {
                        codeLeaf.upsertWhitespaceBeforeMe("\n\n")
                    }
                }
            }
    }

    private fun ASTNode.visitAnnotation() {
        require(elementType == ANNOTATION)

        if (isFollowedByAnotherAnnotationOnSameLine() ||
            isPrecededByAnotherAnnotationOnSameLine()
        ) {
            emit(startOffset, "@[...] style annotations should be on a separate line from other annotations.") {
                if (isFollowedByOtherAnnotationEntry()) {
                    upsertWhitespaceAfterMe(parent!!.getNewlineWithIndent())
                } else if (isPrecededByOtherAnnotationEntry()) {
                    upsertWhitespaceBeforeMe(parent!!.getNewlineWithIndent())
                }
            }
        }
    }

    private fun ASTNode.isFollowedByAnotherAnnotationOnSameLine(): Boolean =
        isFollowedByOtherAnnotationEntry() && isOnSameLineAsNextAnnotationEntry()

    private fun ASTNode.isPrecededByAnotherAnnotationOnSameLine(): Boolean =
        isPrecededByOtherAnnotationEntry() && isOnSameLineAsPreviousAnnotationEntry()

    private fun ASTNode.getNewlineWithIndent(): String {
        val nodeBeforeAnnotations = parent?.prevSibling
        // Make sure we only insert a single newline
        val indentWithoutNewline =
            nodeBeforeAnnotations
                ?.text
                .orEmpty()
                .substringAfterLast('\n')
        return "\n".plus(indentWithoutNewline)
    }

    public companion object {
        private val ANNOTATION_CONTAINER =
            listOf(
                ANNOTATED_EXPRESSION,
                FILE_ANNOTATION_LIST,
                MODIFIER_LIST,
            )
        private val USE_SITE_TARGETS = AnnotationUseSiteTarget.entries.associateBy { it.renderName }
        public val ANNOTATIONS_WITH_PARAMETERS_NOT_TO_BE_WRAPPED_PROPERTY: EditorConfigProperty<Set<String>> =
            EditorConfigProperty(
                type =
                    PropertyType.LowerCasingPropertyType(
                        "ktlint_annotation_handle_annotations_with_parameters_same_as_annotations_without_parameters",
                        "Handle listed annotations identical to annotations without parameters. Value is a comma separated list of names without the '@' prefix. Use '*' for all annotations with parameters.",
                        CommaSeparatedListValueParser(),
                    ),
                defaultValue = setOf("unset"),
            )
    }
}

public val ANNOTATION_RULE_ID: RuleId = AnnotationRule().ruleId
