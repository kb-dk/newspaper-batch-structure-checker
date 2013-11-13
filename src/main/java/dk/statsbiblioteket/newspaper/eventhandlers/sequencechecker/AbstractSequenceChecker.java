package dk.statsbiblioteket.newspaper.eventhandlers.sequencechecker;

import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.NodeBeginsParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.NodeEndParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.eventhandlers.DefaultTreeEventHandler;
import dk.statsbiblioteket.newspaper.treenode.NodeType;
import dk.statsbiblioteket.newspaper.treenode.TreeNodeState;

/**
 * Implements the generic functionality for checking sequence numbers by collecting sequence numbers for a relevant
 * node and checking the sequence is complete without holes after all numbers have been collected for a
 * node.
 */
public abstract class AbstractSequenceChecker extends DefaultTreeEventHandler {
    private SequenceNumberChecker sequenceChecker;
    private final TreeNodeState treeNodeState;
    private final ResultCollector resultCollector;

    public AbstractSequenceChecker(ResultCollector resultCollector, TreeNodeState treeNodeState) {
        this.resultCollector = resultCollector;
        this.treeNodeState = treeNodeState;
    }

    @Override
    public void handleNodeBegin(NodeBeginsParsingEvent event) {
        if (treeNodeState.getCurrentNode().getType().equals(getCollectionNodeType())) {
            sequenceChecker = createSequenceNumberChecker(resultCollector);
        } else if (treeNodeState.getCurrentNode().getType().equals(getNumberingNodeType())) {
            addNumber(treeNodeState.getCurrentNode().getName());
        }
    }

    @Override
    public void handleNodeEnd(NodeEndParsingEvent event) {
        if (treeNodeState.getPreviousNode().getType().equals(getCollectionNodeType())) {
            sequenceChecker.verifySequence();
        }
    }

    /**
     * Used for adding a numbering event to the set of numbers to check. May be overridden
     * by subclasses needing more specialized behaviour.
     * @param eventName
     */
    protected void addNumber(String eventName) {
        int numberBeginIndex = eventName.lastIndexOf('-')+1;
        sequenceChecker.addNumber(
                Integer.parseInt(eventName.substring(numberBeginIndex)),
                eventName);
    }

    /**
     * Creates the <code>SequenceNumberChecker</code> to use. May be overridden with more specialized
     * checkers
     * @param resultCollector
     * @return
     */
    protected SequenceNumberChecker createSequenceNumberChecker(ResultCollector resultCollector) {
        return new SequenceNumberChecker(resultCollector);
    }

    /**
     * Enables the concrete class to defined the type of node to check sequence numbers for.
     */
    protected abstract NodeType getCollectionNodeType();

    /**
     * Enables the concrete class to defined the type of node to collect a sequence numbers.
     */
    protected abstract NodeType getNumberingNodeType();
}
