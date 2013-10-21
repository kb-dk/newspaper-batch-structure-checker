package dk.statsbiblioteket.newspaper.treenode;

/**
 * Represents a node in a tree including parent structure.
 */
public class TreeNode {
    private final String name;
    private final NodeType type;
    private final TreeNode parent;

    public TreeNode(String name, NodeType type, TreeNode parent) {
        this.name = name;
        this.type = type;
        this.parent = parent;
    }

    /**
     * The name of the node.
     */
    public String getName() {
        return name;
    }

    /**
     * The type as defined in the NodeType shortlist.
     */
    public NodeType getType() {
        return type;
    }

    /**
     * The parent node. Will always be non null, except for batch nodes.
     */
    public TreeNode getParent() {
        return parent;
    }

    @Override
    public String toString() {
        return "TreeNode{" +
                "name='" + name + '\'' +
                ", type=" + type +
                ", parent=" + parent +
                '}';
    }
}
