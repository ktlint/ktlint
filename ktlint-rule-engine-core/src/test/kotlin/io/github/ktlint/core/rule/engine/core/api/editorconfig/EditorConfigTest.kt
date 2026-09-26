package io.github.ktlint.core.rule.engine.core.api.editorconfig

import assertk.assertFailure
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.hasMessage
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isInstanceOf
import assertk.assertions.isNotNull
import assertk.assertions.isTrue
import assertk.assertions.message
import assertk.assertions.startsWith
import io.github.ktlint.core.rule.engine.api.EditorConfigDefaults
import io.github.ktlint.core.rule.engine.core.api.editorconfig.ec4j.toPropertyWithValue
import io.github.ktlint.core.test.KtlintTestFileSystem
import io.github.ktlint.core.test.assertDoesNotThrow
import org.ec4j.core.model.Property
import org.ec4j.core.model.PropertyType
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

class EditorConfigTest {
    @Test
    fun `Given an EditorConfig from which a non existing property is retrieved then an exception is thrown`() {
        val editorConfig = EditorConfig()

        assertFailure { editorConfig[sampleEditorConfigProperty()] }
            .isInstanceOf<IllegalStateException>()
            .message()
            .isNotNull()
            .startsWith("Property '$SOME_PROPERTY_NAME' can not be retrieved from this EditorConfig.")
    }

    @Test
    fun `Given an EditorConfig from which an existing property is retrieved then return the value of that property`() {
        val editorConfig = EditorConfig(sampleEditorConfigProperty().toPropertyWithValue(SOME_PROPERTY_VALUE))

        val actual = editorConfig[sampleEditorConfigProperty()]

        assertThat(actual).isEqualTo(SOME_PROPERTY_VALUE)
    }

    @Test
    fun `Given an empty EditorConfig and add a property with default value then the default value can be retrieved for the default code style`() {
        val editorConfig = EditorConfig().addPropertiesWithDefaultValueIfMissing(sampleEditorConfigProperty())

        val actual = editorConfig[sampleEditorConfigProperty()]

        assertThat(actual).isEqualTo(SOME_PROPERTY_VALUE_KTLINT_OFFICIAL)
    }

    @ParameterizedTest(name = "Code style: {0}, expected result: {1}")
    @CsvSource(
        value = [
            "android_studio, $SOME_PROPERTY_VALUE_ANDROID_STUDIO",
            "intellij_idea, $SOME_PROPERTY_VALUE_INTELLIJ_IDEA",
            "ktlint_official, $SOME_PROPERTY_VALUE_KTLINT_OFFICIAL",
        ],
    )
    fun `Given an EditorConfig with a defined code style and add a property with default value then the default value can be retrieved for the default code style`(
        codeStyleValue: CodeStyleValue,
        expectedValue: String,
    ) {
        val editorConfig =
            EditorConfig(CODE_STYLE_PROPERTY.toPropertyWithValue(codeStyleValue.name))
                .addPropertiesWithDefaultValueIfMissing(sampleEditorConfigProperty())

        val actual = editorConfig[sampleEditorConfigProperty()]

        assertThat(actual).isEqualTo(expectedValue)
    }

    @Test
    fun `Given an EditorConfig containing a certain property with a non-default value and add the same property again with default value then the non-default value is not overwritten`() {
        val editorConfig =
            EditorConfig(sampleEditorConfigProperty().toPropertyWithValue(SOME_PROPERTY_VALUE))
                .addPropertiesWithDefaultValueIfMissing(sampleEditorConfigProperty())

        val actual = editorConfig[sampleEditorConfigProperty()]

        assertThat(actual).isEqualTo(SOME_PROPERTY_VALUE)
    }

    @Test
    fun `Given an EditorConfig from which a deprecated -error-level- property is retrieved then thrown an exception`() {
        val someDeprecationMessage = "some-deprecation-message"
        val someDeprecatedEditorConfigProperty = sampleEditorConfigProperty(deprecationError = someDeprecationMessage)
        val editorConfig = EditorConfig().addPropertiesWithDefaultValueIfMissing(someDeprecatedEditorConfigProperty)

        assertFailure { editorConfig[someDeprecatedEditorConfigProperty] }
            .hasMessage("Property '$SOME_PROPERTY_NAME' is disallowed: $someDeprecationMessage")
    }

