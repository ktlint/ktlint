package io.github.ktlint.core.cli.reporter.plain

import io.github.ktlint.core.cli.reporter.core.api.KtlintCliError
import io.github.ktlint.core.cli.reporter.core.api.KtlintCliError.Status.BASELINE_IGNORED
import io.github.ktlint.core.cli.reporter.core.api.KtlintCliError.Status.FORMAT_IS_AUTOCORRECTED
import io.github.ktlint.core.cli.reporter.core.api.KtlintCliError.Status.KOTLIN_PARSE_EXCEPTION
import io.github.ktlint.core.cli.reporter.core.api.KtlintCliError.Status.KTLINT_RULE_ENGINE_EXCEPTION
import io.github.ktlint.core.cli.reporter.core.api.KtlintCliError.Status.LINT_CAN_BE_AUTOCORRECTED
import io.github.ktlint.core.cli.reporter.core.api.KtlintCliError.Status.LINT_CAN_NOT_BE_AUTOCORRECTED
import io.github.ktlint.core.cli.reporter.core.api.ReporterV2
import java.io.File
import java.io.PrintStream
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.toList

/**
 * Reports [KtlintCliError]s which have not been autocorrected and pints a summary (count per rule) of the violations found.
 */
public class PlainReporter(
    private val out: PrintStream,
    private val groupByFile: Boolean = false,
    private val shouldColorOutput: Boolean = false,
    private val outputColor: Color = Color.DARK_GRAY,
    private val pad: Boolean = false,
) : ReporterV2 {
    private val acc = ConcurrentHashMap<String, MutableList<KtlintCliError>>()
    private val summaryCount = ConcurrentHashMap<String, StatusCount>()

    override fun onLintError(
        file: String,
        ktlintCliError: KtlintCliError,
    ) {
        if (ktlintCliError.status != FORMAT_IS_AUTOCORRECTED) {
            if (groupByFile) {
                acc.getOrPut(file) { ArrayList() }.add(ktlintCliError)
            } else {
                ktlintCliError.printError(file)
            }
        }
        ktlintCliError.updateSummaryCount()
    }

    private fun KtlintCliError.printError(file: String) {
        val column =
            if (pad) {
                String.format("%-4s", col)
            } else {
                col
            }
        out.println("${colorFileName(file)}${":".colored()}${line}${":$column:".colored()} $detail ${"($ruleId)".colored()}")
    }

    private fun KtlintCliError.updateSummaryCount() {
        summaryCount
            .compute(causedBy()) { _, previous ->
                val statusCount = previous ?: StatusCount()
                when (status) {
                    BASELINE_IGNORED -> {
                        statusCount.copy(
                            baselineIgnored = statusCount.baselineIgnored + 1,
                            notIgnored = statusCount.notIgnored + 1,
                        )
                    }

                    LINT_CAN_NOT_BE_AUTOCORRECTED, KOTLIN_PARSE_EXCEPTION, KTLINT_RULE_ENGINE_EXCEPTION -> {
                        statusCount.copy(
                            lintCannotBeAutocorrected = statusCount.lintCannotBeAutocorrected + 1,
                            notIgnored = statusCount.notIgnored + 1,
                        )
                    }

                    LINT_CAN_BE_AUTOCORRECTED -> {
                        statusCount.copy(
                            lintCanBeAutocorrected = statusCount.lintCanBeAutocorrected + 1,
                            notIgnored = statusCount.notIgnored + 1,
                        )
                    }

                    FORMAT_IS_AUTOCORRECTED -> {
                        statusCount.copy(
                            formatIsAutocorrected = statusCount.formatIsAutocorrected + 1,
                            notIgnored = statusCount.notIgnored + 1,
                        )
                    }
                }
            }
    }

    override fun after(file: String) {
        if (groupByFile) {
            val errList = acc[file] ?: return
            out.println(colorFileName(file))
            for (err in errList) {
                with(err) {
                    val column =
                        if (pad) {
                            String.format("%-3s", col)
                        } else {
                            col
                        }
                    out.println("  $line${":$column".colored()} $detail ${"($ruleId)".colored()}")
                }
            }
        }
    }

    override fun afterAll() {
        if (summaryCount.isNotEmpty()) {
            printSummary()
        }
    }

    private fun printSummary() {
        val maxCategoryLength = maxOf(summaryCount.keys.maxOf { it.length }, CATEGORY_HEADER.length)
        val sumOfFormatIsAutocorrected = summaryCount.values.sumOf { it.formatIsAutocorrected }
        val sumOfLintCanBeAutocorrected = summaryCount.values.sumOf { it.lintCanBeAutocorrected }
        val sumOfLintCannotBeAutocorrected = summaryCount.values.sumOf { it.lintCannotBeAutocorrected }
        val sumOfTotal = summaryCount.values.sumOf { it.notIgnored }
        val separatorLine =
            "  |-${"-".repeat(maxCategoryLength)}-|---------------|----------------------|-------------------------|-------|"
        out.println("")
        out.println("Summary of violations (descending on total) by by rule:")
        out.println(separatorLine)
        out.println(
            "  | ${CATEGORY_HEADER.padEnd(maxCategoryLength)} " +
                "| $AUTOCORRECTED_HEADER " +
                "| $CAN_BE_AUTOCORRECTED_HEADER " +
                "| $CANNOT_BE_AUTOCORRECTED_HEADER " +
                "| $TOTAL_HEADER " +
                "|",
        )
        out.println(separatorLine)
        summaryCount
            .toList()
            .sortedWith(COUNT_DESC_AND_RULE_ID_ASC_COMPARATOR)
            .forEach { (category, counts) ->
                out.println(
                    "  | ${category.padEnd(maxCategoryLength)} " +
                        "| ${columnValue(counts.formatIsAutocorrected, sumOfFormatIsAutocorrected, AUTOCORRECTED_HEADER)} " +
                        "| ${columnValue(counts.lintCanBeAutocorrected, sumOfLintCanBeAutocorrected, CAN_BE_AUTOCORRECTED_HEADER)} " +
                        "| ${columnValue(
                            counts.lintCannotBeAutocorrected,
                            sumOfLintCannotBeAutocorrected,
                            CANNOT_BE_AUTOCORRECTED_HEADER,
                        )} " +
                        "| ${columnValue(counts.notIgnored, sumOfTotal, TOTAL_HEADER)} " +
                        "|",
                )
            }
        out.println(separatorLine)
        out.println(
            "  | ${"Total".padEnd(maxCategoryLength)} " +
                "| ${columnValue(sumOfFormatIsAutocorrected, sumOfFormatIsAutocorrected, AUTOCORRECTED_HEADER)} " +
                "| ${columnValue(sumOfLintCanBeAutocorrected, sumOfLintCanBeAutocorrected, CAN_BE_AUTOCORRECTED_HEADER)} " +
                "| ${columnValue(sumOfLintCannotBeAutocorrected, sumOfLintCannotBeAutocorrected, CANNOT_BE_AUTOCORRECTED_HEADER)} " +
                "| ${columnValue(sumOfTotal, sumOfTotal, TOTAL_HEADER)} " +
                "|",
        )
        out.println(separatorLine)
    }

    private fun columnValue(
        value: Long,
        maxValue: Long,
        columnHeader: String,
    ): String {
        val stringValue = value.toString()
        val columnWidth = columnHeader.length
        val maxValueWidth = maxValue.toString().length
        val padColumnRight = maxOf(0, columnWidth - maxValueWidth) / 2
        val padValueLeft = maxOf(0, maxValueWidth - stringValue.length)
        val padColumnLeft = maxOf(0, columnWidth - padColumnRight - maxValueWidth)
        return " ".repeat(padColumnLeft + padValueLeft) + stringValue + " ".repeat(padColumnRight)
    }

    private fun colorFileName(fileName: String): String {
        val name = fileName.substringAfterLast(File.separator)
        return fileName.substring(0, fileName.length - name.length).colored() + name
    }

    private fun String.colored() =
        if (shouldColorOutput) {
            this.color(outputColor)
        } else {
            this
        }

    // Collect by exception, or when no exception by rule id
    private fun KtlintCliError.causedBy() =
        when (status) {
            KOTLIN_PARSE_EXCEPTION -> KOTLIN_PARSE_EXCEPTION_MESSAGE
            KTLINT_RULE_ENGINE_EXCEPTION -> KTLINT_RULE_ENGINE_EXCEPTION_MESSAGE
            else -> ruleId.ifEmpty { UNKNOWN_CAUSE_MESSAGE }
        }

    private data class StatusCount(
        val baselineIgnored: Long = 0,
        val lintCannotBeAutocorrected: Long = 0,
        val lintCanBeAutocorrected: Long = 0,
        val formatIsAutocorrected: Long = 0,
        val notIgnored: Long = 0,
    )

    private companion object {
        val COUNT_DESC_AND_RULE_ID_ASC_COMPARATOR =
            Comparator<Pair<String, StatusCount>> { left, right ->
                compareValuesBy(left, right) { it.second.notIgnored }
            }.reversed()
                .thenComparator { left, right ->
                    compareValuesBy(left, right) { it.first }
                }

        const val KOTLIN_PARSE_EXCEPTION_MESSAGE = "Not a valid Kotlin file"
        const val KTLINT_RULE_ENGINE_EXCEPTION_MESSAGE = "An internal error occurred in the Ktlint Rule Engine"
        const val UNKNOWN_CAUSE_MESSAGE = "Unknown"

        const val CATEGORY_HEADER = "Category"
        const val AUTOCORRECTED_HEADER = "Autocorrected"
        const val CAN_BE_AUTOCORRECTED_HEADER = "Can be autocorrected"
        const val CANNOT_BE_AUTOCORRECTED_HEADER = "Cannot be autocorrected"
        const val TOTAL_HEADER = "Total"
    }
}

internal fun String.color(foreground: Color): String = "\u001B[${foreground.code}m$this\u001B[0m"
