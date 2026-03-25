package dev.skymansandy.jsoncmp.component.viewer

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.DisableSelection
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.skymansandy.jsoncmp.component.common.ContentCell
import dev.skymansandy.jsoncmp.component.common.GutterCell
import dev.skymansandy.jsoncmp.component.common.PlainText
import dev.skymansandy.jsoncmp.config.JsonEditorState
import dev.skymansandy.jsoncmp.helper.constants.colors.JsonCmpColors

@Composable
internal fun JsonViewer(
    modifier: Modifier = Modifier,
    state: JsonEditorState,
    searchQuery: String,
    colors: JsonCmpColors,
) {
    val allLines = state.allLines
    val foldState = state.foldState

    if (allLines.isEmpty()) {
        PlainText(
            modifier = modifier,
            text = state.rawJson,
            searchQuery = searchQuery,
            colors = colors,
        )
        return
    }

    val visibleLines = state.visibleLines
    val numDigits = remember(allLines.size) { allLines.size.toString().length }
    val horizontalScrollState = rememberScrollState()
    val borderColor = colors.gutterBorder
    val density = LocalDensity.current
    var gutterWidth by remember { mutableStateOf(0.dp) }

    Box(modifier = modifier.background(colors.background)) {
        // Full-height gutter background strip behind the LazyColumn
        if (gutterWidth > 0.dp) {
            Box(
                modifier = Modifier
                    .width(gutterWidth)
                    .fillMaxHeight()
                    .background(colors.gutterBackground)
                    .drawBehind {
                        val x = size.width
                        drawLine(
                            color = borderColor,
                            start = Offset(x, 0f),
                            end = Offset(x, size.height),
                            strokeWidth = 1.dp.toPx(),
                        )
                    },
            )
        }

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(
                items = visibleLines,
                key = { it.lineNumber },
            ) { line ->
                val isFolded = line.foldId != null && foldState[line.foldId] == true

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min),
                ) {
                    DisableSelection {
                        GutterCell(
                            lineNumber = line.lineNumber,
                            numDigits = numDigits,
                            colors = colors,
                            foldId = line.foldId,
                            isFolded = isFolded,
                            onFoldToggle = {
                                line.foldId?.let { id ->
                                    foldState[id] = !(foldState[id] ?: false)
                                }
                            },
                            modifier = Modifier
                                .fillMaxHeight()
                                .onSizeChanged { size ->
                                    val newWidth: Dp
                                    with(density) { newWidth = size.width.toDp() }
                                    if (newWidth != gutterWidth) gutterWidth = newWidth
                                },
                        )
                    }

                    ContentCell(
                        line = line,
                        isFolded = isFolded,
                        searchQuery = searchQuery,
                        colors = colors,
                        onFoldToggle = {
                            line.foldId?.let { id ->
                                foldState[id] = !(foldState[id] ?: false)
                            }
                        },
                        foldedContentProvider = { state.computeFoldedContent(line) },
                        hasHiddenMatchProvider = { state.hasFoldedMatch(line, searchQuery) },
                        modifier = Modifier
                            .weight(1f)
                            .horizontalScroll(horizontalScrollState),
                    )
                }
            }
        }
    }
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
    MaterialTheme {
        JsonViewer(
            state = JsonEditorState(
                initialJson = previewJson,
                isEditing = false,
            ),
            searchQuery = "",
            colors = previewColors,
        )
    }
}

@Preview
@Composable
private fun Preview_JsonViewerWithSearch() {
    MaterialTheme {
        JsonViewer(
            state = JsonEditorState(
                initialJson = previewJson,
                isEditing = false,
            ),
            searchQuery = "John",
            colors = previewColors,
        )
    }
}
