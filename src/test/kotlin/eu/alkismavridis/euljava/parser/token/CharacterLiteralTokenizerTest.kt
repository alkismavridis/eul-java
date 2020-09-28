package eu.alkismavridis.euljava.parser.token

import eu.alkismavridis.euljava.core.CompileOptions
import eu.alkismavridis.euljava.core.EulLogger
import eu.alkismavridis.euljava.core.ast.operators.SpecialCharType
import eu.alkismavridis.euljava.test_utils.EulAssert.Companion.assertCharLiteral
import eu.alkismavridis.euljava.test_utils.EulAssert.Companion.assertEulReference
import eu.alkismavridis.euljava.test_utils.EulAssert.Companion.assertSpecialCharacter
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import java.io.StringReader

internal class CharacterLiteralTokenizerTest {
    private val logger = Mockito.mock(EulLogger::class.java)
    private val options = CompileOptions("")

    @Test
    fun shouldParseEmptyChar() {
        val tokenizer = this.createTokenizer("''hello")
        assertCharLiteral(tokenizer.getNextToken(true), 0, 1, 1)
        assertEulReference(tokenizer.getNextToken(true), "hello", 1, 3)
    }

    @Test
    fun shouldThrowIfEndOfFileIsReached() {
        val tokenizer = this.createTokenizer("'")

        assertThatExceptionOfType(TokenizerException::class.java)
            .isThrownBy { tokenizer.getNextToken(true) }
            .withMessage("End of file found while parsing character")
            .matches { it.line == 1 && it.column == 1 }
    }

    @Test
    fun shouldThrowIfEndOfLineIsFound() {
        val tokenizer = this.createTokenizer("'\n'")
        assertThatExceptionOfType(TokenizerException::class.java)
            .isThrownBy { tokenizer.getNextToken(true) }
            .withMessage("Illegal end of line while parsing character")
            .matches { it.line == 1 && it.column == 1 }
    }

    @Test
    fun shouldParseRegularCharacter() {
        val tokenizer = this.createTokenizer("'a'hello")
        assertCharLiteral(tokenizer.getNextToken(true), 'a'.toLong(), 1, 1)
        assertEulReference(tokenizer.getNextToken(true), "hello", 1, 4)
    }

    @Test
    fun shouldThrowIfSecondCharacterIsFound() {
        assertThatExceptionOfType(TokenizerException::class.java)
            .isThrownBy { this.createTokenizer("'ab'").getNextToken(true) }
            .withMessage("Expected ' but found b instead")
            .matches { it.line == 1 && it.column == 3 }
    }

    @Test
    fun shouldParseEscapeCharacters() {
        val tokenizer = this.createTokenizer("'\\n'+'\\t' + '\\r' - '\\b'and'\\''")

        assertCharLiteral(tokenizer.getNextToken(true), '\n'.toLong(), 1, 1)
        assertSpecialCharacter(tokenizer.getNextToken(true), SpecialCharType.PLUS, 1, 5)

        assertCharLiteral(tokenizer.getNextToken(true), '\t'.toLong(), 1, 6)
        assertSpecialCharacter(tokenizer.getNextToken(true), SpecialCharType.PLUS, 1, 11)

        assertCharLiteral(tokenizer.getNextToken(true), '\r'.toLong(), 1, 13)
        assertSpecialCharacter(tokenizer.getNextToken(true), SpecialCharType.MINUS, 1, 18)

        assertCharLiteral(tokenizer.getNextToken(true), '\b'.toLong(), 1, 20)
        assertEulReference(tokenizer.getNextToken(true), "and", 1, 24)

        assertCharLiteral(tokenizer.getNextToken(true), '\''.toLong(), 1, 27)
    }

    @Test
    fun shouldRejectIllegalEscapeCharacters() {
        assertThatExceptionOfType(TokenizerException::class.java)
            .isThrownBy { this.createTokenizer("'\\g'").getNextToken(true) }
            .withMessage("Illegal escape character g")
            .matches { it.line == 1 && it.column == 1 }

        assertThatExceptionOfType(TokenizerException::class.java)
            .isThrownBy { this.createTokenizer("'\\").getNextToken(true) }
            .withMessage("End of file found while parsing escape character")
            .matches { it.line == 1 && it.column == 1 }
    }

    @Test
    fun shouldRejectAdditionalCharactersAfterEscape() {
        val tokenizer = this.createTokenizer("'\\na'")
        assertThatExceptionOfType(TokenizerException::class.java)
            .isThrownBy { tokenizer.getNextToken(true) }
            .withMessage("Expected ' but found a instead")
            .matches { it.line == 1 && it.column == 4 }
    }

    @Test
    fun shouldParseUnicodeLiteral() {
        val tokenizer = this.createTokenizer("'\\u1234''\\u0001''\\uaFfA'")
        assertCharLiteral(tokenizer.getNextToken(true), 0x1234, 1, 1)
        assertCharLiteral(tokenizer.getNextToken(true), 0x0001, 1, 9)
        assertCharLiteral(tokenizer.getNextToken(true), 0xAFFA, 1, 17)
    }

    @Test
    fun shouldRejectUnicodeLiteralWithIllegalLength() {
        assertThatExceptionOfType(TokenizerException::class.java)
            .isThrownBy { this.createTokenizer("'\\u").getNextToken(true) }
            .withMessage("End of file found while parsing unicode literal")
            .matches { it.line == 1 && it.column == 4 }

        assertThatExceptionOfType(TokenizerException::class.java)
            .isThrownBy { this.createTokenizer("'\\u'").getNextToken(true) }
            .withMessage("Expected hex digit but found ' instead")
            .matches { it.line == 1 && it.column == 4 }

        assertThatExceptionOfType(TokenizerException::class.java)
            .isThrownBy { this.createTokenizer("'\\u7'").getNextToken(true) }
            .withMessage("Expected hex digit but found ' instead")
            .matches { it.line == 1 && it.column == 5 }

        assertThatExceptionOfType(TokenizerException::class.java)
            .isThrownBy { this.createTokenizer("'\\u77'").getNextToken(true) }
            .withMessage("Expected hex digit but found ' instead")
            .matches { it.line == 1 && it.column == 6 }

        assertThatExceptionOfType(TokenizerException::class.java)
            .isThrownBy { this.createTokenizer("'\\u777'").getNextToken(true) }
            .withMessage("Expected hex digit but found ' instead")
            .matches { it.line == 1 && it.column == 7 }

        assertThatExceptionOfType(TokenizerException::class.java)
            .isThrownBy { this.createTokenizer("'\\u77785'").getNextToken(true) }
            .withMessage("Expected ' but found 5 instead")
            .matches { it.line == 1 && it.column == 8 }
    }


    /// UTILS
    private fun createTokenizer(code: String): EulTokenizer {
        val reader = StringReader(code)
        return EulTokenizer(reader, this.logger, this.options)
    }
}
