/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.jsoncmp.domain.model

/**
 * Ordered path from the JSON root to a specific node.
 *
 * Example: for `{"users": [{"name": "Alice"}]}`, the path to `"Alice"` is
 * `[Key("users"), Index(0), Key("name")]`.
 */
internal typealias JsonPath = List<PathSegment>

/** A single segment in a [JsonPath] — either an object key or an array index. */
internal sealed interface PathSegment {
    data class Key(val name: String) : PathSegment
    data class Index(val idx: Int) : PathSegment
}
