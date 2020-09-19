package eu.alkismavridis.euljava.parser

import eu.alkismavridis.euljava.core.CompileOptions
import eu.alkismavridis.euljava.core.EulLogger
import eu.alkismavridis.euljava.core.ast.expressions.tokens.BooleanLiteral
import eu.alkismavridis.euljava.core.ast.statements.EulStatement
import eu.alkismavridis.euljava.core.ast.statements.ReturnStatement
import eu.alkismavridis.euljava.parser.token.EulTokenizer
import java.io.BufferedReader


class DefaultEulParser(reader: BufferedReader, private val logger:EulLogger, private val options: CompileOptions) : EulParser {
    private val tokeniser = EulTokenizer(reader, logger, options)

    override fun getNextStatement(): EulStatement? {
        val nextToken = tokeniser.getNextToken(true) ?: return null
        return ReturnStatement(BooleanLiteral(true, 5, 5), 5, 5)
    }
}
