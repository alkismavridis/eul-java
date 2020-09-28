package eu.alkismavridis.euljava.parser.token

import eu.alkismavridis.euljava.core.CompileOptions
import eu.alkismavridis.euljava.core.EulLogger
import eu.alkismavridis.euljava.core.ast.keywords.KeywordType
import eu.alkismavridis.euljava.core.ast.operators.SpecialCharType
import eu.alkismavridis.euljava.test_utils.EulAssert.Companion.assertBooleanLiteral
import eu.alkismavridis.euljava.test_utils.EulAssert.Companion.assertCharLiteral
import eu.alkismavridis.euljava.test_utils.EulAssert.Companion.assertEulReference
import eu.alkismavridis.euljava.test_utils.EulAssert.Companion.assertKeyword
import eu.alkismavridis.euljava.test_utils.EulAssert.Companion.assertNullLiteral
import eu.alkismavridis.euljava.test_utils.EulAssert.Companion.assertSpecialCharacter
import eu.alkismavridis.euljava.test_utils.EulAssert.Companion.assertStringLiteral
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import java.io.StringReader

internal class WordTokenizerTest {
    private val logger = mock(EulLogger::class.java)
    private val options = CompileOptions("")

    @Test
    fun shouldStopOnWhiteSpace() {
        val tokenizer = this.createTokenizer("hello world")

        assertEulReference(tokenizer.getNextToken(true), "hello", 1, 1)
        assertEulReference(tokenizer.getNextToken(true), "world", 1, 7)
    }

    @Test
    fun shouldAllowNumbers() {
        val tokenizer = this.createTokenizer("hell9o world7")

        assertEulReference(tokenizer.getNextToken(true), "hell9o", 1, 1)
        assertEulReference(tokenizer.getNextToken(true), "world7", 1, 8)
    }

    @Test
    fun shouldAllowUnderscores() {
        val tokenizer = this.createTokenizer("_hell_o world__")

        assertEulReference(tokenizer.getNextToken(true), "_hell_o", 1, 1)
        assertEulReference(tokenizer.getNextToken(true), "world__", 1, 9)
    }

    @Test
    fun shouldEndAtNewLine() {
        val tokenizer = this.createTokenizer("hello\nworld")

        assertEulReference(tokenizer.getNextToken(true), "hello", 1, 1)
        assertEulReference(tokenizer.getNextToken(true), "world", 2, 1)
    }

    @Test
    fun shouldEndAtWhiteSpace() {
        val tokenizer = this.createTokenizer(
            "space tab\u0009verticalTab\u000bformFeed\u000ccarriageReturn\u000dendOfFile"
        )

        assertEulReference(tokenizer.getNextToken(true), "space", 1, 1)
        assertEulReference(tokenizer.getNextToken(true), "tab", 1, 7)
        assertEulReference(tokenizer.getNextToken(true), "verticalTab", 1, 11)
        assertEulReference(tokenizer.getNextToken(true), "formFeed", 1, 23)
        assertEulReference(tokenizer.getNextToken(true), "carriageReturn", 1, 32)
        assertEulReference(tokenizer.getNextToken(true), "endOfFile", 1, 47)
    }

