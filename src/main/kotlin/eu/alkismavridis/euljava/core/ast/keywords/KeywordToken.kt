package eu.alkismavridis.euljava.core.ast.keywords

import eu.alkismavridis.euljava.core.ast.EulToken

class KeywordToken(val text: String, line: Int, column: Int) : EulToken(line, column) {
}
