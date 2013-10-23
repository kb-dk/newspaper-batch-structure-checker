package dk.statsbiblioteket.newspaper.eventhandlers;

import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.AttributeParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.NodeBeginsParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.NodeEndParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.eventhandlers.DefaultTreeEventHandler;
import dk.statsbiblioteket.newspaper.treenode.NodeType;
import dk.statsbiblioteket.newspaper.treenode.TreeNodeState;

import java.util.*;

/**
 * A generic checker for a given node-level in the tree structure. This class just gathers up the names of all
 * child nodes and attributes at the given node level.
 */
public abstract class AbstractNodeChecker extends DefaultTreeEventHandler {

    String name;
    List<String> childNodes;
    List<String> attributes;
    private Deque<String> nodeStack;

    /**
     * Do the checks for this node level.
     */
    public abstract void doCheck();

    /**
     * Return the node type this concrete instance is intended to check.
     * @return the node type.
     */
    public abstract NodeType getNodeType();

    /**
     * Gets the current tree node state, presumably passed in by injection in a constructor or method.
     * @return
     */
    public abstract TreeNodeState getCurrentState();

    /**
     * @param event
     */
    @Override
    public void handleNodeBegin(NodeBeginsParsingEvent event) {
        if (getCurrentState().getCurrentNode().getType().equals(getNodeType())) {
            childNodes = new ArrayList<String>();
            attributes = new ArrayList<String>();
            nodeStack = new ArrayDeque<String>();
            name = event.getName();
            nodeStack.addFirst(name);
        } else if (nodeStack == null || nodeStack.isEmpty()) {
            //We haven't reached the relevant node so just ignore this node
        } else {  //at a sub-node
            if (nodeStack.size() == 1) {   //at a child-node
                childNodes.add(event.getName());
            }
            nodeStack.addFirst(event.getName());
        }
    }

    @Override
    public void handleNodeEnd(NodeEndParsingEvent event) {
        if (nodeStack == null) {
            return;
        } else {
            nodeStack.removeFirst();
            if (nodeStack.isEmpty()) {
                nodeStack = null;
                doCheck();
                name = null;
                attributes = null;
                childNodes = null;
            }
        }
    }

    @Override
    public void handleAttribute(AttributeParsingEvent event) {
        if (nodeStack != null &&  nodeStack.size() == 1) {
            attributes.add(event.getName());
        }
    }

    @Override
    public void handleFinish() {
        super.handleFinish();    //To change body of overridden methods use File | Settings | File Templates.
    }
}
