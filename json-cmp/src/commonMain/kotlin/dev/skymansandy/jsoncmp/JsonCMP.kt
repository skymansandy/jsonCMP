package dev.skymansandy.jsoncmp

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import dev.skymansandy.jsoncmp.component.editor.CodeEditor
import dev.skymansandy.jsoncmp.component.editor.EditorToolbar
import dev.skymansandy.jsoncmp.component.editor.ErrorBanner
import dev.skymansandy.jsoncmp.component.viewer.JsonViewer
import dev.skymansandy.jsoncmp.config.JsonEditorState
import dev.skymansandy.jsoncmp.config.ThemeOption
import dev.skymansandy.jsoncmp.helper.parser.JsonError
import dev.skymansandy.jsoncmp.model.JsonNode

@Composable
fun JsonCMP(
    modifier: Modifier = Modifier,
    state: JsonEditorState,
    searchQuery: String = "",
    theme: ThemeOption = ThemeOption.Dark,
    onJsonChange: (
        json: String,
        parsed: JsonNode?,
        error: JsonError?,
    ) -> Unit = { _, _, _ -> },
) {

    LaunchedEffect(state.rawJson, state.parsedJson, state.error) {
        onJsonChange(state.rawJson, state.parsedJson, state.error)
    }

    Column(
        modifier = modifier,
    ) {
        if (state.isEditing) {
            EditorToolbar(
                state = state,
                colors = theme.colors,
            )

            ErrorBanner(
                error = state.error,
                colors = theme.colors,
            )

            CodeEditor(
                state = state,
                searchQuery = searchQuery,
                colors = theme.colors,
            )
        } else {
            JsonViewer(
                state = state,
                searchQuery = searchQuery,
                colors = theme.colors,
            )
        }
    }
}
