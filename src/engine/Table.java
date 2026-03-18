package engine;

import exception.DBException;
import index.BTree;

import java.io.Serializable;
import java.util.*;

/**
 * Represents a database table.
 *
 * Holds:
 *  - Schema: column names (ordered)
 *  - Rows: list of all Row objects
 *  - BTree index on the first column (primary key)
 */
public class Table implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String       name;
    private final List<String> columns;       // column names in order
    private final List<Row>    rows;          // all rows
    private final BTree        index;         // B-Tree on primary key (col[0])

    public Table(String name, List<String> columns) {
        this.name    = name;
        this.columns = new ArrayList<>(columns);
        this.rows    = new ArrayList<>();
        this.index   = new BTree(3); // order-3 B-Tree
    }

    // ── Insert ────────────────────────────────────────────────────────

    /**
     * Inserts a new row. Values must match column count.
     * The first column is treated as the primary key.
     */
    public void insert(List<String> values) {
        if (values.size() != columns.size()) {
            throw new DBException(DBException.ErrorType.INVALID_VALUE,
                    "Expected " + columns.size() + " values but got " + values.size());
        }

        // Build row map
        Map<String, String> data = new LinkedHashMap<>();
        for (int i = 0; i < columns.size(); i++) {
            data.put(columns.get(i), values.get(i));
        }

        // Check duplicate primary key
        String pk = values.get(0);
        if (index.search(pk) != -1) {
            throw new DBException(DBException.ErrorType.DUPLICATE_KEY,
                    "Duplicate primary key: " + pk);
        }

        // Add row and update index
        rows.add(new Row(data));
        index.insert(pk, rows.size() - 1); // pk → row index
    }

    // ── Select ────────────────────────────────────────────────────────

    /**
     * Returns all rows that match the given condition.
     * If condition is null, returns all rows.
     */
    public List<Row> select(List<String> selectCols, parser.Query.Condition condition) {
        List<Row> result = new ArrayList<>();

        for (Row row : rows) {
            if (condition == null || evaluateCondition(row, condition)) {
                result.add(projectRow(row, selectCols));
            }
        }
        return result;
    }

    // ── Update ────────────────────────────────────────────────────────

    /**
     * Updates all rows matching the condition with new values.
     * Returns count of updated rows.
     */
    public int update(Map<String, String> setValues, parser.Query.Condition condition) {
        int count = 0;
        for (int i = 0; i < rows.size(); i++) {
            Row row = rows.get(i);
            if (condition == null || evaluateCondition(row, condition)) {
                rows.set(i, row.withUpdates(setValues));
                count++;
            }
        }
        return count;
    }

    // ── Delete ────────────────────────────────────────────────────────

    /**
     * Deletes all rows matching the condition.
     * Returns count of deleted rows.
     */
    public int delete(parser.Query.Condition condition) {
        List<Row> toDelete = new ArrayList<>();
        for (Row row : rows) {
            if (condition == null || evaluateCondition(row, condition)) {
                toDelete.add(row);
            }
        }
        rows.removeAll(toDelete);
        rebuildIndex(); // Rebuild index after deletion
        return toDelete.size();
    }

    // ── Condition evaluator ───────────────────────────────────────────

    /**
     * Evaluates a WHERE condition (supports AND/OR chaining).
     */
    private boolean evaluateCondition(Row row, parser.Query.Condition cond) {
        if (cond == null) return true;

        boolean result = evaluateSingle(row, cond);

        if (cond.next != null) {
            boolean nextResult = evaluateCondition(row, cond.next);
            if ("AND".equals(cond.logic)) return result && nextResult;
            if ("OR".equals(cond.logic))  return result || nextResult;
        }
        return result;
    }

    /**
     * Evaluates a single condition: col op val
     */
    private boolean evaluateSingle(Row row, parser.Query.Condition cond) {
        if (!row.hasColumn(cond.column)) {
            throw new DBException(DBException.ErrorType.COLUMN_NOT_FOUND,
                    "Column not found: " + cond.column);
        }

        String rowVal  = row.get(cond.column);
        String condVal = cond.value;

        // Try numeric comparison first
        try {
            double rv = Double.parseDouble(rowVal);
            double cv = Double.parseDouble(condVal);
            return switch (cond.operator) {
                case "="  -> rv == cv;
                case "!=" -> rv != cv;
                case ">"  -> rv >  cv;
                case "<"  -> rv <  cv;
                case ">=" -> rv >= cv;
                case "<=" -> rv <= cv;
                default   -> false;
            };
        } catch (NumberFormatException e) {
            // Fall back to string comparison
            int cmp = rowVal.compareToIgnoreCase(condVal);
            return switch (cond.operator) {
                case "="  -> cmp == 0;
                case "!=" -> cmp != 0;
                case ">"  -> cmp >  0;
                case "<"  -> cmp <  0;
                case ">=" -> cmp >= 0;
                case "<=" -> cmp <= 0;
                default   -> false;
            };
        }
    }

    // ── Projection ────────────────────────────────────────────────────

    /**
     * Returns only the requested columns from a row.
     * If selectCols contains "*", returns the full row.
     */
    private Row projectRow(Row row, List<String> selectCols) {
        if (selectCols.contains("*")) return row;

        Map<String, String> projected = new LinkedHashMap<>();
        for (String col : selectCols) {
            if (!row.hasColumn(col)) {
                throw new DBException(DBException.ErrorType.COLUMN_NOT_FOUND,
                        "Column not found: " + col);
            }
            projected.put(col, row.get(col));
        }
        return new Row(projected);
    }

    // ── Index rebuild ─────────────────────────────────────────────────

    private void rebuildIndex() {
        BTree fresh = new BTree(3);
        for (int i = 0; i < rows.size(); i++) {
            fresh.insert(rows.get(i).get(columns.get(0)), i);
        }
        index.copyFrom(fresh);
    }

    // ── Getters ───────────────────────────────────────────────────────

    public String       getName()    { return name; }
    public List<String> getColumns() { return Collections.unmodifiableList(columns); }
    public List<Row>    getRows()    { return Collections.unmodifiableList(rows); }
    public int          getRowCount(){ return rows.size(); }
}