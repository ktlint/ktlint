package io.github.ktlint.core.cli.internal

import io.github.ktlint.core.cli.reporter.baseline.Baseline
import io.github.ktlint.core.cli.reporter.baseline.doesNotContain
import io.github.ktlint.core.cli.reporter.core.api.KtlintCliError
import io.github.ktlint.core.cli.reporter.core.api.KtlintCliError.Status.FORMAT_IS_AUTOCORRECTED
import io.github.ktlint.core.cli.reporter.core.api.KtlintCliError.Status.KOTLIN_PARSE_EXCEPTION
import io.github.ktlint.core.cli.reporter.core.api.KtlintCliError.Status.KTLINT_RULE_ENGINE_EXCEPTION
import io.github.ktlint.core.cli.reporter.core.api.KtlintCliError.Status.LINT_CAN_BE_AUTOCORRECTED
import io.github.ktlint.core.cli.reporter.core.api.KtlintCliError.Status.LINT_CAN_NOT_BE_AUTOCORRECTED
import io.github.ktlint.core.cli.reporter.core.api.ReporterV2
import io.github.ktlint.core.rule.engine.api.Code
import io.github.ktlint.core.rule.engine.api.KtLintParseException
import io.github.ktlint.core.rule.engine.api.KtLintRuleEngine
import io.github.ktlint.core.rule.engine.api.KtLintRuleException
import io.github.ktlint.core.rule.engine.api.LintError
import io.github.ktlint.core.rule.engine.core.api.AutocorrectDecision
import io.github.ktlint.core.rule.engine.core.api.AutocorrectDecision.ALLOW_AUTOCORRECT
import io.github.ktlint.core.rule.engine.core.api.AutocorrectDecision.NO_AUTOCORRECT
import org.jetbrains.kotlin.utils.addToStdlib.applyIf
import java.io.File
import java.io.OutputStreamWriter
import java.io.PrintWriter
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.util.Locale
import java.util.concurrent.Callable
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import kotlin.text.Charsets.UTF_8

