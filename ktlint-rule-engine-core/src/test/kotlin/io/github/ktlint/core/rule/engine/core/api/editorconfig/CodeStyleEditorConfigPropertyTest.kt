package io.github.ktlint.core.rule.engine.core.api.editorconfig

import assertk.assertThat
import assertk.assertions.isEqualTo
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

class CodeStyleEditorConfigPropertyTest {
    @ParameterizedTest(name = "Value: [{0}], result: [{1}]")
    @CsvSource(
        value = [
            "ktlint_official,ktlint_official",
            " ktlint_official,ktlint_official",
            "ktlint_official ,ktlint_official",
            " ktlint_official ,ktlint_official",
            "intellij_idea,intellij_idea",
            " intellij_idea,intellij_idea",
            "intellij_idea ,intellij_idea",
            " intellij_idea ,intellij_idea",
            "android_studio,android_studio",
            " android_studio,android_studio",
            "android_studio ,android_studio",
            " android_studio ,android_studio",
        ],
        ignoreLeadingAndTrailingWhitespace = false,
    )
    fun `Given a code style property`(
        value: String,
        expectedCodeStyleValue: CodeStyleValue,
    ) {
        val actual = CODE_STYLE_PROPERTY.type.parse(value)

        assertThat(actual.parsed).isEqualTo(expectedCodeStyleValue)
    }
}
