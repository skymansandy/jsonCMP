package dev.skymansandy.jsoncmp.helper.mock

import dev.skymansandy.jsoncmp.helper.constants.colors.JsonCmpColors
import dev.skymansandy.jsoncmp.model.FoldType
import dev.skymansandy.jsoncmp.model.JsonLine
import dev.skymansandy.jsoncmp.model.JsonPart

internal val previewColors = JsonCmpColors.Dark

internal val previewLines = listOf(
    JsonLine(
        lineNumber = 1,
        depth = 0,
        parts = listOf(JsonPart.Punct("{")),
        foldId = 0,
        foldType = FoldType.Object,
        parentFoldIds = emptyList(),
        foldChildCount = 3,
    ),
    JsonLine(
        lineNumber = 2,
        depth = 1,
        parts = listOf(
            JsonPart.Indent("    "),
            JsonPart.Key("\"name\""),
            JsonPart.Punct(": "),
            JsonPart.StrVal("\"John Doe\""),
        ),
        foldId = null,
        foldType = null,
        parentFoldIds = listOf(0),
    ),
    JsonLine(
        lineNumber = 3,
        depth = 0,
        parts = listOf(JsonPart.Punct("}")),
        foldId = null,
        foldType = null,
        parentFoldIds = emptyList(),
    ),
)
