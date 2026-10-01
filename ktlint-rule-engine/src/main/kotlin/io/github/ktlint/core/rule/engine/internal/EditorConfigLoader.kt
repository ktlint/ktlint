package io.github.ktlint.core.rule.engine.internal

import io.github.ktlint.core.logger.api.initKtLintKLogger
import io.github.ktlint.core.rule.engine.api.EditorConfigDefaults
import io.github.ktlint.core.rule.engine.api.EditorConfigDefaults.Companion.EMPTY_EDITOR_CONFIG_DEFAULTS
import io.github.ktlint.core.rule.engine.api.EditorConfigOverride
import io.github.ktlint.core.rule.engine.api.EditorConfigOverride.Companion.EMPTY_EDITOR_CONFIG_OVERRIDE
import io.github.ktlint.core.rule.engine.core.api.RuleV2
import io.github.ktlint.core.rule.engine.core.api.editorconfig.CODE_STYLE_PROPERTY
import io.github.ktlint.core.rule.engine.core.api.editorconfig.END_OF_LINE_PROPERTY
import io.github.ktlint.core.rule.engine.core.api.editorconfig.EXPERIMENTAL_RULES_EXECUTION_PROPERTY
import io.github.ktlint.core.rule.engine.core.api.editorconfig.EditorConfig
import io.github.ktlint.core.rule.engine.core.api.editorconfig.EditorConfigProperty
import io.github.ktlint.core.rule.engine.core.api.editorconfig.INDENT_SIZE_PROPERTY
import io.github.ktlint.core.rule.engine.core.api.editorconfig.ec4j.toPropertyWithValue
import io.github.ktlint.core.rule.engine.internal.FormatterTags.Companion.FORMATTER_TAGS_ENABLED_PROPERTY
import io.github.ktlint.core.rule.engine.internal.FormatterTags.Companion.FORMATTER_TAG_OFF_ENABLED_PROPERTY
import io.github.ktlint.core.rule.engine.internal.FormatterTags.Companion.FORMATTER_TAG_ON_ENABLED_PROPERTY
import io.github.ktlint.core.rule.engine.internal.ThreadSafeEditorConfigCache.Companion.THREAD_SAFE_EDITOR_CONFIG_CACHE
import io.github.oshai.kotlinlogging.KotlinLogging
import org.ec4j.core.EditorConfigLoader
import org.ec4j.core.PropertyTypeRegistry
import org.ec4j.core.Resource
import org.ec4j.core.ResourcePropertiesService
import org.ec4j.core.model.Property
import org.ec4j.core.model.PropertyType
import org.ec4j.core.model.Version
import org.jetbrains.kotlin.utils.addToStdlib.applyIf
import java.nio.charset.StandardCharsets
import java.nio.file.FileSystem
import java.nio.file.FileSystems
import java.nio.file.Path

private val LOGGER = KotlinLogging.logger {}.initKtLintKLogger()

/**
 * Loader for `.editorconfig` properties for files on [fileSystem].
 */
