package io.github.ktlint.core.ruleset.standard.rules

import io.github.ktlint.core.rule.engine.core.api.AutocorrectDecision
import io.github.ktlint.core.rule.engine.core.api.ElementType.IMPORT_LIST
import io.github.ktlint.core.rule.engine.core.api.ElementType.PACKAGE_DIRECTIVE
import io.github.ktlint.core.rule.engine.core.api.ElementType.WHITE_SPACE
import io.github.ktlint.core.rule.engine.core.api.RuleId
import io.github.ktlint.core.rule.engine.core.api.RuleV2
import io.github.ktlint.core.rule.engine.core.api.SinceKtlint
import io.github.ktlint.core.rule.engine.core.api.SinceKtlint.Status.EXPERIMENTAL
import io.github.ktlint.core.rule.engine.core.api.editorconfig.CODE_STYLE_PROPERTY
import io.github.ktlint.core.rule.engine.core.api.editorconfig.CodeStyleValue
import io.github.ktlint.core.rule.engine.core.api.editorconfig.EditorConfig
import io.github.ktlint.core.rule.engine.core.api.ifAutocorrectAllowed
import io.github.ktlint.core.rule.engine.core.api.indent
import io.github.ktlint.core.rule.engine.core.api.prevLeaf
import io.github.ktlint.core.rule.engine.core.api.upsertWhitespaceBeforeMe
import io.github.ktlint.core.ruleset.standard.StandardRule
import org.jetbrains.kotlin.com.intellij.lang.ASTNode

/**
 * Insert a blank line before the package statement.
 */
@SinceKtlint("2.0", EXPERIMENTAL)
public class BlankLineBeforePackage :
    StandardRule("blank-line-before-package"),
    RuleV2.Experimental {
    override fun beforeFirstNode(editorConfig: EditorConfig) {
        if (editorConfig[CODE_STYLE_PROPERTY] == CodeStyleValue.INTELLIJ_IDEA) {
            stopTraversalOfAST()
        }
    }

    override fun beforeVisitChildNodes(
        node: ASTNode,
        emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> AutocorrectDecision,
    ) {
        when (node.elementType) {
            PACKAGE_DIRECTIVE -> {
                node
                    // Skip empty package directive (e.g. no package statement)
                    .takeUnless { it.firstChildNode == null }
                    ?.takeUnless { it.prevLeaf.isBlankLine() }
                    ?.let { insertBeforeNode ->
                        emit(insertBeforeNode.startOffset, "Expected a blank line before the package statement", true)
                            .ifAutocorrectAllowed {
                                insertBeforeNode.upsertWhitespaceBeforeMe("\n".plus(node.indent))
                            }
                    }
                stopTraversalOfAST()
            }

            IMPORT_LIST -> {
                stopTraversalOfAST()
            }
        }
    }

    private fun ASTNode?.isBlankLine() = this == null || (elementType == WHITE_SPACE && text.count { it == '\n' } > 1)
}

public val BLANK_LINE_BEFORE_PACKAGE_RULE_ID: RuleId = BlankLineBeforePackage().ruleId
