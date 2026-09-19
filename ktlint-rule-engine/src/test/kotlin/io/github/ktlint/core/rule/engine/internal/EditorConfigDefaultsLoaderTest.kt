package io.github.ktlint.core.rule.engine.internal

import assertk.assertThat
import assertk.assertions.isEqualTo
import io.github.ktlint.core.rule.engine.api.EditorConfigDefaults
import io.github.ktlint.core.rule.engine.api.EditorConfigDefaults.Companion.EMPTY_EDITOR_CONFIG_DEFAULTS
import io.github.ktlint.core.test.KtlintTestFileSystem
import org.ec4j.core.model.EditorConfig
import org.ec4j.core.model.Glob
import org.ec4j.core.model.Property
import org.ec4j.core.model.PropertyType
import org.ec4j.core.model.Section
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.DisabledOnOs
import org.junit.jupiter.api.condition.OS
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class EditorConfigDefaultsLoaderTest {
    private val ktlintTestFileSystem = KtlintTestFileSystem()
    private val editorConfigDefaultsLoader =
        EditorConfigDefaultsLoader(
            EditorConfigLoaderEc4j(EC4J_PROPERTY_TYPES_USED_BY_KTLINT),
        )

    @AfterEach
    internal fun tearDown() {
        ktlintTestFileSystem.close()
    }

    @Test
    fun `Given a null path then return empty editor config default`() {
        val actual = editorConfigDefaultsLoader.load(null)

        assertThat(actual).isEqualTo(EMPTY_EDITOR_CONFIG_DEFAULTS)
    }

    @Test
    fun `Given an empty path then return empty editor config default`() {
        val actual =
            editorConfigDefaultsLoader.load(
                ktlintTestFileSystem.resolve(""),
            )

        assertThat(actual).isEqualTo(EMPTY_EDITOR_CONFIG_DEFAULTS)
    }

    @Test
    @DisabledOnOs(OS.WINDOWS) // Filename can not start or end with space
    fun `Given a blank path then return empty editor config default`() {
        val actual =
            editorConfigDefaultsLoader.load(
                ktlintTestFileSystem.resolve("  "),
            )

        assertThat(actual).isEqualTo(EMPTY_EDITOR_CONFIG_DEFAULTS)
    }

    @Test
    fun `Given an non existing path then return empty editor config default`() {
        val actual =
            editorConfigDefaultsLoader.load(
                ktlintTestFileSystem.resolve("/path/to/non/existing/file.kt"),
            )

        assertThat(actual).isEqualTo(EMPTY_EDITOR_CONFIG_DEFAULTS)
    }

    @ParameterizedTest(name = "Filename: {0}")
    @ValueSource(
        strings = [
            ".editorconfig",
            "some-alternative-file-name",
        ],
    )
    fun `Given an existing editor config file then load all settings from it`(fileName: String) {
        val somePathToDirectory = "some/path/to/directory"
        ktlintTestFileSystem.apply {
            writeEditorConfigFile(
                relativeDirectoryToRoot = somePathToDirectory,
                editorConfigFileName = fileName,
                content = SOME_EDITOR_CONFIG.toString(),
            )
        }

        val actual =
            editorConfigDefaultsLoader.load(
                ktlintTestFileSystem.resolve("$somePathToDirectory/$fileName"),
            )

        assertThat(actual).isEqualTo(
            EditorConfigDefaults(SOME_EDITOR_CONFIG),
        )
    }

    @Test
    fun `Given an existing directory containing an editor config file then load all settings from it`() {
        val somePathToDirectory = "some/path/to/directory"
        ktlintTestFileSystem.apply {
            writeEditorConfigFile(somePathToDirectory, SOME_EDITOR_CONFIG.toString())
        }

        val actual =
            editorConfigDefaultsLoader.load(
                ktlintTestFileSystem.resolve(somePathToDirectory),
            )

        assertThat(actual).isEqualTo(
            EditorConfigDefaults(SOME_EDITOR_CONFIG),
        )
    }

    private companion object {
        val SOME_EDITOR_CONFIG: EditorConfig =
            EditorConfig
                .builder()
                .section(
                    Section
                        .builder()
                        .glob(Glob("*.kt"))
                        .properties(
                            Property
                                .builder()
                                .name("some-property")
                                .value("some-property-value"),
                        ),
                ).build()
        val EC4J_PROPERTY_TYPES_USED_BY_KTLINT =
            setOf(
                PropertyType.end_of_line,
                PropertyType.indent_size,
                PropertyType.indent_style,
                PropertyType.insert_final_newline,
                PropertyType.max_line_length,
                PropertyType.tab_width,
            )
    }
}
