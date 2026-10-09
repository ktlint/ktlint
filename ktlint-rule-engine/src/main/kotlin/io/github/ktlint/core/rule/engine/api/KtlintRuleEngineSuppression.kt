package io.github.ktlint.core.rule.engine.api

import io.github.ktlint.core.rule.engine.core.api.RuleId
import io.github.ktlint.core.rule.engine.core.api.isWhiteSpace
import io.github.ktlint.core.rule.engine.core.api.parent
import io.github.ktlint.core.rule.engine.internal.RuleExecutionContext
import io.github.ktlint.core.rule.engine.internal.insertKtlintRuleSuppression
import org.jetbrains.kotlin.com.intellij.lang.ASTNode

/**
 * A [Suppress] annotation can only be inserted at specific locations. This function is intended for API Consumers. It updates given [code]
 * by inserting a [Suppress] annotation for the given [suppression].
 *
 * Throws [KtlintSuppressionOutOfBoundsException] when the position of the [suppression] can not be found in the [code]. Throws
 * [KtlintSuppressionNoElementFoundException] when no element can be found at the given offset.
 *
 * Returns the code with the inserted/modified suppression. Note that the returned code may not (yet) comply with formatting of all rules.
 * This is intentional as adding a suppression for the [suppression] does not mean that other lint errors which can be autocorrected should
 * be autocorrected.
 */
public fun KtLintRuleEngine.insertSuppression(
    code: Code,
    suppression: KtlintSuppression,
): String {
    val rootNode =
        RuleExecutionContext
            .createRuleExecutionContext(this, code)
            .rootNode

    rootNode
        .findLeafElementAt(suppression)
        .insertKtlintRuleSuppression(setOf(suppression.ruleId.value))

    return rootNode.text
}

private fun ASTNode.findLeafElementAt(suppression: KtlintSuppression): ASTNode =
    when (suppression) {
        is KtlintSuppressionForFile -> {
            this
        }

        is KtlintSuppressionAtOffset -> {
            findLeafElementAt(suppression.offsetFromStartOf(text))
                ?.let {
                    if (it.isWhiteSpace) {
                        // A suppression can not be added at a whitespace element. Insert it at the parent instead
                        it.parent
                    } else {
                        it
                    }
                }
                ?: throw KtlintSuppressionNoElementFoundException(suppression)
        }
    }

private fun KtlintSuppressionAtOffset.offsetFromStartOf(code: String): Int {
    val lines = code.split("\n")

    if (line < 1 || col < 1 || line > lines.size) {
        throw KtlintSuppressionOutOfBoundsException(this)
    }
    val startOffsetOfLineContainingLintError =
        lines
            .take((line - 1).coerceAtLeast(0))
            .sumOf { text ->
                // Fix length for newlines which were removed while splitting the original code
                text.length + 1
            }

    val codeLine = lines[line - 1]
    return when {
        col == 1 && codeLine.isEmpty() -> {
            startOffsetOfLineContainingLintError
        }

        col <= codeLine.length -> {
            startOffsetOfLineContainingLintError + (col - 1)
        }

        col == codeLine.length + 1 -> {
            // Offset of suppression is set at EOL of the line. This is visually correct for the reader. But the newline character was stripped
            // from the line because the lines were split using that character.
            startOffsetOfLineContainingLintError + col
        }

        else -> {
            throw KtlintSuppressionOutOfBoundsException(this)
        }
    }
}

public sealed class KtlintSuppressionException(
    message: String,
) : RuntimeException(message)

public class KtlintSuppressionOutOfBoundsException(
    offsetSuppression: KtlintSuppressionAtOffset,
) : KtlintSuppressionException("Offset (${offsetSuppression.line},${offsetSuppression.col}) is invalid")

public class KtlintSuppressionNoElementFoundException(
    offsetSuppression: KtlintSuppressionAtOffset,
) : KtlintSuppressionException("No ASTNode found at offset (${offsetSuppression.line},${offsetSuppression.col})")

public sealed class KtlintSuppression(
    public val ruleId: RuleId,
)

public class KtlintSuppressionForFile(
    ruleId: RuleId,
) : KtlintSuppression(ruleId)

public class KtlintSuppressionAtOffset(
    public val line: Int,
    public val col: Int,
    ruleId: RuleId,
) : KtlintSuppression(ruleId)
