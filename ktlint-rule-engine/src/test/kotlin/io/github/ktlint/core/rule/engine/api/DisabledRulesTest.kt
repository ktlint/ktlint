package io.github.ktlint.core.rule.engine.api

import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.isEmpty
import io.github.ktlint.core.rule.engine.core.api.AutocorrectDecision
import io.github.ktlint.core.rule.engine.core.api.RuleId
import io.github.ktlint.core.rule.engine.core.api.RuleV2
import io.github.ktlint.core.rule.engine.core.api.RuleV2Provider
import io.github.ktlint.core.rule.engine.core.api.editorconfig.RuleExecution
import io.github.ktlint.core.rule.engine.core.api.editorconfig.createRuleExecutionEditorConfigProperty
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

class DisabledRulesTest {
    @Test
    fun `Given some code and a enabled standard rule resulting in a violation then the violation is reported`() {
        assertThat(
            ArrayList<LintError>().apply {
                KtLintRuleEngine(
                    ruleProviders =
                        setOf(
                            RuleV2Provider { NoVarRule(SOME_RULE_ID) },
                        ),
                ).lint(Code.fromSnippet("var foo")) { e -> add(e) }
            },
        ).contains(
            LintError(1, 1, SOME_RULE_ID, NoVarRule.SOME_NO_VAR_RULE_VIOLATION, false),
        )
    }

    @ParameterizedTest(name = "RuleId: {0}, Disabled ruleId: {1}")
    @CsvSource(
        value = [
            "standard:no-var,standard:no-var",
            "custom:no-var,custom:no-var",
        ],
    )
    fun `Given a rule that is disabled via property 'ktlint_some-rule-id' and some code then no violation is reported`(
        ruleId: String,
        disabledRuleId: String,
    ) {
        val someRuleId = RuleId(ruleId)
        assertThat(
            ArrayList<LintError>().apply {
                KtLintRuleEngine(
                    ruleProviders =
                        setOf(
                            RuleV2Provider { NoVarRule(someRuleId) },
                        ),
                    editorConfigOverride =
                        EditorConfigOverride.from(
                            RuleId(disabledRuleId).createRuleExecutionEditorConfigProperty() to RuleExecution.disabled,
                        ),
                ).lint(Code.fromSnippet("var foo")) { e -> add(e) }
            },
        ).isEmpty()
    }

    class NoVarRule(
        ruleId: RuleId,
    ) : RuleV2(
            ruleId = ruleId,
            about = About(),
        ) {
        override fun beforeVisitChildNodes(
            node: ASTNode,
            emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> AutocorrectDecision,
        ) {
            emit(node.startOffset, SOME_NO_VAR_RULE_VIOLATION, false)
        }

        companion object {
            const val SOME_NO_VAR_RULE_VIOLATION = "some-no-var-rule-violation"
        }
    }

    companion object {
        val SOME_RULE_ID = RuleId("test:some-rule-id")
    }
}
