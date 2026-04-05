/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.jsoncmp.domain.parser

import dev.skymansandy.jsoncmp.domain.line.buildDisplayLines
import dev.skymansandy.jsoncmp.domain.model.JsonNode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.booleanOrNull

/** Lenient parser that tolerates trailing commas, comments, and special floats. */
@OptIn(ExperimentalSerializationApi::class)
private val json = Json {
    isLenient = true
    allowTrailingComma = true
    allowComments = true
    allowSpecialFloatingPointValues = true
}

/** Main-safe: parses raw JSON and builds display lines on [Dispatchers.Default]. */
internal suspend fun parseAndBuildLines(
    raw: String,
): ParseResult = withContext(Dispatchers.Default) {
    val trimmed = raw.trim()
    if (trimmed.isEmpty()) return@withContext ParseResult.Empty

    try {
        val element = json.parseToJsonElement(trimmed)
        val node = element.toJsonNode()
        ParseResult.Success(node, buildDisplayLines(node))
    } catch (e: Exception) {
        ParseResult.Failure(JsonError(e.message ?: "Invalid JSON"))
    }
}

/** Converts a kotlinx.serialization [JsonElement] tree to our [JsonNode] tree — plain function, no dispatch overhead. */
private fun JsonElement.toJsonNode(): JsonNode = when (this) {
    is JsonObject -> JsonNode.JObject(entries.map { (key, value) -> key to value.toJsonNode() })
    is JsonArray -> JsonNode.JArray(map { it.toJsonNode() })
    JsonNull -> JsonNode.JNull
    is JsonPrimitive -> when {
        isString -> JsonNode.JString(content)
        booleanOrNull != null -> JsonNode.JBoolean(boolean)
        else -> JsonNode.JNumber(content)
    }
}
