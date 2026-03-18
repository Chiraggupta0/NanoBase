package engine;

import java.io.Serializable;
import java.util.*;

/**
 * Represents one row of data in a table.
 *
 * Internally stored as a LinkedHashMap so column order is preserved.
 *
 * Example row in "students" table:
 *   { id="1", name="Alice", age="20", gpa="9.1" }
 */
public class Row implements Serializable {

    private static final long serialVersionUID = 1L;

    // column name → value (all stored as String, cast on comparison)
    private final Map<String, String> data;

    public Row() {
        this.data = new LinkedHashMap<>();
    }

    public Row(Map<String, String> data) {
        this.data = new LinkedHashMap<>(data);
    }

    // ── Data access ───────────────────────────────────────────────────

    public void set(String column, String value) {
        data.put(column, value);
    }

    public String get(String column) {
        return data.get(column);
    }

    public boolean hasColumn(String column) {
        return data.containsKey(column);
    }

    public Map<String, String> getData() {
        return Collections.unmodifiableMap(data);
    }

    public Set<String> getColumns() {
        return data.keySet();
    }

    /**
     * Returns a copy of this row with updated values from the given map.
     */
    public Row withUpdates(Map<String, String> updates) {
        Map<String, String> newData = new LinkedHashMap<>(data);
        newData.putAll(updates);
        return new Row(newData);
    }

    @Override
    public String toString() {
        return data.toString();
    }
}