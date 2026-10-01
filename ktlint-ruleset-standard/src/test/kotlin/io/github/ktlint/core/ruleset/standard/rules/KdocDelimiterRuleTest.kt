package io.github.ktlint.core.ruleset.standard.rules

import io.github.ktlint.core.rule.engine.core.api.editorconfig.CODE_STYLE_PROPERTY
import io.github.ktlint.core.rule.engine.core.api.editorconfig.CodeStyleValue
import io.github.ktlint.core.rule.engine.core.api.editorconfig.RuleExecution
import io.github.ktlint.core.rule.engine.core.api.editorconfig.createRuleExecutionEditorConfigProperty
import io.github.ktlint.core.test.KtLintAssertThat.Companion.assertThatRule
import io.github.ktlint.core.test.LintViolation
import io.github.ktlint.core.test.SPACE
import io.github.ktlint.core.test.TAB
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource

class KdocDelimiterRuleTest {
    private val kdocDelimiterRuleAssertThat = assertThatRule { KdocDelimiterRule() }

    @Nested
    inner class `Given a well formed KDoc comment` {
        @Test
        fun `Given a single-line KDoc comment then do not reformat`() {
            val code =
                """
                /** A group of *members* */
                class Foo
                """.trimIndent()
            kdocDelimiterRuleAssertThat(code).hasNoLintViolations()
        }

        @Test
        fun `Given a multi-line KDoc comment then do not reformat`() {
            val code =
                """
                /**
                 * A group of *members*.
                 *
                 * This class has no useful logic; it's just a documentation example.
                 *
                 * @param T the type of a member in this group.
                 * @property name the name of this group.
                 * @constructor Creates an empty group.
                 */
                class Foo
                """.trimIndent()
            kdocDelimiterRuleAssertThat(code).hasNoLintViolations()
        }

        @Test
        fun `Given a KDoc comment containing markdown, a link, a tag and a code block then do not reformat`() {
            val code =
                """
                /**
                 * A [Foo] which contains **markdown**.
                 * [Foo] is a kdoc link as first word after the leading asterisk
                 *
                 * ```
                 * val foo = Foo()
                 *     .bar()
                 * ```
                 *
                 * @see Foo.bar
                 */
                class Foo
                """.trimIndent()
            kdocDelimiterRuleAssertThat(code).hasNoLintViolations()
        }

        @Test
        fun `Given a KDoc comment on a nested function then do not reformat`() {
            val code =
                """
                class Foo {
                    /**
                     * Some bar KDoc.
                     */
                    fun bar() {}
                }
                """.trimIndent()
            kdocDelimiterRuleAssertThat(code).hasNoLintViolations()
        }

        @Test
        fun `Given a KDoc comment at the start of the file then do not reformat`() {
            val code =
                """
                /**
                 * A file level KDoc.
                 */
                class Foo
                """.trimIndent()
            kdocDelimiterRuleAssertThat(code).hasNoLintViolations()
        }

        @Test
        fun `Given a KDoc comment with tab indentation then do not reformat`() {
            val code =
                """
                class Foo {
                $TAB/**
                $TAB * Some bar KDoc.
                $TAB */
                ${TAB}fun bar() {}
                }
                """.trimIndent()
            kdocDelimiterRuleAssertThat(code).hasNoLintViolations()
        }
    }

