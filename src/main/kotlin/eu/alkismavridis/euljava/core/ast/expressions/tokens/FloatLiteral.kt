package eu.alkismavridis.euljava.core.ast.expressions.tokens

import eu.alkismavridis.euljava.core.ast.expressions.EulExpression
import eu.alkismavridis.euljava.core.types.NativeEulType

class FloatLiteral(
    val value: Double,
    val size: Int, // in bytes
    line: Int,
    column: Int
) : EulExpression(line, column) {
    private var _type: NativeEulType? = null

    override fun getType() = this._type
}
