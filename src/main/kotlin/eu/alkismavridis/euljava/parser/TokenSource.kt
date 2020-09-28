package eu.alkismavridis.euljava.parser

import eu.alkismavridis.euljava.core.ast.EulToken

interface TokenSource {
    fun getNextToken(skipNewLines: Boolean): EulToken?
    fun rollBackToken(ch: EulToken)
}
