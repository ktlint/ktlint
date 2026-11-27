@file:Suppress("DEPRECATION")

package com.pinterest.ktlint.rule.engine.core.api.editorconfig

import dev.drewhamilton.poko.Poko
import org.ec4j.core.model.Property
import org.ec4j.core.model.PropertyType

/**
 * Definition of '.editorconfig' property enriched with fields required by the [io.github.ktlint.core.rule.engine.api.KtLintRuleEngine].
 *
 * Properties can be:
 * - universal `.editorconfig` properties defined
 *   [here](https://github.com/editorconfig/editorconfig/wiki/EditorConfig-Properties#current-universal-properties)
 * - universal IntelliJ IDEA properties defined
 *   [here](https://github.com/JetBrains/intellij-community/blob/master/platform/lang-api/src/com/intellij/psi/codeStyle/CommonCodeStyleSettings.java)
 * - Kotlin specific properties defined
 *   [here](https://github.com/JetBrains/kotlin/blob/master/idea/formatter/src/org/jetbrains/kotlin/idea/core/formatter/KotlinCodeStyleSettings.java)
 */
@Deprecated(message = "Provides backwards compatibility of custom ruleset JARs created for Ktlint 1.x. Don't use for RuleV2")
@Poko
public class EditorConfigProperty<T>(
    /**
     * Type of property. Could be one of default ones (see [PropertyType.STANDARD_TYPES]) or custom one.
     */
    public val type: PropertyType<T>,
    /**
     * Default value for property if it does not exist in loaded properties. This default applies to all code styles
     * unless the code style specific default value is set.
     */
    public val defaultValue: T,
    /**
     * Default value for property if it does not exist in loaded properties and codestyle 'ktlint_official'. When not
     * set, it is defaulted to [defaultValue].
     */
    public val ktlintOfficialCodeStyleDefaultValue: T = defaultValue,
    /**
     * Default value for property if it does not exist in loaded properties and codestyle 'intellij_idea'. When not
     * set, it is defaulted to [defaultValue].
     */
    public val intellijIdeaCodeStyleDefaultValue: T = defaultValue,
    /**
     * Default value for property if it does not exist in loaded properties and codestyle 'android_studio'. When not
     * set, it is defaulted to [defaultValue].
     */
    public val androidStudioCodeStyleDefaultValue: T = defaultValue,
    /**
     * If set, it maps the actual value set for the property, to another valid value for that property. See example
     * below where
     * ```kotlin
     * propertyMapper = { property, isAndroidCodeStyle ->
     *     when {
     *         property == null ->
     *             // property is not defined in ".editorconfig" file
     *         property.isUnset ->
     *             // property is defined in ".editorconfig" file with special value "unset"
     *         property.sourceValue == "some-string-value" ->
     *             // property is defined in ".editorconfig" file with a value that needs to be remapped to another
     *             // valid value. For example the "max_line_length" property accepts value "off" but is remapped to
     *             // "-1" in ktlint.
     *        else ->
     *             property.getValueAs() // or null
     *     }
     * }
     * ```
     * In case the lambda returns a null value then, the code style specific default value or the generic [defaultValue]
     * is used.
     */
    public val propertyMapper: ((Property?, CodeStyleValue) -> T?)? = null,
    /**
     * Custom function that represents [T] as String. Defaults to the standard `toString()` call. Override the
     * default implementation in case you need a different behavior than the standard `toString()` (e.g. for
     * collections joinToString() is more applicable).
     */
    public val propertyWriter: (T) -> String = { it.toString() },
    /**
     * Optional message to be displayed whenever the value of the property is being retrieved while it has been deprecated.
     */
    public val deprecationWarning: String? = null,
    /**
     * Optional message to be displayed whenever the value of the property is being retrieved while it has been deprecated.
     */
    public val deprecationError: String? = null,
    /**
     * Name of the property. A property must be named in case multiple properties are defined for the same type.
     * Defaults to the name of the type when not set.
     */
    public val name: String = type.name,
)
