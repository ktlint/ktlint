package io.github.ktlint.core.ruleset.standard.rules

import com.intellij.lang.ASTNode
import com.intellij.psi.impl.source.tree.LeafPsiElement
import io.github.ktlint.core.rule.engine.core.api.ElementType.KDOC
import io.github.ktlint.core.rule.engine.core.api.ElementType.KDOC_END
import io.github.ktlint.core.rule.engine.core.api.ElementType.KDOC_LEADING_ASTERISK
import io.github.ktlint.core.rule.engine.core.api.ElementType.KDOC_SECTION
import io.github.ktlint.core.rule.engine.core.api.ElementType.KDOC_START
import io.github.ktlint.core.rule.engine.core.api.ElementType.KDOC_TEXT
import io.github.ktlint.core.rule.engine.core.api.ElementType.WHITE_SPACE
import io.github.ktlint.core.rule.engine.core.api.KtlintKotlinCompiler
import io.github.ktlint.core.rule.engine.core.api.RuleId
import io.github.ktlint.core.rule.engine.core.api.RuleV2
import io.github.ktlint.core.rule.engine.core.api.SinceKtlint
import io.github.ktlint.core.rule.engine.core.api.SinceKtlint.Status.EXPERIMENTAL
import io.github.ktlint.core.rule.engine.core.api.children
import io.github.ktlint.core.rule.engine.core.api.endOffset
import io.github.ktlint.core.rule.engine.core.api.firstChildLeafOrSelf
import io.github.ktlint.core.rule.engine.core.api.indentWithoutNewlinePrefix
import io.github.ktlint.core.rule.engine.core.api.isWhiteSpace
import io.github.ktlint.core.rule.engine.core.api.isWhiteSpaceWithNewline
import io.github.ktlint.core.rule.engine.core.api.lastChildLeafOrSelf
import io.github.ktlint.core.rule.engine.core.api.nextLeaf
import io.github.ktlint.core.rule.engine.core.api.nextSibling
import io.github.ktlint.core.rule.engine.core.api.parent
import io.github.ktlint.core.rule.engine.core.api.prevLeaf
import io.github.ktlint.core.rule.engine.core.api.prevSibling
import io.github.ktlint.core.rule.engine.core.api.remove
import io.github.ktlint.core.rule.engine.core.api.replaceTextWith
import io.github.ktlint.core.rule.engine.core.api.upsertWhitespaceBeforeMe
import io.github.ktlint.core.ruleset.standard.StandardRule

/**
 * Checks that a KDoc comment is delimited consistently:
 * - A single-line KDoc comment starts and ends with the KDoc delimiters, each separated from the content by exactly one space.
 * - A multi-line KDoc comment starts with a line containing only the KDoc opening delimiter, ends with a line containing only the KDoc
 *   closing delimiter, and every other line starts with a leading asterisk followed by a single space (or by nothing, for an otherwise
 *   empty line), where the leading asterisk is indented one space more than the opening delimiter. The line directly after the opening
 *   delimiter, and the line directly before the closing delimiter, are not allowed to be empty.
 * - An empty (or blank) KDoc comment is not allowed.
 *
 * Autocorrect is only applied to whitespace and delimiters. It is never applied when doing so could change the meaning, or the visual
 * indentation, of the actual content of the KDoc comment. Most notably, a continuation line which is missing its leading asterisk is always
 * reported without autocorrect, since inserting the leading asterisk could change the intended visual indentation of that content.
 */
