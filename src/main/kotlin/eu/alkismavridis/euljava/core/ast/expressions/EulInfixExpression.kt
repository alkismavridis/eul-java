package eu.alkismavridis.euljava.core.ast.expressions

import eu.alkismavridis.euljava.core.ast.operators.SpecialCharacterToken

class EulInfixExpression(
        val first: EulExpression,
        operator: SpecialCharacterToken,
        second: EulExpression,
        parent: EulOperationExpression?
) : EulOperationExpression(first.line, first.column, operator, parent) {
    var second = second; private set


    init {
        if (first is EulOperationExpression) first.parent = this
        if (second is EulOperationExpression) second.parent = this
    }

    fun replaceSecond(newSecond: EulExpression) {
        this.second = newSecond
        if (second is EulOperationExpression) (second as EulOperationExpression).parent = this
    }
}