    @Test
    fun `Given an EditorConfig from which a deprecated -warning-level- property is retrieved then do not throw an exception`() {
        val someDeprecatedEditorConfigProperty = sampleEditorConfigProperty(deprecationWarning = "some-deprecation-message")
        val editorConfig = EditorConfig().addPropertiesWithDefaultValueIfMissing(someDeprecatedEditorConfigProperty)

        assertDoesNotThrow { editorConfig[sampleEditorConfigProperty()] }
    }

    @Test
    fun `Given an EditorConfig containing a property then 'contains' returns true when that property is retrieved`() {
        val editorConfig = EditorConfig(sampleEditorConfigProperty().toPropertyWithValue(SOME_PROPERTY_VALUE))

        val actual = editorConfig.contains(sampleEditorConfigProperty().name)

        assertThat(actual).isTrue()
    }

    @Test
    fun `Given an EditorConfig then 'contains' returns false when a non-existent property is retrieved`() {
        val editorConfig = EditorConfig()

        val actual = editorConfig.contains(sampleEditorConfigProperty().name)

        assertThat(actual).isFalse()
    }

    @Test
    fun `Given an EditorConfig containing some properties then mapping of all properties is possible`() {
        val propertyName1 = "property-1"
        val propertyName2 = "property-2"
        val propertyValue1 = "value-1"
        val propertyValue2 = "value-1"
        val editorConfig =
            EditorConfig()
                .addPropertiesWithDefaultValueIfMissing(
                    sampleEditorConfigProperty(name = propertyName1, ktlintOfficialCodeStyleDefaultValue = propertyValue1),
                    sampleEditorConfigProperty(name = propertyName2, ktlintOfficialCodeStyleDefaultValue = propertyValue2),
                )

        val actual =
            editorConfig
                .map { property -> property.name.uppercase() to property.sourceValue.uppercase() }
                .toList()

        assertThat(actual).containsExactly(
            propertyName1.uppercase() to propertyValue1.uppercase(),
            propertyName2.uppercase() to propertyValue2.uppercase(),
        )
    }

    @Test
    fun `Given an EditorConfig to which properties are added with the same name but different identities than those properties can not be loaded in the same EditorConfig`() {
        assertFailure {
            EditorConfig()
                .addPropertiesWithDefaultValueIfMissing(
                    sampleEditorConfigProperty(defaultValue = SOME_PROPERTY_VALUE_ANDROID_STUDIO),
                    sampleEditorConfigProperty(defaultValue = SOME_PROPERTY_VALUE_INTELLIJ_IDEA),
                )
        }.isInstanceOf<IllegalArgumentException>()
            .message()
            .isNotNull()
            .startsWith("Found multiple editorconfig properties with name '$SOME_PROPERTY_NAME' but having distinct identities:")
    }

    @Test
    fun `Given two editorconfig properties with the same name but different identities than those properties can not be loaded in the same EditorConfig`() {
        assertFailure {
            EditorConfig()
                .filterBy(
                    setOf(
                        sampleEditorConfigProperty(defaultValue = SOME_PROPERTY_VALUE_ANDROID_STUDIO),
                        sampleEditorConfigProperty(defaultValue = SOME_PROPERTY_VALUE_INTELLIJ_IDEA),
                    ),
                )
        }.isInstanceOf<IllegalArgumentException>()
            .message()
            .isNotNull()
            .startsWith("Found multiple editorconfig properties with name '$SOME_PROPERTY_NAME' but having distinct identities:")
    }

    @Test
    fun `Given an editorconfig containing a property and a filterBy for a property with the same name is given but with different identity then the existing property is not overwritten`() {
        val editorConfig =
            EditorConfig(sampleEditorConfigProperty().toPropertyWithValue(SOME_PROPERTY_VALUE_ANDROID_STUDIO))
                .filterBy(
                    setOf(sampleEditorConfigProperty(defaultValue = SOME_PROPERTY_VALUE_INTELLIJ_IDEA)),
                )

        val actual = editorConfig[sampleEditorConfigProperty()]

        assertThat(actual).isEqualTo(SOME_PROPERTY_VALUE_ANDROID_STUDIO)
    }

