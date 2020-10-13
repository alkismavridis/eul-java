package eu.alkismavridis.euljava.parser.expressions

import eu.alkismavridis.euljava.core.CompileOptions
import eu.alkismavridis.euljava.core.EulLogger
import eu.alkismavridis.euljava.core.ast.operators.SpecialCharType
import eu.alkismavridis.euljava.parser.ParserException
import eu.alkismavridis.euljava.parser.TokenSource
import eu.alkismavridis.euljava.test_utils.EulAssert.Companion.assertBooleanLiteral
import eu.alkismavridis.euljava.test_utils.EulAssert.Companion.assertEulReference
import eu.alkismavridis.euljava.test_utils.EulAssert.Companion.assertInfixExpression
import eu.alkismavridis.euljava.test_utils.EulAssert.Companion.assertIntegerLiteral
import eu.alkismavridis.euljava.test_utils.EulAssert.Companion.assertPrefixExpression
import eu.alkismavridis.euljava.test_utils.EulAssert.Companion.assertSpecialCharacter
import eu.alkismavridis.euljava.test_utils.EulAssert.Companion.assertSuffixExpression
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import java.io.StringReader

internal class ExpressionParserTest {
    private val logger = Mockito.mock(EulLogger::class.java)
    private val options = CompileOptions("")


    /// SIMPLE TOKENS
    @Test
    fun shouldParseIntegerToken() {
        val source = this.createTokenSource("5")
        val parser = ExpressionParser(source)

        assertIntegerLiteral(parser.readExpression(NewLinePolicy.RESPECT), 5, 32, true, 1, 1)
        assertThat(source.getNextToken(false)).isNull()
    }

    @Test
    fun shouldParseBooleanToken() {
        val source = this.createTokenSource("true")
        val parser = ExpressionParser(source)

        assertBooleanLiteral(parser.readExpression(NewLinePolicy.RESPECT), true, 1, 1)
        assertThat(source.getNextToken(false)).isNull()
    }

    @Test
    fun requireExpression_shouldThrowOnEmptyExpression() {
        val source = this.createTokenSource("")
        val parser = ExpressionParser(source)

        assertThatExceptionOfType(ParserException::class.java)
                .isThrownBy { parser.requireExpression(NewLinePolicy.RESPECT) }
    }

    @Test
    fun readExpression_shouldReturnNullOnEmptyExpression() {
        val source = this.createTokenSource("")
        val parser = ExpressionParser(source)

        assertThat(parser.readExpression(NewLinePolicy.RESPECT)).isNull()
    }


    /// INFIX EXPRESSIONS
    @Test
    fun shouldParseSimpleInfixExpression() {
        val source = this.createTokenSource("x + y")
        val parser = ExpressionParser(source)

        val asInfix = assertInfixExpression(parser.readExpression(NewLinePolicy.RESPECT), 1, 1)
        assertEulReference(asInfix.first, "x", 1, 1)
        assertSpecialCharacter(asInfix.operator, SpecialCharType.PLUS, 1, 3)
        assertEulReference(asInfix.second, "y", 1, 5)
    }

    @Test
    fun shouldParseLtrOperatorsOfSamePrecedence() {
        val source = this.createTokenSource("x + y + z")
        val parser = ExpressionParser(source)

        val topLevelInfix = assertInfixExpression(parser.readExpression(NewLinePolicy.RESPECT), 1, 1)
        val nestedInfix = assertInfixExpression(topLevelInfix.first, 1, 1)
        assertEulReference(nestedInfix.first, "x", 1, 1)
        assertSpecialCharacter(nestedInfix.operator, SpecialCharType.PLUS, 1, 3)
        assertEulReference(nestedInfix.second, "y", 1, 5)

        assertSpecialCharacter(topLevelInfix.operator, SpecialCharType.PLUS, 1, 7)
        assertEulReference(topLevelInfix.second, "z", 1, 9)
    }

    @Test
    fun shouldParseRtlOperatorsOfSamePrecedence() {
        val source = this.createTokenSource("x = y = z")
        val parser = ExpressionParser(source)

        val topLevelInfix = assertInfixExpression(parser.readExpression(NewLinePolicy.RESPECT), 1, 1)

        assertEulReference(topLevelInfix.first, "x", 1, 1)
        assertSpecialCharacter(topLevelInfix.operator, SpecialCharType.EQUALS, 1, 3)

        val nestedInfix = assertInfixExpression(topLevelInfix.second, 1, 5)
        assertEulReference(nestedInfix.first, "y", 1, 5)
        assertSpecialCharacter(nestedInfix.operator, SpecialCharType.EQUALS, 1, 7)
        assertEulReference(nestedInfix.second, "z", 1, 9)
    }

