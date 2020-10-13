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
     * Does NOT Consumes closing token such as ) or ], if and only if the corresponding opening token was also read during this method call.
     * */
    fun readExpression(endPolicy: NewLinePolicy): EulExpression? {
        val builder = ExpressionBuilder()
        this.addLongExpressionToBuilder(builder, endPolicy)
        return builder.getResult()
    }

    fun requireExpression(endPolicy: NewLinePolicy): EulExpression {
        val builder = ExpressionBuilder()
        this.addLongExpressionToBuilder(builder, endPolicy)

        val result = builder.getResult()
        if (result == null) {
            val closingToken = this.source.getNextToken(endPolicy.ignoreFirst)
            throw if (closingToken == null) ParserException.eof("Expected expression but found end of file")
            else ParserException.of(closingToken, "Expected expression")
        }

        return result
    }


    /**
     * Reads the longest possible expression.
     * The next token to be read after this method returns is the token than ended the expression
     * */
    private fun addLongExpressionToBuilder(builder: ExpressionBuilder, newLinePolicy: NewLinePolicy) {
        val firstExpression = this.readShortExpression(newLinePolicy, newLinePolicy.ignoreFirst, false) ?: return
        builder.startWith(firstExpression)

        while (true) {
            val tokenAfterExpression = this.source.getNextToken(newLinePolicy.ignoreAll) ?: return
            val specialCharType = tokenAfterExpression.getSpecialCharType()

            when {
                specialCharType.isSuffix() -> this.integrateSuffix(tokenAfterExpression, builder)
                specialCharType.isInfix() -> this.integrateInfix(tokenAfterExpression as SpecialCharacterToken, builder, newLinePolicy)

                specialCharType == SpecialCharType.NEW_LINE -> {
                    val shouldContinue = this.handleNewLineCharacterAfterExpression(tokenAfterExpression, builder, newLinePolicy)
                    if (!shouldContinue) return
                }

                else -> {
                    this.source.rollBackToken(tokenAfterExpression)
                    return
                }
            }
        }
    }

    /** Returns true if the expression should continue, false if it was really the end. */
    private fun handleNewLineCharacterAfterExpression(nlToken: EulToken, builder:ExpressionBuilder, newLinePolicy: NewLinePolicy) : Boolean {
        val nextToken = this.source.getNextToken(true) ?: return false

        if (!nextToken.getSpecialCharType().isInfix()) {
            this.source.rollBackToken(nextToken)
            this.source.rollBackToken(nlToken)
            return false
        }

        val nextShortExpression = this.readShortExpression(newLinePolicy, true, true)
                ?: throw ParserException.of(nextToken, "Expected expression but end of file war found")
        builder.addInfix(nextToken as SpecialCharacterToken, nextShortExpression)

        return true
    }


    /**
     * Reads the smallest possible self-contained expression.
     * For example, if the input is "-x.y + z", this method will only parse "-x"
     *
     * It handles prefix expressions and expressions starting with parenthesis-open
     * */
    private fun readShortExpression(endPolicy: NewLinePolicy, forceSkipNewLines: Boolean, isRequired: Boolean): EulExpression? {
        val firstToken = this.source.getNextToken(forceSkipNewLines || endPolicy.ignoreAll)
        if (firstToken == null) {
            if (isRequired) throw this.createExpressionExpectedError(firstToken)
            else return null
        }

        if (firstToken is EulExpression) {
            return firstToken
        }

        val specialCharType = firstToken.getSpecialCharType()
        if (firstToken.getSpecialCharType().isPrefix()) {
            val expression = this.readShortExpression(endPolicy, true, true)
                    ?: throw ParserException.of(firstToken, "Expected expression but end of file war found")
            return EulPrefixExpression(firstToken as SpecialCharacterToken, expression, null)
        } else if (specialCharType == SpecialCharType.PARENTHESIS_OPEN) {
            return this.readParenthesisExpression()
        }

        this.source.rollBackToken(firstToken)
        if (isRequired) throw this.createExpressionExpectedError(firstToken)
        else return null
    }

    private fun readParenthesisExpression(): EulExpression {
        val result = this.requireExpression(NewLinePolicy.IGNORE)
        val closingToken = this.source.requireNextToken(true, "Expected parenthesis close")
        if (closingToken.getSpecialCharType() != SpecialCharType.PARENTHESIS_CLOSE) {
            throw ParserException.of(closingToken, "Expected parenthesis close")
        }

        return result
    }

    private fun createExpressionExpectedError(unexpectedToken: EulToken?): ParserException {
        return if (unexpectedToken == null) ParserException.eof("Expected expression but end of file was found")
        else ParserException.of(unexpectedToken, "Expected expression")
    }

    private fun readCommaSeparatedList(endPolicy: NewLinePolicy): List<EulExpression>? {
        var result: MutableList<EulExpression>? = null
        loop@ while (true) {
            val builder = ExpressionBuilder()
            this.addLongExpressionToBuilder(builder, endPolicy)
            val closingToken = this.source.getNextToken(false)
            val specialCharType = closingToken?.getSpecialCharType() ?: SpecialCharType.NOT_A_SPECIAL_CHARACTER

            when (specialCharType) {
                SpecialCharType.COMMA -> {
                    val valueToAdd = builder.getResult()
                            ?: throw ParserException.of(closingToken!!, "Expected expression before comma")
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
                val parameters = this.readCommaSeparatedList(NewLinePolicy.IGNORE)
                builder.addSuffix(tokenAfterExpression as SpecialCharacterToken, parameters)
            }
            else -> {
                builder.addSuffix(tokenAfterExpression as SpecialCharacterToken, null)
            }
        }
    }

    private fun integrateInfix(infixOperator:SpecialCharacterToken, builder: ExpressionBuilder, newLinePolicy: NewLinePolicy) {
        val nextShortExpression = this.readShortExpression(newLinePolicy, true, true)
                ?: throw ParserException.of(infixOperator, "Expected expression but end of file was found")
        builder.addInfix(infixOperator, nextShortExpression)
    }
}





