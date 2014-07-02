package dk.statsbiblioteket.newspaper.xpath;

import java.io.FileInputStream;
import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import dk.statsbiblioteket.medieplatform.autonomous.Batch;
import dk.statsbiblioteket.medieplatform.autonomous.ConfigConstants;
import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
import dk.statsbiblioteket.newspaper.BatchStructureCheckerComponent;
import dk.statsbiblioteket.newspaper.mfpakintegration.batchcontext.BatchContext;
import dk.statsbiblioteket.newspaper.mfpakintegration.batchcontext.BatchContextUtils;
import dk.statsbiblioteket.newspaper.mfpakintegration.configuration.MfPakConfiguration;
import dk.statsbiblioteket.newspaper.mfpakintegration.database.MfPakDAO;
import dk.statsbiblioteket.newspaper.mfpakintegration.database.NewspaperBatchOptions;
import dk.statsbiblioteket.newspaper.mfpakintegration.database.NewspaperDateRange;
import dk.statsbiblioteket.newspaper.xpath.MFpakStructureChecks.FilmDateRange;
import dk.statsbiblioteket.util.xml.DOM;
import dk.statsbiblioteket.util.xml.XPathSelector;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import static org.mockito.Matchers.anyVararg;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.assertEquals;

public class MFpakStructureChecksTest {
/*
    @BeforeMethod 
    public void nukeBatchContext() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        Field contexts = BatchContextUtils.class.getDeclaredField("batchContexts");
        contexts.setAccessible(true);
        Map m = (Map) contexts.get(null);
        m.clear();
    }
*/    
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
        Batch batch = new Batch("400022028241");
        
        BatchContext context = BatchContextUtils.buildBatchContext(new MfPakDAO(mfPakConfiguration), batch);
        MFpakStructureChecks mFpakStructureChecks = new MFpakStructureChecks(context);

        ResultCollector resultCollector = new ResultCollector("tool", "version", 1000);
        mFpakStructureChecks.validate(batch,
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
        when(mfPakDAO.getNewspaperID(eq(batch.getBatchID()))).thenReturn("foobar");
        when(mfPakDAO.getBatchShipmentDate(eq(batch.getBatchID()))).thenReturn(new Date(0));

        BatchContext context = BatchContextUtils.buildBatchContext(mfPakDAO, batch);
        /* FIXME Caching mfpak requests broke this test, so until someone finds a proper way around this
            we're stuck with this hack..*/
        context.getBatchOptions().setOptionB1(false);
        context.getBatchOptions().setOptionB2(false);
        context.getBatchOptions().setOptionB9(false);
        MFpakStructureChecks mFpakStructureChecks = new MFpakStructureChecks(context);

        ResultCollector resultCollector = new ResultCollector("tool", "version", 1000);
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
        when(mfPakDAO.getNewspaperID(eq(batch.getBatchID()))).thenReturn("foobar");
        when(mfPakDAO.getBatchShipmentDate(eq(batch.getBatchID()))).thenReturn(new Date(0));
        
        BatchContext context = BatchContextUtils.buildBatchContext(mfPakDAO, batch);
        /* FIXME Caching mfpak requests broke this test, so until someone finds a proper way around this
        we're stuck with this hack..*/
        context.getBatchOptions().setOptionB1(true);
        MFpakStructureChecks mFpakStructureChecks = new MFpakStructureChecks(context);

        ResultCollector resultCollector = new ResultCollector("tool", "version", 1000);
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
        when(mfPakDAO.getNewspaperID(eq(batch.getBatchID()))).thenReturn("foobar");
        when(mfPakDAO.getBatchShipmentDate(eq(batch.getBatchID()))).thenReturn(new Date(0));
        
        BatchContext context = BatchContextUtils.buildBatchContext(mfPakDAO, batch);
        /* FIXME Caching mfpak requests broke this test, so until someone finds a proper way around this
        we're stuck with this hack..*/
        context.getBatchOptions().setOptionB1(false);
        context.getBatchOptions().setOptionB2(false);
        context.getBatchOptions().setOptionB9(false);
        
        MFpakStructureChecks mFpakStructureChecks = new MFpakStructureChecks(context);

        ResultCollector resultCollector = new ResultCollector("tool", "version", 1000);
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
        when(mfPakDAO.getNewspaperID(eq(batch.getBatchID()))).thenReturn("foobar");
        when(mfPakDAO.getBatchShipmentDate(eq(batch.getBatchID()))).thenReturn(new Date(0));
        
        BatchContext context = BatchContextUtils.buildBatchContext(mfPakDAO, batch);
        MFpakStructureChecks mFpakStructureChecks = new MFpakStructureChecks(context);

        ResultCollector resultCollector = new ResultCollector("tool", "version", 1000);
        mFpakStructureChecks.validate(batch,
                Thread.currentThread()
                      .getContextClassLoader()
                      .getResourceAsStream("no-alto-invalid-structure.xml"),
                resultCollector);
        assertTrue(resultCollector.toReport().contains("2F-M4:"), resultCollector.toReport());
        assertFalse(resultCollector.toReport().contains("2F-M5:"), resultCollector.toReport());
        assertFalse(resultCollector.toReport().contains("exception"), resultCollector.toReport());
    }

