package io.github.ktlint.core.ruleset.standard.rules

import com.intellij.lang.ASTNode
import io.github.ktlint.core.rule.engine.core.api.ElementType.VALUE_ARGUMENT
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
public class ValueArgumentCommentRule : StandardRule("value-argument-comment") {
    override fun beforeVisitChildNodes(node: ASTNode) {
        if (node.isPartOfComment && node.parent?.elementType == VALUE_ARGUMENT) {
            // Disallow:
            //     val foo = foo(
            //         bar /* some comment */ = "bar"
            //     )
            // or
            //     val foo = foo(
            //         bar =
            //            // some comment
            //            "bar"
            //     )
            emit(
                node.startOffset,
                "A (block or EOL) comment inside or on same line after a 'value_argument' is not allowed. It may be placed " +
                    "on a separate line above.",
            )
        }
    }
}

public val VALUE_ARGUMENT_COMMENT_RULE_ID: RuleId = ValueArgumentCommentRule().ruleId
