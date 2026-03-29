package dev.skymansandy.jsoncmp.ui.viewer.component

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.selection.DisableSelection
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import dev.skymansandy.jsoncmp.domain.line.JsonLine
import dev.skymansandy.jsoncmp.domain.store.JsonAction
import dev.skymansandy.jsoncmp.domain.store.JsonStoreState
import dev.skymansandy.jsoncmp.ui.common.ContentCell
import dev.skymansandy.jsoncmp.ui.common.GutterCell
import dev.skymansandy.jsoncmp.ui.theme.JsonCmpColors

/** Virtualised LazyColumn of JSON lines with a sticky gutter and horizontal scroll. */
@Suppress("LongParameterList")
@Composable
internal fun JsonViewerLineList(
    visibleLines: List<JsonLine>,
    numDigits: Int,
    listState: androidx.compose.foundation.lazy.LazyListState,
    horizontalScrollState: androidx.compose.foundation.ScrollState,
    borderColor: androidx.compose.ui.graphics.Color,
    density: androidx.compose.ui.unit.Density,
    gutterWidth: Dp,
    onGutterWidthChange: (Dp) -> Unit,
    state: JsonStoreState,
    onAction: (JsonAction) -> Unit,
    searchQuery: String,
    colors: JsonCmpColors,
    foldState: Map<Int, Boolean>,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(modifier = modifier) {
        val viewportWidth = maxWidth

        // Gutter background — behind the LazyColumn, fills remaining height
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

        // Horizontal scroll wraps the entire LazyColumn so gutter + content scroll together
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .horizontalScroll(horizontalScrollState),
        ) {
            items(
                items = visibleLines,
                key = { it.lineNumber },
            ) { line ->
                val isFolded = line.foldId != null && foldState[line.foldId] == true

                Row(
                    modifier = Modifier
                        .defaultMinSize(minWidth = viewportWidth)
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
                                    onAction(JsonAction.ToggleFold(id))
                                }
                            },
                            modifier = Modifier
                                .fillMaxHeight()
                                .zIndex(1f)
                                .offset { IntOffset(horizontalScrollState.value, 0) }
                                .background(colors.gutterBackground)
                                .onSizeChanged { size ->
                                    val newWidth: Dp
                                    with(density) { newWidth = size.width.toDp() }
                                    if (newWidth != gutterWidth) onGutterWidthChange(newWidth)
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
                                onAction(JsonAction.ToggleFold(id))
                            }
                        },
                        foldedContentProvider = { state.computeFoldedContent(line) },
                        hasHiddenMatchProvider = { state.hasFoldedMatch(line, searchQuery) },
                        modifier = Modifier,
                    )
                }
            }
        }
    }
}
