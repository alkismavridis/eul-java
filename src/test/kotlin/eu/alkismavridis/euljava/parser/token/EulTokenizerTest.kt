package eu.alkismavridis.euljava.parser.token

import eu.alkismavridis.euljava.core.CompileOptions
import eu.alkismavridis.euljava.core.EulLogger
import eu.alkismavridis.euljava.core.ast.operators.EulCommentToken
import eu.alkismavridis.euljava.core.ast.operators.SpecialCharType
import eu.alkismavridis.euljava.test_utils.EulAssert.Companion.assertEulReference
import eu.alkismavridis.euljava.test_utils.EulAssert.Companion.assertSpecialCharacter
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import java.io.StringReader


internal class EulTokenizerTest {
    private val logger = Mockito.mock(EulLogger::class.java)
    private val options = CompileOptions("", 32)


    @Test
    fun getNextToken_shouldReturnAllTokens() {
        val tokenizer = this.createTokenizer("hello +==//this is a comment\nworld")
        assertEulReference(tokenizer.getNextToken(false), "hello", 1, 1)
        assertSpecialCharacter(tokenizer.getNextToken(false), SpecialCharType.PLUS_EQUALS, 1, 7)
        assertSpecialCharacter(tokenizer.getNextToken(false), SpecialCharType.EQUALS, 1, 9)
        assertThat(tokenizer.getNextToken(false)).isInstanceOf(EulCommentToken::class.java)
        assertSpecialCharacter(tokenizer.getNextToken(false), SpecialCharType.NEW_LINE, 1, -1)
        assertEulReference(tokenizer.getNextToken(false), "world", 2, 1)
        assertThat(tokenizer.getNextToken(false)).isNull()
    }

    @Test
    fun getNextToken_shouldBeAbleToSkipNewLineTokens() {
        val tokenizer = this.createTokenizer("hello +==//this is a comment\nworld")
        assertEulReference(tokenizer.getNextToken(true), "hello", 1, 1)
        assertSpecialCharacter(tokenizer.getNextToken(true), SpecialCharType.PLUS_EQUALS, 1, 7)
        assertSpecialCharacter(tokenizer.getNextToken(true), SpecialCharType.EQUALS, 1, 9)
        assertThat(tokenizer.getNextToken(true)).isInstanceOf(EulCommentToken::class.java)
        assertEulReference(tokenizer.getNextToken(true), "world", 2, 1)
        assertThat(tokenizer.getNextToken(true)).isNull()
    }

    @Test
    fun requireNextToken_shouldReturnAllTokens() {
        val tokenizer = this.createTokenizer("hello +==//this is a comment\nworld")
        assertEulReference(tokenizer.requireNextToken(false), "hello", 1, 1)
        assertSpecialCharacter(tokenizer.requireNextToken(false), SpecialCharType.PLUS_EQUALS, 1, 7)
        assertSpecialCharacter(tokenizer.requireNextToken(false), SpecialCharType.EQUALS, 1, 9)
        assertThat(tokenizer.requireNextToken(false)).isInstanceOf(EulCommentToken::class.java)
        assertSpecialCharacter(tokenizer.requireNextToken(false), SpecialCharType.NEW_LINE, 1, -1)
        assertEulReference(tokenizer.requireNextToken(false), "world", 2, 1)

        assertThatExceptionOfType(TokenizerException::class.java)
                .isThrownBy { tokenizer.requireNextToken(false) }
                .withMessage("End of file found while parsing")
    }

    @Test
    fun requireNextToken_shouldBeAbleToSkipNewLineTokens() {
        val tokenizer = this.createTokenizer("hello +==//this is a comment\nworld")
        assertEulReference(tokenizer.requireNextToken(true), "hello", 1, 1)
        assertSpecialCharacter(tokenizer.requireNextToken(true), SpecialCharType.PLUS_EQUALS, 1, 7)
        assertSpecialCharacter(tokenizer.requireNextToken(true), SpecialCharType.EQUALS, 1, 9)
        assertThat(tokenizer.requireNextToken(true)).isInstanceOf(EulCommentToken::class.java)
        assertEulReference(tokenizer.requireNextToken(true), "world", 2, 1)

        assertThatExceptionOfType(TokenizerException::class.java)
                .isThrownBy { tokenizer.requireNextToken(false) }
                .withMessage("End of file found while parsing")
    }