    @Test
    public void testDateValidFilmDates() throws ParseException {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Batch testBatch = new Batch("500022028241");
        BatchContext contextMock = mock(BatchContext.class);
        ResultCollector resultCollectorMock = mock(ResultCollector.class);

        NewspaperDateRange film1DateRange = new NewspaperDateRange(
                dateFormat.parse("1795-06-01"),
                dateFormat.parse("1795-06-15"));
        NewspaperDateRange film2DateRange = new NewspaperDateRange(
                dateFormat.parse("1795-06-16"),
                dateFormat.parse("1795-06-20"));

        when(contextMock.getDateRanges()).thenReturn(Arrays.asList(film1DateRange, film2DateRange));
        MFpakStructureChecks mFpakStructureChecks = new MFpakStructureChecks(contextMock);
        String batchXmlStructure = createBatchXmlDoc(testBatch.getBatchID(),
                createBatchStructureXml(testBatch.getBatchID(), 1, "1795-06-16-01", "1795-06-17-01"),
                createBatchStructureXml(testBatch.getBatchID(), 2, "1795-06-13-01", "1795-06-14-01", "1795-06-15-01"));
        mFpakStructureChecks.validateDateRanges(
                testBatch,
                resultCollectorMock,
                DOM.createXPathSelector(),
                DOM.stringToDOM(batchXmlStructure)
        );

        verify(resultCollectorMock).isSuccess();
        
        verifyNoMoreInteractions(resultCollectorMock);
    }

    @Test
    public void testDateInvalidFilmDates() throws ParseException {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Batch testBatch = new Batch("500022028241");
        BatchContext contextMock = mock(BatchContext.class);
        ResultCollector resultCollectorMock = mock(ResultCollector.class);

        NewspaperDateRange film1DateRange = new NewspaperDateRange(
                dateFormat.parse("1795-06-01"),
                dateFormat.parse("1795-06-15"));

        when(contextMock.getDateRanges()).thenReturn(Arrays.asList(film1DateRange));
        MFpakStructureChecks mFpakStructureChecks = new MFpakStructureChecks(contextMock);
        String batchXmlStructure = createBatchXmlDoc(testBatch.getBatchID(),
                createBatchStructureXml(testBatch.getBatchID(), 1, "1795-06-15-01", "1795-06-16-01"));
        mFpakStructureChecks.validateDateRanges(
                testBatch,
                resultCollectorMock,
                DOM.createXPathSelector(),
                DOM.stringToDOM(batchXmlStructure)
        );
        
        verify(resultCollectorMock).isSuccess();
        
        verify(resultCollectorMock).addFailure(
                eq("500022028241-1"),
                eq(BatchStructureCheckerComponent.TYPE),
                eq(MFpakStructureChecks.class.getSimpleName()),
                eq("2F-M3: The date range (1795-06-15 - 1795-06-16) for the film editions are not valid according to " +
                        "any date range from mfpak"),
                (String)anyVararg());

        verify(resultCollectorMock).addFailure(
                eq("B500022028241-RT1"),
                eq(BatchStructureCheckerComponent.TYPE),
                eq(MFpakStructureChecks.class.getSimpleName()),
                eq("2F-M3: There should have been a film covering the dateranges 1795-06-01 - 1795-06-15"),
                (String)anyVararg());

        verifyNoMoreInteractions(resultCollectorMock);
    }

