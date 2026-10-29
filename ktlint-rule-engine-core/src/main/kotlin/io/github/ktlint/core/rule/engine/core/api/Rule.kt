package io.github.ktlint.core.rule.engine.core.api

import com.intellij.lang.ASTNode
import dev.drewhamilton.poko.Poko
import io.github.ktlint.core.rule.engine.core.api.editorconfig.EditorConfig
import io.github.ktlint.core.rule.engine.core.api.editorconfig.EditorConfigProperty
import io.github.ktlint.core.rule.engine.core.internal.IdNamingPolicy

@Poko
public class RuleId(
    public val value: String,
) {
    init {
        IdNamingPolicy.enforceRuleIdNaming(value)
    }

    public val ruleSetId: RuleSetId
        get() = RuleSetId(value.substringBefore(DELIMITER, ""))

    public companion object {
        private const val DELIMITER = ":"

        public fun isValid(value: String): Boolean = IdNamingPolicy.isValidRuleId(value)
    }
}

@Poko
public class RuleSetId(
    public val value: String,
) {
    init {
        IdNamingPolicy.enforceRuleSetIdNaming(value)
    }

    public companion object {
        /**
         * The `standard` rule set is reserved for rules published by the KtLint project only. Custom rules should be provided via a rule
         * set using a custom id so that in case of problems, it can be more clearly communicated to users which project is responsible for
         * maintenance of the rule (set).
         */
        public val STANDARD: RuleSetId = RuleSetId("standard")

        public fun isValid(value: String): Boolean = IdNamingPolicy.isValidRuleSetId(value)
    }
}

/**
 * The [RuleV2] contains the life cycle hooks which are called by the KtLint rule engine to execute the rule.
 *
 * The implementation of a [RuleV2] **doesn't** have to be thread-safe or stateless provided that the [RuleV2Provider] creates a new
 * instance of [RuleV2] on each call to [RuleV2Provider.provider].
 *
 * When wrapping a rule from the ktlint project and modifying its behavior, please change the [ruleId] and [about] fields, so that it is
 * clear to users whenever they used the original rule provided by KtLint versus a modified version which is not maintained by the KtLint
 * project.
 */
