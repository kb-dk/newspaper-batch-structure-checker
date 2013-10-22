package dk.statsbiblioteket.newspaper.eventhandlers;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import dk.statsbiblioteket.medieplatform.autonomous.Batch;
import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.eventhandlers.EventHandlerFactory;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.eventhandlers.TreeEventHandler;
import dk.statsbiblioteket.newspaper.mfpakintegration.configuration.MfPakConfiguration;
import dk.statsbiblioteket.newspaper.mfpakintegration.database.MfPakDAO;
import dk.statsbiblioteket.newspaper.treenode.TreeNodeState;

/**
 * Provides the complete set of structure checkers.
 */
public class CompleteCheckFactory implements EventHandlerFactory {
    
    
    /** mf-pak Database Url property */
    private final static String MFPAK_DATABASE_URL = "mfpak.postgres.url";
    private final static String MFPAK_DATABASE_USER = "mfpak.postgres.user";
    private final static String MFPAK_DATABASE_PASS = "mfpak.postgres.password";
    private final ResultCollector resultCollector;
    private final MfPakConfiguration mfpakConfig;
    private final Batch batch;

    public CompleteCheckFactory(Properties properties, Batch batch, ResultCollector resultCollector) {
        this.resultCollector = resultCollector;
        this.batch = batch;
        mfpakConfig = new MfPakConfiguration();
        mfpakConfig.setDatabaseUrl(properties.getProperty(MFPAK_DATABASE_URL));
        mfpakConfig.setDatabaseUser(properties.getProperty(MFPAK_DATABASE_USER));
        mfpakConfig.setDatabasePassword(properties.getProperty(MFPAK_DATABASE_PASS));
    }

    @Override
    public List<TreeEventHandler> createEventHandlers() {
        final List<TreeEventHandler> eventHandlers = new ArrayList<>();
        MfPakDAO mfpak = new MfPakDAO(mfpakConfig);
        String newspaperID;
        try {
            newspaperID = mfpak.getNewspaperID(batch.getBatchID());
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to get newspaperID from mfpak database", e);
        }
        //eventHandlers.add(new NewspaperIDChecker(newspaperID, resultCollector));
        //eventHandlers.add(new ConsoleLogger());
        TreeNodeState nodeState = new TreeNodeState();
        eventHandlers.add(nodeState); // Must be the first eventhandler to ensure a update state used by the following handlers (a bit fragile).
        eventHandlers.add(new ChecksumExistenceChecker(resultCollector));
        eventHandlers.add(new BilledIDSequenceChecker(resultCollector, nodeState));
        eventHandlers.add(new BatchIDAndRoundtripChecker(batch, resultCollector, nodeState));        
        return eventHandlers;
    }
}
