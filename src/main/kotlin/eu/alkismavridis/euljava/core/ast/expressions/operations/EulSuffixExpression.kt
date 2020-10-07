package eu.alkismavridis.euljava.core.ast.expressions.operations

import eu.alkismavridis.euljava.core.ast.expressions.EulExpression
import eu.alkismavridis.euljava.core.ast.operators.SpecialCharacterToken

class EulSuffixExpression(
        private var target: EulExpression,
        operator: SpecialCharacterToken,
        val params: List<EulExpression>?,
        parent: EulOperationExpression?
) : EulOperationExpression(target.line, target.column, operator, parent) {
    init {
        if (target is EulOperationExpression) (target as EulOperationExpression).parent = this
    }


    /// EulExpression Overrides
    override fun replaceTarget(newTarget: EulExpression) {
        this.target = newTarget
        if (target is EulOperationExpression) (target as EulOperationExpression).parent = this
    }

    override fun getOperatorPrecedence() = this.operator.type.suffixPriority
    override fun getTarget() = this.target
}
