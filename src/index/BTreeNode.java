package index;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * A single node in the B-Tree.
 *
 * Each node holds:
 *  - keys:     the primary key values (Strings)
 *  - values:   the row index (int) corresponding to each key
 *  - children: child BTreeNode pointers (empty for leaf nodes)
 *  - isLeaf:   true if this node has no children
 *
 * For an order-t tree:
 *  - max keys per node: 2t - 1
 *  - min keys per node (non-root): t - 1
 *  - max children per node: 2t
 */
public class BTreeNode implements Serializable {

    private static final long serialVersionUID = 1L;

    List<String>    keys;
    List<Integer>   values;   // row indices parallel to keys
    List<BTreeNode> children;
    boolean         isLeaf;
    int             keyCount;

    public BTreeNode(int order, boolean isLeaf) {
        this.isLeaf   = isLeaf;
        this.keyCount = 0;
        this.keys     = new ArrayList<>(2 * order - 1);
        this.values   = new ArrayList<>(2 * order - 1);
        this.children = new ArrayList<>(2 * order);
    }

    /**
     * Returns true if this node has reached its maximum capacity.
     */
    public boolean isFull(int order) {
        return keyCount == 2 * order - 1;
    }

    @Override
    public String toString() {
        return "BTreeNode{keys=" + keys.subList(0, keyCount) + ", isLeaf=" + isLeaf + "}";
    }
}