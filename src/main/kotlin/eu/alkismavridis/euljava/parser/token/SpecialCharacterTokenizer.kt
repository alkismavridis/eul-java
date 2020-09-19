package eu.alkismavridis.euljava.parser.token

import eu.alkismavridis.euljava.core.EulLogger
import eu.alkismavridis.euljava.core.ast.EulToken
import eu.alkismavridis.euljava.core.ast.operators.EulCommentToken
import eu.alkismavridis.euljava.core.ast.operators.SpecialCharacterToken

/** Returns OperatorToken, ControlCharacterToken, CommentToken or null */
class SpecialCharacterTokenizer(private val logger: EulLogger, private val source: CharacterSource) {
    fun parse(alreadyReadChar: Char, startingLine: Int, startingColumn: Int) : EulToken? {
        return when(alreadyReadChar) {
            // operators
            '=' -> this.readEqualsStartingOperator(startingLine, startingColumn)
            '+' -> this.readPlusStartingOperator(startingLine, startingColumn)
            '-' -> this.readMinusStartingOperator(startingLine, startingColumn)
            '*' -> this.readStarStartingOperator(startingLine, startingColumn)
            '/' -> this.readSlashStartingOperatorOrComment(startingLine, startingColumn)
            '.' -> SpecialCharacterToken(".", startingLine, startingColumn)
            '~' -> SpecialCharacterToken("~", startingLine, startingColumn)
            '!' -> this.readNotStartingOperator(startingLine, startingColumn)
            '&' -> this.readAndStartingOperator(startingLine, startingColumn)
            '|' -> this.readOrStartingOperator(startingLine, startingColumn)
            '^' -> this.readXorStartingOperator(startingLine, startingColumn)
            '%' -> this.readPercentStartingOperator(startingLine, startingColumn)
            '#' -> SpecialCharacterToken("#", startingLine, startingColumn)
            '$' -> SpecialCharacterToken("$", startingLine, startingColumn)

            // control characters
            '{' -> SpecialCharacterToken("{", startingLine, startingColumn)
            '}' -> SpecialCharacterToken("}", startingLine, startingColumn)
            '(' -> SpecialCharacterToken("(", startingLine, startingColumn)
            ')' -> SpecialCharacterToken(")", startingLine, startingColumn)
            '[' -> SpecialCharacterToken("[", startingLine, startingColumn)
            ']' -> SpecialCharacterToken("]", startingLine, startingColumn)
            '<' -> this.readSmallerStartingOperator(startingLine, startingColumn)
            '>' -> this.readLargerStartingOperator(startingLine, startingColumn)

            ':' -> SpecialCharacterToken(":", startingLine, startingColumn)
            ';' -> SpecialCharacterToken(";", startingLine, startingColumn)
            '@' -> SpecialCharacterToken("@", startingLine, startingColumn)
            '?' -> SpecialCharacterToken("?", startingLine, startingColumn)
            ',' -> SpecialCharacterToken(",", startingLine, startingColumn)
            '\\' -> SpecialCharacterToken("\\", startingLine, startingColumn)

            else -> null
        }
    }


    /// OPERATOR EXTENSIONS
    private fun readEqualsStartingOperator(startingLine: Int, startingColumn: Int): EulToken {
        return when(val nextChar = this.source.getNextChar()) {
            '=' -> this.readDoubleEqualsStartingOperator(startingLine, startingColumn)
            else -> {
                this.source.rollBackCharacter(nextChar)
                SpecialCharacterToken("=", startingLine, startingColumn)
            }
        }
    }

    private fun readDoubleEqualsStartingOperator(startingLine: Int, startingColumn: Int): EulToken {
        return when(val nextChar = this.source.getNextChar()) {
            '=' -> SpecialCharacterToken("===", startingLine, startingColumn)
            else -> {
                this.source.rollBackCharacter(nextChar)
                SpecialCharacterToken("==", startingLine, startingColumn)
            }
        }
    }

    private fun readNotStartingOperator(startingLine: Int, startingColumn: Int): EulToken {
        return when(val nextChar = this.source.getNextChar()) {
            '=' -> this.readNotEqualsStartingOperator(startingLine, startingColumn)
            else -> {
                this.source.rollBackCharacter(nextChar)
                SpecialCharacterToken("!", startingLine, startingColumn)
            }
        }
    }

    private fun readNotEqualsStartingOperator(startingLine: Int, startingColumn: Int): EulToken {
        return when(val nextChar = this.source.getNextChar()) {
            '=' -> SpecialCharacterToken("!==", startingLine, startingColumn)
            else -> {
                this.source.rollBackCharacter(nextChar)
                SpecialCharacterToken("!=", startingLine, startingColumn)
            }
        }
    }

    private fun readPlusStartingOperator(startingLine: Int, startingColumn: Int): EulToken {
        return when(val nextChar = this.source.getNextChar()) {
            '+' -> SpecialCharacterToken("++", startingLine, startingColumn)
            '=' -> SpecialCharacterToken("+=", startingLine, startingColumn)
            else -> {
                this.source.rollBackCharacter(nextChar)
                SpecialCharacterToken("+", startingLine, startingColumn)
            }
        }
    }

