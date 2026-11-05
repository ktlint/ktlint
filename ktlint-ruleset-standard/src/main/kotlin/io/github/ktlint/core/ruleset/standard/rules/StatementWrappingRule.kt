package io.github.ktlint.core.ruleset.standard.rules

import com.intellij.lang.ASTNode
import io.github.ktlint.core.rule.engine.core.api.ElementType.ANNOTATION_ENTRY
import io.github.ktlint.core.rule.engine.core.api.ElementType.ARROW
import io.github.ktlint.core.rule.engine.core.api.ElementType.BLOCK
import io.github.ktlint.core.rule.engine.core.api.ElementType.CLASS
import io.github.ktlint.core.rule.engine.core.api.ElementType.CLASS_BODY
import io.github.ktlint.core.rule.engine.core.api.ElementType.ENUM_ENTRY
import io.github.ktlint.core.rule.engine.core.api.ElementType.ENUM_KEYWORD
import io.github.ktlint.core.rule.engine.core.api.ElementType.FUNCTION_LITERAL
import io.github.ktlint.core.rule.engine.core.api.ElementType.LBRACE
import io.github.ktlint.core.rule.engine.core.api.ElementType.MODIFIER_LIST
import io.github.ktlint.core.rule.engine.core.api.ElementType.RBRACE
import io.github.ktlint.core.rule.engine.core.api.ElementType.SEMICOLON
import io.github.ktlint.core.rule.engine.core.api.ElementType.VALUE_PARAMETER_LIST
import io.github.ktlint.core.rule.engine.core.api.ElementType.WHEN
import io.github.ktlint.core.rule.engine.core.api.IndentConfig
import io.github.ktlint.core.rule.engine.core.api.RuleId
import io.github.ktlint.core.rule.engine.core.api.SinceKtlint
import io.github.ktlint.core.rule.engine.core.api.SinceKtlint.Status.EXPERIMENTAL
import io.github.ktlint.core.rule.engine.core.api.SinceKtlint.Status.STABLE
import io.github.ktlint.core.rule.engine.core.api.children
import io.github.ktlint.core.rule.engine.core.api.editorconfig.EditorConfig
import io.github.ktlint.core.rule.engine.core.api.editorconfig.INDENT_SIZE_PROPERTY
import io.github.ktlint.core.rule.engine.core.api.editorconfig.INDENT_STYLE_PROPERTY
import io.github.ktlint.core.rule.engine.core.api.firstChildLeafOrSelf
import io.github.ktlint.core.rule.engine.core.api.hasModifier
import io.github.ktlint.core.rule.engine.core.api.indent
import io.github.ktlint.core.rule.engine.core.api.isWhiteSpace
import io.github.ktlint.core.rule.engine.core.api.lastChildLeafOrSelf
import io.github.ktlint.core.rule.engine.core.api.nextCodeLeaf
import io.github.ktlint.core.rule.engine.core.api.noNewLineInClosedRange
import io.github.ktlint.core.rule.engine.core.api.parent
import io.github.ktlint.core.rule.engine.core.api.prevCodeLeaf
import io.github.ktlint.core.rule.engine.core.api.upsertWhitespaceAfterMe
import io.github.ktlint.core.rule.engine.core.api.upsertWhitespaceBeforeMe
import io.github.ktlint.core.ruleset.standard.StandardRule
import org.jetbrains.kotlin.utils.addToStdlib.applyIf

