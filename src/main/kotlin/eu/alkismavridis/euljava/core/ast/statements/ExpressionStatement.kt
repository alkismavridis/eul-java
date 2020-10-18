package eu.alkismavridis.euljava.core.ast.statements

import eu.alkismavridis.euljava.core.ast.expressions.EulExpression

class ExpressionStatement(val expression: EulExpression, line: Int, column: Int) : EulStatement(line, column)
