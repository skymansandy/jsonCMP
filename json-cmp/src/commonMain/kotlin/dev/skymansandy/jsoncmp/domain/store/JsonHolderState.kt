package dev.skymansandy.jsoncmp.domain.store

import dev.skymansandy.jsoncmp.domain.line.JsonLine
import dev.skymansandy.jsoncmp.domain.model.JsonNode
import dev.skymansandy.jsoncmp.domain.parser.JsonError

/** Immutable snapshot of the store's state — raw text, parsed tree, lines, and fold state. */
@Suppress("LongParameterList")
internal class JsonHolderState(
    val raw: String = "",
    val parsedJson: JsonNode? = null,
    val error: JsonError? = null,
    val isParsing: Boolean = false,
    val isCompact: Boolean = false,
    val isEditing: Boolean = false,
    val allLines: List<JsonLine> = emptyList(),
    val foldState: Map<Int, Boolean> = emptyMap(),
) {
    val visibleLines: List<JsonLine> by lazy {
        buildVisibleLines(allLines, foldState)
    }

    @Suppress("LongParameterList")
    fun copy(
        raw: String = this.raw,
        parsedJson: JsonNode? = this.parsedJson,
        error: JsonError? = this.error,
        isParsing: Boolean = this.isParsing,
        isCompact: Boolean = this.isCompact,
        isEditing: Boolean = this.isEditing,
        allLines: List<JsonLine> = this.allLines,
        foldState: Map<Int, Boolean> = this.foldState,
    ): JsonHolderState = JsonHolderState(
        raw = raw,
        parsedJson = parsedJson,
        error = error,
        isParsing = isParsing,
        isCompact = isCompact,
        isEditing = isEditing,
        allLines = allLines,
        foldState = foldState,
    )

    fun computeFoldedContent(line: JsonLine): String {
        if (line.foldId == null || line.childEndIndex < 0) return ""
        val startIdx = allLines.indexOf(line)
        if (startIdx < 0) return ""
        val endIdx = line.childEndIndex.coerceAtMost(allLines.size)
        return allLines.subList(startIdx + 1, endIdx)
            .joinToString(" ") { l -> l.parts.joinToString("") { it.text }.trim() }
    }

    fun hasFoldedMatch(line: JsonLine, searchQuery: String): Boolean {
        if (line.foldId == null || line.childEndIndex < 0 || searchQuery.isBlank()) return false
        val startIdx = allLines.indexOf(line)
        if (startIdx < 0) return false
        val endIdx = line.childEndIndex.coerceAtMost(allLines.size)
        val queryLower = searchQuery.lowercase()
        for (i in (startIdx + 1) until endIdx) {
            val lineText = allLines[i].parts.joinToString("") { it.text }
            if (lineText.lowercase().contains(queryLower)) return true
        }
        return false
    }

    fun countFoldedMatches(line: JsonLine, searchQuery: String): Int {
        if (line.foldId == null || line.childEndIndex < 0 || searchQuery.isBlank()) return 0
        if (foldState[line.foldId] != true) return 0
        val startIdx = allLines.indexOf(line)
        if (startIdx < 0) return 0
        val endIdx = line.childEndIndex.coerceAtMost(allLines.size)
        val queryLower = searchQuery.lowercase()
        var count = 0
        for (i in (startIdx + 1) until endIdx) {
            val lineText = allLines[i].parts.joinToString("") { it.text }.lowercase()
            var idx = lineText.indexOf(queryLower)
            while (idx >= 0) {
                count++
                idx = lineText.indexOf(queryLower, idx + queryLower.length)
            }
        }
        return count
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is JsonHolderState) return false
        return raw == other.raw &&
            parsedJson == other.parsedJson &&
            error == other.error &&
            isParsing == other.isParsing &&
            isCompact == other.isCompact &&
            isEditing == other.isEditing &&
            allLines == other.allLines &&
            foldState == other.foldState
    }

    override fun hashCode(): Int {
        var result = raw.hashCode()
        result = 31 * result + (parsedJson?.hashCode() ?: 0)
        result = 31 * result + (error?.hashCode() ?: 0)
        result = 31 * result + isParsing.hashCode()
        result = 31 * result + isCompact.hashCode()
        result = 31 * result + isEditing.hashCode()
        result = 31 * result + allLines.hashCode()
        result = 31 * result + foldState.hashCode()
        return result
    }
}

/** Filters [allLines] to only those visible after applying fold state, using O(1) fold-skipping. */
internal fun buildVisibleLines(
    allLines: List<JsonLine>,
    foldState: Map<Int, Boolean>,
): List<JsonLine> {
    if (allLines.isEmpty()) return emptyList()
    val result = ArrayList<JsonLine>(allLines.size / 2)
    var i = 0
    while (i < allLines.size) {
        val line = allLines[i]
        result.add(line)
        if (line.foldId != null && foldState[line.foldId] == true && line.childEndIndex > 0) {
            i = line.childEndIndex
        } else {
            i++
        }
    }
    return result
}
