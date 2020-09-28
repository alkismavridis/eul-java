package eu.alkismavridis.euljava.core.ast.expressions

import eu.alkismavridis.euljava.core.ast.operators.SpecialCharacterToken

class EulSuffixExpression(
        val target: EulExpression,
        operator: SpecialCharacterToken,
        parent: EulOperationExpression?
) : EulOperationExpression(target.line, target.column, operator, parent) {
    init {
        if (target is EulOperationExpression) target.parent = this
    }
}