    @Test
    fun getNextNonCommentToken_shouldReturnAllNonCommentTokens() {
        val tokenizer = this.createTokenizer("hello +==//this is a comment\nworld")
        assertEulReference(tokenizer.getNextNonCommentToken(false), "hello", 1, 1)
        assertSpecialCharacter(tokenizer.getNextNonCommentToken(false), SpecialCharType.PLUS_EQUALS, 1, 7)
        assertSpecialCharacter(tokenizer.getNextNonCommentToken(false), SpecialCharType.EQUALS, 1, 9)
        assertSpecialCharacter(tokenizer.getNextNonCommentToken(false), SpecialCharType.NEW_LINE, 1, -1)
        assertEulReference(tokenizer.getNextNonCommentToken(false), "world", 2, 1)
        assertThat(tokenizer.getNextNonCommentToken(false)).isNull()
    }

    @Test
    fun getNextNonCommentToken_shouldBeAbleToSkipNewLineTokens() {
        val tokenizer = this.createTokenizer("hello +==//this is a comment\nworld//other comment")
        assertEulReference(tokenizer.getNextNonCommentToken(true), "hello", 1, 1)
        assertSpecialCharacter(tokenizer.getNextNonCommentToken(true), SpecialCharType.PLUS_EQUALS, 1, 7)
        assertSpecialCharacter(tokenizer.getNextNonCommentToken(true), SpecialCharType.EQUALS, 1, 9)
        assertEulReference(tokenizer.getNextNonCommentToken(true), "world", 2, 1)
        assertThat(tokenizer.getNextNonCommentToken(true)).isNull()
    }

    @Test
    fun requireNextNonCommentToken_shouldReturnAllTokens() {
        val tokenizer = this.createTokenizer("hello +==//this is a comment\nworld")
        assertEulReference(tokenizer.requireNextNonCommentToken(false), "hello", 1, 1)
        assertSpecialCharacter(tokenizer.requireNextNonCommentToken(false), SpecialCharType.PLUS_EQUALS, 1, 7)
        assertSpecialCharacter(tokenizer.requireNextNonCommentToken(false), SpecialCharType.EQUALS, 1, 9)
        assertSpecialCharacter(tokenizer.requireNextNonCommentToken(false), SpecialCharType.NEW_LINE, 1, -1)
        assertEulReference(tokenizer.requireNextNonCommentToken(false), "world", 2, 1)

        assertThatExceptionOfType(TokenizerException::class.java)
                .isThrownBy { tokenizer.requireNextNonCommentToken(false) }
                .withMessage("End of file found while parsing")
    }

    @Test
    fun requireNextNonCommentToken_shouldBeAbleToSkipNewLineTokens() {
        val tokenizer = this.createTokenizer("hello +==//this is a comment\nworld")
        assertEulReference(tokenizer.requireNextNonCommentToken(true), "hello", 1, 1)
        assertSpecialCharacter(tokenizer.requireNextNonCommentToken(true), SpecialCharType.PLUS_EQUALS, 1, 7)
        assertSpecialCharacter(tokenizer.requireNextNonCommentToken(true), SpecialCharType.EQUALS, 1, 9)
        assertEulReference(tokenizer.requireNextNonCommentToken(true), "world", 2, 1)

        assertThatExceptionOfType(TokenizerException::class.java)
                .isThrownBy { tokenizer.requireNextNonCommentToken(false) }
                .withMessage("End of file found while parsing")
    }


    /// UTILS
    private fun createTokenizer(code: String): EulTokenizer {
        val reader = StringReader(code)
        return EulTokenizer(reader, this.logger, this.options)
    }
}
