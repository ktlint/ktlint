package io.github.ktlint.core.rule.engine.core.api

import assertk.assertAll
import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import assertk.assertions.isTrue
import io.github.ktlint.core.rule.engine.api.Code
import io.github.ktlint.core.rule.engine.api.KtLintRuleEngine
import io.github.ktlint.core.rule.engine.core.api.ElementType.ANNOTATION_ENTRY
import io.github.ktlint.core.rule.engine.core.api.ElementType.CLASS
import io.github.ktlint.core.rule.engine.core.api.ElementType.CLASS_BODY
import io.github.ktlint.core.rule.engine.core.api.ElementType.CLASS_KEYWORD
import io.github.ktlint.core.rule.engine.core.api.ElementType.ENUM_ENTRY
import io.github.ktlint.core.rule.engine.core.api.ElementType.EOL_COMMENT
import io.github.ktlint.core.rule.engine.core.api.ElementType.FILE
import io.github.ktlint.core.rule.engine.core.api.ElementType.FUN
import io.github.ktlint.core.rule.engine.core.api.ElementType.FUN_KEYWORD
import io.github.ktlint.core.rule.engine.core.api.ElementType.IDENTIFIER
import io.github.ktlint.core.rule.engine.core.api.ElementType.LBRACE
import io.github.ktlint.core.rule.engine.core.api.ElementType.LPAR
import io.github.ktlint.core.rule.engine.core.api.ElementType.MODIFIER_LIST
import io.github.ktlint.core.rule.engine.core.api.ElementType.PRIVATE_KEYWORD
import io.github.ktlint.core.rule.engine.core.api.ElementType.RPAR
import io.github.ktlint.core.rule.engine.core.api.ElementType.THROW
import io.github.ktlint.core.rule.engine.core.api.ElementType.TYPE_REFERENCE
import io.github.ktlint.core.rule.engine.core.api.ElementType.VALUE_PARAMETER
import io.github.ktlint.core.rule.engine.core.api.ElementType.VALUE_PARAMETER_LIST
import io.github.ktlint.core.rule.engine.core.api.ElementType.WHITE_SPACE
import io.github.ktlint.core.test.SPACE
import io.github.ktlint.core.test.assertDoesNotThrow
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.lang.FileASTNode
import org.jetbrains.kotlin.psi.KtAnnotated
import org.jetbrains.kotlin.psi.psiUtil.leaves
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import kotlin.reflect.KFunction1

class ASTNodeExtensionTest {
    @Test
    fun `Given an enum class body then get all leaves in the open range of the class body`() {
        val code =
            """
            enum class Shape {
                FOO, FOOBAR, BAR
            }
            """.trimIndent()
        val enumClassBody = code.transformAst(::toEnumClassBodySequence)

        val actual =
            leavesInOpenRange(enumClassBody.first(), enumClassBody.last())
                .map { it.text }
                .toList()

        assertThat(actual).containsExactly(
            // LBRACE is omitted from class body as it is an open range
            "\n    ",
            "FOO",
            ",",
            " ",
            "FOOBAR",
            ",",
            " ",
            "BAR",
            "\n",
            // RBRACE is omitted from class body as it is an open range
        )
    }

    @Test
    fun `Given an enum class body then get all leaves in the closed range of the class body`() {
        val code =
            """
            enum class Shape {
                FOO, FOOBAR, BAR
            }
            """.trimIndent()
        val enumClassBody = code.transformAst(::toEnumClassBodySequence)

        val actual =
            leavesInClosedRange(enumClassBody.first(), enumClassBody.last())
                .map { it.text }
                .toList()

        assertThat(actual).containsExactly(
            "{",
            "\n    ",
            "FOO",
            ",",
            " ",
            "FOOBAR",
            ",",
            " ",
            "BAR",
            "\n",
            "}",
        )
    }

    @Nested
    inner class NoNewLineInOpenRange {
        @Test
        fun `Given an enum class with no whitespace leaf containing a newline between the first and last enum entry`() {
            val code =
                """
                enum class Shape {
                    FOO, FOOBAR, BAR
                }
                """.trimIndent()
            val enumEntries =
                code
                    .transformAst(::toEnumClassBodySequence)
                    .filter { it.elementType == ENUM_ENTRY }

            val actual = noNewLineInOpenRange(enumEntries.first(), enumEntries.last())

            assertThat(actual).isTrue()
        }

        @Test
        fun `Given a range of nodes not starting or ending with a whitespace leaf containing a newline but containing another whitespace leaf containing a newline`() {
            val code =
                """
                enum class Shape {
                    FOO,
                    FOOBAR,
                    BAR
                }
                """.trimIndent()
            val enumClassBody = code.transformAst(::toEnumClassBodySequence)

            val actual = noNewLineInClosedRange(enumClassBody.first(), enumClassBody.last())

            assertThat(actual).isFalse()
        }

        @Test
        fun `Given a range of nodes not containing a newline inside a block comment in between`() {
            val code =
                """
                enum class Shape { FOO, /*
                newline in a block comment is ignored as it is not part of a whitespace leaf
                */ FOOBAR, BAR }
                """.trimIndent()
            val enumClassBody = code.transformAst(::toEnumClassBodySequence)

            val actual = noNewLineInClosedRange(enumClassBody.first(), enumClassBody.last())

            assertThat(actual).isFalse()
        }
    }

