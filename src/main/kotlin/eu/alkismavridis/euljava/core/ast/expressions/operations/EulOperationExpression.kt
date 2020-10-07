package eu.alkismavridis.euljava.core.ast.expressions.operations

import eu.alkismavridis.euljava.core.ast.expressions.EulExpression
import eu.alkismavridis.euljava.core.ast.operators.SpecialCharacterToken

abstract class EulOperationExpression(line: Int, column: Int, val operator: SpecialCharacterToken, parent: EulOperationExpression?) : EulExpression(line, column) {
    var parent = parent; internal set
    abstract fun getOperatorPrecedence() : Int
    abstract fun replaceTarget(newTarget: EulExpression)
    abstract fun getTarget() : EulExpression
}
