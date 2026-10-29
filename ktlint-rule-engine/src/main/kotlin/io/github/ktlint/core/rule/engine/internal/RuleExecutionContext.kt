package io.github.ktlint.core.rule.engine.internal

import com.intellij.lang.ASTNode
import com.intellij.lang.FileASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiErrorElement
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
import io.github.ktlint.core.rule.engine.core.api.nextSibling
import io.github.ktlint.core.rule.engine.core.api.parent
import io.github.ktlint.core.rule.engine.core.api.prevSibling
import io.github.ktlint.core.rule.engine.internal.rulefilter.InternalRuleProvidersFilter
import io.github.ktlint.core.rule.engine.internal.rulefilter.RuleExecutionRuleFilter
import io.github.ktlint.core.rule.engine.internal.rulefilter.applyRuleFilters
import io.github.ktlint.core.rule.engine.internal.rules.KTLINT_SUPPRESSION_RULE_ID
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
                rule.startTraversalOfAST(emitAndApprove)
                rule.execute { it.beforeFirstNode(it.editorConfig()) }
            }
            val visitChildNodesResult =
                executeRulesOnNodeRecursively(
                    rootNode,
                    rules.filter { it.shouldContinueTraversalOfAST() },
                    autocorrectHandler,
                    emitAndApprove,
                )
            if (visitChildNodesResult == VisitChildNodesResult.CONTINUE) {
                rules.forEach { rule ->
                    rule.execute { it.afterLastNode() }
                }
            }
        } catch (
            // e itself (the internal-only RuleExecutionException wrapper) is intentionally not chained; e.cause below is
            // the real original exception, and chaining e instead would only add a meaningless private type to the
            // cause chain exposed to API consumers.
            @Suppress("SwallowedException")
            e: RuleExecutionException,
        ) {
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

    // Third-party rule implementations can throw anything; the generic catch-all below and the resulting three throw
    // sites (two rethrows to preserve known exception types, one wrap for everything else) are the point of this
    // function, not accidental complexity.
    @Suppress("TooGenericExceptionCaught", "ThrowsCount")
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
    ): VisitChildNodesResult =
        if (visitBeforeChildNodesForAllRules(rules, node, autocorrectHandler) == VisitChildNodesResult.CONTINUE &&
            visitChildrenRecursively(node, rules, autocorrectHandler, emitAndApprove) == VisitChildNodesResult.CONTINUE &&
            visitAfterChildNodesForAllRules(rules, node, autocorrectHandler) == VisitChildNodesResult.CONTINUE
        ) {
            VisitChildNodesResult.CONTINUE
        } else {
            VisitChildNodesResult.STOP
        }

    private fun visitBeforeChildNodesForAllRules(
        rules: List<RuleV2>,
        node: ASTNode,
        autocorrectHandler: AutocorrectHandler,
    ): VisitChildNodesResult {
        // Do not replace the "for" loop with "forEach" as the latter cannot return from the function.
        for (rule in rules) {
            visitChildNodes(node, rule, autocorrectHandler) {
                rule.beforeVisitChildNodes(node)
            }
            if (node.hasBeenReplaced()) {
                // Immediate stop processing other rules on the same node.
                return VisitChildNodesResult.STOP
            }
        }
        return VisitChildNodesResult.CONTINUE
    }

    private fun visitChildrenRecursively(
        node: ASTNode,
        rules: List<RuleV2>,
        autocorrectHandler: AutocorrectHandler,
        emitAndApprove: (Int, RuleId, String, Boolean) -> AutocorrectDecision,
    ): VisitChildNodesResult {
        // Process all children (recursively). In case processing of a childNode has stopped, keep on processing the other child nodes.
        var childNodesResult = VisitChildNodesResult.CONTINUE
        node
            .getChildren(null)
            .forEach { childNode ->
                if (childNode.hasBeenReplaced()) {
                    // Child was replaced with a new AST node while processing an earlier child node. Keep on processing the other child
                    // nodes
                    childNodesResult = VisitChildNodesResult.STOP
                } else {
                    // In case processing of a childNode has stopped, keep on processing the other child nodes.
                    childNodesResult +=
                        executeRulesOnNodeRecursively(
                            childNode,
                            rules.filter { it.shouldContinueTraversalOfAST() },
                            autocorrectHandler,
                            emitAndApprove,
                        )
                }
            }
        return childNodesResult
    }

    private fun visitAfterChildNodesForAllRules(
        rules: List<RuleV2>,
        node: ASTNode,
        autocorrectHandler: AutocorrectHandler,
    ): VisitChildNodesResult {
        // Do not replace the "for" loop with "forEach" as the latter cannot return from the function.
        for (rule in rules) {
            visitChildNodes(node, rule, autocorrectHandler) {
                rule.afterVisitChildNodes(node)
            }
            if (node.hasBeenReplaced()) {
                // Immediate stop processing other rules on the same node.
                return VisitChildNodesResult.STOP
            }
        }
        return VisitChildNodesResult.CONTINUE
    }

    private fun visitChildNodes(
        node: ASTNode,
        rule: RuleV2,
        autocorrectHandler: AutocorrectHandler,
        childNodesVisitor: () -> Unit,
    ) {
        try {
            if (!suppressionLocator.suppress(rootNode, node.startOffset, rule)) {
                childNodesVisitor()
            }
        } catch (
            @Suppress("TooGenericExceptionCaught") t: Throwable,
        ) {
            val (line, col) =
                if (autocorrectHandler is NoneAutocorrectHandler) {
                    positionInTextLocator(node.startOffset)
                } else {
                    // line/col cannot be reliably mapped as exception might originate from a node not present in the original AST
                    Pair(0, 0)
                }
            throw RuleExecutionException(
                rule,
                line,
                col,
                // Prevent extreme long stack trace caused by recursive call and only pass root cause
                t.cause ?: t,
            )
        }
    }

    private fun ASTNode.hasBeenReplaced() =
        // When references below are set to null this means that the node in the original AST has been replaced with a new node. Any further
        // processing of this node might result in unexpected problems.
        elementType != FILE && nextSibling == null && prevSibling == null && parent == null

    private enum class VisitChildNodesResult {
        STOP,
        CONTINUE,
        ;

        operator fun plus(other: VisitChildNodesResult): VisitChildNodesResult =
            if (this == CONTINUE && other == CONTINUE) {
                CONTINUE
            } else {
                STOP
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

        private fun PsiElement.findErrorElement(): PsiErrorElement? =
            if (this is PsiErrorElement) {
                this
            } else {
                children.firstNotNullOfOrNull { it.findErrorElement() }
            }
    }
}

private class RuleExecutionException(
    val rule: RuleV2,
    val line: Int,
    val col: Int,
    override val cause: Throwable,
) : Throwable(cause)
