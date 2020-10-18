package eu.alkismavridis.euljava.parser.statements

import eu.alkismavridis.euljava.core.ast.EulToken
import eu.alkismavridis.euljava.core.ast.expressions.EulExpression
import eu.alkismavridis.euljava.core.ast.operators.SpecialCharType
import eu.alkismavridis.euljava.core.ast.statements.EulStatement
import eu.alkismavridis.euljava.core.ast.statements.IfStatement
import eu.alkismavridis.euljava.parser.EulStatementParser
import eu.alkismavridis.euljava.parser.StatementLevel
import eu.alkismavridis.euljava.parser.TokenSource
import eu.alkismavridis.euljava.parser.expressions.ExpressionParser
import eu.alkismavridis.euljava.parser.expressions.NewLinePolicy

class IfBlockParser(private val source: TokenSource, private val expressionParser: ExpressionParser, private val statementParser: EulStatementParser) {

    /** Considers the "if" token already parsed. Consumes all else-if */
    fun parse(openingToken: EulToken, parentStatementLevel: StatementLevel) : IfStatement {
        val condition = this.readCondition()
        val statements = this.readBlockStatements(parentStatementLevel, "Expected statement of '{' after if-condition")

        // TODO read if elses and else


        return IfStatement(openingToken.line, openingToken.column, condition, statements, null, null, null)

    }

    private fun readCondition() : EulExpression {
        this.source.requireSpecialChar(false, SpecialCharType.PARENTHESIS_OPEN, "'('")
        val condition = this.expressionParser.requireExpression(NewLinePolicy.IGNORE)
        this.source.requireSpecialChar(true, SpecialCharType.PARENTHESIS_CLOSE, "')'")

        return condition
    }

    private fun readBlockStatements(parentStatementLevel: StatementLevel, eofError: String) : List<EulStatement> {
        val openingBlockToken = this.source.requireNextToken(true, eofError)
        if (openingBlockToken.getSpecialCharType() == SpecialCharType.CURLY_OPEN) {
            val result = this.statementParser.getStatements(StatementLevel.BLOCK)
            this.source.requireSpecialChar(true, SpecialCharType.CURLY_CLOSE, "'}'")
            return result
        } else {
            this.source.rollBackToken(openingBlockToken)
            val statement = this.statementParser.requireNextStatement(parentStatementLevel, eofError)
            return listOf(statement)
        }
    }
}
