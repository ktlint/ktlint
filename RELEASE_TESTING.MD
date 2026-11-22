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
   alias ktlint-prev="ktlint-1.8.0 $@" # Replace with the latest release version
   ```
   Note that `~/git/ktlint` is the directory in which the ktlint project is checked out and that `~/git/ktlint/ktlint` refers to the `ktlint` CLI-module.
5. Create an alias or script to run the latest development-version of ktlint (note that this script will print the version and the datetime of compilation as reference to which version is used):
   ```shell
   alias ktlint-dev="echo 'ktlint-dev version:';ls -aslh ~/git/ktlint/ktlint/ktlint-cli/build/run/ktlint;echo ' ';~/git/ktlint/ktlint/ktlint-cli/build/run/ktlint $@"
   ```
   Note that `~/git/ktlint/ktlint/ktlint` is the directory in which the ktlint project is checked out.

## Cleanup from previous testing

1. Reset the repositories of the sample projects:
   ```shell
   ./exec-in-each-project.sh "git reset --hard origin"
    # Remove "root = true" from all ".editorconfig" files in the subdirectories
    find . -mindepth 2 -name ".editorconfig" -exec sed -i '' '/^root = true$/d' {} +
    ./exec-in-each-project.sh "git add --all && git commit -m \"Remove 'root = true' from '.editorconfig'\""
   ```
2. Optionally, pull latest changes of the sample projects:
   ```shell
   ./exec-in-each-project.sh "git pull"
    # Remove "root = true" from all ".editorconfig" files in the subdirectories
    find . -mindepth 2 -name ".editorconfig" -exec sed -i '' '/^root = true$/d' {} +
    ./exec-in-each-project.sh "git add --all && git commit -m \"Remove 'root = true' from '.editorconfig'\""
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

   [kotlinx.serialization/**/*.{kt,kts}]
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
2. Remove the line `root=true` for each `.editorconfig` of the subprojects but keep the line in the `.editorconfig` file in the root directory of the sample projects.
3. Commit changes:
   ```shell
   ./exec-in-each-project.sh "git add --all && git commit -m \"Update .editorconfig to fallback to integration test settings\""
   ```
4. Format with previous (*latest released*) version of KtLint
   ```shell
   clear
   # Step 4
   ktlint-prev --format --relative # Do not call this command via the "./exec-in-each-project.sh" script.
   # Finished step 4
   ```
   Note: Ignore all output as this is the old version!
5. Commit changes:
   ```shell
   #!/bin/bash
   clear
   echo "Step 5"
   ./exec-in-each-project.sh "git add --all && git commit -m \"Format with previous ktlint version (step 4)\""
   echo "Finished step 5"
   ```
   Repeat step 4 and 5 until no files are changed anymore. Although ktlint reruns up to 3 times in case new violations are introduced, it can still happen that not all violations have been fixed with a single invocation.
6. Build baseline file with previous (*latest released*) version of Lint (when building with format the offsets of the error are not saved correctly in the baseline) so that all errors which cannot be autocorrected are ignored.
   ```shell
   clear
   echo "Step 6"
   rm baseline.xml
   ktlint-prev --baseline=baseline.xml --relative # Do not call this command via the "./exec-in-each-project.sh" script.
   echo "Finished step 6"
   ```
   Note: Ignore all output as this is the old version! Inspect the baseline roughly to verify that it only contains errors that cannot be autocorrected.
7. Format the sample projects with the previous (*latest released*) ktlint version:
   ```shell
   clear
   echo "Step 7"
   ktlint-prev -F --baseline=baseline.xml --relative # Do not call this command via the "./exec-in-each-project.sh" script.
   echo "Finished step 7"
   ```
   Note: The output should not report any violations, as the baseline should suppress every error that could not be autocorrected.
8. Check that besides the `baseline.xml` no files are changed (in step 4 and 5 all violations which could be autocorrected have already been committed). Remaining violations which could not be autocorrected are saved in the `baseline.xml` which is stored outside the project directories.
   ```shell
   clear
   echo "Step 8"
   ./exec-in-each-project.sh "git status"
   echo "Finished step 8"
   ```
