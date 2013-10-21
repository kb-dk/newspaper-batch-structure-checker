package dk.statsbiblioteket.newspaper;

import java.util.Properties;

import dk.statsbiblioteket.medieplatform.autonomous.AbstractRunnableComponent;
import dk.statsbiblioteket.medieplatform.autonomous.Batch;
import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.eventhandlers.EventHandlerFactory;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.eventhandlers.EventRunner;
import dk.statsbiblioteket.newspaper.eventhandlers.CompleteCheckFactory;

/**
 * Checks the directory structure of a batch. This should run both at Ninestars and at SB.
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
    /**
     * Check the batch-structure tree received for errors. (I.e. we are gonna check the received tree for
     * errors. The tree received represents a batch structure, which is the structure of a batch).
     *
     * @throws IOException
     */
    public void doWorkOnBatch(Batch batch, ResultCollector resultCollector) throws Exception {
        EventHandlerFactory eventHandlerFactory = new CompleteCheckFactory(getProperties(), batch.getBatchID(), resultCollector);
        new EventRunner(createIterator(batch)).runEvents(eventHandlerFactory.createEventHandlers());
    }
}