public open class RuleV2(
    /**
     * Identification of the rule. A [ruleId] has a value that must adhere the convention "<rule-set-id>:<rule-id>". The rule set id
     * 'standard' is reserved for rules which are maintained by the KtLint project. Rules created by custom rule set providers and API
     * Consumers should use a prefix other than 'standard' to mark the origin of rules which are not maintained by the KtLint project.
     */
    public open val ruleId: RuleId,
    /**
     * About the rule. Background information about the rule and its maintainer. About information is meant to be used in stack traces or
     * API consumers to provide more detailed information about the rule.
     */
    public open val about: About,
    /**
     * Set of [EditorConfigProperty]'s that are to provided to the rule. Only specify the properties that are actually used by the rule.
     */
    public open val usesEditorConfigProperties: Set<EditorConfigProperty<*>> = emptySet(),
) {
    private lateinit var emitLintViolation: (Int, RuleId, String, Boolean) -> AutocorrectDecision
    private var traversalState = TraversalState.NOT_STARTED

    /**
     * Emits a lint violation. If the violation can be autocorrected than the [autoCorrect] parameter should override the default value.
     * The [autoCorrect] will only be called when running in format mode, and the API consumer indicates that this specific violation is to
     * be autocorrected.
     * If the violation cannot be autocorrected than the [autoCorrect] does not need to be overridden.
     */
    public fun emit(
        offset: Int,
        errorMessage: String,
        autoCorrect: () -> Unit = AUTOCORRECT_NOT_IMPLEMENTED,
    ) {
        val canBeAutoCorrected = autoCorrect != AUTOCORRECT_NOT_IMPLEMENTED
        val decision = emitLintViolation(offset, ruleId, errorMessage, canBeAutoCorrected)
        if (canBeAutoCorrected && decision == AutocorrectDecision.ALLOW_AUTOCORRECT) {
            autoCorrect()
        }
    }

    /**
     * This method is called once before the first node is visited. It can be used to initialize the state of the rule
     * before processing of nodes starts.
     */
    public open fun beforeFirstNode(editorConfig: EditorConfig) {}

    /**
     * This method is called on a [node] in the AST before visiting the child nodes. This is repeated recursively for the child nodes
     * resulting in a depth first traversal of the AST.
     *
     * When a rule overrides this method, the API Consumer can decide per violation whether the violation needs to be autocorrected. For
     * this the [emit] function is called.
     */
    public open fun beforeVisitChildNodes(node: ASTNode) {
    }

    /**
     * This method is called on a [node] in tje AST after all its child nodes have been visited.
     *
     * When a rule overrides this method, the API Consumer can decide per violation whether the violation needs to be autocorrected. For
     * this the [emit] function is called.
     */
    public open fun afterVisitChildNodes(node: ASTNode) {
    }

    /**
     * This method is called once after the last node in the AST is visited. It can be used for teardown of the state
     * of the rule.
     */
    public open fun afterLastNode() {}

    /**
     * Checks whether the [RuleV2] instance is used for traversal of the AST and as of that potentially has changed the state of the
     * [RuleV2] provided that it has state.
     */
    public fun isUsedForTraversalOfAST(): Boolean = traversalState != TraversalState.NOT_STARTED

    /**
     * Marks the [RuleV2] instance as being used for traversal of an AST. From this moment on, this instance of the [RuleV2] can not be used
     * to start a new traversal of the same or another AST as the instance might contain state.
     */
    public fun startTraversalOfAST(emitLintViolation: (Int, RuleId, String, Boolean) -> AutocorrectDecision) {
        require(traversalState == TraversalState.NOT_STARTED)
        traversalState = TraversalState.CONTINUE
        this.emitLintViolation = emitLintViolation
    }

    /**
     * Checks whether the next node in the AST is to be traversed. By default, the entire AST is traversed.
     */
    public fun shouldContinueTraversalOfAST(): Boolean = traversalState == TraversalState.CONTINUE

    /**
     * Stops traversal of the AST. Intended usage it to prevent parsing of the remainder of the AST once the goal of the
     * rule is achieved. For example, if the ".editorconfig" property indent_size is set to 0 or -1 then the indent rule
     * should be disabled.
     *
     * When called in [beforeFirstNode], no AST nodes will be visited. [afterLastNode] is still called.
     *
     * When called in [beforeVisitChildNodes], the child nodes of that node will not be visited. [afterVisitChildNodes]
     * is still called for the node and each of its parent nodes. Other nodes in the AST will not be visited. Finally
     * [afterLastNode] is called.
     *
     * When called in [afterVisitChildNodes] the child nodes of that node are already visited. [afterVisitChildNodes] is
     * still called for each of its parent nodes. Other nodes in the AST will not be visited. Finally [afterLastNode] is
     * called.
     *
     * Calling in [afterLastNode] has no effect as traversal of the AST has already been completed.
     */
    public fun stopTraversalOfAST() {
        traversalState = TraversalState.STOP
    }

    private enum class TraversalState {
        /**
         * Traversal of the AST is not started. As no life cycle hooks of the [RuleV2] have been executed, the [RuleV2] instance can not
         * contain state specific for the AST.
         */
        NOT_STARTED,

        /**
         * Traversal of the AST is started and should be continued with next node.
         */
        CONTINUE,

        /**
         * Stops traversal of yet unvisited nodes in the AST. See [stopTraversalOfAST] for more details.
         */
        STOP,
    }

    /**
     * About the rule. Background information about the rule and its maintainer. About information is meant to be used in stack traces or
     * API consumers to provide more detailed information about the rule. Please provide all details below, so that users of your rule set
     * can easily get up-to-date information about the rule.
     */
    @Poko
    public class About(
        /**
         * Name of person, organization or group maintaining the rule.
         */
        public val maintainer: String = "Not specified (and not maintained by the Ktlint project)",
        /**
         * Url to the repository containing the rule.
         */
        public val repositoryUrl: String = "Not specified",
        /**
         * Url to the issue tracker of the project which provides the rule.
         */
        public val issueTrackerUrl: String = "Not specified",
    )

    /**
     * This interface marks a rule as an 'experimental' rule. A rule marked with this interface will only be executed by ktlint in case the
     * '.editorconfig' allows this rule specifically or all experimental rules. This interface is used by Ktlint internally but is also
     * explicitly meant to be used by custom rule providers.
     */
    public interface Experimental

    /**
     * This interface marks a rule as an Official rule. A rule marked with this interface will only be executed when by ktlint in case the
     * '.editorconfig' contains property "code_style = ktlint_official" or when enabled explicitly. This interface is intended to be used
     * in Ktlint internally only. It may be subject to change at any time without providing any backward compatibility.
     */
    public interface OfficialCodeStyle

    /**
     * This interface marks a rule to be run only on explicitly enabled in the `.editorconfig`. This can be used to mark a rule as
     * deprecated, or when a rule is not applicable for general use.
     * This interface should not be used on a rule that is also marked with [Experimental], or [OfficialCodeStyle].
     */
    public interface OnlyWhenEnabledInEditorconfig

    private companion object {
        val AUTOCORRECT_NOT_IMPLEMENTED: () -> Unit = { error("Autocorrect is not implemented") }
    }
}
