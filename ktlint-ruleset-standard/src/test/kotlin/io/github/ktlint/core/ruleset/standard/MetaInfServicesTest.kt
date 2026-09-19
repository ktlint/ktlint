package io.github.ktlint.core.ruleset.standard

import assertk.assertThat
import assertk.assertions.isEqualTo
import io.github.ktlint.core.cli.ruleset.core.api.RuleSetV2Provider
import org.junit.jupiter.api.Test

class MetaInfServicesTest {
    @Test
    fun `Check if RuleSetV2Provider is registered in META-INF services`() {
        val classNameRuleSetProviderBaseClass = RuleSetV2Provider::class.java.name
        val classNameRuleSetProviderImplementationClass = StandardRuleSetProvider::class.java.name

        val actual = getResourceWithPath("META-INF/services/$classNameRuleSetProviderBaseClass")

        assertThat(actual).isEqualTo(classNameRuleSetProviderImplementationClass)
    }

    private fun getResourceWithPath(path: String) =
        (
            ClassLoader
                .getSystemClassLoader()
                .getResourceAsStream(path)
                ?: throw RuntimeException("Expected a resource to exist with path '$path'. Was the class renamed?")
        ).bufferedReader()
            .readText()
            .trim()
}
