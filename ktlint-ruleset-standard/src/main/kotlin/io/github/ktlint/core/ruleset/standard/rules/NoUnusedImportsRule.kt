package io.github.ktlint.core.ruleset.standard.rules

import com.intellij.lang.ASTNode
import com.intellij.psi.impl.source.tree.CompositeElement
import io.github.ktlint.core.rule.engine.core.api.ElementType.BY_KEYWORD
import io.github.ktlint.core.rule.engine.core.api.ElementType.CALL_EXPRESSION
import io.github.ktlint.core.rule.engine.core.api.ElementType.DOT_QUALIFIED_EXPRESSION
import io.github.ktlint.core.rule.engine.core.api.ElementType.FILE
import io.github.ktlint.core.rule.engine.core.api.ElementType.IDENTIFIER
import io.github.ktlint.core.rule.engine.core.api.ElementType.IMPORT_DIRECTIVE
import io.github.ktlint.core.rule.engine.core.api.ElementType.OPERATION_REFERENCE
import io.github.ktlint.core.rule.engine.core.api.ElementType.PACKAGE_DIRECTIVE
import io.github.ktlint.core.rule.engine.core.api.ElementType.REFERENCE_EXPRESSION
import io.github.ktlint.core.rule.engine.core.api.IgnoreKtlintSuppressions
import io.github.ktlint.core.rule.engine.core.api.RuleId
import io.github.ktlint.core.rule.engine.core.api.RuleV2.OnlyWhenEnabledInEditorconfig
import io.github.ktlint.core.rule.engine.core.api.SinceKtlint
import io.github.ktlint.core.rule.engine.core.api.SinceKtlint.Status.STABLE
import io.github.ktlint.core.rule.engine.core.api.children
import io.github.ktlint.core.rule.engine.core.api.isPartOf
import io.github.ktlint.core.rule.engine.core.api.isWhiteSpaceWithNewline
import io.github.ktlint.core.rule.engine.core.api.lastChildLeafOrSelf
import io.github.ktlint.core.rule.engine.core.api.nextLeaf
import io.github.ktlint.core.rule.engine.core.api.nextSibling
import io.github.ktlint.core.rule.engine.core.api.parent
import io.github.ktlint.core.rule.engine.core.api.prevLeaf
import io.github.ktlint.core.rule.engine.core.api.prevSibling
import io.github.ktlint.core.rule.engine.core.api.remove
import io.github.ktlint.core.rule.engine.core.api.replaceTextWith
import io.github.ktlint.core.rule.engine.core.util.safeAs
import io.github.ktlint.core.ruleset.standard.StandardRule
import org.jetbrains.kotlin.kdoc.lexer.KDocTokens.MARKDOWN_LINK
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression
import org.jetbrains.kotlin.psi.KtIdeApi
import org.jetbrains.kotlin.psi.KtImportDirective
import org.jetbrains.kotlin.psi.KtNonPublicApi
import org.jetbrains.kotlin.psi.KtPackageDirective
import org.jetbrains.kotlin.resolve.ImportPath

