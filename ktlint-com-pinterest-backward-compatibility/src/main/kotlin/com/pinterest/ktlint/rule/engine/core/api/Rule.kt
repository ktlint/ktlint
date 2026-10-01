@file:Suppress("DEPRECATION")

package com.pinterest.ktlint.rule.engine.core.api

import com.pinterest.ktlint.rule.engine.core.api.AutocorrectDecision.ALLOW_AUTOCORRECT
import com.pinterest.ktlint.rule.engine.core.api.AutocorrectDecision.NO_AUTOCORRECT
import com.pinterest.ktlint.rule.engine.core.api.editorconfig.EditorConfig
import com.pinterest.ktlint.rule.engine.core.api.editorconfig.EditorConfigProperty
import com.pinterest.ktlint.rule.engine.core.internal.IdNamingPolicy
import dev.drewhamilton.poko.Poko
import io.github.ktlint.core.rule.engine.core.api.RuleV2
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import com.pinterest.ktlint.rule.engine.core.api.editorconfig.CODE_STYLE_PROPERTY_TYPE as CODE_STYLE_PROPERTY_TYPE_COM_PINTEREST_KTLINT
import com.pinterest.ktlint.rule.engine.core.api.editorconfig.CodeStyleValue as CodeStyleValueComPinterestKtlint
import com.pinterest.ktlint.rule.engine.core.api.editorconfig.RULE_EXECUTION_PROPERTY_TYPE as RULE_EXECUTION_PROPERTY_TYPE_COM_PINTEREST_KTLINT
import com.pinterest.ktlint.rule.engine.core.api.editorconfig.RuleExecution as RuleExecutionComPinterestKtlint
import io.github.ktlint.core.rule.engine.core.api.AutocorrectDecision as AutocorrectDecisionIoGithubKtlint
import io.github.ktlint.core.rule.engine.core.api.editorconfig.CODE_STYLE_PROPERTY_TYPE as CODE_STYLE_PROPERTY_TYPE_IO_GITHUB_KTLINT
import io.github.ktlint.core.rule.engine.core.api.editorconfig.CodeStyleValue as CodeStyleValueIoGithubKtlint
import io.github.ktlint.core.rule.engine.core.api.editorconfig.EditorConfig as EditorConfigIoGithubKtlint
import io.github.ktlint.core.rule.engine.core.api.editorconfig.EditorConfigProperty as EditorConfigPropertyIoGithubKtlint
import io.github.ktlint.core.rule.engine.core.api.editorconfig.RULE_EXECUTION_PROPERTY_TYPE as RULE_EXECUTION_PROPERTY_TYPE_IO_GITHUB_KTLINT
import io.github.ktlint.core.rule.engine.core.api.editorconfig.RuleExecution as RuleExecutionIoGithubKtlint

@Poko
@Deprecated(message = "Provides backwards compatibility of custom ruleset JARs created for Ktlint 1.x. Don't use for RuleV2")
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
@Deprecated(message = "Provides backwards compatibility of custom ruleset JARs created for Ktlint 1.x. Don't use for RuleV2")
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

