package io.github.ktlint.core.cli.internal

import io.github.ktlint.core.cli.internal.CustomJarProviderCheck.ERROR_WHEN_REQUIRED_PROVIDER_IS_MISSING
import io.github.ktlint.core.cli.internal.CustomJarProviderCheck.WARN_WHEN_DEPRECATED_PROVIDER_IS_FOUND
import io.github.ktlint.core.logger.api.initKtLintKLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import java.net.URL
import java.net.URLClassLoader
import java.util.ServiceConfigurationError
import java.util.ServiceLoader

private val LOGGER = KotlinLogging.logger {}.initKtLintKLogger()

private const val KTLINT_JAR = "ktlint"

internal fun <T> Class<T>.loadFromKtlintCliJar(providerId: (T) -> String): Set<T> =
    loadProvidersFromJars(null)
        .also { providers ->
            providers
                .mapNotNull { it }
                .forEach {
                    LOGGER.debug { "Discovered $simpleName with id '${providerId(it)}' in ktlint JAR" }
                }
        }

internal fun <T> Class<T>.loadFromJarFile(
    url: URL,
    excludeProviderIds: List<String>,
    providerId: (T) -> String,
    customJarProviderCheck: CustomJarProviderCheck,
): List<T> {
    val providers = loadProvidersFromJars(url).filterNot { excludeProviderIds.contains(providerId(it)) }
    return if (providers.isEmpty()) {
        if (customJarProviderCheck == ERROR_WHEN_REQUIRED_PROVIDER_IS_MISSING) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.error {
                    """
                    JAR file '${url.path}' is missing a class implementing interface '$canonicalName'
                        KtLint uses a ServiceLoader to dynamically load classes from JAR files specified at the command line of KtLint.
                        The JAR file below does not contain an implementation of the interface.
                            Interface: $canonicalName
                            JAR file : ${url.path}
                        Check following:
                          - Does the jar contain an implementation of the interface above?
                          - Does the jar contain a resource file with name '$canonicalName'?
                          - Is the resource file located in directory "src/main/resources/META-INF/services"?
                          - Does the resource file contain the fully qualified class name of the class implementing the interface above?
                    """.trimIndent()
                }
            } else {
                LOGGER.error {
                    "JAR file '${url.path}' is missing a class implementing interface '$canonicalName' (run with '--log-level=debug' " +
                        "for more information)"
                }
            }
            exitKtLintProcess(ExitCode.INVALID_RULESET_JAR)
        }
        providers
    } else {
        providers.mapNotNull {
            if (customJarProviderCheck == WARN_WHEN_DEPRECATED_PROVIDER_IS_FOUND) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.warn {
                        """
                        JAR file '${url.path}' contains a class implementing a deprecated interface '$canonicalName'
                            KtLint uses a ServiceLoader to dynamically load classes from JAR files specified at the command line of KtLint.
                            The JAR file below contains an implementation of a deprecated interface which is no longer fully supported by
                            this version of KtLint. Verify the logging for errors.
                            Use a newer version of the JAR file, or when not available contact the maintainer of this JAR file (not
                            maintained by KtLint) to upgrade the JAR file.
                                Interface: $canonicalName
                                JAR file : ${url.path}
                        """.trimIndent()
                    }
                } else {
                    LOGGER.warn {
                        "JAR file '${url.path}' contains a class implementing a deprecated interface '$canonicalName' " +
                            "(run with '--log-level=debug' for more information)"
                    }
                }
            } else {
                LOGGER.debug {
                    """
                    Discovered $simpleName with id '${providerId(it)}' in JAR file '${url.path}'
                        KtLint uses a ServiceLoader to dynamically load classes from JAR files specified at the command line of KtLint.
                        The JAR file below contains an implementation of an interface which is supported by this version of ktlint:
                            Interface: $canonicalName
                            Id       : ${providerId(it)}
                            JAR file : ${url.path}
                    """.trimIndent()
                }
            }
            it
        }
    }
}

private fun <T> Class<T>.loadProvidersFromJars(url: URL?): Set<T> =
    try {
        ServiceLoader
            .load(
                this,
                URLClassLoader(url.toArray()),
            ).toSet()
    } catch (e: ServiceConfigurationError) {
        LOGGER.warn(e) { "Error while loading JAR file '${url.jarFilePath()}'" }
        emptySet()
    }

private fun URL?.toArray() =
    this
        ?.let { arrayOf(this) }
        .orEmpty()

private fun URL?.jarFilePath() =
    this
        ?.path
        ?: KTLINT_JAR

internal enum class CustomJarProviderCheck {
    /**
     * Log an error and exit ktlint when the JAR file does not contain a required provider class
     */
    ERROR_WHEN_REQUIRED_PROVIDER_IS_MISSING,

    /**
     * Log a warning when the JAR file contains a deprecated provider class
     */
    WARN_WHEN_DEPRECATED_PROVIDER_IS_FOUND,
}
