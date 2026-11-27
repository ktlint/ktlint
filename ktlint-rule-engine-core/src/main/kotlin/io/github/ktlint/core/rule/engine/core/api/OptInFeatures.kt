package io.github.ktlint.core.rule.engine.core.api

@Target(
    AnnotationTarget.CLASS,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.TYPEALIAS,
    AnnotationTarget.PROPERTY,
)
@Retention(AnnotationRetention.BINARY)
@RequiresOptIn(
    message = "This Ktlint feature is highly experimental, and most probably will change in the future releases.",
    level = RequiresOptIn.Level.ERROR,
)
public annotation class FeatureInAlphaState

@Target(
    AnnotationTarget.CLASS,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.TYPEALIAS,
)
@Retention(AnnotationRetention.BINARY)
@RequiresOptIn(
    message = "This Ktlint feature is experimental, and may change in the future releases.",
)
public annotation class FeatureInBetaState
