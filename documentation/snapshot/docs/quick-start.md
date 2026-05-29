Follow steps below for a quick start with latest ktlint release.

## Step 1: Install with brew
```shell
brew install ktlint
```
See [download and verification from GitHub](install/cli.md#download-and-verification) for native executables and the executable JAR, or [other package managers](install/cli.md#package-managers) for alternative ways of installing ktlint. Or, use one of the [integrations like maven and gradle plugins](install/integrations.md).

## Step 2: Lint and format your code
All files with extension `.kt` and `.kts` in the current directory and below will be scanned. Problems will be fixed automatically when possible.
```shell title="Autocorrect style violations"
ktlint --format
# or
ktlint -F
```
See [cli usage](install/cli.md#command-line-usage) for a more extensive description on using ktlint.
