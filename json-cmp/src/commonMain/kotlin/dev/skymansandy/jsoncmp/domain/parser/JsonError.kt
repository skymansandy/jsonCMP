package dev.skymansandy.jsoncmp.domain.parser

/** Describes a JSON parse failure with an optional character position. */
data class JsonError(
    val message: String,
    val position: Int? = null,
)