    @Nested
    inner class `Given an empty KDoc comment` {
        @Test
        fun `Given a completely empty single-line KDoc comment then do report but not autocorrect`() {
            val code =
                """
                /***/
                class Foo
                """.trimIndent()
            kdocDelimiterRuleAssertThat(code).hasLintViolationWithoutAutoCorrect(1, 4, "An empty KDoc comment is not allowed")
        }

        @Test
        fun `Given a blank single-line KDoc comment then do report but not autocorrect`() {
            val code =
                """
                /** */
                class Foo
                """.trimIndent()
            kdocDelimiterRuleAssertThat(code).hasLintViolationWithoutAutoCorrect(1, 4, "An empty KDoc comment is not allowed")
        }

        @Test
        fun `Given a blank multi-line KDoc comment without blank lines then do report but not autocorrect`() {
            val code =
                """
                /**
                 */
                class Foo
                """.trimIndent()
            kdocDelimiterRuleAssertThat(code).hasLintViolationWithoutAutoCorrect(1, 4, "An empty KDoc comment is not allowed")
        }

        @Test
        fun `Given a blank multi-line KDoc comment including blank lines then do report but not autocorrect`() {
            val code =
                """
                /**
                 *
                 */
                class Foo
                """.trimIndent()
            kdocDelimiterRuleAssertThat(code).hasLintViolationWithoutAutoCorrect(1, 4, "An empty KDoc comment is not allowed")
        }
    }

    @Nested
    inner class `Given a malformed opening or closing` {
        @Test
        fun `Given a KDoc comment starting with an additional asterisk then do report but not autocorrect`() {
            // The extra asterisk becomes part of the actual content, so this is caught by the general check on
            // content following the opening delimiter on the same line, rather than by a dedicated check.
            val code =
                """
                /***
                 * A group of *members*.
                 */
                class Foo
                """.trimIndent()
            @Suppress("ktlint:standard:max-line-length")
            kdocDelimiterRuleAssertThat(code).hasLintViolationWithoutAutoCorrect(1, 4, "Opening '/**' of a multi-line KDoc comment should not be followed by any other text on the same line")
        }

        @Test
        fun `Given a KDoc comment starting with multiple additional asterisks then do report but not autocorrect`() {
            val code =
                """
                /*****
                 * A group of *members*.
                 */
                class Foo
                """.trimIndent()
            @Suppress("ktlint:standard:max-line-length")
            kdocDelimiterRuleAssertThat(code).hasLintViolationWithoutAutoCorrect(1, 4, "Opening '/**' of a multi-line KDoc comment should not be followed by any other text on the same line")
        }

        @Test
        fun `Given a single-line KDoc comment starting with an additional asterisk then reformat`() {
            // Unlike the multi-line case, safely inserting the missing space does not require rewriting or moving
            // any content, so this case is autocorrected just like any other single-line spacing issue.
            val code =
                """
                /***foo*/
                class Foo
                """.trimIndent()
            val formattedCode =
                """
                /** *foo */
                class Foo
                """.trimIndent()
            @Suppress("ktlint:standard:max-line-length")
            kdocDelimiterRuleAssertThat(code)
                .hasLintViolations(
                    LintViolation(1, 4, "Expected a single space after '/**' in a single-line KDoc comment"),
                    LintViolation(1, 8, "Expected a single space before '*/' in a single-line KDoc comment"),
                ).isFormattedAs(formattedCode)
        }

        @Test
        fun `Given a multi-line KDoc comment for which the body starts on the opening line then do report but not autocorrect`() {
            val code =
                """
                /** A group of *members*.
                 * This class has no useful logic; it's just a documentation example.
                 */
                class Foo
                """.trimIndent()
            @Suppress("ktlint:standard:max-line-length")
            kdocDelimiterRuleAssertThat(code).hasLintViolationWithoutAutoCorrect(1, 4, "Opening '/**' of a multi-line KDoc comment should not be followed by any other text on the same line")
        }

        @Test
        fun `Given a multi-line KDoc comment for which the body ends on the closing line then do report but not autocorrect`() {
            val code =
                """
                /**
                 * A group of *members*.
                 * This class has no useful logic; it's just a documentation example. */
                class Foo
                """.trimIndent()
            @Suppress("ktlint:standard:max-line-length")
            kdocDelimiterRuleAssertThat(code).hasLintViolationWithoutAutoCorrect(3, 71, "Closing '*/' of a multi-line KDoc comment should not be preceded by any other text on the same line")
        }

        @Test
        fun `Given a multi-line KDoc comment with some text containing an KDOC_LPAR on same line as opening delimiter then do report but not autocorrect`() {
            val code =
                """
                /** Foo (bar)
                 */
                class Foo
                """.trimIndent()
            @Suppress("ktlint:standard:max-line-length")
            kdocDelimiterRuleAssertThat(code).hasLintViolationWithoutAutoCorrect(1, 4, "Opening '/**' of a multi-line KDoc comment should not be followed by any other text on the same line")
        }

        @Test
        fun `Given a multi-line KDoc comment with some text containing an markdown link on same line as opening delimiter then do report but not autocorrect`() {
            val code =
                """
                /** Foo [Bar] baz
                 */
                class Foo
                """.trimIndent()
            @Suppress("ktlint:standard:max-line-length")
            kdocDelimiterRuleAssertThat(code).hasLintViolationWithoutAutoCorrect(1, 4, "Opening '/**' of a multi-line KDoc comment should not be followed by any other text on the same line")
        }

        @Test
        fun `Given a multi-line KDoc comment with a tag on same line as opening delimiter then do report but not autocorrect`() {
            val code =
                """
                /** @see Bar
                 */
                class Foo
                """.trimIndent()
            @Suppress("ktlint:standard:max-line-length")
            kdocDelimiterRuleAssertThat(code).hasLintViolationWithoutAutoCorrect(1, 4, "Opening '/**' of a multi-line KDoc comment should not be followed by any other text on the same line")
        }
    }

