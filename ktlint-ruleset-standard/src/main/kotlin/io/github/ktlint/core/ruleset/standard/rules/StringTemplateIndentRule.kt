package io.github.ktlint.core.ruleset.standard.rules

import com.intellij.lang.ASTNode
import com.intellij.psi.impl.source.tree.LeafPsiElement
import io.github.ktlint.core.rule.engine.core.api.ElementType.BINARY_EXPRESSION
import io.github.ktlint.core.rule.engine.core.api.ElementType.CALL_EXPRESSION
import io.github.ktlint.core.rule.engine.core.api.ElementType.CLOSING_QUOTE
import io.github.ktlint.core.rule.engine.core.api.ElementType.COMMA
import io.github.ktlint.core.rule.engine.core.api.ElementType.DOT
import io.github.ktlint.core.rule.engine.core.api.ElementType.DOT_QUALIFIED_EXPRESSION
import io.github.ktlint.core.rule.engine.core.api.ElementType.EQ
import io.github.ktlint.core.rule.engine.core.api.ElementType.FUN
import io.github.ktlint.core.rule.engine.core.api.ElementType.LITERAL_STRING_TEMPLATE_ENTRY
import io.github.ktlint.core.rule.engine.core.api.ElementType.LONG_STRING_TEMPLATE_ENTRY
import io.github.ktlint.core.rule.engine.core.api.ElementType.OPEN_QUOTE
import io.github.ktlint.core.rule.engine.core.api.ElementType.OPERATION_REFERENCE
import io.github.ktlint.core.rule.engine.core.api.ElementType.PROPERTY
import io.github.ktlint.core.rule.engine.core.api.ElementType.REFERENCE_EXPRESSION
import io.github.ktlint.core.rule.engine.core.api.ElementType.REGULAR_STRING_PART
import io.github.ktlint.core.rule.engine.core.api.ElementType.RETURN_KEYWORD
import io.github.ktlint.core.rule.engine.core.api.ElementType.RPAR
import io.github.ktlint.core.rule.engine.core.api.ElementType.SHORT_STRING_TEMPLATE_ENTRY
import io.github.ktlint.core.rule.engine.core.api.ElementType.STRING_TEMPLATE
import io.github.ktlint.core.rule.engine.core.api.IndentConfig
import io.github.ktlint.core.rule.engine.core.api.KtlintKotlinCompiler
import io.github.ktlint.core.rule.engine.core.api.RuleId
import io.github.ktlint.core.rule.engine.core.api.RuleV2
import io.github.ktlint.core.rule.engine.core.api.SinceKtlint
import io.github.ktlint.core.rule.engine.core.api.SinceKtlint.Status.EXPERIMENTAL
import io.github.ktlint.core.rule.engine.core.api.SinceKtlint.Status.STABLE
import io.github.ktlint.core.rule.engine.core.api.children
import io.github.ktlint.core.rule.engine.core.api.editorconfig.EditorConfig
import io.github.ktlint.core.rule.engine.core.api.editorconfig.INDENT_SIZE_PROPERTY
import io.github.ktlint.core.rule.engine.core.api.editorconfig.INDENT_STYLE_PROPERTY
import io.github.ktlint.core.rule.engine.core.api.firstChildLeafOrSelf
import io.github.ktlint.core.rule.engine.core.api.isWhiteSpaceWithNewline
import io.github.ktlint.core.rule.engine.core.api.lastChildLeafOrSelf
import io.github.ktlint.core.rule.engine.core.api.nextCodeSibling
import io.github.ktlint.core.rule.engine.core.api.nextLeaf
import io.github.ktlint.core.rule.engine.core.api.nextSibling
import io.github.ktlint.core.rule.engine.core.api.parent
import io.github.ktlint.core.rule.engine.core.api.prevCodeLeaf
import io.github.ktlint.core.rule.engine.core.api.prevLeaf
import io.github.ktlint.core.rule.engine.core.api.prevSibling
import io.github.ktlint.core.rule.engine.core.api.replaceTextWith
import io.github.ktlint.core.rule.engine.core.api.upsertWhitespaceBeforeMe
import io.github.ktlint.core.ruleset.standard.StandardRule
import kotlin.properties.Delegates

