package eu.alkismavridis.euljava.core.ast.expressions.tokens

import eu.alkismavridis.euljava.core.ast.expressions.EulExpression

class StringLiteral(val value: String,line: Int, column: Int) : EulExpression(line, column)
