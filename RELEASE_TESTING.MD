# Release testing

Before releasing a new version of KtLint, the release candidate is tested on a selection of open source projects to improve the quality of the release, and the documentation is tested for dead links.

## Setup infrastructure

1. Create a directory in which the projects are grouped:
   ```shell
   mkdir sample-projects
   cd sample-projects
   ```
2. In the `sample-projects` directory, create a shell command script `exec-in-each-project.sh` with content below. This script is used to run the same command in each of the sample projects:
   ```shell
   # Examples of usage
   #
   #   Outstanding changes per project
   #     ./exec-in-each-project.sh "git status"
   #
   #   Rollback outstanding changes per project
   #     ./exec-in-each-project.sh "git reset --hard"
   #
   #   Run ktlint standard rules
   #     ktlint --version && ktlint "**/*.kt" -v --relative -F
   #
   #   Commit changes of standard rules:
   #     ./exec-in-each-project.sh "git commit -m \"ktlint (0.43.2) -F\""

   find . -maxdepth 1 -type d \( ! -name . \) -exec bash -c "cd '{}' && pwd && $@" \;
   ```
3. Clone the repositories of the sample projects (feel free to change this selection):
   ```shell
   git clone https://github.com/google/iosched.git
   git clone https://github.com/android/uamp.git
   git clone https://github.com/siempredelao/StaticAnalysisTest
   git clone https://github.com/Kotlin/kotlinx.coroutines.git
   git clone https://github.com/square/okhttp.git
   git clone https://github.com/detekt/detekt.git
   git clone https://github.com/ExpediaGroup/graphql-kotlin.git
   git clone https://github.com/Kotlin/kotlinx.serialization.git
   ```
4. Create an alias or script to run the latest released version of ktlint (note that this script will print the version as reference to which version is used):
   ```shell
   alias ktlint-prev="ktlint-1.7.1 $@" # Replace with the latest release version
   ```
   Note that `~/git/ktlint` is the directory in which the ktlint project is checked out and that `~/git/ktlint/ktlint` refers to the `ktlint` CLI-module.
5. Create an alias or script to run the latest development-version of ktlint (note that this script will print the version and the datetime of compilation as reference to which version is used):
   ```shell
   alias ktlint-dev="echo 'ktlint-dev version:';ls -aslh ~/git/ktlint/ktlint/ktlint-cli/build/run/ktlint;echo ' ';~/git/ktlint/ktlint/ktlint-cli/build/run/ktlint $@"
   ```
   Note that `~/git/ktlint/pinterest/ktlint` is the directory in which the ktlint project is checked out.

## Cleanup from previous testing

1. Reset the repositories of the sample projects:
   ```shell
   ./exec-in-each-project.sh "git reset --hard origin"
   ```
2. Optionally, pull latest changes of the sample projects:
   ```shell
   ./exec-in-each-project.sh "git pull"
   ```

## Testing a new release

Pre-requisites:
* All changes have been merged to master.
* Latest version of master has been pulled to the local machine.
* The latest released version of ktlint matches with command `ktlint`:
   ```shell
   ktlint-prev --version
   ```
* The latest version of master has been build on the local machine. And it is verified that the datetime of `ktlint-dev` (more or less) matches with the datetime of this compilation.
   ```shell
   ktlint-dev --version
   ```

Formatting projects in which ktlint is not used may result in a huge amount of fixes. The main focus of this test is to see what the effects are when upgrading ktlint in a project already formatted with latest released ktlint version.

1. Define `.editorconfig` in the integration test directory with content below. Also follow up the instructions mentioned:
   ```editorconfig
   root = true

   [graphql-kotlin/**/*.{kt,kts}]
   ktlint_standard_no-unused-imports = disabled

   [kotlinx.coroutines/**/*.{kt,kts}]
   ktlint_standard_no-wildcard-imports = disabled

   [*.{kt,kts}]
   # The open source projects that are used for release testing of ktlint contain a few '.editorconfig' files which need to
   # be changed:
   #   1) Disable the "root = true" property so that each project ultimately falls back on this file. In this way offending
   #      rules can be easily enabled/disabled for all test projects
   ktlint_code_style = ktlint_official
   ktlint_experimental = enabled
   ktlint_standard = enabled
   ```
2. Commit changes:
   ```shell
   ./exec-in-each-project.sh "git add --all && git commit -m \"Update .editorconfig to fallback to integration test settings\""
   ```
3. Format with previous (*latest released*) version of KtLint
   ```shell
   ktlint-prev --format --relative # Do not call this command via the "./exec-in-each-project.sh" script.
   ```
   Note: Ignore all output as this is the old version!
4. Commit changes:
   ```shell
   ./exec-in-each-project.sh "git add --all && git commit -m \"Format with previous ktlint version (round #)\""
   ```
   Repeat step 3 and 4 until no files are changed anymore. Although ktlint reruns up to 3 times in case new violations are introduced, it can still happen that not all violations have been fixed with a single invocation.
