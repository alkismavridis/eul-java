package eu.alkismavridis.euljava.parser.token

class CharMetadata {
    companion object {
        fun isWordStart(ch: Char): Boolean {
            if (ch in 'a'..'z') return true
            if (ch in 'A'..'Z') return true
            if (ch == '_') return true
            return false
        }

        fun isWordPart(ch: Char): Boolean {
            if (ch in 'a'..'z') return true
            if (ch in 'A'..'Z') return true
            if (ch == '_') return true
            if (ch in '0'..'9') return true
            return false
        }

        fun isDecimalDigit(ch: Char): Boolean {
            return ch in '0'..'9'
        }

        /** \n is not considered whitespace by this method */
        fun isWhiteSpace(ch: Char): Boolean {
            return ch == ' ' ||
                    ch == '\t' ||
                    ch in '\u000b'..'\u000d'
        }
    }
}
