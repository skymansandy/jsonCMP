/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.jsoncmp.ui.editor.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.skymansandy.jsoncmp.theme.JsonCmpColors
import dev.skymansandy.jsoncmp.theme.LocalJsonCmpColors
import dev.skymansandy.jsoncmp.theme.monoStyle

/** A tappable label row used inside the sort bottom sheet. */
@Composable
internal fun SortOption(
    modifier: Modifier = Modifier,
    label: String,
    onClick: () -> Unit,
) {
    val colors = LocalJsonCmpColors.current

    Text(
        text = label,
        style = monoStyle.copy(fontSize = 13.sp),
        color = colors.punctuation,
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp),
    )
}

// ── Previews ──

@Preview
@Composable
private fun Preview_SortOption() {
    MaterialTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(JsonCmpColors.Dark.gutterBackground),
        ) {
            SortOption(
                modifier = Modifier.fillMaxWidth(),
                label = "Sort Ascending (A \u2192 Z)",
                onClick = {},
            )
        }
    }
}
