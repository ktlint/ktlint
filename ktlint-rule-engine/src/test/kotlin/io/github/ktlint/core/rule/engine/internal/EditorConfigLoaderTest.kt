package io.github.ktlint.core.rule.engine.internal

import assertk.all
import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.containsAtLeast
import assertk.assertions.containsExactlyInAnyOrder
import assertk.assertions.doesNotContain
import io.github.ktlint.core.rule.engine.api.EditorConfigDefaults
import io.github.ktlint.core.rule.engine.api.EditorConfigOverride
import io.github.ktlint.core.rule.engine.core.api.editorconfig.EditorConfig
import io.github.ktlint.core.rule.engine.core.api.editorconfig.EditorConfigProperty
import io.github.ktlint.core.rule.engine.core.api.editorconfig.INDENT_SIZE_PROPERTY
import io.github.ktlint.core.rule.engine.core.api.editorconfig.INSERT_FINAL_NEWLINE_PROPERTY
import io.github.ktlint.core.test.KtlintTestFileSystem
import org.ec4j.core.model.PropertyType
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

@Suppress("EditorConfigKeyCorrectness")
internal class EditorConfigLoaderTest {
    private val ktlintTestFileSystem = KtlintTestFileSystem()

    @AfterEach
    fun tearDown() {
        ktlintTestFileSystem.close()
    }

    @ParameterizedTest
    @ValueSource(
        strings = [
            //language=EditorConfig
            """
            [*]
            indent_size = 2
            """,
            //language=EditorConfig
            """
            root = true
            [*]
            indent_size = 2
            """,
            // Note, when multiple globs matches with the file being scanned then the value from the last glob wins
            //language=EditorConfig
            """
            [*]
            indent_size = 4
            [*.{kt,kts}]
            indent_size = 2
            """,
            // Note, when multiple globs matches with the file being scanned then the value from the last glob wins
            //language=EditorConfig
            """
            [*.{kt,kts}]
            indent_size = 4
            [*]
            indent_size = 2
            """,
        ],
    )
    fun `Given an editorconfig file in the project root and the file to be linted is in a subdirectory not containing an editorconfig file`(
        editorconfigContent: String,
    ) {
        ktlintTestFileSystem.apply {
            writeRootEditorConfigFile(editorconfigContent.trimIndent())
        }

        createEditorConfigLoader()
            .load(ktlintTestFileSystem.resolve("some-subdirectory/test.kt"))
            .run {
                assertThat(convertToPropertyValues()).contains("indent_size = 2")
            }
    }

    @Test
    fun `Given editorconfig files at different levels in the project hierarchy and an intermediate editorconfig file contains the root is true property then stop reading the parent editorconfig`() {
        val someRelativeProjectDirectory = "some-project-directory"
        val someRelativeProjectSubDirectory = "$someRelativeProjectDirectory/some-project-sub-directory"

        ktlintTestFileSystem.apply {
            writeRootEditorConfigFile(
                //language=EditorConfig
                """
                root = true
                [*]
                some_property_1 = some_value_1
                """.trimIndent(),
            )

            writeEditorConfigFile(
                someRelativeProjectDirectory,
                //language=EditorConfig
                """
                root = true
                [*.{kt,kts}]
                some_property_2 = some_value_2
                """.trimIndent(),
            )

            writeEditorConfigFile(
                someRelativeProjectSubDirectory,
                //language=EditorConfig
                """
                [*]
                indent_size = 2
                some_property_3 = some_value_3
                """.trimIndent(),
            )
        }

        createEditorConfigLoader()
            .load(ktlintTestFileSystem.resolve("$someRelativeProjectSubDirectory/test.kt"))
            .run {
                assertThat(convertToPropertyValues()).all {
                    containsAtLeast(
                        "some_property_2 = some_value_2",
                        "some_property_3 = some_value_3",
                    )
                    doesNotContain(
                        // The '.editorconfig' file in the root directory is skipped because the property "root = true" is
                        // found
                        "some_property_1 = some_value_1",
                    )
                }
            }

        createEditorConfigLoader()
            .load(ktlintTestFileSystem.resolve("$someRelativeProjectDirectory/test.kt"))
            .run {
                assertThat(convertToPropertyValues()).all {
                    contains("some_property_2 = some_value_2")
                    doesNotContain(
                        // The '.editorconfig' file in the root directory is skipped because the property "root = true" is
                        // found
                        "some_property_1 = some_value_1",
                    )
                }
            }

        createEditorConfigLoader()
            .load(ktlintTestFileSystem.resolve("test.kt"))
            .run {
                assertThat(convertToPropertyValues())
                    .contains("some_property_1 = some_value_1")
            }
    }

