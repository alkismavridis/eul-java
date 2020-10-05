package eu.alkismavridis.euljava.core.ast.expressions.tokens

import eu.alkismavridis.euljava.core.ast.expressions.EulExpression
import eu.alkismavridis.euljava.core.types.NativeEulType

class CharLiteral(
    val value: Long,
    val size: Int, // in bytes
    line: Int,
    column: Int
) : EulExpression(line, column) {
    override fun getType() = TYPE

    companion object {
        val TYPE = NativeEulType("Char")
    }
}
