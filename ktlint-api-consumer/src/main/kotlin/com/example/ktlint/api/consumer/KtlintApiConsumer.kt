package com.example.ktlint.api.consumer

import com.example.ktlint.api.consumer.rules.KTLINT_API_CONSUMER_RULE_PROVIDERS
import io.github.ktlint.core.cli.ruleset.core.api.RuleSetV2Provider
import io.github.ktlint.core.logger.api.initKtLintKLogger
import io.github.ktlint.core.rule.engine.api.Code
import io.github.ktlint.core.rule.engine.api.EditorConfigDefaults
import io.github.ktlint.core.rule.engine.api.EditorConfigOverride
import io.github.ktlint.core.rule.engine.api.EditorConfigPropertyRegistry
import io.github.ktlint.core.rule.engine.api.KtLintRuleEngine
import io.github.ktlint.core.rule.engine.core.api.AutocorrectDecision
import io.github.ktlint.core.rule.engine.core.api.IndentConfig
import io.github.ktlint.core.rule.engine.core.api.editorconfig.EXPERIMENTAL_RULES_EXECUTION_PROPERTY
import io.github.ktlint.core.rule.engine.core.api.editorconfig.INDENT_SIZE_PROPERTY
import io.github.ktlint.core.rule.engine.core.api.editorconfig.INDENT_STYLE_PROPERTY
import io.github.ktlint.core.rule.engine.core.api.editorconfig.RuleExecution
import io.github.ktlint.core.rule.engine.core.api.propertyTypes
import io.github.oshai.kotlinlogging.KotlinLogging
import java.io.File
import java.net.URL
import java.net.URLClassLoader
import java.nio.file.Paths
import java.util.ServiceConfigurationError
import java.util.ServiceLoader
import kotlin.collections.toSet

private val LOGGER = KotlinLogging.logger {}.initKtLintKLogger()

/**
 * This API Consumer shows how the [KtLintRuleEngine] can be invoked and how the amount and format of the logging can be configured.
 * This example uses 'slf4j-simple' as logging provider. But any logging provider that implements SLF4J API can be used depending on your
 * needs.
 */
public fun main() {
    // RuleProviders can be supplied by a custom rule provider of the API Consumer itself. This custom rule provider by definition can only
    // provide rules that are available at compile time. But it is also possible to provide rules from jars that are loaded on runtime.
    val ruleProviders = KTLINT_API_CONSUMER_RULE_PROVIDERS.plus(runtimeLoadedRuleProviders)

    val editorConfigPropertyRegistry = EditorConfigPropertyRegistry(ruleProviders)

    // Providing rule dependencies at compile time has the advantage that properties defined in those rules can be used to build the
    // EditorConfigOverride using static types. However, providing the rule dependencies at runtime can offer flexibility to the API
    // Consumer. It comes with the cost that the EditorConfigOverride can not be build with static types.
    val editorConfigOverride =
        EditorConfigOverride
            .from(
                // Properties provided by ktlint-rule-engine-core are best to be loaded statically as they are available at compile time as
                // they are provided by the ktlint-rule-engine-core module.
                INDENT_STYLE_PROPERTY to IndentConfig.IndentStyle.SPACE,
                INDENT_SIZE_PROPERTY to 4,
                EXPERIMENTAL_RULES_EXECUTION_PROPERTY to RuleExecution.ENABLED,
                //
                // Properties defined in the ktlint-ruleset-standard can only be loaded statically when that dependency is provided at
                // compile time. In this example project this ruleset is loaded at runtime, so following decommenting next line results in
                // a compilation error:
                // FUNCTION_BODY_EXPRESSION_WRAPPING_PROPERTY to always
                //
                // For properties that are defined in rules for which the dependency is provided at runtime only, the property name can be
                // provided as String and the value as Any type. As the property is not available at compile time, the value has to be
                // specified as a String (exactly as would be done in the `.editorconfig` file).
                // In case the property value is invalid, the KtlintRuleEngine logs a warning.
                editorConfigPropertyRegistry.find("ktlint_function_signature_body_expression_wrapping") to "always",
                //
                // The properties for enabling/disabling a rule or entire rule set can be set as well. Note that the values of this
                // property can be set via the `RuleExecution` enum which is available at compile time as it is provided by the
                // ktlint-rule-engine-core module.
                editorConfigPropertyRegistry.find("ktlint_standard_function-signature") to RuleExecution.DISABLED,
                editorConfigPropertyRegistry.find("ktlint_standard") to RuleExecution.DISABLED,
                //
                // In case an unknown property is provided, an exception is thrown:
                // ruleProviders.findEditorConfigProperty("unknown_property") to "some-value",
            )

    // The KtLint RuleEngine only needs to be instantiated once and can be reused in multiple invocations
    val apiConsumerKtLintRuleEngine =
        KtLintRuleEngine(
            ruleProviders = ruleProviders,
            editorConfigOverride = editorConfigOverride,
            editorConfigDefaults =
                EditorConfigDefaults.load(
                    path = Paths.get("/some/path/to/editorconfig/file/or/directory"),
                    propertyTypes = ruleProviders.propertyTypes(),
                ),
        )

    val codeFile =
        "ktlint-api-consumer/src/main/kotlin/com/example/ktlint/api/consumer/KtlintApiConsumer.kt"
            .let { fileName ->
                LOGGER.info { "Read code from file '$fileName'" }
                Code.fromFile(
                    File(fileName),
                )
            }

    LOGGER.info {
        """

        ==============================================
        Run ktlintRuleEngine.lint on sample code
        ---
        """.trimIndent()
    }
    apiConsumerKtLintRuleEngine
        .lint(codeFile) {
            LOGGER.info { "LintViolation reported by KtLint: $it" }
        }

    LOGGER.info {
        """

        ==============================================
        Run ktlintRuleEngine.format on sample code
        ---
        """.trimIndent()
    }
    apiConsumerKtLintRuleEngine
        .format(codeFile) { _ -> AutocorrectDecision.ALLOW_AUTOCORRECT }
        .also {
            LOGGER.info { "Code formatted by KtLint:\n$it" }
        }

    LOGGER.info {
        """

        ==============================================
        The amount and format of the logging is configured in file
         'ktlint-api-consumer/src/main/resources/simplelogger.properties'
        """.trimIndent()
    }
}

private val runtimeLoadedRuleProviders =
    try {
        ServiceLoader
            .load(
                RuleSetV2Provider::class.java,
                URLClassLoader(emptyArray<URL?>()),
            ).flatMap { it.getRuleProviders() }
            .toSet()
    } catch (e: ServiceConfigurationError) {
        LOGGER.warn { "Error while loading the rulesets:\n${e.printStackTrace()}" }
        emptySet()
    }
