package dev.skymansandy.jsoncmp.ui.common

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.skymansandy.jsoncmp.helper.util.foldGlyphSize
import dev.skymansandy.jsoncmp.theme.LocalJsonCmpColors
import dev.skymansandy.jsoncmp.theme.monoStyle

/** Line number cell with an optional fold glyph (▶/▼). */
@Composable
internal fun GutterCell(
    modifier: Modifier = Modifier,
    lineNumber: Int,
    numDigits: Int,
    foldId: Int? = null,
    isFolded: Boolean = false,
    onFoldToggle: () -> Unit = {},
) {
    val colors = LocalJsonCmpColors.current
    val foldGlyph = remember(foldId, isFolded) {
        when {
            foldId == null -> ""
            isFolded -> "▶"
            else -> "▼"
        }
    }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = lineNumber.toString().padStart(numDigits),
            style = monoStyle,
            color = colors.lineNumber,
            modifier = Modifier.padding(
                start = 12.dp,
                end = 6.dp,
            ),
        )

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(foldGlyphSize)
                .then(
                    when {
                        foldId == null -> Modifier
                        else -> Modifier.pointerInput(foldId) {
                            detectTapGestures { onFoldToggle() }
                        }
                    },
                ),
        ) {
            if (foldId != null) {
                Text(
                    text = foldGlyph,
                    style = monoStyle,
                    color = colors.foldHint,
                    lineHeight = foldGlyphSize.value.sp,
                )
            }
        }
    }
}

// ── Previews ──

@Preview
@Composable
private fun Preview_GutterCell() {
    MaterialTheme {
        GutterCell(
            lineNumber = 2,
            numDigits = 2,
        )
    }
}

@Preview
@Composable
private fun Preview_GutterCellFoldable() {
    MaterialTheme {
        GutterCell(
            lineNumber = 5,
            numDigits = 2,
            foldId = 1,
            isFolded = false,
            onFoldToggle = {},
        )
    }
}

@Preview
@Composable
private fun Preview_GutterCellFolded() {
    MaterialTheme {
        GutterCell(
            lineNumber = 5,
            numDigits = 2,
            foldId = 1,
            isFolded = true,
            onFoldToggle = {},
        )
    }
}
