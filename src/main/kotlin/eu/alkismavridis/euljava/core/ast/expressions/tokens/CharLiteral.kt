package eu.alkismavridis.euljava.core.ast.expressions.tokens

import eu.alkismavridis.euljava.core.ast.expressions.EulExpression

class CharLiteral(
    val value: Long,
    val size: Int, // in bytes
    line: Int,
    column: Int
) : EulExpression(line, column)
