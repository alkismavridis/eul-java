package eu.alkismavridis.euljava.parser

import eu.alkismavridis.euljava.core.CompileOptions
import eu.alkismavridis.euljava.core.EulLogger
import eu.alkismavridis.euljava.core.ast.expressions.tokens.StringLiteral
import eu.alkismavridis.euljava.core.ast.operators.SpecialCharType
import eu.alkismavridis.euljava.test_utils.EulAssert
import eu.alkismavridis.euljava.test_utils.EulAssert.Companion.assertEulReference
import eu.alkismavridis.euljava.test_utils.EulAssert.Companion.assertSpecialCharacter
import eu.alkismavridis.euljava.test_utils.EulAssert.Companion.assertStringLiteral
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import java.io.StringReader

internal class TokenSourceTest {
    private val logger = Mockito.mock(EulLogger::class.java)
    private val options = CompileOptions("", 32)

    @Test
    fun shouldReturnRollegBackTokensAsLifoBeforeNewTokensAreRead() {
        val source = this.createTokenSource("token1 token2")
        assertEulReference(source.getNextToken(true), "token1", 1, 1)

        source.rollBackToken(StringLiteral("rolled-back-1", 44, 44))
        source.rollBackToken(StringLiteral("rolled-back-2", 55, 55))

        assertStringLiteral(source.getNextToken(true), "rolled-back-2", 55, 55)
        assertStringLiteral(source.getNextToken(true), "rolled-back-1", 44, 44)
        assertEulReference(source.getNextToken(true), "token2", 1, 8)
        assertThat(source.getNextToken(true)).isNull()
    }

    @Test
    fun requireNextToken_shouldReturnAllTokens() {
        val source = this.createTokenSource("hello +==//this is a comment\nworld")
        assertEulReference(source.requireNextToken(false, ""), "hello", 1, 1)
        assertSpecialCharacter(source.requireNextToken(false, ""), SpecialCharType.PLUS_EQUALS, 1, 7)
        assertSpecialCharacter(source.requireNextToken(false, ""), SpecialCharType.EQUALS, 1, 9)
        assertSpecialCharacter(source.requireNextToken(false, ""), SpecialCharType.NEW_LINE, 1, -1)
        assertEulReference(source.requireNextToken(false, ""), "world", 2, 1)

        assertThatExceptionOfType(ParserException::class.java)
                .isThrownBy { source.requireNextToken(false, "My error message") }
                .withMessage("My error message")
    }

    @Test
    fun requireNextToken_shouldBeAbleToSkipNewLineTokens() {
        val source = this.createTokenSource("hello +==//this is a comment\nworld")
        assertEulReference(source.requireNextToken(true, ""), "hello", 1, 1)
        assertSpecialCharacter(source.requireNextToken(true, ""), SpecialCharType.PLUS_EQUALS, 1, 7)
        assertSpecialCharacter(source.requireNextToken(true, ""), SpecialCharType.EQUALS, 1, 9)
        assertEulReference(source.requireNextToken(true, ""), "world", 2, 1)

        assertThatExceptionOfType(ParserException::class.java)
                .isThrownBy { source.requireNextToken(false, "My error message") }
                .withMessage("My error message")
    }


    /// SPECIAL TOKENS REQUEST
    @Test
    fun requireReference_shouldReturnReference() {
        val source = this.createTokenSource("hello world")
        assertEulReference(source.requireReference(true, "identifier"), "hello", 1, 1)
        assertEulReference(source.requireReference(true, "identifier"), "world", 1, 7)
    }

    @Test
    fun requireReference_shouldThrowIfEofIsReached() {
        val source = this.createTokenSource("hello")
        assertEulReference(source.requireReference(true, "identifier"), "hello", 1, 1)
        assertThatExceptionOfType(ParserException::class.java)
                .isThrownBy { source.requireReference(true, "identifier") }
                .withMessage("Expected identifier but end of file was found")
    }

    @Test
    fun requireReference_shouldThrowIfNoReferenceIsFound() {
        val source = this.createTokenSource("(")
        assertThatExceptionOfType(ParserException::class.java)
                .isThrownBy { source.requireReference(true, "identifier") }
                .withMessage("Expected identifier but SpecialCharacterToken was found")
    }


    /// UTILS
    private fun createTokenSource(code: String): TokenSource {
        val reader = StringReader(code)
        return TokenSource(reader, this.logger, this.options)
    }
}
