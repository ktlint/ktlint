package io.github.ktlint.core.ruleset.standard.rules

import io.github.ktlint.core.rule.engine.core.api.ElementType.BLOCK
import io.github.ktlint.core.rule.engine.core.api.ElementType.BODY
import io.github.ktlint.core.rule.engine.core.api.ElementType.CLASS
import io.github.ktlint.core.rule.engine.core.api.ElementType.CLASS_BODY
import io.github.ktlint.core.rule.engine.core.api.ElementType.ENUM_KEYWORD
import io.github.ktlint.core.rule.engine.core.api.ElementType.EOL_COMMENT
import io.github.ktlint.core.rule.engine.core.api.ElementType.FILE
import io.github.ktlint.core.rule.engine.core.api.ElementType.FOR
import io.github.ktlint.core.rule.engine.core.api.ElementType.FUN
import io.github.ktlint.core.rule.engine.core.api.ElementType.IF
import io.github.ktlint.core.rule.engine.core.api.ElementType.IMPORT_DIRECTIVE
import io.github.ktlint.core.rule.engine.core.api.ElementType.LBRACE
import io.github.ktlint.core.rule.engine.core.api.ElementType.OBJECT_KEYWORD
import io.github.ktlint.core.rule.engine.core.api.ElementType.PROPERTY
import io.github.ktlint.core.rule.engine.core.api.ElementType.RBRACE
import io.github.ktlint.core.rule.engine.core.api.ElementType.SEMICOLON
import io.github.ktlint.core.rule.engine.core.api.ElementType.THEN
import io.github.ktlint.core.rule.engine.core.api.ElementType.WHILE
import io.github.ktlint.core.rule.engine.core.api.RuleId
import io.github.ktlint.core.rule.engine.core.api.SinceKtlint
import io.github.ktlint.core.rule.engine.core.api.SinceKtlint.Status.STABLE
import io.github.ktlint.core.rule.engine.core.api.findParentByType
import io.github.ktlint.core.rule.engine.core.api.hasModifier
import io.github.ktlint.core.rule.engine.core.api.indent
import io.github.ktlint.core.rule.engine.core.api.isWhiteSpace
import io.github.ktlint.core.rule.engine.core.api.isWhiteSpaceWithNewline
import io.github.ktlint.core.rule.engine.core.api.isWhiteSpaceWithoutNewline
import io.github.ktlint.core.rule.engine.core.api.nextCodeLeaf
import io.github.ktlint.core.rule.engine.core.api.nextCodeSibling
import io.github.ktlint.core.rule.engine.core.api.nextLeaf
import io.github.ktlint.core.rule.engine.core.api.parent
import io.github.ktlint.core.rule.engine.core.api.prevCodeLeaf
import io.github.ktlint.core.rule.engine.core.api.prevCodeSibling
import io.github.ktlint.core.rule.engine.core.api.prevLeaf
import io.github.ktlint.core.rule.engine.core.api.prevSibling
import io.github.ktlint.core.rule.engine.core.api.remove
import io.github.ktlint.core.rule.engine.core.api.upsertWhitespaceAfterMe
import io.github.ktlint.core.ruleset.standard.StandardRule
import org.jetbrains.kotlin.com.intellij.lang.ASTNode

@SinceKtlint("0.1", STABLE)
public class SemicolonRule : StandardRule(id = "semicolon") {
    override fun beforeVisitChildNodes(node: ASTNode) {
        if (node.elementType == SEMICOLON) {
            node.visitSemicolon()
        }
    }

