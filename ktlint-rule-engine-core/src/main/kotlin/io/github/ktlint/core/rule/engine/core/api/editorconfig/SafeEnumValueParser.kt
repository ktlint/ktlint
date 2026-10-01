package io.github.ktlint.core.rule.engine.core.api.editorconfig

import org.ec4j.core.model.PropertyType
import org.ec4j.core.model.PropertyType.PropertyValueParser
import java.util.Locale

/**
 * A [PropertyValueParser] implementation that allows only members of a given [Enum] type. This class is almost identical to the original
 * [EnumValueParser] provided by ec4j. Difference is that values are trimmed before trying to match the enum values.
 *
 * As the ec4j project has not provided any new release since version 1.0 (2019-08-01) a custom implementation has been added.
 *
 * @param <T> the type of the value <T>
 */
public class SafeEnumValueParser<T : Enum<T>>(
    private val enumType: Class<T>,
) : PropertyValueParser<T> {
    override fun parse(
        name: String?,
        value: String?,
    ): PropertyType.PropertyValue<T> =
        if (value == null) {
            PropertyType.PropertyValue.invalid(null, "Cannot make enum ${enumType.name} out of null")
        } else {
            try {
                PropertyType.PropertyValue.valid(
                    value,
                    java.lang.Enum.valueOf(
                        enumType,
                        value
                            // In case an EOL comment (separated with a space) is appended after the value then the comment
                            // itself is removed but not the space. This results in the enum value not being parsed
                            // correctly.
                            .trim()
                            .uppercase(Locale.getDefault()),
                    ) as T,
                )
            } catch (_: IllegalArgumentException) {
                PropertyType.PropertyValue.invalid(
                    value,
                    "Unexpected parsed \"" + value + "\" for enum " + enumType.name,
                )
            }
        }
}
