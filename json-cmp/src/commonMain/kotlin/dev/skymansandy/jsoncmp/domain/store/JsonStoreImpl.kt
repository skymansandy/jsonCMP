package dev.skymansandy.jsoncmp.domain.store

import androidx.compose.runtime.Stable
import dev.skymansandy.jsoncmp.domain.line.JsonLine
import dev.skymansandy.jsoncmp.domain.line.buildDisplayLines
import dev.skymansandy.jsoncmp.domain.model.JsonNode
import dev.skymansandy.jsoncmp.domain.parser.JsonError
import dev.skymansandy.jsoncmp.domain.parser.parseJsonResult
import dev.skymansandy.jsoncmp.domain.serializer.sortKeys
import dev.skymansandy.jsoncmp.domain.serializer.toJsonString
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** Default [JsonStore] implementation backed by coroutine-based async parsing. */
@Stable
internal class JsonStoreImpl(
    initialJson: String = "",
    isEditing: Boolean = false,
) : JsonStore, AutoCloseable {

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    private val _state = MutableStateFlow(
        JsonStoreState(
            raw = initialJson,
            isEditing = isEditing,
            isParsing = initialJson.trim().isNotEmpty(),
        ),
    )
    override val state: StateFlow<JsonStoreState> = _state.asStateFlow()

    private var parseJob: Job? = null

    init {
        if (initialJson.trim().isNotEmpty()) {
            scheduleParse(initialJson)
        }
    }

    // ── Action dispatch ──

    override fun dispatch(action: JsonAction) {
        when (action) {
            is JsonAction.UpdateJson -> {
                _state.update {
                    it.copy(
                        raw = action.raw,
                        isParsing = action.raw.trim().isNotEmpty(),
                    )
                }
                scheduleParse(action.raw, debounce = _state.value.isEditing)
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
        }
    }

    // ── Background parsing ──

    private fun scheduleParse(raw: String, debounce: Boolean = false) {
        parseJob?.cancel()
        parseJob = scope.launch {
            if (debounce) delay(PARSE_DEBOUNCE_MS)
            val result = parseAndBuildLines(raw)
            ensureActive()
            _state.update { current -> applyParseResult(current, result) }
        }
    }

    /** Main-safe: parses raw JSON and builds display lines on [Dispatchers.Default]. */
    private suspend fun parseAndBuildLines(
        raw: String,
    ): ParseResult = withContext(Dispatchers.Default) {
        val trimmed = raw.trim()
        if (trimmed.isEmpty()) return@withContext ParseResult.Empty

        val (node, err) = parseJsonResult(trimmed)
        if (node != null) {
            ParseResult.Success(node, buildDisplayLines(node))
        } else {
            ParseResult.Failure(err)
        }
    }

    private fun applyParseResult(current: JsonStoreState, result: ParseResult): JsonStoreState =
        when (result) {
            is ParseResult.Empty -> current.copy(
                parsedJson = null,
                error = null,
                allLines = emptyList(),
                isParsing = false,
            )

            is ParseResult.Success -> {
                val validIds = result.lines.mapNotNull { it.foldId }.toSet()
                val cleanedFolds = current.foldState.filterKeys { it in validIds }
                val newRaw = if (!current.isEditing) {
                    result.node.toJsonString(compact = current.isCompact)
                } else {
                    current.raw
                }
                current.copy(
                    raw = newRaw,
                    parsedJson = result.node,
                    error = null,
                    allLines = result.lines,
                    foldState = cleanedFolds,
                    isParsing = false,
                )
            }

            is ParseResult.Failure -> current.copy(
                parsedJson = null,
                error = result.error,
                allLines = emptyList(),
                isParsing = false,
            )
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

    /** Outcome of [parseAndBuildLines]. */
    private sealed interface ParseResult {
        data object Empty : ParseResult
        data class Success(
            val node: JsonNode,
            val lines: List<JsonLine>,
        ) : ParseResult
        data class Failure(val error: JsonError?) : ParseResult
    }

    private companion object {
        const val PARSE_DEBOUNCE_MS = 1000L
    }
}
