package eu.alkismavridis.euljava.parser.statements

import eu.alkismavridis.euljava.core.ast.EulToken
import eu.alkismavridis.euljava.core.ast.expressions.tokens.EulReference
import eu.alkismavridis.euljava.core.ast.operators.SpecialCharType
import eu.alkismavridis.euljava.core.ast.statements.VariableDeclaration
import eu.alkismavridis.euljava.core.ast.statements.VariableDeclarationStatement
import eu.alkismavridis.euljava.parser.ParserException
import eu.alkismavridis.euljava.parser.TokenSource
import eu.alkismavridis.euljava.parser.TypeParser
import eu.alkismavridis.euljava.parser.expressions.NewLinePolicy
import eu.alkismavridis.euljava.parser.expressions.ExpressionParser

class VariableDeclarationStatementParser(
        private val source: TokenSource,
        private val expressionParser: ExpressionParser,
        private val typeParser: TypeParser
) {
    fun parse(openingToken: EulToken): VariableDeclarationStatement {
        val declarations = mutableListOf<VariableDeclaration>()
        loop@while(true) {
            declarations.add(this.parseVariableDeclaration())
            val closingToken = this.source.getNextToken(false) ?: break

            when(closingToken.getSpecialCharType()) {
                SpecialCharType.NEW_LINE,
                SpecialCharType.SEMICOLON -> break@loop

                SpecialCharType.COMMA -> continue@loop
                else -> throw ParserException.of(closingToken, "Expected New line, semicolon or comma")
            }
        }

        return VariableDeclarationStatement(openingToken, declarations)
    }

    private fun parseVariableDeclaration(): VariableDeclaration {
        val variableName = this.source.requireNextToken(false, "Expected variable name, but end of file was found")
        if (variableName !is EulReference) {
            throw ParserException.of(variableName, "Expected variable name")
        }

        val tokenAfterName = this.source.requireNextToken(false, "Expected : or = but end of file was found")
        when(tokenAfterName.getSpecialCharType()) {
            SpecialCharType.COLON -> {
                val type = this.typeParser.requireType()

                val equals = this.source.requireNextToken(false, "Expected = but end of file was found")
                if (equals.getSpecialCharType() != SpecialCharType.EQUALS) {
                    throw ParserException.of(equals, "Expected =")
                }

                return VariableDeclaration(
                        variableName,
                        type,
                        this.expressionParser.requireExpression(NewLinePolicy.RESPECT)
                )
            }

            SpecialCharType.EQUALS -> {
                return VariableDeclaration(
                        variableName,
                        null,
                        this.expressionParser.requireExpression(NewLinePolicy.RESPECT)
                )
            }

            else -> throw ParserException.of(tokenAfterName, "Expected variable type or value")
        }
    }
}
