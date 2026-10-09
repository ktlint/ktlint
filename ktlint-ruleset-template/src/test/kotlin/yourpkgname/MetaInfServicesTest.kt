package yourpkgname

import io.github.ktlint.core.cli.ruleset.core.api.RuleSetV2Provider
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class MetaInfServicesTest {
    @Test
    fun `Check if RuleSetV2Provider is registered in META-INF services`() {
        val classNameRuleSetProviderBaseClass = RuleSetV2Provider::class.java.name
        val classNameRuleSetProviderImplementationClass = CustomRuleSetProvider::class.java.name

        val actual = getResourceWithPath("META-INF/services/$classNameRuleSetProviderBaseClass")

        assertThat(actual).isEqualTo(classNameRuleSetProviderImplementationClass)
    }

    private fun getResourceWithPath(path: String) =
        (
            ClassLoader
                .getSystemClassLoader()
                .getResourceAsStream(path)
                ?: throw IllegalStateException("Expected a resource to exist with path '$path'. Was the class renamed?")
        ).bufferedReader()
            .readText()
            .trim()
}
