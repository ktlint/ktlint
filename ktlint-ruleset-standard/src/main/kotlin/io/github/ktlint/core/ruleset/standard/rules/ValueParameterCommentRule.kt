package io.github.ktlint.core.ruleset.standard.rules

import com.intellij.lang.ASTNode
import io.github.ktlint.core.rule.engine.core.api.ElementType.KDOC
import io.github.ktlint.core.rule.engine.core.api.ElementType.VALUE_PARAMETER
import io.github.ktlint.core.rule.engine.core.api.RuleId
import io.github.ktlint.core.rule.engine.core.api.SinceKtlint
import io.github.ktlint.core.rule.engine.core.api.SinceKtlint.Status.STABLE
import io.github.ktlint.core.rule.engine.core.api.isPartOfComment
import io.github.ktlint.core.rule.engine.core.api.parent
import io.github.ktlint.core.ruleset.standard.StandardRule

/**
 * The AST allows comments to be placed anywhere. This however can lead to code which is unnecessarily hard to read. Or, it makes
 * development of rules unnecessarily complex.
 *
 * This rule is based on the DiscouragedCommentLocationRule which is split in several distinct rules. In this way it becomes more clear why
 * another rule depends on this rule.
 */
@SinceKtlint("1.1", STABLE)
public class ValueParameterCommentRule : StandardRule("value-parameter-comment") {
    override fun beforeVisitChildNodes(node: ASTNode) {
        if (node.isPartOfComment && node.parent?.elementType == VALUE_PARAMETER) {
            if (node.elementType == KDOC && node.parent?.firstChildNode == node) {
                // Allow KDoc to be the first element of a value parameter. EOL and block comments directly before a value parameter are
                // a child of the value parameter list, and as of that will never be the first child of the value.
                //     class Foo(
                //         /** some kdoc */
                //         bar = "bar"
                //     )
            } else {
                // Disallow:
                //     class Foo(
                //         bar /* some comment */ = "bar"
                //     )
                // or
                //     class Foo(
                //         bar =
                //            // some comment
                //            "bar"
                //     )
                emit(
                    node.startOffset,
                    "A comment inside or on same line after a 'value_parameter' is not allowed. It may be placed on a separate line " +
                        "above.",
                )
            }
        }
    }
}

public val VALUE_PARAMETER_COMMENT_RULE_ID: RuleId = ValueParameterCommentRule().ruleId
