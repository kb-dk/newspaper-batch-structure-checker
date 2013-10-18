package dk.statsbiblioteket.newspaper.eventhandlers;

import org.testng.annotations.Test;

import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.AttributeParsingEvent;

import static org.mockito.Mockito.*;

public class ChecksumExistenceCheckerTest {
    @Test
    public void testValidCheckCheck() throws Exception {
        // Fixture setup
        ResultCollector resultCollector = mock(ResultCollector.class);
        AttributeParsingEvent validChecksumEvent = mock(AttributeParsingEvent.class);
        stub(validChecksumEvent.getChecksum()).toReturn("aa");

        //Fire stimuli
        ChecksumExistenceChecker checksumExistenceChecker = new ChecksumExistenceChecker(resultCollector);
        checksumExistenceChecker.handleAttribute(validChecksumEvent);

        //Validate
        verifyNoMoreInteractions(resultCollector);
    }

    @Test
    public void testNullCheckTest() throws Exception {
        // Fixture setup
        ResultCollector resultCollector = mock(ResultCollector.class);
        AttributeParsingEvent validChecksumEvent = mock(AttributeParsingEvent.class);
        stub(validChecksumEvent.getChecksum()).toReturn(null);

        //Fire stimuli
        ChecksumExistenceChecker checksumExistenceChecker = new ChecksumExistenceChecker(resultCollector);
        checksumExistenceChecker.handleAttribute(validChecksumEvent);

        //Validate
        verify(resultCollector).addFailure(anyString(), anyString(), anyString(), anyString());
        verifyNoMoreInteractions(resultCollector);
    }
}
