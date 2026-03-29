package dev.skymansandy.jsoncmp.domain.serializer

import dev.skymansandy.jsoncmp.domain.model.JsonNode

/**
 * Serializes this [JsonNode] tree back to a JSON string.
 *
 * @param indent number of spaces per nesting level (ignored when [compact] is true).
 * @param compact when true, emits minified JSON with no whitespace.
 */
internal fun JsonNode.toJsonString(indent: Int = 2, compact: Boolean = false): String {
    val sb = StringBuilder()
    writeNode(sb = sb, node = this, currentIndent = 0, step = indent, compact = compact)
    return sb.toString()
}

/**
 * Returns a copy of this node tree with object keys sorted alphabetically.
 *
 * @param ascending true for A-Z, false for Z-A.
 * @param recursive when true, sorts keys in nested objects and arrays too.
 */
internal fun JsonNode.sortKeys(
    ascending: Boolean = true,
    recursive: Boolean = true,
): JsonNode = when (this) {
    is JsonNode.JObject -> {
        val sorted = when {
            ascending -> fields.sortedBy { it.first }
            else -> fields.sortedByDescending { it.first }
        }

        val mapped = when {
            recursive -> sorted.map { (k, v) -> k to v.sortKeys(ascending, true) }
            else -> sorted
        }

        JsonNode.JObject(mapped)
    }

    is JsonNode.JArray -> {
        when {
            recursive -> JsonNode.JArray(elements.map { it.sortKeys(ascending, true) })
            else -> this
        }
    }

    else -> this
}

/** Core recursive writer — emits pretty-printed or compact JSON depending on [compact]. */
private fun writeNode(
    sb: StringBuilder,
    node: JsonNode,
    currentIndent: Int,
    step: Int,
    compact: Boolean,
) {
    val nl = if (compact) "" else "\n"
    val childIndent = currentIndent + step
    val indentStr = if (compact) "" else " ".repeat(childIndent)
    val closingIndentStr = if (compact) "" else " ".repeat(currentIndent)
    val colonSep = if (compact) ":" else ": "

    when (node) {
        is JsonNode.JObject -> {
            if (node.fields.isEmpty()) {
                sb.append("{}")
            } else {
                sb.append("{").append(nl)
                node.fields.forEachIndexed { i, (key, value) ->
                    sb.append(indentStr)
                    sb.append('"').append(escapeJsonString(key)).append('"')
                    sb.append(colonSep)
                    writeNode(sb, value, childIndent, step, compact)
                    if (i < node.fields.lastIndex) sb.append(",")
                    sb.append(nl)
                }
                sb.append(closingIndentStr).append("}")
            }
        }

        is JsonNode.JArray -> {
            if (node.elements.isEmpty()) {
                sb.append("[]")
            } else {
                sb.append("[").append(nl)
                node.elements.forEachIndexed { i, element ->
                    sb.append(indentStr)
                    writeNode(sb, element, childIndent, step, compact)
                    if (i < node.elements.lastIndex) sb.append(",")
                    sb.append(nl)
                }
                sb.append(closingIndentStr).append("]")
            }
        }

        is JsonNode.JString -> {
            sb.append('"').append(escapeJsonString(node.value)).append('"')
        }

        is JsonNode.JNumber -> sb.append(node.value)
        is JsonNode.JBoolean -> sb.append(node.value)
        is JsonNode.JNull -> sb.append("null")
    }
}

/** Escapes special characters (quotes, backslashes, control chars) for safe JSON string output. */
private fun escapeJsonString(s: String): String = buildString(s.length) {
    for (c in s) {
        when (c) {
            '"' -> append("\\\"")
            '\\' -> append("\\\\")
            '\n' -> append("\\n")
            '\r' -> append("\\r")
            '\t' -> append("\\t")
            '\b' -> append("\\b")
            '\u000C' -> append("\\f")
            else -> {
                if (c.code < 0x20) {
                    append("\\u${c.code.toString(16).padStart(4, '0')}")
                } else {
                    append(c)
                }
            }
        }
    }
}
