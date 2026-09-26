package io.github.ktlint.core.rule.engine.core.api.editorconfig

import assertk.assertThat
import assertk.assertions.isEqualTo
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

class RuleExecutionEditorConfigPropertyTest {
    @ParameterizedTest(name = "Value: [{0}], result: [{1}]")
    @CsvSource(
        value = [
            "enabled,enabled",
            " enabled,enabled",
            "enabled ,enabled",
            " enabled ,enabled",
            "disabled,disabled",
            " disabled,disabled",
            "disabled ,disabled",
            " disabled ,disabled",
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
