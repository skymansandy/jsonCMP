package dev.skymansandy.jsoncmp.component.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.skymansandy.jsoncmp.component.common.GutterCell
import dev.skymansandy.jsoncmp.helper.constants.colors.JsonCmpColors
import dev.skymansandy.jsoncmp.helper.mock.previewColors

/**
 * Editor-mode gutter: renders plain line numbers aligned to [TextLayoutResult] offsets.
 */
@Composable
internal fun LineGutterEditMode(
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

    if (textLayoutResult == null || textLayoutResult.lineCount < lineCount) {
        Box(modifier = gutterModifier) {
            Column {
                for (i in 1..lineCount) {
                    GutterCell(
                        lineNumber = i,
                        numDigits = numDigits,
                        colors = colors,
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
                GutterCell(
                    lineNumber = i,
                    numDigits = numDigits,
                    colors = colors,
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

@Preview
@Composable
private fun Preview_LineGutter_Editor() {
    MaterialTheme {
        LineGutterEditMode(
            lineCount = 5,
            textLayoutResult = null,
            colors = previewColors,
        )
    }
}
