package eu.alkismavridis.euljava.core.ast.expressions

import eu.alkismavridis.euljava.core.ast.operators.SpecialCharacterToken

class EulInfixExpression(
        val first: EulExpression,
        val operator: SpecialCharacterToken,
        val second: EulExpression
) : EulExpression(first.line, first.column) {

}
