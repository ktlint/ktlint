package io.github.ktlint.core.ruleset.standard.rules

import com.intellij.lang.ASTNode
import com.intellij.psi.tree.TokenSet
import io.github.ktlint.core.rule.engine.core.api.ElementType.CLASS
import io.github.ktlint.core.rule.engine.core.api.ElementType.ENUM_ENTRY
import io.github.ktlint.core.rule.engine.core.api.ElementType.FILE
import io.github.ktlint.core.rule.engine.core.api.ElementType.FUN
import io.github.ktlint.core.rule.engine.core.api.ElementType.KDOC
import io.github.ktlint.core.rule.engine.core.api.ElementType.OBJECT_DECLARATION
import io.github.ktlint.core.rule.engine.core.api.ElementType.PROPERTY
import io.github.ktlint.core.rule.engine.core.api.ElementType.SECONDARY_CONSTRUCTOR
import io.github.ktlint.core.rule.engine.core.api.ElementType.TYPEALIAS
import io.github.ktlint.core.rule.engine.core.api.ElementType.VALUE_PARAMETER
import io.github.ktlint.core.rule.engine.core.api.RuleId
import io.github.ktlint.core.rule.engine.core.api.SinceKtlint
import io.github.ktlint.core.rule.engine.core.api.SinceKtlint.Status.EXPERIMENTAL
import io.github.ktlint.core.rule.engine.core.api.SinceKtlint.Status.STABLE
import io.github.ktlint.core.rule.engine.core.api.editorconfig.INDENT_SIZE_PROPERTY
import io.github.ktlint.core.rule.engine.core.api.editorconfig.INDENT_STYLE_PROPERTY
import io.github.ktlint.core.rule.engine.core.api.parent
import io.github.ktlint.core.ruleset.standard.StandardRule

/**
 * Disallow KDoc except of classes, functions and xxx
 */
@SinceKtlint("1.2", EXPERIMENTAL)
@SinceKtlint("1.8", STABLE)
public class KdocRule :
    StandardRule(
        id = "kdoc",
        usesEditorConfigProperties =
            setOf(
                INDENT_SIZE_PROPERTY,
                INDENT_STYLE_PROPERTY,
            ),
    ) {
    override fun beforeVisitChildNodes(node: ASTNode) {
        node
            .takeIf { it.elementType == KDOC }
            ?.parent
            ?.let { parent ->
                if (parent.elementType in allowedParentElementTypes) {
                    if (parent.firstChildNode != node) {
                        emit(node.startOffset, "A KDoc is allowed only at start of '${parent.elementType.debugName.lowercase()}'")
                    }
                } else if (parent.elementType == FILE) {
                    emit(node.startOffset, "A dangling toplevel KDoc is not allowed")
                } else {
                    emit(node.startOffset, "A KDoc is not allowed inside '${parent.elementType.debugName.lowercase()}'")
                }
            }
    }

    private companion object {
        val allowedParentElementTypes =
            TokenSet.create(
                CLASS,
                ENUM_ENTRY,
                FUN,
                OBJECT_DECLARATION,
                PROPERTY,
                SECONDARY_CONSTRUCTOR,
                TYPEALIAS,
                VALUE_PARAMETER,
            )
    }
}

public val KDOC_RULE_ID: RuleId = KdocRule().ruleId
