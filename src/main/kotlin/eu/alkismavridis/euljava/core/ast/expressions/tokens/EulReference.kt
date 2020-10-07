package eu.alkismavridis.euljava.core.ast.expressions.tokens

import eu.alkismavridis.euljava.core.ast.expressions.EulExpression

class EulReference(val name: String, line: Int, column: Int) : EulExpression(line, column) {}
