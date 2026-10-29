package io.github.ktlint.core.ruleset.standard.rules

import com.intellij.lang.ASTNode
import com.intellij.psi.impl.source.tree.LeafPsiElement
import io.github.ktlint.core.logger.api.initKtLintKLogger
import io.github.ktlint.core.rule.engine.core.api.ElementType.ANNOTATED_EXPRESSION
import io.github.ktlint.core.rule.engine.core.api.ElementType.ANNOTATION
import io.github.ktlint.core.rule.engine.core.api.ElementType.ANNOTATION_ENTRY
import io.github.ktlint.core.rule.engine.core.api.ElementType.ARRAY_ACCESS_EXPRESSION
import io.github.ktlint.core.rule.engine.core.api.ElementType.ARROW
import io.github.ktlint.core.rule.engine.core.api.ElementType.BINARY_EXPRESSION
import io.github.ktlint.core.rule.engine.core.api.ElementType.BINARY_WITH_TYPE
import io.github.ktlint.core.rule.engine.core.api.ElementType.BLOCK
import io.github.ktlint.core.rule.engine.core.api.ElementType.BLOCK_COMMENT
import io.github.ktlint.core.rule.engine.core.api.ElementType.BODY
import io.github.ktlint.core.rule.engine.core.api.ElementType.CALL_EXPRESSION
import io.github.ktlint.core.rule.engine.core.api.ElementType.CATCH
import io.github.ktlint.core.rule.engine.core.api.ElementType.CLASS
import io.github.ktlint.core.rule.engine.core.api.ElementType.CLOSING_QUOTE
import io.github.ktlint.core.rule.engine.core.api.ElementType.COLON
import io.github.ktlint.core.rule.engine.core.api.ElementType.CONDITION
import io.github.ktlint.core.rule.engine.core.api.ElementType.CONSTRUCTOR_DELEGATION_CALL
import io.github.ktlint.core.rule.engine.core.api.ElementType.CONSTRUCTOR_KEYWORD
import io.github.ktlint.core.rule.engine.core.api.ElementType.CONTEXT_PARAMETER_LIST
import io.github.ktlint.core.rule.engine.core.api.ElementType.DELEGATED_SUPER_TYPE_ENTRY
import io.github.ktlint.core.rule.engine.core.api.ElementType.DESTRUCTURING_DECLARATION
import io.github.ktlint.core.rule.engine.core.api.ElementType.DOT
import io.github.ktlint.core.rule.engine.core.api.ElementType.DOT_QUALIFIED_EXPRESSION
import io.github.ktlint.core.rule.engine.core.api.ElementType.ELSE
import io.github.ktlint.core.rule.engine.core.api.ElementType.ELVIS
import io.github.ktlint.core.rule.engine.core.api.ElementType.EQ
import io.github.ktlint.core.rule.engine.core.api.ElementType.FINALLY
import io.github.ktlint.core.rule.engine.core.api.ElementType.FOR
import io.github.ktlint.core.rule.engine.core.api.ElementType.FUN
import io.github.ktlint.core.rule.engine.core.api.ElementType.FUNCTION_LITERAL
import io.github.ktlint.core.rule.engine.core.api.ElementType.IDENTIFIER
import io.github.ktlint.core.rule.engine.core.api.ElementType.IF
import io.github.ktlint.core.rule.engine.core.api.ElementType.IS_EXPRESSION
import io.github.ktlint.core.rule.engine.core.api.ElementType.KDOC
import io.github.ktlint.core.rule.engine.core.api.ElementType.KDOC_END
import io.github.ktlint.core.rule.engine.core.api.ElementType.KDOC_LEADING_ASTERISK
import io.github.ktlint.core.rule.engine.core.api.ElementType.KDOC_START
import io.github.ktlint.core.rule.engine.core.api.ElementType.LBRACE
import io.github.ktlint.core.rule.engine.core.api.ElementType.LBRACKET
import io.github.ktlint.core.rule.engine.core.api.ElementType.LITERAL_STRING_TEMPLATE_ENTRY
import io.github.ktlint.core.rule.engine.core.api.ElementType.LONG_STRING_TEMPLATE_ENTRY
import io.github.ktlint.core.rule.engine.core.api.ElementType.LPAR
import io.github.ktlint.core.rule.engine.core.api.ElementType.MODIFIER_LIST
import io.github.ktlint.core.rule.engine.core.api.ElementType.NULLABLE_TYPE
import io.github.ktlint.core.rule.engine.core.api.ElementType.OBJECT_DECLARATION
import io.github.ktlint.core.rule.engine.core.api.ElementType.OPEN_QUOTE
import io.github.ktlint.core.rule.engine.core.api.ElementType.OPERATION_REFERENCE
import io.github.ktlint.core.rule.engine.core.api.ElementType.PARENTHESIZED
import io.github.ktlint.core.rule.engine.core.api.ElementType.POSTFIX_EXPRESSION
import io.github.ktlint.core.rule.engine.core.api.ElementType.PREFIX_EXPRESSION
import io.github.ktlint.core.rule.engine.core.api.ElementType.PRIMARY_CONSTRUCTOR
import io.github.ktlint.core.rule.engine.core.api.ElementType.PROPERTY
import io.github.ktlint.core.rule.engine.core.api.ElementType.PROPERTY_ACCESSOR
import io.github.ktlint.core.rule.engine.core.api.ElementType.RBRACE
import io.github.ktlint.core.rule.engine.core.api.ElementType.RBRACKET
import io.github.ktlint.core.rule.engine.core.api.ElementType.REGULAR_STRING_PART
import io.github.ktlint.core.rule.engine.core.api.ElementType.RETURN_KEYWORD
import io.github.ktlint.core.rule.engine.core.api.ElementType.RPAR
import io.github.ktlint.core.rule.engine.core.api.ElementType.SAFE_ACCESS_EXPRESSION
import io.github.ktlint.core.rule.engine.core.api.ElementType.SECONDARY_CONSTRUCTOR
import io.github.ktlint.core.rule.engine.core.api.ElementType.STRING_TEMPLATE
import io.github.ktlint.core.rule.engine.core.api.ElementType.SUPER_TYPE_LIST
import io.github.ktlint.core.rule.engine.core.api.ElementType.THEN
import io.github.ktlint.core.rule.engine.core.api.ElementType.TRY
import io.github.ktlint.core.rule.engine.core.api.ElementType.TYPEALIAS
import io.github.ktlint.core.rule.engine.core.api.ElementType.TYPE_ARGUMENT_LIST
import io.github.ktlint.core.rule.engine.core.api.ElementType.TYPE_CONSTRAINT_LIST
import io.github.ktlint.core.rule.engine.core.api.ElementType.TYPE_PARAMETER_LIST
import io.github.ktlint.core.rule.engine.core.api.ElementType.TYPE_REFERENCE
import io.github.ktlint.core.rule.engine.core.api.ElementType.USER_TYPE
import io.github.ktlint.core.rule.engine.core.api.ElementType.VALUE_ARGUMENT
import io.github.ktlint.core.rule.engine.core.api.ElementType.VALUE_ARGUMENT_LIST
import io.github.ktlint.core.rule.engine.core.api.ElementType.VALUE_PARAMETER
import io.github.ktlint.core.rule.engine.core.api.ElementType.VALUE_PARAMETER_LIST
import io.github.ktlint.core.rule.engine.core.api.ElementType.WHEN
import io.github.ktlint.core.rule.engine.core.api.ElementType.WHEN_ENTRY
import io.github.ktlint.core.rule.engine.core.api.ElementType.WHERE_KEYWORD
import io.github.ktlint.core.rule.engine.core.api.ElementType.WHILE
import io.github.ktlint.core.rule.engine.core.api.IndentConfig
import io.github.ktlint.core.rule.engine.core.api.IndentConfig.IndentStyle.SPACE
import io.github.ktlint.core.rule.engine.core.api.IndentConfig.IndentStyle.TAB
import io.github.ktlint.core.rule.engine.core.api.RuleId
import io.github.ktlint.core.rule.engine.core.api.SinceKtlint
import io.github.ktlint.core.rule.engine.core.api.SinceKtlint.Status.STABLE
import io.github.ktlint.core.rule.engine.core.api.TokenSets
import io.github.ktlint.core.rule.engine.core.api.children
import io.github.ktlint.core.rule.engine.core.api.column
import io.github.ktlint.core.rule.engine.core.api.editorconfig.CODE_STYLE_PROPERTY
import io.github.ktlint.core.rule.engine.core.api.editorconfig.CodeStyleValue
import io.github.ktlint.core.rule.engine.core.api.editorconfig.CodeStyleValue.KTLINT_OFFICIAL
import io.github.ktlint.core.rule.engine.core.api.editorconfig.EditorConfig
import io.github.ktlint.core.rule.engine.core.api.editorconfig.EditorConfigProperty
import io.github.ktlint.core.rule.engine.core.api.editorconfig.INDENT_SIZE_PROPERTY
import io.github.ktlint.core.rule.engine.core.api.editorconfig.INDENT_STYLE_PROPERTY
import io.github.ktlint.core.rule.engine.core.api.findParentByType
import io.github.ktlint.core.rule.engine.core.api.firstChildLeafOrSelf
import io.github.ktlint.core.rule.engine.core.api.indentWithoutNewlinePrefix
import io.github.ktlint.core.rule.engine.core.api.isCode
import io.github.ktlint.core.rule.engine.core.api.isPartOf
import io.github.ktlint.core.rule.engine.core.api.isPartOfComment
import io.github.ktlint.core.rule.engine.core.api.isRoot
import io.github.ktlint.core.rule.engine.core.api.isWhiteSpace
import io.github.ktlint.core.rule.engine.core.api.isWhiteSpaceWithNewline
import io.github.ktlint.core.rule.engine.core.api.isWhiteSpaceWithoutNewline
import io.github.ktlint.core.rule.engine.core.api.lastChildLeafOrSelf
import io.github.ktlint.core.rule.engine.core.api.nextCodeLeaf
import io.github.ktlint.core.rule.engine.core.api.nextCodeSibling
import io.github.ktlint.core.rule.engine.core.api.nextLeaf
import io.github.ktlint.core.rule.engine.core.api.nextSibling
import io.github.ktlint.core.rule.engine.core.api.parent
import io.github.ktlint.core.rule.engine.core.api.prevCodeLeaf
import io.github.ktlint.core.rule.engine.core.api.prevCodeSibling
import io.github.ktlint.core.rule.engine.core.api.prevLeaf
import io.github.ktlint.core.rule.engine.core.api.prevSibling
import io.github.ktlint.core.rule.engine.core.api.remove
import io.github.ktlint.core.rule.engine.core.api.replaceTextWith
import io.github.ktlint.core.ruleset.standard.StandardRule
import io.github.oshai.kotlinlogging.KotlinLogging
import org.ec4j.core.model.PropertyType
import org.ec4j.core.model.PropertyType.PropertyValueParser
import org.jetbrains.kotlin.psi.psiUtil.leaves
import org.jetbrains.kotlin.psi.psiUtil.parents
import java.util.Deque
import java.util.LinkedList

