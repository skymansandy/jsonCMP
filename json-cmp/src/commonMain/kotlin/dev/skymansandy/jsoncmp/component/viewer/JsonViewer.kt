package dev.skymansandy.jsoncmp.component.viewer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.DisableSelection
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.tooling.preview.Preview
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

    val visibleLines by remember(allLines) {
        derivedStateOf {
            allLines.filter { line ->
                line.parentFoldIds.none { foldState[it] == true }
            }
        }
    }

    val numDigits = remember(allLines) {
        allLines.size.toString().length
    }

    SelectionContainer(
        modifier = modifier,
    ) {
        BoxWithConstraints {
            val hasBoundedHeight = constraints.hasBoundedHeight
            val scrollModifier = if (hasBoundedHeight) {
                Modifier.verticalScroll(rememberScrollState())
            } else {
                Modifier
            }
            val gutterMinHeight = if (hasBoundedHeight) maxHeight else 0.dp

            Row(
                modifier = Modifier.fillMaxWidth().then(scrollModifier),
            ) {
                val borderColor = colors.gutterBorder
                DisableSelection {
                    Column(
                        modifier = Modifier
                            .defaultMinSize(minHeight = gutterMinHeight)
                            .background(colors.gutterBackground)
                            .drawBehind {
                                val x = size.width
                                drawLine(
                                    borderColor,
                                    Offset(x, 0f),
                                    Offset(x, size.height),
                                    strokeWidth = 1.dp.toPx(),
                                )
                            },
                    ) {
                    for (line in visibleLines) {
                        val isFolded =
                            line.foldId != null && foldState[line.foldId] == true
                        GutterCell(
                            line = line,
                            isFolded = isFolded,
                            numDigits = numDigits,
                            colors = colors,
                            onFoldToggle = {
                                line.foldId?.let { id ->
                                    foldState[id] = !(foldState[id] ?: false)
                                }
                            },
                        )
                    }
                }
            }

            Column(
                modifier = Modifier.weight(1f),
            ) {
                for (line in visibleLines) {
                    val isFolded =
                        line.foldId != null && foldState[line.foldId] == true
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
                    )
                }
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
