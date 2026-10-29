package io.github.ktlint.core.ruleset.standard.rules

import com.intellij.lang.ASTNode
import io.github.ktlint.core.rule.engine.core.api.ElementType.ARROW
import io.github.ktlint.core.rule.engine.core.api.ElementType.BLOCK
import io.github.ktlint.core.rule.engine.core.api.ElementType.WHEN
import io.github.ktlint.core.rule.engine.core.api.ElementType.WHEN_ENTRY
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
import io.github.ktlint.core.rule.engine.core.api.isWhiteSpace
import io.github.ktlint.core.rule.engine.core.api.isWhiteSpaceWithNewline
import io.github.ktlint.core.rule.engine.core.api.leavesForwardsIncludingSelf
import io.github.ktlint.core.rule.engine.core.api.nextSibling
import io.github.ktlint.core.rule.engine.core.api.parent
import io.github.ktlint.core.rule.engine.core.api.prevSibling
import io.github.ktlint.core.ruleset.standard.StandardRule
import org.jetbrains.kotlin.psi.psiUtil.leaves
import org.jetbrains.kotlin.psi.psiUtil.siblings

/**
 * If any when condition is using curly braces, then all other when conditions should use braces as well.
 *
 * Braces are helpful for following reasons:
 *   - Bodies of the when-conditions are all aligned at same column position
 *   - Closing braces helps in separation the when-conditions
 */
@SinceKtlint("1.4", EXPERIMENTAL)
@SinceKtlint("1.8", STABLE)
public class WhenEntryBracing :
    StandardRule(
        id = "when-entry-bracing",
        usesEditorConfigProperties =
            setOf(
                INDENT_SIZE_PROPERTY,
                INDENT_STYLE_PROPERTY,
            ),
    ),
    RuleV2.OfficialCodeStyle {
    private var indentConfig = IndentConfig.DEFAULT_INDENT_CONFIG

    override fun beforeFirstNode(editorConfig: EditorConfig) {
        indentConfig =
            IndentConfig(
                indentStyle = editorConfig[INDENT_STYLE_PROPERTY],
                tabWidth = editorConfig[INDENT_SIZE_PROPERTY],
            )
    }

    override fun beforeVisitChildNodes(node: ASTNode) {
        if (node.elementType == WHEN) {
            node.visitWhenStatement()
        }
    }

    private fun ASTNode.visitWhenStatement() {
        if (hasAnyWhenEntryWithBlockAfterArrow() || hasAnyWhenEntryWithMultilineBody()) {
            addBracesToWhenEntry()
        }
    }

    private fun ASTNode.hasAnyWhenEntryWithBlockAfterArrow() = children.any { it.elementType == WHEN_ENTRY && it.hasBlockAfterArrow() }

    private fun ASTNode.hasBlockAfterArrow(): Boolean {
        require(elementType == WHEN_ENTRY)
        return findChildByType(ARROW)
            ?.siblings()
            .orEmpty()
            .any { it.elementType == BLOCK }
    }

    private fun ASTNode.hasAnyWhenEntryWithMultilineBody() = children.any { it.elementType == WHEN_ENTRY && it.hasMultilineBody() }

    private fun ASTNode.hasMultilineBody(): Boolean {
        require(elementType == WHEN_ENTRY)
        return findChildByType(ARROW)
            ?.siblings()
            .orEmpty()
            .any { it.isWhiteSpaceWithNewline }
    }

    private fun ASTNode.addBracesToWhenEntry() {
        children
            .filter { it.elementType == WHEN_ENTRY }
            .filter { !it.hasBlockAfterArrow() }
            .forEach { whenEntry ->
                whenEntry
                    .findChildByType(ARROW)
                    ?.let { arrow ->
                        val nonWhiteSpaceSibling = arrow.nextSibling { !it.isWhiteSpace } ?: arrow
                        emit(
                            nonWhiteSpaceSibling.startOffset,
                            "Body of when entry should be surrounded by braces if any when entry body is surrounded by braces " +
                                "or has a multiline body",
                        ) {
                            arrow.surroundWithBraces()
                        }
                    }
            }
    }

    private fun ASTNode.surroundWithBraces() {
        require(elementType == ARROW)
        val whenEntryIndent = indentConfig.parentIndentOf(this).removePrefix("\n")
        val whenEntry = parent!!
        val textBeforeArrow =
            whenEntry
                .firstChildLeafOrSelf
                .leavesForwardsIncludingSelf
                .takeWhile { it.elementType != ARROW }
                .joinToString(separator = "") { it.text }
                .trimEnd(' ')
        // Find the anchor node, e.g. the last node before the current when-entry which is not altered
        val siblingBeforeWhenEntry = whenEntry.prevSibling!!
        // Find the first leaf after the when-entry (including the EOL comment after it) that not has to be enclosed within the braces
        val stopLeaf =
            siblingBeforeWhenEntry
                .siblings()
                .takeWhile { !it.isWhiteSpaceWithNewline }
                .last()
                .nextSibling!!
                .firstChildLeafOrSelf
        val whenEntryNode =
            createWhenEntryNode(
                "$whenEntryIndent$textBeforeArrow -> {" +
                    // Replace the whitespaces (possibly this could be a proper indent) at the beginning of the body with an indent. In case
                    // the body was already a multiline statement, then the second and following lines should already be properly indented.
                    indentConfig.childIndentOf(this) +
                    leaves()
                        .dropWhile { it.isWhiteSpace }
                        .takeWhile { it != stopLeaf }
                        .joinToString(separator = "") { it.text } +
                    "\n$whenEntryIndent}",
            )
        // Remove the old when entry and if applicable the EOL-comment after it
        whenEntry.removeRange(whenEntry, stopLeaf)
        siblingBeforeWhenEntry
            .parent
            ?.run { addChild(whenEntryNode!!, stopLeaf) }
    }

    private fun createWhenEntryNode(whenEntry: String) =
        KtlintKotlinCompiler
            .createASTNodeFromText(
                """
                |when {
                |$whenEntry
                |}
                """.trimMargin(),
            )?.findChildByType(WHEN)
            ?.findChildByType(WHEN_ENTRY)
}

public val WHEN_ENTRY_BRACING_RULE_ID: RuleId = WhenEntryBracing().ruleId