    @Test
    fun `Given an editorconfig containing a property for which the name of the property is not identical to the name of the property type then the property can be retrieved by name and type combination`() {
        val property1 = "property-1"
        val property2 = "property-2"
        val editorConfig =
            EditorConfig(
                sampleEditorConfigProperty(name = property1)
                    .toPropertyWithValue(SOME_PROPERTY_VALUE_ANDROID_STUDIO),
                sampleEditorConfigProperty(name = property2)
                    .toPropertyWithValue(SOME_PROPERTY_VALUE_INTELLIJ_IDEA),
            )

        val actual = editorConfig.getEditorConfigValueOrNull(sampleEditorConfigProperty().type, property2)

        assertThat(actual).isEqualTo(SOME_PROPERTY_VALUE_INTELLIJ_IDEA)
    }

    @Test
    fun `Given an editorconfig containing a property for which a property mapper is defined then the property mapper is called`() {
        val editorConfigPropertyWithPropertyMapper =
            sampleEditorConfigProperty(
                propertyMapper = { _, _ -> SOME_PROPERTY_VALUE_KTLINT_OFFICIAL },
                defaultValue = SOME_PROPERTY_VALUE,
                androidStudioCodeStyleDefaultValue = null,
                intellijIdeaCodeStyleDefaultValue = null,
                ktlintOfficialCodeStyleDefaultValue = null,
            )
        val editorConfig = EditorConfig().addPropertiesWithDefaultValueIfMissing(editorConfigPropertyWithPropertyMapper)

        val actual = editorConfig[editorConfigPropertyWithPropertyMapper]

        assertThat(actual).isEqualTo(SOME_PROPERTY_VALUE_KTLINT_OFFICIAL)
    }

    @Test
    fun `Given an editorconfig containing a property for which the value is unset then return its default value`() {
        val editorConfig =
            EditorConfig(
                sampleEditorConfigProperty().toPropertyWithValue("unset"),
            )

        val actual = editorConfig[sampleEditorConfigProperty()]

        assertThat(actual).isEqualTo(SOME_PROPERTY_VALUE_KTLINT_OFFICIAL)
    }

    @Test
    fun `Given an editorconfig containing a property with an invalid source value then return its default value`() {
        val someEditorConfigProperty = editorConfigProperty("some-editor-config-property")
        val editorConfig =
            EditorConfig(
                someEditorConfigProperty.toPropertyWithValue("some-invalid-value"),
            )

        val actual = editorConfig[someEditorConfigProperty]

        assertThat(actual).isEqualTo(someEditorConfigProperty.defaultValue)
    }

    @Test
    fun `Given an editorconfig containing a property with undefined type then retrieving that property via the EditConfig may not result in an exception`() {
        // The Property and PropertyValue builders of the ec4j library do not allow to build a property having field "Property.type" set to
        // null. However, when loading an ".editorconfig" file containing a property with a name for which no property type exists with that
        // same name, this does result in such a property. It takes a detour to create the property:
        //  - Create an ".editorconfig" file on the file system mock
        //  - Load the ".editorconfig" via the EditorConfigDefaults loader
        //  - Find the property which is set in the ".editorconfig" file and validate that its type is null
        //  - Transform the property to a KtLint EditorConfig
        //  - Extract the property of the KtLint EditorConfig
        // Note that two properties are created which are identical except that only one of the properties has a null type and the other
        // property has a correctly defined type.
        //language=
        val ktlintTestRuleExecutionProperty1 = "ktlint_test_rule-1"
        val ktlintTestRuleExecutionPropertyType1 = editorConfigProperty(ktlintTestRuleExecutionProperty1)
        //language=
        val ktlintTestRuleExecutionProperty2 = "ktlint_test_rule-2"
        val ktlintTestRuleExecutionPropertyType2 = editorConfigProperty(ktlintTestRuleExecutionProperty2)

        val ktlintTestFileSystem =
            KtlintTestFileSystem().apply {
                writeRootEditorConfigFile(
                    //language=EditorConfig
                    """
                    [*.{kt,kts}]
                    $ktlintTestRuleExecutionProperty1 = disabled
                    $ktlintTestRuleExecutionProperty2 = disabled
                    """.trimIndent(),
                )
            }

        val ktlintTestRuleProperties =
            EditorConfigDefaults
                .load(
                    path = ktlintTestFileSystem.resolve(""),
                    propertyTypes =
                        setOf(
                            // Note that ktlintTestRuleEditorConfigPropertyType1 has been left out on purpose
                            ktlintTestRuleExecutionPropertyType2.type,
                        ),
                ).value
                .sections
                .flatMap { it.properties.values }
                .associateBy { it.name }

        // Validate that both properties are loaded
        require(ktlintTestRuleProperties[ktlintTestRuleExecutionProperty1] != null) {
            "Can not find property '$ktlintTestRuleExecutionProperty1' in the '.editorconfig' file on the file system mock"
        }
        require(ktlintTestRuleProperties[ktlintTestRuleExecutionProperty2] != null) {
            "Can not find property '$ktlintTestRuleExecutionProperty2' in the '.editorconfig' file on the file system mock"
        }

        // Validate that the type of one of the properties is null
        require(ktlintTestRuleProperties[ktlintTestRuleExecutionProperty1]?.type == null) {
            "Property '$ktlintTestRuleExecutionProperty1' should have an undefined type"
        }
        require(ktlintTestRuleProperties[ktlintTestRuleExecutionProperty2]?.type != null) {
            "Property '$ktlintTestRuleExecutionProperty2' should have a defined type"
        }

        val editorConfig = EditorConfig(ktlintTestRuleProperties)

        // Although the type of one of the properties is null, both can be loaded as the type of EditorConfigProperty is used to parse the
        // raw value of the property
        assertThat(editorConfig[ktlintTestRuleExecutionPropertyType1]).isEqualTo(RuleExecution.disabled)
        assertThat(editorConfig[ktlintTestRuleExecutionPropertyType2]).isEqualTo(RuleExecution.disabled)
    }

