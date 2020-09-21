package eu.alkismavridis.euljava

import eu.alkismavridis.euljava.cli.CliCompiler
import java.io.IOException
import java.util.logging.LogManager



class Main
fun main(args : Array<String>) {
    configureLogger()
    CliCompiler().compile(args)
}

private fun configureLogger() {
    try {
        val stream = Main::class.java.classLoader.getResourceAsStream("logging.properties")!!
        LogManager.getLogManager().readConfiguration(stream)
        stream.close()
    } catch (e: IOException) {
        e.printStackTrace()
    }
}

