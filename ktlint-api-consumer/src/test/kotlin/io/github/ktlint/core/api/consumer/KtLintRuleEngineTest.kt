package io.github.ktlint.core.api.consumer

import assertk.assertThat
import assertk.assertions.containsExactlyInAnyOrder
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import io.github.ktlint.core.api.consumer.KtLintRuleEngineTest.DemoRule.Companion.DEMO_RULE_ID
import io.github.ktlint.core.rule.engine.api.Code
import io.github.ktlint.core.rule.engine.api.EditorConfigOverride.Companion.from
import io.github.ktlint.core.rule.engine.api.KtLintRuleEngine
import io.github.ktlint.core.rule.engine.api.LintError
import io.github.ktlint.core.rule.engine.core.api.AutocorrectDecision
import io.github.ktlint.core.rule.engine.core.api.AutocorrectDecision.ALLOW_AUTOCORRECT
import io.github.ktlint.core.rule.engine.core.api.AutocorrectDecision.NO_AUTOCORRECT
import io.github.ktlint.core.rule.engine.core.api.ElementType.EOL_COMMENT
import io.github.ktlint.core.rule.engine.core.api.RuleId
import io.github.ktlint.core.rule.engine.core.api.RuleV2
import io.github.ktlint.core.rule.engine.core.api.RuleV2Provider
import io.github.ktlint.core.rule.engine.core.api.editorconfig.EXPERIMENTAL_RULES_EXECUTION_PROPERTY
import io.github.ktlint.core.rule.engine.core.api.editorconfig.RuleExecution
import io.github.ktlint.core.rule.engine.core.api.editorconfig.createRuleExecutionEditorConfigProperty
import io.github.ktlint.core.rule.engine.core.api.replaceTextWith
import io.github.ktlint.core.ruleset.standard.rules.FilenameRule
import io.github.ktlint.core.ruleset.standard.rules.INDENTATION_RULE_ID
import io.github.ktlint.core.ruleset.standard.rules.IndentationRule
import io.github.ktlint.core.test.KtlintTestFileSystem
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.io.FileWriter
import java.nio.file.Path
import io.github.ktlint.core.rule.engine.core.api.ifAutocorrectAllowed as ifAutocorrectAllowedLegacy

/**
 * The KtLintRuleEngine is used by the Ktlint CLI and external API Consumers. Although most functionalities of the RuleEngine are already
 * tested via the Ktlint CLI Tests and normal unit tests in KtLint Core, some functionalities need additional testing from the perspective
 * of an API Consumer to ensure that the API is usable and stable across releases.
 */
class KtLintRuleEngineTest {
    private val ktlintTestFileSystem = KtlintTestFileSystem()
    private val ktLintRuleEngine =
        KtLintRuleEngine(
            ruleProviders =
                setOf(
                    RuleV2Provider { IndentationRule() },
                    RuleV2Provider { DemoRule() },
                ),
            editorConfigOverride =
                from(
                    DEMO_RULE_ID.createRuleExecutionEditorConfigProperty() to RuleExecution.enabled,
                ),
            fileSystem = ktlintTestFileSystem.fileSystem,
        )

    @AfterEach
    fun tearDown() {
        ktlintTestFileSystem.close()
    }

