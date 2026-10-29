package io.github.ktlint.core.ruleset.standard.rules

import com.intellij.lang.ASTNode
import io.github.ktlint.core.rule.engine.core.api.ElementType.ANNOTATION_ENTRY
import io.github.ktlint.core.rule.engine.core.api.ElementType.CLASS
import io.github.ktlint.core.rule.engine.core.api.ElementType.CLASS_BODY
import io.github.ktlint.core.rule.engine.core.api.ElementType.ENUM_ENTRY
import io.github.ktlint.core.rule.engine.core.api.ElementType.ENUM_KEYWORD
import io.github.ktlint.core.rule.engine.core.api.ElementType.MODIFIER_LIST
import io.github.ktlint.core.rule.engine.core.api.ElementType.RBRACE
import io.github.ktlint.core.rule.engine.core.api.IndentConfig
import io.github.ktlint.core.rule.engine.core.api.RuleId
import io.github.ktlint.core.rule.engine.core.api.SinceKtlint
import io.github.ktlint.core.rule.engine.core.api.SinceKtlint.Status.EXPERIMENTAL
import io.github.ktlint.core.rule.engine.core.api.SinceKtlint.Status.STABLE
import io.github.ktlint.core.rule.engine.core.api.children
import io.github.ktlint.core.rule.engine.core.api.editorconfig.EditorConfig
import io.github.ktlint.core.rule.engine.core.api.editorconfig.INDENT_SIZE_PROPERTY
import io.github.ktlint.core.rule.engine.core.api.editorconfig.INDENT_STYLE_PROPERTY
import io.github.ktlint.core.rule.engine.core.api.firstChildLeafOrSelf
import io.github.ktlint.core.rule.engine.core.api.hasModifier
import io.github.ktlint.core.rule.engine.core.api.isPartOfComment
import io.github.ktlint.core.rule.engine.core.api.isWhiteSpaceWithNewline
import io.github.ktlint.core.rule.engine.core.api.isWhiteSpaceWithoutNewline
import io.github.ktlint.core.rule.engine.core.api.leavesForwardsIncludingSelf
import io.github.ktlint.core.rule.engine.core.api.nextCodeSibling
import io.github.ktlint.core.rule.engine.core.api.nextSibling
import io.github.ktlint.core.rule.engine.core.api.prevLeaf
import io.github.ktlint.core.rule.engine.core.api.upsertWhitespaceAfterMe
import io.github.ktlint.core.rule.engine.core.api.upsertWhitespaceBeforeMe
import io.github.ktlint.core.ruleset.standard.StandardRule