    @Test
    fun `Should parse properties with and without spaces before or after the =`() {
        ktlintTestFileSystem.apply {
            writeRootEditorConfigFile(
                //language=EditorConfig
                """
                [*.{kt,kts}]
                some_property_1 = some_value_1
                some_property_2= some_value_2
                some_property_3 =some_value_3
                some_property_4=some_value_4
                """.trimIndent(),
            )
        }

        createEditorConfigLoader()
            .load(ktlintTestFileSystem.resolve("test.kt"))
            .run {
                assertThat(convertToPropertyValues())
                    .containsAtLeast(
                        "some_property_1 = some_value_1",
                        "some_property_2 = some_value_2",
                        "some_property_3 = some_value_3",
                        "some_property_4 = some_value_4",
                    )
            }
    }

    @Test
    fun `Should parse unset values`() {
        ktlintTestFileSystem.apply {
            writeRootEditorConfigFile(
                //language=EditorConfig
                """
                [*.{kt,kts}]
                some_property_1 = unset
                """.trimIndent(),
            )
        }

        createEditorConfigLoader()
            .load(ktlintTestFileSystem.resolve("test.kt"))
            .run {
                assertThat(convertToPropertyValues())
                    .contains("some_property_1 = unset")
            }
    }

    @Test
    fun `Given a property with a comma separated list of values, with or without spaces around the comma the return the value inclusive those spaces`() {
        ktlintTestFileSystem.apply {
            writeRootEditorConfigFile(
                //language=EditorConfig
                """
                [*.{kt,kts}]
                some_property_1=some_value_1,some_value_2
                some_property_2=some_value_1 ,some_value_2
                some_property_3=some_value_1, some_value_2
                some_property_4=some_value_1 , some_value_2
                """.trimIndent(),
            )
        }

        createEditorConfigLoader()
            .load(ktlintTestFileSystem.resolve("test.kt"))
            .run {
                assertThat(convertToPropertyValues())
                    .containsAtLeast(
                        "some_property_1 = some_value_1,some_value_2",
                        "some_property_2 = some_value_1 ,some_value_2",
                        "some_property_3 = some_value_1, some_value_2",
                        "some_property_4 = some_value_1 , some_value_2",
                    )
            }
    }

