package dev.skymansandy.jsoncmp.component.viewer

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.DisableSelection
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import dev.skymansandy.jsoncmp.component.common.ContentCell
import dev.skymansandy.jsoncmp.component.common.GutterCell
import dev.skymansandy.jsoncmp.component.common.PlainText
import dev.skymansandy.jsoncmp.config.JsonAction
import dev.skymansandy.jsoncmp.config.JsonStore
import dev.skymansandy.jsoncmp.config.JsonStoreState
import dev.skymansandy.jsoncmp.helper.constants.colors.JsonCmpColors
import dev.skymansandy.jsoncmp.helper.constants.typography.monoStyle
import dev.skymansandy.jsoncmp.model.JsonLine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private data class SearchMatch(val lineIdx: Int, val charOffset: Int)

@Composable
internal fun JsonViewer(
    modifier: Modifier = Modifier,
    state: JsonStoreState,
    onAction: (JsonAction) -> Unit,
    searchQuery: String,
    colors: JsonCmpColors,
) {
    if (state.allLines.isEmpty()) {
        JsonViewerEmptyState(modifier, state, searchQuery, colors)
        return
    }
    JsonViewerContent(modifier, state, onAction, searchQuery, colors)
}

@Composable
private fun JsonViewerEmptyState(
    modifier: Modifier,
    state: JsonStoreState,
    searchQuery: String,
    colors: JsonCmpColors,
) {
    if (state.isParsing) {
        Box(
            modifier = modifier.fillMaxSize().background(colors.background),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = colors.punctuation,
                strokeWidth = 2.dp,
            )
        }
    } else if (state.raw.length <= 100 * 1024) {
        PlainText(
            modifier = modifier,
            text = state.raw,
            searchQuery = searchQuery,
            colors = colors,
        )
    } else {
        val truncatedText = state.raw.take(100 * 1024)
        Column(modifier = modifier.fillMaxHeight()) {
            Text(
                text = "Invalid JSON. Showing truncated preview (first 100 KB of ${state.raw.length / 1024} KB)",
                style = monoStyle,
                color = colors.highlightFg,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.highlight)
                    .padding(horizontal = 12.dp, vertical = 4.dp),
            )
            Box(modifier = Modifier.weight(1f)) {
                PlainText(
                    text = truncatedText,
                    searchQuery = searchQuery,
                    colors = colors,
                )
            }
        }
    }
}

@Suppress("LongMethod", "LongParameterList", "CyclomaticComplexMethod")
@Composable
private fun JsonViewerContent(
    modifier: Modifier,
    state: JsonStoreState,
    onAction: (JsonAction) -> Unit,
    searchQuery: String,
    colors: JsonCmpColors,
) {
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
                    val lineText = line.parts.joinToString("") { it.text }.lowercase()
                    var idx = lineText.indexOf(queryLower)
                    while (idx >= 0) {
                        add(SearchMatch(lineIdx = lineIdx, charOffset = idx))
                        idx = lineText.indexOf(queryLower, idx + queryLower.length)
                    }
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

    // Scroll to current match line (vertical + horizontal)
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

            if (matchX < visibleStart || matchX + charWidthPx * searchQuery.length > visibleEnd) {
                val target = (matchX - viewport / 2).toInt().coerceAtLeast(0)
                horizontalScrollState.animateScrollTo(target)
            }
        }
    }

    Column(modifier = modifier.background(colors.background)) {
        if (searchQuery.isNotBlank()) {
            SearchResultsBar(
                totalMatches = searchMatches.size,
                currentMatch = currentMatchIndex,
                colors = colors,
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
            colors = colors,
            foldState = foldState,
            modifier = Modifier.weight(1f).background(colors.background),
        )
    }
}

@Composable
private fun JsonViewerLineList(
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

        // Single horizontal scroll wraps the entire LazyColumn
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

@Composable
private fun SearchResultsBar(
    totalMatches: Int,
    currentMatch: Int,
    colors: JsonCmpColors,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
) {
    val hasMatches = totalMatches > 0
    val label = if (hasMatches) {
        "${currentMatch + 1} of $totalMatches"
    } else {
        "No results"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.gutterBackground)
            .padding(horizontal = 8.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = monoStyle,
            color = if (hasMatches) colors.punctuation else colors.lineNumber,
        )

        Spacer(modifier = Modifier.weight(1f))

        IconButton(
            onClick = onPrevious,
            enabled = hasMatches,
            modifier = Modifier.size(32.dp),
            colors = IconButtonDefaults.iconButtonColors(
                contentColor = colors.punctuation,
                disabledContentColor = colors.lineNumber,
            ),
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowUp,
                contentDescription = "Previous match",
                modifier = Modifier.size(18.dp),
            )
        }

        IconButton(
            onClick = onNext,
            enabled = hasMatches,
            modifier = Modifier.size(32.dp),
            colors = IconButtonDefaults.iconButtonColors(
                contentColor = colors.punctuation,
                disabledContentColor = colors.lineNumber,
            ),
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = "Next match",
                modifier = Modifier.size(18.dp),
            )
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
    val store = remember { JsonStore(initialJson = previewJson) }
    val state by store.state.collectAsState()
    MaterialTheme {
        JsonViewer(
            state = state,
            onAction = store::dispatch,
            searchQuery = "",
            colors = previewColors,
        )
    }
}

@Preview
@Composable
private fun Preview_JsonViewerWithSearch() {
    val store = remember { JsonStore(initialJson = previewJson) }
    val state by store.state.collectAsState()
    MaterialTheme {
        JsonViewer(
            state = state,
            onAction = store::dispatch,
            searchQuery = "John",
            colors = previewColors,
        )
    }
}
