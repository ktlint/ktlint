package com.example.ktlint.api.consumer.rules

import com.intellij.lang.ASTNode
import io.github.ktlint.core.rule.engine.core.api.ElementType.VAR_KEYWORD
import io.github.ktlint.core.rule.engine.core.api.RuleId
import io.github.ktlint.core.rule.engine.core.api.RuleV2

public class NoVarRule :
    RuleV2(
        ruleId = RuleId("$CUSTOM_RULE_SET_ID:no-var"),
        about = About(),
    ) {
    override fun beforeVisitChildNodes(node: ASTNode) {
        if (node.elementType == VAR_KEYWORD) {
            emit(node.startOffset, "Unexpected var, use val instead")
            // In case that LintError can be autocorrected, use syntax below
            //   emit(node.startOffset, "Unexpected var, use val instead") {
            //       // Fix
            //   }
        }
    }
}
