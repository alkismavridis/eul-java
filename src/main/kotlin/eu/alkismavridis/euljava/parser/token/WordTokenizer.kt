package eu.alkismavridis.euljava.parser.token

import eu.alkismavridis.euljava.core.EulLogger
import eu.alkismavridis.euljava.core.ast.EulToken
import eu.alkismavridis.euljava.core.ast.expressions.tokens.BooleanLiteral
import eu.alkismavridis.euljava.core.ast.expressions.tokens.EulReference
import eu.alkismavridis.euljava.core.ast.expressions.tokens.NullLiteral
import eu.alkismavridis.euljava.core.ast.keywords.KeywordToken
import eu.alkismavridis.euljava.core.ast.keywords.KeywordType

/** Returns one of: EulReference, KeywordToken, BooleanLiteral, NullLiteral */
class WordTokenizer(private val logger: EulLogger, private val source: CharacterSource) {
    private val builder = StringBuilder()

    fun getNextWordToken(startingLine: Int, startingColumn: Int): EulToken {
        this.builder.setLength(0)
        val endingChar = this.addAllWordCharacters()
        this.source.rollBackCharacter(endingChar)

        val wordAsString = this.builder.toString()
        return this.getAsKeyword(wordAsString, startingLine, startingColumn)
                ?: EulReference(wordAsString, startingLine, startingColumn)
    }

    /** returns the ending character */
    private fun addAllWordCharacters() : Char {
        while (true) {
            val nextChar = this.source.getNextChar()
            if (!CharMetadata.isWordPart(nextChar))  {
                return nextChar
            }

            this.builder.append(nextChar)
        }
    }

    private fun getAsKeyword(word: String, line: Int, column: Int): EulToken? {
        return when(word) {
            // values
            "true" -> BooleanLiteral(true, line, column)
            "false" -> BooleanLiteral(false, line, column)
            "null" -> NullLiteral(line, column)

            // definitions
            "let" -> KeywordToken(KeywordType.LET, line, column)
            "const" -> KeywordToken(KeywordType.CONST, line, column)
            "fun" -> KeywordToken(KeywordType.FUN, line, column)


            // conditional
            "if" -> KeywordToken(KeywordType.IF, line, column)
            "else" -> KeywordToken(KeywordType.ELSE, line, column)
            "switch" -> KeywordToken(KeywordType.SWITCH, line, column)

            //loops
            "for" -> KeywordToken(KeywordType.FOR, line, column)
            "while" -> KeywordToken(KeywordType.WHILE, line, column)
            "break" -> KeywordToken(KeywordType.BREAK, line, column)
            "continue" -> KeywordToken(KeywordType.CONTINUE, line, column)

            // return
            "return" -> KeywordToken(KeywordType.RETURN, line, column)
            "throw" -> KeywordToken(KeywordType.THROW, line, column)

            else -> null
        }
    }
}
