package eu.alkismavridis.euljava.core.ast.keywords

enum class KeywordType {
    NOT_A_KEYWORD,

    /// VALUES
    TRUE,
    FALSE,
    NULL,

    /// DEFINITIONS
    VAR,
    VAL,
    FUN,


    /// CONDITIONAL
    IF,
    ELSE,
    SWITCH,

    /// LOOPS
    FOR,
    WHILE,
    BREAK,
    CONTINUE,

    /// RETURN
    RETURN,
    THROW
}
