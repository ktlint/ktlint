package io.github.ktlint.core.rule.engine.internal

import io.github.ktlint.core.rule.engine.api.EditorConfigOverride
import io.github.ktlint.core.rule.engine.core.api.RuleV2
import io.github.ktlint.core.rule.engine.core.api.editorconfig.CODE_STYLE_PROPERTY
import io.github.ktlint.core.rule.engine.core.api.editorconfig.CodeStyleValue
import io.github.ktlint.core.rule.engine.core.api.editorconfig.EditorConfig
import java.nio.file.FileSystem
import java.nio.file.FileSystems
import java.nio.file.Path

/**
 * Generates a Kotlin section content for the `.editorconfig` file. The set of properties is based on the properties used by the given set
 * of [RuleV2]s. The default values for missing properties are based on the given [CodeStyleValue].
 */
internal class EditorConfigGenerator(
    private val fileSystem: FileSystem = FileSystems.getDefault(),
    private val editorConfigLoaderEc4j: EditorConfigLoaderEc4j,
) {
    /**
     * Method loads merged `.editorconfig` content using [filePath], and then, by querying the set of [RuleV2]s generates Kotlin section
     * (default is `[*.{kt,kts}]`) content including expected default values.
     *
     * @return Kotlin section editorconfig content. For example:
     * ```properties
     * final-newline = true
     * indent-size = 4
     * ```
     */
    fun generateEditorconfig(
        rules: Set<RuleV2>,
        codeStyle: CodeStyleValue,
        filePath: Path,
    ): String =
        editorConfig(rules, codeStyle, filePath)
            .map { "${it.name} = ${it.sourceValue.lowercase()}" }
            .distinct()
            .sorted()
            .joinToString(separator = System.lineSeparator())

    private fun editorConfig(
        rules: Set<RuleV2>,
        codeStyle: CodeStyleValue,
        filePath: Path,
    ): EditorConfig {
        val usedEditorConfigProperties =
            rules
                .flatMap { it.usesEditorConfigProperties }
                .toSet()
        return loadEditorConfig(codeStyle, filePath)
            .addPropertiesWithDefaultValueIfMissing(
                *usedEditorConfigProperties
                    .plus(DEFAULT_EDITOR_CONFIG_PROPERTIES)
                    .toTypedArray(),
            )
    }

    private fun loadEditorConfig(
        codeStyle: CodeStyleValue,
        filePath: Path,
    ) = EditorConfigLoader(
        fileSystem = fileSystem,
        editorConfigLoaderEc4j = editorConfigLoaderEc4j,
        editorConfigOverride = EditorConfigOverride.from(CODE_STYLE_PROPERTY to codeStyle.name),
    ).load(filePath = filePath)
}
