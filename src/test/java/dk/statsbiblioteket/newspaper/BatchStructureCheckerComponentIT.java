package dk.statsbiblioteket.newspaper;

import java.io.FileInputStream;
import java.util.Properties;

import dk.statsbiblioteket.medieplatform.autonomous.Batch;
import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
import dk.statsbiblioteket.newspaper.eventhandlers.Util;
import org.testng.annotations.Test;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 */
public class BatchStructureCheckerComponentIT {

    private final static String TEST_BATCH_ID = "400022028241";

    /**
     * Tests that the BatchStructureChecker can parse a production like batch which should contain failures.
     */
    @Test(groups = "integrationTest")
    public void testGoodBatchStructureCheck() throws Exception {
        String pathToProperties = System.getProperty("integration.test.newspaper.properties");
        String pathToTestBatch = System.getProperty("integration.test.newspaper.testdata");
        Properties properties = new Properties();
        properties.load(new FileInputStream(pathToProperties));
        properties.setProperty("scratch", pathToTestBatch + "/" + "small-test-batch");

        BatchStructureCheckerComponent batchStructureCheckerComponent =
                new BatchStructureCheckerComponent(properties);

        ResultCollector resultCollector = new ResultCollector("Batch Structure Checker", "v0.1");
        Batch batch = new Batch();
        batch.setBatchID(TEST_BATCH_ID);
        batch.setRoundTripNumber(1);

        batchStructureCheckerComponent.doWorkOnBatch(batch, resultCollector);
        assertTrue(resultCollector.isSuccess(), "Found failure with run on good batch");
    }



    /**
     * Tests that the BatchStructureChecker can parse a production like batch which should contain failures
     * for all .
     */
    @Test(groups = "integrationTest")
    public void testBadBatchStructureCheck() throws Exception {
        String pathToProperties = System.getProperty("integration.test.newspaper.properties");
        String pathToTestBatch = System.getProperty("integration.test.newspaper.testdata");
        Properties properties = new Properties();
        properties.load(new FileInputStream(pathToProperties));
        properties.setProperty("scratch", pathToTestBatch + "/" + "bad-bad-batch");

        BatchStructureCheckerComponent batchStructureCheckerComponent =
                new BatchStructureCheckerComponent(properties);

        ResultCollector resultCollector = new ResultCollector("Batch Structure Checker", "v0.1");
        Batch batch = new Batch();
        batch.setBatchID(TEST_BATCH_ID);
        batch.setRoundTripNumber(1);

        batchStructureCheckerComponent.doWorkOnBatch(batch, resultCollector);
        assertFalse(resultCollector.isSuccess());
        System.out.println("Found " + Util.countFailures(resultCollector) + " failures.");
    }
}
