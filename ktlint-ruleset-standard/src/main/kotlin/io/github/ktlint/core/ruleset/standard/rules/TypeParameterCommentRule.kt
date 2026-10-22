package io.github.ktlint.core.ruleset.standard.rules

import com.intellij.lang.ASTNode
import com.intellij.psi.tree.TokenSet
import io.github.ktlint.core.rule.engine.core.api.ElementType.BLOCK_COMMENT
import io.github.ktlint.core.rule.engine.core.api.ElementType.EOL_COMMENT
import io.github.ktlint.core.rule.engine.core.api.ElementType.TYPE_PARAMETER
import io.github.ktlint.core.rule.engine.core.api.ElementType.TYPE_PARAMETER_LIST
import io.github.ktlint.core.rule.engine.core.api.RuleId
import io.github.ktlint.core.rule.engine.core.api.SinceKtlint
import io.github.ktlint.core.rule.engine.core.api.SinceKtlint.Status.STABLE
import io.github.ktlint.core.rule.engine.core.api.isPartOfComment
import io.github.ktlint.core.rule.engine.core.api.isWhiteSpaceWithNewline
import io.github.ktlint.core.rule.engine.core.api.parent
import io.github.ktlint.core.rule.engine.core.api.prevLeaf
import io.github.ktlint.core.ruleset.standard.StandardRule

/**
 * The AST allows comments to be placed anywhere. This however can lead to code which is unnecessarily hard to read. Or, it makes
 * development of rules unnecessarily complex.
 *
 * This rule is based on the DiscouragedCommentLocationRule which is split in several distinct rules. In this way it becomes more clear why
 * another rule depends on this rule.
 */
@SinceKtlint("1.1", STABLE)
public class TypeParameterCommentRule : StandardRule("type-parameter-comment") {
    override fun beforeVisitChildNodes(node: ASTNode) {
        if (node.isPartOfComment && node.parent?.elementType in typeParameterTokenSet) {
            when (node.elementType) {
                EOL_COMMENT, BLOCK_COMMENT -> {
                    if (node.parent?.elementType == TYPE_PARAMETER) {
                        // Disallow:
                        //     class Foo<in /* some comment */ Bar>
                        // or
                        //     class Foo<in Bar /* some comment */>
                        emit(
                            node.startOffset,
                            "A (block or EOL) comment inside or on same line after a 'type_parameter' is not allowed. It may be placed " +
                                "on a separate line above.",
                        )
                    } else if (node.parent?.elementType == TYPE_PARAMETER_LIST) {
                        if (node.prevLeaf.isWhiteSpaceWithNewline) {
                            // Allow
                            //     class Foo<
                            //         /* some comment */
                            //         in Bar
                            //     >
                        } else {
                            // Disallow
                            //     class Foo<
                            //         in Bar, // some comment 1
                            //         // some comment 2
                            //         in Bar2,
                            //     >
                            // It is not clear whether "some comment 2" belongs to bar1 as a continuation of "some comment 1" or that it
                            // belongs to bar2. Note both comments are direct children of the value_argument_list.
                            emit(
                                node.startOffset,
                                "A comment in a 'value_argument_list' is only allowed when placed on a separate line",
                            )
                        }
                    }
                }
            }
        }
    }

    private companion object {
        val typeParameterTokenSet = TokenSet.create(TYPE_PARAMETER, TYPE_PARAMETER_LIST)
    }
}

public val TYPE_PARAMETER_COMMENT_RULE_ID: RuleId = TypeParameterCommentRule().ruleId
