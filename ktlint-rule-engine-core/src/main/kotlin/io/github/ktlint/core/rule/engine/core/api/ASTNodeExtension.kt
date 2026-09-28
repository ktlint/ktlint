package io.github.ktlint.core.rule.engine.core.api

import io.github.ktlint.core.rule.engine.core.api.ElementType.ANNOTATED_EXPRESSION
import io.github.ktlint.core.rule.engine.core.api.ElementType.ANNOTATION_ENTRY
import io.github.ktlint.core.rule.engine.core.api.ElementType.EOL_COMMENT
import io.github.ktlint.core.rule.engine.core.api.ElementType.FILE
import io.github.ktlint.core.rule.engine.core.api.ElementType.FILE_ANNOTATION_LIST
import io.github.ktlint.core.rule.engine.core.api.ElementType.MODIFIER_LIST
import io.github.ktlint.core.rule.engine.core.api.ElementType.STRING_TEMPLATE
import io.github.ktlint.core.rule.engine.core.api.ElementType.VALUE_ARGUMENT
import io.github.ktlint.core.rule.engine.core.api.ElementType.VALUE_ARGUMENT_LIST
import io.github.ktlint.core.rule.engine.core.api.ElementType.WHITE_SPACE
import org.jetbrains.kotlin.KtNodeType
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.psi.PsiElement
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.CompositeElement
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.LeafElement
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.PsiWhiteSpaceImpl
import org.jetbrains.kotlin.com.intellij.psi.tree.IElementType
import org.jetbrains.kotlin.com.intellij.psi.tree.TokenSet
import org.jetbrains.kotlin.lexer.KtKeywordToken
import org.jetbrains.kotlin.lexer.KtToken
import org.jetbrains.kotlin.psi.KtAnnotated
import org.jetbrains.kotlin.psi.KtElement
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.psiUtil.leaves
import org.jetbrains.kotlin.psi.psiUtil.parents
import org.jetbrains.kotlin.psi.stubs.elements.KtFileElementType
import org.jetbrains.kotlin.psi.stubs.elements.KtStubElementType
import org.jetbrains.kotlin.psi.stubs.elements.KtTokenSets
import org.jetbrains.kotlin.util.prefixIfNot

/**
 * The next leaf after [ASTNode], or null when no such leaf exists.
 */
public val ASTNode.nextLeaf: ASTNode?
    get(): ASTNode? {
        var node = this.nextLeafAny
        while (node != null && node.textLength == 0) {
            node = node.nextLeafAny
        }
        return node
    }

/**
 * The next leaf after [ASTNode] matching [predicate], or null when no such leaf exists.
 */
public fun ASTNode.nextLeaf(predicate: (ASTNode) -> Boolean): ASTNode? {
    var node = this.nextLeafAny
    while (node != null && !predicate(node)) {
        node = node.nextLeafAny
    }
    return node
}

private val ASTNode.nextLeafAny
    get(): ASTNode? {
        if (firstChildNode == null) {
            return nextLeafStrict
        }
        var node = this
        while (node.firstChildNode != null) {
            node = node.firstChildNode
        }
        return node
    }

private val ASTNode.nextLeafStrict
    get(): ASTNode? = nextSibling?.firstChildLeafOrSelf ?: parent?.nextLeafStrict

/**
 * The first child leaf in [ASTNode], or [ASTNode] when it does not contain any children.
 */
public val ASTNode.firstChildLeafOrSelf: ASTNode
    get(): ASTNode {
        var node = this
        while (node.firstChildNode != null) {
            node = node.firstChildNode
        }
        return node
    }

/**
 * The previous leaf before [ASTNode], or null when no such leaf exists.
 */
public val ASTNode.prevLeaf: ASTNode?
    get(): ASTNode? {
        var node = this.prevLeafAny
        while (node != null && node.textLength == 0) {
            node = node.prevLeafAny
        }
        return node
    }

/**
 * The previous leaf before [ASTNode] matching [predicate], or null when no such leaf exists.
 */
public fun ASTNode.prevLeaf(predicate: (ASTNode) -> Boolean): ASTNode? {
    var node = this.prevLeafAny
    while (node != null && !predicate(node)) {
        node = node.prevLeafAny
    }
    return node
}

private val ASTNode.prevLeafAny
    get(): ASTNode? = prevSibling?.lastChildLeafOrSelf ?: parent?.prevLeafAny

