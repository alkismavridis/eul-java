package eu.alkismavridis.euljava.core.ast.expressions.tokens

import eu.alkismavridis.euljava.core.ast.expressions.EulExpression
import eu.alkismavridis.euljava.core.types.NativeEulType


class BooleanLiteral(val value: Boolean, line: Int, column: Int) : EulExpression(line, column) {
    override fun getType() = TYPE

    companion object {
        val TYPE = NativeEulType("Boolean")
    }
}
