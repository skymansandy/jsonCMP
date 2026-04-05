/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.jsoncmp.domain.line

import dev.skymansandy.jsoncmp.domain.model.FoldType
import dev.skymansandy.jsoncmp.domain.model.JsonNode
import dev.skymansandy.jsoncmp.domain.model.JsonPath
import dev.skymansandy.jsoncmp.domain.model.PathSegment

/**
 * Converts a [JsonNode] tree into a flat list of [JsonLine]s for display.
 *
 * The builder walks the tree depth-first, emitting one [JsonLine] per visual line
 * (opening brackets, key-value pairs, closing brackets). Foldable containers
 * (non-empty objects/arrays) are assigned a unique [foldId] so the UI can
 * collapse/expand them independently.
 */
internal class JsonLineBuilder {

    private val out = mutableListOf<JsonLine>()
    private var lineNum = 0
    private var nextFoldId = 0

    /** Pre-computed indent parts to avoid repeated string allocation. */
    private val indentCache = ArrayList<List<JsonPart>>()

    /**
     * Tracks the index in [out] where each foldable header line was emitted.
     * After the full tree walk, a post-pass uses these to compute [JsonLine.childEndIndex]
     * for O(1) fold-skipping in the visible-line builder.
     */
    private val foldHeaders = mutableListOf<Int>()

    /** Walks the tree and returns the complete flat line list with fold metadata attached. */
    fun build(root: JsonNode): List<JsonLine> {
        addNode(
            root,
            key = null,
            isLast = true,
            depth = 0,
            parentFoldIds = emptyList(),
            path = emptyList(),
        )

        // Post-pass: for each fold header, scan forward to find where its children end.
        // A child belongs to a fold if its parentFoldIds contains the fold's id.
        // The first line that does NOT contain the foldId marks the boundary (the closing bracket).
        // childEndIndex points one past the closing bracket so the viewer can skip the entire block.
        for (headerIdx in foldHeaders) {
            val header = out[headerIdx]
            val foldId = header.foldId ?: continue
            var endIdx = headerIdx + 1
            while (endIdx < out.size && foldId in out[endIdx].parentFoldIds) {
                endIdx++
            }
            out[headerIdx] = header.copy(childEndIndex = endIdx)
        }

        return out
    }

    /**
     * Recursively emits lines for [node] and its children.
     *
     * @param key Object key if this node is a value in a parent object, null for root or array elements.
     * @param isLast True if this is the last sibling — suppresses the trailing comma.
     * @param depth Nesting level, used for indentation (2 spaces per level).
     * @param parentFoldIds Fold IDs of all ancestor containers — used to determine which
     *   folds a line belongs to, enabling collapse to hide the right lines.
     * @param path JSON path segments from root to this node, e.g. [Key("user"), Key("name")].
     */
    @Suppress("LongMethod")
    private fun addNode(
        node: JsonNode,
        key: String?,
        isLast: Boolean,
        depth: Int,
        parentFoldIds: List<Int>,
        path: JsonPath,
    ) {
        // Common parts shared by all node types — indent is cached to avoid repeated allocation
        val indent: List<JsonPart> = indentForDepth(depth)
        val keyParts: List<JsonPart> = if (key != null) {
            listOf(JsonPart.Key("\"$key\""), JsonPart.Punct(": "))
        } else emptyList()
        val comma: List<JsonPart> = if (!isLast) listOf(JsonPart.Punct(",")) else emptyList()

        when (node) {
            is JsonNode.JObject -> {
                // Empty objects render as a single "{}" line — no fold needed
                if (node.fields.isEmpty()) {
                    out += JsonLine(
                        ++lineNum, depth, indent + keyParts + JsonPart.Punct("{}") + comma,
                        null, null, parentFoldIds, path = path,
                    )
                } else {
                    // Assign a fold ID and record this header's position for the post-pass
                    val myId = nextFoldId++
                    val headerIdx = out.size
                    out += JsonLine(
                        ++lineNum, depth, indent + keyParts + JsonPart.Punct("{"),
                        myId, FoldType.Object, parentFoldIds,
                        foldChildCount = node.fields.size, path = path,
                    )
                    foldHeaders += headerIdx

                    // Children inherit all ancestor fold IDs plus this container's ID
                    val childParents = parentFoldIds + myId
                    node.fields.forEachIndexed { i, (k, v) ->
                        addNode(
                            v, k, i == node.fields.lastIndex, depth + 1,
                            childParents, path = path + PathSegment.Key(k),
                        )
                    }

                    // Closing bracket — belongs to the same fold group as children
                    out += JsonLine(
                        ++lineNum, depth, indent + listOf(JsonPart.Punct("}")) + comma,
                        null, null, childParents, isClosingBracket = true, path = path,
                    )
                }
            }

            is JsonNode.JArray -> {
                // Empty arrays render as a single "[]" line — no fold needed
                if (node.elements.isEmpty()) {
                    out += JsonLine(
                        ++lineNum, depth, indent + keyParts + JsonPart.Punct("[]") + comma,
                        null, null, parentFoldIds, path = path,
                    )
                } else {
                    val myId = nextFoldId++
                    val headerIdx = out.size
                    out += JsonLine(
                        ++lineNum, depth, indent + keyParts + JsonPart.Punct("["),
                        myId, FoldType.Array, parentFoldIds,
                        foldChildCount = node.elements.size, path = path,
                    )
                    foldHeaders += headerIdx

                    val childParents = parentFoldIds + myId
                    node.elements.forEachIndexed { i, v ->
                        addNode(
                            v, null, i == node.elements.lastIndex, depth + 1,
                            childParents, path = path + PathSegment.Index(i),
                        )
                    }

                    out += JsonLine(
                        ++lineNum, depth, indent + listOf(JsonPart.Punct("]")) + comma,
                        null, null, childParents, isClosingBracket = true, path = path,
                    )
                }
            }

            // Leaf nodes: no fold, no children — just emit a single line

            is JsonNode.JString -> {
                // Escape special characters for display (keeps the visual output valid JSON)
                val escaped = node.value
                    .replace("\\", "\\\\").replace("\"", "\\\"")
                    .replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t")
                out += JsonLine(
                    ++lineNum, depth, indent + keyParts + JsonPart.StrVal("\"$escaped\"") + comma,
                    null, null, parentFoldIds, path = path,
                )
            }

            is JsonNode.JNumber ->
                out += JsonLine(
                    ++lineNum, depth, indent + keyParts + JsonPart.NumVal(node.value) + comma,
                    null, null, parentFoldIds, path = path,
                )

            is JsonNode.JBoolean ->
                out += JsonLine(
                    ++lineNum,
                    depth,
                    indent + keyParts + JsonPart.BoolVal(node.value.toString()) + comma,
                    null,
                    null,
                    parentFoldIds,
                    path = path,
                )

            is JsonNode.JNull ->
                out += JsonLine(
                    ++lineNum, depth, indent + keyParts + JsonPart.NullVal("null") + comma,
                    null, null, parentFoldIds, path = path,
                )
        }
    }

    private fun indentForDepth(depth: Int): List<JsonPart> {
        if (depth == 0) return emptyList()
        // Grow cache on demand
        while (indentCache.size < depth) {
            indentCache.add(listOf(JsonPart.Indent("  ".repeat(indentCache.size + 1))))
        }
        return indentCache[depth - 1]
    }
}

/** Convenience entry point — creates a builder, walks the tree, and returns the flat line list. */
internal fun buildDisplayLines(root: JsonNode): List<JsonLine> {
    return JsonLineBuilder().build(root)
}
