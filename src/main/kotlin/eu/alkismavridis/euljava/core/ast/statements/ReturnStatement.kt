package eu.alkismavridis.euljava.core.ast.statements

import eu.alkismavridis.euljava.core.ast.expressions.EulExpression

class ReturnStatement(val value: EulExpression?, line: Int, column: Int) : EulStatement(line, column) {}
