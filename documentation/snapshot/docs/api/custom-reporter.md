## Build a custom reporter
Take a look at [ktlint-cli-reporter-plain](https://github.com/ktlint/ktlint/tree/master/ktlint-cli-reporter-plain).

In short, all you need to do is to implement a
[ReporterV2](https://github.com/ktlint/ktlint/blob/master/ktlint-cli-reporter-core/src/main/kotlin/com/ktlint/ktlint/cli/reporter/core/api/ReporterV2.kt) and make it available by registering
a custom [ReporterProviderV2](https://github.com/ktlint/ktlint/blob/master/ktlint-cli-reporter-core/src/main/kotlin/com/ktlint/ktlint/cli/reporter/core/api/ReporterProviderV2.kt) using
`META-INF/services/com.pinterest.ktlint.cli.reporter.core.api.ReporterProviderV2`. Pack all of that into a JAR and you're done.

To load a custom (3rd party) reporter use `ktlint --reporter=name,artifact=/path/to/custom-ktlint-reporter.jar`
(see `ktlint --help` for more).

## Third party reporters

Known third-party reporters:

* [kryanod/ktlint-junit-reporter](https://github.com/kryanod/ktlint-junit-reporter) reports ktlint output as an xml file in JUnit format so that the ktlint report can be made visible on the Merge Request page.
* [musichin/ktlint-github-reporter](https://github.com/musichin/ktlint-github-reporter) uses [GitHub workflow commands](https://docs.github.com/en/actions/reference/workflow-commands-for-github-actions#setting-an-error-message) to set error messages for `ktlint` issues.
* [tobi2k/ktlint-gitlab-reporter](https://github.com/tobi2k/ktlint-gitlab-reporter) provides output in JSON format that can be parsed by GitLab automatically.