@Deprecated(message = "Provides backwards compatibility of custom ruleset JARs created for Ktlint 1.x. Don't use for RuleV2")
public open class Rule(
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
     * Set of modifiers of the visitor. Preferably a rule has no modifiers at all, meaning that it is completely
     * independent of all other rules.
     */
    public open val visitorModifiers: Set<VisitorModifier> = emptySet(),
    /**
     * Set of [EditorConfigProperty]'s that are to provided to the rule. Only specify the properties that are actually used by the rule.
     */
    public open val usesEditorConfigProperties: Set<EditorConfigProperty<*>> = emptySet(),
) {
    private var traversalState = TraversalState.NOT_STARTED

    /**
     * This method is called once before the first node is visited. It can be used to initialize the state of the rule
     * before processing of nodes starts.
     */
    public open fun beforeFirstNode(editorConfig: EditorConfig) {}

    @Deprecated(
        message = "Rules implementing this method are not supported by Ktlint 2.x",
        level = DeprecationLevel.ERROR,
    )
    public open fun beforeVisitChildNodes(
        node: ASTNode,
        autoCorrect: Boolean,
        emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit,
    ): Unit =
        throw UnsupportedOperationException(
            "Ktlint 2.x does not support rules that have not correctly implemented the RuleAutocorrectApproveHandler",
        )

    @Deprecated(
        message = "Rules implementing this method are not supported by Ktlint 2.x. Implement the RuleAutocorrectApproveHandler instead",
        level = DeprecationLevel.ERROR,
    )
    public open fun afterVisitChildNodes(
        node: ASTNode,
        autoCorrect: Boolean,
        emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit,
    ): Unit =
        throw UnsupportedOperationException(
            "Ktlint 2.x does not support rules that have not correctly implemented the RuleAutocorrectApproveHandler",
        )

    /**
     * This method is called once after the last node in the AST is visited. It can be used for teardown of the state
     * of the rule.
     */
    public open fun afterLastNode() {}

    /**
     * Checks whether the [Rule] instance is used for traversal of the AST and as of that potentially has changed the state of the [Rule]
     * provided that it has state.
     */
    public fun isUsedForTraversalOfAST(): Boolean = traversalState != TraversalState.NOT_STARTED

    /**
     * Marks the [Rule] instance as being used for traversal of an AST. From this moment on, this instance of the [Rule]
     * can not be used to start a new traversal of the same or another AST as the instance might contain state.
     */
    public fun startTraversalOfAST() {
        require(traversalState == TraversalState.NOT_STARTED)
        traversalState = TraversalState.CONTINUE
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
         * Traversal of the AST is not started. As no life cycle hooks of the [Rule] have been executed, the [Rule]
         * instance can not contain state specific for the AST.
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
    @Deprecated(
        message = "Provides backwards compatibility of custom ruleset JARs created for Ktlint 1.x. Use RuleV2.about when possible.",
        replaceWith = ReplaceWith("RuleV2.About"),
    )
    public class About(
        /**
         * Name of person, organisation or group maintaining the rule.
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

    @Deprecated(
        message =
            "Provides backwards compatibility of custom ruleset JARs created for Ktlint 1.x. VisitorModifiers are ignored by Ktlint 2.x " +
                "as due to the way that rules are executed, modifying the order in which rules are visited is not needed anymore.",
    )
    public sealed class VisitorModifier {
        /**
         * Defines that the [Rule] that declares this [VisitorModifier] will be run after the [Rule] with rule id
         * [VisitorModifier.RunAfterRule.ruleId].
         */
        @Poko
        @Deprecated(
            message =
                "Provides backwards compatibility of custom ruleset JARs created for Ktlint 1.x. The RunAfterRule VisitorModifier is " +
                    "ignored by Ktlint 2.x as due to the way that rules are executed, modifying the order in which rules are visited is " +
                    "not needed anymore.",
        )
        public class RunAfterRule(
            /**
             * The [RuleId] of the [Rule] which should run before the [Rule] that declares the [VisitorModifier.RunAfterRule].
             */
            public val ruleId: RuleId,
            /**
             * The [Mode] determines whether the [Rule] that declares this [VisitorModifier] can be run in case the [Rule] with rule id
             * [VisitorModifier.RunAfterRule.ruleId] is not loaded or enabled.
             */
            public val mode: Mode,
        ) : VisitorModifier() {
            public enum class Mode {
                /**
                 * Run the [Rule] that declares the [VisitorModifier.RunAfterRule] regardless whether the [Rule] with ruleId
                 * [VisitorModifier.RunAfterRule.ruleId] is loaded or disabled. However, if that other rule is loaded and enabled, it runs
                 * before the [Rule] that declares the [VisitorModifier.RunAfterRule].
                 */
                REGARDLESS_WHETHER_RUN_AFTER_RULE_IS_LOADED_OR_DISABLED,

                /**
                 * Run the [Rule] that declares the [VisitorModifier.RunAfterRule] only in case the [Rule] with ruleId
                 * [VisitorModifier.RunAfterRule.ruleId] is loaded *and* enabled. That other rule runs before the [Rule] that declares the
                 * [VisitorModifier.RunAfterRule].
                 */
                ONLY_WHEN_RUN_AFTER_RULE_IS_LOADED_AND_ENABLED,
            }
        }

        @Deprecated(
            message =
                "Provides backwards compatibility of custom ruleset JARs created for Ktlint 1.x. The RunAsLateAsPossible VisitorModifier " +
                    "is ignored by Ktlint 2.x as due to the way that rules are executed, modifying the order in which rules are visited " +
                    "is not needed anymore.",
        )
        public object RunAsLateAsPossible : VisitorModifier()
    }

    /**
     * This interface marks a rule as an 'experimental' rule. A rule marked with this interface will only be executed by ktlint in case the
     * '.editorconfig' allows this rule specifically or all experimental rules. This interface is used by Ktlint internally but is also
     * explicitly meant to be used by custom rule providers.
     */
    @Deprecated(
        message = "Provides backwards compatibility of custom ruleset JARs created for Ktlint 1.x. Use RuleV2.Experimental when possible",
    )
    public interface Experimental

    /**
     * This interface marks a rule as an Official rule. A rule marked with this interface will only be executed when by ktlint in case the
     * '.editorconfig' contains property "code_style = ktlint_official" or when enabled explicitly. This interface is intended to be used
     * in Ktlint internally only. It may be subject to change at any time without providing any backward compatibility.
     */
    @Deprecated(
        message =
            "Provides backwards compatibility of custom ruleset JARs created for Ktlint 1.x. Use RuleV2.OfficialCodeStyle when possible",
    )
    public interface OfficialCodeStyle

    /**
     * This interface marks a rule to be run only on explicitly enabled in the `.editorconfig`. This can be used to mark a rule as
     * deprecated, or when a rule is not applicable for general use.
     * This interface should not be used on a rule that is also marked with [Experimental], or [OfficialCodeStyle].
     */
    @Deprecated(
        message =
            "Provides backwards compatibility of custom ruleset JARs created for Ktlint 1.x. Use RuleV2.OnlyWhenEnabledInEditorconfig " +
                "when possible",
    )
    public interface OnlyWhenEnabledInEditorconfig
}

public fun Rule.toRuleV2(): RuleV2 =
    this
        .also {
            require(it is RuleAutocorrectApproveHandler) {
                "Ktlint 2.x does not support rules that have not correctly implemented the RuleAutocorrectApproveHandler. Use a new " +
                    "version of the ruleset. or contact the maintainer of this ruleset to upgrade it."
            }
        }.let { rule ->
            object :
                RuleV2(
                    ruleId =
                        io.github.ktlint.core.rule.engine.core.api
                            .RuleId(ruleId.value),
                    about =
                        About(
                            maintainer = rule.about.maintainer,
                            repositoryUrl = rule.about.repositoryUrl,
                            issueTrackerUrl = rule.about.issueTrackerUrl,
                        ),
                    usesEditorConfigProperties = rule.usesEditorConfigProperties.mapToEditorConfigPropertiesIoGithubKtlint(),
                ) {
                override fun beforeFirstNode(editorConfig: EditorConfigIoGithubKtlint) {
                    rule.beforeFirstNode(editorConfig.mapToEditorConfigComPinterestKtlint())
                }

                override fun beforeVisitChildNodes(
                    node: ASTNode,
                    emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> AutocorrectDecisionIoGithubKtlint,
                ) {
                    // Call the legacy version which previously was provided via RuleAutocorrectApproveHandler. Note that the
                    // AutocorrectDecision has to be transformed
                    (rule as RuleAutocorrectApproveHandler).beforeVisitChildNodes(
                        node,
                        emitAndTransformAutoCorrectDecision(emit),
                    )
                }

                private fun emitAndTransformAutoCorrectDecision(
                    emit: (Int, String, Boolean) -> AutocorrectDecisionIoGithubKtlint,
                ): (Int, String, Boolean) -> AutocorrectDecision =
                    { offset: Int, errorMessage: String, canBeAutoCorrected: Boolean ->
                        when (emit(offset, errorMessage, canBeAutoCorrected)) {
                            AutocorrectDecisionIoGithubKtlint.NO_AUTOCORRECT -> NO_AUTOCORRECT
                            AutocorrectDecisionIoGithubKtlint.ALLOW_AUTOCORRECT -> ALLOW_AUTOCORRECT
                        }
                    }

                override fun afterVisitChildNodes(
                    node: ASTNode,
                    emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> AutocorrectDecisionIoGithubKtlint,
                ) {
                    (rule as RuleAutocorrectApproveHandler).afterVisitChildNodes(node, emitAndTransformAutoCorrectDecision(emit))
                }

                override fun afterLastNode() {
                    rule.afterLastNode()
                }
            }
        }

private fun EditorConfigIoGithubKtlint.mapToEditorConfigComPinterestKtlint(): EditorConfig = EditorConfig(*map { it }.toTypedArray())

@Suppress("UNCHECKED_CAST")
private fun Set<EditorConfigProperty<*>>.mapToEditorConfigPropertiesIoGithubKtlint(): Set<EditorConfigPropertyIoGithubKtlint<*>> =
    map { (it as EditorConfigProperty<Any?>).toEditorConfigPropertyIoGithubKtlint() }.toSet()

// Maps PropertyType instances from the com.pinterest package to their io.github.ktlint.core equivalents.
private val PROPERTY_TYPE_REGISTRY: Map<org.ec4j.core.model.PropertyType<*>, org.ec4j.core.model.PropertyType<*>> =
    mapOf(
        CODE_STYLE_PROPERTY_TYPE_COM_PINTEREST_KTLINT to CODE_STYLE_PROPERTY_TYPE_IO_GITHUB_KTLINT,
        RULE_EXECUTION_PROPERTY_TYPE_COM_PINTEREST_KTLINT to RULE_EXECUTION_PROPERTY_TYPE_IO_GITHUB_KTLINT,
    )

@Suppress("UNCHECKED_CAST")
private fun <T> org.ec4j.core.model.PropertyType<T>.toNewPackage(): org.ec4j.core.model.PropertyType<T> =
    (PROPERTY_TYPE_REGISTRY[this] ?: this) as org.ec4j.core.model.PropertyType<T>

// Maps enum values from the com.pinterest package to their io.github.ktlint.core equivalents by name, leaving all
// other values unchanged.
private fun Any?.mapValueToNewPackage(): Any? =
    when (this) {
        is CodeStyleValueComPinterestKtlint -> CodeStyleValueIoGithubKtlint.valueOf(name.uppercase())
        is RuleExecutionComPinterestKtlint -> RuleExecutionIoGithubKtlint.valueOf(name.uppercase())
        else -> this
    }

// Wraps the com.pinterest propertyMapper so that the io.github CodeStyleValue parameter is converted to the
// com.pinterest equivalent before invoking the original mapper, and the return value is mapped back to the
// io.github package when applicable.
@Suppress("UNCHECKED_CAST")
private fun <T> EditorConfigProperty<T>.wrapPropertyMapper(): ((org.ec4j.core.model.Property?, CodeStyleValueIoGithubKtlint) -> T?)? =
    propertyMapper?.let { oldMapper ->
        { property, newCodeStyle ->
            val oldCodeStyle = CodeStyleValueComPinterestKtlint.valueOf(newCodeStyle.name.lowercase())
            oldMapper(property, oldCodeStyle).mapValueToNewPackage() as T?
        }
    }

// For backwards compatibility with dynamic loading of 1.x rules the EditorConfigProperty has to be provided in package
// "com.pinterest.ktlint.rule.engine.core.api.editorconfig" while the 2.x rules need to use the package
// "io.github.ktlint.core.rule.engine.core.api.editorconfig". When the type T is one of the enum types that were
// duplicated across both packages (CodeStyleValue, RuleExecution), the PropertyType, default values, and
// propertyMapper are mapped to their io.github.ktlint.core equivalents.
@Suppress("UNCHECKED_CAST")
private fun <T> EditorConfigProperty<T>.toEditorConfigPropertyIoGithubKtlint(): EditorConfigPropertyIoGithubKtlint<T> =
    EditorConfigPropertyIoGithubKtlint(
        type = type.toNewPackage(),
        defaultValue = defaultValue.mapValueToNewPackage() as T,
        ktlintOfficialCodeStyleDefaultValue = ktlintOfficialCodeStyleDefaultValue.mapValueToNewPackage() as T,
        intellijIdeaCodeStyleDefaultValue = intellijIdeaCodeStyleDefaultValue.mapValueToNewPackage() as T,
        androidStudioCodeStyleDefaultValue = androidStudioCodeStyleDefaultValue.mapValueToNewPackage() as T,
        propertyMapper = wrapPropertyMapper(),
        propertyWriter = propertyWriter,
        deprecationWarning = deprecationWarning,
        deprecationError = deprecationError,
        name = name,
    )
