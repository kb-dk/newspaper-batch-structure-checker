package dk.statsbiblioteket.newspaper.eventhandlers;

import org.testng.annotations.Test;

import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.AttributeParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.NodeBeginsParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.NodeEndParsingEvent;
import static org.mockito.Mockito.*;

public class NewspaperIDCheckerTest {
    
    private static final String VALID_NEWSPAPER_ID = "boersen";
    private static final String WORKSHIFT_ISO_TARGET_BRANCH = "WORKSHIFT-ISO-TARGET";
    private static final String FILM_BRANCH = "1234567890-12";
    private static final String BATCH_DIR = "B1234567890-RT1";
    
    /**
     * Tests that files in the WORKSHIFT-ISO-TARGET dir is allowed to be invalid 
     */
    @Test
    public void testValidCheckCheck() throws Exception {
        // Fixture setup
        ResultCollector resultCollector = mock(ResultCollector.class);
        String targetPath = BATCH_DIR + "/" + WORKSHIFT_ISO_TARGET_BRANCH; 
        String filmPath = BATCH_DIR + "/" + FILM_BRANCH;
        
        NodeBeginsParsingEvent targetNodeBegin = mock(NodeBeginsParsingEvent.class);
        stub(targetNodeBegin.getName()).toReturn(targetPath);
        
        NodeEndParsingEvent targetNodeEnd = mock(NodeEndParsingEvent.class);
        stub(targetNodeEnd.getName()).toReturn(targetPath);
        
        AttributeParsingEvent validTargetEvent = mock(AttributeParsingEvent.class);
        stub(validTargetEvent.getName()).toReturn(targetPath + "/Target-000387-0001.jp2");
        
        NodeBeginsParsingEvent filmNodeBegin = mock(NodeBeginsParsingEvent.class);
        stub(filmNodeBegin.getName()).toReturn(filmPath);
        
        NodeEndParsingEvent filmNodeEnd = mock(NodeEndParsingEvent.class);
        stub(filmNodeEnd.getName()).toReturn(filmPath);
        
        AttributeParsingEvent validAttributeEvent = mock(AttributeParsingEvent.class);
        stub(validAttributeEvent.getName()).toReturn(filmPath + "/" + VALID_NEWSPAPER_ID + "-" + FILM_BRANCH + "-ISO-1.jp2");
        
        NewspaperIDChecker checker = new NewspaperIDChecker(VALID_NEWSPAPER_ID, resultCollector);
        
        // Fire stimuli
        checker.handleNodeBegin(targetNodeBegin);
        checker.handleAttribute(validTargetEvent);
        checker.handleNodeEnd(targetNodeEnd);
        checker.handleNodeBegin(filmNodeBegin);
        checker.handleAttribute(validAttributeEvent);
        checker.handleNodeEnd(filmNodeEnd);
       
        //Validate
        verifyNoMoreInteractions(resultCollector);
    }

    /**
     * Checks that invalid filenames in a non-WORKSHIFT-ISO-TARGET is required to start with the valid newspaperID 
     */
    @Test
    public void testNullCheckTest() throws Exception {
        // Fixture setup
        ResultCollector resultCollector = mock(ResultCollector.class);
        String filmPath = BATCH_DIR + "/" + FILM_BRANCH;
        
        AttributeParsingEvent invalidTargetEvent = mock(AttributeParsingEvent.class);
        stub(invalidTargetEvent.getName()).toReturn(filmPath + "/Target-000387-0001.jp2");
        
        NodeBeginsParsingEvent filmNodeBegin = mock(NodeBeginsParsingEvent.class);
        stub(filmNodeBegin.getName()).toReturn(filmPath);
        
        NodeEndParsingEvent filmNodeEnd = mock(NodeEndParsingEvent.class);
        stub(filmNodeEnd.getName()).toReturn(filmPath);
        
        NewspaperIDChecker checker = new NewspaperIDChecker(VALID_NEWSPAPER_ID, resultCollector);

        // Fire stimuli
        checker.handleNodeBegin(filmNodeBegin);
        checker.handleAttribute(invalidTargetEvent);
        checker.handleNodeEnd(filmNodeEnd);

        //Validate
        verify(resultCollector).addFailure(anyString(), anyString(), anyString(), anyString());
        verifyNoMoreInteractions(resultCollector);
    }
}
