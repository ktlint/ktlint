# Welcome to Ktlint

<p style="text-align: center;">
<a href="https://ktlint.github.io/ktlint/">
  <img src="https://cloud.githubusercontent.com/assets/370176/26518284/38b680da-4262-11e7-8d27-2b9e849fb55f.png" alt="KtLint logo with tag line 'An anti-bikeshedding Kotlin linter with built-in formatter.'"/>
</a>
</p>
<p style="text-align: center;">
  <a href="https://kotlinlang.slack.com/messages/CKS3XG0LS">
    <img src="https://img.shields.io/badge/slack-@kotlinlang/ktlint-yellow.svg?logo=slack" alt="Join the chat at https://kotlinlang.slack.com"/>
  </a>
  <a href="https://github.com/ktlint/ktlint/actions?query=workflow%3A%22Publish+snapshot+build%22">
    <img src="https://github.com/ktlint/ktlint/workflows/Publish%20snapshot%20build/badge.svg" alt="Build status snapshot">
  </a>
  <a href="https://central.sonatype.com/artifact/io.github.ktlint.core/ktlint-cli?smo=true">
    <img src="https://img.shields.io/maven-central/v/io.github.ktlint.core/ktlint-cli.svg" alt="Maven Central">
  </a>
  <a href="https://github.com/ktlint/ktlint?tab=MIT-1-ov-file">
    <img src="https://img.shields.io/github/license/ktlint/ktlint.svg" alt="License">
  </a>
  <a href="https://ktlint.github.io/ktlint/">
    <img src="https://img.shields.io/badge/ktlint%20code--style-%E2%9D%A4-FF4081.svg" alt="ktlint">
  </a>
</p>
<p style="text-align: center;">
<a href="https://kotlinlang.org/">Kotlin</a> linter in spirit of <a href="https://github.com/feross/standard">feross/standard</a> (JavaScript) and <a href="https://golang.org/cmd/gofmt/">gofmt</a> (Go).  
</p>

## Features

- **No configuration required**  
 `ktlint` aims to capture the [Kotlin coding conventions](https://kotlinlang.org/docs/reference/coding-conventions.html) and [Android Kotlin Style Guide](https://android.github.io/kotlin-guides/style.html). In some aspects `ktlint` is stricter[*](https://github.com/ktlint/ktlint/issues/284#issuecomment-425177186).
- **Rule sets**  
  `ktlint` offers a `standard` rule set. Next to this, it is easy to provide [custom rule sets](api/custom-rule-set.md).
- **.editorconfig**  
  Some rules do allow further configuration, but in all cases a reasonable default is set when not provided. `ktlint` primarily uses the [.editorconfig file](rules/configuration-ktlint.md) to read default `.editorconfig`, IntelliJ IDEA specific and Ktlint specific properties.
- **Disable rules**  
  If need be, rules can be disabled easily[*](faq.md#how-do-i-enable-or-disable-a-rule).
- **Built-in formatter**  
  Most lint violations don't need to be fixed manually. `ktlint` has a built-in formatter which fixes violations when possible. Some violations can not be fixed in a deterministic way, and need manual action.
- **Customizable output**
  Several reporters are available out-of-the-box: `plain` (+ `plain?group_by_file`), `plain-summary`, `json`, `html` and `checkstyle`.
  It's also easy to [create a custom reporter](api/custom-reporter.md).
- **Executable jar and native binaries**  
  `ktlint` is released as an executable jar with all dependencies included and as native executables for Linux, macOS, and Windows.

## Legal

This project is not affiliated with nor endorsed by JetBrains.  
All code, unless specified otherwise, is licensed under the [MIT](https://opensource.org/licenses/MIT) license.  
Copyright (c) 2026 Ktlint  
Copyright (c) 2019-2026 Pinterest, Inc.  
Copyright (c) 2016-2019 Stanley Shyiko.
