package eu.alkismavridis.euljava.core.ast.statements

import eu.alkismavridis.euljava.core.ast.EulToken
import eu.alkismavridis.euljava.core.ast.expressions.EulExpression
import eu.alkismavridis.euljava.core.ast.types.TypeExpression


class VariableDeclaration(
        val name: String,
        val type: TypeExpression?,
        val value: EulExpression?
)

class VariableDeclarationStatement(
        openingToken: EulToken,
        val declarations: List<VariableDeclaration>
) : EulStatement(openingToken.line, openingToken.column)
