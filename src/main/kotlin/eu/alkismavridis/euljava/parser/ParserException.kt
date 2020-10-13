package eu.alkismavridis.euljava.parser

import eu.alkismavridis.euljava.core.ast.EulToken
import java.lang.Exception

class ParserException(val line: Int, val column: Int, message: String) : Exception(message) {
    companion object {
        fun of(token: EulToken, message: String) = ParserException(token.line, token.column, message)
        fun eof(message: String) = ParserException(-1, -1, message)
    }
}
