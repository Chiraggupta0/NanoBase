package engine;

import exception.DBException;
import parser.Query;
import storage.StorageManager;

import java.util.*;

/**
 * LAYER 5 — QUERY EXECUTOR
 *
 * The core of the database engine. Receives a parsed Query object,
 * loads the appropriate table, executes the operation, saves changes,
 * and returns a formatted result string.
 *
 * This is the equivalent of MySQL's "query execution engine".
 */
public class DatabaseEngine {

    private final StorageManager storage;

    // In-memory cache of loaded tables (avoids re-reading disk every query)
    private final Map<String, Table> tableCache;

    public DatabaseEngine(String dataDir) {
        this.storage    = new StorageManager(dataDir);
        this.tableCache = new HashMap<>();

        // Load all existing tables into cache on startup
        for (String name : storage.listTables()) {
            Table t = storage.loadTable(name);
            if (t != null) tableCache.put(name.toLowerCase(), t);
        }
    }

    // ── Main execute method ───────────────────────────────────────────

    /**
     * Executes a parsed Query and returns a result string for display.
     */
    public String execute(Query query) {
        return switch (query.getType()) {
            case CREATE_TABLE -> executeCreate(query);
            case DROP_TABLE   -> executeDrop(query);
            case INSERT       -> executeInsert(query);
            case SELECT       -> executeSelect(query);
            case UPDATE       -> executeUpdate(query);
            case DELETE       -> executeDelete(query);
        };
    }

    // ── CREATE TABLE ──────────────────────────────────────────────────

    private String executeCreate(Query q) {
        String name = q.getTableName().toLowerCase();

        if (storage.tableExists(name)) {
            throw new DBException(DBException.ErrorType.TABLE_ALREADY_EXISTS,
                    "Table '" + name + "' already exists");
        }

        Table table = new Table(name, q.getColumns());
        tableCache.put(name, table);
        storage.saveTable(table);

        return "✓ Table '" + name + "' created with columns: " + q.getColumns();
    }

    // ── DROP TABLE ────────────────────────────────────────────────────

    private String executeDrop(Query q) {
        String name = q.getTableName().toLowerCase();
        requireTable(name);

        tableCache.remove(name);
        storage.deleteTable(name);

        return "✓ Table '" + name + "' dropped.";
    }

    // ── INSERT ────────────────────────────────────────────────────────

    private String executeInsert(Query q) {
        String name = q.getTableName().toLowerCase();
        Table table = requireTable(name);

        table.insert(q.getValues());
        storage.saveTable(table); // persist to disk

        return "✓ 1 row inserted into '" + name + "'.";
    }

    // ── SELECT ────────────────────────────────────────────────────────

    private String executeSelect(Query q) {
        String name = q.getTableName().toLowerCase();
        Table table = requireTable(name);

        List<Row> results = table.select(q.getColumns(), q.getCondition());

        if (results.isEmpty()) {
            return "No rows found.";
        }

        return formatTable(results, q.getColumns(), table.getColumns());
    }

    // ── UPDATE ────────────────────────────────────────────────────────

    private String executeUpdate(Query q) {
        String name = q.getTableName().toLowerCase();
        Table table = requireTable(name);

        int count = table.update(q.getSetValues(), q.getCondition());
        storage.saveTable(table);

        return "✓ " + count + " row(s) updated in '" + name + "'.";
    }

    // ── DELETE ────────────────────────────────────────────────────────

    private String executeDelete(Query q) {
        String name = q.getTableName().toLowerCase();
        Table table = requireTable(name);

        int count = table.delete(q.getCondition());
        storage.saveTable(table);

        return "✓ " + count + " row(s) deleted from '" + name + "'.";
    }

    // ── Table formatter ───────────────────────────────────────────────

    /**
     * Formats a list of rows as a pretty ASCII table.
     *
     * Example output:
     * +----+---------+-----+-----+
     * | id | name    | age | gpa |
     * +----+---------+-----+-----+
     * | 1  | Alice   | 20  | 9.1 |
     * | 2  | Bob     | 22  | 8.7 |
     * +----+---------+-----+-----+
     */
    private String formatTable(List<Row> rows, List<String> selectCols, List<String> allCols) {
        // Determine which columns to display
        List<String> displayCols = selectCols.contains("*") ? allCols : selectCols;

        // Calculate column widths
        Map<String, Integer> widths = new LinkedHashMap<>();
        for (String col : displayCols) {
            widths.put(col, col.length());
        }
        for (Row row : rows) {
            for (String col : displayCols) {
                String val = row.get(col);
                if (val != null) {
                    widths.put(col, Math.max(widths.get(col), val.length()));
                }
            }
        }

        // Build separator line
        StringBuilder sep = new StringBuilder("+");
        for (String col : displayCols) {
            sep.append("-".repeat(widths.get(col) + 2)).append("+");
        }
        String separator = sep.toString();

        // Build output
        StringBuilder sb = new StringBuilder();
        sb.append(separator).append("\n");

        // Header row
        sb.append("|");
        for (String col : displayCols) {
            sb.append(" ").append(pad(col, widths.get(col))).append(" |");
        }
        sb.append("\n").append(separator).append("\n");

        // Data rows
        for (Row row : rows) {
            sb.append("|");
            for (String col : displayCols) {
                String val = row.get(col) != null ? row.get(col) : "NULL";
                sb.append(" ").append(pad(val, widths.get(col))).append(" |");
            }
            sb.append("\n");
        }

        sb.append(separator);
        sb.append("\n").append(rows.size()).append(" row(s) returned.");
        return sb.toString();
    }

    private String pad(String s, int width) {
        return s + " ".repeat(Math.max(0, width - s.length()));
    }

    // ── Helper ────────────────────────────────────────────────────────

    private Table requireTable(String name) {
        Table t = tableCache.get(name.toLowerCase());
        if (t == null) {
            throw new DBException(DBException.ErrorType.TABLE_NOT_FOUND,
                    "Table '" + name + "' does not exist");
        }
        return t;
    }

    /**
     * Lists all tables currently in the database.
     */
    public String listTables() {
        List<String> names = storage.listTables();
        if (names.isEmpty()) return "No tables found.";
        StringBuilder sb = new StringBuilder("Tables in database:\n");
        names.forEach(n -> sb.append("  → ").append(n).append("\n"));
        return sb.toString().trim();
    }
}