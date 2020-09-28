package eu.alkismavridis.euljava.core.ast

import eu.alkismavridis.euljava.core.ast.keywords.KeywordType
import eu.alkismavridis.euljava.core.ast.operators.SpecialCharType

abstract class EulToken (
        val line: Int,
        val column: Int
) {
    open fun getKeywordType() = KeywordType.NOT_A_KEYWORD
    open fun getSpecialCharType() = SpecialCharType.NOT_A_SPECIAL_CHARACTER
}
