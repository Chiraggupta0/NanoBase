package parser;

import exception.DBException;
import lexer.Token;
import lexer.Token.TokenType;

import java.util.*;

/**
 * LAYER 2 — PARSER
 *
 * Reads the token list produced by Tokenizer and builds a Query object.
 *
 * Supports:
 *   CREATE TABLE name (col1, col2, ...)
 *   DROP TABLE name
 *   INSERT INTO name VALUES (v1, v2, ...)
 *   SELECT * or col1,col2 FROM name [WHERE condition]
        *   UPDATE name SET col=val [WHERE condition]
        *   DELETE FROM name [WHERE condition]
        */
        public class QueryParser {

            private final List<Token> tokens;
            private int pos;

            public QueryParser(List<Token> tokens) {
                this.tokens = tokens;
                this.pos    = 0;
            }

            public Query parse() {
                Token first = peek();

                if (first.isKeyword("CREATE"))  return parseCreate();
                if (first.isKeyword("DROP"))    return parseDrop();
                if (first.isKeyword("INSERT"))  return parseInsert();
                if (first.isKeyword("SELECT"))  return parseSelect();
                if (first.isKeyword("UPDATE"))  return parseUpdate();
                if (first.isKeyword("DELETE"))  return parseDelete();

                throw new DBException(DBException.ErrorType.SYNTAX_ERROR,
                        "Unknown command: " + first.getValue());
            }

            // ── CREATE TABLE ──────────────────────────────────────────────────

            private Query parseCreate() {
                Query q = new Query();
                q.setType(Query.QueryType.CREATE_TABLE);

                consume("CREATE");
                consume("TABLE");

                q.setTableName(consumeIdentifier());
                consume(TokenType.LPAREN);

                List<String> cols = new ArrayList<>();
                cols.add(consumeIdentifier());
                while (peek().is(TokenType.COMMA)) {
                    consume(TokenType.COMMA);
                    cols.add(consumeIdentifier());
                }
                consume(TokenType.RPAREN);
                q.setColumns(cols);
                return q;
            }

            // ── DROP TABLE ────────────────────────────────────────────────────

            private Query parseDrop() {
                Query q = new Query();
                q.setType(Query.QueryType.DROP_TABLE);
                consume("DROP");
                consume("TABLE");
                q.setTableName(consumeIdentifier());
                return q;
            }

            // ── INSERT INTO ───────────────────────────────────────────────────

            private Query parseInsert() {
                Query q = new Query();
                q.setType(Query.QueryType.INSERT);

                consume("INSERT");
                consume("INTO");
                q.setTableName(consumeIdentifier());
                consume("VALUES");
                consume(TokenType.LPAREN);

                List<String> vals = new ArrayList<>();
                vals.add(consumeValue());
                while (peek().is(TokenType.COMMA)) {
                    consume(TokenType.COMMA);
                    vals.add(consumeValue());
                }
                consume(TokenType.RPAREN);
                q.setValues(vals);
                return q;
            }

            // ── SELECT ────────────────────────────────────────────────────────

            private Query parseSelect() {
                Query q = new Query();
                q.setType(Query.QueryType.SELECT);

                consume("SELECT");

                List<String> cols = new ArrayList<>();
                if (peek().is(TokenType.STAR)) {
                    consume(TokenType.STAR);
                    cols.add("*");
                } else {
                    cols.add(consumeIdentifier());
                    while (peek().is(TokenType.COMMA)) {
                        consume(TokenType.COMMA);
                        cols.add(consumeIdentifier());
                    }
                }
                q.setColumns(cols);

                consume("FROM");
                q.setTableName(consumeIdentifier());

                if (peek().isKeyword("WHERE")) {
                    consume("WHERE");
                    q.setCondition(parseCondition());
                }
                return q;
            }

            // ── UPDATE ────────────────────────────────────────────────────────

            private Query parseUpdate() {
                Query q = new Query();
                q.setType(Query.QueryType.UPDATE);

                consume("UPDATE");
                q.setTableName(consumeIdentifier());
                consume("SET");

                Map<String, String> setMap = new LinkedHashMap<>();
                String col = consumeIdentifier();
                advance(); // skip = operator
                String val = consumeValue();
                setMap.put(col, val);

                while (peek().is(TokenType.COMMA)) {
                    consume(TokenType.COMMA);
                    col = consumeIdentifier();
                    advance(); // skip = operator
                    val = consumeValue();
                    setMap.put(col, val);
                }
                q.setSetValues(setMap);

                if (peek().isKeyword("WHERE")) {
                    consume("WHERE");
                    q.setCondition(parseCondition());
                }
                return q;
            }

            // ── DELETE ────────────────────────────────────────────────────────

            private Query parseDelete() {
                Query q = new Query();
                q.setType(Query.QueryType.DELETE);

                consume("DELETE");
                consume("FROM");
                q.setTableName(consumeIdentifier());

                if (peek().isKeyword("WHERE")) {
                    consume("WHERE");
                    q.setCondition(parseCondition());
                }
                return q;
            }

            // ── WHERE condition parser ────────────────────────────────────────

            private Query.Condition parseCondition() {
                String col = consumeIdentifier();
                String op  = consume(TokenType.OPERATOR);
                String val = consumeValue();

                if (peek().is(TokenType.AND) || peek().is(TokenType.OR)) {
                    String logic = advance().getValue(); // AND / OR
                    Query.Condition next = parseCondition();
                    return new Query.Condition(col, op, val, logic, next);
                }
                return new Query.Condition(col, op, val);
            }

            // ── Token consumption helpers ─────────────────────────────────────

            private Token peek() {
                return tokens.get(pos);
            }

            private Token advance() {
                return tokens.get(pos++);
            }

            private void consume(String keyword) {
                Token t = advance();
                if (!t.isKeyword(keyword)) {
                    throw new DBException(DBException.ErrorType.SYNTAX_ERROR,
                            "Expected keyword '" + keyword + "' but got '" + t.getValue() + "'");
                }
            }

            private String consume(TokenType type) {
                Token t = advance();
                if (!t.is(type)) {
                    throw new DBException(DBException.ErrorType.SYNTAX_ERROR,
                            "Expected token type " + type + " but got " + t.getType() + " ('" + t.getValue() + "')");
                }
                return t.getValue();
            }

            private String consumeIdentifier() {
                Token t = advance();
                if (!t.is(TokenType.IDENTIFIER) && !t.is(TokenType.KEYWORD)) {
                    throw new DBException(DBException.ErrorType.SYNTAX_ERROR,
                            "Expected identifier but got '" + t.getValue() + "'");
                }
                return t.getValue();
            }

            /** Consumes a value token: number, string literal, or identifier */
            private String consumeValue() {
                Token t = advance();
                if (t.is(TokenType.NUMBER) || t.is(TokenType.STRING_LITERAL) || t.is(TokenType.IDENTIFIER)) {
                    return t.getValue();
                }
                throw new DBException(DBException.ErrorType.SYNTAX_ERROR,
                        "Expected a value but got '" + t.getValue() + "'");
            }
        }