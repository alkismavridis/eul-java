package eu.alkismavridis.euljava.parser

import eu.alkismavridis.euljava.core.CompileOptions
import eu.alkismavridis.euljava.core.EulLogger
import eu.alkismavridis.euljava.core.ast.keywords.KeywordToken
import eu.alkismavridis.euljava.core.ast.keywords.KeywordType
import eu.alkismavridis.euljava.core.ast.operators.SpecialCharType
import eu.alkismavridis.euljava.parser.expressions.ExpressionParser
import eu.alkismavridis.euljava.parser.statements.VariableDeclarationStatementParser
import eu.alkismavridis.euljava.test_utils.EulAssert.Companion.assertBooleanLiteral
import eu.alkismavridis.euljava.test_utils.EulAssert.Companion.assertEulReference
import eu.alkismavridis.euljava.test_utils.EulAssert.Companion.assertInfixExpression
import eu.alkismavridis.euljava.test_utils.EulAssert.Companion.assertIntegerLiteral
import eu.alkismavridis.euljava.test_utils.EulAssert.Companion.assertKeyword
import eu.alkismavridis.euljava.test_utils.EulAssert.Companion.assertSpecialCharacter
import eu.alkismavridis.euljava.test_utils.EulAssert.Companion.assertType
import eu.alkismavridis.euljava.test_utils.EulAssert.Companion.assertVarDeclarationStatement
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import java.io.StringReader

internal class VariableDeclarationStatementParserTest {
    private val logger = Mockito.mock(EulLogger::class.java)
    private val options = CompileOptions("")


    /// FULL DECLARATIONS
    @Test
    fun shouldParseFullDeclarationEndingInEof() {
        val source = this.createTokenSource("x:Int = 5 + 6")
        val parser = this.createParser(source)
        val openingToken = KeywordToken(KeywordType.CONST, 1, 1)

        val statement = assertVarDeclarationStatement(parser.parse(openingToken), KeywordType.CONST,1, 1)
        assertThat(statement.declarations.size).isEqualTo(1)
        assertEulReference(statement.declarations[0].name, "x", 1, 1)
        assertType(statement.declarations[0].type, "Int", 1, 3)

        val value = assertInfixExpression(statement.declarations[0].value, 1, 9)
        assertIntegerLiteral(value.first, 5, 32, true, 1, 9)
        assertSpecialCharacter(value.operator, SpecialCharType.PLUS, 1, 11)
        assertIntegerLiteral(value.second, 6, 32, true, 1, 13)

        assertThat(source.getNextToken(false)).isNull()
    }


    @Test
    fun shouldParseFullDeclarationEndingInSemicolon() {
        val source = this.createTokenSource("x:Int = 22u;")
        val parser = this.createParser(source)
        val openingToken = KeywordToken(KeywordType.CONST, 1, 1)

        val statement = assertVarDeclarationStatement(parser.parse(openingToken), KeywordType.CONST, 1, 1)
        assertThat(statement.declarations.size).isEqualTo(1)
        assertEulReference(statement.declarations[0].name, "x", 1, 1)
        assertType(statement.declarations[0].type, "Int", 1, 3)

        assertIntegerLiteral(statement.declarations[0].value, 22, 32, false, 1, 9)
        assertThat(source.getNextToken(false)).isNull()
    }

    @Test
    fun shouldParseFullDeclarationEndingInNewLine() {
        val source = this.createTokenSource("x:Int = 22u\nreturn")
        val parser = this.createParser(source)
        val openingToken = KeywordToken(KeywordType.CONST, 1, 1)

        val statement = assertVarDeclarationStatement(parser.parse(openingToken), KeywordType.CONST, 1, 1)
        assertThat(statement.declarations.size).isEqualTo(1)
        assertEulReference(statement.declarations[0].name, "x", 1, 1)
        assertType(statement.declarations[0].type, "Int", 1, 3)

        assertIntegerLiteral(statement.declarations[0].value, 22, 32, false, 1, 9)
        assertKeyword(source.getNextToken(false), KeywordType.RETURN, 2, 1)
    }

