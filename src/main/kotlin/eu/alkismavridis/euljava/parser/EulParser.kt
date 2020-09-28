package eu.alkismavridis.euljava.parser

import eu.alkismavridis.euljava.core.ast.statements.EulStatement
import java.lang.Exception

class ParserException(val line: Int, val column: Int, message: String) : Exception(message)

interface EulParser {
    fun getNextStatement() : EulStatement?
}
