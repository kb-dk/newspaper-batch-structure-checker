package dk.statsbiblioteket.newspaper;

import java.util.Properties;

import dk.statsbiblioteket.autonomous.ResultCollector;
import dk.statsbiblioteket.autonomous.RunnableComponent;
import dk.statsbiblioteket.doms.iterator.common.TreeIterator;
import dk.statsbiblioteket.newspaper.eventhandlers.CompleteCheckFactory;
import dk.statsbiblioteket.newspaper.eventhandlers.EventHandlerFactory;
import dk.statsbiblioteket.newspaper.processmonitor.datasources.Batch;
import dk.statsbiblioteket.newspaper.processmonitor.datasources.EventID;

/**
 * Wraps the BatchStructureChecker as an autonomous component.
 * @author baj
 */
public class BatchStructureCheckerComponent implements RunnableComponent {
    private Properties properties;
    private BatchStructureChecker batchStructureChecker;
    private EventHandlerFactory eventHandlerFactory;

    public BatchStructureCheckerComponent(Properties properties, TreeIterator iterator) {
        this.properties = properties;
        batchStructureChecker = new BatchStructureChecker(iterator);

    }

    @Override
    public String getComponentName() {
        return "BatchStructureCheckerComponent";
    }

    @Override
    public String getComponentVersion() {
        return "0.1";
    }

    @Override
    public EventID getEventID() {
        return EventID.Structure_Checked;
    }

    @Override
    public void doWorkOnBatch(Batch batch, ResultCollector resultCollector) throws Exception {
        EventHandlerFactory eventHandlerFactory = new CompleteCheckFactory(resultCollector);
        batchStructureChecker.checkBatchStructure(
                eventHandlerFactory.createEventHandlers(), resultCollector);
    }
}
