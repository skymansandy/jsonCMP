/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.jsoncmp.ui.viewer.component

import androidx.compose.foundation.ScrollState
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
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.selection.DisableSelection
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import dev.skymansandy.jsoncmp.domain.line.JsonLine
import dev.skymansandy.jsoncmp.domain.store.JsonAction
import dev.skymansandy.jsoncmp.domain.store.JsonHolderState
import dev.skymansandy.jsoncmp.theme.LocalJsonCmpColors
import dev.skymansandy.jsoncmp.ui.common.GutterCell
import dev.skymansandy.jsoncmp.ui.common.JsonLineView

/** Virtualized LazyColumn of JSON lines with a sticky gutter and horizontal scroll. */
@Suppress("LongParameterList")
@Composable
internal fun JsonViewerLineList(
    modifier: Modifier = Modifier,
    state: JsonHolderState,
    visibleLines: List<JsonLine>,
    numDigits: Int,
    listState: LazyListState,
    horizontalScrollState: ScrollState,
    borderColor: Color,
    density: Density,
    gutterWidth: Dp,
    onGutterWidthChange: (Dp) -> Unit,
    onAction: (JsonAction) -> Unit,
    searchQuery: String,
    foldState: Map<Int, Boolean>,
) {
    val colors = LocalJsonCmpColors.current

    BoxWithConstraints(modifier = modifier) {
        val viewportWidth = maxWidth

        // Static gutter backdrop — fills the full height so the gutter area has a
        // consistent background even below the last line of content.
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

        val lineContent: @Composable (JsonLine) -> Unit = { line ->
            val isFolded = line.foldId != null && foldState[line.foldId] == true

            Row(
                modifier = Modifier
                    .defaultMinSize(minWidth = viewportWidth)
                    .height(IntrinsicSize.Min),
            ) {
                // Sticky gutter: offset counters horizontal scroll so it stays pinned to the left.
                // zIndex(1f) ensures it renders above the scrollable content.
                DisableSelection {
                    GutterCell(
                        lineNumber = line.lineNumber,
                        numDigits = numDigits,
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

                JsonLineView(
                    line = line,
                    isFolded = isFolded,
                    searchQuery = searchQuery,
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

        SelectionContainer {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .horizontalScroll(horizontalScrollState),
            ) {
                items(
                    items = visibleLines,
                    key = { it.lineNumber },
                ) { line -> lineContent(line) }
            }
        }
    }
}
