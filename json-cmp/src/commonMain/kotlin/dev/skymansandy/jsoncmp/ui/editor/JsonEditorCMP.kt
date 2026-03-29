package dev.skymansandy.jsoncmp.ui.editor

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import dev.skymansandy.jsoncmp.ExperimentalJsonCmpApi
import dev.skymansandy.jsoncmp.domain.model.JsonNode
import dev.skymansandy.jsoncmp.domain.parser.JsonError
import dev.skymansandy.jsoncmp.domain.store.JsonAction
import dev.skymansandy.jsoncmp.rememberJsonStore
import dev.skymansandy.jsoncmp.ui.theme.JsonTheme

/**
 * JSON editor composable with a 250 KB size limit.
 *
 * Characters beyond 250 KB are silently dropped on input.
 *
 * @param json The initial JSON string to edit.
 * @param modifier Layout modifier.
 * @param searchQuery Optional search query to highlight matches.
 * @param theme Visual theme for the editor.
 * @param onJsonChange Callback invoked when the JSON text or parse result changes.
 */
@ExperimentalJsonCmpApi
@Composable
fun JsonEditorCMP(
    modifier: Modifier = Modifier,
    json: String,
    searchQuery: String = "",
    theme: JsonTheme = JsonTheme.Dark,
    onJsonChange: (
        json: String,
        parsed: JsonNode?,
        error: JsonError?,
    ) -> Unit = { _, _, _ -> },
) {
    val truncatedJson = if (json.length > EDITOR_MAX_SIZE) json.take(EDITOR_MAX_SIZE) else json

    val store = rememberJsonStore(
        initialJson = truncatedJson,
        isEditing = true,
    )
    val state by store.state.collectAsState()

    LaunchedEffect(state.raw, state.parsedJson, state.error) {
        onJsonChange(state.raw, state.parsedJson, state.error)
    }

    JsonEditor(
        modifier = modifier,
        state = state,
        onAction = { action ->
            when (action) {
                is JsonAction.UpdateJson -> {
                    val clamped = if (action.raw.length > EDITOR_MAX_SIZE) {
                        action.raw.take(EDITOR_MAX_SIZE)
                    } else {
                        action.raw
                    }
                    store.dispatch(JsonAction.UpdateJson(clamped))
                }
                else -> store.dispatch(action)
            }
        },
        searchQuery = searchQuery,
        colors = theme.colors,
    )
}

private const val EDITOR_MAX_SIZE = 50 * 1024
