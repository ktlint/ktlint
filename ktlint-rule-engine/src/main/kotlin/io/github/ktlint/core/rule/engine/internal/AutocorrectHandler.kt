package io.github.ktlint.core.rule.engine.internal

import io.github.ktlint.core.rule.engine.api.LintError
import io.github.ktlint.core.rule.engine.core.api.AutocorrectDecision
import io.github.ktlint.core.rule.engine.core.api.AutocorrectDecision.NO_AUTOCORRECT

/**
 * Handler which determines whether autocorrect should be enabled or disabled for the given offset.
 */
internal sealed interface AutocorrectHandler {
    fun autocorrectDecision(lintError: LintError): AutocorrectDecision
}

/**
 * Invokes the [onLint] is for every [LintError], and returns the [NO_AUTOCORRECT] decision.
 */
internal class NoAutocorrectHandler(
    private val onLint: (LintError) -> Unit,
) : AutocorrectHandler {
    override fun autocorrectDecision(lintError: LintError): AutocorrectDecision {
        onLint(lintError)
        return NO_AUTOCORRECT
    }
}

/**
 * Invokes the [onAutocorrect] is for every [LintError], and let API Consumer determine the [AutocorrectDecision].
 */
internal class CustomAutocorrectHandler(
    private val onAutocorrect: (LintError) -> AutocorrectDecision,
) : AutocorrectHandler {
    override fun autocorrectDecision(lintError: LintError) = onAutocorrect(lintError)
}
