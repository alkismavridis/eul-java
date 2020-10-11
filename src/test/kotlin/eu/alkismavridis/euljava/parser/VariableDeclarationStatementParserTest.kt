package eu.alkismavridis.euljava.parser

import eu.alkismavridis.euljava.core.CompileOptions
import eu.alkismavridis.euljava.core.EulLogger
import eu.alkismavridis.euljava.core.ast.keywords.KeywordType
import eu.alkismavridis.euljava.core.ast.operators.SpecialCharType
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


    /// SINGLE DECLARATIONS
    @Test
    fun shouldParseFullSingleConstEndingInEof() {
        val source = this.createParser("const x:Int = 5 + 6")
        val statement = assertVarDeclarationStatement(source.getNextStatement(), KeywordType.CONST,1, 1)
        assertThat(statement.declarations.size).isEqualTo(1)
        assertEulReference(statement.declarations[0].name, "x", 1, 7)
        assertType(statement.declarations[0].type, "Int", 1, 9)


        val value = assertInfixExpression(statement.declarations[0].value, 1, 15)
        assertIntegerLiteral(value.first, 5, 32, true, 1, 15)
        assertSpecialCharacter(value.operator, SpecialCharType.PLUS, 1, 17)
        assertIntegerLiteral(value.second, 6, 32, true, 1, 19)

        assertThat(source.getNextToken(false)).isNull()
    }

    @Test
    fun shouldParseFullSingleLetEndingInEof() {
        val source = this.createParser("let x:Int = 22u")
        val statement = assertVarDeclarationStatement(source.getNextStatement(), KeywordType.LET, 1, 1)
        assertThat(statement.declarations.size).isEqualTo(1)
        assertEulReference(statement.declarations[0].name, "x", 1, 5)
        assertType(statement.declarations[0].type, "Int", 1, 7)


        assertIntegerLiteral(statement.declarations[0].value, 22, 32, false, 1, 13)
        assertThat(source.getNextToken(false)).isNull()
    }

    @Test
    fun shouldParseFullSingleConstEndingInSemicolon() {
        val source = this.createParser("const x:Int = 22u;")
        val statement = assertVarDeclarationStatement(source.getNextStatement(), KeywordType.CONST, 1, 1)
        assertThat(statement.declarations.size).isEqualTo(1)
        assertEulReference(statement.declarations[0].name, "x", 1, 7)
        assertType(statement.declarations[0].type, "Int", 1, 9)

        assertIntegerLiteral(statement.declarations[0].value, 22, 32, false, 1, 15)
        assertThat(source.getNextToken(false)).isNull()
    }

    @Test
    fun shouldParseFullSingleLetEndingInSemicolon() {
        val source = this.createParser("let x:Int = 22u;")
        val statement = assertVarDeclarationStatement(source.getNextStatement(), KeywordType.LET, 1, 1)
        assertThat(statement.declarations.size).isEqualTo(1)
        assertEulReference(statement.declarations[0].name, "x", 1, 5)
        assertType(statement.declarations[0].type, "Int", 1, 7)

        assertIntegerLiteral(statement.declarations[0].value, 22, 32, false, 1, 13)
        assertThat(source.getNextToken(false)).isNull()
    }

    @Test
    fun shouldParseFullSingleConstEndingInNewLine() {
        val source = this.createParser("const x:Int = 22u\nreturn")
        val statement = assertVarDeclarationStatement(source.getNextStatement(), KeywordType.CONST, 1, 1)
        assertThat(statement.declarations.size).isEqualTo(1)
        assertEulReference(statement.declarations[0].name, "x", 1, 7)
        assertType(statement.declarations[0].type, "Int", 1, 9)

        assertIntegerLiteral(statement.declarations[0].value, 22, 32, false, 1, 15)
        assertReturnStatement(source.getNextStatement(), 2, 1)
    }

    @Test
    fun shouldParseFullSingleLetEndingInNewLine() {
        val source = this.createParser("let x:Int = 22u\nreturn")
        val statement = assertVarDeclarationStatement(source.getNextStatement(), KeywordType.LET, 1, 1)
        assertThat(statement.declarations.size).isEqualTo(1)
        assertEulReference(statement.declarations[0].name, "x", 1, 5)
        assertType(statement.declarations[0].type, "Int", 1, 7)

        assertIntegerLiteral(statement.declarations[0].value, 22, 32, false, 1, 13)
        assertReturnStatement(source.getNextStatement(), 2, 1)
    }


    /// UTILS
    private fun createParser(code: String) : DefaultEulParser {
        return DefaultEulParser(StringReader(code), this.logger, this.options)
    }
}
