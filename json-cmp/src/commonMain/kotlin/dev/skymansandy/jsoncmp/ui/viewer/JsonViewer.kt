package dev.skymansandy.jsoncmp.ui.viewer

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import dev.skymansandy.jsoncmp.domain.store.JsonAction
import dev.skymansandy.jsoncmp.domain.store.JsonStoreImpl
import dev.skymansandy.jsoncmp.domain.store.JsonStoreState
import dev.skymansandy.jsoncmp.ui.theme.JsonCmpColors
import dev.skymansandy.jsoncmp.ui.viewer.component.JsonViewerContent
import dev.skymansandy.jsoncmp.ui.viewer.component.JsonViewerEmptyState

/** Routes to [JsonViewerContent] when lines are available, or [JsonViewerEmptyState] otherwise. */
@Composable
internal fun JsonViewer(
    modifier: Modifier = Modifier,
    state: JsonStoreState,
    onAction: (JsonAction) -> Unit,
    searchQuery: String,
    colors: JsonCmpColors,
) {
    if (state.allLines.isEmpty()) {
        JsonViewerEmptyState(modifier, state, searchQuery, colors)
        return
    }

    JsonViewerContent(modifier, state, onAction, searchQuery, colors)
}

// ── Previews ──

private val previewJson = """
{
    "name": "John Doe",
    "age": 30,
    "isActive": true,
    "tags": ["developer", "kotlin"]
}
""".trimIndent()

private val previewColors = JsonCmpColors.Dark

@Preview
@Composable
private fun Preview_JsonViewer() {
    val store = remember { JsonStoreImpl(initialJson = previewJson) }
    val state by store.state.collectAsState()
    MaterialTheme {
        JsonViewer(
            state = state,
            onAction = store::dispatch,
            searchQuery = "",
            colors = previewColors,
        )
    }
}

@Preview
@Composable
private fun Preview_JsonViewerWithSearch() {
    val store = remember { JsonStoreImpl(initialJson = previewJson) }
    val state by store.state.collectAsState()
    MaterialTheme {
        JsonViewer(
            state = state,
            onAction = store::dispatch,
            searchQuery = "John",
            colors = previewColors,
        )
    }
}
