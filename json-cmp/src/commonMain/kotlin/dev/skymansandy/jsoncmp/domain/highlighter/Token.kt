package dev.skymansandy.jsoncmp.domain.highlighter

/** A highlighted span within the raw JSON text. */
internal data class Token(
    val type: TokenType,
    val start: Int,
    val end: Int,
)
