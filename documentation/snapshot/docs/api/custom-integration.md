# Custom integration

## Ktlint Rule Engine

The `Ktlint Rule Engine` is the central entry point for custom integrations with the `Ktlint API`. See [basic API Consumer](https://github.com/ktlint/ktlint/blob/master/ktlint-api-consumer/src/main/kotlin/com/example/ktlint/api/consumer/KtlintApiConsumer.kt) for a basic example on how to invoke the `Ktlint Rule Engine`. This example also explains how the logging of the `Ktlint Rule Engine` can be configured to your needs.

The `KtLintRuleEngine` instance only needs to be created once for the entire lifetime of your application. Reusing the same instance results in better performance due to caching.

```kotlin title="Creating the KtLintRuleEngine"
val ktLintRuleEngine =
  KtLintRuleEngine(
    ruleProviders = KTLINT_API_CONSUMER_RULE_PROVIDERS,
  )
```

### Rule provider

The `KtLintRuleEngine` must be configured with at least one `RuleProvider`. A `RuleProvider` is a lambda which upon request of the `KtLintRuleEngine` provides a new instance of a specific rule. You can either provide any of the standard rules provided by KtLint, or your own custom rules, or a combination of both.
```kotlin title="Creating a set of RuleProviders"
val KTLINT_API_CONSUMER_RULE_PROVIDERS =
  setOf(
    // Can provide custom rules
    RuleProvider { NoVarRule() },
    // but also reuse rules from KtLint rulesets
    RuleProvider { IndentationRule() },
  )
```

### Editor config: defaults & overrides

When linting and formatting files, the `KtlintRuleEngine` takes the `.editorconfig` file(s) into account which are found on the path to the file. A property which is specified in the `editorConfigOverride` property of the `KtLintRuleEngine` takes precedence above the value of that same property in the `.editorconfig` file. The `editorConfigDefaults` property of the `KtLintRuleEngine` can be used to specify the fallback values for properties in case that property is not defined in the `.editorconfig` file (or in the `editorConfigOverride` property).

```kotlin title="Specifying the editorConfigOverride"
val ktLintRuleEngine =
  KtLintRuleEngine(
    ruleProviders = KTLINT_API_CONSUMER_RULE_PROVIDERS,
    editorConfigOverride = EditorConfigOverride.from(
      INDENT_STYLE_PROPERTY to IndentConfig.IndentStyle.SPACE,
      INDENT_SIZE_PROPERTY to 4
    )
  )
```

The `editorConfigOverride` property takes an `EditorConfigProperty` as key. KtLint defines several such properties, but they can also be defined as part of a custom rule.

The `editorConfigDefaults` property is more cumbersome to define as it is based directly on the data format of the `ec4j` library which is used for parsing the `.editorconfig` file.

The defaults can be loaded from a path or a directory. If a path to a file is specified, the name of the file does not necessarily have to end with `.editorconfig`. If a path to a directory is specified, the directory should contain a file with name `.editorconfig`. Note that the `propertyTypes` have to be derived from the same collection of rule providers that are specified in the `ruleProviders` property of the `KtLintRuleEngine`.

```kotlin title="Specifying the editorConfigDefaults using an '.editorconfig' file"
val ktLintRuleEngine =
  KtLintRuleEngine(
    ruleProviders = KTLINT_API_CONSUMER_RULE_PROVIDERS,
    editorConfigDefaults = EditorConfigDefaults.load(
      path = Paths.get("/some/path/to/editorconfig/file/or/directory"),
      propertyTypes = KTLINT_API_CONSUMER_RULE_PROVIDERS.propertyTypes(),
    )
  )
```
If you want to include all RuleProviders of the Ktlint project than you can easily retrieve the collection using `StandardRuleSetProvider().getRuleProviders()`.

The `EditorConfigDefaults` property can also be specified programmatically as is shown below:

```kotlin title="Specifying the editorConfigDefaults programmatically"
val ktLintRuleEngine =
  KtLintRuleEngine(
    ruleProviders = KTLINT_API_CONSUMER_RULE_PROVIDERS,
    editorConfigDefaults = EditorConfigDefaults(
      org.ec4j.core.model.EditorConfig
        .builder()
        // .. add relevant properties
        .build()
    )
  )
```

### Lint & format

Once the `KtLintRuleEngine` has been defined, it is ready to be invoked for code that has to be linted or formatted. The `lint` and `format` functions take a `Code` instance as parameter. Such an instance can either be created from a file
```kotlin title="Code from file"
val code = Code.fromFile(
    File("/some/path/to/file")
)
```
or a code snippet (set `script` to `true` to handle the snippet as Kotlin script):
```kotlin title="Code from snippet"
val code = Code.fromSnippet(
    """
    val code = "some-code"
    """.trimIndent()
)
```

The `lint` function is invoked with an optional lambda. Once linting is complete, the lambda will be called for each `LintError` which is found.
```kotlin title="Invoking lint"
ktLintRuleEngine
    .lint(code) { lintError ->
        // handle
    }
```

The `format` function is invoked with a lambda. The lambda is called for each `LintError` which is found. If the `LintError` can be autocorrected, the return value of the lambda instructs the rule whether this specific `LintError` is to be autocorrected, or not. If the `LintError` can not be autocorrected, the return result of the lambda is ignored. The formatted code is returned as result of the function.

The new `format` function allows the API Consumer to decide which LintError is to be autocorrected, or not. This is most interesting for API Consumers that let their user interactively decide per `LintError` how it has to be handled. For example see the `ktlint-intellij-plugin` which in 'manual' mode displays all lint violations, which allows the user to decide which `LintError` is to be autocorrected.

!!! note
    The difference with the legacy version of the `format` is subtle. It takes two parameters (a `LintError` and `Boolean` denoting whether the `LintError` is corrected), and it does not return a value.

```kotlin title="Invoke format (preferred, starting from Ktlint 1.3)"
val formattedCode =
    ktLintRuleEngine
      .format(code) { lintError ->
          if (lintError.canBeAutoCorrected) {
              // Return AutocorrectDecision.ALLOW_AUTOCORRECT to execute the autocorrect of this lintError if this is supported by the rule.
              // Return AutocorrectDecision.NO_AUTOCORRECT if the LintError should not be corrected even if is supported by the rule.
          } else {
              // In case the LintError can not be autocorrected, the return value of the lambda will be ignored.
              // For clarity reasons it is advised to return AutocorrectDecision.NO_AUTOCORRECT in case the LintError can not be autocorrected.
              AutocorrectDecision.NO_AUTOCORRECT
          }
      }
```

!!! warning
    Rules need to implement the interface `RuleAutocorrectApproveHandler` in order to let the API Consumer decide whether a `LintError` is to be autocorrected, or not. This interface is implemented for all rules provided via the Ktlint project starting from version 1.3. However, external rulesets may not have implemented this interface on their rulesets though. Contact the maintainer of such a ruleset to implement this interface.

The (legacy) `format` function is invoked with an optional lambda. Once formatting is complete, the lambda will be called for each `LintError` which is found. The (legacy) `format` function fixes all `LintErrors` for which an autocorrect is available. The formatted code is returned as result of the function.

```kotlin title="Invoke format (deprecated as of Ktlint 1.3, will be removed in Ktlint 2.0)"
// Up until Ktlint 1.2.1 the format was invoked with a lambda having two parameters and not returning a result. This function will be removed in Ktlint 2.0 
val formattedCode =
    ktLintRuleEngine
        .format(code) { lintError, corrected ->
            // handle
        }
```

### Rule & RuleAutocorrectApproveHandler

!!! note
    Providers of custom rules are strongly encouraged to implement `RuleAutocorrectApproveHandler` interface as described below. The `ktlint-intellij-plugin`, which will be updated soon after the 1.3 release of Ktlint, make use of this new functionality. If your ruleset is used by users of the plugin, it is very likely that they want to be able to autocorrect individual `LintErrors` or to format a block of code (e.g. a selection) in a file. This functionality will only be available for rules that have implemented this interface.

In Ktlint 1.3 the `RuleAutocorrectApproveHandler` interface is added. This interface adds the ability that the API Consumer decides per `LintError` whether it needs to autocorrected, or not. In Ktlint 2.0 the methods `beforeVisitChildNodes` and `afterVisitChildNodes` of the `Rule` class will be replaced with the new versions which are now added to the `RuleAutocorrectApproveHandler` interface as is shown below (the signature for `afterVisitChildNodes` is changed similarly):

<table>
<tr>
<td> 

```kotlin title="Deprecated signature in `Rule` class"
public open fun beforeVisitChildNodes(
    node: ASTNode,
    autoCorrect: Boolean,
    emit: (
        offset: Int,
        errorMessage: String,
        canBeAutoCorrected: Boolean
    ) -> Unit,
)  
```

</td>
<td>

```kotlin title="New signature in `RuleAutocorrectApproveHandler` interface"
public fun beforeVisitChildNodes(
    node: ASTNode,
    emit: (
        offset: Int,
        errorMessage: String,
        canBeAutoCorrected: Boolean
    ) -> AutocorrectDecision,
)
```

</td>
</tr>
</table>

The `autoCorrect` parameter is no longer passed to the method. Instead, the `emit` lambda now returns the value `AutocorrectDecision.ALLOW_AUTOCORRECT` or `AutocorrectDecision.NO_AUTOCORRECT`.

In case a `LintError` is detected, and can be autocorrected, the `LintError` can be processed as shown below:

```kotlin
emit(node.startOffset, "some detail message", true)
   .ifAutocorrectAllowed {
       // Autocorrect the LintError
   }
```

In case the `LintError` can not be autocorrected, if suffices to emit the violation only: 
```kotlin
emit(node.startOffset, "some detail message", false)
```

## Logging

Ktlint uses the `io.github.oshai:kotlin-logging` which is a `slf4j` wrapper. As API consumer you can choose which logging framework you want to use and configure that framework to your exact needs. The [basic API Consumer](https://github.com/ktlint/ktlint/blob/master/ktlint-api-consumer/src/main/kotlin/com/example/ktlint/api/consumer/KtlintApiConsumer.kt) contains an example with `org.slf4j:slf4j-simple` as logging provider and a customized configuration which shows logging at `DEBUG` level for all classes except one specific class which only displays logging at `WARN` level.
