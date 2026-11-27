# Changelog
All notable changes to this project will be documented in this file.
This project adheres to [Semantic Versioning](https://semver.org/).

## [2.0.0-ALPHA-5] - 2026-11-23

IMPORTANT: This project is no longer maintained by Pinterest. As a result the Maven coordinates of the project have changed from `com.pinterest.ktlint` to `io.github.ktlint.core`. Read more in the [documentation](https://ktlint.github.io/ktlint/dev-snapshot/api/migrate-to-ktlint-2.x/).

Note: Also, see changelist of [2.0.0-ALPHA-4](https://github.com/ktlint/ktlint/releases/tag/2.0.0-ALPHA-4) for other changes since version `1.8`.

IMPORTANT: This version of Ktlint is using a release candidate of the Kotlin compiler `2.5.0-RC2`. Be careful when using this in your production environment. It should be safe for running Ktlint CLI as it runs standalone. Do not use or provide this version of Ktlint without taking proper precautions. 

### 💔 Breaking changes

* Switch from kotlin-compiler-embeddable to Kotlin Analysis API dependencies - [#3437](https://github.com/ktlint/ktlint/pull/3437), by @3flex

* Stop using KtStubElementTypes - [#3398](https://github.com/ktlint/ktlint/pull/3398), by @3flex

* Convert enum values to upper case (breaking change) - [#3407](https://github.com/ktlint/ktlint/pull/3407), by @paul-dingemans

* Move the lint violation emitter from functions to Rule class (breaking change) - [#3424](https://github.com/ktlint/ktlint/pull/3424), by @paul-dingemans

### 🆕 Features

* Add KDoc comment format rule - [#3369](https://github.com/ktlint/ktlint/pull/3369), by @exijn

* Add property `ktlint_indent_explicit_constructor` to `indent` rule - [#3392](https://github.com/ktlint/ktlint/pull/3392), by @paul-dingemans

* Improve statistics of Ktlint CLI PlainReporter - [#3455](https://github.com/ktlint/ktlint/pull/3455), by @paul-dingemans

### 🔧 Fixes

* fix(indent): accept aligned where constraints with tabs - [#3374](https://github.com/ktlint/ktlint/pull/3374), by @autaut03

* Move comma in a multiline constraint type list after the constraint type - [#3378](https://github.com/ktlint/ktlint/pull/3378), by @paul-dingemans

* Fix references to `io.github.ktlint.core` in `ktlint-ruleset-template` - [#3379](https://github.com/ktlint/ktlint/pull/3379), by @paul-dingemans

* Fix alignment of same-line WHERE clause in class declarations - [#3381](https://github.com/ktlint/ktlint/pull/3381), by @ZaMan0806

* Check type-parameter-list-spacing for generic extension properties - [#3384](https://github.com/ktlint/ktlint/pull/3384), by @ZaMan0806

* Wrap `where` keyword in a multiline type constraint list - [#3382](https://github.com/ktlint/ktlint/pull/3382), by @paul-dingemans

* Fix replacing text of last node in the file in rule `no-consecutive-blank-lines` - [#3404](https://github.com/ktlint/ktlint/pull/3404), by @paul-dingemans

* Fix `no-unused-imports` rule - [#3405](https://github.com/ktlint/ktlint/pull/3405), by @paul-dingemans

* Update glob for ktlint self-checks - [#3410](https://github.com/ktlint/ktlint/pull/3410), by @3flex

* Prevent loss of original file in case of error after truncate - [#3418](https://github.com/ktlint/ktlint/pull/3418), by @paul-dingemans

* Do not wrap after context receiver in function type reference in property value - [#3425](https://github.com/ktlint/ktlint/pull/3425), by @paul-dingemans

* Do not remove arrow from function literal when block ends with 'Unit' keyword - [#3426](https://github.com/ktlint/ktlint/pull/3426), by @paul-dingemans

* Fix max-line-length check in call-expression-wrapping - [#3428](https://github.com/ktlint/ktlint/pull/3428), by @paul-dingemans

* Fix kdoc-delimiter rule single line KDoc - [#3429](https://github.com/ktlint/ktlint/pull/3429), by @paul-dingemans

* Fix when-entry-bracing in case arrow of when condition with expression is placed on separate line - [#3430](https://github.com/ktlint/ktlint/pull/3430), by @paul-dingemans

* Do not log warning when all lint violations are fixed in last possible run - [#3431](https://github.com/ktlint/ktlint/pull/3431), by @paul-dingemans

* Fix check on redundant arrow in lambda argument - [#3432](https://github.com/ktlint/ktlint/pull/3432), by @paul-dingemans

* Do not wrap call expression inside string template - [#3449](https://github.com/ktlint/ktlint/pull/3449), by @paul-dingemans

* Fix when-entry-bracing to add braces to all entries in a single run - [#3443](https://github.com/ktlint/ktlint/pull/3443), by @maoxor

* Ignore consecutive spaces when they are trailing spaces - [#3450](https://github.com/ktlint/ktlint/pull/3450), by @paul-dingemans

* Fix detection of when-entry-bracing - [#3451](https://github.com/ktlint/ktlint/pull/3451), by @paul-dingemans

* Do not insert a second newline before the closing quotes of a raw string literal - [#3440](https://github.com/ktlint/ktlint/pull/3440), by @wleeaf

* Fix crash in wrapping rule on a multiline where clause of a class - [#3444](https://github.com/ktlint/ktlint/pull/3444), by @maoxor

* Do not count lines after a newline in a string template in `property-wrapping` - [#3461](https://github.com/ktlint/ktlint/pull/3461), by @1AbdullahButt

* Fix semicolon rule joining statements and leaving a trailing space - [#3462](https://github.com/ktlint/ktlint/pull/3462), by @yamamoto7

* Remove trailing space after unnecessary semicolon - [#3463](https://github.com/ktlint/ktlint/pull/3463), by @paul-dingemans

* Fix autocorrect in `colon-spacing` for EOL comment between class and colon of supertype list - [#3460](https://github.com/ktlint/ktlint/pull/3460), by @Hexay

* Do not report non-const vals in a companion block in property-naming - [#3472](https://github.com/ktlint/ktlint/pull/3472), by @halotukozak

* Format numbers in PlainReporter summary with thousands separator - [#3474](https://github.com/ktlint/ktlint/pull/3474), by @paul-dingemans

* Fix indent of comment on separate line below a function body expression - [#3476](https://github.com/ktlint/ktlint/pull/3476), by @paul-dingemans

* Fix indent for EOL comment in `chain-wrapping` rule - [#3478](https://github.com/ktlint/ktlint/pull/3478), by @paul-dingemans

* Fix autocorrect for EOL comment in `spacing-between-declarations-with-annotations` rule - [#3479](https://github.com/ktlint/ktlint/pull/3479), by @paul-dingemans

* Retain whitespace in `function-expression-body` - [#3482](https://github.com/ktlint/ktlint/pull/3482), by @paul-dingemans

* Fix warning that ktlint format was not able to resolve all errors - [#3483](https://github.com/ktlint/ktlint/pull/3483), by @paul-dingemans

### 📦 Dependencies

* Update dependency com.google.jimfs:jimfs to v1.3.2 - [#3372](https://github.com/ktlint/ktlint/pull/3372), by @renovate[bot]

* Update dependency io.github.detekt.sarif4k:sarif4k to v0.8.0 - [#3376](https://github.com/ktlint/ktlint/pull/3376), by @renovate[bot]

* Update actions/setup-java action to v6 - [#3377](https://github.com/ktlint/ktlint/pull/3377), by @renovate[bot]

* Update dependency dev.drewhamilton.poko:poko-gradle-plugin to v0.23.2 - [#3393](https://github.com/ktlint/ktlint/pull/3393), by @renovate[bot]

* Update dependency org.slf4j:slf4j-simple to v2.0.20 - [#3400](https://github.com/ktlint/ktlint/pull/3400), by @renovate[bot]

* Update kotlin monorepo to v2.4.21 - [#3413](https://github.com/ktlint/ktlint/pull/3413), by @renovate[bot]

* Update plugin com.gradle.develocity to v4.6.1 - [#3435](https://github.com/ktlint/ktlint/pull/3435), by @renovate[bot]

* Update dependency org.jetbrains.dokka:dokka-gradle-plugin to v2.3.0 - [#3454](https://github.com/ktlint/ktlint/pull/3454), by @renovate[bot]

* Update dependency io.github.oshai:kotlin-logging-jvm to v8.0.7 - [#3464](https://github.com/ktlint/ktlint/pull/3464), by @renovate[bot]

* Update dependency com.github.ajalt.clikt:clikt to v5.2.1 - [#3473](https://github.com/ktlint/ktlint/pull/3473), by @paul-dingemans

* Update dependency ch.qos.logback:logback-classic to v1.6.8 - [#3477](https://github.com/ktlint/ktlint/pull/3477), by @renovate[bot]

* Update Gradle to v9.9.2 - [#3480](https://github.com/ktlint/ktlint/pull/3480), by @renovate[bot]

* Update to Kotlin 2.5.0 - [#3436](https://github.com/ktlint/ktlint/pull/3436), by @3flex

### 💬 Other

* Set GraalVM version instead of Java version - [#3390](https://github.com/ktlint/ktlint/pull/3390), by @paul-dingemans

* Address deprecation of Kotlin DSL delegated properties in Gradle build scripts - [#3408](https://github.com/ktlint/ktlint/pull/3408), by @3flex

* Do not set kotlin version explicitly in ktlint-ruleset-template - [#3420](https://github.com/ktlint/ktlint/pull/3420), by @paul-dingemans

* Add detekt for static analysis on ktlint's own codebase - [#3395](https://github.com/ktlint/ktlint/pull/3395), by @kimseongchan0914

* Cleanup based on code inspection IntelliJ IDEA - [#3423](https://github.com/ktlint/ktlint/pull/3423), by @paul-dingemans

* Set group name for all projects - [#3411](https://github.com/ktlint/ktlint/pull/3411), by @3flex

* Use kotlin-dsl plugin in build-logic - [#3446](https://github.com/ktlint/ktlint/pull/3446), by @3flex

* Clean up - remove outdated todo - [#3448](https://github.com/ktlint/ktlint/pull/3448), by @paul-dingemans

* Upgrade JVM toolchain in ktlint-ruleset-template to java 25 - [#3452](https://github.com/ktlint/ktlint/pull/3452), by @paul-dingemans

* Minimize automatic format runs - [#3453](https://github.com/ktlint/ktlint/pull/3453), by @paul-dingemans

* Do not fail CodeQL analysis when CodeQL is lagging behind with new Kotlin version - [#3470](https://github.com/ktlint/ktlint/pull/3470), by @paul-dingemans

* Publish release and snapshot documentation sequentially - [#3471](https://github.com/ktlint/ktlint/pull/3471), by @paul-dingemans

* Temporarily use kotlinDev plugin to build and release with Kotlin "2.5.0-RC*" - [#3485](https://github.com/ktlint/ktlint/pull/3485), by @paul-dingemans

## [2.0.0-ALPHA-4] - 2026-08-20

IMPORTANT: This project is no longer maintained by Pinterest. As a result the Maven coordinates of the project have changed from `com.pinterest.ktlint` to `io.github.ktlint.core`. Read more in the [documentation](https://ktlint.github.io/ktlint/dev-snapshot/api/migrate-to-ktlint-2.x/).

Note: Earlier alpha versions are not available as GitHub, nor as Maven Central download due to problems in fixing the release pipeline.

### 🆕 Features

* Execute all rules on same AST Node before proceeding to next AST Node (possibly breaking change) - [#3252](https://github.com/ktlint/ktlint/pull/3252), by @paul-dingemans  
Due to changing the order in which rules are executed, this is a potentially breaking change when rules are not well written. Normal rules only manipulate the AST for nodes that have not yet been visited. In case a rule for a given AST Node decides to manipulate an AST Node that already has been visited before, this might result in a failure.

* Add RuleV2 to enforce autocorrect approve visitor - [#3241](https://github.com/ktlint/ktlint/pull/3241), by @paul-dingemans

* Add rule `call-expression-wrapping`, and improve wrapping in `chain-method-continuation` and `function-literal` - [#3253](https://github.com/ktlint/ktlint/pull/3253), by @paul-dingemans

* Add blank line check between package and import statements - [#3266](https://github.com/ktlint/ktlint/pull/3266), by @maheen8q

* Add experimental rule `lambda-return` - [#3274](https://github.com/ktlint/ktlint/pull/3274), by @paul-dingemans

* Update kotlin to v2.4.0 / set java compilation to 26 - [#3316](https://github.com/ktlint/ktlint/pull/3316), by @paul-dingemans

* Enable rule `blank-line-before-declaration` by default for `android_studio` - [#3318](https://github.com/ktlint/ktlint/pull/3318), by @paul-dingemans

* Add experimental rules for blank lines between sections - [#3319](https://github.com/ktlint/ktlint/pull/3319), by @paul-dingemans

* feat: add GraalVM native-image binaries to release (linux, macOS, Windows) - [#3284](https://github.com/ktlint/ktlint/pull/3284), by @zeitlinger

* Add experimental rule no-blank-line-at-start-of-file - [#3361](https://github.com/ktlint/ktlint/pull/3361), by @paul-dingemans

### 🔧 Fixes

* Handle trailing comments on when entries in `BlankLineBetweenWhenConditions` - [#3177](https://github.com/ktlint/ktlint/pull/3177), by @jonapoul

* Keep `no-unused-import` as opt-in - [#3192](https://github.com/ktlint/ktlint/pull/3192), by @paul-dingemans

* Fix handling of EOL-comment in `paren-spacing` - [#3236](https://github.com/ktlint/ktlint/pull/3236), by @SubhrajyotiSen

* Fix Square brackets spacing rule heading level - [#3244](https://github.com/ktlint/ktlint/pull/3244), by @moon-dylan

* Respect suppressions on "ktlint:standard:max-line-length" - [#3255](https://github.com/ktlint/ktlint/pull/3255), by @paul-dingemans

* Keep EOL comments on same line as when condition - [#3261](https://github.com/ktlint/ktlint/pull/3261), by @paul-dingemans

* Do not remove UTF BOM character - [#3264](https://github.com/ktlint/ktlint/pull/3264), by @paul-dingemans

* Do not wrap an annotated expression before a lambda expression - [#3268](https://github.com/ktlint/ktlint/pull/3268), by @paul-dingemans

* Fix writing formatted code containing a "%" character provided via --stdin - [#3281](https://github.com/ktlint/ktlint/pull/3281), by @paul-dingemans

* Do not join supertype with previous line when that line ends with and EOL comment - [#3312](https://github.com/ktlint/ktlint/pull/3312), by @paul-dingemans

* Prevent throwing a NullPointerException after fixing EOL comment by `comment-spacing` rule  - [#3314](https://github.com/ktlint/ktlint/pull/3314), by @paul-dingemans

* Load `.editorconfig` correctly when `--stdin-path` is a relative path - [#3322](https://github.com/ktlint/ktlint/pull/3322), by @paul-dingemans

* Fix call expression wrapping when function literal has an empty multiline block body. - [#3353](https://github.com/ktlint/ktlint/pull/3353), by @paul-dingemans

* Fix output when reading input from stdin - [#3359](https://github.com/ktlint/ktlint/pull/3359), by @paul-dingemans

* fix(indent): support tabs before multiline where clauses - [#3363](https://github.com/ktlint/ktlint/pull/3363), by @autaut03

* Support positional name based destructuring - [#3365](https://github.com/ktlint/ktlint/pull/3365), by @paul-dingemans

* Rename `context-receiver-list-wrapping` to `context-parameter-list-wrapping` - [#3367](https://github.com/ktlint/ktlint/pull/3367), by @paul-dingemans

### 📦 Dependencies

* chore(deps): update ffurrer2/extract-release-notes action to v3 - [#3196](https://github.com/ktlint/ktlint/pull/3196), by @renovate[bot]

* fix(deps): update dependency org.ec4j.core:ec4j-core to v1.2.0 - [#3197](https://github.com/ktlint/ktlint/pull/3197), by @renovate[bot]

* fix(deps): update dependency com.github.ajalt.clikt:clikt to v5.1.0 - [#3224](https://github.com/ktlint/ktlint/pull/3224), by @renovate[bot]

* fix(deps): update dependency org.assertj:assertj-core to v3.27.7 - [#3229](https://github.com/ktlint/ktlint/pull/3229), by @renovate[bot]

* fix(deps): update dependency io.github.hakky54:logcaptor to v2.12.6 - [#3273](https://github.com/ktlint/ktlint/pull/3273), by @renovate[bot]

* chore(deps): update gradle/actions action to v6 - [#3275](https://github.com/ktlint/ktlint/pull/3275), by @renovate[bot]

* fix(deps): update dependency org.jetbrains.dokka:dokka-gradle-plugin to v2.2.0 - autoclosed - [#3277](https://github.com/ktlint/ktlint/pull/3277), by @renovate[bot]

* Update dependency io.github.detekt.sarif4k:sarif4k to v0.7.0 - [#3285](https://github.com/ktlint/ktlint/pull/3285), by @renovate[bot]

* Configure Renovate - [#3304](https://github.com/ktlint/ktlint/pull/3304), by @renovate[bot]

* Update softprops/action-gh-release action to v3 - [#3283](https://github.com/ktlint/ktlint/pull/3283), by @renovate[bot]

* Update dependency io.github.oshai:kotlin-logging-jvm to v8.0.4 - [#3292](https://github.com/ktlint/ktlint/pull/3292), by @renovate[bot]

* Update dependency org.slf4j:slf4j-simple to v2.0.18 - [#3294](https://github.com/ktlint/ktlint/pull/3294), by @renovate[bot]

* Update logback monorepo to v1.5.34 - [#3310](https://github.com/ktlint/ktlint/pull/3310), by @renovate[bot]

* Update dependency dev.drewhamilton.poko:poko-gradle-plugin to v0.23.1 - [#3320](https://github.com/ktlint/ktlint/pull/3320), by @renovate[bot]

* Update dependency com.vanniktech:gradle-maven-publish-plugin to v0.37.0 - [#3327](https://github.com/ktlint/ktlint/pull/3327), by @renovate[bot]

* Update plugin com.gradle.develocity to v4.5.0 - [#3332](https://github.com/ktlint/ktlint/pull/3332), by @renovate[bot]

* Update mislav/bump-homebrew-formula-action action to v4.2 - [#3339](https://github.com/ktlint/ktlint/pull/3339), by @renovate[bot]

* Update kotlin monorepo to v2.4.10 - [#3340](https://github.com/ktlint/ktlint/pull/3340), by @renovate[bot]

* Update plugin shadow to v9.6.1 - [#3341](https://github.com/ktlint/ktlint/pull/3341), by @renovate[bot]

* Update actions/setup-python action to v7 - [#3342](https://github.com/ktlint/ktlint/pull/3342), by @renovate[bot]

* Update actions/checkout action to v7 - [#3347](https://github.com/ktlint/ktlint/pull/3347), by @renovate[bot]

* Update Gradle to v9.7.0 - [#3352](https://github.com/ktlint/ktlint/pull/3352), by @renovate[bot]

* Update junit-framework monorepo to v6.1.3 - [#3354](https://github.com/ktlint/ktlint/pull/3354), by @renovate[bot]

* Update dependency ch.qos.logback:logback-classic to v1.6.3 - [#3360](https://github.com/ktlint/ktlint/pull/3360), by @renovate[bot]

* Override outdated log4j dependencies provided via logcaptor 2.12.6 - [#3366](https://github.com/ktlint/ktlint/pull/3366), by @paul-dingemans

### 💬 Other

* Avoid parallel uploads to Maven Central repository - [#3194](https://github.com/ktlint/ktlint/pull/3194), by @paul-dingemans

* Remove deprecated `ConditionWrappingRule`, and `DiscouragedCommentLocationRule` - [#3237](https://github.com/ktlint/ktlint/pull/3237), by @paul-dingemans

* Drop support for Java 8 and 11 - [#3256](https://github.com/ktlint/ktlint/pull/3256), by @paul-dingemans

* Remove deprecated methods which were marked for removal in Ktlint 2.0, and resolve TODO's - [#3263](https://github.com/ktlint/ktlint/pull/3263), by @paul-dingemans

* Update legal text to indicate this project is no longer maintained by Pinterest - [#3297](https://github.com/ktlint/ktlint/pull/3297), by @shashachu

* Update link to documentation in CLI help - [#3311](https://github.com/ktlint/ktlint/pull/3311), by @Palleas

* Migrate BCV - [#3344](https://github.com/ktlint/ktlint/pull/3344), by @Goooler

* Disable enableKotlinModuleRemapping for shadowJar - [#3345](https://github.com/ktlint/ktlint/pull/3345), by @Goooler

* Migrate "com.pinterest.ktlint" namespace to "io.github.ktlint.core" - [#3348](https://github.com/ktlint/ktlint/pull/3348), by @paul-dingemans

* Cleanup compilation warnings - [#3368](https://github.com/ktlint/ktlint/pull/3368), by @paul-dingemans

## [1.8.0] - 2025-11-12

### 🆕 Features

* Set languageVersion to KOTLIN_2_0 to ensure metadata compatibility with Kotlin 2.0 - [#3110](https://github.com/pinterest/ktlint/pull/3110), by @hugoncosta

* Simplify creation of a custom ruleset - [#3118](https://github.com/pinterest/ktlint/pull/3118), by @paul-dingemans

* Add rule `then-spacing` - [#3129](https://github.com/pinterest/ktlint/pull/3129), by @paul-dingemans

* Add CLI option '--ignore-autocorrect-failures' - [#3157](https://github.com/pinterest/ktlint/pull/3157), by @paul-dingemans

* Allow `_` as backing property name (needed for KEEP-412) - [#3166](https://github.com/pinterest/ktlint/pull/3166), by @serras

* Promote experimental rules to standard rules - [#3171](https://github.com/pinterest/ktlint/pull/3171), by @paul-dingemans:  
  * blank-line-between-when-conditions  
  * kdoc  
  * "mixed-condition-operators"  
  * square-brackets-spacing  
  * when-entry-bracing

### 🔧 Fixes

* Fix link to maven wrapper - [#3112](https://github.com/pinterest/ktlint/pull/3112), by @paul-dingemans

* Fix links in documentation, and fix some typo's/formatting - [#3127](https://github.com/pinterest/ktlint/pull/3127), by @paul-dingemans

* Fix indent of function parameter with multiline expression in `android_studio` - [#3131](https://github.com/pinterest/ktlint/pull/3131), by @paul-dingemans


* fix(deps): update dependency com.vanniktech:gradle-maven-publish-plugin to v0.35.0 - [#3169](https://github.com/pinterest/ktlint/pull/3169), by @renovate[bot]

### 💬 Other

* Validate @SinceKtlint annotations with test and uniformize existing rules - [#3101](https://github.com/pinterest/ktlint/pull/3101), by @hugoncosta

* Fix snapshot repository location - [#3106](https://github.com/pinterest/ktlint/pull/3106), by @paul-dingemans

* Add code vulnerability scanning with CodeQL - [#3113](https://github.com/pinterest/ktlint/pull/3113), by @paul-dingemans

* Disable gradle build cache in code scanning workflow - [#3116](https://github.com/pinterest/ktlint/pull/3116), by @paul-dingemans

* Restrict access to GitHub token to "contents: read" - [#3115](https://github.com/pinterest/ktlint/pull/3115), by @paul-dingemans

* Change language for code-scanning workflow to 'kotlin' - [#3117](https://github.com/pinterest/ktlint/pull/3117), by @paul-dingemans

* Allow publication documents workflow to write to Github pages - [#3121](https://github.com/pinterest/ktlint/pull/3121), by @paul-dingemans

* Allow manual dispatch of workflow for publishing documentation - [#3122](https://github.com/pinterest/ktlint/pull/3122), by @paul-dingemans

* Set contents write permission on workflows for publishing documentation - [#3123](https://github.com/pinterest/ktlint/pull/3123), by @paul-dingemans

* Switch publication config to vanniktech's maven-publish plugin - [#3068](https://github.com/pinterest/ktlint/pull/3068), by @mateuszkwiecinski

* Add SECURITY.md file - [#3114](https://github.com/pinterest/ktlint/pull/3114), by @paul-dingemans

* Add public key to GitHub - [#3139](https://github.com/pinterest/ktlint/pull/3139), by @shashachu

* Ignore renovate updates for JUnit v6 as it requires Java17+ - [#3146](https://github.com/pinterest/ktlint/pull/3146), by @paul-dingemans

## [1.7.1] - 2025-07-21

### 🔧 Fixes

* Make Ktlint code backwards compatible with lower Kotlin versions (down to 2.0) - [#3063](https://github.com/pinterest/ktlint/pull/3063), by @mateuszkwiecinski
* Fix conflict between `modifier-list-spacing` and `context-receiver-list-wrapping` - [#3077](https://github.com/pinterest/ktlint/pull/3077), by @paul-dingemans
* Mark `operand-expression-wrapping` rule as experimental - [#3081](https://github.com/pinterest/ktlint/pull/3081), by @paul-dingemans

### 💬 Other

* Update signing config, fix configuration-cache compatibility - [#3058](https://github.com/pinterest/ktlint/pull/3058), by @mateuszkwiecinski
* Refactor ShadowJarExecutableTask to KtlintCLiTask - [#3078](https://github.com/pinterest/ktlint/pull/3078), by @paul-dingemans

## [1.7.0] - 2025-07-14

### 🆕 Features

#### Context receiver / parameters

With upgrade to Kotlin 2.2.0, Ktlint 1.7.0 supports context parameters. 

* Add `context-receiver-list-wrapping` rule - [#3034](https://github.com/pinterest/ktlint/pull/3034), by @paul-dingemans

* Add context receiver list to `modifier-order` rule - [#3031](https://github.com/pinterest/ktlint/pull/3031), by @paul-dingemans

* Ignore context parameters in rule `context-receiver-wrapping` - [#3033](https://github.com/pinterest/ktlint/pull/3033), by @paul-dingemans

#### Other rule changes

* Do not run `no-unused-imports` rule by default - [#3039](https://github.com/pinterest/ktlint/pull/3039), by @paul-dingemans  
  The `no-unused-import` rule is no longer run by default as it keeps causing problems. It will be removed in Ktlint `2.0`. Until then, the rule can still be run, when enabled explicitly in `.editorconfig`.

* Add experimental rule `expression-operand-wrapping` - [#3056](https://github.com/pinterest/ktlint/pull/3056), by @paul-dingemans  
  This rule aligns wrapping of arithmetic and logical multiline expressions. The `condition-wrapping` rule which did something similar for logical multiline expressions only no longer reports and fixes violations. It will be removed in Ktlint `2.0`.

#### Other features

* Deprecate functions in `ASTNodeExtensions`, and where applicable provide property accessors - [#3026](https://github.com/pinterest/ktlint/pull/3026), by @paul-dingemans  
  When using `ASTNodeExtensions` please replace function calls with the new (temporary) property accessors. The temporary property accessors are needed to maintain backwards compatability with Java integration. In Ktlint `2.0` the functions will be removed, and the temporary property accessors will be replaced with final names. 

* Suppress warning "sun.misc.Unsafe::objectFieldOffset" in Ktlint CLI (Java24+) - [#3040](https://github.com/pinterest/ktlint/pull/3040), by @paul-dingemans

* Suppress error "A restricted method in java.lang.System has been called" on Java 24 in KtLint CLI only - [#3043](https://github.com/pinterest/ktlint/pull/3043), by @paul-dingemans

* Remove unneeded "--add-opens=java.base/java.lang=ALL-UNNAMED" - [#3044](https://github.com/pinterest/ktlint/pull/3044), by @paul-dingemans


### 🔧 Fixes


* Do not remove an empty value parameter list from a call expression when it is nested - [#3017](https://github.com/pinterest/ktlint/pull/3017), by @paul-dingemans

* Clarify violation message in `filename` rule - [#3046](https://github.com/pinterest/ktlint/pull/3046), by @paul-dingemans

### 📦 Dependencies


* chore(deps): update plugin org.gradle.toolchains.foojay-resolver-convention to v1 - [#2989](https://github.com/pinterest/ktlint/pull/2989), by @renovate[bot]

* chore(deps): update plugin com.gradle.develocity to v4.0.2 - [#2996](https://github.com/pinterest/ktlint/pull/2996), by @renovate[bot]

* fix(deps): update junit5 monorepo - [#3005](https://github.com/pinterest/ktlint/pull/3005), by @renovate[bot]

* fix(deps): update dependency org.jetbrains.kotlin:kotlin-gradle-plugin to v2.2.0-rc3 - [#3015](https://github.com/pinterest/ktlint/pull/3015), by @renovate[bot]

* fix(deps): update dependency org.junit.jupiter:junit-jupiter to v5.13.2 - [#3020](https://github.com/pinterest/ktlint/pull/3020), by @renovate[bot]

* fix(deps): update dependency org.junit.platform:junit-platform-launcher to v1.13.2 - [#3021](https://github.com/pinterest/ktlint/pull/3021), by @renovate[bot]

* fix(deps): update kotlin monorepo to v2.2.0 - [#3018](https://github.com/pinterest/ktlint/pull/3018), by @renovate[bot]

* chore(deps): update plugin shadow to v8.3.8 - [#3030](https://github.com/pinterest/ktlint/pull/3030), by @renovate[bot]

* fix(deps): update junit-framework monorepo - [#3037](https://github.com/pinterest/ktlint/pull/3037), by @renovate[bot]

* chore(deps): update plugin kotlinx-binary-compatibiltiy-validator to v0.18.1 - [#3050](https://github.com/pinterest/ktlint/pull/3050), by @renovate[bot]

* fix(deps): update dependency dev.drewhamilton.poko:poko-gradle-plugin to v0.19.1 - [#3053](https://github.com/pinterest/ktlint/pull/3053), by @renovate[bot]

* fix(deps): update dependency com.google.jimfs:jimfs to v1.3.1 - [#3054](https://github.com/pinterest/ktlint/pull/3054), by @renovate[bot]

* chore(deps): update dependency gradle to v9.0.0-rc-2 - [#3055](https://github.com/pinterest/ktlint/pull/3055), by @renovate[bot]

### 💬 Other

* Update publishing URLs to point to Central Portal OSSRH staging API - [#3006](https://github.com/pinterest/ktlint/pull/3006), by @shashachu

* Update publishing credentials to use new Central Portal username/token - [#3007](https://github.com/pinterest/ktlint/pull/3007), by @shashachu

* Improve build - [#3025](https://github.com/pinterest/ktlint/pull/3025), by @mateuszkwiecinski

* Bump `poko-gradle-plugin` with a workaround (enforce compatible Kotlin version) - [#3035](https://github.com/pinterest/ktlint/pull/3035), by @mateuszkwiecinski

* Java 24 - [#3049](https://github.com/pinterest/ktlint/pull/3049), by @paul-dingemans

* Make `build-logic` plugins expose binary plugins - [#3047](https://github.com/pinterest/ktlint/pull/3047), by @mateuszkwiecinski

* Cleanup Java 24 bump  - [#3051](https://github.com/pinterest/ktlint/pull/3051), by @mateuszkwiecinski

## [1.6.0] - 2025-05-19

🆕 Features
* Add configuration option to handle (certain) annotation with parameters identical to annotations without parameters - #2950, by @paul-dingemans

* Set system properties to prevent failure when running in ktlint-intellij-plugin with custom installation - #2970, by @paul-dingemans

🔧 Fixes

* Deprecations in 1.5.0 - #2913, by @paul-dingemans

* Fix incorrect error message in PropertyNamingRule when enforcing PascalCase - [#2934](#2934), by @lsurvila

* Sort RuleProvider declarations in StandardRuleSetProvider - [#2941](#2941), by @3flex

* Remove usages of getPsi() - #2901, by @mgroth0

* Refactor KotlinPsiFileFactory to KotlinCompiler - [#2951](#2951), by @paul-dingemans

* Reduces the number of conversions between "psi" and ASTNode - [#2964](#2964), by @paul-dingemans

* Fix slow response of the git pre commit hook as generated by ktlint - [#2978](#2978), by @paul-dingemans

* Move logic of missing whitespace before block comment to NoSingleLineBlockCommentRule - #2979, by @paul-dingemans

* Report and fix a single line condition wrapped between parentheses with an unexpected newline - [#2980](#2980), by @paul-dingemans

* Do not wrap an operation reference after an annotated expression to a separate new line - #2983, by @paul-dingemans

📦 Dependencies

* fix(deps): update dependency org.jetbrains.dokka:dokka-gradle-plugin to v2 - #2906, by @renovate[bot]

* chore(deps): update plugin kotlinx-binary-compatibiltiy-validator to v0.17.0 - #2907, by @renovate[bot]

* Complete upgrade Dokka to V2 - #2914, by @paul-dingemans

* fix(deps): update dependency ch.qos.logback:logback-classic to v1.3.15 - #2917, by @renovate[bot]

* fix(deps): update dependency org.assertj:assertj-core to v3.27.3 - #2927, by @renovate[bot]

* chore(deps): update plugin shadow to v8.3.6 - #2935, by @renovate[bot]

* fix(deps): update dependency com.github.ajalt.clikt:clikt to v5.0.3 - #2938, by @renovate[bot]

* fix(deps): update dependency org.slf4j:slf4j-simple to v2.0.17 - #2947, by @renovate[bot]

* chore(deps): update plugin org.gradle.toolchains.foojay-resolver-convention to v0.10.0 - #2963, by @renovate[bot]

* fix(deps): update dependency org.junit.jupiter:junit-jupiter to v5.12.2 - #2945, by @renovate[bot]

* fix(deps): update dependency io.github.oshai:kotlin-logging-jvm to v7.0.7 - #2965, by @renovate[bot]

* fix(deps): update dependency io.github.hakky54:logcaptor to v2.11.0 - #2968, by @renovate[bot]

* chore(deps): update dependency gradle to v8.14 - #2971, by @renovate[bot]

* chore(deps): update plugin com.gradle.develocity to v4.0.1 - #2972, by @renovate[bot]

* fix(deps): update dependency org.jetbrains.kotlin:kotlin-gradle-plugin to v2.2.0-beta2 - #2977, by @renovate[bot]

* fix(deps): update dependency org.ec4j.core:ec4j-core to v1.1.1 - #2987, by @renovate[bot]

* fix(deps): update dependency dev.drewhamilton.poko:poko-gradle-plugin to v0.18.7 - #2986, by @renovate[bot]

* fix(deps): update kotlin monorepo to 2.1.21 - #2985, by @renovate[bot]

💬 Other

* ignore .kotlin directory - #2916, by @mgroth0

## [1.5.0] - 2024-12-04

The release of Kotlin 2.1 is the primary reason for publishing this release earlier than planned. Because of the removal of some language elements in Kotlin 2.1, the embedded Kotlin compiler in KtLint blocked Kotlinter users from upgrading to Kotlin 2.1.

### 🆕 Features

* Add missing space between `fun` keyword and identifier when latter is wrapped between backticks - [#2890](https://github.com/pinterest/ktlint/pull/2890), by @paul-dingemans

* Add configuration setting for constant names in `property-naming` rule - [#2893](https://github.com/pinterest/ktlint/pull/2893), by @paul-dingemans

* Allow backing property to be defined in the companion object (`backing-property-naming`) - [#2895](https://github.com/pinterest/ktlint/pull/2895), by @paul-dingemans

### 🔧 Fixes

* Ignore multi dollar string interpolation prefix in `string-template-indent` rule - [#2888](https://github.com/pinterest/ktlint/pull/2888), by @paul-dingemans

* Wrap the expression body in case `.editorconfig` property `ktlint_function_signature_body_expression_wrapping` is set to `always` - [#2873](https://github.com/pinterest/ktlint/pull/2873), by @paul-dingemans

* Fix false positive when empty parameter list is in between trailing lambda's of a nested call expression - [#2891](https://github.com/pinterest/ktlint/pull/2891), by @paul-dingemans

* Do not wrap a context receiver in a function parameter type reference - [#2892](https://github.com/pinterest/ktlint/pull/2892), by @paul-dingemans

* Allow comment before parameter list in function literal (`function-literal`) - [#2894](https://github.com/pinterest/ktlint/pull/2894), by @paul-dingemans

### 📦 Dependencies

* chore(deps): update plugin com.gradle.develocity to v3.18.2 - [#2865](https://github.com/pinterest/ktlint/pull/2865), by @renovate[bot]

* chore(deps): update dependency gradle to v8.11.1 - [#2875](https://github.com/pinterest/ktlint/pull/2875), by @renovate[bot]

* fix(deps): update dependency io.github.hakky54:logcaptor to v2.10.0 - [#2874](https://github.com/pinterest/ktlint/pull/2874), by @renovate[bot]

* fix(deps): update dependency org.jetbrains.kotlin:kotlin-gradle-plugin to v2.1.0-rc2 - [#2871](https://github.com/pinterest/ktlint/pull/2871), by @renovate[bot]

* fix(deps): update dependency io.github.oshai:kotlin-logging-jvm to v7.0.3 - [#2883](https://github.com/pinterest/ktlint/pull/2883), by @renovate[bot]

* fix(deps): update kotlin monorepo to v2.1.0 - [#2880](https://github.com/pinterest/ktlint/pull/2880), by @renovate[bot]

* fix(deps): update dependency com.github.ajalt.clikt:clikt to v5.0.2 - [#2886](https://github.com/pinterest/ktlint/pull/2886), by @renovate[bot]

* chore(deps): update plugin org.gradle.toolchains.foojay-resolver-convention to v0.9.0 - [#2887](https://github.com/pinterest/ktlint/pull/2887), by @renovate[bot]

* fix(deps): update dependency dev.drewhamilton.poko:poko-gradle-plugin to v0.18.0 - [#2889](https://github.com/pinterest/ktlint/pull/2889), by @renovate[bot]

## [1.4.1] - 2024-11-05

### 🔧 Fixes
 
* Catch java.lang.NoSuchFieldError when looking for WHEN_ENTRY_GUARD in kotlin version 2.0.1 - [#2857](https://github.com/pinterest/ktlint/pull/2857), by @paul-dingemans

### 📦 Dependencies
 
* chore(deps): update plugin shadow to v8.3.5 - [#2851](https://github.com/pinterest/ktlint/pull/2851), by @renovate[bot]
* fix(deps): update dependency org.jetbrains.kotlin:kotlin-gradle-plugin to v2.1.0-rc - [#2853](https://github.com/pinterest/ktlint/pull/2853), by @renovate[bot]

## [1.4.0] - 2024-10-24

### 🆕 Features


* Suppress `backing-property-naming` on `@Suppress("PropertyName")` - [#2741](https://github.com/pinterest/ktlint/pull/2741), by @paul-dingemans

* Check that code can still be parsed after a format - [#2742](https://github.com/pinterest/ktlint/pull/2742), by @paul-dingemans

* Support suppressing backing-property-naming via ObjectPropertyName - [#2751](https://github.com/pinterest/ktlint/pull/2751), by @Goooler

* Ignore properties starting with `_` in `backing-property-naming` rule - [#2753](https://github.com/pinterest/ktlint/pull/2753), by @paul-dingemans

* Suppress `backing-property-naming` rule with `@Suppress("LocalVariableName")` - [#2785](https://github.com/pinterest/ktlint/pull/2785), by @paul-dingemans

* Do not repeat formatting if file is unchanged - [#2805](https://github.com/pinterest/ktlint/pull/2805), by @paul-dingemans

* Add new rule `when-entry-bracing` - [#2829](https://github.com/pinterest/ktlint/pull/2829), by @paul-dingemans

* Add `.editorconfig` property `ij_kotlin_indent_before_arrow_on_new_line` - [#2838](https://github.com/pinterest/ktlint/pull/2838), by @paul-dingemans

* Add CLI option `--stdin-path` to provide a virtual file location for stdin - [#2836](https://github.com/pinterest/ktlint/pull/2836), by @adecker89

* Add property `.editorconfig` property `ktlint_enum_entry_name_casing` - [#2839](https://github.com/pinterest/ktlint/pull/2839), by @paul-dingemans

### 🔧 Fixes


* Prevent line separators to be changed from CRLF to LF - [#2752](https://github.com/pinterest/ktlint/pull/2752), by @paul-dingemans

* fix: don't remove arrow from lambdas that are when/if leaf nodes - [#2758](https://github.com/pinterest/ktlint/pull/2758), by @tKe

* Fix false positive in `no-semi` rule for enum class without enum entries - [#2774](https://github.com/pinterest/ktlint/pull/2774), by @paul-dingemans

* Prevent conflict between `multiline-expression-wrapping` and `function-signature` - [#2775](https://github.com/pinterest/ktlint/pull/2775), by @paul-dingemans

* Ignore `max_line_length` property unless `max-line-length` rule is enabled  - [#2783](https://github.com/pinterest/ktlint/pull/2783), by @paul-dingemans

* Fix false positive in `class-signature` when EOL comment is between a class annotation and other class modifier - [#2786](https://github.com/pinterest/ktlint/pull/2786), by @paul-dingemans

* Fix globs ending with `**` - [#2787](https://github.com/pinterest/ktlint/pull/2787), by @paul-dingemans

* Return exit code 1 in case violations have been found but file is unchanged - [#2803](https://github.com/pinterest/ktlint/pull/2803), by @paul-dingemans

* Ignore missing whitespace after trailing comma in single line parameter value list - [#2806](https://github.com/pinterest/ktlint/pull/2806), by @paul-dingemans

* Remove HEADER_KEYWORD & IMPL_KEYWORD - [#2810](https://github.com/pinterest/ktlint/pull/2810), by @3flex

* Function signature rule ignores context receiver when on separate line - [#2814](https://github.com/pinterest/ktlint/pull/2814), by @paul-dingemans

* Do not remove imports for which the fully qualified path is identical to the package name - [#2822](https://github.com/pinterest/ktlint/pull/2822), by @paul-dingemans

* Fix indentation of a multiline parameter list inside a function literal for code style `ktlint_official` - [#2823](https://github.com/pinterest/ktlint/pull/2823), by @paul-dingemans

* Do not insert a trailing comma in a multiline when-entry containing a guard - [#2825](https://github.com/pinterest/ktlint/pull/2825), by @paul-dingemans

* Fix wrapping of expression body when `max_line_length` not set - [#2833](https://github.com/pinterest/ktlint/pull/2833), by @paul-dingemans

### 📦 Dependencies


* Update dependency io.github.oshai:kotlin-logging-jvm to v7 - [#2701](https://github.com/pinterest/ktlint/pull/2701), by @renovate[bot]

* Update dependency org.assertj:assertj-core to v3.26.3 - [#2738](https://github.com/pinterest/ktlint/pull/2738), by @renovate[bot]

* Update mikepenz/release-changelog-builder-action action to v5 - [#2756](https://github.com/pinterest/ktlint/pull/2756), by @renovate[bot]

* chore(deps): update plugin kotlinx-binary-compatibiltiy-validator to v0.16.3 - [#2759](https://github.com/pinterest/ktlint/pull/2759), by @renovate[bot]

* fix(deps): update dependency org.slf4j:slf4j-simple to v2.0.16 - [#2764](https://github.com/pinterest/ktlint/pull/2764), by @renovate[bot]

* Migrate to com.gradleup.shadow - [#2763](https://github.com/pinterest/ktlint/pull/2763), by @Goooler

* chore(deps): update gradle/actions action to v4 - [#2760](https://github.com/pinterest/ktlint/pull/2760), by @renovate[bot]

* chore(deps): update plugin com.gradle.develocity to v3.18.1 - [#2792](https://github.com/pinterest/ktlint/pull/2792), by @renovate[bot]

* chore(deps): update dependency gradle to v8.10.2 - [#2812](https://github.com/pinterest/ktlint/pull/2812), by @renovate[bot]

* chore(deps): update plugin shadow to v8.3.3 - [#2820](https://github.com/pinterest/ktlint/pull/2820), by @renovate[bot]

* fix(deps): update dependency com.github.ajalt.clikt:clikt to v5.0.1 - [#2828](https://github.com/pinterest/ktlint/pull/2828), by @renovate[bot]

* chore(deps): update kotlin monorepo to v2.0.21 - [#2831](https://github.com/pinterest/ktlint/pull/2831), by @renovate[bot]

* fix(deps): update dependency org.ec4j.core:ec4j-core to v1.1.0 - [#2832](https://github.com/pinterest/ktlint/pull/2832), by @renovate[bot]

* fix(deps): update dependency org.jetbrains.kotlin:kotlin-gradle-plugin to v2.1.0-beta2 - [#2834](https://github.com/pinterest/ktlint/pull/2834), by @renovate[bot]

* fix(deps): update dependency dev.drewhamilton.poko:poko-gradle-plugin to v0.17.2 - [#2837](https://github.com/pinterest/ktlint/pull/2837), by @renovate[bot]

* fix(deps): update dependency org.junit.jupiter:junit-jupiter to v5.11.3 - [#2840](https://github.com/pinterest/ktlint/pull/2840), by @renovate[bot]

* chore(deps): update actions/checkout digest to 11bd719 - [#2841](https://github.com/pinterest/ktlint/pull/2841), by @renovate[bot]

## [1.3.1] - 2024-07-02

### 🔧 Fixes
* Add link for backing-property-naming, correct binary-expression-wrapping - [#2704](https://github.com/pinterest/ktlint/pull/2704), by @rsmith20
* Do not insert a whitespace element as first or last child inside a composite element - [#2715](https://github.com/pinterest/ktlint/pull/2715), by @paul-dingemans
* Do not rewrite a class to a single line signature in case it contains an EOL comment - [#2716](https://github.com/pinterest/ktlint/pull/2716), by @paul-dingemans
* Fix false positive when anonymous function is used as value argument - [#2718](https://github.com/pinterest/ktlint/pull/2718), by @paul-dingemans
* Fix suppression handling when 'formatter:on' not properly specified - [#2719](https://github.com/pinterest/ktlint/pull/2719), by @paul-dingemans
* Fix false positive when primary constructor has no arguments and a secondary constructor exists - [#2717](https://github.com/pinterest/ktlint/pull/2717), by @paul-dingemans
* Ignore suppressions for no-unused-imports rule - [#2720](https://github.com/pinterest/ktlint/pull/2720), by @paul-dingemans
* Ignore suppressions in rule `no-unused-imports` - [#2725](https://github.com/pinterest/ktlint/pull/2725), by @paul-dingemans
* Fix false alert `Format was not able to resolve all violations which (theoretically) can be autocorrected` - [#2727](https://github.com/pinterest/ktlint/pull/2727), by @paul-dingemans
* Fix "unset" value for property ktlint_chain_method_rule_force_multiline_when_chain_operator_count_greater_or_equal_than - [#2728](https://github.com/pinterest/ktlint/pull/2728), by @paul-dingemans

### 📦 Dependencies

* Update dependency gradle to v8.8 - [#2680](https://github.com/pinterest/ktlint/pull/2680), by @renovate[bot]
* Update plugin com.gradle.develocity to v3.17.5 - [#2697](https://github.com/pinterest/ktlint/pull/2697), by @renovate[bot]
* Update actions/checkout digest to 692973e - [#2699](https://github.com/pinterest/ktlint/pull/2699), by @renovate[bot]
* Update dependency io.github.hakky54:logcaptor to v2.9.3 - [#2707](https://github.com/pinterest/ktlint/pull/2707), by @renovate[bot]
* Update dependency org.junit.jupiter:junit-jupiter to v5.10.3 - [#2721](https://github.com/pinterest/ktlint/pull/2721), by @renovate[bot]

### 💬 Other

* Remove sdkman - [#2693](https://github.com/pinterest/ktlint/pull/2693), by @paul-dingemans

## [1.3.0] - 2024-06-04

### 🆕 Features

* Support partial formatting - [#2631](https://github.com/pinterest/ktlint/pull/2631), by @paul-dingemans

* Suppress `property-name` rule for `ObjectPropertyName` or `PrivatePropertyName` - [#2643](https://github.com/pinterest/ktlint/pull/2643), by @paul-dingemans

* Let API Consumer decide whether a LintError has to be autocorrected, or not - [#2671](https://github.com/pinterest/ktlint/pull/2671), by @paul-dingemans

* Promote experimental rules to non-experimental - [#2674](https://github.com/pinterest/ktlint/pull/2674), by @paul-dingemans
  - [backing-property-naming](https://pinterest.github.io/ktlint/latest/rules/standard/#backing-property-naming)
  - [binary-expression-wrapping](https://pinterest.github.io/ktlint/latest/rules/standard/#binary-expression-wrapping)
  - [chain-method-continuation](https://pinterest.github.io/ktlint/latest/rules/standard/#chain-method-continuation)
  - [class-signature](https://pinterest.github.io/ktlint/latest/rules/standard/#class-signature)
  - [condition-wrapping](https://pinterest.github.io/ktlint/latest/rules/standard/#condition-wrapping)
  - [function-expression-body](https://pinterest.github.io/ktlint/latest/rules/standard/#function-expression-body)
  - [function-literal](https://pinterest.github.io/ktlint/latest/rules/standard/#function-literal)
  - [function-type-modifier-spacing](https://pinterest.github.io/ktlint/latest/rules/standard/#function-type-modifier-spacing)
  - [multiline-loop](https://pinterest.github.io/ktlint/latest/rules/standard/#multiline-loop)

### 🔧 Fixes


* Do not wrap operation reference after multiline string template - [#2591](https://github.com/pinterest/ktlint/pull/2591), by @paul-dingemans

* Ignore max line length in case the line contains only a string template followed by a comma - [#2598](https://github.com/pinterest/ktlint/pull/2598), by @paul-dingemans

* Ignore nested reference expressions in `chain-method-continuation` - [#2606](https://github.com/pinterest/ktlint/pull/2606), by @paul-dingemans

* Prevent exception in `binary-expression-wrapping` rule - [#2607](https://github.com/pinterest/ktlint/pull/2607), by @paul-dingemans

* Do not merge opening quotes of multiline string template with (single line) function signature - [#2609](https://github.com/pinterest/ktlint/pull/2609), by @paul-dingemans

* Fix replacement of redundant curly braces - [#2617](https://github.com/pinterest/ktlint/pull/2617), by @paul-dingemans

* Set and reset Locale to pass test for non-english contributor. - [#2622](https://github.com/pinterest/ktlint/pull/2622), by @Jaehwa-Noh

* Fix unwanted whitespace between super class constructor and its argument list - [#2630](https://github.com/pinterest/ktlint/pull/2630), by @tKe

* Fix typo's - [#2641](https://github.com/pinterest/ktlint/pull/2641), by @paul-dingemans

* Handle trailing space on preceding line in call to `lineLengthWithoutNewlinePrefix` - [#2644](https://github.com/pinterest/ktlint/pull/2644), by @paul-dingemans

* Fix KDoc for RuleSetProviderV3 - [#2645](https://github.com/pinterest/ktlint/pull/2645), by @gumimin

* Fix not checking for spacing around binary operators inside unary expression - [#2653](https://github.com/pinterest/ktlint/pull/2653), by @cflee

* Fix `blank line before declarations` rule code example - [#2657](https://github.com/pinterest/ktlint/pull/2657), by @k-taro56

* Fixed [{ }] notation for issue #2675 - [#2677](https://github.com/pinterest/ktlint/pull/2677), by @Jolanrensen

* Simplify default properties loading - [#2679](https://github.com/pinterest/ktlint/pull/2679), by @Goooler

### 📦 Dependencies

* Update dependency org.jetbrains.dokka:dokka-gradle-plugin to v1.9.20 - [#2590](https://github.com/pinterest/ktlint/pull/2590), by @renovate[bot]

* Update softprops/action-gh-release action to v2 - [#2600](https://github.com/pinterest/ktlint/pull/2600), by @renovate[bot]

* Update dependency io.github.detekt.sarif4k:sarif4k to v0.6.0 - [#2605](https://github.com/pinterest/ktlint/pull/2605), by @renovate[bot]

* Update dependency gradle to v8.7 - [#2616](https://github.com/pinterest/ktlint/pull/2616), by @renovate[bot]

* Migrate to develocity plugin - [#2625](https://github.com/pinterest/ktlint/pull/2625), by @Goooler

* Update dependency org.slf4j:slf4j-simple to v2.0.13 - [#2632](https://github.com/pinterest/ktlint/pull/2632), by @renovate[bot]

* Update gradle/wrapper-validation-action action to v3 - [#2633](https://github.com/pinterest/ktlint/pull/2633), by @renovate[bot]

* Update dependency io.github.oshai:kotlin-logging-jvm to v6.0.9 - [#2634](https://github.com/pinterest/ktlint/pull/2634), by @renovate[bot]

* Update dependency com.github.ajalt.clikt:clikt to v4.4.0 - [#2647](https://github.com/pinterest/ktlint/pull/2647), by @renovate[bot]

* Update kotlin monorepo to v1.9.24 - [#2649](https://github.com/pinterest/ktlint/pull/2649), by @renovate[bot]

* Update plugin com.gradle.develocity to v3.17.4 - [#2660](https://github.com/pinterest/ktlint/pull/2660), by @renovate[bot]

* Update dependency dev.drewhamilton.poko:poko-gradle-plugin to v0.15.3 - [#2662](https://github.com/pinterest/ktlint/pull/2662), by @renovate[bot]

* Update actions/checkout digest to a5ac7e5 - [#2664](https://github.com/pinterest/ktlint/pull/2664), by @renovate[bot]

* Update dependency org.assertj:assertj-core to v3.26.0 - [#2669](https://github.com/pinterest/ktlint/pull/2669), by @renovate[bot]

## [1.2.1] - 2024-02-29

### 🆕 Features
None

### 🔧 Fixes
* Mark new rules as experimental - [#2579](https://github.com/pinterest/ktlint/pull/2579), by @paul-dingemans
* Fix null byte as default value for "--pattern-from-stdin" - [#2580](https://github.com/pinterest/ktlint/pull/2580), by @paul-dingemans
* Fix handling of "--reporter" CLI parameter - [#2581](https://github.com/pinterest/ktlint/pull/2581), by @paul-dingemans

## [1.2.0] - 2024-02-28

### 🆕 Features


* Break dependency between string-template-indent and multiline-expression-wrapping - [#2505](https://github.com/pinterest/ktlint/pull/2505), by @paul-dingemans

* Allow string template to exceed max line length when it is the only element on a line - [#2480](https://github.com/pinterest/ktlint/pull/2480), by @paul-dingemans

* Add configuration setting for ignoring `argument-list-wrapping` above threshold of argument - [#2481](https://github.com/pinterest/ktlint/pull/2481), by @paul-dingemans  
  NOTE: In code style `ktlint_official` this threshold is `unset` so that arguments are always wrapped. If this impacts your code too much, you can make it backward compatible by setting `.editorconfig` property `ktlint_argument_list_wrapping_ignore_when_parameter_count_greater_or_equal_than` to value `8`. For other code styles this property is initialized with value `8` and as of that backward compatible by default.

* Ignore EOL comment that causes max_line_length to be exceeded, except in max-line-length rule - [#2516](https://github.com/pinterest/ktlint/pull/2516), by @paul-dingemans

* Add new rule for disallowing KDoc at non-whitelisted locations - [#2548](https://github.com/pinterest/ktlint/pull/2548), by @paul-dingemans

* Improve insert of suppression - [#2546](https://github.com/pinterest/ktlint/pull/2546), by @paul-dingemans

* Ignore modifier of backing property in `android_studio` code style - [#2552](https://github.com/pinterest/ktlint/pull/2552), by @paul-dingemans

* Add rule to check spacing around square brackets 'square-brackets-spacing' - [#2555](https://github.com/pinterest/ktlint/pull/2555), by @paul-dingemans

* Add rule `blank-line-between-when-conditions` - [#2564](https://github.com/pinterest/ktlint/pull/2564), by @paul-dingemans

### 🔧 Fixes

* Prevent IllegalArgumentException in `argument-list-wrapping` rule - [#2500](https://github.com/pinterest/ktlint/pull/2500), by @paul-dingemans

* Ignore function which is returned as result in a function body - [#2526](https://github.com/pinterest/ktlint/pull/2526), by @paul-dingemans

* Fix false positive newline expected before comment in enum - [#2527](https://github.com/pinterest/ktlint/pull/2527), by @paul-dingemans

* Report violation when parameter list is preceded by a comment - [#2541](https://github.com/pinterest/ktlint/pull/2541), by @paul-dingemans

* Ignore EOL comments in `value-argument-comment` and `value-parameter-comment` - [#2551](https://github.com/pinterest/ktlint/pull/2551), by @paul-dingemans

* Do not indent string template starting at first position of line - [#2553](https://github.com/pinterest/ktlint/pull/2553), by @paul-dingemans

* Prevent conflict when curly closing brace is followed by range (until) operator - [#2554](https://github.com/pinterest/ktlint/pull/2554), by @paul-dingemans

* Run argument-list-wrapping after function-signature - [#2568](https://github.com/pinterest/ktlint/pull/2568), by @paul-dingemans

* Ignore simple reference expressions in `chain-method-continuation` - [#2569](https://github.com/pinterest/ktlint/pull/2569), by @paul-dingemans

### 📦 Dependencies

* chore(deps): update plugin org.gradle.toolchains.foojay-resolver-convention to v0.8.0 - [#2503](https://github.com/pinterest/ktlint/pull/2503), by @renovate[bot]

* fix(deps): update dependency io.github.oshai:kotlin-logging-jvm to v6 - [#2440](https://github.com/pinterest/ktlint/pull/2440), by @renovate[bot]

* Replace gradle/gradle-build-action@v3 with gradle/actions/setup-gradle@v3 - [#2518](https://github.com/pinterest/ktlint/pull/2518), by @paul-dingemans

* chore(deps): update plugin kotlinx-binary-compatibiltiy-validator to v0.14.0 - [#2522](https://github.com/pinterest/ktlint/pull/2522), by @renovate[bot]

* chore(deps): update gradle/wrapper-validation-action action to v2 - [#2523](https://github.com/pinterest/ktlint/pull/2523), by @renovate[bot]

* chore(deps): update ffurrer2/extract-release-notes action to v2 - [#2515](https://github.com/pinterest/ktlint/pull/2515), by @renovate[bot]

* chore(deps): update dependency gradle to v8.6 - [#2531](https://github.com/pinterest/ktlint/pull/2531), by @renovate[bot]

* fix(deps): update dependency org.assertj:assertj-core to v3.25.3 - [#2536](https://github.com/pinterest/ktlint/pull/2536), by @renovate[bot]

* fix(deps): update dependency org.junit.jupiter:junit-jupiter to v5.10.2 - [#2534](https://github.com/pinterest/ktlint/pull/2534), by @renovate[bot]

* fix(deps): update dependency org.slf4j:slf4j-simple to v2.0.12 - [#2538](https://github.com/pinterest/ktlint/pull/2538), by @renovate[bot]

* fix(deps): update dependency org.codehaus.janino:janino to v3.1.12 - [#2559](https://github.com/pinterest/ktlint/pull/2559), by @renovate[bot]

### 💬 Other

* Simplify BOM exclude list - [#2476](https://github.com/pinterest/ktlint/pull/2476), by @Goooler

* 2550 clikt - [#2556](https://github.com/pinterest/ktlint/pull/2556), by @paul-dingemans

## [1.1.1] - 2024-01-08

### 🆕 Features

None

### 🔧 Fixes


* Fix incorrect generateEditorConfig example in documentation - [#2444](https://github.com/pinterest/ktlint/pull/2444), by @stay7

* Fix insert of suppression on binary expression - [#2463](https://github.com/pinterest/ktlint/pull/2463), by @paul-dingemans

* Loosen dependency between chain-method-continuation and argument-list-wrapping - [#2468](https://github.com/pinterest/ktlint/pull/2468), by @paul-dingemans

* Keep arrow when both parameter list and block of function literal are empty - [#2469](https://github.com/pinterest/ktlint/pull/2469), by @paul-dingemans

* Improve wrapping of binary expressions - [#2479](https://github.com/pinterest/ktlint/pull/2479), by @paul-dingemans

* Resolve conflict between parameter-list-spacing and parameter-list-wrapping - [#2491](https://github.com/pinterest/ktlint/pull/2491), by @paul-dingemans

* Do not wrap binary expression value argument if it is already preceded by a newline - [#2493](https://github.com/pinterest/ktlint/pull/2493), by @paul-dingemans

* Fix operator spacing - [#2473](https://github.com/pinterest/ktlint/pull/2473), by @paul-dingemans

* Run `argument-list-wrapping`, `class-signature` and `function-signature` when comment rules are disabled - [#2466](https://github.com/pinterest/ktlint/pull/2466), by @paul-dingemans

### 📦 Dependencies

* fix(deps): update kotlin monorepo to v1.9.22 - [#2456](https://github.com/pinterest/ktlint/pull/2456), by @renovate[bot]

* chore(deps): update actions/setup-python action to v5 - [#2417](https://github.com/pinterest/ktlint/pull/2417), by @renovate[bot]

* fix(deps): update dependency org.slf4j:slf4j-simple to v2.0.10 - [#2470](https://github.com/pinterest/ktlint/pull/2470), by @renovate[bot]

* fix(deps): update dependency dev.drewhamilton.poko:poko-gradle-plugin to v0.15.2 - [#2485](https://github.com/pinterest/ktlint/pull/2485), by @renovate[bot]

* fix(deps): update dependency org.assertj:assertj-core to v3.25.1 - [#2486](https://github.com/pinterest/ktlint/pull/2486), by @renovate[bot]

## [1.1.0] - 2023-12-19

### 🆕 Features

* Compile with java 21 instead of 20 - [#2320](https://github.com/pinterest/ktlint/pull/2320), by @paul-dingemans

* Improve checking on backing property - [#2346](https://github.com/pinterest/ktlint/pull/2346), by @paul-dingemans

* Add multiline-loop to complement multiline-if-else - [#2298](https://github.com/pinterest/ktlint/pull/2298), by @hendraanggrian

* Add "UnusedImport" as @Suppress alias - [#2357](https://github.com/pinterest/ktlint/pull/2357), by @paul-dingemans

* Allow backing property to be correlated to a public function - [#2356](https://github.com/pinterest/ktlint/pull/2356), by @paul-dingemans

* Add helper function 'fromSnippetWithPath' to create a Code instance - [#2359](https://github.com/pinterest/ktlint/pull/2359), by @paul-dingemans

* Support logging and exception throwing when loading baseline - [#2362](https://github.com/pinterest/ktlint/pull/2362), by @paul-dingemans

* Allow factory methods to use generics, and to overload other factory … - [#2366](https://github.com/pinterest/ktlint/pull/2366), by @paul-dingemans

* Remove dependencies on discouraged-comment-location rule - [#2371](https://github.com/pinterest/ktlint/pull/2371), by @paul-dingemans

* Ignore imports for `rangeUntil` in `no-unused-imports` rule - [#2376](https://github.com/pinterest/ktlint/pull/2376), by @paul-dingemans

* Ignore imports for `assign` in `no-unused-imports` rule - [#2382](https://github.com/pinterest/ktlint/pull/2382), by @paul-dingemans

* Ignore invalid function names when importing from "junit.framework" - [#2386](https://github.com/pinterest/ktlint/pull/2386), by @paul-dingemans

* Add experimental rules `condition-wrapping` and `mixed-condition-operators` - [#2401](https://github.com/pinterest/ktlint/pull/2401), by @paul-dingemans

* Allow property, function and class name to be same as keyword wrapped with backticks - [#2405](https://github.com/pinterest/ktlint/pull/2405), by @paul-dingemans

* Set offset of `max-line-length` violation to the last position at which a newline can be inserted to fix the violation - [#2419](https://github.com/pinterest/ktlint/pull/2419), by @paul-dingemans

* Add support for API Consumers to add suppressions - [#2428](https://github.com/pinterest/ktlint/pull/2428), by @paul-dingemans

* Disallow `else-if (..) <statement>` as single line construct - [#2430](https://github.com/pinterest/ktlint/pull/2430), by @paul-dingemans

* Allow empty constructor for expected class declaration - [#2431](https://github.com/pinterest/ktlint/pull/2431), by @paul-dingemans

* Disallow comments in try-catch-finally at unexpected locations - [#2432](https://github.com/pinterest/ktlint/pull/2432), by @paul-dingemans

* Make ktlint.bat more environment agnostic - [#2421](https://github.com/pinterest/ktlint/pull/2421), by @TWiStErRob

* Suppress property-naming rule via `@Suppress("ConstPropertyName")` - [#2442](https://github.com/pinterest/ktlint/pull/2442), by @paul-dingemans

### 🔧 Fixes


* Remove obsolete configuration files - [#2321](https://github.com/pinterest/ktlint/pull/2321), by @paul-dingemans

* De-indent the closing angle bracket of the type argument list and type parameter lists in ktlint_official code style - [#2302](https://github.com/pinterest/ktlint/pull/2302), by @paul-dingemans

* docs: Fix artifact url of Maven Central Badge - [#2327](https://github.com/pinterest/ktlint/pull/2327), by @guicamest

* Remove redundant arrow in function literal without parameters / fix documentation - [#2365](https://github.com/pinterest/ktlint/pull/2365), by @paul-dingemans

* Move curly brace before all consecutive comments preceding that curly brace - [#2375](https://github.com/pinterest/ktlint/pull/2375), by @paul-dingemans

* Prevent stack overflow exception when code provided via stdin can not be parsed as Kotlin, nor Kotlin script - [#2380](https://github.com/pinterest/ktlint/pull/2380), by @paul-dingemans

* Fix searching from inside a hidden directory - [#2377](https://github.com/pinterest/ktlint/pull/2377), by @kitterion

* Prevent unwanted joining of KDoc with preceding type-parameter-list - [#2381](https://github.com/pinterest/ktlint/pull/2381), by @paul-dingemans

* Fix false positive violation in `annotation` rule - [#2400](https://github.com/pinterest/ktlint/pull/2400), by @paul-dingemans

* Replace all function bodies with body expressions in a single run - [#2395](https://github.com/pinterest/ktlint/pull/2395), by @paul-dingemans

* Fix offset for violation when final newline is missing - [#2407](https://github.com/pinterest/ktlint/pull/2407), by @paul-dingemans

* Fix path to ktlint JAR file in `ktlint.bat` - [#2408](https://github.com/pinterest/ktlint/pull/2408), by @paul-dingemans

* Simplify `max-line-length` implementation - [#2410](https://github.com/pinterest/ktlint/pull/2410), by @paul-dingemans

* Remove deprecated cli parameters `--experimental`, `--code-style`, `--disabled-rules` - [#2411](https://github.com/pinterest/ktlint/pull/2411), by @paul-dingemans

* Fix adding blank line between declaration and an annotated declaration which is preceded by comment - [#2429](https://github.com/pinterest/ktlint/pull/2429), by @paul-dingemans

* Update CODE_OF_CONDUCT with correct path - [#2437](https://github.com/pinterest/ktlint/pull/2437), by @OriginalMHV

### 📦 Dependencies


* fix(deps): update dependency org.jetbrains.dokka:dokka-gradle-plugin to v1.9.10 - [#2323](https://github.com/pinterest/ktlint/pull/2323), by @renovate[bot]

* fix(deps): update dependency org.junit.jupiter:junit-jupiter to v5.10.1 - [#2342](https://github.com/pinterest/ktlint/pull/2342), by @renovate[bot]

* fix(deps): update kotlin monorepo to v1.9.21 - [#2374](https://github.com/pinterest/ktlint/pull/2374), by @renovate[bot]

* fix(deps): update dependency org.codehaus.janino:janino to v3.1.11 - [#2387](https://github.com/pinterest/ktlint/pull/2387), by @renovate[bot]

* fix(deps): update dependency dev.drewhamilton.poko:poko-gradle-plugin to v0.15.1 - [#2389](https://github.com/pinterest/ktlint/pull/2389), by @renovate[bot]

* chore(deps): update dependency gradle to v8.5 - [#2392](https://github.com/pinterest/ktlint/pull/2392), by @renovate[bot]

* chore(deps): update actions/checkout digest to b4ffde6 - [#2329](https://github.com/pinterest/ktlint/pull/2329), by @renovate[bot]

* chore(deps): update actions/setup-java action to v4 - [#2393](https://github.com/pinterest/ktlint/pull/2393), by @renovate[bot]

* fix(deps): update dependency ch.qos.logback:logback-classic to v1.3.14 - [#2406](https://github.com/pinterest/ktlint/pull/2406), by @renovate[bot]

* fix(deps): update dependency io.github.hakky54:logcaptor to v2.9.2 - [#2409](https://github.com/pinterest/ktlint/pull/2409), by @renovate[bot]

* fix(deps): update dependency io.github.oshai:kotlin-logging-jvm to v5.1.4 - [#2439](https://github.com/pinterest/ktlint/pull/2439), by @renovate[bot]

## [1.0.1] - 2023-10-13

### 🆕 Features

* Add `.editorconfig` property `ktlint_function_naming_ignore_when_annotated_with` so that rule `function-naming` can be ignored based on annotations on that rule. See [function-naming](https://pinterest.github.io/ktlint/1.0.1/rules/standard/#function-naming).

### 🔧 Fixes

* Update badge for Maven Central - [#2245](https://github.com/pinterest/ktlint/pull/2245), by @Goooler

* Fix code style parameter in cli - [#2241](https://github.com/pinterest/ktlint/pull/2241), by @paul-dingemans

* Anonymous function in assignment - [#2263](https://github.com/pinterest/ktlint/pull/2263), by @paul-dingemans

* Fix indent of multiline object declaration inside class - [#2266](https://github.com/pinterest/ktlint/pull/2266), by @paul-dingemans

* Do not replace function body with multiple exit points - [#2273](https://github.com/pinterest/ktlint/pull/2273), by @paul-dingemans

* Ignore override of function in rule `function-naming` - [#2274](https://github.com/pinterest/ktlint/pull/2274), by @paul-dingemans

* Suppress `function-naming` based on annotations - [#2275](https://github.com/pinterest/ktlint/pull/2275), by @paul-dingemans

* Force blank line before object declaration - [#2287](https://github.com/pinterest/ktlint/pull/2287), by @paul-dingemans

* Multiline expression wrapping - [#2290](https://github.com/pinterest/ktlint/pull/2290), by @paul-dingemans

* Ignore function naming in Kotest classes - [#2291](https://github.com/pinterest/ktlint/pull/2291), by @paul-dingemans

* Improve violation message in `discouraged-comment-location` - [#2293](https://github.com/pinterest/ktlint/pull/2293), by @paul-dingemans

* Fix malformed AST when `&&` or `||` is at start of line `chain-wrapping` - [#2300](https://github.com/pinterest/ktlint/pull/2300), by @paul-dingemans

* Do not report false positives `type-argument-list-spacing` and `type-parameter-list-spacing` - [#2303](https://github.com/pinterest/ktlint/pull/2303), by @paul-dingemans

* Fix chain method continuation containing higher order function call - [#2305](https://github.com/pinterest/ktlint/pull/2305), by @paul-dingemans

### 📦 Dependencies

* Update dependency io.github.detekt.sarif4k:sarif4k to v0.5.0 - [#2277](https://github.com/pinterest/ktlint/pull/2277), by @renovate[bot]

* Update dependency gradle to v8.4 - [#2294](https://github.com/pinterest/ktlint/pull/2294), by @renovate[bot]

* Update actions/checkout action to v4 - [#2225](https://github.com/pinterest/ktlint/pull/2225), by @renovate[bot]

* Update actions/checkout digest to 8ade135 - [#2295](https://github.com/pinterest/ktlint/pull/2295), by @renovate[bot]

### 💬 Other

* Simple property assignments in KTS - [#2123](https://github.com/pinterest/ktlint/pull/2123), by @Goooler

## [1.0.0] - 2023-09-05

### 💔 Breaking changes

* Update and align Maven coordinates - [#2195](https://github.com/pinterest/ktlint/pull/2195), by @paul-dingemans  
  Be sure to update Maven coordinates below, to get latest changes!  
  
  | Old Maven coordinates                              | New Maven coordinates                                  |
  |----------------------------------------------------|--------------------------------------------------------|
  | com.pinterest.ktlint                               | com.pinterest.ktlint:ktlint-cli                        |
  | com.pinterest.ktlint:ktlint-reporter-baseline      | com.pinterest.ktlint:ktlint-cli-reporter-baseline      |
  | com.pinterest.ktlint:ktlint-reporter-checkstyle    | com.pinterest.ktlint:ktlint-cli-reporter-checkstyle    |
  | com.pinterest.ktlint:ktlint-cli-reporter           | com.pinterest.ktlint:ktlint-cli-reporter-core          |
  | com.pinterest.ktlint:ktlint-reporter-format        | com.pinterest.ktlint:ktlint-cli-reporter-format        |
  | com.pinterest.ktlint:ktlint-reporter-html          | com.pinterest.ktlint:ktlint-cli-reporter-html          |
  | com.pinterest.ktlint:ktlint-reporter-json          | com.pinterest.ktlint:ktlint-cli-reporter-json          |
  | com.pinterest.ktlint:ktlint-reporter-plain         | com.pinterest.ktlint:ktlint-cli-reporter-plain         |
  | com.pinterest.ktlint:ktlint-reporter-plain-summary | com.pinterest.ktlint:ktlint-cli-reporter-plain-summary |
  | com.pinterest.ktlint:ktlint-reporter-sarif         | com.pinterest.ktlint:ktlint-cli-reporter-sarif         |


* Add binary compatibility validator - [#2131](https://github.com/pinterest/ktlint/pull/2131), by @mateuszkwiecinski

* Replace kotlin public `data class`es with Poko compiler plugin generated ones - [#2136](https://github.com/pinterest/ktlint/pull/2136), by @mateuszkwiecinski  
  As a part of public API stabilization, data classes are no longer used in the public API. As of that, functions like `copy()` or `componentN()` (used for destructuring declarations) are not available anymore.

* Promote experimental rules - [#2218](https://github.com/pinterest/ktlint/pull/2218), by @paul-dingemans  
  The rules below have been promoted to non-experimental rules:
  * [blank-line-before-declaration](https://pinterest.github.io/ktlint/1.0.0/rules/standard/#blank-line-before-declarations)
  * [context-receiver-wrapping](https://pinterest.github.io/ktlint/1.0.0/rules/standard/#content-receiver-wrapping)
  * [discouraged-comment-location](https://pinterest.github.io/ktlint/1.0.0/rules/standard/#discouraged-comment-location)
  * [enum-wrapping](https://pinterest.github.io/ktlint/1.0.0/rules/standard/#enum-wrapping)
  * [function-naming](https://pinterest.github.io/ktlint/1.0.0/rules/standard/#function-naming)
  * [function-signature](https://pinterest.github.io/ktlint/1.0.0/rules/standard/#function-signature)
  * [if-else-bracing](https://pinterest.github.io/ktlint/1.0.0/rules/standard/#if-else-bracing)
  * [multiline-expression-wrapping](https://pinterest.github.io/ktlint/1.0.0/rules/standard/#multiline-expression-wrapping)
  * [if-else-wrapping](https://pinterest.github.io/ktlint/1.0.0/rules/standard/#if-else-wrapping)
  * [no-blank-line-in-list](https://pinterest.github.io/ktlint/1.0.0/rules/standard/#no-blank-line-in-list)
  * [no-consecutive-comments](https://pinterest.github.io/ktlint/1.0.0/rules/standard/#no-consecutive-comments)
  * [no-empty-file](https://pinterest.github.io/ktlint/1.0.0/rules/standard/#no-empty-file)
  * [no-empty-first-line-in-class-body](https://pinterest.github.io/ktlint/1.0.0/rules/standard/#no-empty-first-line-in-class-body)
  * [no-single-line-block-comment](https://pinterest.github.io/ktlint/1.0.0/rules/standard/#no-single-line-block-comment)
  * [parameter-list-spacing](https://pinterest.github.io/ktlint/1.0.0/rules/standard/#parameter-list-spacing)
  * [property-naming](https://pinterest.github.io/ktlint/1.0.0/rules/standard/#property-naming)
  * [statement-wrapping](https://pinterest.github.io/ktlint/1.0.0/rules/standard/#statement-wrapping)
  * [string-template-indent](https://pinterest.github.io/ktlint/1.0.0/rules/standard/#string-template-indent)
  * [try-catch-finally-spacing](https://pinterest.github.io/ktlint/1.0.0/rules/standard/#try-catch-finally-spacing)
  * [type-argument-list-spacing](https://pinterest.github.io/ktlint/1.0.0/rules/standard/#type-argument-list-spacing)
  * [type-parameter-list-spacing](https://pinterest.github.io/ktlint/1.0.0/rules/standard/#type-parameter-list-spacing)
  * [unnecessary-parentheses-before-trailing-lambda](https://pinterest.github.io/ktlint/1.0.0/rules/standard/#unnecessary-parentheses-before-trailing-lambda)

* Fix statement-wrapping and align rule classes - [#2178](https://github.com/pinterest/ktlint/pull/2178), by @paul-dingemans  
  Rule class `MultilineExpressionWrapping` has been renamed to `MultilineExpressionWrappingRule`. Rule class `StatementWrapping` has been renamed to `StatementWrappingRule`. `RULE_ID` constants below are moved to a different Java class at compile time. Each rule provided by Ktlint is to be accompanied by a `RULE_ID` constant that can be used in the `VisitorModifier.RunAfter`. Filenames did not comply with standard that it should end with `Rule` suffix.  
  
  | RULE ID                                 | Old Java class name           | New Java class name               |
  |-----------------------------------------|-------------------------------|-----------------------------------|
  | `FUNCTION_EXPRESSION_BODY_RULE_ID`      | FunctionExpressionBodyKt      | FunctionExpressionBodyRuleKt      |
  | `FUNCTION_LITERAL_RULE_ID`              | FunctionLiteralKt             | FunctionLiteralRuleKt             |
  | `MULTILINE_EXPRESSION_WRAPPING_RULE_ID` | MultilineExpressionWrappingKt | MultilineExpressionWrappingRuleKt |
  | `NO_BLANK_LINE_IN_LIST_RULE_ID`         | NoBlankLineInListKt           | NoBlankLineInListRuleKt           |
  | `NO_EMPTY_FILE_RULE_ID`                 | (not applicable)              | NoEmptyFileRuleKt                 |
  
* Update to Kotlin 1.9 & remove TreeCopyHandler extension - [#2113](https://github.com/pinterest/ktlint/pull/2113), by @paul-dingemans  
  Class `org.jetbrains.kotlin.com.intellij.treeCopyHandler` is no longer registered as extension point for the compiler as this is not supported in Kotlin 1.9. Please test your custom rules. In case of unexpected exceptions during formatting of code, see [#2044](https://github.com/pinterest/ktlint/pull/2044) for possible remediation.

### 🆕 Features


* Change default code style to `ktlint_official` - [#2144](https://github.com/pinterest/ktlint/pull/2144), by @paul-dingemans

* Add new experimental rule `class-signature` - [#2119](https://github.com/pinterest/ktlint/pull/2119), by @paul-dingemans

* Add new experimental rule `function-expression-body` - [#2151](https://github.com/pinterest/ktlint/pull/2151), by @paul-dingemans

* Add new experimental rule `chain-method-continuation` - [#2088](https://github.com/pinterest/ktlint/pull/2088), by @atulgpt

* Add new experimental rule `function-literal` - [#2137](https://github.com/pinterest/ktlint/pull/2137), by @paul-dingemans

* Add new experimental rule `function-type-modifier-spacing` rule - [#2216](https://github.com/pinterest/ktlint/pull/2216), by @t-kameyama

* Define `EditorConfigOverride` for dynamically loaded ruleset - [#2194](https://github.com/pinterest/ktlint/pull/2194), by @paul-dingemans
  The `EditorConfigOverride` parameter of the `KtlintRuleEngine` can be defined using the factory method `EditorConfigOverride.from(vararg properties: Pair<EditorConfigProperty<*>, *>)`. This requires the `EditorConfigProperty`'s to be available at compile time. Some common `EditorConfigProperty`'s are defined in `ktlint-rule-engine-core` which is loaded as transitive dependency of `ktlint-rule-engine` and as of that are available at compile.
  If an `EditorConfigProperty` is defined in a `Rule` that is only provided via a runtime dependency, it gets a bit more complicated. The `ktlint-api-consumer` example has now been updated to show how the `EditorConfigProperty` can be retrieved from the `Rule`.

* Move wrapping on semicolon from `wrapping` rule to `statement-wrapping` rule - [#2222](https://github.com/pinterest/ktlint/pull/2222), by @paul-dingemans

### 🔧 Fixes

* Do not indent class body for classes having a long super type list - [#2116](https://github.com/pinterest/ktlint/pull/2116), by @paul-dingemans

* Fix indent of explicit constructor - [#2118](https://github.com/pinterest/ktlint/pull/2118), by @paul-dingemans

* Fix incorrect formatting of nested function literal - [#2107](https://github.com/pinterest/ktlint/pull/2107), by @paul-dingemans

* Add property to disable ktlint for a glob in `.editorconfig` - [#2108](https://github.com/pinterest/ktlint/pull/2108), by @paul-dingemans

* Fix spacing around colon in annotations - [#2126](https://github.com/pinterest/ktlint/pull/2126), by @paul-dingemans

* Fix solving problems in 3 consecutive runs - [#2132](https://github.com/pinterest/ktlint/pull/2132), by @paul-dingemans

* Fix indent parenthesized expression - [#2127](https://github.com/pinterest/ktlint/pull/2127), by @paul-dingemans

* Fix indent of IS_EXPRESSION, PREFIX_EXPRESSION and POSTFIX_EXPRESSION - [#2125](https://github.com/pinterest/ktlint/pull/2125), by @paul-dingemans

* Do not wrap a binary expression after an elvis operator - [#2134](https://github.com/pinterest/ktlint/pull/2134), by @paul-dingemans

* Drop obsolete class LintError in ktlint-api-consumer - [#2145](https://github.com/pinterest/ktlint/pull/2145), by @paul-dingemans

* Fix null pointer exception for if-else statement with empty THEN block - [#2142](https://github.com/pinterest/ktlint/pull/2142), by @paul-dingemans

* Fix false positive in property-naming - [#2141](https://github.com/pinterest/ktlint/pull/2141), by @paul-dingemans

* Store relative path of file in baseline file - [#2147](https://github.com/pinterest/ktlint/pull/2147), by @paul-dingemans

* Fix url of build status badge - [#2162](https://github.com/pinterest/ktlint/pull/2162), by @paul-dingemans

* Update CONTRIBUTING.md - [#2163](https://github.com/pinterest/ktlint/pull/2163), by @oshai

* Fix statement-wrapping and align rule classes - [#2178](https://github.com/pinterest/ktlint/pull/2178), by @paul-dingemans

* Fix alignment of type constraints after `where` keyword in function - [#2180](https://github.com/pinterest/ktlint/pull/2180), by @paul-dingemans

* Fix wrapping of multiline postfix expression - [#2184](https://github.com/pinterest/ktlint/pull/2184), by @paul-dingemans

* Do not wrap expression after a spread operator - [#2193](https://github.com/pinterest/ktlint/pull/2193), by @paul-dingemans

* Do not remove parenthesis after explicit class constructor without arguments - [#2226](https://github.com/pinterest/ktlint/pull/2226), by @paul-dingemans

* Fix conflict between rules due to annotated super type call - [#2227](https://github.com/pinterest/ktlint/pull/2227), by @paul-dingemans

* Fix indentation of super type list of class in case it is preceded by  a comment - [#2228](https://github.com/pinterest/ktlint/pull/2228), by @paul-dingemans

* Super type list starting with an annotation having a parameters - [#2230](https://github.com/pinterest/ktlint/pull/2230), by @paul-dingemans

* Do not wrap values in a single line enum when it is preceded by a comment or an annotation - [#2229](https://github.com/pinterest/ktlint/pull/2229), by @paul-dingemans

### 📦 Dependencies


* Update dependency org.codehaus.janino:janino to v3.1.10 - [#2110](https://github.com/pinterest/ktlint/pull/2110), by @renovate[bot]

* Update dependency com.google.jimfs:jimfs to v1.3.0 - [#2112](https://github.com/pinterest/ktlint/pull/2112), by @renovate[bot]

* Update dependency org.junit.jupiter:junit-jupiter to v5.10.0 - [#2148](https://github.com/pinterest/ktlint/pull/2148), by @renovate[bot]

* Update dependency io.github.oshai:kotlin-logging-jvm to v5.1.0 - [#2174](https://github.com/pinterest/ktlint/pull/2174), by @renovate[bot]

* Update dependency dev.drewhamilton.poko:poko-gradle-plugin to v0.15.0 - [#2173](https://github.com/pinterest/ktlint/pull/2173), by @renovate[bot]

* Update plugin org.gradle.toolchains.foojay-resolver-convention to v0.7.0 - [#2187](https://github.com/pinterest/ktlint/pull/2187), by @renovate[bot]

* Update dependency gradle to v8.3 - [#2186](https://github.com/pinterest/ktlint/pull/2186), by @renovate[bot]

* Update kotlin monorepo to v1.9.10 - [#2197](https://github.com/pinterest/ktlint/pull/2197), by @renovate[bot]

* Update dependency info.picocli:picocli to v4.7.5 - [#2215](https://github.com/pinterest/ktlint/pull/2215), by @renovate[bot]

* Update dependency org.jetbrains.dokka:dokka-gradle-plugin to v1.9.0 - [#2221](https://github.com/pinterest/ktlint/pull/2221), by @renovate[bot]

* Update dependency org.slf4j:slf4j-simple to v2.0.9 - [#2224](https://github.com/pinterest/ktlint/pull/2224), by @renovate[bot]

### 💬 Other


* Setup toolchains, compile project with Java 20 only, run test on various Java versions - [#2120](https://github.com/pinterest/ktlint/pull/2120), by @mateuszkwiecinski

* Add release-changelog-builder-action to temporary workflow - [#2196](https://github.com/pinterest/ktlint/pull/2196), by @paul-dingemans

## [0.50.0] - 2023-06-29

### Deprecation of ktlint-enable and ktlint-disable directives

The `ktlint-disable` and `ktlint-enable` directives are no longer supported. Ktlint rules can now only be suppressed using the `@Suppress` or `@SuppressWarnings` annotations. A new rule, `internal:ktlint-suppression`, is provided to replace the directives with annotations.

API consumers do not need to provide this rule, but it does no harm when done.

The `internal:ktlint-suppression` rule can not be disabled via the `.editorconfig` nor via `@Suppress` or `@SuppressWarnings` annotations.

### Custom Rule Providers need to prepare for Kotlin 1.9

In Kotlin 1.9 the extension points of the embedded kotlin compiler will change. Ktlint only uses the `org.jetbrains.kotlin.com.intellij.treeCopyHandler` extension point. This extension is not yet supported in 1.9, neither is it documented ([#KT-58704](https://youtrack.jetbrains.com/issue/KT-58704/Support-and-document-extension-point-org.jetbrains.kotlin.com.intellij.treeCopyHandler)). Without this extension point it might happen that your custom rules will throw exceptions during runtime. See [#1981](https://github.com/pinterest/ktlint/issues/1981).

In Ktlint, 7 out of 77 rules needed small and sometimes bigger changes to become independent of the extension point `org.jetbrains.kotlin.com.intellij.treeCopyHandler`. The impact on your custom rules may vary dependent on the way the autocorrect has been implemented. When manipulating `ASTNode`s there seems to be no impact. When, manipulating `PsiElement`s, some functions consistently result in a runtime exception.

Based on the refactoring of the rules as provided by `ktlint-ruleset-standard` in Ktlint `0.49.x` the suggested refactoring is as follows:

* Replace `LeafElement.replaceWithText(String)` with `LeafElement.rawReplaceWithText(String)`.
* Replace `PsiElement.addAfter(PsiElement, PsiElement)` with `AstNode.addChild(AstNode, AstNode)`. Note that this method inserts the new node (first) argument *before* the second argument node and as of that is not a simple replacement of the `PsiElement.addAfter(PsiElement, PsiElement)`.
* Replace `PsiElement.replace(PsiElement)` with a sequence of `AstNode.addChild(AstNode, AstNode)` and `AstNode.removeChild(AstNode)`.

Be aware that your custom rules might use other functions which also throw exceptions when the extension point `org.jetbrains.kotlin.com.intellij.treeCopyHandler` is no longer supported.

In order to help you to analyze and fix the problems with your custom rules, ktlint temporarily supports to disable the extension point `org.jetbrains.kotlin.com.intellij.treeCopyHandler` using a flag. This flag is available in the Ktlint CLI and in the `KtlintRuleEngine`. By default, the extension point is enabled like it was in previous versions of ktlint.

At least you should analyze the problems by running your test suits by running ktlint and disabling the extension point. Next you can start with fixing and releasing the updated rules. All rules in this version of ktlint have already been refactored and are not dependent on the extension point anymore. In Ktlint CLI the flag is to be activated with parameter `--disable-kotlin-extension-point`. API Consumers that use the `KtlintRuleEngine` directly, have to set property `enableKotlinCompilerExtensionPoint` to `false`.

At this point in time, it is not yet decided what the next steps will be. Ktlint might drop the support of the extension points entirely. Or, if the extension point `org.jetbrains.kotlin.com.intellij.treeCopyHandler` is fully supported at the time that ktlint will be based on kotlin 1.9 it might be kept. In either case, the flag will be dropped in a next version of ktlint.

### Added

* Add new experimental rule `binary-expression-wrapping`. This rule wraps a binary expression in case the max line length is exceeded ([#1940](https://github.com/pinterest/ktlint/issues/1940))
* Add flag to disable extension point `org.jetbrains.kotlin.com.intellij.treeCopyHandler` to analyze impact on custom rules [#1981](https://github.com/pinterest/ktlint/issues/1981)
* Add new experimental rule `no-empty-file` for all code styles. A kotlin (script) file may not be empty ([#1074](https://github.com/pinterest/ktlint/issues/1074))
* Add new experimental rule `statement-wrapping` which ensures function, class, or other blocks statement body doesn't start or end at starting or ending braces of the block ([#1938](https://github.com/pinterest/ktlint/issues/1938)). Note, although this rule is added in this release, it is never executed except in unit tests.
* Add new experimental rule `blank-line-before-declaration`. This rule requires a blank line before class, function or property declarations ([#1939](https://github.com/pinterest/ktlint/issues/1939))
* Wrap multiple statements on same line `wrapping` ([#1078](https://github.com/pinterest/ktlint/issues/1078))
* Add new rule `ktlint-suppression` to replace the `ktlint-disable` and `ktlint-enable` directives with annotations. This rule can not be disabled via the `.editorconfig` ([#1947](https://github.com/pinterest/ktlint/issues/1947))
* Inform user about using `--format` option of KtLint CLI when finding a violation that can be autocorrected ([#1071](https://github.com/pinterest/ktlint/issues/1071))

### Removed

* Code which was deprecated in `0.49.x` is removed. Consult changelog of 0.49.x` released for more information. Summary of removed code: 

### Fixed

* Do not flag a (potential) mutable extension property in case the getter is annotated or prefixed with a modifier `property-naming` ([#2024](https://github.com/pinterest/ktlint/issues/2024))
* Do not merge an annotated expression body with the function signature even if it fits on a single line ([#2043](https://github.com/pinterest/ktlint/issues/2043))
* Ignore property with name `serialVersionUID` in `property-naming` ([#2045](https://github.com/pinterest/ktlint/issues/2045))
* Prevent incorrect reporting of violations in case a nullable function type reference exceeds the maximum line length `parameter-list-wrapping` ([#1324](https://github.com/pinterest/ktlint/issues/1324)) 
* Prevent false negative on `else` branch when body contains only chained calls or binary expression ([#2057](https://github.com/pinterest/ktlint/issues/2057))
* Fix indent when property value is wrapped to next line ([#2095](https://github.com/pinterest/ktlint/issues/2095)) 

### Changed

* Fix Java interoperability issues with `RuleId` and `RuleSetId` classes. Those classes were defined as value classes in `0.49.0` and `0.49.1`. Although the classes were marked with `@JvmInline` it seems that it is not possible to uses those classes from Java base API Consumers like Spotless. The classes have now been replaced with data classes ([#2041](https://github.com/pinterest/ktlint/issues/2041))
* Update dependency `info.picocli:picocli` to `v4.7.4`
* Update dependency `org.junit.jupiter:junit-jupiter` to `v5.9.3`
* Update Kotlin development version to `1.8.22` and Kotlin version to `1.8.22`.
* Update dependency `io.github.detekt.sarif4k:sarif4k` to `v0.4.0`
* Update dependency `org.jetbrains.dokka:dokka-gradle-plugin` to `v1.8.20`
* Run format up to 3 times in case formatting introduces changes which also can be autocorrected ([#2084](https://github.com/pinterest/ktlint/issues/2084))

## [0.49.1] - 2023-05-12

### Added

### Removed

### Fixed
* Store path of file containing a lint violation relative to the location of the baseline file itself ([#1962](https://github.com/pinterest/ktlint/issues/1962))
* Print absolute path of file in lint violations when flag "--relative" is not specified in Ktlint CLI ([#1963](https://github.com/pinterest/ktlint/issues/1963)) 
* Handle parameter `--code-style=android_studio` in Ktlint CLI identical to deprecated parameter `--android` ([#1982](https://github.com/pinterest/ktlint/issues/1982))
* Prevent nullpointer exception (NPE) if class without body is followed by multiple blank lines until end of file `no-consecutive-blank-lines` ([#1987](https://github.com/pinterest/ktlint/issues/1987))
* Allow to 'unset' the `.editorconfig` property `ktlint_function_signature_rule_force_multiline_when_parameter_count_greater_or_equal_than` when using `ktlint_official` code style `function-signature` ([#1977](https://github.com/pinterest/ktlint/issues/1977))
* Prevent nullpointer exception (NPE) if or operator at start of line is followed by dot qualified expression `indent` ([#1993](https://github.com/pinterest/ktlint/issues/1993))
* Fix indentation of multiline parameter list in function literal `indent` ([#1976](https://github.com/pinterest/ktlint/issues/1976))
* Restrict indentation of closing quotes to `ktlint_official` code style to keep formatting of other code styles consistent with `0.48.x` and before `indent` ([#1971](https://github.com/pinterest/ktlint/issues/1971))
* Extract rule `no-single-line-block-comment` from `comment-wrapping` rule. The `no-single-line-block-comment` rule is added as experimental rule to the `ktlint_official` code style, but it can be enabled explicitly for the other code styles as well. ([#1980](https://github.com/pinterest/ktlint/issues/1980))
* Cleanup unwanted logging dependencies ([#1998](https://github.com/pinterest/ktlint/issues/1998))
* Fix directory traversal for patterns referring to paths outside of current working directory or any of it child directories ([#2002](https://github.com/pinterest/ktlint/issues/2002))
* Prevent multiple expressions on same line separated by semicolon ([#1078](https://github.com/pinterest/ktlint/issues/1078))

### Changed

* Moved class `Baseline` from `ktlint-cli` to `ktlint-cli-reporter-baseline` so that Baseline functionality is reusable for API Consumers.

## [0.49.0] - 2023-04-21

WARNING: This version of KtLint contains a number of breaking changes in KtLint CLI and KtLint API. If you are using KtLint with custom ruleset jars or custom reporter jars, then those need to be upgraded before you can use them with this version of ktlint. Please contact the maintainers of those jars and ask them to upgrade a.s.a.p.

All rule id's in the output of Ktlint are now prefixed with a rule set. Although KtLint currently supports standard rules to be unqualified, users are encouraged to include the rule set id in all references to rules. This includes following:
* The `--disabled-rules` parameter in KtLint CLI.
* The `.editorconfig` properties used to enable or disable rule and rule sets. Note that properties `disabled_rules` and `ktlint_disabled_rules` have been removed in this release. See [disabled rules](https://pinterest.github.io/ktlint/rules/configuration-ktlint/#disabled-rules) for more information.
* The `source` element in the KtLint CLI `baseline.xml` file. Regenerating this file, fixes all rule references automatically.
* The KtLint disable directives `ktlint-enable` / `ktlint-disable` and the `@Suppress('ktlint:...')` annotations.
* The `VisitorModifier.RunAfterRule`.

### Moving experimental rules to standard rule set

The `experimental` rule set has been merged with `standard` rule set. The rules which formerly were part of the `experimental` rule set are still being treated as before. The rules will only be run in case `.editorconfig` property `ktlint_experimental` is enabled or in case the rule is explicitly enabled.

Note that the prefix `experimental:` has to be removed from all references to this rule. Check references in:
* The `--disabled-rules` parameter in KtLint CLI.
* The `.editorconfig` properties used to enable or disable rule and rule sets. Note that properties `disabled_rules` and `ktlint_disabled_rules` have been removed in this release. See [disabled rules](https://pinterest.github.io/ktlint/rules/configuration-ktlint/#disabled-rules) for more information.
* The KtLint disable directives `ktlint-enable` / `ktlint-disable` and the `@Suppress('ktlint:...')` annotations.
* The `VisitorModifier.RunAfterRule`.

### Promote experimental rules to non-experimental

The rules below have been promoted to non-experimental rules:
* [block-comment-initial-star-alignment](https://pinterest.github.io/ktlint/rules/standard/#block-comment-initial-star-alignment)
* [class-naming](https://pinterest.github.io/ktlint/rules/standard/#classobject-naming)
* [comment-wrapping](https://pinterest.github.io/ktlint/rules/standard/#comment-wrapping)
* [function-return-type-spacing](https://pinterest.github.io/ktlint/rules/standard/#function-return-type-spacing)
* [function-start-of-body-spacing](https://pinterest.github.io/ktlint/rules/standard/#function-start-of-body-spacing)
* [function-type-reference-spacing](https://pinterest.github.io/ktlint/rules/standard/#function-type-reference-spacing)
* [fun-keyword-spacing](https://pinterest.github.io/ktlint/rules/standard/#fun-keyword-spacing)
* [kdoc-wrapping](https://pinterest.github.io/ktlint/rules/standard/#kdoc-wrapping)
* [modifier-list-spacing](https://pinterest.github.io/ktlint/rules/standard/#modifier-list-spacing)
* [nullable-type-spacing](https://pinterest.github.io/ktlint/rules/standard/#nullable-type-spacing)
* [spacing-between-function-name-and-opening-parenthesis](https://pinterest.github.io/ktlint/rules/standard/#spacing-between-function-name-and-opening-parenthesis)
* [unnecessary-parentheses-before-trailing-lambda](https://pinterest.github.io/ktlint/rules/standard/#unnecessary-parenthesis-before-trailing-lambda)

Note that this only affects users that have enabled the standard ruleset while having the experimental rules disabled.

### API Changes & RuleSet providers & Reporter Providers

This release is intended to be the last release before the 1.0.x release of ktlint. If all goes as planned, the 1.0.x release does not contain any new breaking changes with except of removal of functionality which is deprecated in this release.

This release contains a lot of breaking changes which aims to improve the future maintainability of Ktlint. If you get stuck while migrating, please reach out to us by creating an issue.

#### Experimental rules

Rules in custom rule sets can now be marked as experimental by implementing the `Rule.Experimental` interface on the rule. Rules marked with this interface will only be executed by Ktlint if `.editorconfig` property `ktlint_experimental` is enabled or if the rule itself has been enabled explicitly.

When using this feature, experimental rules should *not* be defined in a separate rule set as that would require a distinct rule set id. When moving a rule from an experimental rule set to a non-experimental rule set this would mean that the qualified rule id changes. For users of such rules this means that ktlint directives to suppress the rule and properties in the `.editorconfig` files have to be changed.

#### EditorConfig

Field `defaultAndroidValue` in class `EditorConfigProperty` has been renamed to `androidStudioCodeStyleDefaultValue`. New fields `ktlintOfficialCodeStyleDefaultValue` and `intellijIdeaCodeStyleDefaultValue` have been added. Read more about this in the section "Ktlint Official code style".

The `.editorconfig` properties `disabled_rules` and `ktlint_disabled_rules` are no longer supported. Specifying those properties in the `editorConfigOverride` or `editorConfigDefaults` result in warnings at runtime.

#### 'Ktlint Official` code style and renaming of existing code styles

A new code style `ktlint_official` is introduced. This code style is work in progress but will become the default code style in the `1.0` release. Please try out the new code style and provide your feedback via the [issue tracker](https://github.com/pinterest/ktlint/issues).

This `ktlint_official` code style combines the best elements from the [Kotlin Coding conventions](https://kotlinlang.org/docs/coding-conventions.html) and [Android's Kotlin styleguide](https://developer.android.com/kotlin/style-guide). This code style also provides additional formatting on topics which are not (explicitly) mentioned in those conventions and style guide. But do note that this code style sometimes formats code in a way which is not accepted by the default code formatters in IntelliJ IDEA and Android Studio. The formatters of those editors produce nicely formatted code in the vast majority of cases. But in a number of edge cases, the formatting contains bugs which are waiting to be fixed for several years. The new code style formats code in a way which is compatible with the default formatting of the editors whenever possible. When using this codestyle, it is best to disable (e.g. not use) code formatting in the editor.

The existing code styles have been renamed to make more clear what the basis of the code style is.

The `official` code style has been renamed to `intellij_idea`. Code formatted with this code style aims to be compatible with default formatter of IntelliJ IDEA. This code style is based on [Kotlin Coding conventions](https://kotlinlang.org/docs/coding-conventions.html). If `.editorconfig` property `ktlint_code_style` has been set to `android` then do not forget to change the value of that property to `intellij_idea`. When not set, this is still the default code style of ktlint `0.49`. Note that the default code style will be changed to `ktlint_official` in the `1.0` release.

Code style `android` has been renamed to `android_studio`. Code formatted with this code style aims to be compatible with default formatter of Android Studio. This code style is based on [Android's Kotlin styleguide](https://developer.android.com/kotlin/style-guide). If `.editorconfig` property `ktlint_code_style` has been set to `android` then do not forget to change the value of that property to `android_studio`.

#### Package restructuring and class relocation

The internal structure of the Ktlint project has been revised. The Ktlint CLI and KtLint API modules have been decoupled where possible. Modules have been restructured and renamed. See [API Overview](https://pinterest.github.io/ktlint/api/overview/) for more information.

This is the last release that contains module `ktlint-core` as it had too many responsibilities. All classes in this module are relocated to other modules. Some classes have also been renamed. See tables below for details. Classes that are left behind in the `ktlint-core` module are deprecated and were kept in this version for backwards compatibility only. The `ktlint-core` module will be removed in Ktlint `0.50.x`.

Classes below have been moved from module `ktlint-core` to the new module `ktlint-rule-engine-core`:

| Old class/package name in `ktlint-core`                  | New class/package name in `ktlint-rule-engine-core`                               |
|----------------------------------------------------------|-----------------------------------------------------------------------------------|
| com.pinterest.ktlint.core.api.editorconfig               | com.pinterest.ktlint.rule.engine.core.api.editorconfig                            |
| com.pinterest.ktlint.core.api.EditorConfigProperties     | com.pinterest.ktlint.rule.engine.core.api.EditorConfig                            |
| com.pinterest.ktlint.core.api.OptInFeatures              | com.pinterest.ktlint.rule.engine.core.api.OptInFeatures                           |
| com.pinterest.ktlint.core.ast.ElementType                | com.pinterest.ktlint.rule.engine.core.api.ElementType                             |
| com.pinterest.ktlint.core.ast.package                    | com.pinterest.ktlint.rule.engine.core.api.ASTNodeExtension                        |
| com.pinterest.ktlint.core.IndentConfig                   | com.pinterest.ktlint.rule.engine.core.api.IndentConfig                            |
| com.pinterest.ktlint.core.Rule                           | com.pinterest.ktlint.rule.engine.core.api.Rule                                    |
| com.pinterest.ktlint.core.RuleProvider                   | com.pinterest.ktlint.rule.engine.core.api.RuleProvider                            |

Classes below have been moved from module `ktlint-core` to the new module `ktlint-rule-engine`:

| Old class/package name in `ktlint-core`                  | New class/package name in `ktlint-rule-engine`                               |
|----------------------------------------------------------|------------------------------------------------------------------------------|
| com.pinterest.ktlint.core.api.EditorConfigDefaults       | com.pinterest.ktlint.rule.engine.api.EditorConfigDefaults                    |
| com.pinterest.ktlint.core.api.EditorConfigOverride       | com.pinterest.ktlint.rule.engine.api.EditorConfigOverride                    |
| com.pinterest.ktlint.core.api.KtLintParseException       | com.pinterest.ktlint.rule.engine.api.KtLintParseException                    |
| com.pinterest.ktlint.core.api.KtLintRuleException        | com.pinterest.ktlint.rule.engine.api.KtLintRuleException                     |
| com.pinterest.ktlint.core.KtLint                         | com.pinterest.ktlint.rule.engine.api.KtLintRuleEngine                        |
| com.pinterest.ktlint.core.LintError                      | com.pinterest.ktlint.rule.engine.api.LintError                               |

Class `com.pinterest.ktlint.core.KtLint.Code.CodeFile` has been replaced with factory method `com.pinterest.ktlint.rule.engine.api.Code.fromFile`. Likewise, class `com.pinterest.ktlint.core.KtLint.Code.CodeSnippet` has been replaced with factory method `com.pinterest.ktlint.rule.engine.api.Code.fromSnippet`.

Class below has been moved from module `ktlint-core` to the new module `ktlint-cli-ruleset-core`:

| Old class/package name in `ktlint-core`                  | New class/package name in `ktlint-cli-ruleset-core`                           |
|----------------------------------------------------------|-------------------------------------------------------------------------------|
| com.pinterest.ktlint.core.RuleSetProviderV2              | com.pinterest.ktlint.cli.ruleset.core.api.RuleSetProviderV3                   |


Class below has been moved from module `ktlint-core` to the new module `ktlint-cli-reporter-core`:

| Old class/package name in `ktlint-core`   | New class/package name in `ktlint-cli-reporter-core`     |
|-------------------------------------------|----------------------------------------------------------|
| com.pinterest.ktlint.core.KtlintVersion   | com.pinterest.ktlint.cli.reporter.core.api.KtlintVersion |

Class below has been moved from module `ktlint-core` to the new module `ktlint-logger`:

| Old class/package name in `ktlint-core`               | New class/package name in `ktlint-logger`                   |
|-------------------------------------------------------|-------------------------------------------------------------|
| com.pinterest.ktlint.core.KtLintKLoggerInitializer.kt | com.pinterest.ktlint.logger.api.KtLintKLoggerInitializer.kt |

Class below has been relocated from module `ktlint-core` to module `ktlint-cli`:

| Old class/package name in `ktlint-core` | New class/package name in `ktlint-cli` |
|-----------------------------------------|----------------------------------------|
| com.pinterest.ktlint.core.api.Baseline  | com.pinterest.ktlint.cli.api.Baseline  |

Module `ktlint-reporter-baseline` has been renamed to `ktlint-cli-reporter-baseline`. Class below has been relocated:

| Old class/package name in `ktlint-reporter-baseline` | New class/package name in `ktlint-cli-reporter-baseline` |
|------------------------------------------------------|----------------------------------------------------------|
| com.pinterest.ktlint.reporter.baseline               | com.pinterest.ktlint.cli.reporter.baseline               |

Module `ktlint-reporter-checkstyle` has been renamed to `ktlint-cli-reporter-checkstyle`. Class below has been relocated:

| Old class/package name in `ktlint-reporter-checkstyle` | New class/package name in `ktlint-cli-reporter-checkstyle` |
|--------------------------------------------------------|------------------------------------------------------------|
| com.pinterest.ktlint.reporter.checkstyle               | com.pinterest.ktlint.cli.reporter.checkstyle               |

Module `ktlint-reporter-format` has been renamed to `ktlint-cli-reporter-format`. Class below has been relocated:

| Old class/package name in `ktlint-reporter-format` | New class/package name in `ktlint-cli-reporter-format` |
|----------------------------------------------------|--------------------------------------------------------|
| com.pinterest.ktlint.reporter.format               | com.pinterest.ktlint.cli.reporter.format               |

Module `ktlint-reporter-html` has been renamed to `ktlint-cli-reporter-html`. Class below has been relocated:

| Old class/package name in `ktlint-reporter-html` | New class/package name in `ktlint-cli-reporter-html` |
|--------------------------------------------------|------------------------------------------------------|
| com.pinterest.ktlint.reporter.html               | com.pinterest.ktlint.cli.reporter.html               |

Module `ktlint-reporter-json` has been renamed to `ktlint-cli-reporter-json`. Class below has been relocated:

| Old class/package name in `ktlint-reporter-json` | New class/package name in `ktlint-cli-reporter-json` |
|--------------------------------------------------|------------------------------------------------------|
| com.pinterest.ktlint.reporter.json               | com.pinterest.ktlint.cli.reporter.json               |

Module `ktlint-reporter-plain` has been renamed to `ktlint-cli-reporter-plain`. Class below has been relocated:

| Old class/package name in `ktlint-reporter-plain` | New class/package name in `ktlint-cli-reporter-plain` |
|---------------------------------------------------|-------------------------------------------------------|
| com.pinterest.ktlint.reporter.plain               | com.pinterest.ktlint.cli.reporter.plain               |

Module `ktlint-reporter-plain-summary` has been renamed to `ktlint-cli-reporter-plain-summary`. Class below has been relocated:

| Old class/package name in `ktlint-reporter-plain-summary` | New class/package name in `ktlint-cli-reporter-plain-summary` |
|-----------------------------------------------------------|---------------------------------------------------------------|
| com.pinterest.ktlint.reporter.plain                       | com.pinterest.ktlint.cli.reporter.plainsummary                |

Module `ktlint-reporter-sarif` has been renamed to `ktlint-cli-reporter-sarif`. Class below has been relocated:

| Old class/package name in `ktlint-reporter-sarif` | New class/package name in `ktlint-cli-reporter-sarif` |
|---------------------------------------------------|-------------------------------------------------------|
| com.pinterest.ktlint.reporter.sarif               | com.pinterest.ktlint.cli.reporter.sarif               |

#### Custom Ruleset Provider `RuleSetProviderV2`

Custom rule sets build for older versions of KtLint are no longer supported by this version of KtLint. The `com.pinterest.ktlint.core.RuleSetProviderV2` interface has been replaced with `RuleSetProviderV3`. The accompanying interfaces `com.pinterest.ktlint.core.RuleProvider` and `com.pinterest.ktlint.core.Rule` have been replaced with `com.pinterest.ktlint.ruleset.core.api.RuleProvider` and `com.pinterest.ktlint.ruleset.core.api.Rule` respectively.

Contrary to `RuleSetProviderV2`, the `RuleSetProviderV3` no longer contains information about the rule set. About information now has to be specified in the new `Rule` class. This allows custom rule set providers to combine rules originating from different rule sets into a new rule set without losing information about its origin. The type of the id of the rule set is changed from `String` to `RuleSetId`. 

Note that due to renaming and relocation of the `RuleSetProviderV3` interface the name of the service provider in the custom reporter needs to be changed from `resources/META-INF/services/com.pinterest.ktlint.core.RuleSetProviderV2` to `resources/META-INF/services/com.pinterest.ktlint.cli.ruleset.core.api.RuleSetProviderV3`.

The rule id's in `com.pinterest.ktlint.ruleset.core.api.Rule` have been changed from type `String` to `RuleId`. A `RuleId` has a value that must adhere the convention "`rule-set-id`:`rule-id`". The rule set id `standard` is reserved for rules which are maintained by the KtLint project. Rules created by custom rule set providers and API Consumers should use a prefix other than `standard` to mark the origin of rules which are not maintained by the KtLint project.

When wrapping a rule from the ktlint project and modifying its behavior, please change the `ruleId` and `about` fields in the wrapped rule, so that it is clear to users whenever they use the original rule provided by KtLint versus a modified version which is not maintained by the KtLint project.

The typealias `com.pinterest.ktlint.core.api.EditorConfigProperties` has been replaced with `com.pinterest.ktlint.rule.engine.core.api.EditorConfig`. The interface `com.pinterest.ktlint.core.api.UsesEditorConfigProperties` has been removed. Instead, the Rule property `usesEditorConfigProperties` needs to be set. As a result of those changes, the `beforeFirstNode` function in each rule has to changed to something like below:

```kotlin
 class SomeRule : Rule(
  ruleId = RuleId("some-rule-set:some-rule"),
  usesEditorConfigProperties = setOf(MY_EDITOR_CONFIG_PROPERTY),
) {
  private lateinit var myEditorConfigProperty: MyEditorConfigProperty

  override fun beforeFirstNode(editorConfig: EditorConfig) {
    myEditorConfigProperty = editorConfig[MY_EDITOR_CONFIG_PROPERTY]
  }
  
  ...
}
```

Fields `loadOnlyWhenOtherRuleIsLoaded` and `runOnlyWhenOtherRuleIsEnabled` have been removed from class `com.pinterest.ktlint.rule.engine.core.api.Rule.VisitorModifier.RunAfterRule` and are replaced with a single field `mode`. The `mode` either contains value `REGARDLESS_WHETHER_RUN_AFTER_RULE_IS_LOADED_OR_DISABLED` or `ONLY_WHEN_RUN_AFTER_RULE_IS_LOADED_AND_ENABLED`.

#### Custom Reporter Provider `ReporterProvider`

Custom Reporters build for older versions of KtLint are no longer supported by this version of KtLint. The `com.pinterest.ktlint.core.ReporterProvider` interface has been replaced with `com.pinterest.ktlint.cli.reporter.core.api.ReporterProviderV2`. The accompanying interface `com.pinterest.ktlint.core.Reporter` has been replaced with `com.pinterest.ktlint.cli.reporter.core.api.ReporterV2`.

Note that due to renaming and relocation of the `ReporterProviderV2` interface the name of the service provider in the custom reporter needs to be changed from `resources/META-INF/services/com.pinterest.ktlint.core.ReporterProvider` to `resources/META-INF/services/com.pinterest.ktlint.cli.reporter.core.api.ReporterProviderV2`.

The biggest change in the `ReporterV2` is the replacement of the `LintError` class with `KtlintCliError` class. The latter class now contains a status field which more clearly explains the difference between a lint error which can be autocorrected versus a lint error that actually has been autocorrected.

#### Custom rules provided by API Consumer

API Consumers provide a set of rules directly to the Ktlint Rule Engine. The `com.pinterest.ktlint.core.Rule` has been replaced with `com.pinterest.ktlint.ruleset.core.api.Rule`.

The type of the rule id's has been changed from type `String` to `RuleId`. A `RuleId` has a value that must adhere to the convention "`rule-set-id`:`rule-id`". Rule set id `standard` is reserved for rules which are maintained by the KtLint project. Custom rules created by the API Consumer should use a prefix other than `standard` to clearly mark the origin of rules which are not maintained by the KtLint project.

Also, the field `About` has been added. This field specifies the name of the maintainer, and the repository url and issue tracker url of the rule. The about information of a rule is printed whenever a rule throws an exception which is caught by the Ktlint Rule Engine.

When wrapping a rule from the ktlint project and modifying its behavior, please change the `ruleId` and `about` fields in the wrapped rule, so that it is clear to users whenever they use the original rule provided by KtLint versus a modified version which is not maintained by the KtLint project.

The typealias `com.pinterest.ktlint.core.api.EditorConfigProperties` has been replaced with `com.pinterest.ktlint.rule.engine.core.api.EditorConfig`. The interface `com.pinterest.ktlint.core.api.UsesEditorConfigProperties` has been removed. Instead, the Rule property `usesEditorConfigProperties` needs to be set. As a result of those changes, the `beforeFirstNode` function in each rule has to changed to something like below:

```kotlin
 class SomeRule : Rule(
  ruleId = RuleId("some-rule-set:some-rule"),
  usesEditorConfigProperties = setOf(MY_EDITOR_CONFIG_PROPERTY),
) {
  private lateinit var myEditorConfigProperty: MyEditorConfigProperty

  override fun beforeFirstNode(editorConfig: EditorConfig) {
    myEditorConfigProperty = editorConfig[MY_EDITOR_CONFIG_PROPERTY]
  }

  ...
}
```

Fields `loadOnlyWhenOtherRuleIsLoaded` and `runOnlyWhenOtherRuleIsEnabled` have been removed from class `com.pinterest.ktlint.rule.engine.core.api.Rule.VisitorModifier.RunAfterRule` and are replaced with a single field `mode`. The `mode` either contains value `REGARDLESS_WHETHER_RUN_AFTER_RULE_IS_LOADED_OR_DISABLED` or `ONLY_WHEN_RUN_AFTER_RULE_IS_LOADED_AND_ENABLED`.

Like before, the API Consumer can still offer a mix of rules originating from `ktlint-ruleset-standard` as well as custom rules.

#### `.editorconfig` property `max_line_length` default value

Previously, the default value for `.editorconfig` property `max_line_length` was set to `-1` in ktlint unless the property was defined explicitly in the `.editorconfig` or when `ktlint_code_style` was set to Android. As a result of that rules have to check that max_line_length contains a positive value before checking that the actual line length is exceeding the maximum. Now the value `Int.MAX_VALUE` (use constant `MAX_LINE_LENGTH_PROPERTY_OFF` to refer to that value) is used instead. 

#### Psi filename replaces FILE_PATH_USER_DATA_KEY

Constant `KtLint.FILE_PATH_USER_DATA_KEY` has been removed. The file path is passed correctly to the node with element type FILE and can be retrieved as follows:
```kotlin
if (node.isRoot()) {
  val filePath = (node.psi as? KtFile)?.virtualFilePath
    ...
}
```

### Added

* Add new code style `ktlint_offical`. The code style is work in progress and should be considered a preview. It is intended to become the default code style in the next release. Please try it out and give your feedback. See [code styles](https://pinterest.github.io/ktlint/rules/code-styles/) for more information. The following rules have been added to the `ktlint_official` code style (the rules can also be run for other code styles when enabled explicitly):
  * Add new experimental rule `no-empty-first-line-in-class-body`. This rule disallows a class to start with a blank line.
  * Add new experimental rule `if-else-bracing`. This rules enforces consistent usage of braces in all branches of a single if, if-else or if-else-if statement.
  * Add new experimental rule `no-consecutive-comments`. This rule disallows consecutive comments except EOL comments (see [examples](See https://pinterest.github.io/ktlint/rules/experimental/#disallow-consecutive-comments)).
  * Add new experimental rule `try-catch-finally-spacing`. This rule enforces consistent spacing in try-catch, try-finally and try-catch-finally statement. This rule can also be run for other code styles, but then it needs to be enabled explicitly.
  * Add new experimental rule `no-blank-line-in-list`. This rule disallows blank lines to be used in super type lists, type argument lists, type constraint lists, type parameter lists, value argument lists, and value parameter lists. ([#1224](https://github.com/pinterest/ktlint/issues/1224))
  * Add new experimental rule `multiline-expression-wrapping`. This forces a multiline expression as value in an assignment to start on a separate line. ([#1217](https://github.com/pinterest/ktlint/issues/1217))
  * Add new experimental rule `string-template-indent`. This forces multiline string templates which are post-fixed with `.trimIndent()` to be formatted consistently. The opening and closing `"""` are placed on separate lines and the indentation of the content of the template is aligned with the `"""`. ([#925](https://github.com/pinterest/ktlint/issues/925))
  * Add new experimental rule `if-else-wrapping`. This enforces that a single line if-statement is kept simple. A single line if-statement may contain no more than one else-branch. The branches a single line if-statement may not be wrapped in a block. ([#812](https://github.com/pinterest/ktlint/issues/812))
* Wrap the type or value of a function or class parameter in case the maximum line length is exceeded `parameter-wrapping` ([#1846](https://github.com/pinterest/ktlint/pull/1846))
* Wrap the type or value of a property in case the maximum line length is exceeded `property-wrapping` ([#1846](https://github.com/pinterest/ktlint/pull/1846))
* Recognize Kotlin Script when linting and formatting code from `stdin` with KtLint CLI ([#1832](https://github.com/pinterest/ktlint/issues/1832))
* Support Bill of Materials (BOM), now you can integrate Ktlint in your `build.gradle` like:
  ```kotlin
  dependencies {
      implementation(platform("com.pinterest:ktlint-bom:0.49.0"))
      implementation("com.pinterest:ktlint-core")
      implementation("com.pinterest:ktlint-reporter-html")
      implementation("com.pinterest:ktlint-ruleset-standard")
      ...
  }
  ```
* Add new experimental rule `enum-wrapping` for all code styles. An enum should either be a single line, or each enum entry should be defined on a separate line. ([#1903](https://github.com/pinterest/ktlint/issues/1903))

### Removed

* Remove support of the `.editorconfig` properties `disabled_rules` and `ktlint_disabled_rules`. See [disabled rules](https://pinterest.github.io/ktlint/rules/configuration-ktlint/#disabled-rules) for more information.
* Remove CLI option `--print-ast`. Use IntelliJ IDEA PsiViewer plugin instead. ([#1925](https://github.com/pinterest/ktlint/issues/1925))

### Fixed

* An enumeration class having a primary constructor and in which the list of enum entries is followed by a semicolon then do not remove the semicolon in case it is followed by code element `no-semi` ([#1733](https://github.com/pinterest/ktlint/issues/1733))
* Do not add the (first line of the) body expression on the same line as the function signature in case the max line length would be exceeded. `function-signature`. 
* Do not add the first line of a multiline body expression on the same line as the function signature in case function body expression wrapping property is set to `multiline`. `function-signature`. 
* Disable the `standard:filename` rule whenever Ktlint CLI is run with option `--stdin` ([#1742](https://github.com/pinterest/ktlint/issues/1742))
* The parameters of a function literal containing a multiline parameter list are aligned with first parameter whenever the first parameter is on the same line as the start of that function literal (not allowed in `ktlint_official` code style) `indent` ([#1756](https://github.com/pinterest/ktlint/issues/1756))
* Do not throw exception when enum class does not contain entries `trailing-comma-on-declaration-site` ([#1711](https://github.com/pinterest/ktlint/issues/1711))
* Fix continuation indent for a dot qualified array access expression in `ktlint_official` code style only `indent` ([#1540](https://github.com/pinterest/ktlint/issues/1540))
* When generating the `.editorconfig` use value `off` for the `max_line_length` property instead of value `-1` to denote that lines are not restricted to a maximum length ([#1824](https://github.com/pinterest/ktlint/issues/1824))
* Do not report an "unnecessary semicolon" after adding a trailing comma to an enum class containing a code element after the last enum entry `trailing-comma-on-declaration-site` ([#1786](https://github.com/pinterest/ktlint/issues/1786))
* A newline before a function return type should not be removed in case that leads to exceeding the maximum line length `function-return-type-spacing` ([#1764](https://github.com/pinterest/ktlint/issues/1764))
* Wrap annotations on type arguments in same way as with other constructs `annotation`, `wrapping` ([#1725](https://github.com/pinterest/ktlint/issues/1725))
* Fix indentation of try-catch-finally when catch or finally starts on a newline `indent` ([#1788](https://github.com/pinterest/ktlint/issues/1788))
* Fix indentation of a multiline typealias `indent` ([#1788](https://github.com/pinterest/ktlint/issues/1788))
* Fix false positive when multiple KDOCs exists between a declaration and another annotated declaration `spacing-between-declarations-with-annotations` ([#1802](https://github.com/pinterest/ktlint/issues/1802))
* Fix false positive when a single line statement containing a block having exactly the maximum line length is preceded by a blank line `wrapping` ([#1808](https://github.com/pinterest/ktlint/issues/1808))
* Fix false positive when a single line contains multiple dot qualified expressions and/or safe expressions `indent` ([#1830](https://github.com/pinterest/ktlint/issues/1830))
* Enforce spacing around rangeUntil operator `..<` similar to the range operator `..` in `range-spacing`  ([#1858](https://github.com/pinterest/ktlint/issues/1858))
* When `.editorconfig` property `ij_kotlin_imports_layout` contains a `|` but no import exists that match any pattern before the first `|` then do not report a violation nor insert a blank line `import-ordering` ([#1845](https://github.com/pinterest/ktlint/issues/1845))
* When negate-patterns only are specified in Ktlint CLI then automatically add the default include patterns (`**/*.kt` and `**/*.kts`) so that all Kotlin files excluding the files matching the negate-patterns will be processed ([#1847](https://github.com/pinterest/ktlint/issues/1847))
* Do not remove newlines from multiline type parameter lists `type-parameter-list-spacing` ([#1867](https://github.com/pinterest/ktlint/issues/1867))
* Wrap each type parameter in a multiline type parameter list `wrapping` ([#1867](https://github.com/pinterest/ktlint/issues/1867))
* Allow value arguments with a multiline expression to be indented on a separate line `indent` ([#1217](https://github.com/pinterest/ktlint/issues/1217))
* When enabled, the ktlint rule checking is disabled for all code surrounded by the formatter tags (see [faq](https://pinterest.github.io/ktlint/faq/#are-formatter-tags-respected)) ([#670](https://github.com/pinterest/ktlint/issues/670)) 
* Remove trailing comma if last two enum entries are on the same line and trailing commas are not allowed. `trailing-comma-on-declaration-site` ([#1905](https://github.com/pinterest/ktlint/issues/1905))
* Wrap annotated function parameters to a separate line in code style `ktlint_official` only. `function-signature`, `parameter-list-wrapping` ([#1908](https://github.com/pinterest/ktlint/issues/1908))
* Wrap annotated projection types in type argument lists to a separate line `annotation` ([#1909](https://github.com/pinterest/ktlint/issues/1909))
* Add newline after adding trailing comma in parameter list of a function literal `trailing-comma-on-declaration-site` ([#1911](https://github.com/pinterest/ktlint/issues/1911))
* Wrap annotations before class constructor in code style `ktlint_official`. `annotation` ([#1916](https://github.com/pinterest/ktlint/issues/1916))
* Annotations on type projections should be wrapped in same way as other annotations `annotation` ([#1917](https://github.com/pinterest/ktlint/issues/1917))
* An if-else followed by an elvis operator should not be wrapped in an else-block `multiline-if-else` ([#1904](https://github.com/pinterest/ktlint/issues/1904))

### Changed
* Wrap the parameters of a function literal containing a multiline parameter list (only in `ktlint_official` code style) `parameter-list-wrapping` ([#1681](https://github.com/pinterest/ktlint/issues/1681)).
* KtLint CLI exits with an error in any of following cases (this list is not exhaustive):
  - A custom ruleset jar is to be loaded and that jar contains a deprecated RuleSetProviderV2.
  - A custom ruleset jar is to be loaded and that jar does not contain the required RuleSetProviderV3.
  - A custom reporter jar is to be loaded and that jar contains a deprecated ReporterProvider.
  - A custom reporter jar is to be loaded and that jar does not contain the required ReporterProviderV2.
* Disable the default patterns if the option `--patterns-from-stdin` is specified ([#1793](https://github.com/pinterest/ktlint/issues/1793))
* Update Kotlin development version to `1.8.20` and Kotlin version to `1.8.20`.
* Revert to matrix build to speed up build, especially for the Windows related build ([#1787](https://github.com/pinterest/ktlint/pull/1787))
* For the new code style `ktlint_official`, do not allow wildcard imports `java.util` and `kotlinx.android.synthetic` by default. Important: `.editorconfig` property `ij_kotlin_packages_to_use_import_on_demand` needs to be set to value `unset` in order to enforce IntelliJ IDEA default formatter to not generate wildcard imports `no-wildcard-imports` ([#1797](https://github.com/pinterest/ktlint/issues/1797))
* Convert a single line block comment to an EOL comment if not preceded or followed by another code element on the same line `comment-wrapping` ([#1941](https://github.com/pinterest/ktlint/issues/1941))
* Ignore a block comment inside a single line block `comment-wrapping` ([#1942](https://github.com/pinterest/ktlint/issues/1942))

## [0.48.2] - 2023-01-21

### Additional clarification on API Changes in `0.48.0` and `0.48.1`

Starting with Ktlint `0.48.x`, rule and rule sets can be enabled/disabled with a separate property per rule (set). Please read [deprecation of (ktlint_)disable_rules property](https://pinterest.github.io/ktlint/faq/#why-is-editorconfig-property-disabled_rules-deprecated-and-how-do-i-resolve-this) for more information.

API Consumers that provide experimental rules to the KtLintRuleEngine, must also enable the experimental rules or instruct their users to do so in the `.editorconfig` file. From the perspective of the API Consumer it might be confusing or unnecessary to do so as the experimental rule was already provided explicitly.

Ktlint wants to provide the user (e.g. a developer) a uniform and consistent user experience. The `.editorconfig` becomes more and more central to store configuration for Ktlint. This to ensure that all team members use the exact same configuration when running ktlint regardless whether the Ktlint CLI or an API Consumer is being used.

The `.editorconfig` is a powerful configuration tool which can be used in very different ways. Most projects use a single `.editorconfig` file containing one common section for kotlin and kotlin scripts files. For example, the `.editorconfig` file of the Ktlint project contains following section:
```editorconfig
[*.{kt,kts}]
ij_kotlin_imports_layout = *
ij_kotlin_allow_trailing_comma = true
ij_kotlin_allow_trailing_comma_on_call_site = true
```
Other projects might contain multiple `.editorconfig` files for different parts of the project directory hierarchy. Or, use a single `.editorconfig` file containing multiple sections with different globs. Like all other configuration settings in Ktlint, the user should be able to enable and disable the experimental rules. Both for the entire set of experimental rules and for individual experimental rules.

Ktlint allows API Consumers to set default values and override values for the `.editorconfig`. Specifying a default value means that the user does not need to define the property in the `.editorconfig` file but if the user specifies the value, it will take precedence. Specifying the override value ensures that this takes precedence on a value specified by the user in the `.editorconfig`.

From the Ktlint perspective, it is advised that API Consumers provide the default value. See example below, for how to specify the `editorConfigDefault` property:
```
KtLintRuleEngine(
    ruleProviders = ruleProviders,
    editorConfigDefaults = EditorConfigDefaults(
        EditorConfig
            .builder()
            .section(
                Section
                    .builder()
                    .glob(Glob("*.{kt,kts}"))
                    .properties(
                        Property
                            .builder()
                            .name("ktlint_experimental")
                            .value("enabled"),
                    ),
            )
            .build()
    )
)
```
If the user has set property `ktlint_experimental` explicitly than that value will be used. If the value is not defined, the value provided via `editorConfigDefaults` will be used.

If you do want to ignore the value of `ktlint_experimental` as set by the user, than you can set the EditorConfigOverride property. But as said before that is discouraged as the user might not understand why the `.editorconfig` property is being ignored (provided that the value set is not equal to the value provided by the API Consumer).

### Added

### Removed

### Fixed
* Fix with array-syntax annotations on the same line as other annotations `annotation` ([#1765](https://github.com/pinterest/ktlint/issues/1765))
* Do not enable the experimental rules by default when `.editorconfig` properties `disabled_rules` or `ktlint_disabled_rules` are set. ([#1771](https://github.com/pinterest/ktlint/issues/1771))
* A function signature not having any parameters which exceeds the `max-line-length` should be ignored by rule `function-signature` ([#1773](https://github.com/pinterest/ktlint/issues/1773))
* Allow diacritics in names of classes, functions packages, and properties `class-naming`, `function-naming`, `package-name`, `property-naming` ([#1757](https://github.com/pinterest/ktlint/issues/1757))
* Prevent violation of `file-name` rule on code snippets ([#1768](https://github.com/pinterest/ktlint/issues/1768))
* Clarify that API Consumers have to enable experimental rules ([#1768](https://github.com/pinterest/ktlint/issues/1768))
* Trim spaces in the `.editorconfig` property `ij_kotlin_imports_layout`'s entries ([#1770](https://github.com/pinterest/ktlint/pull/1770))

### Changed

## [0.48.1] - 2023-01-03

### Added

### Removed

### Fixed

* An enumeration class having a primary constructor and in which the list of enum entries is followed by a semicolon then do not remove the semicolon in case it is followed by code element `no-semi` ([#1733](https://github.com/pinterest/ktlint/issues/1733))
* Add API so that KtLint API consumer is able to process a Kotlin script snippet without having to specify a file path ([#1738](https://github.com/pinterest/ktlint/issues/1738))
* Disable the `standard:filename` rule whenever Ktlint CLI is run with option `--stdin` ([#1742](https://github.com/pinterest/ktlint/issues/1742))
* Fix initialization of the logger when `--log-level` is specified. Throw exception when an invalid value is passed. ([#1749](https://github.com/pinterest/ktlint/issues/1749))
* Fix loading of custom rule set JARs.
* Rules provided via a custom rule set JAR (Ktlint CLI) or by an API provider are enabled by default. Only rules in the `experimental` rule set are disabled by default. ([#1747](https://github.com/pinterest/ktlint/issues/1747))

### Changed

* Update Kotlin development version to `1.8.0` and Kotlin version to `1.8.0`.

## [0.48.0] - 2022-12-15

### Indent rule

The `indent` rule has been rewritten from scratch. Solving problems in the old algorithm was very difficult. With the new algorithm this becomes a lot easier. Although the new implementation of the rule has been compared against several open source projects containing over 400,000 lines of code, it is still likely that new issues will be discovered. Please report your indentation issues so that these can be fixed as well.

### `.editorconfig` property to disable rules

In the previous release (0.47.x), the `.editorconfig`  property `disabled_rules` was deprecated and replaced with `ktlint_disabled_rules`. This latter property has now been deprecated as well in favour of a more flexible and better maintainable solution. Rule and rule sets can now be enabled/disabled with a separate property per rule (set). Please read [deprecation of (ktlint_)disable_rules property](https://pinterest.github.io/ktlint/faq/#why-is-editorconfig-property-disabled_rules-deprecated-and-how-do-i-resolve-this) for more information.

The KtLint CLI has not been changed. Although you can still use parameter `--experimental` to enable KtLint's Experimental rule set, you might want to set `.editorconfig` property `ktlint_experimental = enabled` instead.

### API Changes & RuleSet providers

If you are not an API consumer or Rule Set provider then you can skip this section.

#### Class relocations

Classes below have been relocated:

* Class `com.pinterest.ktlint.core.api.UsesEditorConfigProperties.EditorConfigProperty` has been replaced with `com.pinterest.ktlint.core.api.editorconfig.EditorConfigProperty`. 
* Class `com.pinterest.ktlint.core.KtLintParseException` has been replaced with `com.pinterest.ktlint.core.api.KtLintParseException`.
* Class `com.pinterest.ktlint.core.RuleExecutionException` has been replaced with `com.pinterest.ktlint.core.api.KtLintRuleException`.
* Class `com.pinterest.ktlint.reporter.format.internal.Color` has been moved to `com.pinterest.ktlint.reporter.format.Color`.
* Class `com.pinterest.ktlint.reporter.plain.internal.Color` has been moved to `com.pinterest.ktlint.reporter.plain.Color`.

#### Invoking `lint` and `format`

This is the last release that supports the `ExperimentalParams` to invoke the `lint` and `format` functions of KtLint. The `ExperimentalParams` contains a mix of configuration settings which are not dependent on the file/code which is to be processed. Other parameters in that class describe the code/file to be processed but can be configured inconsistently (for example a file with name "foo.kt" could be marked as a Kotlin Script file).

The static object `KtLint` is deprecated and replaced by class `KtLintRuleEngine` which is configured with `KtLintRuleEngineConfiguration`. The instance of the `KtLintRuleEngine` is intended to be reused for scanning all files in a project and should not be recreated per file.

Both `lint` and `format` are simplified and can now be called for a code block or for an entire file.

```kotlin
import java.io.File

// Define a reusable instance of the KtLint Rule Engine
val ktLintRuleEngine = KtLintRuleEngine(
  // Define configuration
)


// Process a collection of files
val files: Set<File> // Collect files in a convenient way
files.forEach(file in files) {
    ktLintRuleEngine.lint(file) {
        // Handle lint violations
    }
}

// or process a code sample for a given filepath
ktLintRuleEngine.lint(
  code = "code to be linted",
  filePath = Path("/path/to/source/file")
) {
  // Handle lint violations
}
```

#### Retrieve `.editorconfig`s

The list of `.editorconfig` files which will be accessed by KtLint when linting or formatting a given path can now be retrieved with the new API `KtLint.editorConfigFilePaths(path: Path): List<Path>`. 

This API can be called with either a file or a directory. It's intended usage is that it is called once with the root directory of a project before actually linting or formatting files of that project. When called with a directory path, all `.editorconfig` files in the directory or any of its subdirectories (except hidden directories) are returned. In case the given directory does not contain an `.editorconfig` file or if it does not contain the `root=true` setting, the parent directories are scanned as well until a root `.editorconfig` file is found.

Calling this API with a file path results in the `.editorconfig` files that will be accessed when processing that specific file. In case the directory in which the file resides does not contain an `.editorconfig` file or if it does not contain the `root=true` setting, the parent directories are scanned until a root `.editorconfig` file is found.

#### Psi filename replaces FILE_PATH_USER_DATA_KEY

Constant `KtLint.FILE_PATH_USER_DATA_KEY` is deprecated and will be removed in KtLint version 0.49.0. The file name will be passed correctly to the node with element type FILE and can be retrieved as follows:
```kotlin
if (node.isRoot()) {
    val fileName = (node.psi as? KtFile)?.name
    ...
}
```

### Added
* Wrap blocks in case the max line length is exceeded or in case the block contains a new line `wrapping` ([#1643](https://github.com/pinterest/ktlint/issues/1643))
* patterns can be read in from `stdin` with the `--patterns-from-stdin` command line options/flags ([#1606](https://github.com/pinterest/ktlint/pull/1606))
* Add basic formatting for context receiver in `indent` rule and new experimental rule `context-receiver-wrapping` ([#1672](https://github.com/pinterest/ktlint/issues/1672))
* Add naming rules for classes and objects (`class-naming`), functions (`function-naming`) and properties (`property-naming`) ([#44](https://github.com/pinterest/ktlint/issues/44))
* Add new built-in reporter `plain-summary` which prints a summary the number of violation which have been autocorrected or could not be autocorrected, both split by rule. 

### Fixed

* Let a rule process all nodes even in case the rule is suppressed for a node so that the rule can update the internal state ([#1644](https://github.com/pinterest/ktlint/issues/1644))
* Read `.editorconfig` when running CLI with options `--stdin` and `--editorconfig` ([#1651](https://github.com/pinterest/ktlint/issues/1651))
* Do not add a trailing comma in case a multiline function call argument is found but no newline between the arguments `trailing-comma-on-call-site` ([#1642](https://github.com/pinterest/ktlint/issues/1642))
* Add missing `ktlint_disabled_rules` to exposed `editorConfigProperties` ([#1671](https://github.com/pinterest/ktlint/issues/1671))
* Do not add a second trailing comma, if the original trailing comma is followed by a KDOC `trailing-comma-on-declaration-site` and `trailing-comma-on-call-site` ([#1676](https://github.com/pinterest/ktlint/issues/1676))
* A function signature preceded by an annotation array should be handled similar as function preceded by a singular annotation `function-signature` ([#1690](https://github.com/pinterest/ktlint/issues/1690))
* Fix offset of annotation violations
* Fix line offset when blank line found between class and primary constructor
* Remove needless blank line between class followed by EOL, and primary constructor
* Fix offset of unexpected linebreak before assignment
* Remove whitespace before redundant semicolon if the semicolon is followed by whitespace 

### Changed
* Update Kotlin development version to `1.8.0-RC` and Kotlin version to `1.7.21`.
* The default value for trailing commas on call site is changed to `true` unless the `android codestyle` is enabled. Note that KtLint from a consistency viewpoint *enforces* the trailing comma on call site while default IntelliJ IDEA formatting only *allows* the trailing comma but leaves it up to the developer's discretion. ([#1670](https://github.com/pinterest/ktlint/pull/1670))
* The default value for trailing commas on declaration site is changed to `true` unless the `android codestyle` is enabled. Note that KtLint from a consistency viewpoint *enforces* the trailing comma on declaration site while default IntelliJ IDEA formatting only *allows* the trailing comma but leaves it up to the developer's discretion. ([#1669](https://github.com/pinterest/ktlint/pull/1669))
* CLI options `--debug`, `--trace`, `--verbose` and `-v` are replaced with `--log-level=<level>` or the short version `-l=<level>, see [CLI log-level](https://pinterest.github.io/ktlint/install/cli/#logging). ([#1632](https://github.com/pinterest/ktlint/issues/1632))
* In CLI, disable logging entirely by setting `--log-level=none` or `-l=none` ([#1652](https://github.com/pinterest/ktlint/issues/1652))
* Rewrite `indent` rule. Solving problems in the old algorithm was very difficult. With the new algorithm this becomes a lot easier. Although the new implementation of the rule has been compared against several open source projects containing over 400,000 lines of code, it is still likely that new issues will be discovered. Please report your indentation issues so that these can be fixed as well. ([#1682](https://github.com/pinterest/ktlint/pull/1682), [#1321](https://github.com/pinterest/ktlint/issues/1321), [#1200](https://github.com/pinterest/ktlint/issues/1200), [#1562](https://github.com/pinterest/ktlint/issues/1562), [#1563](https://github.com/pinterest/ktlint/issues/1563), [#1639](https://github.com/pinterest/ktlint/issues/1639))
* Add methods "ASTNode.upsertWhitespaceBeforeMe" and "ASTNode.upsertWhitespaceAfterMe" as replacements for "LeafElement.upsertWhitespaceBeforeMe" and "LeafElement.upsertWhitespaceAfterMe". The new methods are more versatile and allow code to be written more readable in most places. ([#1687](https://github.com/pinterest/ktlint/pull/1687))
* Rewrite `indent` rule. Solving problems in the old algorithm was very difficult. With the new algorithm this becomes a lot easier. Although the new implementation of the rule has been compared against several open source projects containing over 400,000 lines of code, it is still likely that new issues will be discovered. Please report your indentation issues so that these can be fixed as well. ([#1682](https://github.com/pinterest/ktlint/pull/1682), [#1321](https://github.com/pinterest/ktlint/issues/1321), [#1200](https://github.com/pinterest/ktlint/issues/1200), [#1562](https://github.com/pinterest/ktlint/issues/1562), [#1563](https://github.com/pinterest/ktlint/issues/1563), [#1639](https://github.com/pinterest/ktlint/issues/1639), [#1688](https://github.com/pinterest/ktlint/issues/1688))
* Add support for running tests on `java 19`, remove support for running tests on `java 18`.
* Update `io.github.detekt.sarif4k:sarif4k` version to `0.2.0` ([#1701](https://github.com/pinterest/ktlint/issues/1701)).

## [0.47.1] - 2022-09-07

### Fixed
* Do not add trailing comma in empty parameter/argument list with comments (`trailing-comma-on-call-site`, `trailing-comma-on-declaration-site`) ([#1602](https://github.com/pinterest/ktlint/issues/1602))
* Fix class cast exception when specifying a non-string editorconfig setting in the default ".editorconfig" ([#1627](https://github.com/pinterest/ktlint/issues/1627))
* Fix indentation before semi-colon when it is pushed down after inserting a trailing comma  ([#1609](https://github.com/pinterest/ktlint/issues/1609))
* Do not show deprecation warning about property "disabled_rules" when using CLi-parameter `--disabled-rules` ([#1599](https://github.com/pinterest/ktlint/issues/1599))
* Traversing directory hierarchy at Windows ([#1600](https://github.com/pinterest/ktlint/issues/1600))
* Ant-style path pattern support ([#1601](https://github.com/pinterest/ktlint/issues/1601))
* Apply `@file:Suppress` on all toplevel declarations ([#1623](https://github.com/pinterest/ktlint/issues/1623)) 

### Changed
* Display warning instead of error when no files are matched, and return with exit code 0. ([#1624](https://github.com/pinterest/ktlint/issues/1624))

## [0.47.0] - 2022-08-19

### API Changes & RuleSet providers

If you are not an API consumer nor a RuleSet provider, then you can safely skip this section. Otherwise, please read below carefully and upgrade your usage of ktlint. In this and coming releases, we are changing and adapting important parts of our API in order to increase maintainability and flexibility for future changes. Please avoid skipping a releases as that will make it harder to migrate.

#### Rule lifecycle hooks / deprecate RunOnRootOnly visitor modifier

Up until ktlint 0.46 the Rule class provided only one life cycle hook. This "visit" hook was called in a depth-first-approach on all nodes in the file. A rule like the IndentationRule used the RunOnRootOnly visitor modifier to call this lifecycle hook for the root node only in combination with an alternative way of traversing the ASTNodes. Downside of this approach was that suppression of the rule on blocks inside a file was not possible ([#631](https://github.com/pinterest/ktlint/issues/631)). More generically, this applied to all rules, applying alternative traversals of the AST.

The Rule class now offers new life cycle hooks:
* beforeFirstNode: This method is called once before the first node is visited. It can be used to initialize the state of the rule before processing of nodes starts. The ".editorconfig" properties (including overrides) are provided as parameter.
* beforeVisitChildNodes: This method is called on a node in AST before visiting its child nodes. This is repeated recursively for the child nodes resulting in a depth first traversal of the AST. This method is the equivalent of the "visit" life cycle hooks. However, note that in KtLint 0.48, the UserData of the rootnode no longer provides access to the ".editorconfig" properties. This method can be used to emit Lint Violations and to autocorrect if applicable.
* afterVisitChildNodes: This method is called on a node in AST after all its child nodes have been visited. This method can be used to emit Lint Violations and to autocorrect if applicable.
* afterLastNode: This method is called once after the last node in the AST is visited. It can be used for teardown of the state of the rule.

Optionally, a rule can stop the traversal of the remainder of the AST whenever the goal of the rule has been achieved. See KDoc on Rule class for more information.

The "visit" life cycle hook will be removed in Ktlint 0.48. In KtLint 0.47 the "visit" life cycle hook will be called *only* when hook "beforeVisitChildNodes" is not overridden. It is recommended to migrate to the new lifecycle hooks in KtLint 0.47. Please create an issue, in case you need additional assistance to implement the new life cycle hooks in your rules.


#### Ruleset providing by Custom Rule Set Provider

The KtLint engine needs a more fine-grained control on the instantiation of new Rule instances. Currently, a new instance of a rule can be created only once per file. However, when formatting files the same rule instance is reused for a second processing iteration in case a Lint violation has been autocorrected. By re-using the same rule instance, state of that rule might leak from the first to the second processing iteration.

Providers of custom rule sets have to migrate the custom rule set JAR file. The current RuleSetProvider interface which is implemented in the custom rule set is deprecated and marked for removal in KtLint 0.48. Custom rule sets using the old RuleSetProvider interface will not be run in KtLint 0.48 or above.

For now, it is advised to implement the new RuleSetProviderV2 interface without removing the old RuleSetProvider interface. In this way, KtLint 0.47 and above use the RuleSetProviderV2 interface and ignore the old RuleSetProvider interface completely. KtLint 0.46 and below only use the old RuleSetProvider interface.

Adding the new interface is straight forward, as can be seen below:

```
// Current implementation
public class CustomRuleSetProvider : RuleSetProvider {
    override fun get(): RuleSet = RuleSet(
        "custom",
        CustomRule1(),
        CustomRule2(),
    )
}

// New implementation
public class CustomRuleSetProvider :
    RuleSetProviderV2(CUSTOM_RULE_SET_ID),
    RuleSetProvider {
    override fun get(): RuleSet =
        RuleSet(
            CUSTOM_RULE_SET_ID,
            CustomRule1(),
            CustomRule2()
        )

    override fun getRuleProviders(): Set<RuleProvider> =
        setOf(
            RuleProvider { CustomRule1() },
            RuleProvider { CustomRule2() }
        )

    private companion object {
        const val CUSTOM_RULE_SET_ID = custom"
    }
}

```

Also note that file 'resource/META-INF/services/com.pinterest.ktlint.core.RuleSetProviderV2' needs to be added. In case your custom rule set provider implements both RuleSetProvider and RuleSetProviderV2, the resource directory contains files for both implementation. The content of those files is identical as the interfaces are implemented on the same class.

Once above has been implemented, rules no longer have to clean up their internal state as the KtLint rule engine can request a new instance of the Rule at any time it suspects that the internal state of the Rule is tampered with (e.g. as soon as the Rule instance is used for traversing the AST).

#### Rule set providing by API Consumer

The KtLint engine needs a more fine-grained control on the instantiation of new Rule instances. Currently, a new instance of a rule can be created only once per file. However, when formatting files the same rule instance is reused for a second processing iteration in case a Lint violation has been autocorrected. By re-using the same rule instance, state of that rule might leak from the first to the second processing iteration.

The ExperimentalParams parameter which is used to invoke "KtLint.lint" and "KtLint.format" contains a new parameter "ruleProviders" which will replace the "ruleSets" parameter in KtLint 0.48. Exactly one of those parameters should be a non-empty set. It is preferred that API consumers migrate to using "ruleProviders".

```
// Old style using "ruleSets"
KtLint.format(
    KtLint.ExperimentalParams(
        ...
        ruleSets = listOf(
            RuleSet(
                "custom",
                CustomRule1(),
                CustomRule2()
            )
        ),
        ...
    )
)

// New style using "ruleProviders"
KtLint.format(
    KtLint.ExperimentalParams(
        ...
        ruleProviders = setOf(
            RuleProvider { CustomRule1() },
            RuleProvider { CustomRule2() }
        ),
        cb = { _, _ -> }
    )
)
```

Once above has been implemented, rules no longer have to clean up their internal state as the KtLint rule engine can request a new instance of the Rule at any time it suspects that the internal state of the Rule is tampered with (e.g. as soon as the Rule instance is used for traversing the AST).

#### Format callback

The callback function provided as parameter to the format function is now called for all errors regardless whether the error has been autocorrected. Existing consumers of the format function should now explicitly check the `autocorrected` flag in the callback result and handle it appropriately (in most case this will be ignoring the callback results for which `autocorrected` has value `true`).

#### CurrentBaseline

Class `com.pinterest.ktlint.core.internal.CurrentBaseline` has been replaced with `com.pinterest.ktlint.core.api.Baseline`.

Noteworthy changes:
* Field `baselineRules` (nullable) is replaced with `lintErrorsPerFile (non-nullable).
* Field `baselineGenerationNeeded` (boolean) is replaced with `status` (type `Baseline.Status`).

The utility functions provided via `com.pinterest.ktlint.core.internal.CurrentBaseline` are moved to the new class. One new method `List<LintError>.doesNotContain(lintError: LintError)` is added.

#### .editorconfig property "disabled_rules"

The `.editorconfig` property `disabled_rules` (api property `DefaultEditorConfigProperties.disabledRulesProperty`) has been deprecated and will be removed in a future version. Use `ktlint_disabled_rules` (api property `DefaultEditorConfigProperties.ktlintDisabledRulesProperty`) instead as it more clearly identifies that ktlint is the owner of the property. This property is to be renamed in `.editorconfig` files and `ExperimentalParams.editorConfigOverride`.   

Although, Ktlint 0.47.0 falls back on property `disabled_rules` whenever `ktlint_disabled_rules` is not found, this result in a warning message being printed. 

#### Default/alternative .editorconfig

Parameter "ExperimentalParams.editorConfigPath" is deprecated in favor of the new parameter "ExperimentalParams.editorConfigDefaults". When used in the old implementation this resulted in ignoring all ".editorconfig" files on the path to the file. The new implementation uses properties from the "editorConfigDefaults"parameter only when no ".editorconfig" files on the path to the file supplies this property for the filepath.

API consumers can easily create the EditConfigDefaults by calling
"EditConfigDefaults.load(path)" or creating it programmatically.

#### Reload of `.editorconfig` file

Some API Consumers keep a long-running instance of the KtLint engine alive. In case an `.editorconfig` file is changed, which was already loaded into the internal cache of the KtLint engine this change would not be taken into account by KtLint. One way to deal with this, was to clear the entire KtLint cache after each change in an `.editorconfig` file.

Now, the API consumer can reload an `.editorconfig`. If the `.editorconfig` with given path is actually found in the cached, it will be replaced with the new value directly. If the file is not yet loaded in the cache, loading will be deferred until the file is actually requested again.

Example:
```kotlin
KtLint.reloadEditorConfigFile("/some/path/to/.editorconfig")
```

#### Miscellaneous

Several methods for which it is unlikely that they are used by API consumers have been marked for removal from the public API in KtLint 0.48.0. Please create an issue in case you have a valid business case to keep such methods in the public API.

### Added

* Add `format` reporter. This reporter prints a one-line-summary of the formatting status per file. ([#621](https://github.com/pinterest/ktlint/issues/621)).

### Fixed

* Fix cli argument "--disabled_rules" ([#1520](https://github.com/pinterest/ktlint/issues/1520)).
* A file which contains a single top level declaration of type function does not need to be named after the function but only needs to adhere to the PascalCase convention. `filename` ([#1521](https://github.com/pinterest/ktlint/issues/1521)).
* Disable/enable IndentationRule on blocks in middle of file. (`indent`) [#631](https://github.com/pinterest/ktlint/issues/631)
* Allow usage of letters with diacritics in enum values and filenames (`enum-entry-name-case`, `filename`) ([#1530](https://github.com/pinterest/ktlint/issues/1530)).
* Fix resolving of Java version when JAVA_TOOL_OPTIONS is set ([#1543](https://github.com/pinterest/ktlint/issues/1543))
* When a glob is specified then ensure that it matches files in the current directory and not only in subdirectories of the current directory ([#1533](https://github.com/pinterest/ktlint/issues/1533)).
* Execute `ktlint` cli on default kotlin extensions only when an (existing) path to a directory is given. ([#917](https://github.com/pinterest/ktlint/issues/917)).
* Invoke callback on `format` function for all errors including errors that are autocorrected ([#1491](https://github.com/pinterest/ktlint/issues/1491))
* Merge first line of body expression with function signature only when it fits on the same line `function-signature` ([#1527](https://github.com/pinterest/ktlint/issues/1527))
* Add missing whitespace when else is on same line as true condition `multiline-if-else` ([#1560](https://github.com/pinterest/ktlint/issues/1560))
* Fix multiline if-statements `multiline-if-else` ([#828](https://github.com/pinterest/ktlint/issues/828))
* Prevent class cast exception on ".editorconfig" property `ktlint_code_style`  ([#1559](https://github.com/pinterest/ktlint/issues/1559))
* Handle trailing comma in enums `trailing-comma` ([#1542](https://github.com/pinterest/ktlint/pull/1542))
* Allow EOL comment after annotation ([#1539](https://github.com/pinterest/ktlint/issues/1539))
* Split rule `trailing-comma` into `trailing-comma-on-call-site` and `trailing-comma-on-declaration-site` ([#1555](https://github.com/pinterest/ktlint/pull/1555))
* Support globs containing directories in the ".editorconfig" supplied via CLI "--editorconfig"  ([#1551](https://github.com/pinterest/ktlint/pull/1551))
* Fix indent of when entry with a dot qualified expression instead of simple value when trailing comma is required ([#1519](https://github.com/pinterest/ktlint/pull/1519))
* Fix whitespace between trailing comma and arrow in when entry when trailing comma is required ([#1519](https://github.com/pinterest/ktlint/pull/1519))
* Prevent false positive in parameter list for which the last value parameter is a destructuring declaration followed by a trailing comma `wrapping` ([#1578](https://github.com/pinterest/ktlint/issues/1578))

### Changed

* Print an error message and return with non-zero exit code when no files are found that match with the globs ([#629](https://github.com/pinterest/ktlint/issues/629)).
* Invoke callback on `format` function for all errors including errors that are autocorrected ([#1491](https://github.com/pinterest/ktlint/issues/1491))
* Improve rule `annotation` ([#1574](https://github.com/pinterest/ktlint/pull/1574))
* Rename `.editorconfig` property `disabled_rules` to `ktlint_disabled_rules` ([#701](https://github.com/pinterest/ktlint/issues/701))
* Allow file and directory paths in CLI-parameter "--editorconfig" ([#1580](https://github.com/pinterest/ktlint/pull/1580))
* Update Kotlin development version to `1.7.20-beta` and Kotlin version to `1.7.10`.
* Update release scripting to set version number in mkdocs documentation ([#1575](https://github.com/pinterest/ktlint/issues/1575)).
* Update Gradle to `7.5.1` version

### Removed
* Remove support to generate IntelliJ IDEA configuration files as this no longer fits the scope of the ktlint project ([#701](https://github.com/pinterest/ktlint/issues/701))

## [0.46.1] - 2022-06-21

Minor release to address some regressions introduced in 0.46.0

### Fixed

* Remove experimental flag `-Xuse-k2` as it forces API Consumers to compile their projects with this same flag ([#1506](https://github.com/pinterest/ktlint/pull/1506)).
* Account for separating spaces when parsing the disabled rules ([#1508](https://github.com/pinterest/ktlint/pull/1508)). 
* Do not remove space before a comment in a parameter list ([#1509](https://github.com/pinterest/ktlint/issues/1509)). 
* A delegate property which starts on the same line as the property declaration should not have an extra indentation `indent` ([#1510](https://github.com/pinterest/ktlint/pull/1510))

## [0.46.0] - 2022-06-18

### Promoting experimental rules to standard 

The rules below are promoted from the `experimental` ruleset to the `standard` ruleset.
* `annotation`
* `annotation-spacing`
* `argument-list-wrapping`
* `double-colon-spacing`
* `enum-entry-name-case`
* `multiline-if-else`
* `no-empty-first-line-in-method-block`
* `package-name`
* `trailing-comma`
* `spacing-around-angle-brackets`
* `spacing-between-declarations-with-annotations`
* `spacing-between-declarations-with-comments`
* `unary-op-spacing`

Note that as a result of moving the rules that the prefix `experimental:` has to be removed from all references to this rule. Check references in:
* The `.editorconfig` setting `disabled_rules`.
* KtLint disable and enable directives.
* The `VisitorModifier.RunAfterRule`.

If your project did not run with the `experimental` ruleset enabled before, you might expect new lint violations to be reported. Please note that rules can be disabled via the the `.editorconfig` in case you do not want the rules to be applied on your project.

### API Changes & RuleSet providers

If you are not an API user nor a RuleSet provider, then you can safely skip this section. Otherwise, please read below carefully and upgrade your usage of ktlint. In this and coming releases, we are changing and adapting important parts of our API in order to increase maintainability and flexibility for future changes. Please avoid skipping a releases as that will make it harder to migrate.

#### Lint and formatting functions

The lint and formatting changes no longer accept parameters of type `Params` but only `ExperimentalParams`. Also, the VisitorProvider parameter has been removed. Because of this, your integration with KtLint breaks. Based on feedback with ktlint 0.45.x, we now prefer to break at compile time instead of trying to keep the interface backwards compatible. Please raise an issue, in case you help to convert to the new API.

#### Use of ".editorconfig" properties & userData

The interface `UsesEditorConfigProperties` provides method `getEditorConfigValue` to retrieve a named `.editorconfig` property for a given ASTNode. When implementing this interface, the value `editorConfigProperties` needs to be overridden. Previously it was not checked whether a retrieved property was actually recorded in this list. Now, retrieval of unregistered properties results in an exception.

Property `Ktlint.DISABLED` has been removed. The property value can now be retrieved as follows:
```kotlin
astNode
    .getEditorConfigValue(DefaultEditorConfigProperties.disabledRulesProperty)
    .split(",")
```
and be supplied via the `ExperimentalParams` as follows:
```kotlin
ExperimentalParams(
    ...
    editorConfigOverride =  EditorConfigOverride.from(
      DefaultEditorConfigProperties.disabledRulesProperty to "some-rule-id,experimental:some-other-rule-id"
    )
    ...
)
```

Property `Ktlint.ANDROID_USER_DATA_KEY` has been removed. The property value can now be retrieved as follows:
```kotlin
astNode
    .getEditorConfigValue(DefaultEditorConfigProperties.codeStyleProperty)
```
and be supplied via the `ExperimentalParams` as follows:
```kotlin
ExperimentalParams(
    ...
    editorConfigOverride =  EditorConfigOverride.from(
      DefaultEditorConfigProperties.codeStyleProperty to "android" 
    )
    ...
)
```
This property defaults to the `official` Kotlin code style when not set.

#### Testing KtLint rules

An AssertJ style API for testing KtLint rules ([#1444](https://github.com/pinterest/ktlint/issues/1444)) has been added. Usage of this API is encouraged in favor of using the old RuleExtension API. For more information, see [KtLintAssertThat API]( https://github.com/pinterest/ktlint/blob/master/ktlint-test/README.MD)

### Added
- Add experimental rule for unexpected spacing between function name and opening parenthesis (`spacing-between-function-name-and-opening-parenthesis`) ([#1341](https://github.com/pinterest/ktlint/issues/1341))
- Add experimental rule for unexpected spacing in the parameter list (`parameter-list-spacing`) ([#1341](https://github.com/pinterest/ktlint/issues/1341))
- Add experimental rule for incorrect spacing around the function return type (`function-return-type-spacing`) ([#1341](https://github.com/pinterest/ktlint/pull/1341))
- Add experimental rule for unexpected spaces in a nullable type (`nullable-type-spacing`) ([#1341](https://github.com/pinterest/ktlint/issues/1341))
- Do not add a space after the typealias name (`type-parameter-list-spacing`) ([#1435](https://github.com/pinterest/ktlint/issues/1435))
- Add experimental rule for consistent spacing before the start of the function body (`function-start-of-body-spacing`) ([#1341](https://github.com/pinterest/ktlint/issues/1341))
- Suppress ktlint rules using `@Suppress` ([more information](https://github.com/pinterest/ktlint#disabling-for-a-statement-using-suppress)) ([#765](https://github.com/pinterest/ktlint/issues/765))
- Add experimental rule for rewriting the function signature (`function-signature`) ([#1341](https://github.com/pinterest/ktlint/issues/1341))

### Fixed
- Move disallowing blank lines in chained method calls from `no-consecutive-blank-lines` to new rule (`no-blank-lines-in-chained-method-calls`) ([#1248](https://github.com/pinterest/ktlint/issues/1248))
- Fix check of spacing in the receiver type of an anonymous function ([#1440](https://github.com/pinterest/ktlint/issues/1440))
- Allow comment on same line as super class in class declaration `wrapping` ([#1457](https://github.com/pinterest/ktlint/pull/1457))
- Respect git hooksPath setting ([#1465](https://github.com/pinterest/ktlint/issues/1465))
- Fix formatting of a property delegate with a dot-qualified-expression `indent` ([#1340](https://github.com/pinterest/ktlint/ssues/1340))
- Keep formatting of for-loop in sync with default IntelliJ formatter (`indent`) and a newline in the expression in a for-statement should not force to wrap it `wrapping` ([#1350](https://github.com/pinterest/ktlint/issues/1350))
- Fix indentation of property getter/setter when the property has an initializer on a separate line `indent` ([#1335](https://github.com/pinterest/ktlint/issues/1335))
- When `.editorconfig` setting `indentSize` is set to value `tab` then return the default tab width as value for `indentSize` ([#1485](https://github.com/pinterest/ktlint/issues/1485))
- Allow suppressing all rules or a list of specific rules in the entire file with `@file:Suppress(...)`  ([#1029](https://github.com/pinterest/ktlint/issues/1029))


### Changed
- Update Kotlin development version to `1.7.0` and Kotlin version to `1.7.0`.
- Update shadow plugin to `7.1.2` release
- Update picocli to `4.6.3` release
- A file containing only one (non private) top level declaration (class, interface, object, type alias or function) must be named after that declaration. The name also must comply with the Pascal Case convention. The same applies to a file containing one single top level class declaration and one or more extension functions for that class. `filename` ([#1004](https://github.com/pinterest/ktlint/pull/1117))
- Promote experimental rules to standard rules set: `annotation`, `annotation-spacing`, `argument-list-wrapping`, `double-colon-spacing`, `enum-entry-name-case`, `multiline-if-else`, `no-empty-first-line-in-method-block`, `package-name`, `traling-comma`, `spacing-around-angle-brackets`, `spacing-between-declarations-with-annotations`, `spacing-between-declarations-with-comments`, `unary-op-spacing` ([#1481](https://github.com/pinterest/ktlint/pull/1481))
- The CLI parameter `--android` can be omitted when the `.editorconfig` property `ktlint_code_style = android` is defined

## [0.45.2] - 2022-04-06

### Fixed
- Resolve compatibility issues introduced in 0.45.0 and 0.45.1 ([#1434](https://github.com/pinterest/ktlint/issues/1434)). Thanks to [mateuszkwiecinski](https://github.com/mateuszkwiecinski) and [jeremymailen](https://github.com/jeremymailen) for your input on this issue.

### Changed
* Set Kotlin development version to `1.6.20` and Kotlin version to `1.6.20`.

## [0.45.1] - 2022-03-21

Minor release to fix a breaking issue with `ktlint` API consumers

### Fixed
- Remove logback dependency from ktlint-core module ([#1421](https://github.com/pinterest/ktlint/issues/1421))

## [0.45.0] - 2022-03-18

### API Changes & RuleSet providers

If you are not an API user nor a RuleSet provider, then you can safely skip this section. Otherwise, please read below carefully and upgrade your usage of ktlint. In this and coming releases, we are changing and adapting important parts of our API in order to increase maintainability and flexibility for future changes. Please avoid skipping a releases as that will make it harder to migrate.

#### Retrieving ".editorconfig" property value

This section is applicable when providing rules that depend on one or more values of ".editorconfig" properties. Property values should no longer be retrieved via *EditConfig* or directly via `userData[EDITOR_CONFIG_USER_DATA_KEY]`. Property values should now only be retrieved using method `ASTNode.getEditorConfigValue(editorConfigProperty)` of interface `UsesEditorConfigProperties` which is provided in this release. Starting from next release after the current release, the *EditConfig* and/or `userData[EDITOR_CONFIG_USER_DATA_KEY]` may be removed without further notice which will break your API or rule. To prevent disruption of your end user, you should migrate a.s.a.p.

### Added
- Add experimental rule for unexpected spaces in a type reference before a function identifier (`function-type-reference-spacing`) ([#1341](https://github.com/pinterest/ktlint/issues/1341))
- Add experimental rule for incorrect spacing after a type parameter list (`type-parameter-list-spacing`) ([#1366](https://github.com/pinterest/ktlint/pull/1366))
- Add experimental rule to detect discouraged comment locations (`discouraged-comment-location`) ([#1365](https://github.com/pinterest/ktlint/pull/1365))
- Add rule to check spacing after fun keyword (`fun-keyword-spacing`) ([#1362](https://github.com/pinterest/ktlint/pull/1362))
- Add experimental rules for unnecessary spacing between modifiers in and after the last modifier in a modifier list ([#1361](https://github.com/pinterest/ktlint/pull/1361))
- New experimental rule for aligning the initial stars in a block comment when present (`experimental:block-comment-initial-star-alignment` ([#297](https://github.com/pinterest/ktlint/issues/297))
- Respect `.editorconfig` property `ij_kotlin_packages_to_use_import_on_demand` (`no-wildcard-imports`) ([#1272](https://github.com/pinterest/ktlint/pull/1272))
- Add new experimental rules for wrapping of block comment (`comment-wrapping`) ([#1403](https://github.com/pinterest/ktlint/pull/1403))
- Add new experimental rules for wrapping of KDoc comment (`kdoc-wrapping`) ([#1403](https://github.com/pinterest/ktlint/pull/1403))
- Add experimental rule for incorrect spacing after a type parameter list (`type-parameter-list-spacing`) ([#1366](https://github.com/pinterest/ktlint/pull/1366))
- Expand check task to run tests on JDK 17 - "testOnJdk17"

### Fixed
- Fix lint message to "Unnecessary long whitespace" (`no-multi-spaces`) ([#1394](https://github.com/pinterest/ktlint/issues/1394))
- Do not remove trailing comma after a parameter of type array in an annotation (experimental:trailing-comma) ([#1379](https://github.com/pinterest/ktlint/issues/1379))
- Do not delete blank lines in KDoc (no-trailing-spaces) ([#1376](https://github.com/pinterest/ktlint/issues/1376))
- Do not indent raw string literals that are not followed by either trimIndent() or trimMargin() (`indent`) ([#1375](https://github.com/pinterest/ktlint/issues/1375))
- Revert remove unnecessary wildcard imports as introduced in Ktlint 0.43.0 (`no-unused-imports`) ([#1277](https://github.com/pinterest/ktlint/issues/1277)), ([#1393](https://github.com/pinterest/ktlint/issues/1393)), ([#1256](https://github.com/pinterest/ktlint/issues/1256))
- (Possibly) resolve memory leak ([#1216](https://github.com/pinterest/ktlint/issues/1216))
- Initialize loglevel in Main class after parsing the CLI parameters ([#1412](https://github.com/pinterest/ktlint/issues/1412))

### Changed
- Print the rule id always in the PlainReporter ([#1121](https://github.com/pinterest/ktlint/issues/1121))
- All wrapping logic is moved from the `indent` rule to the new rule `wrapping` (as part of the `standard` ruleset). In case you currently have disabled the `indent` rule, you may want to reconsider whether this is still necessary or that you also want to disable the new `wrapping` rule to keep the status quo. Both rules can be run independent of each other. ([#835](https://github.com/pinterest/ktlint/issues/835))

## [0.44.0] - 2022-02-15

Please welcome [paul-dingemans](https://github.com/paul-dingemans) as an official maintainer of ktlint!

### Added
- Use Gradle JVM toolchain with language version 8 to compile the project
- Basic tests for CLI ([#540](https://github.com/pinterest/ktlint/issues/540))
- Add experimental rule for unnecessary parentheses in function call followed by lambda ([#1068](https://github.com/pinterest/ktlint/issues/1068))

### Fixed
- Fix indentation of function literal ([#1247](https://github.com/pinterest/ktlint/issues/1247))
- Fix false positive in rule spacing-between-declarations-with-annotations ([#1281](https://github.com/pinterest/ktlint/issues/1281))
- Do not remove imports for same class when different alias is used ([#1243](https://github.com/pinterest/ktlint/issues/1243))
- Fix NoSuchElementException for property accessor (`trailing-comma`) ([#1280](https://github.com/pinterest/ktlint/issues/1280))
- Fix ClassCastException using ktlintFormat on class with KDoc (`no-trailing-spaces`) ([#1270](https://github.com/pinterest/ktlint/issues/1270))
- Do not remove trailing comma in annotation ([#1297](https://github.com/pinterest/ktlint/issues/1297))
- Do not remove import which is used as markdown link in KDoc only (`no-unused-imports`) ([#1282](https://github.com/pinterest/ktlint/issues/1282))
- Fix indentation of secondary constructor (`indent`) ([#1222](https://github.com/pinterest/ktlint/issues/1222))
- Custom gradle tasks with custom ruleset results in warning ([#1269](https://github.com/pinterest/ktlint/issues/1269))
- Fix alignment of arrow when trailing comma is missing in when entry (`trailing-comma`) ([#1312](https://github.com/pinterest/ktlint/issues/1312))
- Fix indent of delegated super type entry (`indent`) ([#1210](https://github.com/pinterest/ktlint/issues/1210))
- Improve indentation of closing quotes of a multiline raw string literal (`indent`) ([#1262](https://github.com/pinterest/ktlint/pull/1262))
- Trailing space should not lead to delete of indent of next line (`no-trailing-spaces`) ([#1334](https://github.com/pinterest/ktlint/pull/1334))
- Force a single line function type inside a nullable type to a separate line when the max line length is exceeded (`parameter-list-wrapping`) ([#1255](https://github.com/pinterest/ktlint/issues/1255))
- A single line function with a parameter having a lambda as default argument does not throw error (`indent`) ([#1330](https://github.com/pinterest/ktlint/issues/1330))
- Fix executable jar on Java 16+ ([#1195](https://github.com/pinterest/ktlint/issues/1195)) 
- Fix false positive unused import after autocorrecting a trailing comma ([#1367](https://github.com/pinterest/ktlint/issues/1367)) 
- Fix false positive indentation (`parameter-list-wrapping`, `argument-list-wrapping`) ([#897](https://github.com/pinterest/ktlint/issues/897), [#1045](https://github.com/pinterest/ktlint/issues/1045), [#1119](https://github.com/pinterest/ktlint/issues/1119), [#1255](https://github.com/pinterest/ktlint/issues/1255), [#1267](https://github.com/pinterest/ktlint/issues/1267), [#1319](https://github.com/pinterest/ktlint/issues/1319), [#1320](https://github.com/pinterest/ktlint/issues/1320), [#1337](https://github.com/pinterest/ktlint/issues/1337)
- Force a single line function type inside a nullable type to a separate line when the max line length is exceeded (`parameter-list-wrapping`) ([#1255](https://github.com/pinterest/ktlint/issues/1255))

### Changed
- Update Kotlin version to `1.6.0` release
- Add separate tasks to run tests on JDK 11 - "testOnJdk11"
- Update Dokka to `1.6.0` release
- Apply ktlint experimental rules on the ktlint code base itself.
- Update shadow plugin to `7.1.1` release
- Add Kotlin-logging backed by logback as logging framework ([#589](https://github.com/pinterest/ktlint/issues/589))
- Update Gradle to `7.4` version

## [0.43.2] - 2021-12-01

### Fixed
- KtLint CLI 0.43 doesn't work with JDK 1.8 ([#1271](https://github.com/pinterest/ktlint/issues/1271))

## [0.43.0] - 2021-11-02

### Added
- New `trailing-comma` rule ([#709](https://github.com/pinterest/ktlint/issues/709)) (prior art by [paul-dingemans](https://github.com/paul-dingemans))
### Fixed
- Fix false positive with lambda argument and call chain (`indent`) ([#1202](https://github.com/pinterest/ktlint/issues/1202))
- Fix trailing spaces not formatted inside block comments (`no-trailing-spaces`) ([#1197](https://github.com/pinterest/ktlint/issues/1197))
- Do not check for `.idea` folder presence when using `applyToIDEA` globally ([#1186](https://github.com/pinterest/ktlint/issues/1186))
- Remove spaces before primary constructor (`paren-spacing`) ([#1207](https://github.com/pinterest/ktlint/issues/1207))
- Fix false positive for delegated properties with a lambda argument (`indent`) ([#1210](https://github.com/pinterest/ktlint/issues/1210))
- (REVERTED in Ktlint 0.45.0) Remove unnecessary wildcard imports (`no-unused-imports`) ([#1256](https://github.com/pinterest/ktlint/issues/1256))
- Fix indentation of KDoc comment when using tab indentation style (`indent`) ([#850](https://github.com/pinterest/ktlint/issues/850))
### Changed
- Support absolute paths for globs ([#1131](https://github.com/pinterest/ktlint/issues/1131))
- Fix regression from 0.41 with argument list wrapping after dot qualified expression (`argument-list-wrapping`)([#1159](https://github.com/pinterest/ktlint/issues/1159))
- Update Gradle to `7.2` version
- Update Gradle shadow plugin to `7.1` version
- Update Kotlin version to `1.5.31` version. Default Kotlin API version was changed to `1.4`!

## [0.42.1] - 2021-08-06

Dot release to fix regressions in `indent` rule introduced in 0.42.0 release. Thanks to [t-kameyama](https://github.com/t-kameyama) for the fixes!

### Fixed
- Fix false positive with delegated properties (`indent`) ([#1189](https://github.com/pinterest/ktlint/issues/1189))
- Fix false positive with lambda argument in super type entry (`indent`) ([#1188](https://github.com/pinterest/ktlint/issues/1188))

## [0.42.0] - 2021-07-29

Thank you to the following contributors for this release:
- [abbenyyyyyy](https://github.com/abbenyyyyyy)
- [carloscsanz](https://github.com/carloscsanz)
- [chao2zhang](https://github.com/chao2zhang)
- [ganadist](https://github.com/ganadist)
- [insiderser](https://github.com/insiderser)
- [paul-dingemans](https://github.com/paul-dingemans)
- [rciovati](https://github.com/rciovati)
- [t-kameyama](https://github.com/t-kameyama)

### Added
- SARIF output support ([#1102](https://github.com/pinterest/ktlint/issues/1102))

### Fixed
- Remove needless blank lines in dot qualified expression ([#1077](https://github.com/pinterest/ktlint/issues/1077))
- Fix false positives for SpacingBetweenDeclarationsWithAnnotationsRule ([#1125](https://github.com/pinterest/ktlint/issues/1125))
- Fix false positive with eol comment (`annotation-spacing`) ([#1124](https://github.com/pinterest/ktlint/issues/1124))
- Fix KtLint dependency variant selection ([#1114](https://github.com/pinterest/ktlint/issues/1114))
- Fix false positive with 'by lazy {}' (`indent`) ([#1162](https://github.com/pinterest/ktlint/issues/1162))
- Fix false positive with value argument list has lambda (`indent`) ([#764](https://github.com/pinterest/ktlint/issues/764))
- Fix false positive in lambda in dot qualified expression (`argument-list-wrapping`) ([#1112](https://github.com/pinterest/ktlint/issues/1112))
- Fix false positive with multiline expression with elvis operator in assignment (`indent`) ([#1165](https://github.com/pinterest/ktlint/issues/1165))
- Ignore backticks in imports for ordering purposes (`import-ordering`) ([#1106](https://github.com/pinterest/ktlint/issues/1106))
- Fix false positive with elvis operator and comment (`chain-wrapping`) ([#1055](https://github.com/pinterest/ktlint/issues/1055))
- Fix false negative in when conditions (`chain-wrapping`) ([#1130](https://github.com/pinterest/ktlint/issues/1130))
- Fix the Html reporter Chinese garbled ([#1140](https://github.com/pinterest/ktlint/issues/1140))
- Performance regression introduced in 0.41.0 ([#1135](https://github.com/pinterest/ktlint/issues/1135))

### Changed
- Updated to dokka 1.4.32 ([#1148](https://github.com/pinterest/ktlint/pull/1148))
- Updated Kotlin to 1.5.20 version

## [0.41.0] - 2021-03-16

**Note:** This release contains breaking changes to globs passed to ktlint via the command line. See ([#999](https://github.com/pinterest/ktlint/issues/999)) and the [README](https://github.com/pinterest/ktlint/blob/master/README.md#command-line-usage).

Thank you to [t-kameyama](https://github.com/t-kameyama) and [paul-dingemans](https://github.com/paul-dingemans) for your contributions to this release!

### Added
- New `ktlint_ignore_back_ticked_identifier` EditorConfig option for `max-line-length` rule to ignore long method names inside backticks
  (primarily used in tests) ([#1007](https://github.com/pinterest/ktlint/issues/1007))
- Allow to add/replace loaded `.editorconfig` values via `ExperimentalParams#editorConfigOverride` ([#1016](https://github.com/pinterest/ktlint/issues/1003))
- `ReporterProvider`, `LintError`, `RuleSetProvider` now implement `Serializable` interface

### Fixed
- Incorrect indentation with multiple interfaces ([#1003](https://github.com/pinterest/ktlint/issues/1003))
- Empty line before primary constructor is not reported and formatted-out ([#1004](https://github.com/pinterest/ktlint/issues/1004))
- Fix '.editorconfig' generation for "import-ordering" rule ([#1011](https://github.com/pinterest/ktlint/issues/1004))
- Fix "filename" rule will not work when '.editorconfig' file is not found ([#997](https://github.com/pinterest/ktlint/issues/1004))
- EditorConfig generation for `import-ordering` ([#1011](https://github.com/pinterest/ktlint/pull/1011))
- Internal error (`no-unused-imports`) ([#996](https://github.com/pinterest/ktlint/issues/996))
- Fix false positive when argument list is after multiline dot-qualified expression (`argument-list-wrapping`) ([#893](https://github.com/pinterest/ktlint/issues/893))
- Fix indentation for function types after a newline (`indent`) ([#918](https://github.com/pinterest/ktlint/issues/918))
- Don't remove the equals sign for a default argument (`no-line-break-before-assignment`) ([#1039](https://github.com/pinterest/ktlint/issues/1039))
- Fix internal error in `no-unused-imports` ([#1040](https://github.com/pinterest/ktlint/issues/1040))
- Fix false positives when declaration has tail comments (`spacing-between-declarations-with-comments`) ([#1053](https://github.com/pinterest/ktlint/issues/1053))
- Fix false positive after `else` keyword (`argument-list-wrapping`) ([#1047](https://github.com/pinterest/ktlint/issues/1047))
- Fix formatting with comments (`colon-spacing`) ([#1057](https://github.com/pinterest/ktlint/issues/1057))
- Fix IndexOutOfBoundsException in `argument-list-wrapping-rule` formatting file with many corrections ([#1081](https://github.com/pinterest/ktlint/issues/1081))
- Fix formatting in arguments (`multiline-if-else`) ([#1079](https://github.com/pinterest/ktlint/issues/1079))
- Fix experimental:annotation-spacing-rule autocorrection with comments
- Migrate from klob dependency and fix negated globs passed to CLI are no longer worked ([#999](https://github.com/pinterest/ktlint/issues/999))
  **Breaking**: absolute paths globs will no longer work, check updated README

### Changed
- Update Gradle shadow plugin to `6.1.0` version
- Align with Kotlin plugin on how alias pattern is represented for imports layout rule ([#753](https://github.com/pinterest/ktlint/issues/753))
- Align with Kotlin plugin on how subpackages are represented ([#753](https://github.com/pinterest/ktlint/issues/753))
- Deprecated custom `kotlin_imports_layout` EditorConfig property. Please use `ij_kotlin_imports_layout` to ensure 
  that the Kotlin IDE plugin and ktlint use same imports layout ([#753](https://github.com/pinterest/ktlint/issues/753))
- Deprecated `idea` and `ascii` shortcuts as the `ij_kotlin_imports_layout` property does not support those. 
  Please check README on how to achieve those with patterns ([#753](https://github.com/pinterest/ktlint/issues/753))
- Update Gradle to `6.8.3` version
- Update Kotlin to `1.4.31` version. Fixes [#1063](https://github.com/pinterest/ktlint/issues/1063).

## [0.40.0] - 2020-12-04

Special thanks to [t-kameyama](https://github.com/t-kameyama) for the huge number of bugfixes in this release! 

### Added
- Initial implementation IDE integration via '.editorconfig' based on rules default values ([#701](https://github.com/pinterest/ktlint/issues/701))
- CLI subcommand `generateEditorConfig` to generate '.editorconfig' content for Kotlin files ([#701](https://github.com/pinterest/ktlint/issues/701)) 
- A new capability to generate baseline and run ktlint against it with `--baseline` cli option ([#707](https://github.com/pinterest/ktlint/pull/707))

### Fixed
- Do not report when semicolon is before annotation/comment/kdoc and lambda ([#825](https://github.com/pinterest/ktlint/issues/825))
- Fix false positive when import directive has backticks and alias ([#910](https://github.com/pinterest/ktlint/issues/910))
- `@receiver` annotations with parameters are not required to be on a separate line ([#885](https://github.com/pinterest/ktlint/issues/885))
- Fix false positive "File annotations should be separated from file contents with a blank line" in kts files ([#914](https://github.com/pinterest/ktlint/issues/914))
- Fix false positive `Missing newline after "->"` when `when` entry has a nested if/else block ([#901](https://github.com/pinterest/ktlint/issues/901))
- Allow an inline block comment in `argument-list-wrapping` ([#926](https://github.com/pinterest/ktlint/issues/926))
- Fix false positive for line-breaks inside lambdas in `argument-list-wrapping` ([#861](https://github.com/pinterest/ktlint/issues/861)) ([#870](https://github.com/pinterest/ktlint/issues/870))
- Fix wrong indentation inside an if-condition in `argument-list-wrapping` ([#854](https://github.com/pinterest/ktlint/issues/854)) ([#864](https://github.com/pinterest/ktlint/issues/864))
- Fix false positive for method after string template in `argument-list-wrapping` ([#842](https://github.com/pinterest/ktlint/issues/842)) ([#859](https://github.com/pinterest/ktlint/issues/859   ))
- Fix false positive when a comment is not between declarations in `spacing-between-declarations-with-comments`([#865](https://github.com/pinterest/ktlint/issues/865))
- Fix formatting with comments (`multiline-if-else`) ([#944](https://github.com/pinterest/ktlint/issues/944))
- Do not insert unnecessary spacings inside multiline if-else condition (`indent`) ([#871](https://github.com/pinterest/ktlint/issues/871)) ([#900](https://github.com/pinterest/ktlint/issues/900))
- Correctly indent primary constructor parameters when class has multiline type parameter (`parameter-list-wrapping`) ([#921](https://github.com/pinterest/ktlint/issues/921)) ([#938](https://github.com/pinterest/ktlint/issues/938))
- Correctly indent property delegates (`indent`) ([#939](https://github.com/pinterest/ktlint/issues/939))
- Fix false positive for semicolon between empty enum entry and member (`no-semicolons`) ([#957](https://github.com/pinterest/ktlint/issues/957))
- Fix wrong indentation for class delegates (`indent`) ([#960](https://github.com/pinterest/ktlint/issues/960)) ([#963](https://github.com/pinterest/ktlint/issues/963)) ([#877](https://github.com/pinterest/ktlint/issues/877))
- Fix wrong indentation in named arguments (`indent`) ([#964](https://github.com/pinterest/ktlint/issues/964))
- Fix wrong indentation when a function has multiline type arguments (`parameter-list-wrapping`) ([#965](https://github.com/pinterest/ktlint/issues/965))
- Fix false positive for `spacing-between-declarations-with-annotations` ([#970](https://github.com/pinterest/ktlint/issues/970))
- Fix ParseException when an assignment contains comments (`no-line-break-before-assignment`) ([#956](https://github.com/pinterest/ktlint/issues/956))
- Fix false positive when right brace is after a try-catch block (`spacing-around-keyword`) ([#978](https://github.com/pinterest/ktlint/issues/978))
- Fix false positive for control flow with empty body (`no-semicolons`) ([#955](https://github.com/pinterest/ktlint/issues/955))
- Fix incorrect indentation for multi-line call expressions in conditions (`indent`) ([#959](https://github.com/pinterest/ktlint/issues/959))
- Fix false positive for trailing comma before right parentheses|bracket|angle (`spacing-around-comma`) ([#975](https://github.com/pinterest/ktlint/issues/975))
- Fix ktlint CLI could skip checking some of explicitly passed files ([#942](https://github.com/pinterest/ktlint/issues/942))

### Changed
- 'import-ordering' now supports `.editorconfig' default value generation ([#701](https://github.com/pinterest/ktlint/issues/701))
- Update Gradle to `6.7.1` version

## [0.39.0] - 2020-09-14

### Added
- Add new applyToIDEA location for IDEA 2020.1.x and above on MacOs
- Debug output: print loaded .editorconfig content
- Extract `argument-list-wrapping` rule into experimental ruleset
- Split `annotation-spacing` into separate experimental rule

### Fixed
- Do not enforce raw strings opening quote to be on a separate line ([#711](https://github.com/pinterest/ktlint/issues/711))
- False negative with multiline type parameter list in function signature for `parameter-list-wrapping`([#680](https://github.com/pinterest/ktlint/issues/680))
- Alternative `.editorconfig` path is ignored on stdin input ([#869](https://github.com/pinterest/ktlint/issues/869))
- False positive with semicolons before annotations/comments/kdoc ([#825](https://github.com/pinterest/ktlint/issues/825))
- Do not report when string-template expression is a keyword ([#883](https://github.com/pinterest/ktlint/issues/883))
- False positive for subclass imports in `no-unused-imports` ([#845](https://github.com/pinterest/ktlint/issues/845))
- False positive for static java function imports in `no-unused-imports` ([#872](https://github.com/pinterest/ktlint/issues/872))
- Missing signature for KtLint CLI artifact published to Github release ([#895](https://github.com/pinterest/ktlint/issues/895))
- Crash in annotation rule ([#868](https://github.com/pinterest/ktlint/issues/868))
- False-positive unused import violation ([#902](https://github.com/pinterest/ktlint/issues/902))

### Changed
- `Ktlint` object internal code cleanup
- Deprecate some of public methods in `Ktlint` object that should not be exposed as public api
- Update Kotlin to 1.4.10 version
- Make `RuleSet` class open so it can be inherited

## [0.38.1] - 2020-08-24
Minor release to support projects using mixed 1.3/1.4 Kotlin versions (e.g. Gradle plugins)

### Changed
- Compile with `apiLevel = 1.3`

## [0.38.0] - 2020-08-21

New release with Kotlin 1.4.0 support and several enhancements and bugfixes.

### Added
- Experimental SpacingAroundAngleBracketsRule ([#769](https://github.com/pinterest/ktlint/pull/769))
- Checksum generation for executable Jar ([#695](https://github.com/pinterest/ktlint/issues/695))
- Enable Gradle dependency verification
- `parameter-list-wrapping` rule now also considers function arguments while wrapping ([#620](https://github.com/pinterest/ktlint/issues/620))
- Publish snapshots built against kotlin development versions
- Initial support for tab-based indentation ([#128](https://github.com/pinterest/ktlint/issues/128))

### Fixed
- Safe-called wrapped trailing lambdas indented correctly ([#776](https://github.com/pinterest/ktlint/issues/776))
- `provideDelegate` imports are not marked as unused anymore ([#669](https://github.com/pinterest/ktlint/issues/669))
- Set continuation indent to 4 in IDE integration codestyle ([#775](https://github.com/pinterest/ktlint/issues/775)) 
- No empty lines between annotation and annotated target ([#688](https://github.com/pinterest/ktlint/issues/688))
- Unused imports reported correctly ([#526](https://github.com/pinterest/ktlint/issues/526)) ([#405](https://github.com/pinterest/ktlint/issues/405))
- No false empty lines inserted in multiline if-else block ([#793](https://github.com/pinterest/ktlint/issues/793))
- No-wildcard-imports properly handles custom infix function with asterisk ([#799](https://github.com/pinterest/ktlint/issues/799))
- Do not require else to be in the same line of a right brace if the right brace is not part of the if statement ([#756](https://github.com/pinterest/ktlint/issues/756))
- Brace-less if-else bodies starting with parens indented correctly ([#829](https://github.com/pinterest/ktlint/issues/829))
- If-condition with multiline call expression inside indented correctly ([#796](https://github.com/pinterest/ktlint/issues/796))

### Changed
- Update Gradle to 6.6 version
- Update ec4j to 0.2.2 version. Now it should report path to `.editorconfig` file on failed parsing 
and allow empty `.editorconfig` files.
- Update Kotlin to 1.4.0 version ([#830](https://github.com/pinterest/ktlint/issues/830))


## [0.37.2] - 2020-06-16

Minor release to fix further bugs in `ImportOrderingRule`.

### Fixed
- Imports with aliases no longer removed ([#766](https://github.com/pinterest/ktlint/issues/766))

## [0.37.1] - 2020-06-08

Minor release to fix some bugs in the 0.37.0 release.

### Fixed
- Invalid path exception error on Windows machines when loading properties from .editorconfig ([#761](https://github.com/pinterest/ktlint/issues/761))
- Imports with `as` no longer removed ([#766](https://github.com/pinterest/ktlint/issues/766))
- The contents of raw strings are no longer modified by the indent rule ([#682](https://github.com/pinterest/ktlint/issues/682))

## [0.37.0] - 2020-06-02

Thank you to [Tapchicoma](https://github.com/Tapchicoma) and [romtsn](https://github.com/romtsn) for all their hard work on this release!

### Added
- Gradle wrapper validation ([#684](https://github.com/pinterest/ktlint/pull/684))
- Experimental `SpacingAroundDoubleColon` rule ([#722](https://github.com/pinterest/ktlint/pull/722)]
- Experimental `SpacingBetweenDeclarationsWithCommentsRule` and `SpacingBetweenDeclarationsWithAnnotationsRule`. Fixes ([#721]https://github.com/pinterest/ktlint/issues/721)
- `kotlin_imports_layout` config for `.editorconfig` file so that import ordering is configurable. Fixes ([#527](https://github.com/pinterest/ktlint/issues/527))


### Changed
- Kotlin was updated to 1.3.70 version
- Loading properties from `.editorconfig` was fully delegated to ec4j library. This fixes ability to override
properties for specific files/directories ([#742](https://github.com/pinterest/ktlint/issues/742))
- Promote experimental "indent" rule to standard one, old standard "indent" rule is removed 
- Functions to calculate line/column are now public so they can be used by 3rd party tools ([#725](https://github.com/pinterest/ktlint/pull/725))
- `AnnotationRule` now handles file annotations as well ([#714](https://github.com/pinterest/ktlint/pull/714))

### Fixed
- Ignore keywords in KDoc comments ([#671](https://github.com/pinterest/ktlint/issues/671))
- Allow multiple spaces in KDoc comments ([#706](https://github.com/pinterest/ktlint/issues/706))
- Trailing comment no longer reported as incorrect indentation ([#710](https://github.com/pinterest/ktlint/issues/710)]
- Annotated function types no longer reported as an error ([#737](https://github.com/pinterest/ktlint/issues/737))
- `FinalNewlineRule` no longer reports error for empty files ([#723](https://github.com/pinterest/ktlint/issues/723))
- EOL comments will no longer cause `AnnotationRule` to report an error ([#736](https://github.com/pinterest/ktlint/issues/736))
- Formatter will no longer break class declaration with trailing comment ([#728](https://github.com/pinterest/ktlint/issues/728))
- Formatting for single line if/else statements ([#174](https://github.com/pinterest/ktlint/issues/174))
- Exception in `NoLineBreakBeforeAssignmentRule` ([#693](https://github.com/pinterest/ktlint/issues/693))


### Removed
- Removed Maven; builds all run under Gradle ([#445](https://github.com/pinterest/ktlint/issues/445))
- Old standard `IndentRule`

## [0.36.0] - 2019-12-03

### Added
- HTML reporter ([#641](https://github.com/pinterest/ktlint/pull/641))
- Experimental rule to lint enum entry names ([#638](https://github.com/pinterest/ktlint/pull/638))
- `@Suppress("RemoveCurlyBracesFromTemplate")` now respected ([#263](https://github.com/pinterest/ktlint/pull/263))

### Upgraded
- Gradle version to 5.6.2 ([#616](https://github.com/pinterest/ktlint/pull/616))
- Kotlin to 1.3.60 ([#658](https://github.com/pinterest/ktlint/pull/658))

### Fixed
- `.git` directory now discovered instead of hardcoded ([#623](https://github.com/pinterest/ktlint/pull/623))
- Several bugs with the experimental annotation rule ([#628](https://github.com/pinterest/ktlint/pull/628)) ([#642](https://github.com/pinterest/ktlint/pull/642)) ([#654](https://github.com/pinterest/ktlint/pull/654)) ([#624](https://github.com/pinterest/ktlint/pull/624))
- Allow newline after lambda return type ([#643](https://github.com/pinterest/ktlint/pull/643))
- Allow empty first line in a function that returns an anonymous object ([#655](https://github.com/pinterest/ktlint/pull/655))
- Indentation with lambda argument ([#627](https://github.com/pinterest/ktlint/pull/627))
- ktlint can now lint UTF-8 files with BOM ([#630](https://github.com/pinterest/ktlint/pull/630)
- Indentation with newline before return type ([#663](https://github.com/pinterest/ktlint/pull/663))
- Build/tests on Windows ([#640](https://github.com/pinterest/ktlint/pull/640))
- Allow whitespace after `(` followed by a comment ([#664](https://github.com/pinterest/ktlint/pull/664))

## [0.35.0] - 2019-10-12

### Added
- Support for specifying color for output via `--color-name` command line flag. ([#585](https://github.com/pinterest/ktlint/pull/585))
- Support for custom rulesets and providers on Java 9+ ([#573](https://github.com/pinterest/ktlint/pull/573))

### Deprecated
- `--apply-to-idea` flag; use `applyToIDEA` subcommand instead ([#554](https://github.com/pinterest/ktlint/pull/554))
- `--apply-to-idea-project` flag; use `applyToIDEAProject` subcommand instead ([#593](https://github.com/pinterest/ktlint/pull/593))
- `0.0.0-SNAPSHOT` builds; snapshot builds are now versioned, e.g. 0.35.0-SNAPSHOT ([#588](https://github.com/pinterest/ktlint/pull/588))
  - Note: When using the new snapshot builds, you may need to add an explicit dependency on `kotlin-compiler-embeddable` to your ruleset project.

### Removed
- Support for loading 3rd party rulesets via Maven ([#566](https://github.com/pinterest/ktlint/pull/566))

### Upgraded
- Kotlin version to 1.3.50 ([#565](https://github.com/pinterest/ktlint/pull/565)) ([#611](https://github.com/pinterest/ktlint/pull/611))

### Fixed
- Bugs with spacing in experimental `AnnotationRule` ([#552](https://github.com/pinterest/ktlint/pull/552)) ([#601](https://github.com/pinterest/ktlint/pull/601/)
- Brackets would be removed from empty companion object ([#600](https://github.com/pinterest/ktlint/pull/600/))
- Bugs with experimental `IndentationRule` ([#597](https://github.com/pinterest/ktlint/pull/597/)) ([#599](https://github.com/pinterest/ktlint/pull/599/))
- Erroneous space between `}` and `]` ([#596](https://github.com/pinterest/ktlint/pull/596))
- Spacing around multiplication sign in lambdas ([#598](https://github.com/pinterest/ktlint/pull/598))
- `--version` output with gradle-built JAR ([#613](https://github.com/pinterest/ktlint/issues/613))

## [0.34.2] - 2019-07-22

Minor bugfix release for 0.34.0. (Note: 0.34.1 deprecated/deleted due to regression in disabled_flags .editorconfig support.)

### Added
- Support for globally disabling rules via `--disabled_rules` command line flag. ([#534](https://github.com/pinterest/ktlint/pull/534))

### Fixed
- Regression with `--stdin` flag for `printAST` command ([#528](https://github.com/pinterest/ktlint/issues/528))
- Regressions with `NoUnusedImports` rule ([#531](https://github.com/pinterest/ktlint/issues/531), [#526](https://github.com/pinterest/ktlint/issues/526))
  - Note: this re-introduces [#405](https://github.com/pinterest/ktlint/issues/405)
- Indentation for enums with multi-line initializers ([#518](https://github.com/pinterest/ktlint/issues/518))

## [0.34.0] - 2019-07-15

### Added
- Support for Kotlin 1.3.41
- Support for globally disabling rules via custom `disabled_rules` property in `.editorconfig` ([#503](https://github.com/pinterest/ktlint/pull/503))
- `experimental:no-empty-first-line-in-method-block` ([#474](https://github.com/pinterest/ktlint/pull/474))
- Unit tests for ruleset providers

### Upgraded
- AssertJ from 3.9.0 to 3.12.2 ([#520](https://github.com/pinterest/ktlint/pull/520)) 

### Enabled
- Final newline by default ([#446](https://github.com/pinterest/ktlint/pull/446))
- `no-wildcard-import` (Re-enabled after temporarily disabling in 0.33.0)
- `experimental:annotation` ([#509](https://github.com/pinterest/ktlint/pull/509))
- `experimental:multiline-if-else` (no autocorrection)
- `experimental:package-name` (currently only disallows underscores in package names)

### Deprecated
- `MavenDependencyResolver`. Scheduled to be removed in 0.35.0 ([#468](https://github.com/pinterest/ktlint/pull/468))
- `--install-git-pre-commit-hook` flag; use `installGitPreCommitHook` subcommand instead ([#487](https://github.com/pinterest/ktlint/pull/487))
- `--print-ast` flag; use `printAST` subcommand instead ([#500](https://github.com/pinterest/ktlint/pull/500))

### Removed
- Support for `--ruleset-repository` and `--ruleset-update` flags

### Fixed
- `import-ordering` will now refuse to format import lists that contain top-level comments ([#408](https://github.com/pinterest/ktlint/issues/408))
- `no-unused-imports` reporting false negatives or false positives in some cases ([#405](https://github.com/pinterest/ktlint/issues/405)) and ([#506](https://github.com/pinterest/ktlint/issues/506))
- `experimental:indent` incorrectly formatting a lambda's closing brace ([#479](https://github.com/pinterest/ktlint/issues/479))

## [0.33.0] - 2019-05-28

### Added
- Support for Kotlin 1.3.31

### Disabled
- No wildcard imports rule ([#48](https://github.com/pinterest/ktlint/issues/48)). Developers wishing to still enforce this rule should add the code into a custom ruleset. 

### Fixed
- Spec file parsing is now platform-agnostic ([#365](https://github.com/pinterest/ktlint/pull/365))
- Unnecessary newline after `->` in some cases ([#403](https://github.com/pinterest/ktlint/pull/403))
- `SpacingAroundCommaRule` will no longer move code into comments
- Made newlines after `=` less aggressive ([#368](https://github.com/pinterest/ktlint/issues/368)) ([#380](https://github.com/pinterest/ktlint/issues/380))
- Erroneous newline when parameter comments are used ([#433](https://github.com/pinterest/ktlint/issues/433))

## [0.32.0] - 2019-04-22

### Added
- `experimental/import-ordering` rule ([#189](https://github.com/pinterest/ktlint/issues/189)).
  Use `ktlint --experimental` to enabled.
- Support for Kotlin 1.3.30
- Build now compatible with jitpack
- `ktlint` now part of Homebrew core (`shyiko/ktlint` tap deprecated)

### Fixed
- Incorrectly flagging a missing newline for functions with no parameters ([#327](https://github.com/pinterest/ktlint/issues/327)).
- Semicolons now allowed in KDocs ([#362](https://github.com/pinterest/ktlint/issues/362)).
- Spaces now disallowed after `super` ([#369](https://github.com/pinterest/ktlint/issues/369)).
- Annotations in function parameters now checked for indentation ([#374](https://github.com/pinterest/ktlint/issues/374)]

### Changed
- Code now lives in `com.pinterest` package
- groupId now `com.pinterest`
- Custom ruleset `META-INF.services` file must be renamed to `com.pinterest.ktlint.core.RuleSetProvider`

## [0.31.0] - 2019-03-10

### Added
- `dot-spacing` rule ([#293](https://github.com/shyiko/ktlint/issues/293)).
- `experimental/indent` rule ([#338](https://github.com/shyiko/ktlint/issues/338)).  
  Use `ktlint --experimental` to enable. 

### Fixed
- Spacing check around `<` & `>` operators.

### Changed
- `no-multi-spaces` rule (horizontal alignment of comments is no longer allowed) ([#269](https://github.com/shyiko/ktlint/issues/269)).
- `colon-spacing` rule (`:` must not appear at the beginning of the line).
- `package-name` rule (disabled until [#208](https://github.com/shyiko/ktlint/issues/208) is resolved).
- `--print-ast` to output [com.pinterest.ktlint.core.ast.ElementType.*](https://github.com/shyiko/ktlint/blob/master/ktlint-core/src/main/kotlin/com/github/shyiko/ktlint/core/ast/ElementType.kt) as `node.elementType`, e.g. 
```
$ echo 'fun f() {}' | ./ktlint/target/ktlint --print-ast --color --stdin
1: ~.psi.KtFile (FILE)
1:   ~.psi.KtPackageDirective (PACKAGE_DIRECTIVE) ""
1:   ~.psi.KtImportList (IMPORT_LIST) ""
1:   ~.psi.KtScript (SCRIPT)
1:     ~.psi.KtBlockExpression (BLOCK)
1:       ~.psi.KtNamedFunction (FUN)
1:         ~.c.i.p.impl.source.tree.LeafPsiElement (FUN_KEYWORD) "fun"
1:         ~.c.i.p.impl.source.tree.PsiWhiteSpaceImpl (WHITE_SPACE) " "
1:         ~.c.i.p.impl.source.tree.LeafPsiElement (IDENTIFIER) "f"
1:         ~.psi.KtParameterList (VALUE_PARAMETER_LIST)
1:           ~.c.i.p.impl.source.tree.LeafPsiElement (LPAR) "("
1:           ~.c.i.p.impl.source.tree.LeafPsiElement (RPAR) ")"
1:         ~.c.i.p.impl.source.tree.PsiWhiteSpaceImpl (WHITE_SPACE) " "
1:         ~.psi.KtBlockExpression (BLOCK)
1:           ~.c.i.p.impl.source.tree.LeafPsiElement (LBRACE) "{"
1:           ~.c.i.p.impl.source.tree.LeafPsiElement (RBRACE) "}"
1:       ~.c.i.p.impl.source.tree.PsiWhiteSpaceImpl (WHITE_SPACE) "\n"

   format: <line_number:> <node.psi::class> (<node.elementType>) "<node.text>"
   legend: ~ = org.jetbrains.kotlin, c.i.p = com.intellij.psi
```

- `kotlin-compiler` version to 1.3.21 (from 1.3.20).

### Removed 
- Dependency on JCenter ([#349](https://github.com/shyiko/ktlint/issues/349)).

## [0.30.0] - 2019-02-03

### Fixed
- `Missing newline before ")"` ([#327](https://github.com/shyiko/ktlint/issues/327)).

### Changed
- `kotlin-compiler` version to 1.3.20 (from 1.2.71) ([#331](https://github.com/shyiko/ktlint/issues/331)).

### Security
- `--ruleset`/`--reporter` switched to HTTPS ([#332](https://github.com/shyiko/ktlint/issues/332)).

## [0.29.0] - 2018-10-02

### Fixed
- `no-semi` rule to preserve semicolon after `companion object;` (see [#281](https://github.com/shyiko/ktlint/issues/281) for details).
- "line number off by one" when `end_of_line=CRLF` is used ([#286](https://github.com/shyiko/ktlint/issues/286)).

### Changed
- `package-name` rule not to check file location (until [#280](https://github.com/shyiko/ktlint/issues/280) can be properly addressed).
- `comment-spacing` rule not to flag `//region` & `//endregion` comments ([#278](https://github.com/shyiko/ktlint/issues/278)).
- `kotlin-compiler` version to 1.2.71 (from 1.2.51).

## [0.28.0] - 2018-09-05

### Fixed
- ktlint hanging in case of unhandled exception in a reporter ([#277](https://github.com/shyiko/ktlint/issues/277)).

### Changed
- `package-name` rule (directories containing `.` in their names are no longer considered to be invalid) ([#276](https://github.com/shyiko/ktlint/issues/276)).

## [0.27.0] - 2018-08-06

### Changed
- ktlint output (report location is now printed only if there are style violations) ([#267](https://github.com/shyiko/ktlint/issues/267)).

## [0.26.0] - 2018-07-30

### Changed
- `max-line-length` rule (multi-line strings are no longer checked) ([#262](https://github.com/shyiko/ktlint/issues/262)).

## [0.25.1] - 2018-07-25

### Fixed
- `json` reporter \ and control characters escaping ([#256](https://github.com/shyiko/ktlint/issues/256)).

## [0.25.0] - 2018-07-25

### Added
- `package-name` rule ([#246](https://github.com/shyiko/ktlint/pull/246)).
- `--editorconfig=path/to/.editorconfig` ([#250](https://github.com/shyiko/ktlint/pull/250)).
- Support for `end_of_line=native` (`.editorconfig`) ([#225](https://github.com/shyiko/ktlint/issues/225)).
- `tab -> space * indent_size` auto-correction (`--format`/`-F`).

### Fixed
- "Unnecessary semicolon" false positive ([#255](https://github.com/shyiko/ktlint/issues/255)).
- `(cannot be auto-corrected)` reporting.
- OOM in `--debug` mode while trying to print `root=true <- root=false` `.editorconfig` chain.

### Changed
- `kotlin-compiler` version to 1.2.51 (from 1.2.50).

## [0.24.0] - 2018-06-22

### Added 
- `paren-spacing` rule ([#223](https://github.com/shyiko/ktlint/issues/223)).
- Report location output ([#218](https://github.com/shyiko/ktlint/issues/218), [#224](https://github.com/shyiko/ktlint/issues/224)).
- An indication that some lint errors cannot be auto-corrected ([#219](https://github.com/shyiko/ktlint/issues/219)).
- Git hook to automatically check files for style violations on push (an alternative to existing `ktlint --install-git-pre-commit-hook`)  
(execute `ktlint --install-git-pre-push-hook` to install) ([#229](https://github.com/shyiko/ktlint/pull/229)).
- Support for `end_of_line=crlf` (`.editorconfig`) ([#225](https://github.com/shyiko/ktlint/issues/225)).

### Fixed
- `.editorconfig` path resolution  
(you no longer need to be inside project directory for `.editorconfig` to be loaded) ([#207](https://github.com/shyiko/ktlint/pull/207)).
- NPE in case of I/O error ([klob@0.2.1](https://github.com/shyiko/klob/blob/master/CHANGELOG.md#020---2017-07-21)).

### Changed
- `comment-spacing` rule to exclude `//noinspection` ([#212](https://github.com/shyiko/ktlint/pull/212)).
- `kotlin-compiler` version to 1.2.50 (from 1.2.41) ([#226](https://github.com/shyiko/ktlint/issues/226)).

## [0.23.1] - 2018-05-04

### Fixed
- `ClassCastException: cannot be cast to LeafPsiElement` ([#205](https://github.com/shyiko/ktlint/issues/205)).

## [0.23.0] - 2018-05-02

### Added
- `comment-spacing` ([#198](https://github.com/shyiko/ktlint/pull/198)),  
  `filename` ([#194](https://github.com/shyiko/ktlint/pull/194)) rules.
- `parameter-list-wrapping` left parenthesis placement check ([#201](https://github.com/shyiko/ktlint/pull/201)).
- `parameter-list-wrapping` auto-correction when `max_line_length` is exceeded ([#200](https://github.com/shyiko/ktlint/pull/200)).

### Fixed
- "Unused import" false positive (x.y.zNNN import inside x.y.z package) ([#204](https://github.com/shyiko/ktlint/issues/204)).

### Changed
- `kotlin-compiler` version to 1.2.41 (from 1.2.40).

## [0.22.0] - 2018-04-22

### Added
- `--apply-to-idea-project` (as an alternative to (global) `--apply-to-idea`) ([#178](https://github.com/shyiko/ktlint/issues/178)).
- Check to verify that annotations are placed before the modifiers ([#183](https://github.com/shyiko/ktlint/pull/183)).
- Access to PsiFile location information ([#194](https://github.com/shyiko/ktlint/pull/194)).

### Fixed
- `--format` commenting out operators (`chain-wrapping` rule) ([#193](https://github.com/shyiko/ktlint/pull/193)).

### Changed
- `indent` rule (`continuation_indent_size` is now ignored) ([#171](https://github.com/shyiko/ktlint/issues/171)).  
NOTE: if you have a custom `continuation_indent_size` (and `gcd(indent_size, continuation_indent_size) == 1`) ktlint
won't check the indentation.
- `--apply-to-idea` to inherit "Predefined style / Kotlin style guide" (Kotlin plugin 1.2.20+).
- `kotlin-compiler` version to 1.2.40 (from 1.2.30).

## [0.21.0] - 2018-03-29

### Changed
- `indent` rule to ignore `where <type constraint list>` clause ([#180](https://github.com/shyiko/ktlint/issues/180)).

## [0.20.0] - 2018-03-20

### Added
- Ability to load 3rd party reporters from the command-line (e.g. `--reporter=<name>,artifact=<group_id>:<artifact_id>:<version>`) ([#176](https://github.com/shyiko/ktlint/issues/176)).
- `--ruleset`/`--reporter` dependency tree validation.

### Fixed
- Handling of spaces in `--reporter=...,output=<path_to_a_file>` ([#177](https://github.com/shyiko/ktlint/issues/177)).
- `+`, `-`, `*`, `/`, `%`, `&&`, `||` wrapping ([#168](https://github.com/shyiko/ktlint/issues/168)).

### Changed
- `comma-spacing` rule to be more strict ([#173](https://github.com/shyiko/ktlint/issues/173)).
- `no-line-break-after-else` rule to allow multi-line `if/else` without curly braces. 

## [0.19.0] - 2018-03-04

### Changed
- Lambda formatting: if lambda is assigned a label, there should be no space between the label and the opening curly brace ([#167](https://github.com/shyiko/ktlint/issues/167)).  

## [0.18.0] - 2018-03-01

### Added
- Java 9 support ([#152](https://github.com/shyiko/ktlint/issues/152)).

### Changed
- `kotlin-compiler` version to 1.2.30 (from 1.2.20).

## [0.17.1] - 2018-02-28

### Fixed
- `Internal Error (parameter-list-wrapping)` when `indent_size=unset` ([#165](https://github.com/shyiko/ktlint/issues/165)). 

## [0.17.0] - 2018-02-28

### Fixed
- `+`/`-` wrapping inside `catch` block, after `else` and `if (..)` ([#160](https://github.com/shyiko/ktlint/issues/160)). 
- Multi-line parameter declaration indentation ([#161](https://github.com/shyiko/ktlint/issues/161)).
- Expected indentation reported by `indent` rule.

### Changed
- Error code returned by `ktlint --format/-F` when some of the errors cannot be auto-corrected (previously it was 0 instead of expected 1) ([#162](https://github.com/shyiko/ktlint/issues/162)). 

## [0.16.1] - 2018-02-27

### Fixed
- Handling of negative number condition in `when` block ([#160](https://github.com/shyiko/ktlint/issues/160)). 

## [0.16.0] - 2018-02-27

### Added
- `parameter-list-wrapping` rule ([#130](https://github.com/shyiko/ktlint/issues/130)).
- `+`, `-`, `*`, `/`, `%`, `&&`, `||` wrapping check (now part of `chain-wrapping` rule).

### Fixed
- Unused `componentN` import (where N > 5) false positive ([#142](https://github.com/shyiko/ktlint/issues/142)).
- max-line-length error suppression ([#158](https://github.com/shyiko/ktlint/issues/158)). 

### Changed
- `modifier-order` rule to match official [Kotlin Coding Conventions](https://kotlinlang.org/docs/reference/coding-conventions.html#modifiers) ([#146](https://github.com/shyiko/ktlint/issues/146))  
(`override` modifier should be placed before `suspend`/`tailrec`, not after) 

## [0.15.1] - 2018-02-14

### Fixed
- Race condition when multiple rules try to modify AST node that gets detached as a result of mutation ([#154](https://github.com/shyiko/ktlint/issues/154)).

## [0.15.0] - 2018-01-18

### Added
- `no-line-break-after-else` rule ([#125](https://github.com/shyiko/ktlint/issues/125)).

### Changed
- `kotlin-compiler` version to 1.2.20 (from 1.2.0).

## [0.14.0] - 2017-11-30

### Changed
- `continuation_indent_size` to 4 when `--android` profile is used ([android/kotlin-guides#37](https://github.com/android/kotlin-guides/issues/37)). 

### Fixed
- Maven integration ([#117](https://github.com/shyiko/ktlint/issues/117)).

## [0.13.0] - 2017-11-28

### Added
- `no-line-break-before-assignment` ([#105](https://github.com/shyiko/ktlint/issues/105)),  
  `chain-wrapping` ([#23](https://github.com/shyiko/ktlint/issues/23))
(when wrapping chained calls `.`, `?.` and `?:` should be placed on the next line),  
  `range-spacing` (no spaces around range (`..`) operator) rules.
- `--print-ast` CLI option which can be used to dump AST of the file   
(see [README / Creating a ruleset / AST](https://github.com/shyiko/ktlint#ast) for more details)
- `--color` CLI option for colored output (where supported, e.g. --print-ast, default (plain) reporter, etc) 

### Changed
- `.editorconfig` property resolution.   
An explicit `[*.{kt,kts}]` is not required anymore (ktlint looks for sections
containing `*.kt` (or `*.kts`) and will fallback to `[*]` whenever property cannot be found elsewhere).   
Also, a search for .editorconfig will no longer stop on first (closest) `.editorconfig` (unless it contains `root=true`). 
- `max-line-length` rule to assume `max_line_length=100` when `ktlint --android ...` is used  
(per [Android Kotlin Style Guide](https://android.github.io/kotlin-guides/style.html)).  
- `kotlin-compiler` version to 1.2.0 (from 1.1.51).

### Fixed
- `no-empty-class-body` auto-correction at the end of file ([#109](https://github.com/shyiko/ktlint/issues/109)).
- `max-line-length` rule when applied to KDoc ([#112](https://github.com/shyiko/ktlint/issues/112))  
(previously KDoc was subject to `max-line-length` even though regular comments were not).
- Spacing around `=` in @annotation|s (`op-spacing`).
- Spacing around generic type parameters of functions (e.g. `fun <T>f(): T {}` -> `fun <T> f(): T {}`).
- `no-consecutive-blank-lines` not triggering at the end of file (when exactly 2 blank lines are present) ([#108](https://github.com/shyiko/ktlint/issues/108)) 
- `indent` `continuation_indent_size % indent_size != 0` case ([#76](https://github.com/shyiko/ktlint/issues/76))
- `indent` rule skipping first parameter indentation check. 
- `final-newline` rule in the context of kotlin script.
- Git hook (previously files containing space character (among others) in their names were ignored)  
- Exit code when file cannot be linted due to the invalid syntax or internal error.

## [0.12.1] - 2017-11-13

### Fixed
- A conflict between `org.eclipse.aether:aether-*:1.1.0` and `org.eclipse.aether:aether-*:1.0.0.v20140518` ([#100](https://github.com/shyiko/ktlint/issues/100)).

## [0.12.0] - 2017-11-10

### Added
- `--android` (`-a`) CLI option (turns on [Android Kotlin Style Guide](https://android.github.io/kotlin-guides/style.html) compatibility)  
(right now it's used only by `ktlint --apply-to-idea`).

### Changed
- `ktlint --apply-to-idea` to account for `indent_size` & `continuation_indent_size` in `.editorconfig` (if any). 

### Removed
- `ktlint-intellij-idea-integration` binary (deprecated in [0.9.0](#090---2017-07-23)).

### Fixed
- "Unused import" false positive (`component1`..`component5`).

## [0.11.1] - 2017-10-26

### Fixed
- `--reporter`'s `output` handling (previously parent directory was expected to exist) ([#97](https://github.com/shyiko/ktlint/issues/97)).

## [0.11.0] - 2017-10-25

### Added
- `no-blank-line-before-rbrace` rule ([#65](https://github.com/shyiko/ktlint/issues/65)).

### Fixed
- Redundant space inserted between `}` and `::` (curly-spacing).

## [0.10.2] - 2017-10-25 [YANKED]

This release contains changes that were meant for 0.11.0 and so it was retagged as such.

## [0.10.1] - 2017-10-22

### Fixed
- Redundant space inserted between `}` and `[key]`/`(...)` (curly-spacing) ([#91](https://github.com/shyiko/ktlint/issues/91)).

## [0.10.0] - 2017-10-10

### Added

- Git hook to automatically check files for style violations on commit   
(execute `ktlint --install-git-pre-commit-hook` to install).
- Ability to specify multiple reporters   
(output can be controlled with `--reporter=<name>,output=<path/to/file>`) ([#71](https://github.com/shyiko/ktlint/issues/71)).
- Support for `indent_size=unset` (`.editorconfig`) ([#70](https://github.com/shyiko/ktlint/issues/70)).

### Fixed
- `( {` formatting   
(previously both `( {` and `({` were accepted as correct, while only `({` should be) (`curly-spacing` rule) ([#80](https://github.com/shyiko/ktlint/issues/80)).
- `if\nfn {}\nelse` formatting (`curly-spacing` rule). 
- `max_line_length=off` & `max_line_length=unset` handling (`.editorconfig`).

### Changed
- `kotlin-compiler` version to 1.1.51 (from 1.1.3-2).
- `ktlint --apply-to-idea` to include `OPTIMIZE_IMPORTS_ON_THE_FLY=true`. 

## [0.9.2] - 2017-09-01

### Fixed

- `: Unit =` formatting (`: Unit` is no longer dropped when `=` is used) ([#77](https://github.com/shyiko/ktlint/issues/77)). 

## [0.9.1] - 2017-07-30

### Fixed

- `${super.toString()}` linting (`string-template` rule) ([#69](https://github.com/shyiko/ktlint/issues/69)). 

## [0.9.0] - 2017-07-23

### Added

- [Reporter](ktlint-core/src/main/kotlin/com/github/shyiko/ktlint/core/Reporter.kt) API.   
`ktlint` comes with 3 built-in reporters: `plain` (default; `?group_by_file` can be appended to enable grouping by file (shown below)), `json` and `checkstyle`. 
```
$ ktlint --reporter=plain?group_by_file
path/to/file.kt
  1:10 Unused import.
  2:10 Unnecessary "Unit" return type.
path/to/another-file.kt
  1:10 Unnecessary semicolon.
```   
- [string-template](https://pinterest.github.io/ktlint/rules/standard/#string-template),  
[no-empty-class-body](https://pinterest.github.io/ktlint/rules/standard/#no-empty-class-bodies),  
max-line-length ([#47](https://github.com/shyiko/ktlint/issues/47)),  
final-newline (activated only if `insert_final_newline` is set in `.editorconfig` (under `[*.{kt,kts}]`)) rules.
- `--limit` CLI option (e.g. use `--limit=10` to limit the number of errors to display).
- `--relative` CLI flag which makes `ktlint` output file paths relative to working directory (e.g. `dir/file.kt` instead of
`/home/269/project/dir/file.kt`).

### Changed

- **BREAKING**: JDK version to 1.8 (as a result of upgrading `kotlin-compiler` to 1.1.3-2 (from 1.1.0)).
- File matching (offloaded to [klob](https://github.com/shyiko/klob)).  

### Deprecated

- `--ruleset-repository` and `--ruleset-update` CLI arguments in favour of `--repository` and `--repository-update` 
respectively (`--ruleset-*` will be removed in 1.0.0).  
- `ktlint-intellij-idea-integration` binary   
([Intellij IDEA integration](https://github.com/shyiko/ktlint#option-1-recommended) task is now included in `ktlint` (as `ktlint --apply-to-idea`)).

## [0.8.3] - 2017-06-19

### Fixed

- "Missing spacing after ";"" at the end of package declaration ([#59](https://github.com/shyiko/ktlint/issues/59)).
- "Unused import" false positive (`setValue`) ([#55](https://github.com/shyiko/ktlint/issues/55)).
- `get`/`set`ter spacing ([#56](https://github.com/shyiko/ktlint/pull/56)).

## [0.8.2] - 2017-06-06

### Fixed

- "Unused import" false positive (`getValue`) ([#54](https://github.com/shyiko/ktlint/issues/54)).

## [0.8.1] - 2017-05-30

### Fixed

- `ktlint --stdin` ([#51](https://github.com/shyiko/ktlint/issues/51)).

## [0.8.0] - 2017-05-30

### Added

- [.editorconfig](https://editorconfig.org/) support (right now only `indent_size` is honored and only if it's 
set in `[*{kt,kts}]` section).
- Support for vertically aligned comments (see [NoMultipleSpacesRuleTest.kt](ktlint-ruleset-standard/src/test/kotlin/com/github/shyiko/ktlint/ruleset/standard/NoMultipleSpacesRuleTest.kt)).

### Fixed

- ktlint-ruleset-standard ("no-unit-return" & "modifier-order" where not included).

## [0.7.1] - 2017-05-29

### Fixed

- Triggering of "Unused import" when element is referenced in KDoc(s) only ([#46](https://github.com/shyiko/ktlint/issues/46)).

## [0.7.0] - 2017-05-28

### Added

- [no-unit-return](https://pinterest.github.io/ktlint/rules/standard/#no-unit-as-return-type) rule.
- [modifier-order](https://pinterest.github.io/ktlint/rules/standard/#modifier-order) rule ([#42](https://github.com/shyiko/ktlint/issues/42)).
- `else/catch/finally` on the same line as `}` check (now part of "keyword-spacing" rule).
- `ktlint-intellij-idea-integration` binary for easy Intellij IDEA config injection.

## [0.6.2] - 2017-05-22

### Fixed
- Unused "iterator" extension function import false positive ([#40](https://github.com/shyiko/ktlint/issues/40)).

## [0.6.1] - 2017-03-06

### Fixed
- Detection of unnecessary "same package" imports (no-unused-imports).
- FileNotFoundException while scanning FS ([#36](https://github.com/shyiko/ktlint/issues/36)).

## [0.6.0] - 2017-03-01

### Changed
- `kotlin-compiler` version to 1.1.0 (from 1.1-M04).

## [0.5.1] - 2017-02-28

### Fixed
- Unnecessary spacing around angle brackets in case of `super<T>` ([#34](https://github.com/shyiko/ktlint/issues/34)).

## [0.5.0] - 2017-02-20

### Fixed
- Redundant space inserted between `}` and `!!` (curly-spacing).

### Changed
- `indent` rule to allow "Method declaration parameters -> Align when multiline" (as this option is (unfortunately) "on" by default in Intellij IDEA) ([#26](https://github.com/shyiko/ktlint/issues/26)).

## [0.4.0] - 2017-02-01

### Fixed
- NPE in case of "Permission denied" (while scanning the file system).

### Changed
- `kotlin-compiler` version to 1.1-M04 (from 1.0.6).

## [0.3.1] - 2017-01-25

### Fixed
- Unused infix function call import false positive ([#25](https://github.com/shyiko/ktlint/issues/25)).

## [0.3.0] - 2017-01-11

### Added 
- `*.kts` (script) support.

### Changed
- `kotlin-compiler` version to 1.0.6 (from 1.0.3).

## [0.2.2] - 2016-10-11

### Fixed
- `no-wildcard-imports` rule (kotlinx.android.synthetic excluded from check) ([#16](https://github.com/shyiko/ktlint/pull/16)).

## [0.2.1] - 2016-09-13

### Fixed
- `curly-spacing` false negative in case of `}?.`.

## [0.2.0] - 2016-09-05

### Added
- Support for 3rd party "ruleset"s. 

### Changed
- `ktlint -F` output (it now includes lint errors that cannot be fixed automatically). 

### Fixed
- `ktlint -F --debug` error count.  
- Glob implementation (previously it was prone to catastrophic backtracking).
- Redundant semicolon false positive in case of enum ([#12](https://github.com/shyiko/ktlint/issues/12)).
- Unused operator import false positive ([#13](https://github.com/shyiko/ktlint/issues/13)).

## [0.1.2] - 2016-08-05

### Fixed
- "in-use" escaped imports detection ([#7](https://github.com/shyiko/ktlint/issues/7)) (no-unused-imports).   

## [0.1.1] - 2016-08-01

### Fixed
- Incorrect spacing around curly braces (curly-spacing).

## 0.1.0 - 2016-07-27

[2.0.0-ALPHA-5]: https://github.com/pinterest/ktlint/compare/2.0.0-ALPHA-5...2.0.0-ALPHA-4
[2.0.0-ALPHA-4]: https://github.com/pinterest/ktlint/compare/2.0.0-ALPHA-4...1.8.0
[1.8.0]: https://github.com/pinterest/ktlint/compare/1.8.0...1.7.1
[1.7.1]: https://github.com/pinterest/ktlint/compare/1.7.1...1.7.0
[1.7.0]: https://github.com/pinterest/ktlint/compare/1.7.0...1.6.0
[1.6.0]: https://github.com/pinterest/ktlint/compare/1.6.0...1.5.0
[1.5.0]: https://github.com/pinterest/ktlint/compare/1.5.0...1.4.1
[1.4.1]: https://github.com/pinterest/ktlint/compare/1.4.1...1.4.0
[1.4.0]: https://github.com/pinterest/ktlint/compare/1.4.0...1.3.1
[1.3.1]: https://github.com/pinterest/ktlint/compare/1.3.1...1.3.0
[1.3.0]: https://github.com/pinterest/ktlint/compare/1.3.0...1.2.1
[1.2.1]: https://github.com/pinterest/ktlint/compare/1.2.1...1.2.0
[1.2.0]: https://github.com/pinterest/ktlint/compare/1.2.0...1.1.1
[1.1.1]: https://github.com/pinterest/ktlint/compare/1.1.1...1.1.0
[1.1.0]: https://github.com/pinterest/ktlint/compare/1.0.1...1.1.0
[1.0.1]: https://github.com/pinterest/ktlint/compare/1.0.0...1.0.1
[1.0.0]: https://github.com/pinterest/ktlint/compare/0.50.0...1.0.0
[0.50.0]: https://github.com/pinterest/ktlint/compare/0.49.1...0.50.0
[0.49.1]: https://github.com/pinterest/ktlint/compare/0.49.0...0.49.1
[0.49.0]: https://github.com/pinterest/ktlint/compare/0.48.2...0.49.0
[0.48.2]: https://github.com/pinterest/ktlint/compare/0.48.1...0.48.2
[0.48.1]: https://github.com/pinterest/ktlint/compare/0.48.0...0.48.1
[0.48.0]: https://github.com/pinterest/ktlint/compare/0.47.1...0.48.0
[0.47.1]: https://github.com/pinterest/ktlint/compare/0.47.0...0.47.1
[0.47.0]: https://github.com/pinterest/ktlint/compare/0.46.1...0.47.0
[0.46.1]: https://github.com/pinterest/ktlint/compare/0.46.0...0.46.1
[0.46.0]: https://github.com/pinterest/ktlint/compare/0.45.2...0.46.0
[0.45.2]: https://github.com/pinterest/ktlint/compare/0.45.1...0.45.2
[0.45.1]: https://github.com/pinterest/ktlint/compare/0.45.0...0.45.1
[0.45.0]: https://github.com/pinterest/ktlint/compare/0.44.0...0.45.0
[0.44.0]: https://github.com/pinterest/ktlint/compare/0.43.2...0.44.0
[0.43.2]: https://github.com/pinterest/ktlint/compare/0.43.0...0.43.2
[0.43.0]: https://github.com/pinterest/ktlint/compare/0.42.1...0.43.0
[0.42.1]: https://github.com/pinterest/ktlint/compare/0.42.0...0.42.1
[0.42.0]: https://github.com/pinterest/ktlint/compare/0.41.0...0.42.0
[0.41.0]: https://github.com/pinterest/ktlint/compare/0.40.0...0.41.0
[0.40.0]: https://github.com/pinterest/ktlint/compare/0.39.0...0.40.0
[0.39.0]: https://github.com/pinterest/ktlint/compare/0.38.1...0.39.0
[0.38.1]: https://github.com/pinterest/ktlint/compare/0.38.0...0.38.1
[0.38.0]: https://github.com/pinterest/ktlint/compare/0.37.2...0.38.0
[0.37.2]: https://github.com/pinterest/ktlint/compare/0.37.1...0.37.2
[0.37.1]: https://github.com/pinterest/ktlint/compare/0.37.0...0.37.1
[0.37.0]: https://github.com/pinterest/ktlint/compare/0.36.0...0.37.0
[0.36.0]: https://github.com/pinterest/ktlint/compare/0.35.0...0.36.0
[0.35.0]: https://github.com/pinterest/ktlint/compare/0.34.2...0.35.0
[0.34.2]: https://github.com/pinterest/ktlint/compare/0.33.0...0.34.2
[0.34.0]: https://github.com/pinterest/ktlint/compare/0.33.0...0.34.0
[0.33.0]: https://github.com/pinterest/ktlint/compare/0.32.0...0.33.0
[0.32.0]: https://github.com/pinterest/ktlint/compare/0.31.0...0.32.0
[0.31.0]: https://github.com/pinterest/ktlint/compare/0.30.0...0.31.0
[0.30.0]: https://github.com/pinterest/ktlint/compare/0.29.0...0.30.0
[0.29.0]: https://github.com/pinterest/ktlint/compare/0.28.0...0.29.0
[0.28.0]: https://github.com/pinterest/ktlint/compare/0.27.0...0.28.0
[0.27.0]: https://github.com/pinterest/ktlint/compare/0.26.0...0.27.0
[0.26.0]: https://github.com/pinterest/ktlint/compare/0.25.1...0.26.0
[0.25.1]: https://github.com/pinterest/ktlint/compare/0.25.0...0.25.1
[0.25.0]: https://github.com/pinterest/ktlint/compare/0.24.0...0.25.0
[0.24.0]: https://github.com/pinterest/ktlint/compare/0.23.1...0.24.0
[0.23.1]: https://github.com/pinterest/ktlint/compare/0.23.0...0.23.1
[0.23.0]: https://github.com/pinterest/ktlint/compare/0.22.0...0.23.0
[0.22.0]: https://github.com/pinterest/ktlint/compare/0.21.0...0.22.0
[0.21.0]: https://github.com/pinterest/ktlint/compare/0.20.0...0.21.0
[0.20.0]: https://github.com/pinterest/ktlint/compare/0.19.0...0.20.0
[0.19.0]: https://github.com/pinterest/ktlint/compare/0.18.0...0.19.0
[0.18.0]: https://github.com/pinterest/ktlint/compare/0.17.1...0.18.0
[0.17.1]: https://github.com/pinterest/ktlint/compare/0.17.0...0.17.1
[0.17.0]: https://github.com/pinterest/ktlint/compare/0.16.1...0.17.0
[0.16.1]: https://github.com/pinterest/ktlint/compare/0.16.0...0.16.1
[0.16.0]: https://github.com/pinterest/ktlint/compare/0.15.1...0.16.0
[0.15.1]: https://github.com/pinterest/ktlint/compare/0.15.0...0.15.1
[0.15.0]: https://github.com/pinterest/ktlint/compare/0.14.0...0.15.0
[0.14.0]: https://github.com/pinterest/ktlint/compare/0.13.0...0.14.0
[0.13.0]: https://github.com/pinterest/ktlint/compare/0.12.1...0.13.0
[0.12.1]: https://github.com/pinterest/ktlint/compare/0.12.0...0.12.1
[0.12.0]: https://github.com/pinterest/ktlint/compare/0.11.1...0.12.0
[0.11.1]: https://github.com/pinterest/ktlint/compare/0.11.0...0.11.1
[0.11.0]: https://github.com/pinterest/ktlint/compare/0.10.1...0.11.0
[0.10.2]: https://github.com/pinterest/ktlint/compare/0.10.1...0.10.2
[0.10.1]: https://github.com/pinterest/ktlint/compare/0.10.0...0.10.1
[0.10.0]: https://github.com/pinterest/ktlint/compare/0.9.2...0.10.0
[0.9.2]: https://github.com/pinterest/ktlint/compare/0.9.1...0.9.2
[0.9.1]: https://github.com/pinterest/ktlint/compare/0.9.0...0.9.1
[0.9.0]: https://github.com/pinterest/ktlint/compare/0.8.3...0.9.0
[0.8.3]: https://github.com/pinterest/ktlint/compare/0.8.2...0.8.3
[0.8.2]: https://github.com/pinterest/ktlint/compare/0.8.1...0.8.2
[0.8.1]: https://github.com/pinterest/ktlint/compare/0.8.0...0.8.1
[0.8.0]: https://github.com/pinterest/ktlint/compare/0.7.1...0.8.0
[0.7.1]: https://github.com/pinterest/ktlint/compare/0.7.0...0.7.1
[0.7.0]: https://github.com/pinterest/ktlint/compare/0.6.2...0.7.0
[0.6.2]: https://github.com/pinterest/ktlint/compare/0.6.1...0.6.2
[0.6.1]: https://github.com/pinterest/ktlint/compare/0.6.0...0.6.1
[0.6.0]: https://github.com/pinterest/ktlint/compare/0.5.1...0.6.0
[0.5.1]: https://github.com/pinterest/ktlint/compare/0.5.0...0.5.1
[0.5.0]: https://github.com/pinterest/ktlint/compare/0.4.0...0.5.0
[0.4.0]: https://github.com/pinterest/ktlint/compare/0.3.1...0.4.0
[0.3.1]: https://github.com/pinterest/ktlint/compare/0.3.0...0.3.1
[0.3.0]: https://github.com/pinterest/ktlint/compare/0.2.2...0.3.0
[0.2.2]: https://github.com/pinterest/ktlint/compare/0.2.1...0.2.2
[0.2.1]: https://github.com/pinterest/ktlint/compare/0.2.0...0.2.1
[0.2.0]: https://github.com/pinterest/ktlint/compare/0.1.2...0.2.0
[0.1.2]: https://github.com/pinterest/ktlint/compare/0.1.1...0.1.2
[0.1.1]: https://github.com/pinterest/ktlint/compare/0.1.0...0.1.1
