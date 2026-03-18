package lexer;

/**
 * Represents a single token produced by the Tokenizer.
 * Each token has a type and a value.
 *
 * Example: "SELECT" → Token(KEYWORD, "SELECT")
 *          "students" → Token(IDENTIFIER, "students")
 *          ">"  → Token(OPERATOR, ">")
 */
public class Token {

    public enum TokenType {
        // SQL Keywords
        KEYWORD,        // SELECT, INSERT, UPDATE, DELETE, CREATE, DROP, FROM, WHERE, INTO, VALUES, SET, TABLE

        // Data
        IDENTIFIER,     // table names, column names
        STRING_LITERAL, // 'Alice'
        NUMBER,         // 42, 3.14

        // Symbols
        OPERATOR,       // =, >, <, >=, <=, !=
        COMMA,          // ,
        STAR,           // *
        LPAREN,         // (
        RPAREN,         // )

        // Logic
        AND,            // AND
        OR,             // OR

        EOF             // end of input
    }

    private final TokenType type;
    private final String value;

    public Token(TokenType type, String value) {
        this.type  = type;
        this.value = value;
    }

    public TokenType getType()  { return type;  }
    public String    getValue() { return value; }

    public boolean is(TokenType t)       { return this.type == t; }
    public boolean isKeyword(String kw)  { return type == TokenType.KEYWORD && value.equalsIgnoreCase(kw); }

    @Override
    public String toString() {
        return String.format("Token(%s, \"%s\")", type, value);
    }
}