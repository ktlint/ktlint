package io.github.ktlint.core.ruleset.standard.rules

import com.intellij.lang.ASTNode
import com.intellij.psi.impl.source.tree.LeafPsiElement
import com.intellij.psi.tree.IElementType
import com.intellij.psi.tree.TokenSet
import io.github.ktlint.core.logger.api.initKtLintKLogger
import io.github.ktlint.core.rule.engine.core.api.ElementType.ANNOTATION
import io.github.ktlint.core.rule.engine.core.api.ElementType.ARROW
import io.github.ktlint.core.rule.engine.core.api.ElementType.BLOCK
import io.github.ktlint.core.rule.engine.core.api.ElementType.CALL_EXPRESSION
import io.github.ktlint.core.rule.engine.core.api.ElementType.CLOSING_QUOTE
import io.github.ktlint.core.rule.engine.core.api.ElementType.COMMA
import io.github.ktlint.core.rule.engine.core.api.ElementType.CONDITION
import io.github.ktlint.core.rule.engine.core.api.ElementType.DESTRUCTURING_DECLARATION
import io.github.ktlint.core.rule.engine.core.api.ElementType.DOT
import io.github.ktlint.core.rule.engine.core.api.ElementType.EOL_COMMENT
import io.github.ktlint.core.rule.engine.core.api.ElementType.FOR
import io.github.ktlint.core.rule.engine.core.api.ElementType.FUN
import io.github.ktlint.core.rule.engine.core.api.ElementType.FUNCTION_LITERAL
import io.github.ktlint.core.rule.engine.core.api.ElementType.GT
import io.github.ktlint.core.rule.engine.core.api.ElementType.LAMBDA_EXPRESSION
import io.github.ktlint.core.rule.engine.core.api.ElementType.LBRACE
import io.github.ktlint.core.rule.engine.core.api.ElementType.LBRACKET
import io.github.ktlint.core.rule.engine.core.api.ElementType.LITERAL_STRING_TEMPLATE_ENTRY
import io.github.ktlint.core.rule.engine.core.api.ElementType.LONG_STRING_TEMPLATE_ENTRY
import io.github.ktlint.core.rule.engine.core.api.ElementType.LPAR
import io.github.ktlint.core.rule.engine.core.api.ElementType.LT
import io.github.ktlint.core.rule.engine.core.api.ElementType.OBJECT_LITERAL
import io.github.ktlint.core.rule.engine.core.api.ElementType.RBRACE
import io.github.ktlint.core.rule.engine.core.api.ElementType.RBRACKET
import io.github.ktlint.core.rule.engine.core.api.ElementType.RPAR
import io.github.ktlint.core.rule.engine.core.api.ElementType.STRING_TEMPLATE
import io.github.ktlint.core.rule.engine.core.api.ElementType.SUPER_TYPE_CALL_ENTRY
import io.github.ktlint.core.rule.engine.core.api.ElementType.SUPER_TYPE_ENTRY
import io.github.ktlint.core.rule.engine.core.api.ElementType.SUPER_TYPE_LIST
import io.github.ktlint.core.rule.engine.core.api.ElementType.TYPE_ARGUMENT_LIST
import io.github.ktlint.core.rule.engine.core.api.ElementType.TYPE_CONSTRAINT
import io.github.ktlint.core.rule.engine.core.api.ElementType.TYPE_CONSTRAINT_LIST
import io.github.ktlint.core.rule.engine.core.api.ElementType.TYPE_PARAMETER
import io.github.ktlint.core.rule.engine.core.api.ElementType.TYPE_PARAMETER_LIST
import io.github.ktlint.core.rule.engine.core.api.ElementType.TYPE_PROJECTION
import io.github.ktlint.core.rule.engine.core.api.ElementType.VALUE_ARGUMENT
import io.github.ktlint.core.rule.engine.core.api.ElementType.VALUE_ARGUMENT_LIST
import io.github.ktlint.core.rule.engine.core.api.ElementType.VALUE_PARAMETER
import io.github.ktlint.core.rule.engine.core.api.ElementType.VALUE_PARAMETER_LIST
import io.github.ktlint.core.rule.engine.core.api.ElementType.WHEN_ENTRY
import io.github.ktlint.core.rule.engine.core.api.ElementType.WHERE_KEYWORD
import io.github.ktlint.core.rule.engine.core.api.ElementType.WHITE_SPACE
import io.github.ktlint.core.rule.engine.core.api.IndentConfig
import io.github.ktlint.core.rule.engine.core.api.IndentConfig.Companion.DEFAULT_INDENT_CONFIG
import io.github.ktlint.core.rule.engine.core.api.RuleId
import io.github.ktlint.core.rule.engine.core.api.SinceKtlint
import io.github.ktlint.core.rule.engine.core.api.SinceKtlint.Status.STABLE
import io.github.ktlint.core.rule.engine.core.api.TokenSets
import io.github.ktlint.core.rule.engine.core.api.children
import io.github.ktlint.core.rule.engine.core.api.editorconfig.EditorConfig
import io.github.ktlint.core.rule.engine.core.api.editorconfig.INDENT_SIZE_PROPERTY
import io.github.ktlint.core.rule.engine.core.api.editorconfig.INDENT_STYLE_PROPERTY
import io.github.ktlint.core.rule.engine.core.api.editorconfig.MAX_LINE_LENGTH_PROPERTY
import io.github.ktlint.core.rule.engine.core.api.editorconfig.MAX_LINE_LENGTH_PROPERTY_OFF
import io.github.ktlint.core.rule.engine.core.api.firstChildLeafOrSelf
import io.github.ktlint.core.rule.engine.core.api.hasNewLineInClosedRange
import io.github.ktlint.core.rule.engine.core.api.hasNoMaxLineLengthSuppression
import io.github.ktlint.core.rule.engine.core.api.indent
import io.github.ktlint.core.rule.engine.core.api.isPartOf
import io.github.ktlint.core.rule.engine.core.api.isPartOfComment
import io.github.ktlint.core.rule.engine.core.api.isWhiteSpace
import io.github.ktlint.core.rule.engine.core.api.isWhiteSpaceWithNewline
import io.github.ktlint.core.rule.engine.core.api.isWhiteSpaceWithoutNewline
import io.github.ktlint.core.rule.engine.core.api.lastChildLeafOrSelf
import io.github.ktlint.core.rule.engine.core.api.leavesForwardsIncludingSelf
import io.github.ktlint.core.rule.engine.core.api.nextCodeLeaf
import io.github.ktlint.core.rule.engine.core.api.nextCodeSibling
import io.github.ktlint.core.rule.engine.core.api.nextLeaf
import io.github.ktlint.core.rule.engine.core.api.nextSibling
import io.github.ktlint.core.rule.engine.core.api.parent
import io.github.ktlint.core.rule.engine.core.api.prevCodeLeaf
import io.github.ktlint.core.rule.engine.core.api.prevCodeSibling
import io.github.ktlint.core.rule.engine.core.api.prevLeaf
import io.github.ktlint.core.rule.engine.core.api.prevSibling
import io.github.ktlint.core.rule.engine.core.api.remove
import io.github.ktlint.core.rule.engine.core.api.upsertWhitespaceAfterMe
import io.github.ktlint.core.rule.engine.core.api.upsertWhitespaceBeforeMe
import io.github.ktlint.core.ruleset.standard.StandardRule
import io.github.oshai.kotlinlogging.KotlinLogging
import org.jetbrains.kotlin.psi.KtStringTemplateExpression
import org.jetbrains.kotlin.psi.KtSuperTypeList
import org.jetbrains.kotlin.psi.psiUtil.leaves
import org.jetbrains.kotlin.psi.psiUtil.siblings

