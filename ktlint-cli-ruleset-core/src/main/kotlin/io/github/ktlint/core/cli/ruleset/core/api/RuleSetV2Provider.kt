package io.github.ktlint.core.cli.ruleset.core.api

import io.github.ktlint.core.rule.engine.core.api.RuleSetId
import io.github.ktlint.core.rule.engine.core.api.RuleV2Provider
import java.io.Serializable

/**
 * KtLint uses [ServiceLoader](https://docs.oracle.com/javase/6/docs/api/java/util/ServiceLoader.html) to
 * discover all available [RuleSetV2Provider]`s on the classpath and so each [RuleSetV2Provider] must be registered using
 * `META-INF/services/io.github.ktlint.core.cli.ruleset.core.api.RuleSetV2Provider` (see `ktlint-ruleset-standard/src/main/resources`
 * for an example).
 */
public abstract class RuleSetV2Provider(
    public val id: RuleSetId,
) : Serializable {
    private companion object {
        private const val serialVersionUID: Long = 1L
    }

    /**
     * Gets a group of related [RuleV2Provider]s. A provided rule is not guaranteed to be run as rules can be disabled,
     * for example via ".editorconfig" properties.
     *
     * Intended usage:
     * ```
     * public class CustomRuleSetProvider :
     *     RuleSetV2Provider(RuleSetId("custom")) {
     *     override fun getRuleProviders(): Set<RuleV2Provider> =
     *         setOf(
     *             RuleV2Provider { CustomRule1() },
     *             RuleV2Provider { CustomRule2() }
     *         )
     *     }
     * ```
     */
    public abstract fun getRuleProviders(): Set<RuleV2Provider>
}
