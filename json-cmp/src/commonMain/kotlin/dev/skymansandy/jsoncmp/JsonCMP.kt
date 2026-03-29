package dev.skymansandy.jsoncmp

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import dev.skymansandy.jsoncmp.component.editor.JsonEditor
import dev.skymansandy.jsoncmp.component.viewer.JsonViewer
import dev.skymansandy.jsoncmp.config.JsonStore
import dev.skymansandy.jsoncmp.config.JsonTheme
import dev.skymansandy.jsoncmp.helper.annotation.ExperimentalJsonCmpApi
import dev.skymansandy.jsoncmp.helper.parser.JsonError
import dev.skymansandy.jsoncmp.model.JsonNode

@ExperimentalJsonCmpApi
@Composable
fun JsonCMP(
    modifier: Modifier = Modifier,
    store: JsonStore,
    searchQuery: String = "",
    theme: JsonTheme = JsonTheme.Dark,
    onJsonChange: (
        json: String,
        parsed: JsonNode?,
        error: JsonError?,
    ) -> Unit = { _, _, _ -> },
) {
    val state by store.state.collectAsState()

    LaunchedEffect(state.raw, state.parsedJson, state.error) {
        onJsonChange(state.raw, state.parsedJson, state.error)
    }

    if (state.isEditing) {
        JsonEditor(
            modifier = modifier,
            state = state,
            onAction = store::dispatch,
            searchQuery = searchQuery,
            colors = theme.colors,
        )
    } else {
        JsonViewer(
            modifier = modifier,
            state = state,
            onAction = store::dispatch,
            searchQuery = searchQuery,
            colors = theme.colors,
        )
    }
}