    @Nested
    inner class HasNewLineInClosedRange {
        @Test
        fun `Given an enum class with no whitespace leaf containing a newline between the first and last enum entry`() {
            val code =
                """
                enum class Shape { FOO, FOOBAR, BAR }
                """.trimIndent()
            val enumEntries = code.transformAst(::toEnumClassBodySequence)

            val actual = hasNewLineInClosedRange(enumEntries.first(), enumEntries.last())

            assertThat(actual).isFalse()
        }

        @Test
        fun `Given a range of nodes starting with a whitespace leaf containing a newline but other whitespace leaves not containing a newline`() {
            val code =
                """
                enum class Shape
                    { FOO, FOOBAR, BAR } // Malformed on purpose for test
                """.trimIndent()
            val enumClass = code.transformAst(::toEnumClassSequence)

            val actual =
                hasNewLineInClosedRange(
                    enumClass.first { it.isWhiteSpaceWithNewline },
                    enumClass.last(),
                )

            assertThat(actual).isTrue()
        }

        @Test
        fun `Given a range of nodes not starting or ending with a whitespace leaf containing a newline but containing another whitespace leaf containing a newline`() {
            val code =
                """
                enum class Shape {
                    FOO,
                    FOOBAR,
                    BAR
                }
                """.trimIndent()
            val enumClassBody = code.transformAst(::toEnumClassBodySequence)

            val actual = hasNewLineInClosedRange(enumClassBody.first(), enumClassBody.last())

            assertThat(actual).isTrue()
        }

        @Test
        fun `Given a range of nodes ending with a whitespace leaf containing a newline but other whitespace leaves not containing a newline`() {
            val code =
                """
                enum class Shape { FOO, FOOBAR, BAR
                } // Malformed on purpose for test
                """.trimIndent()
            val enumBodyClass = code.transformAst(::toEnumClassBodySequence)

            val actual =
                hasNewLineInClosedRange(
                    enumBodyClass.first(),
                    enumBodyClass.last { it.isWhiteSpaceWithNewline },
                )

            assertThat(actual).isTrue()
        }

        @Test
        fun `Given a range of nodes containing a newline inside a block comment in between`() {
            val code =
                """
                enum class Shape { FOO, /*
                newline in a block comment is ignored as it is not part of a whitespace leaf
                */ FOOBAR, BAR }
                """.trimIndent()
            val enumClassBody = code.transformAst(::toEnumClassBodySequence)

            val actual = hasNewLineInClosedRange(enumClassBody.first(), enumClassBody.last())

            assertThat(actual).isTrue()
        }
    }

    @Nested
    inner class NoNewLineInClosedRange {
        @Test
        fun `Given an enum class with no whitespace leaf containing a newline between the first and last enum entry`() {
            val code =
                """
                enum class Shape {
                    FOO, FOOBAR, BAR
                }
                """.trimIndent()
            val enumEntries =
                code
                    .transformAst(::toEnumClassBodySequence)
                    .filter { it.elementType == ENUM_ENTRY }

            val actual = noNewLineInClosedRange(enumEntries.first(), enumEntries.last())

            assertThat(actual).isTrue()
        }

        @Test
        fun `Given a range of nodes starting with a whitespace leaf containing a newline but other whitespace leaves not containing a newline`() {
            val code =
                """
                enum class Shape {
                    FOO, FOOBAR, BAR } // Malformed on purpose for test
                """.trimIndent()
            val enumClassBody = code.transformAst(::toEnumClassBodySequence)

            val actual = noNewLineInClosedRange(enumClassBody.first(), enumClassBody.last())

            assertThat(actual).isFalse()
        }

        @Test
        fun `Given a range of nodes not starting or ending with a whitespace leaf containing a newline but containing another whitespace leaf containing a newline`() {
            val code =
                """
                enum class Shape {
                    FOO,
                    FOOBAR,
                    BAR
                }
                """.trimIndent()
            val enumClassBody = code.transformAst(::toEnumClassBodySequence)

            val actual = noNewLineInClosedRange(enumClassBody.first(), enumClassBody.last())

            assertThat(actual).isFalse()
        }

        @Test
        fun `Given a range of nodes ending with a whitespace leaf containing a newline but other whitespace leaves not containing a newline`() {
            val code =
                """
                enum class Shape { FOO, FOOBAR, BAR
                } // Malformed on purpose for test
                """.trimIndent()
            val enumClassBody = code.transformAst(::toEnumClassBodySequence)

            val actual = noNewLineInClosedRange(enumClassBody.first(), enumClassBody.last())

            assertThat(actual).isFalse()
        }

        @Test
        fun `Given a range of nodes not containing a newline inside a block comment in between`() {
            val code =
                """
                enum class Shape { FOO, /*
                newline in a block comment is ignored as it is not part of a whitespace leaf
                */ FOOBAR, BAR }
                """.trimIndent()
            val enumClassBody = code.transformAst(::toEnumClassBodySequence)

            val actual = noNewLineInClosedRange(enumClassBody.first(), enumClassBody.last())

            assertThat(actual).isFalse()
        }
    }

