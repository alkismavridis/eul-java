package eu.alkismavridis.euljava.emitter

import eu.alkismavridis.euljava.core.ast.statements.EulStatement
import eu.alkismavridis.euljava.core.EulLogger

class DoNothingEmitter(private val eulLogger: EulLogger) : EulEmitter {
    override fun emitStatements(statements: List<EulStatement>) {
        statements.forEach{
            this.eulLogger.info("Emitting statement... ${it.javaClass.canonicalName}")
        }
    }
}
