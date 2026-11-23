package io.github.ktlint.core.ruleset.standard.rules

import com.intellij.lang.ASTNode
import com.intellij.lang.FileASTNode
import com.intellij.psi.tree.IElementType
import io.github.ktlint.core.rule.engine.core.api.ElementType.CLASS
import io.github.ktlint.core.rule.engine.core.api.ElementType.FUN
import io.github.ktlint.core.rule.engine.core.api.ElementType.IDENTIFIER
import io.github.ktlint.core.rule.engine.core.api.ElementType.MODIFIER_LIST
import io.github.ktlint.core.rule.engine.core.api.ElementType.OBJECT_DECLARATION
import io.github.ktlint.core.rule.engine.core.api.ElementType.PROPERTY
import io.github.ktlint.core.rule.engine.core.api.ElementType.TYPEALIAS
import io.github.ktlint.core.rule.engine.core.api.ElementType.TYPE_REFERENCE
import io.github.ktlint.core.rule.engine.core.api.RuleId
import io.github.ktlint.core.rule.engine.core.api.SinceKtlint
import io.github.ktlint.core.rule.engine.core.api.children
import io.github.ktlint.core.rule.engine.core.api.isRoot
import io.github.ktlint.core.ruleset.standard.StandardRule
import io.github.ktlint.core.ruleset.standard.rules.internal.regExIgnoringDiacriticsAndStrokesOnLetters
import org.jetbrains.kotlin.psi.KtFile

/**
 * [Kotlin lang documentation](https://kotlinlang.org/docs/coding-conventions.html#source-file-names):
 * If a Kotlin file contains a single class (potentially with related top-level declarations), its name should be
 * the same as the name of the class, with the `.kt` extension appended. If a file contains multiple classes,
 * or only top-level declarations, choose a name describing what the file contains, and name the file accordingly.
 * Use upper camel case with an uppercase first letter (also known as Pascal case),
 * for example, `ProcessDeclarations.kt`.
 *
 * According to issue https://youtrack.jetbrains.com/issue/KTIJ-21897/Kotlin-coding-convention-file-naming-for-class,
 * "class" above should be read as any type of class (data class, enum class, sealed class) and interfaces.
 *
 * A strict implementation of guideline above had unwanted consequences:
 *   - If the file contains a single top level private class, it does not make sense to force the name of file to be
 *     identical to that class.
 *   - If the file contains a coherent set of functions and one of those function returns an instance of a public class
 *     which happens to be the only top level class in that file, it might not always be best to force the file to be
 *     named after that class.
 *   - Existing functionality regarding files containing a single top level object/typealias was lost.
 *
 * Exceptions to this rule:
 * - file without `.kt` extension
 * - file with name `package.kt`
 */
@SinceKtlint("0.23", SinceKtlint.Status.STABLE)
public class FilenameRule : StandardRule("filename") {
    override fun beforeVisitChildNodes(node: ASTNode) {
        if (node.isRoot) {
            node as FileASTNode? ?: error("node is not ${FileASTNode::class} but ${node::class}")

            val filePath = (node.psi as? KtFile)?.virtualFilePath
            if (filePath?.endsWith(".kt") != true || filePath.endsWith("package.kt")) {
                // ignore all non ".kt" files (including ".kts")
                stopTraversalOfAST()
                return
            }

            val fileName =
                filePath
                    .replace('\\', '/') // Ensure compatibility with Windows OS
                    .substringAfterLast("/")
                    .substringBefore(".")

            val topLevelClassDeclarations = node.topLevelDeclarations(CLASS)
            if (topLevelClassDeclarations.size == 1) {
                val topLevelClassDeclaration = topLevelClassDeclarations.first()
                if (node.hasTopLevelDeclarationNotExtending(topLevelClassDeclaration.identifier)) {
                    fileName.shouldMatchPascalCase()
                } else {
                    // If the file only contains one (non-private) top level class and possibly some extension functions of
                    // that class, then its filename should be identical to the class name.
                    fileName.shouldMatchClassName(topLevelClassDeclaration.identifier)
                }
            } else {
                val topLevelDeclarations = node.topLevelDeclarations()
                if (topLevelDeclarations.size == 1) {
                    val topLevelDeclaration = topLevelDeclarations.first()
                    if (topLevelDeclaration.elementType == OBJECT_DECLARATION ||
                        topLevelDeclaration.elementType == TYPEALIAS
                    ) {
                        val pascalCaseIdentifier =
                            topLevelDeclaration
                                .identifier
                                .toPascalCase()
                        fileName.shouldMatchFileName(pascalCaseIdentifier)
                    } else {
                        fileName.shouldMatchPascalCase()
                    }
                } else {
                    fileName.shouldMatchPascalCase()
                }
            }
            stopTraversalOfAST()
        }
    }

    private fun ASTNode.topLevelDeclarations(elementType: IElementType? = null): List<TopLevelDeclaration> =
        children
            .filter { elementType == null || it.elementType == elementType }
            .filter { it.doesNotHavePrivateModifier() }
            .mapNotNull { it.toTopLevelDeclaration() }
            .distinct()
            .toList()

    private fun ASTNode.doesNotHavePrivateModifier(): Boolean =
        findChildByType(MODIFIER_LIST)
            ?.children
            ?.none { it.text == "private" }
            ?: true

    private fun ASTNode.hasTopLevelDeclarationNotExtending(className: String) =
        children
            .filter { it.doesNotHavePrivateModifier() }
            .any { it.isNotClassRelatedTopLevelDeclaration() || it.isFunctionNotExtending(className) }

    private fun ASTNode.isNotClassRelatedTopLevelDeclaration() = elementType in NON_CLASS_RELATED_TOP_LEVEL_DECLARATION_TYPES

    private fun ASTNode.isFunctionNotExtending(className: String) =
        elementType == FUN &&
            findChildByType(TYPE_REFERENCE)?.text?.let { !it.contains(className) } ?: true

    private fun String.shouldMatchClassName(className: String) {
        if (this != className) {
            emit(
                0,
                "File '$this.kt' contains a single class, and possibly related top level declarations for that class. The file should be " +
                    "named after the class, '$className.kt'",
            )
        }
    }

    private fun String.toPascalCase() = replaceFirstChar { it.uppercaseChar() }

    private fun String.shouldMatchFileName(filename: String) {
        if (this != filename) {
            emit(
                0,
                "File '$this.kt' contains a single top level declaration and should be named '$filename.kt'",
            )
        }
    }

    private fun String.shouldMatchPascalCase() {
        if (!this.matches(PASCAL_CASE_REGEX)) {
            emit(0, "File name '$this.kt' should conform PascalCase")
        }
    }

    private data class TopLevelDeclaration(
        val elementType: IElementType,
        val identifier: String,
    )

    private fun ASTNode.toTopLevelDeclaration(): TopLevelDeclaration? =
        findChildByType(IDENTIFIER)
            ?.text
            ?.removeSurrounding("`")
            ?.let { TopLevelDeclaration(elementType, it) }

    private companion object {
        val PASCAL_CASE_REGEX = "^[A-Z][A-Za-z\\d]*$".regExIgnoringDiacriticsAndStrokesOnLetters()
        val NON_CLASS_RELATED_TOP_LEVEL_DECLARATION_TYPES = listOf(OBJECT_DECLARATION, TYPEALIAS, PROPERTY)
    }
}

public val FILENAME_RULE_ID: RuleId = FilenameRule().ruleId