9. Lint with *latest development* version:
   ```shell
   clear
   echo "Step 9"
   rm ktlint-report-plain-summary.txt
   # Run ktlint (lint) and only write the summary to both console and file
   ktlint-dev --baseline=baseline.xml --relative --reporter=plain-summary --reporter=plain-summary,output=ktlint-report-plain-summary.txt
   echo -e "\n### Disable all rules that emitted a violation in Step 9 for release testing" >> .editorconfig
   cat ktlint-report-plain-summary.txt | sed -n 's/^[[:space:]]*standard:\([^:]*\): \(.*\)/ktlint_standard_\1 = disabled # \2/p' >> .editorconfig
   echo "Finished step 9"
   ```
   Inspect the output roughly (detailed inspection is done when formatting):
   * Important: whenever a file would be legitly changed, and a line break has been added or removed, it may affect suppressions stored in the baseline file. This can result in other changes in the file which previously were not reported/fixed.
   * Is the amount of logging messages comparable to before? If not, are the changes intended?
   * Are violations related to rules that have actually been added or changed?
   * If you see an error like below, then this version obviously may *not* be released. It is best to fix this error before continuing with testing and validating!
     ```plain
     Internal Error (...) in file '...' at position '0:0. Please create a ticket at https://github.com/ktlint/ktlint/issues ...
     ```
   Note that the `.editorconfig` file has been changed to disable all rules that have emitted a violation in this step.
10. Format with *latest development* version but disabling all rules that resulted in changes in last lint command:
    ```shell
    clear
    echo "Step 10"
    # Run ktlint (format) - NOTE: some rules may have been disabled (see '.editorconfig')
    ktlint-dev -F --baseline=baseline.xml --relative --ignore-autocorrect-failures
    echo "Finished step 10"
    ```
    Inspect the output carefully:
    * In step 9 all rules have been disabled that emitted violations (so in the first run there should be no violations). Now enable those rules one by one, so that it is easier to inspect the output. Note that whenever a file is legitly changed, and a line break has been added or removed, it may affect suppressions stored in the baseline file. This can result in other changes in the file which previously were suppressed by the  baseline. Worst case, it might even be needed to run ktlint format multiple times before formatting stabilizes.
    * If you see an error like below, then this version obviously may *not* be released. It is best to fix this error before continuing with testing and validating!
     ```plain
     Internal Error (...) in file '...' at position '0:0. Please create a ticket at https://github.com/ktlint/ktlint/issues ...
     ```
    * Ideally, no violations are shown. This means that all violations have been autocorrected. Note that violations might pop up that previously were suppressed via the baseline. This can happen as due to code changes, the references in the baseline.xml no longer match with the positions where they occur. First check the code changes, and regenerating the baseline before verifying the next can be a helpful approach.
    * Violations which could not be autocorrected should be validated for correctness but do not block the release as most likely this is intended behavior.
    * If a violation is shown which is not marked as being "cannot be autocorrected" this means that during autocorrect of another violation a new violations has been introduced. This should be fixed before releasing especially when the next format introduces the original violation again which of course would result in an endless loop.
11. Inspect all fixed violations. Of course, inspection similar violations tens of times does not make sense. At least check different types of violations a couple of times. Commit changes which do not need to be inspected again:
    ```shell
    clear
    echo "Step 11"
    ./exec-in-each-project.sh "git add --all && git commit -m \"Fixed with latest development version\""
    echo "Finished step 11"
    ```
12. Reset the repositories to undo all changes made in step 10 and rerun lint with *latest development* version and create a new baseline:
    ```shell
    clear
    echo "Step 12"
    rm baseline.xml
    # Restore original test repositories
    ./exec-in-each-project.sh "git reset --hard origin"
    # Remove "root = true" from all ".editorconfig" files in the subdirectories
    find . -mindepth 2 -name ".editorconfig" -exec sed -i '' '/^root = true$/d' {} +
    ./exec-in-each-project.sh "git add --all && git commit -m \"Remove 'root = true' from '.editorconfig'\""
    # Run Ktlint
    ktlint-dev --baseline=baseline.xml --relative # Do not call this command via the "./exec-in-each-project.sh" script as we want to use the one combined baseline.xml file for all projects.
    echo "Finished step 12"
    ```
    As the baseline is removed, thousands of violations are to be reported. Check at least in the summary that no internal errors are thrown like below:
    ```plain
    Internal Error (...) in file '...' at position '0:0. Please create a ticket at https://github.com/ktlint/ktlint/issues ...
    ```
13. Rerun lint with *latest development* version and verify that no violations are reported:
    ```shell
    clear
    echo "Step 13"
    ktlint-dev --baseline=baseline.xml --relative # Do not call this command via the "./exec-in-each-project.sh" script as we want one combined baseline.xml file for all projects.
    echo "Finished step 13"
    ```
    No violations should be reported in this run.