    @Nested
    inner class UpsertWhitespaceBeforeMe {
        @Test
        fun `Given an upsert of a whitespace based before another whitespace node then replace the existing whitespace element`() {
            val code =
                """
                fun foo( ) = 42
                """.trimIndent()
            val formattedCode =
                """
                fun foo(
                ) = 42
                """.trimIndent()

            val actual =
                code
                    .transformAst {
                        findChildByType(FUN)
                            ?.findChildByType(VALUE_PARAMETER_LIST)
                            ?.findChildByType(WHITE_SPACE)
                            ?.upsertWhitespaceBeforeMe("\n")
                    }.text

            assertThat(actual).isEqualTo(formattedCode)
        }

        @Test
        fun `Given an upsert of a whitespace before a non-whitespace node (RPAR) which is preceded by a whitespace leaf element (LPAR) then then replace the whitespace element`() {
            val code =
                """
                fun foo( ) = 42
                """.trimIndent()
            val formattedCode =
                """
                fun foo(

                ) = 42
                """.trimIndent()

            val actual =
                code
                    .transformAst {
                        nextLeaf { it.elementType == RPAR }
                            ?.upsertWhitespaceBeforeMe("\n\n")
                    }.text

            assertThat(actual).isEqualTo(formattedCode)
        }

        @Test
        fun `Given an upsert of a whitespace before a non-whitespace node (RPAR) which is preceded by another non-whitespace leaf element (LPAR) then create a new whitespace element`() {
            val code =
                """
                fun foo() = 42
                """.trimIndent()
            val formattedCode =
                """
                fun foo(

                ) = 42
                """.trimIndent()

            val actual =
                code
                    .transformAst {
                        nextLeaf { it.elementType == RPAR }
                            ?.upsertWhitespaceBeforeMe("\n\n")
                    }.text

            assertThat(actual).isEqualTo(formattedCode)
        }

        @Test
        fun `Given an upsert of a whitespace before a composite node (ANNOTATION_ENTRY) which is the first child of another composite element then create a new whitespace before (VALUE_PARAMETER)`() {
            val code =
                """
                fun foo(@Bar string: String) = 42
                """.trimIndent()
            val formattedCode =
                """
                fun foo(
                    @Bar string: String) = 42
                """.trimIndent()

            val actual =
                code
                    .transformAst {
                        findChildByType(FUN)
                            ?.findChildByType(VALUE_PARAMETER_LIST)
                            ?.findChildByType(VALUE_PARAMETER)
                            ?.findChildByType(MODIFIER_LIST)
                            ?.findChildByType(ANNOTATION_ENTRY)
                            ?.upsertWhitespaceBeforeMe("\n    ")
                    }.text

            assertThat(actual).isEqualTo(formattedCode)
        }

        @Test
        fun `Given an upsert of a whitespace before a composite node (VALUE_PARAMETER) which is preceded by a non-whitespace leaf element (LPAR) then create a new whitespace element before the node (VALUE_PARAMETER)`() {
            val code =
                """
                fun foo(string: String) = 42
                """.trimIndent()
            val formattedCode =
                """
                fun foo(
                    string: String) = 42
                """.trimIndent()

            val actual =
                code
                    .transformAst {
                        findChildByType(FUN)
                            ?.findChildByType(VALUE_PARAMETER_LIST)
                            ?.findChildByType(VALUE_PARAMETER)
                            ?.upsertWhitespaceBeforeMe("\n    ")
                    }.text

            assertThat(actual).isEqualTo(formattedCode)
        }

        @Test
        fun `Given an upsert of a whitespace before a composite node (ANNOTATION_ENTRY) which is preceded by another composite node (ANNOTATION_ENTRY) then create a new whitespace between the ANNOTATION_ENTRIES`() {
            val code =
                """
                fun foo(@Bar@Foo string: String) = 42
                """.trimIndent()
            val formattedCode =
                """
                fun foo(@Bar @Foo string: String) = 42
                """.trimIndent()

            val actual =
                code
                    .transformAst {
                        findChildByType(FUN)
                            ?.findChildByType(VALUE_PARAMETER_LIST)
                            ?.findChildByType(VALUE_PARAMETER)
                            ?.findChildByType(MODIFIER_LIST)
                            ?.findChildByType(ANNOTATION_ENTRY)
                            ?.nextSibling
                            ?.upsertWhitespaceBeforeMe(" ")
                    }.text

            assertThat(actual).isEqualTo(formattedCode)
        }

        @Test
        fun `Issue 2688 - Given a class without a space between the identifier and the left curly brace then insert the whitespace at the correct position in the AST`() {
            val code =
                """
                class Foo{
                    // some comment
                }
                """.trimIndent()

            val actual =
                code
                    .transformAst {
                        findChildByType(CLASS)
                            ?.findChildByType(CLASS_BODY)
                            ?.findChildByType(LBRACE)
                            ?.upsertWhitespaceBeforeMe(" ")
                    }.findChildByType(CLASS)
                    ?.findChildByType(CLASS_BODY)
                    ?.prevSibling
                    ?.let { it.isWhiteSpace && it.text == " " }
                    ?: false

            assertThat(actual).isTrue()
        }
    }

