package eu.alkismavridis.euljava.core.ast.expressions

import eu.alkismavridis.euljava.core.ast.operators.SpecialCharacterToken
import eu.alkismavridis.euljava.core.types.EulType

class EulPrefixExpression(
        operator: SpecialCharacterToken,
        private var target: EulExpression,
        parent: EulOperationExpression?
) : EulOperationExpression(operator.line, operator.column, operator, parent) {
    private var _type: EulType? = null


    init {
        if (target is EulOperationExpression) (target as EulOperationExpression).parent = this
    }


    /// EulExpression Overrides
    override fun replaceTarget(newTarget: EulExpression) {
        this.target = newTarget
        if (target is EulOperationExpression) (target as EulOperationExpression).parent = this
    }

    override fun getOperatorPrecedence() = this.operator.type.prefixPriority
    override fun getTarget() = this.target
    override fun getType() = this._type
}
