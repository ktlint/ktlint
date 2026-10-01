package io.github.ktlint.core.rule.engine.internal

import io.github.ktlint.core.rule.engine.api.Code
import io.github.ktlint.core.rule.engine.api.KtLintParseException
import io.github.ktlint.core.rule.engine.api.KtLintRuleEngine
import io.github.ktlint.core.rule.engine.api.KtLintRuleEngine.Companion.UTF8_BOM
import io.github.ktlint.core.rule.engine.api.KtLintRuleException
import io.github.ktlint.core.rule.engine.core.api.AutocorrectDecision
import io.github.ktlint.core.rule.engine.core.api.ElementType.FILE
import io.github.ktlint.core.rule.engine.core.api.KtlintKotlinCompiler
import io.github.ktlint.core.rule.engine.core.api.RuleId
import io.github.ktlint.core.rule.engine.core.api.RuleV2
import io.github.ktlint.core.rule.engine.core.api.RuleV2Provider
import io.github.ktlint.core.rule.engine.core.api.editorconfig.CODE_STYLE_PROPERTY
import io.github.ktlint.core.rule.engine.core.api.editorconfig.EditorConfig
import io.github.ktlint.core.rule.engine.core.api.editorconfig.RuleExecution
import io.github.ktlint.core.rule.engine.core.api.editorconfig.createRuleExecutionEditorConfigProperty
import io.github.ktlint.core.rule.engine.core.api.parent
import io.github.ktlint.core.rule.engine.internal.rulefilter.InternalRuleProvidersFilter
import io.github.ktlint.core.rule.engine.internal.rulefilter.RuleExecutionRuleFilter
import io.github.ktlint.core.rule.engine.internal.rulefilter.applyRuleFilters
import io.github.ktlint.core.rule.engine.internal.rules.KTLINT_SUPPRESSION_RULE_ID
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.lang.FileASTNode
import org.jetbrains.kotlin.com.intellij.psi.PsiElement
import org.jetbrains.kotlin.com.intellij.psi.PsiErrorElement
import kotlin.io.path.pathString

