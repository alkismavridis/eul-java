package eu.alkismavridis.euljava.parser

import eu.alkismavridis.euljava.core.ast.expressions.tokens.BooleanLiteral
import eu.alkismavridis.euljava.core.ast.expressions.tokens.IntegerLiteral
import eu.alkismavridis.euljava.core.ast.statements.EulStatement
import eu.alkismavridis.euljava.core.ast.statements.ReturnStatement
import java.io.BufferedReader


class DummyEulParser(private val reader: BufferedReader) : EulParser {
    private val statementsIterator = arrayOf(
        ReturnStatement(null, 5, 5),
        ReturnStatement(BooleanLiteral(true, 5, 5), 5, 5),
        ReturnStatement(IntegerLiteral(56, 32, true, 5, 5), 5, 5)
    ).iterator()

    override fun getNextStatement(): EulStatement? {
        return if (this.statementsIterator.hasNext()) this.statementsIterator.next()
        else null
    }
}
