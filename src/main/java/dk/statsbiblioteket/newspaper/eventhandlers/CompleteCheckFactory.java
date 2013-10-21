package dk.statsbiblioteket.newspaper.eventhandlers;

import java.util.ArrayList;
import java.util.List;

import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
import dk.statsbiblioteket.newspaper.treenode.TreeNodeState;
import dk.statsbiblioteket.newspaper.eventhandlers.filter.LeafFilter;
import dk.statsbiblioteket.newspaper.eventhandlers.filter.LeafType;

/**
 * Provides the complete set of structure checkers.
 */
public class CompleteCheckFactory implements EventHandlerFactory {
    private final ResultCollector resultCollector;

    public CompleteCheckFactory(ResultCollector resultCollector) {
        this.resultCollector = resultCollector;
    }

    @Override
    public List<TreeEventHandler> createEventHandlers() {
        final List<TreeEventHandler> eventHandlers = new ArrayList<>();
        //eventHandlers.add(new ConsoleLogger());
        TreeNodeState nodeState = new TreeNodeState();
        eventHandlers.add(nodeState);
        eventHandlers.add(new ChecksumExistenceChecker(resultCollector));
        eventHandlers.add(new LeafFilter(LeafType.JP2, new SequenceChecker(resultCollector)));
        return eventHandlers;
    }
}
