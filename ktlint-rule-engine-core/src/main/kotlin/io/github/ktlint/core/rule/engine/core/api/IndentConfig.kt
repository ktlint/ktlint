package io.github.ktlint.core.rule.engine.core.api

import com.intellij.lang.ASTNode
import dev.drewhamilton.poko.Poko
import io.github.ktlint.core.rule.engine.core.api.IndentConfig.IndentStyle.SPACE
import io.github.ktlint.core.rule.engine.core.api.IndentConfig.IndentStyle.TAB
import io.github.ktlint.core.rule.engine.core.api.editorconfig.INDENT_SIZE_PROPERTY
import io.github.ktlint.core.rule.engine.core.api.editorconfig.INDENT_STYLE_PROPERTY
import org.ec4j.core.model.PropertyType

@Poko
public class IndentConfig(
    public val indentStyle: IndentStyle,
    /**
     * The number of spaces that is equivalent to one tab
     */
    public val tabWidth: Int,
) {
    /**
     * To use the [IndentConfig] in a rule, the following needs to be done:
     * 1. Implement interface [UsesEditorConfigProperties] on the rule
     * 2. Register the used or properties
     *    ```
     *    override val editorConfigProperties: List<UsesEditorConfigProperties.EditorConfigProperty<*>> =
     *        listOf(indentSizeProperty, indentStyleProperty)
     *    ```
     * 3. Initialize the IndentConfig
     *    ```
     *    indentConfig = IndentConfig(
     *        indentStyle = node.getEditorConfigValue(indentStyleProperty),
     *        tabWidth = node.getEditorConfigValue(indentSizeProperty)
     *    )
     *    ```
     */
    public constructor(
        indentStyle: PropertyType.IndentStyleValue,
        /**
         * The number of spaces that is equivalent to one tab
         */
        tabWidth: Int,
    ) : this(
        indentStyle =
            when (indentStyle) {
                PropertyType.IndentStyleValue.tab -> TAB
                PropertyType.IndentStyleValue.space -> SPACE
            },
        tabWidth = tabWidth,
    )

    public enum class IndentStyle { SPACE, TAB }

    private val indentChar =
        when (indentStyle) {
            TAB -> '\t'
            SPACE -> ' '
        }

    private val unexpectedIndentChar =
        when (indentStyle) {
            TAB -> ' '
            SPACE -> '\t'
        }

    /**
     * When disabled, rules may not enforce any indentation related changes regardless of other settings in this
     * configuration.
     */
    public val disabled: Boolean
        get() = tabWidth <= 0

    /**
     * Normalized indent of 1 level deep. Representation is either in spaces or a single tab depending on the
     * configuration.
     */
    public val indent: String =
        if (disabled) {
            ""
        } else {
            when (indentStyle) {
                TAB -> indentChar.toString()
                SPACE -> indentChar.toString().repeat(tabWidth)
            }
        }

    /**
     * Get the indentation including the newline character for a node at the next indent level compared to the given node.
     */
    public fun childIndentOf(node: ASTNode): String = node.indent.plus(indent)

    /**
     * Get the indentation including the newline character for a node at the same indent level as the given node.
     */
    public fun siblingIndentOf(node: ASTNode): String = parentIndentOf(node).plus(indent)

    /**
     * Get the indentation including the newline character for a node at the same indent level as the parent of the given node.
     */
    public fun parentIndentOf(node: ASTNode): String = node.parent!!.indent

    /**
     * Converts [text] to a normalized indent. If [text] contains a new line, then only text after the last new line is converted. This text
     * may only contain spaces and tabs.
     *
     * Tabs in the given text are converted to spaces depending on the tab size of the indent style.
     *
     * When converting a text (possibly contains both tabs and spaces) to tabs, all tabs are first converted to spaces. Each consecutive set
     * of spaces is converted to tabs. Spaces which are left, e.g. not enough space to complete a full tab, are ignored silently.
     */
    public fun toNormalizedIndent(text: String): String {
        val indent = getTextAfterLastNewLine(text)
        require(indent.matches(TABS_AND_SPACES))
        return when (indentStyle) {
            SPACE -> {
                indent.replaceTabWithSpaces()
            }

            TAB -> {
                "\t".repeat(indentLevelFrom(indent))
                // Silently swallow spaces if not enough spaces present to convert to a tab
                // val spaceCount = asSpaces.length - (tabCount * tabWidth)
                // "\t".repeat(tabCount) + " ".repeat(spaceCount)
            }
        }
    }

    private fun getTextAfterLastNewLine(text: String): String {
        val index = text.indexOfLast { it == '\n' }
        val indent =
            if (index == -1) {
                text
            } else {
                text.substring(index + 1, text.length)
            }
        return indent
    }

    private fun String.replaceTabWithSpaces() = replace("\t", " ".repeat(tabWidth))

    /**
     * Gets the indent level for given [text]. If [text] contains a new line, then only text after the last new line
     * is considered. This text may only contain spaces and tabs.
     *
     * Tabs in the [text] are first converted to spaces depending on the tab size of the indent style. For each set
     * of spaces that is equivalent to one tab is counted as indentation level. Space that are remaining because
     * they do not fill up an entire tab, are ignored silently.
     */
    public fun indentLevelFrom(text: String): Int {
        val indent = getTextAfterLastNewLine(text)
        require(indent.matches(TABS_AND_SPACES))
        return indent.replaceTabWithSpaces().length / tabWidth
    }

    public fun containsUnexpectedIndentChar(indentText: String): Boolean = indentText.contains(unexpectedIndentChar)

    public fun indexOfFirstUnexpectedIndentChar(indentText: String): Int = indentText.indexOfFirst { it == unexpectedIndentChar }

    public val unexpectedIndentCharDescription: String =
        when (indentStyle) {
            SPACE -> "tab"
            TAB -> "space"
        }

    public companion object {
        private val TABS_AND_SPACES = Regex("[ \t]*")

        public val DEFAULT_INDENT_CONFIG: IndentConfig =
            IndentConfig(
                indentStyle = INDENT_STYLE_PROPERTY.defaultValue,
                tabWidth = INDENT_SIZE_PROPERTY.defaultValue,
            )
    }
}
