package dk.statsbiblioteket.newspaper.eventhandlers.sequencechecker;

import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
import dk.statsbiblioteket.newspaper.treenode.NodeType;
import dk.statsbiblioteket.newspaper.treenode.TreeNodeState;

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
