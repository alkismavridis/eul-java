package eu.alkismavridis.euljava.parser

import eu.alkismavridis.euljava.core.CompileOptions
import eu.alkismavridis.euljava.core.EulLogger
import eu.alkismavridis.euljava.core.ast.EulToken
import eu.alkismavridis.euljava.core.ast.keywords.KeywordType
import eu.alkismavridis.euljava.core.ast.operators.SpecialCharacterType
import eu.alkismavridis.euljava.core.ast.statements.ContinueStatement
import eu.alkismavridis.euljava.core.ast.statements.EmptyStatement
import eu.alkismavridis.euljava.core.ast.statements.EulStatement
import eu.alkismavridis.euljava.core.ast.statements.ReturnStatement
import eu.alkismavridis.euljava.parser.token.EulTokenizer
import java.io.BufferedReader

class DefaultEulParser(reader: BufferedReader, private val logger: EulLogger, private val options: CompileOptions) : TokenSource, EulParser {
    private val tokenizer = EulTokenizer(reader, logger, options)
    private var rolledBackToken: EulToken? = null

    //private val expressionParser = ExpressionParser(this)

    override fun getNextStatement(): EulStatement? {
        val firstToken = this.getNextToken(true) ?: return null

        when (firstToken.getKeywordType()) {
            KeywordType.RETURN -> return this.parseReturnStatement(firstToken)
            KeywordType.CONTINUE -> return ContinueStatement(firstToken.line, firstToken.column)
        }


        if (firstToken.getSpecialCharacterType() == SpecialCharacterType.SEMICOLON) {
            return EmptyStatement(firstToken.line, firstToken.column)
        }

        throw ParserException(firstToken.line, firstToken.column, "Unexpected token for statement start")

    }


    private fun parseReturnStatement(returnToken: EulToken): ReturnStatement {
        //val expression = this.expressionParser.readExpression(ExpressionBreaker.STATEMENT_EXPRESSION)


        return ReturnStatement(null, returnToken.line, returnToken.column) //TODO
    }


    /// TOKEN READING
    override fun getNextToken(skipNewLines: Boolean): EulToken? {
        if (this.rolledBackToken != null) {
            val result = this.rolledBackToken
            this.rolledBackToken = null
            return result
        }

        return this.tokenizer.getNextToken(skipNewLines)
    }

    override fun rollBackToken(token: EulToken) {
        if (this.rolledBackToken != null) {
            throw ParserException(token.line, token.column, "Cannot roll back more than one token")
        }
        this.rolledBackToken = token
    }
}
