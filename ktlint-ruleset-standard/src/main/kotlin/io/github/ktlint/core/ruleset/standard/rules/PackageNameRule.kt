package io.github.ktlint.core.ruleset.standard.rules

import com.intellij.lang.ASTNode
import io.github.ktlint.core.rule.engine.core.api.ElementType.DOT_QUALIFIED_EXPRESSION
import io.github.ktlint.core.rule.engine.core.api.ElementType.PACKAGE_DIRECTIVE
import io.github.ktlint.core.rule.engine.core.api.ElementType.REFERENCE_EXPRESSION
import io.github.ktlint.core.rule.engine.core.api.RuleId
import io.github.ktlint.core.rule.engine.core.api.SinceKtlint
import io.github.ktlint.core.rule.engine.core.api.SinceKtlint.Status.STABLE
import io.github.ktlint.core.rule.engine.core.api.nextCodeSibling
import io.github.ktlint.core.ruleset.standard.StandardRule
import io.github.ktlint.core.ruleset.standard.rules.internal.regExIgnoringDiacriticsAndStrokesOnLetters

/**
 * https://kotlinlang.org/docs/coding-conventions.html#naming-rules
 */
@SinceKtlint("0.25", STABLE)
public class PackageNameRule : StandardRule("package-name") {
    override fun beforeVisitChildNodes(node: ASTNode) {
        node
            .takeIf { node.elementType == PACKAGE_DIRECTIVE } // Reverted 'it' back to 'node'
            ?.firstChildNode
            ?.nextCodeSibling
            ?.takeIf { it.elementType == DOT_QUALIFIED_EXPRESSION || it.elementType == REFERENCE_EXPRESSION }
            ?.let { expression ->
                if (expression.text.contains('_')) {
                    emit(expression.startOffset, "Package name must not contain underscore")
                } else if (!expression.text.matches(VALID_PACKAGE_NAME_REGEXP)) {
                    emit(expression.startOffset, "Package name contains a disallowed character")
                }
            }
    }

    private companion object {
        val VALID_PACKAGE_NAME_REGEXP =
            "[a-z][a-zA-Z\\d]*(\\.[a-z][a-zA-Z\\d]*)*"
                .regExIgnoringDiacriticsAndStrokesOnLetters()
    }
}

public val PACKAGE_NAME_RULE_ID: RuleId = PackageNameRule().ruleId
