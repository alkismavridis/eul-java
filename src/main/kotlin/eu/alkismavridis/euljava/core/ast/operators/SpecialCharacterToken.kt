package eu.alkismavridis.euljava.core.ast.operators

import eu.alkismavridis.euljava.core.ast.EulToken

class SpecialCharacterToken(val type : SpecialCharacterType, line: Int, column: Int) : EulToken(line, column) {
    override fun getSpecialCharacterType() = this.type
}
