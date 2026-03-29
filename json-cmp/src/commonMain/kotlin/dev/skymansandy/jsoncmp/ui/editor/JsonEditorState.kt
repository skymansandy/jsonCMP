/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.jsoncmp.ui.editor

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
import dev.skymansandy.jsoncmp.domain.store.JsonHolderImpl

/**
 * Observable state for [JsonEditorCMP].
 *
 * Read [json], [parsedJson], and [error] in composition or via `snapshotFlow`
 * to react to changes — no callbacks needed.
 */
@Stable
class JsonEditorState internal constructor(
    internal val store: JsonHolderImpl,
) {
    /** Current raw JSON text in the editor. */
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
 * Creates and remembers a [JsonEditorState].
 *
 * [initialJson] is used only once to seed the editor. Subsequent changes to this
 * parameter are ignored — the editor owns its own text state internally.
 * To load entirely new content, wrap the composable in a `key(documentId)` block.
 */
@ExperimentalJsonCmpApi
@Composable
fun rememberJsonEditorState(initialJson: String = ""): JsonEditorState {
    val clampedJson = if (initialJson.length > EDITOR_MAX_SIZE) {
        initialJson.take(EDITOR_MAX_SIZE)
    } else {
        initialJson
    }
    val store = retain {
        JsonHolderImpl(
            initialJson = clampedJson,
            isEditing = true,
        )
    }

    RetainedEffect(Unit) {
        onRetire {
            store.close()
        }
    }

    val state = remember { JsonEditorState(store) }

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
