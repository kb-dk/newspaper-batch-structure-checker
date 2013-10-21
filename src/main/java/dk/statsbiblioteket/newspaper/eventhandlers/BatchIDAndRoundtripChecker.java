package dk.statsbiblioteket.newspaper.eventhandlers;

import dk.statsbiblioteket.medieplatform.autonomous.Batch;
import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.NodeBeginsParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.eventhandlers.DefaultTreeEventHandler;
import dk.statsbiblioteket.newspaper.treenode.NodeType;
import dk.statsbiblioteket.newspaper.treenode.TreeNodeState;

/**
 * This handler checks whether the directory name (node name) for the root event (batch-node) matches the batch-id
 * and round-trip id's of the Batch we think we are processing.
 */
public class BatchIDAndRoundtripChecker extends DefaultTreeEventHandler {

    private Batch batch;
    private TreeNodeState state;
    private ResultCollector resultCollector;

    public BatchIDAndRoundtripChecker(Batch batch, ResultCollector resultCollector, TreeNodeState state) {
        this.batch = batch;
        this.resultCollector = resultCollector;
        this.state = state;
    }

    @Override
    public void handleNodeBegin(NodeBeginsParsingEvent event) {
        if (state.getCurrentNode().getType().equals(NodeType.BATCH)) {
            if (batch.getFullID().equals(event.getName())) {
                //log the success
                return;
            } else {
                resultCollector.addFailure(event.getName(), "filestructure", getClass().getName(), "The event (directory name) " +
                        "did not match the batch id: " + batch.getFullID());
            }
        }
    }
}
