package eu.alkismavridis.euljava.parser.expressions

import eu.alkismavridis.euljava.core.CompileOptions
import eu.alkismavridis.euljava.core.EulLogger
import eu.alkismavridis.euljava.core.ast.operators.SpecialCharType
import eu.alkismavridis.euljava.parser.DefaultEulParser
import eu.alkismavridis.euljava.parser.ParserException
import eu.alkismavridis.euljava.parser.TokenSource
import eu.alkismavridis.euljava.test_utils.EulAssert.Companion.assertBooleanLiteral
import eu.alkismavridis.euljava.test_utils.EulAssert.Companion.assertEulReference
import eu.alkismavridis.euljava.test_utils.EulAssert.Companion.assertInfixExpression
import eu.alkismavridis.euljava.test_utils.EulAssert.Companion.assertIntegerLiteral
import eu.alkismavridis.euljava.test_utils.EulAssert.Companion.assertPrefixExpression
import eu.alkismavridis.euljava.test_utils.EulAssert.Companion.assertSpecialCharacter
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import java.io.StringReader

internal class ExpressionParserTest {
    private val logger = Mockito.mock(EulLogger::class.java)
    private val options = CompileOptions("")


    @Test
    fun shouldParseIntegerToken() {
        val source = this.createTokenSource("5")
        val parser = ExpressionParser(source)

        assertIntegerLiteral(parser.readExpression(ExpressionBreaker.STATEMENT_EXPRESSION, false), 5, 32, true, 1, 1)
        assertThat(source.getNextToken(false)).isNull()
    }

    @Test
    fun shouldParseBooleanToken() {
        val source = this.createTokenSource("true")
        val parser = ExpressionParser(source)

        assertBooleanLiteral(parser.readExpression(ExpressionBreaker.STATEMENT_EXPRESSION, false), true, 1, 1)
        assertThat(source.getNextToken(false)).isNull()
    }

    @Test
    fun requireExpression_shouldThrowOnEmptyExpression() {
        val source = this.createTokenSource("")
        val parser = ExpressionParser(source)

        assertThatExceptionOfType(ParserException::class.java)
                .isThrownBy { parser.requireExpression(ExpressionBreaker.STATEMENT_EXPRESSION, false) }
    }

    @Test
    fun readExpression_shouldReturnNullOnEmptyExpression() {
        val source = this.createTokenSource("")
        val parser = ExpressionParser(source)

        assertThat(parser.readExpression(ExpressionBreaker.STATEMENT_EXPRESSION, false)).isNull()
    }



    @Test
    fun shouldParseSimplePrefixExpression() {
        val source = this.createTokenSource("-x")
        val parser = ExpressionParser(source)

        val asPrefix = assertPrefixExpression(parser.readExpression(ExpressionBreaker.STATEMENT_EXPRESSION, false), 1, 1)
        assertSpecialCharacter(asPrefix.operator, SpecialCharType.MINUS, 1, 1)
        assertEulReference(asPrefix.target, "x", 1, 2)
    }

    @Test
    fun shouldParseSimpleInfixExpression() {
        val source = this.createTokenSource("x + y")
        val parser = ExpressionParser(source)

        val asPrefix = assertInfixExpression(parser.readExpression(ExpressionBreaker.STATEMENT_EXPRESSION, false), 1, 1)
        assertEulReference(asPrefix.first, "x", 1, 1)
        assertSpecialCharacter(asPrefix.operator, SpecialCharType.PLUS, 1, 3)
        assertEulReference(asPrefix.second, "y", 1, 5)
    }

    @Test
    fun shouldParseLtrOperatorsOfSamePrecedence() {
        val source = this.createTokenSource("x + y + z")
        val parser = ExpressionParser(source)

        val topLevelInfix = assertInfixExpression(parser.readExpression(ExpressionBreaker.STATEMENT_EXPRESSION, false), 1, 1)
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

        val topLevelInfix = assertInfixExpression(parser.readExpression(ExpressionBreaker.STATEMENT_EXPRESSION, false), 1, 1)

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

        val topLevelInfix = assertInfixExpression(parser.readExpression(ExpressionBreaker.STATEMENT_EXPRESSION, false), 1, 1)
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

        val topLevelInfix = assertInfixExpression(parser.readExpression(ExpressionBreaker.STATEMENT_EXPRESSION, false), 1, 1)

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

        val topLevelInfix = assertInfixExpression(parser.readExpression(ExpressionBreaker.STATEMENT_EXPRESSION, false), 1, 1)

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



    /// UTILS
    private fun createTokenSource(code: String) : TokenSource {
        return DefaultEulParser(StringReader(code), this.logger, this.options)
    }
}
