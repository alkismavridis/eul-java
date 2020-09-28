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
import org.mockito.Mockito.mock
import java.io.StringReader

internal class SpecialCharacterTokenizerTest {
    private val logger = mock(EulLogger::class.java)
    private val options = CompileOptions("")

    @Test
    fun shouldProperlyReadEqualsOperators() {
        val tokenizer = this.createTokenizer("== == = === ====")
        assertSpecialCharacter(tokenizer.getNextToken(true), SpecialCharType.DOUBLE_EQUALS, 1, 1)
        assertSpecialCharacter(tokenizer.getNextToken(true), SpecialCharType.DOUBLE_EQUALS, 1, 4)
        assertSpecialCharacter(tokenizer.getNextToken(true), SpecialCharType.EQUALS, 1, 7)
        assertSpecialCharacter(tokenizer.getNextToken(true), SpecialCharType.TRIPLE_EQUALS, 1, 9)
        assertSpecialCharacter(tokenizer.getNextToken(true), SpecialCharType.TRIPLE_EQUALS, 1, 13)
        assertSpecialCharacter(tokenizer.getNextToken(true), SpecialCharType.EQUALS, 1, 16)
        assertThat(tokenizer.getNextToken(true)).isNull()
    }

    @Test
    fun shouldProperlyReadPlusOperators() {
        val tokenizer = this.createTokenizer("++ += + ")
        assertSpecialCharacter(tokenizer.getNextToken(true), SpecialCharType.DOUBLE_PLUS, 1, 1)
        assertSpecialCharacter(tokenizer.getNextToken(true), SpecialCharType.PLUS_EQUALS, 1, 4)
        assertSpecialCharacter(tokenizer.getNextToken(true), SpecialCharType.PLUS, 1, 7)
        assertThat(tokenizer.getNextToken(true)).isNull()
    }

    @Test
    fun shouldProperlyReadOperatorsWithStrings() {
        val tokenizer = this.createTokenizer("aa++aa-- +")
        assertEulReference(tokenizer.getNextToken(true), "aa", 1, 1)
        assertSpecialCharacter(tokenizer.getNextToken(true), SpecialCharType.DOUBLE_PLUS, 1, 3)
        assertEulReference(tokenizer.getNextToken(true), "aa", 1, 5)
        assertSpecialCharacter(tokenizer.getNextToken(true), SpecialCharType.DOUBLE_MINUS, 1, 7)
        assertSpecialCharacter(tokenizer.getNextToken(true), SpecialCharType.PLUS, 1, 10)
        assertThat(tokenizer.getNextToken(true)).isNull()
    }


    /// COMMENTS TESTS
    @Test
    fun shouldHandleSingleLineComment() {
        val tokenizer = this.createTokenizer("hello//this is a comment\nworld")
        assertEulReference(tokenizer.getNextToken(false), "hello", 1, 1)
        assertThat(tokenizer.getNextToken(false)).isInstanceOf(EulCommentToken::class.java)
        assertSpecialCharacter(tokenizer.getNextToken(false), SpecialCharType.NEW_LINE, 1, -1)
        assertEulReference(tokenizer.getNextToken(false), "world", 2, 1)
        assertThat(tokenizer.getNextToken(false)).isNull()
    }

    @Test
    fun shouldHandleSingleLineCommentAtEndOfFile() {
        val tokenizer = this.createTokenizer("hello//this is a comment")
        assertEulReference(tokenizer.getNextToken(false), "hello", 1, 1)
        assertThat(tokenizer.getNextToken(false)).isInstanceOf(EulCommentToken::class.java)
        assertThat(tokenizer.getNextToken(false)).isNull()
    }

    @Test
    fun shouldParseMultiLineCommentInSameLine() {
        val tokenizer = this.createTokenizer("hello/*this is a comment * / still a comment */world")
        assertEulReference(tokenizer.getNextToken(false), "hello", 1, 1)
        assertThat(tokenizer.getNextToken(false)).isInstanceOf(EulCommentToken::class.java)
        assertEulReference(tokenizer.getNextToken(false), "world", 1, 48)
        assertThat(tokenizer.getNextToken(false)).isNull()
    }

    @Test
    fun shouldParseMultiLineCommentInMultipleLines() {
        val tokenizer = this.createTokenizer("hello/*this is a comment \n still a \ncomment **/world")
        assertEulReference(tokenizer.getNextToken(false), "hello", 1, 1)
        assertThat(tokenizer.getNextToken(false)).isInstanceOf(EulCommentToken::class.java)
        assertEulReference(tokenizer.getNextToken(false), "world", 3, 12)
        assertThat(tokenizer.getNextToken(false)).isNull()
    }

    @Test
    fun shouldThrowIfEndOfFileIsReachedInMultilineComment() {
        val tokenizer = this.createTokenizer("hello/*I start a comment but not close it. Why on earth?")
        assertEulReference(tokenizer.getNextToken(true), "hello", 1, 1)
        assertThatExceptionOfType(TokenizerException::class.java)
                .isThrownBy { tokenizer.getNextToken(true) }
                .withMessage("End of file found while parsing multi-line comment")

    }


    /// UTILS
    private fun createTokenizer(code: String): EulTokenizer {
        val reader = StringReader(code)
        return EulTokenizer(reader, this.logger, this.options)
    }
}