    @Test
    fun shouldHandleDecreasingPrecedenceInfix() {
        val source = this.createTokenSource("x * y + z")
        val parser = ExpressionParser(source)

        val topLevelInfix = assertInfixExpression(parser.readExpression(NewLinePolicy.RESPECT), 1, 1)
        val nestedInfix = assertInfixExpression(topLevelInfix.first, 1, 1)
        assertEulReference(nestedInfix.first, "x", 1, 1)
        assertSpecialCharacter(nestedInfix.operator, SpecialCharType.STAR, 1, 3)
        assertEulReference(nestedInfix.second, "y", 1, 5)

        assertSpecialCharacter(topLevelInfix.operator, SpecialCharType.PLUS, 1, 7)
        assertEulReference(topLevelInfix.second, "z", 1, 9)
    }

    @Test
    fun shouldHandleIncreasingPrecedenceInfix() {
        val source = this.createTokenSource("x + y * z")
        val parser = ExpressionParser(source)

        val topLevelInfix = assertInfixExpression(parser.readExpression(NewLinePolicy.RESPECT), 1, 1)

        assertEulReference(topLevelInfix.first, "x", 1, 1)
        assertSpecialCharacter(topLevelInfix.operator, SpecialCharType.PLUS, 1, 3)

        val nestedInfix = assertInfixExpression(topLevelInfix.second, 1, 5)
        assertEulReference(nestedInfix.first, "y", 1, 5)
        assertSpecialCharacter(nestedInfix.operator, SpecialCharType.STAR, 1, 7)
        assertEulReference(nestedInfix.second, "z", 1, 9)
    }

    @Test
    fun shouldHandleComplexInfix() {
        val source = this.createTokenSource("f = g = h && i.a + b * y / d")
        val parser = ExpressionParser(source)

        val topLevelInfix = assertInfixExpression(parser.readExpression(NewLinePolicy.RESPECT), 1, 1)

        assertEulReference(topLevelInfix.first, "f", 1, 1)
        assertSpecialCharacter(topLevelInfix.operator, SpecialCharType.EQUALS, 1, 3)

        val exp2 = assertInfixExpression(topLevelInfix.second, 1, 5) //g = ....
        assertEulReference(exp2.first, "g", 1, 5)
        assertSpecialCharacter(exp2.operator, SpecialCharType.EQUALS, 1, 7)

        val exp3 = assertInfixExpression(exp2.second, 1, 9) //h && i.a + b * y / d
        assertEulReference(exp3.first, "h", 1, 9)
        assertSpecialCharacter(exp3.operator, SpecialCharType.DOUBLE_AND, 1, 11)

        val exp4 = assertInfixExpression(exp3.second, 1, 14) //i.a + b * y / d
        assertSpecialCharacter(exp4.operator, SpecialCharType.PLUS, 1, 18)

        val exp5 = assertInfixExpression(exp4.first, 1, 14) //i.a
        assertEulReference(exp5.first, "i", 1, 14)
        assertSpecialCharacter(exp5.operator, SpecialCharType.DOT, 1, 15)
        assertEulReference(exp5.second, "a", 1, 16)

        val exp6 = assertInfixExpression(exp4.second, 1, 20) //b * y / d
        assertSpecialCharacter(exp6.operator, SpecialCharType.SLASH, 1, 26)
        assertEulReference(exp6.second, "d", 1, 28)

        val exp7 = assertInfixExpression(exp6.first, 1, 20) //b * y
        assertEulReference(exp7.first, "b", 1, 20)
        assertSpecialCharacter(exp7.operator, SpecialCharType.STAR, 1, 22)
        assertEulReference(exp7.second, "y", 1, 24)
    }

    @Test
    fun shouldHandleParenthesis() {
        val source = this.createTokenSource("x * (y + z)")
        val parser = ExpressionParser(source)

        val exp1 = assertInfixExpression(parser.readExpression(NewLinePolicy.RESPECT), 1, 1)
        assertEulReference(exp1.first, "x", 1, 1)
        assertSpecialCharacter(exp1.operator, SpecialCharType.STAR, 1, 3)

        val exp2 = assertInfixExpression(exp1.second, 1, 6)
        assertEulReference(exp2.first, "y", 1, 6)
        assertSpecialCharacter(exp2.operator, SpecialCharType.PLUS, 1, 8)
        assertEulReference(exp2.second, "z", 1, 10)
    }



    /// PREFIX EXPRESSIONS
    @Test
    fun shouldParseSimplePrefixExpression() {
        val source = this.createTokenSource("-x")
        val parser = ExpressionParser(source)

        val asPrefix = assertPrefixExpression(parser.readExpression(NewLinePolicy.RESPECT), 1, 1)
        assertSpecialCharacter(asPrefix.operator, SpecialCharType.MINUS, 1, 1)
        assertEulReference(asPrefix.getTarget(), "x", 1, 2)
    }

