package io.github.ktlint.core.ruleset.standard.rules

import com.intellij.lang.ASTNode
import io.github.ktlint.core.rule.engine.core.api.ElementType.CLASS_BODY
import io.github.ktlint.core.rule.engine.core.api.ElementType.COMPANION_KEYWORD
import io.github.ktlint.core.rule.engine.core.api.ElementType.FUN
import io.github.ktlint.core.rule.engine.core.api.ElementType.IDENTIFIER
import io.github.ktlint.core.rule.engine.core.api.ElementType.INTERNAL_KEYWORD
import io.github.ktlint.core.rule.engine.core.api.ElementType.MODIFIER_LIST
import io.github.ktlint.core.rule.engine.core.api.ElementType.OBJECT_DECLARATION
import io.github.ktlint.core.rule.engine.core.api.ElementType.OVERRIDE_KEYWORD
import io.github.ktlint.core.rule.engine.core.api.ElementType.PRIVATE_KEYWORD
import io.github.ktlint.core.rule.engine.core.api.ElementType.PROPERTY
import io.github.ktlint.core.rule.engine.core.api.ElementType.PROTECTED_KEYWORD
import io.github.ktlint.core.rule.engine.core.api.ElementType.VALUE_PARAMETER
import io.github.ktlint.core.rule.engine.core.api.ElementType.VALUE_PARAMETER_LIST
import io.github.ktlint.core.rule.engine.core.api.RuleId
import io.github.ktlint.core.rule.engine.core.api.SinceKtlint
import io.github.ktlint.core.rule.engine.core.api.SinceKtlint.Status.EXPERIMENTAL
import io.github.ktlint.core.rule.engine.core.api.SinceKtlint.Status.STABLE
import io.github.ktlint.core.rule.engine.core.api.children
import io.github.ktlint.core.rule.engine.core.api.editorconfig.CODE_STYLE_PROPERTY
import io.github.ktlint.core.rule.engine.core.api.editorconfig.CodeStyleValue.ANDROID_STUDIO
import io.github.ktlint.core.rule.engine.core.api.editorconfig.EditorConfig
import io.github.ktlint.core.rule.engine.core.api.findParentByType
import io.github.ktlint.core.rule.engine.core.api.hasModifier
import io.github.ktlint.core.rule.engine.core.api.parent
import io.github.ktlint.core.ruleset.standard.StandardRule
import io.github.ktlint.core.ruleset.standard.rules.internal.regExIgnoringDiacriticsAndStrokesOnLetters

/**
 * https://kotlinlang.org/docs/coding-conventions.html#property-names
 * https://developer.android.com/kotlin/style-guide#backing_properties
 */
