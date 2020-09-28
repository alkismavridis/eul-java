package eu.alkismavridis.euljava.parser.expressions

import eu.alkismavridis.euljava.core.ast.EulToken
import eu.alkismavridis.euljava.core.ast.expressions.EulExpression
import eu.alkismavridis.euljava.core.ast.operators.SpecialCharacterToken
import eu.alkismavridis.euljava.parser.ParserException
import eu.alkismavridis.euljava.parser.TokenSource


/**
 * It can will stop BEFORE:
 * - End of file
 * - New line
 * - Semicolon
 * - Comma
 * - PARENTHESIS_CLOSE
 * - SQUARE_CLOSE
 * - CURLY_CLOSE
 * */
class ExpressionParser(private val source: TokenSource) {

    /** Considers the opening token (such as parenthesis open) already consumed.
     * Consumes closing tokens such as ) or ]. */
    fun readExpression(breaker: ExpressionBreaker, forceSkipNewLines: Boolean): EulExpression? {
        val builder = ExpressionBuilder()

        while (true) {
            val nextToken = this.readOperatorOrShortExpression(breaker, forceSkipNewLines) ?: break

            if (nextToken is EulExpression) {
                builder.startWith(nextToken)
            } else if (nextToken.getSpecialCharType().isInfix()) {
                val nextShortExpression = this.requireShortExpression(breaker, true)
                builder.addInfix(nextToken as SpecialCharacterToken, nextShortExpression)
            }

            // TODO add prefix and suffix support

            else throw ParserException(nextToken.line, nextToken.column, "Expected expression")
        }

        return builder.getResult()
    }

    fun requireExpression(breaker: ExpressionBreaker, forceSkipNewLines: Boolean): EulExpression {
        return this.readExpression(breaker, forceSkipNewLines)
                ?: throw ParserException(-1, -1, "Expected expression but found end of file")
    }

    private fun readOperatorOrShortExpression(breaker: ExpressionBreaker, forceSkipNewLines: Boolean): EulToken? {
        val firstToken = this.source.getNextToken(forceSkipNewLines || breaker.ignoresNewLines)

        when (breaker.getClosingStatusFor(firstToken)) {
            CloseStatus.END_OF_EXPRESSION -> return null
            CloseStatus.MIDDLE_OF_EXPRESSION -> {
                // TODO check for ( and [
                return firstToken
            }
            CloseStatus.END_IF_NO_INFIX_FOLLOWS -> {
                val nextToken = this.source.getNextToken(true) ?: return null
                if (nextToken.getSpecialCharType().isInfixOrPrefix()) return nextToken

                // it is the end
                this.source.rollBackToken(nextToken)
                return null
            }
        }
    }

    private fun requireShortExpression(breaker: ExpressionBreaker, forceSkipNewLines: Boolean): EulExpression {
        val firstToken = this.source.getNextToken(forceSkipNewLines || breaker.ignoresNewLines)
                ?: throw ParserException(-1, -1, "Expected expression but end of file was found.")

        if (firstToken is EulExpression) return firstToken

        throw ParserException(firstToken.line, firstToken.column, "Expected expression")
    }
}





