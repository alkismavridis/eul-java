package eu.alkismavridis.euljava.parser.token

import eu.alkismavridis.euljava.core.EulLogger
import eu.alkismavridis.euljava.core.ast.expressions.tokens.StringLiteral
import java.lang.StringBuilder

class StringLiteralTokenizer(private val logger: EulLogger, private val source: CharacterSource) {
    private val builder = StringBuilder()

    fun parse(line: Int, column: Int): StringLiteral {
        this.builder.setLength(0)
        while(true) {
            val nextChar = this.source.getNextChar()
            when (nextChar) {
                '\u0000' -> throw TokenizerException(line, column, "End of file found while parsing string literal")
                '\\' -> this.addEscapeCharacter()
                '"' -> return StringLiteral(this.builder.toString(), line, column)
                else -> this.builder.append(nextChar)
            }
        }
    }

    private fun addEscapeCharacter() {
        val line = this.source.getLine()
        val column = this.source.getColumn()
        val escapeValue = this.source.getNextChar()

        when(escapeValue) {
            'n' -> this.builder.append('\n')
            't' -> this.builder.append('\t')
            'r' -> this.builder.append('\r')
            'b' -> this.builder.append('\b')
            '\\' -> this.builder.append('\\')
            '"' -> this.builder.append('"')
            '$' -> this.builder.append('$')
            else -> throw TokenizerException(line, column, "Unknown escape character $escapeValue")
        }
    }
}
