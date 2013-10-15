package dk.statsbiblioteket.newspaper;

import dk.statsbiblioteket.autonomous.ResultCollector;
import dk.statsbiblioteket.doms.iterator.common.NodeBeginsParsingEvent;
import dk.statsbiblioteket.doms.iterator.common.ParsingEvent;
import dk.statsbiblioteket.doms.iterator.common.TreeIterator;
import dk.statsbiblioteket.doms.iterator.filesystem.transforming.TransformingIteratorForFileSystems;
import dk.statsbiblioteket.newspaper.processmonitor.datasources.Batch;
import org.junit.Test;

import java.io.File;
import java.net.URISyntaxException;

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
        TreeIterator iterator = getIterator("batch");
        BatchStructureChecker batchStructureChecker = new BatchStructureChecker(iterator);
        ResultCollector resultCollector = new ResultCollector("Batch Structure Checker", "v0.1");

        batchStructureChecker.checkBatchStructure(resultCollector);

        System.out.println("isSuccess: " + resultCollector.isSuccess());
        assertTrue(true);
    }

    public TreeIterator getIterator(String batch) throws URISyntaxException {
        File file = new File(Thread.currentThread().getContextClassLoader().getResource(batch).toURI());
        System.out.println(file);
        return new TransformingIteratorForFileSystems(file, "\\.", "\\.jp2$", ".md5");
    }
}
