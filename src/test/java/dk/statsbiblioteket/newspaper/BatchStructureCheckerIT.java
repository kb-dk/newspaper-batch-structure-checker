package dk.statsbiblioteket.newspaper;

import java.io.File;
import java.io.FileInputStream;
import java.net.URISyntaxException;
import java.util.Properties;

import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.TreeIterator;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.filesystem.transforming.TransformingIteratorForFileSystems;
import dk.statsbiblioteket.newspaper.eventhandlers.CompleteCheckFactory;
import dk.statsbiblioteket.newspaper.eventhandlers.EventHandlerFactory;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 */
public class BatchStructureCheckerIT {
    
    private final static String TEST_BATCH_ID = "400022028241";
    
    /**
     * Tests that the BatchStructureChecker can parse a production like batch.
     */
    @Test(groups = "integrationTest")
    public void testBatchStructureCheck() throws Exception {
        String pathToProperties = System.getProperty("integration.test.newspaper.properties");
        Properties properties = new Properties();
        properties.load(new FileInputStream(pathToProperties));
        
        TreeIterator iterator = getIterator();
        BatchStructureChecker batchStructureChecker = new BatchStructureChecker(iterator);
        ResultCollector resultCollector = new ResultCollector("Batch Structure Checker", "v0.1");

        EventHandlerFactory eventHandlerFactory = new CompleteCheckFactory(properties, TEST_BATCH_ID, resultCollector);
        batchStructureChecker.checkBatchStructure(
                eventHandlerFactory.createEventHandlers());
        //Assert.fail();
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
