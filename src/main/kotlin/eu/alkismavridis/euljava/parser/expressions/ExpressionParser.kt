package eu.alkismavridis.euljava.parser.expressions

import eu.alkismavridis.euljava.core.ast.EulToken
import eu.alkismavridis.euljava.core.ast.expressions.EulExpression
import eu.alkismavridis.euljava.core.ast.expressions.operations.EulPrefixExpression
import eu.alkismavridis.euljava.core.ast.operators.SpecialCharType
import eu.alkismavridis.euljava.core.ast.operators.SpecialCharacterToken
import eu.alkismavridis.euljava.parser.ParserException
import eu.alkismavridis.euljava.parser.TokenSource
import java.util.*


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
    private var lastReadToken: EulToken? = null

    /**
     * Reads an expression from its start.
     * The resulting expression will have null parent.
     * If any opening token exists (such as parenthesis open), this method considers that it is already consumed.
     * Consumes closing token such as ) or ].
     * */
    fun readExpression(breaker: ExpressionBreaker): EulExpression? {
        val builder =  ExpressionBuilder()
        this.addLongExpressionToBuilder(builder, breaker)
        return builder.getResult()
    }

    fun requireExpression(breaker: ExpressionBreaker): EulExpression {
        val builder =  ExpressionBuilder()
        val closingToken = this.addLongExpressionToBuilder(builder, breaker)
        val result = builder.getResult()

        if (result == null) {
            throw if(closingToken == null) ParserException.eof("Expected expression but found end of file")
            else ParserException.of(closingToken, "Expected expression")
        }

        return result
    }


    /**
     * Reads the longest possible expression.
     * Returns the last read token
     * */
    private fun addLongExpressionToBuilder(builder: ExpressionBuilder, breaker: ExpressionBreaker) : EulToken? {
        val firstExpression = this.readShortExpression(breaker, breaker.newLinePolicy.ignoreFirst, false) ?: return this.lastReadToken
        builder.startWith(firstExpression)

        while (true) {
            val tokenAfterExpression = this.getNextToken(breaker.newLinePolicy.ignoreAll) ?: return null

            val closingStatus = breaker.getClosingStatusFor(tokenAfterExpression)
            if (closingStatus == CloseStatus.END_OF_EXPRESSION) return tokenAfterExpression

            val specialCharType = tokenAfterExpression.getSpecialCharType()

            if (specialCharType.isSuffix()) {
                this.integrateSuffix(tokenAfterExpression, builder)
            }

            else if (specialCharType.isInfix()) {
                val nextShortExpression = this.readShortExpression(breaker, true, true) ?:
                    throw ParserException.of(tokenAfterExpression, "Expected expression but end of file was found")
                builder.addInfix(tokenAfterExpression as SpecialCharacterToken, nextShortExpression)
            }

            else if (closingStatus == CloseStatus.END_IF_NO_INFIX_FOLLOWS) {
                val nextToken = this.getNextToken(true) ?: return tokenAfterExpression

                if (!nextToken.getSpecialCharType().isInfix()) {
                    // it is really the end
                    this.rollBackToken(nextToken)
                    return tokenAfterExpression
                }

                val nextShortExpression = this.readShortExpression(breaker, true, true) ?:
                    throw ParserException.of(nextToken, "Expected expression but end of file war found")
                builder.addInfix(nextToken as SpecialCharacterToken, nextShortExpression)
            }

            else throw ParserException.of(tokenAfterExpression, "Expected expression")
        }
    }


            /**
     * Reads the smallest possible self-contained expression.
     * For example, if the input is "-x.y + z", this method will only parse "-x"
     *
     * It handles prefix expressions and expressions starting with parenthesis-open
     * */
    private fun readShortExpression(breaker: ExpressionBreaker, forceSkipNewLines: Boolean, isRequired: Boolean): EulExpression? {
        val firstToken = this.getNextToken(forceSkipNewLines || breaker.newLinePolicy.ignoreAll)
        val closeStatus = breaker.getClosingStatusFor(firstToken)

        if (firstToken == null || closeStatus == CloseStatus.END_OF_EXPRESSION) {
            if (isRequired) throw this.createExpressionExpectedError(firstToken)
            else return null
        }

        if (firstToken is EulExpression) return firstToken

        val specialCharType = firstToken.getSpecialCharType()
        if (firstToken.getSpecialCharType().isPrefix()) {
            val expression = this.readShortExpression(breaker, true, true) ?:
                throw ParserException.of(firstToken, "Expected expression but end of file war found")
            return EulPrefixExpression(firstToken as SpecialCharacterToken, expression, null)
        } else if (specialCharType == SpecialCharType.PARENTHESIS_OPEN) {
            return this.requireExpression(ExpressionBreaker.PARENTHESIS)
        }

        if (isRequired) throw this.createExpressionExpectedError(firstToken)
        else return null
    }

    private fun createExpressionExpectedError(unexpectedToken: EulToken?) : ParserException {
        return if (unexpectedToken == null) ParserException.eof("Expected expression but end of file was found")
        else ParserException.of(unexpectedToken, "Expected expression")
    }

    private fun readCommaSeparatedList(breaker: ExpressionBreaker) : List<EulExpression>? {
        var result: MutableList<EulExpression>? = null
        loop@while(true) {
            val builder =  ExpressionBuilder()
            val closingToken = this.addLongExpressionToBuilder(builder, breaker)!!

            when(closingToken.getSpecialCharType()) {
                SpecialCharType.COMMA -> {
                    val valueToAdd = builder.getResult() ?: throw ParserException.of(closingToken, "Expected expression before comma")
                    if (result == null) result = LinkedList()
                    result.add(valueToAdd)
                }
                else -> {
                    val lastParam = builder.getResult() ?: break@loop
                    if (result == null) result = LinkedList()
                    result.add(lastParam)
                    break@loop
                }
            }
        }

        return result
    }

    private fun integrateSuffix(tokenAfterExpression: EulToken, builder: ExpressionBuilder) {
        when (tokenAfterExpression.getSpecialCharType()) {
            SpecialCharType.PARENTHESIS_OPEN -> {
                val parameters = this.readCommaSeparatedList(ExpressionBreaker.COMMA_SEPARATED_PARENTHESIS)
                builder.addSuffix(tokenAfterExpression as SpecialCharacterToken, parameters)
            }
            else -> {
                builder.addSuffix(tokenAfterExpression as SpecialCharacterToken, null)
            }
        }
    }

    private fun getNextToken(skipNewLines: Boolean) : EulToken? {
        this.lastReadToken = this.source.getNextToken(skipNewLines)
        return this.lastReadToken
    }

    private fun rollBackToken(token: EulToken) {
        this.source.rollBackToken(token)
        this.lastReadToken = null
    }
}





