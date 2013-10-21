package dk.statsbiblioteket.newspaper.treenode;

import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.NodeBeginsParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.NodeEndParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.eventhandlers.DefaultTreeEventHandler;

/**
 * Provides functionality for accessing the current state for the current node in the batch structure.
 */
public class TreeNodeState extends DefaultTreeEventHandler {
    private TreeNode currentNode = null;

    public TreeNode getCurrentNode() {
        return currentNode;
    }

    @Override
    public void handleNodeBegin(NodeBeginsParsingEvent event) {
        updateCurrentNode(event);
    }

    @Override
    public void handleNodeEnd(NodeEndParsingEvent event) {
        currentNode = currentNode.getParent();
    }

    private void updateCurrentNode(NodeBeginsParsingEvent event) {
        NodeType nextNodeType = null;
        if (currentNode == null) {
            nextNodeType = NodeType.BATCH;
        } else if (currentNode.getType().equals(NodeType.BATCH)) {
            if (event.getName().endsWith("WORKSHIFT-ISO-TARGET")) {
                nextNodeType = NodeType.WORKSHIFT_ISO_TARGET;
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
            throw new IllegalStateException("Unexpected event: " + event + " for current node: " + currentNode);
        }
        assert (nextNodeType != null);
        currentNode = new TreeNode(event.getName(), nextNodeType, currentNode);
    }
}
