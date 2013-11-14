package dk.statsbiblioteket.newspaper.eventhandlers.sequencechecker;

import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
import dk.statsbiblioteket.newspaper.treenode.NodeType;
import dk.statsbiblioteket.newspaper.treenode.TreeNodeState;

/**
 * Takes care of checking the film iso numbering is in sequence. See
 * https://sbforge.org/display/NEWSPAPER/Structure+checks+done 2F-Q5
 */
public class FilmIsoTargetSequenceChecker extends AbstractSequenceChecker {

    public FilmIsoTargetSequenceChecker(ResultCollector resultCollector, TreeNodeState treeNodeState) {
        super(resultCollector, treeNodeState);
    }

    @Override
    protected NodeType getCollectionNodeType() {
        return NodeType.FILM_ISO_TARGET;
    }

    @Override
    protected NodeType getNumberingNodeType() {
        return NodeType.FILM_TARGET;
    }
}
