package io.github.ktlint.core.cli.reporter.baseline

import assertk.all
import assertk.assertFailure
import assertk.assertThat
import assertk.assertions.cause
import assertk.assertions.contains
import assertk.assertions.hasMessage
import assertk.assertions.isEqualTo
import assertk.assertions.isEqualToWithGivenProperties
import assertk.assertions.isInstanceOf
import assertk.assertions.isNotNull
import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import io.github.ktlint.core.cli.reporter.core.api.KtlintCliError
import io.github.ktlint.core.cli.reporter.core.api.KtlintCliError.Status.BASELINE_IGNORED
import io.github.ktlint.core.logger.api.initKtLintKLogger
import io.github.ktlint.core.logger.api.setDefaultLoggerModifier
import io.github.oshai.kotlinlogging.DelegatingKLogger
import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import nl.altindag.log.LogCaptor
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.xml.sax.SAXParseException
import java.nio.file.Path
import java.nio.file.Paths
import java.util.Locale
import kotlin.io.path.Path
import kotlin.io.path.copyTo
import kotlin.io.path.pathString

@Suppress("unused")
private val LOGGER =
    KotlinLogging
        .logger {}
        .setDefaultLoggerModifier { logger -> logger.level = Level.DEBUG }
        .initKtLintKLogger()

private var KLogger.level: Level?
    get() = underlyingLogger()?.level
    set(value) {
        underlyingLogger()?.level = value
    }

private fun KLogger.underlyingLogger(): Logger? =
    @Suppress("UNCHECKED_CAST")
    (this as? DelegatingKLogger<Logger>)
        ?.underlyingLogger

class BaselineTest {
    private val currentLocale = Locale.getDefault()

    @BeforeEach
    fun setLocaleEnglish() {
        // SAXParseException messages are localized. Prevent tests from failing when run on a system for which local is not set to English.
        Locale.setDefault(Locale.ENGLISH)
    }

    @AfterEach
    fun resetLocaleDefault() = Locale.setDefault(currentLocale)

    @Nested
    inner class `Given a valid baseline file` {
        @Test
        fun `Given that the baseline is loaded with classic loader`(
            @TempDir
            tempDir: Path,
        ) {
            val path = "baseline-valid.xml".copyResourceToFileIn(tempDir)

            val actual = loadBaseline(path)

            val expected =
                Baseline(
                    path = path,
                    status = Baseline.Status.VALID,
                    lintErrorsPerFile =
                        mapOf(
                            "src/main/kotlin/Foo.kt" to
                                listOf(
                                    KtlintCliError(1, 1, "standard:max-line-length", "", BASELINE_IGNORED),
                                    KtlintCliError(2, 1, "standard:max-line-length", "", BASELINE_IGNORED),
                                    KtlintCliError(4, 9, "standard:property-naming", "", BASELINE_IGNORED),
                                ),
                        ),
                )
            assertThat(actual)
                .isEqualToWithGivenProperties(
                    expected,
                    Baseline::path,
                    Baseline::status,
                    Baseline::lintErrorsPerFile,
                )
        }

        @ParameterizedTest(name = "BaselineErrorHandling: {0}")
        @EnumSource(value = BaselineErrorHandling::class)
        fun `Given that the baseline is loaded with the new loader with LOG error handling`(
            baselineErrorHandling: BaselineErrorHandling,
            @TempDir
            tempDir: Path,
        ) {
            val path = "baseline-valid.xml".copyResourceToFileIn(tempDir)

            val actual = loadBaseline(path, baselineErrorHandling)

            val expected =
                Baseline(
                    path = path,
                    status = Baseline.Status.VALID,
                    lintErrorsPerFile =
                        mapOf(
                            "src/main/kotlin/Foo.kt" to
                                listOf(
                                    KtlintCliError(1, 1, "standard:max-line-length", "", BASELINE_IGNORED),
                                    KtlintCliError(2, 1, "standard:max-line-length", "", BASELINE_IGNORED),
                                    KtlintCliError(4, 9, "standard:property-naming", "", BASELINE_IGNORED),
                                ),
                        ),
                )
            assertThat(actual)
                .isEqualToWithGivenProperties(
                    expected,
                    Baseline::path,
                    Baseline::status,
                    Baseline::lintErrorsPerFile,
                )
        }
    }

    @Nested
    inner class `Baseline has invalid xml structure` {
        @Test
        fun `Given that the baseline is loaded with BaselineErrorHandling EXCEPTION then on error an exception is thrown`(
            @TempDir
            tempDir: Path,
        ) {
            val path = "baseline-invalid.xml".copyResourceToFileIn(tempDir)

            assertFailure { loadBaseline(path, BaselineErrorHandling.EXCEPTION) }
                .isInstanceOf<BaselineLoaderException>()
                .all {
                    hasMessage("Unable to parse baseline file: $path")
                    cause()
                        .isNotNull()
                        .isInstanceOf<SAXParseException>()
                        .hasMessage("The element type \"file\" must be terminated by the matching end-tag \"</file>\".")
                }
        }

        @Test
        fun `Given that the baseline is loaded with BaselineErrorHandling LOG then on error a the log message is printed and no exception is thrown`(
            @TempDir
            tempDir: Path,
        ) {
            val path = "baseline-invalid.xml".copyResourceToFileIn(tempDir)

            val logCaptor = LogCaptor.forClass(Baseline::class.java)

            loadBaseline(path, BaselineErrorHandling.LOG)

            assertThat(logCaptor.errorLogs).contains("Unable to parse baseline file: $path")
        }
    }

    /**
     * The resource needs to be copied in each test, because the file will be deleted in case it contains an error.
     */
    private fun String.copyResourceToFileIn(tempDir: Path) =
        Paths
            .get("$TEST_RESOURCE_PATH/$this")
            .copyTo(tempDir, overwrite = true)
            .pathString

    private companion object {
        val TEST_RESOURCE_PATH: Path = Path("src", "test", "resources")
    }
}