    private fun editorConfigProperty(name: String) =
        EditorConfigProperty(
            name = name,
            type =
                PropertyType.LowerCasingPropertyType(
                    name,
                    "",
                    SafeEnumValueParser(RuleExecution::class.java),
                    RuleExecution.entries.map { it.name }.toSet(),
                ),
            defaultValue = RuleExecution.enabled,
        )

    private companion object {
        const val SOME_PROPERTY_NAME = "some-property-name"
        const val SOME_PROPERTY_VALUE = "some-property-value"
        const val SOME_PROPERTY_VALUE_ANDROID_STUDIO = "some-property-value-android"
        const val SOME_PROPERTY_VALUE_DEFAULT = "some-property-value-default"
        const val SOME_PROPERTY_VALUE_INTELLIJ_IDEA = "some-property-value-intellij-idea"
        const val SOME_PROPERTY_VALUE_KTLINT_OFFICIAL = "some-property-value-ktlint-official"

        fun sampleEditorConfigProperty(
            name: String = SOME_PROPERTY_NAME,
            defaultValue: String = SOME_PROPERTY_VALUE_DEFAULT,
            androidStudioCodeStyleDefaultValue: String? = SOME_PROPERTY_VALUE_ANDROID_STUDIO,
            ktlintOfficialCodeStyleDefaultValue: String? = SOME_PROPERTY_VALUE_KTLINT_OFFICIAL,
            intellijIdeaCodeStyleDefaultValue: String? = SOME_PROPERTY_VALUE_INTELLIJ_IDEA,
            deprecationError: String? = null,
            deprecationWarning: String? = null,
            propertyMapper: ((Property?, CodeStyleValue) -> String?)? = null,
        ) = EditorConfigProperty(
            name = name,
            type =
                PropertyType(
                    name,
                    "",
                    PropertyType.PropertyValueParser.IDENTITY_VALUE_PARSER,
                    setOf(SOME_PROPERTY_VALUE_ANDROID_STUDIO, SOME_PROPERTY_VALUE_INTELLIJ_IDEA),
                ),
            defaultValue = defaultValue,
            androidStudioCodeStyleDefaultValue = androidStudioCodeStyleDefaultValue,
            ktlintOfficialCodeStyleDefaultValue = ktlintOfficialCodeStyleDefaultValue,
            intellijIdeaCodeStyleDefaultValue = intellijIdeaCodeStyleDefaultValue,
            deprecationError = deprecationError,
            deprecationWarning = deprecationWarning,
            propertyMapper = propertyMapper,
        )
    }
}
