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

import static org.testng.Assert.assertTrue;

/**
 * Integration test for StructureValidator
 */
public class StructureValidatorTestIT {

    /**
     * This test ingests a batch to DOMS and performs a structure validation on the ingested structure.
     * @throws Exception
     */
    //@Test(groups = "integrationTest")
    public void testValidateOnFedora() throws Exception {
        String pathToProperties = System.getProperty("integration.test.newspaper.properties");
        String pathToTestBatch = System.getProperty("integration.test.newspaper.testdata");
        Properties properties = new Properties();
        properties.load(new FileInputStream(pathToProperties));
        Credentials creds = new Credentials(properties.getProperty("fedora.admin.username"), properties.getProperty("fedora.admin.password"));
        String fedoraLocation = properties.getProperty("doms.server");
        EnhancedFedoraImpl eFedora = new EnhancedFedoraImpl(creds, fedoraLocation, properties.getProperty("pidgenerator.location") , null);
        SimpleFedoraIngester ingester = SimpleFedoraIngester.getNewspaperInstance(eFedora);
        File rootTestdataDir = new File(System.getProperty("integration.test.newspaper.testdata"));
        File testRoot = new File(rootTestdataDir, "small-test-batch/B400022028241-RT1");
        assertTrue(testRoot.exists(), testRoot.getAbsolutePath() + " does not exist.");
        TransformingIteratorForFileSystems fileSystemIterator =
                new TransformingIteratorForFileSystems(testRoot, Pattern.quote("."), ".*\\.jp2$", ".md5");
        String rootPid = ingester.ingest(fileSystemIterator);
        properties.setProperty("scratch", pathToTestBatch + "/" + "small-test-batch");
        ResultCollector resultCollector = new ResultCollector("Batch Structure Checker", "v0.1");
        TreeIterator iterator = null;
        properties.load(new FileReader(new File(System.getProperty(
                "integration.test.newspaper.properties"))));
        System.out.println(properties.getProperty("fedora.admin.username"));
        Client client = Client.create();
        client.addFilter(new HTTPBasicAuthFilter(properties.getProperty("fedora.admin.username"),
                properties.getProperty("fedora.admin.password")));
        try {
            iterator = new IteratorGetter(rootPid, client,
                    properties.getProperty("fedora.server"), new TestFilter());
        } catch (Exception e) {
            throw new IOException(e);
        }
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
    }

    static class TestFilter implements ContentModelFilter {

            public boolean isAttributeDatastream(String dsid, List<String> types) {
                List<String> names = Arrays.asList("DC", "MODS", "FILM", "EDITION", "ALTO", "MIX");
                return names.contains(dsid);
            }

            public boolean isChildRel(String predicate, List<String> types) {
                if (predicate.contains("#hasPart")){
                    return true;
                }
                return false;
            }
        }

        static class IteratorGetter extends IteratorForFedora3 {

            /**
             * Constructor.
             *
             * @param id      the fedora pid of the root object
             * @param client  the jersey client to use
             * @param restUrl the url to Fedora
             * @param filter  the content model filter to know which relations and datastreams to use
             */
            public IteratorGetter(String id, Client client, String restUrl, ContentModelFilter filter) throws CommunicationException {
                super(id, client, restUrl, filter);
            }

        }
}
