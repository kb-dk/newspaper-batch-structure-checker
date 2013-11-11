package dk.statsbiblioteket.newspaper.xpath;

import dk.statsbiblioteket.medieplatform.autonomous.Batch;
import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
import dk.statsbiblioteket.newspaper.mfpakintegration.configuration.ConfigurationProperties;
import dk.statsbiblioteket.newspaper.mfpakintegration.configuration.MfPakConfiguration;
import dk.statsbiblioteket.newspaper.mfpakintegration.database.MfPakDAO;
import org.testng.annotations.Test;

import java.io.FileInputStream;
import java.util.Properties;

public class MFpakStructureChecksTest {


    @Test(groups = "integrationTest")
    public void testValidate() throws Exception {
        String pathToProperties = System.getProperty("integration.test.newspaper.properties");
        String pathToTestBatch = System.getProperty("integration.test.newspaper.testdata");
        Properties properties = new Properties();
        properties.load(new FileInputStream(pathToProperties));
        properties.setProperty("scratch", pathToTestBatch + "/" + "small-test-batch");


        MfPakConfiguration mfPakConfiguration = new MfPakConfiguration();
                mfPakConfiguration.setDatabaseUrl(properties.getProperty(ConfigurationProperties.DATABASE_URL));
                mfPakConfiguration.setDatabaseUser(properties.getProperty(ConfigurationProperties.DATABASE_USER));
                mfPakConfiguration.setDatabasePassword(properties.getProperty(ConfigurationProperties.DATABASE_PASSWORD));



        MFpakStructureChecks mFpakStructureChecks = new MFpakStructureChecks(new MfPakDAO(mfPakConfiguration));


        ResultCollector resultCollector = new ResultCollector("tool", "version");
        mFpakStructureChecks.validate(new Batch("400022028241"),Thread.currentThread().getContextClassLoader().getResourceAsStream("structure.xml"),resultCollector);

    }
}
