Ktlint has been moved away from the Pinterest GitHub organization towards its own [Ktlint GitHub organization](https://github.com/ktlint). This results in several breaking changes which are documented below.

!!! tip
    Please [create an issue](https://github.com/ktlint/ktlint/issues/new/choose) if you run into problems when migrating your Ktlint integration or your custom ruleset, so that this guide can be updated.

Ktlint Integrators are strongly encouraged to upgrade to Ktlint `2.x` as soon possible, but also to keep backward compatibility with Ktlint `1.x` rulesets when relevant. At the time of writing of this guide, the Ktlint CLI `2.x and higher`, and Ktlint IntelliJ Plugin `0.31.0 and higher`, are both compatible with:

* rulesets provided by Ktlint `1.3.x and higher`
* custom rules implementing the `com.pinterest.ktlint.rule.engine.core.api.RuleAutocorrectApproveHandler` interface.

## Prepare

Before starting the migration to Ktlint 2.x it is advised to upgrade to Ktlint `1.8.0` version, and resolve all deprecation warnings. Especially, a lot of extensions functions provided via the `ASTNodeExtensions` file are removed in Ktlint 2.x. By first migrating the deprecated extension functions to their new counterparts in Ktlint `1.8.0` the migration to the new package names becomes easier.

## Change Maven Coordinates

Ktlint 2.x is published with changed maven coordinates. The Maven group id has changed from `com.pinterest.ktlint` to `io.github.ktlint.core`. Artifact id's of the Ktlint modules are not changed. Examples:
* `com.pinterest.ktlint:ktlint-rule-engine` should be replaced with `io.github.ktlint.core:ktlint-rule-engine`
* `com.pinterest.ktlint:ktlint-cli-ruleset-core` should be replaced with `io.github.ktlint-cli-ruleset-core`

## Change package names

All package names starting with `com.pinterest.ktlint` have been changed and now start with `io.github.ktlint.core`. A simple search and replace should resolve most problems.

This substitution results in several compilation errors as some classes are not provided in the `io.github.ktlint.core` namespace. See below for how to resolve the compilation errors.

### Rule extends non existent `io.github.ktlint.core...Rule`

The `com.pinterest.ktlint.rule.engine.core.api.Rule` class has been replaced with the `io.github.ktlint.core.rule.engine.core.api.RuleV2` class. 

If the custom rule implemented the `com.pinterest.ktlint.rule.engine.core.api.RuleAutocorrectApproveHandler` interface then remove this interface from the class signature. The functions provided by this interface are now provided directly by the `RuleV2` class.

If the custom rule did not yet implement the `com.pinterest.ktlint.rule.engine.core.api.RuleAutocorrectApproveHandler` interface then you will need to update the  signatures of functions `beforeVisitChildNodes` and `afterVisitChildNodes`.

<table>
<tr>
<td> 

```kotlin title="Old signature (not supported in Ktlint 2.x)"
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

```kotlin title="New signature"
public fun beforeVisitChildNodes(
    node: ASTNode,

    
    
    
    
    
    )
```

</td>
</tr>
</table>

The `emit` parameter is no longer be passed to the `beforeVisitChildNodes` and `afterVisitChildNodes` functions. Instead, the `RuleV2` base class now provides an `emit` function with signature:
```kotlin title="New signature"
public fun emit(
    offset: Int,
    errorMessage: String,
    autoCorrect: () -> Unit = AUTOCORRECT_NOT_IMPLEMENTED,
)
```


This function internally calls the `emit` lambda provided by the API consumer to decide whether the violation should be autocorrected, and only executes the autocorrect block when allowed.

In case a `LintError` is detected and can be autocorrected, pass the autocorrect logic as a trailing lambda:

```kotlin
emit(node.startOffset, "some detail message") {
    // Autocorrect the LintError
}
```

In case the `LintError` cannot be autocorrected, omit the lambda:
```kotlin
emit(node.startOffset, "some detail message")
```

### Rule set provider extends non existent `io.github.ktlint.core...RuleSetProviderV3`

The `com.pinterest.ktlint.cli.ruleset.core.api.RuleSetProviderV3` class has been replaced with the `io.github.ktlint.core.cli.ruleset.core.api.RuleSetV2Provider`. Its function `getRuleProviders` should return a set of `io.github.ktlint.core.rule.engine.core.api.RuleV2Provider`.

Example:
```kotlin
import io.github.ktlint.core.cli.ruleset.core.api.RuleSetV2Provider
import io.github.ktlint.core.rule.engine.core.api.RuleV2Provider
import io.github.ktlint.core.rule.engine.core.api.RuleSetId

internal val CUSTOM_RULE_SET_ID = "custom-rule-set-id"

class CustomRuleSetProvider : RuleSetV2Provider(RuleSetId(CUSTOM_RULE_SET_ID)) {
    override fun getRuleProviders(): Set<RuleV2Provider> =
        setOf(
            RuleV2Provider { NoVarRule() },
            RuleV2Provider { EmptyCollectionInitializationRule() },
        )
}
```

## Backward compatibility `com.pinterest.ktlint`

However, above cannot be applied fully for Ktlint API Integrators that need to support dynamic loading of rulesets extend from classes living in the `com.pinterest.ktlint` namespace.

The new module `com.pinterest.ktlint:ktlint-com-pinterest-backward-compatibility` contains the `1.8.0` version of classes that are needed to load rulesets extended from class `RuleSetProviderV3`. Rules loaded via the `RuleSetProviderV3` should be transformed to the Ktlint 2.x format using the `com.pinterest.ktlint.rule.engine.core.api.RuleProvider.toRuleV2Provider`.  

!!! Warning
    Do not use the backward compatibility module to avoid migrating your custom ruleset. This module will be removed in a future Ktlint version without further notice. Once removed, your ruleset will not work anymore in newer versions of Ktlint CLI, Ktlint IntelliJ Plugin, Ktlint Gradle Plugin, etc. 
