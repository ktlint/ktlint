package io.github.ktlint.core.ruleset.standard.rules

import com.intellij.lang.ASTNode
import io.github.ktlint.core.rule.engine.core.api.ElementType.FUN_KEYWORD
import io.github.ktlint.core.rule.engine.core.api.ElementType.TYPE_ARGUMENT_LIST
import io.github.ktlint.core.rule.engine.core.api.ElementType.TYPE_PARAMETER_LIST
import io.github.ktlint.core.rule.engine.core.api.ElementType.VAL_KEYWORD
import io.github.ktlint.core.rule.engine.core.api.ElementType.VAR_KEYWORD
import io.github.ktlint.core.rule.engine.core.api.RuleId
import io.github.ktlint.core.rule.engine.core.api.SinceKtlint
import io.github.ktlint.core.rule.engine.core.api.SinceKtlint.Status.STABLE
import io.github.ktlint.core.rule.engine.core.api.isWhiteSpace
import io.github.ktlint.core.rule.engine.core.api.isWhiteSpaceWithoutNewline
import io.github.ktlint.core.rule.engine.core.api.nextLeaf
import io.github.ktlint.core.rule.engine.core.api.prevLeaf
import io.github.ktlint.core.rule.engine.core.api.remove
import io.github.ktlint.core.rule.engine.core.api.replaceTextWith
import io.github.ktlint.core.ruleset.standard.StandardRule

@SinceKtlint("0.30", STABLE)
public class SpacingAroundAngleBracketsRule : StandardRule("spacing-around-angle-brackets") {
    override fun beforeVisitChildNodes(node: ASTNode) {
        if (node.elementType != TYPE_PARAMETER_LIST && node.elementType != TYPE_ARGUMENT_LIST) {
            return
        }

        val openingBracket = node.firstChildNode
        if (openingBracket != null) {
            // Check for rogue spacing before an opening bracket, e.g. Map <String, Int>
            openingBracket
                .prevLeaf
                .takeIf { it.isWhiteSpace }
                ?.let { beforeLeftAngle ->
                    // Ignore when the whitespace is preceded by certain keywords, e.g. fun <T> func(arg: T) {}
                    if (!ELEMENT_TYPES_ALLOWING_PRECEDING_WHITESPACE.contains(beforeLeftAngle.prevLeaf?.elementType)) {
                        emit(beforeLeftAngle.startOffset, "Unexpected spacing before \"<\"") { beforeLeftAngle.remove() }
                    }
                }

            // Check for rogue spacing after an opening bracket
            openingBracket
                .nextLeaf
                .takeIf { it.isWhiteSpace }
                ?.let { afterLeftAngle ->
                    if (afterLeftAngle.isWhiteSpaceWithoutNewline) {
                        emit(afterLeftAngle.startOffset, "Unexpected spacing after \"<\"") {
                            // when spacing does not include any new lines, e.g. Map< String, Int>
                            afterLeftAngle.remove()
                        }
                    } else {
                        // when spacing contains at least one new line, e.g.
                        // SomeGenericType<[whitespace]
                        //
                        //      String, Int, String>
                        // gets converted to
                        // SomeGenericType<
                        //      String, Int, String>
                        val newLineWithIndent = afterLeftAngle.text.trimBeforeLastLine()
                        if (newLineWithIndent != afterLeftAngle.text) {
                            emit(afterLeftAngle.startOffset, "Single newline expected after \"<\"") {
                                afterLeftAngle.replaceTextWith(newLineWithIndent)
                            }
                        }
                    }
                }
        }

        val closingBracket = node.lastChildNode
        closingBracket
            ?.prevLeaf
            ?.takeIf { it.isWhiteSpace }
            ?.let { beforeRightAngle ->
                // Check for rogue spacing before a closing bracket
                if (beforeRightAngle.isWhiteSpaceWithoutNewline) {
                    emit(beforeRightAngle.startOffset, "Unexpected spacing before \">\"") {
                        // when spacing does not include any new lines, e.g. Map<String, Int >
                        beforeRightAngle.remove()
                    }
                } else {
                    // when spacing contains at least one new line, e.g.
                    // SomeGenericType<String, Int, String[whitespace]
                    //
                    //      >
                    // gets converted to
                    // SomeGenericType<String, Int, String
                    //      >
                    val newLineWithIndent = beforeRightAngle.text.trimBeforeLastLine()
                    if (newLineWithIndent != beforeRightAngle.text) {
                        emit(beforeRightAngle.startOffset, "Single newline expected before \">\"") {
                            beforeRightAngle.replaceTextWith(newLineWithIndent)
                        }
                    }
                }
            }
    }

    private fun String.trimBeforeLastLine() = this.substring(this.lastIndexOf('\n'))

    private companion object {
        val ELEMENT_TYPES_ALLOWING_PRECEDING_WHITESPACE = setOf(VAL_KEYWORD, VAR_KEYWORD, FUN_KEYWORD)
    }
}

public val SPACING_AROUND_ANGLE_BRACKETS_RULE_ID: RuleId = SpacingAroundAngleBracketsRule().ruleId