    @Test
    fun shouldParseMultiplePrefixOperatorsAsRtlWithSamePrecedence() {
        val source = this.createTokenSource("++-+x")
        val parser = ExpressionParser(source)

        val exp1 = assertPrefixExpression(parser.readExpression(NewLinePolicy.RESPECT), 1, 1)
        assertSpecialCharacter(exp1.operator, SpecialCharType.DOUBLE_PLUS, 1, 1)

        val exp2 = assertPrefixExpression(exp1.getTarget(), 1, 3)
        assertSpecialCharacter(exp2.operator, SpecialCharType.MINUS, 1, 3)

        val exp3 = assertPrefixExpression(exp2.getTarget(), 1, 4)
        assertSpecialCharacter(exp3.operator, SpecialCharType.PLUS, 1, 4)
        assertEulReference(exp3.getTarget(), "x", 1, 5)
    }

    @Test
    fun shouldMixPrefixAndInfixOperators() {
        val source = this.createTokenSource("-x + ++y.z-a * ++-x")
        val parser = ExpressionParser(source)

        val exp1 = assertInfixExpression(parser.readExpression(NewLinePolicy.RESPECT), 1, 1)
        assertSpecialCharacter(exp1.operator, SpecialCharType.MINUS, 1, 11)

        val exp2 = assertInfixExpression(exp1.first, 1, 1) // -x + ++y.z
        assertSpecialCharacter(exp2.operator, SpecialCharType.PLUS, 1, 4)

        val exp3 = assertInfixExpression(exp1.second, 1, 12) // a * ++-x
        assertSpecialCharacter(exp3.operator, SpecialCharType.STAR, 1, 14)
        assertEulReference(exp3.first, "a", 1, 12)

        val exp4 = assertPrefixExpression(exp2.first, 1, 1) // -x
        assertSpecialCharacter(exp4.operator, SpecialCharType.MINUS, 1, 1)
        assertEulReference(exp4.getTarget(), "x", 1, 2)

        val exp5 = assertPrefixExpression(exp2.second, 1, 6) // ++y.z
        assertSpecialCharacter(exp5.operator, SpecialCharType.DOUBLE_PLUS, 1, 6)

        val exp6 = assertInfixExpression(exp5.getTarget(), 1, 8) // y.z
        assertEulReference(exp6.first, "y", 1, 8)
        assertSpecialCharacter(exp6.operator, SpecialCharType.DOT, 1, 9)
        assertEulReference(exp6.second, "z", 1, 10)

        val exp7 = assertPrefixExpression(exp3.second, 1, 16) // ++-x
        assertSpecialCharacter(exp7.operator, SpecialCharType.DOUBLE_PLUS, 1, 16)

        val exp8 = assertPrefixExpression(exp7.getTarget(), 1, 18) // -x
        assertSpecialCharacter(exp8.operator, SpecialCharType.MINUS, 1, 18)
        assertEulReference(exp8.getTarget(), "x", 1, 19)
   }

    @Test
    fun shouldHandlePrefixInParenthesis() {
        val source = this.createTokenSource("++(-+x)")
        val parser = ExpressionParser(source)

        val exp1 = assertPrefixExpression(parser.readExpression(NewLinePolicy.RESPECT), 1, 1)
        assertSpecialCharacter(exp1.operator, SpecialCharType.DOUBLE_PLUS, 1, 1)

        val exp2 = assertPrefixExpression(exp1.getTarget(), 1, 4)
        assertSpecialCharacter(exp2.operator, SpecialCharType.MINUS, 1, 4)

        val exp3 = assertPrefixExpression(exp2.getTarget(), 1, 5)
        assertSpecialCharacter(exp3.operator, SpecialCharType.PLUS, 1, 5)
        assertEulReference(exp3.getTarget(), "x", 1, 6)
    }


    /// SUFFIX EXPRESSIONS
    @Test
    fun shouldParseSimpleSuffixExpression() {
        val source = this.createTokenSource("x++")
        val parser = ExpressionParser(source)

        val asSuffix = assertSuffixExpression(parser.readExpression(NewLinePolicy.RESPECT), 1, 1)
        assertEulReference(asSuffix.getTarget(), "x", 1, 1)
        assertSpecialCharacter(asSuffix.operator, SpecialCharType.DOUBLE_PLUS, 1, 2)
    }

