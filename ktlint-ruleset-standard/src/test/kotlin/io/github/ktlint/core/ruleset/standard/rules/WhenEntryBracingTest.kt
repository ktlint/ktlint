package io.github.ktlint.core.ruleset.standard.rules

import io.github.ktlint.core.test.KtLintAssertThat.Companion.assertThatRule
import io.github.ktlint.core.test.LintViolation
import org.junit.jupiter.api.Test

class WhenEntryBracingTest {
    private val whenEntryBracingRuleAssertThat = assertThatRule { WhenEntryBracing() }

    @Test
    fun `Given a when-statement for which no entry has a block body then do not reformat`() {
        val code =
            """
            val foo =
                when (bar) {
                    BAR1 -> "bar1"
                    BAR2 -> "bar2"
                    else -> null
                }
            """.trimIndent()
        whenEntryBracingRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `Given a when-statement for which all entries have a block body then do not reformat`() {
        val code =
            """
            val foo =
                when (bar) {
                    BAR1 -> { "bar1" }
                    BAR2 -> { "bar2" }
                    else -> { null }
                }
            """.trimIndent()
        whenEntryBracingRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `Given a when-statement containing an entry with braces and an entry without braces then add braces to all entries`() {
        val code =
            """
            val foo =
                when (bar) {
                    BAR1 -> { "bar1" }
                    BAR2 -> "bar2"
                    else -> null
                }
            """.trimIndent()
        val formattedCode =
            """
            val foo =
                when (bar) {
                    BAR1 -> {
                        "bar1"
                    }
                    BAR2 -> {
                        "bar2"
                    }
                    else -> {
                        null
                    }
                }
            """.trimIndent()
        @Suppress("ktlint:standard:max-line-length")
        whenEntryBracingRuleAssertThat(code)
            .addAdditionalRuleProvider {
                // Ensures that the first when entry is also wrapped to a multiline body
                StatementWrappingRule()
            }.hasLintViolations(
                LintViolation(4, 17, "Body of when entry should be surrounded by braces if any when entry body is surrounded by braces or has a multiline body"),
                LintViolation(5, 17, "Body of when entry should be surrounded by braces if any when entry body is surrounded by braces or has a multiline body"),
            ).isFormattedAs(formattedCode)
    }

    @Test
    fun `Given a when-statement containing an entry with braces and an entry without braces which contains one multiline statement then add braces to all entries`() {
        val code =
            """
            val foo =
                when (bar) {
                    BAR1 -> { "bar1" }
                    BAR2 -> "bar2"
                        .plus("bar3")
                        .plus("bar4")
                    BAR_TOO_LONGGGGGGGGGGGGGGGG,
                    -> "bar2"
                        .plus("bar3")
                        .plus("bar4")
                    else -> null
                }
            """.trimIndent()
        val formattedCode =
            """
            val foo =
                when (bar) {
                    BAR1 -> {
                        "bar1"
                    }
                    BAR2 -> {
                        "bar2"
                            .plus("bar3")
                            .plus("bar4")
                    }
                    BAR_TOO_LONGGGGGGGGGGGGGGGG,
                    -> {
                        "bar2"
                            .plus("bar3")
                            .plus("bar4")
                    }
                    else -> {
                        null
                    }
                }
            """.trimIndent()
        @Suppress("ktlint:standard:max-line-length")
        whenEntryBracingRuleAssertThat(code)
            .addAdditionalRuleProvider {
                // Ensures that the first when entry is also wrapped to a multiline body
                StatementWrappingRule()
            }.addAdditionalRuleProvider {
                // Fix indent of the wrapped multiline statement
                IndentationRule()
            }.hasLintViolations(
                LintViolation(4, 17, "Body of when entry should be surrounded by braces if any when entry body is surrounded by braces or has a multiline body"),
                LintViolation(8, 12, "Body of when entry should be surrounded by braces if any when entry body is surrounded by braces or has a multiline body"),
                LintViolation(11, 17, "Body of when entry should be surrounded by braces if any when entry body is surrounded by braces or has a multiline body"),
            ).isFormattedAs(formattedCode)
    }

    @Test
    fun `Given a when-statement containing an entry with braces and an entry without braces which starts with some EOL comments then add braces to all entries`() {
        val code =
            """
            val foo =
                when (bar) {
                    BAR1 -> { "bar1" }
                    BAR2 -> // some comment 1
                        // some comment 2
                        "bar2"
                    else -> null
                }
            """.trimIndent()
        val formattedCode =
            """
            val foo =
                when (bar) {
                    BAR1 -> {
                        "bar1"
                    }
                    BAR2 -> {
                        // some comment 1
                        // some comment 2
                        "bar2"
                    }
                    else -> {
                        null
                    }
                }
            """.trimIndent()
        @Suppress("ktlint:standard:max-line-length")
        whenEntryBracingRuleAssertThat(code)
            .addAdditionalRuleProvider {
                // Ensures that the first when entry is also wrapped to a multiline body
                StatementWrappingRule()
            }.hasLintViolations(
                LintViolation(4, 17, "Body of when entry should be surrounded by braces if any when entry body is surrounded by braces or has a multiline body"),
                LintViolation(7, 17, "Body of when entry should be surrounded by braces if any when entry body is surrounded by braces or has a multiline body"),
            ).isFormattedAs(formattedCode)
    }

    @Test
    fun `Given a when-statement with a multiline body not contained in a block then add braces to all entries`() {
        val code =
            """
            val foo =
                when (bar) {
                    BAR1 -> "bar1"
                    BAR2 ->
                        "bar2"
                    else -> null
                }
            """.trimIndent()
        val formattedCode =
            """
            val foo =
                when (bar) {
                    BAR1 -> {
                        "bar1"
                    }
                    BAR2 -> {
                        "bar2"
                    }
                    else -> {
                        null
                    }
                }
            """.trimIndent()
        @Suppress("ktlint:standard:max-line-length")
        whenEntryBracingRuleAssertThat(code)
            .addAdditionalRuleProvider {
                // Ensures that the first when entry is also wrapped to a multiline body
                StatementWrappingRule()
            }.hasLintViolations(
                LintViolation(3, 17, "Body of when entry should be surrounded by braces if any when entry body is surrounded by braces or has a multiline body"),
                LintViolation(5, 13, "Body of when entry should be surrounded by braces if any when entry body is surrounded by braces or has a multiline body"),
                LintViolation(6, 17, "Body of when entry should be surrounded by braces if any when entry body is surrounded by braces or has a multiline body"),
            ).isFormattedAs(formattedCode)
    }

    @Test
    fun `Given a when-statement with a when entry condition followed by an EOL comment on same line then keep that comment inside the block`() {
        val code =
            """
            val foo =
                when (bar) {
                    BAR1 -> "bar1" // bar 1 comment
                    // comment before BAR2
                    BAR2 ->
                        "bar2" // bar 2 comment
                    else -> null // else comment
                }
            """.trimIndent()
        val formattedCode =
            """
            val foo =
                when (bar) {
                    BAR1 -> {
                        "bar1" // bar 1 comment
                    }
                    // comment before BAR2
                    BAR2 -> {
                        "bar2" // bar 2 comment
                    }
                    else -> {
                        null // else comment
                    }
                }
            """.trimIndent()
        @Suppress("ktlint:standard:max-line-length")
        whenEntryBracingRuleAssertThat(code)
            .addAdditionalRuleProvider {
                // Ensures that the first when entry is also wrapped to a multiline body
                StatementWrappingRule()
            }.hasLintViolations(
                LintViolation(3, 17, "Body of when entry should be surrounded by braces if any when entry body is surrounded by braces or has a multiline body"),
                LintViolation(6, 13, "Body of when entry should be surrounded by braces if any when entry body is surrounded by braces or has a multiline body"),
                LintViolation(7, 17, "Body of when entry should be surrounded by braces if any when entry body is surrounded by braces or has a multiline body"),
            ).isFormattedAs(formattedCode)
    }

    @Test
    fun `Given a when-statement with an entry with braces and more than three entries without braces then add braces to all entries`() {
        val code =
            """
            val foo =
                when (bar) {
                    BAR1 -> "bar1"
                    BAR2 -> "bar2"
                    BAR3 -> "bar3"
                    BAR4 -> {
                        "bar4"
                    }
                    else -> null
                }
            """.trimIndent()
        val formattedCode =
            """
            val foo =
                when (bar) {
                    BAR1 -> {
                        "bar1"
                    }
                    BAR2 -> {
                        "bar2"
                    }
                    BAR3 -> {
                        "bar3"
                    }
                    BAR4 -> {
                        "bar4"
                    }
                    else -> {
                        null
                    }
                }
            """.trimIndent()
        @Suppress("ktlint:standard:max-line-length")
        whenEntryBracingRuleAssertThat(code)
            .hasLintViolations(
                LintViolation(3, 17, "Body of when entry should be surrounded by braces if any when entry body is surrounded by braces or has a multiline body"),
                LintViolation(4, 17, "Body of when entry should be surrounded by braces if any when entry body is surrounded by braces or has a multiline body"),
                LintViolation(5, 17, "Body of when entry should be surrounded by braces if any when entry body is surrounded by braces or has a multiline body"),
                LintViolation(9, 17, "Body of when entry should be surrounded by braces if any when entry body is surrounded by braces or has a multiline body"),
            ).isFormattedAs(formattedCode)
    }
}