    @Test
    fun shouldEndAtSpecialCharacters() {
        val tokenizer = this.createTokenizer(
            "tilde~exclamation!at@hash#dollar\$percent%xor^and&star*\n" +
                    "parenthesisOpen(parenthesisClose)minus-plus+equals=curlyOpen{curlyClose}squareOpen[squareClose]\n" +
                    "backslash\\or|colon:semicolon;singleQuote'a'doubleQuote\"hi\"comma,smaller<dot.greater>question?slash/"
        )

        // first line
        assertEulReference(tokenizer.getNextToken(true), "tilde", 1, 1)
        assertSpecialCharacter(tokenizer.getNextToken(true), SpecialCharType.TILDE, 1, 6)

        assertEulReference(tokenizer.getNextToken(true), "exclamation", 1, 7)
        assertSpecialCharacter(tokenizer.getNextToken(true), SpecialCharType.NOT, 1, 18)

        assertEulReference(tokenizer.getNextToken(true), "at", 1, 19)
        assertSpecialCharacter(tokenizer.getNextToken(true), SpecialCharType.AT, 1, 21)

        assertEulReference(tokenizer.getNextToken(true), "hash", 1, 22)
        assertSpecialCharacter(tokenizer.getNextToken(true), SpecialCharType.HASH, 1, 26)

        assertEulReference(tokenizer.getNextToken(true), "dollar", 1, 27)
        assertSpecialCharacter(tokenizer.getNextToken(true), SpecialCharType.DOLLAR, 1, 33)

        assertEulReference(tokenizer.getNextToken(true), "percent", 1, 34)
        assertSpecialCharacter(tokenizer.getNextToken(true), SpecialCharType.PERCENT, 1, 41)

        assertEulReference(tokenizer.getNextToken(true), "xor", 1, 42)
        assertSpecialCharacter(tokenizer.getNextToken(true), SpecialCharType.XOR, 1, 45)

        assertEulReference(tokenizer.getNextToken(true), "and", 1, 46)
        assertSpecialCharacter(tokenizer.getNextToken(true), SpecialCharType.AND, 1, 49)

        assertEulReference(tokenizer.getNextToken(true), "star", 1, 50)
        assertSpecialCharacter(tokenizer.getNextToken(true), SpecialCharType.STAR, 1, 54)

        // second line
        assertEulReference(tokenizer.getNextToken(true), "parenthesisOpen", 2, 1)
        assertSpecialCharacter(tokenizer.getNextToken(true), SpecialCharType.PARENTHESIS_OPEN, 2, 16)

        assertEulReference(tokenizer.getNextToken(true), "parenthesisClose", 2, 17)
        assertSpecialCharacter(tokenizer.getNextToken(true), SpecialCharType.PARENTHESIS_CLOSE, 2, 33)

        assertEulReference(tokenizer.getNextToken(true), "minus", 2, 34)
        assertSpecialCharacter(tokenizer.getNextToken(true), SpecialCharType.MINUS, 2, 39)

        assertEulReference(tokenizer.getNextToken(true), "plus", 2, 40)
        assertSpecialCharacter(tokenizer.getNextToken(true), SpecialCharType.PLUS, 2, 44)

        assertEulReference(tokenizer.getNextToken(true), "equals", 2, 45)
        assertSpecialCharacter(tokenizer.getNextToken(true), SpecialCharType.EQUALS, 2, 51)

        assertEulReference(tokenizer.getNextToken(true), "curlyOpen", 2, 52)
        assertSpecialCharacter(tokenizer.getNextToken(true), SpecialCharType.CURLY_OPEN, 2, 61)

        assertEulReference(tokenizer.getNextToken(true), "curlyClose", 2, 62)
        assertSpecialCharacter(tokenizer.getNextToken(true), SpecialCharType.CURLY_CLOSE, 2, 72)

        assertEulReference(tokenizer.getNextToken(true), "squareOpen", 2, 73)
        assertSpecialCharacter(tokenizer.getNextToken(true), SpecialCharType.SQUARE_OPEN, 2, 83)

        assertEulReference(tokenizer.getNextToken(true), "squareClose", 2, 84)
        assertSpecialCharacter(tokenizer.getNextToken(true), SpecialCharType.SQUARE_CLOSE, 2, 95)

        // third line
        assertEulReference(tokenizer.getNextToken(true), "backslash", 3, 1)
        assertSpecialCharacter(tokenizer.getNextToken(true), SpecialCharType.BACKSLASH, 3, 10)

        assertEulReference(tokenizer.getNextToken(true), "or", 3, 11)
        assertSpecialCharacter(tokenizer.getNextToken(true), SpecialCharType.OR, 3, 13)

        assertEulReference(tokenizer.getNextToken(true), "colon", 3, 14)
        assertSpecialCharacter(tokenizer.getNextToken(true), SpecialCharType.COLON, 3, 19)

        assertEulReference(tokenizer.getNextToken(true), "semicolon", 3, 20)
        assertSpecialCharacter(tokenizer.getNextToken(true), SpecialCharType.SEMICOLON, 3, 29)

        assertEulReference(tokenizer.getNextToken(true), "singleQuote", 3, 30)
        assertCharLiteral(tokenizer.getNextToken(true), 'a'.toLong(), 3, 41)

        assertEulReference(tokenizer.getNextToken(true), "doubleQuote", 3, 44)
        assertStringLiteral(tokenizer.getNextToken(true), "hi", 3, 55)

        assertEulReference(tokenizer.getNextToken(true), "comma", 3, 59)
        assertSpecialCharacter(tokenizer.getNextToken(true), SpecialCharType.COMMA, 3, 64)

        assertEulReference(tokenizer.getNextToken(true), "smaller", 3, 65)
        assertSpecialCharacter(tokenizer.getNextToken(true), SpecialCharType.LESS, 3, 72)

        assertEulReference(tokenizer.getNextToken(true), "dot", 3, 73)
        assertSpecialCharacter(tokenizer.getNextToken(true), SpecialCharType.DOT, 3, 76)

        assertEulReference(tokenizer.getNextToken(true), "greater", 3, 77)
        assertSpecialCharacter(tokenizer.getNextToken(true), SpecialCharType.GREATER, 3, 84)

        assertEulReference(tokenizer.getNextToken(true), "question", 3, 85)
        assertSpecialCharacter(tokenizer.getNextToken(true), SpecialCharType.QUESTION_MARK, 3, 93)

        assertEulReference(tokenizer.getNextToken(true), "slash", 3, 94)
        assertSpecialCharacter(tokenizer.getNextToken(true), SpecialCharType.SLASH, 3, 99)
    }