    @Nested
    inner class `Given a single-line KDoc comment with incorrect spacing` {
        @Test
        fun `Given a single-line KDoc comment missing the leading space then reformat`() {
            val code =
                """
                /**A group of *members* */
                class Foo
                """.trimIndent()
            val formattedCode =
                """
                /** A group of *members* */
                class Foo
                """.trimIndent()
            kdocDelimiterRuleAssertThat(code)
                .hasLintViolation(1, 4, "Expected a single space after '/**' in a single-line KDoc comment")
                .isFormattedAs(formattedCode)
        }

        @Test
        fun `Given a single-line KDoc comment missing the trailing space then reformat`() {
            val code =
                """
                /** A group of members*/
                class Foo
                """.trimIndent()
            val formattedCode =
                """
                /** A group of members */
                class Foo
                """.trimIndent()
            kdocDelimiterRuleAssertThat(code)
                .hasLintViolation(1, 23, "Expected a single space before '*/' in a single-line KDoc comment")
                .isFormattedAs(formattedCode)
        }

        @Test
        fun `Given a single-line KDoc comment with multiple leading and trailing spaces then reformat`() {
            val code =
                """
                /**   A group of *members*   */
                class Foo
                """.trimIndent()
            val formattedCode =
                """
                /** A group of *members* */
                class Foo
                """.trimIndent()
            kdocDelimiterRuleAssertThat(code)
                .hasLintViolations(
                    LintViolation(1, 4, "Expected a single space after '/**' in a single-line KDoc comment"),
                    LintViolation(1, 30, "Expected a single space before '*/' in a single-line KDoc comment"),
                ).isFormattedAs(formattedCode)
        }

        @Test
        fun `Given a single-line KDoc comment missing both the leading and trailing space then reformat`() {
            val code =
                """
                /**A group of members*/
                class Foo
                """.trimIndent()
            val formattedCode =
                """
                /** A group of members */
                class Foo
                """.trimIndent()
            kdocDelimiterRuleAssertThat(code)
                .hasLintViolations(
                    LintViolation(1, 4, "Expected a single space after '/**' in a single-line KDoc comment"),
                    LintViolation(1, 22, "Expected a single space before '*/' in a single-line KDoc comment"),
                ).isFormattedAs(formattedCode)
        }

        @Test
        fun `Given a single-line KDoc comment ending with an extra closing asterisk then only report the violation`() {
            // The extra asterisk is real content (e.g. the closing asterisk of markdown emphasis) that needs to be
            // moved out of the KDOC_END token. As that requires replacing that token, which would conflict with
            // rules (such as IndentationRule) that may already be tracking a reference to it when run in the same
            // pass, this specific case is reported without autocorrect.
            val code =
                """
                /** A group of *members**/
                class Foo
                """.trimIndent()
            @Suppress("ktlint:standard:max-line-length")
            kdocDelimiterRuleAssertThat(code).hasLintViolationWithoutAutoCorrect(1, 24, "Expected a single space before '*/' in a single-line KDoc comment")
        }

        @Test
        fun `Given a single-line KDoc comment ending with an extra closing asterisk then format but avoid conflict with indentation`() {
            // The extra asterisk is real content (e.g. the closing asterisk of markdown emphasis) that needs to be
            // moved out of the KDOC_END token. As that requires replacing that token, which would conflict with
            // rules (such as IndentationRule) that may already be tracking a reference to it when run in the same
            // pass, this specific case is reported without autocorrect.
            val code =
                """
                /** A group of *members**/
                class Foo
                """.trimIndent()
            val formattedCode =
                """
                /** A group of *members* */
                class Foo
                """.trimIndent()
            kdocDelimiterRuleAssertThat(code)
                .addAdditionalRuleProvider { IndentationRule() }
                .hasLintViolation(1, 24, "Expected a single space before '*/' in a single-line KDoc comment")
                .isFormattedAs(formattedCode)
        }
    }

