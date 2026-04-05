/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.jsoncmp.domain.store

import androidx.compose.runtime.Stable
import dev.skymansandy.jsoncmp.domain.line.buildDisplayLines
import dev.skymansandy.jsoncmp.domain.parser.ParseResult
import dev.skymansandy.jsoncmp.domain.parser.parseAndBuildLines
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

/** Default [JsonHolder] implementation backed by coroutine-based async parsing. */
@Stable
internal class JsonHolderImpl(
    initialJson: String = "",
    isEditing: Boolean = false,
) : JsonHolder, AutoCloseable {

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    private val _state = MutableStateFlow(
        JsonHolderState(
            raw = initialJson,
            isEditing = isEditing,
            isParsing = initialJson.trim().isNotEmpty(),
        ),
    )
    override val state: StateFlow<JsonHolderState> = _state.asStateFlow()

    private var parserJob: Job? = null

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

                scheduleParse(raw = action.raw, debounce = _state.value.isEditing)
            }

            is JsonAction.Format -> formatJson(action.compact)

            is JsonAction.SortKeys -> sortKeys(action.ascending)

            is JsonAction.ToggleFold -> {
                _state.update { current ->
                    val isCollapsed = current.foldState[action.foldId] ?: false
                    current.copy(foldState = current.foldState + (action.foldId to !isCollapsed))
                }
            }

            is JsonAction.CollapseAll -> {
                scope.launch {
                    val current = _state.value
                    val collapsedFolds = current.allLines
                        .mapNotNull { it.foldId }
                        .associateWith { true }
                    _state.update { it.copy(foldState = collapsedFolds) }
                }
            }

            is JsonAction.ExpandAll -> {
                _state.update { it.copy(foldState = emptyMap()) }
            }
        }
    }

    // ── Background parsing ──
    private fun scheduleParse(raw: String, debounce: Boolean = false) {
        parserJob?.cancel()
        parserJob = scope.launch {
            if (debounce) delay(PARSE_DEBOUNCE_MS)
            ensureActive()

            val result = parseAndBuildLines(raw)
            _state.update { current ->
                applyParseResult(current, result)
            }
        }
    }

    private fun applyParseResult(current: JsonHolderState, result: ParseResult): JsonHolderState =
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

    private fun formatJson(compact: Boolean) {
        val node = _state.value.parsedJson ?: return
        scope.launch {
            val raw = node.toJsonString(compact = compact)
            val lines = buildDisplayLines(node)
            _state.update { current ->
                current.copy(
                    raw = raw,
                    isCompact = compact,
                    allLines = lines,
                )
            }
        }
    }

    private fun sortKeys(ascending: Boolean) {
        val node = _state.value.parsedJson ?: return
        scope.launch {
            val sorted = node.sortKeys(ascending = ascending, recursive = true)
            val raw = sorted.toJsonString(compact = _state.value.isCompact)
            val lines = buildDisplayLines(sorted)
            _state.update { current ->
                current.copy(
                    raw = raw,
                    parsedJson = sorted,
                    error = null,
                    allLines = lines,
                )
            }
        }
    }

    override fun close() {
        scope.cancel()
    }

    private companion object {
        const val PARSE_DEBOUNCE_MS = 1000L
    }
}
