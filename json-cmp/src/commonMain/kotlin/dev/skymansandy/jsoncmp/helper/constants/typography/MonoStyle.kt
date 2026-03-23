package dev.skymansandy.jsoncmp.helper.constants.typography

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.sp

// Single style used by both view-mode Text cells and edit-mode BasicTextField so that
// line heights are identical when switching modes.
// LineHeightStyle.Trim.None ensures the lineHeight is fully respected even for single-line
// Text composables (the default trims leading on first / last lines).
internal val monoStyle = TextStyle(
    fontFamily = FontFamily.Monospace,
    fontSize = 12.sp,
    lineHeight = 24.sp,
    lineHeightStyle = LineHeightStyle(
        alignment = LineHeightStyle.Alignment.Center,
        trim = LineHeightStyle.Trim.None,
    ),
)
