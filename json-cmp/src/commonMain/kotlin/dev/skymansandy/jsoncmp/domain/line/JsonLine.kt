/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.jsoncmp.domain.line

import androidx.compose.runtime.Immutable
import dev.skymansandy.jsoncmp.domain.model.FoldType
import dev.skymansandy.jsoncmp.domain.model.JsonPath

/**
 * A single rendered line in the JSON viewer/editor, with parts, folding info, and depth.
 *
 * @param lineNumber 1-based display line number.
 * @param depth nesting depth (0 = root), used for indentation.
 * @param parts typed text segments that make up this line's content.
 * @param foldId unique fold identifier if this line opens a foldable block, null otherwise.
 * @param foldType whether the foldable block is an object or array, null for non-foldable lines.
 * @param parentFoldIds fold IDs of all ancestor foldable blocks, innermost last.
 * @param foldChildCount number of direct children inside the fold (fields or elements).
 * @param path path from root to the node this line represents. Empty for closing brackets.
 * @param isClosingBracket true when this line is a closing bracket, not a value node.
 * @param childEndIndex index in `allLines` of the first line AFTER this fold's children,
 *   enabling O(1) fold-skipping: when a fold is collapsed the visible-line builder jumps
 *   straight to `allLines[childEndIndex]` instead of iterating through every hidden child.
 *   Set to -1 for non-foldable lines (values, closing brackets).
 */
@Immutable
internal data class JsonLine(
    val lineNumber: Int,
    val depth: Int,
    val parts: List<JsonPart>,
    val foldId: Int?,
    val foldType: FoldType?,
    val parentFoldIds: List<Int>,
    val foldChildCount: Int = 0,
    val path: JsonPath = emptyList(),
    val isClosingBracket: Boolean = false,
    val childEndIndex: Int = -1,
) {
    /** Cached concatenation of all parts' text — avoids repeated joinToString in hot paths. */
    val text: String = parts.joinToString("") { it.text }
}