@SinceKtlint("0.49", EXPERIMENTAL)
@SinceKtlint("1.0", STABLE)
public class StringTemplateIndentRule :
    StandardRule(
        id = "string-template-indent",
        usesEditorConfigProperties =
            setOf(
                INDENT_SIZE_PROPERTY,
                INDENT_STYLE_PROPERTY,
            ),
    ),
    RuleV2.OfficialCodeStyle {
    private lateinit var indentConfig: IndentConfig
    private lateinit var nextIndent: String
    private var wrongIndentChar by Delegates.notNull<Char>()
    private lateinit var wrongIndentDescription: String

    override fun beforeFirstNode(editorConfig: EditorConfig) {
        indentConfig =
            IndentConfig(
                indentStyle = editorConfig[INDENT_STYLE_PROPERTY],
                tabWidth = editorConfig[INDENT_SIZE_PROPERTY],
            )
        if (indentConfig.disabled) {
            return
        }

        nextIndent = indentConfig.indent
        when (indentConfig.indentStyle) {
            IndentConfig.IndentStyle.SPACE -> {
                wrongIndentChar = '\t'
                wrongIndentDescription = "tab"
            }

            IndentConfig.IndentStyle.TAB -> {
                wrongIndentChar = ' '
                wrongIndentDescription = "space"
            }
        }
    }

    override fun beforeVisitChildNodes(node: ASTNode) {
        if (node.isMultilineStringTemplateFollowedByTrimIndent()) {
            val stringTemplateContainsMixedIndentationCharacters = node.isPartOfStringTemplateContainingMixedIndentationCharacters()
            val newIndent = node.stringTemplateIndent()
            when {
                node.elementType == OPEN_QUOTE -> {
                    node.visitOpeningQuote()
                    if (!stringTemplateContainsMixedIndentationCharacters) {
                        node.checkAndFixNewLineAfterOpeningQuotes(newIndent)
                    }
                }

                node.nextSibling?.elementType == CLOSING_QUOTE -> {
                    if (!stringTemplateContainsMixedIndentationCharacters) {
                        node.checkAndFixNewLineBeforeClosingQuotes()
                    }
                }

                else -> {
                    node
                        .parent
                        ?.let { stringTemplate ->
                            require(stringTemplate.elementType == STRING_TEMPLATE)
                            if (stringTemplateContainsMixedIndentationCharacters) {
                                // It can not be determined with certainty how mixed indentation characters should be interpreted. The trimIndent
                                // function handles tabs and spaces equally (one tabs equals one space) while the user might expect that the tab
                                // size in the indentation is more than one space.
                                emit(
                                    stringTemplate.startOffset,
                                    "Indentation of multiline raw string literal should not contain both tab(s) and space(s)",
                                )
                            } else {
                                stringTemplate.visitStringTemplate(newIndent)
                            }
                        }
                }
            }
        }
        if (isTrimIndentAfterMultilineStringTemplate(node)) {
            node.visitTrimIndentAfterMultilineStringLiteral()
        }
    }

    private fun ASTNode.isMultilineStringTemplateFollowedByTrimIndent(): Boolean =
        takeIf {
            // A multi-dollar interpolation prefix precedes the opening quotes and must not normalize the margin first.
            elementType == OPEN_QUOTE || prevSibling { it.elementType == OPEN_QUOTE } != null
        }?.parent
            ?.takeIf { it.elementType == STRING_TEMPLATE }
            ?.let { it.containsLiteralStringTemplateEntryWithNewline() && it.isFollowedByTrimIndent() }
            ?: false

    private fun ASTNode.visitOpeningQuote() {
        require(elementType == OPEN_QUOTE)
        parent!!
            .takeUnless { it.isPrecededByWhitespaceWithNewline() }
            ?.takeUnless { it.isPrecededByReturnKeyword() }
            ?.takeUnless { it.isFunctionBodyExpressionOnSameLine() }
            ?.let { stringTemplate ->
                emit(startOffset, "Expected newline before multiline string template") {
                    stringTemplate.upsertWhitespaceBeforeMe(indentConfig.childIndentOf(stringTemplate.parent!!))
                }
            }
    }

    private fun isTrimIndentAfterMultilineStringTemplate(node: ASTNode): Boolean =
        node
            .takeIf { it.elementType == REFERENCE_EXPRESSION && it.text == "trimIndent" }
            ?.parent
            ?.prevSibling { it.elementType == STRING_TEMPLATE }
            ?.containsLiteralStringTemplateEntryWithNewline()
            ?: false

    private fun ASTNode.visitTrimIndentAfterMultilineStringLiteral() {
        require(elementType == REFERENCE_EXPRESSION)
        parent
            ?.takeIf { it.elementType == CALL_EXPRESSION }
            ?.lastChildLeafOrSelf
            ?.nextLeaf
            ?.takeUnless { it.isWhiteSpaceWithNewline }
            ?.takeUnless { it.elementType == COMMA }
            ?.takeUnless { it.parent?.elementType == DOT_QUALIFIED_EXPRESSION }
            ?.takeUnless {
                it.parent?.elementType == BINARY_EXPRESSION && it.nextSibling?.elementType == OPERATION_REFERENCE
            }?.let { nextLeaf ->
                emit(nextLeaf.startOffset, "Expected newline after 'trimIndent' in a multiline string template") {
                    nextLeaf.upsertWhitespaceBeforeMe(indentConfig.parentIndentOf(nextLeaf))
                }
            }
    }

    private fun ASTNode.isPrecededByWhitespaceWithNewline() = prevLeaf.isWhiteSpaceWithNewline

    private fun ASTNode.isPrecededByReturnKeyword() =
        // Allow below as otherwise it results in compilation failure:
        //   return """
        //       some string
        //       """
        prevCodeLeaf?.elementType == RETURN_KEYWORD

    private fun ASTNode.isFunctionBodyExpressionOnSameLine() =
        prevCodeLeaf
            ?.takeIf { it.elementType == EQ }
            ?.closingParenthesisOfFunctionOrNull()
            ?.prevLeaf
            ?.isWhiteSpaceWithNewline
            ?: false

    private fun ASTNode.closingParenthesisOfFunctionOrNull() =
        takeIf { parent?.elementType == FUN }
            ?.prevCodeLeaf
            ?.takeIf { it.elementType == RPAR }

    private fun ASTNode.stringTemplateIndent(): String {
        require(parent!!.elementType == STRING_TEMPLATE)
        // When executing this rule, the indentation rule may not have run yet. The functionality to determine the correct indentation level
        // is out of scope of this rule as it is owned by the indentation rule. Therefore, the indentation of the line at which the
        // string template is found, is assumed to be correct and is used to indent all lines of the string template. The indent will be
        // fixed once the indent rule is run as well.
        val firstWhiteSpaceLeafOnSameLine = parent!!.prevLeaf { it.isWhiteSpaceWithNewline }
        return if (parent!!.prevLeaf == firstWhiteSpaceLeafOnSameLine) {
            // The string template already is on a separate new line. Keep the current indent.
            firstWhiteSpaceLeafOnSameLine.getTextAfterLastNewline()
        } else {
            // String template is forced to a new line. So indent must be increased
            firstWhiteSpaceLeafOnSameLine.getTextAfterLastNewline() + nextIndent
        }
    }

    private fun ASTNode?.getTextAfterLastNewline() =
        this
            ?.text
            ?.split("\n")
            ?.last()
            ?: ""

    private fun ASTNode.isPartOfStringTemplateContainingMixedIndentationCharacters(): Boolean =
        parent
            ?.takeIf { it.elementType == STRING_TEMPLATE }
            ?.containsMixedIndentationCharacters()
            ?: false

    private fun ASTNode.containsMixedIndentationCharacters(): Boolean {
        require(containsLiteralStringTemplateEntryWithNewline())
        val nonBlankLines = getNonBlankLines()
        val prefixLength =
            nonBlankLines
                .minOfOrNull { it.indentLength() }
                ?: 0
        val distinctIndentCharacters =
            nonBlankLines
                .joinToString(separator = "") {
                    it.splitIndentAt(prefixLength).first
                }.toCharArray()
                .distinct()
                .count()
        return distinctIndentCharacters > 1
    }

    private fun ASTNode.getNonBlankLines(): List<String> {
        require(elementType == STRING_TEMPLATE)
        return text
            .substringAfter(RAW_STRING_LITERAL_QUOTES)
            .removeSuffix(RAW_STRING_LITERAL_QUOTES)
            .split("\n")
            .filterNot { it.isBlank() }
    }

    private fun ASTNode.visitStringTemplate(
        newIndent: String,
        wrapOpeningLine: Boolean = false,
    ) {
        require(elementType == STRING_TEMPLATE)
        val prefixLength = calculateCommonPrefixLength()
        children
            .filterNot {
                // Opening quotes are indented by IndentationRule
                it.elementType == OPEN_QUOTE
            }.filterNot {
                // Blank lines inside the string template should not be indented
                it.text == "\n"
            }.forEach {
                if (it.prevLeaf?.text == "\n" || (wrapOpeningLine && it.prevSibling?.elementType == OPEN_QUOTE)) {
                    val actualMarginIndent =
                        if (it.isIndentBeforeClosingQuote()) {
                            it.text
                        } else {
                            it.text.splitIndentAt(prefixLength).first
                        }
                    val newMarginIndent =
                        if (it.isIndentBeforeClosingQuote() || prefixLength > 0 || wrapOpeningLine) {
                            newIndent
                        } else {
                            ""
                        }
                    if (actualMarginIndent.contains(wrongIndentChar)) {
                        it.checkAndFixWrongIndentationChar(
                            newIndent = newMarginIndent,
                            newContent = it.text.drop(prefixLength),
                        )
                    } else if (actualMarginIndent != newMarginIndent || (wrapOpeningLine && it.prevSibling?.elementType == OPEN_QUOTE)) {
                        it.checkAndFixIndent(
                            wrapOpeningLine = wrapOpeningLine,
                            prefixLength = prefixLength,
                            newMarginIndent = newMarginIndent,
                        )
                    }
                }
            }
    }

    private fun ASTNode.calculateCommonPrefixLength(): Int =
        // Get the max prefix length that all lines in the multiline string have in common. All whitespace characters are counted as one
        // single position. Note that the way of counting should be in sync with the way this is done by the trimIndent function.
        getNonBlankLines().minOfOrNull { it.indentLength() } ?: 0

    private fun ASTNode.checkAndFixNewLineAfterOpeningQuotes(indent: String) {
        require(elementType == OPEN_QUOTE)
        if (nextLeaf?.text != "\n") {
            // Wrap content after opening quotes to new line. When this content is preceded by whitespace, then take this into consideration
            // when calculating the common indent of the entire raw string literal
            parent!!.visitStringTemplate(indent, wrapOpeningLine = true)
        }
    }

    private fun ASTNode.checkAndFixWrongIndentationChar(
        newIndent: String,
        newContent: String,
    ) {
        require(elementType == LITERAL_STRING_TEMPLATE_ENTRY)
        firstChildNode
            .also { require(it.elementType == REGULAR_STRING_PART) }
            ?.let { regularStringPart ->
                emit(
                    regularStringPart.startOffset + regularStringPart.text.indexOf(wrongIndentChar),
                    "Unexpected '$wrongIndentDescription' character(s) in margin of multiline string",
                ) {
                    regularStringPart.replaceTextWith(newIndent + newContent)
                }
            }
    }

    private fun ASTNode.checkAndFixIndent(
        wrapOpeningLine: Boolean,
        prefixLength: Int,
        newMarginIndent: String, // Common indent for all content lines in the string template
    ) {
        val contentWithoutIndent = this.text.dropWhile { it.isWhitespace() && it != wrongIndentChar }
        val oldIndent = this.text.takeWhile { it.isWhitespace() && it != wrongIndentChar }
        val newIndent =
            if (nextSibling?.elementType == CLOSING_QUOTE) {
                newMarginIndent
            } else {
                newMarginIndent + oldIndent.take(maxOf(0, oldIndent.length - prefixLength))
            }
        when {
            wrapOpeningLine && prevSibling?.elementType == OPEN_QUOTE -> {
                emit(
                    this.startOffset,
                    "Missing newline after opening quotes. Expected indent: ${newIndent.length}. Found: ${oldIndent.length}",
                ) {
                    insertLiteralStringTemplateEntryBeforeMe("\n")
                    fixStringTemplateEntryIndent(newMarginIndent, newIndent, contentWithoutIndent)
                }
            }

            this.elementType == CLOSING_QUOTE -> {
                emit(
                    this.startOffset,
                    "Unexpected indent before closing quotes of raw string literal. Expected: ${newIndent.length}. Found: ${oldIndent.length}",
                ) {
                    (this as LeafPsiElement).rawInsertBeforeMe(
                        LeafPsiElement(REGULAR_STRING_PART, newIndent),
                    )
                }
            }

            else -> {
                emit(
                    this.startOffset,
                    "Unexpected indent inside raw string literal. Expected: ${newIndent.length}. Found: ${oldIndent.length}",
                ) {
                    fixStringTemplateEntryIndent(newMarginIndent, newIndent, contentWithoutIndent)
                }
            }
        }
    }

    private fun ASTNode.fixStringTemplateEntryIndent(
        newMarginIndent: String,
        newIndent: String,
        contentWithoutIndent: String,
    ) {
        if (elementType == SHORT_STRING_TEMPLATE_ENTRY || elementType == LONG_STRING_TEMPLATE_ENTRY) {
            // Keep interpolation tokens intact when adding their indentation.
            takeIf { newMarginIndent != "" }?.insertLiteralStringTemplateEntryBeforeMe(newMarginIndent)
        } else {
            takeIf { text != newIndent + contentWithoutIndent }
                ?.firstChildLeafOrSelf
                ?.also { require(it.elementType == REGULAR_STRING_PART) }
                ?.replaceTextWith(newIndent + contentWithoutIndent)
        }
    }

    private fun ASTNode.insertLiteralStringTemplateEntryBeforeMe(text: String) {
        val literalStringTemplateEntry =
            KtlintKotlinCompiler
                .createASTNodeFromText(
                    """
                    val foo = ${'"'}${'"'}${'"'}$text${'"'}${'"'}${'"'}
                    """.trimIndent(),
                )?.findChildByType(PROPERTY)
                ?.findChildByType(STRING_TEMPLATE)
                ?.findChildByType(LITERAL_STRING_TEMPLATE_ENTRY)
                ?.takeIf { it.text == text }
                ?: error("Can not create LITERAL_STRING_TEMPLATE_ENTRY with text '$text'")
        parent?.addChild(literalStringTemplateEntry, this)
    }

    private fun ASTNode.checkAndFixNewLineBeforeClosingQuotes() {
        require(nextSibling?.elementType == CLOSING_QUOTE)
        takeIf { it.text.isNotBlank() }
            ?.let { lastNodeBeforeClosingQuotes ->
                emit(
                    lastNodeBeforeClosingQuotes.startOffset + lastNodeBeforeClosingQuotes.text.length,
                    "Missing newline before the closing quotes of the raw string literal",
                ) {
                    createEmptyMultilineRawStringTemplate()
                        .children
                        .filter { it.elementType == LITERAL_STRING_TEMPLATE_ENTRY }
                        .let { literalStringTemplateEntries ->
                            parent!!.addChildren(literalStringTemplateEntries.first(), literalStringTemplateEntries.last(), nextSibling)
                        }
                }
            }
    }

    private fun createEmptyMultilineRawStringTemplate(): ASTNode =
        KtlintKotlinCompiler
            .createASTNodeFromText(
                """
                val foo =
                    ${'"'}${'"'}${'"'}
                    ${'"'}${'"'}${'"'}
                """.trimIndent(),
            )?.findChildByType(PROPERTY)
            ?.findChildByType(STRING_TEMPLATE)
            ?: error("Cannot create an empty raw string template")

    private fun ASTNode.containsLiteralStringTemplateEntryWithNewline(): Boolean {
        require(elementType == STRING_TEMPLATE)
        return children
            .any { it.elementType == LITERAL_STRING_TEMPLATE_ENTRY && it.text == "\n" }
    }

    private fun ASTNode.isFollowedByTrimIndent(): Boolean {
        require(elementType == STRING_TEMPLATE)
        return nextSibling { it.elementType != DOT }
            .let { it?.elementType == CALL_EXPRESSION && it.text == "trimIndent()" }
    }

    private fun String.indentLength() =
        indexOfFirst { !it.isWhitespace() }
            .let { if (it == -1) length else it }

    /**
     * Splits the string at the given index or at the first non-white space character before that index. The returned pair
     * consists of the indentation and the second part contains the remainder. Note that the second part still can start
     * with whitespace characters in case the original strings starts with more white space characters than the requested
     * split index.
     */
    private fun String.splitIndentAt(index: Int): Pair<String, String> {
        require(index >= 0)
        val firstNonWhitespaceIndex =
            indexOfFirst { !it.isWhitespace() }
                .let {
                    if (it == -1) {
                        length
                    } else {
                        it
                    }
                }
        val safeIndex = kotlin.math.min(firstNonWhitespaceIndex, index)
        return Pair(
            first = take(safeIndex),
            second = substring(safeIndex),
        )
    }

    private fun ASTNode.isIndentBeforeClosingQuote() = text.isBlank() && nextCodeSibling?.elementType == CLOSING_QUOTE

    private companion object {
        const val RAW_STRING_LITERAL_QUOTES = "\"\"\""
    }
}

public val STRING_TEMPLATE_INDENT_RULE_ID: RuleId = StringTemplateIndentRule().ruleId