@SinceKtlint("0.50", EXPERIMENTAL)
@SinceKtlint("1.0", STABLE)
public class StatementWrappingRule :
    StandardRule(
        "statement-wrapping",
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
        when (node.elementType) {
            BLOCK -> {
                if (node.parent?.elementType == FUNCTION_LITERAL) {
                    // LBRACE and RBRACE are outside of BLOCK
                    node.parent!!.visitBlock()
                } else {
                    node.visitBlock()
                }
            }

            CLASS_BODY, WHEN -> {
                node.visitBlock()
            }

            SEMICOLON -> {
                node.visitSemiColon()
            }
        }
    }

    private fun ASTNode.visitBlock() {
        takeUnless {
            // Allow
            //     val foo = {}
            it.isBlockWithoutStatements
        }?.takeUnless {
            // Allow
            //     val foo = { /* no-op */ }
            //     val foo = { a, b -> println(a + b) }
            it.isFunctionLiteralWithSingleStatementOnSingleLine
        }?.takeUnless {
            // Allow
            //     enum class FooBar { FOO, BAR }
            // or
            //     /** Some comment */
            //     enum class FooBar { FOO, BAR }
            it.parent!!.isEnumClassOnSingleLine
        }?.findChildByType(LBRACE)
            ?.applyIf(isFunctionLiteralWithParameterList) {
                // Allow:
                // val foobar =
                //     foo { bar ->
                //         doSomething()
                //     }
                this@visitBlock.findChildByType(ARROW)
            }?.let { lbraceOrArrow ->
                val nextCodeLeaf = lbraceOrArrow.nextCodeLeaf
                if (nextCodeLeaf != null && noNewLineInClosedRange(lbraceOrArrow, nextCodeLeaf)) {
                    emit(nextCodeLeaf.startOffset, "Missing newline after '${lbraceOrArrow.text}'") {
                        if (elementType == WHEN) {
                            lbraceOrArrow.upsertWhitespaceAfterMe(lbraceOrArrow.indentAsChild)
                        } else {
                            lbraceOrArrow.upsertWhitespaceAfterMe(lbraceOrArrow.indentAsSibling)
                        }
                    }
                }

                findChildByType(RBRACE)
                    ?.let { rbrace ->
                        val prevCodeLeaf = rbrace.prevCodeLeaf
                        if (prevCodeLeaf != null && noNewLineInClosedRange(prevCodeLeaf, rbrace)) {
                            emit(rbrace.startOffset, "Missing newline before '}'") {
                                rbrace.upsertWhitespaceBeforeMe(rbrace.indentAsParent)
                            }
                        }
                    }
            }
    }

    private inline val ASTNode.isBlockWithoutStatements: Boolean
        get() =
            RBRACE ==
                findChildByType(LBRACE)
                    ?.nextCodeLeaf
                    ?.elementType

    private inline val ASTNode.isFunctionLiteralWithParameterList: Boolean
        get() =
            elementType == FUNCTION_LITERAL &&
                findChildByType(VALUE_PARAMETER_LIST) != null

    private inline val ASTNode.isFunctionLiteralWithSingleStatementOnSingleLine: Boolean
        get() =
            takeIf { elementType == FUNCTION_LITERAL }
                ?.takeUnless { it.textContains('\n') }
                ?.findChildByType(BLOCK)
                ?.children
                ?.count { it.elementType != VALUE_PARAMETER_LIST && it.elementType != ARROW }
                ?.let { count -> count <= 1 }
                ?: false

    private inline val ASTNode.isEnumClassOnSingleLine: Boolean
        get() =
            if (isEnumClass) {
                val lastChildLeaf = lastChildLeafOrSelf
                // Ignore the leading comment
                noNewLineInClosedRange(firstCodeLeafOrNull!!, lastChildLeaf)
            } else {
                false
            }

    private inline val ASTNode.firstCodeLeafOrNull: ASTNode?
        get() =
            // Skip the comment on top of the node by getting modifier list
            findChildByType(MODIFIER_LIST)
                ?.children
                ?.dropWhile {
                    // Ignore annotations placed on separate lines above the node
                    it.elementType == ANNOTATION_ENTRY || it.isWhiteSpace
                }?.firstOrNull()
                ?.firstChildLeafOrSelf

    private inline val ASTNode.isEnumClass: Boolean
        get() = elementType == CLASS && hasModifier(ENUM_KEYWORD)

    private inline val ASTNode.indentAsChild: String
        get() = indent.plus(indentConfig.indent)

    private inline val ASTNode.indentAsSibling: String
        get() = parent!!.indent.plus(indentConfig.indent)

    private inline val ASTNode.indentAsParent: String
        get() = parent!!.indent

    private fun ASTNode.visitSemiColon() {
        val previousCodeLeaf = prevCodeLeaf
        val nextCodeLeaf = nextCodeLeaf
        if (previousCodeLeaf != null && nextCodeLeaf != null) {
            when {
                previousCodeLeaf.parent?.elementType == ENUM_ENTRY && nextCodeLeaf.elementType == RBRACE -> {
                    // Allow
                    // enum class INDEX2 { ONE, TWO, THREE; }
                }

                noNewLineInClosedRange(previousCodeLeaf, nextCodeLeaf) -> {
                    emit(startOffset + 1, """Missing newline after '$text'""") {
                        upsertWhitespaceAfterMe(previousCodeLeaf.indent)
                    }
                }
            }
        }
    }
}

public val STATEMENT_WRAPPING_RULE_ID: RuleId = StatementWrappingRule().ruleId
