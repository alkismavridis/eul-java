package eu.alkismavridis.euljava.parser.expressions


enum class NewLinePolicy(val ignoreFirst: Boolean, val ignoreAll: Boolean) {
    RESPECT(false, false),
    IGNORE(true, true)
}
