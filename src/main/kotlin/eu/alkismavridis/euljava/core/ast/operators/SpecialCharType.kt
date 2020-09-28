package eu.alkismavridis.euljava.core.ast.operators

enum class SpecialCharType(val prefixPriority: Int, isPrefixRtl: Boolean, val infixPriority: Int, val isInfixRtl: Boolean, val suffixPriority: Int, isSuffixRtl: Boolean) {
    NOT_A_SPECIAL_CHARACTER(-1, false, -1, false, -1, false),
    DOT(-1, false, 20, false, -1, false),
    TILDE(17, true, -1, false, -1, false),
    HASH(-1, false, -1, false, -1, false),
    DOLLAR(-1, false, -1, false, -1, false),
    PARENTHESIS_OPEN(-1, false, -1, false, 20, false),
    PARENTHESIS_CLOSE(-1, false, -1, false, -1, false),
    SQUARE_OPEN(-1, false, -1, false, 20, false),
    SQUARE_CLOSE(-1, false, -1, false, -1, false),
    CURLY_OPEN(-1, false, -1, false, -1, false),
    CURLY_CLOSE(-1, false, -1, false, -1, false),
    COLON(-1, false, -1, false, -1, false),
    SEMICOLON(-1, false, -1, false, -1, false),
    AT(-1, false, -1, false, -1, false),
    QUESTION_MARK(-1, false, -1, false, -1, false),
    COMMA(-1, false, -1, false, -1, false),
    BACKSLASH(-1, false, -1, false, -1, false),
    EQUALS(-1, false, 3, true, -1, false),
    DOUBLE_EQUALS(-1, false, 11, false, -1, false),
    TRIPLE_EQUALS(-1, false, 11, false, -1, false),
    NOT(17, true, -1, false, -1, false),
    NOT_EQUALS(-1, false, 11, false, -1, false),
    NOT_DOUBLE_EQUALS(-1, false, 11, false, -1, false),
    PLUS(17, true, 14, false, -1, false),
    DOUBLE_PLUS(17, true, -1, false, 18, false),
    PLUS_EQUALS(-1, false, 3, true, -1, false),
    MINUS(17, true, 14, false, -1, false),
    DOUBLE_MINUS(17, true, -1, false, 18, false),
    MINUS_EQUALS(-1, false, 3, true, -1, false),
    ARROW(-1, false, -1, false, -1, false),
    STAR(-1, false, 15, false, -1, false),
    STAR_EQUALS(-1, false, 3, true, -1, false),
    SLASH(-1, false, 15, false, -1, false),
    SLASH_EQUALS(-1, false, 3, true, -1, false),
    GREATER(-1, false, 12, false, -1, false),
    GREATER_EQUALS(-1, false, 12, false, -1, false),
    LESS(-1, false, 12, false, -1, false),
    LESS_EQUALS(-1, false, 12, false, -1, false),
    XOR(-1, false, 9, false, -1, false),
    XOR_EQUALS(-1, false, 3, true, -1, false),
    PERCENT(-1, false, 15, false, -1, false),
    PERCENT_EQUALS(-1, false, 3, true, -1, false),
    OR(-1, false, 8, false, -1, false),
    DOUBLE_OR(-1, false, 6, false, -1, false),
    OR_EQUALS(-1, false, 3, true, -1, false),
    AND(-1, false, 10, false, -1, false),
    DOUBLE_AND(-1, false, 7, false, -1, false),
    AND_EQUALS(-1, false, 3, true, -1, false),
    NEW_LINE(-1, false, -1, false, -1, false);

    fun isPrefix() = this.prefixPriority > 0
    fun isInfix() = this.infixPriority > 0
    fun isSuffix() = this.suffixPriority > 0

    fun isInfixOrPrefix(): Boolean {
        return this.prefixPriority > 0 || this.infixPriority > 0
    }
}