@SinceKtlint("2.0", EXPERIMENTAL)
public class KdocDelimiterRule :
    StandardRule("kdoc-delimiter"),
    RuleV2.OfficialCodeStyle,
    RuleV2.Experimental {
    override fun beforeVisitChildNodes(node: ASTNode) {
        when (node.elementType) {
            KDOC_START -> {
                if (!node.isPartOfMultiLineKdoc()) {
                    node.visitKdocStartInSingleLineKdoc()
                }
            }

            KDOC_SECTION -> {
                if (node.isEmptyOrBlank()) {
                    // Nothing but whitespace and/or (empty) leading asterisks between the opening and closing delimiter,
                    // e.g. `/***/`, `/** */`, `/**\n */` or `/**\n *\n */`.
                    // Although it could be removed automatically, it is likely that the developer had an intent to write a KDoc.
                    emit(node.treeParent.findChildByType(KDOC_START)!!.endOffset, "An empty KDoc comment is not allowed")
                }
            }

            KDOC_LEADING_ASTERISK -> {
                if (node.parent { it.elementType == KDOC_SECTION }!!.isEmptyOrBlank()) {
                    // Ignore multiline empty KDoc
                    //    /**
                    //     *
                    //     */
                    // Note that a non auto-correctable error is reported for the empty KDoc. Removing the empty line, but not removing the
                    // KDOC_START and KDOC_END is confusing.
                } else {
                    node.visitLeadingAsterisk()
                }
            }

            KDOC_TEXT -> {
                if (node.isPartOfMultiLineKdoc()) {
                    node.visitKdocTextInMultiLineKdoc()
                } else {
                    node.visitKdocTextInSingleLineKdoc()
                }
            }

            KDOC_END -> {
                if (node.isPartOfMultiLineKdoc()) {
                    node.visitKdocEndInMultiLineKdoc()
                } else {
                    node.visitKdocEndInSingleLineKdoc()
                }
            }
        }
    }

    private fun ASTNode.isPartOfMultiLineKdoc(): Boolean = parent { it.elementType == KDOC }?.textContains('\n') ?: false

    private fun ASTNode.visitKdocStartInSingleLineKdoc() {
        require(elementType == KDOC_START)
        nextLeaf
            ?.takeUnless { it.elementType == KDOC_END }
            ?.let { leafAfterKdocStart ->
                leafAfterKdocStart
                    .text
                    .takeWhile { it == ' ' }
                    .takeUnless { it.length == 1 }
                    ?.let {
                        emit(leafAfterKdocStart.startOffset, "Expected a single space after '/**' in a single-line KDoc comment") {
                            leafAfterKdocStart.replaceKDocText(' ' + leafAfterKdocStart.text.trimStart(' '))
                        }
                    }
            }
    }

    // Checks whether the KDOC_SECTION contains any content except for whitespaces and leading asterisks.
    private fun ASTNode.isEmptyOrBlank(): Boolean {
        require(elementType == KDOC_SECTION)
        return children.all {
            it.elementType == WHITE_SPACE ||
                it.elementType == KDOC_LEADING_ASTERISK ||
                (it.elementType == KDOC_TEXT && it.text.isBlank())
        }
    }

    private fun ASTNode.visitLeadingAsterisk() {
        require(elementType == KDOC_LEADING_ASTERISK)

        if (checkEmptyLineAfterOpeningDelimiter()) {
            // Remainder of checks (improper indentation, and context after asterisk) are not relevant when the line is (to be) removed.
            return
        }
        if (checkEmptyLineBeforeClosingDelimiter()) {
            // Remainder of checks (improper indentation, and context after asterisk) are not relevant when the line is (to be) removed.
            return
        }
        checkIndentationBeforeLeadingAsterisk()
        checkContentAfterLeadingAsterisk()
        checkConsecutiveBlankLines()
    }

    private fun ASTNode.checkEmptyLineAfterOpeningDelimiter(): Boolean {
        require(elementType == KDOC_LEADING_ASTERISK)

        val kdocSection = parent { it.elementType == KDOC_SECTION }
        takeIf { this == kdocSection?.firstChildLeafOrSelf }
            ?.nextLeaf
            ?.takeIf { it.isWhiteSpaceWithNewline }
            ?.let { whitespaceAfterLeadingAsterisk ->
                emit(startOffset, "No empty line expected after opening delimiter") {
                    whitespaceAfterLeadingAsterisk.remove()
                    remove()
                }
                // Remainder of checks (improper indentation, and context after asterisk) are not relevant when the line is (to be)
                // removed.
                return true
            }

        return false
    }

    private fun ASTNode.checkEmptyLineBeforeClosingDelimiter(): Boolean {
        require(elementType == KDOC_LEADING_ASTERISK)

        val kdocSection = parent { it.elementType == KDOC_SECTION }
        takeIf { this == kdocSection?.lastChildLeafOrSelf }
            ?.prevLeaf
            ?.takeIf { it.isWhiteSpaceWithNewline }
            ?.let { whitespaceAfterLeadingAsterisk ->
                emit(startOffset, "No empty line expected before closing delimiter") {
                    whitespaceAfterLeadingAsterisk.remove()
                    remove()
                }
                // Remainder of checks (improper indentation, and context after asterisk) are not relevant when the line is (to be)
                // removed.
                return true
            }

        return false
    }

    private fun ASTNode.checkIndentationBeforeLeadingAsterisk() {
        require(elementType == KDOC_LEADING_ASTERISK)
        val actualIndent =
            prevLeaf { it.isWhiteSpace }
                ?.text
                .orEmpty()
                .substringAfterLast('\n')
        val expectedContinuationIndent = parent { it.elementType == KDOC }?.indentWithoutNewlinePrefix.orEmpty() + " "
        if (actualIndent != expectedContinuationIndent) {
            // Given code sample below, the lines for "option 1" and "option 2" below do start with an actual leading asterisk but
            // are not properly indented. However, the intent of the developer can not be determined here with certainty.
            //    /**
            //     * However, in case below:
            //        * option 1
            //        * option 2
            //     * following applies: some more text.
            //     */
            // A possible intent could be that it should have been formatted as:
            //    /**
            //     * However, in case below:
            //     *   * option 1
            //     *   * option 2
            //     * following applies: some more text.
            //     */
            emit(startOffset, "Leading asterisk is not properly aligned with the first asterisk of the KDoc opening '/**'")
        }
    }

    private fun ASTNode.checkContentAfterLeadingAsterisk() {
        require(elementType == KDOC_LEADING_ASTERISK)
        nextSibling
            // Ignore leading asterisk when it is the last element in the KDoc section. There is a separate message for case.
            ?.let { nextSibling ->
                when {
                    nextSibling.text.isBlank() -> {
                        // A blank content leaf directly following the leading asterisk is either the mandatory single space before more
                        // content on the same line (e.g. before a KDOC_TAG), or the trailing whitespace of an otherwise empty line. Only
                        // the latter is a violation, which is the case when nothing but a line break or the end of the KDoc comment
                        // follows.
                        if (nextSibling.nextLeaf!!.isWhiteSpaceWithNewline) {
                            emit(
                                startOffset + textLength,
                                "An empty line in a KDoc comment should not contain trailing whitespace after the leading asterisk",
                            ) { nextSibling.remove() }
                        }
                    }

                    !nextSibling.text.startsWith(" ") -> {
                        emit(
                            startOffset + textLength,
                            "A leading asterisk in a KDoc comment should be followed by a single space",
                        ) { nextSibling.upsertWhitespaceBeforeMe(" ") }
                    }
                }
            }
    }

    private fun ASTNode.checkConsecutiveBlankLines() {
        require(elementType == KDOC_LEADING_ASTERISK)
        takeIf { it.nextLeaf.isWhiteSpaceWithNewline && it.prevLeaf.isWhiteSpaceWithNewline }
            ?.prevSibling { !it.isWhiteSpace }
            ?.takeIf { it.elementType == KDOC_LEADING_ASTERISK }
            ?.let {
                emit(startOffset, "Unexpected consecutive blank line") {
                    prevSibling.takeIf { it.isWhiteSpace }?.remove()
                    remove()
                }
            }
    }

    private fun ASTNode.visitKdocTextInMultiLineKdoc() {
        require(elementType == KDOC_TEXT)
        if (isFirstKdocTextInKdocSection()) {
            val kdocStart = parent { it.elementType == KDOC }!!.findChildByType(KDOC_START)!!
            emit(
                kdocStart.endOffset,
                "Opening '/**' of a multi-line KDoc comment should not be followed by any other text on the same line",
            )
        } else {
            val hasKdocLeadingAsteriskOnSameLine =
                prevLeaf { it.isWhiteSpaceWithNewline || it.elementType == KDOC_LEADING_ASTERISK || it.elementType == KDOC_START }
                    .isWhiteSpaceWithNewline
            if (hasKdocLeadingAsteriskOnSameLine) {
                emit(startOffset, "Each line of a multi-line KDoc comment should start with a leading asterisk")
            }
        }
    }

    private fun ASTNode.isFirstKdocTextInKdocSection(): Boolean {
        require(elementType == KDOC_TEXT)
        return this == parent { it.elementType == KDOC_SECTION }!!.firstChildNode
    }

    private fun ASTNode.visitKdocTextInSingleLineKdoc() {
        require(elementType == KDOC_TEXT)

        nextLeaf
            ?.takeIf { it.elementType == KDOC_END && it.text.endsWith("**/") }
            ?.let { kdocEnd ->
                // The Kotlin parser adds all asterisks, but no other characters, directly preceding the closing delimiter '*/' to the
                // KDOC_END token. Ktlint considers those to be part of the actual content. For example the KDoc `/***Foo***/` is parsed
                // into following three tokens:
                // - KDOC_START = `/**`
                // - KDOC_SECTION = `*Foo`
                // - KDOC_END = `***/`
                // Those extra leading asterisks are actual content (typically the closing asterisk of Markdown emphasis) and are treated as
                // such below.
                val extraClosingAsterisks = kdocEnd.text.dropLast(VALID_KDOC_END.length)
                if (extraClosingAsterisks == "*") {
                    // Check whether additional asterisk is the closing asterisk of a Markdown section
                    if (text.lastOrNull() != ' ' && text.count { it == '*' }.mod(2) == 1) {
                        // When an uneven number of asterisk are found in the node, the additional star in the closing delimiter is most likely
                        // the closing asterisk of a Markdown section.
                        // Fixes /** Text *emphasized**/ to /** Text *emphasized* */
                        // Fixes /** Text *emphasized1* or *emphasized2**/ to /** Text *emphasized1* or *emphasize2* */
                        emit(
                            kdocEnd.endOffset - VALID_KDOC_END.length,
                            "Expected a single space before '*/' in a single-line KDoc comment",
                        ) {
                            replaceKDocText(this.text.trimEnd(' ') + "$extraClosingAsterisks ")
                        }
                    } else {
                        // The additional asterisk is unneeded and should not be moved to KDOC_TEXT
                        Unit
                    }
                } else {
                    val kdocSectionContent = text + extraClosingAsterisks
                    kdocSectionContent
                        .takeLastWhile { it == ' ' }
                        .takeUnless { it.length == 1 }
                        ?.let {
                            emit(
                                kdocEnd.endOffset - VALID_KDOC_END.length,
                                "Expected a single space before '*/' in a single-line KDoc comment",
                            ) {
                                replaceKDocText(this.text.trimEnd(' ') + "$extraClosingAsterisks ")
                            }
                        }
                }
            }
    }

    private fun ASTNode.replaceKDocText(text: String) {
        require(elementType == KDOC_TEXT)
        val newKdocText =
            KtlintKotlinCompiler
                .createASTNodeFromText(
                    """
                    /**$text*/
                    """.trimIndent(),
                )?.findChildByType(KDOC)
                ?.findChildByType(KDOC_SECTION)
                ?.findChildByType(KDOC_TEXT)
                ?: error("Cannot create Kdoc")
        parent!!.replaceChild(this, newKdocText)
    }

    private fun ASTNode.visitKdocEndInMultiLineKdoc() {
        require(elementType == KDOC_END)
        val siblingBeforeKdocEnd = prevSibling!!
        when {
            siblingBeforeKdocEnd.elementType == KDOC_SECTION &&
                siblingBeforeKdocEnd.lastChildLeafOrSelf.elementType == KDOC_TEXT &&
                siblingBeforeKdocEnd.lastChildLeafOrSelf.text.isBlank() &&
                siblingBeforeKdocEnd.lastChildLeafOrSelf.prevSibling?.elementType == KDOC_LEADING_ASTERISK -> {
                // Remove leading asterisk before closing delimiter when on same line only
                //     /**
                //      * Some text
                //      * */
                siblingBeforeKdocEnd
                    .lastChildLeafOrSelf
                    .let { kdocText ->
                        emit(kdocText.prevSibling!!.startOffset, "Unexpected leading asterisk followed by Closing '*/' om same line") {
                            kdocText.prevSibling?.remove()
                            kdocText.remove()
                        }
                    }
            }

            siblingBeforeKdocEnd.elementType == KDOC_SECTION && siblingBeforeKdocEnd.text.isNotBlank() -> {
                emit(
                    startOffset,
                    "Closing '*/' of a multi-line KDoc comment should not be preceded by any other text on the same line",
                )
            }

            siblingBeforeKdocEnd.isWhiteSpaceWithNewline -> {
                val actualIndent = siblingBeforeKdocEnd.text.substringAfterLast('\n')
                val expectedIndent = treeParent.indentWithoutNewlinePrefix + " "
                if (actualIndent != expectedIndent) {
                    val prefixLength = siblingBeforeKdocEnd.text.length - actualIndent.length
                    emit(
                        siblingBeforeKdocEnd.startOffset + prefixLength,
                        "Closing '*/' should align with the leading asterisks of the KDoc comment",
                    ) {
                        siblingBeforeKdocEnd.replaceTextWith(siblingBeforeKdocEnd.text.substring(0, prefixLength) + expectedIndent)
                    }
                }
            }
        }
    }

    private fun ASTNode.visitKdocEndInSingleLineKdoc() {
        require(elementType == KDOC_END)

        // Ignore empty single line KDOC: `/***/`
        if (prevLeaf?.elementType == KDOC_START) return

        if (text == "*/") {
            prevSibling!!
                .text
                .takeLastWhile { it == ' ' }
                .takeUnless { it.length == 1 }
                ?.let {
                    emit(startOffset, "Expected a single space before '*/' in a single-line KDoc comment") {
                        prevSibling!!
                            .lastChildLeafOrSelf
                            .let { kdocText -> kdocText.replaceKDocText(kdocText.text.trimEnd(' ') + " ") }
                    }
                }
        } else {
            emit(startOffset, "Unexpected '*' before closing delimiter '*/' in a single-line KDoc comment") {
                (this as LeafPsiElement).rawReplaceWithText(VALID_KDOC_END)
            }
        }
    }

    private companion object {
        const val VALID_KDOC_END = "*/"
    }
}

public val KDOC_DELIMITER_RULE_ID: RuleId = KdocDelimiterRule().ruleId
