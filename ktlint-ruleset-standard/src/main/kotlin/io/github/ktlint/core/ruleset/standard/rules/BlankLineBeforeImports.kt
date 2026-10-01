package io.github.ktlint.core.ruleset.standard.rules

import io.github.ktlint.core.rule.engine.core.api.AutocorrectDecision
import io.github.ktlint.core.rule.engine.core.api.ElementType.IMPORT_LIST
import io.github.ktlint.core.rule.engine.core.api.ElementType.PACKAGE_DIRECTIVE
import io.github.ktlint.core.rule.engine.core.api.RuleId
import io.github.ktlint.core.rule.engine.core.api.RuleV2
import io.github.ktlint.core.rule.engine.core.api.SinceKtlint
import io.github.ktlint.core.rule.engine.core.api.SinceKtlint.Status.EXPERIMENTAL
import io.github.ktlint.core.rule.engine.core.api.editorconfig.CODE_STYLE_PROPERTY
import io.github.ktlint.core.rule.engine.core.api.editorconfig.CodeStyleValue
import io.github.ktlint.core.rule.engine.core.api.editorconfig.EditorConfig
import io.github.ktlint.core.rule.engine.core.api.ifAutocorrectAllowed
import io.github.ktlint.core.rule.engine.core.api.isWhiteSpaceWithNewline
import io.github.ktlint.core.rule.engine.core.api.replaceTextWith
import io.github.ktlint.core.ruleset.standard.StandardRule
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.psi.psiUtil.siblings

/**
 * Insert a blank line before the imports list:
 * https://developer.android.com/kotlin/style-guide#structure
 */
@SinceKtlint("2.0", EXPERIMENTAL)
public class BlankLineBeforeImports :
    StandardRule("blank-line-before-imports"),
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
        node
            .takeIf { it.elementType == PACKAGE_DIRECTIVE }
            ?.siblings()
            ?.takeWhile { it.elementType != IMPORT_LIST }
            ?.firstOrNull { it.isWhiteSpaceWithNewline }
            ?.takeIf {
                // Only handle case when no blank line is found before import. When too many blank lines are found, this is to be handled by
                // the no-consecutive-blank-lines rule.
                whitespace ->
                whitespace.text.count { it == '\n' } == 1
            }?.let { whitespace ->
                emit(
                    whitespace.startOffset + 1,
                    "Expected a blank line before the import(s)",
                    true,
                ).ifAutocorrectAllowed { whitespace.replaceTextWith("\n\n") }
            }
    }
}

public val BLANK_LINE_BEFORE_IMPORTS_RULE_ID: RuleId = BlankLineBeforeImports().ruleId
