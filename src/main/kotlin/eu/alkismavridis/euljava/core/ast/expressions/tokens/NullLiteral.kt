package eu.alkismavridis.euljava.core.ast.expressions.tokens

import eu.alkismavridis.euljava.core.ast.expressions.EulExpression
import eu.alkismavridis.euljava.core.types.EulType

class NullLiteral(line: Int, column: Int) : EulExpression(line, column) {
    override fun getType() : EulType? = null
}