    @Test
    fun `Given a closing marker which is not aligned with the leading asterisks then reformat`() {
        val code =
            """
            /**
             * A group of *members*.
               */
            class Foo
            """.trimIndent()
        val formattedCode =
            """
            /**
             * A group of *members*.
             */
            class Foo
            """.trimIndent()
        kdocDelimiterRuleAssertThat(code)
            .hasLintViolation(3, 1, "Closing '*/' should align with the leading asterisks of the KDoc comment")
            .isFormattedAs(formattedCode)
    }

    @Test
    fun `Given a closing marker preceded by an additional leading asterisk then reformat`() {
        val code =
            """
            /**
             * A group of *members*.
             * */
            class Foo
            """.trimIndent()
        val formattedCode =
            """
            /**
             * A group of *members*.
             */
            class Foo
            """.trimIndent()
        kdocDelimiterRuleAssertThat(code)
            .hasLintViolation(3, 2, "Unexpected leading asterisk followed by Closing '*/' om same line")
            .isFormattedAs(formattedCode)
    }

    @Nested
    inner class `Given a missing leading asterisk` {
        @Test
        fun `Given a continuation line without a leading asterisk which is indented at least as much as required then only report the violation`() {
            val code =
                """
                class Bar {
                    /**
                     * It starts well, but for some reason the line below does not start with the asterisk sign, and is properly indented.
                        Some indented text not starting with an asterisk.
                     */
                    fun bar() {}
                }
                """.trimIndent()
            @Suppress("ktlint:standard:max-line-length")
            kdocDelimiterRuleAssertThat(code).hasLintViolationWithoutAutoCorrect(4, 9, "Each line of a multi-line KDoc comment should start with a leading asterisk")
        }

        @Test
        fun `Given a continuation line without a leading asterisk which is not indented at all then only report the violation`() {
            val code =
                """
                class Foo {
                    /**
                     * It starts well, but for some reason the line below does not start with the asterisk sign, and is not properly indented.
                Some unindented text not starting with an asterisk.
                     */
                    fun foo() {}
                }
                """.trimIndent()
            @Suppress("ktlint:standard:max-line-length")
            kdocDelimiterRuleAssertThat(code).hasLintViolationWithoutAutoCorrect(4, 1, "Each line of a multi-line KDoc comment should start with a leading asterisk")
        }

        @Test
        fun `Given a continuation line without a leading asterisk which is indented with a tab then only report the violation`() {
            val code =
                """
                class Foo {
                    /**
                     * It starts well, but the line below does not start with the asterisk sign.
                $TAB${TAB}Some text indented with tabs.
                     */
                    fun foo() {}
                }
                """.trimIndent()
            @Suppress("ktlint:standard:max-line-length")
            kdocDelimiterRuleAssertThat(code).hasLintViolationWithoutAutoCorrect(4, 3, "Each line of a multi-line KDoc comment should start with a leading asterisk")
        }

        @Test
        fun `Given two consecutive continuation lines without a leading asterisk then only report each violation`() {
            val code =
                """
                class Bar {
                    /**
                     * It starts well.
                        Some indented text not starting with an asterisk.
                            and more indented text not starting with an asterisk.
                     */
                    fun bar() {}
                }
                """.trimIndent()
            kdocDelimiterRuleAssertThat(code)
                .hasLintViolationsWithoutAutoCorrect(
                    LintViolation(4, 9, "Each line of a multi-line KDoc comment should start with a leading asterisk"),
                    LintViolation(5, 13, "Each line of a multi-line KDoc comment should start with a leading asterisk"),
                )
        }

        @Test
        fun `Given two consecutive lines with a deeply misaligned leading asterisk then reformat each individually`() {
            // Note that lines for "option 1" and "option 2" below do start with an actual leading asterisk but are not properly indented.
            // The intent of the developer can not be determined here.
            val code =
                """
                class Bar {
                    /**
                     * However, in case below:
                            * option 1
                            * option 2
                     * following applies: some more text.
                     */
                    fun bar() {}
                }
                """.trimIndent()
            @Suppress("ktlint:standard:max-line-length")
            kdocDelimiterRuleAssertThat(code)
                .hasLintViolationsWithoutAutoCorrect(
                    LintViolation(4, 13, "Leading asterisk is not properly aligned with the first asterisk of the KDoc opening '/**'"),
                    LintViolation(5, 13, "Leading asterisk is not properly aligned with the first asterisk of the KDoc opening '/**'"),
                )
        }

        @Test
        fun `Given two consecutive lines with a deeply misaligned leading asterisk using tab indents then reformat each individually`() {
            // Note that lines for "option 1" and "option 2" below do start with an actual leading asterisk but are not properly indented.
            // The intent of the developer can not be determined here.
            val code =
                """
                class Bar {
                $TAB/**
                $TAB * However, in case below:
                $TAB$TAB$TAB* option 1
                $TAB$TAB$TAB* option 2
                $TAB * following applies: some more text.
                $TAB */
                ${TAB}fun bar() {}
                }
                """.trimIndent()
            @Suppress("ktlint:standard:max-line-length")
            kdocDelimiterRuleAssertThat(code)
                .hasLintViolationsWithoutAutoCorrect(
                    LintViolation(4, 4, "Leading asterisk is not properly aligned with the first asterisk of the KDoc opening '/**'"),
                    LintViolation(5, 4, "Leading asterisk is not properly aligned with the first asterisk of the KDoc opening '/**'"),
                )
        }
    }