5. Build baseline file with previous (*latest released*) version of Lint (when building with format the offsets of the error are not saved correctly in the baseline) so that all errors which cannot be autocorrected are ignored.
   ```shell
   rm baseline.xml
   ktlint-prev --baseline=baseline.xml --relative # Do not call this command via the "./exec-in-each-project.sh" script.
   ```
   Note: Ignore all output as this is the old version! Inspect the baseline roughly to verify that it only contains errors that cannot be autocorrected.
6. Format the sample projects with the previous (*latest released*) ktlint version:
   ```shell
   ktlint-prev -F --baseline=baseline.xml --relative # Do not call this command via the "./exec-in-each-project.sh" script.
   ```
   Note: The output should not report any violations, as the baseline should suppress every error that could not autocorrected.
7. Check that besides the `baseline.xml` no files are changed (in step 4 and 5 all violations which could be autocorrected have already been committed). Remaining violations which could not be autocorrected are saved in the `baseline.xml` which is stored outside the project directories.
   ```shell
   ./exec-in-each-project.sh "git status"
   ```
8. Lint with *latest development* version:
   ```shell
   ktlint-dev --baseline=baseline.xml --relative # Do not call this command via the "./exec-in-each-project.sh" script as we want to use the one combined baseline.xml file for all projects.
   ```
   Inspect the output roughly (detailed inspection is done when formatting):
   * Is the amount of logging messages comparable to before? If not, are the changes intended?
   * Are violations related to rules that have actually been added or changed?
   * If you see an error like below, then this version obviously may *not* be released. It is best to fix this error before continuing with testing and validating!
    ```plain
    Internal Error (...) in file '...' at position '0:0. Please create a ticket at https://github.com/pinterest/ktlint/issues ...
    ```
9. Format with *latest development* version:
   ```shell
   ktlint-dev -F --baseline=baseline.xml --relative # Do not call this command via the "./exec-in-each-project.sh" script as we want to use the one combined baseline.xml file for all projects.
   ```
   Inspect the output carefully:
   * If you see an error like below, then this version obviously may *not* be released. It is best to fix this error before continuing with testing and validating!
    ```plain
    Internal Error (...) in file '...' at position '0:0. Please create a ticket at https://github.com/pinterest/ktlint/issues ...
    ```
   * Usually it helps to disable all rules that emit violations, except one of those rules. In this way it is possible to evaluate the changes rule by rule.
   * Ideally, no violations are shown. This means that all violations have been autocorrected. Note that violations might pop up that previously were suppressed via the baseline. This can happen as due to code changes, the references in the baseline.xml no longer match with the positions where they occur. First check the code changes, and regenerating the baseline before verifying the next can be a helpful approach.
   * Violations which could not be autocorrected should be validated for correctness but do not block the release as most likely this is intended behavior.
   * If a violation is shown which is not marked as being "cannot be autocorrected" this means that during autocorrect of another violation a new violations has been introduced. This should be fixed before releasing especially when the next format introduces the original violation again which of course would result in an endless loop.
10. Inspect all fixed violations. Of course, inspection similar violations tens of times does not make sense. At least check different types of violations a couple of times. Commit changes which do not need to be inspected again:
    ```shell
    ./exec-in-each-project.sh "git add --all && git commit -m \"Fixed with latest development version\""
    ```
11. Rerun lint with *latest development* version and create a new baseline:
     ```shell
     rm baseline.xml
     ktlint-dev --baseline=baseline.xml --relative # Do not call this command via the "./exec-in-each-project.sh" script as we want to use the one combined baseline.xml file for all projects.
     ```
    As the baseline is removed, thousands of violations are to be reported. Check at least in the summary that no internal errors are thrown like below:
    ```plain
    Internal Error (...) in file '...' at position '0:0. Please create a ticket at https://github.com/pinterest/ktlint/issues ...
    ```
12. Rerun lint with *latest development* version and verify that no violations are reported:
   ```shell
   ktlint-dev --baseline=baseline.xml --relative # Do not call this command via the "./exec-in-each-project.sh" script as we want one combined baseline.xml file for all projects.
   ```
    No violations should be reported in this run.
13. Rerun format with *latest development* version without baseline:
     ```shell
     ktlint-dev -F --relative # Do not call this command via the "./exec-in-each-project.sh" script as we want to use the one combined baseline.xml file for all projects.
     ```
    As the baseline is removed, thousands of violations are to be expected. Check at least in the summary that no internal errors are thrown like below:
    ```plain
    Internal Error (...) in file '...' at position '0:0. Please create a ticket at https://github.com/pinterest/ktlint/issues ...
    ```
    As the baseline is removed, thousands of violations are to be reported and/or autocorrected. Check at least in the summary that no internal errors are thrown like below:

## Checking documentation
[The documentation for KtLint](https://ktlint.github.io/ktlint/) should be checked for dead links.
Follow the instructions for building the documentation in `/docs/readme.md`, and use a tool such as [Broken Link Checker Tool](https://www.deadlinkchecker.com/website-dead-link-checker.asp) to find broken links.
