package eu.alkismavridis.euljava.parser.expressions

import eu.alkismavridis.euljava.core.ast.expressions.EulExpression
import eu.alkismavridis.euljava.core.ast.expressions.EulInfixExpression
import eu.alkismavridis.euljava.core.ast.expressions.EulOperationExpression
import eu.alkismavridis.euljava.core.ast.operators.SpecialCharType
import eu.alkismavridis.euljava.core.ast.operators.SpecialCharacterToken
import eu.alkismavridis.euljava.parser.ParserException
import java.lang.IllegalArgumentException

internal class ExpressionBuilder {
    private var result: EulExpression? = null
    private var head: EulOperationExpression? = null


    fun getResult() = this.result

    fun startWith(exp: EulExpression) {
        if (this.result != null) {
            throw ParserException(exp.line, exp.column, "Expected operator or end of expression")
        }

        this.result = exp
        if (exp is EulOperationExpression) this.head = exp
    }

    fun addInfix(specialChar: SpecialCharacterToken, newExpression: EulExpression) {
        if (this.result == null) {
            throw ParserException(newExpression.line, newExpression.column, "Cannot integrate infix when result is null")
        }

        val parentToAdjust = this.getParentToHostInfix(specialChar.getSpecialCharType())

        if (parentToAdjust == null) {
            this.head = EulInfixExpression(this.result!!, specialChar, newExpression, null)
            this.result = this.head
        } else if (parentToAdjust is EulInfixExpression) {
            val newInfix = EulInfixExpression(parentToAdjust.second, specialChar, newExpression, null)
            parentToAdjust.replaceSecond(newInfix)
            this.head = newInfix
        } else {
            throw IllegalArgumentException("Not yet implemented")
        }
    }

    private fun getParentToHostInfix(incomingOperator: SpecialCharType): EulOperationExpression? {
        var currentNode = this.head
        while (currentNode != null) {
            val currentNodeInfixPriority = currentNode.operator.getSpecialCharType().infixPriority
            if (incomingOperator.infixPriority > currentNodeInfixPriority || (incomingOperator.infixPriority == currentNodeInfixPriority && incomingOperator.isInfixRtl)) {
                return currentNode
            } else {
                currentNode = currentNode.parent
            }
        }

        return null
    }
}
