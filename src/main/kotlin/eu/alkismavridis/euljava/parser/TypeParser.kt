package eu.alkismavridis.euljava.parser

import eu.alkismavridis.euljava.core.ast.types.TypeExpression

class TypeParser(private val source: TokenSource) {
    fun requireType() : TypeExpression {
        val typeReference = this.source.requireReference("Expected type", "Expected type but end of file was found")
        return TypeExpression(typeReference.name, typeReference.line, typeReference.column)
    }
}