@SinceKtlint("1.0", EXPERIMENTAL)
@SinceKtlint("1.3", STABLE)
public class BackingPropertyNamingRule :
    StandardRule(
        id = "backing-property-naming",
        usesEditorConfigProperties = setOf(CODE_STYLE_PROPERTY),
    ) {
    private var codeStyle = CODE_STYLE_PROPERTY.defaultValue

    override fun beforeFirstNode(editorConfig: EditorConfig) {
        codeStyle = editorConfig[CODE_STYLE_PROPERTY]
    }

    override fun beforeVisitChildNodes(node: ASTNode) {
        node
            .takeIf { node.elementType == PROPERTY }
            ?.visitProperty()
    }

    private fun ASTNode.visitProperty() {
        findChildByType(IDENTIFIER)
            ?.takeIf { it.text.startsWith("_") }
            ?.takeUnless {
                // Exclude local property with name equals to "_". This is used to express the intent that the return value of a function
                // call is not used (https://github.com/Kotlin/KEEP/blob/main/proposals/KEEP-0412-underscores-for-local-variables.md)
                it.textLength == 1
            }?.takeUnless {
                // Do not report overridden properties as they can only be changed by changing the base property
                it.parent!!.hasModifier(OVERRIDE_KEYWORD)
            }?.visitBackingProperty()
    }

    private fun ASTNode.visitBackingProperty() {
        text
            .takeUnless { it.matches(BACKING_PROPERTY_LOWER_CAMEL_CASE_REGEXP) }
            ?.let { emit(startOffset, "Backing property should start with underscore followed by lower camel case") }

        if (!hasPrivateModifierInPropertyDeclaration() && !isDeclaredInPrivateCompanionObject()) {
            emit(startOffset, "Backing property not allowed when 'private' modifier is missing")
        }

        // A backing property can only exist when a correlated public property or function exists
        val correlatedPropertyOrFunction = findCorrelatedPropertyOrFunction()
        if (correlatedPropertyOrFunction == null) {
            emit(startOffset, "Backing property is only allowed when a matching property or function exists")
        } else {
            if (codeStyle == ANDROID_STUDIO || correlatedPropertyOrFunction.isPublic()) {
                return
            } else {
                emit(startOffset, "Backing property is only allowed when the matching property or function is public")
            }
        }
    }

    private fun ASTNode.hasPrivateModifierInPropertyDeclaration() = parent!!.hasModifier(PRIVATE_KEYWORD)

    private fun ASTNode.isDeclaredInPrivateCompanionObject() =
        true ==
            parent
                ?.parent
                ?.findCompanionObject()
                ?.parent
                ?.parent
                ?.hasModifier(PRIVATE_KEYWORD)

    private fun ASTNode.findCorrelatedPropertyOrFunction() = findCorrelatedProperty() ?: findCorrelatedFunction()

    private fun ASTNode.findCorrelatedProperty(): ASTNode? {
        val propertyName = text.removePrefix("_")
        return findParentByType(CLASS_BODY)
            ?.let { classBody ->
                // Check if related property exists in the same class body. If not, and the class body is the body of a companion object,
                // than also search in the class body where the companion object is defined.
                classBody.findPropertyWithName(propertyName)
                    ?: classBody
                        .findCompanionObject()
                        ?.findParentByType(CLASS_BODY)
                        ?.findPropertyWithName(propertyName)
            }
    }

    private fun ASTNode.findPropertyWithName(name: String) =
        children
            .filter { it.elementType == PROPERTY }
            .mapNotNull { it.findChildByType(IDENTIFIER) }
            .firstOrNull { it.text == name }
            ?.parent

    private fun ASTNode.findCompanionObject() =
        parent
            ?.takeIf { it.elementType == OBJECT_DECLARATION }
            ?.findChildByType(MODIFIER_LIST)
            ?.children
            ?.firstOrNull { it.elementType == COMPANION_KEYWORD }

    private fun ASTNode.findCorrelatedFunction(): ASTNode? {
        val correlatedFunctionName = "get${capitalizeFirstChar()}"
        return findParentByType(CLASS_BODY)
            ?.let { classBody ->
                // Check if related property exists in the same class body. If not, and the class body is the body of a companion object,
                // than also search in the class body where the companion object is defined.
                classBody.findFunctionWithName(correlatedFunctionName)
                    ?: classBody
                        .findCompanionObject()
                        ?.findParentByType(CLASS_BODY)
                        ?.findFunctionWithName(correlatedFunctionName)
            }
    }

    private fun ASTNode.findFunctionWithName(name: String) =
        children
            .filter { it.elementType == FUN }
            .filter { it.hasNonEmptyParameterList() }
            .mapNotNull { it.findChildByType(IDENTIFIER) }
            .firstOrNull { it.text == name }
            ?.parent

    private fun ASTNode.hasNonEmptyParameterList() =
        findChildByType(VALUE_PARAMETER_LIST)
            ?.children
            ?.none { it.elementType == VALUE_PARAMETER }
            ?: false

    private fun ASTNode.capitalizeFirstChar() =
        text
            .removePrefix("_")
            .replaceFirstChar { it.uppercase() }

    private fun ASTNode.isPublic() =
        !hasModifier(PRIVATE_KEYWORD) &&
            !hasModifier(PROTECTED_KEYWORD) &&
            !hasModifier(INTERNAL_KEYWORD)

    private companion object {
        val BACKING_PROPERTY_LOWER_CAMEL_CASE_REGEXP = "_[a-z][a-zA-Z0-9]*".regExIgnoringDiacriticsAndStrokesOnLetters()
    }
}

public val BACKING_PROPERTY_NAMING_RULE_ID: RuleId = BackingPropertyNamingRule().ruleId
