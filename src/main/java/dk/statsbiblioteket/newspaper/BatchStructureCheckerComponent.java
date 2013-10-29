package dk.statsbiblioteket.newspaper;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Properties;

import dk.statsbiblioteket.medieplatform.autonomous.AbstractRunnableComponent;
import dk.statsbiblioteket.medieplatform.autonomous.Batch;
import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.eventhandlers.EventHandlerFactory;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.eventhandlers.EventRunner;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.eventhandlers.TreeEventHandler;
import dk.statsbiblioteket.newspaper.eventhandlers.CompleteCheckFactory;
import dk.statsbiblioteket.newspaper.schematron.StructureValidator;
import dk.statsbiblioteket.newspaper.schematron.XmlBuilderEventHandler;

/**
 * Checks the directory structure of a batch. This should run both at Ninestars and at SB.
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
        EventHandlerFactory eventHandlerFactory = new CompleteCheckFactory(getProperties(), batch, resultCollector);
        EventRunner eventRunner = new EventRunner(createIterator(batch));
        final List<TreeEventHandler> eventHandlers = eventHandlerFactory.createEventHandlers();
        eventRunner.runEvents(eventHandlers);
        String xml = null;
        for (TreeEventHandler handler: eventHandlers) {
            if (handler instanceof XmlBuilderEventHandler) {
                xml = ((XmlBuilderEventHandler) handler).getXml();
            }
        }
        if (xml == null) {
            throw new RuntimeException("Did not generate xml representation of directory structure. Could not complete tests.");
        }
        StructureValidator validator = new StructureValidator("demands.sch");
        validator.validate(batch, new ByteArrayInputStream(xml.getBytes("UTF-8")), resultCollector);
    }
}
