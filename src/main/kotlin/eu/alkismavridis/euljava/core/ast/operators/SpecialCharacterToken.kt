package eu.alkismavridis.euljava.core.ast.operators

import eu.alkismavridis.euljava.core.ast.EulToken

class SpecialCharacterToken(val type : SpecialCharType, line: Int, column: Int) : EulToken(line, column) {
    override fun getSpecialCharType() = this.type
}
