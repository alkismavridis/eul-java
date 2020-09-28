package eu.alkismavridis.euljava.parser.expressions

import eu.alkismavridis.euljava.core.ast.EulToken
import eu.alkismavridis.euljava.core.ast.operators.SpecialCharType
import eu.alkismavridis.euljava.parser.ParserException

enum class CloseStatus {
    MIDDLE_OF_EXPRESSION,
    END_OF_EXPRESSION,
    END_IF_NO_INFIX_FOLLOWS
}

enum class ExpressionBreaker(val ignoresNewLines: Boolean) {
    /** Ends only in \n and semicolon. Rejects commas and closing braces. Useful for return statements or similar.  */
    STATEMENT_EXPRESSION(false) {
        override fun getClosingStatusFor(token: EulToken?): CloseStatus {
            if (token == null) return CloseStatus.END_OF_EXPRESSION

            return when (token.getSpecialCharType()) {
                SpecialCharType.NEW_LINE -> CloseStatus.END_IF_NO_INFIX_FOLLOWS
                SpecialCharType.SEMICOLON -> CloseStatus.END_OF_EXPRESSION

                SpecialCharType.COMMA,
                SpecialCharType.PARENTHESIS_CLOSE,
                SpecialCharType.SQUARE_CLOSE,
                SpecialCharType.CURLY_CLOSE -> throw ParserException(token.line, token.column, "Illegal end of statement")

                else -> CloseStatus.MIDDLE_OF_EXPRESSION
            }
        }
    },

    /** Ends only in \n and semicolon and comma. Rejects commas and closing braces. Useful for variable declarations or similar */
    COMMA_SEPARATED_EXPRESSION(false) {
        override fun getClosingStatusFor(token: EulToken?): CloseStatus {
            if (token == null) return CloseStatus.END_OF_EXPRESSION
            return when (token.getSpecialCharType()) {
                SpecialCharType.NEW_LINE -> CloseStatus.END_IF_NO_INFIX_FOLLOWS
                SpecialCharType.SEMICOLON,
                SpecialCharType.COMMA -> CloseStatus.END_OF_EXPRESSION

                SpecialCharType.PARENTHESIS_CLOSE,
                SpecialCharType.SQUARE_CLOSE,
                SpecialCharType.CURLY_CLOSE -> throw ParserException(token.line, token.column, "Illegal end of statement")

                else -> CloseStatus.MIDDLE_OF_EXPRESSION
            }
        }
    },

    PARENTHESIS(true) {
        override fun getClosingStatusFor(token: EulToken?): CloseStatus {
            if (token == null) throw ParserException(-1, -1, "End of file reached waiting for closing closing ')'")
            return when (token.getSpecialCharType()) {
                SpecialCharType.PARENTHESIS_CLOSE -> CloseStatus.END_OF_EXPRESSION

                SpecialCharType.SEMICOLON,
                SpecialCharType.SQUARE_CLOSE,
                SpecialCharType.COMMA,
                SpecialCharType.CURLY_CLOSE -> throw ParserException(token.line, token.column, "Expected closing ')'")

                else -> CloseStatus.MIDDLE_OF_EXPRESSION
            }
        }
    },

    COMMA_SEPARATED_PARENTHESIS(true) {
        override fun getClosingStatusFor(token: EulToken?): CloseStatus {
            if (token == null) throw ParserException(-1, -1, "End of file reached waiting for closing closing ')'")
            return when (token.getSpecialCharType()) {
                SpecialCharType.COMMA,
                SpecialCharType.PARENTHESIS_CLOSE -> CloseStatus.END_OF_EXPRESSION

                SpecialCharType.SEMICOLON,
                SpecialCharType.SQUARE_CLOSE,
                SpecialCharType.CURLY_CLOSE -> throw ParserException(token.line, token.column, "Expected closing ')'")

                else -> CloseStatus.MIDDLE_OF_EXPRESSION
            }
        }
    },

    SQUARE_BRACE(true) {
        override fun getClosingStatusFor(token: EulToken?): CloseStatus {
            if (token == null) throw ParserException(-1, -1, "End of file reached waiting for closing closing ']'")

            return when (token.getSpecialCharType()) {
                SpecialCharType.COMMA,
                SpecialCharType.SQUARE_CLOSE -> CloseStatus.END_OF_EXPRESSION

                SpecialCharType.SEMICOLON,
                SpecialCharType.CURLY_CLOSE,
                SpecialCharType.PARENTHESIS_CLOSE -> throw ParserException(token.line, token.column, "Expected closing ']'")

                else -> CloseStatus.MIDDLE_OF_EXPRESSION
            }
        }
    };

    abstract fun getClosingStatusFor(token: EulToken?): CloseStatus
}
