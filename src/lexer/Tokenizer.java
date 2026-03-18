package lexer;

import exception.DBException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * LAYER 1 — LEXER
 *
 * Converts a raw SQL string into a list of Token objects.
 *
 * Example:
 *   Input:  "SELECT * FROM students WHERE age > 19"
 *   Output: [KEYWORD:SELECT] [STAR:*] [KEYWORD:FROM] [IDENTIFIER:students]
 *           [KEYWORD:WHERE] [IDENTIFIER:age] [OPERATOR:>] [NUMBER:19]
 */
public class Tokenizer {

    // All supported SQL keywords
    private static final Set<String> KEYWORDS = Set.of(
            "SELECT", "INSERT", "UPDATE", "DELETE", "CREATE", "DROP",
            "FROM", "WHERE", "INTO", "VALUES", "SET", "TABLE",
            "AND", "OR", "INT", "STRING", "FLOAT"
    );

    private final String input;
    private int pos;

    public Tokenizer(String input) {
        this.input = input.trim();
        this.pos   = 0;
    }

    /**
     * Main method — tokenizes the full input and returns token list.
     */
    public List<Token> tokenize() {
        List<Token> tokens = new ArrayList<>();

        while (pos < input.length()) {
            skipWhitespace();
            if (pos >= input.length()) break;

            char c = input.charAt(pos);

            if (Character.isLetter(c) || c == '_') {
                tokens.add(readWord());
            } else if (Character.isDigit(c) || (c == '-' && isNextDigit())) {
                tokens.add(readNumber());
            } else if (c == '\'') {
                tokens.add(readStringLiteral());
            } else if (isOperatorChar(c)) {
                tokens.add(readOperator());
            } else if (c == ',') {
                tokens.add(new Token(Token.TokenType.COMMA, ","));
                pos++;
            } else if (c == '*') {
                tokens.add(new Token(Token.TokenType.STAR, "*"));
                pos++;
            } else if (c == '(') {
                tokens.add(new Token(Token.TokenType.LPAREN, "("));
                pos++;
            } else if (c == ')') {
                tokens.add(new Token(Token.TokenType.RPAREN, ")"));
                pos++;
            } else {
                throw new DBException(DBException.ErrorType.SYNTAX_ERROR,
                        "Unexpected character '" + c + "' at position " + pos);
            }
        }

        tokens.add(new Token(Token.TokenType.EOF, ""));
        return tokens;
    }

    // ── Private helpers ──────────────────────────────────────────────

    private void skipWhitespace() {
        while (pos < input.length() && Character.isWhitespace(input.charAt(pos))) {
            pos++;
        }
    }

    private boolean isNextDigit() {
        return (pos + 1 < input.length()) && Character.isDigit(input.charAt(pos + 1));
    }

    private boolean isOperatorChar(char c) {
        return c == '=' || c == '>' || c == '<' || c == '!';
    }

    /**
     * Reads a word token — could be keyword, AND/OR, or identifier.
     */
    private Token readWord() {
        int start = pos;
        while (pos < input.length() && (Character.isLetterOrDigit(input.charAt(pos)) || input.charAt(pos) == '_')) {
            pos++;
        }
        String word = input.substring(start, pos);

        if (word.equalsIgnoreCase("AND")) return new Token(Token.TokenType.AND, word.toUpperCase());
        if (word.equalsIgnoreCase("OR"))  return new Token(Token.TokenType.OR,  word.toUpperCase());

        if (KEYWORDS.contains(word.toUpperCase())) {
            return new Token(Token.TokenType.KEYWORD, word.toUpperCase());
        }
        return new Token(Token.TokenType.IDENTIFIER, word);
    }

    /**
     * Reads a numeric token (int or float).
     */
    private Token readNumber() {
        int start = pos;
        if (input.charAt(pos) == '-') pos++; // handle negative
        while (pos < input.length() && (Character.isDigit(input.charAt(pos)) || input.charAt(pos) == '.')) {
            pos++;
        }
        return new Token(Token.TokenType.NUMBER, input.substring(start, pos));
    }

    /**
     * Reads a single-quoted string literal: 'Alice' → Alice
     */
    private Token readStringLiteral() {
        pos++; // skip opening '
        int start = pos;
        while (pos < input.length() && input.charAt(pos) != '\'') {
            pos++;
        }
        if (pos >= input.length()) {
            throw new DBException(DBException.ErrorType.SYNTAX_ERROR, "Unterminated string literal");
        }
        String value = input.substring(start, pos);
        pos++; // skip closing '
        return new Token(Token.TokenType.STRING_LITERAL, value);
    }

    /**
     * Reads operator tokens: =, >, <, >=, <=, !=
     */
    private Token readOperator() {
        char c = input.charAt(pos);
        if (pos + 1 < input.length()) {
            String two = input.substring(pos, pos + 2);
            if (two.equals(">=") || two.equals("<=") || two.equals("!=")) {
                pos += 2;
                return new Token(Token.TokenType.OPERATOR, two);
            }
        }
        pos++;
        return new Token(Token.TokenType.OPERATOR, String.valueOf(c));
    }
}