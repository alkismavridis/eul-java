package eu.alkismavridis.euljava.parser.token

import eu.alkismavridis.euljava.core.EulLogger
import eu.alkismavridis.euljava.core.ast.EulToken
import eu.alkismavridis.euljava.core.ast.operators.EulCommentToken
import eu.alkismavridis.euljava.core.ast.operators.SpecialCharacterToken
import eu.alkismavridis.euljava.core.ast.operators.SpecialCharacterType

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
            '.' -> SpecialCharacterToken(SpecialCharacterType.DOT, startingLine, startingColumn)
            '~' -> SpecialCharacterToken(SpecialCharacterType.TILDE, startingLine, startingColumn)
            '!' -> this.readNotStartingOperator(startingLine, startingColumn)
            '&' -> this.readAndStartingOperator(startingLine, startingColumn)
            '|' -> this.readOrStartingOperator(startingLine, startingColumn)
            '^' -> this.readXorStartingOperator(startingLine, startingColumn)
            '%' -> this.readPercentStartingOperator(startingLine, startingColumn)
            '#' -> SpecialCharacterToken(SpecialCharacterType.HASH, startingLine, startingColumn)
            '$' -> SpecialCharacterToken(SpecialCharacterType.DOLLAR, startingLine, startingColumn)

            // control characters
            '{' -> SpecialCharacterToken(SpecialCharacterType.CURLY_OPEN, startingLine, startingColumn)
            '}' -> SpecialCharacterToken(SpecialCharacterType.CURLY_CLOSE, startingLine, startingColumn)
            '(' -> SpecialCharacterToken(SpecialCharacterType.PARENTHESIS_OPEN, startingLine, startingColumn)
            ')' -> SpecialCharacterToken(SpecialCharacterType.PARENTHESIS_CLOSE, startingLine, startingColumn)
            '[' -> SpecialCharacterToken(SpecialCharacterType.SQUARE_OPEN, startingLine, startingColumn)
            ']' -> SpecialCharacterToken(SpecialCharacterType.SQUARE_CLOSE, startingLine, startingColumn)
            '<' -> this.readSmallerStartingOperator(startingLine, startingColumn)
            '>' -> this.readLargerStartingOperator(startingLine, startingColumn)

            ':' -> SpecialCharacterToken(SpecialCharacterType.COLON, startingLine, startingColumn)
            ';' -> SpecialCharacterToken(SpecialCharacterType.SEMICOLON, startingLine, startingColumn)
            '@' -> SpecialCharacterToken(SpecialCharacterType.AT, startingLine, startingColumn)
            '?' -> SpecialCharacterToken(SpecialCharacterType.QUESTION_MARK, startingLine, startingColumn)
            ',' -> SpecialCharacterToken(SpecialCharacterType.COMMA, startingLine, startingColumn)
            '\\' -> SpecialCharacterToken(SpecialCharacterType.BACKSLASH, startingLine, startingColumn)

            else -> null
        }
    }


    /// OPERATOR EXTENSIONS
    private fun readEqualsStartingOperator(startingLine: Int, startingColumn: Int): EulToken {
        return when (val nextChar = this.source.getNextChar()) {
            '=' -> this.readDoubleEqualsStartingOperator(startingLine, startingColumn)
            else -> {
                this.source.rollBackCharacter(nextChar)
                SpecialCharacterToken(SpecialCharacterType.EQUALS, startingLine, startingColumn)
            }
        }
    }

    private fun readDoubleEqualsStartingOperator(startingLine: Int, startingColumn: Int): EulToken {
        return when (val nextChar = this.source.getNextChar()) {
            '=' -> SpecialCharacterToken(SpecialCharacterType.TRIPLE_EQUALS, startingLine, startingColumn)
            else -> {
                this.source.rollBackCharacter(nextChar)
                SpecialCharacterToken(SpecialCharacterType.DOUBLE_EQUALS, startingLine, startingColumn)
            }
        }
    }

    private fun readNotStartingOperator(startingLine: Int, startingColumn: Int): EulToken {
        return when (val nextChar = this.source.getNextChar()) {
            '=' -> this.readNotEqualsStartingOperator(startingLine, startingColumn)
            else -> {
                this.source.rollBackCharacter(nextChar)
                SpecialCharacterToken(SpecialCharacterType.NOT, startingLine, startingColumn)
            }
        }
    }

    private fun readNotEqualsStartingOperator(startingLine: Int, startingColumn: Int): EulToken {
        return when (val nextChar = this.source.getNextChar()) {
            '=' -> SpecialCharacterToken(SpecialCharacterType.NOT_DOUBLE_EQUALS, startingLine, startingColumn)
            else -> {
                this.source.rollBackCharacter(nextChar)
                SpecialCharacterToken(SpecialCharacterType.NOT_EQUALS, startingLine, startingColumn)
            }
        }
    }

    private fun readPlusStartingOperator(startingLine: Int, startingColumn: Int): EulToken {
        return when (val nextChar = this.source.getNextChar()) {
            '+' -> SpecialCharacterToken(SpecialCharacterType.DOUBLE_PLUS, startingLine, startingColumn)
            '=' -> SpecialCharacterToken(SpecialCharacterType.PLUS_EQUALS, startingLine, startingColumn)
            else -> {
                this.source.rollBackCharacter(nextChar)
                SpecialCharacterToken(SpecialCharacterType.PLUS, startingLine, startingColumn)
            }
        }
    }

    private fun readMinusStartingOperator(startingLine: Int, startingColumn: Int): EulToken {
        return when (val nextChar = this.source.getNextChar()) {
            '-' -> SpecialCharacterToken(SpecialCharacterType.DOUBLE_MINUS, startingLine, startingColumn)
            '=' -> SpecialCharacterToken(SpecialCharacterType.MINUS_EQUALS, startingLine, startingColumn)
            '>' -> SpecialCharacterToken(SpecialCharacterType.ARROW, startingLine, startingColumn)
            else -> {
                this.source.rollBackCharacter(nextChar)
                SpecialCharacterToken(SpecialCharacterType.MINUS, startingLine, startingColumn)
            }
        }
    }

    private fun readStarStartingOperator(startingLine: Int, startingColumn: Int): EulToken {
        return when (val nextChar = this.source.getNextChar()) {
            '=' -> SpecialCharacterToken(SpecialCharacterType.STAR_EQUALS, startingLine, startingColumn)
            else -> {
                this.source.rollBackCharacter(nextChar)
                SpecialCharacterToken(SpecialCharacterType.STAR, startingLine, startingColumn)
            }
        }
    }

    private fun readSlashStartingOperatorOrComment(startingLine: Int, startingColumn: Int): EulToken {
        return when (val nextChar = this.source.getNextChar()) {
            '=' -> SpecialCharacterToken(SpecialCharacterType.SLASH_EQUALS, startingLine, startingColumn)
            '/' -> this.readSingleLineComment(startingLine, startingColumn)
            '*' -> this.readMultiLineComment(startingLine, startingColumn)
            else -> {
                this.source.rollBackCharacter(nextChar)
                SpecialCharacterToken(SpecialCharacterType.SLASH, startingLine, startingColumn)
            }
        }
    }

    private fun readLargerStartingOperator(startingLine: Int, startingColumn: Int): EulToken {
        return when (val nextChar = this.source.getNextChar()) {
            '=' -> SpecialCharacterToken(SpecialCharacterType.GREATER_EQUALS, startingLine, startingColumn)
            else -> {
                this.source.rollBackCharacter(nextChar)
                SpecialCharacterToken(SpecialCharacterType.GREATER, startingLine, startingColumn)
            }
        }
    }

    private fun readSmallerStartingOperator(startingLine: Int, startingColumn: Int): EulToken {
        return when (val nextChar = this.source.getNextChar()) {
            '=' -> SpecialCharacterToken(SpecialCharacterType.LESS_EQUALS, startingLine, startingColumn)
            else -> {
                this.source.rollBackCharacter(nextChar)
                SpecialCharacterToken(SpecialCharacterType.LESS, startingLine, startingColumn)
            }
        }
    }

    private fun readXorStartingOperator(startingLine: Int, startingColumn: Int): EulToken {
        return when (val nextChar = this.source.getNextChar()) {
            '=' -> SpecialCharacterToken(SpecialCharacterType.XOR_EQUALS, startingLine, startingColumn)
            else -> {
                this.source.rollBackCharacter(nextChar)
                SpecialCharacterToken(SpecialCharacterType.XOR, startingLine, startingColumn)
            }
        }
    }

    private fun readPercentStartingOperator(startingLine: Int, startingColumn: Int): EulToken {
        return when (val nextChar = this.source.getNextChar()) {
            '=' -> SpecialCharacterToken(SpecialCharacterType.PERCENT_EQUALS, startingLine, startingColumn)
            else -> {
                this.source.rollBackCharacter(nextChar)
                SpecialCharacterToken(SpecialCharacterType.PERCENT, startingLine, startingColumn)
            }
        }
    }

    private fun readOrStartingOperator(startingLine: Int, startingColumn: Int): EulToken {
        return when (val nextChar = this.source.getNextChar()) {
            '=' -> SpecialCharacterToken(SpecialCharacterType.OR_EQUALS, startingLine, startingColumn)
            '|' -> SpecialCharacterToken(SpecialCharacterType.DOUBLE_OR, startingLine, startingColumn)
            else -> {
                this.source.rollBackCharacter(nextChar)
                SpecialCharacterToken(SpecialCharacterType.OR, startingLine, startingColumn)
            }
        }
    }

    private fun readAndStartingOperator(startingLine: Int, startingColumn: Int): EulToken {
        return when (val nextChar = this.source.getNextChar()) {
            '=' -> SpecialCharacterToken(SpecialCharacterType.AND_EQUALS, startingLine, startingColumn)
            '&' -> SpecialCharacterToken(SpecialCharacterType.DOUBLE_AND, startingLine, startingColumn)
            else -> {
                this.source.rollBackCharacter(nextChar)
                SpecialCharacterToken(SpecialCharacterType.AND, startingLine, startingColumn)
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
