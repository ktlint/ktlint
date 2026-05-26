## Annotation formatting

Multiple annotations should be on a separate line than the annotated declaration. Annotations with parameters should be on separate lines. Annotations should be followed by a space.

=== "[:material-heart:](#) Ktlint"

    ```kotlin
    // A single annotation (without parameters) is allowed on same line as annotated construct
    @FunctionalInterface class FooBar {
        @JvmField var foo: String
    
        @Test fun bar() {}
    }
    
    // A class or function parameter may have a single annotation with parameter(s) on the same line
    class Foo(
        @Path("fooId") val fooId: String,
        @NotNull("bar") bar: String,
    )
    
    // Multiple annotations (without parameters) are allowed on the same line
    @Foo @Bar
    class FooBar {
        @Foo @Bar
        var foo: String
    
        @Foo @Bar
        fun bar() {}
    }
    
    // An array of annotations (without parameters) is allowed on same line as annotated construct
    @[Foo Bar] class FooBar2 {
        @[Foo Bar] var foo: String
    
        @[Foo Bar] fun bar() {}
    }
    ```
=== "[:material-heart-off-outline:](#) Disallowed"

    ```kotlin
    // An annotation with parameter(s) is not allowed on same line as annotated construct
    @Suppress("Unused") class FooBar {
        @Suppress("Unused") var foo: String
        @Suppress("Unused") fun bar() {}
    }
    // Multiple annotation on same line as annotated construct are not allowed
    @Foo @Bar class FooBar {
        @Foo @Bar var foo: String
        @Foo @Bar fun bar() {}
    }
    ```

| Configuration setting                                                                                                                                                                                                                                                                         | ktlint_official | intellij_idea | android_studio |
|:----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|:---------------:|:-------------:|:--------------:|
| `ktlint_annotation_handle_annotations_with_parameters_same_as_annotations_without_parameters`<br/><i>Handle listed annotations identical to annotations without parameters. Value is a comma separated list of names without the `@` prefix. Use `*` for all annotations with parameters.</i> |     `unset`     |    `unset`    |    `unset`     |

Rule id: `standard:annotation`

Suppress or disable rule (1)
{ .annotate }

1. Suppress rule in code with annotation below:
    ```kotlin
    @Suppress("ktlint:standard:annotation")
    ```
   Enable rule via `.editorconfig`
    ```editorconfig
    ktlint_standard_annotation = enabled
    ```
   Disable rule via `.editorconfig`
    ```editorconfig
    ktlint_standard_annotation = disabled
    ```

## Blank line between when-conditions

Consistently add or remove a blank line between when-conditions in a when-statement. A blank line is only added between when-conditions if the when-statement contains at lease one multiline when-condition. If a when-statement only contains single line when-conditions, then the blank lines between the when-conditions are removed.

!!! note
    Ktlint uses `.editorconfig` property `ij_kotlin_line_break_after_multiline_when_entry` but applies it also on single line entries to increase consistency.

=== "[:material-heart:](#) Ktlint"

    ```kotlin
    val foo1 =
        when (bar) {
            BAR1 -> "bar1"
            BAR2 -> "bar2"
            else -> null
        }

    // ij_kotlin_line_break_after_multiline_when_entry = true
    val foo2 =
        when (bar) {
            BAR1 -> "bar1"

            BAR2 -> {
                "bar2"
            }

            else -> null
        }

    // ij_kotlin_line_break_after_multiline_when_entry = true
    val foo3 =
        when (bar) {
            BAR1 -> "bar1"

            // BAR2 comment
            BAR2 -> "bar2"

            else -> null
        }

    // ij_kotlin_line_break_after_multiline_when_entry = false
    val foo4 =
        when (bar) {
            BAR1 -> "bar1"
            BAR2 -> {
                "bar2"
            }
            else -> null
        }
    ```
=== "[:material-heart-off-outline:](#) Disallowed"

    ```kotlin
    // ij_kotlin_line_break_after_multiline_when_entry = true | false (no blank lines in simple when-statement)
    val foo1 =
        when (bar) {
            BAR1 -> "bar1"

            BAR2 -> "bar2"

            else -> null
        }

    // ij_kotlin_line_break_after_multiline_when_entry = true (missing newline after BAR1)
    val foo2 =
        when (bar) {
            BAR1 -> "bar1"
            BAR2 -> {
                "bar2"
            }

            else -> null
        }

    // ij_kotlin_line_break_after_multiline_when_entry = true (missing newline after BAR1, and BAR2)
    val foo3 =
        when (bar) {
            BAR1 -> "bar1"
            // BAR2 comment
            BAR2 -> "bar2"
            else -> null
        }

    // ij_kotlin_line_break_after_multiline_when_entry = false (unexpected newline after BAR2)
    val foo4 =
        when (bar) {
            BAR1 -> "bar1"
            BAR2 -> {
                "bar2"
            }

            else -> null
        }
    ```

| Configuration setting                                                                                                                                                                                                    | ktlint_official | intellij_idea | android_studio |
|:-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|:---------------:|:-------------:|:--------------:|
| `ij_kotlin_line_break_after_multiline_when_entry`<br/><i>Despite its name, forces a blank line between single line and multiline when-entries when at least one multiline when-entry is found in the when-statement.</i> |     `true`      |    `true`     |     `true`     |

Rule id: `standard:blank-line-between-when-conditions`

Suppress or disable rule (1)
{ .annotate }

1. Suppress rule in code with annotation below:
    ```kotlin
    @Suppress("ktlint:standard:blank-line-between-when-conditions")
    ```
   Enable rule via `.editorconfig`
    ```editorconfig
    ktlint_standard_blank-line-between-when-conditions = enabled
    ```
   Disable rule via `.editorconfig`
    ```editorconfig
    ktlint_standard_blank-line-between-when-conditions = disabled
    ```

## Binary expression wrapping

Wraps binary expression at the operator reference whenever the binary expression does not fit on the line. In case the binary expression is nested, the expression is evaluated from outside to inside. If the left and right hand sides of the binary expression, after wrapping, fit on a single line then the inner binary expressions will not be wrapped. If one or both inner binary expression still do not fit on a single after wrapping of the outer binary expression, then each of those inner binary expressions will be wrapped.

=== "[:material-heart:](#) Ktlint"

    ```kotlin
    fun foo() {
        // Assume that the last allowed character is
        // at the X character on the right                       X
        if ((leftHandSideExpression && rightHandSideExpression) ||
            (
                leftHandSideLongExpression &&
                    rightHandSideExpression
            )
        ) {
            // do something
        }
    }
    ```

=== "[:material-heart-off-outline:](#) Disallowed"

    ```kotlin
    fun foo() {
        // Assume that the last allowed character is
        // at the X character on the right                       X
        if ((leftHandSideExpression && rightHandSideExpression) ||
            (leftHandSideLongExpression && rightHandSideExpression)
        ) {
            // do something
        }
    }
    ```

| Configuration setting                                                                                                                                                                                                         | ktlint_official | intellij_idea | android_studio |
|:------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|:---------------:|:-------------:|:--------------:|
| `max_line_length`<br/><i>Maximum length of a (regular) line. This property is ignored in case the `max-line-length` rule is disabled, or when using Ktlint via a third party integration that does not provide this rule.</i> |       140       |     `off`     |     `100`      |

Rule id: `standard:binary-expression-wrapping`

Suppress or disable rule (1)
{ .annotate }

1. Suppress rule in code with annotation below:
    ```kotlin
    @Suppress("ktlint:standard:binary-expression-wrapping")
    ```
   Enable rule via `.editorconfig`
    ```editorconfig
    ktlint_standard_binary-expression-wrapping = enabled
    ```
   Disable rule via `.editorconfig`
    ```editorconfig
    ktlint_standard_binary-expression-wrapping = disabled
    ```

## Blank line before declarations

Requires a blank line before any class or function declaration. No blank line is required between the class signature and the first declaration in the class. In a similar way, a blank line is required before any list of top level or class properties. No blank line is required before local properties or between consecutive properties.

=== "[:material-heart:](#) Ktlint"

    ```kotlin
    const val FOO_1 = "foo1"

    class FooBar {
        val foo2 = "foo2"
        val foo3 = "foo3"

        fun bar1() {
            val foo4 = "foo4"
            val foo5 = "foo5"
        }

        fun bar2() = "bar"

        val foo6 = "foo3"
        val foo7 = "foo4"

        enum class Foo
    }
    ```

=== "[:material-heart-off-outline:](#) Disallowed"

    ```kotlin
    const val FOO_1 = "foo1"

    class FooBar {
        val foo2 = "foo2"
        val foo3 = "foo3"
        fun bar1() {
            val foo4 = "foo4"
            val foo5 = "foo5"
        }
        fun bar2() = "bar"
        val foo6 = "foo3"
        val foo7 = "foo4"
        enum class Foo
    }
    ```

Rule id: `standard:blank-line-before-declaration`

Suppress or disable rule (1)
{ .annotate }

1. Suppress rule in code with annotation below:
    ```kotlin
    @Suppress("ktlint:standard:blank-line-before-declaration")
    ```
   Enable rule via `.editorconfig`
    ```editorconfig
    ktlint_standard_blank-line-before-declaration = enabled
    ```
   Disable rule via `.editorconfig`
    ```editorconfig
    ktlint_standard_blank-line-before-declaration = disabled
    ```

!!! Note
    This rule is only run when `ktlint_code_style` is set to `ktlint_official` or when the rule is enabled explicitly.

## Block comment initial star alignment

Lines in a block comment which (excluding the indentation) start with a `*` should have this `*` aligned with the `*` in the opening of the block comment.

=== "[:material-heart:](#) Ktlint"

    ```kotlin
    /*
     * This comment is formatted well.
     */
    ```
=== "[:material-heart-off-outline:](#) Disallowed"

    ```kotlin
    /*
          * This comment is not formatted well.
        */
    ```

Rule id: `standard:block-comment-initial-star-alignment`

Suppress or disable rule (1)
{ .annotate }

1. Suppress rule in code with annotation below:
    ```kotlin
    @Suppress("ktlint:standard:block-comment-initial-star-alignment")
    ```
   Enable rule via `.editorconfig`
    ```editorconfig
    ktlint_standard_block-comment-initial-star-alignment = enabled
    ```
   Disable rule via `.editorconfig`
    ```editorconfig
    ktlint_standard_block-comment-initial-star-alignment = disabled
    ```

## Chain method continuation

In a multiline method chain, the chain operators (`.` or `?.`) have to be aligned with each other.

Multiple chained methods on a single line are allowed as long as the maximum line length, and the maximum number of chain operators are not exceeded. Under certain conditions, it is allowed that the expression before the first and/or the expression after the last chain operator is a multiline expression.

The `.` in `java.class` is ignored when wrapping on chain operators.

!!! warning
    A binary expression for which the left and/or right operand consist of a method chain are currently being ignored by this rule. Please reach out, if you can help to determine what the best strategy is to deal with such kind of expressions.

=== "[:material-heart:](#) Ktlint"

    ```kotlin
    val foo1 =
        listOf(1, 2, 3)
            .filter { it > 2 }!!
            .takeIf { it > 2 }
            .map {
                it * it
            }?.map {
                it * it
            }
    val foo2 =
        listOf(1, 2, 3)
            .filter {
                it > 2
            }.map {
                2 * it
            }?.map {
                2 * it
            }
    val foo3 =
        foo().bar().map {
            it.foobar()
        }
    val foo4 =
        """
        Some text
        """.trimIndent().foo().bar()
    ```

=== "[:material-heart-off-outline:](#) Disallowed"

    ```kotlin
    val foo1 =
        listOf(1, 2, 3).
            filter { it > 2 }!!.
            takeIf { it > 2 }.
            map {
                it * it
            }?.
            map {
                it * it
            }
    val foo2 =
        listOf(1, 2, 3)
            .filter {
                it > 2
            }
            .map {
                2 * it
            }
            ?.map {
                2 * it
            }
    val foo3 = 
        foo()
        .bar().map {
            it.foobar()
        }
    val foo4 =
        """
        Some text
        """.trimIndent().foo()
            .bar()
    ```

| Configuration setting                                                                                                                                                                                                                                                                                                                                                                                  | ktlint_official | intellij_idea | android_studio |
|:-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|:---------------:|:-------------:|:--------------:|
| `ktlint_chain_method_rule_force_multiline_when_chain_operator_count_greater_or_equal_than`<br/><i>Force wrapping of chained methods in case an expression contains at least the specified number of chain operators. If a chained method contains nested expressions, the chain operators of the inner expression are not taken into account. Use value `unset` (default) to disable this setting.</i> |        4        |       4       |       4        |
| `max_line_length`<br/><i>Maximum length of a (regular) line. This property is ignored in case the `max-line-length` rule is disabled, or when using Ktlint via a third party integration that does not provide this rule.</i>                                                                                                                                                                          |       140       |     `off`     |     `100`      |


Rule id: `standard:chain-method-continuation`

Suppress or disable rule (1)
{ .annotate }

1. Suppress rule in code with annotation below:
    ```kotlin
    @Suppress("ktlint:standard:chain-method-continuation")
    ```
   Enable rule via `.editorconfig`
    ```editorconfig
    ktlint_standard_chain-method-continuation = enabled
    ```
   Disable rule via `.editorconfig`
    ```editorconfig
    ktlint_standard_chain-method-continuation = disabled
    ```

!!! Note
    This rule is only run when `ktlint_code_style` is set to `ktlint_official` or when the rule is enabled explicitly.

## Class signature

Rewrites the class signature to a consistent format respecting the `.editorconfig` property `max_line_length` if set. In the `ktlint_official` code style all class parameters are wrapped by default. Set `.editorconfig` property `ktlint_class_signature_rule_force_multiline_when_parameter_count_greater_or_equal_than` to a value greater than 1 to allow classes with a few parameters to be placed on a single line.
The other code styles allow an infinite amount of parameters on the same line (as long as the `max_line_length` is not exceeded) unless `.editorconfig` property `ktlint_class_signature_rule_force_multiline_when_parameter_count_greater_or_equal_than` is set explicitly.

=== "[:material-heart:](#) Ktlint (ktlint_official)"

    ```kotlin
    // Assume that max_line_length is not exceeded when written as single line
    class Foo0
    
    class Foo1(
        a: Any,
    )
    
    class Foo2(
        a: Any,
        b: Any,
    )
    
    class Foo3(
        @Foo a: Any,
        b: Any,
        c: Any,
    )
    
    class Foo4(
        a: Any,
        b: Any,
        c: Any,
    ) : FooBar(a, c)
    
    class Foo5 :
        FooBar(
            "bar1",
            "bar2",
        ) {
        // body
    }
    
    class Foo6(
        val bar1: Bar,
        val bar2: Bar,
    ) : FooBar(
            bar1,
            bar2,
        ) {
        // body
    }
    
    class Foo7(
        val bar1: Bar,
        val bar2: Bar,
    ) : FooBar(
            bar1,
            bar2,
        ),
        BarFoo1,
        BarFoo2 {
        // body
    }
    
    class Foo8
        constructor(
            val bar1: Bar,
            val bar2: Bar,
        ) : FooBar(bar1, bar2),
            BarFoo1,
            BarFoo2 {
            // body
        }
    ```

=== "[:material-heart-off-outline:](#) Disallowed (ktlint_official)"

    ```kotlin
    // Assume that max_line_length is not exceeded when written as single line
    class Foo0()

    class Foo1(a: Any)

    class Foo2(a: Any, b: Any)

    class Foo3(@Foo a: Any, b: Any, c: Any)

    class Foo4(a: Any, b: Any, c: Any) : FooBar(a, c)

    class Foo5 : FooBar(
        "bar1",
        "bar2",
    ) {
        // body
    }

    class Foo6(
        val bar1: Bar,
        val bar2: Bar,
    ) : FooBar(
        bar1,
        bar2,
    ) {
        // body
    }

    class Foo7(
        val bar1: Bar,
        val bar2: Bar,
    ) : FooBar(
        bar1,
        bar2,
    ),
        BarFoo1,
        BarFoo2 {
        // body
    }

    class Foo8
    constructor(
        val bar1: Bar,
        val bar2: Bar,
    ) : FooBar(bar1, bar2),
        BarFoo1,
        BarFoo2 {
        // body
    }
    ```

=== "[:material-heart:](#) Ktlint (non ktlint_official)"

    ```kotlin
    // Assume that the last allowed character is
    // at the X character on the right           X
    class Foo0
    
    // Entire class signature does not fit on single line
    class Foo1(
        aVeryLonggggggggggggggggggggggg: Any
    )
    
    // Entire class signature does fit on single line
    class Foo2(a: Any)
    
    // Entire class signature does not fit on single line
    class Foo3(
        aVeryLonggggggggggggggg: Any,
        b: Any
    )
    
    // Entire class signature does fit on single line
    class Foo4(a: Any, b: Any)
    
    // Entire class signature does fit on single line
    class Foo5(@Foo a: Any, b: Any, c: Any)
    
    // Entire class signature does fit on single line
    class Foo6(a: Any, b: Any, c: Any) :
        FooBar(a, c)
    
    // Entire class signature (without constructor invocation) does fit on single line
    class Foo7 :
        FooBar(
            "bar1",
            "bar2"
        ) {
        // body
    }
    
    // Entire class signature (without constructor invocation) does fit on single line
    class Foo8(val bar1: Bar, val bar2: Bar) :
        FooBar(
            bar1,
            bar2
        ) {
        // body
    }
    
    // Entire class signature (without constructor invocation) does not fit on single line
    class Foo9(
        val aVeryLonggggggggggggggg: Bar,
        val bar2: Bar
    ) : FooBar(
        bar1,
        bar2
    ) {
        // body
    }
    
    class Foo10(val bar1: Bar, val bar2: Bar) :
        FooBar(
            bar1,
            bar2
        ),
        BarFoo1,
        BarFoo2 {
        // body
    }
    
    class Foo11
    constructor(
        val bar1: Bar,
        val bar2: Bar
    ) : FooBar(bar1, bar2),
        BarFoo1,
        BarFoo2 {
        // body
    }
    
    ```

