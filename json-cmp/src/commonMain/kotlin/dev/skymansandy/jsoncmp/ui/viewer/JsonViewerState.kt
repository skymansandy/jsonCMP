/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.jsoncmp.ui.viewer

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.retain.RetainedEffect
import androidx.compose.runtime.retain.retain
import androidx.compose.runtime.setValue
import dev.skymansandy.jsoncmp.domain.ExperimentalJsonCmpApi
import dev.skymansandy.jsoncmp.domain.model.JsonNode
import dev.skymansandy.jsoncmp.domain.parser.JsonError
import dev.skymansandy.jsoncmp.domain.store.JsonAction
import dev.skymansandy.jsoncmp.domain.store.JsonHolderImpl

/**
 * Observable state for [JsonViewerCMP].
 *
 * Read [json], [parsedJson], and [error] in composition or via `snapshotFlow`
 * to react to changes — no callbacks needed.
 */
@Stable
class JsonViewerState internal constructor(
    internal val store: JsonHolderImpl,
) {
    /** Current raw JSON text being displayed. */
    var json: String by mutableStateOf(store.state.value.raw)
        internal set

    /** Last successfully parsed tree, or null if parsing failed or is pending. */
    var parsedJson: JsonNode? by mutableStateOf(store.state.value.parsedJson)
        internal set

    /** Last parse error, or null if JSON is valid or parsing is pending. */
    var error: JsonError? by mutableStateOf(store.state.value.error)
        internal set
}

/**
 * Creates and remembers a [JsonViewerState].
 *
 * Unlike the editor, the viewer responds to changes in [json] — passing a new value
 * triggers a re-parse and updates the display.
 */
@ExperimentalJsonCmpApi
@Composable
fun rememberJsonViewerState(json: String = ""): JsonViewerState {
    val store = retain {
        JsonHolderImpl(initialJson = json, isEditing = false)
    }

    RetainedEffect(Unit) {
        onRetire {
            store.close()
        }
    }

    val state = remember { JsonViewerState(store) }

    // Re-parse when the caller provides new JSON
    LaunchedEffect(json) {
        if (json.isNotBlank() && json != store.state.value.raw) {
            store.dispatch(JsonAction.UpdateJson(json))
        }
    }

    // Sync internal store state → public observable properties
    LaunchedEffect(store) {
        store.state.collect { storeState ->
            state.json = storeState.raw
            state.parsedJson = storeState.parsedJson
            state.error = storeState.error
        }
    }

    return state
}