    @Nested
    inner class `Given a null file path` {
        @Test
        fun `Given no default and no override properties then return only the properties required for internal processing by KtLint`() {
            createEditorConfigLoader()
                .load(null)
                .run {
                    assertThat(convertToPropertyValues())
                        .containsExactlyInAnyOrder(
                            "end_of_line = lf",
                            "ij_formatter_tags_enabled = false",
                            "ij_formatter_off_tag = @formatter:off",
                            "ij_formatter_on_tag = @formatter:on",
                            "ktlint_code_style = ktlint_official",
                            "ktlint_experimental = disabled",
                        )
                }
        }

        @Test
        fun `Given some override properties then return the override properties`() {
            val editorConfigOverride = EditorConfigOverride.from(INSERT_FINAL_NEWLINE_PROPERTY to "true")
            createEditorConfigLoader(editorConfigOverride = editorConfigOverride)
                .load(null)
                .run {
                    assertThat(convertToPropertyValues())
                        .contains("insert_final_newline = true")
                }
        }

        @Test
        fun `Given some properties in an editorconfig file then return the properties from this file`() {
            ktlintTestFileSystem.apply {
                writeRootEditorConfigFile(
                    //language=EditorConfig
                    """
                    [*.{kt,kts}]
                    some_property = some_value
                    """.trimIndent(),
                )
            }

            createEditorConfigLoader()
                .load(ktlintTestFileSystem.resolve(".kt"))
                .run {
                    assertThat(convertToPropertyValues())
                        .contains("some_property = some_value")
                }
        }

        @Test
        fun `Given some properties in an editorconfig file which is passed in as default editorconfig then return the properties from this file`() {
            ktlintTestFileSystem.apply {
                writeEditorConfigFile(
                    "some/dir",
                    //language=EditorConfig
                    """
                    [*.{kt,kts}]
                    some_property = some_value
                    """.trimIndent(),
                )
            }

            val editorConfigDefaults =
                EditorConfigDefaults.load(
                    path = ktlintTestFileSystem.resolve("some/dir/.editorconfig"),
                    propertyTypes = setOf(SOME_EDITOR_CONFIG_PROPERTY.type),
                )
            createEditorConfigLoader(editorConfigDefaults)
                .load(ktlintTestFileSystem.resolve(".kt"))
                .run {
                    assertThat(convertToPropertyValues())
                        .contains("some_property = some_value")
                }
        }
    }

    @Nested
    inner class `Given a file path with a non kotlin extension` {
        @Test
        fun `Given no default and no override properties then return only the properties required for internal processing by KtLint`() {
            createEditorConfigLoader()
                .load(ktlintTestFileSystem.resolve("test.java"))
                .run {
                    assertThat(convertToPropertyValues())
                        .containsExactlyInAnyOrder(
                            "end_of_line = lf",
                            "ij_formatter_tags_enabled = false",
                            "ij_formatter_off_tag = @formatter:off",
                            "ij_formatter_on_tag = @formatter:on",
                            "ktlint_code_style = ktlint_official",
                            "ktlint_experimental = disabled",
                        )
                }
        }

        @Test
        fun `Given some override properties then return the override properties`() {
            val editorConfigOverride = EditorConfigOverride.from(INSERT_FINAL_NEWLINE_PROPERTY to "true")
            createEditorConfigLoader(editorConfigOverride = editorConfigOverride)
                .load(ktlintTestFileSystem.resolve("test.java"))
                .run {
                    assertThat(convertToPropertyValues())
                        .contains("insert_final_newline = true")
                }
        }

        @Test
        fun `Given some kotlin properties in an editorconfig file then do not return the properties from this file`() {
            ktlintTestFileSystem.apply {
                writeRootEditorConfigFile(
                    //language=EditorConfig
                    """
                    [*.{kt,kts}]
                    some_property = some_value
                    """.trimIndent(),
                )
            }

            createEditorConfigLoader()
                .load(ktlintTestFileSystem.resolve("test.java"))
                .run {
                    assertThat(convertToPropertyValues())
                        .doesNotContain("some_property = some_value")
                }
        }
    }

