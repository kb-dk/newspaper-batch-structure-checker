package dk.statsbiblioteket.newspaper.eventhandlers;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import dk.statsbibliokeket.newspaper.treenode.TreeNodeState;
import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.eventhandlers.EventHandlerFactory;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.eventhandlers.TreeEventHandler;
import dk.statsbiblioteket.newspaper.eventhandlers.sequencechecker.EditionSequenceChecker;
import dk.statsbiblioteket.newspaper.eventhandlers.sequencechecker.FilmSuffixSequenceChecker;
import dk.statsbiblioteket.newspaper.eventhandlers.sequencechecker.PageImageIDSequenceChecker;
import dk.statsbiblioteket.newspaper.eventhandlers.sequencechecker.WorkshiftIsoTargetSequenceChecker;
import dk.statsbiblioteket.newspaper.mfpakintegration.configuration.MfPakConfiguration;
import dk.statsbiblioteket.newspaper.schematron.XmlBuilderEventHandler;

/**
 * Provides the complete set of structure checkers for the batch structure.
 */
public class BatchStructureEventHandlerFactory implements EventHandlerFactory {
    
    
    /** mf-pak Database Url property */
    private final static String MFPAK_DATABASE_URL = "mfpak.postgres.url";
    private final static String MFPAK_DATABASE_USER = "mfpak.postgres.user";
    private final static String MFPAK_DATABASE_PASS = "mfpak.postgres.password";
    private final ResultCollector resultCollector;
    private final MfPakConfiguration mfpakConfig;

    public BatchStructureEventHandlerFactory(Properties properties, ResultCollector resultCollector) {
        this.resultCollector = resultCollector;
        //TODO This mfpak initialisation is expected to be replaced by a BatchContext class elsewhere.
        mfpakConfig = new MfPakConfiguration();
        mfpakConfig.setDatabaseUrl(properties.getProperty(MFPAK_DATABASE_URL));
        mfpakConfig.setDatabaseUser(properties.getProperty(MFPAK_DATABASE_USER));
        mfpakConfig.setDatabasePassword(properties.getProperty(MFPAK_DATABASE_PASS));
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
