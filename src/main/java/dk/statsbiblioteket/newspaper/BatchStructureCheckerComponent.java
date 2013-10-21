package dk.statsbiblioteket.newspaper;

import java.util.Properties;

import dk.statsbiblioteket.medieplatform.autonomous.AbstractRunnableComponent;
import dk.statsbiblioteket.medieplatform.autonomous.Batch;
import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
import dk.statsbiblioteket.newspaper.eventhandlers.CompleteCheckFactory;
import dk.statsbiblioteket.newspaper.eventhandlers.EventHandlerFactory;

/**
 * Wraps the BatchStructureChecker as an autonomous component.
 * @author baj
 */
public class BatchStructureCheckerComponent extends AbstractRunnableComponent {
    public BatchStructureCheckerComponent(Properties properties) {
        super(properties);
    }

    @Override
    public String getComponentName() {
        return "BatchStructureCheckerExecutable";
    }

    @Override
    public String getComponentVersion() {
        return "0.1";
    }

    @Override
    public String getEventID() {
        return "Structure_Checked";
    }

    @Override
    public void doWorkOnBatch(Batch batch, ResultCollector resultCollector) throws Exception {
        EventHandlerFactory eventHandlerFactory = new CompleteCheckFactory(getProperties(), batch.getBatchID(), resultCollector);
        new BatchStructureChecker(createIterator(batch)).checkBatchStructure(
                eventHandlerFactory.createEventHandlers());
    }
}
