package dev.skymansandy.jsoncmp.component.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.skymansandy.jsoncmp.component.common.highlightJson
import dev.skymansandy.jsoncmp.config.JsonEditorState
import dev.skymansandy.jsoncmp.helper.constants.colors.JsonCmpColors
import dev.skymansandy.jsoncmp.helper.constants.typography.monoStyle

@Composable
internal fun JsonEditor(
    modifier: Modifier = Modifier,
    state: JsonEditorState,
    searchQuery: String,
    colors: JsonCmpColors,
) {
    var textFieldValue by remember { mutableStateOf(TextFieldValue(state.rawJson)) }
    var lastSyncedRaw by remember { mutableStateOf(state.rawJson) }

    if (state.rawJson != lastSyncedRaw) {
        textFieldValue = TextFieldValue(state.rawJson)
        lastSyncedRaw = state.rawJson
    }

    val horizontalScrollState = rememberScrollState()
    val lineCount = remember(textFieldValue.text) { textFieldValue.text.count { it == '\n' } + 1 }
    var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }

    val focusRequester = remember { FocusRequester() }

    val highlighted: AnnotatedString = remember(textFieldValue.text, searchQuery, colors) {
        highlightJson(
            text = textFieldValue.text,
            searchQuery = searchQuery,
            colors = colors,
        )
    }

    BoxWithConstraints(
        modifier = modifier
            .background(colors.background)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
            ) {
                focusRequester.requestFocus()
            },
    ) {
        val hasBoundedHeight = constraints.hasBoundedHeight
        val gutterMinHeight = if (hasBoundedHeight) maxHeight else 200.dp
        val scrollModifier = if (hasBoundedHeight) {
            Modifier.verticalScroll(rememberScrollState())
        } else {
            Modifier
        }

        Row(
            modifier = Modifier
                .defaultMinSize(minHeight = gutterMinHeight)
                .then(scrollModifier),
        ) {
            // Line number gutter — positioned using actual text layout line offsets
            LineGutterEditMode(
                lineCount = lineCount,
                textLayoutResult = textLayoutResult,
                colors = colors,
                gutterMinHeight = gutterMinHeight,
            )

            // JSON editor
            BasicTextField(
                value = textFieldValue.copy(annotatedString = highlighted),
                onValueChange = { newValue ->
                    textFieldValue = newValue
                    lastSyncedRaw = newValue.text
                    state.updateRawJson(newValue.text)
                },
                onTextLayout = { textLayoutResult = it },
                textStyle = monoStyle,
                cursorBrush = SolidColor(colors.key),
                modifier = Modifier
                    .weight(1f)
                    .focusRequester(focusRequester)
                    .horizontalScroll(horizontalScrollState)
                    .padding(start = 8.dp, end = 16.dp),
            )
        }
    }
}

// ── Previews ──

private val previewJson = """
{
    "name": "John Doe",
    "age": 30,
    "isActive": true
}
""".trimIndent()

private val previewColors = JsonCmpColors.Dark

@Preview
@Composable
private fun Preview_JsonEditor() {
    MaterialTheme {
        JsonEditor(
            state = JsonEditorState(
                initialJson = previewJson,
                isEditing = true,
            ),
            searchQuery = "",
            colors = previewColors,
        )
    }
}

@Preview
@Composable
private fun Preview_JsonEditorWithSearch() {
    MaterialTheme {
        JsonEditor(
            state = JsonEditorState(
                initialJson = previewJson,
                isEditing = true,
            ),
            searchQuery = "age",
            colors = previewColors,
        )
    }
}
