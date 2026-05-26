## Why should I use ktlint?

the short answer is **Simplicity**.

Spending time on configuration (and maintenance down the road) of hundred-line long style config file(s) is counter-productive. Instead of wasting your energy on something that has no business value - focus on what really matters (not debating whether to use tabs or spaces).

By using ktlint you put the importance of code clarity and community conventions over personal preferences. This makes things easier for people reading your code as well as frees you from having to document and explain what style potential contributor(s) have to follow.

ktlint is a single binary with both linter & formatter included. All you need is to drop it in (no need to get [overwhelmed](https://en.wikipedia.org/wiki/Decision_fatigue) while choosing among [dozens of code style options](https://checkstyle.sourceforge.net/checks.html)).


## What are the Maven coordinates in Ktlint 1.x?

With the release of ktlint `1.0` the Maven coordinates of most modules have been changed. Now all ktlint modules are published in Maven group `com.pinterest.ktlint`. Also, the artifact id's of some modules have been changed.

The Maven coordinates of modules below have been changed:

| Old Maven coordinates                                | New Maven coordinates                                    |
|------------------------------------------------------|----------------------------------------------------------|
| `com.pinterest:ktlint`                               | `com.pinterest.ktlint:ktlint-cli`                        |
| `com.pinterest.ktlint:ktlint-reporter-baseline`      | `com.pinterest.ktlint:ktlint-cli-reporter-baseline`      |
| `com.pinterest.ktlint:ktlint-reporter-checkstyle`    | `com.pinterest.ktlint:ktlint-cli-reporter-checkstyle`    |
| `com.pinterest.ktlint:ktlint-cli-reporter`           | `com.pinterest.ktlint:ktlint-cli-reporter-core`          |
| `com.pinterest.ktlint:ktlint-reporter-format`        | `com.pinterest.ktlint:ktlint-cli-reporter-format`        |
| `com.pinterest.ktlint:ktlint-reporter-html`          | `com.pinterest.ktlint:ktlint-cli-reporter-html`          |
| `com.pinterest.ktlint:ktlint-reporter-json`          | `com.pinterest.ktlint:ktlint-cli-reporter-json`          |
| `com.pinterest.ktlint:ktlint-reporter-plain`         | `com.pinterest.ktlint:ktlint-cli-reporter-plain`         |
| `com.pinterest.ktlint:ktlint-reporter-plain-summary` | `com.pinterest.ktlint:ktlint-cli-reporter-plain-summary` |
| `com.pinterest.ktlint:ktlint-reporter-sarif`         | `com.pinterest.ktlint:ktlint-cli-reporter-sarif`         |

##  How do I enable or disable a rule?

An individual rule can be enabled or disabled with a rule property. The name of the rule property consists of the `ktlint_` prefix followed by the rule set id followed by a `_` and the rule id. Examples:
```editorconfig
ktlint_standard_final-newline = disabled # Disables the `final-newline` rule in the `standard` rule set provided by KtLint
ktlint_standard_some-experimental-rule = enabled # Enables the (experimental) `some-experimental-rule` in the `standard` rule set provided by KtLint
ktlint_custom-rule-set_custom-rule = disabled # Disables the `custom-rule` rule in the `custom-rule-set` rule set (not provided by KtLint)
```

!!! note
    The *rule* properties are applied after applying the *rule set* properties and take precedence. So if a rule set is disabled but a specific rule of that rule set is enabled, then the rule will be executed.

##  How do I enable or disable a rule set?

All rules in a rule set can be enabled or disabled with a rule set property. The name of the rule set property consists of the `ktlint_` prefix followed by the rule set id. Examples:
```editorconfig
ktlint_standard = disabled # Disable all rules from the `standard` rule set provided by KtLint
ktlint_experimental = enabled # Enable rules marked as experimental for all rule sets that are enabled
ktlint_custom-rule-set = enabled # Enable all rules in the `custom-rule-set` rule set (not provided by KtLint)
```

!!! note
    All rules from the `standard` and custom rule sets are *enabled* by default and can optionally be disabled in the `.editorconfig`. All `experimental` rules are *disabled* by default and can optionally be enabled in the `.editorconfig`.

## Why is a rule skipped when I disable some other rule?

Most rules in ktlint can be executed independently of other rules. However, some rules can only be executed in case one or more other rules are also loaded and/or enabled. Dependencies between rules are introduced to reduce complexity in ktlint. Similar logic in different rules has to be avoided as this might result in formatting conflicts between different rules, which could result in endless loops of formatting and reformatting by a set of rules.

In case, you disable a rule, you might run into an `IllegalStateException` like below:

```text
java.lang.IllegalStateException: Skipping rule(s) which are depending on a rule which is not loaded. Please check if you need to add additional rule sets before creating an issue.
  - Rule with id 'RuleId(value=standard:string-template-indent)' requires rule with id 'RuleId(value=standard:multiline-expression-wrapping)' to be loaded
```

For the example above, the `string-template-indent` rule depends on the `multiline-expression-wrapping` so that the former rule does not need to know, how to wrap a multiline string that is not yet wrapped:
```kotlin
val foo = """
    some text
   """.trimIndent()
```

## Why does ktlint discourage certain comment locations?

Kotlin has three different type of comments. Although the KDoc and the block comment look similar in code, their internal PSI structure is different. The EOL comment is yet very different.

In Kotlin, it is possible to insert a comment everywhere. It is very challenging, and time-consuming, to make each rule fully resilient for each possible comment location, even in case such locations will (almost) never by used.

For example, in sample below it is unclear whether the comment applies to the `if` block, or to the `else` block without interpreting the comment itself.

=== "[:material-heart-off-outline:](#) Unclear comment"

    ```kotlin
    if (someCondition) {
        doTrue()
    } // comment
    else {
        doFalse()
    }
    ```

=== "[:material-heart:](#) Clear comment"

    ```kotlin
    if (someCondition) {
        doTrue()
    } else { 
        // comment
        doFalse()
    }
    ```

In other cases, a comment location is more widely used but semantically still incorrect. For example, in sample below the EOL comment is placed after the comma, but it obviously is related to the part before the comma:

=== "[:material-heart-off-outline:](#) Unclear comment"

    ```kotlin
    fun fooBar(
        foo: Foo, // foo-comment
        bar: Bar, // bar-comment
    ) {}
    ```

=== "[:material-heart:](#) Clear comment"

    ```kotlin
    fun fooBar(
        // foo-comment
        foo: Foo,
        // bar-comment
        bar: Bar,
    ) {}
    ```

By forbidding certain comment locations, the logic in the rules becomes a bit easier.

## Can I have my own rules on top of ktlint?

Absolutely, "no configuration" doesn't mean "no extensibility". You can add your own ruleset(s) to discover potential bugs, check for antipatterns, etc.

See [adding a custom rule set](api/custom-rule-set.md) for more information.

## How do I suppress errors for a line/block/file?

!!! tip
    Suppressing a `ktlint` violation is meant primarily as an escape latch for the rare cases when **ktlint** is not able to produce the correct result. Please report any such instances using [GitHub Issues](https://github.com/ktlint/ktlint/issues).

To disable a specific rule you'll need the fully qualified rule identifier. This identifier is displayed at the end of the lint error. In case your code was autocorrected, you need to revert the code and run the `lint` task instead of the `format` to find the rule identifier.

As of Ktlint 0.50, an error can only be suppressed using @Suppress or @SuppressWarnings annotations

!!! warning
    Import statements can not be annotated in Kotlin. The rules related to import statements can be suppressed with file annotations only.

=== "[:material-heart:](#) Allowed"

    ```kotlin
    // Suppressing all rules for the entire file
    @file:Suppress("ktlint")

    // Suppressing specific rules for the entire file
    // Rules related to import statements can only be suppressed using file annotations
    @file:Suppress("ktlint:standard:no-wildcard-imports", "ktlint:custom-rule-set-id:custom-rule-id")

    // Suppress all rules for the annotated construct
    @Suppress("ktlint")
    class Foo {}

    // Suppress a single rule (with id 'rule-id', defined in rule set with id 'rule-set-id') in the scope of the annotated construct
    @Suppress("ktlint:rule-set-id:rule-id")
    class Foo {}
    ```

## Why is `.editorconfig` property `disabled_rules` deprecated and how do I resolve this?

The `.editorconfig` properties `disabled_rules` and `ktlint_disabled_rules` are deprecated as of KtLint version `0.48` and are removed in version `0.49`. Those properties contain a comma separated list of rules which are disabled. Using a comma separated list of values has some disadvantages.

A big disadvantage is that it is not possible to override the property partially in an `.editorconfig` file in a subpackage. Another disadvantage is that it is not possible to express explicitly that a rule is enabled. Lastly, (qualified) rule ids can be 20 characters or longer, which makes a list with multiple entries hard to read.

Starting with KtLint `0.48` entire rule sets and individual rules can be disabled / enabled with a separate property per rule (set). Examples:
```editorconfig
ktlint_standard = disabled # Disable all rules from the `standard` rule set provided by KtLint
ktlint_standard_final-newline = enabled # Enables the `final-newline` rule in the `standard` rule set provided by KtLint
ktlint_experimental = enabled # Enable rules marked as experimental for all rule sets that are enabled
ktlint_standard_some-experimental-rule = disabled # Disables the (experimental) `some-experimental-rule` in the `standard` rule set provided by KtLint
ktlint_custom-rule-set = enabled # Enable all rules in the `custom-rule-set` rule set (not provided by KtLint)
ktlint_custom-rule-set_custom-rule = disabled # Disables the `custom-rule` rule in the `custom-rule-set` rule set (not provided by KtLint)
```

!!! note
    All rules from the `standard` and custom rule sets are *enabled* by default and can optionally be disabled in the `.editorconfig`. All `experimental` rules are *disabled* by default and can optionally be enabled in the `.editorconfig`.

!!! note
    The *rule* properties are applied after applying the *rule set* properties and take precedence. So if a rule set is disabled but a specific rule of that rule set is enabled, then the rule will be executed.  

## Why is wildcard import `java.util.*` not reported by the `no-wildcard-imports` rule?

The `no-wildcard-imports` rule forbids wildcard imports, except for imports defined in `.editorconfig` property `ij_kotlin_packages_to_use_import_on_demand`. If this property is not explicitly set, it allows wildcards imports like `java.util.*` by default to keep in sync with IntelliJ IDEA behavior.

## Can a new toggle be added to optionally (enable/disable) format code in a particular way?

Ktlint can be configured by enabling and disabling rules. Some rules can be configured in more details with additional `.editorconfig` properties. Regularly, a new configuration option is requested to modify behavior in existing rules.

Ktlint is restrictive with adding additional configuration settings to customize behavior in rules. Each configuration option that Ktlint offers comes with complexity that has to be maintained by only a couple of maintainers. As of that, we cannot provide tens or even hundreds of such options.

Less configuration options also means less discussions in teams about settings to use. Unfortunately this means that you cannot tweak Ktlint exactly to the format you prefer.

!!! tip
    Any idea for a new configuration option is valuable. Please create an issue for it so that it can be considered to incorporate it in Ktlint.

## Can I use KtLint to directly format the code I'm generating with KotlinPoet?

Yes, it is possible to use KtLint to directly format the code generated with KotlinPoet. 
To do so, you must include the dependencies `com.pinterest.ktlint:ktlint-core` and `com.pinterest.ktlint:ktlint-ruleset-standard` in your Gradle/Maven project.

!!! warning
    Do not include the dependency `com.pinterest.ktlint:ktlint-cli` as that would import the entire ktlint project including unwanted dependencies. Besides a much bigger artifact, it might also result in problems regarding logging.

To format the output of KotlinPoet with KtLint, you can use the following snippet:

```kotlin
val ruleProviders = buildSet {
  ServiceLoader
      .load(RuleSetProviderV2::class.java)
      .flatMapTo(this) { it.getRuleProviders() }
}
val ktLintRuleEngine = KtLintRuleEngine(
  ruleProviders = ruleProviders,
  editorConfigDefaults = EditorConfigDefaults.load(EDITORCONFIG_PATH),
)
ktLintRuleEngine.format(outputDir.toPath())
```
Here, outputDir refers to the directory of the generated files by KotlinPoet, ktLintRuleEngine is an instance of KtLint rule engine.

It is also possible to format file-by-file the output of KotlinPoet if you write your `FileSpec` to a `StringBuilder()`, instead of a `File`, 
and send the generated code as `String` to KtLint inside a `CodeSnippet`:
```kotlin
kotlinFile.writeText(
  ktLintRuleEngine.format(
    Code.CodeSnippet(
      stringBuilder.toString()
    )
  )
)
```

# Are formatter tags respected?

As of version `0.49.x` the formatter tags of IntelliJ IDEA are respected. By default, those formatter tags are disabled. The formatter tags can be enabled with `.editorconfig` properties below:
```editorconfig
ij_formatter_tags_enabled = true # Defaults to 'false'
ij_formatter_off_tag = some-custom-off-tag # Defaults to '@formatter:off'
ij_formatter_on_tag = some-custom-on-tag # Defaults to '@formatter:on'
```

When enabled, the ktlint rule checking is disabled for all code surrounded by the formatter tags.

# How do I disable ktlint for generated code?

Running ktlint on generated code is not useful. Fixing lint and format errors on generated code is a waste of time as errors will be re-introduced once that code is generated again. Given that generated code is located in a separate directory, you can disable ktlint for such directory by adding a glob for that directory:
```editorconfig
[some/path/to/generated/code/**/*]
ktlint = disabled
```

!!! warning
    The `ec4j` library used by ktlint does not seem to work with globs starting with `**` followed by a chain of multiple directories (for example `**/path/to/generated/**/*`). But both `some/path/to/generated/**/*` and `**/generated/**/*` work fine.


