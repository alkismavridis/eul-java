package eu.alkismavridis.euljava.parser

import eu.alkismavridis.euljava.core.CompileOptions
import eu.alkismavridis.euljava.core.EulLogger
import eu.alkismavridis.euljava.core.ast.keywords.KeywordType
import eu.alkismavridis.euljava.core.ast.operators.SpecialCharType
import eu.alkismavridis.euljava.test_utils.EulAssert.Companion.assertBooleanLiteral
import eu.alkismavridis.euljava.test_utils.EulAssert.Companion.assertEulReference
import eu.alkismavridis.euljava.test_utils.EulAssert.Companion.assertInfixExpression
import eu.alkismavridis.euljava.test_utils.EulAssert.Companion.assertIntegerLiteral
import eu.alkismavridis.euljava.test_utils.EulAssert.Companion.assertReturnStatement
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
    fun shouldParseFullSingleConstEndingInEof() {
        val parser = this.createParser("const x:Int = 5 + 6")
        val statement = assertVarDeclarationStatement(parser.getNextStatement(StatementLevel.TOP_LEVEL), KeywordType.CONST,1, 1)
        assertThat(statement.declarations.size).isEqualTo(1)
        assertEulReference(statement.declarations[0].name, "x", 1, 7)
        assertType(statement.declarations[0].type, "Int", 1, 9)

        val value = assertInfixExpression(statement.declarations[0].value, 1, 15)
        assertIntegerLiteral(value.first, 5, 32, true, 1, 15)
        assertSpecialCharacter(value.operator, SpecialCharType.PLUS, 1, 17)
        assertIntegerLiteral(value.second, 6, 32, true, 1, 19)

        assertThat(parser.source.getNextToken(false)).isNull()
    }

    @Test
    fun shouldParseFullSingleLetEndingInEof() {
        val parser = this.createParser("let x:Int = 22u")
        val statement = assertVarDeclarationStatement(parser.getNextStatement(StatementLevel.TOP_LEVEL), KeywordType.LET, 1, 1)
        assertThat(statement.declarations.size).isEqualTo(1)
        assertEulReference(statement.declarations[0].name, "x", 1, 5)
        assertType(statement.declarations[0].type, "Int", 1, 7)

        assertIntegerLiteral(statement.declarations[0].value, 22, 32, false, 1, 13)
        assertThat(parser.source.getNextToken(false)).isNull()
    }

    @Test
    fun shouldParseFullSingleConstEndingInSemicolon() {
        val parser = this.createParser("const x:Int = 22u;")
        val statement = assertVarDeclarationStatement(parser.getNextStatement(StatementLevel.TOP_LEVEL), KeywordType.CONST, 1, 1)
        assertThat(statement.declarations.size).isEqualTo(1)
        assertEulReference(statement.declarations[0].name, "x", 1, 7)
        assertType(statement.declarations[0].type, "Int", 1, 9)

        assertIntegerLiteral(statement.declarations[0].value, 22, 32, false, 1, 15)
        assertThat(parser.source.getNextToken(false)).isNull()
    }

    @Test
    fun shouldParseFullSingleLetEndingInSemicolon() {
        val parser = this.createParser("let x:Int = 22u;")
        val statement = assertVarDeclarationStatement(parser.getNextStatement(StatementLevel.TOP_LEVEL), KeywordType.LET, 1, 1)
        assertThat(statement.declarations.size).isEqualTo(1)
        assertEulReference(statement.declarations[0].name, "x", 1, 5)
        assertType(statement.declarations[0].type, "Int", 1, 7)

        assertIntegerLiteral(statement.declarations[0].value, 22, 32, false, 1, 13)
        assertThat(parser.source.getNextToken(false)).isNull()
    }

    @Test
    fun shouldParseFullSingleConstEndingInNewLine() {
        val parser = this.createParser("const x:Int = 22u\nreturn")
        val statement = assertVarDeclarationStatement(parser.getNextStatement(StatementLevel.TOP_LEVEL), KeywordType.CONST, 1, 1)
        assertThat(statement.declarations.size).isEqualTo(1)
        assertEulReference(statement.declarations[0].name, "x", 1, 7)
        assertType(statement.declarations[0].type, "Int", 1, 9)

        assertIntegerLiteral(statement.declarations[0].value, 22, 32, false, 1, 15)
        assertReturnStatement(parser.getNextStatement(StatementLevel.TOP_LEVEL), 2, 1)
    }

    @Test
    fun shouldParseFullSingleLetEndingInNewLine() {
        val parser = this.createParser("let x:Int = 22u\nreturn")
        val statement = assertVarDeclarationStatement(parser.getNextStatement(StatementLevel.TOP_LEVEL), KeywordType.LET, 1, 1)
        assertThat(statement.declarations.size).isEqualTo(1)
        assertEulReference(statement.declarations[0].name, "x", 1, 5)
        assertType(statement.declarations[0].type, "Int", 1, 7)

        assertIntegerLiteral(statement.declarations[0].value, 22, 32, false, 1, 13)
        assertReturnStatement(parser.getNextStatement(StatementLevel.TOP_LEVEL), 2, 1)
    }

    @Test
    fun shouldHandleMultipleDeclarations() {
        val parser = this.createParser("let x:Int = 22u, y:Boolean = true\nreturn")
        val statement = assertVarDeclarationStatement(parser.getNextStatement(StatementLevel.TOP_LEVEL), KeywordType.LET, 1, 1)
        assertThat(statement.declarations.size).isEqualTo(2)

        assertEulReference(statement.declarations[0].name, "x", 1, 5)
        assertType(statement.declarations[0].type, "Int", 1, 7)
        assertIntegerLiteral(statement.declarations[0].value, 22, 32, false, 1, 13)

        assertEulReference(statement.declarations[1].name, "y", 1, 18)
        assertType(statement.declarations[1].type, "Boolean", 1, 20)
        assertBooleanLiteral(statement.declarations[1].value, true, 1, 30)

        assertReturnStatement(parser.getNextStatement(StatementLevel.TOP_LEVEL), 2, 1)
    }


    /// DECLARATIONS WITHOUT TYPE
    @Test
    fun shouldParseSingleTypelessConstEndingInEof() {
        val parser = this.createParser("const x = 5 + 6")
        val statement = assertVarDeclarationStatement(parser.getNextStatement(StatementLevel.TOP_LEVEL), KeywordType.CONST,1, 1)
        assertThat(statement.declarations.size).isEqualTo(1)
        assertEulReference(statement.declarations[0].name, "x", 1, 7)
        assertThat(statement.declarations[0].type).isNull()


        val value = assertInfixExpression(statement.declarations[0].value, 1, 11)
        assertIntegerLiteral(value.first, 5, 32, true, 1, 11)
        assertSpecialCharacter(value.operator, SpecialCharType.PLUS, 1, 13)
        assertIntegerLiteral(value.second, 6, 32, true, 1, 15)

        assertThat(parser.source.getNextToken(false)).isNull()
    }

    @Test
    fun shouldParseSingleTypelessLetEndingInEof() {
        val parser = this.createParser("let x = 5 + 6")
        val statement = assertVarDeclarationStatement(parser.getNextStatement(StatementLevel.TOP_LEVEL), KeywordType.LET,1, 1)
        assertThat(statement.declarations.size).isEqualTo(1)
        assertEulReference(statement.declarations[0].name, "x", 1, 5)
        assertThat(statement.declarations[0].type).isNull()

        val value = assertInfixExpression(statement.declarations[0].value, 1, 9)
        assertIntegerLiteral(value.first, 5, 32, true, 1, 9)
        assertSpecialCharacter(value.operator, SpecialCharType.PLUS, 1, 11)
        assertIntegerLiteral(value.second, 6, 32, true, 1, 13)

        assertThat(parser.source.getNextToken(false)).isNull()
    }

    @Test
    fun shouldParseSingleTypelessConstEndingInSemicolon() {
        val parser = this.createParser("const x = 22u;")
        val statement = assertVarDeclarationStatement(parser.getNextStatement(StatementLevel.TOP_LEVEL), KeywordType.CONST, 1, 1)
        assertThat(statement.declarations.size).isEqualTo(1)
        assertEulReference(statement.declarations[0].name, "x", 1, 7)
        assertThat(statement.declarations[0].type).isNull()

        assertIntegerLiteral(statement.declarations[0].value, 22, 32, false, 1, 11)
        assertThat(parser.source.getNextToken(false)).isNull()
    }

    @Test
    fun shouldParseSingleTypelessLetEndingInSemicolon() {
        val parser = this.createParser("let x = 22u;")
        val statement = assertVarDeclarationStatement(parser.getNextStatement(StatementLevel.TOP_LEVEL), KeywordType.LET, 1, 1)
        assertThat(statement.declarations.size).isEqualTo(1)
        assertEulReference(statement.declarations[0].name, "x", 1, 5)
        assertThat(statement.declarations[0].type).isNull()

        assertIntegerLiteral(statement.declarations[0].value, 22, 32, false, 1, 9)
        assertThat(parser.source.getNextToken(false)).isNull()
    }

    @Test
    fun shouldParseSingleTypelessConstEndingInNewLine() {
        val parser = this.createParser("const x = 22u\nreturn")
        val statement = assertVarDeclarationStatement(parser.getNextStatement(StatementLevel.TOP_LEVEL), KeywordType.CONST, 1, 1)
        assertThat(statement.declarations.size).isEqualTo(1)
        assertEulReference(statement.declarations[0].name, "x", 1, 7)
        assertThat(statement.declarations[0].type).isNull()

        assertIntegerLiteral(statement.declarations[0].value, 22, 32, false, 1, 11)
        assertReturnStatement(parser.getNextStatement(StatementLevel.TOP_LEVEL), 2, 1)
    }

    @Test
    fun shouldParseSingleTypelessLetEndingInNewLine() {
        val parser = this.createParser("let x = 22u\nreturn")
        val statement = assertVarDeclarationStatement(parser.getNextStatement(StatementLevel.TOP_LEVEL), KeywordType.LET, 1, 1)
        assertThat(statement.declarations.size).isEqualTo(1)
        assertEulReference(statement.declarations[0].name, "x", 1, 5)
        assertThat(statement.declarations[0].type).isNull()

        assertIntegerLiteral(statement.declarations[0].value, 22, 32, false, 1, 9)
        assertReturnStatement(parser.getNextStatement(StatementLevel.TOP_LEVEL), 2, 1)
    }

    @Test
    fun shouldHandleMultipleTypelessDeclarations() {
        val parser = this.createParser("let x = 22u, y = true\nreturn")
        val statement = assertVarDeclarationStatement(parser.getNextStatement(StatementLevel.TOP_LEVEL), KeywordType.LET, 1, 1)
        assertThat(statement.declarations.size).isEqualTo(2)

        assertEulReference(statement.declarations[0].name, "x", 1, 5)
        assertThat(statement.declarations[0].type).isNull()
        assertIntegerLiteral(statement.declarations[0].value, 22, 32, false, 1, 9)

        assertEulReference(statement.declarations[1].name, "y", 1, 14)
        assertThat(statement.declarations[1].type).isNull()
        assertBooleanLiteral(statement.declarations[1].value, true, 1, 18)

        assertReturnStatement(parser.getNextStatement(StatementLevel.TOP_LEVEL), 2, 1)
    }


    /// UTILS
    private fun createParser(code: String) : EulStatementParser {
        return EulStatementParser(StringReader(code), this.logger, this.options)
    }
}
