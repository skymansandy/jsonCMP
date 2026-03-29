package dev.skymansandy.jsoncmp

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import dev.skymansandy.jsoncmp.component.viewer.JsonViewer
import dev.skymansandy.jsoncmp.config.JsonTheme
import dev.skymansandy.jsoncmp.config.rememberJsonStore
import dev.skymansandy.jsoncmp.helper.annotation.ExperimentalJsonCmpApi
import dev.skymansandy.jsoncmp.helper.parser.JsonError
import dev.skymansandy.jsoncmp.model.JsonNode

/**
 * Read-only JSON viewer composable.
 *
 * No size restriction for valid JSON (virtualized rendering).
 * Invalid JSON falls back to plain text with a 100 KB preview limit.
 *
 * @param json The JSON string to display.
 * @param modifier Layout modifier.
 * @param searchQuery Optional search query to highlight matches.
 * @param theme Visual theme for the viewer.
 * @param onJsonParsed Callback invoked when parsing completes.
 */
@ExperimentalJsonCmpApi
@Composable
fun JsonViewerCMP(
    modifier: Modifier = Modifier,
    json: String,
    searchQuery: String = "",
    theme: JsonTheme = JsonTheme.Dark,
    onJsonParsed: (
        parsed: JsonNode?,
        error: JsonError?,
    ) -> Unit = { _, _ -> },
) {
    val store = rememberJsonStore(
        initialJson = json,
        isEditing = false,
    )
    val state by store.state.collectAsState()

    LaunchedEffect(state.parsedJson, state.error) {
        onJsonParsed(state.parsedJson, state.error)
    }

    JsonViewer(
        modifier = modifier,
        state = state,
        onAction = store::dispatch,
        searchQuery = searchQuery,
        colors = theme.colors,
    )
}
