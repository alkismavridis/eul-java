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
            'u' -> this.parseUnicode()
            '\\' -> this.builder.append('\\')
            '"' -> this.builder.append('"')
            '$' -> this.builder.append('$')
            else -> throw TokenizerException(line, column, "Unknown escape character $escapeValue")
        }
    }


    private fun parseUnicode() {
        val first = this.requireHexDigit()
        val second = this.requireHexDigit()
        val third = this.requireHexDigit()
        val fourth = this.requireHexDigit()

        val result = (first shl 12) or (second shl 8) or (third shl 4) or fourth
        this.builder.append(result.toChar())
    }


    private fun requireHexDigit(): Int {
        val line = this.source.getLine()
        val column = this.source.getColumn()

        val nextChar = this.source.getNextChar()
        if (nextChar in '0'..'9') return nextChar.toInt() - 48
        else if (nextChar in 'a'..'f') return nextChar.toInt() - 87
        else if (nextChar in 'A'..'F') return nextChar.toInt() - 55
        else if (nextChar == '\u0000') throw TokenizerException(line, column, "End of file found while parsing unicode literal")
        throw TokenizerException(line, column, "Expected hex digit but found $nextChar instead")
    }
}
