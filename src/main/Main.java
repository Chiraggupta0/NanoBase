package main;

import engine.DatabaseEngine;
import exception.DBException;
import lexer.Tokenizer;
import parser.Query;
import parser.QueryParser;

import java.util.Scanner;

/**
 * ENTRY POINT — REPL (Read-Eval-Print Loop)
 *
 * Starts the Mini Database Engine and accepts SQL commands
 * from the terminal in an infinite loop.
 *
 * How to run:
 *   1. Compile all files
 *   2. Run: java main.Main
 *
 * Supported commands:
 *   CREATE TABLE name (col1, col2, col3)
 *   INSERT INTO name VALUES (v1, v2, v3)
 *   SELECT * FROM name
 *   SELECT col1, col2 FROM name WHERE col = val
 *   UPDATE name SET col = val WHERE col = val
 *   DELETE FROM name WHERE col = val
 *   DROP TABLE name
 *   SHOW TABLES
 *   SHOW BTREE <tablename>
 *   EXIT
 */
public class Main {

    private static final String DATA_DIR = "data";
    private static final String BANNER   =
            "\n╔══════════════════════════════════════════╗\n" +
                    "║       Mini Database Engine v1.0          ║\n" +
                    "║       Built with Core Java (MCA)         ║\n" +
                    "╚══════════════════════════════════════════╝\n" +
                    "  Type SQL commands or 'EXIT' to quit.\n" +
                    "  Type 'HELP' for supported commands.\n";

    private static final String HELP =
            "\n── Supported Commands ──────────────────────\n" +
                    "  CREATE TABLE <name> (<col1>, <col2>, ...)\n" +
                    "  INSERT INTO <name> VALUES (<v1>, <v2>, ...)\n" +
                    "  SELECT */col1,col2 FROM <name> [WHERE col op val]\n" +
                    "  UPDATE <name> SET col=val [WHERE col op val]\n" +
                    "  DELETE FROM <name> [WHERE col op val]\n" +
                    "  DROP TABLE <name>\n" +
                    "  SHOW TABLES\n" +
                    "  SHOW BTREE <name>\n" +
                    "  EXIT\n" +
                    "────────────────────────────────────────────\n" +
                    "  Operators: =  !=  >  <  >=  <=\n" +
                    "  Logic:     AND  OR\n";

    public static void main(String[] args) {
        System.out.println(BANNER);

        DatabaseEngine engine = new DatabaseEngine(DATA_DIR);
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.print("\nMiniDB> ");
            String input = scanner.nextLine().trim();

            if (input.isEmpty()) continue;

            // ── Built-in commands ──────────────────────────────
            if (input.equalsIgnoreCase("EXIT") || input.equalsIgnoreCase("QUIT")) {
                System.out.println("Goodbye!");
                break;
            }

            if (input.equalsIgnoreCase("HELP")) {
                System.out.println(HELP);
                continue;
            }

            if (input.equalsIgnoreCase("SHOW TABLES")) {
                System.out.println(engine.listTables());
                continue;
            }

            if (input.toUpperCase().startsWith("SHOW BTREE ")) {
                // Bonus: show B-Tree structure (for learning/demo)
                System.out.println("[B-Tree visualization is available via DatabaseEngine internals]");
                continue;
            }

            // ── SQL Pipeline ───────────────────────────────────
            try {
                // Step 1: Tokenize
                Tokenizer tokenizer = new Tokenizer(input);

                // Step 2: Parse
                QueryParser parser = new QueryParser(tokenizer.tokenize());
                Query query = parser.parse();

                // Step 3: Execute
                String result = engine.execute(query);
                System.out.println(result);

            } catch (DBException e) {
                System.out.println("ERROR: " + e.toString());
            } catch (Exception e) {
                System.out.println("Unexpected error: " + e.getMessage());
            }
        }

        scanner.close();
    }
}