    @Nested
    inner class UpsertWhitespaceAfterMe {
        @Test
        fun `Given an upsert of a whitespace based after another whitespace node then replace the existing whitespace element`() {
            val code =
                """
                fun foo( ) = 42
                """.trimIndent()
            val formattedCode =
                """
                fun foo(
                ) = 42
                """.trimIndent()

            val actual =
                code
                    .transformAst {
                        findChildByType(FUN)
                            ?.findChildByType(VALUE_PARAMETER_LIST)
                            ?.findChildByType(WHITE_SPACE)
                            ?.upsertWhitespaceAfterMe("\n")
                    }.text

            assertThat(actual).isEqualTo(formattedCode)
        }

        @Test
        fun `Given an upsert of a whitespace after a non-whitespace node (LPAR) which is followed by a whitespace leaf element (RPAR) then then replace the whitespace element`() {
            val code =
                """
                fun foo( ) = 42
                """.trimIndent()
            val formattedCode =
                """
                fun foo(

                ) = 42
                """.trimIndent()

            val actual =
                code
                    .transformAst {
                        nextLeaf { it.elementType == LPAR }
                            ?.upsertWhitespaceAfterMe("\n\n")
                    }.text

            assertThat(actual).isEqualTo(formattedCode)
        }

        @Test
        fun `Given an upsert of a whitespace after a non-whitespace node (LPAR) which is followed by another non-whitespace leaf element (RPAR) then create a new whitespace element`() {
            val code =
                """
                fun foo() = 42
                """.trimIndent()
            val formattedCode =
                """
                fun foo(

                ) = 42
                """.trimIndent()

            val actual =
                code
                    .transformAst {
                        nextLeaf { it.elementType == LPAR }
                            ?.upsertWhitespaceAfterMe("\n\n")
                    }.text

            assertThat(actual).isEqualTo(formattedCode)
        }

        @Test
        fun `Given an upsert of a whitespace after a composite node (ANNOTATION_ENTRY) which is the last child of another composite element then create a new whitespace after (VALUE_PARAMETER)`() {
            val code =
                """
                fun foo(@Bar string: String) = 42
                """.trimIndent()
            val formattedCode =
                """
                fun foo(@Bar string: String
                ) = 42
                """.trimIndent()

            val actual =
                code
                    .transformAst {
                        findChildByType(FUN)
                            ?.findChildByType(VALUE_PARAMETER_LIST)
                            ?.findChildByType(VALUE_PARAMETER)
                            ?.findChildByType(TYPE_REFERENCE)
                            ?.upsertWhitespaceAfterMe("\n")
                    }.text

            assertThat(actual).isEqualTo(formattedCode)
        }

        @Test
        fun `Given an upsert of a whitespace after a composite node (VALUE_PARAMETER) which is followed by a non-whitespace leaf element (RPAR) then create a new whitespace element after the node (VALUE_PARAMETER)`() {
            val code =
                """
                fun foo(string: String) = 42
                """.trimIndent()
            val formattedCode =
                """
                fun foo(string: String
                ) = 42
                """.trimIndent()

            val actual =
                code
                    .transformAst {
                        findChildByType(FUN)
                            ?.findChildByType(VALUE_PARAMETER_LIST)
                            ?.findChildByType(VALUE_PARAMETER)
                            ?.upsertWhitespaceAfterMe("\n")
                    }.text

            assertThat(actual).isEqualTo(formattedCode)
        }

        @Test
        fun `Given an upsert of a whitespace after a composite node (ANNOTATION_ENTRY) which is followed by another composite node (ANNOTATION_ENTRY) then create a new whitespace between the ANNOTATION_ENTRIES`() {
            val code =
                """
                fun foo(@Bar@Foo string: String) = 42
                """.trimIndent()
            val formattedCode =
                """
                fun foo(@Bar @Foo string: String) = 42
                """.trimIndent()

            val actual =
                code
                    .transformAst {
                        findChildByType(FUN)
                            ?.findChildByType(VALUE_PARAMETER_LIST)
                            ?.findChildByType(VALUE_PARAMETER)
                            ?.findChildByType(MODIFIER_LIST)
                            ?.findChildByType(ANNOTATION_ENTRY)
                            ?.upsertWhitespaceAfterMe(" ")
                    }.text

            assertThat(actual).isEqualTo(formattedCode)
        }

        @Test
        fun `Issue 2688 - Given a class without a space between the identifier and the left curly brace then insert the whitespace at the correct position in the AST`() {
            val code =
                """
                fun foo(){
                    // some comment
                }
                """.trimIndent()

            val actual =
                code
                    .transformAst {
                        findChildByType(FUN)
                            ?.findChildByType(VALUE_PARAMETER_LIST)
                            ?.findChildByType(RPAR)
                            ?.upsertWhitespaceAfterMe(" ")
                    }.findChildByType(FUN)
                    ?.findChildByType(VALUE_PARAMETER_LIST)
                    ?.nextSibling
                    ?.let { it.isWhiteSpace && it.text == " " }
                    ?: false

            assertThat(actual).isTrue()
        }
    }