private val LOGGER = KotlinLogging.logger {}.initKtLintKLogger()

/**
 * This rule inserts missing newlines (e.g. between parentheses of a multi-line function call). This logic previously
 * was part of the IndentationRule (phase 1).
 *
 * Whenever a linebreak is inserted, this rules assumes that the parent node it indented correctly. So the indentation
 * is fixed with respect to indentation of the parent. This is just a simple best effort for the case that the
 * indentation rule is not run.
 */
@SinceKtlint("0.45", STABLE)
public class WrappingRule :
    StandardRule(
        id = "wrapping",
        usesEditorConfigProperties =
            setOf(
                INDENT_SIZE_PROPERTY,
                INDENT_STYLE_PROPERTY,
                MAX_LINE_LENGTH_PROPERTY,
            ),
    ) {
    private var line = 1
    private var indentConfig = DEFAULT_INDENT_CONFIG
    private var maxLineLength: Int = MAX_LINE_LENGTH_PROPERTY.defaultValue

    override fun beforeFirstNode(editorConfig: EditorConfig) {
        line = 1
        indentConfig =
            IndentConfig(
                indentStyle = editorConfig[INDENT_STYLE_PROPERTY],
                tabWidth = editorConfig[INDENT_SIZE_PROPERTY],
            )
        maxLineLength = editorConfig.maxLineLength()
    }

    override fun beforeVisitChildNodes(node: ASTNode) {
        when (node.elementType) {
            BLOCK -> node.beforeVisitBlock()
            LPAR, LBRACE, LBRACKET -> node.rearrangeBlock()
            SUPER_TYPE_LIST -> node.rearrangeSuperTypeList()
            VALUE_PARAMETER_LIST, VALUE_ARGUMENT_LIST -> node.rearrangeValueList()
            TYPE_ARGUMENT_LIST, TYPE_PARAMETER_LIST -> node.rearrangeTypeArgumentList()
            WHERE_KEYWORD -> node.rearrangeTypeConstraintList()
            ARROW -> node.rearrangeArrow()
            WHITE_SPACE -> line += node.text.count { it == '\n' }
            CLOSING_QUOTE -> node.rearrangeClosingQuote()
        }
    }

    private fun ASTNode.beforeVisitBlock() {
        require(elementType == BLOCK)

        val lbrace =
            getStartOfBlock()
                ?.takeIf { it.elementType == LBRACE }
                ?: return
        if (lbrace.lbraceStartsSpecialBlock()) {
            // String template inside raw string literal may exceed the maximum line length
            return
        }

        takeUnless { it.firstChildLeafOrSelf.elementType == EOL_COMMENT }
            ?.getEndOfBlock()
            ?.takeIf { it.elementType == RBRACE }
            ?.let { rbrace ->
                if (hasNewLineInClosedRange(lbrace, rbrace)) {
                    lbrace.requireNewlineAfterLeaf()
                }
            }

        if (maxLineLength != MAX_LINE_LENGTH_PROPERTY_OFF && hasNoMaxLineLengthSuppression()) {
            val lengthUntilBeginOfLine =
                leaves(false)
                    .takeWhile { !it.isWhiteSpaceWithNewline }
                    .sumOf { it.textLength }
            val lengthUntilEndOfLine =
                firstChildLeafOrSelf
                    .leavesForwardsIncludingSelf
                    .takeWhile { !it.isWhiteSpaceWithNewline }
                    .sumOf { it.textLength }
            if (lengthUntilBeginOfLine + lengthUntilEndOfLine > maxLineLength) {
                lbrace.requireNewlineAfterLeaf()
            }
        }
    }

    private fun ASTNode.lbraceStartsSpecialBlock() =
        followedByNewline() ||
            followedByEolComment() ||
            followedByFunctionLiteralParameterList() ||
            isPartOf(LONG_STRING_TEMPLATE_ENTRY)

    private fun ASTNode.followedByEolComment() =
        null !=
            leaves()
                .takeWhile { it.isWhiteSpaceWithoutNewline || it.elementType == EOL_COMMENT }
                .firstOrNull { it.elementType == EOL_COMMENT }

    private fun ASTNode.followedByFunctionLiteralParameterList() =
        VALUE_PARAMETER_LIST ==
            takeIf { parent?.elementType == FUNCTION_LITERAL }
                ?.nextCodeSibling
                ?.elementType

    private fun ASTNode.rearrangeBlock() {
        val closingElement = findMatchingClosingElement()
        if (shouldNotRearrangeBlock(closingElement)) {
            return
        }
        if (!isFollowedByNewline() &&
            // Allow:
            //    if (foo(
            //            "bar",
            //            "baz",
            //        )
            //     ) {
            //        // something
            //
            nextSibling?.elementType != CONDITION
        ) {
            requireNewlineAfterLeaf()
        }
        if (!closingElement.prevLeaf.isWhiteSpaceWithNewline) {
            closingElement.requireNewlineBeforeLeaf(indentConfig.parentIndentOf(this))
        }
    }

    private fun ASTNode.shouldNotRearrangeBlock(closingElement: ASTNode): Boolean =
        hasNoDisallowedNewlines(closingElement) ||
            containsMultilineValueParameterList() ||
            hasSingleValueArgument(OBJECT_LITERAL, LAMBDA_EXPRESSION) ||
            isPartOfForLoopConditionWithMultilineExpression()

    private fun ASTNode.findMatchingClosingElement(): ASTNode {
        val closingElementType =
            when (elementType) {
                LPAR -> RPAR
                LBRACE -> RBRACE
                LBRACKET -> RBRACKET
                LT -> GT
                else -> error("Unexpected element type $elementType")
            }
        return requireNotNull(nextSibling { it.elementType == closingElementType })
    }

    private fun ASTNode.hasNoDisallowedNewlines(closingElement: ASTNode): Boolean =
        siblings()
            .takeWhile { it != closingElement }
            .none {
                if (it.elementType == VALUE_ARGUMENT) {
                    it.hasLineBreak(LAMBDA_EXPRESSION, FUN)
                } else {
                    it.hasLineBreak()
                }
            }

    private fun ASTNode.containsMultilineValueParameterList(): Boolean =
        // keep
        //     { p ->
        //     }
        elementType == LBRACE && siblings().any { it.elementType == VALUE_PARAMETER_LIST }

    private fun ASTNode.hasSingleValueArgument(vararg elementTypes: IElementType): Boolean =
        // keep
        //     (object : C {
        //     })
        // and
        //     ({
        //     })
        siblings()
            .filter { it.elementType == VALUE_ARGUMENT }
            .takeIf { it.count() == 1 }
            ?.first()
            ?.firstChildNode
            ?.elementType
            ?.let { it in elementTypes }
            ?: false

    private fun ASTNode.isFollowedByNewline(): Boolean =
        nextCodeLeaf
            ?.prevLeaf {
                // Skip comments, whitespace, and empty nodes
                !it.isPartOfComment && !it.isWhiteSpaceWithoutNewline && it.textLength > 0
            }.isWhiteSpaceWithNewline

    private fun ASTNode.rearrangeSuperTypeList() {
        val entries = (psi as KtSuperTypeList).entries
        // e.g. class A : B, C, D (multi-line) but not class A : B, C, D({})
        val isLastEntryACallButNotAllOthersAreEntries =
            entries.dropLast(1).all { it.iElementType == SUPER_TYPE_ENTRY } &&
                entries.last().iElementType == SUPER_TYPE_CALL_ENTRY
        val requiresSuperTypeSeparation =
            textContains('\n') && entries.size > 1 && !isLastEntryACallButNotAllOthersAreEntries
        if (requiresSuperTypeSeparation) {
            // put space after :
            if (!prevLeaf.isWhiteSpaceWithNewline) {
                val colon = prevCodeLeaf!!
                if (
                    !colon.prevLeaf.isWhiteSpaceWithNewline &&
                    colon.prevCodeLeaf.let { it?.elementType != RPAR || !it.prevLeaf.isWhiteSpaceWithNewline }
                ) {
                    colon.requireNewlineAfterLeaf()
                }
            }
            // put entries on separate lines
            for (c in children) {
                if (c.elementType == COMMA &&
                    !c.nextSibling.isWhiteSpaceWithNewline &&
                    !c.isFollowedByCommentOnSameLine()
                ) {
                    c.requireNewlineAfterLeaf(indent = indent)
                }
            }
        }
    }

    private fun ASTNode.isFollowedByCommentOnSameLine() =
        nextLeaf { !it.isWhiteSpaceWithoutNewline }
            ?.isPartOfComment == true

    private fun ASTNode.rearrangeValueList() {
        for (c in children) {
            val hasLineBreak =
                when (c.elementType) {
                    VALUE_ARGUMENT -> c.hasLineBreak(LAMBDA_EXPRESSION, FUN)
                    VALUE_PARAMETER, ANNOTATION -> c.hasLineBreak()
                    else -> false
                }
            if (hasLineBreak) {
                // rearrange
                //
                // a, b, value(
                // ), c, d
                //
                // to
                //
                // a, b,
                // value(
                // ),
                // c, d

                // insert \n in front of multi-line value
                val prevSibling = c.prevSibling { !it.isWhiteSpace }
                if (
                    prevSibling?.elementType == COMMA &&
                    !prevSibling.nextSibling.isWhiteSpaceWithNewline
                ) {
                    prevSibling.requireNewlineAfterLeaf()
                }
                // insert \n after multi-line value
                val nextSibling = c.nextSibling { !it.isWhiteSpace }
                val hasDestructuringDeclarationAsLastValueParameter =
                    c.isLastValueParameter() && c.firstChildNode.elementType == DESTRUCTURING_DECLARATION
                val needsNewlineAfterComma =
                    nextSibling?.elementType == COMMA &&
                        !hasDestructuringDeclarationAsLastValueParameter &&
                        !nextSibling.nextSibling.isWhiteSpaceWithNewline &&
                        // value( // ), // a comment // c, d
                        nextSibling.nextSibling?.nextSibling?.let { !TokenSets.COMMENTS.contains(it.elementType) } != false
                if (needsNewlineAfterComma) {
                    nextSibling.requireNewlineAfterLeaf()
                }
            }
        }
    }

    private fun ASTNode.isLastValueParameter() =
        elementType == VALUE_PARAMETER &&
            siblings().none { it.elementType == VALUE_PARAMETER }

    private fun ASTNode.rearrangeTypeArgumentList() {
        if (textContains('\n')) {
            // Each type projection must be preceded with a whitespace containing a newline
            children
                .filter { it.elementType == TYPE_PROJECTION || it.elementType == TYPE_PARAMETER }
                .forEach { typeProjection ->
                    typeProjection
                        .prevSibling { !it.isPartOfComment }
                        .let { prevSibling ->
                            if (prevSibling?.elementType == LT || prevSibling.isWhiteSpaceWithoutNewline) {
                                emit(typeProjection.startOffset, "A newline was expected before '${typeProjection.text}'") {
                                    typeProjection.upsertWhitespaceBeforeMe(indentConfig.siblingIndentOf(this))
                                }
                            }
                        }
                }

            // After the last type projection a whitespace containing a newline must exist
            findChildByType(GT)
                ?.let { closingAngle ->
                    val prevSibling = closingAngle.prevSibling { !it.isPartOfComment }
                    if (!prevSibling.isWhiteSpaceWithNewline) {
                        emit(closingAngle.startOffset, "A newline was expected before '${closingAngle.text}'") {
                            closingAngle.upsertWhitespaceBeforeMe(indentConfig.siblingIndentOf(this))
                        }
                    }
                }
        }
    }

    private fun ASTNode.rearrangeTypeConstraintList() {
        require(elementType == WHERE_KEYWORD)

        if (hasNewLineInClosedRange(this, nextSibling { it.elementType == TYPE_CONSTRAINT_LIST } ?: this)) {
            // Indent where keyword
            prevSibling { !it.isPartOfComment }
                .let { prevSibling ->
                    if (prevSibling.isWhiteSpaceWithoutNewline) {
                        emit(startOffset, "A newline was expected before 'where'") {
                            upsertWhitespaceBeforeMe(indentConfig.siblingIndentOf(this))
                        }
                    }
                }

            // Each type projection must be preceded with a whitespace containing a newline
            nextSibling { it.elementType == TYPE_CONSTRAINT_LIST }
                ?.children
                ?.forEach { child ->
                    when (child.elementType) {
                        COMMA -> child.rearrangeCommaInTypeConstraintList()
                        TYPE_CONSTRAINT -> child.rearrangeTypeConstraintInTypeConstraintList()
                    }
                }
        }
    }

    private fun ASTNode.rearrangeCommaInTypeConstraintList() {
        require(elementType == COMMA && parent!!.elementType == TYPE_CONSTRAINT_LIST)
        prevSibling
            ?.takeIf { it.isWhiteSpaceWithNewline }
            ?.let { whitespaceWithNewline ->
                emit(startOffset, "No whitespace was expected before '$text'") {
                    if (whitespaceWithNewline.prevLeaf?.elementType == EOL_COMMENT) {
                        // In case the comma was preceded by an EOL_COMMENT then move the comma before that comment
                        //        where T : CharSequence // Some comment
                        //            , T : Comparable<T>
                        val eolCommentBeforeComma = whitespaceWithNewline.prevLeaf!!
                        val insertCommaBefore =
                            eolCommentBeforeComma
                                .prevLeaf
                                .takeIf { it.isWhiteSpace }
                                ?: eolCommentBeforeComma
                        eolCommentBeforeComma.treeParent.addChild(LeafPsiElement(COMMA, ","), insertCommaBefore)
                        nextSibling
                            ?.takeIf { it.isWhiteSpace }
                            ?.remove()
                        remove()
                    } else {
                        whitespaceWithNewline.remove()
                    }
                }
            }
    }

    private fun ASTNode.rearrangeTypeConstraintInTypeConstraintList() {
        require(elementType == TYPE_CONSTRAINT)
        prevSibling { !it.isPartOfComment }
            .let { prevSibling ->
                if (prevSibling.isWhiteSpaceWithoutNewline) {
                    emit(startOffset, "A newline was expected before '$text'") {
                        upsertWhitespaceBeforeMe(
                            indentConfig.siblingIndentOf(
                                parent { it.elementType == FUN }
                                    ?.findChildByType(WHERE_KEYWORD)!!,
                            ),
                        )
                    }
                }
            }
    }

    private fun ASTNode.rearrangeClosingQuote() {
        parent
            ?.takeIf { it.elementType == STRING_TEMPLATE }
            ?.let { it.psi as KtStringTemplateExpression }
            ?.takeIf { it.isMultiLine() }
            ?.takeIf { it.isFollowedByTrimIndent() || it.isFollowedByTrimMargin() }
            ?.takeUnless { prevSibling?.text.isNullOrBlank() }
            ?.let {
                // rewriting
                // """
                //     text
                // _""".trimIndent()
                // to
                // """
                //     text
                // _
                // """.trimIndent()
                emit(startOffset, "Missing newline before \"\"\"") {
                    LOGGER.trace { "$line: inserted newline before (closing) \"\"\"" }
                    this as LeafPsiElement
                    rawInsertBeforeMe(LeafPsiElement(LITERAL_STRING_TEMPLATE_ENTRY, "\n"))
                }
            }
    }

    private fun ASTNode.mustBeFollowedByNewline(): Boolean {
        // find EOL token (last token before \n)
        // if token is in lTokenSet
        //     find matching rToken
        //     return true if there is no newline after the rToken
        // return false
        val nextCodeSibling = nextCodeSibling // e.g. BINARY_EXPRESSION
        var lToken = nextCodeSibling?.nextLeaf { it.isWhiteSpaceWithNewline }?.prevCodeLeaf
        if (lToken != null && lToken.elementType !in LTOKEN_SET) {
            // special cases:
            // x = y.f({ z ->
            // })
            // x = y.f(0, 1,
            // 2, 3)
            lToken = lToken.prevLeaf { it.elementType in LTOKEN_SET || it == this }
        }
        if (lToken != null && lToken.elementType in LTOKEN_SET) {
            val rElementType = MATCHING_RTOKEN_MAP[lToken.elementType]
            val rToken = lToken.nextSibling { it.elementType == rElementType }
            return rToken?.parent == lToken.parent
        }
        return nextCodeSibling?.textContains('\n') == false
    }

    private fun ASTNode.rearrangeArrow() {
        val parent = parent!!
        // Only check `{ p -> ... }` and `when { m -> ... }` that span multiple lines
        val arrowDoesNotRequireNewlineRearrangement =
            parent.elementType.let { it != FUNCTION_LITERAL && it != WHEN_ENTRY } ||
                !parent.textContains('\n') ||
                (parent.elementType == WHEN_ENTRY && mustBeFollowedByNewline()) ||
                (parent.elementType == WHEN_ENTRY && prevLeaf.isWhiteSpaceWithNewline)
        if (arrowDoesNotRequireNewlineRearrangement) {
            return
        }
        if (!nextCodeLeaf?.prevLeaf.isWhiteSpaceWithNewline) {
            requireNewlineAfterLeaf()
        }
        val r = nextSibling { it.elementType == RBRACE } ?: return
        if (!r.prevLeaf.isWhiteSpaceWithNewline) {
            r.requireNewlineBeforeLeaf(indent)
        }
    }

    private fun ASTNode.requireNewlineBeforeLeaf(indent: String) {
        emit(startOffset - 1, """Missing newline before "$text"""") {
            LOGGER.trace { "$line: inserted newline before $text" }
            upsertWhitespaceBeforeMe(indent)
        }
    }

    private fun ASTNode.requireNewlineAfterLeaf(indent: String? = null) {
        emit(startOffset + 1, """Missing newline after "$text"""") {
            LOGGER.trace { "$line: inserted newline after $text" }
            val tempIndent = indent ?: (indentConfig.childIndentOf(this))
            upsertWhitespaceAfterMe(tempIndent)
        }
    }

    private fun KtStringTemplateExpression.isMultiLine(): Boolean {
        for (child in node.children) {
            if (child.elementType == LITERAL_STRING_TEMPLATE_ENTRY) {
                val v = child.text
                if (v == "\n") {
                    return true
                }
            }
        }
        return false
    }

    private fun ASTNode.hasLineBreak(vararg ignoreElementTypes: IElementType): Boolean {
        if (isWhiteSpaceWithNewline) return true
        return if (ignoreElementTypes.isEmpty()) {
            textContains('\n')
        } else {
            elementType !in ignoreElementTypes &&
                children.any { c -> c.textContains('\n') && c.elementType !in ignoreElementTypes }
        }
    }

    private fun KtStringTemplateExpression.isFollowedByTrimIndent() = isFollowedBy("trimIndent()")

    private fun KtStringTemplateExpression.isFollowedByTrimMargin() = isFollowedBy("trimMargin()")

    private fun KtStringTemplateExpression.isFollowedBy(callExpressionName: String) =
        this
            .node
            .nextSibling { it.elementType != DOT }
            .let { it?.elementType == CALL_EXPRESSION && it.text == callExpressionName }

    /**
     *  Allow for-statement in which only the expression contains a newline:
     *     for (foo in listOf(
     *         "foo-1",
     *         "foo-2"
     *     )) { ... }
     * but reject:
     *     for (
     *         foo in listOf(
     *             "foo-1",
     *             "foo-2"
     *         )
     *     ) { ... }
     */
    private fun ASTNode.isPartOfForLoopConditionWithMultilineExpression(): Boolean =
        parent?.elementType == FOR &&
            if (elementType == LPAR) {
                nextSibling { it.elementType == RPAR || it.isWhiteSpaceWithNewline }?.elementType == RPAR
            } else {
                parent?.findChildByType(LPAR)!!.isPartOfForLoopConditionWithMultilineExpression()
            }

    override fun afterVisitChildNodes(node: ASTNode) {
        if (node.elementType == BLOCK) {
            val lbrace =
                node
                    .getStartOfBlock()
                    ?.takeIf { it.elementType == LBRACE }
                    ?: return
            node
                .getEndOfBlock()
                ?.takeIf { it.elementType == RBRACE }
                ?.takeUnless { it.isPrecededByNewline() }
                ?.let { rbrace ->
                    if (hasNewLineInClosedRange(lbrace, rbrace)) {
                        rbrace.requireNewlineBeforeLeaf(indentConfig.parentIndentOf(node))
                    }
                }
        }
    }

    private fun ASTNode.followedByNewline() = nextLeaf.isWhiteSpaceWithNewline

    private fun ASTNode.isPrecededByNewline() = prevLeaf.isWhiteSpaceWithNewline

    private fun ASTNode.getStartOfBlock() =
        if (parent?.elementType == FUNCTION_LITERAL) {
            parent!!.findChildByType(LBRACE)
        } else {
            firstChildLeafOrSelf
                .let { node ->
                    if (node.elementType == LBRACE) {
                        // WHEN-entry block have LBRACE and RBRACE as first and last elements
                        node
                    } else {
                        // Other blocks have LBRACE and RBRACE as siblings of the block
                        node.prevCodeSibling
                    }
                }
        }

    private fun ASTNode.getEndOfBlock() =
        if (parent?.elementType == FUNCTION_LITERAL) {
            parent!!.findChildByType(RBRACE)
        } else {
            lastChildLeafOrSelf
                .let { node ->
                    if (node.elementType == RBRACE) {
                        // WHEN-entry block have LBRACE and RBRACE as first and last elements
                        node
                    } else {
                        // Other blocks have LBRACE and RBRACE as siblings of the block
                        node.nextCodeSibling
                    }
                }
        }

    private companion object {
        private val LTOKEN_SET = TokenSet.create(LPAR, LBRACE, LBRACKET, LT)
        private val RTOKEN_SET = TokenSet.create(RPAR, RBRACE, RBRACKET, GT)
        private val MATCHING_RTOKEN_MAP =
            LTOKEN_SET
                .types
                .zip(RTOKEN_SET.types)
                .toMap()
    }
}

public val WRAPPING_RULE_ID: RuleId = WrappingRule().ruleId