/**
 * The last child leaf in [ASTNode], or [ASTNode] when it does not contain any children.
 */
public val ASTNode.lastChildLeafOrSelf: ASTNode
    get(): ASTNode {
        var node = this
        while (node.lastChildNode != null) {
            node = node.lastChildNode
        }
        return node
    }

/**
 *  `true` when [ASTNode] is not a whitespace element, and is not part of a comment.
 */
public val ASTNode.isCode: Boolean
    get(): Boolean = !isWhiteSpace && !isPartOfComment

/**
 * The previous code leaf before [ASTNode], or null when no such leaf exists.
 */
public val ASTNode.prevCodeLeaf: ASTNode?
    get(): ASTNode? {
        var node = prevLeaf
        while (node != null && !node.isCode) {
            node = node.prevLeaf
        }
        return node
    }

/**
 * The next code leaf after [ASTNode], or null when no such leaf exists.
 */
public val ASTNode.nextCodeLeaf: ASTNode?
    get(): ASTNode? {
        var node = nextLeaf
        while (node != null && !node.isCode) {
            node = node.nextLeaf
        }
        return node
    }

/**
 * The previous code sibling of [ASTNode], or null when no such sibling exists.
 */
public val ASTNode.prevCodeSibling: ASTNode?
    get(): ASTNode? = prevSibling { it.isCode }

/**
 * The previous sibling of [ASTNode] matching [predicate], or null when no such sibling exists.
 */
public inline fun ASTNode.prevSibling(predicate: (ASTNode) -> Boolean = { true }): ASTNode? {
    var node = this.treePrev
    while (node != null) {
        if (predicate(node)) {
            return node
        }
        node = node.treePrev
    }
    return null
}

/**
 * The previous sibling of [ASTNode], or null when no such sibling exists.
 */
public inline val ASTNode.prevSibling: ASTNode?
    get(): ASTNode? = treePrev

/**
 * The next code sibling of [ASTNode], or null when no such sibling exists.
 */
public val ASTNode.nextCodeSibling: ASTNode?
    get(): ASTNode? = nextSibling { it.isCode }

/**
 * The next sibling of [ASTNode] matching [predicate], or null when no such sibling exists.
 */
public inline fun ASTNode.nextSibling(predicate: (ASTNode) -> Boolean): ASTNode? {
    var node = this.treeNext
    while (node != null) {
        if (predicate(node)) {
            return node
        }
        node = node.treeNext
    }
    return null
}

/**
 * The next sibling of [ASTNode], or null when no such sibling exists.
 */
public inline val ASTNode.nextSibling: ASTNode?
    get(): ASTNode? = treeNext

/**
 * The parent of the [ASTNode]. This counterpart of the PSI `treeParent` function is nullable as the root node does not have a parent.
 */
public inline val ASTNode.parent: ASTNode?
    get(): ASTNode? = treeParent

/**
 * Find the first parent from [ASTNode] with [elementType].
 */
public fun ASTNode.findParentByType(elementType: IElementType): ASTNode? {
    var node: ASTNode? = parent
    while (node != null) {
        if (node.elementType == elementType) {
            return node
        }
        node = node.parent
    }
    return null
}

/**
 * Find the first parent from [ASTNode] matching [predicate].
 */
public fun ASTNode.parent(predicate: (ASTNode) -> Boolean): ASTNode? {
    var node: ASTNode? = this.parent
    while (node != null && !predicate(node)) {
        node = node.parent
    }
    return node
}

/**
 * `true` when the [ASTNode], or any of its parents, has an element type in [tokenSet]
 */
public fun ASTNode.isPartOf(tokenSet: TokenSet): Boolean = elementType in tokenSet || parent { it.elementType in tokenSet } != null

/**
 * `true` when the [ASTNode], or any of its parents, has [elementType]
 */
public fun ASTNode.isPartOf(elementType: IElementType): Boolean = this.elementType == elementType || findParentByType(elementType) != null

/**
 * `true` when [ASTNode] is part of a string template
 */
public val ASTNode.isPartOfString: Boolean
    get(): Boolean = findParentByType(STRING_TEMPLATE) != null

/**
 * `true` when [ASTNode] is a whitespace element
 */
public val ASTNode?.isWhiteSpace: Boolean
    get() = this != null && elementType == WHITE_SPACE

/**
 * `true` when [ASTNode] is a whitespace element that contains a newline
 */