internal class RuleExecutionContext private constructor(
    val code: Code,
    val rootNode: FileASTNode,
    val ruleProviders: Set<RuleV2Provider>,
    val editorConfig: EditorConfig,
    val positionInTextLocator: (offset: Int) -> LineAndColumn,
) {
    private var suppressionLocator = SuppressionLocator(editorConfig)

    fun executeRules(
        rules: List<RuleV2>,
        autocorrectHandler: AutocorrectHandler,
        emitAndApprove: (offset: Int, ruleId: RuleId, errorMessage: String, canBeAutoCorrected: Boolean) -> AutocorrectDecision,
    ) {
        // Whenever the KtlintSuppressionRule finds a "ktlint-disable" directive, it will traverse up in the AST Node Tree to add a Suppress
        // annotation. Therefore, this rule needs to run on the entire AST Node before processing other rules.
        executeRulesOnAST(rules.filter { it.ruleId == KTLINT_SUPPRESSION_RULE_ID }, autocorrectHandler, emitAndApprove)

        // Process all other rules on the same AST Node before proceeding to the next AST Node.
        executeRulesOnAST(rules.filterNot { it.ruleId == KTLINT_SUPPRESSION_RULE_ID }, autocorrectHandler, emitAndApprove)
    }

    /**
     * While traversing the AST, execute all rules on an AST Node before proceeding to the next AST Node.
     */
    private fun executeRulesOnAST(
        rules: List<RuleV2>,
        autocorrectHandler: AutocorrectHandler,
        emitAndApprove: (offset: Int, ruleId: RuleId, errorMessage: String, canBeAutoCorrected: Boolean) -> AutocorrectDecision,
    ) {
        try {
            rules.forEach { rule ->
                rule.startTraversalOfAST()
                rule.execute { it.beforeFirstNode(it.editorConfig()) }
            }
            this.executeRulesOnNodeRecursively(
                rootNode,
                rules.filter { it.shouldContinueTraversalOfAST() },
                autocorrectHandler,
                emitAndApprove,
            )
            rules.forEach { rule ->
                rule.execute { it.afterLastNode() }
            }
        } catch (e: RuleExecutionException) {
            throw KtLintRuleException(
                e.line,
                e.col,
                e.rule.ruleId.value,
                """
                Rule '${e.rule.ruleId.value}' throws exception in file '${code.filePathOrStdin()}' at position (${e.line}:${e.col})
                   Rule maintainer: ${e.rule.about.maintainer}
                   Issue tracker  : ${e.rule.about.issueTrackerUrl}
                   Repository     : ${e.rule.about.repositoryUrl}
                """.trimIndent(),
                e.cause,
            )
        }
    }

    // The rule gets access to an EditConfig which is filtered by the properties which are actually registered as being used by the rule. In
    // this way it can be forced that the rule actually registers the properties that it uses and the field becomes reliable to be used by
    // for example the ".editorconfig" file generator.
    private fun RuleV2.editorConfig(): EditorConfig =
        editorConfig.filterBy(
            usesEditorConfigProperties
                // Provide the CODE_STYLE_PROPERTY as this property is needed to determine the default value of an EditorConfigProperty that
                // is not explicitly defined.
                .plus(CODE_STYLE_PROPERTY)
                // Provide the rule execution property for the "standard:max-line-length" property based on whether a rule provider for this
                // rule exists. This property is required to determine whether the property `max_line_length` needs to be taken into
                // account.
                .plus(
                    RuleId("standard:max-line-length")
                        .createRuleExecutionEditorConfigProperty(
                            if (ruleProviders.any { it.ruleId.value == "standard:max-line-length" }) {
                                RuleExecution.ENABLED
                            } else {
                                RuleExecution.DISABLED
                            },
                        ),
                ),
        )

    private fun RuleV2.execute(action: (RuleV2) -> Unit) {
        try {
            action(this)
        } catch (e: KtLintParseException) {
            throw e
        } catch (e: RuleExecutionException) {
            throw e
        } catch (e: Exception) {
            // Wrap remaining exception. Line and column can not be determined for this type of exception
            throw RuleExecutionException(this, line = 0, col = 0, e)
        }
    }

    private fun executeRulesOnNodeRecursively(
        node: ASTNode,
        rules: List<RuleV2>,
        autocorrectHandler: AutocorrectHandler,
        emitAndApprove: (offset: Int, ruleId: RuleId, errorMessage: String, canBeAutoCorrected: Boolean) -> AutocorrectDecision,
    ) {
        rules.forEach { rule ->
            if (node.parent == null && node.elementType != FILE) {
                // In commit 729a0fe23c34a8d03fd06854484fe24571b92bd6 the rule execution order has been changed to process all rules on an
                // ASTNode before proceeding to the next node. Only when testing the Ktlint 2.0 SNAPSHOT with the Ktlint Intellij Plugin it
                // was found this in a particular case led to a regression error when running the plugin with an older Ktlint ruleset
                // version. Similar problems could also occur with custom rulesets.
                // In case the entire node has been replaced by a previous rule, for example by using function rawReplaceWithText(text) on a
                // LeafElement then the reference to the parent element of that element is set to null. Also, other fields of the node may
                // be initialized to a default value. This could result for example result in Null Pointer Exception whenever a next rule is
                // inspecting the element type of the parent of the node. Normally this would be resolved by fixing that rule to check for a
                // non-null parent. However, older Ktlint rulesets are not maintained, and can therefore not be fixed.
                // Hack solution is to stop processing of the node once it is identified that it is likely that the node has been replaced.
                return
            }
            try {
                if (!suppressionLocator.suppress(rootNode, node.startOffset, rule)) {
                    rule.beforeVisitChildNodes(node) { offset, errorMessage, canBeAutoCorrected ->
                        emitAndApprove(offset, rule.ruleId, errorMessage, canBeAutoCorrected)
                    }
                }
            } catch (e: Throwable) {
                if (autocorrectHandler is NoneAutocorrectHandler) {
                    val (line, col) = positionInTextLocator(node.startOffset)
                    throw RuleExecutionException(
                        rule,
                        line,
                        col,
                        // Prevent extreme long stack trace caused by recursive call and only pass root cause
                        e.cause ?: e,
                    )
                } else {
                    // line/col cannot be reliably mapped as exception might originate from a node not present in the
                    // original AST
                    throw RuleExecutionException(
                        rule,
                        0,
                        0,
                        // Prevent extreme long stack trace caused by recursive call and only pass root cause
                        e.cause ?: e,
                    )
                }
            }
        }
        node
            .getChildren(null)
            .forEach { childNode ->
                executeRulesOnNodeRecursively(
                    childNode,
                    rules.filter { it.shouldContinueTraversalOfAST() },
                    autocorrectHandler,
                    emitAndApprove,
                )
            }
        rules.forEach { rule ->
            if (node.parent == null && node.elementType != FILE) {
                // In commit 729a0fe23c34a8d03fd06854484fe24571b92bd6 the rule execution order has been changed to process all rules on an
                // ASTNode before proceeding to the next node. Only when testing the Ktlint 2.0 SNAPSHOT with the Ktlint Intellij Plugin it
                // was found this in a particular case led to a regression error when running the plugin with an older Ktlint ruleset
                // version. Similar problems could also occur with custom rulesets.
                // In case the entire node has been replaced by a previous rule, for example by using function rawReplaceWithText(text) on a
                // LeafElement then the reference to the parent element of that element is set to null. Also, other fields of the node may
                // be initialized to a default value. This could result for example result in Null Pointer Exception whenever a next rule is
                // inspecting the element type of the parent of the node. Normally this would be resolved by fixing that rule to check for a
                // non-null parent. However, older Ktlint rulesets are not maintained, and can therefore not be fixed.
                // Hack solution is to stop processing of the node once it is identified that it is likely that the node has been replaced.
                return
            }
            try {
                if (!suppressionLocator.suppress(rootNode, node.startOffset, rule)) {
                    rule.afterVisitChildNodes(node) { offset, errorMessage, canBeAutoCorrected ->
                        emitAndApprove(offset, rule.ruleId, errorMessage, canBeAutoCorrected)
                    }
                }
            } catch (e: Throwable) {
                if (autocorrectHandler is NoneAutocorrectHandler) {
                    val (line, col) = positionInTextLocator(node.startOffset)
                    throw RuleExecutionException(
                        rule,
                        line,
                        col,
                        // Prevent extreme long stack trace caused by recursive call and only pass root cause
                        e.cause ?: e,
                    )
                } else {
                    // line/col cannot be reliably mapped as exception might originate from a node not present in the
                    // original AST
                    throw RuleExecutionException(
                        rule,
                        0,
                        0,
                        // Prevent extreme long stack trace caused by recursive call and only pass root cause
                        e.cause ?: e,
                    )
                }
            }
        }
    }

    companion object {
        internal fun createRuleExecutionContext(
            ktLintRuleEngine: KtLintRuleEngine,
            code: Code,
        ): RuleExecutionContext {
            val normalizedText = normalizeText(code.content)
            val positionInTextLocator = buildPositionInTextLocator(normalizedText)

            val psiFileName =
                code
                    .filePath
                    ?.pathString
                    ?: if (code.script) {
                        "File.kts"
                    } else {
                        "File.kt"
                    }
            val rootNode =
                KtlintKotlinCompiler
                    .createPsiFileFromText(psiFileName, normalizedText)
                    .also {
                        // Throw exception when PSI contains an error element
                        it
                            .findErrorElement()
                            ?.let { errorElement ->
                                val (line, col) = positionInTextLocator(errorElement.textOffset)
                                throw KtLintParseException(line, col, errorElement.errorDescription)
                            }
                    }.node

            val editorConfig =
                ktLintRuleEngine
                    .editorConfigLoader
                    .load(code.filePath)

            val ruleProviders =
                ktLintRuleEngine
                    .applyRuleFilters(
                        InternalRuleProvidersFilter(ktLintRuleEngine),
                        RuleExecutionRuleFilter(editorConfig),
                    )

            return RuleExecutionContext(
                code,
                rootNode,
                ruleProviders,
                editorConfig,
                positionInTextLocator,
            )
        }

        private fun normalizeText(text: String): String =
            text
                .replace("\r\n", "\n")
                .replace("\r", "\n")
                .let {
                    if (it.startsWith(UTF8_BOM)) {
                        it.replaceFirst(UTF8_BOM, "")
                    } else {
                        it
                    }
                }

        private fun PsiElement.findErrorElement(): PsiErrorElement? {
            if (this is PsiErrorElement) {
                return this
            }
            this.children.forEach { child ->
                val errorElement = child.findErrorElement()
                if (errorElement != null) {
                    return errorElement
                }
            }
            return null
        }
    }
}

private class RuleExecutionException(
    val rule: RuleV2,
    val line: Int,
    val col: Int,
    override val cause: Throwable,
) : Throwable(cause)
