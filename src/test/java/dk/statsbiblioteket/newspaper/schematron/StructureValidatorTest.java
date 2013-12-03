package dk.statsbiblioteket.newspaper.schematron;

import dk.statsbiblioteket.medieplatform.autonomous.Batch;
import dk.statsbiblioteket.medieplatform.autonomous.ConfigConstants;
import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.TreeIterator;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.eventhandlers.EventRunner;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.eventhandlers.TreeEventHandler;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.filesystem.transforming.TransformingIteratorForFileSystems;
import dk.statsbiblioteket.newspaper.eventhandlers.Util;
import dk.statsbiblioteket.newspaper.mfpakintegration.configuration.MfPakConfiguration;
import dk.statsbiblioteket.newspaper.mfpakintegration.database.MfPakDAO;
import dk.statsbiblioteket.newspaper.mfpakintegration.database.NewspaperBatchOptions;
import dk.statsbiblioteket.util.xml.DOM;
import dk.statsbiblioteket.util.xml.XPathSelector;
import org.testng.annotations.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * Tests for structure-validator.
 */
public class StructureValidatorTest {

    /**
     * Checks that running on a well-structured batch produces no errors.
     * @throws Exception
     */
    @Test(groups = "integrationTest")
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
        eventRunner.runEvents(handlers, resultCollector);

        String xml = xmlBuilderEventHandler.getXml();
        //System.out.println(xml);

        MfPakConfiguration mfPakConfiguration = new MfPakConfiguration();
        mfPakConfiguration.setDatabaseUrl(properties.getProperty(ConfigConstants.MFPAK_URL));
        mfPakConfiguration.setDatabaseUser(properties.getProperty(ConfigConstants.MFPAK_USER));
        mfPakConfiguration.setDatabasePassword(properties.getProperty(ConfigConstants.MFPAK_PASSWORD));

        StructureValidator validator = new StructureValidator("demands.sch", new MfPakDAO(mfPakConfiguration));
        Batch batch = new Batch();
        batch.setRoundTripNumber(1);
        batch.setBatchID("400022028241");
        validator.validate(batch, new ByteArrayInputStream(xml.getBytes("UTF-8")), resultCollector);
        System.out.println(resultCollector.toReport());
    }


    /**
     * Checks that running on a badly-structured batch produces many errors.
     * @throws Exception
     */
    @Test(groups = "integrationTest", enabled = false)
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
        eventRunner.runEvents(handlers, resultCollector);
        String xml = xmlBuilderEventHandler.getXml();

        MfPakConfiguration mfPakConfiguration = new MfPakConfiguration();
        mfPakConfiguration.setDatabaseUrl(properties.getProperty(ConfigConstants.MFPAK_URL));
        mfPakConfiguration.setDatabaseUser(properties.getProperty(ConfigConstants.MFPAK_USER));
        mfPakConfiguration.setDatabasePassword(properties.getProperty(ConfigConstants.MFPAK_PASSWORD));

        StructureValidator validator = new StructureValidator("demands.sch", new MfPakDAO(mfPakConfiguration));
        Batch batch = new Batch();
        batch.setRoundTripNumber(1);
        batch.setBatchID("400022028241");
        validator.validate(batch, new ByteArrayInputStream(xml.getBytes("UTF-8")), resultCollector);
        assertFalse(resultCollector.isSuccess());
        assertTrue(Util.countFailures(resultCollector) >= 40);
    }


    /* This commented out code can be deleted when alto-checks have been successfully coded in MFpakStructureChecks
    @Test
    public void testValidateCheckAlto() throws Exception {
        Batch batch = new Batch("400022028241");

        String schematronPath = "demands.sch";
        MfPakDAO mfPakDAO = mock(MfPakDAO.class);
        NewspaperBatchOptions options = new NewspaperBatchOptions();
        options.setOptionB1(true);

        when(mfPakDAO.getBatchOptions(eq(batch.getBatchID()))).thenReturn(options);

        StructureValidator structureValidator = new StructureValidator(schematronPath, mfPakDAO);

        Document document = mock(Document.class);
        ResultCollector resultCollector = mock(ResultCollector.class);
        document = structureValidator.setAltoCheckFlagIfNeeded(document, batch, resultCollector);

        String xpathForAlto = "/s:schema/s:let[@name='altoFlag']";
        final XPathSelector SCHEMATRON_XPATH_SELECTOR = DOM.createXPathSelector("s", "http://purl.oclc.org/dsdl/schematron");

        String flag = ((Element)(SCHEMATRON_XPATH_SELECTOR.selectNode(document, xpathForAlto))).getAttribute("altoFlag");

        assertTrue(flag.equals("true()"));
    }
    */

}
