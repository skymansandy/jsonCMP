/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.jsoncmp.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.DisableSelection
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.skymansandy.jsoncmp.domain.line.JsonLine
import dev.skymansandy.jsoncmp.helper.mocks.previewFoldableLine
import dev.skymansandy.jsoncmp.helper.mocks.previewLine
import dev.skymansandy.jsoncmp.theme.JsonCmpColors
import dev.skymansandy.jsoncmp.theme.LocalJsonCmpColors
import dev.skymansandy.jsoncmp.theme.monoStyle
import dev.skymansandy.jsoncmp.theme.partColor

/** Renders a single JSON line with syntax colouring, search highlighting, and fold support. */
@Composable
internal fun JsonLineView(
    modifier: Modifier = Modifier,
    line: JsonLine,
    isFolded: Boolean,
    searchQuery: String,
    onFoldToggle: () -> Unit,
    foldedContentProvider: () -> String = { "" },
    hasHiddenMatchProvider: () -> Boolean = { false },
) {
    val colors = LocalJsonCmpColors.current

    if (isFolded && line.foldId != null) {
        // Folded: show "{ ... }" as visible text.
        // Tap → expand fold. Long-press → copy full JSON to clipboard.
        @Suppress("DEPRECATION")
        val clipboardManager = LocalClipboardManager.current
        val openingBracket = line.foldType?.opening
        val closingBracket = line.foldType?.closing

        val prefixText = remember(line, searchQuery, colors) {
            buildAnnotatedString {
                var cursor = 0
                line.parts.forEach { part ->
                    append(part.text)
                    addStyle(
                        SpanStyle(color = partColor(part, colors)),
                        cursor,
                        cursor + part.text.length,
                    )
                    cursor += part.text.length
                }
                if (searchQuery.isNotBlank()) {
                    val queryLower = searchQuery.lowercase()
                    val lower = toAnnotatedString().text.lowercase()
                    var idx = lower.indexOf(queryLower)
                    while (idx >= 0) {
                        addStyle(
                            SpanStyle(background = colors.highlight, color = colors.highlightFg),
                            start = idx,
                            end = idx + queryLower.length,
                        )
                        idx = lower.indexOf(queryLower, idx + queryLower.length)
                    }
                }
            }
        }

        val hasHiddenMatch = remember(line, searchQuery) {
            hasHiddenMatchProvider()
        }

        val ellipsisText = remember(hasHiddenMatch, colors) {
            buildAnnotatedString {
                append("...")
                val ellipsisStyle = if (hasHiddenMatch) {
                    SpanStyle(background = colors.highlight, color = colors.highlightFg)
                } else {
                    SpanStyle(color = colors.foldEllipsis)
                }
                addStyle(ellipsisStyle, 0, length)
            }
        }

        val bracketText = remember(closingBracket, colors) {
            buildAnnotatedString {
                append(closingBracket)
                addStyle(SpanStyle(color = colors.punctuation), 0, length)
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = modifier.padding(start = 8.dp, end = 16.dp),
        ) {
            Text(
                text = prefixText,
                style = monoStyle,
                softWrap = false,
                overflow = TextOverflow.Clip,
            )

            DisableSelection {
                Text(
                    text = ellipsisText,
                    style = monoStyle,
                    softWrap = false,
                    modifier = Modifier
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                        .pointerInput(line.foldId) {
                            detectTapGestures(
                                onTap = { onFoldToggle() },
                                onLongPress = {
                                    val foldedJson =
                                        (openingBracket + " " + foldedContentProvider()).trimEnd(
                                            ',',
                                            ' ',
                                        ) + " " + closingBracket
                                    clipboardManager.setText(AnnotatedString(foldedJson))
                                },
                            )
                        }
                        .background(
                            color = colors.foldEllipsis.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(4.dp),
                        )
                        .border(
                            width = 1.dp,
                            color = colors.foldEllipsis.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(4.dp),
                        )
                        .padding(horizontal = 4.dp),
                )
            }

            Text(
                text = bracketText,
                style = monoStyle,
                softWrap = false,
            )
        }
    } else {
        // Expanded: render all parts with syntax colors and optional search highlights
        val lineText = remember(line) {
            buildString { line.parts.forEach { append(it.text) } }
        }

        val styledText = remember(line, searchQuery, colors) {
            buildAnnotatedString {
                append(lineText)
                var cursor = 0
                line.parts.forEach { part ->
                    addStyle(
                        SpanStyle(color = partColor(part, colors)),
                        cursor,
                        cursor + part.text.length,
                    )
                    cursor += part.text.length
                }
                if (searchQuery.isNotBlank()) {
                    val lower = lineText.lowercase()
                    val queryLower = searchQuery.lowercase()
                    var idx = lower.indexOf(queryLower)
                    while (idx >= 0) {
                        addStyle(
                            SpanStyle(background = colors.highlight, color = colors.highlightFg),
                            start = idx,
                            end = idx + queryLower.length,
                        )
                        idx = lower.indexOf(queryLower, idx + queryLower.length)
                    }
                }
            }
        }

        Text(
            text = styledText,
            style = monoStyle,
            softWrap = false,
            overflow = TextOverflow.Clip,
            modifier = modifier
                .background(colors.background)
                .padding(
                    start = 8.dp,
                    end = 16.dp,
                ),
        )
    }
}

// ── Previews ──
@Preview
@Composable
private fun Preview_JsonLineView() {
    MaterialTheme {
        CompositionLocalProvider(LocalJsonCmpColors provides JsonCmpColors.Dark) {
            JsonLineView(
                line = previewLine,
                isFolded = false,
                searchQuery = "",
                onFoldToggle = {},
            )
        }
    }
}

@Preview
@Composable
private fun Preview_JsonLineViewFolded() {
    MaterialTheme {
        CompositionLocalProvider(LocalJsonCmpColors provides JsonCmpColors.Dark) {
            JsonLineView(
                line = previewFoldableLine,
                isFolded = true,
                searchQuery = "",
                onFoldToggle = {},
            )
        }
    }
}

@Preview
@Composable
private fun Preview_JsonLineViewWithSearch() {
    MaterialTheme {
        CompositionLocalProvider(LocalJsonCmpColors provides JsonCmpColors.Dark) {
            JsonLineView(
                line = previewLine,
                isFolded = false,
                searchQuery = "John",
                onFoldToggle = {},
            )
        }
    }
}
