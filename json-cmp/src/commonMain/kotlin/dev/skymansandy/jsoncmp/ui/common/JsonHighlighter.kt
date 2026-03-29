package dev.skymansandy.jsoncmp.ui.common

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import dev.skymansandy.jsoncmp.domain.highlighter.Token
import dev.skymansandy.jsoncmp.domain.highlighter.TokenType
import dev.skymansandy.jsoncmp.theme.JsonCmpColors

/** Tokenises [text] and returns an [AnnotatedString] with syntax colors and search highlights. */
internal fun highlightJson(
    text: String,
    searchQuery: String,
    colors: JsonCmpColors,
): AnnotatedString = buildAnnotatedString {

    // Base layer: default all text to punctuation color, then overlay token-specific colors
    append(text)
    addStyle(SpanStyle(color = colors.punctuation), 0, text.length)

    val tokens = tokenizeJson(text)
    for (token in tokens) {
        val color = when (token.type) {
            TokenType.Key -> colors.key
            TokenType.String -> colors.string
            TokenType.Number -> colors.number
            TokenType.Boolean -> colors.booleanColor
            TokenType.Null -> colors.nullColor
            TokenType.Punctuation -> colors.punctuation
        }
        addStyle(SpanStyle(color = color), token.start, token.end)
    }

    // Overlay search highlights on top of syntax colors (case-insensitive)
    if (searchQuery.isNotBlank()) {
        val lowerText = text.lowercase()
        val lowerQuery = searchQuery.lowercase()
        var idx = lowerText.indexOf(lowerQuery)
        while (idx >= 0) {
            addStyle(
                SpanStyle(background = colors.highlight, color = colors.highlightFg),
                idx,
                idx + lowerQuery.length,
            )
            idx = lowerText.indexOf(lowerQuery, idx + lowerQuery.length)
        }
    }
}

/** Single-pass lexer that produces [Token]s for JSON syntax elements. Handles escaped strings,
 *  integers/decimals/scientific notation, boolean/null literals, and structural punctuation. */
@Suppress("LongMethod", "CyclomaticComplexMethod")
private fun tokenizeJson(text: String): List<Token> {
    val tokens = mutableListOf<Token>()
    var pos = 0
    val len = text.length

    while (pos < len) {
        val c = text[pos]
        when {
            c == '"' -> {
                val start = pos
                pos++
                while (pos < len) {
                    when (text[pos]) {
                        '\\' -> pos += 2
                        '"' -> {
                            pos++
                            break
                        }

                        else -> pos++
                    }
                }
                // A quoted string followed by ':' is a key, otherwise a value
                val isKey = isFollowedByColon(text, pos, len)
                tokens += Token(if (isKey) TokenType.Key else TokenType.String, start, pos)
            }

            c == '-' || c.isDigit() -> {
                val start = pos
                if (c == '-') pos++
                while (pos < len && text[pos].isDigit()) pos++
                if (pos < len && text[pos] == '.') {
                    pos++
                    while (pos < len && text[pos].isDigit()) pos++
                }
                if (pos < len && (text[pos] == 'e' || text[pos] == 'E')) {
                    pos++
                    if (pos < len && (text[pos] == '+' || text[pos] == '-')) pos++
                    while (pos < len && text[pos].isDigit()) pos++
                }
                tokens += Token(TokenType.Number, start, pos)
            }

            text.startsWith(
                "true",
                pos,
            ) && (pos + 4 >= len || !text[pos + 4].isLetterOrDigit()) -> {
                tokens += Token(TokenType.Boolean, pos, pos + 4)
                pos += 4
            }

            text.startsWith(
                "false",
                pos,
            ) && (pos + 5 >= len || !text[pos + 5].isLetterOrDigit()) -> {
                tokens += Token(TokenType.Boolean, pos, pos + 5)
                pos += 5
            }

            text.startsWith(
                "null",
                pos,
            ) && (pos + 4 >= len || !text[pos + 4].isLetterOrDigit()) -> {
                tokens += Token(TokenType.Null, pos, pos + 4)
                pos += 4
            }

            c == '{' || c == '}' || c == '[' || c == ']' || c == ':' || c == ',' -> {
                tokens += Token(TokenType.Punctuation, pos, pos + 1)
                pos++
            }

            else -> pos++
        }
    }
    return tokens
}

/** Skips whitespace after [from] and checks for ':', used to distinguish keys from string values. */
private fun isFollowedByColon(text: String, from: Int, len: Int): Boolean {
    var i = from
    while (i < len && text[i].isWhitespace()) i++
    return i < len && text[i] == ':'
}