    @Nested
    inner class `Given input from stdin` {
        @Test
        fun `Given no default and no override properties then return only the properties required for internal processing by KtLint`() {
            createEditorConfigLoader()
                .load(ktlintTestFileSystem.resolve(".kt"))
                .run {
                    assertThat(convertToPropertyValues())
                        .containsExactlyInAnyOrder(
                            "end_of_line = lf",
                            "ij_formatter_tags_enabled = false",
                            "ij_formatter_off_tag = @formatter:off",
                            "ij_formatter_on_tag = @formatter:on",
                            "ktlint_code_style = ktlint_official",
                            "ktlint_experimental = disabled",
                        )
                }
        }

        @Test
        fun `Given some override properties then return the override properties`() {
            ktlintTestFileSystem.apply {
                writeRootEditorConfigFile(
                    //language=EditorConfig
                    """
                    [*.{kt,kts}]
                    some_property_1 = some_value_1
                    """.trimIndent(),
                )
            }

            val editorConfigOverride = EditorConfigOverride.from(INSERT_FINAL_NEWLINE_PROPERTY to true)
            createEditorConfigLoader(editorConfigOverride = editorConfigOverride)
                .load(ktlintTestFileSystem.resolve(".kt"))
                .run {
                    assertThat(convertToPropertyValues())
                        .contains("insert_final_newline = true")
                }
        }

        @Test
        fun `Given some properties in an editorconfig file then return the properties from this file`() {
            ktlintTestFileSystem.apply {
                writeRootEditorConfigFile(
                    //language=EditorConfig
                    """
                    [*.{kt,kts}]
                    some_property = some_value
                    """.trimIndent(),
                )
            }

            createEditorConfigLoader()
                .load(ktlintTestFileSystem.resolve(".kt"))
                .run {
                    assertThat(convertToPropertyValues())
                        .contains("some_property = some_value")
                }
        }
    }

    @Test
    fun `Given a project with editorconfig properties (root=true) and override properties then ignore properties from root dir but apply the override properties`() {
        val someRelativeProjectDirectory = "some-project-directory"

        ktlintTestFileSystem.apply {
            writeRootEditorConfigFile(
                //language=EditorConfig
                """
                root = true
                [*]
                some_property_1 = some_value_1
                """.trimIndent(),
            )

            writeEditorConfigFile(
                someRelativeProjectDirectory,
                //language=EditorConfig
                """
                root = true
                [*.{kt,kts}]
                some_property_1 = some_value_2
                some_property_2 = some_value_2
                indent_size = 4
                """.trimIndent(),
            )
        }

        val editorConfigOverride = EditorConfigOverride.from(INDENT_SIZE_PROPERTY to 2)
        createEditorConfigLoader(editorConfigOverride = editorConfigOverride)
            .load(ktlintTestFileSystem.resolve("$someRelativeProjectDirectory/test.kt"))
            .run {
                assertThat(convertToPropertyValues())
                    .containsAtLeast(
                        "some_property_1 = some_value_2",
                        "some_property_2 = some_value_2",
                        "indent_size = 2",
                    )
            }
    }

    @Test
    fun `Given that code is loaded via stdin then load properties from override and properties required for internal processing of KtLint`() {
        ktlintTestFileSystem.apply {
            writeRootEditorConfigFile(
                //language=EditorConfig
                """
                root = true
                [*]
                ${SOME_EDITOR_CONFIG_PROPERTY.name} = $SOME_PROPERTY_VALUE_1
                """.trimIndent(),
            )
        }

        val editorConfigOverride = EditorConfigOverride.from(SOME_EDITOR_CONFIG_PROPERTY to SOME_PROPERTY_VALUE_2)
        createEditorConfigLoader(editorConfigOverride = editorConfigOverride)
            .load(null)
            .run {
                assertThat(convertToPropertyValues())
                    .contains("${SOME_EDITOR_CONFIG_PROPERTY.name} = $SOME_PROPERTY_VALUE_2")
            }
    }

    @Test
    fun `Given that the indent_size and tab_width property have same value and the indent_size is changed in the override properties then also keep the tab_width property in sync with the indent_size`() {
        ktlintTestFileSystem.apply {
            writeRootEditorConfigFile(
                //language=EditorConfig
                """
                root = true
                [*]
                indent_size = 5
                tab_width = 5
                """.trimIndent(),
            )
        }

        val editorConfigOverride = EditorConfigOverride.from(INDENT_SIZE_PROPERTY to 3)
        createEditorConfigLoader(editorConfigOverride = editorConfigOverride)
            .load(ktlintTestFileSystem.resolve("test.kt"))
            .run {
                assertThat(convertToPropertyValues())
                    .containsAtLeast(
                        "indent_size = 3",
                        "tab_width = 3",
                    )
            }
    }

