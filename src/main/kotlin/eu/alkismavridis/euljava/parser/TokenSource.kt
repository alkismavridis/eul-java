package eu.alkismavridis.euljava.parser

import eu.alkismavridis.euljava.core.CompileOptions
import eu.alkismavridis.euljava.core.EulLogger
import eu.alkismavridis.euljava.core.ast.EulToken
import eu.alkismavridis.euljava.core.ast.expressions.tokens.EulReference
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


    fun requireReference(wrongTokenMessage: String, eofErrorMessage: String) : EulReference {
        val nextToken = this.getNextToken(true)
                ?: throw ParserException.eof(eofErrorMessage)

        if (nextToken !is EulReference) {
            throw ParserException.of(nextToken, wrongTokenMessage)
        }

        return nextToken
    }
}
