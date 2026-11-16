package io.github.ktlint.core.cli.internal

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.core.MutuallyExclusiveGroupException
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.multiple
import com.github.ajalt.clikt.parameters.options.check
import com.github.ajalt.clikt.parameters.options.convert
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.multiple
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.optionalValue
import com.github.ajalt.clikt.parameters.options.split
import com.github.ajalt.clikt.parameters.options.versionOption
import com.github.ajalt.clikt.parameters.types.int
import io.github.ktlint.core.cli.reporter.baseline.Baseline
import io.github.ktlint.core.cli.reporter.baseline.BaselineErrorHandling
import io.github.ktlint.core.cli.reporter.baseline.loadBaseline
import io.github.ktlint.core.cli.reporter.core.api.ReporterV2
import io.github.ktlint.core.cli.reporter.plain.Color
import io.github.ktlint.core.logger.api.initKtLintKLogger
import io.github.ktlint.core.logger.api.setDefaultLoggerModifier
import io.github.ktlint.core.rule.engine.api.Code
import io.github.ktlint.core.rule.engine.api.EditorConfigDefaults
import io.github.ktlint.core.rule.engine.api.EditorConfigOverride
import io.github.ktlint.core.rule.engine.api.EditorConfigOverride.Companion.plus
import io.github.ktlint.core.rule.engine.api.KtLintRuleEngine
import io.github.ktlint.core.rule.engine.core.api.RuleV2Provider
import io.github.ktlint.core.rule.engine.core.api.editorconfig.RuleExecution
import io.github.ktlint.core.rule.engine.core.api.editorconfig.createRuleExecutionEditorConfigProperty
import io.github.ktlint.core.rule.engine.core.api.propertyTypes
import io.github.ktlint.core.ruleset.standard.rules.FILENAME_RULE_ID
import io.github.oshai.kotlinlogging.DelegatingKLogger
import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.oshai.kotlinlogging.KotlinLoggingConfiguration
import org.jetbrains.kotlin.utils.addToStdlib.applyIf
import java.io.File
import java.nio.file.Paths

internal lateinit var logger: KLogger

internal class KtlintCommandLine : CliktCommand(name = "ktlint") {
    init {
        // Suppress the kotlin-logging startup message below as it would be included in the formatted output when using the option --stdin
        //   kotlin-logging: initializing... active logger factory: Slf4jLoggerFactory
        KotlinLoggingConfiguration.logStartupMessage = false
    }

    override fun help(context: Context) =
        """
        An anti-bikeshedding Kotlin linter with built-in formatter.
        (https://ktlint.github.io/ktlint/latest/).

        Usage on Windows:
          java -jar ktlint.jar  [<options>] [<arguments>]... <command> [<args>]...

        # EXAMPLES

        ## Use default patterns

        Check the style of all Kotlin files (ending with '.kt' or '.kts') inside the current dir (recursively). Hidden folders will be skipped.

        `ktlint`

        ## Specify patterns

        Check only certain locations starting from the current directory.  Prepend ! to negate the pattern, KtLint uses .gitignore pattern style syntax. Globs are applied starting from the last one.

        Check all '.kt' files in 'src/' directory, but ignore files ending with 'Test.kt':

        `ktlint "src/**/*.kt" "!src/**/*Test.kt"`

        Check all '.kt' files in 'src/' directory, but ignore 'generated' directory and its subdirectories:

        `ktlint "src/**/*.kt" "!src/**/generated/**"`

        ## Auto-correct style violations

        Check all '.kt' files in 'src/' directory, and when possible automatically correct the lint violations:

        `ktlint -F "src/**/*.kt"`

        ## Using custom reporter jar and overriding report location

        `ktlint --reporter=csv,artifact=/path/to/reporter/csv.jar,output=my-custom-report.csv`

        # Options and commands
        """.trimIndent()

    override val invokeWithoutSubcommand = true

    init {
        versionOption(KtlintVersionProvider().version, names = setOf("-v", "--version"))
    }

    private val color: Boolean by
        option("--color", help = "Make output colorful")
            .flag(default = false)

    private val colorName: String by
        option("--color-name", help = "Customize the output color")
            .default(Color.DARK_GRAY.name)

    private val format: Boolean by
        option("--format", "-F", help = "Fix deviations from the code style when possible")
            .flag(default = false)

