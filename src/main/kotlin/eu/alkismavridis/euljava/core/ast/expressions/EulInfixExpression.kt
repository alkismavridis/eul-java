package eu.alkismavridis.euljava.core.ast.expressions

import eu.alkismavridis.euljava.core.ast.operators.SpecialCharacterToken
import eu.alkismavridis.euljava.core.types.EulType

class EulInfixExpression(
        val first: EulExpression,
        operator: SpecialCharacterToken,
        second: EulExpression,
        parent: EulOperationExpression?
) : EulOperationExpression(first.line, first.column, operator, parent) {
    var second = second; private set
    private var _type: EulType? = null


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

    override fun getType() = this._type
}
