package io.github.ktlint.core.ruleset.standard.rules

import io.github.ktlint.core.rule.engine.core.api.AutocorrectDecision
import io.github.ktlint.core.rule.engine.core.api.ElementType.CLASS_BODY
import io.github.ktlint.core.rule.engine.core.api.ElementType.CONST_KEYWORD
import io.github.ktlint.core.rule.engine.core.api.ElementType.FILE
import io.github.ktlint.core.rule.engine.core.api.ElementType.GET_KEYWORD
import io.github.ktlint.core.rule.engine.core.api.ElementType.IDENTIFIER
import io.github.ktlint.core.rule.engine.core.api.ElementType.OBJECT_DECLARATION
import io.github.ktlint.core.rule.engine.core.api.ElementType.OVERRIDE_KEYWORD
import io.github.ktlint.core.rule.engine.core.api.ElementType.PROPERTY
import io.github.ktlint.core.rule.engine.core.api.ElementType.PROPERTY_ACCESSOR
import io.github.ktlint.core.rule.engine.core.api.ElementType.VAL_KEYWORD
import io.github.ktlint.core.rule.engine.core.api.RuleId
import io.github.ktlint.core.rule.engine.core.api.SinceKtlint
import io.github.ktlint.core.rule.engine.core.api.SinceKtlint.Status.EXPERIMENTAL
import io.github.ktlint.core.rule.engine.core.api.SinceKtlint.Status.STABLE
import io.github.ktlint.core.rule.engine.core.api.children
import io.github.ktlint.core.rule.engine.core.api.editorconfig.EditorConfig
import io.github.ktlint.core.rule.engine.core.api.editorconfig.EditorConfigProperty
import io.github.ktlint.core.rule.engine.core.api.editorconfig.SafeEnumValueParser
import io.github.ktlint.core.rule.engine.core.api.hasModifier
import io.github.ktlint.core.rule.engine.core.api.parent
import io.github.ktlint.core.ruleset.standard.StandardRule
import io.github.ktlint.core.ruleset.standard.rules.internal.regExIgnoringDiacriticsAndStrokesOnLetters
import org.ec4j.core.model.PropertyType
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.lexer.KtTokens

/**
 * https://kotlinlang.org/docs/coding-conventions.html#function-names
 * https://kotlinlang.org/docs/coding-conventions.html#property-names
 */
