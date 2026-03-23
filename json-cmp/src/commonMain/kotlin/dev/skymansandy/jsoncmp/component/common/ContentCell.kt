package dev.skymansandy.jsoncmp.component.common

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
import dev.skymansandy.jsoncmp.helper.constants.colors.JsonCmpColors
import dev.skymansandy.jsoncmp.helper.constants.colors.partColor
import dev.skymansandy.jsoncmp.helper.constants.typography.monoStyle
import dev.skymansandy.jsoncmp.helper.mock.previewColors
import dev.skymansandy.jsoncmp.helper.mock.previewFoldableLine
import dev.skymansandy.jsoncmp.helper.mock.previewLine
import dev.skymansandy.jsoncmp.model.FoldType
import dev.skymansandy.jsoncmp.model.JsonLine

@Composable
internal fun ContentCell(
    line: JsonLine,
    isFolded: Boolean,
    searchQuery: String,
    colors: JsonCmpColors,
    onFoldToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val lineText = buildString {
        line.parts.forEach { append(it.text) }
    }

    if (isFolded && line.foldedContent.isNotEmpty()) {
        // Folded: show "{ ... }" as visible text.
        // Tap → expand fold. Long-press → copy full JSON to clipboard.
        @Suppress("DEPRECATION")
        val clipboardManager = LocalClipboardManager.current
        val openingBracket = if (line.foldType == FoldType.Object) "{" else "["
        val closingBracket = if (line.foldType == FoldType.Object) "}" else "]"
        val foldedJson = openingBracket + " " + line.foldedContent

        val prefixText = buildAnnotatedString {
            var cursor = 0
            line.parts.forEach { part ->
                append(part.text)
                addStyle(SpanStyle(color = partColor(part, colors)), cursor, cursor + part.text.length)
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

        val hasHiddenMatch = searchQuery.isNotBlank() &&
            line.foldedContent.lowercase().contains(searchQuery.lowercase())

        val ellipsisText = buildAnnotatedString {
            append("...")
            val ellipsisStyle = if (hasHiddenMatch) {
                SpanStyle(background = colors.highlight, color = colors.highlightFg)
            } else {
                SpanStyle(color = colors.foldEllipsis)
            }
            addStyle(ellipsisStyle, 0, length)
        }

        val bracketText = buildAnnotatedString {
            append(closingBracket)
            addStyle(SpanStyle(color = colors.punctuation), 0, length)
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = modifier
                .background(colors.background)
                .padding(start = 8.dp, end = 16.dp),
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
        val styledText = buildAnnotatedString {
            append(lineText)
            var cursor = 0
            line.parts.forEach { part ->
                addStyle(SpanStyle(color = partColor(part, colors)), cursor, cursor + part.text.length)
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
private fun Preview_ContentCell() {
    MaterialTheme {
        ContentCell(
            line = previewLine,
            isFolded = false,
            searchQuery = "",
            colors = previewColors,
            onFoldToggle = {},
        )
    }
}

@Preview
@Composable
private fun Preview_ContentCellFolded() {
    MaterialTheme {
        ContentCell(
            line = previewFoldableLine,
            isFolded = true,
            searchQuery = "",
            colors = previewColors,
            onFoldToggle = {},
        )
    }
}

@Preview
@Composable
private fun Preview_ContentCellWithSearch() {
    MaterialTheme {
        ContentCell(
            line = previewLine,
            isFolded = false,
            searchQuery = "John",
            colors = previewColors,
            onFoldToggle = {},
        )
    }
}
