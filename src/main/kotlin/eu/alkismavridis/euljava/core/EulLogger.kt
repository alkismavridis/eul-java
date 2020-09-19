package eu.alkismavridis.euljava.core

interface EulLogger {
    fun info(line: Int, column: Int, message: String)
    fun info(message: String)

    fun warn(line: Int, column: Int, message: String)
    fun warn(message: String)

    fun error(line: Int, column: Int, message: String)
    fun error(message: String)

    fun fatal(line: Int, column: Int, message: String)
    fun fatal(message: String)
}
