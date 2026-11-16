!!! important
    Up and until Ktlint version `0.47`, experimental were located in a separate `experimental` rule set. As of Ktlint version `0.48`, each rule set can optionally define experimental rules.

All experimental rules described below are part of the `standard` rule set of Ktlint. To enable all experimental rules (from all rule sets), set `editorconfig` property below:
```ini
[*.{kt,kts}]
ktlint_experimental=enabled
```
Also see [enable/disable specific rules](configuration-ktlint.md#disable-rules).

## Blank line before file annotation

Requires a blank line before the file annotation unless that file annotation is on the first line of the file.

=== "[:material-heart:](#) Ktlint"

    ```kotlin
    /*
     * Copyright comment
     */

    @file:Foo
    ```

=== "[:material-heart-off-outline:](#) Disallowed"

    ```kotlin
    /*
     * Copyright comment
     */
    @file:Foo
    ```

Rule id: `standard:blank-line-before-file-annotation`

Suppress or disable rule (1)
{ .annotate }

1. Suppress rule in code with annotation below:
    ```kotlin
    @Suppress("ktlint:standard:blank-line-before-file-annotation")
    ```
   Enable rule via `.editorconfig`
    ```editorconfig
    ktlint_standard_blank-line-before-file-annotation = enabled
    ```
   Disable rule via `.editorconfig`
    ```editorconfig
    ktlint_standard_blank-line-before-file-annotation = disabled
    ```

!!! Note
    This rule is only run when `ktlint_code_style` is set to `ktlint_official` or `android_studio` or when the rule is enabled explicitly.

## Blank line before imports

Requires a blank line before the imports unless the imports starts at the first line of the file.

=== "[:material-heart:](#) Ktlint"

    ```kotlin
    package bar

    import foo
    ```

=== "[:material-heart-off-outline:](#) Disallowed"

    ```kotlin
    package bar
    import foo
    ```

Rule id: `standard:blank-line-before-imports`

Suppress or disable rule (1)
{ .annotate }

1. Suppress rule in code with annotation below:
    ```kotlin
    @Suppress("ktlint:standard:blank-line-before-imports")
    ```
   Enable rule via `.editorconfig`
    ```editorconfig
    ktlint_standard_blank-line-before-imports = enabled
    ```
   Disable rule via `.editorconfig`
    ```editorconfig
    ktlint_standard_blank-line-before-imports = disabled
    ```

!!! Note
    This rule is only run when `ktlint_code_style` is set to `ktlint_official` or `android_studio` or when the rule is enabled explicitly.

## Blank line before package

Requires a blank line before the package statement unless the package statement is the first line of the file.

=== "[:material-heart:](#) Ktlint"

    ```kotlin
    @file:Bar

    package foo
    ```

=== "[:material-heart-off-outline:](#) Disallowed"

    ```kotlin
    @file:Bar
    package foo
    ```

Rule id: `standard:blank-line-before-package`

Suppress or disable rule (1)
{ .annotate }

1. Suppress rule in code with annotation below:
    ```kotlin
    @Suppress("ktlint:standard:blank-line-before-package")
    ```
   Enable rule via `.editorconfig`
    ```editorconfig
    ktlint_standard_blank-line-before-package = enabled
    ```
   Disable rule via `.editorconfig`
    ```editorconfig
    ktlint_standard_blank-line-before-package = disabled
    ```

!!! Note
    This rule is only run when `ktlint_code_style` is set to `ktlint_official` or `android_studio` or when the rule is enabled explicitly.

## Call expression wrapping

In case a call expression does not fit on the line, the lambda expression, and/or the value argument list after a reference expression are wrapped. 

=== "[:material-heart:](#) Ktlint"

    ```kotlin
    // Assume that the last allowed
    // character is at the X character
    // on the right                  X
    val foo1 = bar() {
        "some message"
    }
    val foo2 = bar(
        "foobarrrrrrrrrrrr"
    ) { "some message" }
    val foo3 = bar(
        "foobarrrrrrrrrrrr"
    ) {
        "some longgggggggggg message"
    }
    ```

=== "[:material-heart-off-outline:](#) Disallowed"

    ```kotlin
    // Assume that the last allowed
    // character is at the X character
    // on the right                  X
    val foo1 = bar() { "some message" }
    val foo2 = bar("foobarrrrrrrrrrrr") { "some message" }
    val foo3 = bar("foobarrrrrrrrrrrr") { "some longgggggggggg message" }
    ```

Rule id: `standard:call-expression-wrapping`

Suppress or disable rule (1)
{ .annotate }

1. Suppress rule in code with annotation below:
    ```kotlin
    @Suppress("ktlint:standard:call-expression-wrapping")
    ```
   Enable rule via `.editorconfig`
    ```editorconfig
    ktlint_standard_call-expression-wrapping = enabled
    ```
   Disable rule via `.editorconfig`
    ```editorconfig
    ktlint_standard_call-expression-wrapping = disabled
    ```

## Expression operand wrapping

Wraps each operand in a multiline expression to a separate line.

=== "[:material-heart:](#) Ktlint"

    ```kotlin
    val foo1 = bar || baz
    val foo2 =
        bar1 ||
            bar2 ||
            baz1 ||
            (baz2 && baz3)
    val foo3 = bar + baz
    val foo4 =
        bar1 -
            bar2 -
            baz1 -
            (baz2 * baz3)
    ```

=== "[:material-heart-off-outline:](#) Disallowed"

    ```kotlin
    val foo =
      multiLineOperand(
          "bar"
      ) || baz
    if (bar1 || bar2 ||
        baz1 || (baz2 && baz3)
    ) {
       // do something
    }
    ```

Rule id: `standard:expression-operand-wrapping`

Suppress or disable rule (1)
{ .annotate }

1. Suppress rule in code with annotation below:
    ```kotlin
    @Suppress("ktlint:standard:expression-operand-wrapping")
    ```
   Enable rule via `.editorconfig`
    ```editorconfig
    ktlint_standard_expression-operand-wrapping = enabled
    ```
   Disable rule via `.editorconfig`
    ```editorconfig
    ktlint_standard_expression-operand-wrapping = disabled
    ```

## KDoc delimiter

A single-line KDoc comment starts with `/** ` and ends with ` */`. A multi-line KDoc comment starts with a line containing only `/**`, ends with a line containing only `*/`, and every other line starts with a leading asterisk indented one space more than the opening `/**`, followed by a single space and the content of that line, or by nothing in case the line is empty. The line directly after the opening delimiter, and the line directly before the closing delimiter, must not be empty. An empty (or blank) KDoc comment is not allowed.

Autocorrect is only applied to whitespace and delimiters, and only when doing so will not change the meaning, or the visual indentation, of the actual content of the KDoc comment. Most notably, a continuation line which is missing its leading asterisk is always reported without being autocorrected, as inserting the asterisk could change the intended visual indentation of that content.

=== "[:material-heart:](#) Ktlint"

    ```kotlin
    /** A group of *members* */

    /**
     * A group of *members*.
     *
     * This class has no useful logic; it's just a documentation example.
     *
     * @param T the type of a member in this group.
     * @property name the name of this group.
     * @constructor Creates an empty group.
     */
    ```
=== "[:material-heart-off-outline:](#) Disallowed"

    ```kotlin
    /***
     * A group of *members*.
     */

    /**A group of *members* */

    /** A group of *members*.
     * This class has no useful logic; it's just a documentation example.
     */

    /**
     * A group of *members*.
       This class has no useful logic; it's just a documentation example.
     */

    /**
     * A group of *members*.
     * This class has no useful logic; it's just a documentation example.
    */

    /**
     *
     * A group of *members*.
     */

    /***/
    ```

!!! note
    This rule is only run when `ktlint_experimental` is enabled and `ktlint_code_style` is set to `ktlint_official`, or when the rule is enabled explicitly.

Rule id: `standard:kdoc-delimiter`

Suppress or disable rule (1)
{ .annotate }

1. Suppress rule in code with annotation below:
    ```kotlin
    @Suppress("ktlint:standard:kdoc-delimiter")
    ```
   Enable rule via `.editorconfig`
    ```editorconfig
    ktlint_standard_kdoc-delimiter = enabled
    ```
   Disable rule via `.editorconfig`
    ```editorconfig
    ktlint_standard_kdoc-delimiter = disabled
    ```

## Lambda return

Do not use a labeled return for the last statement in a lambda if that label refers to the lambda itself.

=== "[:material-heart:](#) Ktlint"

    ```kotlin
    val foo =
        "Foo"
            .let outer@{ foo ->
                if (foo != "Foo") {
                    return@outer "$foo is not expected input"
                }
                foo
                    .let { "$it was a" }
                    .let secondLet@{
                        if (foo != "Foo") {
                            return@secondLet "$foo is not expected input"
                        }
                        "$it success"
                    }.let { return@outer "$it (map after let)" }
                // This is unreachable code due to "return@outer" in let above.
                "$foo was a failure"
            }.let { "$it (let after also)" }
    ```

=== "[:material-heart-off-outline:](#) Disallowed"

    ```kotlin
    val foo =
        "Foo"
            .let outer@{ foo ->
                if (foo != "Foo") {
                    return@outer "$foo is not expected input"
                }
                foo
                    .let { return@let "$it was a" }
                    .let secondLet@{
                        if (foo != "Foo") {
                            return@secondLet "$foo is not expected input"
                        }
                        return@secondLet "$it success"
                    }.let { return@outer "$it (map after let)" }
                // This is unreachable code due to "return@outer" in let above.
                return@outer "$foo was a failure"
            }.let { return@let "$it (let after also)" }
    ```

Rule id: `standard:lambda-return`

Suppress or disable rule (1)
{ .annotate }

1. Suppress rule in code with annotation below:
    ```kotlin
    @Suppress("ktlint:standard:lambda-return")
    ```
   Enable rule via `.editorconfig`
    ```editorconfig
    ktlint_standard_lambda-return = enabled
    ```
   Disable rule via `.editorconfig`
    ```editorconfig
    ktlint_standard_lambda-return = disabled
    ```

## No blank line at start of file

Don't allow whitespace at the start of the file.

=== "[:material-heart:](#) Ktlint"

    ```kotlin 
    package foo
    ```

=== "[:material-heart-off-outline:](#) Disallowed"

    ```kotlin
    
    package foo
    ```

Rule id: `standard:no-blank-line-at-start-of-file`

Suppress or disable rule (1)
{ .annotate }

1. Suppress rule in code with annotation below:
    ```kotlin
    @Suppress("ktlint:standard:no-blank-line-at-start-of-file")
    ```
   Enable rule via `.editorconfig`
    ```editorconfig
    ktlint_standard_no-blank-line-at-start-of-file = enabled
    ```
   Disable rule via `.editorconfig`
    ```editorconfig
    ktlint_standard_no-blank-line-at-start-of-file = disabled
    ```