    @Nested
    inner class `Given an empty line inside a multi-line KDoc comment` {
        @Test
        fun `Given a well formed empty line then do not reformat`() {
            val code =
                """
                /**
                 * A group of *members*.
                 *
                 * More text.
                 */
                class Foo
                """.trimIndent()
            kdocDelimiterRuleAssertThat(code).hasNoLintViolations()
        }

        @Test
        fun `Given an empty line with trailing whitespace then reformat`() {
            val code =
                """
                /**
                 * A group of *members*.
                 *$SPACE
                 * More text.
                 */
                class Foo
                """.trimIndent()
            val formattedCode =
                """
                /**
                 * A group of *members*.
                 *
                 * More text.
                 */
                class Foo
                """.trimIndent()
            kdocDelimiterRuleAssertThat(code)
                .hasLintViolation(3, 3, "An empty line in a KDoc comment should not contain trailing whitespace after the leading asterisk")
                .isFormattedAs(formattedCode)
        }

        @Test
        fun `Given consecutive empty lines then reformat`() {
            val code =
                """
                /**
                 * A group of *members*.
                 *
                 *
                 * More text.
                 */
                class Foo
                """.trimIndent()
            val formattedCode =
                """
                /**
                 * A group of *members*.
                 *
                 * More text.
                 */
                class Foo
                """.trimIndent()
            kdocDelimiterRuleAssertThat(code)
                .hasLintViolation(4, 2, "Unexpected consecutive blank line")
                .isFormattedAs(formattedCode)
        }

        @Test
        fun `Given a single empty line between tags then do not reformat`() {
            val code =
                """
                /**
                 * A group of *members*.
                 *
                 * @param bar some bar
                 *
                 * @return New Baz for bar
                 */
                fun foo(bar: Bar): Baz = Baz(bar)
                """.trimIndent()
            kdocDelimiterRuleAssertThat(code).hasNoLintViolations()
        }

        @Test
        fun `Given consecutive empty lines between tags then do reformat but keep one blank line`() {
            val code =
                """
                /**
                 * A group of *members*.
                 *
                 * @param bar some bar
                 *
                 *
                 * @return New Baz for bar
                 */
                fun foo(bar: Bar): Baz = Baz(bar)
                """.trimIndent()
            val formattedCode =
                """
                /**
                 * A group of *members*.
                 *
                 * @param bar some bar
                 *
                 * @return New Baz for bar
                 */
                fun foo(bar: Bar): Baz = Baz(bar)
                """.trimIndent()
            kdocDelimiterRuleAssertThat(code)
                .hasLintViolation(6, 2, "Unexpected consecutive blank line")
                .isFormattedAs(formattedCode)
        }

        @Test
        fun `Given an empty line directly after the opening delimiter then reformat`() {
            val code =
                """
                /**
                 *
                 * Some text.
                 */
                class Foo
                """.trimIndent()
            val formattedCode =
                """
                /**
                 * Some text.
                 */
                class Foo
                """.trimIndent()
            kdocDelimiterRuleAssertThat(code)
                .hasLintViolation(2, 2, "No empty line expected after opening delimiter")
                .isFormattedAs(formattedCode)
        }

        @Test
        fun `Given an empty line directly before the closing delimiter then reformat`() {
            val code =
                """
                /**
                 * Some text.
                 *
                 */
                class Foo
                """.trimIndent()
            val formattedCode =
                """
                /**
                 * Some text.
                 */
                class Foo
                """.trimIndent()
            kdocDelimiterRuleAssertThat(code)
                .hasLintViolation(3, 2, "No empty line expected before closing delimiter")
                .isFormattedAs(formattedCode)
        }
    }

