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
                val column =
                    if (pad) {
                        String.format("%-4s", ktlintCliError.col)
                    } else {
                        ktlintCliError.col
                    }
                out.println(
                    "${colorFileName(file)}${":".colored()}${ktlintCliError.line}${":$column:".colored()} " +
                        "${ktlintCliError.detail} ${"(${ktlintCliError.ruleId})".colored()}",
                )
            }
        }
        summaryCount
            .compute(ktlintCliError.causedBy()) { _, previous ->
                val statusCount = previous ?: StatusCount()
                when (ktlintCliError.status) {
                    BASELINE_IGNORED -> {
                        statusCount.copy(
                            countBaselineIgnored = statusCount.countBaselineIgnored + 1,
                            countNotIgnored = statusCount.countNotIgnored + 1,
                        )
                    }

                    LINT_CAN_NOT_BE_AUTOCORRECTED, KOTLIN_PARSE_EXCEPTION, KTLINT_RULE_ENGINE_EXCEPTION -> {
                        statusCount.copy(
                            countLintCannotBeAutocorrected = statusCount.countLintCannotBeAutocorrected + 1,
                            countNotIgnored = statusCount.countNotIgnored + 1,
                        )
                    }

                    LINT_CAN_BE_AUTOCORRECTED -> {
                        statusCount.copy(
                            countLintCanBeAutocorrected = statusCount.countLintCanBeAutocorrected + 1,
                            countNotIgnored = statusCount.countNotIgnored + 1,
                        )
                    }

                    FORMAT_IS_AUTOCORRECTED -> {
                        statusCount.copy(
                            countFormatIsAutocorrected = statusCount.countFormatIsAutocorrected + 1,
                            countNotIgnored = statusCount.countNotIgnored + 1,
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
                    out.println(
                        "  $line${":$column".colored()} $detail ${"($ruleId)".colored()}",
                    )
                }
            }
        }
    }

    override fun afterAll() {
        if (summaryCount.isNotEmpty()) {
            val maxCategoryLength = maxOf(summaryCount.keys.maxOf { it.length }, CATEGORY_HEADER.length)
            val sumOfFormatIsAutocorrectedLength = summaryCount.values.sumOf { it.countFormatIsAutocorrected }
            val sumOfLintCanBeAutocorrected = summaryCount.values.sumOf { it.countLintCanBeAutocorrected }
            val sumOfLintCannotBeAutocorrected = summaryCount.values.sumOf { it.countLintCannotBeAutocorrected }
            val sumOfTotalLength = summaryCount.values.sumOf { it.countNotIgnored }
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
                            "| ${columnValue(
                                counts.countFormatIsAutocorrected,
                                sumOfFormatIsAutocorrectedLength.toString().length,
                                AUTOCORRECTED_HEADER.length,
                            )} " +
                            "| ${columnValue(
                                counts.countLintCanBeAutocorrected,
                                sumOfLintCanBeAutocorrected.toString().length,
                                CAN_BE_AUTOCORRECTED_HEADER.length,
                            )} " +
                            "| ${columnValue(
                                counts.countLintCannotBeAutocorrected,
                                sumOfLintCannotBeAutocorrected.toString().length,
                                CANNOT_BE_AUTOCORRECTED_HEADER.length,
                            )} " +
                            "| ${columnValue(
                                counts.countNotIgnored,
                                sumOfTotalLength.toString().length,
                                TOTAL_HEADER.length,
                            )} " +
                            "|",
                    )
                }
            out.println(separatorLine)
            out.println(
                "  | ${"Total".padEnd(maxCategoryLength)} " +
                    "| ${columnValue(
                        sumOfFormatIsAutocorrectedLength,
                        sumOfFormatIsAutocorrectedLength.toString().length,
                        AUTOCORRECTED_HEADER.length,
                    )} " +
                    "| ${columnValue(
                        sumOfLintCanBeAutocorrected,
                        sumOfLintCanBeAutocorrected.toString().length,
                        CAN_BE_AUTOCORRECTED_HEADER.length,
                    )} " +
                    "| ${columnValue(
                        sumOfLintCannotBeAutocorrected,
                        sumOfLintCannotBeAutocorrected.toString().length,
                        CANNOT_BE_AUTOCORRECTED_HEADER.length,
                    )} " +
                    "| ${columnValue(
                        sumOfTotalLength,
                        sumOfTotalLength.toString().length,
                        TOTAL_HEADER.length,
                    )} " +
                    "|",
            )
            out.println(separatorLine)
        }
    }

    private fun columnValue(
        value: Long,
        maxValueWidth: Int,
        columnWidth: Int,
    ): String {
        val stringValue = value.toString()
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
        val countBaselineIgnored: Long = 0,
        val countLintCannotBeAutocorrected: Long = 0,
        val countLintCanBeAutocorrected: Long = 0,
        val countFormatIsAutocorrected: Long = 0,
        val countNotIgnored: Long = 0,
    )

    private companion object {
        val COUNT_DESC_AND_RULE_ID_ASC_COMPARATOR =
            Comparator<Pair<String, StatusCount>> { left, right ->
                compareValuesBy(left, right) { it.second.countNotIgnored }
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
