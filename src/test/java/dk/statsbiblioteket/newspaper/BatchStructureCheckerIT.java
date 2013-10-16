package dk.statsbiblioteket.newspaper;

import java.io.File;
import java.net.URISyntaxException;

import dk.statsbiblioteket.autonomous.ResultCollector;
import dk.statsbiblioteket.doms.iterator.common.TreeIterator;
import dk.statsbiblioteket.doms.iterator.filesystem.transforming.TransformingIteratorForFileSystems;
import dk.statsbiblioteket.newspaper.eventhandlers.CompleteCheckFactory;
import dk.statsbiblioteket.newspaper.eventhandlers.EventHandlerFactory;
import org.junit.Test;

/**
 * @author jrg
 */
public class BatchStructureCheckerIT {
    /**
     * Tests that the BatchStructureChecker can parse a production like batch.
     */
    @Test
    public void testBatchStructureCheck() throws Exception {
        TreeIterator iterator = getIterator();
        BatchStructureChecker batchStructureChecker = new BatchStructureChecker(iterator);
        ResultCollector resultCollector = new ResultCollector("Batch Structure Checker", "v0.1");

        EventHandlerFactory eventHandlerFactory = new CompleteCheckFactory(resultCollector);
        batchStructureChecker.checkBatchStructure(
                eventHandlerFactory.createEventHandlers(),
                resultCollector);

        System.out.println("Result: " + resultCollector);
    }

    /**
     * Creates and returns a iteration based on the test batch file structure found in the test/ressources folder.
     * @return A iterator the the test batch
     * @throws URISyntaxException
     */
    public TreeIterator getIterator() throws URISyntaxException {
        File file = new File(Thread.currentThread().getContextClassLoader().getResource("batch").toURI());
        System.out.println(file);
        return new TransformingIteratorForFileSystems(file, "\\.", "\\.jp2$", ".md5");
    }
}
