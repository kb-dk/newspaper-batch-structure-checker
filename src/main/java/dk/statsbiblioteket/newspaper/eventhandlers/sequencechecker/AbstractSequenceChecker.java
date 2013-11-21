package dk.statsbiblioteket.newspaper.eventhandlers.sequencechecker;

import java.util.HashMap;
import java.util.Map;

import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.NodeBeginsParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.NodeEndParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.eventhandlers.DefaultTreeEventHandler;
import dk.statsbiblioteket.newspaper.treenode.NodeType;
import dk.statsbiblioteket.newspaper.treenode.TreeNodeState;

/**
 * Implements the generic functionality for checking sequence numbers by collecting sequence numbers for a relevant
 * node and checking the sequence is complete without holes after all numbers have been collected for a
 * node. The numbering model and verification is delegated to a <code>SequenceNumberingModel</code>.<p>
 * The default behaviour may be customised by overriding the protedted methods.
 * </p>
 *
 *
 */
public abstract class AbstractSequenceChecker extends DefaultTreeEventHandler {
    private Map<String,SequenceNumberingModel> sequenceCheckerMap;
    private final TreeNodeState treeNodeState;
    private final ResultCollector resultCollector;
    private final String descriptionPrefix;

    public AbstractSequenceChecker(ResultCollector resultCollector, TreeNodeState treeNodeState,
                                   String descriptionPrefix) {
        this.resultCollector = resultCollector;
        this.treeNodeState = treeNodeState;
        this.descriptionPrefix = descriptionPrefix;
    }

    @Override
    public void handleNodeBegin(NodeBeginsParsingEvent event) {
        if (treeNodeState.getCurrentNode().getType().equals(getCollectionNodeType())) {
            sequenceCheckerMap = new HashMap<>();
        } else if (treeNodeState.getCurrentNode().getType().equals(getNumberingNodeType())) {
            addNumber(treeNodeState.getCurrentNode().getName());
        }
    }

    @Override
    public void handleNodeEnd(NodeEndParsingEvent event) {
        if (treeNodeState.getPreviousNode().getType().equals(getCollectionNodeType())) {
            for (SequenceNumberingModel sequenceNumberingModel : sequenceCheckerMap.values()) {
                sequenceNumberingModel.verifySequence();
            }
        }
    }

    /**
     * Used for adding a numbering event to the set of numbers to check. May be overridden
     * by subclasses needing more specialized behaviour.
     * @param eventName
     */
    protected void addNumber(String eventName) {
        int numberBeginIndex = eventName.lastIndexOf('-')+1;
        String subsetID = getSubsetID(eventName);
        if (!sequenceCheckerMap.containsKey(subsetID)) {
            sequenceCheckerMap.put(subsetID, createSequenceNumberChecker(resultCollector, descriptionPrefix));
        }
        SequenceNumberingModel sequenceNumberingModel = sequenceCheckerMap.get(subsetID);
        sequenceNumberingModel.addNumber(
                Integer.parseInt(eventName.substring(numberBeginIndex)),
                eventName);
    }

    /**
     * Creates the <code>SequenceNumberingModel</code> to use. May be overridden with more specialized
     * checkers
     *
     * @param resultCollector
     * @param descriptionPrefix
     * @return
     */
    protected SequenceNumberingModel createSequenceNumberChecker(ResultCollector resultCollector,
                                                                 String descriptionPrefix) {
        return new SequenceNumberingModel(resultCollector, descriptionPrefix);
    }

    /**
     * May be used by subclasses to differentiate between sequences in subsets of the node. The default implementation
     * is to put all numbers in the same set.
     * @param eventname
     * @return
     */
    protected String getSubsetID(String eventname) {
        return "Default set";
    }

    /**
     * Enables the concrete class to define the type of node to check sequence numbers for.
     */
    protected abstract NodeType getCollectionNodeType();

    /**
     * Enables the concrete class to define the type of node to collect a sequence numbers.
     */
    protected abstract NodeType getNumberingNodeType();
}
