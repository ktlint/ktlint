package io.github.ktlint.core.ruleset.standard.rules.internal

import assertk.assertThat
import assertk.assertions.isTrue
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class RemoveDiacriticsFromLettersTest {
    @ParameterizedTest(name = "Original character: {0}")
    @ValueSource(
        strings = [
            "àáâäæãåā",
            "çćč",
            "èéêëēėę",
            "îïíīįì",
            "ł",
            "ñń",
            "ôöòóœøōõ",
            "ßśš",
            "ûüùúū",
            "ÿ",
            "žźż",
            "ÀÁÂÄÆÃÅĀ",
            "ÇĆČ",
            "ÈÉÊËĒĖĘ",
            "ÎÏÍĪĮÌ",
            "Ł",
            "ÑŃ",
            "ÔÖÒÓŒØŌÕ",
            "ŚŠ",
            "ÛÜÙÚŪ",
            "Ÿ",
            "ŽŹŻ",
        ],
    )
    fun `Given a letter with a diacritic then remove it`(original: String) {
        assertThat(original.matches("[A-Za-z]*".regExIgnoringDiacriticsAndStrokesOnLetters())).isTrue()
    }
}