    @Test
    fun `Given some identifiers at different indentation levels`() {
        val code =
            """
            class Foo1 {
                val foo2 = "foo2"

                fun foo3() {
                    val foo4 = "foo4"
                }
            }
            """.trimIndent()

        val actual =
            transformCodeToAST(code)
                .firstChildLeafOrSelf
                .leaves()
                .filter { it.elementType == IDENTIFIER }
                .associate { it.text to it.indent }
                .toList()

        assertThat(actual).containsExactly(
            "Foo1" to "\n",
            "foo2" to "\n    ",
            "foo3" to "\n    ",
            "foo4" to "\n        ",
        )
    }

    @Suppress("DEPRECATION")
    @Test
    fun `Given some line containing identifiers at different indentation levels then check that all leaves on those line are found`() {
        val code =
            """
            class Foo1 {
                val foo2 = "foo2"

                fun foo3() {
                    val foo4 = "foo4"
                }
            }
            """.trimIndent()

        val actual =
            transformCodeToAST(code)
                .firstChildLeafOrSelf
                .leaves()
                .filter { it.elementType == IDENTIFIER }
                .map { identifier ->
                    identifier
                        .leavesOnLine
                        .joinToString(separator = "") { it.text }
                }.toList()

        assertThat(actual).containsExactly(
            "class Foo1 {",
            "\n    val foo2 = \"foo2\"",
            "\n\n    fun foo3() {",
            "\n        val foo4 = \"foo4\"",
        )
    }

    @Nested
    inner class LineLengthWithoutNewlinePrefix {
        @Suppress("DEPRECATION")
        @Test
        fun `Given some lines containing identifiers at different indentation levels then get line length exclusive the leading newline characters`() {
            val code =
                """
                class Foo1 {
                    val foo2 = "foo2"

                    fun foo3() {
                        val foo4 = "foo4"
                    }
                }
                """.trimIndent()

            val actual =
                transformCodeToAST(code)
                    .firstChildLeafOrSelf
                    .leaves()
                    .filter { it.elementType == IDENTIFIER }
                    .map { identifier -> identifier.leavesOnLine.lineLength }
                    .toList()

            assertThat(actual).containsExactly(
                "class Foo1 {".length,
                "    val foo2 = \"foo2\"".length,
                "    fun foo3() {".length,
                "        val foo4 = \"foo4\"".length,
            )
        }

        @Suppress("DEPRECATION")
        @Test
        fun `Given some lines containing identifiers and EOL comment then get line length exclusive the leading newline characters and exclusive EOL comment`() {
            val code =
                """
                class Foo1 {
                    val foo2 = "foo2" // some comment

                    fun foo3() {
                        val foo4 = "foo4" // some comment
                    }
                }
                """.trimIndent()

            val actual =
                transformCodeToAST(code)
                    .firstChildLeafOrSelf
                    .leaves()
                    .filter { it.elementType == IDENTIFIER }
                    .map { identifier -> identifier.leavesOnLine.lineLength }
                    .toList()

            assertThat(actual).containsExactly(
                "class Foo1 {".length,
                "    val foo2 = \"foo2\" // some comment".length,
                "    fun foo3() {".length,
                "        val foo4 = \"foo4\" // some comment".length,
            )
        }

        @Suppress("DEPRECATION")
        @Test
        fun `Given some lines containing identifiers at different indentation levels then get line length exclusive the leading newline characters until and including the identifier`() {
            val code =
                """
                class Foo1 {
                    val foo2 = "foo2"

                    fun foo3() {
                        val foo4 = "foo4"
                    }
                }
                """.trimIndent()

            val actual =
                transformCodeToAST(code)
                    .firstChildLeafOrSelf
                    .leaves()
                    .filter { it.elementType == IDENTIFIER }
                    .map { identifier ->
                        identifier
                            .leavesOnLine
                            .takeWhile { it.prevLeaf != identifier }
                            .lineLength
                    }.toList()

            assertThat(actual).containsExactly(
                "class Foo1".length,
                "    val foo2".length,
                "    fun foo3".length,
                "        val foo4".length,
            )
        }

        @Suppress("DEPRECATION")
        @Test
        fun `xxGiven some lines containing identifiers at different indentation levels then get line length exclusive the leading newline characters until and including the identifier`() {
            val code =
                """
                val foo1 = "foo1"$SPACE
                val foo2 = "foo2"
                """.trimIndent()

            assertDoesNotThrow {
                transformCodeToAST(code)
                    .firstChildLeafOrSelf
                    .leaves()
                    .filter { it.elementType == IDENTIFIER }
                    .map { identifier ->
                        identifier
                            .leavesOnLine
                            .takeWhile { it.prevLeaf != identifier }
                            .lineLength
                    }.toList()
            }
        }
    }

