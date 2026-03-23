package dev.skymansandy.jsoncmp.component.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
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
            val borderColor = colors.gutterBorder
            LineNumberGutter(
                lineCount = lineCount,
                textLayoutResult = textLayoutResult,
                colors = colors,
                borderColor = borderColor,
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

@Composable
private fun LineNumberGutter(
    lineCount: Int,
    textLayoutResult: TextLayoutResult?,
    colors: JsonCmpColors,
    borderColor: androidx.compose.ui.graphics.Color,
    gutterMinHeight: androidx.compose.ui.unit.Dp = 0.dp,
) {
    val numDigits by remember(lineCount) {
        mutableStateOf(lineCount.toString().length)
    }

    if (textLayoutResult == null || textLayoutResult.lineCount < lineCount) {
        // Fallback: render without alignment until layout is available
        Box(
            modifier = Modifier
                .defaultMinSize(minHeight = gutterMinHeight)
                .background(colors.gutterBackground)
                .drawBehind {
                    val x = size.width
                    drawLine(
                        borderColor,
                        Offset(x, 0f),
                        Offset(x, size.height),
                        strokeWidth = 1.dp.toPx()
                    )
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
        modifier = Modifier
            .defaultMinSize(minHeight = gutterMinHeight)
            .background(colors.gutterBackground)
            .drawBehind {
                val x = size.width
                drawLine(
                    borderColor,
                    Offset(x, 0f),
                    Offset(x, size.height),
                    strokeWidth = 1.dp.toPx()
                )
            }
            .padding(start = 12.dp, end = 8.dp),
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
    ) { measurables, constraints ->
        val placeables =
            measurables.map { it.measure(constraints.copy(minWidth = 0, minHeight = 0)) }
        val width = placeables.maxOfOrNull { it.width } ?: 0
        val contentHeight = if (textLayoutResult.lineCount > 0) {
            textLayoutResult.getLineBottom(textLayoutResult.lineCount - 1).toInt()
        } else {
            placeables.sumOf { it.height }
        }
        val height = maxOf(contentHeight, constraints.minHeight)

        layout(width, height) {
            placeables.forEachIndexed { index, placeable ->
                val y = textLayoutResult.getLineTop(index).toInt()
                    ?: (index * (placeables.firstOrNull()?.height ?: 0))
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
