package dk.statsbiblioteket.newspaper.eventhandlers.filter;

import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.AttributeParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.NodeBeginsParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.NodeEndParsingEvent;
import dk.statsbiblioteket.newspaper.treenode.TreeNode;
import dk.statsbiblioteket.newspaper.eventhandlers.TreeEventHandler;

/**
 * May be used to filter the attribute events according to certain node types.
 */
public abstract class NodeFilter implements TreeEventHandler {
    private final TreeEventHandler leafHandler;
    protected TreeNode currentNode = null;

    public NodeFilter(TreeEventHandler leafHandler) {
        this.leafHandler = leafHandler;
    }

    @Override
    public void handleNodeBegin(NodeBeginsParsingEvent event) {
        updateCurrentNode(event);
        leafHandler.handleNodeBegin(event);
    }

    @Override
    public void handleNodeEnd(NodeEndParsingEvent event) {
            leafHandler.handleNodeEnd(event);
    }

    @Override
    public void handleAttribute(AttributeParsingEvent event) {
        if (!shouldIgnoreNode()) {
            leafHandler.handleAttribute(event);
        }
    }

    @Override
    public void handleFinish() {
        leafHandler.handleFinish();
    }

    private void updateCurrentNode(NodeBeginsParsingEvent event) {
        NodeType nextNodeType = null;
        if (currentNode == null) {
            nextNodeType = NodeType.BATCH;
        } else if (currentNode.getType().equals(NodeType.BATCH)) {
            if (event.getName().endsWith("WORKSHIFT-ISO-TARGET")) {
            } else {
                nextNodeType = NodeType.FILMID;
            }
        } else if (currentNode.getType().equals(NodeType.FILMID)) {
            if (event.getName().endsWith("FILM-ISO-TARGET")) {
                nextNodeType = NodeType.FILMID;
            } else if (event.getName().endsWith("UNMATCHED")) {
                nextNodeType = NodeType.UNMATCHED;
            } else {
                nextNodeType = NodeType.UDGAVE;
            }
        } else {
            throw new IllegalStateException("Unexpected event: " + event + " for curent node: " + currentNode);
        }
        assert (nextNodeType != null);
        currentNode = new TreeNode(event.getName(), nextNodeType, currentNode);
    }

    /**
     * Implemented by concrete node filter to indicated whether the indicated node should be ignored.
     * @return <code>true</code> if this node should be ignored, else false.
     */
    public abstract boolean shouldIgnoreNode();
}