public val ASTNode?.isWhiteSpaceWithNewline: Boolean
    get(): Boolean = this != null && isWhiteSpace && textContains('\n')

/**
 * `true` when [ASTNode] is a whitespace element not containing a newline
 */
public val ASTNode?.isWhiteSpaceWithoutNewline: Boolean
    get(): Boolean = this != null && isWhiteSpace && !textContains('\n')

/**
 * `true` when [ASTNode] is null, or when [ASTNode] is a whitespace element not containing a newline
 */
public val ASTNode?.isWhiteSpaceWithoutNewlineOrNull: Boolean
    get(): Boolean = this == null || isWhiteSpaceWithoutNewline

/**
 * `true` when [ASTNode] is the root (e.g. FILE) element type
 */
public val ASTNode.isRoot: Boolean
    get(): Boolean = elementType == FILE

/**
 * `true` when [ASTNode] is a leaf element type
 */
public val ASTNode.isLeaf: Boolean
    get(): Boolean = firstChildNode == null

/**
 * `true` when [ASTNode] is part of a comment
 */
public val ASTNode.isPartOfComment: Boolean
    get(): Boolean = isPartOf(TokenSets.COMMENTS)

/**
 * A sequence of the children of [ASTNode]
 */
public val ASTNode.children: Sequence<ASTNode>
    get(): Sequence<ASTNode> = generateSequence(firstChildNode) { node -> node.nextSibling }

/**
 * A sequence of the children of [ASTNode] recursively iterated
 */
public val ASTNode.recursiveChildren: Sequence<ASTNode>
    get(): Sequence<ASTNode> = recursiveChildrenInternal(false)

private fun ASTNode.recursiveChildrenInternal(includeSelf: Boolean = false): Sequence<ASTNode> =
    sequence {
        if (includeSelf) {
            yield(this@recursiveChildrenInternal)
        }
        this@recursiveChildrenInternal.children.forEach { yieldAll(it.recursiveChildrenInternal(includeSelf = true)) }
    }

/**
 * Updates or inserts a new whitespace element with [text] before the given node. If the node itself is a whitespace
 * then its contents is replaced with [text]. If the node is a (nested) composite element, the whitespace element is
 * added after the previous leaf node.
 */
public fun ASTNode.upsertWhitespaceBeforeMe(text: String) {
    if (isLeaf) {
        if (isWhiteSpace) {
            return replaceTextWith(text)
        }
        val previous = prevSibling ?: this.prevLeaf
        when {
            previous != null && previous.isWhiteSpace -> {
                previous.replaceTextWith(text)
            }

            parent?.firstChildNode == this -> {
                // Never insert a whitespace node as first node in a composite node
                parent?.upsertWhitespaceBeforeMe(text)
            }

            else -> {
                PsiWhiteSpaceImpl(text).also { psiWhiteSpace ->
                    (psi as LeafElement).rawInsertBeforeMe(psiWhiteSpace)
                }
            }
        }
    } else {
        when (val prevSibling = prevSibling) {
            null -> {
                // Never insert a whitespace element as first child node in a composite node. Instead, upsert just before the composite node
                parent?.upsertWhitespaceBeforeMe(text)
            }

            is LeafElement -> {
                prevSibling.upsertWhitespaceAfterMe(text)
            }

            else -> {
                // Insert in between two composite nodes
                PsiWhiteSpaceImpl(text).also { psiWhiteSpace ->
                    parent?.addChild(psiWhiteSpace.node, this)
                }
            }
        }
    }
}

/**
 * Replace text [ASTNode] with [text]. [ASTNode] must be a [LeafElement].
 */
public fun ASTNode.replaceTextWith(text: String) {
    require(this is LeafElement)
    if (this.text != text) {
        // Since Kotlin 2.4.20 the "rawReplaceWithText" sometimes result in messing up the reference to the previous and next leaf/siblings.
        rawInsertBeforeMe(PsiWhiteSpaceImpl(text))
        remove()
    }
}

/**
 * Updates or inserts a new whitespace element with [text] after the given node. If the node itself is a whitespace
 * then its contents is replaced with [text]. If the node is a (nested) composite element, the whitespace element is
 * added after the last child leaf.
 */