    private val ignoreAutocorrectFailures: Boolean by
        option("--ignore-autocorrect-failures", help = "Ignore all violations for which no autocorrect is available")
            .flag(default = false)

    private val limit: Int by
        option("--limit", help = "Maximum number of errors to show (default: show all)")
            .int()
            .default(Int.MAX_VALUE)
            .check("Value must be bigger than 0") { it > 0 }

    private val relative: Boolean by
        option(
            "--relative",
            help = "Print files relative to the working directory (e.g. dir/file.kt instead of /home/user/project/dir/file.kt)",
        ).flag(default = false)

    private val reporterConfigurations: List<String> by
        option(
            "--reporter",
            help =
                "A reporter to use (built-in: plain (default), plain?group_by_file, plain-summary, json, sarif, checkstyle, html). To use" +
                    "a third-party reporter specify a path to a JAR file on the filesystem via ',artifact=' option. To override reporter " +
                    "output, use ',output=' option.",
        ).multiple()

    private val rulesetJarPaths: List<String> by
        option(
            "--ruleset",
            "-R",
            help = "A path to a JAR file containing additional ruleset(s)",
        ).split(",").default(emptyList())

    private val stdin: Boolean by
        option(
            "--stdin",
            help = "Read file from stdin",
        ).flag()

    private val stdinPath: String? by
        option(
            "--stdin-path",
            help = "Virtual file location for stdin. When combined with option '--format' the actual file will not be overwritten",
        )

    private val patternsFromStdin: String? by
        option(
            "--patterns-from-stdin",
            help =
                "Read additional patterns to check/format from stdin. Patterns are delimited by the given argument. (default is " +
                    "newline). If the argument is an empty string, the NUL byte is used.",
        ).optionalValue(default = "", acceptsUnattachedValue = false)

    private val editorConfigPath: String? by
        option(
            "--editorconfig",
            help =
                "Path to the default '.editorconfig'. A property value from this file is used only when no '.editorconfig' file on the " +
                    "path to the source file specifies that property. Note: up until ktlint 0.46 the property value in this file used to " +
                    "override values found in '.editorconfig' files on the path to the source file.",
        )

    private val forceLintAfterFormat: Boolean by
        option(
            "--force-lint-after-format",
            help = "Force to run lint after code has been formatted to check that it still can be successfully parsed",
            hidden = true,
        ).flag()

    private val noRerunFormatAfterAutocorrect: Boolean by
        option(
            "--no-rerun-format-after-autocorrect",
            help = "Do not rerun format after a lint violation has been autocorrected",
            hidden = true,
        ).flag(default = false)

    private val baselinePath: String by
        option("--baseline", help = "Defines a baseline file to check against")
            .default("")

    private val arguments: List<String> by argument().multiple()

    private val minLogLevel: Level by
        option(
            "--log-level",
            "-l",
            help = "Defines the minimum log level (trace, debug, info, warn, error) or none to suppress all logging",
        ).convert {
            when (it.uppercase()) {
                "TRACE" -> Level.TRACE
                "DEBUG" -> Level.DEBUG
                "INFO" -> Level.INFO
                "WARN" -> Level.WARN
                "ERROR" -> Level.ERROR
                "NONE" -> Level.OFF
                else -> error("Invalid log level '$it'")
            }
        }.default(Level.INFO)

    override fun run() {
        // Ensure the logger is configured for the subcommands as well
        logger = configureLogger()

        if (currentContext.invokedSubcommand == null) {
            lintOrFormat()
        }
    }

    private fun lintOrFormat() {
        if (stdin && patternsFromStdin != null) {
            throw MutuallyExclusiveGroupException(listOf("--stdin", "--patterns-from-stdin"))
        }
        val baseline = buildBaseline()
        LintAndFormatProcessor(
            format = format,
            ignoreAutocorrectFailures = ignoreAutocorrectFailures,
            forceLintAfterFormat = forceLintAfterFormat,
            rerunFormatAfterAutocorrect = !noRerunFormatAfterAutocorrect,
            relative = relative,
            limit = limit,
            ktLintRuleEngine = ktLintRuleEngine(),
            baseline = baseline,
            reporter = reporter(baseline),
        ).run(
            stdinCode = if (stdin) buildStdinCode() else null,
            patterns = arguments.replaceWithPatternsFromStdinOrDefaultPatternsWhenEmpty(),
        )
    }