@SinceKtlint("0.2", STABLE)
public class NoUnusedImportsRule :
    StandardRule("no-unused-imports"),
    // This rule is an opt-in rule as occasionally an import is falsely marked as unused, and after removal results in code that cannot be
    // compiled. Upon request of the community (https://github.com/ktlint/ktlint/issues/3038) the rule will not be removed, but kept as
    // an opt-in rule.
    OnlyWhenEnabledInEditorconfig,
    // Prevent that imports which are only used inside code that is suppressed are (falsely) reported as unused.
    IgnoreKtlintSuppressions {
    private val ref =
        mutableSetOf(
            Reference("*", false),
        )
    private val parentExpressions = mutableSetOf<String>()
    private val imports = mutableMapOf<ImportPath, ASTNode>()
    private var packageName = ""
    private var foundByKeyword = false

    // `ktImportDirective.delete()` with Kotlin 2.4.0 results in exception below (`KtElementStub` is a superclass of `KtImportDirective`)
    //    Caused by: java.lang.IllegalStateException: Cannot mutate Kotlin PSI because KtPsiMutationService is missing
    // 	      at org.jetbrains.kotlin.psi.KtPsiMutationService$Companion.getInstance(KtPsiMutationService.kt:365)
    // 	      at org.jetbrains.kotlin.psi.KtPsiMutationService.getInstance(KtPsiMutationService.kt)
    // 	      at org.jetbrains.kotlin.psi.KtElementImplStub.delete(KtElementImplStub.java:99)
    @OptIn(KtNonPublicApi::class)
    private fun ASTNode.deleteImportDirective() {
        @OptIn(KtIdeApi::class)
        psi.safeAs<KtImportDirective>()?.rawDelete()
    }

    override fun afterVisitChildNodes(node: ASTNode) {
        if (node.elementType == FILE) {
            // Only after processing the entire file, the unused imports are to be checked. Finding the imports, references and parent
            // expressions in the beforeVisitChildNodes, and actual checking of unused imports in the afterVisitChildNodes is unreliable
            // as rules might refactor code that is containing references. The first step is to traverse the entire file to build the list
            // of imports, references and parent expressions. In the second step the unused imports are identified. Combining both steps
            // inside the same method, triggered by the FILE node only, ensures consistency between the steps.
            node.findImportsAndReferencesAndParentExpressions()
            findUnusedImports()
        }
    }

    private fun ASTNode.findImportsAndReferencesAndParentExpressions() {
        when (elementType) {
            PACKAGE_DIRECTIVE -> {
                val packageDirective = psi as KtPackageDirective
                packageName = packageDirective.qualifiedName
            }

            IMPORT_DIRECTIVE -> {
                val importPath = (psi as KtImportDirective).importPath!!
                if (imports.containsKey(importPath)) {
                    // Emit directly when same import occurs more than once
                    emit(startOffset, "Unused import") { deleteImportDirective() }
                } else {
                    imports[importPath] = this
                }
            }

            DOT_QUALIFIED_EXPRESSION -> {
                if (isExpressionForStaticImportWithExistingParentImport()) {
                    parentExpressions.add(text.substringBeforeLast("("))
                }
            }

            MARKDOWN_LINK -> {
                val linkText =
                    this
                        .text
                        .removeSurrounding("[", "]")
                        .removeBackticksAndTrim()
                ref.add(Reference(linkText.split('.').first(), false))
                ref.add(Reference(linkText.split('.').last(), false))
            }

            REFERENCE_EXPRESSION, OPERATION_REFERENCE -> {
                if (!isPartOf(IMPORT_DIRECTIVE)) {
                    val identifier =
                        if (this is CompositeElement) {
                            findChildByType(IDENTIFIER)
                        } else {
                            this
                        }
                    identifier
                        ?.let { identifier.text }
                        ?.takeIf { it.isNotBlank() }
                        ?.let {
                            ref.add(
                                Reference(
                                    it.removeBackticksAndTrim(),
                                    isParentDotQualifiedExpressionOrNull(),
                                ),
                            )
                        }
                }
            }

            BY_KEYWORD -> {
                foundByKeyword = true
            }
        }
        children.forEach { child ->
            child.findImportsAndReferencesAndParentExpressions()
        }
    }

    private fun findUnusedImports() {
        val directCalls = ref.filter { !it.inDotQualifiedExpression }.map { it.text }
        parentExpressions.forEach { parent ->
            imports
                .filterKeys { import ->
                    val importPath = import.pathStr.removeBackticksAndTrim()
                    importPath.endsWith(".$parent") && directCalls.none { importPath.endsWith(".$it") }
                }.forEach { (importPath, importNode) ->
                    emit(importNode.startOffset, "Unused import") {
                        imports.remove(importPath, importNode)
                        importNode.removeImportDirective()
                    }
                }
        }

        imports.forEach { (_, node) ->
            node.visitImport()
        }
    }

    private fun ASTNode.visitImport() {
        val importDirective = psi as KtImportDirective
        if (importDirective.isUnaliasedImportFromSamePackage()) {
            // Allow imports without alias for which the fully qualified path is equal to the package name. See
            // https://github.com/ktlint/ktlint/issues/2821 for an example in which marking an import from the same package
            // led to compile failure.
        } else if (importDirective.isUnusedImport()) {
            emit(startOffset, "Unused import") {
                val nextSibling = nextSibling
                if (nextSibling == null) {
                    // Last import
                    lastChildLeafOrSelf
                        .nextLeaf
                        ?.takeIf { it.isWhiteSpaceWithNewline }
                        ?.let { whitespace ->
                            if (prevLeaf == null) {
                                // Also it was the first import, and it is not preceded by any other node containing some text. So
                                // all whitespace until the next is redundant
                                whitespace.remove()
                            } else {
                                val textAfterFirstNewline =
                                    whitespace
                                        .text
                                        .substringAfter("\n")
                                if (textAfterFirstNewline.isNotBlank()) {
                                    whitespace.replaceTextWith(textAfterFirstNewline)
                                }
                            }
                        }
                } else {
                    nextSibling
                        .takeIf { it.isWhiteSpaceWithNewline }
                        ?.remove()
                }
                deleteImportDirective()
            }
        }
    }

    private fun KtImportDirective.isUnaliasedImportFromSamePackage(): Boolean {
        val importPath = importPath?.pathStr?.removeBackticksAndTrim()!!
        return aliasName == null &&
            (packageName.isEmpty() || importPath.startsWith("$packageName.")) &&
            importPath.substring(packageName.length + 1).indexOf('.') == -1
    }

    private fun KtImportDirective.isUnusedImport(): Boolean {
        val importedName =
            importPath
                ?.importedName
                ?.asString()
                ?.removeBackticksAndTrim()
        val importPath = importPath?.pathStr?.removeBackticksAndTrim()!!
        return importedName != null &&
            (!ref.map { it.text }.contains(importedName) || !isAValidImport(importPath)) &&
            !OPERATOR_SET.contains(importedName) &&
            !importedName.isComponentN() &&
            !importPath.ignoreProvideDelegate()
    }

    private fun String.ignoreProvideDelegate() =
        // Ignore provideDelegate if the `by` keyword is found anywhere in the file
        endsWith(".provideDelegate") && foundByKeyword

    private fun ASTNode.removeImportDirective() {
        require(elementType == IMPORT_DIRECTIVE)
        when {
            parent?.firstChildNode == this -> {
                nextSibling
                    ?.takeIf { it.isWhiteSpaceWithNewline }
                    ?.remove()
            }

            parent?.lastChildNode == this -> {
                prevSibling
                    ?.takeIf { it.isWhiteSpaceWithNewline }
                    ?.remove()
            }

            else -> {
                nextLeaf
                    ?.takeIf { it.isWhiteSpaceWithNewline }
                    ?.remove()
            }
        }
        remove()
    }

    private fun ASTNode.isExpressionForStaticImportWithExistingParentImport(): Boolean =
        if (containsMethodCall()) {
            val methodCallExpression = text.substringBeforeLast("(")
            !(methodCallExpression.isNotEmpty() && methodCallExpression[0] !in 'A'..'Z') &&
                areParentAndStaticImportsBothPresent(methodCallExpression)
            // Not a static import when method call expression starts with uppercase which identifies a class name rather than a
            // sub-package
        } else {
            false
        }

    private fun ASTNode.containsMethodCall() = text.split(".").last().contains("(")

    private fun areParentAndStaticImportsBothPresent(methodCallExpression: String) =
        imports
            .filterKeys { it.pathStr.removeBackticksAndTrim().endsWith(".$methodCallExpression") }
            .map {
                it
                    .key
                    .pathStr
                    .removeBackticksAndTrim()
                    .substringBefore(methodCallExpression)
            }.any { import ->
                imports.any {
                    it.key.pathStr
                        .removeBackticksAndTrim()
                        .startsWith(import)
                }
            }

    // Check if the import being checked is present in the filtered import list
    private fun isAValidImport(importPath: String) =
        imports.any {
            it
                .key
                .pathStr
                .removeBackticksAndTrim()
                .contains(importPath)
        }

    private fun String.isComponentN() = COMPONENT_N_REGEX.matches(this)

    private fun ASTNode.isParentDotQualifiedExpressionOrNull(): Boolean {
        val callExpressionOrThis = parentCallExpressionOrNull() ?: this
        return callExpressionOrThis.isDotQualifiedExpression()
    }

    private fun ASTNode.parentCallExpressionOrNull() =
        parent
            ?.takeIf { it.elementType == CALL_EXPRESSION }

    private fun ASTNode.isDotQualifiedExpression() =
        parent
            ?.takeIf { it.elementType == DOT_QUALIFIED_EXPRESSION }
            ?.let { it.psi as? KtDotQualifiedExpression }
            ?.takeIf { it.selectorExpression?.node == this }
            .let { it != null }

    private fun String.removeBackticksAndTrim() = replace("`", "").trim()

    private data class Reference(
        val text: String,
        val inDotQualifiedExpression: Boolean,
    )

    private companion object {
        val COMPONENT_N_REGEX = Regex("^component\\d+$")

        @Suppress("ktlint:standard:argument-list-wrapping")
        val OPERATOR_SET =
            setOf(
                // unary
                "unaryPlus", "unaryMinus", "not",
                // inc/dec
                "inc", "dec",
                // arithmetic
                "plus", "minus", "times", "div", "rem", "mod", "rangeTo", "rangeUntil",
                // in
                "contains",
                // indexed access
                "get", "set",
                // invoke
                "invoke",
                // (augmented) assignment
                "assign", "plusAssign", "minusAssign", "timesAssign", "divAssign", "modAssign",
                // (in)equality
                "equals",
                // comparison
                "compareTo",
                // iteration (https://github.com/shyiko/ktlint/issues/40)
                "iterator",
                // by (https://github.com/shyiko/ktlint/issues/54)
                "getValue", "setValue",
            )
    }
}

public val NO_UNUSED_IMPORTS_RULE_ID: RuleId = NoUnusedImportsRule().ruleId
