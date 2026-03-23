package dev.skymansandy.jsoncmp.component.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.selection.DisableSelection
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.skymansandy.jsoncmp.helper.constants.colors.JsonCmpColors
import dev.skymansandy.jsoncmp.helper.constants.typography.monoStyle
import dev.skymansandy.jsoncmp.model.FoldType
import dev.skymansandy.jsoncmp.model.JsonLine
import dev.skymansandy.jsoncmp.model.JsonPart

/**
 * Viewer-mode gutter: renders [GutterCell] items in a [Column] with fold/collapse support.
 */
@Composable
internal fun LineGutter(
    lines: List<JsonLine>,
    foldState: SnapshotStateMap<Int, Boolean>,
    colors: JsonCmpColors,
    modifier: Modifier = Modifier,
    gutterMinHeight: Dp = 0.dp,
) {
    val numDigits by remember(lines) {
        mutableStateOf(lines.size.toString().length)
    }

    val borderColor = colors.gutterBorder

    DisableSelection {
        Column(
            modifier = modifier
                .defaultMinSize(minHeight = gutterMinHeight)
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
        ) {
            for (line in lines) {
                val isFolded = line.foldId != null && foldState[line.foldId] == true
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
}

/**
 * Editor-mode gutter: renders plain line numbers aligned to [TextLayoutResult] offsets.
 */
@Composable
internal fun LineGutter(
    lineCount: Int,
    textLayoutResult: TextLayoutResult?,
    colors: JsonCmpColors,
    modifier: Modifier = Modifier,
    gutterMinHeight: Dp = 0.dp,
) {
    val borderColor = colors.gutterBorder
    val numDigits by remember(lineCount) {
        mutableStateOf(lineCount.toString().length)
    }

    val gutterModifier = modifier
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
        }
        .padding(start = 12.dp, end = 8.dp)

    if (textLayoutResult == null || textLayoutResult.lineCount < lineCount) {
        Box(modifier = gutterModifier) {
            Column {
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

    Layout(
        modifier = gutterModifier,
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
                placeable.placeRelative(0, y)
            }
        }
    }
}

// ── Previews ──

private val previewColors = JsonCmpColors.Dark

private val previewLines = listOf(
    JsonLine(
        lineNumber = 1,
        depth = 0,
        parts = listOf(JsonPart.Punct("{")),
        foldId = 0,
        foldType = FoldType.Object,
        parentFoldIds = emptyList(),
        foldChildCount = 3,
    ),
    JsonLine(
        lineNumber = 2,
        depth = 1,
        parts = listOf(
            JsonPart.Indent("    "),
            JsonPart.Key("\"name\""),
            JsonPart.Punct(": "),
            JsonPart.StrVal("\"John Doe\""),
        ),
        foldId = null,
        foldType = null,
        parentFoldIds = listOf(0),
    ),
    JsonLine(
        lineNumber = 3,
        depth = 0,
        parts = listOf(JsonPart.Punct("}")),
        foldId = null,
        foldType = null,
        parentFoldIds = emptyList(),
    ),
)

@Preview
@Composable
private fun Preview_LineGutter_Viewer() {
    MaterialTheme {
        LineGutter(
            lines = previewLines,
            foldState = androidx.compose.runtime.mutableStateMapOf(),
            colors = previewColors,
        )
    }
}

@Preview
@Composable
private fun Preview_LineGutter_Editor() {
    MaterialTheme {
        LineGutter(
            lineCount = 5,
            textLayoutResult = null,
            colors = previewColors,
        )
    }
}