    private fun ktLintRuleEngine(): KtLintRuleEngine =
        KtLintRuleEngine(
            ruleProviders = ruleProviders,
            editorConfigDefaults = editorConfigDefaults(ruleProviders),
            editorConfigOverride = buildEditorConfigOverride(),
        )

    private fun reporter(baseline: Baseline): ReporterV2 =
        ReporterAggregator(
            baseline,
            reporterConfigurations,
            color,
            colorName,
            stdin,
            format,
            relative,
        ).aggregatedReporter()

    private fun buildEditorConfigOverride(): EditorConfigOverride =
        EditorConfigOverride
            .EMPTY_EDITOR_CONFIG_OVERRIDE
            .applyIf(stdin && stdinPath.isNullOrBlank()) {
                logger.debug {
                    "Add editor config override to disable 'filename' rule which can not be used in combination with reading from " +
                        "<stdin>"
                }
                plus(FILENAME_RULE_ID.createRuleExecutionEditorConfigProperty() to RuleExecution.DISABLED)
            }

    private fun buildBaseline(): Baseline =
        if (stdin || baselinePath.isBlank()) {
            Baseline(status = Baseline.Status.DISABLED)
        } else {
            loadBaseline(baselinePath, BaselineErrorHandling.LOG)
        }

    private fun buildStdinCode(): Code =
        stdinPath
            ?.expandTildeToFullPath()
            ?.let { path -> Paths.get(path) }
            ?.let { Code.fromStdin(it) }
            ?: Code.fromStdin()

    private fun editorConfigDefaults(ruleProviders: Set<RuleV2Provider>): EditorConfigDefaults {
        val fullyExpandedEditorConfigPath =
            editorConfigPath
                ?.expandTildeToFullPath()
                ?.let { path -> Paths.get(path) }
        return EditorConfigDefaults.load(fullyExpandedEditorConfigPath, ruleProviders.propertyTypes())
    }

    private fun List<String>.replaceWithPatternsFromStdinOrDefaultPatternsWhenEmpty(): List<String> =
        when {
            patternsFromStdin != null -> {
                readPatternsFromStdin(patternsFromStdin!!)
                    .let { stdinPatterns ->
                        if (stdinPatterns.isNotEmpty()) {
                            if (isEmpty()) {
                                logger.debug { "Patterns read from 'stdin' due to flag '--patterns-from-stdin': $stdinPatterns" }
                            } else {
                                logger.warn {
                                    "Patterns specified at command line ($this) and patterns from 'stdin' due to flag " +
                                        "'--patterns-from-stdin' ($stdinPatterns) are merged"
                                }
                            }
                        }
                        // Note: it is okay in case both the original patterns and the patterns from stdin are empty
                        this.plus(stdinPatterns)
                    }
            }

            this.isEmpty() -> {
                logger.info { "Enable default patterns $DEFAULT_PATTERNS" }
                DEFAULT_PATTERNS
            }

            else -> {
                // Keep original patterns
                this
            }
        }

    internal val ruleProviders: Set<RuleV2Provider> by lazy {
        loadRuleProviders(rulesetJarPaths.toFilesURIList())
    }

    private fun configureLogger() =
        KotlinLogging
            .logger {}
            .setDefaultLoggerModifier { logger -> logger.level = minLogLevel }
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

    private fun readPatternsFromStdin(delimiter: String): Set<String> =
        String(System.`in`.readBytes())
            // The NUL byte (\u0000) is used as separator of file names, for example produced by the command "git diff --name-only  -z".
            // The output looks like:
            //     src/main/kotlin/Foo.kt\u0000src/main/kotlin/FooComposable.kt\u0000
            .split(delimiter.ifEmpty { "\u0000" })
            .let { patterns: List<String> ->
                patterns.filterTo(LinkedHashSet(patterns.size), String::isNotEmpty)
            }
}

// Do not convert to "val" as the function depends on PicoCli options which are not fully instantiated until the "run" method is started
internal fun List<String>.toFilesURIList() =
    map {
        val jarFile = File(it.expandTildeToFullPath())
        if (!jarFile.exists()) {
            logger.error { "File '$it' does not exist" }
            exitKtLintProcess(ExitCode.FILE_NOT_FOUND)
        }
        jarFile.toURI().toURL()
    }
