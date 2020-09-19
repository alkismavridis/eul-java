package eu.alkismavridis.euljava.core.ast.expressions.tokens

import eu.alkismavridis.euljava.core.ast.expressions.EulExpression

class FloatLiteral(
    val value: Double,
    val size: Int, // in bytes
    line: Int,
    column: Int
) : EulExpression(line, column)
