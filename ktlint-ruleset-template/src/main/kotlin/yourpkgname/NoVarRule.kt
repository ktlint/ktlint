package yourpkgname

import com.intellij.lang.ASTNode
import io.github.ktlint.core.rule.engine.core.api.ElementType.VAR_KEYWORD
import io.github.ktlint.core.rule.engine.core.api.RuleId
import io.github.ktlint.core.rule.engine.core.api.RuleV2

public class NoVarRule :
    RuleV2(
        ruleId = RuleId("$CUSTOM_RULE_SET_ID:no-var"),
        about =
            RuleV2.About(
                maintainer = "Your name",
                repositoryUrl = "https://github.com/your/project/",
                issueTrackerUrl = "https://github.com/your/project/issues",
            ),
    ) {
    override fun beforeVisitChildNodes(node: ASTNode) {
        if (node.elementType == VAR_KEYWORD) {
            emit(node.startOffset, "Unexpected var, use val instead")
        }
    }
}