@SinceKtlint("0.48", EXPERIMENTAL)
@SinceKtlint("1.0", STABLE)
public class PropertyNamingRule :
    StandardRule(
        id = "property-naming",
        usesEditorConfigProperties = setOf(CONSTANT_NAMING_PROPERTY),
    ) {
    private var constantNamingProperty = CONSTANT_NAMING_PROPERTY.defaultValue

    override fun beforeFirstNode(editorConfig: EditorConfig) {
        constantNamingProperty = editorConfig[CONSTANT_NAMING_PROPERTY]
    }

    override fun beforeVisitChildNodes(
        node: ASTNode,
        emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> AutocorrectDecision,
    ) {
        node
            .takeIf { node.elementType == PROPERTY }
            ?.let { property -> visitProperty(property, emit) }
    }

    private fun visitProperty(
        property: ASTNode,
        emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> AutocorrectDecision,
    ) {
        property
            .findChildByType(IDENTIFIER)
            ?.takeUnless { it.isTokenKeywordBetweenBackticks() }
            ?.let { identifier ->
                when {
                    property.hasConstModifier() -> {
                        visitConstProperty(identifier, emit)
                    }

                    property.hasCustomGetter() || property.isTopLevelValue() || property.isObjectValue() -> {
                        // Can not reliably determine whether the value is immutable or not
                    }

                    else -> {
                        visitNonConstProperty(identifier, emit)
                    }
                }
            }
    }

    private fun visitConstProperty(
        identifier: ASTNode,
        emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> AutocorrectDecision,
    ) {
        identifier
            .text
            .takeUnless {
                // Allow
                // object Foo {
                //     private const val serialVersionUID: Long = 123
                // }
                it == SERIAL_VERSION_UID_PROPERTY_NAME
            }?.takeUnless { it.matches(constantNamingProperty.regEx) }
            ?.let {
                val expectedNaming = constantNamingProperty.name.lowercase().replace("_", " ")
                emit(
                    identifier.startOffset,
                    "Property name should use the $expectedNaming notation when the value can not be changed",
                    false,
                )
            }
    }

    private fun visitNonConstProperty(
        identifier: ASTNode,
        emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> AutocorrectDecision,
    ) {
        identifier
            .text
            .takeUnless { it.matches(LOWER_CAMEL_CASE_REGEXP) }
            ?.takeUnless {
                // Ignore backing properties
                it.startsWith("_")
            }?.let {
                emit(identifier.startOffset, "Property name should start with a lowercase letter and use camel case", false)
            }
    }

    private fun ASTNode.hasCustomGetter() = findChildByType(PROPERTY_ACCESSOR)?.findChildByType(GET_KEYWORD) != null

    private fun ASTNode.hasConstModifier() = hasModifier(CONST_KEYWORD)

    private fun ASTNode.isTopLevelValue() = parent?.elementType == FILE && containsValKeyword()

    private fun ASTNode.containsValKeyword() = children.any { it.elementType == VAL_KEYWORD }

    private fun ASTNode.isObjectValue() =
        parent?.elementType == CLASS_BODY &&
            parent?.parent?.elementType == OBJECT_DECLARATION &&
            containsValKeyword() &&
            !hasModifier(OVERRIDE_KEYWORD)

    private fun ASTNode.isTokenKeywordBetweenBackticks() =
        this
            .takeIf { it.elementType == IDENTIFIER }
            ?.text
            .orEmpty()
            .removeSurrounding("`")
            .let { KEYWORDS.contains(it) }

    public companion object {
        private val LOWER_CAMEL_CASE_REGEXP = "[a-z][a-zA-Z0-9]*".regExIgnoringDiacriticsAndStrokesOnLetters()
        private const val SERIAL_VERSION_UID_PROPERTY_NAME = "serialVersionUID"
        private val KEYWORDS =
            setOf(KtTokens.KEYWORDS, KtTokens.SOFT_KEYWORDS)
                .flatMap { tokenSet -> tokenSet.types.mapNotNull { it.debugName } }
                .filterNot { keyword ->
                    // The keyword sets contain a few 'keywords' which should be ignored. All valid keywords only contain lowercase
                    // characters
                    keyword.any { it.isUpperCase() }
                }.toSet()

        public enum class ConstantNamingStyle(
            public val regEx: Regex,
        ) {
            /**
             * The name of a constant must start with an uppercase character followed by zero or more uppercase characters, numbers, or
             * underscore characters to separate words in the name. The latin characters may also be combined with strokes and diacritics.
             */
            SCREAMING_SNAKE_CASE("[A-Z][_A-Z0-9]*".regExIgnoringDiacriticsAndStrokesOnLetters()),

            /**
             * The name of a constant must start with an uppercase character followed by zero or more uppercase characters or numbers. Each
             * word in the name should start with an uppercase character. The latin characters may also be combined with strokes and
             * diacritics.
             */
            PASCAL_CASE("[A-Z][a-zA-Z0-9]*".regExIgnoringDiacriticsAndStrokesOnLetters()),
        }

        public val CONSTANT_NAMING_PROPERTY_TYPE:
            PropertyType.LowerCasingPropertyType<ConstantNamingStyle> =
            PropertyType.LowerCasingPropertyType(
                "ktlint_property_naming_constant_naming",
                "The naming style ('screaming_snake_case', or 'pascal_case') to be applied on constant properties. All code styles use " +
                    "'screaming_snake_case' code as default.",
                SafeEnumValueParser(ConstantNamingStyle::class.java),
                ConstantNamingStyle.entries
                    .map { it.name }
                    .toSet(),
            )

        public val CONSTANT_NAMING_PROPERTY: EditorConfigProperty<ConstantNamingStyle> =
            EditorConfigProperty(
                type = CONSTANT_NAMING_PROPERTY_TYPE,
                defaultValue = ConstantNamingStyle.SCREAMING_SNAKE_CASE,
            )
    }
}

public val PROPERTY_NAMING_RULE_ID: RuleId = PropertyNamingRule().ruleId
