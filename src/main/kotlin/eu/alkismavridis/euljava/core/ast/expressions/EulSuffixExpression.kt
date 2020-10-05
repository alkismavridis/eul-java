package eu.alkismavridis.euljava.core.ast.expressions

import eu.alkismavridis.euljava.core.ast.operators.SpecialCharacterToken
import eu.alkismavridis.euljava.core.types.EulType

class EulSuffixExpression(
        private var target: EulExpression,
        operator: SpecialCharacterToken,
        val params: List<EulExpression>?,
        parent: EulOperationExpression?
) : EulOperationExpression(target.line, target.column, operator, parent) {
    private var _type: EulType? = null

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
    override fun getType() = this._type
}
