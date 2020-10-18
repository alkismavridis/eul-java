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

enum class StatementLevel {
    TOP_LEVEL,
    BLOCK;
}

class EulStatementParser(private val source: TokenSource, private val logger: EulLogger, private val options: CompileOptions) {
    private val expressionParser = ExpressionParser(this.source)
    private val typeParser = TypeParser(this.source)
    private val varDeclarationParser = VariableDeclarationStatementParser(this.source, this.expressionParser, this.typeParser)
    private val ifBlockParser = IfBlockParser(this.source, this.expressionParser, this)


    /// API
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
            KeywordType.IF -> return this.ifBlockParser.parse(firstToken, level)
        }

        // Check for expression statement
        this.source.rollBackToken(firstToken)
        val expression = this.expressionParser.readExpression(NewLinePolicy.RESPECT)

        if (expression == null) this.source.getNextToken(false)
        else {
            return ExpressionStatement(expression, expression.line, expression.column)
        }

        this.assertEndingTokenLegality(firstToken, level)
        this.source.rollBackToken(firstToken)
        return null
    }

    fun requireNextStatement(level: StatementLevel, notAStatementError: String) : EulStatement {
        return this.getNextStatement(level) ?: throw ParserException.eof(notAStatementError)
    }

    fun getStatements(level: StatementLevel) : List<EulStatement> {
        val statements = mutableListOf<EulStatement>()
        while(true) {
            val nextStatement = this.getNextStatement(level) ?: break
            statements.add(nextStatement)
        }

        return statements
    }

    fun assertEndOfInput() {
        val nextToken = this.source.getNextToken(true)
        if (nextToken != null) {
            throw ParserException.of(nextToken, "Unexpected token")
        }
    }


    /// UTILS
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
