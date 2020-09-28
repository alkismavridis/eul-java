package eu.alkismavridis.euljava.parser.token

import eu.alkismavridis.euljava.core.EulLogger
import eu.alkismavridis.euljava.core.ast.EulToken
import eu.alkismavridis.euljava.core.ast.operators.EulCommentToken
import eu.alkismavridis.euljava.core.ast.operators.SpecialCharacterToken
import eu.alkismavridis.euljava.core.ast.operators.SpecialCharType

/** Returns OperatorToken, ControlCharacterToken, CommentToken or null */
class SpecialCharacterTokenizer(private val logger: EulLogger, private val source: CharacterSource) {
    fun parse(alreadyReadChar: Char, startingLine: Int, startingColumn: Int): EulToken? {
        return when (alreadyReadChar) {
            // operators
            '=' -> this.readEqualsStartingOperator(startingLine, startingColumn)
            '+' -> this.readPlusStartingOperator(startingLine, startingColumn)
            '-' -> this.readMinusStartingOperator(startingLine, startingColumn)
            '*' -> this.readStarStartingOperator(startingLine, startingColumn)
            '/' -> this.readSlashStartingOperatorOrComment(startingLine, startingColumn)
            '.' -> SpecialCharacterToken(SpecialCharType.DOT, startingLine, startingColumn)
            '~' -> SpecialCharacterToken(SpecialCharType.TILDE, startingLine, startingColumn)
            '!' -> this.readNotStartingOperator(startingLine, startingColumn)
            '&' -> this.readAndStartingOperator(startingLine, startingColumn)
            '|' -> this.readOrStartingOperator(startingLine, startingColumn)
            '^' -> this.readXorStartingOperator(startingLine, startingColumn)
            '%' -> this.readPercentStartingOperator(startingLine, startingColumn)
            '#' -> SpecialCharacterToken(SpecialCharType.HASH, startingLine, startingColumn)
            '$' -> SpecialCharacterToken(SpecialCharType.DOLLAR, startingLine, startingColumn)

            // control characters
            '{' -> SpecialCharacterToken(SpecialCharType.CURLY_OPEN, startingLine, startingColumn)
            '}' -> SpecialCharacterToken(SpecialCharType.CURLY_CLOSE, startingLine, startingColumn)
            '(' -> SpecialCharacterToken(SpecialCharType.PARENTHESIS_OPEN, startingLine, startingColumn)
            ')' -> SpecialCharacterToken(SpecialCharType.PARENTHESIS_CLOSE, startingLine, startingColumn)
            '[' -> SpecialCharacterToken(SpecialCharType.SQUARE_OPEN, startingLine, startingColumn)
            ']' -> SpecialCharacterToken(SpecialCharType.SQUARE_CLOSE, startingLine, startingColumn)
            '<' -> this.readSmallerStartingOperator(startingLine, startingColumn)
            '>' -> this.readLargerStartingOperator(startingLine, startingColumn)

            ':' -> SpecialCharacterToken(SpecialCharType.COLON, startingLine, startingColumn)
            ';' -> SpecialCharacterToken(SpecialCharType.SEMICOLON, startingLine, startingColumn)
            '@' -> SpecialCharacterToken(SpecialCharType.AT, startingLine, startingColumn)
            '?' -> SpecialCharacterToken(SpecialCharType.QUESTION_MARK, startingLine, startingColumn)
            ',' -> SpecialCharacterToken(SpecialCharType.COMMA, startingLine, startingColumn)
            '\\' -> SpecialCharacterToken(SpecialCharType.BACKSLASH, startingLine, startingColumn)

            else -> null
        }
    }


    /// OPERATOR EXTENSIONS
    private fun readEqualsStartingOperator(startingLine: Int, startingColumn: Int): EulToken {
        return when (val nextChar = this.source.getNextChar()) {
            '=' -> this.readDoubleEqualsStartingOperator(startingLine, startingColumn)
            else -> {
                this.source.rollBackCharacter(nextChar)
                SpecialCharacterToken(SpecialCharType.EQUALS, startingLine, startingColumn)
            }
        }
    }

