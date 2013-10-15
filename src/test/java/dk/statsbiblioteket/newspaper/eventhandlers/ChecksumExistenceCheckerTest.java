package dk.statsbiblioteket.newspaper.eventhandlers;

import dk.statsbiblioteket.autonomous.ResultCollector;
import dk.statsbiblioteket.doms.iterator.common.AttributeParsingEvent;
import org.testng.annotations.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.stub;
import static org.mockito.Mockito.verifyNoMoreInteractions;

public class ChecksumExistenceCheckerTest {
    @Test
    public void testValidCheckCheck() throws Exception {
        // Setup fixture
        ResultCollector resultCollector = mock(ResultCollector.class);
        AttributeParsingEvent validChecksumEvent = mock(AttributeParsingEvent.class);
        stub(validChecksumEvent.getChecksum()).toReturn("aa");

        ChecksumExistenceChecker checksumExistenceChecker = new ChecksumExistenceChecker(resultCollector);
        checksumExistenceChecker.handleAttribute(validChecksumEvent);
        verifyNoMoreInteractions(resultCollector);
    }
}
