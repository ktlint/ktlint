package io.github.ktlint.core.ruleset.standard.rules

import io.github.ktlint.core.rule.engine.core.api.AutocorrectDecision
import io.github.ktlint.core.rule.engine.core.api.ElementType.IMPORT_DIRECTIVE
import io.github.ktlint.core.rule.engine.core.api.RuleId
import io.github.ktlint.core.rule.engine.core.api.SinceKtlint
import io.github.ktlint.core.rule.engine.core.api.SinceKtlint.Status.STABLE
import io.github.ktlint.core.rule.engine.core.api.editorconfig.EditorConfig
import io.github.ktlint.core.rule.engine.core.api.editorconfig.EditorConfigProperty
import io.github.ktlint.core.ruleset.standard.StandardRule
import io.github.ktlint.core.ruleset.standard.rules.internal.importordering.PatternEntry
import org.ec4j.core.model.PropertyType
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.psi.KtImportDirective

@SinceKtlint("0.2", STABLE)
public class NoWildcardImportsRule :
    StandardRule(
        id = "no-wildcard-imports",
        usesEditorConfigProperties =
            setOf(
                IJ_KOTLIN_PACKAGES_TO_USE_IMPORT_ON_DEMAND,
            ),
    ) {
    private lateinit var allowedWildcardImports: List<PatternEntry>

    override fun beforeFirstNode(editorConfig: EditorConfig) {
        allowedWildcardImports = editorConfig[IJ_KOTLIN_PACKAGES_TO_USE_IMPORT_ON_DEMAND]
    }

    override fun beforeVisitChildNodes(
        node: ASTNode,
        emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> AutocorrectDecision,
    ) {
        if (node.elementType == IMPORT_DIRECTIVE) {
            val importDirective = node.psi as KtImportDirective
            val path = importDirective.importPath ?: return
            if (!path.isAllUnder) return
            if (allowedWildcardImports.none { it.matches(path) }) {
                emit(node.startOffset, "Wildcard import", false)
            }
        }
    }

    public companion object {
        private const val WILDCARD_WITHOUT_SUBPACKAGES = "*"
        private const val WILDCARD_WITH_SUBPACKAGES = "**"

        private fun parseAllowedWildcardImports(allowedWildcardImports: String): List<PatternEntry> {
            val importsList = allowedWildcardImports.split(",").onEach { it.trim() }

            return importsList.map { import ->
                if (import.endsWith(WILDCARD_WITH_SUBPACKAGES)) { // java.**
                    PatternEntry(
                        packageName = import.removeSuffix(WILDCARD_WITH_SUBPACKAGES).plus(WILDCARD_WITHOUT_SUBPACKAGES),
                        withSubpackages = true,
                        hasAlias = false,
                    )
                } else {
                    PatternEntry(
                        packageName = import,
                        withSubpackages = false,
                        hasAlias = false,
                    )
                }
            }
        }

        private val PACKAGES_TO_USE_ON_DEMAND_IMPORT_PROPERTY_PARSER: (String, String?) -> PropertyType.PropertyValue<List<PatternEntry>> =
            { _, value ->
                try {
                    PropertyType.PropertyValue.valid(
                        value,
                        value?.let(Companion::parseAllowedWildcardImports) ?: emptyList(),
                    )
                } catch (e: IllegalArgumentException) {
                    PropertyType.PropertyValue.invalid(
                        value,
                        "Unexpected imports layout: $value (${e.message})",
                    )
                }
            }

        public val IJ_KOTLIN_PACKAGES_TO_USE_IMPORT_ON_DEMAND: EditorConfigProperty<List<PatternEntry>> =
            EditorConfigProperty(
                type =
                    PropertyType(
                        "ij_kotlin_packages_to_use_import_on_demand",
                        "Defines allowed wildcard imports",
                        PACKAGES_TO_USE_ON_DEMAND_IMPORT_PROPERTY_PARSER,
                    ),
                /*
                 * Default IntelliJ IDEA style: Use wildcard imports for packages in "java.util", "kotlin.android.synthetic" and
                 * it's subpackages.
                 *
                 * https://github.com/JetBrains/kotlin/blob/ffdab473e28d0d872136b910eb2e0f4beea2e19c/idea/formatter/src/org/jetbrains/kotlin/idea/core/formatter/KotlinCodeStyleSettings.java#L81-L82
                 */
                defaultValue = parseAllowedWildcardImports("java.util.*,kotlinx.android.synthetic.**"),
                propertyWriter = {
                    if (it.isEmpty()) {
                        "unset"
                    } else {
                        it.joinToString(separator = ",")
                    }
                },
                ktlintOfficialCodeStyleDefaultValue = emptyList(),
            )
    }
}

public val NO_WILDCARD_IMPORTS_RULE_ID: RuleId = NoWildcardImportsRule().ruleId
