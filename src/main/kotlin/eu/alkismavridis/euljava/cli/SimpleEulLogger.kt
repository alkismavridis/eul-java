package eu.alkismavridis.euljava.cli

import eu.alkismavridis.euljava.core.EulFatalErrorException
import eu.alkismavridis.euljava.core.EulLogger
import java.util.logging.Logger

class SimpleEulLogger  : EulLogger {
    var hasErrors: Boolean = false; private set

    companion object {
        val log = Logger.getLogger(this::class.java.canonicalName)!!
    }

    override fun info(line: Int, column: Int, message: String) {
        log.info("$line - $column: $message")
    }

    override fun info(message: String) {
        log.info(message)
    }

    override fun warn(line: Int, column: Int, message: String) {
        log.warning("$line - $column: $message")
    }

    override fun warn(message: String) {
        log.warning(message)
    }

    override fun error(line: Int, column: Int, message: String) {
        this.hasErrors = true
        log.severe("$line - $column: $message")
    }

    override fun error(message: String) {
        this.hasErrors = true
        log.severe(message)
    }

    override fun fatal(line: Int, column: Int, message: String) {
        this.hasErrors = true
        log.severe("$line - $column: $message")
        throw EulFatalErrorException("Abort.")
    }

    override fun fatal(message: String) {
        this.hasErrors = true
        log.severe(message)
        throw EulFatalErrorException("Abort.")
    }
}
