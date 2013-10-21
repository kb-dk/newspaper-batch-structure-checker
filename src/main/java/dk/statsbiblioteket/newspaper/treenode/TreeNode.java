package dk.statsbiblioteket.newspaper.treenode;

import dk.statsbiblioteket.newspaper.eventhandlers.filter.NodeType;

public class TreeNode {
    private final String name;
    private final NodeType type;
    private final TreeNode parent;

    public TreeNode(String name, NodeType type, TreeNode parent) {
        this.name = name;
        this.type = type;
        this.parent = parent;
    }

    public String getName() {
        return name;
    }

    public NodeType getType() {
        return type;
    }

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
