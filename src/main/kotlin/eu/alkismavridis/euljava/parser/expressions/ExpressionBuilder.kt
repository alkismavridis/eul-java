package eu.alkismavridis.euljava.parser.expressions

import eu.alkismavridis.euljava.core.ast.expressions.*
import eu.alkismavridis.euljava.core.ast.expressions.operations.EulInfixExpression
import eu.alkismavridis.euljava.core.ast.expressions.operations.EulOperationExpression
import eu.alkismavridis.euljava.core.ast.expressions.operations.EulSuffixExpression
import eu.alkismavridis.euljava.core.ast.operators.SpecialCharType
import eu.alkismavridis.euljava.core.ast.operators.SpecialCharacterToken
import eu.alkismavridis.euljava.parser.ParserException

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


    /// INFIX INTEGRATION
    fun addInfix(incomingOperator: SpecialCharacterToken, newExpression: EulExpression) {
        if (this.result == null) {
            throw ParserException(newExpression.line, newExpression.column, "Cannot integrate infix when result is null")
        }

        val parentToAdjust = this.getParentToHostInfix(incomingOperator.getSpecialCharType())
        if (parentToAdjust == null) {
            val newInfix = EulInfixExpression(this.result!!, incomingOperator, newExpression, null)
            this.result = newInfix
            this.head = if (newExpression is EulOperationExpression) newExpression else newInfix
        } else {
            val newInfix = EulInfixExpression(parentToAdjust.getTarget(), incomingOperator, newExpression, null)
            parentToAdjust.replaceTarget(newInfix)
            this.head = if (newExpression is EulOperationExpression) newExpression else newInfix
        }
    }

    private fun getParentToHostInfix(incomingOperator: SpecialCharType): EulOperationExpression? {
        var currentNode = this.head
        while (currentNode != null) {
            val currentNodePrecedence = currentNode.getOperatorPrecedence()
            if (incomingOperator.infixPriority > currentNodePrecedence || (incomingOperator.infixPriority == currentNodePrecedence && incomingOperator.isInfixRtl)) {
                return currentNode
            } else {
                currentNode = currentNode.parent
            }
        }

        return null
    }


    /// SUFFIX INTEGRATION
    fun addSuffix(incomingOperator: SpecialCharacterToken, parameters: List<EulExpression>?) {
        if (this.result == null) {
            throw ParserException(incomingOperator.line, incomingOperator.column, "Cannot integrate suffix when result is null")
        }

        val parentToAdjust = this.getParentToHostSuffix(incomingOperator.getSpecialCharType())
        if (parentToAdjust == null) {
            val newSuffix = EulSuffixExpression(this.result!!, incomingOperator, parameters, null)
            this.result = newSuffix
            this.head = newSuffix
        } else {
            val newSuffix = EulSuffixExpression(parentToAdjust.getTarget(), incomingOperator, parameters, null)
            parentToAdjust.replaceTarget(newSuffix)
            this.head = newSuffix
        }
    }

    private fun getParentToHostSuffix(incomingOperator: SpecialCharType): EulOperationExpression? {
        var currentNode = this.head
        while (currentNode != null) {
            val currentNodePrecedence = currentNode.getOperatorPrecedence()
            if (currentNode !is EulSuffixExpression && incomingOperator.suffixPriority > currentNodePrecedence) {
                return currentNode
            } else {
                currentNode = currentNode.parent
            }
        }

        return null
    }
}
