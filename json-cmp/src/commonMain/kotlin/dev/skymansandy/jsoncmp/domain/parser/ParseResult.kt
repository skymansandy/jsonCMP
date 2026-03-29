package dev.skymansandy.jsoncmp.domain.parser

import dev.skymansandy.jsoncmp.domain.line.JsonLine
import dev.skymansandy.jsoncmp.domain.model.JsonNode

/** Outcome of parsing raw JSON and building display lines. */
internal sealed interface ParseResult {

    data object Empty : ParseResult

    data class Success(
        val node: JsonNode,
        val lines: List<JsonLine>,
    ) : ParseResult

    data class Failure(val error: JsonError?) : ParseResult
}
