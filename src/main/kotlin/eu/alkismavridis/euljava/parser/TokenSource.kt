package eu.alkismavridis.euljava.parser

import eu.alkismavridis.euljava.core.CompileOptions
import eu.alkismavridis.euljava.core.EulLogger
import eu.alkismavridis.euljava.core.ast.EulToken
import eu.alkismavridis.euljava.core.ast.expressions.tokens.EulReference
import eu.alkismavridis.euljava.core.ast.keywords.KeywordToken
import eu.alkismavridis.euljava.core.ast.keywords.KeywordType
import eu.alkismavridis.euljava.core.ast.operators.SpecialCharType
import eu.alkismavridis.euljava.core.ast.operators.SpecialCharacterToken
import eu.alkismavridis.euljava.parser.token.EulTokenizer
import java.io.Reader
import java.util.ArrayDeque

class TokenSource(reader: Reader, private val logger: EulLogger, private val options: CompileOptions) {
    private val tokenizer = EulTokenizer(reader, logger, options)
    private var rolledBackTokens = ArrayDeque<EulToken>()


    fun getNextToken(skipNewLines: Boolean): EulToken? {
        return if (this.rolledBackTokens.isEmpty()) this.tokenizer.getNextNonCommentToken(skipNewLines)
        else this.rolledBackTokens.pop()
    }

    fun requireNextToken(skipNewLines: Boolean, eofMessage: String): EulToken {
        if (this.rolledBackTokens.isEmpty()) {
            return this.tokenizer.getNextNonCommentToken(skipNewLines) ?: throw ParserException.eof(eofMessage)
        }

        return this.rolledBackTokens.pop()
    }

    fun rollBackToken(token: EulToken) {
        this.rolledBackTokens.push(token)
    }


    /// SPECIAL TOKEN REQUESTS
    fun requireReference(skipNewLines: Boolean, labelOfExpected: String) : EulReference {
        val nextToken = this.getNextToken(skipNewLines)
                ?: throw ParserException.eof("Expected $labelOfExpected but end of file was found")

        if (nextToken !is EulReference) {
            throw ParserException.of(nextToken, "Expected $labelOfExpected but ${nextToken.javaClass.simpleName} was found")
        }

        return nextToken
    }

    fun requireKeyword(skipNewLines: Boolean, type: KeywordType, labelOfExpected: String) : KeywordToken {
        val nextToken = this.getNextToken(true)
                ?: throw ParserException.eof("Expected $labelOfExpected but end of file was found")

        if (nextToken !is KeywordToken) {
            throw ParserException.of(nextToken, "Expected $labelOfExpected but ${nextToken.javaClass.simpleName} was found")
        } else if (nextToken.getKeywordType() != type) {
            throw ParserException.of(nextToken, "Expected $labelOfExpected but ${nextToken.getKeywordType().name} was found")
        }

        return nextToken
    }

    fun requireSpecialChar(skipNewLines: Boolean, type: SpecialCharType, labelOfExpected: String) : SpecialCharacterToken {
        val nextToken = this.getNextToken(true)
                ?: throw ParserException.eof("Expected $labelOfExpected but end of file was found")

        if (nextToken !is SpecialCharacterToken) {
            throw ParserException.of(nextToken, "Expected $labelOfExpected but ${nextToken.javaClass.simpleName} was found")
        } else if (nextToken.getSpecialCharType() != type) {
            throw ParserException.of(nextToken, "Expected $labelOfExpected but ${nextToken.getSpecialCharType().name} was found")
        }

        return nextToken
    }
}
