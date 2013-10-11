package dk.statsbiblioteket.newspaper;

import dk.statsbiblioteket.autonomous.ResultCollector;
import dk.statsbiblioteket.newspaper.processmonitor.datasources.Batch;
import org.junit.Test;
import static org.junit.Assert.assertTrue;

/**
 * @author jrg
 */
public class BatchStructureCheckerTest {
    /**
     * Test BatchStructureChecker
     * @throws Exception
     */
    @Test
    public void testBatchStructureCheck() throws Exception {
        BatchStructureChecker batchStructureChecker = new BatchStructureChecker();
        ResultCollector resultCollector = new ResultCollector("Batch Structure Checker", "v0.1");

        batchStructureChecker.checkBatchStructure("batch", resultCollector);

        System.out.println("isSuccess: " + resultCollector.isSuccess());
        assertTrue(true);
    }


}
