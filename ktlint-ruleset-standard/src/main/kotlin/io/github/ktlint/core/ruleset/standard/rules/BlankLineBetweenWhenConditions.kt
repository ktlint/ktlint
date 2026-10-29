package io.github.ktlint.core.ruleset.standard.rules

import com.intellij.lang.ASTNode
import io.github.ktlint.core.rule.engine.core.api.ElementType.WHEN
import io.github.ktlint.core.rule.engine.core.api.ElementType.WHEN_ENTRY
import io.github.ktlint.core.rule.engine.core.api.RuleId
import io.github.ktlint.core.rule.engine.core.api.SinceKtlint
import io.github.ktlint.core.rule.engine.core.api.SinceKtlint.Status.EXPERIMENTAL
import io.github.ktlint.core.rule.engine.core.api.SinceKtlint.Status.STABLE
import io.github.ktlint.core.rule.engine.core.api.children
import io.github.ktlint.core.rule.engine.core.api.editorconfig.EditorConfig
import io.github.ktlint.core.rule.engine.core.api.editorconfig.EditorConfigProperty
import io.github.ktlint.core.rule.engine.core.api.indent
import io.github.ktlint.core.rule.engine.core.api.indentWithoutNewlinePrefix
import io.github.ktlint.core.rule.engine.core.api.isPartOfComment
import io.github.ktlint.core.rule.engine.core.api.isWhiteSpace
import io.github.ktlint.core.rule.engine.core.api.isWhiteSpaceWithNewline
import io.github.ktlint.core.rule.engine.core.api.lastChildLeafOrSelf
import io.github.ktlint.core.rule.engine.core.api.leavesForwardsIncludingSelf
import io.github.ktlint.core.rule.engine.core.api.nextLeaf
import io.github.ktlint.core.rule.engine.core.api.prevCodeLeaf
import io.github.ktlint.core.rule.engine.core.api.prevCodeSibling
import io.github.ktlint.core.rule.engine.core.api.prevSibling
import io.github.ktlint.core.rule.engine.core.api.upsertWhitespaceBeforeMe
import io.github.ktlint.core.ruleset.standard.StandardRule
import org.ec4j.core.model.PropertyType

/**
 * The Kotlin Coding Conventions suggest to consider using a blank line after a multiline when-condition
 * (https://kotlinlang.org/docs/coding-conventions.html#control-flow-statements) which behavior is managed via '.editorconfig' property
 * `ij_kotlin_line_break_after_multiline_when_entry`.
 *
 * Ktlint uses the property `ij_kotlin_line_break_after_multiline_when_entry` to consistently add/remove blank line between all
 * when-conditions in the when-statement depending on whether the statement contains at least one multiline when-condition.
 */