    private fun readDoubleEqualsStartingOperator(startingLine: Int, startingColumn: Int): EulToken {
        return when (val nextChar = this.source.getNextChar()) {
            '=' -> SpecialCharacterToken(SpecialCharType.TRIPLE_EQUALS, startingLine, startingColumn)
            else -> {
                this.source.rollBackCharacter(nextChar)
                SpecialCharacterToken(SpecialCharType.DOUBLE_EQUALS, startingLine, startingColumn)
            }
        }
    }

    private fun readNotStartingOperator(startingLine: Int, startingColumn: Int): EulToken {
        return when (val nextChar = this.source.getNextChar()) {
            '=' -> this.readNotEqualsStartingOperator(startingLine, startingColumn)
            else -> {
                this.source.rollBackCharacter(nextChar)
                SpecialCharacterToken(SpecialCharType.NOT, startingLine, startingColumn)
            }
        }
    }

    private fun readNotEqualsStartingOperator(startingLine: Int, startingColumn: Int): EulToken {
        return when (val nextChar = this.source.getNextChar()) {
            '=' -> SpecialCharacterToken(SpecialCharType.NOT_DOUBLE_EQUALS, startingLine, startingColumn)
            else -> {
                this.source.rollBackCharacter(nextChar)
                SpecialCharacterToken(SpecialCharType.NOT_EQUALS, startingLine, startingColumn)
            }
        }
    }

    private fun readPlusStartingOperator(startingLine: Int, startingColumn: Int): EulToken {
        return when (val nextChar = this.source.getNextChar()) {
            '+' -> SpecialCharacterToken(SpecialCharType.DOUBLE_PLUS, startingLine, startingColumn)
            '=' -> SpecialCharacterToken(SpecialCharType.PLUS_EQUALS, startingLine, startingColumn)
            else -> {
                this.source.rollBackCharacter(nextChar)
                SpecialCharacterToken(SpecialCharType.PLUS, startingLine, startingColumn)
            }
        }
    }

    private fun readMinusStartingOperator(startingLine: Int, startingColumn: Int): EulToken {
        return when (val nextChar = this.source.getNextChar()) {
            '-' -> SpecialCharacterToken(SpecialCharType.DOUBLE_MINUS, startingLine, startingColumn)
            '=' -> SpecialCharacterToken(SpecialCharType.MINUS_EQUALS, startingLine, startingColumn)
            '>' -> SpecialCharacterToken(SpecialCharType.ARROW, startingLine, startingColumn)
            else -> {
                this.source.rollBackCharacter(nextChar)
                SpecialCharacterToken(SpecialCharType.MINUS, startingLine, startingColumn)
            }
        }
    }

    private fun readStarStartingOperator(startingLine: Int, startingColumn: Int): EulToken {
        return when (val nextChar = this.source.getNextChar()) {
            '=' -> SpecialCharacterToken(SpecialCharType.STAR_EQUALS, startingLine, startingColumn)
            else -> {
                this.source.rollBackCharacter(nextChar)
                SpecialCharacterToken(SpecialCharType.STAR, startingLine, startingColumn)
            }
        }
    }

    private fun readSlashStartingOperatorOrComment(startingLine: Int, startingColumn: Int): EulToken {
        return when (val nextChar = this.source.getNextChar()) {
            '=' -> SpecialCharacterToken(SpecialCharType.SLASH_EQUALS, startingLine, startingColumn)
            '/' -> this.readSingleLineComment(startingLine, startingColumn)
            '*' -> this.readMultiLineComment(startingLine, startingColumn)
            else -> {
                this.source.rollBackCharacter(nextChar)
                SpecialCharacterToken(SpecialCharType.SLASH, startingLine, startingColumn)
            }
        }
    }

