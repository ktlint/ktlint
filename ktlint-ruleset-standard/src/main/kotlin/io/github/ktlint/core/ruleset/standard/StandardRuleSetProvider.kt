package io.github.ktlint.core.ruleset.standard

import io.github.ktlint.core.cli.ruleset.core.api.RuleSetV2Provider
import io.github.ktlint.core.rule.engine.core.api.RuleSetId
import io.github.ktlint.core.rule.engine.core.api.RuleV2Provider
import io.github.ktlint.core.ruleset.standard.rules.AnnotationRule
import io.github.ktlint.core.ruleset.standard.rules.AnnotationSpacingRule
import io.github.ktlint.core.ruleset.standard.rules.ArgumentListWrappingRule
import io.github.ktlint.core.ruleset.standard.rules.BackingPropertyNamingRule
import io.github.ktlint.core.ruleset.standard.rules.BinaryExpressionWrappingRule
import io.github.ktlint.core.ruleset.standard.rules.BlankLineBeforeDeclarationRule
import io.github.ktlint.core.ruleset.standard.rules.BlankLineBeforeFileAnnotation
import io.github.ktlint.core.ruleset.standard.rules.BlankLineBeforeImports
import io.github.ktlint.core.ruleset.standard.rules.BlankLineBeforePackage
import io.github.ktlint.core.ruleset.standard.rules.BlankLineBetweenWhenConditions
import io.github.ktlint.core.ruleset.standard.rules.BlockCommentInitialStarAlignmentRule
import io.github.ktlint.core.ruleset.standard.rules.CallExpressionWrappingRule
import io.github.ktlint.core.ruleset.standard.rules.ChainMethodContinuationRule
import io.github.ktlint.core.ruleset.standard.rules.ChainWrappingRule
import io.github.ktlint.core.ruleset.standard.rules.ClassNamingRule
import io.github.ktlint.core.ruleset.standard.rules.ClassSignatureRule
import io.github.ktlint.core.ruleset.standard.rules.CommentSpacingRule
import io.github.ktlint.core.ruleset.standard.rules.CommentWrappingRule
import io.github.ktlint.core.ruleset.standard.rules.ContextParameterListWrappingRule
import io.github.ktlint.core.ruleset.standard.rules.ContextReceiverWrappingRule
import io.github.ktlint.core.ruleset.standard.rules.EnumEntryNameCaseRule
import io.github.ktlint.core.ruleset.standard.rules.EnumWrappingRule
import io.github.ktlint.core.ruleset.standard.rules.ExpressionOperandWrappingRule
import io.github.ktlint.core.ruleset.standard.rules.FilenameRule
import io.github.ktlint.core.ruleset.standard.rules.FinalNewlineRule
import io.github.ktlint.core.ruleset.standard.rules.FunKeywordSpacingRule
import io.github.ktlint.core.ruleset.standard.rules.FunctionExpressionBodyRule
import io.github.ktlint.core.ruleset.standard.rules.FunctionLiteralRule
import io.github.ktlint.core.ruleset.standard.rules.FunctionNamingRule
import io.github.ktlint.core.ruleset.standard.rules.FunctionReturnTypeSpacingRule
import io.github.ktlint.core.ruleset.standard.rules.FunctionSignatureRule
import io.github.ktlint.core.ruleset.standard.rules.FunctionStartOfBodySpacingRule
import io.github.ktlint.core.ruleset.standard.rules.FunctionTypeModifierSpacingRule
import io.github.ktlint.core.ruleset.standard.rules.FunctionTypeReferenceSpacingRule
import io.github.ktlint.core.ruleset.standard.rules.IfElseBracingRule
import io.github.ktlint.core.ruleset.standard.rules.IfElseWrappingRule
import io.github.ktlint.core.ruleset.standard.rules.ImportOrderingRule
import io.github.ktlint.core.ruleset.standard.rules.IndentationRule
import io.github.ktlint.core.ruleset.standard.rules.KdocDelimiterRule
import io.github.ktlint.core.ruleset.standard.rules.KdocRule
import io.github.ktlint.core.ruleset.standard.rules.KdocWrappingRule
import io.github.ktlint.core.ruleset.standard.rules.LambdaReturnRule
import io.github.ktlint.core.ruleset.standard.rules.MaxLineLengthRule
import io.github.ktlint.core.ruleset.standard.rules.MixedConditionOperatorsRule
import io.github.ktlint.core.ruleset.standard.rules.ModifierListSpacingRule
import io.github.ktlint.core.ruleset.standard.rules.ModifierOrderRule
import io.github.ktlint.core.ruleset.standard.rules.MultiLineIfElseRule
import io.github.ktlint.core.ruleset.standard.rules.MultilineExpressionWrappingRule
import io.github.ktlint.core.ruleset.standard.rules.MultilineLoopRule
import io.github.ktlint.core.ruleset.standard.rules.NoBlankLineAtStartOfFileRule
import io.github.ktlint.core.ruleset.standard.rules.NoBlankLineBeforeRbraceRule
import io.github.ktlint.core.ruleset.standard.rules.NoBlankLineInListRule
import io.github.ktlint.core.ruleset.standard.rules.NoBlankLinesInChainedMethodCallsRule
import io.github.ktlint.core.ruleset.standard.rules.NoConsecutiveBlankLinesRule
import io.github.ktlint.core.ruleset.standard.rules.NoConsecutiveCommentsRule
import io.github.ktlint.core.ruleset.standard.rules.NoEmptyClassBodyRule
import io.github.ktlint.core.ruleset.standard.rules.NoEmptyFileRule
import io.github.ktlint.core.ruleset.standard.rules.NoEmptyFirstLineInClassBodyRule
import io.github.ktlint.core.ruleset.standard.rules.NoEmptyFirstLineInMethodBlockRule
import io.github.ktlint.core.ruleset.standard.rules.NoLineBreakAfterElseRule
import io.github.ktlint.core.ruleset.standard.rules.NoLineBreakBeforeAssignmentRule
import io.github.ktlint.core.ruleset.standard.rules.NoMultipleSpacesRule
import io.github.ktlint.core.ruleset.standard.rules.NoSingleLineBlockCommentRule
import io.github.ktlint.core.ruleset.standard.rules.NoTrailingSpacesRule
import io.github.ktlint.core.ruleset.standard.rules.NoUnitReturnRule
import io.github.ktlint.core.ruleset.standard.rules.NoUnusedImportsRule
import io.github.ktlint.core.ruleset.standard.rules.NoWildcardImportsRule
import io.github.ktlint.core.ruleset.standard.rules.NullableTypeSpacingRule
import io.github.ktlint.core.ruleset.standard.rules.PackageNameRule
import io.github.ktlint.core.ruleset.standard.rules.ParameterListSpacingRule
import io.github.ktlint.core.ruleset.standard.rules.ParameterListWrappingRule
import io.github.ktlint.core.ruleset.standard.rules.ParameterWrappingRule
import io.github.ktlint.core.ruleset.standard.rules.PropertyNamingRule
import io.github.ktlint.core.ruleset.standard.rules.PropertyWrappingRule
import io.github.ktlint.core.ruleset.standard.rules.SemicolonRule
import io.github.ktlint.core.ruleset.standard.rules.SpacingAroundAngleBracketsRule
import io.github.ktlint.core.ruleset.standard.rules.SpacingAroundColonRule
import io.github.ktlint.core.ruleset.standard.rules.SpacingAroundCommaRule
import io.github.ktlint.core.ruleset.standard.rules.SpacingAroundCurlyRule
import io.github.ktlint.core.ruleset.standard.rules.SpacingAroundDotRule
import io.github.ktlint.core.ruleset.standard.rules.SpacingAroundDoubleColonRule
import io.github.ktlint.core.ruleset.standard.rules.SpacingAroundKeywordRule
import io.github.ktlint.core.ruleset.standard.rules.SpacingAroundOperatorsRule
import io.github.ktlint.core.ruleset.standard.rules.SpacingAroundParensRule
import io.github.ktlint.core.ruleset.standard.rules.SpacingAroundRangeOperatorRule
import io.github.ktlint.core.ruleset.standard.rules.SpacingAroundSquareBracketsRule
import io.github.ktlint.core.ruleset.standard.rules.SpacingAroundUnaryOperatorRule
import io.github.ktlint.core.ruleset.standard.rules.SpacingBetweenDeclarationsWithAnnotationsRule
import io.github.ktlint.core.ruleset.standard.rules.SpacingBetweenDeclarationsWithCommentsRule
import io.github.ktlint.core.ruleset.standard.rules.SpacingBetweenFunctionNameAndOpeningParenthesisRule
import io.github.ktlint.core.ruleset.standard.rules.StatementWrappingRule
import io.github.ktlint.core.ruleset.standard.rules.StringTemplateIndentRule
import io.github.ktlint.core.ruleset.standard.rules.StringTemplateRule
import io.github.ktlint.core.ruleset.standard.rules.ThenSpacingRule
import io.github.ktlint.core.ruleset.standard.rules.TrailingCommaOnCallSiteRule
import io.github.ktlint.core.ruleset.standard.rules.TrailingCommaOnDeclarationSiteRule
import io.github.ktlint.core.ruleset.standard.rules.TryCatchFinallySpacingRule
import io.github.ktlint.core.ruleset.standard.rules.TypeArgumentCommentRule
import io.github.ktlint.core.ruleset.standard.rules.TypeArgumentListSpacingRule
import io.github.ktlint.core.ruleset.standard.rules.TypeParameterCommentRule
import io.github.ktlint.core.ruleset.standard.rules.TypeParameterListSpacingRule
import io.github.ktlint.core.ruleset.standard.rules.UnnecessaryParenthesesBeforeTrailingLambdaRule
import io.github.ktlint.core.ruleset.standard.rules.ValueArgumentCommentRule
import io.github.ktlint.core.ruleset.standard.rules.ValueParameterCommentRule
import io.github.ktlint.core.ruleset.standard.rules.WhenEntryBracing
import io.github.ktlint.core.ruleset.standard.rules.WrappingRule

