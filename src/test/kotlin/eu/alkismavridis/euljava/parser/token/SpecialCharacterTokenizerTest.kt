package eu.alkismavridis.euljava.parser.token

import eu.alkismavridis.euljava.core.CompileOptions
import eu.alkismavridis.euljava.core.EulLogger
import eu.alkismavridis.euljava.test_utils.EulAssert.Companion.assertEulReference
import eu.alkismavridis.euljava.test_utils.EulAssert.Companion.assertOperator
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import java.io.StringReader

internal class SpecialCharacterTokenizerTest {
    private val logger = mock(EulLogger::class.java)
    private val options = CompileOptions("")

    @Test
    fun shouldProperlyReadEqualsOperators() {
        val tokenizer = this.createTokenizer("== == = ===")
        assertOperator(tokenizer.getNextToken(true), "==", 1, 1);
        assertOperator(tokenizer.getNextToken(true), "==", 1, 4);
        assertOperator(tokenizer.getNextToken(true), "=", 1, 7);
        assertOperator(tokenizer.getNextToken(true), "===", 1, 9);
        assertThat(tokenizer.getNextToken(true)).isNull();
    }

    @Test
    fun shouldProperlyReadPlusOperators() {
        val tokenizer = this.createTokenizer("++ += + ")
        assertOperator(tokenizer.getNextToken(true), "++", 1, 1);
        assertOperator(tokenizer.getNextToken(true), "+=", 1, 4);
        assertOperator(tokenizer.getNextToken(true), "+", 1, 7);
        assertThat(tokenizer.getNextToken(true)).isNull();
    }

    @Test
    fun shouldProperlyReadOperatorsWithStrings() {
        val tokenizer = this.createTokenizer("aa++aa-- +")
        assertEulReference(tokenizer.getNextToken(true), "aa", 1, 1);
        assertOperator(tokenizer.getNextToken(true), "++", 1, 3);
        assertEulReference(tokenizer.getNextToken(true), "aa", 1, 5);
        assertOperator(tokenizer.getNextToken(true), "--", 1, 7);
        assertOperator(tokenizer.getNextToken(true), "+", 1, 10);
        assertThat(tokenizer.getNextToken(true)).isNull();
    }

    /// UTILS
    private fun createTokenizer(code: String): EulTokenizer {
        val reader = StringReader(code)
        return EulTokenizer(reader, this.logger, this.options)
    }
}
