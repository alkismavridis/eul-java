package eu.alkismavridis.euljava.parser

import eu.alkismavridis.euljava.core.ast.EulToken
import eu.alkismavridis.euljava.core.ast.expressions.tokens.EulReference
import eu.alkismavridis.euljava.core.ast.operators.SpecialCharType
import eu.alkismavridis.euljava.core.ast.statements.VariableDeclaration
import eu.alkismavridis.euljava.core.ast.statements.VariableDeclarationStatement
import eu.alkismavridis.euljava.parser.expressions.ExpressionBreaker
import eu.alkismavridis.euljava.parser.expressions.ExpressionParser

class VariableDeclarationStatementParser(
        private val source: TokenSource,
        private val expressionParser: ExpressionParser,
        private val typeParser: TypeParser
) {
    fun parse(openingToken: EulToken): VariableDeclarationStatement {
        val declarations = mutableListOf<VariableDeclaration>()
        while(true) {
            val nextToken = this.source.getNextToken(false) ?: break

            val specialTokenType = nextToken.getSpecialCharType()
            if (specialTokenType == SpecialCharType.NEW_LINE || specialTokenType == SpecialCharType.SEMICOLON) {
                break
            }

            if (nextToken !is EulReference) throw ParserException.of(nextToken, "Expected variable name")
            declarations.add(this.parseVariableDeclaration(nextToken))
        }

        if (declarations.isEmpty()) {
            throw ParserException.of(openingToken,"Variable declaration statement should have at least one declaration")
        }

        return VariableDeclarationStatement(openingToken, declarations)
    }

    private fun parseVariableDeclaration(variableName: EulReference): VariableDeclaration {
        val tokenAfterName = this.source.requireNextToken(false, "Expected : or = but end of file was found ")
        when(tokenAfterName.getSpecialCharType()) {
            SpecialCharType.COLON -> {
                val type = this.typeParser.requireType()

                val equals = this.source.requireNextToken(false, "Expected = but end of file was found")
                if (equals.getSpecialCharType() != SpecialCharType.EQUALS) {
                    throw ParserException.of(equals, "Expected =")
                }

                return VariableDeclaration(
                        variableName.name,
                        type,
                        this.expressionParser.requireExpression(ExpressionBreaker.COMMA_SEPARATED_EXPRESSION, true)
                )
            }

            SpecialCharType.EQUALS -> {
                return VariableDeclaration(
                        variableName.name,
                        null,
                        this.expressionParser.requireExpression(ExpressionBreaker.COMMA_SEPARATED_EXPRESSION, true)
                )
            }

            else -> throw ParserException.of(tokenAfterName, "Expected variable type or value")
        }
    }
}
