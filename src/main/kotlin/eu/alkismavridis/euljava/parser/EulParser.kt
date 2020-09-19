package eu.alkismavridis.euljava.parser

import eu.alkismavridis.euljava.core.ast.statements.EulStatement

interface EulParser {
    fun getNextStatement() : EulStatement?
}
