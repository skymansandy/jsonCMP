package dev.skymansandy.jsoncmp.config

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import dev.skymansandy.jsoncmp.helper.lines.buildDisplayLines
import dev.skymansandy.jsoncmp.helper.parser.JsonError
import dev.skymansandy.jsoncmp.helper.parser.parseJsonResult
import dev.skymansandy.jsoncmp.helper.serializer.sortKeys
import dev.skymansandy.jsoncmp.helper.serializer.toJsonString
import dev.skymansandy.jsoncmp.model.JsonLine
import dev.skymansandy.jsoncmp.model.JsonNode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed class JsonAction {
    data class SetRaw(val raw: String) : JsonAction()
    data class Format(val compact: Boolean) : JsonAction()
    data class SortKeys(val ascending: Boolean) : JsonAction()
    data class ToggleFold(val foldId: Int) : JsonAction()
    data class SetEditing(val editing: Boolean) : JsonAction()
    data object CollapseAll : JsonAction()
    data object ExpandAll : JsonAction()
    data object Reset : JsonAction()
}

@Suppress("LongParameterList")
class JsonStoreState internal constructor(
    val raw: String = "",
    val parsedJson: JsonNode? = null,
    val error: JsonError? = null,
    val isParsing: Boolean = false,
    val isCompact: Boolean = false,
    val isEditing: Boolean = false,
    internal val allLines: List<JsonLine> = emptyList(),
    val foldState: Map<Int, Boolean> = emptyMap(),
) {
    internal val visibleLines: List<JsonLine> by lazy {
        buildVisibleLines(allLines, foldState)
    }

    @Suppress("LongParameterList")
    internal fun copy(
        raw: String = this.raw,
        parsedJson: JsonNode? = this.parsedJson,
        error: JsonError? = this.error,
        isParsing: Boolean = this.isParsing,
        isCompact: Boolean = this.isCompact,
        isEditing: Boolean = this.isEditing,
        allLines: List<JsonLine> = this.allLines,
        foldState: Map<Int, Boolean> = this.foldState,
    ): JsonStoreState = JsonStoreState(
        raw = raw,
        parsedJson = parsedJson,
        error = error,
        isParsing = isParsing,
        isCompact = isCompact,
        isEditing = isEditing,
        allLines = allLines,
        foldState = foldState,
    )

    internal fun computeFoldedContent(line: JsonLine): String {
        if (line.foldId == null || line.childEndIndex < 0) return ""
        val startIdx = allLines.indexOf(line)
        if (startIdx < 0) return ""
        val endIdx = line.childEndIndex.coerceAtMost(allLines.size)
        return allLines.subList(startIdx + 1, endIdx)
            .joinToString(" ") { l -> l.parts.joinToString("") { it.text }.trim() }
    }

    internal fun hasFoldedMatch(line: JsonLine, searchQuery: String): Boolean {
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

    internal fun countFoldedMatches(line: JsonLine, searchQuery: String): Int {
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
        if (other !is JsonStoreState) return false
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

@Stable
class JsonStore internal constructor(
    initialJson: String = "",
    isEditing: Boolean = false,
) : AutoCloseable {

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    private val _state = MutableStateFlow(
        JsonStoreState(
            raw = initialJson,
            isEditing = isEditing,
            isParsing = initialJson.trim().isNotEmpty(),
        ),
    )
    val state: StateFlow<JsonStoreState> = _state.asStateFlow()

    private var parseJob: Job? = null

    init {
        if (initialJson.trim().isNotEmpty()) {
            scheduleParse(initialJson)
        }
    }

    fun dispatch(action: JsonAction) {
        when (action) {
            is JsonAction.SetRaw -> {
                _state.update { it.copy(raw = action.raw) }
                scheduleParse(action.raw)
            }

            is JsonAction.Format -> reduceFormat(action.compact)
            is JsonAction.SortKeys -> reduceSortKeys(action.ascending)

            is JsonAction.ToggleFold -> {
                _state.update { current ->
                    val isCollapsed = current.foldState[action.foldId] ?: false
                    current.copy(foldState = current.foldState + (action.foldId to !isCollapsed))
                }
            }

            is JsonAction.CollapseAll -> {
                _state.update { current ->
                    val allFoldIds = current.allLines.mapNotNull { it.foldId }
                    current.copy(foldState = allFoldIds.associateWith { true })
                }
            }

            is JsonAction.ExpandAll -> {
                _state.update { it.copy(foldState = emptyMap()) }
            }

            is JsonAction.SetEditing -> {
                _state.update { it.copy(isEditing = action.editing) }
            }

            is JsonAction.Reset -> {
                parseJob?.cancel()
                _state.value = JsonStoreState()
            }
        }
    }

    private fun scheduleParse(raw: String) {
        parseJob?.cancel()
        parseJob = scope.launch {
            val trimmed = raw.trim()
            if (trimmed.isEmpty()) {
                ensureActive()
                _state.update {
                    it.copy(
                        parsedJson = null,
                        error = null,
                        allLines = emptyList(),
                        isParsing = false,
                    )
                }
                return@launch
            }

            val (node, err) = parseJsonResult(trimmed)
            ensureActive()

            _state.update { current ->
                if (node != null) {
                    val lines = buildDisplayLines(node)
                    val validIds = lines.mapNotNull { it.foldId }.toSet()
                    val cleanedFolds = current.foldState.filterKeys { it in validIds }
                    val newRaw = if (!current.isEditing) {
                        node.toJsonString(compact = current.isCompact)
                    } else {
                        current.raw
                    }
                    current.copy(
                        raw = newRaw,
                        parsedJson = node,
                        error = null,
                        allLines = lines,
                        foldState = cleanedFolds,
                        isParsing = false,
                    )
                } else {
                    current.copy(
                        parsedJson = null,
                        error = err,
                        allLines = emptyList(),
                        isParsing = false,
                    )
                }
            }
        }
    }

    private fun reduceFormat(compact: Boolean) {
        _state.update { current ->
            val node = current.parsedJson ?: return@update current
            current.copy(
                raw = node.toJsonString(compact = compact),
                isCompact = compact,
            )
        }
    }

    private fun reduceSortKeys(ascending: Boolean) {
        _state.update { current ->
            val node = current.parsedJson ?: return@update current
            val sorted = node.sortKeys(ascending = ascending, recursive = true)
            val raw = sorted.toJsonString(compact = current.isCompact)
            val lines = buildDisplayLines(sorted)
            current.copy(
                raw = raw,
                parsedJson = sorted,
                error = null,
                allLines = lines,
            )
        }
    }

    override fun close() {
        scope.cancel()
    }
}

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

@Composable
fun rememberJsonStore(
    initialJson: String,
    isEditing: Boolean = false,
): JsonStore {
    val store = remember(initialJson) {
        JsonStore(initialJson = initialJson, isEditing = isEditing)
    }
    DisposableEffect(store) {
        onDispose { store.close() }
    }
    return store
}