    private fun ASTNode.visitSemicolon() {
        if (isRedundantSemicolon()) {
            if (isUnnecessarySemicolon()) {
                // Remove semicolon and whitespace
                emit(this.startOffset, "Unnecessary semicolon") {
                    val prevLeaf = prevLeaf
                    val nextLeaf = nextLeaf
                    if (prevLeaf.isNonNullWhitespace() && nextLeaf.isNonNullWhitespace()) {
                        if (prevLeaf.isWhiteSpaceWithNewline && nextLeaf.isWhiteSpaceWithoutNewline) {
                            // Keep the newline before the semicolon
                            nextLeaf!!.remove()
                        } else {
                            prevLeaf!!.remove()
                        }
                    }
                    remove()
                }
            } else if (nextLeaf.isWhiteSpaceWithoutNewline || !nextLeaf.isWhiteSpace) {
                // Remove semicolon and add a newline
                emit(this.startOffset, """Replace ';' with newline""") {
                    // Remove the whitespace before the semicolon as it would otherwise become a trailing whitespace
                    prevLeaf
                        ?.takeIf { it.isWhiteSpaceWithoutNewline }
                        ?.remove()
                    upsertWhitespaceAfterMe(prevCodeLeaf!!.indent)
                    remove()
                }
            }
        } else if (nextLeaf.isWhiteSpaceWithoutNewline || !nextLeaf.isWhiteSpace) {
            if (!prevLeaf.isWhiteSpaceWithNewline) {
                // Preserve the semicolon and add a newline
                emit(this.startOffset + 1, "Missing newline after ';'") {
                    upsertWhitespaceAfterMe(indent)
                }
            }
        }
    }

    @Suppress("CyclomaticComplexMethod")
    private fun ASTNode.isRedundantSemicolon(): Boolean {
        require(elementType == SEMICOLON)
        return when {
            prevCodeLeaf?.elementType == OBJECT_KEYWORD -> {
                // https://github.com/ktlint/ktlint/issues/281
                false
            }

            prevCodeLeaf?.parent?.isLoopWithoutBody() ?: false -> {
                // https://github.com/ktlint/ktlint/issues/955
                false
            }

            prevCodeLeaf?.parent?.isIfExpressionWithoutThen() ?: false -> {
                false
            }

            isLastLeafOrFollowedByRbrace() -> {
                true
            }

            isEnumClassWithoutValues() -> {
                false
            }

            isLastLeafOrFollowedByRbrace() -> {
                true
            }

            parent?.elementType == IMPORT_DIRECTIVE && nextCodeSibling == null -> {
                // Disallow multiple import directive on the same line
                // import foo; import bar
                true
            }

            parent?.elementType == PROPERTY && nextCodeSibling == null -> {
                // Disallow multiple top level property declarations on the same line
                // val foo = "foo"; val bar = "bar"
                true
            }

            parent?.elementType == FUN && nextCodeSibling == null -> {
                // Disallow multiple functions on the same line
                // fun foo() = "foo"; fun bar() = "bar"
                true
            }

            parent?.elementType == CLASS_BODY || parent?.elementType == FILE -> {
                // Disallow multiple declarations on same line
                true
            }

            else -> {
                parent?.elementType == BLOCK && nextCodeLeaf?.elementType != LBRACE
            }
        }
    }

    private fun ASTNode.isLoopWithoutBody() =
        (elementType == WHILE || elementType == FOR) &&
            findChildByType(BODY)?.firstChildNode == null

    private fun ASTNode.isIfExpressionWithoutThen() = elementType == IF && findChildByType(THEN)?.firstChildNode == null

    private fun ASTNode?.isEnumClassWithoutValues() =
        this
            ?.takeIf { !it.isLastLeafOrFollowedByRbrace() }
            ?.findParentByType(CLASS_BODY)
            ?.takeIf { this == it.firstChildNode.nextCodeSibling }
            ?.findParentByType(CLASS)
            ?.hasModifier(ENUM_KEYWORD)
            ?: false

    private fun ASTNode.isLastLeafOrFollowedByRbrace(): Boolean {
        require(elementType == SEMICOLON)
        return nextLeaf == null || nextLeaf?.nextCodeLeaf?.elementType == RBRACE
    }

    private fun ASTNode.isUnnecessarySemicolon(): Boolean {
        require(elementType == SEMICOLON)
        return isLastLeafOrFollowedByRbrace() ||
            (nextLeaf.isWhiteSpaceWithoutNewline && nextLeaf?.nextLeaf?.elementType == EOL_COMMENT) ||
            prevCodeLeaf == null ||
            prevCodeSibling?.elementType == LBRACE ||
            prevSibling.isWhiteSpaceWithNewline
    }

    private fun ASTNode?.isNonNullWhitespace(): Boolean = this != null && isWhiteSpace
}

public val NO_SEMICOLONS_RULE_ID: RuleId = SemicolonRule().ruleId