    private fun readLargerStartingOperator(startingLine: Int, startingColumn: Int): EulToken {
        return when (val nextChar = this.source.getNextChar()) {
            '=' -> SpecialCharacterToken(SpecialCharType.GREATER_EQUALS, startingLine, startingColumn)
            else -> {
                this.source.rollBackCharacter(nextChar)
                SpecialCharacterToken(SpecialCharType.GREATER, startingLine, startingColumn)
            }
        }
    }

    private fun readSmallerStartingOperator(startingLine: Int, startingColumn: Int): EulToken {
        return when (val nextChar = this.source.getNextChar()) {
            '=' -> SpecialCharacterToken(SpecialCharType.LESS_EQUALS, startingLine, startingColumn)
            else -> {
                this.source.rollBackCharacter(nextChar)
                SpecialCharacterToken(SpecialCharType.LESS, startingLine, startingColumn)
            }
        }
    }

    private fun readXorStartingOperator(startingLine: Int, startingColumn: Int): EulToken {
        return when (val nextChar = this.source.getNextChar()) {
            '=' -> SpecialCharacterToken(SpecialCharType.XOR_EQUALS, startingLine, startingColumn)
            else -> {
                this.source.rollBackCharacter(nextChar)
                SpecialCharacterToken(SpecialCharType.XOR, startingLine, startingColumn)
            }
        }
    }

    private fun readPercentStartingOperator(startingLine: Int, startingColumn: Int): EulToken {
        return when (val nextChar = this.source.getNextChar()) {
            '=' -> SpecialCharacterToken(SpecialCharType.PERCENT_EQUALS, startingLine, startingColumn)
            else -> {
                this.source.rollBackCharacter(nextChar)
                SpecialCharacterToken(SpecialCharType.PERCENT, startingLine, startingColumn)
            }
        }
    }

    private fun readOrStartingOperator(startingLine: Int, startingColumn: Int): EulToken {
        return when (val nextChar = this.source.getNextChar()) {
            '=' -> SpecialCharacterToken(SpecialCharType.OR_EQUALS, startingLine, startingColumn)
            '|' -> SpecialCharacterToken(SpecialCharType.DOUBLE_OR, startingLine, startingColumn)
            else -> {
                this.source.rollBackCharacter(nextChar)
                SpecialCharacterToken(SpecialCharType.OR, startingLine, startingColumn)
            }
        }
    }

    private fun readAndStartingOperator(startingLine: Int, startingColumn: Int): EulToken {
        return when (val nextChar = this.source.getNextChar()) {
            '=' -> SpecialCharacterToken(SpecialCharType.AND_EQUALS, startingLine, startingColumn)
            '&' -> SpecialCharacterToken(SpecialCharType.DOUBLE_AND, startingLine, startingColumn)
            else -> {
                this.source.rollBackCharacter(nextChar)
                SpecialCharacterToken(SpecialCharType.AND, startingLine, startingColumn)
            }
        }
    }


    /// COMMENT PARSING
    /** This method assumes that the start of the comment has already been read */
    private fun readSingleLineComment(startingLine: Int, startingColumn: Int): EulCommentToken {
        while (true) {
            val nextChar = this.source.getNextChar()
            if (nextChar == '\u0000') {
                return EulCommentToken(startingLine, startingColumn)
            } else if (nextChar == '\n') {
                this.source.rollBackCharacter('\n')
                return EulCommentToken(startingLine, startingColumn)
            }
        }
    }

    /** This method assumes that the start of the comment has already been read */
    private fun readMultiLineComment(startingLine: Int, startingColumn: Int): EulCommentToken {
        var nextChar = this.source.getNextChar()

        while (true) {
            if (nextChar == '\u0000') {
                throw TokenizerException(startingLine, startingColumn, "End of file found while parsing multi-line comment")
            } else if (nextChar == '*') {
                nextChar = this.source.getNextChar()
                if (nextChar == '/') {
                    return EulCommentToken(startingLine, startingColumn)
                }
            } else {
                nextChar = this.source.getNextChar()
            }
        }
    }
}
