package index;

import java.io.Serializable;

/**
 * CORE DATA STRUCTURE — B-TREE INDEX
 *
 * A self-balancing tree used to index the primary key of each table.
 * Provides O(log n) search, insert operations.
 *
 * Why B-Tree (not BST or HashMap)?
 *  - B-Tree stays balanced automatically — no worst-case O(n) like BST
 *  - Better cache locality than a binary tree
 *  - This is exactly what MySQL's InnoDB uses internally
 *
 * Order-t B-Tree properties:
 *  - Every node has at most 2t-1 keys
 *  - Every non-root node has at least t-1 keys
 *  - All leaves are at the same depth
 *  - A full node is split during insertion (proactive split)
 */
public class BTree implements Serializable {

    private static final long serialVersionUID = 1L;

    private BTreeNode root;
    private final int t; // minimum degree (order)

    public BTree(int t) {
        this.t    = t;
        this.root = new BTreeNode(t, true);
    }

    // ── Search ────────────────────────────────────────────────────────

    /**
     * Search for a key. Returns the row index, or -1 if not found.
     * Time complexity: O(log n)
     */
    public int search(String key) {
        return searchNode(root, key);
    }

    private int searchNode(BTreeNode node, String key) {
        int i = 0;
        // Find the first key >= search key
        while (i < node.keyCount && compareTo(key, node.keys.get(i)) > 0) {
            i++;
        }

        // Key found at position i
        if (i < node.keyCount && compareTo(key, node.keys.get(i)) == 0) {
            return node.values.get(i);
        }

        // Key not in this node — search appropriate child
        if (node.isLeaf) return -1;
        return searchNode(node.children.get(i), key);
    }

    // ── Insert ────────────────────────────────────────────────────────

    /**
     * Insert a key-value pair (primary key → row index).
     * Uses proactive splitting: splits full nodes on the way down.
     */
    public void insert(String key, int rowIndex) {
        BTreeNode r = root;

        if (r.isFull(t)) {
            // Root is full — create new root, split old root
            BTreeNode newRoot = new BTreeNode(t, false);
            newRoot.children.add(root);
            newRoot.keyCount = 0;
            splitChild(newRoot, 0, root);
            root = newRoot;
        }

        insertNonFull(root, key, rowIndex);
    }

    /**
     * Insert into a node that is guaranteed to be non-full.
     */
    private void insertNonFull(BTreeNode node, String key, int rowIndex) {
        int i = node.keyCount - 1;

        if (node.isLeaf) {
            // Shift keys right to make space, then insert
            node.keys.add(null);
            node.values.add(0);

            while (i >= 0 && compareTo(key, node.keys.get(i)) < 0) {
                node.keys.set(i + 1, node.keys.get(i));
                node.values.set(i + 1, node.values.get(i));
                i--;
            }
            node.keys.set(i + 1, key);
            node.values.set(i + 1, rowIndex);
            node.keyCount++;

        } else {
            // Find correct child to descend into
            while (i >= 0 && compareTo(key, node.keys.get(i)) < 0) {
                i--;
            }
            i++;

            // Split child if full
            if (node.children.get(i).isFull(t)) {
                splitChild(node, i, node.children.get(i));
                if (compareTo(key, node.keys.get(i)) > 0) {
                    i++;
                }
            }
            insertNonFull(node.children.get(i), key, rowIndex);
        }
    }

    /**
     * Splits the i-th child of parent (which must be full).
     * The median key of the child is promoted to the parent.
     */
    private void splitChild(BTreeNode parent, int i, BTreeNode child) {
        BTreeNode sibling = new BTreeNode(t, child.isLeaf);
        sibling.keyCount = t - 1;

        // Copy right half of child's keys to sibling
        for (int j = 0; j < t - 1; j++) {
            sibling.keys.add(child.keys.get(j + t));
            sibling.values.add(child.values.get(j + t));
        }

        // Copy right half of child's children to sibling (if not leaf)
        if (!child.isLeaf) {
            for (int j = 0; j < t; j++) {
                sibling.children.add(child.children.get(j + t));
            }
        }

        // Median key goes up to parent
        String medianKey = child.keys.get(t - 1);
        int    medianVal = child.values.get(t - 1);

        // Shrink child
        child.keyCount = t - 1;
        while (child.keys.size() > t - 1)    child.keys.remove(child.keys.size() - 1);
        while (child.values.size() > t - 1)  child.values.remove(child.values.size() - 1);
        if (!child.isLeaf) {
            while (child.children.size() > t) child.children.remove(child.children.size() - 1);
        }

        // Insert median into parent
        parent.keys.add(i, medianKey);
        parent.values.add(i, medianVal);
        parent.children.add(i + 1, sibling);
        parent.keyCount++;
    }

    // ── Utility ───────────────────────────────────────────────────────

    /**
     * Compares two keys. Tries numeric comparison first, falls back to string.
     */
    private int compareTo(String a, String b) {
        try {
            return Double.compare(Double.parseDouble(a), Double.parseDouble(b));
        } catch (NumberFormatException e) {
            return a.compareToIgnoreCase(b);
        }
    }

    /**
     * Copies all data from another BTree into this one (used after deletion rebuild).
     */
    public void copyFrom(BTree other) {
        this.root = other.root;
    }

    /**
     * Prints the B-Tree level by level (useful for debugging).
     */
    public void printTree() {
        printNode(root, 0);
    }

    private void printNode(BTreeNode node, int level) {
        StringBuilder sb = new StringBuilder("  ".repeat(level));
        sb.append("Level ").append(level).append(": ");
        for (int i = 0; i < node.keyCount; i++) {
            sb.append("[").append(node.keys.get(i)).append("→").append(node.values.get(i)).append("] ");
        }
        System.out.println(sb);
        if (!node.isLeaf) {
            for (int i = 0; i <= node.keyCount; i++) {
                if (i < node.children.size()) {
                    printNode(node.children.get(i), level + 1);
                }
            }
        }
    }
}