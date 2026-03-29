package dev.skymansandy.jsoncmp.ui.viewer

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import dev.skymansandy.jsoncmp.ExperimentalJsonCmpApi
import dev.skymansandy.jsoncmp.ui.theme.JsonTheme

/**
 * Read-only JSON viewer composable with virtualized rendering.
 *
 * Observe state via [JsonViewerState.json], [JsonViewerState.parsedJson],
 * and [JsonViewerState.error] — no callbacks needed.
 *
 * @param state state holder created via [rememberJsonViewerState].
 * @param modifier layout modifier.
 * @param searchQuery optional search query to highlight matches.
 * @param theme visual theme for the viewer.
 */
@ExperimentalJsonCmpApi
@Composable
fun JsonViewerCMP(
    state: JsonViewerState,
    modifier: Modifier = Modifier,
    searchQuery: String = "",
    theme: JsonTheme = JsonTheme.Dark,
) {
    val storeState by state.store.state.collectAsState()

    JsonViewer(
        modifier = modifier,
        state = storeState,
        onAction = state.store::dispatch,
        searchQuery = searchQuery,
        colors = theme.colors,
    )
}
