package dev.skymansandy.jsoncmp.model

internal data class JsonLine(
    val lineNumber: Int,
    val depth: Int,
    val parts: List<JsonPart>,
    val foldId: Int?,
    val foldType: FoldType?,
    val parentFoldIds: List<Int>,
    val foldChildCount: Int = 0,
    /** Path from root to the node this line represents. Empty for closing brackets. */
    val path: JsonPath = emptyList(),
    /** Whether this line is a closing bracket (not a value node). */
    val isClosingBracket: Boolean = false,
    /** Index in allLines of the first line AFTER this fold's children.
     *  -1 for non-foldable lines. Used for O(1) fold-skipping. */
    val childEndIndex: Int = -1,
)
