package eu.alkismavridis.euljava.core.ast.expressions

import eu.alkismavridis.euljava.core.ast.EulToken
import eu.alkismavridis.euljava.core.ast.operators.SpecialCharacterToken
import eu.alkismavridis.euljava.core.types.EulType

abstract class EulExpression(line: Int, column: Int) : EulToken(line, column) {
    abstract fun getType() : EulType?
}

abstract class EulOperationExpression(line: Int, column: Int, val operator: SpecialCharacterToken, parent: EulOperationExpression?) : EulExpression(line, column) {
    var parent = parent; internal set
    abstract fun getOperatorPrecedence() : Int
    abstract fun replaceTarget(newTarget: EulExpression)
    abstract fun getTarget() : EulExpression
}
