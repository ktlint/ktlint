package io.github.ktlint.core.rule.engine.core.api.editorconfig

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

class CodeStyleEditorConfigPropertyTest {
    @ParameterizedTest(name = "Value: [{0}], result: [{1}]")
    @CsvSource(
        value = [
            "ktlint_official,KTLINT_OFFICIAL",
            " ktlint_official,KTLINT_OFFICIAL",
            "ktlint_official ,KTLINT_OFFICIAL",
            " ktlint_official ,KTLINT_OFFICIAL",
            "intellij_idea,INTELLIJ_IDEA",
            " intellij_idea,INTELLIJ_IDEA",
            "intellij_idea ,INTELLIJ_IDEA",
            " intellij_idea ,INTELLIJ_IDEA",
            "android_studio,ANDROID_STUDIO",
            " android_studio,ANDROID_STUDIO",
            "android_studio ,ANDROID_STUDIO",
            " android_studio ,ANDROID_STUDIO",
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
