package io.github.ktlint.core.rule.engine.core.api.editorconfig

import dev.drewhamilton.poko.Poko
import io.github.ktlint.core.logger.api.initKtLintKLogger
import io.github.ktlint.core.rule.engine.core.api.RuleV2
import io.github.oshai.kotlinlogging.KotlinLogging
import org.ec4j.core.model.Property
import org.ec4j.core.model.PropertyType
import org.ec4j.core.model.PropertyType.PropertyValue

private val LOGGER = KotlinLogging.logger {}.initKtLintKLogger()

/**
 * Loaded [Property]s from `.editorconfig` files.
 */
@Poko
public class EditorConfig(
    private val properties: Map<String, Property> = emptyMap(),
) {
    public constructor(vararg properties: Property) : this(
        properties.associateBy { property -> property.name },
    )

    private val codeStyle: CodeStyleValue
        get() =
            with(CODE_STYLE_PROPERTY) {
                // Prevent printing a lot of warnings when the code_style_value is not defined explicitly by not using the function to get
                // the default value for the property
                type
                    .getPropertyValue(type.name)
                    .parsed
                    ?: defaultValue
            }

    /**
     * Gets the value of [editorConfigProperty] from [EditorConfig] provided that the name of the property is identical to the name of the
     * type of the property. If the value is not found, the default value for the matching code style is returned.
     */
    public operator fun <T> get(editorConfigProperty: EditorConfigProperty<T>): T {
        when {
            editorConfigProperty.deprecationError != null -> {
                throw DeprecatedEditorConfigPropertyException(
                    "Property '${editorConfigProperty.name}' is disallowed: ${editorConfigProperty.deprecationError}",
                )
            }

            editorConfigProperty.deprecationWarning != null -> {
                LOGGER.warn { "Property '${editorConfigProperty.name}' is deprecated: ${editorConfigProperty.deprecationWarning}" }
            }
        }
        val property =
            properties.getOrElse(editorConfigProperty.name) {
                throw IllegalStateException(
                    "Property '${editorConfigProperty.name}' can not be retrieved from this EditorConfig. Note that the EditorConfig " +
                        "which is provided to class '${RuleV2::class.qualifiedName}' only contains the properties which are defined in " +
                        "the property '${RuleV2::usesEditorConfigProperties.name}'.",
                )
            }

        editorConfigProperty
            .propertyMapper
            ?.invoke(property, codeStyle)
            ?.let { newValue ->
                // If the property value is remapped to a non-null value then return it immediately.
                val originalValue = property.sourceValue
                if (newValue.toString() != originalValue) {
                    LOGGER.trace {
                        "Value of '.editorconfig' property '${editorConfigProperty.name}' is remapped " +
                            "from '$originalValue' to '$newValue'"
                    }
                }
                return newValue
            }

        return if (property.isUnset) {
            editorConfigProperty
                .getDefaultValue()
                .also {
                    LOGGER.trace {
                        "No value of '.editorconfig' property '${editorConfigProperty.name}' was found. Value has been defaulted to " +
                            "'$it'. Setting the value explicitly in '.editorconfig' removes this message from the log."
                    }
                }
        } else {
            // In case the EditorConfig is loaded incorrectly it can happen that the type field of the loaded property is null. Getting the
            // value via "property.getValueAs()" can lead to a class cast exception as the actual type of the value returned is transformed
            // to a String instead of type T. Getting the value via the editorConfigProperty type is safe as it can not be null.
            editorConfigProperty.getParsedValueOrDefault() ?: editorConfigProperty.getDefaultValue()
        }
    }

    /**
     * Gets the value of the property with [propertyType] and name [propertyName] from [EditorConfig]. Returns null if the type is not
     * found. Also, returns null when no property with the name is found.
     */
    public fun <T> getEditorConfigValueOrNull(
        propertyType: PropertyType<T>,
        propertyName: String,
    ): T? = propertyType.getPropertyValue(propertyName).parsed

    private fun <T> EditorConfigProperty<T>.getDefaultValue() =
        when (codeStyle) {
            CodeStyleValue.ANDROID_STUDIO -> androidStudioCodeStyleDefaultValue
            CodeStyleValue.INTELLIJ_IDEA -> intellijIdeaCodeStyleDefaultValue
            CodeStyleValue.KTLINT_OFFICIAL -> ktlintOfficialCodeStyleDefaultValue
        }

    /**
     * Checks whether a property with name [propertyName] is defined.
     */
    public fun contains(propertyName: String): Boolean = properties.contains(propertyName)

    /**
     * Gets the parsed value of the property with type [PropertyType]. Returns null if the property type is not found. Can only be used for
     * properties for which the name of the property is identical to the name of the type.
     */
    private fun <T> EditorConfigProperty<T>.getParsedValueOrDefault(): T? =
        this.type.getParsedValueOrDefault(this.type.name, this.defaultValue)

    /**
     * Gets the parsed value of the property with type [PropertyType] and name [propertyName]. Returns null if the property type is not
     * found. Intended to be used for properties for which the name is not identical to the name of the property type.
     */
    private fun <T> PropertyType<T>.getParsedValueOrDefault(
        propertyName: String,
        defaultValue: T,
    ): T =
        getPropertyValue(propertyName)
            .let { propertyValue ->
                if (propertyValue.isValid) {
                    propertyValue.parsed
                } else {
                    LOGGER.warn {
                        "Editorconfig property '$propertyName' contains an invalid value '${propertyValue.source}'. The default value " +
                            "'$defaultValue' is used instead."
                    }
                    defaultValue
                }
            }

    private fun <T> PropertyType<T>.getPropertyValue(propertyName: String): PropertyValue<T> =
        parse(
            properties[propertyName]?.sourceValue,
        )

    /**
     * Maps all properties with given [mapper] to a collection.
     */
    public fun <T> map(mapper: (Property) -> T): Collection<T> =
        properties
            .values
            .map { mapper(it) }

    /**
     * Adds the given [EditorConfigProperty]'s with its default value for the active code style to the [EditorConfig] if the property does
     * not yet exist.
     */
    public fun addPropertiesWithDefaultValueIfMissing(vararg additionalEditorConfigProperties: EditorConfigProperty<*>): EditorConfig =
        additionalEditorConfigProperties
            // Only add the additional properties if they are not yet provided in de EditorConfig. This prevents duplicates for ktlint 1.x
            // rulesets whenever a 1.x rule has defined an EditorConfig property which is also provided via the
            // additionalEditorConfigProperties.
            .filter { !properties.containsKey(it.name) }
            .toSet()
            .also { requireSingularIdentities(it) }
            .defaultProperties()
            .plus(
                // Overwrite default properties with values that were actually already defined in the EditorConfig
                properties,
            ).let { EditorConfig(it) }

    /**
     * Creates an [EditorConfig] containing [additionalEditorConfigProperties] only. Retains the value of the property when defined in the original
     * [EditorConfig]. If the property does not exist in the original [EditorConfig], it will be added and its value is set to the default
     * value of the active code style.
     */
    public fun filterBy(additionalEditorConfigProperties: Set<EditorConfigProperty<*>>): EditorConfig =
        addPropertiesWithDefaultValueIfMissing(*additionalEditorConfigProperties.toTypedArray())

    private fun requireSingularIdentities(editorConfigProperties: Set<EditorConfigProperty<*>>) {
        val editorConfigPropertiesWithMultipleIdentities =
            editorConfigProperties
                .groupBy { it.name }
                .filterValues { groupedEditorConfigProperties ->
                    groupedEditorConfigProperties
                        .distinctBy { it.hashCode() }
                        .count() > 1
                }
        require(editorConfigPropertiesWithMultipleIdentities.isEmpty()) {
            editorConfigPropertiesWithMultipleIdentities
                .map { (name, editorConfigProperties) ->
                    editorConfigProperties
                        .joinToString(
                            separator = "\n",
                            prefix = "Found multiple editorconfig properties with name '$name' but having distinct identities:\n",
                        ) { "  - $it" }
                }.joinToString(separator = "\n")
        }
    }

    private fun Set<EditorConfigProperty<*>>.defaultProperties(): Map<String, Property> =
        associate { editorConfigProperty ->
            editorConfigProperty
                .writeDefaultValue()
                .let { editorConfigProperty.name to editorConfigProperty.toPropertyWithValue(it) }
        }

    private fun <T> EditorConfigProperty<T>.writeDefaultValue() = propertyWriter(getDefaultValue())

    private fun <T> EditorConfigProperty<T>.toPropertyWithValue(value: String): Property =
        Property
            .builder()
            .type(type)
            .name(name)
            .value(value)
            .build()
}

private class DeprecatedEditorConfigPropertyException(
    message: String,
) : RuntimeException(message)