public class StandardRuleSetProvider : RuleSetV2Provider(RuleSetId.STANDARD) {
    @Suppress("LongMethod")
    override fun getRuleProviders(): Set<RuleV2Provider> =
        setOf(
            RuleV2Provider { AnnotationRule() },
            RuleV2Provider { AnnotationSpacingRule() },
            RuleV2Provider { ArgumentListWrappingRule() },
            RuleV2Provider { BackingPropertyNamingRule() },
            RuleV2Provider { BinaryExpressionWrappingRule() },
            RuleV2Provider { BlankLineBeforeDeclarationRule() },
            RuleV2Provider { BlankLineBeforeFileAnnotation() },
            RuleV2Provider { BlankLineBeforeImports() },
            RuleV2Provider { BlankLineBeforePackage() },
            RuleV2Provider { BlankLineBetweenWhenConditions() },
            RuleV2Provider { BlockCommentInitialStarAlignmentRule() },
            RuleV2Provider { CallExpressionWrappingRule() },
            RuleV2Provider { ChainMethodContinuationRule() },
            RuleV2Provider { ChainWrappingRule() },
            RuleV2Provider { ClassNamingRule() },
            RuleV2Provider { ClassSignatureRule() },
            RuleV2Provider { CommentSpacingRule() },
            RuleV2Provider { CommentWrappingRule() },
            RuleV2Provider { ContextReceiverWrappingRule() },
            RuleV2Provider { ContextParameterListWrappingRule() },
            RuleV2Provider { EnumEntryNameCaseRule() },
            RuleV2Provider { EnumWrappingRule() },
            RuleV2Provider { ExpressionOperandWrappingRule() },
            RuleV2Provider { FilenameRule() },
            RuleV2Provider { FinalNewlineRule() },
            RuleV2Provider { FunKeywordSpacingRule() },
            RuleV2Provider { FunctionExpressionBodyRule() },
            RuleV2Provider { FunctionLiteralRule() },
            RuleV2Provider { FunctionNamingRule() },
            RuleV2Provider { FunctionReturnTypeSpacingRule() },
            RuleV2Provider { FunctionSignatureRule() },
            RuleV2Provider { FunctionStartOfBodySpacingRule() },
            RuleV2Provider { FunctionTypeModifierSpacingRule() },
            RuleV2Provider { FunctionTypeReferenceSpacingRule() },
            RuleV2Provider { IfElseBracingRule() },
            RuleV2Provider { IfElseWrappingRule() },
            RuleV2Provider { ImportOrderingRule() },
            RuleV2Provider { IndentationRule() },
            RuleV2Provider { KdocDelimiterRule() },
            RuleV2Provider { KdocRule() },
            RuleV2Provider { KdocWrappingRule() },
            RuleV2Provider { LambdaReturnRule() },
            RuleV2Provider { MaxLineLengthRule() },
            RuleV2Provider { MixedConditionOperatorsRule() },
            RuleV2Provider { ModifierListSpacingRule() },
            RuleV2Provider { ModifierOrderRule() },
            RuleV2Provider { MultiLineIfElseRule() },
            RuleV2Provider { MultilineExpressionWrappingRule() },
            RuleV2Provider { MultilineLoopRule() },
            RuleV2Provider { NoBlankLineAtStartOfFileRule() },
            RuleV2Provider { NoBlankLineBeforeRbraceRule() },
            RuleV2Provider { NoBlankLineInListRule() },
            RuleV2Provider { NoBlankLinesInChainedMethodCallsRule() },
            RuleV2Provider { NoConsecutiveBlankLinesRule() },
            RuleV2Provider { NoConsecutiveCommentsRule() },
            RuleV2Provider { NoEmptyClassBodyRule() },
            RuleV2Provider { NoEmptyFileRule() },
            RuleV2Provider { NoEmptyFirstLineInClassBodyRule() },
            RuleV2Provider { NoEmptyFirstLineInMethodBlockRule() },
            RuleV2Provider { NoLineBreakAfterElseRule() },
            RuleV2Provider { NoLineBreakBeforeAssignmentRule() },
            RuleV2Provider { NoMultipleSpacesRule() },
            RuleV2Provider { NoSingleLineBlockCommentRule() },
            RuleV2Provider { NoTrailingSpacesRule() },
            RuleV2Provider { NoUnitReturnRule() },
            RuleV2Provider { NoUnusedImportsRule() },
            RuleV2Provider { NoWildcardImportsRule() },
            RuleV2Provider { NullableTypeSpacingRule() },
            RuleV2Provider { PackageNameRule() },
            RuleV2Provider { ParameterListSpacingRule() },
            RuleV2Provider { ParameterListWrappingRule() },
            RuleV2Provider { ParameterWrappingRule() },
            RuleV2Provider { PropertyNamingRule() },
            RuleV2Provider { PropertyWrappingRule() },
            RuleV2Provider { SemicolonRule() },
            RuleV2Provider { SpacingAroundAngleBracketsRule() },
            RuleV2Provider { SpacingAroundColonRule() },
            RuleV2Provider { SpacingAroundCommaRule() },
            RuleV2Provider { SpacingAroundCurlyRule() },
            RuleV2Provider { SpacingAroundDotRule() },
            RuleV2Provider { SpacingAroundDoubleColonRule() },
            RuleV2Provider { SpacingAroundKeywordRule() },
            RuleV2Provider { SpacingAroundOperatorsRule() },
            RuleV2Provider { SpacingAroundParensRule() },
            RuleV2Provider { SpacingAroundRangeOperatorRule() },
            RuleV2Provider { SpacingAroundSquareBracketsRule() },
            RuleV2Provider { SpacingAroundUnaryOperatorRule() },
            RuleV2Provider { SpacingBetweenDeclarationsWithAnnotationsRule() },
            RuleV2Provider { SpacingBetweenDeclarationsWithCommentsRule() },
            RuleV2Provider { SpacingBetweenFunctionNameAndOpeningParenthesisRule() },
            RuleV2Provider { StatementWrappingRule() },
            RuleV2Provider { StringTemplateIndentRule() },
            RuleV2Provider { StringTemplateRule() },
            RuleV2Provider { ThenSpacingRule() },
            RuleV2Provider { TrailingCommaOnCallSiteRule() },
            RuleV2Provider { TrailingCommaOnDeclarationSiteRule() },
            RuleV2Provider { TryCatchFinallySpacingRule() },
            RuleV2Provider { TypeArgumentCommentRule() },
            RuleV2Provider { TypeArgumentListSpacingRule() },
            RuleV2Provider { TypeParameterCommentRule() },
            RuleV2Provider { TypeParameterListSpacingRule() },
            RuleV2Provider { UnnecessaryParenthesesBeforeTrailingLambdaRule() },
            RuleV2Provider { ValueArgumentCommentRule() },
            RuleV2Provider { ValueParameterCommentRule() },
            RuleV2Provider { WhenEntryBracing() },
            RuleV2Provider { WrappingRule() },
        )
}
