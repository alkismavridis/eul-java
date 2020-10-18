package eu.alkismavridis.euljava.core.ast.statements

import eu.alkismavridis.euljava.core.ast.EulToken
import eu.alkismavridis.euljava.core.ast.expressions.EulExpression

class ElseIfBlock (
    line: Int,
    column: Int,
    val condition: EulExpression,
    val statements: List<EulStatement>
) : EulToken(line, column)


class IfStatement(
        line: Int,
        column: Int,
        val condition: EulExpression,
        val ifBlockStatements: List<EulStatement>,
        val elseIfBlocks: List<ElseIfBlock>?,
        val elseToken: EulToken?,
        val elseBlockStatements: List<EulStatement>?
) : EulStatement(line, column)
