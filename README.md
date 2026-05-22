<h1 align="center">
<a href="https://ktlint.github.io/ktlint/">
  <img src="https://cloud.githubusercontent.com/assets/370176/26518284/38b680da-4262-11e7-8d27-2b9e849fb55f.png"/>
</a>
</h1>

<p align="center">
<a href="https://kotlinlang.slack.com/messages/CKS3XG0LS"><img src="https://img.shields.io/badge/slack-@kotlinlang/ktlint-yellow.svg?logo=slack" alt="Join the chat at https://kotlinlang.slack.com"/></a>
<a href="https://github.com/pinterest/ktlint/actions/workflows/publish-snapshot-build.yml"><img src="https://github.com/pinterest/ktlint/actions/workflows/publish-snapshot-build.yml/badge.svg" alt="Build status"></a>
<a href="https://central.sonatype.com/artifact/com.pinterest.ktlint/ktlint-cli?smo=true"><img src="https://img.shields.io/maven-central/v/com.pinterest.ktlint/ktlint-cli.svg" alt="Maven Central"></a>
<a href="https://jitpack.io/#pinterest/ktlint"><img src="https://jitpack.io/v/pinterest/ktlint.svg" alt="JitPack"></a>
<a href="https://formulae.brew.sh/formula/ktlint"><img src="https://img.shields.io/homebrew/v/ktlint.svg" alt="HomeBrew"></a>
<a href="LICENSE"><img src="https://img.shields.io/github/license/pinterest/ktlint.svg" alt="License"></a>
<a href="https://ktlint.github.io/ktlint/"><img src="https://img.shields.io/badge/code%20style-%E2%9D%A4-FF4081.svg" alt="ktlint"></a>
</p>

<p align="center">
<a href="https://kotlinlang.org/">Kotlin</a> linter in spirit of <a href="https://github.com/standard/standard">standard/standard</a> (JavaScript) and <a href="https://golang.org/cmd/gofmt/">gofmt</a> (Go).  
</p>

## Key features

- No configuration required
- Built-in Rule sets
- Built-in formatter
- `.editorconfig` support
- Several built-in reporters: `plain`, `json`, `html` and `checkstyle`
- Executable jar
- Allows extension with custom rule sets and reporters

## Quick start

Follow steps below for a quick start with latest ktlint release.

* Step 1: Install with brew
  ```shell
  brew install ktlint
  ```
  See [download and verification from GitHub](https://ktlint.github.io/ktlint/latest/install/cli/#download-and-verification) or [other package managers](https://ktlint.github.io/ktlint/latest/install/cli/#package-managers) for alternative ways of installing ktlint. Or, use one of the [integrations like maven and gradle plugins](https://ktlint.github.io/ktlint/latest/install/integrations/).

* Step 2: Lint and format your code  
  All files with extension `.kt` and `.kts` in the current directory and below will be scanned. Problems will be fixed automatically when possible.
  ```shell title="Autocorrect style violations"
  ktlint --format
  # or
  ktlint -F
  ```
  See [cli usage](https://ktlint.github.io/ktlint/latest/install/cli/#command-line-usage) for a more extensive description on using ktlint.

## Documentation

<a href="https://ktlint.github.io/ktlint/">User guide</a>


### Legal

This project is no longer affiliated with Pinterest and is not affiliated with nor endorsed by JetBrains.
All code, unless specified otherwise, is licensed under the [MIT](https://opensource.org/licenses/MIT) license.  
Copyright (c) 2019 Pinterest, Inc.  
Copyright (c) 2016-2019 Stanley Shyiko.