    private fun readMinusStartingOperator(startingLine: Int, startingColumn: Int): EulToken {
        return when(val nextChar = this.source.getNextChar()) {
            '-' -> SpecialCharacterToken("--", startingLine, startingColumn)
            '=' -> SpecialCharacterToken("-=", startingLine, startingColumn)
            '>' -> SpecialCharacterToken("->", startingLine, startingColumn)
            else -> {
                this.source.rollBackCharacter(nextChar)
                SpecialCharacterToken("-", startingLine, startingColumn)
            }
        }
    }

    private fun readStarStartingOperator(startingLine: Int, startingColumn: Int): EulToken {
        return when(val nextChar = this.source.getNextChar()) {
            '=' -> SpecialCharacterToken("*=", startingLine, startingColumn)
            else -> {
                this.source.rollBackCharacter(nextChar)
                SpecialCharacterToken("*", startingLine, startingColumn)
            }
        }
    }

    private fun readSlashStartingOperatorOrComment(startingLine: Int, startingColumn: Int): EulToken {
        return when(val nextChar = this.source.getNextChar()) {
            '=' -> SpecialCharacterToken("/=", startingLine, startingColumn)
            '/' -> this.readSingleLineComment(startingLine, startingColumn)
            '*' -> this.readMultiLineComment(startingLine, startingColumn)
            else -> {
                this.source.rollBackCharacter(nextChar)
                SpecialCharacterToken("/", startingLine, startingColumn)
            }
        }
    }

    private fun readLargerStartingOperator(startingLine: Int, startingColumn: Int): EulToken {
        return when(val nextChar = this.source.getNextChar()) {
            '=' -> SpecialCharacterToken(">=", startingLine, startingColumn)
            else -> {
                this.source.rollBackCharacter(nextChar)
                SpecialCharacterToken(">", startingLine, startingColumn)
            }
        }
    }

    private fun readSmallerStartingOperator(startingLine: Int, startingColumn: Int): EulToken {
        return when(val nextChar = this.source.getNextChar()) {
            '=' -> SpecialCharacterToken("<=", startingLine, startingColumn)
            else -> {
                this.source.rollBackCharacter(nextChar)
                SpecialCharacterToken("<", startingLine, startingColumn)
            }
        }
    }

    private fun readXorStartingOperator(startingLine: Int, startingColumn: Int): EulToken {
        return when(val nextChar = this.source.getNextChar()) {
            '=' -> SpecialCharacterToken("^=", startingLine, startingColumn)
            else -> {
                this.source.rollBackCharacter(nextChar)
                SpecialCharacterToken("^", startingLine, startingColumn)
            }
        }
    }

    private fun readPercentStartingOperator(startingLine: Int, startingColumn: Int): EulToken {
        return when(val nextChar = this.source.getNextChar()) {
            '=' -> SpecialCharacterToken("%=", startingLine, startingColumn)
            else -> {
                this.source.rollBackCharacter(nextChar)
                SpecialCharacterToken("%", startingLine, startingColumn)
            }
        }
    }

    private fun readOrStartingOperator(startingLine: Int, startingColumn: Int): EulToken {
        return when(val nextChar = this.source.getNextChar()) {
            '=' -> SpecialCharacterToken("|=", startingLine, startingColumn)
            '|' -> SpecialCharacterToken("||", startingLine, startingColumn)
            else -> {
                this.source.rollBackCharacter(nextChar)
                SpecialCharacterToken("|", startingLine, startingColumn)
            }
        }
    }

    private fun readAndStartingOperator(startingLine: Int, startingColumn: Int): EulToken {
        return when(val nextChar = this.source.getNextChar()) {
            '=' -> SpecialCharacterToken("&=", startingLine, startingColumn)
            '&' -> SpecialCharacterToken("&&", startingLine, startingColumn)
            else -> {
                this.source.rollBackCharacter(nextChar)
                SpecialCharacterToken("&", startingLine, startingColumn)
            }
        }
    }


    /// COMMENT PARSING
    /** This method assumes that the start of the comment has already been read */
    private fun readSingleLineComment(startingLine: Int, startingColumn: Int): EulCommentToken {
        while(true) {
            val nextChar = this.source.getNextChar()
            if (nextChar == '\u0000' || nextChar == '\n') {
                return EulCommentToken(startingLine, startingColumn)
            }
        }
    }

    /** This method assumes that the start of the comment has already been read */
    private fun readMultiLineComment(startingLine: Int, startingColumn: Int): EulCommentToken {
        var nextChar = this.source.getNextChar()

        while(true) {
            if (nextChar == '\u0000') {
                throw TokenizerException(startingLine, startingColumn, "End of file found while parsing multi-line comment")
            }

            else if (nextChar == '*') {
                nextChar = this.source.getNextChar()
                if (nextChar == '/') {
                    return EulCommentToken(startingLine, startingColumn)
                }
            }

            else {
                nextChar = this.source.getNextChar()
            }
        }
    }
}
