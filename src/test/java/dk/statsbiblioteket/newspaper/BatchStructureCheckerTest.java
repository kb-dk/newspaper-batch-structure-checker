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

        batchStructureChecker.checkBatchStructure("batch", new ResultCollector());

        assertTrue(true);
    }


}