@SinceKtlint("0.49", EXPERIMENTAL)
@SinceKtlint("1.0", STABLE)
public class EnumWrappingRule :
    StandardRule(
        id = "enum-wrapping",
        usesEditorConfigProperties =
            setOf(
                INDENT_SIZE_PROPERTY,
                INDENT_STYLE_PROPERTY,
            ),
    ) {
    private var indentConfig = IndentConfig.DEFAULT_INDENT_CONFIG

    override fun beforeFirstNode(editorConfig: EditorConfig) {
        indentConfig =
            IndentConfig(
                indentStyle = editorConfig[INDENT_STYLE_PROPERTY],
                tabWidth = editorConfig[INDENT_SIZE_PROPERTY],
            )
    }

    override fun beforeVisitChildNodes(node: ASTNode) {
        node
            .takeIf { node.elementType == CLASS }
            ?.takeIf { node.hasModifier(ENUM_KEYWORD) }
            ?.findChildByType(CLASS_BODY)
            ?.let { classBody ->
                visitEnumClass(classBody)
            }
    }

    private fun visitEnumClass(node: ASTNode) {
        require(node.elementType == CLASS_BODY)

        if (wrapCommentBeforeFirstEnumEntry(node) || node.needsWrapping()) {
            wrapEnumEntries(node)
            wrapClosingBrace(node)
        }
        addBlankLineBetweenEnumEntriesAndOtherDeclarations(node)
    }

    private fun wrapCommentBeforeFirstEnumEntry(node: ASTNode): Boolean {
        val firstEnumEntry = node.findChildByType(ENUM_ENTRY)?.firstChildLeafOrSelf
        if (firstEnumEntry != null) {
            node
                .firstChildLeafOrSelf
                .leavesForwardsIncludingSelf
                .takeWhile { it != firstEnumEntry }
                .firstOrNull { it.isPartOfComment }
                ?.let { commentBeforeFirstEnumEntry ->
                    val expectedIndent = indentConfig.childIndentOf(node)
                    if (commentBeforeFirstEnumEntry.prevLeaf?.text != expectedIndent) {
                        emit(node.startOffset, "Expected a (single) newline before comment") {
                            commentBeforeFirstEnumEntry.upsertWhitespaceBeforeMe(indentConfig.siblingIndentOf(node))
                        }
                        return true
                    }
                }
        }
        return false
    }

    private fun ASTNode.needsWrapping() = isMultiline() || hasAnnotatedEnumEntry() || hasCommentedEnumEntry()

    private fun ASTNode.isMultiline() = text.contains('\n')

    private fun ASTNode.hasAnnotatedEnumEntry() =
        children
            .filter { it.elementType == ENUM_ENTRY }
            .any { it.isAnnotated() }

    private fun ASTNode.isAnnotated(): Boolean =
        findChildByType(MODIFIER_LIST)
            ?.children
            .orEmpty()
            .any { it.elementType == ANNOTATION_ENTRY }

    private fun ASTNode.hasCommentedEnumEntry() = children.any { it.containsCommentInEnumEntry() }

    private fun ASTNode.containsCommentInEnumEntry() = children.any { it.isPartOfComment }

    private fun wrapEnumEntries(node: ASTNode) {
        node
            .children
            .filter { it.elementType == ENUM_ENTRY }
            .forEach { enumEntry ->
                wrapEnumEntry(enumEntry)
            }
    }

    private fun wrapEnumEntry(node: ASTNode) {
        node
            .prevLeaf { !it.isPartOfComment && !it.isWhiteSpaceWithoutNewline }
            ?.takeUnless { it.isWhiteSpaceWithNewline }
            ?.let { prevLeaf ->
                emit(node.startOffset, "Enum entry should start on a separate line") {
                    prevLeaf.upsertWhitespaceAfterMe(indentConfig.siblingIndentOf(node))
                }
            }
    }

    private fun wrapClosingBrace(node: ASTNode) {
        node
            .findChildByType(RBRACE)
            ?.let { rbrace ->
                val prevLeaf = rbrace.prevLeaf
                val expectedIndent = indentConfig.parentIndentOf(node)
                if (prevLeaf?.text != expectedIndent) {
                    emit(rbrace.startOffset, "Expected newline before '}'") {
                        rbrace.upsertWhitespaceBeforeMe(expectedIndent)
                    }
                }
            }
    }

    private fun addBlankLineBetweenEnumEntriesAndOtherDeclarations(node: ASTNode) {
        node
            .children
            .lastOrNull { it.elementType == ENUM_ENTRY }
            ?.nextSibling { !it.isPartOfComment }
            ?.takeUnless { it.nextCodeSibling?.elementType == RBRACE }
            ?.let { nextSibling ->
                val expectedIndent = "\n".plus(indentConfig.siblingIndentOf(node))
                if (nextSibling.text != expectedIndent) {
                    emit(nextSibling.startOffset + 1, "Expected blank line between enum entries and other declaration(s)") {
                        nextSibling.upsertWhitespaceBeforeMe(expectedIndent)
                    }
                }
            }
    }
}

public val ENUM_WRAPPING_RULE_ID: RuleId = EnumWrappingRule().ruleId
