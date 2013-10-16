package dk.statsbiblioteket.newspaper.eventhandlers;

import java.util.ArrayList;
import java.util.List;

import dk.statsbiblioteket.autonomous.ResultCollector;

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
        eventHandlers.add(new ConsoleLogger());
        eventHandlers.add(new ChecksumExistenceChecker(resultCollector));
        return eventHandlers;
    }
}
