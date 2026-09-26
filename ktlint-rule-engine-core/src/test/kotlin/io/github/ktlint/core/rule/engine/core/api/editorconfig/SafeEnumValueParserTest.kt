package io.github.ktlint.core.rule.engine.core.api.editorconfig

import assertk.assertThat
import assertk.assertions.isEqualTo
import org.ec4j.core.model.PropertyType
import org.junit.jupiter.api.Test

class SafeEnumValueParserTest {
    @Test
    fun `Given a rule execution property for which the value`() {
        val propertyType =
            PropertyType.LowerCasingPropertyType(
                "some-property-type",
                null,
                SafeEnumValueParser(SomePropertyType::class.java),
                SomePropertyType.entries.map { it.name }.toSet(),
            )

        val actual = propertyType.parse(" value2 ")

        assertThat(actual.parsed).isEqualTo(SomePropertyType.value2)
    }

    @Suppress("EnumEntryName")
    private enum class SomePropertyType {
        value1,
        value2,
    }
}
