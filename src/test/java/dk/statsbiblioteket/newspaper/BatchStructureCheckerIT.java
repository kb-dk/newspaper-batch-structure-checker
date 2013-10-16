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
 */
public class BatchStructureCheckerIT {
    /**
     * Test BatchStructureChecker
     * @throws Exception
     */
    @Test
    public void testBatchStructureCheck() throws Exception {
        TreeIterator iterator = getIterator("batch");
        BatchStructureChecker batchStructureChecker = new BatchStructureChecker(iterator);
        ResultCollector resultCollector = new ResultCollector("Batch Structure Checker", "v0.1");

        EventHandlerFactory eventHandlerFactory = new CompleteCheckFactory(resultCollector);
        batchStructureChecker.checkBatchStructure(
                eventHandlerFactory.createEventHandlers(),
                resultCollector);

        System.out.println("Result: " + resultCollector);
    }

    public TreeIterator getIterator(String batch) throws URISyntaxException {
        File file = new File(Thread.currentThread().getContextClassLoader().getResource(batch).toURI());
        System.out.println(file);
        return new TransformingIteratorForFileSystems(file, "\\.", "\\.jp2$", ".md5");
    }
}
