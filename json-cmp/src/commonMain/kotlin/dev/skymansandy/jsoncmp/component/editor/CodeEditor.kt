package dev.skymansandy.jsoncmp.component.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.Layout
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
internal fun CodeEditor(
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
    val verticalScrollState = rememberScrollState()
    val lineCount = remember(textFieldValue.text) { textFieldValue.text.count { it == '\n' } + 1 }
    val numDigits = remember(lineCount) { lineCount.toString().length }
    var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }

    val highlighted: AnnotatedString = remember(textFieldValue.text, searchQuery, colors) {
        highlightJson(
            text = textFieldValue.text,
            searchQuery = searchQuery,
            colors = colors,
        )
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 200.dp)
            .verticalScroll(verticalScrollState)
            .background(colors.background),
    ) {
        // Line number gutter — positioned using actual text layout line offsets
        val borderColor = colors.gutterBorder
        LineNumberGutter(
            lineCount = lineCount,
            numDigits = numDigits,
            textLayoutResult = textLayoutResult,
            colors = colors,
            borderColor = borderColor,
        )

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
                .horizontalScroll(horizontalScrollState)
                .padding(start = 8.dp, end = 16.dp),
        )
    }
}

@Composable
private fun LineNumberGutter(
    lineCount: Int,
    numDigits: Int,
    textLayoutResult: TextLayoutResult?,
    colors: JsonCmpColors,
    borderColor: androidx.compose.ui.graphics.Color,
) {
    if (textLayoutResult == null || textLayoutResult.lineCount < lineCount) {
        // Fallback: render without alignment until layout is available
        Box(
            modifier = Modifier
                .background(colors.gutterBackground)
                .drawBehind {
                    val x = size.width
                    drawLine(borderColor, Offset(x, 0f), Offset(x, size.height), strokeWidth = 1.dp.toPx())
                }
                .padding(start = 12.dp, end = 8.dp),
        ) {
            androidx.compose.foundation.layout.Column {
                for (i in 1..lineCount) {
                    Text(
                        text = i.toString().padStart(numDigits),
                        style = monoStyle,
                        color = colors.lineNumber,
                        softWrap = false,
                    )
                }
            }
        }
        return
    }

    // Use Layout to position each line number at the exact Y offset from the text layout
    Layout(
        content = {
            for (i in 1..lineCount) {
                Text(
                    text = i.toString().padStart(numDigits),
                    style = monoStyle,
                    color = colors.lineNumber,
                    softWrap = false,
                )
            }
        },
        modifier = Modifier
            .background(colors.gutterBackground)
            .drawBehind {
                val x = size.width
                drawLine(borderColor, Offset(x, 0f), Offset(x, size.height), strokeWidth = 1.dp.toPx())
            }
            .padding(start = 12.dp, end = 8.dp),
    ) { measurables, constraints ->
        val placeables = measurables.map { it.measure(constraints.copy(minWidth = 0, minHeight = 0)) }
        val width = placeables.maxOfOrNull { it.width } ?: 0
        val height = if (textLayoutResult.lineCount > 0) {
            textLayoutResult.getLineBottom(textLayoutResult.lineCount - 1).toInt()
        } else {
            placeables.sumOf { it.height }
        }

        layout(width, height) {
            placeables.forEachIndexed { index, placeable ->
                val y = textLayoutResult.getLineTop(index).toInt() ?: (index * (placeables.firstOrNull()?.height ?: 0))
                placeable.placeRelative(0, y)
            }
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
private fun Preview_CodeEditor() {
    MaterialTheme {
        CodeEditor(
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
private fun Preview_CodeEditorWithSearch() {
    MaterialTheme {
        CodeEditor(
            state = JsonEditorState(
                initialJson = previewJson,
                isEditing = true,
            ),
            searchQuery = "age",
            colors = previewColors,
        )
    }
}