private val LOGGER = KotlinLogging.logger {}.initKtLintKLogger()

@SinceKtlint("0.1", STABLE)
public class IndentationRule :
    StandardRule(
        id = "indent",
        usesEditorConfigProperties =
            setOf(
                CODE_STYLE_PROPERTY,
                INDENT_SIZE_PROPERTY,
                INDENT_STYLE_PROPERTY,
                INDENT_WHEN_ARROW_ON_NEW_LINE,
                INDENT_EXPLICIT_CONSTRUCTOR_PROPERTY,
            ),
    ) {
    private var codeStyle = CODE_STYLE_PROPERTY.defaultValue
    private var indentConfig = IndentConfig.DEFAULT_INDENT_CONFIG
    private var indentWhenArrowOnNewLine = INDENT_WHEN_ARROW_ON_NEW_LINE.defaultValue
    private var identExplicitConstructor = INDENT_EXPLICIT_CONSTRUCTOR_PROPERTY.defaultValue

    private var line = 1

    private val indentContextStack: Deque<IndentContext> = LinkedList()

    private lateinit var stringTemplateIndenter: StringTemplateIndenter

    override fun beforeFirstNode(editorConfig: EditorConfig) {
        codeStyle = editorConfig[CODE_STYLE_PROPERTY]
        indentConfig =
            IndentConfig(
                indentStyle = editorConfig[INDENT_STYLE_PROPERTY],
                tabWidth = editorConfig[INDENT_SIZE_PROPERTY],
            )
        if (indentConfig.disabled) {
            stopTraversalOfAST()
        }
        indentWhenArrowOnNewLine = editorConfig[INDENT_WHEN_ARROW_ON_NEW_LINE]
        identExplicitConstructor = editorConfig[INDENT_EXPLICIT_CONSTRUCTOR_PROPERTY]
    }

    override fun beforeVisitChildNodes(node: ASTNode) {
        if (node.isRoot) {
            // File should not start with a whitespace
            node
                .nextLeaf
                ?.takeIf { it.isWhiteSpaceWithoutNewline }
                ?.let { whitespaceWithoutNewline ->
                    emit(node.startOffset, "Unexpected indentation") { whitespaceWithoutNewline.remove() }
                }
            indentContextStack.addLast(
                IndentContext(
                    fromASTNode = node,
                    nodeIndent = "",
                    firstChildIndent = "",
                    childIndent = "",
                    lastChildIndent = "",
                    activated = true,
                ),
            )
        }

        node.visitNode()
    }

    @Suppress("LongMethod", "CyclomaticComplexMethod")
    private fun ASTNode.visitNode() {
        when {
            isWhiteSpaceWithNewline -> {
                visitNewLineIndentation()
            }

            elementType == CONTEXT_PARAMETER_LIST ||
                elementType == LONG_STRING_TEMPLATE_ENTRY ||
                elementType == STRING_TEMPLATE ||
                elementType == VALUE_ARGUMENT_LIST -> {
                startIndentContext(fromAstNode = this, lastChildIndent = "")
            }

            (elementType == SUPER_TYPE_LIST && !isPrecededByComment()) ||
                (isPartOfComment && nextCodeSibling?.elementType == SUPER_TYPE_LIST) -> {
                visitSuperTypeList()
            }

            elementType == VALUE_ARGUMENT -> {
                visitValueArgument()
            }

            elementType == SECONDARY_CONSTRUCTOR -> {
                visitSecondaryConstructor()
            }

            elementType == PARENTHESIZED -> {
                visitParenthesized()
            }

            elementType == TYPE_ARGUMENT_LIST || elementType == TYPE_PARAMETER_LIST -> {
                visitTypeList()
            }

            elementType == BINARY_WITH_TYPE || elementType == USER_TYPE -> {
                startIndentContext(this)
            }

            elementType == IS_EXPRESSION ||
                elementType == PREFIX_EXPRESSION ||
                elementType == POSTFIX_EXPRESSION -> {
                startIndentContext(this)
            }

            elementType == DELEGATED_SUPER_TYPE_ENTRY ||
                elementType == ANNOTATED_EXPRESSION ||
                elementType == TYPE_REFERENCE -> {
                startIndentContext(fromAstNode = this, childIndent = "")
            }

            elementType == IF -> {
                visitIf()
            }

            elementType == LBRACE -> {
                visitLbrace()
            }

            elementType == VALUE_PARAMETER_LIST && parent?.elementType != FUNCTION_LITERAL -> {
                startIndentContext(fromAstNode = this, lastChildIndent = "")
            }

            elementType == LPAR &&
                nextCodeSibling?.elementType == CONDITION -> {
                visitLparBeforeCondition()
            }

            elementType == VALUE_PARAMETER -> {
                visitValueParameter()
            }

            elementType == FUN -> {
                visitFun()
            }

            elementType == CLASS -> {
                visitClass()
            }

            elementType == OBJECT_DECLARATION -> {
                visitObjectDeclaration()
            }

            elementType == BINARY_EXPRESSION -> {
                visitBinaryExpression()
            }

            elementType in CHAINABLE_EXPRESSION -> {
                visitChainableExpression()
            }

            elementType == IDENTIFIER && parent?.elementType == PROPERTY -> {
                visitIdentifierInProperty()
            }

            elementType == LITERAL_STRING_TEMPLATE_ENTRY && nextCodeSibling?.elementType == CLOSING_QUOTE -> {
                visitWhiteSpaceBeforeClosingQuote()
            }

            elementType == WHEN -> {
                visitWhen()
            }

            elementType == WHEN_ENTRY -> {
                visitWhenEntry()
            }

            elementType == WHERE_KEYWORD && nextCodeSibling?.elementType == TYPE_CONSTRAINT_LIST -> {
                visitWhereKeywordBeforeTypeConstraintList()
            }

            elementType == KDOC -> {
                visitKdoc()
            }

            elementType == PROPERTY_ACCESSOR || elementType == TYPEALIAS -> {
                visitPropertyAccessor()
            }

            elementType == FOR || elementType == WHILE -> {
                visitConditionalLoop()
            }

            elementType == LBRACKET -> {
                visitLBracket()
            }

            elementType == NULLABLE_TYPE -> {
                visitNullableType()
            }

            elementType == DESTRUCTURING_DECLARATION -> {
                visitDestructuringDeclaration()
            }

            elementType == TRY -> {
                visitTryCatchFinally()
            }

            else -> {
                LOGGER.trace { "No processing for $elementType: ${textWithEscapedTabAndNewline()}" }
            }
        }
    }

    private fun ASTNode.visitSuperTypeList() {
        if (codeStyle == KTLINT_OFFICIAL) {
            val superTypeList =
                if (isPartOfComment) {
                    nextCodeLeaf!!
                } else {
                    this
                }
            if (superTypeList.isPartOfClassWithAMultilinePrimaryConstructor()) {
                // Contrary to the default IntelliJ IDEA formatter, indent the super type call entry so that it looks better in case it
                // is followed by another super type:
                //      class Foo(
                //          val bar1: Bar,
                //          val bar2: Bar,
                //      ) : FooBar(
                //              bar1,
                //              bar2
                //          ),
                //          BarFoo,
                startIndentContext(
                    fromAstNode = this,
                    activated = true,
                )
            }
        }
    }

    private fun ASTNode.visitParenthesized() {
        if (codeStyle == KTLINT_OFFICIAL) {
            // Contrary to the IntelliJ IDEA default formatter, do not indent the closing RPAR
            startIndentContext(fromAstNode = this, lastChildIndent = "")
        } else if (parent?.parent?.elementType != IF) {
            startIndentContext(this)
        }
    }

    private fun ASTNode.visitTypeList() {
        if (codeStyle == KTLINT_OFFICIAL) {
            // Contrary to the IntelliJ IDEA default formatter, do not indent the closing angle bracket
            startIndentContext(fromAstNode = this, lastChildIndent = "")
        } else {
            startIndentContext(this)
        }
    }

    private fun ASTNode.visitChainableExpression() {
        if (codeStyle == KTLINT_OFFICIAL && isDotQualifiedExpressionInArrayAccessCall()) {
            // Issue 1540: Deviate and fix from incorrect formatting in IntelliJ IDEA formatting and produce following:
            // val fooBar2 = foo
            //    .bar[0] {
            //        "foobar"
            //    }
            startIndentContext(
                fromAstNode = parent!!,
                toAstNode = parent!!.parent!!.lastChildLeafOrSelf,
            )
        } else if (prevCodeSibling.isElvisOperator()) {
            startIndentContext(this)
        } else if (parent?.elementType in CHAINABLE_EXPRESSION) {
            // Multiple dot qualified expressions and/or safe expression on the same line should not increase the indent level
        } else {
            startIndentContext(this)
        }
    }

    private fun ASTNode.isPartOfClassWithAMultilinePrimaryConstructor() =
        parent
            ?.takeIf { it.elementType == CLASS }
            ?.findChildByType(PRIMARY_CONSTRUCTOR)
            ?.textContains('\n') == true

    private fun ASTNode.visitValueArgument() {
        if (codeStyle == KTLINT_OFFICIAL || codeStyle == CodeStyleValue.ANDROID_STUDIO) {
            // Deviate from standard IntelliJ IDEA formatting to allow formatting below:
            //     val foo = foo(
            //         parameterName =
            //             "The quick brown fox "
            //                .plus("jumps ")
            //                .plus("over the lazy dog"),
            //     )
            startIndentContext(
                fromAstNode = this,
                lastChildIndent = "",
            )
        }
    }

    private fun ASTNode.visitSecondaryConstructor() {
        findChildByType(CONSTRUCTOR_DELEGATION_CALL)
            ?.let { constructorDelegationCall ->
                val fromAstNode = skipLeadingWhitespaceCommentsAndAnnotations()
                val nextToAstNode =
                    startIndentContext(
                        fromAstNode = constructorDelegationCall,
                    ).prevCodeLeaf()

                // Leading annotations and comments should be indented at same level as constructor itself
                if (fromAstNode != nextLeaf) {
                    startIndentContext(
                        fromAstNode = this,
                        toAstNode = nextToAstNode,
                        childIndent = "",
                    )
                }
            }
    }

    private fun ASTNode.visitIf() {
        var nextToAstNode = lastChildLeafOrSelf
        findChildByType(ELSE)
            ?.let { fromAstNode ->
                nextToAstNode =
                    startIndentContext(
                        fromAstNode = fromAstNode,
                        toAstNode = nextToAstNode,
                    ).prevCodeLeaf()
            }

        findChildByType(THEN)
            ?.lastChildLeafOrSelf
            ?.nextLeaf
            ?.let { nodeAfterThenBlock ->
                nextToAstNode =
                    startIndentContext(
                        fromAstNode = nodeAfterThenBlock,
                        toAstNode = nextToAstNode,
                        childIndent = "",
                    ).prevCodeLeaf()
            }
        findChildByType(RPAR)
            ?.nextCodeLeaf
            ?.let { nodeAfterConditionBlock ->
                nextToAstNode =
                    startIndentContext(
                        fromAstNode = nodeAfterConditionBlock,
                        toAstNode = nextToAstNode,
                    ).prevCodeLeaf()
            }
        // No indent for the RPAR
        startIndentContext(
            fromAstNode = this,
            toAstNode = nextToAstNode,
            lastChildIndent = "",
        )
    }

    private fun ASTNode.visitLbrace() {
        // Outer indent context
        val rbrace =
            requireNotNull(
                nextSibling { it.elementType == RBRACE },
            ) { "Can not find matching rbrace" }
        startIndentContext(
            fromAstNode = this,
            toAstNode = rbrace,
            firstChildIndent = "",
            lastChildIndent = "",
        )

        // Inner indent context in reversed order
        parent
            ?.takeIf { it.elementType == FUNCTION_LITERAL }
            ?.findChildByType(ARROW)
            ?.let { arrow ->
                startIndentContext(
                    fromAstNode = arrow,
                    toAstNode = rbrace,
                    lastChildIndent = "",
                )
                startIndentContext(
                    fromAstNode = this,
                    toAstNode = arrow.prevCodeLeaf!!,
                    childIndent = arrow.calculateIndentOfFunctionLiteralParameters(),
                )
            }
    }

    private fun ASTNode.calculateIndentOfFunctionLiteralParameters() =
        if (isFirstParameterOfFunctionLiteralPrecededByNewLine()) {
            if (codeStyle == KTLINT_OFFICIAL) {
                // Indent with single indent as defined in Kotlin Coding conventions
                indentConfig.indent
            } else {
                // Comply with default IDEA formatting, although it is not compliant with Kotlin Coding conventions
                // val fieldExample =
                //      LongNameClass {
                //              paramA,
                //              paramB,
                //              paramC ->
                //          ClassB(paramA, paramB, paramC)
                //      }
                indentConfig.indent.repeat(2)
            }
        } else {
            // Allow default IntelliJ IDEA formatting:
            // val fieldExample =
            //     LongNameClass { paramA,
            //                     paramB,
            //                     paramC ->
            //         ClassB(paramA, paramB, paramC)
            //     }
            // val fieldExample =
            //     someFunction(
            //         1,
            //         2,
            //     ) { paramA,
            //         paramB,
            //         paramC ->
            //         ClassB(paramA, paramB, paramC)
            //     }
            this
                .takeIf { it.isPartOf(CALL_EXPRESSION) }
                ?.parent
                ?.leaves(false)
                ?.takeWhile { !it.isWhiteSpaceWithNewline }
                ?.sumOf { it.textLength }
                ?.plus(2) // need to add spaces to compensate for "{ "
                ?.let { length -> " ".repeat(length) }
                ?: indentConfig.indent.repeat(2)
        }

    private fun ASTNode.isFirstParameterOfFunctionLiteralPrecededByNewLine() =
        findParentByType(FUNCTION_LITERAL)
            ?.findChildByType(VALUE_PARAMETER_LIST)
            ?.prevSibling { it.textContains('\n') } != null

    private fun ASTNode.visitLparBeforeCondition() {
        startIndentContext(
            // Allow to pickup whitespace before condition
            fromAstNode = requireNotNull(nextLeaf),
            // Ignore whitespace after condition but before rpar
            toAstNode = requireNotNull(nextCodeSibling).lastChildLeafOrSelf,
            nodeIndent = currentIndent() + indentConfig.indent,
            childIndent = "",
        )
    }

    private fun ASTNode.visitValueParameter() {
        // Inner indent contexts in reversed order
        var nextToAstNode: ASTNode = lastChildLeafOrSelf
        findChildByType(EQ)
            ?.let { fromAstNode ->
                nextToAstNode =
                    startIndentContext(
                        fromAstNode = fromAstNode,
                        toAstNode = nextToAstNode,
                    ).prevCodeLeaf()
            }

        if (codeStyle == KTLINT_OFFICIAL) {
            // Deviate from standard IntelliJ IDEA formatting to allow formatting below:
            //     fun process(
            //         aVariableWithAVeryLongName:
            //             TypeWithAVeryLongNameThatDoesNotFitOnSameLineAsTheVariableName
            //     ): List<Output>
            findChildByType(COLON)
                ?.let { fromAstNode ->
                    nextToAstNode =
                        startIndentContext(
                            fromAstNode = fromAstNode,
                            toAstNode = nextToAstNode,
                        ).prevCodeLeaf()
                }
        }

        // Leading annotations and comments should be indented at same level as constructor itself
        val fromAstNode = skipLeadingWhitespaceCommentsAndAnnotations()
        if (fromAstNode != firstChildNode &&
            prevSibling { it.isWhiteSpaceWithNewline } == null &&
            this == parent?.findChildByType(VALUE_PARAMETER)
        ) {
            startIndentContext(
                fromAstNode = fromAstNode,
                toAstNode = nextToAstNode,
            ).fromASTNode.prevLeaf { !it.isWhiteSpace }!!
        } else {
            startIndentContext(
                fromAstNode = this,
                toAstNode = nextToAstNode,
                childIndent = "",
            )
        }
    }

    private fun ASTNode.visitFun() {
        // Inner indent contexts in reversed order
        var nextToAstNode: ASTNode = lastChildLeafOrSelf
        val eqOrBlock = findChildByType(EQ) ?: findChildByType(BLOCK)
        eqOrBlock?.let {
            nextToAstNode =
                startIndentContext(
                    fromAstNode = eqOrBlock,
                    toAstNode = nextToAstNode,
                ).prevCodeLeaf()
        }

        findChildByType(WHERE_KEYWORD)
            ?.let { where -> nextToAstNode = where.visitWhereKeyword() }

        findChildByType(TYPE_REFERENCE)
            ?.let { typeReference ->
                nextToAstNode =
                    startIndentContext(
                        fromAstNode = typeReference.getPrecedingLeadingCommentsAndWhitespaces(),
                        toAstNode = nextToAstNode,
                    ).prevCodeLeaf()
            }

        // Leading annotations and comments should be indented at same level as function itself
        startIndentContext(
            fromAstNode = this,
            toAstNode = nextToAstNode,
            childIndent = "",
        )
    }

    private fun ASTNode.visitWhereKeyword(): ASTNode {
        require(elementType == WHERE_KEYWORD) {}
        val typeConstraintList =
            requireNotNull(nextCodeSibling) { "Can not find code sibling after WHERE in '${parent?.elementType}'" }
        require(typeConstraintList.elementType == TYPE_CONSTRAINT_LIST) {
            "Code sibling after WHERE in '${parent?.elementType}' is not a TYPE_CONSTRAINT_LIST"
        }
        return startIndentContext(
            fromAstNode = getPrecedingLeadingCommentsAndWhitespaces(),
            toAstNode = typeConstraintList.lastChildLeafOrSelf,
            childIndent =
                if (prevLeaf?.isWhiteSpaceWithNewline == true) {
                    indentConfig.indent
                } else {
                    " ".repeat(
                        maxOf(0, column - 1 - parent!!.indentWithoutNewlinePrefix.length),
                    )
                },
        ).prevCodeLeaf()
    }

    private fun ASTNode.visitClass() {
        // Inner indent contexts in reversed order
        var nextToAstNode: ASTNode = lastChildLeafOrSelf
        findChildByType(WHERE_KEYWORD)
            ?.let { where -> nextToAstNode = where.visitWhereKeyword() }

        val primaryConstructor = findChildByType(PRIMARY_CONSTRUCTOR)
        val containsConstructorKeyword = primaryConstructor?.findChildByType(CONSTRUCTOR_KEYWORD) != null
        if (identExplicitConstructor && primaryConstructor != null && containsConstructorKeyword) {
            nextToAstNode =
                startIndentContext(
                    fromAstNode = primaryConstructor.getPrecedingLeadingCommentsAndWhitespaces(),
                    toAstNode = nextToAstNode,
                ).prevCodeLeaf()
        } else {
            findChildByType(SUPER_TYPE_LIST)
                ?.let { superTypeList ->
                    nextToAstNode =
                        startIndentContext(
                            fromAstNode = superTypeList.getPrecedingLeadingCommentsAndWhitespaces(),
                            toAstNode = superTypeList.lastChildLeafOrSelf,
                        ).prevCodeLeaf()
                }
        }

        // Leading annotations and comments should be indented at same level as class itself
        startIndentContext(
            fromAstNode = this,
            toAstNode = nextToAstNode,
            childIndent = "",
        )
    }

    private fun ASTNode.visitObjectDeclaration() {
        // Inner indent contexts in reversed order
        var nextToAstNode: ASTNode = lastChildLeafOrSelf
        findChildByType(SUPER_TYPE_LIST)
            ?.let { superTypeList ->
                nextToAstNode =
                    startIndentContext(
                        fromAstNode = superTypeList.getPrecedingLeadingCommentsAndWhitespaces(),
                        toAstNode = superTypeList.lastChildLeafOrSelf,
                    ).prevCodeLeaf()
            }

        // Leading annotations and comments should be indented at same level as class itself
        startIndentContext(
            fromAstNode = this,
            toAstNode = nextToAstNode,
            childIndent = "",
        )
    }

    private fun ASTNode.visitBinaryExpression() {
        if (isPartOfBinaryExpressionWrappedInCondition()) {
            // Complex binary expression are nested in such a way that the indent context of the condition
            // wrapper is not the last node on the stack
            val conditionIndentContext =
                indentContextStack.last { it.fromASTNode.elementType != BINARY_EXPRESSION }
            // Create new indent context for the remainder (operator and right-hand side) of the binary expression
            startIndentContext(
                fromAstNode = findChildByType(OPERATION_REFERENCE)!!,
                toAstNode = lastChildLeafOrSelf,
                nodeIndent = conditionIndentContext.nodeIndent,
                childIndent = conditionIndentContext.childIndent,
            )
            startIndentContext(firstChildNode)
        } else if (parent?.elementType != BINARY_EXPRESSION ||
            findChildByType(OPERATION_REFERENCE)?.firstChildNode?.elementType == ELVIS
        ) {
            startIndentContext(this)
        }
    }

    private fun ASTNode.visitIdentifierInProperty() {
        startIndentContext(
            fromAstNode = this,
            toAstNode = parent!!.lastChildLeafOrSelf,
        )
    }

    private fun ASTNode.visitWhen() {
        var nextToASTNode = lastChildLeafOrSelf
        findChildByType(LPAR)
            ?.let { lpar ->
                nextToASTNode =
                    startIndentContext(
                        fromAstNode = lpar,
                        toAstNode = findChildByType(RPAR)!!,
                        lastChildIndent = "",
                    ).prevCodeLeaf()
            }
        startIndentContext(
            fromAstNode = this,
            toAstNode = nextToASTNode,
        )
    }

    private fun ASTNode.visitWhenEntry() {
        findChildByType(ARROW)
            ?.let { arrow ->
                arrow
                    .prevSibling { !it.isPartOfComment }
                    .let { prevSibling ->
                        if (indentWhenArrowOnNewLine && prevSibling != null && prevSibling.isWhiteSpaceWithNewline) {
                            if (arrow.nextCodeSibling?.elementType == BLOCK && codeStyle != KTLINT_OFFICIAL) {
                                // Uglify the indentation to below to keep compatible with default formatting IntelliJ IDEA
                                //     val foo =
                                //        when (bar()) {
                                //            is Bar1
                                //                -> {
                                //                "bar1"
                                //            }
                                //        }
                                startIndentContext(
                                    fromAstNode = prevSibling,
                                    toAstNode = lastChildLeafOrSelf,
                                    firstChildIndent = indentConfig.indent,
                                    childIndent = "",
                                )
                            } else {
                                // Reformat to below. This is not compatible with default IDEA formatting. But the closing brace is now at
                                // least aligned consistently.
                                //     val foo =
                                //        when (bar()) {
                                //            is Bar1
                                //                -> {
                                //                    "bar1"
                                //                }
                                //        }
                                startIndentContext(
                                    fromAstNode = arrow.nextLeaf!!,
                                    toAstNode = lastChildLeafOrSelf,
                                )
                                startIndentContext(
                                    fromAstNode = this,
                                    toAstNode = prevSibling.prevLeaf!!,
                                    childIndent = "",
                                )
                            }
                        } else {
                            startIndentContext(
                                fromAstNode = arrow.nextLeaf!!,
                                toAstNode = lastChildLeafOrSelf,
                            )
                            startIndentContext(
                                fromAstNode = this,
                                toAstNode = arrow,
                                childIndent = "",
                            )
                        }
                    }
            }
    }

    private fun ASTNode.visitWhereKeywordBeforeTypeConstraintList() {
        prevLeaf
            .takeUnless { it.isWhiteSpaceWithNewline }
            ?.takeIf { !indentContextStack.peekLast().activated }
            ?.let {
                val lastIndentContext = indentContextStack.removeLast()
                indentContextStack.addLast(
                    lastIndentContext.copy(activated = true),
                )
            }

        startIndentContext(
            fromAstNode = this,
            toAstNode = nextCodeSibling?.lastChildLeafOrSelf!!,
            childIndent = TYPE_CONSTRAINT_CONTINUATION_INDENT,
        )
    }

    private fun ASTNode.visitKdoc() {
        findChildByType(KDOC_START)
            ?.nextLeaf
            ?.let { fromAstNode ->
                startIndentContext(
                    fromAstNode = fromAstNode,
                    toAstNode = lastChildLeafOrSelf,
                    childIndent = KDOC_CONTINUATION_INDENT,
                )
            }
    }

    private fun ASTNode.visitPropertyAccessor() {
        var nextToASTNode = lastChildLeafOrSelf
        findChildByType(EQ)
            ?.let { fromAstNode ->
                nextToASTNode =
                    startIndentContext(
                        fromAstNode = fromAstNode,
                        toAstNode = lastChildLeafOrSelf,
                    ).prevCodeLeaf()
            }
        // No indent on preceding annotations and comments
        startIndentContext(
            fromAstNode = this,
            toAstNode = nextToASTNode,
            childIndent = "",
        )
    }

    private fun ASTNode.visitConditionalLoop() {
        // Inner indent contexts in reversed order
        findChildByType(BODY)
            ?.takeIf { it.nextCodeLeaf?.elementType != LBRACE }
            ?.let { rpar ->
                startIndentContext(
                    fromAstNode = rpar,
                    toAstNode = lastChildLeafOrSelf,
                )
            }
        findChildByType(CONDITION)
            ?.let { fromAstNode ->
                startIndentContext(fromAstNode)
            }
    }

    private fun ASTNode.visitLBracket() {
        parent
            ?.takeUnless {
                // Should be resolved in IntelliJ IDEA default formatter:
                // https://youtrack.jetbrains.com/issue/KTIJ-14859/Too-little-indentation-inside-the-brackets-in-multiple-annotations-with-the-same-target
                it.elementType == ANNOTATION
            }?.findChildByType(RBRACKET)
            ?.let { rbracket ->
                startIndentContext(
                    fromAstNode = this,
                    toAstNode = rbracket,
                    firstChildIndent = "",
                    lastChildIndent = "",
                )
            }
    }

    private fun ASTNode.visitNullableType() {
        // Inner indent contexts in reversed order
        var nextToAstNode = lastChildLeafOrSelf
        findChildByType(RPAR)
            ?.let { fromAstNode ->
                nextToAstNode =
                    startIndentContext(
                        fromAstNode = fromAstNode,
                        toAstNode = nextToAstNode,
                        childIndent = "",
                    ).prevCodeLeaf()
            }
        startIndentContext(
            fromAstNode = this,
            toAstNode = nextToAstNode,
        )
    }

    private fun ASTNode.visitDestructuringDeclaration() {
        // Inner indent contexts in reversed order
        var nextToAstNode = lastChildLeafOrSelf
        findChildByType(EQ)
            ?.let { eq ->
                nextToAstNode =
                    startIndentContext(
                        fromAstNode = eq,
                        toAstNode = nextToAstNode,
                    ).prevCodeLeaf()
            }

        findChildByType(RPAR)
            ?.let {
                startIndentContext(
                    fromAstNode = this,
                    toAstNode = nextToAstNode,
                    lastChildIndent = "",
                )
            }
    }

    private fun ASTNode.visitTryCatchFinally() {
        // Inner indent contexts in reversed order
        var nextToAstNode = lastChildLeafOrSelf
        findChildByType(FINALLY)
            ?.let { finally ->
                nextToAstNode =
                    startIndentContext(
                        fromAstNode = finally,
                        toAstNode = nextToAstNode,
                        childIndent = "",
                    ).prevCodeLeaf()
            }

        findChildByType(CATCH)
            ?.let { catch ->
                nextToAstNode =
                    startIndentContext(
                        fromAstNode = catch,
                        toAstNode = nextToAstNode,
                        childIndent = "",
                    ).prevCodeLeaf()
            }

        startIndentContext(
            fromAstNode = this,
            toAstNode = nextToAstNode,
            lastChildIndent = "",
        )
    }

    private fun ASTNode.skipLeadingWhitespaceCommentsAndAnnotations(): ASTNode {
        require(elementType == SECONDARY_CONSTRUCTOR || elementType == VALUE_PARAMETER)
        return findChildByType(MODIFIER_LIST)
            ?.let { modifierList ->
                modifierList
                    .children
                    .firstOrNull { it.isCode && it.elementType != ANNOTATION_ENTRY }
                    ?: modifierList.nextCodeSibling
            }
            ?: children.firstOrNull { it.isCode }
            ?: this
    }

    private fun ASTNode.getPrecedingLeadingCommentsAndWhitespaces(): ASTNode {
        var fromAstNode: ASTNode? = this
        while (fromAstNode?.prevLeaf != null &&
            (fromAstNode.prevLeaf.isWhiteSpace || fromAstNode.prevLeaf?.isPartOfComment == true)
        ) {
            fromAstNode = fromAstNode.prevLeaf
        }
        return fromAstNode!!
    }

    private fun ASTNode.isPartOfBinaryExpressionWrappedInCondition() =
        parents()
            .takeWhile { it.elementType == BINARY_EXPRESSION || it.elementType == CONDITION }
            .lastOrNull()
            ?.elementType == CONDITION

    private fun currentIndent() =
        indentContextStack
            .peekLast()
            .indent()

    private fun startIndentContext(
        fromAstNode: ASTNode,
        toAstNode: ASTNode = fromAstNode.lastChildLeafOrSelf,
        nodeIndent: String = currentIndent(),
        childIndent: String = indentConfig.indent,
        firstChildIndent: String = childIndent,
        lastChildIndent: String = childIndent,
        activated: Boolean = false,
    ): IndentContext =
        IndentContext(
            fromASTNode = fromAstNode,
            toASTNode = toAstNode,
            nodeIndent = nodeIndent,
            firstChildIndent = firstChildIndent,
            childIndent = childIndent,
            lastChildIndent = lastChildIndent,
            activated = activated,
        ).also { newIndentContext ->
            LOGGER.trace {
                val nodeIndentLevel = indentConfig.indentLevelFrom(newIndentContext.nodeIndent)
                val childIndentLevel = indentConfig.indentLevelFrom(newIndentContext.childIndent)
                "Create new indent context (same as parent) with level ($nodeIndentLevel, $childIndentLevel) for " +
                    "${fromAstNode.elementType}: ${newIndentContext.nodes}"
            }
            indentContextStack.addLast(newIndentContext)
        }

    override fun afterVisitChildNodes(node: ASTNode) {
        while (
            // End visit of the node matches with the toAstNode on top of the stack
            indentContextStack.peekLast()?.toASTNode == node ||
            // When the ktlint indentation violations on child elements have been suppressed, those child elements are not visited, and the
            // stack is therefore not cleaned up properly. So remove all elements from top of the stack until the start of an index context
            // is found for the current node.
            indentContextStack.peekLast()?.toASTNode?.parent { it == node } != null
        ) {
            LOGGER.trace {
                val indentContext = indentContextStack.peekLast()
                val nodeIndentLevel = indentConfig.indentLevelFrom(indentContext.nodeIndent)
                val childIndentLevel = indentConfig.indentLevelFrom(indentContext.childIndent)
                "Remove indent context with level ($nodeIndentLevel, $childIndentLevel) for ${indentContext.fromASTNode.elementType}: " +
                    indentContext.nodes
            }
            indentContextStack
                .removeLast()
                .also {
                    LOGGER.trace {
                        val indentContext = indentContextStack.peekLast()
                        val nodeIndentLevel = indentConfig.indentLevelFrom(indentContext.nodeIndent)
                        val childIndentLevel = indentConfig.indentLevelFrom(indentContext.childIndent)
                        "Last indent context with level ($nodeIndentLevel, $childIndentLevel) for " +
                            "${indentContext.fromASTNode.elementType}: ${indentContext.nodes}"
                    }
                }
        }
    }

    override fun afterLastNode() {
        require(indentContextStack.isEmpty()) {
            indentContextStack
                .joinToString(prefix = "Stack should be empty:\n\t", separator = "\n\t") { it.toString() }
        }
    }

    private fun ASTNode.visitNewLineIndentation() {
        line++
        if (indentContextStack.peekLast()?.activated == false) {
            val lastIndentContext = indentContextStack.removeLast()
            indentContextStack.addLast(
                lastIndentContext.copy(activated = true),
            )
        }

        if (ignoreIndent()) {
            return
        }

        val normalizedNodeIndent = normalizedIndent()
        val expectedIndentation = expectedIndent()
        val text = text
        val nodeIndent = text.substringAfterLast("\n")
        if (nodeIndent != normalizedNodeIndent || normalizedNodeIndent != expectedIndentation) {
            // Indentation was at correct level but contained invalid indent characters. The violation has already been emitted.
            var autoCorrect = normalizedNodeIndent == expectedIndentation
            if (normalizedNodeIndent != expectedIndentation) {
                emit(
                    startOffset + text.length - nodeIndent.length,
                    "Unexpected indentation (${normalizedNodeIndent.length}) (should be ${expectedIndentation.length})",
                ) {
                    autoCorrect = true
                }
            }
            if (autoCorrect) {
                LOGGER.trace {
                    "Line $line: changed indentation to ${expectedIndentation.length} " +
                        "(from ${normalizedNodeIndent.length}) for $elementType: ${textWithEscapedTabAndNewline()}"
                }
                replaceTextWith(
                    text.substringBeforeLast("\n") + "\n" + expectedIndentation,
                )
            }
        } else {
            processedButNoIndentationChangedNeeded()
        }
    }

    private fun ASTNode.ignoreIndent(): Boolean =
        if (isRawStringLiteralStartingAtPosition0() || isCommentStartingAtPosition0()) {
            processedButNoIndentationChangedNeeded()
            true
        } else {
            false
        }

    private fun ASTNode.isCommentStartingAtPosition0(): Boolean =
        nextLeaf
            ?.takeIf { it.isPartOf(TokenSets.COMMENTS) }
            ?.let { comment ->
                when {
                    text.endsWith("\n") -> {
                        // comments are allowed at column 0
                        true
                    }

                    comment.textContains('\n') && comment.elementType == BLOCK_COMMENT -> {
                        // FIXME: while we cannot assume any kind of layout inside a block comment,
                        // `/*` and `*/` can still be indented
                        true
                    }

                    else -> {
                        false
                    }
                }
            } ?: false

    private fun ASTNode.isRawStringLiteralStartingAtPosition0(): Boolean =
        // raw strings (""") are allowed at column 0
        text.endsWith("\n") && nextLeaf.isStartOfRawStringLiteral()

    private fun ASTNode?.isStartOfRawStringLiteral(): Boolean = this != null && elementType == OPEN_QUOTE && text == "\"\"\""

    private fun ASTNode.processedButNoIndentationChangedNeeded() =
        LOGGER.trace { "No indentation change required for $elementType: ${textWithEscapedTabAndNewline()}" }

    private fun ASTNode.expectedIndent(): String {
        val lastIndexContext = indentContextStack.peekLast()
        val nextLeaf = nextLeaf
        val adjustedChildIndent =
            when {
                this == lastIndexContext.fromASTNode.firstChildLeafOrSelf ||
                    nextLeaf == lastIndexContext.fromASTNode.firstChildLeafOrSelf -> {
                    lastIndexContext.firstChildIndent
                }

                this == lastIndexContext.toASTNode ||
                    nextLeaf == lastIndexContext.toASTNode -> {
                    lastIndexContext.lastChildIndent
                }

                else -> {
                    lastIndexContext.childIndent
                }
            }
        return lastIndexContext.nodeIndent + adjustedChildIndent
    }

    private fun ASTNode.normalizedIndent(): String {
        var nodeIndent = text.substringAfterLast("\n")
        when (indentConfig.indentStyle) {
            SPACE -> {
                if ('\t' in nodeIndent) {
                    emit(startOffset + text.length - nodeIndent.length, "Unexpected tab character(s)") {
                        nodeIndent = indentConfig.toNormalizedIndent(nodeIndent)
                    }
                }
            }

            TAB -> {
                val expectedTrailingSpaces = expectedTrailingSpaces()
                val expectedIndent = text.trimEnd() + expectedTrailingSpaces
                takeIf { nodeIndent.substringAfterLast('\t') > expectedIndent }
                    ?.let {
                        // Actual indentation contains more spaces than expected
                        nodeIndent
                            .removeSuffix(expectedTrailingSpaces)
                            .takeIf { ' ' in it }
                            ?.let { nodeIndentWithoutExpectedTrailingSpaces ->
                                // Remaining indentation contains unexpected space(s)
                                emit(
                                    startOffset + textLength - nodeIndent.substringAfterLast('\t').length + expectedIndent.length,
                                    "Unexpected space character(s)",
                                ) {
                                    nodeIndent =
                                        indentConfig
                                            .toNormalizedIndent(nodeIndentWithoutExpectedTrailingSpaces)
                                            .plus(expectedTrailingSpaces)
                                }
                            }
                    }
            }
        }
        return nodeIndent
    }

    private fun ASTNode.visitWhiteSpaceBeforeClosingQuote() {
        if (!this@IndentationRule::stringTemplateIndenter.isInitialized) {
            stringTemplateIndenter =
                StringTemplateIndenter(
                    codeStyle,
                    indentConfig,
                    emit = { offset, errorMessage, autoCorrect -> emit(offset, errorMessage, autoCorrect) },
                )
        }
        stringTemplateIndenter.visitClosingQuotes(currentIndent(), parent!!)
    }

    private fun ASTNode?.isElvisOperator() =
        this != null &&
            elementType == OPERATION_REFERENCE &&
            firstChildNode?.elementType == ELVIS

    private fun ASTNode.expectedTrailingSpaces(): String {
        require(isWhiteSpace)
        return when {
            nextLeaf?.elementType == KDOC_LEADING_ASTERISK || nextLeaf?.elementType == KDOC_END -> {
                // The indentation of a KDoc comment contains a space as the last character regardless of the indentation
                // style (tabs or spaces) except for the starting line of the KDoc comment
                KDOC_CONTINUATION_INDENT
            }

            treeParent.elementType == TYPE_CONSTRAINT_LIST -> {
                // 6 spaces (length of "where" keyword plus a separator space) to indent type constraints as below:
                //    where A1 : RecyclerView.Adapter<V1>,
                //          A1 : ComposableAdapter.ViewTypeProvider,
                TYPE_CONSTRAINT_CONTINUATION_INDENT
            }

            else -> {
                ""
            }
        }
    }

    private fun ASTNode.isPrecededByComment() = prevSibling { !it.isWhiteSpace }?.isPartOfComment == true

    public companion object {
        private const val KDOC_CONTINUATION_INDENT = " "
        private const val TYPE_CONSTRAINT_CONTINUATION_INDENT = "      " // Length of keyword "where" plus separating space
        private val CHAINABLE_EXPRESSION = setOf(DOT_QUALIFIED_EXPRESSION, SAFE_ACCESS_EXPRESSION)

        public val INDENT_WHEN_ARROW_ON_NEW_LINE: EditorConfigProperty<Boolean> =
            EditorConfigProperty(
                type =
                    PropertyType.LowerCasingPropertyType(
                        "ij_kotlin_indent_before_arrow_on_new_line",
                        "Indent the arrow in a when-entry if the arrow starts on a new line.",
                        PropertyValueParser.BOOLEAN_VALUE_PARSER,
                        setOf("true", "false"),
                    ),
                // Up until (and including) IntelliJ IDEA version `2024.1.6` the arrow on a newline was not indented. In version `2024.2`
                // the default behavior was changed to true. Disable by default to keep backward compatibility with older ktlint and IDEA
                // versions.
                defaultValue = false,
            )

        public val INDENT_EXPLICIT_CONSTRUCTOR_PROPERTY: EditorConfigProperty<Boolean> =
            EditorConfigProperty(
                type =
                    PropertyType.LowerCasingPropertyType(
                        "ktlint_indent_explicit_constructor",
                        "Indent an explicit constructor of a class. As side effect the entire class body will also be indented.",
                        PropertyValueParser.BOOLEAN_VALUE_PARSER,
                        setOf("true", "false"),
                    ),
                defaultValue = true,
                androidStudioCodeStyleDefaultValue = false,
                intellijIdeaCodeStyleDefaultValue = false,
                ktlintOfficialCodeStyleDefaultValue = true,
            )
    }

    private data class IndentContext(
        /**
         * The node on which the indent context starts.
         */
        val fromASTNode: ASTNode,
        /**
         * The node at which the indent context ends. If null, then the context ends at the last child leaf of the node
         * on which the indent context starts.
         */
        val toASTNode: ASTNode = fromASTNode.lastChildLeafOrSelf,
        /**
         * Cumulative indentation for the node. Normally this should be equal to a multiple of the
         * 'indentConfig.indentStyle' to ensure a consistent indentation style.
         */
        val nodeIndent: String,
        /**
         * Additional indentation for first child node. Normally this should be equal to the 'indentConfig.indent' to
         * ensure a consistent indentation style. In very limited cases when the default indentation is set to 'tab' it
         * is still needed to adjust the last indentation using spaces.
         */
        val firstChildIndent: String,
        /**
         * Additional indentation for child nodes. Normally this should be equal to the 'indentConfig.indent' to ensure
         * a consistent indentation style. In very limited cases when the default indentation is set to 'tab' it is
         * still needed to adjust the last indentation using spaces.
         */
        val childIndent: String,
        /**
         * Additional indentation for last child node. Normally this should be equal to the 'indentConfig.indent' to
         * ensure a consistent indentation style. In very limited cases when the default indentation is set to 'tab' it
         * is still needed to adjust the last indentation using spaces.
         */
        val lastChildIndent: String,
        /**
         * True when the indentation level of this context is activated
         */
        val activated: Boolean = false,
    ) {
        val nodes: String
            get() =
                fromASTNode
                    .prevLeaf // The 'fromAstNode' itself needs to be returned by '.leaves()' call as well
                    ?.leaves()
                    .orEmpty()
                    .takeWhile {
                        // The 'toAstNode' itself needs to be included as well
                        it != toASTNode.nextLeaf
                    }.joinToString(separator = "") { it.text }
                    .textWithEscapedTabAndNewline()

        fun indent() =
            if (activated) {
                nodeIndent + childIndent
            } else {
                nodeIndent
            }

        fun prevCodeLeaf() = fromASTNode.prevCodeLeaf!!
    }
}

