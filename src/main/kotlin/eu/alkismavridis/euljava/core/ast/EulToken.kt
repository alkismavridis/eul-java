package eu.alkismavridis.euljava.core.ast

import eu.alkismavridis.euljava.core.ast.keywords.KeywordType

abstract class EulToken (
        val line: Int,
        val column: Int
) {
    open fun getKeywordType() = KeywordType.NOT_A_KEYWORD
}