    @Nested
    inner class `Lint with KtLintRuleEngine` {
        @Test
        fun `Given a file containing errors found by standard and custom rules`(
            @TempDir
            tempDir: Path,
        ) {
            val filePath = "$tempDir/Code.kt"
            FileWriter(filePath).use {
                it.write(
                    """
                    fun bar() {
                        // foo
                        // bar
                        }
                    """.trimIndent(),
                )
            }

            val lintErrors = mutableListOf<LintError>()
            ktLintRuleEngine.lint(
                code = Code.fromFile(File(filePath)),
            ) { lintErrors.add(it) }

            assertThat(lintErrors).containsExactlyInAnyOrder(
                LintError(3, 5, DEMO_RULE_ID, "Bar comment", true),
                LintError(4, 1, INDENTATION_RULE_ID, "Unexpected indentation (4) (should be 0)", true),
            )
        }

        @Test
        fun `Given a kotlin code snippet containing errors found by standard and custom rules`() {
            val lintErrors = mutableListOf<LintError>()
            ktLintRuleEngine.lint(
                code =
                    Code.fromSnippet(
                        """
                        fun bar() {
                            // foo
                            // bar
                            }
                        """.trimIndent(),
                    ),
            ) { lintErrors.add(it) }

            assertThat(lintErrors).containsExactlyInAnyOrder(
                LintError(3, 5, DEMO_RULE_ID, "Bar comment", true),
                LintError(4, 1, INDENTATION_RULE_ID, "Unexpected indentation (4) (should be 0)", true),
            )
        }

        @Test
        fun `Given a kotlin script code snippet containing errors found by standard and custom rules`() {
            val lintErrors = mutableListOf<LintError>()
            ktLintRuleEngine.lint(
                code =
                    Code.fromSnippet(
                        """
                        plugins {
                            // foo
                            // bar
                            }
                        """.trimIndent(),
                        script = true,
                    ),
            ) { lintErrors.add(it) }

            assertThat(lintErrors).containsExactlyInAnyOrder(
                LintError(3, 5, DEMO_RULE_ID, "Bar comment", true),
                LintError(4, 1, INDENTATION_RULE_ID, "Unexpected indentation (4) (should be 0)", true),
            )
        }

        @Test
        fun `Given a code snippet then the file name rule may not result in a Lint violation`() {
            val ktLintRuleEngine =
                KtLintRuleEngine(
                    ruleProviders =
                        setOf(
                            RuleV2Provider { FilenameRule() },
                        ),
                    fileSystem = ktlintTestFileSystem.fileSystem,
                )
            val lintErrors = mutableListOf<LintError>()
            ktLintRuleEngine.lint(
                code =
                    Code.fromSnippet(
                        """
                        var foo = "foo"
                        """.trimIndent(),
                    ),
            ) { lintErrors.add(it) }

            assertThat(lintErrors).isEmpty()
        }
    }

