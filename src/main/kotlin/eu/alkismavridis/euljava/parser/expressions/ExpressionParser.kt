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
    /**
     * Reads an expression from its start.
     * The resulting expression will have null parent.
     * If any opening token exists (such as parenthesis open), this method considers that it is already consumed.
     * Consumes closing token such as ) or ].
     * */
    fun readExpression(endPolicy: ExpressionEndPolicy): EulExpression? {
        val builder =  ExpressionBuilder()
        this.addLongExpressionToBuilder(builder, endPolicy)
        return builder.getResult()
    }

    fun requireExpression(endPolicy: ExpressionEndPolicy): EulExpression {
        val builder =  ExpressionBuilder()
        this.addLongExpressionToBuilder(builder, endPolicy)

        val result = builder.getResult()
        if (result == null) {
            val closingToken = this.source.getNextToken(endPolicy.ignoreFirstNewLine)
            throw if(closingToken == null) ParserException.eof("Expected expression but found end of file")
            else ParserException.of(closingToken, "Expected expression")
        }

        return result
    }


    /**
     * Reads the longest possible expression.
     * The next token to be read after this method returns is the token than ended the expression
     * */
    private fun addLongExpressionToBuilder(builder: ExpressionBuilder, endPolicy: ExpressionEndPolicy) {
        val firstExpression = this.readShortExpression(endPolicy, endPolicy.ignoreFirstNewLine, false) ?: return
        builder.startWith(firstExpression)

        while (true) {
            val tokenAfterExpression = this.source.getNextToken(endPolicy.ignoreAllNewLines) ?: return

            val closingStatus = endPolicy.getClosingStatusFor(tokenAfterExpression)
            if (closingStatus == CloseStatus.END_OF_EXPRESSION) {
                this.source.rollBackToken(tokenAfterExpression)
                return
            }

            val specialCharType = tokenAfterExpression.getSpecialCharType()

            if (specialCharType.isSuffix()) {
                this.integrateSuffix(tokenAfterExpression, builder)
            }

            else if (specialCharType.isInfix()) {
                val nextShortExpression = this.readShortExpression(endPolicy, true, true) ?:
                    throw ParserException.of(tokenAfterExpression, "Expected expression but end of file was found")
                builder.addInfix(tokenAfterExpression as SpecialCharacterToken, nextShortExpression)
            }

            else if (closingStatus == CloseStatus.END_IF_NO_INFIX_FOLLOWS) {
                val nextToken = this.source.getNextToken(true) ?: return

                if (!nextToken.getSpecialCharType().isInfix()) {
                    // it is really the end
                    this.source.rollBackToken(nextToken)
                    this.source.rollBackToken(tokenAfterExpression)
                    return
                }

                val nextShortExpression = this.readShortExpression(endPolicy, true, true) ?:
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
    private fun readShortExpression(endPolicy: ExpressionEndPolicy, forceSkipNewLines: Boolean, isRequired: Boolean): EulExpression? {
        val firstToken = this.source.getNextToken(forceSkipNewLines || endPolicy.ignoreAllNewLines)
        val closeStatus = endPolicy.getClosingStatusFor(firstToken)

        if (firstToken == null || closeStatus == CloseStatus.END_OF_EXPRESSION) {
            if (firstToken != null) this.source.rollBackToken(firstToken)

            if (isRequired) throw this.createExpressionExpectedError(firstToken)
            else return null
        }

        if (firstToken is EulExpression) return firstToken

        val specialCharType = firstToken.getSpecialCharType()
        if (firstToken.getSpecialCharType().isPrefix()) {
            val expression = this.readShortExpression(endPolicy, true, true) ?:
                throw ParserException.of(firstToken, "Expected expression but end of file war found")
            return EulPrefixExpression(firstToken as SpecialCharacterToken, expression, null)
        } else if (specialCharType == SpecialCharType.PARENTHESIS_OPEN) {
            return this.readParenthesisExpression()
        }

        this.source.rollBackToken(firstToken)
        if (isRequired) throw this.createExpressionExpectedError(firstToken)
        else return null
    }

    private fun readParenthesisExpression() : EulExpression {
        val result = this.requireExpression(ExpressionEndPolicy.PARENTHESIS)
        val closingToken = this.source.requireNextToken(true, "Expected parenthesis close")
        if (closingToken.getSpecialCharType() != SpecialCharType.PARENTHESIS_CLOSE) {
            throw ParserException.of(closingToken, "Expected parenthesis close")
        }

        return result
    }

    private fun createExpressionExpectedError(unexpectedToken: EulToken?) : ParserException {
        return if (unexpectedToken == null) ParserException.eof("Expected expression but end of file was found")
        else ParserException.of(unexpectedToken, "Expected expression")
    }

    private fun readCommaSeparatedList(endPolicy: ExpressionEndPolicy) : List<EulExpression>? {
        var result: MutableList<EulExpression>? = null
        loop@while(true) {
            val builder =  ExpressionBuilder()
            this.addLongExpressionToBuilder(builder, endPolicy)
            val closingToken = this.source.getNextToken(false)
            val specialCharType = closingToken?.getSpecialCharType() ?: SpecialCharType.NOT_A_SPECIAL_CHARACTER

            when(specialCharType) {
                SpecialCharType.COMMA -> {
                    val valueToAdd = builder.getResult() ?: throw ParserException.of(closingToken!!, "Expected expression before comma")
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
                val parameters = this.readCommaSeparatedList(ExpressionEndPolicy.COMMA_SEPARATED_PARENTHESIS)
                builder.addSuffix(tokenAfterExpression as SpecialCharacterToken, parameters)
            }
            else -> {
                builder.addSuffix(tokenAfterExpression as SpecialCharacterToken, null)
            }
        }
    }
}





