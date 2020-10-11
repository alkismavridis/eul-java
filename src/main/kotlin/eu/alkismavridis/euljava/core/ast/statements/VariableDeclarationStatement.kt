package eu.alkismavridis.euljava.core.ast.statements

import eu.alkismavridis.euljava.core.ast.EulToken
import eu.alkismavridis.euljava.core.ast.expressions.EulExpression
import eu.alkismavridis.euljava.core.ast.expressions.tokens.EulReference
import eu.alkismavridis.euljava.core.ast.types.TypeExpression


class VariableDeclaration(
        val name: EulReference,
        val type: TypeExpression?,
        val value: EulExpression?
)

class VariableDeclarationStatement(
        val openingToken: EulToken,
        val declarations: List<VariableDeclaration>
) : EulStatement(openingToken.line, openingToken.column)
