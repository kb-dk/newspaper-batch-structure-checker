package dk.statsbiblioteket.newspaper.schematron;

import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.TreeIterator;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.eventhandlers.EventRunner;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.eventhandlers.TreeEventHandler;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.filesystem.transforming.TransformingIteratorForFileSystems;
import dk.statsbiblioteket.util.xml.DOM;
import org.testng.annotations.Test;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 *
 */
public class XmlBuilderEventHandlerTest {

    @Test(groups = "integrationTest")
    public void testGetXml() throws Exception {
        String pathToProperties = System.getProperty("integration.test.newspaper.properties");
        String pathToTestBatch = System.getProperty("integration.test.newspaper.testdata");
        Properties properties = new Properties();
        properties.load(new FileInputStream(pathToProperties));
        properties.setProperty("scratch", pathToTestBatch + "/" + "small-test-batch");String groupingChar = Pattern.quote(properties.getProperty("groupingChar", "."));
        String dataFilePattern = properties.getProperty("dataFilePattern", ".*\\.jp2$");
        String checksumPostFix = properties.getProperty("checksumPostfix",".md5");
        TreeIterator iterator = new TransformingIteratorForFileSystems(new File(pathToTestBatch + "/" + "small-test-batch/B400022028241-RT1")
                ,groupingChar,dataFilePattern,checksumPostFix);
        EventRunner eventRunner = new EventRunner(iterator);
        List<TreeEventHandler> handlers = new ArrayList<TreeEventHandler>();
        XmlBuilderEventHandler xmlBuilderEventHandler = new XmlBuilderEventHandler();
        handlers.add(xmlBuilderEventHandler);
        eventRunner.runEvents(handlers);
        String xml = xmlBuilderEventHandler.getXml();
        assertTrue(xml.split("<node").length > 10, "Should be at least 10 nodes in document.");
        assertTrue(xml.split("<attribute").length > 10, "Should be at least 10 nodes in document.");
        assertNotNull(DOM.stringToDOM(xml), "Should have gotten parseable xml, not " + xml);
        System.out.println(xml);
    }
}