=== "[:material-heart-off-outline:](#) Disallowed (non ktlint_official)"

    ```kotlin
    // Assume that the last allowed character is
    // at the X character on the right           X
    class Foo0()

    class Foo6(a: Any, b: Any, c: Any) : FooBar(a, c)

    // Entire class signature (without constructor invocation) does fit on single line
    class Foo7 : FooBar(
        "bar1",
        "bar2",
    ) {
        // body
    }
    ```

| Configuration setting                                                                                                                                                                                                                                                                                                                       | ktlint_official | intellij_idea | android_studio |
|:--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|:---------------:|:-------------:|:--------------:|
| `ktlint_class_signature_rule_force_multiline_when_parameter_count_greater_or_equal_than`<br/><i>Force wrapping of the parameters of the class signature in case it contains at least the specified number of parameters, even in case the entire class signature would fit on a single line. Use value `unset` to disable this setting.</i> |        1        |    `unset`    |    `unset`     |
| `max_line_length`<br/><i>Maximum length of a (regular) line. This property is ignored in case the `max-line-length` rule is disabled, or when using Ktlint via a third party integration that does not provide this rule.</i>                                                                                                               |       140       |     `off`     |     `100`      |

Rule id: `standard:class-signature`

!!! warn
    For code styles `android_studio` and `intellij_idea` this rule rewrites multiline class signature to a single line class signature in case the entire class signature fits on a single line by default. In case you want to leave it to the discretion of the developer to decider whether a single or a multiline class signature is used, please suppress or disable this rule.

Suppress or disable rule (1)
{ .annotate }

1. Suppress rule in code with annotation below:
    ```kotlin
    @Suppress("ktlint:standard:class-signature")
    ```
   Enable rule via `.editorconfig`
    ```editorconfig
    ktlint_standard_class-signature = enabled
    ```
   Disable rule via `.editorconfig`
    ```editorconfig
    ktlint_standard_class-signature = disabled
    ```

## Enum entry

Enum entry names should be uppercase underscore-separated or upper camel-case separated.

=== "[:material-heart:](#) Ktlint"

    ```kotlin
    enum class Bar {
        FOO,
        Foo,
        FOO_BAR,
        FooBar,
    }
    ```
=== "[:material-heart-off-outline:](#) Disallowed"

    ```kotlin
    enum class Bar {
        foo,
        bAr,
        Foo_Bar,
    }
    ```

| Configuration setting                                                                                                                                                                                                                                                                                                                                                                                                           |    ktlint_official     |     intellij_idea      |     android_studio     |
|:--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|:----------------------:|:----------------------:|:----------------------:|
| `ktlint_enum_entry_name_casing`</br><i>Choose any of `upper_cases` (an enum entry may only contain uppercases, and underscores, and digits, and diacritics on letters and strokes), `camel_cases` (an enum entry may only contain CamelCase values, including digits, and diacritics on letters and strokes), or `upper_or_camel_case` (allows mixing of uppercase and CamelCase entries as per Kotlin Coding Conventions).</i> | `upper_or_camel_cases` | `upper_or_camel_cases` | `upper_or_camel_cases` |

Rule id: `standard:enum-entry-name-case`

Suppress or disable rule (1)
{ .annotate }

1. Suppress rule in code with annotation below:
    ```kotlin
    @Suppress("ktlint:standard:enum-entry-name-case")
    ```
   Enable rule via `.editorconfig`
    ```editorconfig
    ktlint_standard_enum-entry-name-case = enabled
    ```
   Disable rule via `.editorconfig`
    ```editorconfig
    ktlint_standard_enum-entry-name-case = disabled
    ```

## File name

A file containing only one visible (e.g. non-private) class, and visible declarations related to that class only, should be named according to that element. The same applies if the file does not contain a visible class but exactly one type alias or one object declaration. Otherwise, the PascalCase notation should be used.

Rule id: `standard:filename`

Suppress or disable rule (1)
{ .annotate }

1. Suppress rule in code with annotation below:
    ```kotlin
    @Suppress("ktlint:standard:filename")
    ```
   Enable rule via `.editorconfig`
    ```editorconfig
    ktlint_standard_filename = enabled
    ```
   Disable rule via `.editorconfig`
    ```editorconfig
    ktlint_standard_filename = disabled
    ```

## Final newline

Ensures consistent usage of a newline at the end of each file.

| Configuration setting  | ktlint_official | intellij_idea | android_studio |
|:-----------------------|:---------------:|:-------------:|:--------------:|
| `insert_final_newline` |     `true`      |    `true`     |     `true`     |

Rule id: `standard:final-newline`

Suppress or disable rule (1)
{ .annotate }

1. Suppress rule in code with annotation below:
    ```kotlin
    @Suppress("ktlint:standard:final-newline")
    ```
   Enable rule via `.editorconfig`
    ```editorconfig
    ktlint_standard_final-newline = enabled
    ```
   Disable rule via `.editorconfig`
    ```editorconfig
    ktlint_standard_final-newline = disabled
    ```

## Function expression body

Rewrites a function body only containing a `return` or `throw` expression to an expression body.

!!! note
    If the function body contains a comment, it is not rewritten to an expression body.

=== "[:material-heart:](#) Ktlint"

    ```kotlin
    fun foo1() = "foo"

    fun foo2(): String = "foo"

    fun foo3(): Unit = throw IllegalArgumentException("some message")

    fun foo4(): Foo = throw IllegalArgumentException("some message")

    fun foo5() {
        return "foo" // some comment
    }

    fun foo6(): String {
        /* some comment */
        return "foo"
    }

    fun foo7() {
        throw IllegalArgumentException("some message")
        /* some comment */
    }

    fun foo8(): Foo {
        throw IllegalArgumentException("some message")
        // some comment
    }
    ```
=== "[:material-heart-off-outline:](#) Disallowed"

    ```kotlin
    fun foo1() {
        return "foo"
    }

    fun foo2(): String {
        return "foo"
    }

    fun foo3() {
        throw IllegalArgumentException("some message")
    }

    fun foo4(): Foo {
        throw IllegalArgumentException("some message")
    }
    ```

Rule id: `standard:function-expression-body`

Suppress or disable rule (1)
{ .annotate }

1. Suppress rule in code with annotation below:
    ```kotlin
    @Suppress("ktlint:standard:function-expression-body")
    ```
   Enable rule via `.editorconfig`
    ```editorconfig
    ktlint_standard_function-expression-body = enabled
    ```
   Disable rule via `.editorconfig`
    ```editorconfig
    ktlint_standard_function-expression-body = disabled
    ```

## Function literal

Enforces the parameters of a function literal and the arrow to be written on the same line as the opening brace if the maximum line length is not exceeded. In case the parameters are wrapped to multiple lines then this is respected.

If the function literal contains multiple parameters and at least one parameter other than the first parameter starts on a new line than all parameters and the arrow are wrapped to separate lines.

=== "[:material-heart:](#) Ktlint (ktlint_official)"

    ```kotlin
    val foobar1 = { foo + bar }
    val foobar2 =
        {
            foo + bar
        }
    val foobar3 =
        { foo: Foo ->
            foo.repeat(2)
        }
    val foobar4 =
        { foo: Foo, bar: Bar ->
            foo + bar
        }
    val foobar5 = { foo: Foo, bar: Bar -> foo + bar }
    val foobar6 =
        {
            foo: Foo,
            bar: Bar,
            ->
            foo + bar
        }
    
    // Assume that the last allowed character is
    // at the X character on the right           X
    val foobar7 =
        barrrrrrrrrrrrrr { 
            fooooooooooooooo: Foo
            ->
            foo.repeat(2)
        }
    ```

=== "[:material-heart:](#) Ktlint (non ktlint_official)"

    ```kotlin
    val foobar6 =
        {
                foo: Foo,
                bar: Bar,
            ->
            foo + bar
        }
    
    // Assume that the last allowed character is
    // at the X character on the right           X
    val foobar7 =
        barrrrrrrrrrrrrr { 
                fooooooooooooooo: Foo
            ->
            foo.repeat(2)
        }
    ```

=== "[:material-heart-off-outline:](#) Disallowed"

    ```kotlin
    val foobar3 =
        {
            foo: Foo ->
            foo.repeat(2)
        }
    val foobar6 =
        { foo: Foo,
          bar: Bar ->
            foo + bar
        }
    // Assume that the last allowed character is
    // at the X character on the right           X
    val foobar7 =
        barrrrrrrrrrrrrr { fooooooooooooooo: Foo ->
            foo.repeat(2)
        }
    ```

| Configuration setting                                                                                                                                                                                                         | ktlint_official | intellij_idea | android_studio |
|:------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|:---------------:|:-------------:|:--------------:|
| `max_line_length`<br/><i>Maximum length of a (regular) line. This property is ignored in case the `max-line-length` rule is disabled, or when using Ktlint via a third party integration that does not provide this rule.</i> |       140       |     `off`     |     `100`      |

Rule id: `standard:function-literal`

Suppress or disable rule (1)
{ .annotate }

1. Suppress rule in code with annotation below:
    ```kotlin
    @Suppress("ktlint:standard:function-literal")
    ```
   Enable rule via `.editorconfig`
    ```editorconfig
    ktlint_standard_function-literal = enabled
    ```
   Disable rule via `.editorconfig`
    ```editorconfig
    ktlint_standard_function-literal = disabled
    ```

## Function signature

Rewrites the function signature to a single line when possible (e.g. when not exceeding the `max_line_length` property) or a multiline signature otherwise.

!!! note
    Wrapping of parameters is also influenced by the `parameter-list-wrapping` rule.

=== "[:material-heart:](#) Ktlint"

    ```kotlin
    // Assume that the last allowed character is
    // at the X character on the right           X
    fun foooooooo(
        a: Any,
        b: Any,
        c: Any,
    ): String {
        // body
    }

    // Assume that the last allowed character is
    // at the X character on the right           X
    fun bar(a: Any, b: Any, c: Any): String {
        // body
    }

    // When wrapping of body is set to 'default'.
    // Assume that the last allowed character is
    // at the X character on the right           X
    fun f(a: Any, b: Any): String = "some-result"
        .uppercase()

    // When wrapping of body is set to 'multiline'
    // or 'always'.
    // Assume that the last allowed character is
    // at the X character on the right           X
    fun f(a: Any, b: Any): String =
        "some-result"
            .uppercase()
    ```
=== "[:material-heart-off-outline:](#) Disallowed"

    ```kotlin
    // Assume that the last allowed character is
    // at the X character on the right           X
    fun foooooooo(a: Any, b: Any, c: Any): String {
        // body
    }

    // Assume that the last allowed character is
    // at the X character on the right           X
    fun bar(
        a: Any,
        b: Any,
        c: Any
    ): String {
        // body
    }

    // When wrapping of body is set to 'default'.
    // Assume that the last allowed character is
    // at the X character on the right           X
    fun f(a: Any, b: Any): String =
        "some-result"
            .uppercase()

    // When wrapping of body is set to 'multiline'
    // or 'always'.
    // Assume that the last allowed character is
    // at the X character on the right           X
    fun f(a: Any, b: Any): String = "some-result"
        .uppercase()
    ```

| Configuration setting                                                                                                                                                                                                                                                                                                                                                                                                                   | ktlint_official | intellij_idea | android_studio |
|:----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|:---------------:|:-------------:|:--------------:|
| `ktlint_function_signature_body_expression_wrapping`<br/><i>Determines how to wrap the body of function in case it is an expression. Use `default` to wrap the body expression only when the first line of the expression does not fit on the same line as the function signature. Use `multiline` to force wrapping of body expressions that consists of multiple lines. Use `always` to force wrapping of body expression always.</i> |   `multiline`   |   `default`   |   `default`    |
| `ktlint_function_signature_rule_force_multiline_when_parameter_count_greater_or_equal_than`<br/><i>Forces a multiline function signature in case the function contains the specified minimum number of parameters even in case the function signature would fit on a single line. Use value `unset` (default) to disable this setting.</i>                                                                                              |        2        |    `unset`    |    `unset`     |

=== "[:material-heart:](#) default"

    When `ktlint_function_signature_body_expression_wrapping` is set to `default`, the first line of a body expression is appended to the function signature as long as the max line length is not exceeded.

    ```kotlin title="ktlint_function_signature_body_expression_wrapping=default"
    // Given that the function signature has to be written as a single line
    // function signature and that the function has a multiline body expression
    fun someFunction(a: Any, b: Any): String = "some-result"
        .uppercase()
    
    // Given that the function signature has to be written as a multiline
    // function signature and that the function has a multiline body expression
    fun someFunction(
        a: Any,
        b: Any
    ): String = "some-result"
        .uppercase()
    ```

=== "[:material-heart:](#) multiline"

    When `ktlint_function_signature_body_expression_wrapping` is set to `multiline`, the body expression starts on a separate line in case it is a multiline expression. A single line body expression is wrapped only when it does not fit on the same line as the function signature.
    
    ```kotlin title="ktlint_function_signature_body_expression_wrapping=multiline"
    // Given that the function signature has to be written as a single line
    // function signature and that the function has a single line body expression
    // that fits on the same line as the function signature.
    fun someFunction(a: Any, b: Any): String = "some-result".uppercase()
    
    // Given that the function signature has to be written as a multiline
    // function signature and that the function has a single line body expression
    // that fits on the same line as the function signature.
    fun someFunction(
        a: Any,
        b: Any
    ): String = "some-result".uppercase()
    
    // Given that the function signature has to be written as a single line
    // function signature and that the function has a multiline body expression
    fun someFunction(a: Any, b: Any): String =
        "some-result"
             .uppercase()

    // Given that the function signature has to be written as a multiline
    // function signature and that the function has a multiline body expression
    fun someFunction(
        a: Any,
        b: Any
    ): String =
        "some-result"
           .uppercase()
    ```

=== "[:material-heart:](#) always"

    When `ktlint_function_signature_body_expression_wrapping` is  set to `always` the body expression is always wrapped to a separate line.
    
    ```kotlin title="ktlint_function_signature_body_expression_wrapping=always"
    // Given that the function signature has to be written as a single line
    // function signature and that the function has a single line body expression
    fun someFunction(a: Any, b: Any): String =
        "some-result".uppercase()

    // Given that the function signature has to be written as a multiline
    // function signature and that the function has a multiline body expression
    fun functionWithAVeryLongName(
        a: Any,
        b: Any
    ): String =
        "some-result"
            .uppercase()
    ```

| Configuration setting                                                                                                                                                                                                         | ktlint_official | intellij_idea | android_studio |
|:------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|:---------------:|:-------------:|:--------------:|
| `max_line_length`<br/><i>Maximum length of a (regular) line. This property is ignored in case the `max-line-length` rule is disabled, or when using Ktlint via a third party integration that does not provide this rule.</i> |       140       |     `off`     |     `100`      |

Rule id: `standard:function-signature`

Suppress or disable rule (1)
{ .annotate }

1. Suppress rule in code with annotation below:
    ```kotlin
    @Suppress("ktlint:standard:function-signature")
    ```
   Enable rule via `.editorconfig`
    ```editorconfig
    ktlint_standard_function-signature = enabled
    ```
   Disable rule via `.editorconfig`
    ```editorconfig
    ktlint_standard_function-signature = disabled
    ```

## Function type modifier spacing

Enforce a single whitespace between the modifier list and the function type.

=== "[:material-heart:](#) Ktlint"

    ```kotlin
    val foo: suspend () -> Unit = {}

    suspend fun bar(baz: suspend () -> Unit) = baz()
    ```

=== "[:material-heart-off-outline:](#) Disallowed"

    ```kotlin
    val foo: suspend() -> Unit = {}

    suspend fun bar(baz: suspend   () -> Unit) = baz()
    ```

Rule id: `standard:function-type-modifier-spacing`

Suppress or disable rule (1)
{ .annotate }

1. Suppress rule in code with annotation below:
    ```kotlin
    @Suppress("ktlint:standard:function-type-modifier-spacing")
    ```
   Enable rule via `.editorconfig`
    ```editorconfig
    ktlint_standard_function-type-modifier-spacing = enabled
    ```
   Disable rule via `.editorconfig`
    ```editorconfig
    ktlint_standard_function-type-modifier-spacing = disabled
    ```

## If else bracing

If at least one branch of an if-else statement or an if-else-if statement is wrapped between curly braces, then all branches should be wrapped between braces.

=== "[:material-heart:](#) Ktlint"

    ```kotlin
    fun foo(value: Int) {
        if (value > 0) {
            doSomething()
        } else if (value < 0) {
            doSomethingElse()
        } else {
            doSomethingElse2()
        }
    }
    ```

=== "[:material-heart-off-outline:](#) Disallowed"

    ```kotlin
    fun foo(value: Int) {
        if (value > 0)
            doSomething()
        else if (value < 0) {
            doSomethingElse()
        } else
            doSomethingElse2()
    }
    ```

Rule id: `standard:if-else-bracing`

Suppress or disable rule (1)
{ .annotate }

1. Suppress rule in code with annotation below:
    ```kotlin
    @Suppress("ktlint:standard:if-else-bracing")
    ```
   Enable rule via `.editorconfig`
    ```editorconfig
    ktlint_standard_if-else-bracing = enabled
    ```
   Disable rule via `.editorconfig`
    ```editorconfig
    ktlint_standard_if-else-bracing = disabled
    ```

