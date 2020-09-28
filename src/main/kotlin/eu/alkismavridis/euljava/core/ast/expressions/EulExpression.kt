package eu.alkismavridis.euljava.core.ast.expressions

import eu.alkismavridis.euljava.core.ast.EulToken
import eu.alkismavridis.euljava.core.ast.operators.SpecialCharacterToken

open class EulExpression(line: Int, column: Int) : EulToken(line, column)
open class EulOperationExpression(line: Int, column: Int, val operator: SpecialCharacterToken, parent: EulOperationExpression?) : EulExpression(line, column) {
    var parent = parent; internal set
}
