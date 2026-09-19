package io.github.ktlint.core.rule.engine.internal

import assertk.assertThat
import assertk.assertions.isSuccess
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail

class VisitorProviderTest {
    @Test
    fun `When no runnable rules are found for the root node, the visit function on the root node is not executed`() {
        assertThat(
            runCatching {
                VisitorProvider(
                    ruleProviders = emptySet(),
                    recreateRuleSorter = true,
                ).rules.forEach { _ ->
                    fail("The visitor provider should not have called this lambda in case it has no rule providers")
                }
            },
        ).isSuccess()
    }
}
