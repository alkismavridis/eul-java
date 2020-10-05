package eu.alkismavridis.euljava.core.ast.expressions.tokens

import eu.alkismavridis.euljava.core.ast.expressions.EulExpression
import eu.alkismavridis.euljava.core.types.NativeEulType

class IntegerLiteral(
        val value: Long,
        size: Int,
        isSigned: Boolean,
        line: Int,
        column: Int
) : EulExpression(line, column) {
    private var _type: NativeEulType? = null
    private val size: Int = if (isSigned) -size else size


    /// INFOS
    fun getSize(): Int {
        return if (this.size >= 0) this.size else -this.size
    }

    fun isSigned(): Boolean {
        return this.size < 0
    }

    override fun getType() = this._type
}