    @Nested
    inner class LineLength {
        @Test
        fun `Given some lines containing identifiers at different indentation levels then get line length exclusive the leading newline characters`() {
            val code =
                """
                class Foo1 {
                    val foo2 = "foo2"

                    fun foo3() {
                        val foo4 = "foo4"
                    }
                }
                """.trimIndent()

            val actual =
                transformCodeToAST(code)
                    .firstChildLeafOrSelf
                    .leaves()
                    .filter { it.elementType == IDENTIFIER }
                    .map { identifier -> identifier.leavesOnLine.lineLength }
                    .toList()

            assertThat(actual).containsExactly(
                "class Foo1 {".length,
                "    val foo2 = \"foo2\"".length,
                "    fun foo3() {".length,
                "        val foo4 = \"foo4\"".length,
            )
        }

        @Test
        fun `Given some lines containing identifiers and EOL comment then get line length exclusive the leading newline characters`() {
            val code =
                """
                class Foo1 {
                    val foo2 = "foo2" // some comment

                    fun foo3() {
                        val foo4 = "foo4" // some comment
                    }
                }
                """.trimIndent()

            val actual =
                transformCodeToAST(code)
                    .firstChildLeafOrSelf
                    .leaves()
                    .filter { it.elementType == IDENTIFIER }
                    .map { identifier -> identifier.leavesOnLine.lineLength }
                    .toList()

            assertThat(actual).containsExactly(
                "class Foo1 {".length,
                "    val foo2 = \"foo2\" // some comment".length,
                "    fun foo3() {".length,
                "        val foo4 = \"foo4\" // some comment".length,
            )
        }

        @Test
        fun `Given some lines containing identifiers and EOL comment then get line length exclusive the leading newline characters and exclusive EOL comment`() {
            val code =
                """
                class Foo1 {
                    val foo2 = "foo2" // some comment

                    fun foo3() {
                        val foo4 = "foo4" // some comment
                    }
                }
                """.trimIndent()

            val actual =
                transformCodeToAST(code)
                    .firstChildLeafOrSelf
                    .leaves()
                    .filter { it.elementType == IDENTIFIER }
                    .map { identifier -> identifier.leavesOnLine.dropTrailingEolComment().lineLength }
                    .toList()

            assertThat(actual).containsExactly(
                "class Foo1 {".length,
                "    val foo2 = \"foo2\"".length,
                "    fun foo3() {".length,
                "        val foo4 = \"foo4\"".length,
            )
        }

        @Test
        fun `Given a line only containing an EOL-comment`() {
            val code =
                """
                fun bar() {
                    // no-op
                }
                """.trimIndent()
            val actual =
                transformCodeToAST(code)
                    .firstChildLeafOrSelf
                    .leavesForwardsIncludingSelf
                    .first { it.elementType == EOL_COMMENT }
                    .leavesOnLine
                    .dropTrailingEolComment()
                    .lineLength

            assertThat(actual).isEqualTo(4)
        }
    }

    @ParameterizedTest(name = "Text between FUN_KEYWORD and IDENTIFIER: {0}")
    @ValueSource(
        strings = [
            " ",
            "\n",
            "/* some comment*/",
            "// some EOL comment\n",
        ],
    )
    fun `Given a function declaration then the IDENTIFIER should be after the FUN_KEYWORD element type`(separator: String) {
        val code =
            """
            fun${separator}foo() = 42
            """.trimIndent()
        val actual =
            transformCodeToAST(code)
                .findChildByType(FUN)!!
                .findChildByType(IDENTIFIER)!!
                .afterCodeSibling(FUN_KEYWORD)

        assertThat(actual).isTrue()
    }