    @Test
    public void testDateRagesWithOverlappingValidFilmDates() throws ParseException {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Batch testBatch = new Batch("500022028241");
        BatchContext contextMock = mock(BatchContext.class);
        ResultCollector resultCollectorMock = mock(ResultCollector.class);

        NewspaperDateRange film1DateRange = new NewspaperDateRange(
                dateFormat.parse("1795-06-01"),
                dateFormat.parse("1795-06-15"));
        NewspaperDateRange film2DateRange = new NewspaperDateRange(
                dateFormat.parse("1795-06-14"),
                dateFormat.parse("1795-06-20"));

        when(contextMock.getDateRanges()).thenReturn(Arrays.asList(film1DateRange, film2DateRange));
        MFpakStructureChecks mFpakStructureChecks = new MFpakStructureChecks(contextMock);
        String batchXmlStructure = createBatchXmlDoc(testBatch.getBatchID(),
                createBatchStructureXml(testBatch.getBatchID(), 1, "1795-06-14-01", "1795-06-15-01", "1795-06-16-01", "1795-06-17-01"),
                createBatchStructureXml(testBatch.getBatchID(), 2, "1795-06-13-01", "1795-06-14-01", "1795-06-15-01"));
        mFpakStructureChecks.validateDateRanges(
                testBatch,
                resultCollectorMock,
                DOM.createXPathSelector(),
                DOM.stringToDOM(batchXmlStructure)
        );

        verify(resultCollectorMock).isSuccess();
        
        verifyNoMoreInteractions(resultCollectorMock);
    }

    @Test
    public void testDateRangesWithOverlappingInvalidFilmDates() throws ParseException {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Batch testBatch = new Batch("500022028241");
        BatchContext contextMock = mock(BatchContext.class);
        ResultCollector resultCollectorMock = mock(ResultCollector.class);

        NewspaperDateRange film1DateRange = new NewspaperDateRange(
                dateFormat.parse("1795-06-01"),
                dateFormat.parse("1795-06-15"));
        NewspaperDateRange film2DateRange = new NewspaperDateRange(
                dateFormat.parse("1795-06-14"),
                dateFormat.parse("1795-06-20"));

        when(contextMock.getDateRanges()).thenReturn(Arrays.asList(film1DateRange, film2DateRange));
        MFpakStructureChecks mFpakStructureChecks = new MFpakStructureChecks(contextMock);
        String batchXmlStructure = createBatchXmlDoc(testBatch.getBatchID(),
                createBatchStructureXml(testBatch.getBatchID(), 1, "1795-06-14-01", "1795-06-15-01", "1795-06-16-01", "1795-06-17-01"),
                createBatchStructureXml(testBatch.getBatchID(), 2, "1795-06-13-01", "1795-06-14-01", "1795-06-15-01", "1795-06-16-01"));
        mFpakStructureChecks.validateDateRanges(
                testBatch,
                resultCollectorMock,
                DOM.createXPathSelector(),
                DOM.stringToDOM(batchXmlStructure)
        );

        verify(resultCollectorMock).addFailure(
                eq("500022028241-2"),
                eq(BatchStructureCheckerComponent.TYPE),
                eq(MFpakStructureChecks.class.getSimpleName()),
                eq("2F-M3: The date range (1795-06-13 - 1795-06-16) for the film editions are not valid according " +
                        "to any date range from mfpak"),
                (String)anyVararg());
        verify(resultCollectorMock).addFailure(
                eq("B500022028241-RT1"),
                eq(BatchStructureCheckerComponent.TYPE),
                eq(MFpakStructureChecks.class.getSimpleName()),
                eq("2F-M3: There should have been a film covering the dateranges 1795-06-01 - 1795-06-15"),
                (String)anyVararg());

        verify(resultCollectorMock).isSuccess();
        
        verifyNoMoreInteractions(resultCollectorMock);
    }
    
