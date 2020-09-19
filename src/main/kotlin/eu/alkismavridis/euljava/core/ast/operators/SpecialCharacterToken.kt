package eu.alkismavridis.euljava.core.ast.operators

import eu.alkismavridis.euljava.core.ast.EulToken

class SpecialCharacterToken(val text : String, line: Int, column: Int) : EulToken(line, column) {
}
