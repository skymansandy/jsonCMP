package dev.skymansandy.jsoncmp.config

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateMap
import dev.skymansandy.jsoncmp.helper.lines.buildDisplayLines
import dev.skymansandy.jsoncmp.helper.parser.JsonError
import dev.skymansandy.jsoncmp.helper.parser.parseJsonResult
import dev.skymansandy.jsoncmp.helper.serializer.sortKeys
import dev.skymansandy.jsoncmp.helper.serializer.toJsonString
import dev.skymansandy.jsoncmp.model.JsonLine
import dev.skymansandy.jsoncmp.model.JsonNode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Stable
class JsonEditorState(initialJson: String, isEditing: Boolean) {

    var rawJson: String by mutableStateOf(initialJson)
        internal set

    var parsedJson: JsonNode? by mutableStateOf(null)
        private set

    var error: JsonError? by mutableStateOf(null)
        private set

    var isCompact: Boolean by mutableStateOf(false)
        private set

    var isEditing: Boolean by mutableStateOf(isEditing)

    internal val foldState: SnapshotStateMap<Int, Boolean> = mutableStateMapOf()

    internal var allLines: List<JsonLine> by mutableStateOf(emptyList())
        private set

    /** Visible lines computed via index-jump: O(visible) instead of O(all). */
    internal val visibleLines: List<JsonLine> by derivedStateOf {
        buildVisibleLines(allLines, foldState)
    }

    fun collapseAll() {
        allLines.forEach { line ->
            line.foldId?.let { foldState[it] = true }
        }
    }

    fun expandAll() {
        foldState.clear()
    }

    fun format(compact: Boolean) {
        val node = parsedJson ?: return

        isCompact = compact
        rawJson = node.toJsonString(compact = compact)
    }

    fun sortKeys(ascending: Boolean) {
        val node = parsedJson ?: return

        val sorted = node.sortKeys(ascending = ascending, recursive = true)
        parsedJson = sorted
        error = null
        rawJson = sorted.toJsonString(compact = isCompact)
    }

    /** Lazily computes the folded content text for a given foldable line. */
    internal fun computeFoldedContent(line: JsonLine): String {
        if (line.foldId == null || line.childEndIndex < 0) return ""
        val lines = allLines
        val startIdx = lines.indexOf(line)
        if (startIdx < 0) return ""
        val endIdx = line.childEndIndex.coerceAtMost(lines.size)
        return lines.subList(startIdx + 1, endIdx)
            .joinToString(" ") { l -> l.parts.joinToString("") { it.text }.trim() }
    }

    /** Checks whether any line inside a fold matches the search query. */
    internal fun hasFoldedMatch(line: JsonLine, searchQuery: String): Boolean {
        if (line.foldId == null || line.childEndIndex < 0 || searchQuery.isBlank()) return false
        val lines = allLines
        val startIdx = lines.indexOf(line)
        if (startIdx < 0) return false
        val endIdx = line.childEndIndex.coerceAtMost(lines.size)
        val queryLower = searchQuery.lowercase()
        for (i in (startIdx + 1) until endIdx) {
            val lineText = lines[i].parts.joinToString("") { it.text }
            if (lineText.lowercase().contains(queryLower)) return true
        }
        return false
    }

    suspend fun parseJsonElement(json: String) = withContext(Dispatchers.Default) {
        val trimmed = json.trim()
        if (trimmed.isEmpty()) {
            parsedJson = null
            error = null
            allLines = emptyList()
            return@withContext
        }

        val (node, err) = parseJsonResult(trimmed)
        parsedJson = node
        error = err
        if (node != null) {
            if (!isEditing) {
                val normalized = node.toJsonString(compact = isCompact)
                if (normalized != rawJson) {
                    rawJson = normalized
                }
            }
            allLines = buildDisplayLines(node)
            val validIds = allLines.mapNotNull { it.foldId }.toSet()
            foldState.keys.removeAll { it !in validIds }
        } else {
            allLines = emptyList()
        }
    }

    companion object {
        /** Builds visible lines using index-jump to skip folded sections in O(visible). */
        private fun buildVisibleLines(
            allLines: List<JsonLine>,
            foldState: SnapshotStateMap<Int, Boolean>,
        ): List<JsonLine> {
            if (allLines.isEmpty()) return emptyList()
            val result = ArrayList<JsonLine>(allLines.size / 2)
            var i = 0
            while (i < allLines.size) {
                val line = allLines[i]
                result.add(line)
                // If this line is a folded header, jump past all its children
                if (line.foldId != null && foldState[line.foldId] == true && line.childEndIndex > 0) {
                    i = line.childEndIndex
                } else {
                    i++
                }
            }
            return result
        }
    }
}

@Composable
fun rememberJsonEditorState(
    initialJson: String,
    isEditing: Boolean = false,
): JsonEditorState {
    return remember(initialJson) {
        JsonEditorState(
            initialJson = initialJson,
            isEditing = isEditing,
        )
    }
}
