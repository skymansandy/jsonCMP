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

    val (node, err) = parseJsonResult(trimmed)
    if (node != null) {
        ParseResult.Success(node, buildDisplayLines(node))
    } else {
        ParseResult.Failure(err)
    }
}

/** Parses [input] into a [JsonNode] tree, returning (node, null) on success or (null, error) on failure. */
private suspend fun parseJsonResult(
    input: String,
): Pair<JsonNode?, JsonError?> = withContext(Dispatchers.Default) {
    try {
        val trimmed = input.trim()
        if (trimmed.isEmpty()) {
            null to JsonError("Empty input")
        } else {
            val element = json.parseToJsonElement(trimmed)
            element.toJsonNode() to null
        }
    } catch (e: Exception) {
        null to JsonError(e.message ?: "Invalid JSON")
    }
}

private suspend fun JsonElement.toJsonNode(): JsonNode = withContext(Dispatchers.Default) {
    when (this@toJsonNode) {
        is JsonObject -> JsonNode.JObject(entries.map { (key, value) -> key to value.toJsonNode() })
        is JsonArray -> JsonNode.JArray(map { it.toJsonNode() })
        JsonNull -> JsonNode.JNull
        is JsonPrimitive -> when {
            isString -> JsonNode.JString(content)
            booleanOrNull != null -> JsonNode.JBoolean(boolean)
            else -> JsonNode.JNumber(content)
        }
    }
}
