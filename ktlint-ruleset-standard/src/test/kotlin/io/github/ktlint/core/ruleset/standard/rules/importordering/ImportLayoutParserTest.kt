package io.github.ktlint.core.ruleset.standard.rules.importordering

import assertk.assertFailure
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isInstanceOf
import io.github.ktlint.core.ruleset.standard.rules.internal.importordering.PatternEntry
import io.github.ktlint.core.ruleset.standard.rules.internal.importordering.parseImportsLayout
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class ImportLayoutParserTest {
    @Test
    fun `blank lines in the beginning and end of import list are not allowed`() {
        assertFailure {
            parseImportsLayout("|,*,|")
        }.isInstanceOf<IllegalArgumentException>()
    }

    @Test
    fun `pattern without single wildcard is not allowed`() {
        assertFailure {
            parseImportsLayout("java.util.List.*")
        }.isInstanceOf<IllegalArgumentException>()
    }

    @ParameterizedTest(name = "Imports layout: {0}")
    @ValueSource(
        strings = [
            "android.**,|,org.junit.**,|,^android.**,*,kotlin.io.Closeable.*,^",
            "android.**, |, org.junit.**, |, ^android.**, *, kotlin.io.Closeable.*, ^",
        ],
    )
    fun `Given some imports layout then parse it correctly`(importsLayout: String) {
        val actual = parseImportsLayout(importsLayout)

        assertThat(actual).containsExactly(
            PatternEntry("android.*", withSubpackages = true, hasAlias = false),
            PatternEntry.BLANK_LINE_ENTRY,
            PatternEntry("org.junit.*", withSubpackages = true, hasAlias = false),
            PatternEntry.BLANK_LINE_ENTRY,
            PatternEntry("android.*", withSubpackages = true, hasAlias = true),
            PatternEntry.ALL_OTHER_IMPORTS_ENTRY,
            PatternEntry("kotlin.io.Closeable.*", withSubpackages = false, hasAlias = false),
            PatternEntry.ALL_OTHER_ALIAS_IMPORTS_ENTRY,
        )
    }
}