    @ParameterizedTest(name = "Text between FUN_KEYWORD and IDENTIFIER: {0}")
    @ValueSource(
        strings = [
            " ",
            "\n",
            "/* some comment*/",
            "// some EOL comment\n",
        ],
    )
    fun `Given a function declaration then the FUN_KEYWORD should be before the IDENTIFIER element type`(separator: String) {
        val code =
            """
            fun${separator}foo() = 42
            """.trimIndent()
        val actual =
            transformCodeToAST(code)
                .findChildByType(FUN)!!
                .findChildByType(FUN_KEYWORD)!!
                .beforeCodeSibling(IDENTIFIER)

        assertThat(actual).isTrue()
    }

    @Test
    fun `Given a function declaration then the IDENTIFIER should be between the FUN_KEYWORD and the VALUE_PARAMETER_LIST element type`() {
        val code =
            """
            public fun /*test*/ foo /*test*/ (): Int = 42
            """.trimIndent()
        val identifier =
            transformCodeToAST(code)
                .findChildByType(FUN)!!
                .findChildByType(IDENTIFIER)!!

        assertAll {
            assertThat(identifier.betweenCodeSiblings(FUN_KEYWORD, VALUE_PARAMETER_LIST)).isTrue()
            assertThat(identifier.betweenCodeSiblings(MODIFIER_LIST, TYPE_REFERENCE)).isTrue()
        }
    }

    @Test
    fun `Given that getFirstLeafOnLineOrSelf is called for the FILE element type then do not throw a null pointer exception`() {
        val code =
            """
            // some comment
            """.trimIndent()

        val actual =
            transformCodeToAST(code)
                .also { require(it.elementType == FILE) }
                .firstLeafOnLineOrSelf
                .text

        assertThat(actual).contains(code)
    }

    @Test
    fun `Issue 2602 - Given that getFirstLeafOnLineOrSelf is called for a block comment containing a new line then do not throw a null pointer exception`() {
        val code =
            """
            val foo = /* some
                comment */ "foo"
            """.trimIndent()

        val actual =
            transformCodeToAST(code)
                .lastChildLeafOrSelf
                .firstLeafOnLineOrSelf
                .text

        // The newline (and indentation spaces) before the word "comment" inside the block comment is entirely ignored. As there are no
        // whitespace nodes containing a newline, this piece of code is considered to be an oneliner starting with the word "val".
        assertThat(actual).contains("val")
    }

    @Nested
    inner class HasModifier {
        @Test
        fun `Given a node having the specified modifier then return true`() {
            val code =
                """
                private fun foo() = 42
                """.trimIndent()
            val actual =
                transformCodeToAST(code)
                    .findChildByType(FUN)!!
                    .hasModifier(PRIVATE_KEYWORD)

            assertThat(actual).isTrue()
        }

        @Test
        fun `Given a node having a modifier list but not having the specified modifier then return false`() {
            val code =
                """
                internal fun foo() = 42
                """.trimIndent()
            val actual =
                transformCodeToAST(code)
                    .findChildByType(FUN)!!
                    .hasModifier(PRIVATE_KEYWORD)

            assertThat(actual).isFalse()
        }

        @Test
        fun `Given a node not having a modifier list then return false`() {
            val code =
                """
                fun foo() = 42
                """.trimIndent()
            val actual =
                transformCodeToAST(code)
                    .findChildByType(FUN)!!
                    .hasModifier(PRIVATE_KEYWORD)

            assertThat(actual).isFalse()
        }
    }

    @Nested
    inner class FindChildByTypeRecursively {
        @Test
        fun `Given a node with a target type return non-null`() {
            val code =
                """
                class MyClass {
                    fun foo() = 42
                }
                """.trimIndent()
            val result =
                transformCodeToAST(code)
                    .findChildByTypeRecursively(FUN)
            assertThat(result).isNotNull()
        }

        @Test
        fun `Given a node without a target type return null`() {
            val code =
                """
                class MyClass {

                }
                """.trimIndent()
            val result =
                transformCodeToAST(code)
                    .findChildByTypeRecursively(FUN)
            assertThat(result).isNull()
        }
    }

    @Test
    fun `Given a simple class declaration without body then the declaration itself is derived from KtAnnotated while its child elements are not derived from KtAnnotated`() {
        val code =
            """
            class Foo
            """.trimIndent()

        val actual = transformCodeToAST(code).findChildByType(CLASS)!!

        assertThat(actual.isKtAnnotated).isTrue()
        assertThat(actual.findChildByType(CLASS_KEYWORD)!!.isKtAnnotated).isFalse()
        assertThat(actual.findChildByType(IDENTIFIER)!!.isKtAnnotated).isFalse()

        assertThat(actual.isPsiType<KtAnnotated>()).isTrue()
        assertThat(actual.findChildByType(CLASS_KEYWORD)!!.isPsiType<KtAnnotated>()).isFalse()
        assertThat(actual.findChildByType(IDENTIFIER)!!.isPsiType<KtAnnotated>()).isFalse()
    }