public fun ASTNode.upsertWhitespaceAfterMe(text: String) {
    if (isLeaf) {
        if (isWhiteSpace) {
            return replaceTextWith(text)
        }
        val next = nextSibling ?: nextLeaf
        when {
            next != null && next.isWhiteSpace -> {
                next.replaceTextWith(text)
            }

            parent?.lastChildNode == this -> {
                // Never insert whitespace as last node in a composite node
                parent?.upsertWhitespaceAfterMe(text)
            }

            else -> {
                PsiWhiteSpaceImpl(text).also { psiWhiteSpace ->
                    (psi as LeafElement).rawInsertAfterMe(psiWhiteSpace)
                }
            }
        }
    } else {
        when (val nextSibling = nextSibling) {
            null -> {
                // Never insert a whitespace element as last child node in a composite node. Instead, upsert just after the composite node
                parent?.upsertWhitespaceAfterMe(text)
            }

            is LeafElement -> {
                nextSibling.upsertWhitespaceBeforeMe(text)
            }

            else -> {
                // Insert in between two composite nodes
                PsiWhiteSpaceImpl(text).also { psiWhiteSpace ->
                    parent?.addChild(psiWhiteSpace.node, nextSibling)
                }
            }
        }
    }
}

/**
 * The column offset at which the [ASTNode] starts
 */
public val ASTNode.column: Int
    get() {
        var leaf = prevLeaf
        var offsetToTheLeft = 0
        while (leaf != null) {
            if (leaf.isWhiteSpaceWithNewline) {
                offsetToTheLeft += leaf.textLength - 1 - leaf.text.lastIndexOf('\n')
                break
            }
            offsetToTheLeft += leaf.textLength
            leaf = leaf.prevLeaf
        }
        return offsetToTheLeft + 1
    }

/**
 * The indentation of [ASTNode] including the newline prefix
 */
public val ASTNode.indent: String
    get(): String = indentInternal().prefixIfNot("\n")

/**
 * The indentation of [ASTNode] excluding the newline prefix
 */
public val ASTNode.indentWithoutNewlinePrefix: String
    get(): String = indentInternal().removePrefix("\n")

/**
 * Get the current indentation of the line containing the [ASTNode]
 */
private fun ASTNode.indentInternal(): String =
    leaves(forward = false)
        .firstOrNull { it.isWhiteSpaceWithNewline }
        ?.text
        ?.substringAfterLast('\n')
        .orEmpty() // Fallback if node is not preceded by any newline character

/**
 * Verifies that a leaf containing a newline exist in the closed range [from] - [to]. Also, returns true in case any of
 * the boundary nodes [from] or [to] contains a newline.
 */
public fun hasNewLineInClosedRange(
    from: ASTNode,
    to: ASTNode,
): Boolean = leavesInClosedRange(from, to).any { it.textContains('\n') }

/**
 * Verifies that no leaf contains a newline in the closed range [from] - [to]. Also, the boundary nodes [from] and [to]
 * should not contain a newline.
 */
public fun noNewLineInClosedRange(
    from: ASTNode,
    to: ASTNode,
): Boolean = leavesInClosedRange(from, to).none { it.textContains('\n') }

/**
 * Verifies that no leaf contains a newline in the open range [from] - [to]. This means that the boundary nodes are excluded from the range
 * in case they would happen to be a leaf node. In case [from] is a [CompositeElement] than the first leaf node in the sequence is the first
 * leaf node in this [CompositeElement]. In case [to] is a [CompositeElement] than the last node in the sequence is the last leaf node prior
 * to this [CompositeElement].
 */
public fun noNewLineInOpenRange(
    from: ASTNode,
    to: ASTNode,
): Boolean = leavesInOpenRange(from, to).none { it.textContains('\n') }

/**
 * Creates a sequence of leaf nodes in the open range [from] - [to]. This means that the boundary nodes are excluded
 * from the range in case they would happen to be a leaf node. In case [from] is a [CompositeElement] than the first
 * leaf node in the sequence is the first leaf node in this [CompositeElement]. In case [to] is a [CompositeElement]
 * than the last node in the sequence is the last leaf node prior to this [CompositeElement].
 */
public fun leavesInOpenRange(
    from: ASTNode,
    to: ASTNode,
): Sequence<ASTNode> =
    from
        .leaves()
        .takeWhile { it != to && it != to.lastChildLeafOrSelf }

/**
 * Creates a sequence of leaf nodes in the closed range [from] - [to]. This means that the boundary nodes are included from the range in
 * case they would happen to be a leaf node. In case [from] is a [CompositeElement] than the first leaf node in the sequence is the first
 * leaf node in this [CompositeElement]. In case [to] is a [CompositeElement] than the last node in the sequence is the last leaf node of
 * this [CompositeElement].
 */