!!! Note
    This rule is only run when `ktlint_code_style` is set to `ktlint_official` or when the rule is enabled explicitly.

## Import ordering

Ensures that imports are ordered consistently.

=== "[:material-heart:](#) Ktlint"

    ```kotlin
    import com.bar.Bar
    import com.foo.Foo
    import org.foo.bar.FooBar
    import java.util.concurrent.ConcurrentHashMap
    ```
=== "[:material-heart-off-outline:](#) Disallowed"

    ```kotlin
    import com.bar.Bar
    import java.util.concurrent.ConcurrentHashMap
    import org.foo.bar.FooBar
    import com.foo.Foo
    ```

| Configuration setting                                                                                                  |          ktlint_official           |           intellij_idea            | android_studio |
|:-----------------------------------------------------------------------------------------------------------------------|:----------------------------------:|:----------------------------------:|:--------------:|
| `ij_kotlin_imports_layout`</br><i>Defines imports order layout for Kotlin files</i>. For more details see below table. | *,java.**,javax.**,kotlin.**,^ <1> | *,java.**,javax.**,kotlin.**,^ <1> |     * <2>      |

### ij_kotlin_packages_to_use_import_on_demand

This property holds 0 or more import paths. The import path can be a full path, e.g. "java.util.List.*" as well as wildcard path, e.g. "kotlin.**".

Imports can be grouped by composing the layout with symbols below:

*  `*` - wildcard. There must be at least one entry of a single wildcard to match all other imports. Matches anything after a specified symbol/import as well.
* `|` - blank line. Supports only single blank lines between imports. No blank line is allowed in the beginning or end of the layout.
* `^` - alias import, e.g. "^android.*" will match all android alias imports, "^" will match all other alias imports.

Imports in the same group are sorted alphabetical with capital letters before lower case letters (e.g. Z before a).

Examples:
```kotlin
ij_kotlin_imports_layout=* # alphabetical with capital letters before lower case letters (e.g. Z before a), no blank lines
ij_kotlin_imports_layout=*,java.**,javax.**,kotlin.**,^ # default IntelliJ IDEA style, same as alphabetical, but with "java", "javax", "kotlin" and alias imports in the end of the imports list
ij_kotlin_imports_layout=android.**,|,^org.junit.**,kotlin.io.Closeable.*,|,*,^ # custom imports layout
```

Wildcard imports can be allowed for specific import paths (Comma-separated list, use "**" as wildcard for package and all subpackages). This setting overrides the no-wildcard-imports rule. This setting is best be used for allowing wildcard imports from libraries like Ktor where extension functions are used in a way that creates a lot of imports.

Rule id: `standard:import-ordering`

Suppress or disable rule (1)
{ .annotate }

1. Suppress rule in code with annotation below:
    ```kotlin
    @Suppress("ktlint:standard:import-ordering")
    ```
   Enable rule via `.editorconfig`
    ```editorconfig
    ktlint_standard_import-ordering = enabled
    ```
   Disable rule via `.editorconfig`
    ```editorconfig
    ktlint_standard_import-ordering = disabled
    ```

## Indentation

Indentation formatting - respects `.editorconfig` `indent_size` with no continuation indent (see [EditorConfig](configuration-ktlint.md) section for more).

=== "[:material-heart:](#) Ktlint"

    ```kotlin
    fun main() {
        foobar(
            a,
            b,
            c,
        )
    }
    ```
=== "[:material-heart-off-outline:](#) Disallowed"

    ```kotlin
    fun main() {
        foobar(
              a,
              b,
              c,
              )
    }
    ```

