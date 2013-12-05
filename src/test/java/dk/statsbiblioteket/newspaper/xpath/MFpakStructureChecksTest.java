package dk.statsbiblioteket.newspaper.xpath;

import dk.statsbiblioteket.medieplatform.autonomous.Batch;
import dk.statsbiblioteket.medieplatform.autonomous.ConfigConstants;
import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
import dk.statsbiblioteket.newspaper.mfpakintegration.configuration.MfPakConfiguration;
import dk.statsbiblioteket.newspaper.mfpakintegration.database.MfPakDAO;
import dk.statsbiblioteket.newspaper.mfpakintegration.database.NewspaperBatchOptions;
import org.testng.annotations.Test;

import java.io.FileInputStream;
import java.util.Properties;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class MFpakStructureChecksTest {

    @Test(groups = "integrationTest")
    public void testValidate() throws Exception {
        String pathToProperties = System.getProperty("integration.test.newspaper.properties");
        String pathToTestBatch = System.getProperty("integration.test.newspaper.testdata");
        Properties properties = new Properties();
        properties.load(new FileInputStream(pathToProperties));
        properties.setProperty("scratch", pathToTestBatch + "/" + "small-test-batch");

        MfPakConfiguration mfPakConfiguration = new MfPakConfiguration();
        mfPakConfiguration.setDatabaseUrl(properties.getProperty(ConfigConstants.MFPAK_URL));
        mfPakConfiguration.setDatabaseUser(properties.getProperty(ConfigConstants.MFPAK_USER));
        mfPakConfiguration.setDatabasePassword(properties.getProperty(ConfigConstants.MFPAK_PASSWORD));

        MFpakStructureChecks mFpakStructureChecks = new MFpakStructureChecks(new MfPakDAO(mfPakConfiguration));

        ResultCollector resultCollector = new ResultCollector("tool", "version");
        mFpakStructureChecks.validate(new Batch("400022028241"),
                Thread.currentThread()
                      .getContextClassLoader()
                      .getResourceAsStream("assumed-valid-structure.xml"),
                resultCollector);
    }

    @Test
    public void testValidateFailForNoAltoOption() throws Exception {
        Batch batch = new Batch("400022028241");

        MfPakDAO mfPakDAO = mock(MfPakDAO.class);
        NewspaperBatchOptions options = new NewspaperBatchOptions();
        options.setOptionB1(false);
        options.setOptionB2(false);
        options.setOptionB9(false);

        when(mfPakDAO.getBatchOptions(eq(batch.getBatchID()))).thenReturn(options);

        MFpakStructureChecks mFpakStructureChecks = new MFpakStructureChecks(mfPakDAO);

        ResultCollector resultCollector = new ResultCollector("tool", "version");
        mFpakStructureChecks.validate(batch,
                Thread.currentThread()
                      .getContextClassLoader()
                      .getResourceAsStream("assumed-valid-structure.xml"),
                resultCollector);
        assertTrue(resultCollector.toReport().contains("2F-M5:"), resultCollector.toReport());
        assertFalse(resultCollector.isSuccess(), resultCollector.toReport());
    }

    @Test
    public void testValidateSucceedForAltoOptionB1() throws Exception {
        Batch batch = new Batch("400022028241");

        MfPakDAO mfPakDAO = mock(MfPakDAO.class);
        NewspaperBatchOptions options = new NewspaperBatchOptions();
        options.setOptionB1(true);

        when(mfPakDAO.getBatchOptions(eq(batch.getBatchID()))).thenReturn(options);

        MFpakStructureChecks mFpakStructureChecks = new MFpakStructureChecks(mfPakDAO);

        ResultCollector resultCollector = new ResultCollector("tool", "version");
        mFpakStructureChecks.validate(batch,
                Thread.currentThread()
                      .getContextClassLoader()
                      .getResourceAsStream("assumed-valid-structure.xml"),
                resultCollector);
        assertFalse(resultCollector.toReport().contains("2F-M4:"), resultCollector.toReport());
        assertFalse(resultCollector.toReport().contains("2F-M5:"), resultCollector.toReport());
        assertFalse(resultCollector.toReport().contains("exception"), resultCollector.toReport());
    }

    @Test
    public void testValidateSucceedForNoAltoOption() throws Exception {
        Batch batch = new Batch("400022028241");

        MfPakDAO mfPakDAO = mock(MfPakDAO.class);
        NewspaperBatchOptions options = new NewspaperBatchOptions();
        options.setOptionB1(false);
        options.setOptionB2(false);
        options.setOptionB9(false);

        when(mfPakDAO.getBatchOptions(eq(batch.getBatchID()))).thenReturn(options);

        MFpakStructureChecks mFpakStructureChecks = new MFpakStructureChecks(mfPakDAO);

        ResultCollector resultCollector = new ResultCollector("tool", "version");
        mFpakStructureChecks.validate(batch,
                Thread.currentThread()
                      .getContextClassLoader()
                      .getResourceAsStream("no-alto-invalid-structure.xml"),
                resultCollector);
        assertFalse(resultCollector.toReport().contains("2F-M4:"), resultCollector.toReport());
        assertFalse(resultCollector.toReport().contains("2F-M5:"), resultCollector.toReport());
        assertFalse(resultCollector.toReport().contains("exception"), resultCollector.toReport());
    }

    @Test
    public void testValidateFailForAltoOptionB1() throws Exception {
        Batch batch = new Batch("400022028241");

        MfPakDAO mfPakDAO = mock(MfPakDAO.class);
        NewspaperBatchOptions options = new NewspaperBatchOptions();
        options.setOptionB1(true);

        when(mfPakDAO.getBatchOptions(eq(batch.getBatchID()))).thenReturn(options);

        MFpakStructureChecks mFpakStructureChecks = new MFpakStructureChecks(mfPakDAO);

        ResultCollector resultCollector = new ResultCollector("tool", "version");
        mFpakStructureChecks.validate(batch,
                Thread.currentThread()
                      .getContextClassLoader()
                      .getResourceAsStream("no-alto-invalid-structure.xml"),
                resultCollector);
        assertTrue(resultCollector.toReport().contains("2F-M4:"), resultCollector.toReport());
        assertFalse(resultCollector.toReport().contains("2F-M5:"), resultCollector.toReport());
        assertFalse(resultCollector.toReport().contains("exception"), resultCollector.toReport());
    }
}
