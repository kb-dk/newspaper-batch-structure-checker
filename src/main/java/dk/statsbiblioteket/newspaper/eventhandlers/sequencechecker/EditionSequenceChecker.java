package dk.statsbiblioteket.newspaper.eventhandlers.sequencechecker;

import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
import dk.statsbiblioteket.newspaper.treenode.NodeType;
import dk.statsbiblioteket.newspaper.treenode.TreeNodeState;

public class EditionSequenceChecker extends AbstractSequenceChecker {

    public EditionSequenceChecker(ResultCollector resultCollector, TreeNodeState treeNodeState) {
        super(resultCollector, treeNodeState);
    }

    @Override
    protected NodeType getCollectionNodeType() {
        return NodeType.FILM;
    }

    @Override
    protected NodeType getNumberingNodeType() {
        return NodeType.EDITION;
    }

    /**
     * Defines numbering subsets for individual dates.
     * @param eventname
     * @return
     */
    @Override
    protected String getSubsetID(String eventname) {
        return eventname.substring(0, eventname.lastIndexOf('-'));
    }
}