internal class LintAndFormatProcessor(
    private val format: Boolean,
    private val ignoreAutocorrectFailures: Boolean,
    private val forceLintAfterFormat: Boolean,
    private val rerunFormatAfterAutocorrect: Boolean,
    private val relative: Boolean,
    private val limit: Int,
    private val ktLintRuleEngine: KtLintRuleEngine,
    private val baseline: Baseline,
    private val reporter: ReporterV2,
) {
    private val containsUnfixedLintErrors = AtomicBoolean()
    private val fileNumber = AtomicInteger()
    private val errorNumber = AtomicInteger()
    private val adviseToUseFormat = AtomicBoolean()

    internal fun run(
        stdinCode: Code?,
        patterns: List<String>,
    ) {
        val start = System.currentTimeMillis()
        reporter.beforeAll()
        if (stdinCode != null) {
            lintStdin(stdinCode)
        } else {
            lintFiles(patterns)
            if (adviseToUseFormat.get()) {
                if (format) {
                    logger.error { "Format was not able to autocorrect all errors that theoretically can be autocorrected." }
                } else {
                    logger.warn { "Lint has found errors than can be autocorrected using 'ktlint --format'" }
                }
            }
        }
        reporter.afterAll()
        logger.debug {
            "Finished processing in ${System.currentTimeMillis() - start}ms / ${fileNumber.get()} file(s) scanned / ${errorNumber.get()} error(s) found"
        }
        if (fileNumber.get() == 0) {
            // Do not return an error as this would implicate that in a multi-module project, each module has to contain
            // at least one Kotlin file.
            logger.warn { "No files matched $patterns" }
        }
        if (containsUnfixedLintErrors.get()) {
            exitKtLintProcess(ExitCode.HAS_UNFIXED_LINT_ERRORS_AFTER_FORMAT)
        } else {
            exitKtLintProcess(ExitCode.OK)
        }
    }

    private fun lintFiles(patterns: List<String>) {
        FileSystems
            .getDefault()
            .fileSequence(patterns)
            .map { it.toFile() }
            .takeWhile { errorNumber.get() < limit }
            .map { file ->
                Callable {
                    file to
                        process(
                            code = Code.fromFile(file),
                            baselineLintErrors =
                                baseline
                                    .lintErrorsPerFile
                                    .getOrDefault(
                                        // Baseline stores the lint violations as relative path to work dir
                                        file.location(true),
                                        emptyList(),
                                    ),
                        )
                }
            }.parallel({ (file, errList) -> report(file.location(relative), errList) })
    }

    private fun process(
        code: Code,
        baselineLintErrors: List<KtlintCliError>,
    ): List<KtlintCliError> {
        if (code.fileName != null) {
            logger.trace { "Checking ${File(code.fileName!!).location(relative)}" }
        }
        return if (format) {
            formatCode(code, baselineLintErrors)
        } else {
            lintCode(code, baselineLintErrors)
        }
    }

    private fun formatCode(
        code: Code,
        baselineLintErrors: List<KtlintCliError>,
    ): List<KtlintCliError> {
        val ktlintCliErrors = mutableListOf<KtlintCliError>()
        try {
            val formatAutocorrectDecisionHandler = createFormatAutocorrectDecisionHandler(baselineLintErrors, ktlintCliErrors)
            ktLintRuleEngine
                .format(code, rerunAfterAutocorrect = rerunFormatAfterAutocorrect, callback = formatAutocorrectDecisionHandler)
                .also { formattedCode -> processFormatResults(code, formattedCode, ktlintCliErrors) }
        } catch (e: KtLintParseException) {
            if (code.isStdIn) {
                if (code.script) {
                    // When reading from stdin, code is only parsed as Kotlin script, if it could not be parsed as pure Kotlin. Now
                    // parsing of the code has failed for both, the file has to be ignored.
                    logger.error {
                        """
                        Can not parse input from <stdin> as Kotlin script, due to error below:
                            ${e.toKtlintCliError(code).detail}
                        """.trimIndent()
                    }
                    // Write original code back to stdout before exiting with a non-zero error code. This is useful in case a tool that
                    // integrates with Ktlint CLI without checking the exit code does not replace the original content with an empty
                    // stdout but just with the original content instead.
                    writeToStdout(code.content)
                    exitKtLintProcess(ExitCode.PARSE_EXCEPTION_STDIN)
                } else {
                    // When reading from stdin, it is first assumed that the provided code is pure Kotlin instead of Kotlin script. If
                    // parsing fails, retry parsing at Kotlin script.
                    logger.warn {
                        """
                        Can not parse input from <stdin> as Kotlin, due to error below:
                            ${e.toKtlintCliError(code).detail}
                        Now, trying to read the input as Kotlin Script.
                        """.trimIndent()
                    }
                    return formatCode(
                        code = Code.fromSnippet(code.content, script = true),
                        baselineLintErrors = baselineLintErrors,
                    )
                }
            } else {
                if (!ignoreAutocorrectFailures) {
                    ktlintCliErrors.add(e.toKtlintCliError(code))
                    containsUnfixedLintErrors.set(true)
                }
                code.content // making sure `cat file | ktlint --stdin > file` is (relatively) safe
            }
        } catch (
            // Third party rules can throw any exception or error
            @Suppress("TooGenericExceptionCaught")
            t: Throwable,
        ) {
            if (code.isStdIn) {
                // Write original code back to stdout before exiting with a non-zero error code. This is useful in case a tool that
                // integrates with Ktlint CLI without checking the exit code does not replace the original content with an empty
                // stdout but just with the original content instead.
                writeToStdout(code.content)
                logger.error(t) {}
                exitKtLintProcess(ExitCode.EXCEPTION_STDIN)
            } else {
                if (!ignoreAutocorrectFailures) {
                    ktlintCliErrors.add(t.toKtlintCliError(code))
                    containsUnfixedLintErrors.set(true)
                }
                code.content // making sure `cat file | ktlint --stdin > file` is (relatively) safe
            }
        }
        return ktlintCliErrors.toList()
    }

    private fun processFormatResults(
        code: Code,
        formattedFileContent: String,
        ktlintCliErrors: List<KtlintCliError>,
    ) {
        if (ktlintCliErrors.isNotEmpty() && code.content == formattedFileContent) {
            // In very rare cases it is possible that Lint violations are detected but that they have opposite effects and the
            // file gets not altered at all. This is to be treated as unfixable error.
            containsUnfixedLintErrors.set(true)
        }
        if (forceLintAfterFormat && code.content != formattedFileContent) {
            // Rerun lint to check that the formatted code can still be successfully parsed.
            try {
                ktLintRuleEngine.lint(Code.fromSnippet(formattedFileContent, code.script))
            } catch (e: KtLintParseException) {
                logger.error(e) {
                    "After formatting code in file '${code.filePath}' it cannot be successfully parsed anymore.\n"
                        .plus(
                            if (logger.isDebugEnabled()) {
                                """
                                Content of file which cannot be parsed:
                                ---
                                $formattedFileContent
                                ---
                                """.trimIndent()
                                // .prependIndent("    ")
                            } else {
                                "Run again with '--log-level=trace' to log the formatting result which cannot be parsed."
                            },
                        )
                }
                // Intentionally stop formatting other files. The '--force-lint-after-format' is only supposed to be used during
                // release testing of Ktlint CLI. An immediate exit forces to fix the issue immediately as release test can
                // otherwise not be completed.
                exitKtLintProcess(ExitCode.PARSE_EXCEPTION_AFTER_FORMAT)
            }
        }
        when {
            code.isStdIn -> {
                writeToStdout(formattedFileContent)
            }

            code.content != formattedFileContent -> {
                replaceFile(code, formattedFileContent)
            }
        }
    }

    private fun createFormatAutocorrectDecisionHandler(
        baselineLintErrors: List<KtlintCliError>,
        ktlintCliErrors: MutableList<KtlintCliError>,
    ): (LintError) -> AutocorrectDecision =
        { lintError ->
            if (ignoreAutocorrectFailures && !lintError.canBeAutoCorrected) {
                NO_AUTOCORRECT
            } else {
                lintError
                    .toFormatKtlintCliError()
                    .takeIf { baselineLintErrors.doesNotContain(it) }
                    ?.let { ktlintCliError ->
                        ktlintCliErrors.add(ktlintCliError)
                        if (lintError.canBeAutoCorrected) {
                            ALLOW_AUTOCORRECT
                        } else {
                            containsUnfixedLintErrors.set(true)
                            NO_AUTOCORRECT
                        }
                    } ?: NO_AUTOCORRECT
            }
        }

    private fun LintError.toFormatKtlintCliError(): KtlintCliError =
        KtlintCliError(
            line = line,
            col = col,
            ruleId = ruleId.value,
            detail = detail.applyIf(!canBeAutoCorrected) { "$this (cannot be auto-corrected)" },
            status =
                if (canBeAutoCorrected) {
                    FORMAT_IS_AUTOCORRECTED
                } else {
                    LINT_CAN_NOT_BE_AUTOCORRECTED
                },
        )

    private fun writeToStdout(content: String) {
        // Avoid Unicode characters on Windows OS to be malformed as stdout does not default to UTF-8 (#3133).
        with(PrintWriter(OutputStreamWriter(System.out, UTF_8), true)) {
            // Do not use print, print which invoke the format functions which will malfunction when the input file
            // contains a '%' character (#3278).
            write(content)
            // The write method does not autoflush.
            flush()
        }
    }

    private fun replaceFile(
        code: Code,
        formattedFileContent: String,
    ) {
        code
            .filePath
            ?.toFile()
            ?.let { target ->
                // Write output first to temporary file. Only when this write has succeeded than replace the original file with the
                // temporary file. This should prevent replacing the original file with empty contents in case an IO error occurs between
                // truncating the original content and writing the new content.
                val tmp = File(target.parent, "${target.name}.tmp")
                try {
                    tmp.writeText(formattedFileContent, UTF_8)
                    Files.move(
                        tmp.toPath(),
                        target.toPath(),
                        StandardCopyOption.REPLACE_EXISTING,
                        StandardCopyOption.ATOMIC_MOVE,
                    )
                } catch (
                    @Suppress("TooGenericExceptionCaught") e: Exception,
                ) {
                    tmp.delete()
                    throw e
                }
            }
    }

    private fun Throwable.toKtlintCliError(code: Code): KtlintCliError =
        this.let { e ->
            when (e) {
                is KtLintParseException -> {
                    KtlintCliError(
                        line = e.line,
                        col = e.col,
                        ruleId = "",
                        detail = "Not a valid Kotlin file (${e.message?.lowercase(Locale.getDefault())})",
                        status = KOTLIN_PARSE_EXCEPTION,
                    )
                }

                is KtLintRuleException -> {
                    logger.debug(e) { "Internal Error (${e.ruleId}) in ${code.fileNameOrStdin()} at position '${e.line}:${e.col}" }
                    KtlintCliError(
                        line = e.line,
                        col = e.col,
                        ruleId = "",
                        detail =
                            "Internal Error (rule '${e.ruleId}') in ${code.fileNameOrStdin()} at position '${e.line}:${e.col}. Please " +
                                "create a ticket at https://github.com/ktlint/ktlint/issues and provide the source code that " +
                                "triggered an error.\n" +
                                e.stackTraceToString(),
                        status = KTLINT_RULE_ENGINE_EXCEPTION,
                    )
                }

                else -> {
                    throw e
                }
            }
        }

    private fun lintCode(
        code: Code,
        baselineLintErrors: List<KtlintCliError>,
    ): List<KtlintCliError> {
        val ktlintCliErrors = mutableListOf<KtlintCliError>()
        try {
            ktLintRuleEngine.lint(code) { lintError ->
                val ktlintCliError = toLintKtlintCliError(lintError)
                if (baselineLintErrors.doesNotContain(ktlintCliError) && !ignoreAutocorrectFailures) {
                    ktlintCliErrors.add(ktlintCliError)
                    containsUnfixedLintErrors.set(true)
                }
            }
        } catch (e: KtLintParseException) {
            if (code.isStdIn) {
                if (code.script) {
                    logger.error {
                        """
                        Can not parse input from <stdin> as Kotlin script, due to error below:
                            ${e.toKtlintCliError(code).detail}
                        """.trimIndent()
                    }
                    exitKtLintProcess(ExitCode.PARSE_EXCEPTION_STDIN)
                } else {
                    logger.warn {
                        """
                        Can not parse input from <stdin> as Kotlin, due to error below:
                            ${e.toKtlintCliError(code).detail}
                        Now, trying to read the input as Kotlin Script.
                        """.trimIndent()
                    }
                    return lintCode(
                        code = Code.fromSnippet(code.content, script = true),
                        baselineLintErrors = baselineLintErrors,
                    )
                }
            } else if (!ignoreAutocorrectFailures) {
                ktlintCliErrors.add(e.toKtlintCliError(code))
                containsUnfixedLintErrors.set(true)
            }
        } catch (
            // Third party rules can throw any exception or error
            @Suppress("TooGenericExceptionCaught")
            t: Throwable,
        ) {
            if (code.isStdIn) {
                logger.error(t) {}
                exitKtLintProcess(ExitCode.EXCEPTION_STDIN)
            } else if (!ignoreAutocorrectFailures) {
                ktlintCliErrors.add(t.toKtlintCliError(code))
                containsUnfixedLintErrors.set(true)
            }
        }
        return ktlintCliErrors.toList()
    }

    private fun toLintKtlintCliError(lintError: LintError): KtlintCliError =
        KtlintCliError(
            line = lintError.line,
            col = lintError.col,
            ruleId = lintError.ruleId.value,
            detail = lintError.detail,
            status =
                if (lintError.canBeAutoCorrected) {
                    LINT_CAN_BE_AUTOCORRECTED
                } else {
                    LINT_CAN_NOT_BE_AUTOCORRECTED
                },
        )

    private fun report(
        relativeRoute: String,
        ktlintCliErrors: List<KtlintCliError>,
    ) {
        fileNumber.incrementAndGet()
        val errListLimit = minOf(ktlintCliErrors.size, maxOf(limit - errorNumber.get(), 0))
        errorNumber.addAndGet(errListLimit)
        if (!adviseToUseFormat.get() && ktlintCliErrors.containsErrorThatCanBeAutocorrected()) {
            adviseToUseFormat.set(true)
        }

        reporter.before(relativeRoute)
        ktlintCliErrors
            .take(errListLimit)
            .filterNot { ignoreAutocorrectFailures && it.status == LINT_CAN_NOT_BE_AUTOCORRECTED }
            .forEach { reporter.onLintError(relativeRoute, it) }
        reporter.after(relativeRoute)
    }

    private fun List<KtlintCliError>.containsErrorThatCanBeAutocorrected() = any { it.status == LINT_CAN_BE_AUTOCORRECTED }

    private fun lintStdin(code: Code) {
        report(
            KtLintRuleEngine.STDIN_FILE,
            process(
                code = code,
                baselineLintErrors = emptyList(),
            ),
        )
    }
}
