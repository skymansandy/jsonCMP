package dev.skymansandy.jsoncmp.ui.viewer

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import dev.skymansandy.jsoncmp.domain.store.JsonAction
import dev.skymansandy.jsoncmp.domain.store.JsonHolderImpl
import dev.skymansandy.jsoncmp.domain.store.JsonHolderState
import dev.skymansandy.jsoncmp.helper.mocks.previewJson
import dev.skymansandy.jsoncmp.theme.JsonCmpColors
import dev.skymansandy.jsoncmp.theme.LocalJsonCmpColors
import dev.skymansandy.jsoncmp.ui.viewer.component.JsonViewerContent
import dev.skymansandy.jsoncmp.ui.viewer.component.JsonViewerEmptyState

/** Routes to [JsonViewerContent] when lines are available, or [JsonViewerEmptyState] otherwise. */
@Composable
internal fun JsonViewer(
    modifier: Modifier = Modifier,
    state: JsonHolderState,
    onAction: (JsonAction) -> Unit,
    searchQuery: String,
) {
    if (state.allLines.isEmpty()) {
        JsonViewerEmptyState(
            modifier = modifier,
            state = state,
            searchQuery = searchQuery,
        )

        return
    }

    JsonViewerContent(
        modifier = modifier,
        state = state,
        onAction = onAction,
        searchQuery = searchQuery,
    )
}

// ── Previews ──
@Preview
@Composable
private fun Preview_JsonViewer() {
    val store = remember { JsonHolderImpl(initialJson = previewJson) }
    val state by store.state.collectAsState()
    MaterialTheme {
        CompositionLocalProvider(LocalJsonCmpColors provides JsonCmpColors.Dark) {
            JsonViewer(
                state = state,
                onAction = store::dispatch,
                searchQuery = "",
            )
        }
    }
}

@Preview
@Composable
private fun Preview_JsonViewerWithSearch() {
    val store = remember { JsonHolderImpl(initialJson = previewJson) }
    val state by store.state.collectAsState()
    MaterialTheme {
        CompositionLocalProvider(LocalJsonCmpColors provides JsonCmpColors.Dark) {
            JsonViewer(
                state = state,
                onAction = store::dispatch,
                searchQuery = "John",
            )
        }
    }
}
