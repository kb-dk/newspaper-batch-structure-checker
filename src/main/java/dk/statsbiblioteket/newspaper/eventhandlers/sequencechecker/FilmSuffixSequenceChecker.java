package dk.statsbiblioteket.newspaper.eventhandlers.sequencechecker;

import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
import dk.statsbiblioteket.newspaper.treenode.NodeType;
import dk.statsbiblioteket.newspaper.treenode.TreeNodeState;
/**
 * Takes care of checking the film numbering is in sequence without any holes. See
 * https://sbforge.org/display/NEWSPAPER/Structure+checks+done 2F-Q4.
 */
public class FilmSuffixSequenceChecker extends AbstractSequenceChecker {

    public FilmSuffixSequenceChecker(ResultCollector resultCollector, TreeNodeState treeNodeState) {
        super(resultCollector, treeNodeState);
    }

    @Override
    protected NodeType getCollectionNodeType() {
        return NodeType.BATCH;
    }

    @Override
    protected NodeType getNumberingNodeType() {
        return NodeType.FILM;
    }
}