    @Nested
    inner class HasNoMaxLineLengthSuppression {
        val ktlintRuleEngine =
            KtLintRuleEngine(
                ruleProviders =
                    setOf(
                        RuleV2Provider { DummyRule() },
                    ),
            )

        @Test
        fun `Given an ASTNode, and all of its parents are not annotated with suppression then return true`() {
            val code =
                """
                class Foo {
                    fun bar() =
                         throw RuntimeException("some very long message")
                }
                """.trimIndent()

            val actual = ktlintRuleEngine.transformToAst(Code.fromSnippet(code)).findChildByTypeRecursively(THROW)!!

            assertThat(actual.hasNoMaxLineLengthSuppression()).isTrue()
        }

        @Test
        fun `Given an ASTNode annotated with suppression for other rule than max-line-length, then return true`() {
            val code =
                """
                class Foo {
                    fun bar() =
                         @Suppress("ktlint:standard:some-rule")
                         throw RuntimeException("some very long message")
                }
                """.trimIndent()

            val actual = ktlintRuleEngine.transformToAst(Code.fromSnippet(code)).findChildByTypeRecursively(THROW)!!

            assertThat(actual.hasNoMaxLineLengthSuppression()).isTrue()
        }

        @Test
        fun `Given an expression annotated with suppression for max-line-length rule, then return false`() {
            val code =
                """
                class Foo {
                    fun bar() =
                         @Suppress("ktlint:standard:max-line-length")
                         throw RuntimeException("some very long message")
                }
                """.trimIndent()

            val actual = transformCodeToAST(code).findChildByTypeRecursively(THROW)!!

            assertThat(actual.hasNoMaxLineLengthSuppression()).isFalse()
        }

        @Test
        fun `Given an expression annotated with suppression for multiple rules including max-line-length rule, then return false`() {
            val code =
                """
                class Foo {
                    fun bar() =
                         @Suppress("ktlint:standard:abc", "ktlint:standard:max-line-length", "ktlint:standard:xyz")
                         throw RuntimeException("some very long message")
                }
                """.trimIndent()

            val actual = transformCodeToAST(code).findChildByTypeRecursively(THROW)!!

            assertThat(actual.hasNoMaxLineLengthSuppression()).isFalse()
        }

        @Test
        fun `Given an expression, for which a parent is annotated with suppression for max-line-length rule, then return false`() {
            val code =
                """
                class Foo {
                     @Suppress("ktlint:standard:max-line-length")
                    fun bar() =
                         throw RuntimeException("some very long message")
                }
                """.trimIndent()

            val actual = transformCodeToAST(code).findChildByTypeRecursively(THROW)!!

            assertThat(actual.hasNoMaxLineLengthSuppression()).isFalse()
        }

        @Test
        fun `Given an expression, for which a grandparent is annotated with suppression for max-line-length rule, then return false`() {
            val code =
                """
                @Suppress("ktlint:standard:max-line-length")
                class Foo {
                    fun bar() =
                         throw RuntimeException("some very long message")
                }
                """.trimIndent()

            val actual = transformCodeToAST(code).findChildByTypeRecursively(THROW)!!

            assertThat(actual.hasNoMaxLineLengthSuppression()).isFalse()
        }

        @Test
        fun `Given a file annotated with suppression for max-line-length rule, then return false`() {
            val code =
                """
                @file:Suppress("ktlint:standard:max-line-length")

                class Foo {
                    fun bar() =
                         throw RuntimeException("some very long message")
                }
                """.trimIndent()

            val actual = transformCodeToAST(code).findChildByTypeRecursively(THROW)!!

            assertThat(actual.hasNoMaxLineLengthSuppression()).isFalse()
        }
    }

    private inline fun String.transformAst(block: FileASTNode.() -> Unit): FileASTNode =
        transformCodeToAST(this)
            .apply(block)

    private fun String.transformAst(kFunction1: KFunction1<FileASTNode, Sequence<ASTNode>>) = kFunction1(transformCodeToAST(this))

    private fun transformCodeToAST(code: String) =
        KtLintRuleEngine(
            ruleProviders =
                setOf(
                    RuleV2Provider { DummyRule() },
                ),
        ).transformToAst(
            Code.fromSnippet(code),
        )

    private fun toEnumClassBodySequence(fileASTNode: FileASTNode) =
        fileASTNode
            .findChildByType(CLASS)
            ?.findChildByType(CLASS_BODY)
            ?.children
            .orEmpty()

    private fun toEnumClassSequence(fileASTNode: FileASTNode) =
        fileASTNode
            .findChildByType(CLASS)
            ?.children
            .orEmpty()

    /**
     * A dummy rule for testing. Optionally, the rule can be created with a lambda to be executed for each node visited.
     */
    private open class DummyRule(
        val block: (node: ASTNode) -> Unit = {},
    ) : RuleV2(
            ruleId = RuleId("test:dummy-rule"),
            about = About(),
        ) {
        override fun beforeVisitChildNodes(
            node: ASTNode,
            emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> AutocorrectDecision,
        ) {
            block(node)
        }
    }
}
