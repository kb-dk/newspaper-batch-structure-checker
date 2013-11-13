package dk.statsbiblioteket.newspaper.eventhandlers.sequencechecker;

import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
import dk.statsbiblioteket.newspaper.treenode.NodeType;
import dk.statsbiblioteket.newspaper.treenode.TreeNodeState;

public class WorkshiftIsoTargetSequenceChecker extends AbstractSequenceChecker {

    public WorkshiftIsoTargetSequenceChecker(ResultCollector resultCollector, TreeNodeState treeNodeState) {
        super(resultCollector,treeNodeState);
    }

    @Override
    protected NodeType getCollectionNodeType() {
        return NodeType.WORKSHIFT_ISO_TARGET;
    }

    @Override
    protected NodeType getNumberingNodeType() {
        return NodeType.WORKSHIFT_TARGET;
    }
}
