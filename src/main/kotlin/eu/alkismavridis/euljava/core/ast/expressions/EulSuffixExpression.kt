package eu.alkismavridis.euljava.core.ast.expressions

import eu.alkismavridis.euljava.core.ast.operators.SpecialCharacterToken

class EulSuffixExpression(
        private var target: EulExpression,
        operator: SpecialCharacterToken,
        parent: EulOperationExpression?
) : EulOperationExpression(target.line, target.column, operator, parent) {
    init {
        if (target is EulOperationExpression) (target as EulOperationExpression).parent = this
    }


    override fun replaceTarget(newTarget: EulExpression) {
        this.target = newTarget
        if (target is EulOperationExpression) (target as EulOperationExpression).parent = this
    }

    override fun getOperatorPrecedence() = this.operator.type.suffixPriority
    override fun getTarget() = this.target
}
