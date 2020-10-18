package eu.alkismavridis.euljava.parser.statements

import eu.alkismavridis.euljava.core.CompileOptions
import eu.alkismavridis.euljava.core.EulLogger
import eu.alkismavridis.euljava.core.ast.keywords.KeywordToken
import eu.alkismavridis.euljava.core.ast.keywords.KeywordType
import eu.alkismavridis.euljava.core.ast.operators.SpecialCharType
import eu.alkismavridis.euljava.parser.EulStatementParser
import eu.alkismavridis.euljava.parser.ParserException
import eu.alkismavridis.euljava.parser.StatementLevel
import eu.alkismavridis.euljava.parser.TokenSource
import eu.alkismavridis.euljava.parser.expressions.ExpressionParser
import eu.alkismavridis.euljava.test_utils.EulAssert.Companion.assertEulReference
import eu.alkismavridis.euljava.test_utils.EulAssert.Companion.assertExpressionStatement
import eu.alkismavridis.euljava.test_utils.EulAssert.Companion.assertIfStatement
import eu.alkismavridis.euljava.test_utils.EulAssert.Companion.assertReturnStatement
import eu.alkismavridis.euljava.test_utils.EulAssert.Companion.assertSpecialCharacter
import eu.alkismavridis.euljava.test_utils.EulAssert.Companion.assertSuffixExpression
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import java.io.StringReader

internal class IfBlockParserTest {
    companion object {
        private val OPENING_TOKEN = KeywordToken(KeywordType.IF, 1, 1)
    }

    private val logger = Mockito.mock(EulLogger::class.java)
    private val options = CompileOptions("")


    /// SIMPLE IFS
    @Test
    fun shouldThrowIfParenthesisOpenIsNotFound() {
        val source = this.createTokenSource("[condition] doStuff()")
        val parser = this.createParser(source)
        assertThatExceptionOfType(ParserException::class.java)
                .isThrownBy { parser.parse(OPENING_TOKEN, StatementLevel.TOP_LEVEL) }
                .withMessage("Expected '(' but SQUARE_OPEN was found")
    }

    @Test
    fun shouldThrowIfConditionIsEmpty() {
        val source = this.createTokenSource("() doStuff()")
        val parser = this.createParser(source)
        assertThatExceptionOfType(ParserException::class.java)
                .isThrownBy { parser.parse(OPENING_TOKEN, StatementLevel.TOP_LEVEL) }
                .withMessage("Expected expression")
    }

    @Test
    fun shouldThrowIfConditionDoesNotEndInParenthesisClose() {
        val source = this.createTokenSource("(condition] doStuff()")
        val parser = this.createParser(source)
        assertThatExceptionOfType(ParserException::class.java)
                .isThrownBy { parser.parse(OPENING_TOKEN, StatementLevel.TOP_LEVEL) }
                .withMessage("Expected ')' but SQUARE_CLOSE was found")
    }

    @Test
    fun shouldThrowIfEofIsFoundAfterCondition() {
        val source = this.createTokenSource("(condition) ")
        val parser = this.createParser(source)
        assertThatExceptionOfType(ParserException::class.java)
                .isThrownBy { parser.parse(KeywordToken(KeywordType.IF, 1, 1), StatementLevel.TOP_LEVEL) }
                .withMessage("Expected statement of '{' after if-condition")
    }

    @Test
    fun shouldParseSingleStatementBody() {
        val source = this.createTokenSource("(condition) doStuff()")
        val parser = this.createParser(source)
        val stmt = assertIfStatement(parser.parse(OPENING_TOKEN, StatementLevel.TOP_LEVEL), 1, 1)

        assertEulReference(stmt.condition, "condition", 1, 2)
        assertThat(stmt.ifBlockStatements.size).isEqualTo(1)

        val body = assertExpressionStatement(stmt.ifBlockStatements[0], 1, 13)
        val expression = assertSuffixExpression(body.expression, 1, 13)
        assertEulReference(expression.getTarget(), "doStuff", 1, 13)
        assertSpecialCharacter(expression.operator, SpecialCharType.PARENTHESIS_OPEN, 1, 20)
    }

    @Test
    fun shouldParseIfWithBlock() {
        val source = this.createTokenSource("(condition) { doStuff(); return }")
        val parser = this.createParser(source)
        val stmt = assertIfStatement(parser.parse(OPENING_TOKEN, StatementLevel.TOP_LEVEL), 1, 1)

        assertEulReference(stmt.condition, "condition", 1, 2)
        assertThat(stmt.ifBlockStatements.size).isEqualTo(2)

        val body = assertExpressionStatement(stmt.ifBlockStatements[0], 1, 15)
        val expression = assertSuffixExpression(body.expression, 1, 15)
        assertEulReference(expression.getTarget(), "doStuff", 1, 15)
        assertSpecialCharacter(expression.operator, SpecialCharType.PARENTHESIS_OPEN, 1, 22)

        assertReturnStatement(stmt.ifBlockStatements[1], 1, 26)
    }


    @Test
    fun shouldThrowOnIllegalBlockEnd() {
        val source = this.createTokenSource("(condition) { doStuff() ]")
        val parser = this.createParser(source)
        assertThatExceptionOfType(ParserException::class.java)
                .isThrownBy { parser.parse(KeywordToken(KeywordType.IF, 1, 1), StatementLevel.TOP_LEVEL) }
                .withMessage("Unexpected token for statement start")
    }

    @Test
    fun shouldThrowIfBlockDoesNotClose() {
        val source = this.createTokenSource("(condition) { doStuff()")
        val parser = this.createParser(source)
        assertThatExceptionOfType(ParserException::class.java)
                .isThrownBy { parser.parse(KeywordToken(KeywordType.IF, 1, 1), StatementLevel.TOP_LEVEL) }
                .withMessage("Expected end of block")
    }



    /// UTILS
    private fun createTokenSource(code: String): TokenSource {
        return TokenSource(StringReader(code), this.logger, this.options)
    }

    private fun createParser(source: TokenSource): IfBlockParser {
        val expressionParser = ExpressionParser(source)
        val statementParser = EulStatementParser(source, this.logger, this.options)

        return IfBlockParser(source, expressionParser, statementParser)
    }
}
