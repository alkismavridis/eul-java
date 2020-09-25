package eu.alkismavridis.euljava.parser.token

import eu.alkismavridis.euljava.core.CompileOptions
import eu.alkismavridis.euljava.core.EulLogger
import eu.alkismavridis.euljava.core.ast.EulToken
import eu.alkismavridis.euljava.core.ast.operators.EulCommentToken
import eu.alkismavridis.euljava.core.ast.operators.NewLineToken
import java.io.Reader


class TokenizerException(val line: Int, val column: Int, message: String) : Exception(message)

class EulTokenizer(
    private val reader: Reader,
    logger: EulLogger,
    options: CompileOptions
) : CharacterSource {
    private val numberTokenizer = NumberTokenizer(logger, this, options)
    private val wordTokenizer = WordTokenizer(logger, this)
    private val specialCharacterTokenizer = SpecialCharacterTokenizer(logger, this)
    private val characterLiteralTokenizer = CharacterLiteralTokenizer(logger, this)
    private val stringLiteralTokenizer = StringLiteralTokenizer(logger, this)

    // state
    private var line = 1
    private var column = 1
    private var rolledBackCharacter: Char = '\u0000'


    /// API
    override fun getLine() = this.line
    override fun getColumn() = this.column

    fun getNextToken(skipNewLines: Boolean): EulToken? {
        val nextNonWhite =
            if (skipNewLines) this.readNextNonWhiteAndNotNl()
            else this.readNextNonWhite()

        if (nextNonWhite == '\u0000') {
            return null
        } else if (nextNonWhite == '\n') {
            return NewLineToken(this.line - 1, -1)
        } else if (CharMetadata.isWordStart(nextNonWhite)) {
            this.rollBackCharacter(nextNonWhite)
            return this.wordTokenizer.getNextWordToken(this.line, this.column)
        } else if (CharMetadata.isDecimalDigit(nextNonWhite)) {
            this.rollBackCharacter(nextNonWhite)
            return this.numberTokenizer.getNextNumberToken(this.line, this.column)
        } else if (nextNonWhite == '\'') {
            return this.characterLiteralTokenizer.parse(this.line, this.column - 1)
        } else if (nextNonWhite == '"') {
            return this.stringLiteralTokenizer.parse(this.line, this.column - 1)
        }
        // TODO add  support for back-quotes ``


        val asSpecialCharacter = this.specialCharacterTokenizer.parse(nextNonWhite, this.line, this.column - 1)
        if (asSpecialCharacter != null) return asSpecialCharacter

        throw TokenizerException(this.line, this.column, "Unknown token: $nextNonWhite")
    }

    fun requireNextToken(skipNewLines: Boolean): EulToken {
        return this.getNextToken(skipNewLines)
            ?: throw TokenizerException(this.line, this.column, "End of file found while parsing")
    }

    fun getNextNonCommentToken(skipNewLines: Boolean): EulToken? {
        while (true) {
            val nextToken = this.getNextToken(skipNewLines)
            if (nextToken !is EulCommentToken) return nextToken
        }
    }

    fun requireNextNonCommentToken(skipNewLines: Boolean): EulToken? {
        while (true) {
            val nextToken = this.getNextToken(skipNewLines)
                ?: throw TokenizerException(this.line, this.column, "End of file found while parsing")

            if (nextToken !is EulCommentToken) {
                return nextToken
            }
        }
    }


    /// CHAR READING
    override fun getNextChar(): Char {
        if (this.rolledBackCharacter != '\u0000') {
            val ret = this.rolledBackCharacter
            this.rolledBackCharacter = '\u0000'

            this.progressIndex(ret)
            return ret
        }

        val nextByte = this.reader.read()
        if (nextByte == -1) return '\u0000'

        val ret = nextByte.toChar()
        this.progressIndex(ret)
        return ret
    }

    override fun rollBackCharacter(ch: Char) {
        this.rolledBackCharacter = ch

        if (ch == '\n') {
            this.line--
            this.column = -1 // -1 indicates end of line
        } else {
            this.column--
        }
    }

    private fun progressIndex(char: Char) {
        if (char == '\n') {
            this.line++
            this.column = 1
        } else {
            this.column++
        }
    }

    private fun readNextNonWhite(): Char {
        while (true) {
            val nextChar = this.getNextChar()
            if (!CharMetadata.isWhiteSpace(nextChar)) {
                return nextChar
            }
        }
    }

    private fun readNextNonWhiteAndNotNl(): Char {
        while (true) {
            val nextChar = this.getNextChar()
            if (nextChar != '\n' && !CharMetadata.isWhiteSpace(nextChar)) {
                return nextChar
            }
        }
    }
}
