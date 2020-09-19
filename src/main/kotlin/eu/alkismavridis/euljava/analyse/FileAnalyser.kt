package eu.alkismavridis.euljava.analyse

import eu.alkismavridis.euljava.core.EulLogger
import eu.alkismavridis.euljava.core.ast.statements.EulStatement

class FileAnalyser(private val logger: EulLogger, private val statements: List<EulStatement>) {
    fun analyse() {
        statements.forEach(this::preProcessStatement)
        statements.forEach(this::processStatement)
    }


    private fun preProcessStatement(stmt: EulStatement) {
        this.logger.info("I pre-process statement... ${stmt.javaClass.name}")
    }

    private fun processStatement(stmt: EulStatement) {
        this.logger.info("I process statement... ${stmt.javaClass.name}")
    }
}