    @Nested
    inner class `Given a leading asterisk not followed by a single space` {
        @Test
        fun `Given a leading asterisk directly followed by content then reformat`() {
            val code =
                """
                /**
                 *A group of *members*.
                 */
                class Foo
                """.trimIndent()
            val formattedCode =
                """
                /**
                 * A group of *members*.
                 */
                class Foo
                """.trimIndent()
            kdocDelimiterRuleAssertThat(code)
                .hasLintViolation(2, 3, "A leading asterisk in a KDoc comment should be followed by a single space")
                .isFormattedAs(formattedCode)
        }

        @Test
        fun `Given a leading asterisk directly followed by a tag then reformat`() {
            val code =
                """
                /**
                 *@return Foo
                 */
                class Foo
                """.trimIndent()
            val formattedCode =
                """
                /**
                 * @return Foo
                 */
                class Foo
                """.trimIndent()
            kdocDelimiterRuleAssertThat(code)
                .hasLintViolation(2, 3, "A leading asterisk in a KDoc comment should be followed by a single space")
                .isFormattedAs(formattedCode)
        }

        @Test
        fun `Given a leading asterisk directly followed by a markdown link then reformat`() {
            val code =
                """
                /**
                 *[Foo] is a kdoc link as first word after the leading asterisk
                 */
                class Foo
                """.trimIndent()
            val formattedCode =
                """
                /**
                 * [Foo] is a kdoc link as first word after the leading asterisk
                 */
                class Foo
                """.trimIndent()
            kdocDelimiterRuleAssertThat(code)
                .hasLintViolation(2, 3, "A leading asterisk in a KDoc comment should be followed by a single space")
                .isFormattedAs(formattedCode)
        }

        @Test
        fun `Given a leading asterisk followed by multiple spaces then do not reformat`() {
            val code =
                """
                /**
                 *   A group of *members*.
                 */
                class Foo
                """.trimIndent()
            kdocDelimiterRuleAssertThat(code).hasNoLintViolations()
        }
    }

