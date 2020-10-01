package eu.alkismavridis.euljava.parser.expressions

import eu.alkismavridis.euljava.core.ast.EulToken
import eu.alkismavridis.euljava.core.ast.expressions.EulExpression
import eu.alkismavridis.euljava.core.ast.expressions.EulPrefixExpression
import eu.alkismavridis.euljava.core.ast.operators.SpecialCharType
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

    /**
     * Reads an expression from its start.
     * The resulting expression will have null parent.
     * If any opening token exists (such as parenthesis open), this method considers that it is already consumed.
     * Consumes closing token such as ) or ].
     * */
    fun readExpression(breaker: ExpressionBreaker, forceSkipNewLines: Boolean): EulExpression? {
        val firstExpression = this.readShortExpression(breaker, forceSkipNewLines, false) ?: return null

        val builder = ExpressionBuilder()
        builder.startWith(firstExpression)

        while (true) {
            val tokenAfterExpression = this.source.getNextToken(breaker.ignoresNewLines || forceSkipNewLines) ?: break

            val closingStatus = breaker.getClosingStatusFor(tokenAfterExpression)
            if (closingStatus == CloseStatus.END_OF_EXPRESSION) return builder.getResult()

            val specialCharType = tokenAfterExpression.getSpecialCharType()
            if (specialCharType.isSuffix()) {
                TODO("Suffix operators not yet implemented")
            }
            else if (specialCharType.isInfix()) {
                val nextShortExpression = this.readShortExpression(breaker, true, true)!!
                builder.addInfix(tokenAfterExpression as SpecialCharacterToken, nextShortExpression)
            }
            else if (closingStatus == CloseStatus.END_IF_NO_INFIX_FOLLOWS) {
                val nextToken = this.source.getNextToken(true) ?: return builder.getResult()

                if (!nextToken.getSpecialCharType().isInfix()) {
                    // it is really the end
                    this.source.rollBackToken(nextToken)
                    return builder.getResult()
                }

                val nextShortExpression = this.readShortExpression(breaker, true, true)!!
                builder.addInfix(nextToken as SpecialCharacterToken, nextShortExpression)
            }

            else throw ParserException(tokenAfterExpression.line, tokenAfterExpression.column, "Expected expression")
        }

        return builder.getResult()
    }

    fun requireExpression(breaker: ExpressionBreaker, forceSkipNewLines: Boolean): EulExpression {
        return this.readExpression(breaker, forceSkipNewLines)
                ?: throw ParserException(-1, -1, "Expected expression but found end of file")
    }


    /**
     * Reads the smallest self-contained expression possible.
     * For example, if the input is "-x.y + z", this will just parse "-x"
     *
     * It handles prefix expressions and expressions starting with parenthesis-open
     * */
    private fun readShortExpression(breaker: ExpressionBreaker, forceSkipNewLines: Boolean, isRequired: Boolean): EulExpression? {
        val firstToken = this.source.getNextToken(forceSkipNewLines || breaker.ignoresNewLines)
        val closeStatus = breaker.getClosingStatusFor(firstToken)

        if (firstToken == null || closeStatus == CloseStatus.END_OF_EXPRESSION) {
            if (isRequired) throw this.createExpressionExpectedError(firstToken)
            else return null
        }

        if (firstToken is EulExpression) return firstToken

        val specialCharType = firstToken.getSpecialCharType()
        if (firstToken.getSpecialCharType().isPrefix()) {
            return EulPrefixExpression(firstToken as SpecialCharacterToken, this.readShortExpression(breaker, true, true)!!, null)
        } else if (specialCharType == SpecialCharType.PARENTHESIS_OPEN) {
            return this.requireExpression(ExpressionBreaker.PARENTHESIS, true)
        }

        if (isRequired) throw this.createExpressionExpectedError(firstToken)
        else return null
    }

    private fun createExpressionExpectedError(unexpectedToken: EulToken?) : ParserException {
        return if (unexpectedToken == null) ParserException(-1, -1, "Expected expression but end of file was found")
        else ParserException(unexpectedToken.line, unexpectedToken.column, "Expected expression")
    }
}





