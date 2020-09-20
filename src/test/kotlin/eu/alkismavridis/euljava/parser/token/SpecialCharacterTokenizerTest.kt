package eu.alkismavridis.euljava.parser.token

import eu.alkismavridis.euljava.core.CompileOptions
import eu.alkismavridis.euljava.core.EulLogger
import eu.alkismavridis.euljava.test_utils.EulAssert.Companion.assertBooleanLiteral
import eu.alkismavridis.euljava.test_utils.EulAssert.Companion.assertCharLiteral
import eu.alkismavridis.euljava.test_utils.EulAssert.Companion.assertEulReference
import eu.alkismavridis.euljava.test_utils.EulAssert.Companion.assertKeyword
import eu.alkismavridis.euljava.test_utils.EulAssert.Companion.assertNullLiteral
import eu.alkismavridis.euljava.test_utils.EulAssert.Companion.assertOperator
import eu.alkismavridis.euljava.test_utils.EulAssert.Companion.assertStringLiteral
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import java.io.StringReader

internal class SpecialCharacterTokenizerTest {
    private val logger = mock(EulLogger::class.java)
    private val options = CompileOptions("")

    @Test
    fun shouldEndWithOperator() {
        val tokenizer = this.createTokenizer("==")
        assertOperator(tokenizer.getNextToken(true), "==", 1, 1);
        assertThat(tokenizer.getNextToken(true)).isNull();
    }

    @Test
    fun shouldReadSeperateOperators() {
        val tokenizer = this.createTokenizer("== =")
        assertOperator(tokenizer.getNextToken(true), "==", 1, 1);
        assertOperator(tokenizer.getNextToken(true), "=", 1, 4);
        assertThat(tokenizer.getNextToken(true)).isNull();
    }

    /// UTILS
    private fun createTokenizer(code: String): EulTokenizer {
        val reader = StringReader(code)
        return EulTokenizer(reader, this.logger, this.options)
    }
}
