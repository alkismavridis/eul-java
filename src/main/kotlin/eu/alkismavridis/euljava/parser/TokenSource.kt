package eu.alkismavridis.euljava.parser

import eu.alkismavridis.euljava.core.ast.EulToken
import eu.alkismavridis.euljava.core.ast.expressions.tokens.EulReference

interface TokenSource {
    fun getNextToken(skipNewLines: Boolean): EulToken?
    fun requireNextToken(skipNewLines: Boolean, eofMessage: String): EulToken
    fun rollBackToken(token: EulToken)

    fun requireReference(wrongTokenMessage: String, eofErrorMessage: String) : EulReference
}