    @Test
    fun shouldRecognizeKeywords() {
        val tokenizer = this.createTokenizer(
            "true false null\n" +
                    "var val fun\n" +
                    "if else switch\n" +
                    "for while break continue\n" +
                    "return throw\n" +
                    "ifif forif if_ _else elsefor" // non keywords
        )

        assertBooleanLiteral(tokenizer.getNextToken(true), true, 1, 1)
        assertBooleanLiteral(tokenizer.getNextToken(true), false, 1, 6)
        assertNullLiteral(tokenizer.getNextToken(true), 1, 12)

        assertKeyword(tokenizer.getNextToken(true), KeywordType.VAR, 2, 1)
        assertKeyword(tokenizer.getNextToken(true), KeywordType.VAL, 2, 5)
        assertKeyword(tokenizer.getNextToken(true), KeywordType.FUN, 2, 9)

        assertKeyword(tokenizer.getNextToken(true), KeywordType.IF, 3, 1)
        assertKeyword(tokenizer.getNextToken(true), KeywordType.ELSE, 3, 4)
        assertKeyword(tokenizer.getNextToken(true), KeywordType.SWITCH, 3, 9)

        assertKeyword(tokenizer.getNextToken(true), KeywordType.FOR, 4, 1)
        assertKeyword(tokenizer.getNextToken(true), KeywordType.WHILE, 4, 5)
        assertKeyword(tokenizer.getNextToken(true), KeywordType.BREAK, 4, 11)
        assertKeyword(tokenizer.getNextToken(true), KeywordType.CONTINUE, 4, 17)

        assertKeyword(tokenizer.getNextToken(true), KeywordType.RETURN, 5, 1)
        assertKeyword(tokenizer.getNextToken(true), KeywordType.THROW, 5, 8)

        // Check for false positives. Those are simply references, not keywords.
        assertEulReference(tokenizer.getNextToken(true), "ifif", 6, 1)
        assertEulReference(tokenizer.getNextToken(true), "forif", 6, 6)
        assertEulReference(tokenizer.getNextToken(true), "if_", 6, 12)
        assertEulReference(tokenizer.getNextToken(true), "_else", 6, 16)
        assertEulReference(tokenizer.getNextToken(true), "elsefor", 6, 22)
    }


    /// UTILS
    private fun createTokenizer(code: String): EulTokenizer {
        val reader = StringReader(code)
        return EulTokenizer(reader, this.logger, this.options)
    }
}
