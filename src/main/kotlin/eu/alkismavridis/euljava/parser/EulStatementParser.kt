package eu.alkismavridis.euljava.parser

import eu.alkismavridis.euljava.core.CompileOptions
import eu.alkismavridis.euljava.core.EulLogger
import eu.alkismavridis.euljava.core.ast.EulToken
import eu.alkismavridis.euljava.core.ast.keywords.KeywordType
import eu.alkismavridis.euljava.core.ast.operators.SpecialCharType
import eu.alkismavridis.euljava.core.ast.statements.*
import eu.alkismavridis.euljava.parser.expressions.NewLinePolicy
import eu.alkismavridis.euljava.parser.expressions.ExpressionParser
import eu.alkismavridis.euljava.parser.statements.IfBlockParser
import eu.alkismavridis.euljava.parser.statements.VariableDeclarationStatementParser
import java.io.Reader

enum class StatementLevel {
    TOP_LEVEL,
    BLOCK;
}

class EulStatementParser(reader: Reader, private val logger: EulLogger, private val options: CompileOptions) {
    internal val source = TokenSource(reader, logger, options)

    private val expressionParser = ExpressionParser(this.source)
    private val typeParser = TypeParser(this.source)
    private val varDeclarationParser = VariableDeclarationStatementParser(this.source, this.expressionParser, this.typeParser)
    private val ifBlockParser = IfBlockParser(this.source, this.expressionParser)

    fun getNextStatement(level: StatementLevel): EulStatement? {
        val firstToken = this.getNextStatementStart()
        if(firstToken == null) {
            this.assertEndingTokenLegality(null, level)
            return null
        }

        when (firstToken.getKeywordType()) {
            KeywordType.RETURN -> return this.parseReturnStatement(firstToken)
            KeywordType.CONTINUE -> return ContinueStatement(firstToken.line, firstToken.column)
            KeywordType.BREAK -> return BreakStatement(firstToken.line, firstToken.column)

            KeywordType.CONST,
            KeywordType.LET -> return this.varDeclarationParser.parse(firstToken)
        }

        this.assertEndingTokenLegality(firstToken, level)
        return null
    }

    private fun getNextStatementStart() : EulToken? {
        while(true) {
            val next = this.source.getNextToken(true) ?: return null
            if (next.getSpecialCharType() != SpecialCharType.SEMICOLON) {
                return next
            }
        }
    }

    private fun parseReturnStatement(returnToken: EulToken): ReturnStatement {
        val expression = this.expressionParser.readExpression(NewLinePolicy.RESPECT)
        return ReturnStatement(expression, returnToken.line, returnToken.column)
    }


    /// UTILS
    private fun assertEndingTokenLegality(token: EulToken?, level: StatementLevel) {
        when(level) {
            StatementLevel.TOP_LEVEL -> {
                if (token == null) return
                throw ParserException.of(token, "Unexpected token for statement start")
            }

            StatementLevel.BLOCK -> {
                if (token == null) {
                    throw ParserException.eof("Expected end of block")
                }

                if (token.getSpecialCharType() == SpecialCharType.CURLY_CLOSE) return
                else throw ParserException.of(token, "Unexpected token for statement start")
            }
        }
    }
}