    @Test
    fun shouldHandleMultipleDeclarations() {
        val source = this.createTokenSource("x:Int = 22u, y:Boolean = true\nreturn")
        val parser = this.createParser(source)
        val openingToken = KeywordToken(KeywordType.LET, 1, 1)

        val statement = assertVarDeclarationStatement(parser.parse(openingToken), KeywordType.LET, 1, 1)
        assertThat(statement.declarations.size).isEqualTo(2)

        assertEulReference(statement.declarations[0].name, "x", 1, 1)
        assertType(statement.declarations[0].type, "Int", 1, 3)
        assertIntegerLiteral(statement.declarations[0].value, 22, 32, false, 1, 9)

        assertEulReference(statement.declarations[1].name, "y", 1, 14)
        assertType(statement.declarations[1].type, "Boolean", 1, 16)
        assertBooleanLiteral(statement.declarations[1].value, true, 1, 26)

        assertKeyword(source.getNextToken(false), KeywordType.RETURN, 2, 1)
    }


    /// DECLARATIONS WITHOUT TYPE
    @Test
    fun shouldParseTypelessDeclarationEndingInEof() {
        val source = this.createTokenSource("x = 5 + 6")
        val parser = this.createParser(source)
        val openingToken = KeywordToken(KeywordType.CONST, 1, 1)

        val statement = assertVarDeclarationStatement(parser.parse(openingToken), KeywordType.CONST,1, 1)
        assertThat(statement.declarations.size).isEqualTo(1)
        assertEulReference(statement.declarations[0].name, "x", 1, 1)
        assertThat(statement.declarations[0].type).isNull()


        val value = assertInfixExpression(statement.declarations[0].value, 1, 5)
        assertIntegerLiteral(value.first, 5, 32, true, 1, 5)
        assertSpecialCharacter(value.operator, SpecialCharType.PLUS, 1, 7)
        assertIntegerLiteral(value.second, 6, 32, true, 1, 9)

        assertThat(source.getNextToken(false)).isNull()
    }

    @Test
    fun shouldParseTypelessDeclarationEndingInSemicolon() {
        val source = this.createTokenSource("x = 22u;")
        val parser = this.createParser(source)
        val openingToken = KeywordToken(KeywordType.CONST, 1, 1)

        val statement = assertVarDeclarationStatement(parser.parse(openingToken), KeywordType.CONST, 1, 1)
        assertThat(statement.declarations.size).isEqualTo(1)
        assertEulReference(statement.declarations[0].name, "x", 1, 1)
        assertThat(statement.declarations[0].type).isNull()

        assertIntegerLiteral(statement.declarations[0].value, 22, 32, false, 1, 5)
        assertThat(source.getNextToken(false)).isNull()
    }

    @Test
    fun shouldParseTypelessDeclarationEndingInNewLine() {
        val source = this.createTokenSource("x = 22u\nreturn")
        val parser = this.createParser(source)
        val openingToken = KeywordToken(KeywordType.CONST, 1, 1)

        val statement = assertVarDeclarationStatement(parser.parse(openingToken), KeywordType.CONST, 1, 1)
        assertThat(statement.declarations.size).isEqualTo(1)
        assertEulReference(statement.declarations[0].name, "x", 1, 1)
        assertThat(statement.declarations[0].type).isNull()

        assertIntegerLiteral(statement.declarations[0].value, 22, 32, false, 1, 5)
        assertKeyword(source.getNextToken(false), KeywordType.RETURN, 2, 1)
    }

    @Test
    fun shouldHandleMultipleTypelessDeclarations() {
        val source = this.createTokenSource("x = 22u, y = true\nreturn")
        val parser = this.createParser(source)
        val openingToken = KeywordToken(KeywordType.LET, 1, 1)

        val statement = assertVarDeclarationStatement(parser.parse(openingToken), KeywordType.LET, 1, 1)
        assertThat(statement.declarations.size).isEqualTo(2)

        assertEulReference(statement.declarations[0].name, "x", 1, 1)
        assertThat(statement.declarations[0].type).isNull()
        assertIntegerLiteral(statement.declarations[0].value, 22, 32, false, 1, 5)

        assertEulReference(statement.declarations[1].name, "y", 1, 10)
        assertThat(statement.declarations[1].type).isNull()
        assertBooleanLiteral(statement.declarations[1].value, true, 1, 14)

        assertKeyword(source.getNextToken(false), KeywordType.RETURN, 2, 1)
    }


    /// UTILS
    private fun createTokenSource(code: String) : TokenSource {
        return TokenSource(StringReader(code), this.logger, this.options)
    }

    private fun createParser(source: TokenSource) : VariableDeclarationStatementParser {
        val expressionParser = ExpressionParser(source)
        val typeParser = TypeParser(source)
        return VariableDeclarationStatementParser(source, expressionParser, typeParser)
    }
}
