package eu.alkismavridis.euljava.parser

import eu.alkismavridis.euljava.core.CompileOptions
import eu.alkismavridis.euljava.core.EulLogger
import eu.alkismavridis.euljava.core.ast.EulToken
import eu.alkismavridis.euljava.core.ast.expressions.tokens.BooleanLiteral
import eu.alkismavridis.euljava.core.ast.keywords.KeywordType
import eu.alkismavridis.euljava.core.ast.statements.ContinueStatement
import eu.alkismavridis.euljava.core.ast.statements.EulStatement
import eu.alkismavridis.euljava.core.ast.statements.ReturnStatement
import eu.alkismavridis.euljava.parser.token.EulTokenizer
import java.io.BufferedReader


class DefaultEulParser(reader: BufferedReader, private val logger: EulLogger, private val options: CompileOptions) :
    EulParser {
    private val tokenizer = EulTokenizer(reader, logger, options)
    private var rolledBackToken: EulToken? = null

    override fun getNextStatement(): EulStatement? {
        val firstToken = this.getNextToken(true) ?: return null

        if (firstToken.getKeywordType() == KeywordType.RETURN) {
            print("A return statement!\n\n")
            return ReturnStatement(null, firstToken.line, firstToken.column)
        }

        return ContinueStatement(firstToken.line, firstToken.column)
    }

    private fun getNextToken(skipNewLines: Boolean) : EulToken? {
        if (this.rolledBackToken != null) {
            val result = this.rolledBackToken
            this.rolledBackToken = null
            return result
        }

        return this.tokenizer.getNextToken(skipNewLines)
    }
}