public fun leavesInClosedRange(
    from: ASTNode,
    to: ASTNode,
): Sequence<ASTNode> {
    val stopAtLeaf =
        to
            .lastChildLeafOrSelf
            .nextLeaf
    return from
        .firstChildLeafOrSelf
        .leavesForwardsIncludingSelf
        .takeWhile { it != stopAtLeaf }
}

/**
 * Creates a forward sequence of leaves including the [ASTNode] in case it is a [LeafElement] itself. Otherwise, an empty sequence is
 * returned.
 */
public val ASTNode.leavesForwardsIncludingSelf: Sequence<ASTNode>
    get(): Sequence<ASTNode> = sequenceOfLeafOrEmpty.plus(leaves(forward = true))

/**
 * Creates a backward sequence of leaves including the [ASTNode] in case it is a [LeafElement] itself. Otherwise, an empty sequence is
 *  * returned.
 */
public val ASTNode.leavesBackwardsIncludingSelf: Sequence<ASTNode>
    get(): Sequence<ASTNode> = sequenceOfLeafOrEmpty.plus(leaves(forward = false))

private val ASTNode.sequenceOfLeafOrEmpty
    get(): Sequence<ASTNode> {
        val sequence =
            if (isLeaf) {
                sequenceOf(this)
            } else {
                emptySequence()
            }
        return sequence
    }

/**
 * A sequence of all leaves on the same line at which the [ASTNode] starts
 */
public val ASTNode.leavesOnLine: Sequence<ASTNode>
    get(): Sequence<ASTNode> {
        val takeAll = lastLeafOnLineOrNull == null
        return firstLeafOnLineOrSelf
            .leavesForwardsIncludingSelf
            .takeWhile { takeAll || it.prevLeaf != lastLeafOnLineOrNull }
    }

/**
 * Take all nodes preceding the whitespace before the EOL comment
 */
public fun Sequence<ASTNode>.dropTrailingEolComment(): Sequence<ASTNode> =
    takeWhile {
        !(it.isWhiteSpaceWithoutNewline && it.nextLeaf?.elementType == EOL_COMMENT) &&
            // But if EOL-comment not preceded by whitespace than take all nodes before the EOL comment
            it.elementType != EOL_COMMENT
    }

internal val ASTNode.firstLeafOnLineOrSelf
    get() =
        prevLeaf { (it.textContains('\n') && !it.isPartOfComment) || it.prevLeaf == null }
            ?: this

internal val ASTNode.lastLeafOnLineOrNull
    get() = nextLeaf { it.textContains('\n') }?.prevLeaf

/**
 * The length of the first non-empty line in the [Sequence] of [ASTNode]'s
 */
public val Sequence<ASTNode>.lineLength: Int
    get(): Int {
        val first = firstOrNull() ?: return 0
        require(first.textContains('\n') || first.prevLeaf == null) {
            "First node in non-empty sequence must be a whitespace containing a newline"
        }
        return joinToString(separator = "") { it.text }
            // If a line is preceded by a blank line then the ident contains multiple newline chars
            .dropWhile { it == '\n' }
            // In case the last element on the line would contain a newline then only include chars before that newline. Note that this should
            // not occur if the AST is parsed correctly
            .takeWhile { it != '\n' }
            .length
    }

/**
 * `true` if [ASTNode] is found after a code sibling with [afterElementType]
 */
public fun ASTNode.afterCodeSibling(afterElementType: IElementType): Boolean =
    prevSibling { it.isCode && it.elementType == afterElementType } != null

/**
 * `true` if [ASTNode] is found before a code sibling with [beforeElementType]
 */
public fun ASTNode.beforeCodeSibling(beforeElementType: IElementType): Boolean =
    nextSibling { it.isCode && it.elementType == beforeElementType } != null

/**
 * `true` if [ASTNode] is found between a code sibling with [afterElementType] and a code sibling with [beforeElementType]
 */
public fun ASTNode.betweenCodeSiblings(
    afterElementType: IElementType,
    beforeElementType: IElementType,
): Boolean = afterCodeSibling(afterElementType) && beforeCodeSibling(beforeElementType)

/**
 * `true` if [ASTNode] contains a modifier list with an element of [iElementType]
 */
