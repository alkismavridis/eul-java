package eu.alkismavridis.euljava.parser.token

import eu.alkismavridis.euljava.core.CompileOptions
import eu.alkismavridis.euljava.core.EulLogger
import eu.alkismavridis.euljava.test_utils.EulAssert.Companion.assertFloatLiteral
import eu.alkismavridis.euljava.test_utils.EulAssert.Companion.assertIntegerLiteral
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import java.io.StringReader

internal class NumberTokenizerTest {
    private val logger = Mockito.mock(EulLogger::class.java)
    private val options = CompileOptions("", 32)


    @Test
    fun shouldReadPlainIntegers() {
        val tokenizer = this.createTokenizer("1 123 0 55")
        assertIntegerLiteral(tokenizer.getNextToken(true), 1, this.options.defaultIntSizeBits, true, 1, 1)
        assertIntegerLiteral(tokenizer.getNextToken(true), 123, this.options.defaultIntSizeBits, true, 1, 3)
        assertIntegerLiteral(tokenizer.getNextToken(true), 0, this.options.defaultIntSizeBits, true, 1, 7)
        assertIntegerLiteral(tokenizer.getNextToken(true), 55, this.options.defaultIntSizeBits, true, 1, 9)
    }

    @Test
    fun shouldReadIntegersWithQualifier() {
        val tokenizer = this.createTokenizer("12u 123s 56f 0f")
        assertIntegerLiteral(tokenizer.getNextToken(true), 12, this.options.defaultIntSizeBits, false, 1, 1)
        assertIntegerLiteral(tokenizer.getNextToken(true), 123, this.options.defaultIntSizeBits, true, 1, 5)
        assertFloatLiteral(tokenizer.getNextToken(true), 56.0, this.options.defaultFloatSizeBits, 1, 10)
        assertFloatLiteral(tokenizer.getNextToken(true), 0.0, this.options.defaultFloatSizeBits, 1, 14)
    }

    @Test
    fun shouldReadIntegersWithQualifierAndSize() {
        val tokenizer = this.createTokenizer("12u32 123s16 56f32 0f64")
        assertIntegerLiteral(tokenizer.getNextToken(true), 12, 32, false, 1, 1)
        assertIntegerLiteral(tokenizer.getNextToken(true), 123, 16, true, 1, 7)
        assertFloatLiteral(tokenizer.getNextToken(true), 56.0, 32, 1, 14)
        assertFloatLiteral(tokenizer.getNextToken(true), 0.0, 64, 1, 20)
    }

    @Test
    fun shouldReadFloatingPointNumbers() {
        val tokenizer = this.createTokenizer("123. 12.56 24.f 43.5f 843.f32 37.67f32")
        assertFloatLiteral(tokenizer.getNextToken(true), 123.0, this.options.defaultFloatSizeBits, 1, 1)
        assertFloatLiteral(tokenizer.getNextToken(true), 12.56, this.options.defaultFloatSizeBits, 1, 6)
        assertFloatLiteral(tokenizer.getNextToken(true), 24.0, this.options.defaultFloatSizeBits, 1, 12)
        assertFloatLiteral(tokenizer.getNextToken(true), 43.5, this.options.defaultFloatSizeBits, 1, 17)
        assertFloatLiteral(tokenizer.getNextToken(true), 843.0, 32, 1, 23)
        assertFloatLiteral(tokenizer.getNextToken(true), 37.67, 32, 1, 31)
    }





    // TODO check size boundaries


    /// UTILS
    private fun createTokenizer(code: String): EulTokenizer {
        val reader = StringReader(code)
        return EulTokenizer(reader, this.logger, this.options)
    }
}
