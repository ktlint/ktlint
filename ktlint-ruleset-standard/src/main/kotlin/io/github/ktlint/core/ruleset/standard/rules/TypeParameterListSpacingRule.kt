package io.github.ktlint.core.ruleset.standard.rules

import com.intellij.lang.ASTNode
import io.github.ktlint.core.rule.engine.core.api.ElementType.CLASS
import io.github.ktlint.core.rule.engine.core.api.ElementType.CLASS_BODY
import io.github.ktlint.core.rule.engine.core.api.ElementType.CONSTRUCTOR_KEYWORD
import io.github.ktlint.core.rule.engine.core.api.ElementType.EQ
import io.github.ktlint.core.rule.engine.core.api.ElementType.FUN
import io.github.ktlint.core.rule.engine.core.api.ElementType.GT
import io.github.ktlint.core.rule.engine.core.api.ElementType.LT
import io.github.ktlint.core.rule.engine.core.api.ElementType.PRIMARY_CONSTRUCTOR
import io.github.ktlint.core.rule.engine.core.api.ElementType.PROPERTY
import io.github.ktlint.core.rule.engine.core.api.ElementType.TYPEALIAS
import io.github.ktlint.core.rule.engine.core.api.ElementType.TYPE_PARAMETER_LIST
import io.github.ktlint.core.rule.engine.core.api.IndentConfig
import io.github.ktlint.core.rule.engine.core.api.RuleId
import io.github.ktlint.core.rule.engine.core.api.SinceKtlint
import io.github.ktlint.core.rule.engine.core.api.SinceKtlint.Status.EXPERIMENTAL
import io.github.ktlint.core.rule.engine.core.api.SinceKtlint.Status.STABLE
import io.github.ktlint.core.rule.engine.core.api.editorconfig.EditorConfig
import io.github.ktlint.core.rule.engine.core.api.editorconfig.INDENT_SIZE_PROPERTY
import io.github.ktlint.core.rule.engine.core.api.editorconfig.INDENT_STYLE_PROPERTY
import io.github.ktlint.core.rule.engine.core.api.isWhiteSpace
import io.github.ktlint.core.rule.engine.core.api.isWhiteSpaceWithNewline
import io.github.ktlint.core.rule.engine.core.api.isWhiteSpaceWithoutNewline
import io.github.ktlint.core.rule.engine.core.api.nextCodeSibling
import io.github.ktlint.core.rule.engine.core.api.nextLeaf
import io.github.ktlint.core.rule.engine.core.api.nextSibling
import io.github.ktlint.core.rule.engine.core.api.parent
import io.github.ktlint.core.rule.engine.core.api.prevLeaf
import io.github.ktlint.core.rule.engine.core.api.prevSibling
import io.github.ktlint.core.rule.engine.core.api.remove
import io.github.ktlint.core.rule.engine.core.api.replaceTextWith
import io.github.ktlint.core.rule.engine.core.api.upsertWhitespaceBeforeMe
import io.github.ktlint.core.ruleset.standard.StandardRule

/**
 * Lints and formats the spacing before and after the angle brackets of a type parameter list.
 */
