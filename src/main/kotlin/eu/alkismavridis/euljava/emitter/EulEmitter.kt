package eu.alkismavridis.euljava.emitter

import eu.alkismavridis.euljava.core.ast.statements.EulStatement

interface EulEmitter {
    fun emitStatements(statements: List<EulStatement>)
}