public fun ASTNode.hasModifier(iElementType: IElementType): Boolean =
    findChildByType(MODIFIER_LIST)
        ?.children
        .orEmpty()
        .any { it.elementType == iElementType }

/**
 * Replaces [ASTNode] with [node]
 */
public fun ASTNode.replaceWith(node: ASTNode) {
    parent?.addChild(node, this)
    this.remove()
}

/**
 * Removes [ASTNode] from its current parent
 */
public fun ASTNode.remove() {
    parent?.removeChild(this)
}

/**
 * Searches the receiver [ASTNode] recursively, returning the first child with type [elementType]. If none are found, returns `null`.
 */
public fun ASTNode.findChildByTypeRecursively(elementType: IElementType): ASTNode? =
    recursiveChildren.firstOrNull { it.elementType == elementType }

/**
 * Returns the end offset of the text of this [ASTNode]
 */
public val ASTNode.endOffset: Int
    get(): Int = textRange.endOffset

private val elementTypeCache = hashMapOf<IElementType, PsiElement>()

/**
 * Checks if the [AstNode] extends the [KtAnnotated] interface. Using this function to check the interface is more performant than checking
 * whether `psi is KtAnnotated` as the psi does not need to be derived from [ASTNode].
 */
public val ASTNode.isKtAnnotated: Boolean
    get(): Boolean = psiType { it is KtAnnotated }

private inline fun ASTNode.psiType(predicate: (psiElement: PsiElement) -> Boolean): Boolean = predicate(dummyPsiElement())

/**
 * Checks if the [AstNode] extends the [T] interface which implements [KtElement]. Call this function like:
 * ```
 * astNode.isPsiType<KtAnnotated>()
 * ```
 * Using this function to check the [PsiElement] type of the [ASTNode] is more performant than checking whether `astNode.psi is KtAnnotated`
 * as the psi does not need to be derived from [ASTNode].
 */
public inline fun <reified T : KtElement> ASTNode.isPsiType(): Boolean = this.dummyPsiElement() is T

/**
 * FOR INTERNAL USE ONLY. The returned element is a stub version of a [PsiElement] of the same type as the given [ASTNode]. The returned
 * result may only be used to validate the type of the [PsiElement].
 */
public fun ASTNode.dummyPsiElement(): PsiElement =
    elementTypeCache
        .getOrPut(elementType) {
            // Create a dummy Psi element based on the current node, so that we can store the Psi Type for this ElementType.
            // Creating this cache entry once per elementType is cheaper than accessing the psi for every node.
            when (elementType) {
                is KtFileElementType -> createDummyKtFile()
                is KtKeywordToken -> this as PsiElement
                is KtNodeType -> (elementType as KtNodeType).createPsi(this)
                is KtStubElementType<*, *> -> (elementType as KtStubElementType<*, *>).createPsiFromAst(this)
                is KtToken -> this as PsiElement
                else -> throw NotImplementedError("Cannot create dummy psi for $elementType (${elementType::class})")
            }
        }

private fun createDummyKtFile(): KtFile = KtlintKotlinCompiler.createPsiFileFromText("File.kt", "") as KtFile

/**
 * `true` if [ASTNode] is a declaration
 */
public val ASTNode?.isDeclaration: Boolean
    get(): Boolean = this != null && elementType in KtTokenSets.DECLARATION_TYPES

/**
 * Verified that the [ASTNode], nor any of its parents, is annotated with a suppression of the ktlint rule `max-line-length`.
 */
public fun ASTNode.hasNoMaxLineLengthSuppression(): Boolean =
    !isAnnotatedWithMaxLineLengthSuppression() &&
        parents().none { it.isAnnotatedWithMaxLineLengthSuppression() }

private fun ASTNode.isAnnotatedWithMaxLineLengthSuppression(): Boolean =
    (elementType == ANNOTATED_EXPRESSION && containsMaxLineLengthSuppression()) ||
        findChildByType(MODIFIER_LIST).containsMaxLineLengthSuppression() ||
        (isRoot && findChildByType(FILE_ANNOTATION_LIST)?.containsMaxLineLengthSuppression() == true)

private fun ASTNode?.containsMaxLineLengthSuppression(): Boolean =
    this
        ?.findChildByType(ANNOTATION_ENTRY)
        ?.findChildByType(VALUE_ARGUMENT_LIST)
        ?.children
        ?.any { it.elementType == VALUE_ARGUMENT && it.text == "\"ktlint:standard:max-line-length\"" }
        ?: false
