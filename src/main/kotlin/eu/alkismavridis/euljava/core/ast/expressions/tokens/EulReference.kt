package eu.alkismavridis.euljava.core.ast.expressions.tokens

import eu.alkismavridis.euljava.core.ast.expressions.EulExpression
import eu.alkismavridis.euljava.core.types.EulType

class EulReference(val name: String, line: Int, column: Int) : EulExpression(line, column) {
    private var _type: EulType? = null

    override fun getType() = this._type
}