internal class EditorConfigLoader(
    private val fileSystem: FileSystem = FileSystems.getDefault(),
    private val editorConfigLoaderEc4j: EditorConfigLoaderEc4j,
    private val editorConfigDefaults: EditorConfigDefaults = EMPTY_EDITOR_CONFIG_DEFAULTS,
    private val editorConfigOverride: EditorConfigOverride = EMPTY_EDITOR_CONFIG_OVERRIDE,
) {
    /**
     * Loads properties used by [RuleV2]s from the `.editorconfig` file on given [filePath]. When [filePath] is null, the properties for
     * the ".kt" pattern in the current directory are loaded. The '.editorconfig' files on the [filePath] are read starting from the
     * [filePath] upwards until an '.editorconfig' file is found in which the property "root" is found with value "true" or until the root
     * of the filesystem is reached.
     *
     * Properties specified in [editorConfigDefaults] will be used in case the property was not found in any '.editorconfig' on [filePath].
     * If the property is not specified in [editorConfigDefaults] then the default value as specified in the property definition
     * [EditorConfigProperty] is used.
     *
     * Properties specified in [editorConfigOverride] take precedence above any other '.editorconfig' file on [filePath] or default value.
     */
    internal fun load(filePath: Path?): EditorConfig {
        val editorConfigPath = filePath?.toAbsolutePath() ?: defaultFilePath()
        val editorConfigProperties: MutableMap<String, Property> =
            createResourcePropertiesService(editorConfigLoaderEc4j.editorConfigLoader, editorConfigDefaults)
                .queryProperties(editorConfigPath.resource())
                .properties
                .toMutableMap()
        return editorConfigProperties
            .also { properties ->
                if (properties[TAB_WIDTH_PROPERTY_NAME]?.sourceValue == properties[INDENT_SIZE_PROPERTY.name]?.sourceValue &&
                    editorConfigOverride.properties[INDENT_SIZE_PROPERTY] != null
                ) {
                    // The tab_width property can not be overridden via the editorConfigOverride. So if it has been
                    // set to the same value as the indent_size property then keep its value in sync with that
                    // property.
                    properties[TAB_WIDTH_PROPERTY_NAME] =
                        Property
                            .builder()
                            .name(TAB_WIDTH_PROPERTY_NAME)
                            .type(PropertyType.tab_width)
                            .value(editorConfigOverride.properties[INDENT_SIZE_PROPERTY]?.source)
                            .build()
                }
                editorConfigOverride
                    .properties
                    .forEach {
                        properties[it.key.name] = it.key.toPropertyWithValue(it.value)
                    }
            }.also { properties ->
                LOGGER.debug { properties.prettyPrint(filePath) }
            }.let { properties ->
                // Only add properties which are not related to rules but which are needed by the KtLint Rule Engine
                EditorConfig(properties)
                    .addPropertiesWithDefaultValueIfMissing(
                        /*
                         * Used in [EditorConfig] to determine the default value for a property when it is not specified in the
                         * [EditorConfig].
                         */
                        CODE_STYLE_PROPERTY,
                        /*
                         * Used by [KtLintRuleEngine] to use correct line separator when writing the formatted output.
                         */
                        END_OF_LINE_PROPERTY,
                        /*
                         * Used by [VisitorProvider] to determine whether experimental rules have to be executed.
                         */
                        EXPERIMENTAL_RULES_EXECUTION_PROPERTY,
                        /*
                         * Used by [FormatterTags] to determine whether formatter tags should be respected.
                         */
                        FORMATTER_TAGS_ENABLED_PROPERTY,
                        /*
                         * Used by [FormatterTags] to get the tag to disable the formatter.
                         */
                        FORMATTER_TAG_OFF_ENABLED_PROPERTY,
                        /*
                         * Used by [FormatterTags] to get the tag to enable the formatter.
                         */
                        FORMATTER_TAG_ON_ENABLED_PROPERTY,
                    )
            }
    }

    private fun MutableMap<String, Property>.prettyPrint(normalizedFilePath: Path?) =
        map { entry -> "${entry.key}: ${entry.value.sourceValue.lowercase()}" }
            .joinToString(
                prefix =
                    "Effective editorconfig properties${
                        if (normalizedFilePath == null) {
                            ""
                        } else {
                            " for file '$normalizedFilePath'"
                        }
                    }:\n\t",
                separator = "\n\t",
            )

    private fun Path?.resource() = Resource.Resources.ofPath(this, StandardCharsets.UTF_8)

    private fun defaultFilePath() =
        fileSystem
            .getPath(".")
            .toAbsolutePath()
            .resolve(SUPPORTED_FILES.first())

    private fun createResourcePropertiesService(
        editorConfigLoader: EditorConfigLoader,
        editorConfigDefaults: EditorConfigDefaults,
    ) = ResourcePropertiesService
        .builder()
        .keepUnset(true)
        .cache(THREAD_SAFE_EDITOR_CONFIG_CACHE)
        .loader(editorConfigLoader)
        .applyIf(editorConfigDefaults != EMPTY_EDITOR_CONFIG_DEFAULTS) {
            defaultEditorConfigs(editorConfigDefaults.value)
        }.build()

    companion object {
        /**
         * List of file extensions, editorconfig lookup will be performed.
         */
        internal val SUPPORTED_FILES =
            arrayOf(
                ".kt",
                ".kts",
            )

        private const val TAB_WIDTH_PROPERTY_NAME = "tab_width"
    }
}

/**
 * Wrapper around the native EditorConfigLoader of the ec4j library. Whenever Ktlints loads ".editorconfig" files via the ec4j library, it
 * needs to be aware of all native (ec4j) but also the custom property types defined in KtLint and the custom rules sets provided to KtLint.
 *
 * The ec4j library uses the convention that a property type has a name that is identical to the name of that property. If no
 * property type is registered for a property the property will be returned with a null type. When retrieving the property value it will be
 * returned as type "String" which results in class cast exceptions in KtLint's EditorConfig whenever the expected type of the variable is
 * not of type String.
 */
internal class EditorConfigLoaderEc4j(
    private val propertyTypes: Set<PropertyType<*>>,
) {
    val editorConfigLoader: EditorConfigLoader
        get() = EditorConfigLoader.of(Version.CURRENT, propertyTypeRegistry(propertyTypes))

    private fun propertyTypeRegistry(propertyTypes: Set<PropertyType<*>>) =
        PropertyTypeRegistry
            .builder()
            .defaults()
            .apply {
                propertyTypes.forEach { type(it) }
            }.build()
}