    @Test
    fun `Should support editorconfig globs when loading properties for file specified under such glob`() {
        ktlintTestFileSystem.apply {
            writeRootEditorConfigFile(
                //language=EditorConfig
                """
                [*.{kt,kts}]
                some_property_1 = some_value_1
                some_property_2 = some_value_2

                [api/*.{kt,kts}]
                some_property_1 = some_value_2
                some_property_3 = some_value_3
                """.trimIndent(),
            )
        }

        createEditorConfigLoader()
            .load(ktlintTestFileSystem.resolve("api/test.kt"))
            .run {
                assertThat(convertToPropertyValues())
                    .containsAtLeast(
                        "some_property_1 = some_value_2",
                        "some_property_2 = some_value_2",
                        "some_property_3 = some_value_3",
                    )
            }
    }

    @Test
    fun `Should add property from override`() {
        ktlintTestFileSystem.apply {
            writeRootEditorConfigFile(
                //language=EditorConfig
                """
                [*.{kt,kts}]
                some_property_1 = some_value_1
                """.trimIndent(),
            )
        }

        val editorConfigDefaults = EditorConfigDefaults.EMPTY_EDITOR_CONFIG_DEFAULTS
        val editorConfigOverride = EditorConfigOverride.from(INSERT_FINAL_NEWLINE_PROPERTY to "true")
        createEditorConfigLoader(editorConfigDefaults, editorConfigOverride)
            .load(ktlintTestFileSystem.resolve("test.kt"))
            .run {
                assertThat(convertToPropertyValues())
                    .containsAtLeast(
                        "some_property_1 = some_value_1",
                        "insert_final_newline = true",
                    )
            }
    }

    @Test
    fun `Should replace property from override`() {
        ktlintTestFileSystem.apply {
            writeRootEditorConfigFile(
                //language=EditorConfig
                """
                [*.{kt,kts}]
                insert_final_newline = true
                """.trimIndent(),
            )
        }

        val editorConfigDefaults = EditorConfigDefaults.EMPTY_EDITOR_CONFIG_DEFAULTS
        val editorConfigOverride = EditorConfigOverride.from(INSERT_FINAL_NEWLINE_PROPERTY to "false")
        createEditorConfigLoader(editorConfigDefaults, editorConfigOverride)
            .load(ktlintTestFileSystem.resolve("test.kt"))
            .run {
                assertThat(convertToPropertyValues())
                    .contains("insert_final_newline = false")
            }
    }

    private fun createEditorConfigLoader(
        editorConfigDefaults: EditorConfigDefaults = EditorConfigDefaults.EMPTY_EDITOR_CONFIG_DEFAULTS,
        editorConfigOverride: EditorConfigOverride = EditorConfigOverride.EMPTY_EDITOR_CONFIG_OVERRIDE,
    ) = EditorConfigLoader(
        fileSystem = ktlintTestFileSystem.fileSystem,
        editorConfigLoaderEc4j = EditorConfigLoaderEc4j(emptySet()),
        editorConfigDefaults = editorConfigDefaults,
        editorConfigOverride = editorConfigOverride,
    )

    private fun EditorConfig.convertToPropertyValues(): List<String> =
        map {
            val value =
                if (it.isUnset) {
                    "unset"
                } else {
                    it.sourceValue
                }
            "${it.name} = $value"
        }.toList()

    private companion object {
        const val SOME_PROPERTY_NAME = "some-property-name"

        //language=
        const val SOME_PROPERTY_VALUE_1 = "some-property-value-1"
        const val SOME_PROPERTY_VALUE_2 = "some-property-value-2"
        val SOME_EDITOR_CONFIG_PROPERTY =
            EditorConfigProperty(
                name = SOME_PROPERTY_NAME,
                type =
                    PropertyType(
                        SOME_PROPERTY_NAME,
                        "",
                        PropertyType.PropertyValueParser.IDENTITY_VALUE_PARSER,
                        setOf(SOME_PROPERTY_VALUE_1, SOME_PROPERTY_VALUE_2),
                    ),
                defaultValue = SOME_PROPERTY_VALUE_1,
            )
    }
}
