package dk.statsbiblioteket.newspaper.eventhandlers.sequencechecker;

import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
import dk.statsbiblioteket.newspaper.treenode.NodeType;
import dk.statsbiblioteket.newspaper.treenode.TreeNodeState;

/**
 * Takes care of checking the film edition numbering for a given date is in sequence without any holes. See
 * https://sbforge.org/display/NEWSPAPER/Structure+checks+done 2F-Q6.
 */
public class EditionSequenceChecker extends AbstractSequenceChecker {

    public EditionSequenceChecker(ResultCollector resultCollector, TreeNodeState treeNodeState) {
        super(resultCollector, treeNodeState, "2F-Q5: ");
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
