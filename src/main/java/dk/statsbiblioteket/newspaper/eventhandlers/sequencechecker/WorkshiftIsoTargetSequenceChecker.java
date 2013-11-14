package dk.statsbiblioteket.newspaper.eventhandlers.sequencechecker;

import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
import dk.statsbiblioteket.newspaper.treenode.NodeType;
import dk.statsbiblioteket.newspaper.treenode.TreeNodeState;
/**
 * Takes care of checking the Workshift iso target for a given workshift is in sequence without any holes
 * and starting with 1  . See
 * https://sbforge.org/display/NEWSPAPER/Structure+checks+done 2F-Q3.
 */
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

    /**
     * Defines numbering subsets for individual workshifts.
     */
    @Override
    protected String getSubsetID(String eventname) {
        return eventname.substring(0, eventname.lastIndexOf('-'));
    }
}
