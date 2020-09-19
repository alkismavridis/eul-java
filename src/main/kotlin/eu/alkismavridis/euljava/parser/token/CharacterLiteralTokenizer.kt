package eu.alkismavridis.euljava.parser.token

import eu.alkismavridis.euljava.core.EulLogger
import eu.alkismavridis.euljava.core.ast.expressions.tokens.CharLiteral

class CharacterLiteralTokenizer(private val logger: EulLogger, private val source: CharacterSource) {
    fun parse(line: Int, column: Int): CharLiteral {
        val charValue = this.source.getNextChar()
        when (charValue) {
            '\'' -> return CharLiteral(0, 8, line, column)
            '\\' -> return this.parseEscapeCharacter(line, column)
            '\u0000' -> throw TokenizerException(line, column, "End of file found while parsing character")
            '\n' -> throw TokenizerException(line, column, "Illegal end of line while parsing character")

            else -> {
                this.requireClosingQuote()
                return CharLiteral(charValue.toLong(), 8, line, column)
            }
        }
    }

    private fun parseEscapeCharacter(line: Int, column: Int): CharLiteral {
        val escapeValue = this.source.getNextChar()
        when (escapeValue) {
            'n' -> {
                this.requireClosingQuote()
                return CharLiteral('\n'.toLong(), 8, line, column)
            }
            't' -> {
                this.requireClosingQuote()
                return CharLiteral('\t'.toLong(), 8, line, column)
            }
            'r' -> {
                this.requireClosingQuote()
                return CharLiteral('\r'.toLong(), 8, line, column)
            }
            'b' -> {
                this.requireClosingQuote()
                return CharLiteral('\b'.toLong(), 8, line, column)
            }
            'u' -> return this.parseUnicode(line, column)
            '\\' -> {
                this.requireClosingQuote()
                return CharLiteral('\\'.toLong(), 8, line, column)
            }
            '\'' -> {
                this.requireClosingQuote()
                return CharLiteral('\''.toLong(), 8, line, column)
            }
            '\u0000' -> {
                throw TokenizerException(line, column, "End of file found while parsing escape character")
            }
            else -> {
                throw TokenizerException(line, column, "Illegal escape character $escapeValue")
            }
        }
    }

    private fun parseUnicode(line: Int, column: Int): CharLiteral {
        val first = this.requireHexDigit()
        val second = this.requireHexDigit()
        val third = this.requireHexDigit()
        val fourth = this.requireHexDigit()

        val result = (first shl 12) or (second shl 8) or (third shl 4) or fourth
        val size = if(result <= 0xFF) 8 else 16

        this.requireClosingQuote()
        return CharLiteral(result.toLong(), size, line, column)
    }

    private fun requireClosingQuote() {
        val line = this.source.getLine()
        val column = this.source.getColumn()

        val nextChar = this.source.getNextChar()
        if (nextChar != '\'') {
            throw TokenizerException(line, column, "Expected ' but found $nextChar instead")
        }
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