    @Test 
    public void testDateRangesWithOverlappingSubsetFilmDates() throws ParseException {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Batch testBatch = new Batch("500022028241");
        BatchContext contextMock = mock(BatchContext.class);
        ResultCollector resultCollectorMock = mock(ResultCollector.class);

        NewspaperDateRange film1DateRange = new NewspaperDateRange(
                dateFormat.parse("1795-06-01"),
                dateFormat.parse("1795-06-10"));
        NewspaperDateRange film2DateRange = new NewspaperDateRange(
                dateFormat.parse("1795-06-03"),
                dateFormat.parse("1795-06-07"));
   
        when(contextMock.getDateRanges()).thenReturn(Arrays.asList(film1DateRange, film2DateRange));
        MFpakStructureChecks mFpakStructureChecks = new MFpakStructureChecks(contextMock);
        String batchXmlStructure = createBatchXmlDoc(testBatch.getBatchID(),
                createBatchStructureXml(testBatch.getBatchID(), 2, "1795-06-04-01", "1795-06-06-01", "1795-06-07-01"),
                createBatchStructureXml(testBatch.getBatchID(), 1, "1795-06-01-01", "1795-06-02-01", "1795-06-05-01", "1795-06-09-01"));
        mFpakStructureChecks.validateDateRanges(
                testBatch,
                resultCollectorMock,
                DOM.createXPathSelector(),
                DOM.stringToDOM(batchXmlStructure)
        );

        verify(resultCollectorMock).isSuccess();
        
        verifyNoMoreInteractions(resultCollectorMock);
    }    
    
    @Test 
    public void testDateRangesFuzzyFilmDates() throws ParseException {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Batch testBatch = new Batch("500022028241");
        BatchContext contextMock = mock(BatchContext.class);
        ResultCollector resultCollectorMock = mock(ResultCollector.class);

        NewspaperDateRange film1DateRange = new NewspaperDateRange(
                dateFormat.parse("1795-06-01"),
                dateFormat.parse("1795-06-30"));
        
        when(contextMock.getDateRanges()).thenReturn(Arrays.asList(film1DateRange));
        MFpakStructureChecks mFpakStructureChecks = new MFpakStructureChecks(contextMock);
        String batchXmlStructure = createBatchXmlDoc(testBatch.getBatchID(),
                createBatchStructureXml(testBatch.getBatchID(), 1, "1795-06-01", "1795-06-06-01", "1795-06-30-01"));
        mFpakStructureChecks.validateDateRanges(
                testBatch,
                resultCollectorMock,
                DOM.createXPathSelector(),
                DOM.stringToDOM(batchXmlStructure)
        );

        verify(resultCollectorMock).isSuccess();
        
        verifyNoMoreInteractions(resultCollectorMock);
    }    
    
