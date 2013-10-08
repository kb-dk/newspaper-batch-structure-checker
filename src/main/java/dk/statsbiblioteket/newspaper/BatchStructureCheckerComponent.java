package dk.statsbiblioteket.newspaper;

import dk.statsbiblioteket.autonomous.ResultCollector;
import dk.statsbiblioteket.autonomous.RunnableComponent;
import dk.statsbiblioteket.newspaper.processmonitor.datasources.Batch;
import dk.statsbiblioteket.newspaper.processmonitor.datasources.EventID;

import java.util.Properties;

/**
 * Wraps the BatchStructureChecker as an autonomous component.
 * @author baj@statsbiblioteket.dk
 * Date: 10/8/13
 * Time: 10:08 AM
 */
public class BatchStructureCheckerComponent implements RunnableComponent {
    private Properties properties;
    private BatchStructureChecker batchStructureChecker;

    public BatchStructureCheckerComponent(Properties properties) {
        this.properties = properties;
        batchStructureChecker = new BatchStructureChecker();
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
        return null; //TODO EventID.Structure_Checked;
    }

    @Override
    public void doWorkOnBatch(Batch batch, ResultCollector resultCollector) throws Exception {
        batchStructureChecker.checkBatchStructure(batch.toString(), resultCollector);
    }
}