    @Nested
    inner class `Format with KtLintRuleEngine` {
        @Nested
        inner class `Given a file that containing some errors` {
            @Test
            fun `Given defaultAutocorrect is not set`(
                @TempDir
                tempDir: Path,
            ) {
                val filePath = "$tempDir/Code.kt"
                FileWriter(filePath).use {
                    it.write(
                        """
                        fun bar() {
                            // bar
                            }
                        """.trimIndent(),
                    )
                }

                val lintErrors = mutableListOf<LintError>()
                val actual =
                    ktLintRuleEngine.format(
                        code = Code.fromFile(File(filePath)),
                    ) { lintError ->
                        lintErrors.add(lintError)
                        ALLOW_AUTOCORRECT
                    }

                assertThat(lintErrors).containsExactlyInAnyOrder(
                    LintError(2, 5, DEMO_RULE_ID, "Bar comment", true),
                    LintError(3, 1, INDENTATION_RULE_ID, "Unexpected indentation (4) (should be 0)", true),
                )
                assertThat(actual).isEqualTo(
                    """
                    fun bar() {
                        // BAR
                    }
                    """.trimIndent(),
                )
            }

            @Test
            fun `Given defaultAutocorrect is enabled`(
                @TempDir
                tempDir: Path,
            ) {
                val filePath = "$tempDir/Code.kt"
                FileWriter(filePath).use {
                    it.write(
                        """
                        fun bar() {
                            // bar
                            }
                        """.trimIndent(),
                    )
                }

                val lintErrors = mutableListOf<LintError>()
                val actual =
                    ktLintRuleEngine.format(
                        code = Code.fromFile(File(filePath)),
                        defaultAutocorrect = true,
                    ) { lintError ->
                        lintErrors.add(lintError)
                        ALLOW_AUTOCORRECT
                    }

                assertThat(lintErrors).containsExactlyInAnyOrder(
                    LintError(2, 5, DEMO_RULE_ID, "Bar comment", true),
                    LintError(3, 1, INDENTATION_RULE_ID, "Unexpected indentation (4) (should be 0)", true),
                )
                assertThat(actual).isEqualTo(
                    """
                    fun bar() {
                        // BAR
                    }
                    """.trimIndent(),
                )
            }

            @Test
            fun `Given defaultAutocorrect is disabled`(
                @TempDir
                tempDir: Path,
            ) {
                val filePath = "$tempDir/Code.kt"
                FileWriter(filePath).use {
                    it.write(
                        """
                        fun bar() {
                            // foo
                            // bar
                            }
                        """.trimIndent(),
                    )
                }

                val lintErrors = mutableSetOf<LintError>()
                val actual =
                    ktLintRuleEngine.format(
                        code = Code.fromFile(File(filePath)),
                        defaultAutocorrect = false,
                    ) { lintError ->
                        lintErrors.add(lintError)
                        ALLOW_AUTOCORRECT
                    }

                assertThat(lintErrors).containsExactlyInAnyOrder(
                    LintError(3, 5, DEMO_RULE_ID, "Bar comment", true),
                    LintError(4, 1, INDENTATION_RULE_ID, "Unexpected indentation (4) (should be 0)", true),
                )
                assertThat(actual).isEqualTo(
                    // Note that "foo" is not transformed to "FOO" as the defaultAutocorrect for rules without AutocorrectApproveHandler is
                    // not set
                    """
                    fun bar() {
                        // foo
                        // BAR
                    }
                    """.trimIndent(),
                )
            }
        }

        @Nested
        inner class `Given a kotlin code snippet containing some errors` {
            @Test
            fun `Given defaultAutocorrect is not set`() {
                val lintErrors = mutableListOf<LintError>()
                val actual =
                    ktLintRuleEngine.format(
                        code =
                            Code.fromSnippet(
                                """
                                fun bar() {
                                    // bar
                                    }
                                """.trimIndent(),
                            ),
                    ) { lintError ->
                        lintErrors.add(lintError)
                        ALLOW_AUTOCORRECT
                    }

                assertThat(lintErrors).containsExactlyInAnyOrder(
                    LintError(2, 5, DEMO_RULE_ID, "Bar comment", true),
                    LintError(3, 1, INDENTATION_RULE_ID, "Unexpected indentation (4) (should be 0)", true),
                )
                assertThat(actual).isEqualTo(
                    """
                    fun bar() {
                        // BAR
                    }
                    """.trimIndent(),
                )
            }

            @Test
            fun `Given defaultAutocorrect is enabled`() {
                val lintErrors = mutableListOf<LintError>()
                val actual =
                    ktLintRuleEngine.format(
                        code =
                            Code.fromSnippet(
                                """
                                fun bar() {
                                    // bar
                                    }
                                """.trimIndent(),
                            ),
                        defaultAutocorrect = true,
                    ) { lintError ->
                        lintErrors.add(lintError)
                        ALLOW_AUTOCORRECT
                    }

                assertThat(lintErrors).containsExactlyInAnyOrder(
                    LintError(2, 5, DEMO_RULE_ID, "Bar comment", true),
                    LintError(3, 1, INDENTATION_RULE_ID, "Unexpected indentation (4) (should be 0)", true),
                )
                assertThat(actual).isEqualTo(
                    """
                    fun bar() {
                        // BAR
                    }
                    """.trimIndent(),
                )
            }

            @Test
            fun `Given defaultAutocorrect is disabled`() {
                val lintErrors = mutableSetOf<LintError>()
                val actual =
                    ktLintRuleEngine.format(
                        code =
                            Code.fromSnippet(
                                """
                                fun bar() {
                                    // foo
                                    // bar
                                    }
                                """.trimIndent(),
                            ),
                        defaultAutocorrect = false,
                    ) { lintError ->
                        lintErrors.add(lintError)
                        ALLOW_AUTOCORRECT
                    }

                assertThat(lintErrors).containsExactlyInAnyOrder(
                    LintError(3, 5, DEMO_RULE_ID, "Bar comment", true),
                    LintError(4, 1, INDENTATION_RULE_ID, "Unexpected indentation (4) (should be 0)", true),
                )
                assertThat(actual).isEqualTo(
                    // Note that "foo" is not transformed to "FOO" as the defaultAutocorrect for rules without AutocorrectApproveHandler is
                    // not set
                    """
                    fun bar() {
                        // foo
                        // BAR
                    }
                    """.trimIndent(),
                )
            }
        }

        @Nested
        inner class `Given a kotlin script code snippet containing some errors` {
            @Test
            fun `Given defaultAutocorrect is not set`() {
                val lintErrors = mutableListOf<LintError>()
                val actual =
                    ktLintRuleEngine.format(
                        code =
                            Code.fromSnippet(
                                """
                                plugins {
                                    // bar
                                    }
                                """.trimIndent(),
                                script = true,
                            ),
                    ) { lintError ->
                        lintErrors.add(lintError)
                        ALLOW_AUTOCORRECT
                    }

                assertThat(lintErrors).containsExactlyInAnyOrder(
                    LintError(2, 5, DEMO_RULE_ID, "Bar comment", true),
                    LintError(3, 1, INDENTATION_RULE_ID, "Unexpected indentation (4) (should be 0)", true),
                )
                assertThat(actual).isEqualTo(
                    """
                    plugins {
                        // BAR
                    }
                    """.trimIndent(),
                )
            }

            @Test
            fun `Given defaultAutocorrect is enabled`() {
                val lintErrors = mutableListOf<LintError>()
                val actual =
                    ktLintRuleEngine.format(
                        code =
                            Code.fromSnippet(
                                """
                                plugins {
                                    // bar
                                    }
                                """.trimIndent(),
                                script = true,
                            ),
                        defaultAutocorrect = true,
                    ) { lintError ->
                        lintErrors.add(lintError)
                        ALLOW_AUTOCORRECT
                    }

                assertThat(lintErrors).containsExactlyInAnyOrder(
                    LintError(2, 5, DEMO_RULE_ID, "Bar comment", true),
                    LintError(3, 1, INDENTATION_RULE_ID, "Unexpected indentation (4) (should be 0)", true),
                )
                assertThat(actual).isEqualTo(
                    """
                    plugins {
                        // BAR
                    }
                    """.trimIndent(),
                )
            }

            @Test
            fun `Given defaultAutocorrect is disabled`() {
                val lintErrors = mutableSetOf<LintError>()
                val actual =
                    ktLintRuleEngine.format(
                        code =
                            Code.fromSnippet(
                                """
                                plugins {
                                    // foo
                                    // bar
                                    }
                                """.trimIndent(),
                                script = true,
                            ),
                        defaultAutocorrect = false,
                    ) { lintError ->
                        lintErrors.add(lintError)
                        ALLOW_AUTOCORRECT
                    }

                assertThat(lintErrors).containsExactlyInAnyOrder(
                    LintError(3, 5, DEMO_RULE_ID, "Bar comment", true),
                    LintError(4, 1, INDENTATION_RULE_ID, "Unexpected indentation (4) (should be 0)", true),
                )
                assertThat(actual).isEqualTo(
                    // Note that "foo" is not transformed to "FOO" as the defaultAutocorrect for rules without AutocorrectApproveHandler is
                    // not set
                    """
                    plugins {
                        // foo
                        // BAR
                    }
                    """.trimIndent(),
                )
            }
        }

        @Test
        fun `Given a kotlin code snippet that does contain multiple errors then only format the lint error at specific offset and message`() {
            val lintErrors = mutableSetOf<LintError>()
            val actual =
                ktLintRuleEngine
                    .format(
                        code =
                            Code.fromSnippet(
                                """
                                // bar
                                // bar
                                // bar
                                """.trimIndent(),
                            ),
                    ) { lintError ->
                        lintErrors.add(lintError)
                        if (lintError.line == 2 &&
                            lintError.col == 1 &&
                            lintError.ruleId == DEMO_RULE_ID &&
                            lintError.detail == "Bar comment"
                        ) {
                            ALLOW_AUTOCORRECT
                        } else {
                            NO_AUTOCORRECT
                        }
                    }

            assertThat(lintErrors).containsExactlyInAnyOrder(
                LintError(1, 1, DEMO_RULE_ID, "Bar comment", true),
                LintError(2, 1, DEMO_RULE_ID, "Bar comment", true),
                LintError(3, 1, DEMO_RULE_ID, "Bar comment", true),
            )
            assertThat(actual).isEqualTo(
                """
                // bar
                // BAR
                // bar
                """.trimIndent(),
            )
        }

        @Test
        fun `Issue 2747 - Given some code with crlf separators instead of lfs, but not containing any lint error, then do no reformat the line separators`(
            @TempDir
            tempDir: Path,
        ) {
            val codeWithCrlfSeparators =
                """
                fun bar() {
                    // BAR
                }
                """.trimIndent().replace("\n", "\r\n")
            val filePath = "$tempDir/Code.kt"
            FileWriter(filePath).use {
                it.write(codeWithCrlfSeparators)
            }

            val lintErrors = mutableListOf<LintError>()
            val actual =
                KtLintRuleEngine(
                    ruleProviders =
                        setOf(
                            RuleV2Provider { IndentationRule() },
                            RuleV2Provider { DemoRule() },
                        ),
                    editorConfigOverride =
                        from(
                            // Do not set END_OF_LINE_PROPERTY explicitly!
                            DEMO_RULE_ID.createRuleExecutionEditorConfigProperty() to RuleExecution.enabled,
                        ),
                    fileSystem = ktlintTestFileSystem.fileSystem,
                ).format(
                    code = Code.fromFile(File(filePath)),
                    defaultAutocorrect = true,
                ) { lintError ->
                    lintErrors.add(lintError)
                    ALLOW_AUTOCORRECT
                }

            assertThat(lintErrors).isEmpty()
            assertThat(actual).isEqualTo(codeWithCrlfSeparators)
        }
    }

