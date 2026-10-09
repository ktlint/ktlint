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
    fun format(
        code: Code,
        autocorrectHandler: AutocorrectHandler,
        callback: (LintError, Boolean) -> Unit = { _, _ -> },
        maxFormatRunsPerFile: Int,
    ): String {
        LOGGER.debug { "Starting with processing file '${code.fileNameOrStdin()}'" }

        val (formattedCode, errors) = format(code, autocorrectHandler, maxFormatRunsPerFile)

        errors
            .sortedWith(lintErrorLineAndColumnComparator { it.first })
            .forEach { (e, corrected) -> callback(e, corrected) }

        return (formattedCode.prefixIfNot(code.utf8Bom())).also {
            LOGGER.debug { "Finished with processing file '${code.fileNameOrStdin()}'" }
        }
    }

    private fun Code.utf8Bom() =
        if (content.startsWith(KtLintRuleEngine.UTF8_BOM)) {
            KtLintRuleEngine.UTF8_BOM
        } else {
            ""
        }

    private fun format(
        code: Code,
        autocorrectHandler: AutocorrectHandler,
        maxFormatRunsPerFile: Int,
    ): Pair<String, Set<Pair<LintError, Boolean>>> {
        with(RuleExecutionContext.createRuleExecutionContext(ktLintRuleEngine, code)) {
            val lineSeparator = code.determineLineSeparator(editorConfig[END_OF_LINE_PROPERTY])
            var codeContent = formattedCode(lineSeparator)
            val errors = mutableSetOf<Pair<LintError, Boolean>>()
            var formatRunCount = 0
            var mutated = false
            // This is the core format-retry loop (see "Format Loop" in CLAUDE.md); the two early-exit conditions below are
            // deliberately kept as separate `break`s rather than merged into one boolean, to keep each one individually
            // documented and not risk changing this well-tested control flow for a style metric.
            @Suppress("LoopWithTooManyJumpStatements")
            do {
                val newErrors = format(autocorrectHandler, code)
                errors.addAll(newErrors)
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
            if (mutated && formatRunCount == maxFormatRunsPerFile && autocorrectHandler !is NoneAutocorrectHandler) {
                // It is unknown if the last format run introduces new lint violations which can be autocorrected. So run lint once more
                // so that the user can be informed about this correctly.
                lintAfterFormat().also {
                    LOGGER.warn {
                        "Format was not able to resolve all violations which (theoretically) can be autocorrected in file " +
                            "${code.filePathOrStdin()} in $maxFormatRunsPerFile consecutive runs of format."
                    }
                }
            }
            return if (mutated) {
                Pair(formattedCode(lineSeparator), errors)
            } else {
                // None of the format runs has found
                Pair(code.content, errors)
            }
        }
    }

    private fun RuleExecutionContext.formattedCode(lineSeparator: String) = rootNode.text.replace("\n", lineSeparator)

    private fun RuleExecutionContext.format(
        autocorrectHandler: AutocorrectHandler,
        code: Code,
    ): Set<Pair<LintError, Boolean>> {
        val errors: MutableSet<Pair<LintError, Boolean>> = mutableSetOf()
        executeRules(VisitorProvider(ruleProviders).rules, autocorrectHandler, code).let { ruleErrors -> errors.addAll(ruleErrors) }
        return errors
    }

    private fun RuleExecutionContext.lintAfterFormat(): Boolean {
        var hasErrorsWhichCanBeAutocorrected = false
        executeRules(VisitorProvider(ruleProviders).rules, NoneAutocorrectHandler) { _, _, _, canBeAutoCorrected ->
            if (canBeAutoCorrected) {
                hasErrorsWhichCanBeAutocorrected = true
            }
            // No need to ask for approval in lint mode
            NO_AUTOCORRECT
        }
        return hasErrorsWhichCanBeAutocorrected
    }

    private fun RuleExecutionContext.executeRules(
        rules: List<RuleV2>,
        autocorrectHandler: AutocorrectHandler,
        code: Code,
    ): Set<Pair<LintError, Boolean>> {
        val errors = mutableSetOf<Pair<LintError, Boolean>>()
        executeRules(rules, autocorrectHandler) { offset, ruleId, errorMessage, canBeAutoCorrected ->
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

    private fun <T> lintErrorLineAndColumnComparator(transformer: (T) -> LintError) =
        compareBy<T> { transformer(it).line }
            .then(compareBy { transformer(it).col })

    private fun LintError.logMessage(code: Code) =
        "${code.fileNameOrStdin()}:$line:$col: $detail ($ruleId)" +
            if (canBeAutoCorrected) {
                ""
            } else {
                " [cannot be autocorrected]"
            }
}
