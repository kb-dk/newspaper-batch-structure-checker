package dk.statsbiblioteket.newspaper;

import dk.statsbiblioteket.medieplatform.autonomous.AbstractRunnableComponent;
import dk.statsbiblioteket.medieplatform.autonomous.Batch;
import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.eventhandlers.EventHandlerFactory;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.eventhandlers.EventRunner;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.eventhandlers.TreeEventHandler;
import dk.statsbiblioteket.newspaper.eventhandlers.BatchStructureEventHandlerFactory;
import dk.statsbiblioteket.newspaper.mfpakintegration.database.MfPakDAO;
import dk.statsbiblioteket.newspaper.schematron.StructureValidator;
import dk.statsbiblioteket.newspaper.schematron.XmlBuilderEventHandler;
import dk.statsbiblioteket.newspaper.xpath.MFpakStructureChecks;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Properties;

/** Checks the directory structure of a batch. This should run both at Ninestars and at SB. */
public class BatchStructureCheckerComponent extends AbstractRunnableComponent {

    public static final String DEMANDS_SCH = "demands.sch";
    private MfPakDAO mfPakDao;

    public BatchStructureCheckerComponent(Properties properties,
                                          MfPakDAO mfPakDao) {
        super(properties);
        this.mfPakDao = mfPakDao;
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
    public void doWorkOnBatch(Batch batch,
                              ResultCollector resultCollector) throws Exception {
        EventHandlerFactory eventHandlerFactory =
                new BatchStructureEventHandlerFactory(getProperties(), resultCollector);
        EventRunner eventRunner = new EventRunner(createIterator(batch));
        final List<TreeEventHandler> eventHandlers = eventHandlerFactory.createEventHandlers();
        eventRunner.runEvents(eventHandlers);
        String xml = null;
        //Need to find handler in the list returned by the EventHandlerFactory was the xml builder. One could imagine
        // refactoring
        //EventHandlerFactory to return a map from classname to EventHandler so that one could simple look it up.
        for (TreeEventHandler handler : eventHandlers) {
            if (handler instanceof XmlBuilderEventHandler) {
                xml = ((XmlBuilderEventHandler) handler).getXml();
            }
        }
        if (xml == null) {
            throw new RuntimeException(
                    "Did not generate xml representation of directory structure. Could not complete tests.");
        }
        storeBatchStructure(batch, new ByteArrayInputStream(xml.getBytes("UTF-8")));
        Validator validator1 = new StructureValidator(DEMANDS_SCH);
        validator1.validate(batch, new ByteArrayInputStream(xml.getBytes("UTF-8")), resultCollector);

        Validator validator2 = new MFpakStructureChecks(mfPakDao);
        validator2.validate(batch, new ByteArrayInputStream(xml.getBytes("UTF-8")), resultCollector);


    }
}
