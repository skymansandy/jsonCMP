package dev.skymansandy.jsoncmp.ui.viewer.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.text.selection.DisableSelection
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.skymansandy.jsoncmp.domain.line.JsonLine
import dev.skymansandy.jsoncmp.helper.mocks.previewLines
import dev.skymansandy.jsoncmp.theme.LocalJsonCmpColors
import dev.skymansandy.jsoncmp.ui.common.GutterCell

/**
 * Viewer-mode gutter: renders [GutterCell] items in a [Column] with fold/collapse support.
 */
@Composable
internal fun LineGutterViewMode(
    modifier: Modifier = Modifier,
    lines: List<JsonLine>,
    totalLineCount: Int,
    foldState: SnapshotStateMap<Int, Boolean>,
    gutterMinHeight: Dp = 0.dp,
) {
    val colors = LocalJsonCmpColors.current
    val borderColor = colors.gutterBorder

    val numDigits by remember(totalLineCount) {
        mutableStateOf(totalLineCount.toString().length)
    }

    // DisableSelection prevents text selection from spilling into gutter cells
    DisableSelection {
        Column(
            modifier = modifier
                .defaultMinSize(minHeight = gutterMinHeight)
                .background(colors.gutterBackground)
                // Right border separating gutter from content
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
                    lineNumber = line.lineNumber,
                    numDigits = numDigits,
                    foldId = line.foldId,
                    isFolded = isFolded,
                    // Toggle fold directly in the shared SnapshotStateMap
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

@Preview
@Composable
private fun Preview_LineGutter_ViewMode_Viewer() {
    MaterialTheme {
        LineGutterViewMode(
            lines = previewLines,
            totalLineCount = previewLines.size,
            foldState = mutableStateMapOf(),
        )
    }
}
