Ktlint uses a limited set of `.editorconfig` properties for additional configuration. A sensible default value is provided for each property when not explicitly defined. Properties can be overridden, provided they are specified under `[*.{kt,kts}]`. Ktlint uses some properties defined by [.editorconfig](https://editorconfig.org/), IntelliJ IDEA and custom properties.

!!! danger

    Unfortunately, [IntelliJ IDEA](https://www.jetbrains.com/idea/) has an [autoformat issue regarding `.editorconfig`](https://youtrack.jetbrains.com/issue/IDEA-242506). Due to this error, an additional space is added between glob statements, resulting in `[*{kt, kts}]` instead of `[*{kt,kts}]`. The `.editorconfig` library used by `ktlint` [ignores sections after encountering a space in the list](https://github.com/editorconfig/editorconfig/issues/148). As a result, the rule is not applied on all files as documented in the [original ktlint issue](https://github.com/ktlint/ktlint/issues/762).

## Code style

By default, the `ktlint_official` code style is applied. Alternatively, the code style can be set to `intellij_idea` or `android_studio`.

```ini
[*.{kt,kts}]
ktlint_code_style = ktlint_official
```

## Disable rule(s)

Rule sets and individual rules can be disabled / enabled with a separate property per rule (set).

All rules in a rule set can be enabled or disabled with a rule set property. The name of the rule set property consists of the `ktlint_` prefix followed by the rule set id. Examples:
```editorconfig
[*.{kt,kts}]
ktlint_standard = disabled # Disable all rules from the `standard` rule set provided by KtLint
ktlint_experimental = enabled # Enable all `experimental` rules from all rule sets provided by KtLint or other rule providers
ktlint_custom-rule-set = enabled # Enable all rules in the `custom-rule-set` rule set (not provided by KtLint)
```

Rules that are marked as experimental will not run unless explicitly enabled:
```editorconfig
[*.{kt,kts}]
ktlint_experimental = enabled # Enable rules marked as experimental for all rule sets that are enabled
```

An individual rule can be enabled or disabled with a rule property. The name of the rule property consists of the `ktlint_` prefix followed by the rule set id followed by a `_` and the rule id. Examples:
```editorconfig
[*.{kt,kts}]
ktlint_standard_final-newline = disabled # Disables the `final-newline` rule provided by KtLint
ktlint_standard_some-experimental-rule = enabled # Enables the (experimental) `some-experimental-rule` in the `standard` rule set provided by KtLint
ktlint_custom-rule-set_custom-rule = disabled # Disables the `custom-rule` rule in the `custom-rule-set` rule set (not provided by KtLint)
```

!!! note
    The *rule* properties are applied after applying the *rule set* properties and take precedence. So if a rule set is disabled but a specific rule of that rule set is enabled, then the rule will be executed.

## Rule specific configuration settings

The configuration settings below are used to configure the behavior of a specific rule. As of that, those settings only take effect when the corresponding rule is enabled. See the description of the specific rule for more information about the setting.

| Configuration setting                                                                     | Rule                                                                                 |
|:------------------------------------------------------------------------------------------|:-------------------------------------------------------------------------------------|
| ij_kotlin_allow_trailing_comma                                                            | [trailing-comma-on-declaration-site](standard.md#trailing-comma-on-declaration-site) |
| ij_kotlin_allow_trailing_comma_on_call_site                                               | [trailing-comma-on-call-site](standard.md#trailing-comma-on-call-site)               |
| ij_kotlin_imports_layout                                                                  | [import-ordering](standard.md#import-ordering)                                       |
| ij_kotlin_packages_to_use_import_on_demand                                                | [no-wildcard-imports](standard.md#no-wildcard-imports)                               |
| indent_size                                                                               | [indent](standard.md#indentation)                                                    |                                                                                       |
| indent_style                                                                              | [indent](standard.md#indentation)                                                    |                                                                                       |
|
| insert_final_newline                                                                      | [final-newline](standard.md#final-newline)                                           |                                                                                       |
| ktlint_chain_method_rule_force_multiline_when_chain_operator_count_greater_or_equal_than  | [chain-method-continuation](standard.md#chain-method-continuation)                   |
| ktlint_class_signature_rule_force_multiline_when_parameter_count_greater_or_equal_than    | [class-signature](standard.md#class-signature)                                       |
| ktlint_ignore_back_ticked_identifier                                                      | [max-line-length](standard.md#max-line-length)                                       |
| ktlint_function_naming_ignore_when_annotated_with                                         | [function-naming](standard.md#function-naming)                                       |
| ktlint_function_signature_body_expression_wrapping                                        | [function-signature](standard.md#function-signature)                                 |
| ktlint_function_signature_rule_force_multiline_when_parameter_count_greater_or_equal_than | [function-signature](standard.md#function-signature)                                 |
| max_line_length                                                                           | [max-line-length](standard.md#max-line-length) and several other rules               |

## Overriding Editorconfig properties for specific directories

You can [override](https://editorconfig.org/#file-format-details) properties for specific directories inside your project:
```ini
[*.{kt,kts}]
ktlint_standard_import-ordering = disabled

[api/*.{kt,kts}]
ktlint_standard_indent = disabled
```

Note: In the example above the `import-ordering` rule is disabled for *all* packages, including the `api` sub package. Next to this, the `indent` rule is disabled for the `api` package and its sub packages.