    @Test
    fun shouldParseMultipleSuffixOperatorsAsLtrWithSamePrecedence() {
        val source = this.createTokenSource("x++--()++")
        val parser = ExpressionParser(source)

        val exp1 = assertSuffixExpression(parser.readExpression(NewLinePolicy.RESPECT), 1, 1)
        assertSpecialCharacter(exp1.operator, SpecialCharType.DOUBLE_PLUS, 1, 8)

        val exp2 = assertSuffixExpression(exp1.getTarget(), 1, 1)
        assertSpecialCharacter(exp2.operator, SpecialCharType.PARENTHESIS_OPEN, 1, 6)

        val exp3 = assertSuffixExpression(exp2.getTarget(), 1, 1)
        assertSpecialCharacter(exp3.operator, SpecialCharType.DOUBLE_MINUS, 1, 4)

        val exp4 = assertSuffixExpression(exp3.getTarget(), 1, 1)
        assertSpecialCharacter(exp4.operator, SpecialCharType.DOUBLE_PLUS, 1, 2)
        assertEulReference(exp4.getTarget(), "x", 1, 1)
    }

    @Test
    fun shouldMixPrefixAndSuffixOperators() {
        val source = this.createTokenSource("-x()")
        val parser = ExpressionParser(source)

        val exp1 = assertPrefixExpression(parser.readExpression(NewLinePolicy.RESPECT), 1, 1)
        assertSpecialCharacter(exp1.operator, SpecialCharType.MINUS, 1, 1)

        val exp2 = assertSuffixExpression(exp1.getTarget(), 1, 2)
        assertSpecialCharacter(exp2.operator, SpecialCharType.PARENTHESIS_OPEN, 1, 3)
        assertEulReference(exp2.getTarget(),"x", 1, 2)
    }

    @Test
    fun shouldMixSuffixAndInfixOperators() {
        val source = this.createTokenSource("x-- + y.z++-a * x()++")
        val parser = ExpressionParser(source)

        val exp1 = assertInfixExpression(parser.readExpression(NewLinePolicy.RESPECT), 1, 1)
        assertSpecialCharacter(exp1.operator, SpecialCharType.MINUS, 1, 12)

        val exp2 = assertInfixExpression(exp1.first, 1, 1) // x-- + y.z++
        assertSpecialCharacter(exp2.operator, SpecialCharType.PLUS, 1, 5)

        val exp3 = assertInfixExpression(exp1.second, 1, 13) // a * x()++
        assertSpecialCharacter(exp3.operator, SpecialCharType.STAR, 1, 15)
        assertEulReference(exp3.first, "a", 1, 13)

        val exp4 = assertSuffixExpression(exp2.first, 1, 1) // x--
        assertSpecialCharacter(exp4.operator, SpecialCharType.DOUBLE_MINUS, 1, 2)
        assertEulReference(exp4.getTarget(), "x", 1, 1)

        val exp5 = assertSuffixExpression(exp2.second, 1, 7) // y.z++
        assertSpecialCharacter(exp5.operator, SpecialCharType.DOUBLE_PLUS, 1, 10)

        val exp6 = assertInfixExpression(exp5.getTarget(), 1, 7) // y.z
        assertEulReference(exp6.first, "y", 1, 7)
        assertSpecialCharacter(exp6.operator, SpecialCharType.DOT, 1, 8)
        assertEulReference(exp6.second, "z", 1, 9)

        val exp7 = assertSuffixExpression(exp3.second, 1, 17) // x()++
        assertSpecialCharacter(exp7.operator, SpecialCharType.DOUBLE_PLUS, 1, 20)

        val exp8 = assertSuffixExpression(exp7.getTarget(), 1, 17) // x()
        assertSpecialCharacter(exp8.operator, SpecialCharType.PARENTHESIS_OPEN, 1, 18)
        assertEulReference(exp8.getTarget(), "x", 1, 17)
    }

    @Test
    fun shouldHandleSuffixInParenthesis() {
        val source = this.createTokenSource("(x--())++")
        val parser = ExpressionParser(source)

        val exp1 = assertSuffixExpression(parser.readExpression(NewLinePolicy.RESPECT), 1, 2)
        assertSpecialCharacter(exp1.operator, SpecialCharType.DOUBLE_PLUS, 1, 8)

        val exp2 = assertSuffixExpression(exp1.getTarget(), 1, 2)
        assertSpecialCharacter(exp2.operator, SpecialCharType.PARENTHESIS_OPEN, 1, 5)

        val exp3 = assertSuffixExpression(exp2.getTarget(), 1, 2)
        assertSpecialCharacter(exp3.operator, SpecialCharType.DOUBLE_MINUS, 1, 3)
        assertEulReference(exp3.getTarget(), "x", 1, 2)
    }

    // TODO test new line character handling


    /// UTILS
    private fun createTokenSource(code: String) : TokenSource {
        return TokenSource(StringReader(code), this.logger, this.options)
    }
}