    @Nested
    inner class `Given the rule is run together with other KDoc related rules` {
        @Test
        fun `Given a well formed KDoc comment then running with kdoc-wrapping, kdoc and IndentationRule does not conflict`() {
            val code =
                """
                /**
                 * A group of *members*.
                 */
                class Foo
                """.trimIndent()
            kdocDelimiterRuleAssertThat(code)
                .addAdditionalRuleProviders({ KdocWrappingRule() }, { KdocRule() }, { IndentationRule() })
                .hasNoLintViolations()
        }

        @Test
        fun `Given a malformed KDoc comment then autocorrection is stable and does not loop`() {
            val code =
                """
                class Bar {
                    /**
                       * It starts well.
                     */
                    fun bar() {}
                }
                """.trimIndent()
            val formattedCode =
                """
                class Bar {
                    /**
                     * It starts well.
                     */
                    fun bar() {}
                }
                """.trimIndent()
            kdocDelimiterRuleAssertThat(code)
                .addAdditionalRuleProviders({ IndentationRule() })
                .isFormattedAs(formattedCode)
            // A second format run should not produce any further changes.
            kdocDelimiterRuleAssertThat(formattedCode)
                .addAdditionalRuleProviders({ IndentationRule() })
                .hasNoLintViolations()
        }

        @Test
        fun `Given a single-line KDoc comment with an extra closing asterisk then linting alongside IndentationRule does not throw`() {
            // Regression test: shrinking the KDOC_END leaf while IndentationRule also tracks it in the same pass
            // used to throw an exception from IndentationRule (see https://github.com/ktlint/ktlint/issues/2982).
            // As this case is no longer autocorrected (see the test above), running it together with
            // IndentationRule should merely report the violation, without throwing.
            val code =
                """
                class Bar {
                    /** A group of *members**/
                    fun bar() {}
                }
                """.trimIndent()
            kdocDelimiterRuleAssertThat(code)
                .addAdditionalRuleProviders({ IndentationRule() })
                .hasLintViolationWithoutAutoCorrect(2, 28, "Expected a single space before '*/' in a single-line KDoc comment")
        }
    }

    @ParameterizedTest(name = "code style: {0}")
    @EnumSource(
        value = CodeStyleValue::class,
        mode = EnumSource.Mode.EXCLUDE,
        names = ["KTLINT_OFFICIAL"],
    )
    fun `Given a malformed KDoc comment and a code style other than ktlint_official then the rule can still be run explicitly`(
        codeStyleValue: CodeStyleValue,
    ) {
        val code =
            """
            /**A group of *members* */
            class Foo
            """.trimIndent()
        val formattedCode =
            """
            /** A group of *members* */
            class Foo
            """.trimIndent()
        kdocDelimiterRuleAssertThat(code)
            .withEditorConfigOverride(CODE_STYLE_PROPERTY to codeStyleValue)
            .withEditorConfigOverride(KDOC_DELIMITER_RULE_ID.createRuleExecutionEditorConfigProperty() to RuleExecution.ENABLED)
            .hasLintViolation(1, 4, "Expected a single space after '/**' in a single-line KDoc comment")
            .isFormattedAs(formattedCode)
    }
}
