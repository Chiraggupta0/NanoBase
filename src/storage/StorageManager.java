package storage;

import engine.Table;
import exception.DBException;

import java.io.*;
import java.nio.file.*;
import java.util.*;

/**
 * LAYER 4 — STORAGE ENGINE
 *
 * Handles reading and writing Table objects to disk as .tbl files.
 * Uses Java's built-in serialization — each table is one binary file.
 *
 * File location: data/<tablename>.tbl
 *
 * This simulates how real databases persist data on disk.
 * MySQL uses .ibd files, SQLite uses a single .db file — same concept.
 */
public class StorageManager {

    private final String dataDir;

    public StorageManager(String dataDir) {
        this.dataDir = dataDir;
        // Auto-create data directory if it doesn't exist
        try {
            Files.createDirectories(Paths.get(dataDir));
        } catch (IOException e) {
            throw new DBException(DBException.ErrorType.STORAGE_ERROR,
                    "Could not create data directory: " + dataDir);
        }
    }

    // ── Save ──────────────────────────────────────────────────────────

    /**
     * Serializes a Table object and writes it to disk.
     * File: data/<tablename>.tbl
     */
    public void saveTable(Table table) {
        String path = getPath(table.getName());
        try (ObjectOutputStream oos = new ObjectOutputStream(
                new BufferedOutputStream(new FileOutputStream(path)))) {
            oos.writeObject(table);
        } catch (IOException e) {
            throw new DBException(DBException.ErrorType.STORAGE_ERROR,
                    "Failed to save table '" + table.getName() + "': " + e.getMessage());
        }
    }

    // ── Load ──────────────────────────────────────────────────────────

    /**
     * Reads and deserializes a Table object from disk.
     * Returns null if the table file doesn't exist.
     */
    public Table loadTable(String tableName) {
        String path = getPath(tableName);
        File file = new File(path);

        if (!file.exists()) return null;

        try (ObjectInputStream ois = new ObjectInputStream(
                new BufferedInputStream(new FileInputStream(file)))) {
            return (Table) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new DBException(DBException.ErrorType.STORAGE_ERROR,
                    "Failed to load table '" + tableName + "': " + e.getMessage());
        }
    }

    // ── Delete ────────────────────────────────────────────────────────

    /**
     * Deletes the .tbl file for the given table name.
     */
    public boolean deleteTable(String tableName) {
        File file = new File(getPath(tableName));
        return file.exists() && file.delete();
    }

    // ── List ──────────────────────────────────────────────────────────

    /**
     * Returns all table names currently stored on disk.
     */
    public List<String> listTables() {
        File dir = new File(dataDir);
        List<String> names = new ArrayList<>();
        File[] files = dir.listFiles((d, name) -> name.endsWith(".tbl"));
        if (files != null) {
            for (File f : files) {
                names.add(f.getName().replace(".tbl", ""));
            }
        }
        return names;
    }

    /**
     * Returns true if a .tbl file exists for the given table name.
     */
    public boolean tableExists(String tableName) {
        return new File(getPath(tableName)).exists();
    }

    // ── Helper ────────────────────────────────────────────────────────

    private String getPath(String tableName) {
        return dataDir + File.separator + tableName.toLowerCase() + ".tbl";
    }
}