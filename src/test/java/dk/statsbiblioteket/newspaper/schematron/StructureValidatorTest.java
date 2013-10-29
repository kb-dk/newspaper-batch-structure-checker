package dk.statsbiblioteket.newspaper.schematron;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import dk.statsbiblioteket.doms.central.connectors.EnhancedFedoraImpl;
import dk.statsbiblioteket.doms.webservices.authentication.Credentials;
import dk.statsbiblioteket.medieplatform.autonomous.Batch;
import dk.statsbiblioteket.medieplatform.autonomous.CommunicationException;
import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.TreeIterator;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.eventhandlers.EventRunner;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.eventhandlers.TreeEventHandler;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.fedora3.ContentModelFilter;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.fedora3.IteratorForFedora3;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.filesystem.transforming.TransformingIteratorForFileSystems;
import dk.statsbiblioteket.newspaper.BatchStructureCheckerComponent;
import dk.statsbiblioteket.newspaper.eventhandlers.Util;
import dk.statsbiblioteket.newspaper.promptdomsingester.SimpleFedoraIngester;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * Tests for structure-validator.
 */
public class StructureValidatorTest {

    /**
     * Checks that running on a well-strcutured batch produces no errors.
     * @throws Exception
     */
    @Test
    public void testValidate() throws Exception {
        String pathToProperties = System.getProperty("integration.test.newspaper.properties");
        String pathToTestBatch = System.getProperty("integration.test.newspaper.testdata");
        Properties properties = new Properties();
        properties.load(new FileInputStream(pathToProperties));
        properties.setProperty("scratch", pathToTestBatch + "/" + "small-test-batch");
        ResultCollector resultCollector = new ResultCollector("Batch Structure Checker", "v0.1");
        String groupingChar = Pattern.quote(properties.getProperty("groupingChar", "."));
        String dataFilePattern = properties.getProperty("dataFilePattern", ".*\\.jp2$");
        String checksumPostFix = properties.getProperty("checksumPostfix",".md5");
        String path = pathToTestBatch + "/" + "small-test-batch/B400022028241-RT1";
        TreeIterator iterator = new TransformingIteratorForFileSystems(new File(path), groupingChar,
                dataFilePattern, checksumPostFix);
        EventRunner eventRunner = new EventRunner(iterator);
        List<TreeEventHandler> handlers = new ArrayList<TreeEventHandler>();
        XmlBuilderEventHandler xmlBuilderEventHandler = new XmlBuilderEventHandler();
        handlers.add(xmlBuilderEventHandler);
        eventRunner.runEvents(handlers);

        String xml = xmlBuilderEventHandler.getXml();
        //System.out.println(xml);

        StructureValidator validator = new StructureValidator("demands.sch");
        Batch batch = new Batch();
        batch.setRoundTripNumber(1);
        batch.setBatchID("400022028241");
        validator.validate(batch, new ByteArrayInputStream(xml.getBytes("UTF-8")), resultCollector);
    }


    /**
     * Checks that running on a badly-structured batch produces many errors.
     * @throws Exception
     */
    @Test
    public void testValidateBadBadBatch() throws Exception {
        String pathToProperties = System.getProperty("integration.test.newspaper.properties");
        String pathToTestBatch = System.getProperty("integration.test.newspaper.testdata");
        Properties properties = new Properties();
        properties.load(new FileInputStream(pathToProperties));
        properties.setProperty("scratch", pathToTestBatch + "/" + "bad-bad-batch");
        ResultCollector resultCollector = new ResultCollector("Batch Structure Checker", "v0.1");
        String groupingChar = Pattern.quote(properties.getProperty("groupingChar", "."));
        String dataFilePattern = properties.getProperty("dataFilePattern", ".*\\.jp2$");
        String checksumPostFix = properties.getProperty("checksumPostfix",".md5");
        File batchRoot = new File(pathToTestBatch + "/" + "bad-bad-batch/B400022028241-RT1");
        TreeIterator iterator = new TransformingIteratorForFileSystems(batchRoot, groupingChar,
                dataFilePattern,checksumPostFix);
        EventRunner eventRunner = new EventRunner(iterator);
        List<TreeEventHandler> handlers = new ArrayList<TreeEventHandler>();
        XmlBuilderEventHandler xmlBuilderEventHandler = new XmlBuilderEventHandler();
        handlers.add(xmlBuilderEventHandler);
        eventRunner.runEvents(handlers);
        String xml = xmlBuilderEventHandler.getXml();
        StructureValidator validator = new StructureValidator("demands.sch");
        Batch batch = new Batch();
        batch.setRoundTripNumber(1);
        batch.setBatchID("400022028241");
        validator.validate(batch, new ByteArrayInputStream(xml.getBytes("UTF-8")), resultCollector);
        assertFalse(resultCollector.isSuccess());
        assertTrue(Util.countFailures(resultCollector) > 40);
    }
}
