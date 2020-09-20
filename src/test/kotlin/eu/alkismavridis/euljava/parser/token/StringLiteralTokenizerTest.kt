package eu.alkismavridis.euljava.parser.token

import eu.alkismavridis.euljava.core.CompileOptions
import eu.alkismavridis.euljava.core.EulLogger
import eu.alkismavridis.euljava.test_utils.EulAssert.Companion.assertEulReference
import eu.alkismavridis.euljava.test_utils.EulAssert.Companion.assertStringLiteral
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import java.io.StringReader

internal class StringLiteralTokenizerTest {
    private val logger = Mockito.mock(EulLogger::class.java)
    private val options = CompileOptions("")


    @Test
    fun shouldParseEmptyString() {
        val tokenizer = this.createTokenizer("before\"\"after")
        assertEulReference(tokenizer.getNextToken(true), "before", 1, 1)
        assertStringLiteral(tokenizer.getNextToken(true), "", 1, 7)
        assertEulReference(tokenizer.getNextToken(true), "after", 1, 9)
        assertThat(tokenizer.getNextToken(true)).isNull()
    }

    @Test
    fun shouldParseSimpleString() {
        val tokenizer = this.createTokenizer("before\"simple\"after")
        assertEulReference(tokenizer.getNextToken(true), "before", 1, 1)
        assertStringLiteral(tokenizer.getNextToken(true), "simple", 1, 7)
        assertEulReference(tokenizer.getNextToken(true), "after", 1, 15)
        assertThat(tokenizer.getNextToken(true)).isNull()
    }

    @Test
    fun shouldParseMultiLineString() {
        val tokenizer = this.createTokenizer("before\"I\n  am a multi\n\n line\n string\"after")
        assertEulReference(tokenizer.getNextToken(true), "before", 1, 1)
        assertStringLiteral(tokenizer.getNextToken(true), "I\n  am a multi\n\n line\n string", 1, 7)
        assertEulReference(tokenizer.getNextToken(true), "after", 5, 9)
        assertThat(tokenizer.getNextToken(true)).isNull()
    }

    @Test
    fun shouldThrowIfEndOfFileIsReached() {
        val tokenizer = this.createTokenizer("\"I started but I will not end")
        assertThatExceptionOfType(TokenizerException::class.java)
            .isThrownBy { tokenizer.getNextToken(true) }
            .withMessage("End of file found while parsing string literal")
            .matches { it.line == 1 && it.column == 1 }
    }

    @Test
    fun shouldParseEscapeCharacters() {
        val tokenizer = this.createTokenizer("before\"I\\n \\thello\\r\\bAND\\\\ quote\\\" dollar\\$\"after")
        assertEulReference(tokenizer.getNextToken(true), "before", 1, 1)
        assertStringLiteral(tokenizer.getNextToken(true), "I\n \thello\r\bAND\\ quote\" dollar\$", 1, 7)
        assertEulReference(tokenizer.getNextToken(true), "after", 1, 46)
        assertThat(tokenizer.getNextToken(true)).isNull()
    }

    @Test
    fun shouldThrowOnIllegalEscapeCharacter() {
        val tokenizer = this.createTokenizer("\"Illegal escape: \\j\"")
        assertThatExceptionOfType(TokenizerException::class.java)
            .isThrownBy { tokenizer.getNextToken(true) }
            .withMessage("Unknown escape character j")
            .matches { it.line == 1 && it.column == 19 }
    }

    @Test
    fun shouldParseUnicodeCharacters() {
        val tokenizer = this.createTokenizer("before\"\\u0059\\u0065\\u0073\\u270D\"after")
        assertEulReference(tokenizer.getNextToken(true), "before", 1, 1)
        assertStringLiteral(tokenizer.getNextToken(true), "Yes✍", 1, 7)
        assertEulReference(tokenizer.getNextToken(true), "after", 1, 33)
        assertThat(tokenizer.getNextToken(true)).isNull()
    }

    @Test
    fun shouldTrowOnEmptyUnicodeCharacter() {
        assertThatExceptionOfType(TokenizerException::class.java)
            .isThrownBy { this.createTokenizer("\"\\u").getNextToken(true) }
            .withMessage("End of file found while parsing unicode literal")
            .matches { it.line == 1 && it.column == 4 }

        assertThatExceptionOfType(TokenizerException::class.java)
            .isThrownBy { this.createTokenizer("\"\\u\"").getNextToken(true) }
            .withMessage("Expected hex digit but found \" instead")
            .matches { it.line == 1 && it.column == 4 }

        assertThatExceptionOfType(TokenizerException::class.java)
            .isThrownBy { this.createTokenizer("\"\\uj\"").getNextToken(true) }
            .withMessage("Expected hex digit but found j instead")
            .matches { it.line == 1 && it.column == 4 }
    }

    @Test
    fun shouldTrowOnUnicodeCharacterOfOneDigit() {
        assertThatExceptionOfType(TokenizerException::class.java)
            .isThrownBy { this.createTokenizer("\"\\u5").getNextToken(true) }
            .withMessage("End of file found while parsing unicode literal")
            .matches { it.line == 1 && it.column == 5 }

        assertThatExceptionOfType(TokenizerException::class.java)
            .isThrownBy { this.createTokenizer("\"\\u5\"").getNextToken(true) }
            .withMessage("Expected hex digit but found \" instead")
            .matches { it.line == 1 && it.column == 5 }

        assertThatExceptionOfType(TokenizerException::class.java)
            .isThrownBy { this.createTokenizer("\"\\u5j\"").getNextToken(true) }
            .withMessage("Expected hex digit but found j instead")
            .matches { it.line == 1 && it.column == 5 }
    }

    @Test
    fun shouldTrowOnUnicodeCharacterOfTwoDigits() {
        assertThatExceptionOfType(TokenizerException::class.java)
            .isThrownBy { this.createTokenizer("\"\\u5F").getNextToken(true) }
            .withMessage("End of file found while parsing unicode literal")
            .matches { it.line == 1 && it.column == 6 }

        assertThatExceptionOfType(TokenizerException::class.java)
            .isThrownBy { this.createTokenizer("\"\\u5F\"").getNextToken(true) }
            .withMessage("Expected hex digit but found \" instead")
            .matches { it.line == 1 && it.column == 6 }

        assertThatExceptionOfType(TokenizerException::class.java)
            .isThrownBy { this.createTokenizer("\"\\u5Fj\"").getNextToken(true) }
            .withMessage("Expected hex digit but found j instead")
            .matches { it.line == 1 && it.column == 6 }
    }

    @Test
    fun shouldTrowOnUnicodeCharacterOfThreeDigits() {
        assertThatExceptionOfType(TokenizerException::class.java)
            .isThrownBy { this.createTokenizer("\"\\u5Fa").getNextToken(true) }
            .withMessage("End of file found while parsing unicode literal")
            .matches { it.line == 1 && it.column == 7 }

        assertThatExceptionOfType(TokenizerException::class.java)
            .isThrownBy { this.createTokenizer("\"\\u5Fa\"").getNextToken(true) }
            .withMessage("Expected hex digit but found \" instead")
            .matches { it.line == 1 && it.column == 7 }

        assertThatExceptionOfType(TokenizerException::class.java)
            .isThrownBy { this.createTokenizer("\"\\u5Faj\"").getNextToken(true) }
            .withMessage("Expected hex digit but found j instead")
            .matches { it.line == 1 && it.column == 7 }
    }


    /// UTILS
    private fun createTokenizer(code: String): EulTokenizer {
        val reader = StringReader(code)
        return EulTokenizer(reader, this.logger, this.options)
    }
}
