package dev.skymansandy.jsoncmp.ui.editor

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import dev.skymansandy.jsoncmp.domain.ExperimentalJsonCmpApi
import dev.skymansandy.jsoncmp.domain.store.JsonAction
import dev.skymansandy.jsoncmp.theme.JsonTheme
import dev.skymansandy.jsoncmp.theme.LocalJsonCmpColors

/**
 * JSON editor composable with a 50 KB size limit.
 *
 * Observe changes via [JsonEditorState.json], [JsonEditorState.parsedJson],
 * and [JsonEditorState.error] — no callbacks needed.
 *
 * @param state state holder created via [rememberJsonEditorState].
 * @param modifier layout modifier.
 * @param searchQuery optional search query to highlight matches.
 * @param theme visual theme for the editor.
 */
@ExperimentalJsonCmpApi
@Composable
fun JsonEditorCMP(
    modifier: Modifier = Modifier,
    state: JsonEditorState,
    searchQuery: String = "",
    theme: JsonTheme = JsonTheme.Dark,
) {
    val storeState by state.store.state.collectAsState()

    CompositionLocalProvider(LocalJsonCmpColors provides theme.colors) {
        JsonEditor(
            modifier = modifier,
            state = storeState,
            searchQuery = searchQuery,
            onAction = { action ->
                when (action) {
                    is JsonAction.UpdateJson -> {
                        val clamped = if (action.raw.length > EDITOR_MAX_SIZE) {
                            action.raw.take(EDITOR_MAX_SIZE)
                        } else {
                            action.raw
                        }
                        state.store.dispatch(JsonAction.UpdateJson(clamped))
                    }
                    else -> state.store.dispatch(action)
                }
            },
        )
    }
}

private const val EDITOR_MAX_SIZE = 50 * 1024
