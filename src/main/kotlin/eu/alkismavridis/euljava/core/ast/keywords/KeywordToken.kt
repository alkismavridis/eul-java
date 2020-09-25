package eu.alkismavridis.euljava.core.ast.keywords

import eu.alkismavridis.euljava.core.ast.EulToken

class KeywordToken(val type: KeywordType, line: Int, column: Int) : EulToken(line, column) {
    override fun getKeywordType() = this.type
}
