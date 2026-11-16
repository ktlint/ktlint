package io.github.ktlint.core.rule.engine.internal

import io.github.ktlint.core.logger.api.initKtLintKLogger
import io.github.ktlint.core.rule.engine.api.Code
import io.github.ktlint.core.rule.engine.api.KtLintRuleEngine
import io.github.ktlint.core.rule.engine.api.LintError
import io.github.ktlint.core.rule.engine.core.api.AutocorrectDecision.ALLOW_AUTOCORRECT
import io.github.ktlint.core.rule.engine.core.api.AutocorrectDecision.NO_AUTOCORRECT
import io.github.ktlint.core.rule.engine.core.api.RuleV2
import io.github.ktlint.core.rule.engine.core.api.editorconfig.END_OF_LINE_PROPERTY
import io.github.oshai.kotlinlogging.KotlinLogging
import org.ec4j.core.model.PropertyType
import org.jetbrains.kotlin.util.prefixIfNot

private val LOGGER = KotlinLogging.logger {}.initKtLintKLogger()

internal class CodeFormatter(
    val ktLintRuleEngine: KtLintRuleEngine,
) {
    /**
     * Formats or lints the given [code]. The formatted code is returned.
     *
     * The [autocorrectHandler] is called for each [LintError] to determine whether the API Consumer intends to autocorrect the [LintError].
     * In case code is only to be linted, the [autocorrectHandler] should return the [NO_AUTOCORRECT] decision for each [LintError].
     *
     * The [onLintAfterMaxFormatRuns] is called for each [LintError] that is found after [maxFormatRunsPerFile] rounds of formatting have
     * not yet resolved all [LintError] that potentially can be autocorrected but still was not fixed.
     */
    fun format(
        code: Code,
        autocorrectHandler: AutocorrectHandler,
        onLintAfterMaxFormatRuns: (LintError) -> Unit = { _ -> },
        maxFormatRunsPerFile: Int,
    ): String {
        LOGGER.debug { "Starting with processing file '${code.fileNameOrStdin()}'" }

        with(RuleExecutionContext.createRuleExecutionContext(ktLintRuleEngine, code)) {
            val lineSeparator = code.determineLineSeparator(editorConfig[END_OF_LINE_PROPERTY])
            var codeContent = formattedCode(lineSeparator)
            var formatRunCount = 0
            var mutated = false
            // This is the core format-retry loop (see "Format Loop" in CLAUDE.md); the two early-exit conditions below are
            // deliberately kept as separate `break`s rather than merged into one boolean, to keep each one individually
            // documented and not risk changing this well-tested control flow for a style metric.
            @Suppress("LoopWithTooManyJumpStatements")
            do {
                LOGGER.trace { "Start formatting round ${formatRunCount + 1} on file '${code.fileNameOrStdin()}'" }
                val newErrors = format(autocorrectHandler, code)
                if (newErrors.none { it.first.canBeAutoCorrected && it.second }) {
                    // No errors found which can and may be autocorrected
                    break
                }

                // Check if the code has actually been mutated. Note: do not use the ruleErrors to determine this. In rare cases it is
                // possible that multiple rule errors negate each other and the code is eventually unchanged.
                val updatedCodeContent = formattedCode(lineSeparator)
                if (updatedCodeContent == codeContent) {
                    // Lint violations were found, but the file has not been changed.
                    LOGGER.warn {
                        "Format was not able to resolve all violations which (theoretically) can be autocorrected in file " +
                            code.filePathOrStdin()
                    }
                    break
                }
                codeContent = updatedCodeContent
                mutated = true
                formatRunCount++
            } while (formatRunCount < maxFormatRunsPerFile)
            if (mutated && formatRunCount == maxFormatRunsPerFile && autocorrectHandler !is NoAutocorrectHandler) {
                // It is unknown if the last format run introduces new lint violations which can be autocorrected. So run lint once more
                // so that the user can be informed about this correctly. The lint errors are added to the format errors so that the
                // report can indicate how many errors will be fixed when ktlint is run again.
                if (hasLintErrorsAfterFormat(NoAutocorrectHandler(onLintAfterMaxFormatRuns))) {
                    LOGGER.warn {
                        "Format was not able to resolve all violations which (theoretically) can be autocorrected in file " +
                            "${code.filePathOrStdin()} in $maxFormatRunsPerFile consecutive runs of format."
                    }
                }
            }

            return if (mutated) {
                formattedCode(lineSeparator).prefixIfNot(code.utf8Bom())
            } else {
                // None of the format runs has found
                code.content
            }.also {
                LOGGER.debug { "Finished with processing file '${code.fileNameOrStdin()}'" }
            }
        }
    }

    private fun Code.utf8Bom() =
        if (content.startsWith(KtLintRuleEngine.UTF8_BOM)) {
            KtLintRuleEngine.UTF8_BOM
        } else {
            ""
        }

    private fun RuleExecutionContext.formattedCode(lineSeparator: String) = rootNode.text.replace("\n", lineSeparator)

    private fun RuleExecutionContext.format(
        autocorrectHandler: AutocorrectHandler,
        code: Code,
    ): Set<Pair<LintError, Boolean>> {
        val errors: MutableSet<Pair<LintError, Boolean>> = mutableSetOf()
        format(VisitorProvider(ruleProviders).rules, autocorrectHandler, code).let { ruleErrors -> errors.addAll(ruleErrors) }
        return errors
    }

    private fun RuleExecutionContext.hasLintErrorsAfterFormat(noAutocorrectHandler: NoAutocorrectHandler): Boolean {
        var hasLintErrors = false
        executeRules(VisitorProvider(ruleProviders).rules, noAutocorrectHandler) { offset, ruleId, errorMessage, canBeAutoCorrected ->
            hasLintErrors = true
            val (line, col) = positionInTextLocator(offset)
            noAutocorrectHandler.autocorrectDecision(LintError(line, col, ruleId, errorMessage, canBeAutoCorrected))
        }
        return hasLintErrors
    }

    private fun RuleExecutionContext.format(
        groupedRules: List<List<RuleV2>>,
        autocorrectHandler: AutocorrectHandler,
        code: Code,
    ): Set<Pair<LintError, Boolean>> {
        val errors = mutableSetOf<Pair<LintError, Boolean>>()
        executeRules(groupedRules, autocorrectHandler) { offset, ruleId, errorMessage, canBeAutoCorrected ->
            val (line, col) = positionInTextLocator(offset)
            val lintError = LintError(line, col, ruleId, errorMessage, canBeAutoCorrected)

            // Always request the autocorrectDecision, even in case it is already known that the LintError can not be autocorrected. In
            // this way the API Consumer can still use data from the LintError for other purposes.
            autocorrectHandler
                .autocorrectDecision(lintError)
                .also { autocorrectDecision ->
                    // Ignore decision of the API Consumer in case the error can not be autocorrected
                    val autocorrect = autocorrectDecision == ALLOW_AUTOCORRECT && canBeAutoCorrected
                    if (autocorrect) {
                        // In trace mode report the violation immediately. The order in which violations are actually found might be
                        // different from the order in which they are reported. For debugging purposes it can be helpful to know the
                        // exact order in which violations are being solved.
                        LOGGER.trace { "Format violation: ${lintError.logMessage(code)}" }
                    }
                    errors.add(
                        Pair(
                            lintError,
                            // It is assumed that a rule that asks for autocorrect approval, also does correct the error.
                            autocorrect,
                        ),
                    )
                }
        }
        return errors
    }

    private fun Code.determineLineSeparator(eolEditorConfigProperty: PropertyType.EndOfLineValue): String =
        when {
            eolEditorConfigProperty == PropertyType.EndOfLineValue.crlf ||
                (eolEditorConfigProperty != PropertyType.EndOfLineValue.lf && doesNotContain('\r')) -> {
                "\r\n".also { LOGGER.trace { "line separator: ${eolEditorConfigProperty.name} --> CRLF" } }
            }

            else -> {
                "\n".also { LOGGER.trace { "line separator: ${eolEditorConfigProperty.name} --> LF" } }
            }
        }

    private fun Code.doesNotContain(char: Char) = content.lastIndexOf(char) != -1

    private fun LintError.logMessage(code: Code) =
        "${code.fileNameOrStdin()}:$line:$col: $detail ($ruleId)" +
            if (canBeAutoCorrected) {
                ""
            } else {
                " [cannot be autocorrected]"
            }
}