@SinceKtlint("1.2", EXPERIMENTAL)
@SinceKtlint("1.8", STABLE)
public class BlankLineBetweenWhenConditions :
    StandardRule(
        id = "blank-line-between-when-conditions",
        usesEditorConfigProperties = setOf(LINE_BREAK_AFTER_WHEN_CONDITION_PROPERTY),
    ) {
    private var lineBreakAfterWhenCondition = LINE_BREAK_AFTER_WHEN_CONDITION_PROPERTY.defaultValue

    override fun beforeFirstNode(editorConfig: EditorConfig) {
        lineBreakAfterWhenCondition = editorConfig[LINE_BREAK_AFTER_WHEN_CONDITION_PROPERTY]
    }

    override fun beforeVisitChildNodes(node: ASTNode) {
        if (node.elementType == WHEN) {
            node.visitWhenStatement()
        }
    }

    private fun ASTNode.visitWhenStatement() {
        val hasMultilineWhenCondition = hasAnyMultilineWhenCondition()
        if (hasMultilineWhenCondition && lineBreakAfterWhenCondition) {
            addBlankLinesBetweenWhenConditions()
        } else {
            removeBlankLinesBetweenWhenConditions()
        }
    }

    private fun ASTNode.addBlankLinesBetweenWhenConditions() {
        children
            .filter { it.elementType == WHEN_ENTRY }
            // Blank lines should only be added *between* when-conditions, so first when-condition is to be skipped
            .drop(1)
            .forEach { whenEntry ->
                whenEntry
                    .prevCodeLeaf
                    ?.leavesForwardsIncludingSelf
                    ?.takeWhile { !it.isWhiteSpaceWithNewline }
                    ?.last()
                    ?.nextLeaf
                    ?.takeUnless { it.containsBlankLine() }
                    ?.let { whitespaceBeforeWhenEntry ->
                        emit(
                            whitespaceBeforeWhenEntry.startOffset + 1,
                            "Add a blank line between all when-conditions in case at least one multiline when-condition is found in the statement",
                        ) {
                            whitespaceBeforeWhenEntry.upsertWhitespaceBeforeMe("\n${whenEntry.indent}")
                        }
                    }
            }
    }

    private fun ASTNode.containsBlankLine(): Boolean = isWhiteSpace && text.count { it == '\n' } > 1

    private fun ASTNode.hasAnyMultilineWhenCondition() =
        children
            .any { it.elementType == WHEN_ENTRY && (it.textContains('\n') || it.isPrecededByComment()) }

    private fun ASTNode.isPrecededByComment(): Boolean {
        // Check if this when-entry is preceded by a comment on its own line - not a trailing comment on the previous when-entry
        val prevNonWhitespace = prevSibling { !it.isWhiteSpace }
        if (prevNonWhitespace?.isPartOfComment != true) return false

        // Found a comment before this when-entry, so check whether it's on its own line
        val whitespaceBeforeComment = prevNonWhitespace.prevSibling { it.isWhiteSpace }
        return whitespaceBeforeComment?.text?.contains('\n') == true
    }

    private fun ASTNode.findWhitespaceAfterPreviousCodeSibling() =
        prevCodeSibling
            ?.lastChildLeafOrSelf
            ?.nextLeaf { it.isWhiteSpace }

    private fun ASTNode.removeBlankLinesBetweenWhenConditions() {
        children
            .filter { it.elementType == WHEN_ENTRY }
            // Blank lines should only be removed *between* when-conditions, so first when-condition is to be skipped
            .drop(1)
            .forEach { whenEntry ->
                whenEntry
                    .findWhitespaceAfterPreviousCodeSibling()
                    ?.takeIf { it.containsBlankLine() }
                    ?.let { whitespaceBeforeWhenEntry ->
                        emit(
                            whitespaceBeforeWhenEntry.startOffset + 1,
                            "Unexpected blank lines between when-condition if all when-conditions are single lines",
                        ) {
                            whitespaceBeforeWhenEntry.upsertWhitespaceBeforeMe("\n${whenEntry.indentWithoutNewlinePrefix}")
                        }
                    }
            }
    }

    public companion object {
        private val BOOLEAN_VALUES_SET = setOf("true", "false")

        public val LINE_BREAK_AFTER_WHEN_CONDITION_PROPERTY: EditorConfigProperty<Boolean> =
            EditorConfigProperty(
                type =
                    PropertyType.LowerCasingPropertyType(
                        "ij_kotlin_line_break_after_multiline_when_entry",
                        "Defines whether a blank line is to be added after a when entry. Contrary to default IDEA formatting, " +
                            "ktlint adds the blank line between all when-conditions if the when-statement contains at least one " +
                            "multiline when-condition. Or, it removes all blank lines between the when-conditions if the when-statement " +
                            "does not contain any multiline when-condition.",
                        PropertyType.PropertyValueParser.BOOLEAN_VALUE_PARSER,
                        BOOLEAN_VALUES_SET,
                    ),
                defaultValue = true,
            )
    }
}

public val BLANK_LINE_BETWEEN_WHEN_CONDITIONS_RULE_ID: RuleId = BlankLineBetweenWhenConditions().ruleId
