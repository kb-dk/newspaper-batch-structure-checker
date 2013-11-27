package dk.statsbiblioteket.newspaper.schematron;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import dk.statsbiblioteket.doms.central.connectors.EnhancedFedora;
import dk.statsbiblioteket.doms.central.connectors.EnhancedFedoraImpl;
import dk.statsbiblioteket.doms.webservices.authentication.Credentials;
import dk.statsbiblioteket.medieplatform.autonomous.Batch;
import dk.statsbiblioteket.medieplatform.autonomous.ConfigConstants;
import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.TreeIterator;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.eventhandlers.EventRunner;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.eventhandlers.TreeEventHandler;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.fedora3.ConfigurableFilter;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.fedora3.IteratorForFedora3;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/** Integration test for StructureValidator */
public class StructureValidatorTestIT {

    /**
     * This test ingests a batch to DOMS and performs a structure validation on the ingested structure.
     *
     * @throws Exception
     */
    @Test(groups = "integrationTest", enabled = true)
    public void testValidateOnFedora() throws Exception {

        System.out.println("Starting integration test against Fedora instance");
        String pathToProperties = System.getProperty("integration.test.newspaper.properties");
        Properties properties = new Properties();
        properties.load(new FileInputStream(pathToProperties));
        Credentials creds = new Credentials(properties.getProperty(ConfigConstants.DOMS_USERNAME),
                                            properties.getProperty(ConfigConstants.DOMS_PASSWORD));
        String fedoraLocation = properties.getProperty(ConfigConstants.DOMS_URL);
        EnhancedFedora eFedora =
                new EnhancedFedoraImpl(creds, fedoraLocation, properties.getProperty(ConfigConstants.DOMS_PIDGENERATOR_URL), null);


        String pid = eFedora.findObjectFromDCIdentifier("path:B400022028241-RT1").get(0);

        ResultCollector resultCollector = new ResultCollector("Batch Structure Checker", "v0.1");
        TreeIterator iterator;

        Client client = Client.create();
        client.addFilter(new HTTPBasicAuthFilter(properties.getProperty(ConfigConstants.DOMS_USERNAME),
                                                 properties.getProperty(ConfigConstants.DOMS_PASSWORD)));
        try {
            iterator = new IteratorForFedora3(pid,
                                              client,
                                              properties.getProperty(ConfigConstants.DOMS_URL),
                                              new ConfigurableFilter(Arrays.asList("MODS",
                                                                                   "FILM",
                                                                                   "EDITION",
                                                                                   "ALTO",
                                                                                   "MIX"),
                                                                     Arrays.asList(
                                                                             "info:fedora/fedora-system:def/relations-external#hasPart")),
                                              ConfigConstants.ITERATOR_DATAFILEPATTERN);
        } catch (Exception e) {
            throw new IOException(e);
        }
        EventRunner eventRunner = new EventRunner(iterator);
        List<TreeEventHandler> handlers = new ArrayList<>();
        XmlBuilderEventHandler xmlBuilderEventHandler = new XmlBuilderEventHandler();
        handlers.add(xmlBuilderEventHandler);
        eventRunner.runEvents(handlers, resultCollector);
        String xml = xmlBuilderEventHandler.getXml();
        StructureValidator validator = new StructureValidator("demands.sch");
        Batch batch = new Batch();
        batch.setRoundTripNumber(1);
        batch.setBatchID("400022028241");
        validator.validate(batch, new ByteArrayInputStream(xml.getBytes("UTF-8")), resultCollector);
    }


}
