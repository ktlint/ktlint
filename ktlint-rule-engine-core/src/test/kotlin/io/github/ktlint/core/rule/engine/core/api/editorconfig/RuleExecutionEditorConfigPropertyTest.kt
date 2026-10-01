package io.github.ktlint.core.rule.engine.core.api.editorconfig

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

class RuleExecutionEditorConfigPropertyTest {
    @ParameterizedTest(name = "Value: [{0}], result: [{1}]")
    @CsvSource(
        value = [
            "enabled,ENABLED",
            " enabled,ENABLED",
            "enabled ,ENABLED",
            " enabled ,ENABLED",
            "disabled,DISABLED",
            " disabled,DISABLED",
            "disabled ,DISABLED",
            " disabled ,DISABLED",
        ],
        ignoreLeadingAndTrailingWhitespace = false,
    )
    fun `Given a rule execution property for which the value`(
        value: String,
        expectedRuleExecution: RuleExecution,
    ) {
        val actual = RULE_EXECUTION_PROPERTY_TYPE.parse(value)

        assertThat(actual.parsed).isEqualTo(expectedRuleExecution)
    }
}
