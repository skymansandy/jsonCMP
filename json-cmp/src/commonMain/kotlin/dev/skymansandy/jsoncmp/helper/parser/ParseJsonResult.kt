package dev.skymansandy.jsoncmp.helper.parser

import dev.skymansandy.jsoncmp.model.JsonNode
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.booleanOrNull

@OptIn(ExperimentalSerializationApi::class)
private val json = Json {
    isLenient = true
    allowTrailingComma = true
    allowComments = true
    allowSpecialFloatingPointValues = true
}

internal fun parseJsonResult(input: String): Pair<JsonNode?, JsonError?> {
    return try {
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