!!! note
    This rule handles indentation for many different language constructs which can not be summarized with a few examples. See the [unit tests](https://github.com/ktlint/ktlint/blob/master/ktlint-ruleset-standard/src/test/kotlin/com/ktlint/ktlint/ruleset/standard/rules/IndentationRuleTest.kt) for more details.

| Configuration setting                                                                                                                                                                                                                                                                             | ktlint_official | intellij_idea | android_studio |
|:--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|:---------------:|:-------------:|:--------------:|
| `indent_size`</br><i>The size of an indentation level when `indent_style` is set to `space`. Use value `unset` to ignore indentation.</i>                                                                                                                                                         |        4        |       4       |       4        |
| `indent_style`</br><i>Style of indentation. Set this value to `space` or `tab`.</i>                                                                                                                                                                                                               |     `space`     |    `space`    |    `space`     |
| `ij_kotlin_indent_before_arrow_on_new_line`</br><i>Indent the arrow in a when-entry if the arrow starts on a new line. Set this value to `true` or `false`. Starting from IDEA version `2024.2` or above this value needs to be set to `true` to maintain compatibility with IDEA formatting.</i> |     `false`     |    `false`    |    `false`     |

Rule id: `standard:indent`

Suppress or disable rule (1)
{ .annotate }

1. Suppress rule in code with annotation below:
    ```kotlin
    @Suppress("ktlint:standard:indent")
    ```
   Enable rule via `.editorconfig`
    ```editorconfig
    ktlint_standard_indent = enabled
    ```
   Disable rule via `.editorconfig`
    ```editorconfig
    ktlint_standard_indent = disabled
    ```

## KDoc

KDoc's should only be used on elements for which KDoc is to be transformed to documentation. Normal block comments should be used in other cases.

!!! note:
Access modifiers are ignored. Strictly speaking, one could argue that private declarations should not have a KDoc as no documentation will be generated for it. However, for internal use of developers, the KDoc still serves documentation purposes.

=== "[:material-heart:](#) Ktlint"

    ```kotlin
    /** some KDoc */
    class FooBar(
        /** some KDoc */
        val foo: Foo
    ) {
        /**
         * Some bar KDoc
         */
        constructor() : this()

        /** some KDoc */
        val bar: Bar
    }

    enum class Foo {
        /** some KDoc */
        BAR
    }

    /** some KDoc */
    interface Foo
    /** some KDoc */
    fun foo()
    /** some KDoc */
    val foo: Foo
    /** some KDoc */
    object foo: Foo
    /** some KDoc */
    typealias FooBar = (Foo) -> Bar
    ```

=== "[:material-heart-off-outline:](#) Disallowed"

    ```kotlin
    /**
     * Some dangling Kdoc (e.g. not followed by a declaration)
     */

    val foo /** Some KDoc */ = "foo"

    class Foo(
        /** some dangling KDoc inside a parameter list */
    )
    ```

Rule id: `standard:kdoc`

Suppress or disable rule (1)
{ .annotate }

1. Suppress rule in code with annotation below:
    ```kotlin
    @Suppress("ktlint:standard:kdoc")
    ```
   Enable rule via `.editorconfig`
    ```editorconfig
    ktlint_standard_kdoc = enabled
    ```
   Disable rule via `.editorconfig`
    ```editorconfig
    ktlint_standard_kdoc = disabled
    ```

## Mixed condition operators

Conditions cannot use both `&&` and `||` operators between operators at the same level. By using parenthesis the expression is to be clarified.

=== "[:material-heart:](#) Ktlint"

    ```kotlin
    val foo = bar1 && (bar2 || bar3) && bar4
    ```

=== "[:material-heart-off-outline:](#) Disallowed"

    ```kotlin
    val foo = bar1 &&
        bar2 ||
        bar3
    val foo = bar1 && (bar2 || bar3 && bar4) && bar5
    ```

Rule id: `standard:mixed-condition-operators`

Suppress or disable rule (1)
{ .annotate }

1. Suppress rule in code with annotation below:
    ```kotlin
    @Suppress("ktlint:standard:mixed-condition-operators")
    ```
   Enable rule via `.editorconfig`
    ```editorconfig
    ktlint_standard_mixed-condition-operators = enabled
    ```
   Disable rule via `.editorconfig`
    ```editorconfig
    ktlint_standard_mixed-condition-operators = disabled
    ```

## Naming

### Backing property naming

Allows property names to start with `_` in case the property is a backing property. `ktlint_official` and `android_studio` code styles require the correlated property/function to be defined as `public`.

=== "[:material-heart:](#) Ktlint"

    ```kotlin
    class Bar {
        // Backing property as normal class member
        private val _elementList = mutableListOf<Element>()
        val elementList: List<Element>
            get() = _elementList

        // Backing property defined in companion object
        val elementList2: List<Element>
            get() = _elementList2

        companion object {
            private val _elementList2 = mutableListOf<Element>()
        }
    }
    ```
=== "[:material-heart-off-outline:](#) Disallowed"

    ```kotlin
    class Bar {
        // Incomplete backing property as public property 'elementList1' is missing
        private val _elementList1 = mutableListOf<Element>()

        // Invalid backing property as '_elementList2' is not a private property
        val _elementList2 = mutableListOf<Element>()
        val elementList2: List<Element>
            get() = _elementList2
    }
    ```

Rule id: `standard:backing-property-naming`

Suppress or disable rule (1)
{ .annotate }

1. Suppress rule in code with annotation below:
    ```kotlin
    @Suppress("ktlint:standard:backing-property-naming")
    ```
   Enable rule via `.editorconfig`
    ```editorconfig
    ktlint_standard_backing-property-naming = enabled
    ```
   Disable rule via `.editorconfig`
    ```editorconfig
    ktlint_standard_backing-property-naming = disabled
    ```

### Class naming

Enforce naming of class and objects.

=== "[:material-heart:](#) Ktlint"

    ```kotlin
    class Foo

    class Foo1

    class `class` // Any keyword is allowed when wrapped between backticks
    ```
=== "[:material-heart:](#) Ktlint JUnit Test"

    ```kotlin
    @Nested
    inner class `Some descriptive class name` {
        @Test
        fun `Some descriptive test name`() {
            // do something
        }
    }
    ```

=== "[:material-heart-off-outline:](#) Disallowed"

    ```kotlin
    class foo
    class Foo_Bar
    class `Some class in the production code`
    ```

!!! note
    Functions in files which import a class from package `org.junit.jupiter.api` are considered to be test functions and are allowed to have a name specified between backticks and do not need to adhere to the normal naming convention. Although, the [Kotlin coding conventions](https://kotlinlang.org/docs/coding-conventions.html) does not allow this explicitly for class identifiers, `ktlint` does allow it.

This rule can also be suppressed with the IntelliJ IDEA inspection suppression `ClassName`.

Rule id: `standard:class-naming`

Suppress or disable rule (1)
{ .annotate }

1. Suppress rule in code with annotation below:
    ```kotlin
    @Suppress("ktlint:standard:class-naming")
    ```
   Enable rule via `.editorconfig`
    ```editorconfig
    ktlint_standard_class-naming = enabled
    ```
   Disable rule via `.editorconfig`
    ```editorconfig
    ktlint_standard_class-naming = disabled
    ```

### Function naming

Enforce naming of function.

=== "[:material-heart:](#) Ktlint"

    ```kotlin
    fun foo() {}

    fun fooBar() {}

    fun `fun`() {} // Any keyword is allowed when wrapped between backticks
    ```
=== "[:material-heart:](#) Ktlint Test"

    ```kotlin
    @Test
    fun `Some name`() {}

    @Test
    fun do_something() {}
    ```

=== "[:material-heart-off-outline:](#) Disallowed"

    ```kotlin
    fun Foo() {}
    fun Foo_Bar() {}
    fun `Some name`() {}
    fun do_something() {}
    ```

| Configuration setting                                                                                                                                                                                 | ktlint_official | intellij_idea | android_studio |
|:------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|:---------------:|:-------------:|:--------------:|
| `ktlint_function_naming_ignore_when_annotated_with`</br><i>Ignore functions that are annotated with values in this setting. This value is a comma separated list of names without the '@' prefix.</i> |     `unset`     |    `unset`    |    `unset`     |

!!! note
    When using Compose, you might want to configure the `function-naming` rule with `.editorconfig` property `ktlint_function_naming_ignore_when_annotated_with=Composable`. Furthermore, you can use a dedicated ktlint ruleset like [Compose Rules](https://mrmans0n.github.io/compose-rules/ktlint/) for checking naming conventions for Composable functions. 

!!! note
    Functions in files which import a class from package `io.kotest`, `junit.framework`, `kotlin.test`, `org.junit`, or `org.testng` are considered to be test functions. Functions in such classes are allowed to have underscores in the name. Also, function names enclosed between backticks do not need to adhere to the normal naming convention.

This rule can also be suppressed with the IntelliJ IDEA inspection suppression `FunctionName`.

Rule id: `standard:function-naming`

Suppress or disable rule (1)
{ .annotate }

1. Suppress rule in code with annotation below:
    ```kotlin
    @Suppress("ktlint:standard:function-naming")
    ```
   Enable rule via `.editorconfig`
    ```editorconfig
    ktlint_standard_function-naming = enabled
    ```
   Disable rule via `.editorconfig`
    ```editorconfig
    ktlint_standard_function-naming = disabled
    ```

### Package name

Validates that the package name matches the regular expression `[a-z][a-zA-Z\d]*(\.[a-z][a-zA-Z\d]*)*`.

=== "[:material-heart:](#) Ktlint"

    ```kotlin
    package foo
    package foo.bar
    ```
=== "[:material-heart-off-outline:](#) Disallowed"

    ```kotlin
    package Foo
    package foo.Foo
    package `foo bar`
    package foo.`foo bar`
    ```

Rule id: `standard:package-name`

Suppress or disable rule (1)
{ .annotate }

1. Suppress rule in code with annotation below:
    ```kotlin
    @Suppress("ktlint:standard:package-name")
    ```
   Enable rule via `.editorconfig`
    ```editorconfig
    ktlint_standard_package-name = enabled
    ```
   Disable rule via `.editorconfig`
    ```editorconfig
    ktlint_standard_package-name = disabled
    ```

### Property naming

Enforce naming of property.

!!! note
    This rule can not reliably detect all situations in which incorrect property naming is used. So it only detects in which it is certain that naming is incorrect.

=== "[:material-heart:](#) Ktlint"

    ```kotlin
    val foo1 = Foo() // In case developers want to tell that Foo is mutable
    val FOO1 = Foo() // In case developers want to tell that Foo is deeply immutable

    const val FOO_BAR = "FOO-BAR" // By definition deeply immutable

    var foo2: Foo = Foo() // By definition not immutable

    class Bar {
        val foo1 = "foo1" // Class properties always start with lowercase, const is not allowed

        const val FOO_BAR = "FOO-BAR" // By definition deeply immutable

        var foo2: Foo = Foo() // By definition not immutable

        // Backing property
        private val _elementList = mutableListOf<Element>()
        val elementList: List<Element>
            get() = _elementList
    
        companion object {
            val foo1 = Foo() // In case developer want to communicate that Foo is mutable
            val FOO1 = Foo() // In case developer want to communicate that Foo is deeply immutable
        }
    }

    var `package` = "foo" // Any keyword is allowed when wrapped between backticks
    ```
=== "[:material-heart-off-outline:](#) Disallowed"

    ```kotlin
    const val fooBar = "FOO-BAR" // By definition deeply immutable

    var FOO2: Foo = Foo() // By definition not immutable

    class Bar {
        val FOO_BAR = "FOO-BAR" // Class properties always start with lowercase, const is not allowed

        // Incomplete backing property as public property 'elementList1' is missing
        private val _elementList1 = mutableListOf<Element>()

        // Invalid backing property as '_elementList2' is not a private property
        val _elementList2 = mutableListOf<Element>()
        val elementList2: List<Element>
            get() = _elementList2
    }
    ```

This rule is suppressed whenever the IntelliJ IDEA inspection suppression `PropertyName`, `ConstPropertyName`, `ObjectPropertyName` or `PrivatePropertyName` is used.

| Configuration setting                                                                                                                                 |    ktlint_official     |     intellij_idea      |     android_studio     |
|:------------------------------------------------------------------------------------------------------------------------------------------------------|:----------------------:|:----------------------:|:----------------------:|
| `ktlint_property_naming_constant_naming`<br/><i>The naming style ('screaming_snake_case', or 'pascal_case') to be applied on constant properties.</i> | `screaming_snake_case` | `screaming_snake_case` | `screaming_snake_case` |

!!! note
    When using Compose, you might want to configure the `property-naming` rule with `.editorconfig` property `ktlint_property_naming_constant_naming = pascal_case`.

Rule id: `standard:property-naming`

Suppress or disable rule (1)
{ .annotate }

1. Suppress rule in code with annotation below:
    ```kotlin
    @Suppress("ktlint:standard:property-naming")
    ```
   Enable rule via `.editorconfig`
    ```editorconfig
    ktlint_standard_property-naming = enabled
    ```
   Disable rule via `.editorconfig`
    ```editorconfig
    ktlint_standard_property-naming = disabled
    ```

## No blank lines in list

Disallow blank lines to be used in lists before the first element, between elements, and after the last element.

*Super type*

=== "[:material-heart:](#) Ktlint"

    ```kotlin
    class FooBar :
        Foo,
        Bar {
        // body
    }
    ```

=== "[:material-heart-off-outline:](#) Disallowed"

    ```kotlin
    class FooBar :

        Foo,

        Bar

    {
        // body
    }
    ```

*Type argument list*

=== "[:material-heart:](#) Ktlint"

    ```kotlin
    val foobar: FooBar<
        Foo,
        Bar,
        > = FooBar(Foo(), Bar())
    ```

=== "[:material-heart-off-outline:](#) Disallowed"

    ```kotlin
    val foobar: FooBar<

        Foo,

        Bar,

        > = FooBar(Foo(), Bar())
    ```

*Type constraint list*

=== "[:material-heart:](#) Ktlint"

    ```kotlin
    class BiAdapter<C : RecyclerView.ViewHolder, V1 : C, V2 : C, out A1, out A2>(
        val adapter1: A1,
        val adapter2: A2,
    ) : RecyclerView.Adapter<C>()
        where A1 : RecyclerView.Adapter<V1>, A1 : ComposableAdapter.ViewTypeProvider,
              A2 : RecyclerView.Adapter<V2>, A2 : ComposableAdapter.ViewTypeProvider {
        // body
    }
    ```

=== "[:material-heart-off-outline:](#) Disallowed"

    ```kotlin
    class BiAdapter<C : RecyclerView.ViewHolder, V1 : C, V2 : C, out A1, out A2>(
        val adapter1: A1,
        val adapter2: A2
    ) : RecyclerView.Adapter<C>()
        where
              A1 : RecyclerView.Adapter<V1>, A1 : ComposableAdapter.ViewTypeProvider,

              A2 : RecyclerView.Adapter<V2>, A2 : ComposableAdapter.ViewTypeProvider
    {
        // body
    }
    ```

*Type parameter list*

=== "[:material-heart:](#) Ktlint"

    ```kotlin
    fun <
        Foo,
        Bar,
        > foobar()
    ```

=== "[:material-heart-off-outline:](#) Disallowed"

    ```kotlin
    fun <

        Foo,

        Bar,

        > foobar()
    ```

*Value argument list*

=== "[:material-heart:](#) Ktlint"

    ```kotlin
    val foobar =
        foobar(
            "foo",
            "bar",
        )
    ```

=== "[:material-heart-off-outline:](#) Disallowed"

    ```kotlin
    val foobar = 
        foobar(
  
          "foo",
  
          "bar",
  
      )
    ```

*Value parameter list*

=== "[:material-heart:](#) Ktlint"

    ```kotlin
    fun foobar(
        foo: String,
        bar: String,
    )
    ```

=== "[:material-heart-off-outline:](#) Disallowed"

    ```kotlin
    fun foobar(

        foo: String,

        bar: String,

    )
    ```

Rule id: `standard:no-blank-line-in-list`

Suppress or disable rule (1)
{ .annotate }

1. Suppress rule in code with annotation below:
    ```kotlin
    @Suppress("ktlint:standard:no-blank-line-in-list")
    ```
   Enable rule via `.editorconfig`
    ```editorconfig
    ktlint_standard_no-blank-line-in-list = enabled
    ```
   Disable rule via `.editorconfig`
    ```editorconfig
    ktlint_standard_no-blank-line-in-list = disabled
    ```

!!! Note
    This rule is only run when `ktlint_code_style` is set to `ktlint_official` or when the rule is enabled explicitly.

## No consecutive comments

Consecutive comments are disallowed in following cases:

- Any mix of a consecutive kdoc, a block comment or an EOL comment unless separated by a blank line in between

- Consecutive KDocs (even when separated by a blank line)

- Consecutive block comments (even when separated by a blank line)

Consecutive EOL comments are always allowed as they are often used instead of a block comment.

=== "[:material-heart:](#) Ktlint"

    ```kotlin
    // An EOL comment
    // may be followed by another EOL comment
    val foo = "foo"

    // Different comment types (including KDoc) may be consecutive ..

    /*
     * ... but do need to be separated by a blank line ...
     */

    /**
      * ... but a KDoc can not be followed by an EOL or a block comment or another KDoc
      */
    fun bar() = "bar"
    ```

=== "[:material-heart-off-outline:](#) Disallowed"

    ```kotlin
    /*
     * Block comments can not be consecutive ...
     */
    /*
     * ... even not when separated by a new line.
     */
    val bar = "bar" 

    /**
      * A KDoc can not be followed by a block comment or an EOL comment or another KDOC
      */

    // ... even not when separated by a new line.
    ```

Rule id: `standard:no-consecutive-comments`

Suppress or disable rule (1)
{ .annotate }

1. Suppress rule in code with annotation below:
    ```kotlin
    @Suppress("ktlint:standard:no-consecutive-comments")
    ```
   Enable rule via `.editorconfig`
    ```editorconfig
    ktlint_standard_no-consecutive-comments = enabled
    ```
   Disable rule via `.editorconfig`
    ```editorconfig
    ktlint_standard_no-consecutive-comments = disabled
    ```

!!! Note
    This rule is only run when `ktlint_code_style` is set to `ktlint_official` or when the rule is enabled explicitly.

## No empty file

A kotlin (script) file should not be empty. It needs to contain at least one declaration. Files only contain a package and/or import statements are as of that disallowed.

Rule id: `standard:no-empty-file`

Suppress or disable rule (1)
{ .annotate }

1. Suppress rule in code with annotation below:
    ```kotlin
    @Suppress("ktlint:standard:no-empty-file")
    ```
   Enable rule via `.editorconfig`
    ```editorconfig
    ktlint_standard_no-empty-file = enabled
    ```
   Disable rule via `.editorconfig`
    ```editorconfig
    ktlint_standard_no-empty-file = disabled
    ```

## No empty first line at start in class body

Detect blank lines at the start of a class body.

=== "[:material-heart:](#) Ktlint"

    ```kotlin
    class Foo {
        val foo = "foo"
    }
    ```

=== "[:material-heart-off-outline:](#) Disallowed"

    ```kotlin
    class Foo {

        val foo = "foo"
    }
    ```

Rule id: `standard:no-empty-first-line-in-class-body`

Suppress or disable rule (1)
{ .annotate }

1. Suppress rule in code with annotation below:
    ```kotlin
    @Suppress("ktlint:standard:no-empty-first-line-in-class-body")
    ```
   Enable rule via `.editorconfig`
    ```editorconfig
    ktlint_standard_no-empty-first-line-in-class-body = enabled
    ```
   Disable rule via `.editorconfig`
    ```editorconfig
    ktlint_standard_no-empty-first-line-in-class-body = disabled
    ```

!!! Note
    This rule is only run when `ktlint_code_style` is set to `ktlint_official` or when the rule is enabled explicitly.


## No single line block comment

A single line block comment should be replaced with an EOL comment when possible.

=== "[:material-heart:](#) Ktlint"

    ```kotlin
    /*
     * Some comment
     */
    val foo = "foo" // Some comment
    val foo = { /* no-op */ }
    ```
=== "[:material-heart-off-outline:](#) Disallowed"

    ```kotlin
    /* Some comment */
    val foo = "foo" /* Some comment */
    ```

Rule id: `standard:no-single-line-block-comment`

Suppress or disable rule (1)
{ .annotate }

1. Suppress rule in code with annotation below:
    ```kotlin
    @Suppress("ktlint:standard:no-single-line-block-comment")
    ```
   Enable rule via `.editorconfig`
    ```editorconfig
    ktlint_standard_no-single-line-block-comment = enabled
    ```
   Disable rule via `.editorconfig`
    ```editorconfig
    ktlint_standard_no-single-line-block-comment = disabled
    ```

## Ktlint-suppression rule

The `ktlint-disable` and `ktlint-enable` directives are no longer supported as of ktlint version `0.50.0`. This rule migrates the directives to Suppress or SuppressWarnings annotations.

Identifiers in the @Suppress and @SuppressWarnings annotations to suppress ktlint rules are checked for validity and autocorrected when possible.

=== "[:material-heart:](#) Ktlint"

    ```kotlin
    @file:Suppress("ktlint:standard:no-wildcard-imports")

    class FooBar {
        @Suppress("ktlint:standard:max-line-length")
        val foo = "some longggggggggggggggggggg text"

        fun bar() =
            @Suppress("ktlint:standard:no-multi-spaces")
            listOf(
                "1   One", 
                "10  Ten", 
                "100 Hundred", 
            )
    }
    ```
=== "[:material-heart-off-outline:](#) Disallowed"

    ```kotlin
    /* ktlint-disable standard:no-wildcard-imports */

    class FooBar {
        val foo = "some longggggggggggggggggggg text" // ktlint-disable standard:max-line-length

        fun bar() =
            listOf(
                /* ktlint-disable standard:no-multi-spaces */
                "1   One", 
                "10  Ten", 
                "100 Hundred", 
                /* ktlint-enable standard:no-multi-spaces */
            )
    }
    ```

Rule id: `standard:ktlint-suppression`

!!! note
    This rule cannot be suppressed via `@Suppress` or be disabled in the `.editorconfig`.

## Max line length

Ensures that lines do not exceed the maximum length of a line as specified in `.editorconfig` property `max_line_length`.

This rule does not apply in a number of situations. The `.editorconfig` property `ktlint_ignore_back_ticked_identifier` can be set to ignore identifiers which are enclosed in backticks, which for example is very useful when you want to allow longer names for unit tests.

=== "[:material-heart:](#) Ktlint"

    ```kotlin
    // Assume that the last allowed character is
    // at the X character on the right           X
    // Lines below are accepted although the max
    // line length is exceeded.
    package com.toooooooooooooooooooooooooooo.long
    import com.tooooooooooooooooooooooooooooo.long

    val foo1 =
        """
        fooooooooooooooooooooooooooooooooooooooooo
        """

    val foo2 =
        "fooooooooooooooooooooooooooooooooooooooo"

    @Test
    fun `Test description which is toooooooooooo long`() {
    }
    ```
=== "[:material-heart-off-outline:](#) Disallowed"

    ```kotlin
    // Assume that the last allowed character is
    // at the X character on the right           X
    val fooooooooooooooo = "fooooooooooooooooooooo"
    val foo = "foo" + "ooooooooooooooooooooooooooo"
    val foooooooooooooo = "foooooooooooooooooooo" // some comment
    ```


| Configuration setting                                                                                                                                                                                                         | ktlint_official | intellij_idea | android_studio |
|:------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|:---------------:|:-------------:|:--------------:|
| `ktlint_ignore_back_ticked_identifier`<br/><i>Defines whether the backticked identifier (``) should be ignored.</i>                                                                                                           |     `false`     |    `false`    |    `false`     |
| `max_line_length`<br/><i>Maximum length of a (regular) line. This property is ignored in case the `max-line-length` rule is disabled, or when using Ktlint via a third party integration that does not provide this rule.</i> |       140       |     `off`     |     `100`      |

Rule id: `standard:max-line-length`

Suppress or disable rule (1)
{ .annotate }

1. Suppress rule in code with annotation below:
    ```kotlin
    @Suppress("ktlint:standard:max-line-length")
    ```
   Enable rule via `.editorconfig`
    ```editorconfig
    ktlint_standard_max-line-length = enabled
    ```
   Disable rule via `.editorconfig`
    ```editorconfig
    ktlint_standard_max-line-length = disabled
    ```

## Modifier order

Requires consistent order of modifiers.

=== "[:material-heart:](#) Ktlint"

    ```kotlin
    abstract class A {
        protected open val v = ""

        internal open suspend fun f(v: Any): Any = ""

        protected lateinit var lv: String
    }
    ```
=== "[:material-heart-off-outline:](#) Disallowed"

    ```kotlin
    abstract class A {
        open protected val v = ""

        open suspend internal fun f(v: Any): Any = ""

        lateinit protected var lv: String
    }
    ```

Rule id: `standard:modifier-order`

Suppress or disable rule (1)
{ .annotate }

1. Suppress rule in code with annotation below:
    ```kotlin
    @Suppress("ktlint:standard:modifier-order")
    ```
   Enable rule via `.editorconfig`
    ```editorconfig
    ktlint_standard_modifier-order = enabled
    ```
   Disable rule via `.editorconfig`
    ```editorconfig
    ktlint_standard_modifier-order = disabled
    ```

## Multiline if-else

Braces required for multiline if/else statements.

=== "[:material-heart:](#) Ktlint"

    ```kotlin
    val foo =
        if (true) {
            return 0
        } else {
            return 1
        }
    ```
=== "[:material-heart-off-outline:](#) Disallowed"

    ```kotlin
    val foo =
        if (true)
            return 0
        else
            return 1
    ```

Rule id: `standard:multiline-if-else`

Suppress or disable rule (1)
{ .annotate }

1. Suppress rule in code with annotation below:
    ```kotlin
    @Suppress("ktlint:standard:multiline-if-else")
    ```
   Enable rule via `.editorconfig`
    ```editorconfig
    ktlint_standard_multiline-if-else = enabled
    ```
   Disable rule via `.editorconfig`
    ```editorconfig
    ktlint_standard_multiline-if-else = disabled
    ```

## Multiline loop

Braces required for multiline for, while, and do statements.

=== "[:material-heart:](#) Ktlint"

    ```kotlin
    for (i in 1..10) {
        println(i)
    }
    ```

=== "[:material-heart-off-outline:](#) Disallowed"

    ```kotlin
    for (i in 1..10)
        println(i)
    ```

Rule id: `standard:multiline-loop`

Suppress or disable rule (1)
{ .annotate }

1. Suppress rule in code with annotation below:
    ```kotlin
    @Suppress("ktlint:standard:multiline-loop")
    ```
   Enable rule via `.editorconfig`
    ```editorconfig
    ktlint_standard_multiline-loop = enabled
    ```
   Disable rule via `.editorconfig`
    ```editorconfig
    ktlint_standard_multiline-loop = disabled
    ```

## No blank lines before `}`

No blank lines before `}`.

=== "[:material-heart:](#) Ktlint"

    ```kotlin
    fun main() {
        fun a() {
        }

        fun b()
    }
    ```
=== "[:material-heart-off-outline:](#) Disallowed"

    ```kotlin
    fun main() {
        fun a() {

        }
        fun b()

    }
    ```

Rule id: `standard:no-blank-line-before-rbrace`

Suppress or disable rule (1)
{ .annotate }

1. Suppress rule in code with annotation below:
    ```kotlin
    @Suppress("ktlint:standard:no-blank-line-before-rbrace")
    ```
   Enable rule via `.editorconfig`
    ```editorconfig
    ktlint_standard_no-blank-line-before-rbrace = enabled
    ```
   Disable rule via `.editorconfig`
    ```editorconfig
    ktlint_standard_no-blank-line-before-rbrace = disabled
    ```

## No blank lines in chained method calls

=== "[:material-heart:](#) Ktlint"

    ```kotlin
    fun foo(inputText: String) {
        inputText
            .lowercase(Locale.getDefault())
    }
    ```
=== "[:material-heart-off-outline:](#) Disallowed"

    ```kotlin
    fun foo(inputText: String) {
        inputText

            .lowercase(Locale.getDefault())
    }
    ```

Rule id: `standard:no-blank-lines-in-chained-method-calls`

Suppress or disable rule (1)
{ .annotate }

1. Suppress rule in code with annotation below:
    ```kotlin
    @Suppress("ktlint:standard:no-blank-lines-in-chained-method-calls")
    ```
   Enable rule via `.editorconfig`
    ```editorconfig
    ktlint_standard_no-blank-lines-in-chained-method-calls = enabled
    ```
   Disable rule via `.editorconfig`
    ```editorconfig
    ktlint_standard_no-blank-lines-in-chained-method-calls = disabled
    ```

## No consecutive blank lines

=== "[:material-heart:](#) Ktlint"

    ```kotlin
    package com.test

    import com.test.util

    val a = "a"

    fun b() {
    }

    fun c()
    ```
=== "[:material-heart-off-outline:](#) Disallowed"

    ```kotlin
    package com.test


    import com.test.util


    val a = "a"


    fun b() {
    }


    fun c()
    ```

Rule id: `standard:no-consecutive-blank-lines`

Suppress or disable rule (1)
{ .annotate }

1. Suppress rule in code with annotation below:
    ```kotlin
    @Suppress("ktlint:standard:no-consecutive-blank-lines")
    ```
   Enable rule via `.editorconfig`
    ```editorconfig
    ktlint_standard_no-consecutive-blank-lines = enabled
    ```
   Disable rule via `.editorconfig`
    ```editorconfig
    ktlint_standard_no-consecutive-blank-lines = disabled
    ```

## No empty (`{}`) class bodies

=== "[:material-heart:](#) Ktlint"

    ```kotlin
    class C

    data class DC(val v: Any)

    interface I

    object O
    ```
=== "[:material-heart-off-outline:](#) Disallowed"

    ```kotlin
    class C {}

    data class DC(val v: Any) { }

    interface I {
    }

    object O{}
    ```

Rule id: `standard:no-empty-class-body`

Suppress or disable rule (1)
{ .annotate }

1. Suppress rule in code with annotation below:
    ```kotlin
    @Suppress("ktlint:standard:no-empty-class-body")
    ```
   Enable rule via `.editorconfig`
    ```editorconfig
    ktlint_standard_no-empty-class-body = enabled
    ```
   Disable rule via `.editorconfig`
    ```editorconfig
    ktlint_standard_no-empty-class-body = disabled
    ```

## No leading empty lines in method blocks

=== "[:material-heart:](#) Ktlint"

    ```kotlin
    fun bar() {
       val a = 2
    }
    ```
=== "[:material-heart-off-outline:](#) Disallowed"

    ```kotlin
    fun bar() {

       val a = 2
    }
    ```

Rule id: `standard:no-empty-first-line-in-method-block`

Suppress or disable rule (1)
{ .annotate }

1. Suppress rule in code with annotation below:
    ```kotlin
    @Suppress("ktlint:standard:no-empty-first-line-in-method-block")
    ```
   Enable rule via `.editorconfig`
    ```editorconfig
    ktlint_standard_no-empty-first-line-in-method-block = enabled
    ```
   Disable rule via `.editorconfig`
    ```editorconfig
    ktlint_standard_no-empty-first-line-in-method-block = disabled
    ```

## No line break after else

Disallows line breaks after the else keyword if that could lead to confusion, for example:

=== "[:material-heart:](#) Ktlint"

    ```kotlin
    fun funA() {
        if (conditionA()) {
            doSomething()
        } else if (conditionB()) {
            doAnotherThing()
        }
    }
    ```
=== "[:material-heart-off-outline:](#) Disallowed"

    ```kotlin
    fun funA() {
        if (conditionA()) {
            doSomething()
        } else
        if (conditionB()) {
            doAnotherThing()
        }
    }
    ```

Rule id: `standard:no-line-break-after-else`

Suppress or disable rule (1)
{ .annotate }

1. Suppress rule in code with annotation below:
    ```kotlin
    @Suppress("ktlint:standard:no-line-break-after-else")
    ```
   Enable rule via `.editorconfig`
    ```editorconfig
    ktlint_standard_no-line-break-after-else = enabled
    ```
   Disable rule via `.editorconfig`
    ```editorconfig
    ktlint_standard_no-line-break-after-else = disabled
    ```

## No line break before assignment

When a line is broken at an assignment (`=`) operator the break comes after the symbol.

=== "[:material-heart:](#) Ktlint"

    ```kotlin
    val valA =
        ""
    ```
=== "[:material-heart-off-outline:](#) Disallowed"

    ```kotlin
    val valA
        = ""
    ```

Rule id: `standard:no-line-break-before-assignment`

Suppress or disable rule (1)
{ .annotate }

1. Suppress rule in code with annotation below:
    ```kotlin
    @Suppress("ktlint:standard:no-line-break-before-assignment")
    ```
   Enable rule via `.editorconfig`
    ```editorconfig
    ktlint_standard_no-line-break-before-assignment = enabled
    ```
   Disable rule via `.editorconfig`
    ```editorconfig
    ktlint_standard_no-line-break-before-assignment = disabled
    ```

## No multi spaces

Except in indentation and in KDoc's it is not allowed to have multiple consecutive spaces.

=== "[:material-heart:](#) Ktlint"

    ```kotlin
    fun main() {
        x(1, 3)
    }
    ```
=== "[:material-heart-off-outline:](#) Disallowed"

    ```kotlin
    fun  main()  {
        x(1,  3)
    }
    ```

Rule id: `standard:no-multi-spaces`

Suppress or disable rule (1)
{ .annotate }

1. Suppress rule in code with annotation below:
    ```kotlin
    @Suppress("ktlint:standard:no-multi-spaces")
    ```
   Enable rule via `.editorconfig`
    ```editorconfig
    ktlint_standard_no-multi-spaces = enabled
    ```
   Disable rule via `.editorconfig`
    ```editorconfig
    ktlint_standard_no-multi-spaces = disabled
    ```

## No semicolons

Avoid using unnecessary semicolons.

=== "[:material-heart:](#) Ktlint"

    ```kotlin
    fun foo() {
        bar()

        bar()
    }
    ```
=== "[:material-heart-off-outline:](#) Disallowed"

    ```kotlin
    fun foo() {
        ;
        bar()
        ;

        bar()

        ;
    }
    ```

Rule id: `standard:no-semi`

Suppress or disable rule (1)
{ .annotate }

1. Suppress rule in code with annotation below:
    ```kotlin
    @Suppress("ktlint:standard:no-semi")
    ```
   Enable rule via `.editorconfig`
    ```editorconfig
    ktlint_standard_no-semi = enabled
    ```
   Disable rule via `.editorconfig`
    ```editorconfig
    ktlint_standard_no-semi = disabled
    ```

## No trailing whitespaces

Rule id: `standard:no-trailing-spaces`

Suppress or disable rule (1)
{ .annotate }

1. Suppress rule in code with annotation below:
    ```kotlin
    @Suppress("ktlint:standard:no-trailing-spaces")
    ```
   Enable rule via `.editorconfig`
    ```editorconfig
    ktlint_standard_no-trailing-spaces = enabled
    ```
   Disable rule via `.editorconfig`
    ```editorconfig
    ktlint_standard_no-trailing-spaces = disabled
    ```

## No `Unit` as return type

The `Unit` type is not allowed as return-type of a function.

=== "[:material-heart:](#) Ktlint"

    ```kotlin
    fun fn() {}
    ```
=== "[:material-heart-off-outline:](#) Disallowed"

    ```kotlin
    fun fn(): Unit {}
    ```

Rule id: `standard:no-unit-return`

Suppress or disable rule (1)
{ .annotate }

1. Suppress rule in code with annotation below:
    ```kotlin
    @Suppress("ktlint:standard:no-unit-return")
    ```
   Enable rule via `.editorconfig`
    ```editorconfig
    ktlint_standard_no-unit-return = enabled
    ```
   Disable rule via `.editorconfig`
    ```editorconfig
    ktlint_standard_no-unit-return = disabled
    ```

## No unused imports

!!! important
    Starting from KtLint `1.7.0` this rule no longer runs by default. In some cases the rule marks imports as unused, while after removal of the import the code no longer compiles.
    If you insist on running the rule despite its [shortcomings](https://github.com/ktlint/ktlint/issues/3038), you need to enable the rule in the `.editorconfig` by adding:
    ```
    ktlint_standard_no-unused-imports = enabled
    ```
    Newly reported shortcomings to this rule are not guaranteed to be fixed by maintainers of KtLint.

Rule id: `standard:no-unused-imports`

Suppress or disable rule (1)
{ .annotate }

1. Suppress rule in code with annotation below:
    ```kotlin
    @Suppress("ktlint:standard:no-unused-imports")
    ```
   Enable rule via `.editorconfig`
    ```editorconfig
    ktlint_standard_no-unused-imports = enabled
    ```
   Disable rule via `.editorconfig`
    ```editorconfig
    ktlint_standard_no-unused-imports = disabled
    ```

## No wildcard imports

No wildcard imports except whitelisted imports.

=== "[:material-heart:](#) Ktlint"

    ```kotlin
    import foobar.Bar
    import foobar.Foo
    ```
=== "[:material-heart-off-outline:](#) Disallowed"

    ```kotlin
    import foobar.*
    ```

| Configuration setting                                                                                               | ktlint_official |                   intellij_idea                   |                  android_studio                   |
|:--------------------------------------------------------------------------------------------------------------------|:---------------:|:-------------------------------------------------:|:-------------------------------------------------:|
| `ij_kotlin_packages_to_use_import_on_demand`<br/><i>Defines allowed wildcard imports as a comma separated list.</i> |        -        | `java.util.*,`<br/>`kotlinx.android.synthetic.**` | `java.util.*,`<br/>`kotlinx.android.synthetic.**` |

!!! warning
    In case property `ij_kotlin_packages_to_use_import_on_demand` is not explicitly set, Intellij IDEA allows wildcards imports like `java.util.*` which lead to conflicts with the `no-wildcard-imports` rule. See [Intellij IDEA configuration](configuration-intellij-idea.md) to prevent such conflicts.

Rule id: `standard:no-wildcard-imports`

Suppress or disable rule (1)
{ .annotate }

1. Suppress rule in code with annotation below:
    ```kotlin
    @Suppress("ktlint:standard:no-wildcard-imports")
    ```
   Enable rule via `.editorconfig`
    ```editorconfig
    ktlint_standard_no-wildcard-imports = enabled
    ```
   Disable rule via `.editorconfig`
    ```editorconfig
    ktlint_standard_no-wildcard-imports = disabled
    ```

## Spacing

### Angle bracket spacing

No spaces around angle brackets when used for typing.

=== "[:material-heart:](#) Ktlint"

    ```kotlin
    val a: Map<Int, String> = mapOf()
    val b: Map<Int, String> = mapOf()
    val c: Map<Int, String> = mapOf()
    ```
=== "[:material-heart-off-outline:](#) Disallowed"

    ```kotlin
    val a: Map< Int, String> = mapOf()
    val b: Map<Int, String > = mapOf()
    val c: Map <Int, String> = mapOf()
    ```

Rule id: `standard:spacing-around-angle-brackets`

Suppress or disable rule (1)
{ .annotate }

1. Suppress rule in code with annotation below:
    ```kotlin
    @Suppress("ktlint:standard:spacing-around-angle-brackets")
    ```
   Enable rule via `.editorconfig`
    ```editorconfig
    ktlint_standard_spacing-around-angle-brackets = enabled
    ```
   Disable rule via `.editorconfig`
    ```editorconfig
    ktlint_standard_spacing-around-angle-brackets = disabled
    ```

### Annotation spacing

Annotations should be separated by a single line break.

=== "[:material-heart:](#) Ktlint"

    ```kotlin
    @JvmField
    fun foo() {}

    /**
     * block comment
     */
    @Foo @Bar
    class FooBar {
    }
    ```
=== "[:material-heart-off-outline:](#) Disallowed"

    ```kotlin
    @JvmField

    fun foo() {}

    @Foo @Bar
    /**
     * block comment
     */
    class FooBar {
    }
    ```

Rule id: `standard:annotation-spacing`

Suppress or disable rule (1)
{ .annotate }

1. Suppress rule in code with annotation below:
    ```kotlin
    @Suppress("ktlint:standard:annotation-spacing")
    ```
   Enable rule via `.editorconfig`
    ```editorconfig
    ktlint_standard_annotation-spacing = enabled
    ```
   Disable rule via `.editorconfig`
    ```editorconfig
    ktlint_standard_annotation-spacing = disabled
    ```

### Blank line between declarations with annotations

Declarations with annotations should be separated by a blank line.

=== "[:material-heart:](#) Ktlint"

    ```kotlin
    fun a()

    @Bar
    fun b()
    ```
=== "[:material-heart-off-outline:](#) Disallowed"

    ```kotlin
    fun a()
    @Bar
    fun b()
    ```

Rule id: `standard:spacing-between-declarations-with-annotations`

Suppress or disable rule (1)
{ .annotate }

1. Suppress rule in code with annotation below:
    ```kotlin
    @Suppress("ktlint:standard:spacing-between-declarations-with-annotations")
    ```
   Enable rule via `.editorconfig`
    ```editorconfig
    ktlint_standard_spacing-between-declarations-with-annotations = enabled
    ```
   Disable rule via `.editorconfig`
    ```editorconfig
    ktlint_standard_spacing-between-declarations-with-annotations = disabled
    ```

### Blank line between declaration with comments

Declarations with comments should be separated by a blank line.

=== "[:material-heart:](#) Ktlint"

    ```kotlin
    // some comment 1
    bar()

    /*
     * some comment 2
     */
    foo()
    ```
=== "[:material-heart-off-outline:](#) Disallowed"

    ```kotlin
    // some comment 1
    bar()
    /*
     * some comment 2
     */
    foo()
    ```

Rule id: `standard:spacing-between-declarations-with-comments`

Suppress or disable rule (1)
{ .annotate }

1. Suppress rule in code with annotation below:
    ```kotlin
    @Suppress("ktlint:standard:spacing-between-declarations-with-comments")
    ```
   Enable rule via `.editorconfig`
    ```editorconfig
    ktlint_standard_spacing-between-declarations-with-comments = enabled
    ```
   Disable rule via `.editorconfig`
    ```editorconfig
    ktlint_standard_spacing-between-declarations-with-comments = disabled
    ```

### Colon spacing

Consistent spacing around colon.

=== "[:material-heart:](#) Ktlint"

    ```kotlin
    class A : B

    class A2 : B2
    ```
=== "[:material-heart-off-outline:](#) Disallowed"

    ```kotlin
    class A:B

    class A2  :  B2
    ```

Rule id: `standard:colon-spacing`

Suppress or disable rule (1)
{ .annotate }

1. Suppress rule in code with annotation below:
    ```kotlin
    @Suppress("ktlint:standard:colon-spacing")
    ```
   Enable rule via `.editorconfig`
    ```editorconfig
    ktlint_standard_colon-spacing = enabled
    ```
   Disable rule via `.editorconfig`
    ```editorconfig
    ktlint_standard_colon-spacing = disabled
    ```

### Comma spacing

Consistent spacing around comma.

=== "[:material-heart:](#) Ktlint"

    ```kotlin
    val foo1 = Foo(1, 3)
    val foo2 = Foo(1, 3)
    ```
=== "[:material-heart-off-outline:](#) Disallowed"

    ```kotlin
    val foo1 = Foo(1 ,3)
    val foo2 = Foo(1,3)
    ```

Rule id: `standard:comma-spacing`

Suppress or disable rule (1)
{ .annotate }

1. Suppress rule in code with annotation below:
    ```kotlin
    @Suppress("ktlint:standard:comma-spacing")
    ```
   Enable rule via `.editorconfig`
    ```editorconfig
    ktlint_standard_comma-spacing = enabled
    ```
   Disable rule via `.editorconfig`
    ```editorconfig
    ktlint_standard_comma-spacing = disabled
    ```

### Comment spacing

The end of line comment sign `//` should be preceded and followed by exactly a space.

=== "[:material-heart:](#) Ktlint"

    ```kotlin
    // comment
    var debugging = false // comment
    var debugging = false // comment
    var debugging = false // comment
    
    fun main() {
        System.out.println(
            // comment
            "test",
        )
    } // comment
    ```

=== "[:material-heart-off-outline:](#) Disallowed"

    ```kotlin
    //comment
    var debugging = false// comment
    var debugging = false //comment
    var debugging = false//comment

    fun main() {
        System.out.println(
             //123
            "test"
        )
    }//comment
    ```

Rule id: `standard:comment-spacing`

Suppress or disable rule (1)
{ .annotate }

1. Suppress rule in code with annotation below:
    ```kotlin
    @Suppress("ktlint:standard:comment-spacing")
    ```
   Enable rule via `.editorconfig`
    ```editorconfig
    ktlint_standard_comment-spacing = enabled
    ```
   Disable rule via `.editorconfig`
    ```editorconfig
    ktlint_standard_comment-spacing = disabled
    ```

### Curly spacing

Consistent spacing around curly braces.

=== "[:material-heart:](#) Ktlint"

    ```kotlin
    val foo = bar { foo() }
    ```
=== "[:material-heart-off-outline:](#) Disallowed"

    ```kotlin
    val foo = bar{foo()}
    ```

Rule id: `standard:curly-spacing`

Suppress or disable rule (1)
{ .annotate }

1. Suppress rule in code with annotation below:
    ```kotlin
    @Suppress("ktlint:standard:curly-spacing")
    ```
   Enable rule via `.editorconfig`
    ```editorconfig
    ktlint_standard_curly-spacing = enabled
    ```
   Disable rule via `.editorconfig`
    ```editorconfig
    ktlint_standard_curly-spacing = disabled
    ```

### Dot spacing

Consistent spacing around dots.

=== "[:material-heart:](#) Ktlint"

    ```kotlin
    fun String.foo() = "foo"
    ```
=== "[:material-heart-off-outline:](#) Disallowed"

    ```kotlin
    fun String . foo() = "foo"
    ```

Rule id: `standard:dot-spacing`

Suppress or disable rule (1)
{ .annotate }

1. Suppress rule in code with annotation below:
    ```kotlin
    @Suppress("ktlint:standard:dot-spacing")
    ```
   Enable rule via `.editorconfig`
    ```editorconfig
    ktlint_standard_dot-spacing = enabled
    ```
   Disable rule via `.editorconfig`
    ```editorconfig
    ktlint_standard_dot-spacing = disabled
    ```

### Double colon spacing

No spaces around `::`.

=== "[:material-heart:](#) Ktlint"

    ```kotlin
    val foo = Foo::class
    ```
=== "[:material-heart-off-outline:](#) Disallowed"

    ```kotlin
    val foo1 = Foo ::class
    val foo2 = Foo:: class
    val foo3 = Foo :: class
    val foo4 = Foo::
        class
    ```

Rule id: `standard:double-colon-spacing`

Suppress or disable rule (1)
{ .annotate }

1. Suppress rule in code with annotation below:
    ```kotlin
    @Suppress("ktlint:standard:double-colon-spacing")
    ```
   Enable rule via `.editorconfig`
    ```editorconfig
    ktlint_standard_double-colon-spacing = enabled
    ```
   Disable rule via `.editorconfig`
    ```editorconfig
    ktlint_standard_double-colon-spacing = disabled
    ```

### Function return type spacing

Consistent spacing around the function return type.

=== "[:material-heart:](#) Ktlint"

    ```kotlin
    fun foo(): String = "some-result"
    ```
=== "[:material-heart-off-outline:](#) Disallowed"

    ```kotlin
    fun foo1() : String = "some-result"

    fun foo2():  String = "some-result"

    fun foo3():String = "some-result"

    fun foo4():
        String = "some-result"
    ```

| Configuration setting                                                                                                                                                                                                         | ktlint_official | intellij_idea | android_studio |
|:------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|:---------------:|:-------------:|:--------------:|
| `max_line_length`<br/><i>Maximum length of a (regular) line. This property is ignored in case the `max-line-length` rule is disabled, or when using Ktlint via a third party integration that does not provide this rule.</i> |       140       |     `off`     |     `100`      |

Rule id: `standard:function-return-type-spacing`

Suppress or disable rule (1)
{ .annotate }

1. Suppress rule in code with annotation below:
    ```kotlin
    @Suppress("ktlint:standard:function-return-type-spacing")
    ```
   Enable rule via `.editorconfig`
    ```editorconfig
    ktlint_standard_function-return-type-spacing = enabled
    ```
   Disable rule via `.editorconfig`
    ```editorconfig
    ktlint_standard_function-return-type-spacing = disabled
    ```

### Function start of body spacing

Consistent spacing before start of function body.

=== "[:material-heart:](#) Ktlint"

    ```kotlin
    // In case `ktlint_function_signature_body_expression_wrapping` is set to `default` or `multiline`
    fun foo1() = "some-result"
    
    // In case `ktlint_function_signature_body_expression_wrapping` is set to `always`
    fun foo2() =
        "some-result"
    
    fun foo3() {
        // do something
    }
    
    // In case `ktlint_function_signature_body_expression_wrapping` is set to `default` or `multiline`
    fun bar1(): String = "some-result"
    
    // In case `ktlint_function_signature_body_expression_wrapping` is set to `always`
    fun bar2(): String =
        "some-result"
    
    fun bar3(): String {
        doSomething()
        return "some-result"
    }
    ```
=== "[:material-heart-off-outline:](#) Disallowed"

    ```kotlin
    fun foo1()= "some-result"
    
    fun foo2()
        = "some-result"
    
    fun foo3()
    {
        // do something
    }
    
    fun bar1(): String= "some-result"
    
    fun bar2(): String
        = "some-result"
    
    fun bar3(): String
    {
        return "some-result"
    }
    ```

Rule id: `standard:function-start-of-body-spacing`

Suppress or disable rule (1)
{ .annotate }

1. Suppress rule in code with annotation below:
    ```kotlin
    @Suppress("ktlint:standard:function-start-of-body-spacing")
    ```
   Enable rule via `.editorconfig`
    ```editorconfig
    ktlint_standard_function-start-of-body-spacing = enabled
    ```
   Disable rule via `.editorconfig`
    ```editorconfig
    ktlint_standard_function-start-of-body-spacing = disabled
    ```

### Function type reference spacing

Consistent spacing in the type reference before a function.

=== "[:material-heart:](#) Ktlint"

    ```kotlin
    fun String.foo() = "some-result"
    ```
=== "[:material-heart-off-outline:](#) Disallowed"

    ```kotlin
    fun String .foo() = "some-result"
    fun String
        .foo() = "some-result"
    fun String? .foo() = "some-result"
    fun String?
        .foo() = "some-result"
    ```

Rule id: `standard:function-type-reference-spacing`

Suppress or disable rule (1)
{ .annotate }

1. Suppress rule in code with annotation below:
    ```kotlin
    @Suppress("ktlint:standard:function-type-reference-spacing")
    ```
   Enable rule via `.editorconfig`
    ```editorconfig
    ktlint_standard_function-type-reference-spacing = enabled
    ```
   Disable rule via `.editorconfig`
    ```editorconfig
    ktlint_standard_function-type-reference-spacing = disabled
    ```

### Fun keyword spacing

Consistent spacing after the fun keyword.

=== "[:material-heart:](#) Ktlint"

    ```kotlin
    fun foo() = "some-result"
    ```
=== "[:material-heart-off-outline:](#) Disallowed"

    ```kotlin
    fun  foo() = "some-result"
    fun
    foo() = "some-result"
    ```

Rule id: `standard:fun-keyword-spacing`

Suppress or disable rule (1)
{ .annotate }

1. Suppress rule in code with annotation below:
    ```kotlin
    @Suppress("ktlint:standard:fun-keyword-spacing")
    ```
   Enable rule via `.editorconfig`
    ```editorconfig
    ktlint_standard_fun-keyword-spacing = enabled
    ```
   Disable rule via `.editorconfig`
    ```editorconfig
    ktlint_standard_fun-keyword-spacing = disabled
    ```

### Kdoc wrapping

A KDoc comment should start and end on a line that does not contain any other element.

=== "[:material-heart:](#) Ktlint"

    ```kotlin
    /** Some KDoc comment 1 */
    val foo1 = "foo1"
    ```
=== "[:material-heart-off-outline:](#) Disallowed"

    ```kotlin
    /** Some KDoc comment 1 */ val foo1 = "foo1"
    val foo2 = "foo2" /** Some KDoc comment
                       * with a newline
                       */
    ```

Rule id: `standard:kdoc-wrapping`

Suppress or disable rule (1)
{ .annotate }

1. Suppress rule in code with annotation below:
    ```kotlin
    @Suppress("ktlint:standard:kdoc-wrapping")
    ```
   Enable rule via `.editorconfig`
    ```editorconfig
    ktlint_standard_kdoc-wrapping = enabled
    ```
   Disable rule via `.editorconfig`
    ```editorconfig
    ktlint_standard_kdoc-wrapping = disabled
    ```

### Keyword spacing

Requires consistent spacing around keywords.

=== "[:material-heart:](#) Ktlint"

    ```kotlin
    fun main() {
        if (true) {
            doSomething()
        }
    }
    ```
=== "[:material-heart-off-outline:](#) Disallowed"

    ```kotlin
    fun main() {
        if(true) {
            doSomething()
        }
    }
    ```

Rule id: `standard:keyword-spacing`

Suppress or disable rule (1)
{ .annotate }

1. Suppress rule in code with annotation below:
    ```kotlin
    @Suppress("ktlint:standard:keyword-spacing")
    ```
   Enable rule via `.editorconfig`
    ```editorconfig
    ktlint_standard_keyword-spacing = enabled
    ```
   Disable rule via `.editorconfig`
    ```editorconfig
    ktlint_standard_keyword-spacing = disabled
    ```

### Modifier list spacing

Requires consistent spacing between modifiers in and after the last modifier in a modifier list.

=== "[:material-heart:](#) Ktlint"

    ```kotlin
    abstract class Foo {
        protected abstract suspend fun execute()
    }
    ```
=== "[:material-heart-off-outline:](#) Disallowed"

    ```kotlin
    abstract  class Foo {
        protected  abstract  suspend  fun execute()
    }
    abstract
    class Foo {
        protected
        abstract
        suspend
        fun execute()
    }
    ```

Rule id: `standard:modifier-list-spacing`

Suppress or disable rule (1)
{ .annotate }

1. Suppress rule in code with annotation below:
    ```kotlin
    @Suppress("ktlint:standard:modifier-list-spacing")
    ```
   Enable rule via `.editorconfig`
    ```editorconfig
    ktlint_standard_modifier-list-spacing = enabled
    ```
   Disable rule via `.editorconfig`
    ```editorconfig
    ktlint_standard_modifier-list-spacing = disabled
    ```

### Nullable type spacing

Disallows spaces in a nullable type.

=== "[:material-heart:](#) Ktlint"

    ```kotlin
    val foo: String? = null
    val foo: List<String?> = listOf(null)
    ```
=== "[:material-heart-off-outline:](#) Disallowed"

    ```kotlin
    val foo: String ? = null
    val foo: List<String ?> = listOf(null)
    ```

Rule id: `standard:nullable-type-spacing`

Suppress or disable rule (1)
{ .annotate }

1. Suppress rule in code with annotation below:
    ```kotlin
    @Suppress("ktlint:standard:nullable-type-spacing")
    ```
   Enable rule via `.editorconfig`
    ```editorconfig
    ktlint_standard_nullable-type-spacing = enabled
    ```
   Disable rule via `.editorconfig`
    ```editorconfig
    ktlint_standard_nullable-type-spacing = disabled
    ```

### Operator spacing

Requires consistent spacing around operators.

=== "[:material-heart:](#) Ktlint"

    ```kotlin
    val foo1 = 1 + 2
    val foo2 = 1 - 2
    val foo3 = 1 * 2
    val foo4 = 1 / 2
    ```
=== "[:material-heart-off-outline:](#) Disallowed"

    ```kotlin
    val foo1 = 1+2
    val foo2 = 1- 2
    val foo3 = 1 *2
    val foo4 = 1  /  2
    ```

Rule id: `standard:op-spacing`

Suppress or disable rule (1)
{ .annotate }

1. Suppress rule in code with annotation below:
    ```kotlin
    @Suppress("ktlint:standard:op-spacing")
    ```
   Enable rule via `.editorconfig`
    ```editorconfig
    ktlint_standard_op-spacing = enabled
    ```
   Disable rule via `.editorconfig`
    ```editorconfig
    ktlint_standard_op-spacing = disabled
    ```

### Parameter list spacing

Requires consistent spacing inside the parameter list.

=== "[:material-heart:](#) Ktlint"

    ```kotlin
    fun foo(a: Any) = "some-result"
    
    fun foo() = "some-result"
    ```
=== "[:material-heart-off-outline:](#) Disallowed"

    ```kotlin
    fun foo( a : Any ) = "some-result"
    fun foo(
    ) = "some-result"
    ```

| Configuration setting                                                                                                                                                                                                         | ktlint_official | intellij_idea | android_studio |
|:------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|:---------------:|:-------------:|:--------------:|
| `max_line_length`<br/><i>Maximum length of a (regular) line. This property is ignored in case the `max-line-length` rule is disabled, or when using Ktlint via a third party integration that does not provide this rule.</i> |       140       |     `off`     |     `100`      |

Rule id: `standard:parameter-list-spacing`

Suppress or disable rule (1)
{ .annotate }

1. Suppress rule in code with annotation below:
    ```kotlin
    @Suppress("ktlint:standard:parameter-list-spacing")
    ```
   Enable rule via `.editorconfig`
    ```editorconfig
    ktlint_standard_parameter-list-spacing = enabled
    ```
   Disable rule via `.editorconfig`
    ```editorconfig
    ktlint_standard_parameter-list-spacing = disabled
    ```

### Parenthesis spacing

Requires consistent spacing around parenthesis.

=== "[:material-heart:](#) Ktlint"

    ```kotlin
    class Foo : Bar {
        constructor(string: String) : super()
    }
    
    val foo1 = ((1 + 2) / 3)
    ```
=== "[:material-heart-off-outline:](#) Disallowed"

    ```kotlin
    class Foo : Bar {
        constructor(string: String) : super ()
    }

    val foo1 = ( (1 + 2 ) / 3)
    ```

Rule id: `standard:paren-spacing`

Suppress or disable rule (1)
{ .annotate }

1. Suppress rule in code with annotation below:
    ```kotlin
    @Suppress("ktlint:standard:paren-spacing")
    ```
   Enable rule via `.editorconfig`
    ```editorconfig
    ktlint_standard_paren-spacing = enabled
    ```
   Disable rule via `.editorconfig`
    ```editorconfig
    ktlint_standard_paren-spacing = disabled
    ```

### Range spacing

Requires consistent spacing around range operators.

=== "[:material-heart:](#) Ktlint"

    ```kotlin
    val foo1 = (1..12 step 2).last
    val foo2 = (1..12 step 2).last
    val foo3 = (1..12 step 2).last
    ```
=== "[:material-heart-off-outline:](#) Disallowed"

    ```kotlin
    val foo1 = (1.. 12 step 2).last
    val foo2 = (1 .. 12 step 2).last
    val foo3 = (1 ..12 step 2).last
    ```

Rule id: `standard:range-spacing`

Suppress or disable rule (1)
{ .annotate }

1. Suppress rule in code with annotation below:
    ```kotlin
    @Suppress("ktlint:standard:range-spacing")
    ```
   Enable rule via `.editorconfig`
    ```editorconfig
    ktlint_standard_range-spacing = enabled
    ```
   Disable rule via `.editorconfig`
    ```editorconfig
    ktlint_standard_range-spacing = disabled
    ```

### Spacing between function name and opening parenthesis

Requires consistent spacing between function name and opening parenthesis.

=== "[:material-heart:](#) Ktlint"

    ```kotlin
    fun foo() = "foo"
    ```
=== "[:material-heart-off-outline:](#) Disallowed"

    ```kotlin
    fun foo () = "foo"
    ```

Rule id: `standard:spacing-between-function-name-and-opening-parenthesis`

Suppress or disable rule (1)
{ .annotate }

1. Suppress rule in code with annotation below:
    ```kotlin
    @Suppress("ktlint:standard:spacing-between-function-name-and-opening-parenthesis")
    ```
   Enable rule via `.editorconfig`
    ```editorconfig
    ktlint_standard_spacing-between-function-name-and-opening-parenthesis = enabled
    ```
   Disable rule via `.editorconfig`
    ```editorconfig
    ktlint_standard_spacing-between-function-name-and-opening-parenthesis = disabled
    ```

### Square brackets spacing

Checks for spacing around square brackets.

=== "[:material-heart:](#) Ktlint"

    ```kotlin
    val foo1 = bar[1]
    val foo2 =
       bar[
           1,
           2,
       ]

    @Foo(
        fooBar = ["foo", "bar"],
        fooBaz = [
            "foo",
            "baz",
        ],
    )
    fun foo() {}
    ```

=== "[:material-heart-off-outline:](#) Disallowed"

    ```kotlin
    val foo1 = bar [1]
    val foo2 = bar[ 1]
    val foo3 = bar[1 ]
    ```

Rule id: `standard:square-brackets-spacing`

Suppress or disable rule (1)
{ .annotate }

1. Suppress rule in code with annotation below:
    ```kotlin
    @Suppress("ktlint:standard:square-brackets-spacing")
    ```
   Enable rule via `.editorconfig`
    ```editorconfig
    ktlint_standard_square-brackets-spacing = enabled
    ```
   Disable rule via `.editorconfig`
    ```editorconfig
    ktlint_standard_square-brackets-spacing = disabled
    ```

### Then spacing

Enforces consistent spacing around the `then` block in an `if`-statement.
=== "[:material-heart:](#) Ktlint"

    ```kotlin
    fun foo() {
        if (true) true
        if (true) (0) else (1)
        if (true) print(0) else (1)
    }
    ```
=== "[:material-heart-off-outline:](#) Disallowed"

    ```kotlin
    fun foo() {
        if (true)true
        if (true)(0)else (1)
        if (true)print(0)else (1)
    }
    ```

Rule id: `standard:then-spacing`

Suppress or disable rule (1)
{ .annotate }

1. Suppress rule in code with annotation below:
    ```kotlin
    @Suppress("ktlint:standard:then-spacing")
    ```
   Enable rule via `.editorconfig`
    ```editorconfig
    ktlint_standard_then-spacing = enabled
    ```
   Disable rule via `.editorconfig`
    ```editorconfig
    ktlint_standard_then-spacing = disabled
    ```

### Try catch finally spacing

Enforces consistent spacing in `try { .. } catch { .. } finally { .. }`.

=== "[:material-heart:](#) Ktlint"

    ```kotlin
    fun foo() =
        try {
            // do something
        } catch (exception: Exception) {
            // handle exception
        } finally {
            // clean up
        }
    ```
=== "[:material-heart-off-outline:](#) Disallowed"

    ```kotlin
    fun foo1() = try { /* ... */ } catch (exception: Exception) { /* ... */ } finally { /* ... */ }
    fun foo2() = 
        try {
            // do something
        }
        catch (exception: Exception) {
            // handle exception
        }
        finally {
            // clean up
        }
    ```

Rule id: `standard:try-catch-finally-spacing`

Suppress or disable rule (1)
{ .annotate }

1. Suppress rule in code with annotation below:
    ```kotlin
    @Suppress("ktlint:standard:try-catch-finally-spacing")
    ```
   Enable rule via `.editorconfig`
    ```editorconfig
    ktlint_standard_try-catch-finally-spacing = enabled
    ```
   Disable rule via `.editorconfig`
    ```editorconfig
    ktlint_standard_try-catch-finally-spacing = disabled
    ```

!!! Note
    This rule is only run when `ktlint_code_style` is set to `ktlint_official` or when the rule is enabled explicitly.

### Type argument list spacing

Disallows spacing before and after the angle brackets of a type argument list.

=== "[:material-heart:](#) Ktlint"

    ```kotlin
    val res = ArrayList<LintError>()

    class B<T> : A<T>() {
        override fun x() = super<A>.x()
    }
    ```
=== "[:material-heart-off-outline:](#) Disallowed"

    ```kotlin
    val res = ArrayList < LintError > ()

    class B<T> : A< T >() {
        override fun x() = super< A >.x()
    }
    ```

Rule id: `standard:type-argument-list-spacing`

Suppress or disable rule (1)
{ .annotate }

1. Suppress rule in code with annotation below:
    ```kotlin
    @Suppress("ktlint:standard:type-argument-list-spacing")
    ```
   Enable rule via `.editorconfig`
    ```editorconfig
    ktlint_standard_type-argument-list-spacing = enabled
    ```
   Disable rule via `.editorconfig`
    ```editorconfig
    ktlint_standard_type-argument-list-spacing = disabled
    ```

### Type parameter list spacing

Disallows spacing after a type parameter list in function and class declarations.

=== "[:material-heart:](#) Ktlint"

    ```kotlin
    fun <T> foo1(t: T) = "some-result"
    
    fun <T> foo2(t: T) = "some-result"

    fun <T> foo3(t: T) = "some-result"
    ```
=== "[:material-heart-off-outline:](#) Disallowed"

    ```kotlin
    fun<T> foo1(t: T) = "some-result"

    fun <T>foo2(t: T) = "some-result"

    fun<T>foo3(t: T) = "some-result"
    ```

Rule id: `standard:type-parameter-list-spacing`

Suppress or disable rule (1)
{ .annotate }

1. Suppress rule in code with annotation below:
    ```kotlin
    @Suppress("ktlint:standard:type-parameter-list-spacing")
    ```
   Enable rule via `.editorconfig`
    ```editorconfig
    ktlint_standard_type-parameter-list-spacing = enabled
    ```
   Disable rule via `.editorconfig`
    ```editorconfig
    ktlint_standard_type-parameter-list-spacing = disabled
    ```

### Unary operator spacing

Disallows spaces around unary operators.

=== "[:material-heart:](#) Ktlint"

    ```kotlin
    fun foo1(i: Int) = i++

    fun foo2(i: Int) = ++i

    fun foo3(i: Int) = ++i
    ```
=== "[:material-heart-off-outline:](#) Disallowed"

    ```kotlin
    fun foo1(i: Int) = i ++

    fun foo2(i: Int) = ++ i

    fun foo3(i: Int) = ++
        i
    ```

Rule id: `standard:unary-op-spacing`

Suppress or disable rule (1)
{ .annotate }

1. Suppress rule in code with annotation below:
    ```kotlin
    @Suppress("ktlint:standard:unary-op-spacing")
    ```
   Enable rule via `.editorconfig`
    ```editorconfig
    ktlint_standard_unary-op-spacing = enabled
    ```
   Disable rule via `.editorconfig`
    ```editorconfig
    ktlint_standard_unary-op-spacing = disabled
    ```

## String template

Enforces consistent string templates (`$v` instead of `${v}`, `${p.v}` instead of `${p.v.toString()}`)

=== "[:material-heart:](#) Ktlint"

    ```kotlin
    val foo = "$foo hello"
    ```
=== "[:material-heart-off-outline:](#) Disallowed"

    ```kotlin
    val foo = "${foo} hello"
    ```

Rule id: `standard:string-template`

Suppress or disable rule (1)
{ .annotate }

1. Suppress rule in code with annotation below:
    ```kotlin
    @Suppress("ktlint:standard:string-template")
    ```
   Enable rule via `.editorconfig`
    ```editorconfig
    ktlint_standard_string-template = enabled
    ```
   Disable rule via `.editorconfig`
    ```editorconfig
    ktlint_standard_string-template = disabled
    ```

## String template indent

Enforces consistent string template indentation for multiline string templates which are post-fixed with `.trimIndent()`. The opening and closing `"""` are placed on separate lines and the indentation of the content of the template is aligned with the `"""`.

=== "[:material-heart:](#) Ktlint"

    ```kotlin
    val foo =
        """
        line1
        line2
        """.trimIndent()
    
    fun foo() {
        // The opening """ can not be wrapped to next line as that would result in a compilation error
        return """
            line1
            line2
            """.trimIndent()
    }
    ```
=== "[:material-heart-off-outline:](#) Disallowed"

    ```kotlin
    val foo = """
              line1
              line2
              """.trimIndent()

    fun foo() {
        return """
            line1
            line2
        """.trimIndent()
    }
    ```

Rule id: `standard:string-template-indent`

Suppress or disable rule (1)
{ .annotate }

1. Suppress rule in code with annotation below:
    ```kotlin
    @Suppress("ktlint:string-template-indent")
    ```
   Enable rule via `.editorconfig`
    ```editorconfig
    ktlint_standard_string-template-indent = enabled
    ```
   Disable rule via `.editorconfig`
    ```editorconfig
    ktlint_standard_string-template-indent = disabled
    ```

!!! Note
    This rule is only run when `ktlint_code_style` is set to `ktlint_official` or when the rule is enabled explicitly.

## Trailing comma on call site

Enforces consistent removal (default) or adding of trailing commas on call site.

=== "[:material-heart:](#) Ktlint"

    ```kotlin
    val foo =
        FooWrapper(
            Foo(
                a = 3,
                b = 4,
            ),
        )
    ```
=== "[:material-heart-off-outline:](#) Disallowed"

    ```kotlin
    val foo =
        FooWrapper(Foo(
            a = 3,
            b = 4,
        ),) // it's weird to insert "," between unwrapped (continued) parenthesis
    ```

| Configuration setting                                                                                                                                                                                                                                                                                                                                                                                                                               | ktlint_official | intellij_idea | android_studio |
|:----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|:---------------:|:-------------:|:--------------:|
| `ij_kotlin_allow_trailing_comma_on_call_site`<br/><i>Defines whether a trailing comma (or no trailing comma) should be enforced on the calling site, e.g. argument-list, when-entries, lambda-arguments, indices, etc. When set, IntelliJ IDEA uses this property to <b>allow</b> usage of a trailing comma by discretion of the developer. KtLint however uses this setting to <b>enforce</b> consistent usage of the trailing comma when set.</i> |     `true`      |    `true`     |    `false`     |

!!! note
    Although the [Kotlin coding conventions](https://kotlinlang.org/docs/reference/coding-conventions.html#trailing-commas) leaves it to the developer's discretion to use trailing commas on the call site, it also states that usage of trailing commas has several benefits:
    
     * It makes version-control diffs cleaner – as all the focus is on the changed value.
     * It makes it easy to add and reorder elements – there is no need to add or delete the comma if you manipulate elements.
     * It simplifies code generation, for example, for object initializers. The last element can also have a comma.

    KtLint values *consistent* formatting more than a per-situation decision, and therefore uses this setting to *enforce/disallow* usage of trailing comma's on the calling site.

Rule id: `standard:trailing-comma-on-call-site`

Suppress or disable rule (1)
{ .annotate }

1. Suppress rule in code with annotation below:
    ```kotlin
    @Suppress("ktlint:standard:trailing-comma-on-call-site")
    ```
   Enable rule via `.editorconfig`
    ```editorconfig
    ktlint_standard_trailing-comma-on-call-site = enabled
    ```
   Disable rule via `.editorconfig`
    ```editorconfig
    ktlint_standard_trailing-comma-on-call-site = disabled
    ```

## Trailing comma on declaration site

Enforces consistent removal (default) or adding of trailing commas on declaration site.

=== "[:material-heart:](#) Ktlint"

    ```kotlin
    class FooWrapper(
        val foo = Foo(
            a = 3,
            b = 4,
        ),
    )
    ```
=== "[:material-heart-off-outline:](#) Disallowed"

    ```kotlin
    class FooWrapper(val foo = Foo(
        a = 3,
        b = 4,
    ),) // it's weird to insert "," between unwrapped (continued) parenthesis
    ```

| Configuration setting                                                                                                                                                                                                                                                                                                                                                                                                                                      | ktlint_official | intellij_idea | android_studio |
|:-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|:---------------:|:-------------:|:--------------:|
| `ij_kotlin_allow_trailing_comma`<br/><i>Defines whether a trailing comma (or no trailing comma) should be enforced on the defining site, e.g. parameter-list, type-argument-list, lambda-value-parameters, enum-entries, etc. When set, IntelliJ IDEA uses this property to <b>allow</b> usage of a trailing comma by discretion of the developer. KtLint however uses this setting to <b>enforce</b> consistent usage of the trailing comma when set.</i> |     `true`      |    `true`     |    `false`     |

!!! note
    The [Kotlin coding conventions](https://kotlinlang.org/docs/reference/coding-conventions.html#trailing-commas) encourages the usage of trailing commas on the declaration site, but leaves it to the developer's discretion to use trailing commas on the call site. But next to this, it also states that usage of trailing commas has several benefits:
    
     * It makes version-control diffs cleaner – as all the focus is on the changed value.
     * It makes it easy to add and reorder elements – there is no need to add or delete the comma if you manipulate elements.
     * It simplifies code generation, for example, for object initializers. The last element can also have a comma.

   KtLint values *consistent* formatting more than a per-situation decision, and therefore uses this setting to *enforce/disallow* usage of trailing comma's in declarations.

Rule id: `standard:trailing-comma-on-declaration-site`

Suppress or disable rule (1)
{ .annotate }

1. Suppress rule in code with annotation below:
    ```kotlin
    @Suppress("ktlint:standard:trailing-comma-on-declaration-site")
    ```
   Enable rule via `.editorconfig`
    ```editorconfig
    ktlint_standard_trailing-comma-on-declaration-site = enabled
    ```
   Disable rule via `.editorconfig`
    ```editorconfig
    ktlint_standard_trailing-comma-on-declaration-site = disabled
    ```

## Type argument comment

Disallows comments to be placed at certain locations inside a type argument.

=== "[:material-heart:](#) Ktlint"

    ```kotlin
    fun Foo<
        /* some comment */ 
        out Any
        >.foo() {}
    fun Foo<
        // some comment 
        out Any
        >.foo() {}
    ```

=== "[:material-heart-off-outline:](#) Disallowed"

    ```kotlin
    fun Foo<out /* some comment */ Any>.foo() {}
    fun Foo<
        out Any, // some comment
        >.foo() {}
    ```

!!! note
    In some projects it is an accepted practice to use EOL comments to document the parameter *before* the comma as is shown below:
    ```kotlin
    fun Foo<
        out Bar1, // some comment
        out Bar2, // some other comment
    >.foo() {}
    ```
    Although this code sample might look ok, it is semantically and programmatically unclear to which type `some comment` refers. From the developer perspective it might be clear that it belongs to type `Bar1`. From the parsers perspective, it does belong to type `Bar2`.

Rule id: `standard:type-argument-comment`

Suppress or disable rule (1)
{ .annotate }

1. Suppress rule in code with annotation below:
    ```kotlin
    @Suppress("ktlint:standard:type-argument-comment")
    ```
   Enable rule via `.editorconfig`
    ```editorconfig
    ktlint_standard_type-argument-comment = enabled
    ```
   Disable rule via `.editorconfig`
    ```editorconfig
    ktlint_standard_type-argument-comment = disabled
    ```

## Type parameter comment

Disallows comments to be placed at certain locations inside a type parameter.

=== "[:material-heart:](#) Ktlint"

    ```kotlin
    class Foo1<
        /* some comment */ 
        out Bar
        >
    class Foo2<
        // some comment 
        out Bar
        >
    ```

=== "[:material-heart-off-outline:](#) Disallowed"

    ```kotlin
    class Foo1<in /* some comment */ Bar>
    class Foo2<
        in Bar, // some comment
        >
    ```

!!! note
    In some projects, it is an accepted practice to use EOL comments to document the parameter *before* the comma as is shown below:
    ```kotlin
    class Foo<
        out Bar1, // some comment
        out Bar2, // some other comment
    >
    ```
   Although this code sample might look ok, it is semantically and programmatically unclear on which parameter `some comment` refers. From the developer perspective, it might be clear that it belongs to type `Bar1`. From the parsers perspective, it does belong to type `Bar2`.

Rule id: `standard:type-parameter-comment`

Suppress or disable rule (1)
{ .annotate }

1. Suppress rule in code with annotation below:
    ```kotlin
    @Suppress("ktlint:type-parameter-comment")
    ```
   Enable rule via `.editorconfig`
    ```editorconfig
    ktlint_standard_type-parameter-comment = enabled
    ```
   Disable rule via `.editorconfig`
    ```editorconfig
    ktlint_standard_type-parameter-comment = disabled
    ```

## Unnecessary parenthesis before trailing lambda

An empty parentheses block before a lambda is redundant.

=== "[:material-heart:](#) Ktlint"

    ```kotlin
    val foo = "some-string".count { it == '-' }
    ```

=== "[:material-heart-off-outline:](#) Disallowed"

    ```kotlin
    val foo = "some-string".count() { it == '-' }
    ```

Rule id: `standard:unnecessary-parentheses-before-trailing-lambda`

Suppress or disable rule (1)
{ .annotate }

1. Suppress rule in code with annotation below:
    ```kotlin
    @Suppress("ktlint:standard:unnecessary-parentheses-before-trailing-lambda")
    ```
   Enable rule via `.editorconfig`
    ```editorconfig
    ktlint_standard_unnecessary-parentheses-before-trailing-lambda = enabled
    ```
   Disable rule via `.editorconfig`
    ```editorconfig
    ktlint_standard_unnecessary-parentheses-before-trailing-lambda = disabled
    ```

## Value argument comment

Disallows comments to be placed at certain locations inside a value argument.

=== "[:material-heart:](#) Ktlint"

    ```kotlin
    val foo1 =
        foo(
            /* some comment */
            bar = "bar"
        )
    val foo2 =
        foo(
            // some comment
            bar = "bar"
        )
    ```

=== "[:material-heart-off-outline:](#) Disallowed"

    ```kotlin
    val foo1 = foo(bar /* some comment */ = "bar")
    val foo2 = 
        foo(
            bar = // some comment
                "bar"
        )
    ```

Rule id: `standard:value-argument-comment`

Suppress or disable rule (1)
{ .annotate }

1. Suppress rule in code with annotation below:
    ```kotlin
    @Suppress("ktlint:value-argument-comment")
    ```
   Enable rule via `.editorconfig`
    ```editorconfig
    ktlint_standard_value-argument-comment = enabled
    ```
   Disable rule via `.editorconfig`
    ```editorconfig
    ktlint_standard_value-argument-comment = disabled
    ```

## Value parameter comment

Disallows comments to be placed at certain locations inside a value argument.

=== "[:material-heart:](#) Ktlint"

    ```kotlin
    class Foo1(
        /** some kdoc */
        bar = "bar"
    )
    class Foo2(
        /* some comment */
        bar = "bar"
    )
    class Foo3(
        // some comment
        bar = "bar"
    )
    ```

=== "[:material-heart-off-outline:](#) Disallowed"

    ```kotlin
    class Foo1(
       bar = /** some kdoc */ "bar"
    )
    class Foo2(
       bar = /* some comment */ "bar"
    )
    class Foo3(
        bar =
           // some comment
           "bar"
    )
    ```

Rule id: `standard:value-parameter-comment`

Suppress or disable rule (1)
{ .annotate }

1. Suppress rule in code with annotation below:
    ```kotlin
    @Suppress("ktlint:standard:value-parameter-comment")
    ```
   Enable rule via `.editorconfig`
    ```editorconfig
    ktlint_standard_value-parameter-comment = enabled
    ```
   Disable rule via `.editorconfig`
    ```editorconfig
    ktlint_standard_value-parameter-comment = disabled
    ```

## When-entry bracing

Enforces consistent usages of braces inside the when-statement. All when-entries in the when-statement should use braces around their bodies in case at least one when-entry has a multiline body, or when the body is surrounded by braces.

Braces are helpful for following reasons:

- Bodies of the when-conditions are all aligned at same column position
- Closing braces helps in separating the when-conditions

This rule is not incorporated in the Kotlin Coding conventions, nor in the Android Kotlin Styleguide. It is based on similar behavior in enforcing consistent use of braces in if-else statements. As of that the rule is only enabled automatically for code style `ktlint_official`. It can be enabled explicitly for other code styles.

=== "[:material-heart:](#) Ktlint"

    ```kotlin
    val foo1 =
        when (bar) {
            BAR1 -> "bar1"
            BAR2 -> "bar2"
            else -> null
        }

    val foo2 =
        when (bar) {
            BAR1 -> {
                "bar1"
            }
            BAR2 -> {
                "bar2"
            }
            else -> {
                null
            }
        }
    ```

=== "[:material-heart-off-outline:](#) Disallowed"

    ```kotlin
    val foo3 =
        when (bar) {
            BAR1 -> "bar1"
            BAR2 -> {
                "bar2"
            }
            else -> null
        }

    val foo4 =
        when (bar) {
            BAR1 -> "bar1"
            BAR2 ->
                "bar2"
            else -> null
        }
    ```

Rule id: `standard:when-entry-bracing`

Suppress or disable rule (1)
{ .annotate }

1. Suppress rule in code with annotation below:
    ```kotlin
    @Suppress("ktlint:standard:when-entry-bracing")
    ```
   Enable rule via `.editorconfig`
    ```editorconfig
    ktlint_standard_when-entry-bracing = enabled
    ```
   Disable rule via `.editorconfig`
    ```editorconfig
    ktlint_standard_when-entry-bracing = disabled
    ```

## Wrapping

### Argument list wrapping

All arguments should either be on the same line, or on a separate line.

=== "[:material-heart:](#) Ktlint"

    ```kotlin
    val foo =
        foo(
            a,
            b,
            c,
        )
    ```
=== "[:material-heart-off-outline:](#) Disallowed"

    ```kotlin
    val foo =
        foo(
            a,
            b, c,
        )
    ```

| Configuration setting                                                                                                                                                                                                         | ktlint_official | intellij_idea | android_studio |
|:------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|:---------------:|:-------------:|:--------------:|
| `ktlint_argument_list_wrapping_ignore_when_parameter_count_greater_or_equal_than`                                                                                                                                             |     `unset`     |       8       |       8        |
| `max_line_length`<br/><i>Maximum length of a (regular) line. This property is ignored in case the `max-line-length` rule is disabled, or when using Ktlint via a third party integration that does not provide this rule.</i> |       140       |     `off`     |     `100`      |

Rule id: `standard:argument-list-wrapping`

Suppress or disable rule (1)
{ .annotate }

1. Suppress rule in code with annotation below:
    ```kotlin
    @Suppress("ktlint:standard:argument-list-wrapping")
    ```
   Enable rule via `.editorconfig`
    ```editorconfig
    ktlint_standard_argument-list-wrapping = enabled
    ```
   Disable rule via `.editorconfig`
    ```editorconfig
    ktlint_standard_argument-list-wrapping = disabled
    ```

### Chain wrapping

When wrapping chained calls `.`, `?.` and `?:` should be placed on the next line

=== "[:material-heart:](#) Ktlint"

    ```kotlin
    val foo =
        listOf(1, 2, 3)
            .filter { it > 2 }!!
            .takeIf { it.count() > 100 }
            ?.sum()
    val foobar =
        foo()
            ?: bar
    ```
=== "[:material-heart-off-outline:](#) Disallowed"

    ```kotlin
    val foo =
        listOf(1, 2, 3).
            filter { it > 2 }!!.
            takeIf { it.count() > 100 }?.
            sum()
    val foobar =
        foo() ?:
            bar
    ```

Rule id: `standard:chain-wrapping`

Suppress or disable rule (1)
{ .annotate }

1. Suppress rule in code with annotation below:
    ```kotlin
    @Suppress("ktlint:chain-wrapping")
    ```
   Enable rule via `.editorconfig`
    ```editorconfig
    ktlint_standard_chain-wrapping = enabled
    ```
   Disable rule via `.editorconfig`
    ```editorconfig
    ktlint_standard_chain-wrapping = disabled
    ```

### Comment wrapping

A block comment must start and end on a line that does not contain any other element.

=== "[:material-heart:](#) Ktlint"

    ```kotlin
    // Some comment 1
    val foo1 = "foo1"
    val foo2 = "foo" // Some comment
    val foo3 = { /* no-op */ } 
    ```
=== "[:material-heart-off-outline:](#) Disallowed"

    ```kotlin
    /* Some comment 1 */ val foo1 = "foo1"
    val foo2 = "foo" /* Block comment instead of end-of-line comment */
    val foo3 = "foo" /* Some comment
                      * with a newline
                      */
    ```

Rule id: `standard:comment-wrapping`

Suppress or disable rule (1)
{ .annotate }

1. Suppress rule in code with annotation below:
    ```kotlin
    @Suppress("ktlint:standard:comment-wrapping")
    ```
   Enable rule via `.editorconfig`
    ```editorconfig
    ktlint_standard_comment-wrapping = enabled
    ```
   Disable rule via `.editorconfig`
    ```editorconfig
    ktlint_standard_comment-wrapping = disabled
    ```

### Context receiver wrapping

!!! important
    Context receivers are deprecated starting from Kotlin 2.2.0 and will be removed in a future version. Once KtLint is upgraded to a Kotlin version that no longer supports context receivers, then you will not be able to use that KtLint version as long as your code still contains context receiver.

!!! tip
    This rule does not affect context parameters. See rule `context-receiver-list-wrapping` for wrapping of context parameters.

Wraps the context receiver list containing a context receiver to a separate line regardless of maximum line length. If the maximum line length is configured and is exceeded, wrap the context receivers and if needed its projection types to separate lines.

=== "[:material-heart:](#) Ktlint"

    ```kotlin
    // Always wrap regardless of whether max line length is set
    context(Foo)
    fun fooBar()
    
    // Wrap each context receiver to a separate line when the
    // entire context receiver list does not fit on a single line
    context(
        Fooooooooooooooooooo1,
        Foooooooooooooooooooooooooooooo2
    )
    fun fooBar()
    
    // Wrap each context receiver to a separate line when the
    // entire context receiver list does not fit on a single line.
    // Also, wrap each of it projection types in case a context
    // receiver does not fit on a single line after it has been
    // wrapped.
    context(
        Foooooooooooooooo<
            Foo,
            Bar,
            >
    )
    fun fooBar()
    ```

=== "[:material-heart-off-outline:](#) Disallowed"

    ```kotlin
    // Should be wrapped regardless of whether max line length is set
    context(Foo) fun fooBar()

    // Should be wrapped when the entire context receiver list does not
    // fit on a single line
    context(Fooooooooooooooooooo1, Foooooooooooooooooooooooooooooo2)
    fun fooBar()

    // Should be wrapped when the entire context receiver list does not
    // fit on a single line. Also, it should wrap each of it projection
    // type in case a context receiver does not fit on a single line 
    // after it has been wrapped.
    context(Foooooooooooooooo<Foo, Bar>)
    fun fooBar()
    ```

| Configuration setting                                                                                                                                                                                                         | ktlint_official | intellij_idea | android_studio |
|:------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|:---------------:|:-------------:|:--------------:|
| `max_line_length`<br/><i>Maximum length of a (regular) line. This property is ignored in case the `max-line-length` rule is disabled, or when using Ktlint via a third party integration that does not provide this rule.</i> |       140       |     `off`     |     `100`      |

Rule id: `standard:context-receiver-wrapping`

Suppress or disable rule (1)
{ .annotate }

1. Suppress rule in code with annotation below:
    ```kotlin
    @Suppress("ktlint:standard:context-receiver-wrapping")
    ```
   Enable rule via `.editorconfig`
    ```editorconfig
    ktlint_standard_context-receiver-wrapping = enabled
    ```
   Disable rule via `.editorconfig`
    ```editorconfig
    ktlint_standard_context-receiver-wrapping = disabled
    ```
   
### Context receiver list wrapping

!!! tip
    This rule does not affect context receivers. See rule `context-receiver-wrapping` for wrapping of context receivers.

Wraps the context receiver list containing a context parameter to a separate line regardless of maximum line length. If the maximum line length is configured and is exceeded, wrap the context receivers and if needed its projection types to separate lines.

=== "[:material-heart:](#) Ktlint"

    ```kotlin
    // Always wrap regardless of whether max line length is set
    context(_: Foo)
    fun fooBar()
    
    // Wrap each context receiver to a separate line when the
    // entire context receiver list does not fit on a single line
    context(
        foo1: Fooooooooooooooooooo1,
        foo2: Foooooooooooooooooooooooooooooo2
    )
    fun fooBar()
    
    // Wrap each context receiver to a separate line when the
    // entire context receiver list does not fit on a single line.
    // Also, wrap each of it projection types in case a context
    // receiver does not fit on a single line after it has been
    // wrapped.
    context(
        _: Foooooooooooooooo<
            Foo,
            Bar,
            >
    )
    fun fooBar()
    ```

=== "[:material-heart-off-outline:](#) Disallowed"

    ```kotlin
    // Should be wrapped regardless of whether max line length is set
    context(_: Foo) fun fooBar()

    // Should be wrapped when the entire context receiver list does not
    // fit on a single line
    context(foo1: Fooooooooooooooooooo1, foo2: Foooooooooooooooooooooooooooooo2)
    fun fooBar()

    // Should be wrapped when the entire context receiver list does not
    // fit on a single line. Also, it should wrap each of it projection
    // type in case a context receiver does not fit on a single line 
    // after it has been wrapped.
    context(_: Foooooooooooooooo<Foo, Bar>)
    fun fooBar()
    ```

| Configuration setting                                                                                                                                                                                                         | ktlint_official | intellij_idea | android_studio |
|:------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|:---------------:|:-------------:|:--------------:|
| `max_line_length`<br/><i>Maximum length of a (regular) line. This property is ignored in case the `max-line-length` rule is disabled, or when using Ktlint via a third party integration that does not provide this rule.</i> |       140       |     `off`     |     `100`      |

Rule id: `standard:context-receiver-list-wrapping`

Suppress or disable rule (1)
{ .annotate }

1. Suppress rule in code with annotation below:
    ```kotlin
    @Suppress("ktlint:standard:context-receiver-list-wrapping")
    ```
   Enable rule via `.editorconfig`
    ```editorconfig
    ktlint_standard_context-receiver-list-wrapping = enabled
    ```
   Disable rule via `.editorconfig`
    ```editorconfig
    ktlint_standard_context-receiver-list-wrapping = disabled
    ```

### Enum wrapping

Each individual enum entry should be on a separate line. Multiple enums cannot be on the same line. In case the enumeration contains enum entries and declarations, those are to be separated by a blank line.

=== "[:material-heart:](#) Ktlint"

    ```kotlin
    enum class Foo { A, B, C, D }

    enum class Foo {
        A,
        B,
        C,
        D,
        ;

        fun foo() = "foo"
    }
    ```

=== "[:material-heart-off-outline:](#) Disallowed"

    ```kotlin
    enum class Foo {
        A,
        B, C,
        D
    }

    enum class Foo {
        A;
        fun foo() = "foo"
    }
    ```

Rule id: `standard:enum-wrapping`

Suppress or disable rule (1)
{ .annotate }

1. Suppress rule in code with annotation below:
    ```kotlin
    @Suppress("ktlint:enum-wrapping")
    ```
   Enable rule via `.editorconfig`
    ```editorconfig
    ktlint_standard_enum-wrapping = enabled
    ```
   Disable rule via `.editorconfig`
    ```editorconfig
    ktlint_standard_enum-wrapping = disabled
    ```

### If else wrapping

A single line if-statement should be kept simple. It may contain no more than one else-branch. The branches may not be wrapped in a block.

=== "[:material-heart:](#) Ktlint"

    ```kotlin
    fun foobar() {
        if (true) foo()
        if (true) foo() else bar()
    }
    ```

=== "[:material-heart-off-outline:](#) Disallowed"

    ```kotlin
    fun foobar() {
        if (true) if (false) foo() else bar()
        if (true) bar() else if (false) foo() else bar()
        if (true) { foo() } else bar()
        if (true) bar() else { if (false) foo() else bar() }
    }
    ```

Rule id: `standard:if-else-wrapping`

Suppress or disable rule (1)
{ .annotate }

1. Suppress rule in code with annotation below:
    ```kotlin
    @Suppress("ktlint:standard:if-else-wrapping")
    ```
   Enable rule via `.editorconfig`
    ```editorconfig
    ktlint_standard_if-else-wrapping = enabled
    ```
   Disable rule via `.editorconfig`
    ```editorconfig
    ktlint_standard_if-else-wrapping = disabled
    ```

!!! Note
    This rule is only run when `ktlint_code_style` is set to `ktlint_official` or when the rule is enabled explicitly.

### Multiline expression wrapping

Multiline expressions on the right hand side of an expression are forced to start on a separate line. Expressions in return statement are excluded as that would result in a compilation error.

Setting `ktlint_function_signature_body_expression_wrapping` of the `function-signature` rule takes precedence when set to `default`. This setting keeps the first line of a multiline expression body on the same line as the end of function signature as long as the max line length is not exceeded. In that case, this rule does not wrap the multiline expression. 

=== "[:material-heart:](#) Ktlint"

    ```kotlin
    val foo =
        foo(
            parameterName =
                "The quick brown fox "
                    .plus("jumps ")
                    .plus("over the lazy dog"),
        )
    ```

=== "[:material-heart-off-outline:](#) Disallowed"

    ```kotlin
    val foo = foo(
        parameterName = "The quick brown fox "
            .plus("jumps ")
            .plus("over the lazy dog"),
    )
    ```

Rule id: `standard:multiline-expression-wrapping`

Suppress or disable rule (1)
{ .annotate }

1. Suppress rule in code with annotation below:
    ```kotlin
    @Suppress("ktlint:standard:multiline-expression-wrapping")
    ```
   Enable rule via `.editorconfig`
    ```editorconfig
    ktlint_standard_multiline-expression-wrapping = enabled
    ```
   Disable rule via `.editorconfig`
    ```editorconfig
    ktlint_standard_multiline-expression-wrapping = disabled
    ```

!!! Note
    This rule is only run when `ktlint_code_style` is set to `ktlint_official` or when the rule is enabled explicitly.

### Parameter list wrapping

When class/function signature doesn't fit on a single line, each parameter must be on a separate line.

!!! Note
    Wrapping of parameters is also influenced by the `function-signature` rule.

=== "[:material-heart:](#) Ktlint"

    ```kotlin
    // If `ktlint_class_signature_rule_force_multiline_when_parameter_count_greater_or_equal_than` equals
    // `unset` the parameters are not wrapped as long as they fit on a single line
    class ClassA(paramA: String, paramB: String, paramC: String)
    
    class ClassA(
        paramA: String,
        paramB: String,
        paramC: String
    )
    
    // If `ktlint_function_signature_rule_force_multiline_when_parameter_count_greater_or_equal_than` equals
    // `unset` the parameters are not wrapped as long as they fit on a single line
    fun f(a: Any, b: Any, c: Any)
    
    fun f(
        a: Any,
        b: Any,
        c: Any
    )
    
    fun foo(
        @Bar fooBar: FooBar
    )
    ```
=== "[:material-heart-off-outline:](#) Disallowed (ktlint_official)"

    ```kotlin
    class ClassA(
        paramA: String, paramB: String,
        paramC: String
    )

    fun f(
        a: Any,
        b: Any, c: Any
    )

    fun foo(@Bar fooBar: FooBar)
    ```
=== "[:material-heart-off-outline:](#) Disallowed (non ktlint_official)"

    ```kotlin
    class ClassA(
        paramA: String, paramB: String,
        paramC: String
    )

    fun f(
        a: Any,
        b: Any, c: Any
    )
    ```

| Configuration setting                                                                                                                                                                                                         | ktlint_official | intellij_idea | android_studio |
|:------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|:---------------:|:-------------:|:--------------:|
| `max_line_length`<br/><i>Maximum length of a (regular) line. This property is ignored in case the `max-line-length` rule is disabled, or when using Ktlint via a third party integration that does not provide this rule.</i> |       140       |     `off`     |     `100`      |

Rule id: `standard:parameter-list-wrapping`

Suppress or disable rule (1)
{ .annotate }

1. Suppress rule in code with annotation below:
    ```kotlin
    @Suppress("ktlint:parameter-list-wrapping")
    ```
   Enable rule via `.editorconfig`
    ```editorconfig
    ktlint_standard_parameter-list-wrapping = enabled
    ```
   Disable rule via `.editorconfig`
    ```editorconfig
    ktlint_standard_parameter-list-wrapping = disabled
    ```

### Parameter wrapping

When a function or class parameter doesn't fit on a single line, wrap the type or value to a separate line.

=== "[:material-heart:](#) Ktlint (ktlint_official)"

    ```kotlin
    // Assume that the last allowed character is
    // at the X character on the right           X
    class Bar(
        val fooooooooooooooooooooooooTooLong:
            Foo,
    )

    fun bar(
        fooooooooooooooooooooooooTooLong:
            Foo,
    )
    ```
=== "[:material-heart:](#) Ktlint (non ktlint_official)"

    ```kotlin
    // Assume that the last allowed character is
    // at the X character on the right           X
    class Bar(
        val fooooooooooooooooooooooooTooLong:
        Foo,
    )

    fun bar(
        fooooooooooooooooooooooooTooLong:
        Foo,
    )
    ```
=== "[:material-heart-off-outline:](#) Disallowed"

    ```kotlin
    // Assume that the last allowed character is
    // at the X character on the right           X
    class Bar(
        val fooooooooooooooooooooooooTooLong: Foo,
    )

    fun bar(
        fooooooooooooooooooooooooooooTooLong: Foo,
    )
    ```

| Configuration setting                                                                                                                                                                                                         | ktlint_official | intellij_idea | android_studio |
|:------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|:---------------:|:-------------:|:--------------:|
| `max_line_length`<br/><i>Maximum length of a (regular) line. This property is ignored in case the `max-line-length` rule is disabled, or when using Ktlint via a third party integration that does not provide this rule.</i> |       140       |     `off`     |     `100`      |

Rule id: `standard:parameter-wrapping`

Suppress or disable rule (1)
{ .annotate }

1. Suppress rule in code with annotation below:
    ```kotlin
    @Suppress("ktlint:standard:parameter-wrapping")
    ```
   Enable rule via `.editorconfig`
    ```editorconfig
    ktlint_standard_parameter-wrapping = enabled
    ```
   Disable rule via `.editorconfig`
    ```editorconfig
    ktlint_standard_parameter-wrapping = disabled
    ```

### Property wrapping

When a property doesn't fit on a single line, wrap the type or value to a separate line.

=== "[:material-heart:](#) Ktlint"

    ```kotlin
    // Assume that the last allowed character is
    // at the X character on the right           X
    val aVariableWithALooooooooooooongName:
        String
    ```
=== "[:material-heart-off-outline:](#) Disallowed"

    ```kotlin
    // Assume that the last allowed character is
    // at the X character on the right           X
    val aVariableWithALooooooooooooongName: String
    ```

| Configuration setting                                                                                                                                                                                                         | ktlint_official | intellij_idea | android_studio |
|:------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|:---------------:|:-------------:|:--------------:|
| `max_line_length`<br/><i>Maximum length of a (regular) line. This property is ignored in case the `max-line-length` rule is disabled, or when using Ktlint via a third party integration that does not provide this rule.</i> |       140       |     `off`     |     `100`      |

Rule id: `standard:property-wrapping`

Suppress or disable rule (1)
{ .annotate }

1. Suppress rule in code with annotation below:
    ```kotlin
    @Suppress("ktlint:property-wrapping")
    ```
   Enable rule via `.editorconfig`
    ```editorconfig
    ktlint_standard_property-wrapping = enabled
    ```
   Disable rule via `.editorconfig`
    ```editorconfig
    ktlint_standard_property-wrapping = disabled
    ```

### Statement wrapping

A function, class/object body or other block body statement has to be placed on different line than the braces of the body block.

=== "[:material-heart:](#) Ktlint"

    ```kotlin
    fun foo() {
        if (true) {
            // do something
        }
    }

    class A {
        val a = 0
        val b = 1
    }

    enum class FooBar1 { FOO, BAR }

    enum class FooBar2 {
        FOO,
        BAR,
    }
    ```

=== "[:material-heart-off-outline:](#) Disallowed"

    ```kotlin
    fun foo() { if (true) {
            // do something
        }
    }

    class A { val a = 0
        val b = 1 }
    ```

Rule id: `standard:statement-wrapping`

Suppress or disable rule (1)
{ .annotate }

1. Suppress rule in code with annotation below:
    ```kotlin
    @Suppress("ktlint:standard:statement-wrapping")
    ```
   Enable rule via `.editorconfig`
    ```editorconfig
    ktlint_standard_statement-wrapping = enabled
    ```
   Disable rule via `.editorconfig`
    ```editorconfig
    ktlint_standard_statement-wrapping = disabled
    ```

### Wrapping

Inserts missing newlines (for example, between parentheses of a multi-line function call).

=== "[:material-heart:](#) Ktlint"

    ```kotlin
    val foo =
        foo(
            a,
            b,
            c,
        )
    ```

=== "[:material-heart-off-outline:](#) Disallowed"

    ```kotlin
    val foo = foo(
        a,
        b,
        c)
    ```

| Configuration setting                                                                                                                                                                                                         | ktlint_official | intellij_idea | android_studio |
|:------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|:---------------:|:-------------:|:--------------:|
| `max_line_length`<br/><i>Maximum length of a (regular) line. This property is ignored in case the `max-line-length` rule is disabled, or when using Ktlint via a third party integration that does not provide this rule.</i> |       140       |     `off`     |     `100`      |

Rule id: `standard:wrapping`

Suppress or disable rule (1)
{ .annotate }

1. Suppress rule in code with annotation below:
    ```kotlin
    @Suppress("ktlint:standard:wrapping")
    ```
   Enable rule via `.editorconfig`
    ```editorconfig
    ktlint_standard_wrapping = enabled
    ```
   Disable rule via `.editorconfig`
    ```editorconfig
    ktlint_standard_wrapping = disabled
    ```