    @Test 
    public void testFilmDateRangesFuzzyGeneration() throws ParseException {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Batch testBatch = new Batch("500022028241");
        BatchContext contextMock = mock(BatchContext.class);
        ResultCollector resultCollectorMock = mock(ResultCollector.class);

        NewspaperDateRange film1DateRange = new NewspaperDateRange(
                dateFormat.parse("1795-06-01"),
                dateFormat.parse("1795-06-30"));
        
        when(contextMock.getDateRanges()).thenReturn(Arrays.asList(film1DateRange));
        MFpakStructureChecks mFpakStructureChecks = new MFpakStructureChecks(contextMock);
        String batchXmlStructure = createBatchXmlDoc(testBatch.getBatchID(),
                createBatchStructureXml(testBatch.getBatchID(), 1, "1795-02-16-01", "1795-02-15-01", "1795-02-17-01"),
                createBatchStructureXml(testBatch.getBatchID(), 2, "1795-02-17-01", "1795-02-15-01"),
                createBatchStructureXml(testBatch.getBatchID(), 3, "1795-01-17-01", "1795-02-01"),
                createBatchStructureXml(testBatch.getBatchID(), 4, "1795-02-17-01", "1795-02-01"),
                createBatchStructureXml(testBatch.getBatchID(), 5, "1795-01"));
        
        XPathSelector xpath = DOM.createXPathSelector();
        Document doc = DOM.stringToDOM(batchXmlStructure);
        String xpathFilmNode =
                "/node[@shortName='" + testBatch.getFullID() + "']/node[starts-with(@shortName,'" + testBatch.getBatchID()
                        + "')]";
        NodeList filmNodes = xpath.selectNodeList(doc, xpathFilmNode);
        
        List<MFpakStructureChecks.FilmDateRange> unmappedFilmRanges 
            = mFpakStructureChecks.buildBatchStructureDateRanges(filmNodes, xpath, resultCollectorMock);
        
        assertEquals(5, unmappedFilmRanges.size());
        
        assertEquals(testBatch.getBatchID() + "-1", unmappedFilmRanges.get(0).getFilmShortName());
        assertTrue(unmappedFilmRanges.get(0).startDate.compareTo(dateFormat.parse("1795-02-15")) == 0);
        assertTrue(unmappedFilmRanges.get(0).endDate.compareTo(dateFormat.parse("1795-02-17")) == 0);
        
        assertEquals(testBatch.getBatchID() + "-2", unmappedFilmRanges.get(1).getFilmShortName());
        assertTrue(unmappedFilmRanges.get(1).startDate.compareTo(dateFormat.parse("1795-02-15")) == 0);
        assertTrue(unmappedFilmRanges.get(1).endDate.compareTo(dateFormat.parse("1795-02-17")) == 0);
        
        assertEquals(testBatch.getBatchID() + "-3", unmappedFilmRanges.get(2).getFilmShortName());
        assertTrue(unmappedFilmRanges.get(2).startDate.compareTo(dateFormat.parse("1795-01-17")) == 0);
        assertTrue(unmappedFilmRanges.get(2).endDate.compareTo(dateFormat.parse("1795-02-28")) == 0);
        
        assertEquals(testBatch.getBatchID() + "-4", unmappedFilmRanges.get(3).getFilmShortName());
        assertTrue(unmappedFilmRanges.get(3).startDate.compareTo(dateFormat.parse("1795-02-01")) == 0);
        assertTrue(unmappedFilmRanges.get(3).endDate.compareTo(dateFormat.parse("1795-02-28")) == 0);
        
        assertEquals(testBatch.getBatchID() + "-5", unmappedFilmRanges.get(4).getFilmShortName());
        assertTrue(unmappedFilmRanges.get(4).startDate.compareTo(dateFormat.parse("1795-01-01")) == 0);
        assertTrue(unmappedFilmRanges.get(4).endDate.compareTo(dateFormat.parse("1795-12-31")) == 0);
       
        verifyNoMoreInteractions(resultCollectorMock);
    }    
    

    private String createBatchXmlDoc(String batchID, String... filmNodes ) {
        StringBuilder xmlStructure = new StringBuilder(
                "<node name=\"B" + batchID + "\" shortName=\"B"+batchID+"-RT1\">\n");
        for (String editionDate : filmNodes) {
            xmlStructure.append(editionDate + "\n");
        }

        xmlStructure.append("</node>");
        return xmlStructure.toString();
    }

    private String createBatchStructureXml(String batchID, int filmCounter, String... editionDates ) {

        StringBuilder xmlStructure = new StringBuilder(
                        "    <node " +
                                "name=\"B" + batchID + "/400022028241-1\" " +
                                "shortName=\"" + batchID + "-"+filmCounter+"\">\n");
        for (String editionDate : editionDates) {
            xmlStructure.append("            " +
                    "<node name=\"B" + batchID + "/400022028241-" + filmCounter + "/" + editionDate + "\"" +
                    " shortName=\""+ editionDate +"\">");
            xmlStructure.append("</node>\n");
        }

        xmlStructure.append("    </node>");
        return xmlStructure.toString();
    }
}
