package io.github.ktlint.core.rule.engine.core.api.editorconfig

import assertk.assertThat
import assertk.assertions.containsExactlyInAnyOrder
import assertk.assertions.isTrue
import org.ec4j.core.model.PropertyType
import org.junit.jupiter.api.Test

class CommaSeparatedListValueParserTest {
    private val propertyType =
        PropertyType.LowerCasingPropertyType(
            "some-property-type",
            null,
            CommaSeparatedListValueParser(),
        )

    @Test
    fun `Given a comma separated list property with value unset`() {
        val actual = propertyType.parse("unset")

        assertThat(actual.isUnset).isTrue()
    }

    @Test
    fun `Given a comma separated list property with a single value`() {
        val actual = propertyType.parse(SOME_VALUE_1)

        assertThat(actual.parsed).containsExactlyInAnyOrder(SOME_VALUE_1)
    }

    @Test
    fun `Given a comma separated list property with a multiple values`() {
        val actual = propertyType.parse("$SOME_VALUE_1,$SOME_VALUE_2")

        assertThat(actual.parsed).containsExactlyInAnyOrder(SOME_VALUE_1, SOME_VALUE_2)
    }

    @Test
    fun `Given a comma separated list property with a multiple values and redundant space before or after value`() {
        val actual = propertyType.parse(" $SOME_VALUE_1 , $SOME_VALUE_2 ")

        assertThat(actual.parsed).containsExactlyInAnyOrder(SOME_VALUE_1, SOME_VALUE_2)
    }

    private companion object {
        const val SOME_VALUE_1 = "some-value-1"
        const val SOME_VALUE_2 = "some-value-2"
    }
}