    @Test
    fun `Given that all experimental rules are enabled`() {
        val ktLintRuleEngine =
            KtLintRuleEngine(
                ruleProviders =
                    setOf(
                        RuleV2Provider { IndentationRule() },
                        RuleV2Provider { DemoRule() },
                    ),
                editorConfigOverride =
                    from(
                        EXPERIMENTAL_RULES_EXECUTION_PROPERTY to RuleExecution.enabled,
                    ),
                fileSystem = ktlintTestFileSystem.fileSystem,
            )

        val lintErrors = mutableListOf<LintError>()
        ktLintRuleEngine.lint(
            code =
                Code.fromSnippet(
                    """
                    fun bar() {
                        // foo
                        // bar
                        }
                    """.trimIndent(),
                ),
            callback = lintErrors::add,
        )

        assertThat(lintErrors).containsExactlyInAnyOrder(
            LintError(3, 5, DEMO_RULE_ID, "Bar comment", true),
            LintError(4, 1, INDENTATION_RULE_ID, "Unexpected indentation (4) (should be 0)", true),
        )
    }

    private class DemoRule :
        RuleV2(ruleId = DEMO_RULE_ID, about = About()),
        RuleV2.Experimental {
        override fun beforeVisitChildNodes(
            node: ASTNode,
            emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> AutocorrectDecision,
        ) {
            if (node.elementType == EOL_COMMENT && node.text == "// bar") {
                emit(node.startOffset, "Bar comment", true)
                    .ifAutocorrectAllowedLegacy {
                        node.replaceTextWith("// BAR")
                    }
            }
        }

        companion object {
            val DEMO_RULE_ID = RuleId("custom:demo-rule")
        }
    }
}
