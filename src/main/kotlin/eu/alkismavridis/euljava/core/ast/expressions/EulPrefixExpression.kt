package eu.alkismavridis.euljava.core.ast.expressions

import eu.alkismavridis.euljava.core.ast.operators.SpecialCharacterToken

class EulPrefixExpression(
        operator: SpecialCharacterToken,
        private var target: EulExpression,
        parent: EulOperationExpression?
) : EulOperationExpression(operator.line, operator.column, operator, parent) {
    init {
        if (target is EulOperationExpression) (target as EulOperationExpression).parent = this
    }

    override fun replaceTarget(newTarget: EulExpression) {
        this.target = newTarget
        if (target is EulOperationExpression) (target as EulOperationExpression).parent = this
    }

    override fun getOperatorPrecedence() = this.operator.type.prefixPriority
    override fun getTarget() = this.target
}
