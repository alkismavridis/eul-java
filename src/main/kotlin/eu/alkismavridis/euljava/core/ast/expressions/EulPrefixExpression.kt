package eu.alkismavridis.euljava.core.ast.expressions

import eu.alkismavridis.euljava.core.ast.operators.SpecialCharacterToken

class EulPrefixExpression(
        operator: SpecialCharacterToken,
        val target: EulExpression,
        parent: EulOperationExpression?
) : EulOperationExpression(operator.line, operator.column, operator, parent) {
    init {
        if (target is EulOperationExpression) target.parent = this
    }
}