14. Rerun format with *latest development* version without baseline:
    ```shell
    clear
    echo "Step 14"
    ktlint-dev -F --relative # Do not call this command via the "./exec-in-each-project.sh" script as we want to use the one combined baseline.xml file for all projects.
    echo "Finished step 14"
    ```
    As the baseline is removed, thousands of violations are to be expected. Check at least in the summary that no internal errors are thrown like below:
    ```plain
    Internal Error (...) in file '...' at position '0:0. Please create a ticket at https://github.com/ktlint/ktlint/issues ...
    ```
15. Inspect some random files
    ```shell
    clear
    echo "Step 15"
    ./exec-in-each-project.sh "git add --all && git commit -m \"Fixed with latest development version\""
    echo "Finished step 15"
    ```
16. Repeat step 14 and 15. Ideally, as of Ktlint 2.x, no new violations are found that can be autocorrected.
17. Rerun format with *latest development* on original version and force to lint after formatting a file to ensure that changed files can still be parsed in the next run:
    ```shell
    clear
    echo "Step 17"
    # Restore original test repositories
    ./exec-in-each-project.sh "git reset --hard origin"
    # Remove "root = true" from all ".editorconfig" files in the subdirectories
    find . -mindepth 2 -name ".editorconfig" -exec sed -i '' '/^root = true$/d' {} +
    ./exec-in-each-project.sh "git add --all && git commit -m \"Remove 'root = true' from '.editorconfig'\""
    # Run Ktlint
    ktlint-dev -F --relative --force-lint-after-format
    echo "Finished step 17"
    ```
    Ktlint should terminate without error. If a lint error is found after file is formatted, the execution stops directly.
18. Rerun format with *latest development* on original version and search logging for message that not all violations could be fixed in a single format run:
    ```shell
    clear
    echo "Step 18"
    # Restore original test repositories
    ./exec-in-each-project.sh "git reset --hard origin"
    # Remove "root = true" from all ".editorconfig" files in the subdirectories
    find . -mindepth 2 -name ".editorconfig" -exec sed -i '' '/^root = true$/d' {} +
    ./exec-in-each-project.sh "git add --all && git commit -m \"Remove 'root = true' from '.editorconfig'\""
    # Run Ktlint and grep the log for all files that have more unresolved lint errors that can be autocorrected
    ktlint-dev -F --relative | grep -e "Format was not able to resolve all violations which" | sed -n 's/.*in file \(.*\) in [0-9]* consecutive.*/\1/p'
    echo "Finished step 18"
    ```
    Ideally all lint errors that can be autocorrected will be autocorrected in one run of ktlint cli. For each file that is listed above, research which errors were not resolved by rerunning lint on those specific files:
    `ktlint-dev --relative ***filename***`
19. Rerun format with *latest development* on original version and disable the automatic rerun when a format error was autocorrected:
    ```shell
    clear
    echo "Step 19"
    # Restore original test repositories
    ./exec-in-each-project.sh "git reset --hard origin"
    # Remove "root = true" from all ".editorconfig" files in the subdirectories
    find . -mindepth 2 -name ".editorconfig" -exec sed -i '' '/^root = true$/d' {} +
    ./exec-in-each-project.sh "git add --all && git commit -m \"Remove 'root = true' from '.editorconfig'\""
    # Run Ktlint without automatic rerun of format after autocorrect
    ktlint-dev -F --relative --no-rerun-format-after-autocorrect --ignore-autocorrect-failures
    echo "Finished step 19"
    ```
    Commit the changes, and rerun same ktlint command.
    ```shell
    clear
    echo "Step 19b"
    # Save result previous step
    ./exec-in-each-project.sh "git add --all && git commit -m \"Ktlint -F --no-rerun-format-after-autocorrect'\""
    # Run Ktlint without automatic rerun of format after autocorrect
    ktlint-dev --relative -F --no-rerun-format-after-autocorrect --ignore-autocorrect-failures
    echo "Finished step 19b"
    ```
    Repeat until no more violations are fixed.

## Checking documentation
[The documentation for KtLint](https://ktlint.github.io/ktlint/) should be checked for dead links.
Follow the instructions for building the documentation in `/docs/readme.md`, and use a tool such as [Broken Link Checker Tool](https://www.deadlinkchecker.com/website-dead-link-checker.asp) to find broken links.
