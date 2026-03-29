package dev.skymansandy.jsoncmp.domain.highlighter

/** Classification of a JSON syntax token for highlighting. */
internal enum class TokenType {
    Key,
    String,
    Number,
    Boolean,
    Null,
    Punctuation,
}
