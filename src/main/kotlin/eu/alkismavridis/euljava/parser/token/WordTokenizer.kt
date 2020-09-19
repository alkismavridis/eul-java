package eu.alkismavridis.euljava.parser.token

import eu.alkismavridis.euljava.core.EulLogger
import eu.alkismavridis.euljava.core.ast.EulToken
import eu.alkismavridis.euljava.core.ast.expressions.tokens.BooleanLiteral
import eu.alkismavridis.euljava.core.ast.expressions.tokens.EulReference
import eu.alkismavridis.euljava.core.ast.expressions.tokens.NullLiteral
import eu.alkismavridis.euljava.core.ast.keywords.KeywordToken

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
            "var" -> KeywordToken("var", line, column)
            "val" -> KeywordToken("val", line, column)
            "fun" -> KeywordToken("fun", line, column)


            // conditional
            "if" -> KeywordToken("if", line, column)
            "else" -> KeywordToken("else", line, column)
            "switch" -> KeywordToken("switch", line, column)

            //loops
            "for" -> KeywordToken("for", line, column)
            "while" -> KeywordToken("while", line, column)
            "break" -> KeywordToken("break", line, column)
            "continue" -> KeywordToken("continue", line, column)

            // return
            "return" -> KeywordToken("return", line, column)
            "throw" -> KeywordToken("throw", line, column)

            else -> null
        }
    }
}
