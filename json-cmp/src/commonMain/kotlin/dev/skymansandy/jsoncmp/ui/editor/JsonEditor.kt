/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.jsoncmp.ui.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.skymansandy.jsoncmp.domain.store.JsonAction
import dev.skymansandy.jsoncmp.domain.store.JsonHolderImpl
import dev.skymansandy.jsoncmp.domain.store.JsonHolderState
import dev.skymansandy.jsoncmp.helper.mocks.previewJson
import dev.skymansandy.jsoncmp.theme.JsonCmpColors
import dev.skymansandy.jsoncmp.theme.LocalJsonCmpColors
import dev.skymansandy.jsoncmp.theme.monoStyle
import dev.skymansandy.jsoncmp.ui.common.highlightJson
import dev.skymansandy.jsoncmp.ui.editor.component.EditorToolbar
import dev.skymansandy.jsoncmp.ui.editor.component.ErrorBanner
import dev.skymansandy.jsoncmp.ui.editor.component.LineGutterEditMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

/** Internal JSON editor with syntax-highlighted BasicTextField and line gutter. */
@Composable
internal fun JsonEditor(
    modifier: Modifier = Modifier,
    state: JsonHolderState,
    searchQuery: String,
    onAction: (JsonAction) -> Unit,
) {
    val colors = LocalJsonCmpColors.current

    var textFieldValue by remember { mutableStateOf(TextFieldValue(state.raw)) }

    // Sync from store when the raw text changes externally (format, sort, etc.)
    LaunchedEffect(state.raw) {
        if (state.raw != textFieldValue.text) {
            textFieldValue = textFieldValue.copy(
                text = state.raw,
                selection = textFieldValue.selection.constrain(state.raw.length),
            )
        }
    }

    val horizontalScrollState = rememberScrollState()
    val lineCount = remember(textFieldValue.text) { textFieldValue.text.count { it == '\n' } + 1 }
    var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }

    val focusRequester = remember { FocusRequester() }

    // Syntax highlighting runs off the main thread to avoid jank on large inputs.
    // Short debounce avoids redundant work during fast typing; stale results are
    // discarded via the text-length guard below.
    val highlighted by produceState(
        initialValue = AnnotatedString(textFieldValue.text),
        textFieldValue.text, searchQuery, colors,
    ) {
        delay(HIGHLIGHT_DEBOUNCE_MS)
        value = withContext(Dispatchers.Default) {
            highlightJson(
                text = textFieldValue.text,
                searchQuery = searchQuery,
                colors = colors,
            )
        }
    }

    // Guard: if highlighting is stale (from previous text), fall back to plain text
    // so BasicTextField never receives a mismatched AnnotatedString.
    val safeHighlighted = if (highlighted.text == textFieldValue.text) {
        highlighted
    } else {
        AnnotatedString(textFieldValue.text)
    }

    Column(
        modifier = modifier.background(colors.background),
    ) {
        EditorToolbar(
            modifier = Modifier.fillMaxWidth(),
            state = state,
            onAction = onAction,
        )

        ErrorBanner(
            modifier = Modifier.fillMaxWidth(),
            error = state.error,
        )

        BoxWithConstraints(
            modifier = Modifier.clickable(
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
                    gutterMinHeight = gutterMinHeight,
                )

                // JSON editor
                BasicTextField(
                    value = textFieldValue.copy(annotatedString = safeHighlighted),
                    onValueChange = { newValue ->
                        textFieldValue = newValue
                        onAction(JsonAction.UpdateJson(newValue.text))
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
}

private const val HIGHLIGHT_DEBOUNCE_MS = 100L

private fun TextRange.constrain(maxLength: Int): TextRange {
    val newStart = start.coerceIn(0, maxLength)
    val newEnd = end.coerceIn(0, maxLength)
    return if (newStart == start && newEnd == end) this else TextRange(newStart, newEnd)
}

// ── Previews ──
@Preview
@Composable
private fun Preview_JsonEditor() {
    val store = remember { JsonHolderImpl(initialJson = previewJson, isEditing = true) }
    val state by store.state.collectAsState()
    MaterialTheme {
        CompositionLocalProvider(LocalJsonCmpColors provides JsonCmpColors.Dark) {
            JsonEditor(
                state = state,
                onAction = store::dispatch,
                searchQuery = "",
            )
        }
    }
}

@Preview
@Composable
private fun Preview_JsonEditorWithSearch() {
    val store = remember { JsonHolderImpl(initialJson = previewJson, isEditing = true) }
    val state by store.state.collectAsState()
    MaterialTheme {
        CompositionLocalProvider(LocalJsonCmpColors provides JsonCmpColors.Dark) {
            JsonEditor(
                state = state,
                onAction = store::dispatch,
                searchQuery = "age",
            )
        }
    }
}
