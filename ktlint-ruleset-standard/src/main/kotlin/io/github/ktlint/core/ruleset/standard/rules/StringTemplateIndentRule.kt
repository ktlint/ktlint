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
import io.github.ktlint.core.rule.engine.core.api.ElementType.OPEN_QUOTE
import io.github.ktlint.core.rule.engine.core.api.ElementType.OPERATION_REFERENCE
import io.github.ktlint.core.rule.engine.core.api.ElementType.REGULAR_STRING_PART
import io.github.ktlint.core.rule.engine.core.api.ElementType.RETURN_KEYWORD
import io.github.ktlint.core.rule.engine.core.api.ElementType.RPAR
import io.github.ktlint.core.rule.engine.core.api.ElementType.STRING_TEMPLATE
import io.github.ktlint.core.rule.engine.core.api.IndentConfig
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
import io.github.ktlint.core.rule.engine.core.api.replaceTextWith
import io.github.ktlint.core.rule.engine.core.api.upsertWhitespaceBeforeMe
import io.github.ktlint.core.ruleset.standard.StandardRule

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
    private lateinit var wrongIndentChar: String
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
                wrongIndentChar = "\t"
                wrongIndentDescription = "tab"
            }

            IndentConfig.IndentStyle.TAB -> {
                wrongIndentChar = " "
                wrongIndentDescription = "space"
            }
        }
    }

    override fun beforeVisitChildNodes(node: ASTNode) {
        node
            .takeIf { it.elementType == STRING_TEMPLATE }
            ?.let { stringTemplate ->
                if (stringTemplate.containsLiteralStringTemplateEntryWithNewline() && stringTemplate.isFollowedByTrimIndent()) {
                    stringTemplate
                        .takeUnless { it.isPrecededByWhitespaceWithNewline() }
                        ?.takeUnless { it.isPrecededByReturnKeyword() }
                        ?.takeUnless { it.isFunctionBodyExpressionOnSameLine() }
                        ?.let { whiteSpace ->
                            emit(stringTemplate.startOffset, "Expected newline before multiline string template") {
                                whiteSpace.upsertWhitespaceBeforeMe(indentConfig.childIndentOf(whiteSpace.parent!!))
                            }
                        }
                    stringTemplate
                        .getFirstLeafAfterTrimIndent()
                        ?.takeUnless { it.isWhiteSpaceWithNewline }
                        ?.takeUnless { it.elementType == COMMA }
                        ?.takeUnless { it.parent?.elementType == DOT_QUALIFIED_EXPRESSION }
                        ?.takeUnless { it.parent?.elementType == BINARY_EXPRESSION && it.nextSibling?.elementType == OPERATION_REFERENCE }
                        ?.let { nextLeaf ->
                            emit(nextLeaf.startOffset, "Expected newline after multiline string template") {
                                nextLeaf.upsertWhitespaceBeforeMe(indentConfig.childIndentOf(stringTemplate.parent!!))
                            }
                        }

                    if (stringTemplate.containsMixedIndentationCharacters()) {
                        // It can not be determined with certainty how mixed indentation characters should be interpreted. The trimIndent
                        // function handles tabs and spaces equally (one tabs equals one space) while the user might expect that the tab
                        // size in the indentation is more than one space.
                        emit(
                            stringTemplate.startOffset,
                            "Indentation of multiline raw string literal should not contain both tab(s) and space(s)",
                        )
                        return
                    }

                    val indent = stringTemplate.getIndent()
                    indentStringTemplate(node, indent)
                }
            }
    }

    private fun ASTNode.getFirstLeafAfterTrimIndent() =
        takeIf { it.elementType == STRING_TEMPLATE }
            ?.takeIf { it.isFollowedByTrimIndent() }
            ?.parent
            ?.lastChildLeafOrSelf
            ?.nextLeaf

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

    private fun ASTNode.getIndent(): String {
        // When executing this rule, the indentation rule may not have run yet. The functionality to determine the correct indentation level
        // is out of scope of this rule as it is owned by the indentation rule. Therefore, the indentation of the line at which the
        // string template is found, is assumed to be correct and is used to indent all lines of the string template. The indent will be
        // fixed once the indent rule is run as well.
        val firstWhiteSpaceLeafOnSameLine = this.prevLeaf { it.isWhiteSpaceWithNewline }
        return if (prevLeaf == firstWhiteSpaceLeafOnSameLine) {
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

    private fun ASTNode.containsMixedIndentationCharacters(): Boolean {
        require(containsLiteralStringTemplateEntryWithNewline())
        val nonBlankLines = this.getNonBlankLines()
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
            .split("\n")
            .map { it.removePrefix(RAW_STRING_LITERAL_QUOTES) }
            .map { it.removeSuffix(RAW_STRING_LITERAL_QUOTES) }
            .filterNot { it.isBlank() }
    }

    private fun indentStringTemplate(
        node: ASTNode,
        newIndent: String,
    ) {
        // Get the max prefix length that all lines in the multiline string have in common. All whitespace characters are counted as
        // one single position. Note that the way of counting should be in sync with the way this is done by the trimIndent
        // function.
        val prefixLength =
            node
                .text
                .split("\n")
                .asSequence()
                .filterNot {
                    // For a multiline raw string literal it is very unlikely that text after the opening quotes do contain indentation
                    // characters (it really looks ugly). In such a case this text should be ignored when calculating the common prefix
                    // length as otherwise it is probably set to 0.
                    it.startsWith(RAW_STRING_LITERAL_QUOTES)
                }.map {
                    // Indentation before the closing quotes however is relevant to take into account
                    it.removeSuffix(RAW_STRING_LITERAL_QUOTES)
                }.filterNot { it.isBlank() }
                .minOfOrNull { it.indentLength() } ?: 0

        checkAndFixNewLineAfterOpeningQuotes(node, newIndent)

        node
            .children
            .filterNot { it.elementType == OPEN_QUOTE }
            .filterNot {
                // Blank lines inside the string template should not be indented
                it.text == "\n"
            }.forEach {
                if (it.prevLeaf?.text == "\n") {
                    val (currentIndent, currentContent) =
                        if (it.isIndentBeforeClosingQuote()) {
                            Pair(it.text, "")
                        } else {
                            it.text.splitIndentAt(prefixLength)
                        }
                    val expectedIndent =
                        if (it.isIndentBeforeClosingQuote() || prefixLength > 0) {
                            newIndent
                        } else {
                            ""
                        }
                    if (currentIndent.contains(wrongIndentChar)) {
                        checkAndFixWrongIndentationChar(
                            node = it,
                            oldIndent = currentIndent,
                            newIndent = expectedIndent,
                            newContent = currentContent,
                        )
                    } else if (currentIndent != expectedIndent) {
                        checkAndFixIndent(
                            node = it,
                            oldIndentLength = currentIndent.length,
                            newIndent = expectedIndent,
                            newContent = currentContent,
                        )
                    }
                }
            }

        checkAndFixNewLineBeforeClosingQuotes(node, newIndent)
    }

    private fun checkAndFixNewLineAfterOpeningQuotes(
        node: ASTNode,
        indent: String,
    ) {
        val firstNodeAfterOpeningQuotes =
            node
                // The string template can start with an INTERPOLATION_PREFIX (multi dollar string interpolation, see
                // https://kotlinlang.org/docs/strings.html#multi-dollar-string-interpolation), which is to be skipped.
                .findChildByType(OPEN_QUOTE)
                ?.nextLeaf
                ?: return
        if (firstNodeAfterOpeningQuotes.text.isNotBlank()) {
            emit(
                firstNodeAfterOpeningQuotes.startOffset + firstNodeAfterOpeningQuotes.text.length,
                "Missing newline after the opening quotes of the raw string literal",
            ) {
                (firstNodeAfterOpeningQuotes as LeafPsiElement).rawInsertBeforeMe(
                    LeafPsiElement(REGULAR_STRING_PART, "\n" + indent),
                )
            }
        }
    }

    private fun checkAndFixWrongIndentationChar(
        node: ASTNode,
        oldIndent: String,
        newIndent: String,
        newContent: String,
    ) {
        emit(
            node.startOffset + oldIndent.indexOf(wrongIndentChar),
            "Unexpected '$wrongIndentDescription' character(s) in margin of multiline string",
        ) {
            node.firstChildNode.replaceTextWith(newIndent + newContent)
        }
    }

    private fun checkAndFixIndent(
        node: ASTNode,
        oldIndentLength: Int,
        newIndent: String,
        newContent: String,
    ) {
        emit(node.startOffset + oldIndentLength, "Unexpected indent of raw string literal") {
            if (node.elementType == CLOSING_QUOTE) {
                (node as LeafPsiElement).rawInsertBeforeMe(
                    LeafPsiElement(REGULAR_STRING_PART, newIndent),
                )
            } else {
                node.firstChildLeafOrSelf.replaceTextWith(newIndent + newContent)
            }
        }
    }

    private fun checkAndFixNewLineBeforeClosingQuotes(
        node: ASTNode,
        indent: String,
    ) {
        val lastNodeBeforeClosingQuotes = node.lastChildNode.prevLeaf ?: return
        if (lastNodeBeforeClosingQuotes.text.isNotBlank()) {
            emit(
                lastNodeBeforeClosingQuotes.startOffset + lastNodeBeforeClosingQuotes.text.length,
                "Missing newline before the closing quotes of the raw string literal",
            ) {
                (lastNodeBeforeClosingQuotes as LeafPsiElement).rawInsertAfterMe(
                    LeafPsiElement(REGULAR_STRING_PART, "\n" + indent),
                )
            }
        }
    }

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
                        this.length
                    } else {
                        it
                    }
                }
        val safeIndex = kotlin.math.min(firstNonWhitespaceIndex, index)
        return Pair(
            first = this.take(safeIndex),
            second = this.substring(safeIndex),
        )
    }

    private fun ASTNode.isIndentBeforeClosingQuote() = text.isBlank() && nextCodeSibling?.elementType == CLOSING_QUOTE

    private companion object {
        const val RAW_STRING_LITERAL_QUOTES = "\"\"\""
    }
}

public val STRING_TEMPLATE_INDENT_RULE_ID: RuleId = StringTemplateIndentRule().ruleId