@SinceKtlint("0.45", EXPERIMENTAL)
@SinceKtlint("1.0", STABLE)
public class TypeParameterListSpacingRule :
    StandardRule(
        id = "type-parameter-list-spacing",
        usesEditorConfigProperties =
            setOf(
                INDENT_SIZE_PROPERTY,
                INDENT_STYLE_PROPERTY,
            ),
    ) {
    private var indentConfig = IndentConfig.DEFAULT_INDENT_CONFIG

    override fun beforeFirstNode(editorConfig: EditorConfig) {
        indentConfig =
            IndentConfig(
                indentStyle = editorConfig[INDENT_STYLE_PROPERTY],
                tabWidth = editorConfig[INDENT_SIZE_PROPERTY],
            )
    }

    override fun beforeVisitChildNodes(node: ASTNode) {
        if (node.elementType != TYPE_PARAMETER_LIST) {
            return
        }

        when (node.parent?.elementType) {
            CLASS -> node.visitClassDeclaration()
            TYPEALIAS -> node.visitTypeAliasDeclaration()
            FUN, PROPERTY -> node.visitFunctionOrPropertyDeclaration()
        }
        node.visitInsideTypeParameterList()
    }

    private fun ASTNode.visitClassDeclaration() {
        // No white space expected between class name and parameter list
        //     class Bar <T>
        prevSibling
            ?.takeIf { it.isWhiteSpace }
            ?.visitWhitespace()

        // No white space expected between parameter type list and the constructor except when followed by compound
        // constructor
        //     class Bar<T> (...)
        nextSibling
            ?.takeIf { it.isWhiteSpace && it.nextCodeSibling?.elementType == PRIMARY_CONSTRUCTOR }
            ?.let { whiteSpace ->
                if (whiteSpace.nextCodeSibling?.findChildByType(CONSTRUCTOR_KEYWORD) != null) {
                    if (whiteSpace.isWhiteSpaceWithNewline) {
                        // Newline is acceptable before (modifier list of) constructor
                        //    class Bar<T>
                        //        constructor(...)
                        //    class Bar<T>
                        //        actual constructor(...)
                        //    class Bar<T>
                        //        @SomeAnnotation constructor(...)
                        //    class Bar<T>
                        //        @SomeAnnotation1
                        //        @SomeAnnotation2
                        //        constructor(...)
                        //    class Bar<T>
                        //        /**
                        //         * Some KDoc
                        //         */
                        //        constructor(...)
                    } else {
                        // Single space before (modifier list of) constructor
                        //    class Bar<T> constructor(...)
                        //    class Bar<T> actual constructor(...)
                        //    class Bar<T> @SomeAnnotation constructor(...)
                        if (whiteSpace.text != " ") {
                            emit(whiteSpace.startOffset, "Expected a single space") {
                                // If line is to be wrapped this should have been done by other rules before running this rule
                                whiteSpace.upsertWhitespaceBeforeMe(" ")
                            }
                        }
                    }
                } else {
                    whiteSpace.visitWhitespace()
                }
            }

        // No white space expected between parameter type list and class body when constructor is missing
        //    class Bar<T> {
        nextSibling
            ?.takeIf { it.isWhiteSpace && it.nextCodeSibling?.elementType == CLASS_BODY }
            ?.singleSpaceExpected()
    }

    private fun ASTNode.visitTypeAliasDeclaration() {
        // No white space expected between typealias keyword name and parameter list
        //     typealias Bar <T>
        prevSibling
            ?.takeIf { it.isWhiteSpace }
            ?.visitWhitespace()

        // No white space expected between parameter type list and equals sign
        //    typealias Bar<T> = ...
        nextSibling
            ?.takeIf { it.isWhiteSpace && it.nextCodeSibling?.elementType == EQ }
            ?.singleSpaceExpected()
    }

    private fun ASTNode.visitFunctionOrPropertyDeclaration() {
        // Single space expected before type parameter list of function or property
        //    fun<T> foo(...)
        //    val<T> List<T>.foo: T
        prevLeaf
            ?.let { prevLeaf ->
                if (prevLeaf.isWhiteSpace) {
                    prevLeaf.singleSpaceExpected()
                } else {
                    firstChildNode.singleSpaceExpected()
                }
            }

        // Single space expected after type parameter list of function or property
        //   fun <T>foo(...)
        //   fun <T>List<T>foo(...)
        //   val <T>List<T>.foo: T
        this
            .lastChildNode
            .nextLeaf
            ?.singleSpaceExpected()
    }

    private fun ASTNode.visitInsideTypeParameterList() {
        findChildByType(LT)
            ?.nextSibling
            ?.takeIf { it.isWhiteSpace }
            ?.let {
                val expectedWhitespace =
                    if (textContains('\n')) {
                        indentConfig.childIndentOf(this)
                    } else {
                        ""
                    }
                it.visitWhitespace(expectedWhitespace)
            }

        findChildByType(GT)
            ?.prevSibling
            ?.takeIf { it.isWhiteSpace }
            ?.let {
                val expectedWhitespace =
                    if (textContains('\n')) {
                        indentConfig.childIndentOf(this)
                    } else {
                        ""
                    }
                it.visitWhitespace(expectedWhitespace)
            }
    }

    private fun ASTNode.visitWhitespace(expectedWhitespace: String = "") {
        if (text == expectedWhitespace) {
            return
        }

        when {
            expectedWhitespace.isEmpty() -> {
                emit(startOffset, "No whitespace expected") { remove() }
            }

            isWhiteSpaceWithoutNewline && expectedWhitespace.startsWith("\n") -> {
                emit(startOffset, "Expected a newline") { replaceTextWith(expectedWhitespace) }
            }

            expectedWhitespace == " " -> {
                emit(startOffset, "Expected a single space") { replaceTextWith(expectedWhitespace) }
            }
        }
    }

    private fun ASTNode.singleSpaceExpected() {
        when {
            text == " " -> {
                // Nothing to do
            }

            isWhiteSpaceWithNewline -> {
                emit(startOffset, "Expected a single space instead of newline") { replaceTextWith(" ") }
            }

            else -> {
                emit(startOffset, "Expected a single space") {
                    upsertWhitespaceBeforeMe(" ")
                }
            }
        }
    }
}

public val TYPE_PARAMETER_LIST_SPACING_RULE_ID: RuleId = TypeParameterListSpacingRule().ruleId
