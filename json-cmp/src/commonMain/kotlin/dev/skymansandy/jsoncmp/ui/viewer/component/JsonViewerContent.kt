/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.jsoncmp.ui.viewer.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import dev.skymansandy.jsoncmp.domain.store.JsonAction
import dev.skymansandy.jsoncmp.domain.store.JsonHolderState
import dev.skymansandy.jsoncmp.theme.LocalJsonCmpColors
import dev.skymansandy.jsoncmp.theme.monoStyle
import dev.skymansandy.jsoncmp.ui.viewer.model.SearchMatch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Main viewer body: search bar, virtualized line list, and off-thread match computation. */
@Suppress("LongMethod", "LongParameterList", "CyclomaticComplexMethod")
@Composable
internal fun JsonViewerContent(
    modifier: Modifier,
    state: JsonHolderState,
    onAction: (JsonAction) -> Unit,
    searchQuery: String,
) {
    val colors = LocalJsonCmpColors.current

    val allLines = state.allLines
    val foldState = state.foldState
    val visibleLines = state.visibleLines
    val numDigits = remember(allLines.size) { allLines.size.toString().length }
    val horizontalScrollState = rememberScrollState()
    val borderColor = colors.gutterBorder
    val density = LocalDensity.current
    var gutterWidth by remember { mutableStateOf(0.dp) }
    val listState = rememberLazyListState()
    val textMeasurer = rememberTextMeasurer()
    val charWidthPx = remember(textMeasurer, density) {
        textMeasurer.measure("M", monoStyle).size.width
    }

    // Search match tracking — computed off main thread.
    var searchMatches by remember { mutableStateOf(emptyList<SearchMatch>()) }
    var currentMatchIndex by remember { mutableStateOf(0) }
    // Bumped by next/prev navigation to trigger scroll without false triggers from fold changes.
    var scrollToMatchTrigger by remember { mutableStateOf(0) }

    // Re-index search matches whenever the query, visible lines, or fold state changes.
    // Runs on Default dispatcher to avoid blocking the UI on large JSON.
    LaunchedEffect(visibleLines, searchQuery, foldState) {
        if (searchQuery.isBlank()) {
            searchMatches = emptyList()
            currentMatchIndex = 0
            return@LaunchedEffect
        }

        val previousQuery = searchMatches.firstOrNull()?.let { searchQuery } ?: ""
        searchMatches = withContext(Dispatchers.Default) {
            val queryLower = searchQuery.lowercase()
            buildList {
                visibleLines.forEachIndexed { lineIdx, line ->
                    // Find all occurrences in the visible text of this line
                    val lineText = line.text.lowercase()
                    var idx = lineText.indexOf(queryLower)
                    while (idx >= 0) {
                        add(SearchMatch(lineIdx = lineIdx, charOffset = idx))
                        idx = lineText.indexOf(queryLower, idx + queryLower.length)
                    }
                    // Also count matches hidden inside collapsed folds
                    val foldedCount = state.countFoldedMatches(line, searchQuery)
                    repeat(foldedCount) { add(SearchMatch(lineIdx = lineIdx, charOffset = 0)) }
                }
            }
        }
        if (previousQuery != searchQuery) {
            // New search query → reset to first match and scroll there.
            currentMatchIndex = 0
            scrollToMatchTrigger++
        } else {
            // Fold/visibility change only → keep position, don't scroll.
            currentMatchIndex = currentMatchIndex.coerceIn(
                0,
                (searchMatches.size - 1).coerceAtLeast(0),
            )
        }
    }

    // Scroll to current match (vertical via LazyList + horizontal via ScrollState)
    LaunchedEffect(scrollToMatchTrigger) {
        if (searchMatches.isNotEmpty()) {
            val match = searchMatches[currentMatchIndex]
            listState.animateScrollToItem(match.lineIdx)

            // Horizontal scroll: position of match char in the full row
            val gutterPx = with(density) { gutterWidth.toPx() }
            val padPx = with(density) { 8.dp.toPx() }
            val matchX = gutterPx + padPx + charWidthPx * match.charOffset
            val viewport = horizontalScrollState.viewportSize
            val visibleStart = horizontalScrollState.value + gutterPx
            val visibleEnd = horizontalScrollState.value + viewport

            // Scroll to the search occurrence
            if (matchX < visibleStart || matchX + charWidthPx * searchQuery.length > visibleEnd) {
                val target = (matchX - viewport / 2).toInt().coerceAtLeast(0)
                horizontalScrollState.animateScrollTo(target)
            }
        }
    }

    Column(
        modifier = modifier
            .background(colors.background),
    ) {
        if (searchQuery.isNotBlank()) {
            SearchResultsBar(
                modifier = Modifier.fillMaxWidth(),
                totalMatches = searchMatches.size,
                currentMatch = currentMatchIndex,
                onPrevious = {
                    if (searchMatches.isNotEmpty()) {
                        currentMatchIndex = if (currentMatchIndex > 0) {
                            currentMatchIndex - 1
                        } else {
                            searchMatches.size - 1
                        }
                        scrollToMatchTrigger++
                    }
                },
                onNext = {
                    if (searchMatches.isNotEmpty()) {
                        currentMatchIndex = if (currentMatchIndex < searchMatches.size - 1) {
                            currentMatchIndex + 1
                        } else {
                            0
                        }
                        scrollToMatchTrigger++
                    }
                },
            )
        }

        JsonViewerLineList(
            modifier = Modifier.weight(1f).background(colors.background),
            visibleLines = visibleLines,
            numDigits = numDigits,
            listState = listState,
            horizontalScrollState = horizontalScrollState,
            borderColor = borderColor,
            density = density,
            gutterWidth = gutterWidth,
            onGutterWidthChange = { gutterWidth = it },
            state = state,
            onAction = onAction,
            searchQuery = searchQuery,
            foldState = foldState,
        )
    }
}