private fun ASTNode.isDotQualifiedExpressionInArrayAccessCall() =
    elementType == DOT_QUALIFIED_EXPRESSION &&
        parent?.elementType == ARRAY_ACCESS_EXPRESSION &&
        parent?.parent?.elementType == CALL_EXPRESSION

private fun ASTNode.textWithEscapedTabAndNewline() = text.textWithEscapedTabAndNewline()

private fun String.textWithEscapedTabAndNewline(): String {
    val (prefix, suffix) =
        if (all { it.isWhitespace() }) {
            Pair("[", "]")
        } else {
            Pair("", "")
        }
    return prefix
        .plus(
            this
                .replace("\t", "\\t")
                .replace("\n", "\\n"),
        ).plus(suffix)
}

private class StringTemplateIndenter(
    private val codeStyle: CodeStyleValue,
    private val indentConfig: IndentConfig,
    private val emit: (offset: Int, errorMessage: String, autoCorrect: () -> Unit) -> Unit,
) {
    fun visitClosingQuotes(
        expectedIndent: String,
        node: ASTNode,
    ) {
        require(node.elementType == STRING_TEMPLATE)
        node
            .takeIf { it.isFollowedByTrimIndent() || it.isFollowedByTrimMargin() }
            ?.takeIf { it.isMultiLine() }
            ?.let { _ ->
                if (node.containsMixedIndentationCharacters()) {
                    // It can not be determined with certainty how mixed indentation characters should be interpreted.
                    // The trimIndent function handles tabs and spaces equally (one tabs equals one space) while the user
                    // might expect that the tab size in the indentation is more than one space.
                    emit(
                        node.startOffset,
                        "Indentation of multiline string should not contain both tab(s) and space(s)",
                    ) { /* NO-OP */ }
                    return
                }

                val prevLeaf = node.prevLeaf
                val correctedExpectedIndent =
                    if (codeStyle == KTLINT_OFFICIAL && node.isRawStringLiteralReturnInFunctionBodyBlock()) {
                        // Allow:
                        //   fun foo(): String {
                        //       return """
                        //           some text
                        //           """.trimIndent
                        //   }
                        node
                            .indentWithoutNewlinePrefix
                            .plus(indentConfig.indent)
                    } else if (codeStyle == KTLINT_OFFICIAL && node.isRawStringLiteralFunctionBodyExpression()) {
                        // Allow:
                        //   fun foo(
                        //       bar: String
                        //   ) = """
                        //       $bar
                        //       """.trimIndent
                        node
                            .indentWithoutNewlinePrefix
                            .plus(indentConfig.indent)
                    } else if (prevLeaf?.text == "\n") {
                        // In case the opening quotes are placed at the start of the line, then the closing quotes
                        // should have no indent as well.
                        ""
                    } else {
                        expectedIndent
                    }
                node
                    .children
                    .filter { it.isIndentBeforeClosingQuote() }
                    .forEach {
                        if (it.prevLeaf?.text == "\n") {
                            val (actualIndent, actualContent) = it.text.splitIndentAt(it.text.length)
                            if (actualIndent != correctedExpectedIndent) {
                                // It is a deliberate choice not to fix the indents inside the string literal except the line which only
                                // contains the closing quotes. See `string-template-indent` rule for fixing the content of the string
                                // template itself
                                emit(it.startOffset, "Unexpected indent of multiline string closing quotes") {
                                    if (it.firstChildNode == null) {
                                        (it as LeafPsiElement).rawInsertBeforeMe(
                                            LeafPsiElement(REGULAR_STRING_PART, correctedExpectedIndent),
                                        )
                                    } else {
                                        it.firstChildNode.replaceTextWith(correctedExpectedIndent + actualContent)
                                    }
                                }
                            }
                        }
                    }
            }
    }

    private fun ASTNode.isRawStringLiteralFunctionBodyExpression() =
        (!prevLeaf.isWhiteSpace || prevLeaf?.text == " ") &&
            FUN ==
            prevCodeLeaf
                .takeIf { it?.elementType == EQ }
                ?.parent
                ?.elementType

    private fun ASTNode.isRawStringLiteralReturnInFunctionBodyBlock() = RETURN_KEYWORD == prevCodeLeaf?.elementType

    private fun ASTNode.isFollowedByTrimIndent() = isFollowedBy("trimIndent()")

    private fun ASTNode.isFollowedByTrimMargin() = isFollowedBy("trimMargin()")

    private fun ASTNode.isFollowedBy(callExpressionName: String) =
        this
            .also { require(it.elementType == STRING_TEMPLATE) }
            .nextSibling { it.elementType != DOT }
            .let { it?.elementType == CALL_EXPRESSION && it.text == callExpressionName }

    private fun ASTNode.isMultiLine(): Boolean =
        children
            .filter { it.elementType == LITERAL_STRING_TEMPLATE_ENTRY }
            .any { it.text == "\n" }

    private fun ASTNode.containsMixedIndentationCharacters(): Boolean {
        assert(isMultiLine())
        val nonBlankLines =
            this
                .text
                .split("\n")
                .filterNot { it.startsWith("\"\"\"") }
                .filterNot { it.endsWith("\"\"\"") }
                .filterNot { it.isBlank() }
        val prefixLength = nonBlankLines.minOfOrNull { it.indentLength() } ?: 0
        val distinctIndentCharacters =
            nonBlankLines
                .joinToString(separator = "") {
                    it.splitIndentAt(prefixLength).first
                }.toCharArray()
                .distinct()
                .count()
        return distinctIndentCharacters > 1
    }

    private fun ASTNode.isIndentBeforeClosingQuote() =
        elementType == CLOSING_QUOTE || (text.isBlank() && nextCodeSibling?.elementType == CLOSING_QUOTE)

    private fun String.indentLength() = indexOfFirst { !it.isWhitespace() }.let { if (it == -1) length else it }

    /**
     * Splits the string at the given index or at the first non-white space character before that index. The returned pair
     * consists of the indentation and the second part contains the remainder. Note that the second part still can start
     * with whitespace characters in case the original strings starts with more white space characters than the requested
     * split index.
     */
    private fun String.splitIndentAt(index: Int): Pair<String, String> {
        assert(index >= 0)
        if (this == "\n") {
            return Pair("", "")
        }
        val firstNonWhitespaceIndex =
            indexOfFirst { !it.isWhitespace() }.let {
                if (it == -1) {
                    length
                } else {
                    it
                }
            }
        val safeIndex = kotlin.math.min(firstNonWhitespaceIndex, index)
        return Pair(
            first = take(safeIndex),
            second = substring(safeIndex),
        )
    }
}

public val INDENTATION_RULE_ID: RuleId = IndentationRule().ruleId
