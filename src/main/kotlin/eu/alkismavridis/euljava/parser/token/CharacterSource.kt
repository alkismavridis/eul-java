package eu.alkismavridis.euljava.parser.token

interface CharacterSource {
    fun getNextChar(): Char
    fun rollBackCharacter(ch: Char)
    fun getLine() : Int
    fun getColumn() : Int
}
