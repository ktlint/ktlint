package io.github.ktlint.core.ruleset.standard.rules

import com.intellij.lang.ASTNode
import io.github.ktlint.core.rule.engine.core.api.ElementType.IMPORT_LIST
import io.github.ktlint.core.rule.engine.core.api.ElementType.PACKAGE_DIRECTIVE
import io.github.ktlint.core.rule.engine.core.api.ElementType.SCRIPT
import io.github.ktlint.core.rule.engine.core.api.RuleId
import io.github.ktlint.core.rule.engine.core.api.SinceKtlint
import io.github.ktlint.core.rule.engine.core.api.SinceKtlint.Status.EXPERIMENTAL
import io.github.ktlint.core.rule.engine.core.api.SinceKtlint.Status.STABLE
import io.github.ktlint.core.rule.engine.core.api.children
import io.github.ktlint.core.rule.engine.core.api.isCode
import io.github.ktlint.core.rule.engine.core.api.isRoot
import io.github.ktlint.core.ruleset.standard.StandardRule

@SinceKtlint("0.50", EXPERIMENTAL)
@SinceKtlint("1.0", STABLE)
public class NoEmptyFileRule : StandardRule(id = "no-empty-file") {
    override fun beforeVisitChildNodes(node: ASTNode) {
        node
            .takeIf { it.isRoot }
            ?.takeIf { it.isEmptyFile() }
            ?.let { emit(0, "File '${node.getFileName()}' should not be empty") }
    }

    private fun ASTNode.getFileName() =
        psi
            .containingFile
            .virtualFile
            .name
            .replace("\\", "/") // Ensure compatibility with Windows OS
            .substringAfterLast("/")

    private fun ASTNode.isEmptyFile(): Boolean =
        null ==
            children
                .firstOrNull {
                    it.isCode &&
                        it.elementType != PACKAGE_DIRECTIVE &&
                        it.elementType != IMPORT_LIST &&
                        !(it.elementType == SCRIPT && it.text.isBlank())
                }
}

public val NO_EMPTY_FILE_RULE_ID: RuleId = NoEmptyFileRule().ruleId
