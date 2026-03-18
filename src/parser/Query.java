package parser;

import java.util.List;
import java.util.Map;

/**
 * A Query object holds all information extracted by the QueryParser.
 * Think of it as a structured representation of your SQL statement.
 *
 * Example:
 *   "SELECT name, age FROM students WHERE age > 18"
 *    → type       = SELECT
 *    → tableName  = "students"
 *    → columns    = ["name", "age"]
 *    → condition  = Condition{column="age", op=">", value="18"}
 */
public class Query {

    public enum QueryType {
        SELECT, INSERT, UPDATE, DELETE, CREATE_TABLE, DROP_TABLE
    }

    // ── All fields (only relevant ones are set per query type) ────────

    private QueryType         type;
    private String            tableName;
    private List<String>      columns;        // SELECT columns or CREATE column names
    private List<String>      values;         // INSERT values
    private Map<String,String> setValues;     // UPDATE col=val pairs
    private Condition         condition;      // WHERE clause

    // ── Inner class: WHERE condition ──────────────────────────────────

    public static class Condition {
        public final String column;
        public final String operator;   // =, >, <, >=, <=, !=
        public final String value;
        public final String logic;      // AND / OR (null if single condition)
        public final Condition next;    // chained condition

        public Condition(String column, String operator, String value, String logic, Condition next) {
            this.column   = column;
            this.operator = operator;
            this.value    = value;
            this.logic    = logic;
            this.next     = next;
        }

        public Condition(String column, String operator, String value) {
            this(column, operator, value, null, null);
        }

        @Override
        public String toString() {
            String base = column + " " + operator + " " + value;
            if (next != null) return base + " " + logic + " " + next;
            return base;
        }
    }

    // ── Getters & Setters ─────────────────────────────────────────────

    public QueryType          getType()       { return type; }
    public String             getTableName()  { return tableName; }
    public List<String>       getColumns()    { return columns; }
    public List<String>       getValues()     { return values; }
    public Map<String,String> getSetValues()  { return setValues; }
    public Condition          getCondition()  { return condition; }

    public void setType(QueryType type)                   { this.type = type; }
    public void setTableName(String tableName)            { this.tableName = tableName; }
    public void setColumns(List<String> columns)          { this.columns = columns; }
    public void setValues(List<String> values)            { this.values = values; }
    public void setSetValues(Map<String,String> setValues){ this.setValues = setValues; }
    public void setCondition(Condition condition)         { this.condition = condition; }

    @Override
    public String toString() {
        return String.format("Query{type=%s, table=%s, columns=%s, values=%s, condition=%s}",
                type, tableName, columns, values, condition);
    }
}