package io.github.ktlint.core.rule.engine.core.api.editorconfig

import assertk.assertThat
import assertk.assertions.isEqualTo
import io.github.ktlint.core.rule.engine.core.api.editorconfig.ec4j.toPropertyWithValue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.EnumSource

class MaxLineLengthEditorConfigPropertyTest {
    @Nested
    inner class PropertyMapper {
        val maxLineLengthPropertyMapper = MAX_LINE_LENGTH_PROPERTY.propertyMapper!!

        @ParameterizedTest(name = "Code style: {0}, default value: {1}")
        @CsvSource(
            value = [
                "android_studio, 100",
                "intellij_idea, ${Int.MAX_VALUE}",
                "ktlint_official, 140",
            ],
        )
        fun `Given a null property then the property mapper returns the default value of the code style`(
            codeStyleValue: CodeStyleValue,
            expectedDefaultValue: Int,
        ) {
            val actual = maxLineLengthPropertyMapper(null, codeStyleValue)

            assertThat(actual).isEqualTo(expectedDefaultValue)
        }

        @ParameterizedTest(name = "Code style: {0}, default value: {1}")
        @CsvSource(
            value = [
                "android_studio, 100",
                "intellij_idea, ${Int.MAX_VALUE}",
                "ktlint_official, 140",
            ],
        )
        fun `Given a property which is unset then the property mapper returns the default value of the code style`(
            codeStyleValue: CodeStyleValue,
            expectedDefaultValue: Int,
        ) {
            val property = MAX_LINE_LENGTH_PROPERTY.toPropertyWithValue("unset")

            val actual = maxLineLengthPropertyMapper(property, codeStyleValue)

            assertThat(actual).isEqualTo(expectedDefaultValue)
        }

        @ParameterizedTest(name = "Code style: {0}")
        @EnumSource(CodeStyleValue::class)
        fun `Given a valid string value then the property mapper returns the integer value`(codeStyleValue: CodeStyleValue) {
            val someValue = 123
            val property = MAX_LINE_LENGTH_PROPERTY.toPropertyWithValue(someValue.toString())

            val actual = maxLineLengthPropertyMapper(property, codeStyleValue)

            assertThat(actual).isEqualTo(someValue)
        }

        @ParameterizedTest(name = "Code style: {0}")
        @EnumSource(CodeStyleValue::class)
        fun `Given the value 'off' then the property mapper returns integer MAX_VALUE which denotes that no maximum line length is set`(
            codeStyleValue: CodeStyleValue,
        ) {
            val property = MAX_LINE_LENGTH_PROPERTY.toPropertyWithValue("off")

            val actual = maxLineLengthPropertyMapper(property, codeStyleValue)

            assertThat(actual).isEqualTo(Int.MAX_VALUE)
        }

        @ParameterizedTest(name = "Code style: {0}")
        @EnumSource(CodeStyleValue::class)
        fun `Given the value '-1' then the property mapper returns integer MAX_VALUE which denotes that no maximum line length is set`(
            codeStyleValue: CodeStyleValue,
        ) {
            val property = MAX_LINE_LENGTH_PROPERTY.toPropertyWithValue("-1")

            val actual = maxLineLengthPropertyMapper(property, codeStyleValue)

            assertThat(actual).isEqualTo(Int.MAX_VALUE)
        }

        @ParameterizedTest(name = "Code style: {0}, default value: {1}")
        @CsvSource(
            value = [
                "android_studio, 100",
                "intellij_idea, ${Int.MAX_VALUE}",
                "ktlint_official, 140",
            ],
        )
        fun `Given an invalid value then the property mapper returns the default value of the code style`(
            codeStyleValue: CodeStyleValue,
            expectedDefaultValue: Int,
        ) {
            val property = MAX_LINE_LENGTH_PROPERTY.toPropertyWithValue("some-invalid-value")

            val actual = maxLineLengthPropertyMapper(property, codeStyleValue)

            assertThat(actual).isEqualTo(expectedDefaultValue)
        }
    }

    @ParameterizedTest(name = "Input value: {0}, output value: {1}")
    @CsvSource(
        value = [
            "-1, off",
            "0, off",
            "1, 1",
            "${Int.MAX_VALUE}, off",
        ],
    )
    fun `Given a property with an integer value than write that property`(
        inputValue: Int,
        expectedOutputValue: String,
    ) {
        val actual = MAX_LINE_LENGTH_PROPERTY.propertyWriter(inputValue)

        assertThat(actual).isEqualTo(expectedOutputValue)
    }
}
