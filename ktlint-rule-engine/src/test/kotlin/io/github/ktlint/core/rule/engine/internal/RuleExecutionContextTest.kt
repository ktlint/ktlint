package io.github.ktlint.core.rule.engine.internal

import assertk.all
import assertk.assertFailure
import assertk.assertions.cause
import assertk.assertions.hasMessage
import assertk.assertions.isInstanceOf
import assertk.assertions.isNotNull
import io.github.ktlint.core.rule.engine.api.Code
import io.github.ktlint.core.rule.engine.api.KtLintRuleEngine
import io.github.ktlint.core.rule.engine.api.KtLintRuleException
import io.github.ktlint.core.rule.engine.core.api.AutocorrectDecision.ALLOW_AUTOCORRECT
import io.github.ktlint.core.rule.engine.core.api.RuleId
import io.github.ktlint.core.rule.engine.core.api.RuleV2
import io.github.ktlint.core.rule.engine.core.api.RuleV2Provider
import io.github.ktlint.core.rule.engine.core.api.editorconfig.EditorConfig
import org.junit.jupiter.api.Test

class RuleExecutionContextTest {
    @Test
    fun `Given a rule that throws an exception in the beforeFirstNode callback then the exception is wrapped inside a KtlintRuleException`() {
        val ktLintRuleEngine =
            KtLintRuleEngine(
                ruleProviders =
                    setOf(
                        RuleV2Provider {
                            object : RuleV2(
                                ruleId = SOME_RULE_ID,
                                about =
                                    About(
                                        maintainer = SOME_MAINTAINER,
                                        repositoryUrl = SOME_REPOSITORY_URL,
                                        issueTrackerUrl = SOME_ISSUE_TRACKER_URL,
                                    ),
                            ) {
                                override fun beforeFirstNode(editorConfig: EditorConfig): Unit =
                                    throw IllegalArgumentException(SOME_EXCEPTION_MESSAGE)
                            }
                        },
                    ),
            )
        assertFailure { ktLintRuleEngine.format(SOME_CODE_SNIPPET) { ALLOW_AUTOCORRECT } }
            .isInstanceOf<KtLintRuleException>()
            .all {
                hasMessage(
                    """
                    Rule '${SOME_RULE_ID.value}' throws exception in file '<stdin>' at position (0:0)
                       Rule maintainer: $SOME_MAINTAINER
                       Issue tracker  : $SOME_ISSUE_TRACKER_URL
                       Repository     : $SOME_REPOSITORY_URL
                    """.trimIndent(),
                )
                cause().isNotNull().isInstanceOf<IllegalArgumentException>().hasMessage(SOME_EXCEPTION_MESSAGE)
            }
    }

    @Test
    fun `Given a rule that throws an exception in the afterLastNode callback then the exception is wrapped inside a KtlintRuleException`() {
        val ktLintRuleEngine =
            KtLintRuleEngine(
                ruleProviders =
                    setOf(
                        RuleV2Provider {
                            object : RuleV2(
                                ruleId = SOME_RULE_ID,
                                about =
                                    About(
                                        maintainer = SOME_MAINTAINER,
                                        repositoryUrl = SOME_REPOSITORY_URL,
                                        issueTrackerUrl = SOME_ISSUE_TRACKER_URL,
                                    ),
                            ) {
                                override fun afterLastNode(): Unit = throw IllegalArgumentException(SOME_EXCEPTION_MESSAGE)
                            }
                        },
                    ),
            )
        assertFailure { ktLintRuleEngine.format(SOME_CODE_SNIPPET) { ALLOW_AUTOCORRECT } }
            .isInstanceOf<KtLintRuleException>()
            .all {
                hasMessage(
                    """
                    Rule '${SOME_RULE_ID.value}' throws exception in file '<stdin>' at position (0:0)
                       Rule maintainer: $SOME_MAINTAINER
                       Issue tracker  : $SOME_ISSUE_TRACKER_URL
                       Repository     : $SOME_REPOSITORY_URL
                    """.trimIndent(),
                )
                cause().isNotNull().isInstanceOf<IllegalArgumentException>().hasMessage(SOME_EXCEPTION_MESSAGE)
            }
    }

    private companion object {
        val SOME_RULE_ID = RuleId("standard:some-rule-id")
        const val SOME_MAINTAINER = "some-maintainer"
        const val SOME_REPOSITORY_URL = "some-repository-url"
        const val SOME_ISSUE_TRACKER_URL = "some-issue-tracker-url"
        const val SOME_EXCEPTION_MESSAGE = "some-exception-message"
        val SOME_CODE_SNIPPET =
            Code.fromSnippet(
                """
                val foo = "foo"
                """.trimIndent(),
            )
    }
}
