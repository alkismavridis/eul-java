package eu.alkismavridis.euljava.parser.statements

import eu.alkismavridis.euljava.core.ast.EulToken
import eu.alkismavridis.euljava.core.ast.statements.IfStatement
import eu.alkismavridis.euljava.parser.TokenSource
import eu.alkismavridis.euljava.parser.expressions.ExpressionParser

class IfBlockParser(private val source: TokenSource, private val expressionParser: ExpressionParser) {

    /** Considers the "if" token already parsed. Consumes all else-if */
    fun parse(openingToken: EulToken) : IfStatement {
        TODO()
    }
}
