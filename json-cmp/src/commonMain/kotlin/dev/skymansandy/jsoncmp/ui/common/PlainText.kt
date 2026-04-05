/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.jsoncmp.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.skymansandy.jsoncmp.theme.LocalJsonCmpColors
import dev.skymansandy.jsoncmp.theme.monoStyle

/** Fallback text display for invalid/unparseable JSON with lazy line-based rendering and optional search highlighting. */
@Composable
internal fun PlainText(
    modifier: Modifier = Modifier,
    text: String,
    searchQuery: String,
) {
    val colors = LocalJsonCmpColors.current
    val lines = remember(text) { text.split('\n') }
    val horizontalScrollState = rememberScrollState()

    SelectionContainer(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.background),
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(horizontalScrollState),
        ) {
            itemsIndexed(
                items = lines,
                key = { index, _ -> index },
            ) { _, line ->
                val annotated = remember(line, searchQuery, colors) {
                    buildAnnotatedString {
                        append(line)
                        addStyle(SpanStyle(color = colors.punctuation), 0, line.length)
                        if (searchQuery.isNotBlank()) {
                            val lowerLine = line.lowercase()
                            val lowerQuery = searchQuery.lowercase()
                            var idx = lowerLine.indexOf(lowerQuery)
                            while (idx >= 0) {
                                addStyle(
                                    SpanStyle(
                                        background = colors.highlight,
                                        color = colors.highlightFg,
                                    ),
                                    idx,
                                    idx + lowerQuery.length,
                                )
                                idx = lowerLine.indexOf(lowerQuery, idx + lowerQuery.length)
                            }
                        }
                    }
                }

                Text(
                    text = annotated,
                    style = monoStyle,
                    modifier = Modifier.padding(horizontal = 12.dp),
                )
            }
        }
    }
}

// ── Previews ──
@Preview
@Composable
private fun Preview_PlainText() {
    MaterialTheme {
        PlainText(
            text = "This is plain, unparseable text content",
            searchQuery = "",
        )
    }
}

@Preview
@Composable
private fun Preview_PlainTextWithSearch() {
    MaterialTheme {
        PlainText(
            text = "This is plain, unparseable text content",
            searchQuery = "plain",
        )
    }
}
