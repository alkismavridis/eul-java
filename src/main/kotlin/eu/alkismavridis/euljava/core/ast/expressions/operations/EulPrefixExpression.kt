package eu.alkismavridis.euljava.core.ast.expressions.operations

import eu.alkismavridis.euljava.core.ast.expressions.EulExpression
import eu.alkismavridis.euljava.core.ast.operators.SpecialCharacterToken

class EulPrefixExpression(
        operator: SpecialCharacterToken,
        private var target: EulExpression,
        parent: EulOperationExpression?
) : EulOperationExpression(operator.line, operator.column, operator, parent) {
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
}
