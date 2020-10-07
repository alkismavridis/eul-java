package eu.alkismavridis.euljava.core.ast.expressions.operations

import eu.alkismavridis.euljava.core.ast.expressions.EulExpression
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


    /// EulExpression Overrides
    override fun replaceTarget(newTarget: EulExpression) {
        this.second = newTarget
        if (second is EulOperationExpression) (second as EulOperationExpression).parent = this
    }

    override fun getOperatorPrecedence() = this.operator.type.infixPriority

    override fun getTarget() = this.second
}
