package dk.statsbiblioteket.newspaper.eventhandlers;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import dk.statsbiblioteket.newspaper.treenode.TreeNodeState;
import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.eventhandlers.EventHandlerFactory;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.eventhandlers.TreeEventHandler;
import dk.statsbiblioteket.newspaper.eventhandlers.sequencechecker.EditionSequenceChecker;
import dk.statsbiblioteket.newspaper.eventhandlers.sequencechecker.FilmSuffixSequenceChecker;
import dk.statsbiblioteket.newspaper.eventhandlers.sequencechecker.PageImageIDSequenceChecker;
import dk.statsbiblioteket.newspaper.eventhandlers.sequencechecker.WorkshiftIsoTargetSequenceChecker;
import dk.statsbiblioteket.newspaper.schematron.XmlBuilderEventHandler;

/**
 * Provides the complete set of structure checkers for the batch structure.
 */
public class BatchStructureEventHandlerFactory implements EventHandlerFactory {

    private final ResultCollector resultCollector;

    public BatchStructureEventHandlerFactory(Properties properties, ResultCollector resultCollector) {
        this.resultCollector = resultCollector;
    }

    @Override
    public List<TreeEventHandler> createEventHandlers() {
        final List<TreeEventHandler> eventHandlers = new ArrayList<>();
        TreeNodeState nodeState = new TreeNodeState();
        eventHandlers.add(nodeState); // Must be the first eventhandler to ensure a update state used by the following handlers (a bit fragile).
        eventHandlers.add(new PageImageIDSequenceChecker(resultCollector, nodeState));
        eventHandlers.add(new WorkshiftIsoTargetSequenceChecker(resultCollector, nodeState));
        eventHandlers.add(new EditionSequenceChecker(resultCollector, nodeState));
        eventHandlers.add(new FilmSuffixSequenceChecker(resultCollector, nodeState));
        eventHandlers.add(new XmlBuilderEventHandler());
        return eventHandlers;
    }
}
