/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.jsoncmp.helper.mocks

import dev.skymansandy.jsoncmp.domain.line.JsonLine
import dev.skymansandy.jsoncmp.domain.line.JsonPart
import dev.skymansandy.jsoncmp.domain.model.FoldType
import dev.skymansandy.jsoncmp.theme.JsonCmpColors

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

internal val previewLine = JsonLine(
    lineNumber = 2,
    depth = 1,
    parts = listOf(
        JsonPart.Indent("    "),
        JsonPart.Key("\"name\""),
        JsonPart.Punct(": "),
        JsonPart.StrVal("\"John Doe\""),
        JsonPart.Punct(","),
    ),
    foldId = null,
    foldType = null,
    parentFoldIds = emptyList(),
)

internal val previewFoldableLine = JsonLine(
    lineNumber = 5,
    depth = 1,
    parts = listOf(
        JsonPart.Indent("    "),
        JsonPart.Key("\"address\""),
        JsonPart.Punct(": "),
        JsonPart.Punct("{"),
    ),
    foldId = 1,
    foldType = FoldType.Object,
    parentFoldIds = emptyList(),
    foldChildCount = 3,
)

internal val previewJson = """
{
    "name": "John Doe",
    "age": 30,
    "isActive": true
}
""".trimIndent()
